package syntax;

import java.util.HashMap;
import java.util.HashSet;

/**
 * @author ben
 */
public class Types {
    
    // --------------------------------------------------
    // ESPECIAL TOKENS
    public static final int EOF = 0;        /* End of file */
    public static final int UNKNOWN = 1;    /* Unknown token */
    
    // --------------------------------------------------
    // LINE FEED TOKEN
    public static final int NEWLINE = -1;   /* Line feed */
    
    // --------------------------------------------------
    // KEYWORDS
    
    /* Atomic Types */
    public static final int KEYWORD_BOOLEAN = 2;
    public static final int KEYWORD_BYTE = 3;
    public static final int KEYWORD_SHORT = 4;
    public static final int KEYWORD_INT = 5;
    public static final int KEYWORD_LONG = 6;
    public static final int KEYWORD_FLOAT = 7;
    public static final int KEYWORD_DOUBLE = 8;
    public static final int KEYWORD_CHAR = 9;
    public static final int KEYWORD_STRING = 10;
    public static final int KEYWORD_VOID = 11;
    
    /* Channel related keywords */
    public static final int KEYWORD_CHAN = 12;
    public static final int KEYWORD_READ = 13;
    public static final int KEYWORD_WRITE = 14;
    public static final int KEYWORD_SHARED = 15;
    public static final int KEYWORD_CLAIM = 16;  /* No longer need */
    
    /* Barrier related keywords */
    public static final int KEYWORD_BARRIER = 17;
    public static final int KEYWORD_SYNC = 18;
    public static final int KEYWORD_ENROLL = 19;
    
    /* Timer related keywords */
    public static final int KEYWORD_TIMER = 20;
    public static final int KEYWORD_TIMEOUT = 21;
    
    /* Silly keywords */
    public static final int KEYWORD_SKIP = 22;
    public static final int KEYWORD_STOP = 23;
    public static final int KEYWORD_IS = 24;
    
    /* Control flow related keywords */
    public static final int KEYWORD_IF = 25;
    public static final int KEYWORD_ELSE = 26;
    public static final int KEYWORD_FOR = 27;
    public static final int KEYWORD_WHILE = 28;
    public static final int KEYWORD_SWITCH = 29;
    public static final int KEYWORD_CASE = 30;
    public static final int KEYWORD_DO = 31;
    public static final int KEYWORD_DEFAULT = 32;
    public static final int KEYWORD_BREAK = 33;
    public static final int KEYWORD_CONTINUE = 34;
    public static final int KEYWORD_RETURN = 35;
    
    /* Other process oriented programming related keywords */
    public static final int KEYWORD_SEQ = 36;
    public static final int KEYWORD_PAR = 37;
    public static final int KEYWORD_PRI = 38;
    public static final int KEYWORD_ALT = 39;
    
    public static final int KEYWORD_NEW = 40;
    
    /* Mobile processes related keywords */
    public static final int KEYWORD_RESUME = 41;
    public static final int KEYWORD_SUSPEND = 42;
    public static final int KEYWORD_WITH = 43;
    
    /* Top level element related keywords */
    public static final int KEYWORD_PROC = 44;
    public static final int KEYWORD_PROTOCOL = 45;
    public static final int KEYWORD_RECORD = 46;
    public static final int KEYWORD_EXTENDS = 47;
    public static final int KEYWORD_IMPLEMENTS = 48;
    
    /* Package related keywords */
    public static final int KEYWORD_PACKAGE = 49;
    public static final int KEYWORD_IMPORT = 50;
    
    /* Modifiers */
    public static final int KEYWORD_MOBILE = 51;
    public static final int KEYWORD_NATIVE = 52;
    public static final int KEYWORD_PUBLIC = 53;
    public static final int KEYWORD_PRIVATE = 54;
    public static final int KEYWORD_PROTECTED = 55;
    public static final int KEYWORD_CONST = 56;
    public static final int KEYWORD_EXTERN = 57;
    
    /* Boolean Literals */
    public static final int KEYWORD_TRUE = 581;  /* BOOLEAN_LITERAL */
    public static final int KEYWORD_FALSE = 582;  /* BOOLEAN_LITERAL */
    
    /* null Literal */
    public static final int KEYWORD_NULL = 59;
    
    /* Other stuff */
    public static final int KEYWORD_PRAGMA = 60;

    // --------------------------------------------------
    // OPERATORS AND OTHER STUFF
    
    /* Parentheses */
    public static final int LPAREN = 61;
    public static final int RPAREN = 62;
    public static final int LBRACE = 63;
    public static final int RBRACE = 64;
    public static final int LBRACK = 65;
    public static final int RBRACK = 66;
    
