package namechecker;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Hashtable;

import ast.AST;
import ast.Compilation;
import ast.ConstantDecl;
import ast.Import;
import ast.Modifier;
import ast.Name;
import ast.NamedType;
import ast.ProcTypeDecl;
import ast.ProtocolTypeDecl;
import ast.RecordTypeDecl;
import parser.parser;
import scanner.Scanner;
import utilities.Error;
import utilities.Log;
import utilities.SymbolTable;
import utilities.Visitor;

// Error message number range: [2100 - 2199]
// Used 2100 - 2104

/**
 * ToplevelDecls.java:
 *
 * Inserts all top-level declarations into a symbol table. When an import
 * statement is encountered.
 *
 */
public class TopLevelDecls<T extends AST> extends Visitor<T> {
    // Symbol table associated with this file. Set in the constructor.
    private SymbolTable symtab;

    public static String currentFileName = Error.fileName;

    // All imported files are kept in this list - indexed by absolute path and name.
    public static Hashtable<String, Compilation> alreadyImportedFiles = new Hashtable<String, Compilation>();

    public TopLevelDecls(SymbolTable symtab) {
        Log.logHeader("==============================================================");
        Log.logHeader("*                  T O P   L E V E L   D E C L S             *");
        Log.logHeader("*       -----------------------------------------------      *");
        Log.logHeader("*       File: " + Error.fileName);
        Log.logHeader("");
        this.symtab = symtab;
    }

    // TODO: imported files MUST name checked BECAUSE names in 'extends' of protcols and record and constants and procedures can be undefined.
    // What does that mean?

    // TODO: locally imported files should not be in packages .... what about 'this' file ? what about its package ... this must be sorted out

    /**
     * Establishes a symbol table with the top-level declarations declared in the file associated with this compilation
     * inserted. Also causes the creation of a symbol table chain for imported files that is available through the
     * `importParent' field of the symbol table. This chain can be traversed through its parent links.
     *
     * @param co
     *            a Compilation parse tree node.
     */
    public T visitCompilation(Compilation co) {
        Log.log(" Defining forward referencable names (" + Error.fileName
                + ").");
        // 'symtab' is either passed in here from the driver (ProcessJ.java) or from
        // the visitImport() method in this file. Save it cause we need to put all
        // the types and constants for this compilation into it after we handle
        // the imports.
        SymbolTable myGlobals = symtab;

        // reset the symbol table to null so we get a fresh chain for these imports.
        symtab = null;
        Log.log(" Visit Imports in " + Error.fileName + ":");
        co.imports().visit(this);
        Log.log(" Visit Imports in " + Error.fileName + " - done!");
        // symtab now contains a chain through the parent link of all the imports for this compilation.
        // set the 'importParent' of this compilation's symbol table to point to the
        // import chain of symbol tables.
        myGlobals.setImportParent(symtab);
        // re-establish this compilation's table
        symtab = myGlobals;
        // now vist all the type declarations and the constants in this compilation
        Log.log(" Visiting type declarations for " + Error.fileName);
        co.typeDecls().visit(this);
        // hook the symbol table so it can be grabbed from who ever called us.
        SymbolTable.hook = symtab;
        Log.logHeader("");
        Log.logHeader("*                  T O P   L E V E L   D E C L S             *");
        Log.logHeader("*                           D O N E                          *");
        Log.logHeader("*       File: " + Error.fileName);
        Log.logHeader("==============================================================");

        return null;
    }

    // **********************************************************************
    // * Top-Level declarations handled below.
    // *
    // * Top-level declarations are simply inserted into the current symbol
    // * table which is held in the variable symtab.
    // **********************************************************************

    // ConstantDecl
    public T visitConstantDecl(ConstantDecl cd) {
        Log.log(cd.line + ": Visiting a ConstantDecl "
                + cd.var().name().getname());
        if (!symtab.put(cd.var().name().getname(), cd))
            Error.error(cd, "Type with name '" + cd.var().name().getname()
                    + "' already declared in this scope.", false, 2100);
        return null;
    }

    // ProcTypeDecl
    public T visitProcTypeDecl(ProcTypeDecl pd) {
        Log.log(pd.line + ": Visiting a ProcTypeDecl " + pd.name().getname());
        // Procedures can be overloaded, so an entry in the symbol table for a procedure is
        // another symbol table which is indexed by signature.
        if (Modifier.hasModifierSet(pd.modifiers(), Modifier.MOBILE))
            if (!pd.returnType().isVoidType())
                Error.error(pd, "Mobile procedure '" + pd.name().getname()
                        + "' must have void return type.", false, 2109); // TODO error number

        // Mobile procedure may NOT be overloaded.
        // If a symbol table contains a mobile the field isMobileProcedure is true.
        Object s = symtab.getShallow(pd.name().getname());
        if (s == null) {
            // this is the first time we see a procedure by this name in this scope
            SymbolTable st = new SymbolTable();
            if (Modifier.hasModifierSet(pd.modifiers(), Modifier.MOBILE))
                st.isMobileProcedure = true;
            st.put(pd.signature(), pd);
            symtab.put(pd.name().getname(), st);
        } else {
            if (s instanceof SymbolTable) {
                SymbolTable st = (SymbolTable) s;
                if (Modifier.hasModifierSet(pd.modifiers(), Modifier.MOBILE)) {
                    if (st.isMobileProcedure)
                        Error.error(pd,
                                "Only one declaration of mobile procedure '"
                                        + pd.name().getname() + "' may exist.",
                                false, 2110);
                    else
                        Error.error(pd, "Non-mobile proecdure '"
                                        + pd.name().getname() + "' already exists.",
                                false, 2111);
                } else
                    st.put(pd.signature(), pd);
            } else
                Error.error(pd, "Non-procedure type with name '" + pd.getname()
                        + "' already declared in this scope", false, 2101);
        }
        return null;
    }

    // RecordTypeDecl
    public T visitRecordTypeDecl(RecordTypeDecl rd) {
        Log.log(rd.line + ": Visiting a RecordTypeDecl " + rd.name().getname());
        if (!symtab.put(rd.name().getname(), rd))
            Error.error(rd, "Type with name '" + rd.name().getname()
                    + "' already declared in this scope.", false, 2102);
        return null;
    }

    // ProtocolTypeDecl
    public T visitProtocolTypeDecl(ProtocolTypeDecl pd) {
        Log.log(pd.line + ": Visiting a ProtocolTypeDecl "
                + pd.name().getname());
        if (!symtab.put(pd.name().getname(), pd))
            Error.error(pd, "Type with name '" + pd.name().getname()
                    + "' already declared in this scope.", false, 2103);
        return null;
    }

    // NamedType
    public T visitNamedType(NamedType nt) {
        Log.log("Toplevel Named Type:" + nt);
        if (!symtab.put(nt.name().getname(), nt))
            Error.error(nt, "Type with name '" + nt.name().getname()
                    + "' already declared in this scope.", false, 2111);
        return null;
    }

}