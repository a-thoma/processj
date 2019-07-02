package rewriters;

import ast.AST;
import ast.Assignment;
import ast.Block;
import ast.ExprStat;
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
 * Temporary dirty fix for unreachable code due to infinite loop.
 * 
 * For example:
 *                                  boolean foreverLoopX = true;
 *                                  ...
 *      while (true) {              while (foreverLoopX) {
 *          ...                         ...
 *      }                           }
 *
 * or
 *                                  boolean foreverLoopX = true;
 *                                  boolean foreverLoopY = true;
 *                                  ...
 *      while (true) {              while (foreverLoopX) {
 *          while (true) {              while (foreverLoopY) {
 *              ...                         ...
 *          }                           }
 *      }                           }
 * 
 * 
 * @author Ben
 */
public class ForeverLoopRewrite {
    
    private int tempCounter = 0;

    private String nextTemp() {
        return "foreverLoop" + tempCounter++ + "_";
    }
    
    @SuppressWarnings("unchecked")
    public void go(AST a) {
        if (a instanceof ProcTypeDecl) {
            // Rewrite the body if needed.
            ProcTypeDecl pd = (ProcTypeDecl) a;
            go(pd.body());
        } else if (a instanceof Sequence) {
            Sequence<AST> s = (Sequence<AST>) a;
            // Iterate through all the nodes in the sequence.
            for (int i = 0; i < s.size(); ++i) {
                if (s.child(i) != null && s.child(i) instanceof Statement) {
                    Statement stat = (Statement) s.child(i);
                    if (stat instanceof Block) {
                        // Visit the statements in the block.
                        go(stat);
                    } else if (stat instanceof WhileStat && ((WhileStat) stat).foreverLoop) {
                        WhileStat ws = (WhileStat) stat;
                        // Rewrite the boolean literal.
                        String temp = nextTemp();
                        // Create a local variable for the boolean literal value in
                        // the while-loop.
                        LocalDecl ld = new LocalDecl(
                                new PrimitiveType(PrimitiveType.BooleanKind),
                                new Var(new Name(temp), null),
                                true /* constant */);
                        // Replace the boolean literal value in the while-loop with the
                        // new local variable.
                        NameExpr ne = new NameExpr(new Name(temp));
                        ExprStat es = new ExprStat(new Assignment(ne, ws.expr(), Assignment.EQ));
                        // Rewrite the expression for the while-loop.
                        ws.children[0] = ne;
                        // Rewrite the i'th sequence of statements.
                        Sequence<Statement> stmts = new Sequence<Statement>();
                        stmts.append(ld);
                        stmts.append(es);
                        stmts.append(stat);
                        s.set(i, stmts);
                        // Visit the while-loop's block.
                        go(ws.stat());
                    }
                } else if (s.child(i) != null)
                    go(s.child(i));
            }
        } else {
            // Iterate through it's 'children' array.
            for (int i = 0; i < a.nchildren; ++i) {
                if (a.children[i] != null)
                    go(a.children[i]);
            }
        }
    }
}
