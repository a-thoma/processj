package namechecker;

import java.util.Hashtable;

import ast.AST;
import ast.Compilation;
import ast.Import;
import ast.Pragma;
import ast.ProcTypeDecl;
import ast.Sequence;
import utilities.CompilerMessageManager;
import utilities.Log;
import utilities.Visitor;

public class ResolveProcTypeDecl<T extends AST> extends Visitor<T> {
    
    public Import currentImport = null;
    public static Hashtable<String, String> pragmaTable = new Hashtable<>();
    
    public ResolveProcTypeDecl() {
        Log.logHeader("==============================================================");
        Log.logHeader("*        R E S O L V E   N A T I V E   P R O C T Y P E       *");
        Log.logHeader("*       -----------------------------------------------      *");
        Log.logHeader("*       File: " + CompilerMessageManager.INSTANCE.fileName);
        Log.logHeader("==============================================================");
    }
    
    public T visitCompilation(Compilation co) {
        Log.log(" Finding native proc type declarations for " + CompilerMessageManager.INSTANCE.fileName
                + ".");
        Log.log(" Visiting type declarations for " + CompilerMessageManager.INSTANCE.fileName);
        co.imports().visit(this);

        return null;
    }
    
    public T visitPragma(Pragma pr) {
        String name = pr.value() != null ? pr.value() : "";
        Log.log(pr.line + ": Visiting an pragma " + pr.pname().getname() + " " + name);
        if (name.isEmpty())
            pragmaTable.put(pr.pname().getname(), pr.pname().getname());
        else {
            name = name.replace("\"", "");
            pragmaTable.put(pr.pname().getname(), name);
        }
        return null;
    }
    
    public T visitImport(Import im) {
        Log.log(im.line + ": Visiting an import (of file: " + im + ")");
        currentImport = im;
        Sequence<Compilation> compilations = im.getCompilations();
        for (Compilation c : compilations) {
            if (c != null) {
                c.pragmas().visit(this);
                c.typeDecls().visit(this);
            }
        }
        currentImport = null;
        
        return null;
    }
    
    public T visitProcTypeDecl(ProcTypeDecl pd) {
        Log.log(pd.line + ": Visiting a ProcTypeDecl (" + pd.name().getname() + ")");
        if (pragmaTable.size() > 0 && currentImport != null) {
            if (pragmaTable.contains("LIBRARY") && pragmaTable.contains("NATIVE")) {
                pd.isNative = true;
                pd.library = currentImport.toString();
                pd.filename = pragmaTable.get("FILE");
                pd.nativeFunction = pd.name().getname();
            }
            pragmaTable.clear();
        }
        return null;
    }
}
