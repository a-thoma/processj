package ast;

import utilities.Visitor;

public class ProtocolTypeDecl extends Type implements DefineTopLevelDecl {

    public ProtocolTypeDecl(Sequence<Modifier> modifiers, Name name,
                            Sequence<AST> extend, Annotations annotations,
                            Sequence<ProtocolCase> body) {
        super(name);
        nchildren = 5;
        children = new AST[] { modifiers, name, extend, annotations, body };
    }

    // *************************************************************************
    // ** Accessor Methods

    public Sequence<Modifier> modifiers() {
        return (Sequence<Modifier>) children[0];
    }

    public Name name() {
        return (Name) children[1];
    }

    public Sequence<Name> extend() {
        return (Sequence<Name>) children[2];
    }

    public Annotations annotations() {
        return (Annotations) children[3];
    }

    public Sequence<ProtocolCase> body() {
        return (Sequence<ProtocolCase>) children[4];
    }


    // *************************************************************************
    // ** Misc. Methods
 
    public String toString() {
        return typeName();
    }

    // *************************************************************************
    // ** Visitor Related Methods

    public <S extends Object> S visit(Visitor<S> v) {
        return v.visitProtocolTypeDecl(this);
    }

    // *************************************************************************
    // ** Type Related Methods

    public String signature() {
        return "<P" + name().getname() + ";";
    }

    public String typeName() {
        return "Protocol: " + name();
    }

    @Override public boolean isProtocolType() {
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

    @Override
    public String defaultType() {
        return null;
    }
}