    /* Separators */
    public static final int SEMICOLON = 67;
    public static final int COMMA = 68;
    
    /* Assignment Operators */
    public static final int EQ = 69;
    public static final int MULTEQ = 70;
    public static final int DIVEQ = 71;
    public static final int MODEQ = 72;
    public static final int PLUSEQ = 73;
    public static final int MINUSEQ = 74;
    public static final int LSHIFTEQ = 75;
    public static final int RSHIFTEQ = 76;
    public static final int RRSHIFTEQ = 77;
    public static final int ANDEQ = 78;
    public static final int XOREQ = 79;
    public static final int OREQ = 80;
    
    /* Relational Operators */
    public static final int GT = 81;
    public static final int LT = 82;
    public static final int EQEQ = 83;
    public static final int LTEQ = 84;
    public static final int GTEQ = 85;
    public static final int NOTEQ = 86;
    
    /* Binary Operators (Some Unary: +, -) */
    public static final int LSHIFT = 87;
    public static final int RSHIFT = 88;
    public static final int RRSHIFT = 89;
    public static final int ANDAND = 90;
    public static final int OROR = 91;
    public static final int PLUS = 92;
    public static final int MINUS = 93;
    public static final int MULT = 94;
    public static final int DIV = 95;
    public static final int AND = 96;
    public static final int OR = 97;
    public static final int XOR = 98;
    public static final int MOD = 99;
    
    /* Unary Operators */
    public static final int NOT = 100;
    public static final int COMP = 101;
    public static final int PLUSPLUS = 102;
    public static final int MINUSMINUS = 103;
    
    /* Other stuff */
    public static final int QUEST = 104;
    public static final int COLONCOLON = 105;
    public static final int COLON = 106;
    public static final int DOT = 107;

    // --------------------------------------------------
    // LITERALS
    
    /* Numeric literals */
    public static final int INTEGER_LITERAL = 108;
    public static final int LONG_LITERAL = 109;
    public static final int FLOAT_LITERAL = 110;
    public static final int DOUBLE_LITERAL = 111;
    public static final int STRING_LITERAL = 112;
    public static final int CHARACTER_LITERAL = 113;
    
    /* Identifiers */
    public static final int IDENTIFIER = 114;
    
    // --------------------------------------------------
    // OTHER
    
    private static final HashMap<Integer, String> TEXT = new HashMap<Integer, String>(); /* Symbol -> text */
    private static final HashMap<String, Integer> TABLE = new HashMap<String, Integer>(); /* Text -> symbol */
    private static final HashSet<String> KEYWORDS = new HashSet<String>(); /* ProcessJ keywords */
    
    // ---------------------------------------------------
    // FIELDS AND METHODS
    
    private static void addSymbols(String text, int sym) {
        TEXT.put(sym, text);
        TABLE.put(text, sym);
    }
    
    private static void addKeywords(String text, int sym) {
        KEYWORDS.add(text);
        addSymbols(text, sym);
        
    }
    
    private Types() {
        /* Empty */
    }
    
