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
 * into a record that extends other records.
 * 
 * @author Ben
 */
public class RecordRewrite extends Visitor<Object> {
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
        HashSet<RecordMember> se = new LinkedHashSet<RecordMember>();
        for (RecordMember rm : rt.body()) {
            Log.log(rt, "adding member " + rm.type() + " " + rm.name().getname());
            se.add(rm);
        }
        // Add new members iff the record extends other records.
        if (rt.extend().size() > 0) {
            for (Name parent : rt.extend()) {
                if (sym.get(parent.getname()) != null) {
                    HashSet<RecordMember> seqr = addExtendedRecords((RecordTypeDecl) sym.get(parent.getname()));
                    for (RecordMember rm : seqr) {
                        if (!se.add(rm))
                            Log.log(rt, "already in (" + rt.name().getname() + ")");
                    }
                }
            }
        }        
        return se;
    }
    
    @Override
    public Object visitRecordTypeDecl(RecordTypeDecl rt) {
        Log.log(rt, "Visiting a RecordTypeDecl (" + rt.name().getname() + ")");
        
        HashSet<RecordMember> se = new LinkedHashSet<RecordMember>();
        if (rt.extend().size() > 0) {
            // Merge the sequence of members of all extend records.
            for (Name name : rt.extend()) {
                if (sym.get(name.getname()) != null)
                    se.addAll(addExtendedRecords((RecordTypeDecl) sym.get(name.getname())));
            }
        }
        for (RecordMember rm : rt.body())
            se.add(rm);
        // Rewrite the sequence of members.
        rt.body().clear();
        for (RecordMember rm : se)
            rt.body().append(rm);
        Log.log(rt, "record " + rt.name().getname() + " with " + rt.body().size() + " member(s)");
        for (RecordMember rm : rt.body())
            Log.log(rt, "> member " + rm.type() + " " + rm.name());
        return null;
    }
}
