package namechecker;

import java.util.Hashtable;

import ast.AST;
import ast.Compilation;
import ast.ConstantDecl;
import ast.Modifier;
import ast.NamedType;
import ast.ProcTypeDecl;
import ast.ProtocolTypeDecl;
import ast.RecordTypeDecl;
import utilities.PJMessage;
import utilities.CompilerMessageManager;
import utilities.Log;
import utilities.MessageType;
import utilities.SymbolTable;
import utilities.Visitor;
import utilities.VisitorMessageNumber;

/**
 * ToplevelDecls.java:
 *
 * Inserts all top-level declarations into a symbol table. When
 * an import statement is encountered.
 *
 */
public class TopLevelDecls<T extends AST> extends Visitor<T> {
    // Symbol table associated with this file. Set in the constructor.
    private SymbolTable symtab;

    public static String currentFileName = CompilerMessageManager.INSTANCE.fileName;

    // All imported files are kept in this table - indexed by absolute path and name.
    public static Hashtable<String, Compilation> alreadyImportedFiles = new Hashtable<String, Compilation>();

    public TopLevelDecls(SymbolTable symtab) {
        Log.logHeader("==============================================================");
        Log.logHeader("*                  T O P   L E V E L   D E C L S             *");
        Log.logHeader("*       -----------------------------------------------      *");
        Log.logHeader("*       File: " + CompilerMessageManager.INSTANCE.fileName);
        Log.logHeader("");
        this.symtab = symtab;
    }

    // TODO: imported files MUST name checked BECAUSE names in 'extends' of protcols and record and constants and procedures can be undefined.
    // What does that mean?

    // TODO: locally imported files should not be in packages .... what about 'this' file ? what about its package ... this must be sorted out

    /**
     * Establishes a symbol table with the top-level declarations declared
     * in the file associated with this compilation inserted. Also causes
     * the creation of a symbol table chain for imported files that is
     * available through the 'importParent' field of the symbol table. This
     * chain can be traversed through its parent links.
     *
     * @param co
     *            a Compilation parse tree node.
     */
    public T visitCompilation(Compilation co) {
        Log.log(" Defining forward referencable names (" + CompilerMessageManager.INSTANCE.fileName
                + ").");
        // 'symtab' is either passed in here from the driver (ProcessJ.java) or from
        // the visitImport() method in this file. Save it cause we need to put all
        // the types and constants for this compilation into it after we handle
        // the imports.
//        SymbolTable myGlobals = symtab;

        // reset the symbol table to null so we get a fresh chain for these imports.
//        symtab = null;
//        Log.log(" Visit Imports in " + CompilerMessageManager.INSTANCE.fileName + ":");
//        co.imports().visit(this);
//        Log.log(" Visit Imports in " + CompilerMessageManager.INSTANCE.fileName + " - done!");
        // symtab now contains a chain through the parent link of all the imports for this compilation.
        // set the 'importParent' of this compilation's symbol table to point to the
        // import chain of symbol tables.
//        myGlobals.setImportParent(symtab);
        // re-establish this compilation's table
//        symtab = myGlobals;
        // now visit all the type declarations and the constants in this compilation
        Log.log(" Visiting type declarations for " + CompilerMessageManager.INSTANCE.fileName);
        co.typeDecls().visit(this);
        // hook the symbol table so it can be grabbed from who ever called us.
//        SymbolTable.hook = symtab;
//        System.out.println("From visitCompilation: *******");
//        symtab.printStructure("");
        
        Log.logHeader("");
        Log.logHeader("*                  T O P   L E V E L   D E C L S             *");
        Log.logHeader("*                           D O N E                          *");
        Log.logHeader("*       File: " + CompilerMessageManager.INSTANCE.fileName);
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
            CompilerMessageManager.INSTANCE.reportMessage(new PJMessage.Builder()
                        .addAST(cd)
                        .addError(VisitorMessageNumber.TOP_LEVEL_DECLS_200)
                        .addArguments(cd.var().name().getname())
                        .build(), MessageType.PRINT_CONTINUE);
        return null;
    }

    // ProcTypeDecl
    public T visitProcTypeDecl(ProcTypeDecl pd) {
        Log.log(pd.line + ": Visiting a ProcTypeDecl " + pd.name().getname());
        // Procedures can be overloaded, so an entry in the symbol table for a procedure is
        // another symbol table which is indexed by signature.
        if (Modifier.hasModifierSet(pd.modifiers(), Modifier.MOBILE))
            if (!pd.returnType().isVoidType())
                CompilerMessageManager.INSTANCE.reportMessage(new PJMessage.Builder()
                            .addAST(pd)
                            .addError(VisitorMessageNumber.TOP_LEVEL_DECLS_205)
                            .addArguments(pd.name().getname())
                            .build(), MessageType.PRINT_CONTINUE);

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
                        CompilerMessageManager.INSTANCE.reportMessage(new PJMessage.Builder()
                                    .addAST(pd)
                                    .addError(VisitorMessageNumber.TOP_LEVEL_DECLS_206)
                                    .addArguments(pd.name().getname())
                                    .build(), MessageType.PRINT_CONTINUE);
                    else
                        CompilerMessageManager.INSTANCE.reportMessage(new PJMessage.Builder()
                                .addAST(pd)
                                .addError(VisitorMessageNumber.TOP_LEVEL_DECLS_208)
                                .addArguments(pd.name().getname())
                                .build(), MessageType.PRINT_CONTINUE);
                } else
                    st.put(pd.signature(), pd);
            } else
                CompilerMessageManager.INSTANCE.reportMessage(new PJMessage.Builder()
                            .addAST(pd)
                            .addError(VisitorMessageNumber.TOP_LEVEL_DECLS_201)
                            .addArguments(pd.getname())
                            .build(), MessageType.PRINT_CONTINUE);
        }
        return null;
    }

    // RecordTypeDecl
    public T visitRecordTypeDecl(RecordTypeDecl rd) {
        Log.log(rd.line + ": Visiting a RecordTypeDecl " + rd.name().getname());
        if (!symtab.put(rd.name().getname(), rd))
            CompilerMessageManager.INSTANCE.reportMessage(new PJMessage.Builder()
                        .addAST(rd)
                        .addError(VisitorMessageNumber.TOP_LEVEL_DECLS_202)
                        .addArguments(rd.name().getname())
                        .build(), MessageType.PRINT_CONTINUE);
        return null;
    }

    // ProtocolTypeDecl
    public T visitProtocolTypeDecl(ProtocolTypeDecl pd) {
        Log.log(pd.line + ": Visiting a ProtocolTypeDecl "
                + pd.name().getname());
        if (!symtab.put(pd.name().getname(), pd))
            CompilerMessageManager.INSTANCE.reportMessage(new PJMessage.Builder()
                        .addAST(pd)
                        .addError(VisitorMessageNumber.TOP_LEVEL_DECLS_203)
                        .addArguments(pd.name().getname())
                        .build(), MessageType.PRINT_CONTINUE);
        return null;
    }

    // NamedType
    public T visitNamedType(NamedType nt) {
        Log.log("Toplevel Named Type:" + nt);
        if (!symtab.put(nt.name().getname(), nt))
            CompilerMessageManager.INSTANCE.reportMessage(new PJMessage.Builder()
                        .addAST(nt)
                        .addError(VisitorMessageNumber.TOP_LEVEL_DECLS_207)
                        .addArguments(nt.name().getname())
                        .build(), MessageType.PRINT_CONTINUE);
        return null;
    }
}