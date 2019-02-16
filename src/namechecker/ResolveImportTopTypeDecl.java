package namechecker;

import java.util.Hashtable;

import ast.AST;
import ast.Compilation;
import ast.Import;
import ast.Pragma;
import ast.ProcTypeDecl;
import ast.Sequence;
import ast.Type;
import utilities.CompilerMessageManager;
import utilities.Log;
import utilities.Visitor;

/**
 * Visitors used for marking procedures as 'native' ProcessJ procedures.
 *
 * @param <T>
 *          The visitor interface used to traverse and resolve each
 *          type in an 'import' statement.
 * 
 * @author Ben
 * @version 01/31/2019
 * @since 1.2
 */
public class ResolveImportTopTypeDecl<T extends AST> extends Visitor<T> {
    
    public Import currentImport = null;
    public static Hashtable<String, String> pragmaTable = new Hashtable<>();
    
    public ResolveImportTopTypeDecl() {
        Log.logHeader("==============================================================");
        Log.logHeader("*                 R E S O L V E   N A T I V E                *");
        Log.logHeader("*                T O P   L E V E L   D E C L S               *");
        Log.logHeader("*       -----------------------------------------------      *");
        Log.logHeader("*       File: " + CompilerMessageManager.INSTANCE.fileName);
        Log.logHeader("");
    }
    
    public T visitCompilation(Compilation co) {
        Log.log(" Finding native top type declarations for " + CompilerMessageManager.INSTANCE.fileName
                + ".");
        Log.log(" Visiting type declarations for " + CompilerMessageManager.INSTANCE.fileName);
        co.imports().visit(this);

        Log.logHeader("");
        Log.logHeader("*                 R E S O L V E   N A T I V E                *");
        Log.logHeader("*                T O P   L E V E L   D E C L S               *");
        Log.logHeader("*       File: " + CompilerMessageManager.INSTANCE.fileName);
        Log.logHeader("==============================================================");
        
        return null;
    }
    
    public T visitPragma(Pragma pr) {
        String name = pr.value() != null ? pr.value() : "";
        Log.log(pr.line + ": Visiting an pragma " + pr.pname().getname() + " " + name);
        if (name.isEmpty())
            pragmaTable.put(pr.pname().getname(), pr.pname().getname());
        else
            pragmaTable.put(pr.pname().getname(), name.replace("\"", ""));
        return null;
    }
    
    public T visitImport(Import im) {
        Log.log(im.line + ": Visiting an import (of file: " + im + ")");
        Import prevImpot = currentImport;
        currentImport = im;
        Sequence<Compilation> compilations = im.getCompilations();
        // For every to-level decl in a file, determine if this type
        // is part of a ProcessJ native library
        for (Compilation c : compilations) {
            if (c == null)
                continue;
            // We visit the fields related to 'pragma' values to check
            // if a type is part of a native library function
            for (Pragma p : c.pragmas())
                p.visit(this);
            // Mark all top-level decls as 'native' if they are part of
            // a ProcessJ native library
            for (Type t : c.typeDecls())
                t.visit(this);
        }
        currentImport = prevImpot;
        pragmaTable.clear();
        return null;
    }
    
    public T visitProcTypeDecl(ProcTypeDecl pd) {
        Log.log(pd.line + ": Visiting a ProcTypeDecl (" + pd.name().getname() + ")");
        if (pragmaTable.size() > 0 && currentImport != null) {
            String path = ResolveImports.makeImportPath(currentImport);
            Log.log("visitImport(): Package path is : " + path);
            if (pragmaTable.contains("LIBRARY") && pragmaTable.contains("NATIVE")) {
                Log.log("visitImport(): Package file name is : " + currentImport.file().getname());
                pd.isNative = true;
                pd.library = currentImport.toString();
                pd.filename = pragmaTable.get("FILE");
                pd.nativeFunction = pd.name().getname();
            }
        }
        return null;
    }
}
