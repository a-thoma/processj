package rewriters;

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
import ast.Type;
import ast.Var;
import ast.WhileStat;
import utilities.Log;
import utilities.Visitor;

/**
 * Temporary dirty fix for unreachable code due to infinite loop.
 * 
 * @author Ben
 */
public class LoopRewrite extends Visitor<Object> {
    
    // The procedure currently being checked.
    private ProcTypeDecl currentProcedure = null;
    
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
    public Object visitWhileStat(WhileStat ws) {
        Log.log(ws, "Visiting a while statement");
        
        // Is this an infinite loop?
        if (ws.foreverLoop) {
            // Rewrite the procedure's body
            // Turn while (true) { ... }:
            // boolean temp = true;
            // while (temp) { ... }
            String temp = nextTemp();
            LocalDecl ld = new LocalDecl(
                    new PrimitiveType(PrimitiveType.BooleanKind),
                    new Var(new Name(temp), null),
                    false /* not constant */);
            // Replace the literal in the while
            NameExpr ne = new NameExpr(new Name(temp));
            ExprStat es = new ExprStat(new Assignment(ne, ws.expr(), Assignment.EQ));
            // This a quick dirty access!
            ws.children[0] = ne;
            // Append the new localDecl
            Sequence<Statement> stats = new Sequence<Statement>();
            stats.append(ld);
            stats.append(es);
            // Append remaining statements
            for (Statement st : currentProcedure.body().stats())
                stats.append(st);
            // Create a new block for the current procedure
            Block b = new Block(stats);
            // I'm ashamed of myself -- another quick dirty access
            currentProcedure.children[6] = b;
        }
        
        return null;
    }
    
    @Override
    public Object visitProcTypeDecl(ProcTypeDecl pd) {
        Log.log(pd, "Visiting a procedure type declaration (" + pd.name().getname() + ").");
        currentProcedure = pd;
        super.visitProcTypeDecl(pd);
        return null;
    }
}
