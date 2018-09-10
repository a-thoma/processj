package ast;

import utilities.Visitor;

public class ErrorType extends Type {
    public static int errorCount = 0;

    public ErrorType() {
        super();
    }

    public String signature() {
        return "";
    }

    public String typeName() {
        return "Error type";
    }

    public String toString() {
        return "<Error>";
    }

    public <S extends Object> S visit(Visitor<S> v) {
        return v.visitErrorType(this);
    }
}