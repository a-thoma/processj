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
                            System.out.println("Yields!!!");
                            // Rewrite the block in the while-loop.
                            ws.assignments.append(stat);
                            Block b = (Block) ws.stat();
                            for (AST expr : ws.assignments) {
                                if (expr instanceof ExprStat)
                                    b.stats().append((ExprStat) expr);
                            }
                            s.set(i, new Block(ws.assignments));
                        }
                    } else if (stat instanceof ForStat) {
                        // TODO: 'ExprStat' in for-loop.
                    }
                } else if (s.child(i) != null)
                    go(s.child(i), stmts);
            }
        } else {
//            System.out.println("Maybe");
            // Iterate through it's 'children' array.
            for (int i = 0; i < a.nchildren; ++i) {
//                System.out.println("Ok");
                if (a.children[i] != null && a.children[i] instanceof ChannelReadExpr) {
//                    System.out.println("Maybe");
                    if (stmts != null) {
                        ChannelReadExpr cre = (ChannelReadExpr) a.children[i];
                        // Rewrite the boolean literal.
                        String temp = nextTemp();
                        // Create a local variable for the boolean literal value in
                        // the while-loop.
                        LocalDecl ld = new LocalDecl(cre.type, new Var(new Name(temp), null), true /* constant */);
                        ExprStat es = new ExprStat(new Assignment(new NameExpr(new Name(temp)), cre, Assignment.EQ));
                        // Add the rewrites to the statement.
                        stmts.assignments.append(ld);
                        stmts.assignments.append(es);
                        // Mark the name expression as a 'yield expression'.
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
