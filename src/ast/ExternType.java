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

    // ********************
    // Type Related Stuff
    // ********************

    @Override public boolean isExternType() {
	return true;
    }

    // TODO
    @Override public boolean typeEqual(Type t) {
        return false;
    }

    // TODO
    @Override public boolean typeEquivalent(Type t) {
        return false;
    }

    // TODO
    @Override public boolean typeAssignmentCompatible(Type t) {
        return false;
    }
}