package rewriters;

import java.util.HashSet;
import java.util.LinkedHashSet;

import ast.AST;
import ast.Name;
import ast.ProtocolTypeDecl;
import utilities.Log;
import utilities.SymbolTable;
import utilities.Visitor;

/**
 * Visitor used for rewriting the body of protocols that inherit tags
 * from one or more than one protocol.
 * 
 * @author Ben
 */
public class ProtocolRewrite extends Visitor<Object> {
    // The top level symbol table.
    public SymbolTable sym;
    
    public ProtocolRewrite(SymbolTable sym) {
        this.sym = sym;
        Log.logHeader("****************************************");
        Log.logHeader("*   P R O T O C O L   R E W R I T E    *");
        Log.logHeader("****************************************");
    }
    
    public boolean addName(HashSet<Name> se, Name name) {
        for (Name n : se) {
            if (n.getname().equals(name.getname()))
                return true;
        }
        se.add(name);
        return false;
    }
    
    public HashSet<Name> addExtendProtocols(AST a) {
        Log.log(a, "extends a RecordTypeDecl (" + ((ProtocolTypeDecl) a).name().getname() + ")");
        
        ProtocolTypeDecl pd = (ProtocolTypeDecl) a;
        HashSet<Name> se = new LinkedHashSet<Name>();
        Log.log(pd, "adding protocol " + pd.name().getname());
        se.add(pd.name());
        // Add new protocols iff the protocol extends other protocols.
        if (pd.extend().size() > 0) {
            for (Name parent : pd.extend()) {
                if (sym.get(parent.getname()) != null) {
                    HashSet<Name> seq = addExtendProtocols((ProtocolTypeDecl) sym.get(parent.getname()));
                    for (Name pdt : seq) {
                        if (addName(se, pdt))
                            Log.log(pd, "already in (" + pd.name().getname() + ")");
                    }
                }
            }
        }
        return se;
    }
    
    @Override
    public Object visitProtocolTypeDecl(ProtocolTypeDecl pd) {
        Log.log(pd, "Visiting a ProtocolTypeDecl (" + pd.name().getname() + ")");
        
        HashSet<Name> se = new LinkedHashSet<Name>();
        if (pd.extend().size() > 0) {
            for (Name name : pd.extend()) {
                if (sym.get(name.getname()) != null) {
                    se.addAll(addExtendProtocols((ProtocolTypeDecl) sym.get(name.getname())));
                }
            }
        }
        for (Name n : pd.extend())
            addName(se, n);
        // Rewrite the extend node.
        pd.extend().clear();
        for (Name n : se)
            pd.extend().append(n);
        Log.log(pd, "protocol " + pd.name().getname() + " with " + pd.extend().size() + " member(s)");
        for (Name n : pd.extend())
            Log.log(pd, "  > protocol " + n);
        return null;
    }
}
