package rewriters;

import java.util.HashSet;
import java.util.LinkedHashSet;

import ast.*;
import utilities.Log;
import utilities.SymbolTable;
import utilities.Visitor;

/**
 * Visitor used for rewriting the body of records that inherit fields
 * from one or more than one record. Since multiple inheritance is not
 * supported in Java, this visitor adds _shallow_ copies of fields
 * into a record that extends multiple records.
 * 
 * @author Ben
 */
public class RecordRewrite extends Visitor<AST> {
    // The top level symbol table.
    public SymbolTable sym;
    
    public RecordRewrite(SymbolTable sym) {
        this.sym = sym;
        Log.logHeader("****************************************");
        Log.logHeader("*     R E C O R D   R E W R I T E      *");
        Log.logHeader("****************************************");
    }
    
    public HashSet<RecordMember> addExtendedRecords(AST a) {
        Log.log(a, "extends a RecordTypeDecl (" + ((RecordTypeDecl) a).name().getname() + ")");

        RecordTypeDecl rt = (RecordTypeDecl) a;
        HashSet<RecordMember> hashSet = new LinkedHashSet<RecordMember>();
        for (RecordMember rm : rt.body()) {
            Log.log(rt, "adding member " + rm.type() + " " + rm.name().getname());
            hashSet.add(rm);
        }
        /* Add member fields that belong to extended records */
        if (rt.extend().size() > 0) {
            for (Name parent : rt.extend()) {
                if (sym.get(parent.getname()) != null) {
                    HashSet<RecordMember> memberSet = addExtendedRecords((RecordTypeDecl) sym.get(parent.getname()));
                    for (RecordMember rm : memberSet) {
                        if (!hashSet.add(rm))
                            Log.log(rt, "Name '" + rm.name().getname() + "' already in (" + rt.name().getname() + ")");
                    }
                }
            }
        }        
        return hashSet;
    }
    
    // DONE
    @Override
    public AST visitRecordTypeDecl(RecordTypeDecl rt) {
        Log.log(rt, "Visiting a RecordTypeDecl (" + rt.name().getname() + ")");
        
        HashSet<RecordMember> hashSet = new LinkedHashSet<RecordMember>();
        /* Merge the member fields of all extended records */
        if (rt.extend().size() > 0) {
            for (Name name : rt.extend()) {
                if (sym.get(name.getname()) != null)
                    hashSet.addAll(addExtendedRecords((RecordTypeDecl) sym.get(name.getname())));
            }
        }
        for (RecordMember rm : rt.body())
            hashSet.add(rm);
        rt.body().clear();
        /* Rewrite the extend node */
        for (RecordMember rm : hashSet)
            rt.body().append(rm);
        Log.log(rt, "record " + rt.name().getname() + " with " + rt.body().size() + " member(s)");
        for (RecordMember rm : rt.body())
            Log.log(rt, "> member " + rm.type() + " " + rm.name());
        return null;
    }
}
