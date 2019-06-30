package rewriters;

import java.util.LinkedHashSet;
import java.util.Set;

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
public class RecordRewrite extends Visitor<AST> {
    // The actual entries in the table
    public SymbolTable sym;
    
    public RecordRewrite(SymbolTable sym) {
        this.sym = sym;
        Log.logHeader("****************************************");
        Log.logHeader("*     R E C O R D   R E W R I T E      *");
        Log.logHeader("****************************************");
    }
    
    public Set<RecordMember> addExtendedRecords(AST ast) {
        RecordTypeDecl rt = (RecordTypeDecl) ast;
        Log.log(rt, "extends a RecordTypeDecl (" + rt.name().getname() + ")");
        
        Set<RecordMember> se = new LinkedHashSet<>();
        for (RecordMember rm : rt.body()) {
            Log.log(rt, "adding member " + rm.type() + " " + rm.name().getname());
            se.add(rm);
        }
        // Add new members if and only if a record extends other records
        if (rt.extend().size() > 0) {
            for (Name parent : rt.extend()) {
                if (sym.get(parent.getname()) != null) {
                    RecordTypeDecl rtd = (RecordTypeDecl) sym.get(parent.getname());
                    Set<RecordMember> seqr = addExtendedRecords(rtd);
                    for (RecordMember rm : seqr)
                        if (!se.add(rm))
                            Log.log(rt, "already in (" + rt.name().getname() + ")");
                }
            }
        }        
        return se;
    }
    
    @Override
    public AST visitRecordTypeDecl(RecordTypeDecl rt) {
        Log.log(rt, "Visiting a RecordTypeDecl (" + rt.name().getname() + ")");
        
        Set<RecordMember> se = new LinkedHashSet<>();        
        if (rt.extend().size() > 0) {
            // Merge sequence of members
            for (Name name : rt.extend()) {
                if (sym.get(name.getname()) != null) {
                    RecordTypeDecl rte = (RecordTypeDecl) sym.get(name.getname());
                    se.addAll(addExtendedRecords(rte));
                }
            }
        }
        // Add this record's members to the set
        for (RecordMember rm : rt.body())
            se.add(rm);
        // Rewrite the record's body and add the new set of members
        rt.body().clear();
        for (RecordMember rm : se)
            rt.body().append(rm);
        Log.log(rt, "record " + rt.name().getname() + " with " + rt.body().size() + " member(s)");
        for (RecordMember rm : rt.body())
            Log.log("  > member " + rm.type() + " " + rm.name());
        return null;
    }
}
