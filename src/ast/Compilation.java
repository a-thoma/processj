package ast;

import utilities.Visitor;

public class Compilation extends AST {
    
    public boolean visited = false;
    public String sourceFile = "";    // The name of the file that caused this compilation to exist. Read by the ProcTypeDel.
    public String path = "";          // Absolute path to where the file is located.
    public boolean isImport = false;  // Is it an import?
    public String packageName = "";   // Name of the package.
    
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