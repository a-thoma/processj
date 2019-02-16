package ast;

public abstract class Statement extends AST {

    private String label = "";
    private boolean yields = false;

    public Statement(Token t) {
        super(t);
    }

    public Statement(AST a) {
        super(a);
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
    
    public boolean doesYield() { 
	    return yields;
    }

    public void setYield() {
	    yields = true;
    }
}