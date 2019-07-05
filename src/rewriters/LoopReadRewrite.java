package rewriters;

import ast.AST;
import ast.Assignment;
import ast.BinaryExpr;
import ast.Block;
import ast.ChannelReadExpr;
import ast.DoStat;
import ast.ExprStat;
import ast.Expression;
import ast.ForStat;
import ast.Invocation;
import ast.LocalDecl;
import ast.Name;
import ast.NameExpr;
import ast.PrimitiveType;
import ast.ProcTypeDecl;
import ast.Sequence;
import ast.Statement;
import ast.Var;
import ast.WhileStat;

/**
 * 
 * @author Ben
 */
public class LoopReadRewrite {
    
    private int tempCounter = 0;

    private String nextTemp() {
        return "readExprLoop" + tempCounter++ + "$";
    }
    
    @SuppressWarnings("unchecked")
    public void go(AST a, Statement stmts) {
        if (a instanceof ProcTypeDecl) {
            // Rewrite the body if needed.
            ProcTypeDecl pd = (ProcTypeDecl) a;
            go(pd.body(), stmts);
        } else if (a instanceof Sequence) {
            Sequence<AST> s = (Sequence<AST>) a;
            // Iterate through all the nodes in the sequence.
            for (int i = 0; i < s.size(); ++i) {
                if (s.child(i) != null && s.child(i) instanceof Statement) {
                    Statement stat = (Statement) s.child(i);
                    if (stat instanceof Block) {
                        // Visit the statements in the block.
                        Block b = (Block) stat;
                        b.assignments = new Sequence<Statement>();
                        go(b, b);
                    } else if (stat instanceof WhileStat) {
                        WhileStat ws = (WhileStat) stat;
                        ws.assignments = new Sequence<Statement>();
                        // Visit the while-loop's block.
                        go(ws, ws);
                        // Do we have a read expression?
                        if (ws.expr().doesYield()) {
                            // Rewrite the block in the while-loop.
                            ws.assignments.append(stat);
                            Block b = (Block) ws.stat();
                            for (Statement expr : ws.assignments) {
                                if (expr instanceof ExprStat)
                                    b.stats().append((ExprStat) expr);
                            }
                            s.set(i, new Block(ws.assignments));
                        }
                    } else if (stat instanceof DoStat) {
                        DoStat ds = (DoStat) stat;
                        ds.assignments = new Sequence<Statement>();
                        // Visit the do-while's block.
                        go(ds, ds);
                        // Do we have a read expression?
                        if (ds.expr().doesYield()) {
                            // Only the expression in the do-while should be handled.
                            // Add locals for each channel read expression.
                            Sequence<Statement> newStmts = new Sequence<Statement>();
                            for (Statement expr : ds.assignments) {
                                if (expr instanceof LocalDecl)
                                    newStmts.append((LocalDecl) expr);
                            }
                            // Rewrite the block in the do-while.
                            newStmts.append(stat);
                            Block b = (Block) ds.stat();
                            for (Statement expr : ds.assignments) {
                                if (expr instanceof ExprStat)
                                    b.stats().append((ExprStat) expr);
                            }
                            s.set(i, new Block(newStmts));
                        }
                    } else if (stat instanceof ForStat) {
                        ForStat fs = (ForStat) stat;
                        fs.assignments = new Sequence<Statement>();
                        // New sequence of statements for: init(), expr(), incr(), and stats().
                        Sequence<Statement> forStats = new Sequence<Statement>();
                        // Check for channel read expressions in the declaration
                        // section of the loop.
                        go(fs.init(), fs);
                        // Add declarations for each channel read expression.
                        forStats.merge(fs.assignments);
                        fs.assignments = new Sequence<Statement>();
                        // Check for channel read expressions in the loop-continuation
                        // condition section of the loop.
                        Sequence<Statement> crt = null;
                        if (fs.expr().doesYield()) {
                            go(fs.expr(), fs);
                            crt = fs.assignments; // LocalDecls and ExprStat for this expr()
                            forStats.merge(fs.assignments);
                            fs.assignments = new Sequence<Statement>();
                        }
                        // Check for channel read expressions in the increment
                        // section of the loop.
                        go(fs.incr(), fs);
                        // Add local declarations for channel read expressions.
                        for (Statement expr : fs.assignments) {
                            if (expr instanceof LocalDecl)
                                forStats.append((LocalDecl) expr);
                        }
                        // Rewrite the block in the for-loop.
                        forStats.append(stat);
                        Block b = (Block) fs.stats();
                        // Add channel read expressions to the block.
                        for (Statement expr : fs.assignments) {
                            if (expr instanceof ExprStat)
                                b.stats().append((ExprStat) expr);
                        }
                        // Add channel read expressions from the loop-continuation
                        // condition to the block.
                        if (crt != null) {
                            for (Statement expr : crt) {
                                if (expr instanceof ExprStat)
                                    b.stats().append((ExprStat) expr);
                            }
                        }
                        s.set(i, new Block(forStats));
                        // Visit the for-loop's block.
                        go(fs.stats(), stmts);
                    } else if (stat instanceof LocalDecl) {
                        LocalDecl ld = (LocalDecl) stat;
                        if (ld.var().init() != null && ld.var().init().doesYield()) {
                            // Create a local variable for the channel read expression in the loop.
                            stmts.assignments.append(new LocalDecl(ld.type(), new Var(ld.var().name(), null),
                                    ld.isConst()));
                            Assignment as = new Assignment(new NameExpr(ld.var().name()), ld.var().init(),
                                    Assignment.EQ);
                            // Mark the assignment and expression as 'yield'.
                            as.setYield();
                            ExprStat es = new ExprStat(as);
                            es.setYield();
                            stmts.assignments.append(es);
                            // Remove the channel read expression from the i'th node
                            s.set(i, null);
                        }
                    } else if (stat instanceof ExprStat) {
                        ExprStat es = (ExprStat) stat;
                        es.assignments = new Sequence<Statement>();
                        if (es.doesYield()) {
                            go(es, es);
                            if (stmts != null && es.assignments.size() > 0)
                                stmts.assignments.merge(es.assignments);
                        }
                    }
                } else if (s.child(i) != null)
                    go(s.child(i), stmts);
            }
        } else {
            // Iterate through it's children array.
            for (int i = 0; i < a.nchildren; ++i) {
                if (a.children[i] != null && a.children[i] instanceof ChannelReadExpr) {
                    if (stmts != null) {
                        ChannelReadExpr cre = (ChannelReadExpr) a.children[i];
                        // Rewrite the boolean literal.
                        String temp = nextTemp();
                        // Create a local variable for the boolean literal value in
                        // the while-loop.
                        LocalDecl ld = new LocalDecl(cre.type, new Var(new Name(temp), null),
                                true /* constant */);
                        ExprStat es = new ExprStat(new Assignment(new NameExpr(new Name(temp)),
                                cre, Assignment.EQ));
                        // Add the rewrites to the statement.
                        stmts.assignments.append(ld);
                        stmts.assignments.append(es);
                        // Mark the name expression as 'yield'.
                        NameExpr ne = new NameExpr(new Name(temp));
                        ne.setYield();
                        // Rewrite the channel read expression.
                        a.children[i] = ne;
                    }
                } else if (a.children[i] != null)
                    go(a.children[i], stmts);
            }
        }
    }

}
