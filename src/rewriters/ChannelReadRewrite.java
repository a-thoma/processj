package rewriters;

import ast.*;
import utilities.Visitor;
import printers.*;
import utilities.ProcessJMessage;
import utilities.CompilerMessageManager;
import utilities.MessageType;
import utilities.VisitorMessageNumber;

/**
 * The purpose of this rewriter is to lift yielding Channel Read Expressions out of other expressions. For example:
 *
 * x = c.read() + 2;
 *
 * causes a problem because the code generated for the 'c.read' expression yields. This is solved by turning all 
 * channel read expressions into simple assignments with simple (NameExpr) target channels (the channel expression
 * from which the read happens. The above example generates:
 *
 * temp_0 = c.read();
 * x = temp_0 + 2;
 *
 * Errors generated in this file:
 *
 * 900 - Precondition in alt statement cannot contain channel reads 
 */
public class ChannelReadRewrite {
    private int tempCounter = 0;

    private String nextTemp() {
        return "temp" + tempCounter++;
    }

    /**
     * flatten will merge blocks at one level deeper up one level.
     *
     * For example:
     *
     * {                     {
     *    {
     *      ...                 ...
     *    }
     *              becomes
     *    {
     *      ...                 ...
     *    }
     * }                      }
    */
    public void flatten(AST a) {
        if (a instanceof Block) {
            Block block = (Block) a;
            Sequence<Statement> stmts = block.stats();
            Sequence<Statement> newStmts = new Sequence<Statement>();
            for (int i = 0; i < stmts.size(); i++) {
                Statement st = stmts.child(i);
                flatten(stmts.child(i));
                if (st instanceof Block && ((Block) st).canBeMerged)
                    newStmts.merge(((Block) st).stats());
                else
                    newStmts.append(st);
            }
            block.children[0] = newStmts;
        } else {
            if (a != null)
                for (int i = 0; i < a.nchildren; i++) {
                    flatten(a.children[i]);
                }
        }
    }

