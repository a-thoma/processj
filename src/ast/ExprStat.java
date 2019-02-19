package ast;

import utilities.Visitor;

public class ExprStat extends Statement {
    
    // This sequenec is used in the rewriting phase.
    // It holds Declarations and Assignments of the form:
    //
    // T temp_1;
    // temp_1 = c.read();
    public Sequence<Statement> assignments = null;

    public ExprStat(Expression expr) {
        super(expr);
        nchildren = 1;
        children = new AST[] { expr };
    }

    public Expression expr() {
        return (Expression) children[0];
    }

    public <S extends Object> S visit(Visitor<S> v) {
        return v.visitExprStat(this);
    }
}