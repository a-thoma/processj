package rewriters;

import ast.AST;
import ast.Assignment;
import ast.BinaryExpr;
import ast.Block;
import ast.ChannelReadExpr;
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
import utilities.Visitor;

/**
 * 
 * @author Ben
 */
public class LoopReadRewrite extends Visitor<AST> {
    
    private int tempCounter = 0;

    private String nextTemp() {
        return "readExprLoop" + tempCounter++ + "_";
    }
    
    @SuppressWarnings("unchecked")
    public void go(AST a, Sequence<Statement> stmts) {
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
                        go(stat, stmts);
                    } else if (stat instanceof WhileStat) {
                        WhileStat ws = (WhileStat) stat;
                        Sequence<Statement> newStmts = new Sequence<Statement>();
                        // Do we have a read expression?
                        if (ws.expr().doesYield())
                            go(ws.expr(), newStmts);
                        // Rewrite the expression in the while-loop
                        if (newStmts.size() > 0) {
                            LocalDecl ld = (LocalDecl) newStmts.child(0);
                            ws.children[0] = new NameExpr(new Name(ld.name()));
                            newStmts.append(stat);
                            // Rewrite the block in the while-loop
                            Block b = (Block) ws.stat();
                            b.stats().append(newStmts.child(1));
                            s.set(i, newStmts);
                        }
                        // Visit the while-loop's block.
                        go(ws.stat(), stmts);
                    } else if (stat instanceof ForStat) {
                        // TODO: 'ExprStat' in for-loop
                    }
                } else if (s.child(i) != null)
                    go(s.child(i), stmts);
            }
        } else if (a instanceof ChannelReadExpr) {
            if (stmts != null) {
                ChannelReadExpr cre = (ChannelReadExpr) a;
                // Rewrite the boolean literal.
                String temp = nextTemp();
                // Create a local variable for the boolean literal value in
                // the while-loop.
                LocalDecl ld = new LocalDecl(cre.type,
                        new Var(new Name(temp), null),
                        true /* constant */);
                ExprStat es = new ExprStat(new Assignment(new NameExpr(new Name(temp)),
                        (ChannelReadExpr) a, Assignment.EQ));
                stmts.append(ld);
                stmts.append(es);
            }
        } else {
            // Iterate through it's 'children' array.
            for (int i = 0; i < a.nchildren; ++i) {
                if (a.children[i] != null)
                    go(a.children[i], stmts);
            }
        }
    }

}
