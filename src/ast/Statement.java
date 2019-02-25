package ast;

public abstract class Statement extends AST {

    // This sequenec is used in the rewriting phase.                                                                                                                                                               
    // It holds Declarations and Assignments of the form:                                                                                                                                                          
    //                                                                                                                                                                                                             
    // T temp_1;                                                                                                                                                                                                   
    // temp_1 = c.read();                                                                                                                                                                                          
    public Sequence<Statement> assignments = null;


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