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
 * Visitor used to rewrite the body of protocols that inherit tags
 * from one or more than one protocol. Note that this visitor adds
 * _shallow_ copies of tags (and their members) into a protocol that
 * extends multiple protocols.
 * 
 * @author Ben
 */
public class ProtocolRewrite extends Visitor<AST> {
    // The top level symbol table.
    public SymbolTable sym;
    
    public ProtocolRewrite(SymbolTable sym) {
        this.sym = sym;
        Log.logHeader("****************************************");
        Log.logHeader("*   P R O T O C O L   R E W R I T E    *");
        Log.logHeader("****************************************");
    }
    
    public void addName(HashSet<Name> se, Name name) {
    	boolean found = false;
        for (Name n : se) {
            if (n.getname().equals(name.getname())) {
            	found = true;
            	break;
            }
        }
        if (!found)
        	se.add(name);
    }
    
    public HashSet<Name> addExtendProtocols(AST a) {
        Log.log(a, "extends a ProtocolypeDecl (" + ((ProtocolTypeDecl) a).name().getname() + ")");
        
        ProtocolTypeDecl pd = (ProtocolTypeDecl) a;
        HashSet<Name> se = new LinkedHashSet<Name>();
        Log.log(pd, "adding protocol " + pd.name().getname());
        se.add(pd.name());
        // Add new tags if the protocol extends other protocols.
        if (pd.extend().size() > 0) {
            for (Name parent : pd.extend()) {
                if (sym.get(parent.getname()) != null) {
                    HashSet<Name> seq = addExtendProtocols((ProtocolTypeDecl) sym.get(parent.getname()));
                    for (Name pdt : seq)
                    	addName(se, pdt);
                }
            }
        }
        return se;
    }
    
    // DONE
    @Override
    public AST visitProtocolTypeDecl(ProtocolTypeDecl pd) {
        Log.log(pd, "Visiting a ProtocolTypeDecl (" + pd.name().getname() + ")");
        
        HashSet<Name> se = new LinkedHashSet<Name>();
        // Merge the sequence of tags of all extend protocols.
        if (pd.extend().size() > 0) {
            for (Name name : pd.extend()) {
                if (sym.get(name.getname()) != null)
                    se.addAll(addExtendProtocols((ProtocolTypeDecl) sym.get(name.getname())));
            }
        }
        for (Name n : pd.extend())
            addName(se, n);
        pd.extend().clear();
        // Rewrite the extend node.
        for (Name n : se)
            pd.extend().append(n);
        Log.log(pd, "protocol " + pd.name().getname() + " with " + pd.extend().size() + " parent(s)");
        for (Name n : pd.extend())
            Log.log(pd, "> protocol " + n);
        return null;
    }
}
