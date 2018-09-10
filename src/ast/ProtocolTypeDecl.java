package ast;

import utilities.Visitor;

public class ProtocolTypeDecl extends Type implements TopLevelDecl {

    public ProtocolTypeDecl(Sequence<Modifier> modifiers, Name name,
                            Sequence<AST> extend, Annotations annotations,
                            Sequence<ProtocolCase> body) {
        super(name);
        nchildren = 5;
        children = new AST[] { modifiers, name, extend, annotations, body };
    }

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

    public String signature() {
        return "<P" + name().getname() + ";";
    }

    public String toString() {
        return typeName();
    }

    public String typeName() {
        return "Protocol: " + name();
    }

    public <S extends Object> S visit(Visitor<S> v) {
        return v.visitProtocolTypeDecl(this);
    }
}