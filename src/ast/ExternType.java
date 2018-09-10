package ast;

import utilities.Visitor;

public class ExternType extends Type {

    public ExternType(Name name) {
        super(name);
        nchildren = 1;
        children = new AST[] { name };
    }

    public Name name() {
        return (Name) children[0];
    }

    public String typeName() {
        return "ExternType: " + name();
    }

    public String signature() {
        return "E" + name().getname() + ";";
    }

    public <S extends Object> S visit(Visitor<S> v) {
        return v.visitExternType(this);
    }

    public String toString() {
        return typeName();
    }
}