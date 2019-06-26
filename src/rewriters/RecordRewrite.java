package rewriters;

import java.util.LinkedHashSet;
import java.util.Set;

import ast.*;
import utilities.Log;
import utilities.SymbolTable;
import utilities.Visitor;

/**
 * Visitor used for rewriting the body of records that inherit fields
 * from one or more than one record. Since multiple inheritance is
 * not supported in Java, this visitor adds _shallow_ copies of fields
 * into a record that inherits from one or more records.
 * 
 * @author Ben
 */
public class RecordRewrite<T extends Object> extends Visitor<T> {
    // The actual entries in the table
    public SymbolTable sym;
    
    public RecordRewrite(SymbolTable sym) {
        this.sym = sym;
        Log.logHeader("****************************************");
        Log.logHeader("*     R E C O R D   R E W R I T E      *");
        Log.logHeader("****************************************");
    }
    
    public Set<RecordMember> findExtendedRecords(AST ast) {
        RecordTypeDecl rt = (RecordTypeDecl) ast;
        Log.log(rt.line + ": extends a RecordTypeDecl (" + rt.name().getname() + ")");
        
        Set<RecordMember> se = new LinkedHashSet<>();
        for (RecordMember rm : rt.body()) {
            Log.log(rt.line + ": adding member " + rm.type() + " " + rm.name().getname());
            se.add(rm);
        }
        if (rt.extend().size() > 0) { // Append members
            for (Name parent : rt.extend()) {
                if (sym.get(parent.getname()) != null) {
                    RecordTypeDecl rte = (RecordTypeDecl) sym.get(parent.getname());
                    Set<RecordMember> seqr = findExtendedRecords(rte);
                    for (RecordMember rm : seqr)
                        if (!se.add(rm))
                            Log.log(rt.line + ": already in (" + rt.name().getname() + ")");
                }
            }
        }        
        return se;
    }
    
    @Override
    public T visitRecordTypeDecl(RecordTypeDecl rt) {
        Log.log(rt.line + ": Visiting a RecordTypeDecl (" + rt.name().getname() + ")");
        
        Set<RecordMember> se = new LinkedHashSet<>();        
        if (rt.extend().size() > 0) {
            // Merge sequence of members
            for (Name name : rt.extend()) {
                if (sym.get(name.getname()) != null) {
                    RecordTypeDecl rte = (RecordTypeDecl) sym.get(name.getname());
                    se.addAll(findExtendedRecords(rte));
                }
            }
        }
        // Add this 'record' members to the set
        for (RecordMember rm : rt.body())
            se.add(rm);
        // Combine all into one
        rt.body().clear();
        for (RecordMember rm : se)
            rt.body().append(rm);
        Log.log(rt.line + ": record " + rt.name().getname() + " with " + rt.body().size() + " member(s)");
        for (RecordMember rm : rt.body())
            Log.log("  > member " + rm.type() + " " + rm.name());
        return null;
    }
}
