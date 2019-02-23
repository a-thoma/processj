package rewriters;

import ast.*;
import utilities.Visitor;
import printers.*;
import utilities.Error;
import java.util.HashSet;

public class LabeledBreakContinueCheck {
    HashSet<String> hs = new HashSet<String>();

    public void go(AST a) {
	if (a instanceof BreakStat) {
	    BreakStat bs = (BreakStat)a;
	    if (bs.target() != null) {
		String target = bs.target().getname();
		if (!hs.contains(target)) {
		    System.out.println("Undefined break label '" + target + "' (or not in scope).");
		    System.exit(1);
		}
	    }
	} else if (a instanceof ContinueStat) {
	    ContinueStat cs = (ContinueStat)a;
	    if (cs.target() != null) {
		String target = cs.target().getname();
		if (!hs.contains(target)) {
                    System.out.println("Undefined continue label '" + target + "' (or not in scope).");
                    System.exit(1);
                }
            }
	} else if (a instanceof Statement) {
	    Statement s = (Statement)a;
	    String label = s.getLabel();
	    if (!label.equals("")) {
		// Check if we already have a label of that name?
		if (hs.contains(label)) {
		    System.out.println("label '" + label + "' already in use.");
                    System.exit(1);
		} 
		hs.add(label);
	    }
	    for (int i=0; i<s.nchildren; i++) {
		if (s.children[i] != null)
		    go(s.children[i]);
	    }
	    if (!label.equals(""))
		hs.remove(label);
	} else if (a instanceof Sequence) {
	    Sequence s = (Sequence)a;
	    for (int i=0; i<s.size(); i++)
		if (s.child(i) != null)
		    go(s.child(i));
	} else {
	    for (int i=0; i<a.nchildren; i++) {
		if (a.children[i] != null)
		    go(a.children[i]);
	    }   
	}
    }
}

		    




    