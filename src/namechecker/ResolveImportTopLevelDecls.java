package namechecker;

import java.util.Hashtable;

import ast.AST;
import ast.Compilation;
import ast.Import;
import ast.Pragma;
import ast.ProcTypeDecl;
import ast.Sequence;
import ast.Type;
import utilities.Log;
import utilities.Visitor;

/**
 * Visitors used for marking top-level decls as 'native'.
 *
 * @param <T>
 *          The visitor interface used to traverse and resolve the
 *          type in an 'import' statement.
 * 
 * @author Ben
 * @version 01/31/2019
 * @since 1.2
 */
public class ResolveImportTopLevelDecls<T extends AST> extends Visitor<T> {
    
    // The import currently being resolved.
    public Import currentImport = null;
    public static Hashtable<String, String> pragmatable = new Hashtable<>();
    
    public ResolveImportTopLevelDecls() {
        Log.logHeader("****************************************");
        Log.logHeader("*     R E S O L V E   N A T I V E      *");
        Log.logHeader("*    T O P   L E V E L   D E C L S     *");
        Log.logHeader("****************************************");
    }
    
    @Override
    public T visitCompilation(Compilation co) {
        Log.log("Finding native top type declarations");
        Log.log("Visiting type declarations");
        co.imports().visit(this);
        return null;
    }
    
    @Override
    public T visitPragma(Pragma pr) {
        String val = pr.value() != null ? pr.value() : "";
        Log.log(pr, "Visiting an pragma " + pr.pname().getname() + " " + val);
        if (val.isEmpty())
            pragmatable.put(pr.pname().getname(), pr.pname().getname());
        else
            pragmatable.put(pr.pname().getname(), val.replace("\"", ""));
        return null;
    }
    
    @Override
    public T visitImport(Import im) {
        Log.log(im, "Visiting an import (of file: " + im + ")");
        Import prevImport = currentImport;
        currentImport = im;
        // For every top-level declaration in a file, determine if this
        // declaration is part of a ProcessJ native library
        for (Compilation c : im.getCompilations()) {
            if (c == null)
                continue;
            // Visit the fields associated with pragma values to check
            // if they are part of a native library function
            for (Pragma p : c.pragmas())
                p.visit(this);
            // Mark all top-level decls _native_ if they are part of
            // a ProcessJ native library
            for (Type t : c.typeDecls())
                t.visit(this);
            // TODO: the 'pragmatable' may need to be updated here due to
            // compilations that this import perform 
        }
        // For now, resolve any updates here
        pragmatable.clear();
        currentImport = prevImport;
        return null;
    }
    
    @Override
    public T visitProcTypeDecl(ProcTypeDecl pd) {
        Log.log(pd, "Visiting a ProcTypeDecl (" + pd.name().getname() + " " + pd.signature() + ")");
        if (!pragmatable.isEmpty() && currentImport != null) {
            String path = ResolveImports.makeImportPath(currentImport);
            Log.log(pd, "Package path: " + path);
            if (pragmatable.contains("LIBRARY") && pragmatable.contains("NATIVE")) {
                Log.log(pd, "Package file name: " + currentImport.file().getname());
                pd.isNative = true;
                pd.library = currentImport.toString();
                pd.filename = pragmatable.get("FILE");
                pd.nativeFunction = pd.name().getname();
            } else
                ; // Non-native procedure found
        }
        return null;
    }
}
