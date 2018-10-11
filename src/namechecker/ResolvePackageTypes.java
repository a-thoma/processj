package namechecker;

import java.io.File;

import ast.AST;
import ast.Compilation;
import ast.Name;
import ast.NamedType;
import ast.Sequence;
import ast.DefineTopLevelDecl;
import utilities.Error;
import utilities.Log;
import utilities.Settings;
import utilities.SymbolTable;
import utilities.Visitor;

public class ResolvePackageTypes extends Visitor<AST> {

    public ResolvePackageTypes() {
        Log.logHeader("==============================================================");
        Log.logHeader("*       P A C K A G E D   T Y P E   R E S O L U T I O N      *");
        Log.logHeader("*       -----------------------------------------------      *");
        Log.logHeader("*       File: " + Error.fileName);
        Log.logHeader("==============================================================");
    }

    // X.Y.Z::f, pa is X.Y.Z and we get that turned into X/Y/Z.pj
    private String makeImportFileName(Sequence<Name> pa) {
        String path = "";
        int i = 0;
        for (Name n : pa) {
            path = path + n.getname();
            if (i < pa.size() - 1)
                path = path + "/";
            i++;
        }
        return path + ".pj";
    }

    public void resolveTypeOrConstant(Name na) {
        Log.log("ResolvePackagedTypes: Resolving `" + na + "'");
        Sequence<Name> pa = na.packageAccess();
        String fileName = "", oldCurrentFileName = "";
        Compilation comp = null;
        // pa is the sequence of names before the :: (if any)
        // if there is no package access then the name must be a
        // name declared locally or in an imported file - both will
        // be correctly resolved at name checking time.
        if (pa.size() > 0) {
            oldCurrentFileName = Error.fileName;
            // Turn X.Y.Z::f into X/Y/Z.pj
            fileName = Settings.absolutePath + makeImportFileName(pa);
            // Does X/Y/Z.pj exist?
            if (new File(fileName).isFile()) // Yes it did - so it is a non-libtary file.
                ; // don't do anything just continue after the if.
            else { // No, it was not a local file so now try the library directory
                fileName = new File(utilities.Settings.includeDir)
                        .getAbsolutePath()
                        + "/"
                        + utilities.Settings.targetLanguage
                        + "/"
                        + makeImportFileName(pa);
                if (new File(fileName).isFile()) { // Yes it is a library file.
                    // don't do anything just continue after the if.
                } else {
                    // It was neither a local nor a library file - throw an error...
                    Error.error(pa, "Cannot resolve file `"
                            + makeImportFileName(pa)
                            + "' as a local or library file.", true, 2150);
                }
            }
            Error.setFileName(fileName);
            // Now import it
            comp = ResolveImports.importFile(pa.child(0), fileName, makeImportFileName(pa));

            SymbolTable st = new SymbolTable();
            if (comp.visited == false) {
                comp.visited = true;
                comp.visit(new TopLevelDecls<AST>(st));
                comp.visit(new ResolvePackageTypes());
                comp.visit(new NameChecker<AST>(st));
                // TODO: should we type check here?
            }
            Error.setFileName(oldCurrentFileName);
            st = SymbolTable.hook;
            // TODO: this should do a proper find if its a symb ol table that comes back
            // but we probably need Type checking for that !
            // so for now - SymbolTable implements TopLevelDecl as well!
            DefineTopLevelDecl td = (DefineTopLevelDecl) st.getShallow(na.simplename());
            if (td != null) { // Yes, we found something
                na.c = comp;
                na.resolvedPackageAccess = td;
                Log.log(na.line + " Resolved `" + na + "' to `" + td + "'");
            } else {
                ;// TODO: don't error out now - the NameChecker will do that!
                //Error.error(na,"Constant or Type '" + na + "' not declared.", false, 0000);
            }
        }
    }

    public AST visitName(Name na) {
        Log.log(na.line + " Resolving Name `" + na.getname() + "'");
        if (na.packageAccess().size() > 0) {
            resolveTypeOrConstant(na);
        }
        return null;
    }

    public AST visitNamedType(NamedType nt) {
        Log.log(nt.line + " Resolving NamedType `" + nt.name() + "'");
        Sequence<Name> packages = nt.name().packageAccess();
        if (packages.size() > 0) {
            resolveTypeOrConstant(nt.name());
        }

        return null;
    }
}