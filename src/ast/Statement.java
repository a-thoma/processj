package ast;

public abstract class Statement extends AST {

    private String label = "";
    public boolean yields = false;

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public Statement(Token t) {
        super(t);
    }

    public Statement(AST a) {
        super(a);
    }
    
    public boolean doesYield() { 
        return yields;
    }

    public void setYield() {
        yields = true;
    }
}