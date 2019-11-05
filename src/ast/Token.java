package ast;

public class Token {

    public int kind;        // Gets its value from sym.java.
    public String lexeme;   // The actual text scanned for this token.
    public int line;        // The line number on which this token appears.
    public int charBegin;   // The column number in which the token begins.
    public int charEnd;     // The column number in which the token ends.
    // TODO: add strings
    public static final String names[] = { "EOF", "error", "" };

    public Token(int kind, String text, int line, int charBegin, int charEnd) {
        this.kind = kind;
        this.lexeme = text;
        this.line = line;
        this.charBegin = charBegin;
        this.charEnd = charEnd;
    }
    
    // This constructor is used in the rewrite of loops for the codegen.
    public Token(String text) {
    	this.kind = -1;
    	this.lexeme = text;
    	this.line = 0;
    	this.charBegin = 0;
    	this.charEnd = 0;
    }

    public String toString() {
        return "Token " + names[kind] + " '" + lexeme + "' @ line: " + line
            + "[" + charBegin + ".." + charEnd + "]";
    }
}