    static {
        addSymbols("<EOF>",         EOF);
        addSymbols("<\n>",          NEWLINE);
        addSymbols("<UNKNOWN>",     UNKNOWN);
        
        addKeywords("boolean",      KEYWORD_BOOLEAN);
        addKeywords("byte",         KEYWORD_BYTE);
        addKeywords("short",        KEYWORD_SHORT);
        addKeywords("int",          KEYWORD_INT);
        addKeywords("long",         KEYWORD_LONG);
        addKeywords("float",        KEYWORD_FLOAT);
        addKeywords("double",       KEYWORD_DOUBLE);
        addKeywords("char",         KEYWORD_CHAR);
        addKeywords("string",       KEYWORD_STRING);
        addKeywords("void",         KEYWORD_VOID);
        
        addKeywords("chan",         KEYWORD_CHAN);
        addKeywords("read",         KEYWORD_READ);
        addKeywords("write",        KEYWORD_WRITE);
        addKeywords("shared",       KEYWORD_SHARED);
        addKeywords("claim",        KEYWORD_CLAIM);
        
        addKeywords("barrier",      KEYWORD_BARRIER);
        addKeywords("sync",         KEYWORD_SYNC);
        addKeywords("enroll",       KEYWORD_ENROLL);
        
        addKeywords("timer",        KEYWORD_TIMER);
        addKeywords("timeout",      KEYWORD_TIMEOUT);
        
        addKeywords("skip",         KEYWORD_SKIP);
        addKeywords("stop",         KEYWORD_STOP);
        addKeywords("is",           KEYWORD_IS);
        
        addKeywords("if",           KEYWORD_IF);
        addKeywords("else",         KEYWORD_ELSE);
        addKeywords("for",          KEYWORD_FOR);
        addKeywords("while",        KEYWORD_WHILE);
        addKeywords("switch",       KEYWORD_SWITCH);
        addKeywords("case",         KEYWORD_CASE);
        addKeywords("do",           KEYWORD_DO);
        addKeywords("default",      KEYWORD_DEFAULT);
        addKeywords("break",        KEYWORD_BREAK);
        addKeywords("continue",     KEYWORD_CONTINUE);
        addKeywords("return",       KEYWORD_RETURN);
        
        addKeywords("seq",          KEYWORD_SEQ);
        addKeywords("par",          KEYWORD_PAR);
        addKeywords("pri",          KEYWORD_PRI);
        addKeywords("alt",          KEYWORD_ALT);
        
        addKeywords("new",          KEYWORD_NEW);
        
        addKeywords("resume",       KEYWORD_RESUME);
        addKeywords("suspend",      KEYWORD_SUSPEND);
        addKeywords("with",         KEYWORD_WITH);
        
        addKeywords("proc",         KEYWORD_PROC);
        addKeywords("protocol",     KEYWORD_PROTOCOL);
        addKeywords("record",       KEYWORD_RECORD);
        addKeywords("extends",      KEYWORD_EXTENDS);
        addKeywords("implements",   KEYWORD_IMPLEMENTS);
        
        addKeywords("package",      KEYWORD_PACKAGE);
        addKeywords("import",       KEYWORD_IMPORT);
        
        addKeywords("mobile",       KEYWORD_MOBILE);
        addKeywords("native",       KEYWORD_NATIVE);
        addKeywords("public",       KEYWORD_PUBLIC);
        addKeywords("private",      KEYWORD_PRIVATE);
        addKeywords("protected",    KEYWORD_PROTECTED);
        addKeywords("const",        KEYWORD_CONST);
        addKeywords("extern",       KEYWORD_EXTERN);
        
        addKeywords("true",         KEYWORD_TRUE);
        addKeywords("false",        KEYWORD_FALSE);
        
        addKeywords("null",         KEYWORD_NULL);
        
        addKeywords("#pragma",      KEYWORD_PRAGMA);
        
        addSymbols("(",             LPAREN);
        addSymbols(")",             RPAREN);
        addSymbols("{",             LBRACE);
        addSymbols("}",             RBRACE);
        addSymbols("[",             RBRACK);
        addSymbols("]",             LBRACK);
        
        addSymbols(";",             SEMICOLON);
        addSymbols(",",             COMMA);
        
        addSymbols("=",             EQ);
        addSymbols("*=",            MULTEQ);
        addSymbols("/=",            DIVEQ);
        addSymbols("%=",            MODEQ);
        addSymbols("+=",            PLUSEQ);
        addSymbols("-=",            MINUSEQ);
        addSymbols("<<=",           LSHIFTEQ);
        addSymbols(">>=",           RSHIFTEQ);
        addSymbols(">>>=",          RRSHIFTEQ);
        addSymbols("&=",            ANDEQ);
        addSymbols("^=",            XOREQ);
        addSymbols("|=",            OREQ);
        
        addSymbols(">",             GT);
        addSymbols("<",             LT);
        addSymbols("==",            EQEQ);
        addSymbols("<=",            LTEQ);
        addSymbols(">=",            GTEQ);
        addSymbols("!=",            NOTEQ);
        
        addSymbols("<<",            LSHIFT);
        addSymbols(">>",            RSHIFT);
        addSymbols(">>>",           RRSHIFT);
        addSymbols("&&",            ANDAND);
        addSymbols("||",            OROR);
        addSymbols("+",             PLUS);
        addSymbols("-",             MINUS);
        addSymbols("*",             MULT);
        addSymbols("/",             DIV);
        addSymbols("&",             AND);
        addSymbols("|",             OR);
        addSymbols("^",             XOR);
        addSymbols("%",             MOD);
        
        addSymbols("!",             NOT);
        addSymbols("~",             COMP);
        addSymbols("++",            PLUSPLUS);
        addSymbols("--",            MINUSMINUS);
        
        addSymbols("?",             QUEST);
        addSymbols("::",            COLONCOLON);
        addSymbols(":",             COLON);
        addSymbols(".",             DOT);
    }
}
