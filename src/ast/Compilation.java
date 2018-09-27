package ast;

import utilities.Visitor;

public class Compilation extends AST {
    
    public boolean visited = false;
    
    public Compilation(Sequence<Pragma> pragmas,
                       Sequence<Name> packageName,
                       Sequence<Import> imports,
                       Sequence<Type> typeDecls) {
        super(typeDecls);
        nchildren = 4;
        children = new AST[] { pragmas, packageName, imports, typeDecls };
    }

    public Sequence<Pragma> pragmas() {
        return (Sequence<Pragma>) children[0];
    }

    public Sequence<Name> packageName() {
        return (Sequence<Name>) children[1];
    }

    public Sequence<Import> imports() {
        return (Sequence<Import>) children[2];
    }

    public Sequence<Type> typeDecls() {
        return (Sequence<Type>) children[3];
    }

    public <S extends Object> S visit(Visitor<S> v) {
        return v.visitCompilation(this);
    }
}