package namechecker;

import java.util.Hashtable;

import ast.Compilation;
import ast.Import;
import ast.Pragma;
import ast.ProcTypeDecl;
import ast.Type;
import utilities.Log;
import utilities.Visitor;

/**
 * Visitors used for marking top-level declarations as 'native'.
 *
 * @param <T>
 *          The visitor interface used to traverse and resolve the
 *          type in an import statement.
 * 
 * @author Ben
 * @version 01/31/2019
 * @since 1.2
 */
public class ResolveImportTopLevelDecls extends Visitor<Object> {
    
    // The import currently being resolved.
    public Import currentImport = null;
    
    public static Hashtable<String, String> pt = new Hashtable<String, String>();
    
    public ResolveImportTopLevelDecls() {
        Log.logHeader("****************************************");
        Log.logHeader("*     R E S O L V E   N A T I V E      *");
        Log.logHeader("*    T O P   L E V E L   D E C L S     *");
        Log.logHeader("****************************************");
    }
    
    @Override
    public Object visitCompilation(Compilation co) {
        Log.log("Finding native top type declarations");
        Log.log("Visiting type declarations");
        co.imports().visit(this);
        return null;
    }
    
    @Override
    public Object visitPragma(Pragma pr) {
        String val = pr.value() != null ? pr.value() : "";
        Log.log(pr, "Visiting an pragma " + pr.pname().getname() + " " + val);
        if (val.isEmpty())
            pt.put(pr.pname().getname(), pr.pname().getname());
        else
            pt.put(pr.pname().getname(), val.replace("\"", ""));
        return null;
    }
    
    @Override
    public Object visitImport(Import im) {
        Log.log(im, "Visiting an import (of file: " + im + ")");
        Import prevImport = currentImport;
        currentImport = im;
        // For every top-level declaration in the given file, determine if this
        // declaration is part of a ProcessJ native library.
        for (Compilation c : im.getCompilations()) {
            if (c == null) continue;
            // Visit each pragma and check if they are part of a native
            // library function.
            for (Pragma p : c.pragmas())
                p.visit(this);
            // Mark all top-level declarations 'native' if they are part of
            // a native library function.
            for (Type t : c.typeDecls())
                t.visit(this);
        }
        // For now, resolve any updates here.
        pt.clear();
        currentImport = prevImport;
        return null;
    }
    
    @Override
    public Object visitProcTypeDecl(ProcTypeDecl pd) {
        Log.log(pd, "Visiting a ProcTypeDecl (" + pd.name().getname() + " " + pd.signature() + ")");
        if (!pt.isEmpty() && currentImport != null) {
            String path = ResolveImports.makeImportPath(currentImport);
            Log.log(pd, "Package path: " + path);
            if (pt.contains("LIBRARY") && pt.contains("NATIVE")) {
                Log.log(pd, "Package file name: " + currentImport.file().getname());
                pd.isNative = true;
                pd.library = currentImport.toString();
                pd.filename = pt.get("FILE");
                pd.nativeFunction = pd.name().getname();
            } else
                ; // Non-native procedure found.
        }
        return null;
    }
}
