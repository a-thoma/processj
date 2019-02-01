package ast;

import utilities.Visitor;

public class NamedType extends Type implements DefineTopLevelDecl {

    private DefineTopLevelDecl resolvedTopLevelDecl = null; // could be a SymbolTable
    private Type type = null;

    public NamedType(Name name) {
        super(name);
        nchildren = 1;
        children = new AST[] { name };
    }

    public NamedType(Name name, Type type) {
        this(name);
        this.type = type;
        nchildren = 1;
        children = new AST[] { name };
    }

    public Name name() {
        return (Name) children[0];
    }

    public Type type() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String typeName() {
        return "NamedType: " + name();
    }

    public void setResolvedTopLevelDecl(DefineTopLevelDecl td) {
        this.resolvedTopLevelDecl = td;
    }

    public String toString() {
        return typeName();
    }

    public String signature() {
        return "L" + name().getname() + ";";
    }

    public <S extends Object> S visit(Visitor<S> v) {
        return v.visitNamedType(this);
    }

    @Override
    public boolean equal(Type t) {
        return false;
    }

    @Override
    public boolean equivalent(Type t) {
        return false;
    }

    @Override
    public boolean assignmentCompatible(Type t) {
        return false;
    }
}