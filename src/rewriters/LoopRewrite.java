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
import utilities.Log;
import utilities.Visitor;

/**
 * Temporary dirty fix for unreachable code due to infinite loop.
 * 
 * For example:
 *                              boolean tempLoopX = true;
 *                              ...
 *      while(true) {           while (tempLoopX) {
 *          ...                     ...
 *      }                       }
 * 
 * @author Ben
 */
public class LoopRewrite extends Visitor<AST> {
    
    private int tempCounter = 0;

    private String nextTemp() {
        return "tempLoop" + tempCounter++;
    }
    
    public LoopRewrite() {
        Log.logHeader("****************************************");
        Log.logHeader("*        L O O P - R E W R I T E       *");
        Log.logHeader("****************************************");
    }
    
    @Override
    public AST visitBlock(Block bl) {
        Log.log(bl, "Visiting a Block");
        
        // TODO: Rewrite inner infinite loops??
        
        Sequence<Statement> stats = bl.stats();
        // LocalDecls of the while-loop currently being visited.
        Sequence<Statement> localStmts = new Sequence<>();
        for (int i = 0; i < stats.size(); ++i) {
            AST st = stats.child(i);
            // Is this an infinite loop?
            if (st instanceof WhileStat && ((WhileStat) st).foreverLoop) {
                WhileStat ws = (WhileStat) st.visit(this);
                // Create a local variable for the boolean literal in
                // the while-loop
                LocalDecl ld = new LocalDecl(
                        new PrimitiveType(PrimitiveType.BooleanKind),
                        new Var(new Name(((NameExpr) ws.expr()).name().getname()), null),
                        true /* constant */);
                localStmts.append(ld);
            }
        }
        
        if (localStmts.size() > 0) {
            // Combine the original statements of the current procedure
            // with the new one(s)
            Sequence<Statement> newStmts = new Sequence<>();
            newStmts.merge(localStmts);
            newStmts.merge(bl.stats());
            // Rewrite the procedure's body
            bl.children[0] = newStmts;
        }       
        return bl;
    }
    
    @Override
    public AST visitWhileStat(WhileStat ws) {
        Log.log(ws, "Rewriting the boolean literal in while-loop");
        
        // Rewrite the boolean literal
        String temp = nextTemp();
        // Replace the literal value in the while-loop with the
        // new local variable
        NameExpr ne = new NameExpr(new Name(temp));
        ExprStat es = new ExprStat(new Assignment(ne, ws.expr(), Assignment.EQ));
        // Rewrite the expression for the while-loop
        ws.children[0] = ne;
        ws.assignments = new Sequence<Statement>();
        ws.assignments.append(es);
        
        return ws;
    }
    
    @Override
    public AST visitProcTypeDecl(ProcTypeDecl pd) {
        Log.log(pd, "Visiting a procedure type declaration (" + pd.name().getname() + ").");
        super.visitProcTypeDecl(pd);
        return null;
    }
}
