package ast;

import utilities.Visitor;

public class RecordTypeDecl extends Type implements DefineTopLevelDecl {

    public RecordTypeDecl(Sequence<Modifier> modifiers, Name name,
                          Sequence<AST> extend, Annotations annotations,
                          Sequence<RecordMember> body) {
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

    public Sequence<RecordMember> body() {
        return (Sequence<RecordMember>) children[4];
    }

    public RecordMember getMember(String name) {
        for (RecordMember rm : body())
            if (rm.name().getname().equals(name))
                return rm;
        return null;
    }

    public String toString() {
        return typeName();
    }

    public String signature() {
        return "<R" + name().getname() + ";";
    }

    public String typeName() {
        return "Record: " + name();
    }

    public <S extends Object> S visit(Visitor<S> v) {
        return v.visitRecordTypeDecl(this);
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