    /**
     * This parse-tree traversal method rewrites the parse tree by lifting out channel read expressions
     * from other expressions and statements such that _all_ channel reads will appear as:
     *
     * temp_i = c.read();
     *
     * If 'a' is a ProcTypeDecl we rewrite it's body and flatten it.
     * If 'a' is a Sequence we process each member in the following way:
     *   If it is a statement attach to that statement an empty sequence of statements called 'assignments'.
     *   Then call recurisvly on the statement. If and when a channel real is encountered further down the tree,
     *   a temporary variable 'temp_i' can be generated and 'temp_i = <channel read expression>' can be added to 
     *   the statements 'assignments' sequence, and the <channel read expression> can be replaced by 'tamp_i'.
     *   Each type of statement is handled slightly differently.
     *   If the node is a not a statment but a channel read expression, we handle it as just described.
     *   Upon returning from a recursive call, the statement replaces itself with a new Block in which the
     *   generated assignmes followed by the original statement have been placed.
     *
     * If 'a' is not a Sequence, then travers the children of it by iterating through its 'children' array.
    */
    public void go(AST a, Statement currentStatement) {
        if (a instanceof ProcTypeDecl) {
            // Rewrite the body.
            ProcTypeDecl pd = (ProcTypeDecl) a;
            go(pd.body(), currentStatement);
            // Now flatten it.
            flatten(pd);
        } else if (a instanceof Sequence) {
            // The node represents a Sequence.
            Sequence<AST> s = (Sequence<AST>) a;
            // Iterate through all the members of the sequence.
            for (int i = 0; i < s.size(); i++) {
                // Is the i'th node a statement?
                if (s.child(i) != null && s.child(i) instanceof Statement) {
                    Statement st = (Statement) s.child(i);
                    if (st instanceof AltStat) { // AltStat
                        // Alt Stats cannot have channel reads in the precondition.
                        AltStat as = (AltStat) st;
                        for (int j = 0; j < as.body().size(); j++) {
                            AltCase ac = (AltCase) as.body().child(j);
                            if (ac.precondition() != null && ac.precondition().doesYield())
                                CompilerMessageManager.INSTANCE.reportMessage(new ProcessJMessage.Builder()
                                                                .addAST(ac.precondition())
                                                                .addError(VisitorMessageNumber.REWRITE_900).build(),
                                                                MessageType.PRINT_CONTINUE);
                            ;// TODO: Error.addError("Something here");
                        }
                    } else if (st instanceof Block || st instanceof ChannelWriteStat || st instanceof IfStat
                            || st instanceof ParBlock || st instanceof ReturnStat || st instanceof SyncStat
                            || st instanceof TimeoutStat) {
                        // Block, ChannelWriteStat, IfStat, ParBlock, ReturnStat, SyncStat, Timeout
                        st.assignments = new Sequence<Statement>();
                        go(st, st);
                    } else if (st instanceof BreakStat || st instanceof ContinueStat || st instanceof SkipStat
                            || st instanceof StopStat || st instanceof SuspendStat) {
                        // BreakStat, ContinueStat, SkipStat, StopStat, SuspendStat
                        // Nothing to do.
                    } else if (st instanceof ExprStat) { // ExprStat
                        ExprStat es = (ExprStat) st;
                        es.assignments = new Sequence<Statement>();
                        // Don't bother with simple v = c.read();
                        if (es.doesYield()) {
                            if (es.expr() instanceof Assignment) {
                                Assignment as = (Assignment) es.expr();
                                if (as.right() instanceof ChannelReadExpr) {
                                    ChannelReadExpr cre = (ChannelReadExpr) as.right();
                                    if (!cre.channel().doesYield()) {
                                        // Don't bother with simple assignments of reads.
                                        continue;
                                    } else
                                        go(cre, es);
                                }
                            }
                            go(es, es);
                        }
                    } else if (st instanceof LocalDecl) { // LocalDecl
                        // Turn T v = expr int:
                        // T v;
                        // v = expr; and rewrite expr.
                        LocalDecl ld = (LocalDecl) st;
                        if (ld.var().init() != null && ld.var().init().doesYield()) {
                            Sequence<Statement> stats = new Sequence<Statement>();
                            // Create and append T v;
                            stats.append(new LocalDecl(ld.type(), new Var(ld.var().name(), null), ld.isConst()));
                            // Create and append v = expr;
                            Assignment as = new Assignment(new NameExpr(ld.var().name()), ld.var().init(),
                                    Assignment.EQ);
                            as.setYield();
                            ExprStat es = new ExprStat(as);
                            es.setYield();
                            stats = stats.append(es);
                            Block b = new Block(stats);
                            b.canBeMerged = true;
                            b.assignments = new Sequence<Statement>();
                            go(b, b);
                            s.set(i, b);
                        }
                    } else if (st instanceof SwitchStat) { // SwithStat
                        // Only the expression in the switch should be handled.
                        st.assignments = new Sequence<Statement>();
                        SwitchStat ss = (SwitchStat) st;
                        go(ss, ss);
                    }
                    if (st.assignments != null && st.assignments.size() > 0) {
                        st.assignments.append(st);
                        Block b = new Block(st.assignments);
                        b.canBeMerged = true;
                        s.set(i, b);
                        s.child(i).visit(new printers.PrettyPrinter<AST>());
                    }
                } else {
                    if (s.child(i) instanceof ChannelReadExpr) {
                        if (currentStatement != null) {
                            ChannelReadExpr cre = (ChannelReadExpr) s.child(i);
                            Type type = cre.type;
                            String temp = nextTemp();
                            // Rewrite the Channel Read Expression if needed.
                            go(s.child(i), currentStatement);

                            LocalDecl ld = new LocalDecl(type, new Var(new Name(temp), null), false /* not constant */);
                            ExprStat es = new ExprStat(new Assignment(new NameExpr(new Name(temp)),
                                    (ChannelReadExpr) s.child(i), Assignment.EQ));
                            currentStatement.assignments.append(ld);
                            currentStatement.assignments.append(es);
                            // Replace the channel read expression with the temp name expression.
                            s.set(i, new NameExpr(new Name(temp)));
                        }
                    } else if (s.child(i) != null)
                        go(s.child(i), currentStatement);
                }
            }
        } else {
            for (int i = 0; i < a.nchildren; i++) {
                if (a.children[i] != null && a.children[i] instanceof ChannelReadExpr && currentStatement != null) {
                    // If currentExprStat is not null, then we were called from a read that yields.
                    if (currentStatement != null) {
                        ChannelReadExpr cre = (ChannelReadExpr) a.children[i];
                        // Rewrite the channel read expression if needed.
                        go(a.children[i], currentStatement);

                        Type type = cre.type;
                        String temp = nextTemp();
                        LocalDecl ld = new LocalDecl(type, new Var(new Name(temp), null), false /* not constant */);
                        ExprStat es = new ExprStat(new Assignment(new NameExpr(new Name(temp)),
                                (ChannelReadExpr) a.children[i], Assignment.EQ));
                        currentStatement.assignments.append(ld);
                        currentStatement.assignments.append(es);
                        // Replace the channel read expression with the temp name expression.
                        a.children[i] = new NameExpr(new Name(temp));
                    }
                } else {
                    if (a.children[i] != null)
                        go(a.children[i], currentStatement);
                }
            }
        }
    }
}
