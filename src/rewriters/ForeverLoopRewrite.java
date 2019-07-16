package rewriters;

import ast.AST;
import ast.Assignment;
import ast.Block;
import ast.DoStat;
import ast.ExprStat;
import ast.Expression;
import ast.ForStat;
import ast.LocalDecl;
import ast.Name;
import ast.NameExpr;
import ast.PrimitiveLiteral;
import ast.PrimitiveType;
import ast.ProcTypeDecl;
import ast.Sequence;
import ast.Statement;
import ast.Token;
import ast.Var;
import ast.WhileStat;

/**
 * Temporary (silly) fixed for unreachable code due to infinite loop.
 * 
 * @author Ben
 */
public class ForeverLoopRewrite {
    
    private int tempCounter = 0;

    private String nextTemp() {
        return "foreverLoop" + tempCounter++;
    }
    
    /**
     * This tree-traversal method rewrites 'WhileStat', 'DoStat', and 'ForStat'
     * parse-tree nodes when the code represented by each of these nodes cannot
     * run to completion due to infinite loop.
     * 
     * 1.) While-loop rewrite:
     *                                                  boolean foreverLoop0 = true;
     *      while (true) {                              while (foreverLoop0) {
     *          while (true) {          becomes             boolean foreverLoop1 = true;
     *              ...                                     while (foreverLoop1) {
     *          }                                               ...
     *      }                                               }
     *                                                  }
     * 
     * 2.) Do-while loop rewrite:
     *                                                  boolean foreverLoop0 = true;
     *      do {                                        do {
     *          do {                                        boolean foreverLoop1 = true;
     *          ...                                         do {
     *          } while (true)                                  ...
     *      } while (true);                                 } while (foreverLoop1);
     *                                                  } while (foreverLoop0);
     * 
     * 3.) For-loop rewrite:
     *                                                  boolean foreverLoop0 = true;
     *      for (...; ...; ...;) {                      for (...; foreverLoop0; ...) {
     *          for (...; ...; ...;) {                      boolean foreverLoop1 = true;
     *              ...                                     for (...; foreverLoop1; ...) {
     *          }                                               ...
     *      }                                               }
     *                                                  }
     */
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
                    if (stat instanceof WhileStat) { // WhileStat.
                        WhileStat ws = (WhileStat) stat;
                        if (ws.foreverLoop) {
                            String temp = nextTemp();
                            // Create a local declaration to replace the boolean literal value with in the while-loop.
                            LocalDecl ld = new LocalDecl(new PrimitiveType(PrimitiveType.BooleanKind),
                                    new Var(new Name(temp), null), true /* constant */);
                            // Replace the boolean literal value with the new local variable.
                            NameExpr ne = new NameExpr(new Name(temp));
                            ExprStat es = new ExprStat(new Assignment(ne, ws.expr(), Assignment.EQ));
                            // Rewrite the expression for the while-loop.
                            ws.children[0] = ne;
                            // Rewrite the i'th sequence of statements.
                            Sequence<Statement> stats = new Sequence<Statement>();
                            stats.append(ld);
                            stats.append(es);
                            stats.append(stat);
                            Block b = new Block(stats);
                            s.set(i, b);
                        }
                        go(ws.stat());
                    } else if (stat instanceof DoStat) { // DoStat.
                        DoStat ds = (DoStat) stat;
                        if (ds.foreverLoop) {
                            String temp = nextTemp();
                            // Create a local declaration to replace the boolean literal value with in the do-while loop.
                            LocalDecl ld = new LocalDecl(new PrimitiveType(PrimitiveType.BooleanKind),
                                    new Var(new Name(temp), null), true /* constant */);
                            // Replace the boolean literal value with the new local variable.
                            NameExpr ne = new NameExpr(new Name(temp));
                            ExprStat es = new ExprStat(new Assignment(ne, ds.expr(), Assignment.EQ));
                            // Rewrite the expression for the do-while loop.
                            ds.children[1] = ne;
                            // Rewrite the i'th sequence of statements.
                            Sequence<Statement> stats = new Sequence<Statement>();
                            stats.append(ld);
                            stats.append(es);
                            stats.append(stat);
                            Block b = new Block(stats);
                            s.set(i, b);
                        }
                        go(ds.stat());
                    } else if (stat instanceof ForStat) { // ForStat.
                        ForStat fs = (ForStat) stat;
                        if (fs.foreverLoop) {
                            String temp = nextTemp();
                            // Create a local declaration to replace the boolean literal value with in the for-loop.
                            LocalDecl ld = new LocalDecl(new PrimitiveType(PrimitiveType.BooleanKind),
                                    new Var(new Name(temp), null), true /* constant */);
                            // Rewrite the expression if it is not of the form: for (...; true ; ...) { ... }
                            Expression newExpr = fs.expr();
                            if (newExpr == null)
                                newExpr = new PrimitiveLiteral(new Token(0, Boolean.toString(true), 0, 0, 0), 0 /* kind */);
                            // Replace the boolean literal value with the new local variable.
                            NameExpr ne = new NameExpr(new Name(temp));
                            ExprStat es = new ExprStat(new Assignment(ne, newExpr, Assignment.EQ));
                            // Rewrite the expression for the for-loop.
                            fs.children[1] = ne;
                            // Rewrite the i'th sequence of statements.
                            Sequence<Statement> stats = new Sequence<Statement>();
                            stats.append(ld);
                            stats.append(es);
                            stats.append(stat);
                            Block b = new Block(stats);
                            s.set(i, b);
                        }
                        go(fs.stats());
                    } else if (s.child(i) != null) // Block, IfStat, ParBlock, SwitchStat.
                        go(s.child(i));
                } else if (s.child(i) != null)
                    go(s.child(i));
            }
        } else {
            // Iterate through it's children array.
            for (int i = 0; i < a.nchildren; ++i) {
                if (a.children[i] != null)
                    go(a.children[i]);
            }
        }
    }
}
