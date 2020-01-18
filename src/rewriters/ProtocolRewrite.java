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
    
    public void addProtocolName(HashSet<Name> hashSet, Name name) {
    	boolean found = false;
        for (Name n : hashSet) {
            if (n.getname().equals(name.getname())) {
            	found = true;
            	break;
            }
        }
        if (!found)
        	hashSet.add(name);
    }
    
    public HashSet<Name> addExtendProtocolNames(AST a) {
        Log.log(a, "extends a ProtocolypeDecl (" + ((ProtocolTypeDecl) a).name().getname() + ")");
        
        ProtocolTypeDecl pd = (ProtocolTypeDecl) a;
        HashSet<Name> hashSet = new LinkedHashSet<Name>();
        Log.log(pd, "adding protocol " + pd.name().getname());
        hashSet.add(pd.name());
        /* Add member tags that belong to the extended protocols */
        if (pd.extend().size() > 0) {
            for (Name parent : pd.extend()) {
                if (sym.get(parent.getname()) != null) {
                    HashSet<Name> nameSet = addExtendProtocolNames((ProtocolTypeDecl) sym.get(parent.getname()));
                    for (Name pdt : nameSet)
                    	addProtocolName(hashSet, pdt);
                }
            }
        }
        return hashSet;
    }
    
    // DONE
    @Override
    public AST visitProtocolTypeDecl(ProtocolTypeDecl pd) {
        Log.log(pd, "Visiting a ProtocolTypeDecl (" + pd.name().getname() + ")");
        
        HashSet<Name> hashSet = new LinkedHashSet<Name>();
        /* Merge the member tags of all extended protocols */
        if (pd.extend().size() > 0) {
            for (Name name : pd.extend()) {
                if (sym.get(name.getname()) != null)
                    hashSet.addAll(addExtendProtocolNames((ProtocolTypeDecl) sym.get(name.getname())));
            }
        }
        for (Name n : pd.extend())
            addProtocolName(hashSet, n);
        pd.extend().clear();
        /* Rewrite the extend node */
        for (Name n : hashSet)
            pd.extend().append(n);
        Log.log(pd, "protocol " + pd.name().getname() + " with " + pd.extend().size() + " parent(s)");
        for (Name n : pd.extend())
            Log.log(pd, "> protocol " + n);
        return null;
    }
}
