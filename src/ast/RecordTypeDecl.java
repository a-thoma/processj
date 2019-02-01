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

    public Sequence<RecordMember> body() {
        return (Sequence<RecordMember>) children[4];
    }

    // *************************************************************************
    // ** Misc. Methods
    
    public RecordMember getMember(String name) {
        for (RecordMember rm : body())
            if (rm.name().getname().equals(name))
                return rm;
        return null;
    }

    public String toString() {
        return typeName();
    }

    // *************************************************************************
    // ** Visitor Related Methods

    public <S extends Object> S visit(Visitor<S> v) {
        return v.visitRecordTypeDecl(this);
    }

    // *************************************************************************
    // ** Type Related Methods
    
    public String signature() {
        return "<R" + name().getname() + ";";
    }

    public String typeName() {
        return "Record: " + name();
    }

    @Override public boolean isRecordType() {
	return true;
    }

    @Override public boolean typeEqual(Type t) {
        return false;
    }

    @Override public boolean typeEquivalent(Type t) {
        return false;
    }

    @Override public boolean typeAssignmentCompatible(Type t) {
        return false;
    }
}