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
 *          The visitor interface used to traverse and resolve
 *          the type in an import statement.
 * 
 * @author Ben
 * @version 01/31/2019
 * @since 1.2
 */
public class ResolveImportTopDecls extends Visitor<Object> {
    
    /* The import currently being resolved */
    public Import curImport = null; 
    public static Hashtable<String, String> ht = new Hashtable<String, String>();
    
    public ResolveImportTopDecls() {
        Log.logHeader("*******************************************");
        Log.logHeader("* R E S O L V E   N A T I V E   D E C L S *");
        Log.logHeader("*******************************************");
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
        String str = pr.value() != null ? pr.value() : "";
        Log.log(pr, "Visiting an pragma " + pr.pname().getname() + " " + str);
        if (str.isEmpty())
            ht.put(pr.pname().getname(), pr.pname().getname());
        else
            ht.put(pr.pname().getname(), str.replace("\"", ""));
        return null;
    }
    
    @Override
    public Object visitImport(Import im) {
        Log.log(im, "Visiting an import (of file: " + im + ")");
        Import prevImport = curImport;
        curImport = im;
        /* For every top-level declaration in the given file, determine
         * if this declaration is part of a ProcessJ native library */
        for (Compilation c : im.getCompilations()) {
            if (c == null)
                continue;
            /* Visit each pragma (if any) and check if they are part of
             * a native library function */
            for (Pragma p : c.pragmas())
                p.visit(this);
            /* Mark all top-level declarations 'native' if they are part
             * of a native library function */
            for (Type t : c.typeDecls())
                t.visit(this);
            /* Resolve updates here (if any) */
            ht.clear();
        }
        curImport = prevImport;
        return null;
    }
    
    @Override
    public Object visitProcTypeDecl(ProcTypeDecl pd) {
        Log.log(pd, "Visiting a ProcTypeDecl (" + pd.name().getname() + " " + pd.signature() + ")");
        if (!ht.isEmpty() && curImport != null) {
            String path = ResolveImports.makeImportPath(curImport);
            Log.log(pd, "Package path: " + path);
            if (ht.contains("LIBRARY") && ht.contains("NATIVE")) {
                Log.log(pd, "Package file name: " + curImport.file().getname());
                pd.isNative = true;
                pd.library = curImport.toString();
                pd.filename = ht.get("FILE");
                pd.nativeFunction = pd.name().getname();
            } else
                ; /* Non-native procedure found */
        }
        return null;
    }
}
