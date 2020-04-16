package codegen.java;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import ast.*;
import codegen.Helper;
import codegen.Tag;
import processj.runtime.*;
import utilities.Log;
import utilities.SymbolTable;
import utilities.Visitor;

/**
 * A tree walker which collects data from an AST object and then
 * pushes this data into a template to translate a ProcessJ source
 * file to Java code.
 * 
 * @author ben
 * @version 06/10/2018
 * @since 1.2
 */
public class CodeGenJava extends Visitor<Object> {

    /* String template file locator */
    private final String __stGammarFile = "resources/stringtemplates/java/grammarTemplatesJava.stg";
    
    /* Current Java version -- only needed for debugging */
    private final String __currJVM = System.getProperty("java.version");

    /* Collection of templates, imported templates, and/or groups that 
     * contain formal template definitions */
    private STGroup __stGroup;
    
    /* Current compilation unit */
    private Compilation __curCompilation = null;

    /* The user working directory */
    private String __workingDir = null;

    /* Currently executing procedure */
    private String __curProcName = null;
    
    /* Currently executing par-block */
    private String __curParBlock = null;
    
    /* Currently executing protocol */
    private String __curProtocol = null;
    
    /* All imports are kept in this table */
    private HashSet<String> __importFiles = new LinkedHashSet<String>();
    
    /* Top level declarations */
    private SymbolTable __topLvlDecls = null;

    /* Formal parameters transformed into fields */
    private HashMap<String, String> __param2Field = new LinkedHashMap<String, String>();
    
    /* Formal parameter names transformed into variable names */
    private HashMap<String, String> __param2VarName = new LinkedHashMap<String, String>();
    
    /* Local parameters transformed into fields */
    private HashMap<String, String> __local2Field = new LinkedHashMap<String, String>();
    
    /* Record members transformed into fields */
    private HashMap<String, String> __recMember2Field = new LinkedHashMap<String, String>();

    /* Protocol names and tags currently switched on */
    private HashMap<String, String> __protName2ProtTag = new HashMap<String, String>();
    
    /* List of switch labels */
    private ArrayList<String> __switchCases = new ArrayList<String>();
    
    /* List of barrier expressions */
    private ArrayList<String> __barriers = new ArrayList<String>();

    /* Identifier for parameter declaration */
    private int __varDecId = 0;
    
    /* Identifier for par-block declaration */
    private int __parDecId = 0;

    /* Identifier for local variable declaration */
    private int __localDecId = 0;
    
    /* Jump label used when procedures yield */
    private int __jumpLabel = 0;
    
    /* Access to protocol case */
    private boolean __isProtocCase = false;
    
    /* Access to protocol tag */
    private String __currProtocTag = null;
    
    /* This is used for arrays of N-dimensions */
    private boolean __isArrayLiteral = false;
    
    private final static String DELIMITER = ";";

    /**
     * Internal constructor that loads a group file containing a collection of
     * templates, imported templates, and/or groups containing formal template
     * definitions. Additionally, the constructor initializes a symbol table
     * with top-level declarations.
     * 
     * @param s
     *          The top-level declarations which can be procedures, records,
     *          protocols, constants, and/or external types.
     */
    public CodeGenJava(SymbolTable s) {
        Log.logHeader("*****************************************");
        Log.logHeader("*         CODE GENERATOR JAVA           *");
        Log.logHeader("*****************************************");
        
        __topLvlDecls = s;
        __stGroup = new STGroupFile(__stGammarFile);
    }
    
    /**
     * Sets the system properties to a current working directory.
     *
     * @param workingDir
     *          A working directory.
     */
    public void workingDir(String workingDir) {
        __workingDir = workingDir;
    }
    
    /**
     * Return a string representing the current working directory.
     */
    public String workingDir() {
        return __workingDir;
    }
    
    /**
     * Visit a single compilation unit which starts with an optional package
     * declaration, followed by zero or more import declarations, followed
     * by zero or more type declarations.
     *
     * @param co
     *          An AST that represents the entire compilation unit.
     * @return A text generated after evaluating this compilation unit.
     */
    @Override
    public Object visitCompilation(Compilation co) {
        Log.log(co, "Visiting a Compilation");
        
        __curCompilation = co;
        /* Code generated by the template */
        String codeGen = null;
        /* Template to fill in */
        ST stCompilation = __stGroup.getInstanceOf("Compilation");
        /* Reference to all remaining types */
        ArrayList<String> body = new ArrayList<String>();
        /* Holds all top-level declarations */
        Sequence<Type> typeDecls = co.typeDecls();
        /* Package name for this source file */
        String packagename = co.packageNoName();
        
        for (Import im : co.imports())
            if (im != null)
                __importFiles.add((String) im.visit(this));
        
        for (AST decl : typeDecls) {
            if (decl instanceof Type) {
                /* Collect procedures, records, protocols, external types, etc. */
                String t = (String) ((Type) decl).visit(this);
                if (t != null)
                    body.add(t);
            } else if (decl instanceof ConstantDecl) {
                /* Iterate over remaining declarations, which is anything that
                 * comes after top-level declarations */
                String cd = (String) ((ConstantDecl) decl).visit(this);
                if (cd != null)
                    body.add(cd);
            }
        }

        stCompilation.add("pathName", packagename);
        stCompilation.add("fileName", co.fileName);
        stCompilation.add("name", co.fileNoExtension());
        stCompilation.add("body", body);
        stCompilation.add("version", __currJVM);
        
        /* Add all import statements to the file (if any) */
        if (__importFiles.size() > 0)
            stCompilation.add("imports", __importFiles);

        /* This will render the code for debbuging */
        codeGen = stCompilation.render();
        
        Log.logHeader("****************************************");
        Log.logHeader("*            GENERATED CODE            *");
        Log.logHeader("****************************************");
        Log.logHeader(codeGen);
        
        return codeGen;
    }
    
    @Override
    public Object visitProcTypeDecl(ProcTypeDecl pd) {
        Log.log(pd, "Visiting a ProcTypeDecl (" + pd.name().getname() + ")");
        
        ST stProcTypeDecl = null;
        /* Save previous procedure state */
        String prevProcName = __curProcName;
        /* Save previous jump labels */
        ArrayList<String> prevLabels = __switchCases;
        if (!__switchCases.isEmpty())
            __switchCases = new ArrayList<String>();
        /* Name of the invoked procedure */
        __curProcName = (String) pd.name().visit(this);
        /* Procedures are static classes which belong to the same package and
         * class. To avoid having classes with the same name, we generate a
         * new name for the currently executing procedure */
        String procName = null;
        /* For non-invocations, that is, for anything other than a procedure
         * that yields, we need to extends the PJProcess class anonymously */
        if ("Anonymous".equals(__curProcName)) {
            /* Preserve current jump label for resumption */
            int prevJumpLabel = __jumpLabel;
            __jumpLabel = 0;
            /* Create an instance for such anonymous procedure */
            stProcTypeDecl = __stGroup.getInstanceOf("AnonymousProcess");
            /* Statements that appear in the procedure being executed */
            String[] body = (String[]) pd.body().visit(this);
            stProcTypeDecl.add("parBlock", __curParBlock);
            stProcTypeDecl.add("syncBody", body);
            /* Add the barrier this procedure should resign from */
            if (!__barriers.isEmpty())
                stProcTypeDecl.add("barrier", __barriers);
            /* Add the switch block for yield and resumption */
            if (!__switchCases.isEmpty()) {
                ST stSwitchBlock = __stGroup.getInstanceOf("SwitchBlock");
                stSwitchBlock.add("jumps", __switchCases);
                stProcTypeDecl.add("switchBlock", stSwitchBlock.render());
            }
            /* Restore jump label so it knows where to resume from */
            __jumpLabel = prevJumpLabel;
        } else {
            /* Restore global variables for a new PJProcess class */
            resetGlobals();
            /* Formal parameters that must be passed to the procedure */
            Sequence<ParamDecl> formals = pd.formalParams();
            /* Do we have any parameters? */
            if (formals != null && formals.size() > 0) {
                /* Iterate through and visit every parameter declaration */
                for (int i = 0; i < formals.size(); ++i) {
                    ParamDecl actualParam = formals.child(i);
                    /* Retrieve the name and type of each parameter specified in a
                     * list of comma-separated arguments. Note that we ignored the
                     * value returned by this visitor */
                    actualParam.visit(this);
                }
            }            
            /* Visit all declarations that appear in the procedure */
            String[] body = (String[]) pd.body().visit(this);
            /* Retrieve modifier(s) attached to invoked procedure such as private,
             * public, protected, etc. */
            String[] modifiers = (String[]) pd.modifiers().visit(this);
            /* Grab the return type of the invoked procedure */
            String procType = (String) pd.returnType().visit(this);
            /* The procedure's annotation determines if we have a yielding procedure
             * or a Java method (a non-yielding procedure) */
            boolean doesProcYield = Helper.doesProcYield(pd);
            /* Set the template to the correct instance value and then initialize
             * its attributes */
            if (doesProcYield) {
                /* This procedure yields! Grab the instance of a yielding procedure
                 * from the string template in order to define a new class */
                procName = Helper.makeVariableName(__curProcName + createProcSignature(pd), 0, Tag.PROCEDURE_NAME);
                stProcTypeDecl = __stGroup.getInstanceOf("ProcClass");
                stProcTypeDecl.add("name", procName);
                /* Add the statements that appear in the body of the procedure */
                stProcTypeDecl.add("syncBody", body);
            } else {
                /* Otherwise, grab the instance of a non-yielding procedure to
                 * define a new static Java method */
                procName = Helper.makeVariableName(__curProcName + createProcSignature(pd), 0, Tag.METHOD_NAME);
                stProcTypeDecl = __stGroup.getInstanceOf("Method");
                stProcTypeDecl.add("name", procName);
                stProcTypeDecl.add("type", procType);
                /* Do we have any access modifier? If so, add them */
                if (modifiers != null && modifiers.length > 0)
                    stProcTypeDecl.add("modifier", modifiers);
                stProcTypeDecl.add("body", body);
            }
            
            /* Create an entry point for the ProcessJ program, which is just
             * a Java main method that is called by the JVM */
            if ("main".equals(__curProcName) && pd.signature().equals(Tag.MAIN_NAME.toString())) {
                /* Create an instance of a Java main method template */
                ST stMain = __stGroup.getInstanceOf("Main");
                stMain.add("class", __curCompilation.fileNoExtension());
                stMain.add("name", procName);
                /* Pass the list of command line arguments to this main method */
                if (!__param2Field.isEmpty()) {
                    stMain.add("types", __param2Field.values());
                    stMain.add("vars", __param2Field.keySet());
                }
                /* Add the entry point of the program */
                stProcTypeDecl.add("main", stMain.render());
            }
            /* The list of command-line arguments should be passed to the constructor
             * of the static class that the main method belongs or be passed to the
             * a static method */
            if (!__param2Field.isEmpty()) {
                stProcTypeDecl.add("types", __param2Field.values());
                stProcTypeDecl.add("vars", __param2Field.keySet());
            }
            /* The list of local variables defined in the body of a procedure
             * becomes the instance fields of the class */
            if (!__local2Field.isEmpty()) {
                stProcTypeDecl.add("ltypes", __local2Field.values());
                stProcTypeDecl.add("lvars", __local2Field.keySet());
            }
            /* Add the switch block for resumption */
            if (!__switchCases.isEmpty()) {
                ST stSwitchBlock = __stGroup.getInstanceOf("SwitchBlock");
                stSwitchBlock.add("jumps", __switchCases);
                stProcTypeDecl.add("switchBlock", stSwitchBlock.render());
            }
        }
        
        /* Restore and reset previous values */
        __curProcName = prevProcName;
        /* Restore previous jump labels */
        __switchCases = prevLabels;

        return stProcTypeDecl.render();
    }
    
    @Override
    public Object visitBinaryExpr(BinaryExpr be) {
        Log.log(be, "Visiting a BinaryExpr");
        
        ST stBinaryExpr = __stGroup.getInstanceOf("BinaryExpr");
        String op = be.opString();
        String lhs = (String) be.left().visit(this);        
        lhs = lhs.replace(DELIMITER, "");
        lhs = be.left().hasParens ? "(" + lhs + ")" : lhs;
        String rhs = (String) be.right().visit(this);
        rhs = be.right().hasParens ? "(" + rhs + ")" : rhs;
        rhs = rhs.replace(DELIMITER, "");
        
        // <--
        /* If 'op' is the 'istype' keyword, we need to look in the top-level
         * decls table for the record or protocol in question and the records
         * or protocols such record or protocol extends */
        if (__local2Field.containsKey(lhs)) {
            String namedType = __local2Field.get(lhs);
            Object o = __topLvlDecls.get(namedType);
            if (o instanceof RecordTypeDecl) {
                stBinaryExpr = __stGroup.getInstanceOf("RecordExtend");
                stBinaryExpr.add("name", lhs);
                stBinaryExpr.add("type", String.format("I_%s", rhs));
                return stBinaryExpr.render();
            } else if (namedType.equals(PJProtocolCase.class.getSimpleName())) {
                stBinaryExpr = __stGroup.getInstanceOf("RecordExtend");
                stBinaryExpr.add("name", lhs);
                stBinaryExpr.add("type", __curProtocol);
                return stBinaryExpr.render();
            }
        }
        // -->
        
        stBinaryExpr.add("lhs", lhs);
        stBinaryExpr.add("rhs", rhs);
        stBinaryExpr.add("op", op);

        return stBinaryExpr.render();
    }
    
    // WhileStat -- nothing to do    
    // DoStat -- nothing to do
    // ForStat -- nothing to do
    
    @Override
    public Object visitContinueStat(ContinueStat cs) {
        Log.log(cs, "Visiting a ContinueStat");
        
        ST stContinueStat = __stGroup.getInstanceOf("ContinueStat");
        String name = null;
        /* If target isn't null, then we have a label to jump to */
        if (cs.target() != null) {
            name = (String) cs.target().visit(this);
            stContinueStat.add("name", name);
        }
        
        return stContinueStat.render();
    }
    
    @Override
    public Object visitIfStat(IfStat is) {
        Log.log(is, "Visiting a IfStat");
        
        ST stIfStat = __stGroup.getInstanceOf("IfStat");
        /* Sequence of statements enclosed in a block-stmt */
        String[] thenStats = null;
        String[] thenParts = null;
        String condExpr = null;
        
        if (is.expr() != null)
            condExpr = (String) is.expr().visit(this);
        if (is.thenpart() != null) {
            if (is.thenpart() instanceof Block)
                thenStats = (String[]) is.thenpart().visit(this);
            else {
                String stat = (String) is.thenpart().visit(this);
                thenStats = new String[] { stat };
            }
        }
        if (is.elsepart() != null) {
            if (is.elsepart() instanceof Block)
                thenParts = (String[]) is.elsepart().visit(this);
            else {
                String stat = (String) is.elsepart().visit(this);
                thenParts = new String[] { stat };
            }
        }
        
        stIfStat.add("expr", condExpr);
        stIfStat.add("thenPart", thenStats);
        stIfStat.add("elsePart", thenParts);
        
        return stIfStat.render();
    }
    
    @Override
    public Object visitAssignment(Assignment as) {
        Log.log(as, "Visiting an Assignment");
        
        ST stVar = __stGroup.getInstanceOf("Var");
        
        String op = as.opString();
        String lhs = null;
        String rhs = null;
        
        if (as.left() != null) {
            if (as.left() instanceof RecordAccess)
                return createRecordAssignment(as);
            /* Not a protocol or record */
            lhs =  (String) as.left().visit(this);
        }
        
        if (as.right() instanceof NewArray)
            return createNewArray(lhs, ((NewArray) as.right()));
        else if (as.right() instanceof ChannelReadExpr)
            return createChannelReadExpr(lhs, op, ((ChannelReadExpr) as.right()));
        else if (as.right() != null) {
            rhs = (String) as.right().visit(this);
            rhs = rhs.replace(DELIMITER, "");
        }
        
        stVar.add("name", lhs);
        stVar.add("val", rhs);
        stVar.add("op", op);
        
        return stVar.render();
    }
    
    @Override
    public Object visitParamDecl(ParamDecl pd) {
        Log.log(pd, "Visiting a ParamDecl (" + pd.type().typeName() + " " + pd.paramName().getname() + ")");
        
        /* Grab the type and name of a variable declaration */
        String name = (String) pd.paramName().visit(this);
        String type = (String) pd.type().visit(this);
        
        /* Silly fix for channel types */
        if (pd.type().isChannelType() || pd.type().isChannelEndType())
            type = PJChannel.class.getSimpleName() + type.substring(type.indexOf("<"), type.length());
        
        /* Create a tag for this parameter and then add it to the
         * collection of parameters for reference */
        String newName = Helper.makeVariableName(name, ++__varDecId, Tag.PARAM_NAME);
        __param2Field.put(newName, type);
        __param2VarName.put(name, newName);
        
        /* Ignored the value returned by this visitor. The types and
         * variables are _always_ resolved elsewhere */
        return null;
    }
    
    @Override
    public Object visitLocalDecl(LocalDecl ld) {
        Log.log(ld, "Visting a LocalDecl (" + ld.type().typeName() + " " + ld.var().name().getname() + ")");

        /* We could have the following targets:
         *   1.) T x;                                         // A declaration
         *   2.) T x = 4;                                     // A simple declaration
         *   3.) T x = in.read();                             // A single channel read
         *   4.) T x = a.read() + b.read() + ... + z.read();  // Multiple channel reads
         *   5.) T x = read();                                // A Java method that returns a value
         *   6.) T x = a + b;                                 // A binary expression
         *   7.) T x = a = b ...;                             // A complex assignment statement */
        String name = ld.var().name().getname();
        String type = (String) ld.type().visit(this);
        String val = null;
        
        String chantype = type;
        /* Is this a channel? e.g. one-2-one, one-2-many, many-2-one, many-to-many */
        if (ld.type().isChannelType() || ld.type().isChannelEndType())
            type = PJChannel.class.getSimpleName() + type.substring(type.indexOf("<"), type.length());

        /* Create a tag for this local declaration */
        String newName = Helper.makeVariableName(name, ++__localDecId, Tag.LOCAL_NAME);
        __local2Field.put(newName, type);
        __param2VarName.put(name, newName);
        
        /* This variable could be initialized, e.g. through an assignment operator */
        Expression expr = ld.var().init();
        /* Visit the expressions associated with this variable */
        if (expr != null) {
            if (ld.type() instanceof PrimitiveType)
                val = (String) expr.visit(this);
            else if (ld.type() instanceof NamedType) /* Must be a record or protocol */
                val = (String) expr.visit(this);
            else if (ld.type() instanceof ArrayType)
                val = (String) expr.visit(this);
        }
        
        /* Is it a barrier declaration? If so, we must generate code that
         * creates a barrier object */
        if (ld.type().isBarrierType() && expr == null) {
            ST stBarrierDecl = __stGroup.getInstanceOf("BarrierDecl");
            val = stBarrierDecl.render();
        }
        /* Is it a simple declaration for a channel type? If so, and since
         * channels cannot be created using the operator 'new', we generate
         * code to create a channel object */
        if (ld.type().isChannelType() && expr == null) {
            ST stChannelDecl = __stGroup.getInstanceOf("ChannelDecl");
            stChannelDecl.add("type", chantype);
            val = stChannelDecl.render();
        }
        /* After making this local declaration a field of the procedure
         * in which it was declared, we return iff this local variable
         * is not initialized */
        if (expr == null) {
            if (!ld.type().isBarrierType() && (ld.type().isPrimitiveType() ||
                ld.type().isArrayType() ||  /* Could be an uninitialized array declaration */
                ld.type().isNamedType()))   /* Could be a record or protocol declaration */
                return null;                /* The 'null' is used to removed empty sequences in
                                               the generated code */
        }
        
        /* If we reach this section of code, then we have a variable
         * declaration with some initial value */
        if (val != null)
            val = val.replace(DELIMITER, "");
        
        ST stVar = __stGroup.getInstanceOf("Var");
        stVar.add("name", newName);
        stVar.add("val", val);
        
        return stVar.render();
    }
    
    @Override
    public Object visitExprStat(ExprStat es) {
        Log.log(es, "Visiting an ExprStat");

        return es.expr().visit(this);
    }
    
    @Override
    public Object visitName(Name na) {
        Log.log(na, "Visiting a Name (" + na.getname() + ")");
        
        String name = null;
        
        if (!__param2VarName.isEmpty() && __param2VarName.containsKey(na.getname()))
            name = __param2VarName.get(na.getname());
        
        if (name == null)
            name = na.getname();
        
        return name;
    }
    
    @Override
    public Object visitNameExpr(NameExpr ne) {
        Log.log(ne, "Visiting a NameExpr (" + ne.name().getname() + ")");
        
        return ne.name().visit(this);
    }
    
    @Override
    public Object visitNamedType(NamedType nt) {
        Log.log(nt, "Visiting a NamedType (" + nt.name().getname() + ")");
        
        String type = nt.name().getname();
        /* Is this a protocol? Change the type to enable multiple inheritance */
        if (nt.type() != null && nt.type().isProtocolType())
            type = PJProtocolCase.class.getSimpleName();

        return type;
    }
    
    @Override
    public Object visitNewArray(NewArray ne) {
        Log.log(ne, "Visiting a NewArray");
        
        return createNewArray(null, ne);
    }
    
    @Override
    public Object visitPrimitiveType(PrimitiveType py) {
        Log.log(py, "Visiting a Primitive Type (" + py.typeName() + ")");
        
        /* ProcessJ primitive types that do not translate directly
         * to Java primitive types */
        String typeStr = py.typeName();
        if (py.isStringType())
            typeStr = "String";
        else if (py.isTimerType())
            typeStr = PJTimer.class.getSimpleName();
        else if (py.isBarrierType())
            typeStr = PJBarrier.class.getSimpleName();

        return typeStr;
    }
    
    @Override
    public Object visitPrimitiveLiteral(PrimitiveLiteral li) {
        Log.log(li, "Visiting a Primitive Literal (" + li.getText() + ")");
        
        ST stPrimitiveLiteral = __stGroup.getInstanceOf("PrimitiveLiteral");
        if (li.isSuffixed())
            stPrimitiveLiteral.add("type", li.suffix());
        stPrimitiveLiteral.add("value", li.getText());
        
        return stPrimitiveLiteral.render();
    }
    
    @Override
    public Object visitChannelType(ChannelType ct) {
        Log.log(ct, "Visiting a ChannelType (" + ct + ")");
        
        /* Channel class type */
        String chantype = "";
        switch (ct.shared()) {
        case ChannelType.NOT_SHARED:
            chantype = PJOne2OneChannel.class.getSimpleName();
            break;
        case ChannelType.SHARED_READ:
            chantype = PJOne2ManyChannel.class.getSimpleName();
            break;
        case ChannelType.SHARED_WRITE:
            chantype = PJMany2OneChannel.class.getSimpleName();
            break;
        case ChannelType.SHARED_READ_WRITE:
            chantype = PJMany2ManyChannel.class.getSimpleName();
            break;
        }
        /* Resolve parameterized type for channel, e.g. chan<T> where
         * 'T' is the type to be resolved */
        String type = getChannelType(ct.baseType());
        
        return String.format("%s<%s>", chantype, type);
    }
    
    @Override
    public Object visitChannelEndExpr(ChannelEndExpr ce) {
        Log.log(ce, "Visiting a ChannelEndExpr (" + (ce.isRead() ? "read" : "write") + ")");
        
        String channel = (String) ce.channel().visit(this);
        
        return channel;
    }
    
    @Override
    public Object visitChannelEndType(ChannelEndType ct) {
        Log.log(ct, "Visiting a ChannelEndType (" + ct.typeName() + ")");
        
        /* Channel class type */
        String chanType = PJOne2OneChannel.class.getSimpleName();
        /* Is it a shared channel? */
        if (ct.isShared()) {
            if (ct.isRead()) /* One-2-Many channel */
                chanType = PJOne2ManyChannel.class.getSimpleName();
            else if (ct.isWrite()) /* Many-2-One channel */
                chanType = PJMany2OneChannel.class.getSimpleName();
            else /* Many-2-Many channel */
                chanType = PJMany2ManyChannel.class.getSimpleName();
        }
        /* Resolve parameterized type for channels, e.g. chan<T> where
         * 'T' is the type to be resolved */
        String type = getChannelType(ct.baseType());
        
        return String.format("%s<%s>", chanType, type);
    }
    
    @Override
    public Object visitChannelWriteStat(ChannelWriteStat cw) {
        Log.log(cw, "Visiting a ChannelWriteStat");
        
        ST stChanWriteStat = __stGroup.getInstanceOf("ChanWriteStat");
        /* 'c.write(x)' is a channel-end expression, where 'c' is the
         * writing end of the channel */
        Expression chanExpr = cw.channel();
        /* 'c' is the name of the channel */
        String chanWriteName = (String) chanExpr.visit(this);
        /* Expression sent through channel */
        String expr = (String) cw.expr().visit(this);
        expr = expr.replace(DELIMITER, "");
        /* The value one is for the 'runLabel' */
        int countLabel = 1;
        /* Is the writing end of this channel shared? */
        if (chanExpr.type.isChannelEndType() && ((ChannelEndType) chanExpr.type).isShared()) {
            stChanWriteStat = __stGroup.getInstanceOf("ChannelMany2One");
            ++countLabel;
        }
        
        stChanWriteStat.add("chanName", chanWriteName);
        stChanWriteStat.add("writeExpr", expr);
        
        /* Add the switch block for resumption */
        for (int label = 0; label < countLabel; ++label) {
            /* Increment jump label and it to the switch-stmt list */
            stChanWriteStat.add("resume" + label, ++__jumpLabel);
            __switchCases.add(renderSwitchCase(__jumpLabel));
        }
        
        return stChanWriteStat.render();
    }
    
    @Override
    public Object visitChannelReadExpr(ChannelReadExpr cr) {
        Log.log(cr, "Visiting a ChannelReadExpr");
        
        ST stChannelReadExpr = __stGroup.getInstanceOf("ChannelReadExpr");
        /* 'c.read()' is a channel-end expression, where 'c' is the reading
         * end of the channel */
        Expression chanExpr = cr.channel();
        /* 'c' is the name of the channel */
        String chanEndName = (String) chanExpr.visit(this);
        stChannelReadExpr.add("chanName", chanEndName);
        /* One for the 'label' and one for the 'read' operation */
        int countLabel = 2;
        /* Add the switch block for resumption */
        for (int label = 0; label < countLabel; ++label) {
            /* Increment jump label and it to the switch-stmt list */
            stChannelReadExpr.add("resume" + label, ++__jumpLabel);
            __switchCases.add(renderSwitchCase(__jumpLabel));
        }
        
        return stChannelReadExpr.render();
    }
    
    @Override
    public Object visitVar(Var va) {
        Log.log(va, "Visiting a Var (" + va.name().getname() + ")");
        
        ST stVar = __stGroup.getInstanceOf("Var");
        /* Returned values for name and expression (if any) */
        String name = (String) va.name().visit(this);
        String exprStr = null;
        /* This variable could be initialized, e.g. through an assignment
         * operator */
        Expression expr = va.init();
        /* Visit the expressions associated with this variable */
        if (expr != null) {
            /* This is safe for when our target is not too complicated, e.g.
             * initializing variables with primitives or string literals */
            exprStr = (String) expr.visit(this);
            stVar.add("val", exprStr);
        }

        stVar.add("name", name);

        return stVar.render();
    }
    
    @Override
    public Object visitArrayAccessExpr(ArrayAccessExpr ae) {
        Log.log(ae, "Visiting an ArrayAccessExpr");
        
        ST stArrayAccessExpr = __stGroup.getInstanceOf("ArrayAccessExpr");
        String name = (String) ae.target().visit(this);
        String index = (String) ae.index().visit(this);
        
        stArrayAccessExpr.add("name", name);
        stArrayAccessExpr.add("index", index);
        
        return stArrayAccessExpr.render();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Object visitArrayLiteral(ArrayLiteral al) {
        Log.log(al, "Visiting an ArrayLiteral");
        
        /* Is the array initialize at compile time? If so, create a list
         * of values separated by commas and enclosed between braces -- the
         * syntax for array literals in ProcessJ is different to Java's */
        if (al.elements().size() > 1 || __isArrayLiteral) {
            /* The following extends naturally to two-dimensional, and
             * even higher-dimensional arrays -- but they are not used
             * very often in practice */
            String[] vals = (String[]) al.elements().visit(this);
            return Arrays.asList(vals)
                    .toString()
                    .replace("[", " { ")
                    .replace("]", " } ");
        }
        
        return al.elements().visit(this);
    }
    
    @Override
    public Object visitArrayType(ArrayType at) {
        Log.log(at, "Visiting an ArrayType (" + at.typeName() + ")");

        String stArrayType = String.format("%s[]", (String) at.baseType().visit(this));

        return stArrayType;
    }
    
    @Override
    public Object visitModifier(Modifier mo) {
        Log.log(mo, "Visiting a Modifier (" + mo + ")");
        
        /* Type of modifiers: public, protected, private, etc. */
        return mo.toString();
    }
    
    @Override
    public Object visitBlock(Block bl) {
        Log.log(bl, "Visiting a Block");
        
        /* The scope in which declarations appear, starting with their
         * own initializers and including any further declarations like
         * invocations or sequence of statements */
        String[] stats = (String[]) bl.stats().visit(this);

        return stats;
    }
    
    @Override
    @SuppressWarnings("rawtypes")
    public Object visitSequence(Sequence se) {
        Log.log(se, "Visiting a Sequence");
        
        /* Sequence of statements enclosed in a block-stmt */
        ArrayList<String> seqs = new ArrayList<String>();
        /* Iterate through every statement */
        for (int i = 0; i < se.size(); ++i) {
            if (se.child(i) != null) {
                Object stats = se.child(i).visit(this);
                if (stats == null)
                    continue;
                /* These are either
                 *   1.) a sequence of statements, or
                 *   2.) a single statement
                 * found in a block statement, e.g. local declarations,
                 * variable declarations, invocations, etc. */
                if (stats instanceof String[]) {
                    String[] statsStr = (String[]) stats;
                    seqs.addAll(Arrays.asList(statsStr));
                } else {
                    seqs.add((String) stats);
                }
            }
        }
        
        return seqs.toArray(new String[0]);
    }
    
    @Override
    public Object visitBreakStat(BreakStat bs) {
        Log.log(bs, "Visiting a BreakStat");
        
        ST stBreakStat = __stGroup.getInstanceOf("BreakStat");
        /* No parse-tree for 'break' */
        if (bs.target() != null)
            stBreakStat.add("name", bs.target().visit(this));
        
        return stBreakStat.render();
    }
    
    @Override
    public Object visitSwitchLabel(SwitchLabel sl) {
        Log.log(sl, "Visiting a SwitchLabel");
        
        ST stSwitchLabel = __stGroup.getInstanceOf("SwitchLabel");
        
        /* This could be a default label, in which case, expr() would be null */
        String label = null;
        if (!sl.isDefault())
            label = (String) sl.expr().visit(this);
        if (__isProtocCase) {
            /* The protocol tag currently being used */
            __currProtocTag = label;
            label = String.format("\"%s\"", label);
        }
        
        stSwitchLabel.add("label", label);
        
        return stSwitchLabel.render();
    }
    
    @Override
    public Object visitSwitchGroup(SwitchGroup sg) {
        Log.log(sg, "Visit a SwitchGroup");
        
        ST stSwitchGroup = __stGroup.getInstanceOf("SwitchGroup");
        
        ArrayList<String> labels = new ArrayList<String>();
        for (SwitchLabel sl : sg.labels())
            labels.add((String) sl.visit(this));
        
        ArrayList<String> stats = new ArrayList<String>();
        for (Statement st : sg.statements())
            if (st != null)
                stats.add((String) st.visit(this));
        
        stSwitchGroup.add("labels", labels);
        stSwitchGroup.add("stats", stats);
        
        return stSwitchGroup.render();
    }
    
    @Override
    public Object visitSwitchStat(SwitchStat st) {
        Log.log(st, "Visiting a SwitchStat");
        
        ST stSwitchStat = __stGroup.getInstanceOf("SwitchStat");
        /* Is this a protocol tag? */
        if (st.expr().type.isProtocolType())
            __isProtocCase = true;
        
        String expr = (String) st.expr().visit(this);
        ArrayList<String> switchGroup = new ArrayList<String>();
        
        for (SwitchGroup sg : st.switchBlocks())
            switchGroup.add((String) sg.visit(this));
        
        stSwitchStat.add("tag", __isProtocCase);
        stSwitchStat.add("expr", expr);
        stSwitchStat.add("block", switchGroup);
        
        /* Reset the value for this protocol tag */
        __isProtocCase = false;
        
        return stSwitchStat.render();
    }
    
    @Override
    public Object visitCastExpr(CastExpr ce) {
        Log.log(ce, "Visiting a CastExpr");
        
        ST stCastExpr = __stGroup.getInstanceOf("CastExpr");
        /* This result in (<type>) (<expr>) */
        String type = (String) ce.type().visit(this);
        String expr = (String) ce.expr().visit(this);
        
        stCastExpr.add("type", type);
        stCastExpr.add("expr", expr);
        
        return stCastExpr.render();
    }
    
    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object visitInvocation(Invocation in) {
        /* We ignore any GOTO or LABEL invocation because it is only needed
         * for the ASM bytecode rewrite */
        if (in.ignore) {
            Log.log(in, "Visiting a " + in.procedureName().getname());
            ST stIgnore = __stGroup.getInstanceOf("InvocationIgnore");
            stIgnore.add("name", in.procedureName().visit(this));
            stIgnore.add("var", in.params().visit(this));
            return stIgnore.render();
        }
        
        Log.log(in, "Visiting Invocation (" + in.targetProc.name().getname() + ")");
        
        ST stInvocation = null;
        /* Target procedure */
        ProcTypeDecl pd = in.targetProc;
        /* Name of invoked procedure */
        String pdName = pd.name().getname();
        /* Check local procedures, if none is found then the procedure must come
         * from a different file and maybe package */
        if (__curCompilation.fileName.equals(pd.myCompilation.fileName)) {
            String name = pdName + createProcSignature(pd);
            if (Helper.doesProcYield(pd))
                name = Helper.makeVariableName(name, 0, Tag.PROCEDURE_NAME);
            else
                name = Helper.makeVariableName(name, 0, Tag.METHOD_NAME);
            pdName = pd.myCompilation.fileNoExtension() + "." + name;
        } else if (pd.isNative) {
            /* Make the package visible on import by using the qualified name of
             * the class the procedure belongs to and the name of the directory
             * the procedure's class belongs to, e.g. std.io.println(), where
             *   1.) 'std' is the name of the package,
             *   2.) 'io' is the name of the class/file,
             *   3.) 'println' is the method declared in the class */
            pdName = pd.filename + "." + pdName;
        } else
            ; /* TODO: This procedure is called from another package */
        
        /* These are the formal parameters of the procedure/method being invoked
         * which are specified by a list of comma-separated arguments */
        Sequence<Expression> parameters = in.params();
        String[] paramsList = (String[]) parameters.visit(this);
        if (paramsList != null)
            for (int i = 0; i < paramsList.length; ++i)
                paramsList[i] = paramsList[i].replace(DELIMITER, "");
        
        /* For an invocation of a procedure that yields and one which
         * is not inside par-block, we wrap the procedure in a par-block */
        if (Helper.doesProcYield(pd) && __curParBlock == null) {
            return (new ParBlock(
                    new Sequence(new ExprStat(in)), /* Statements */
                    new Sequence()))                /* Barriers */
                    .visit(this);                   /* Return a procedure wrapped in a par-block */
        }
        
        /* Does this procedure yield? */
        if (Helper.doesProcYield(pd)) {
            stInvocation = __stGroup.getInstanceOf("InvocationProcType");
            stInvocation.add("parBlock", __curParBlock);
            /* Add the barrier this procedure should resign from */
            if (!__barriers.isEmpty())
                stInvocation.add("barrier", __barriers);
        } else
            /* Must be an invocation made through a static Java method */
            stInvocation = __stGroup.getInstanceOf("Invocation");
        
        stInvocation.add("name", pdName);
        stInvocation.add("vars", paramsList);
        
        return stInvocation.render();
    }
    
    @Override
    public Object visitImport(Import im) {
        Log.log(im, "Visiting an import statement (" + im + ")");
        
        ST stImport = __stGroup.getInstanceOf("Import");
        stImport = __stGroup.getInstanceOf("Import");
        stImport.add("package", im.toString());
        
        return stImport.render();
    }
    
    @Override
    public Object visitProtocolTypeDecl(ProtocolTypeDecl pd) {
        Log.log(pd, "Visiting a ProtocolTypeDecl (" + pd.name().getname() + ")");
        
        ST stProtocolClass = __stGroup.getInstanceOf("ProtocolClass");
        String name = (String) pd.name().visit(this);
        ArrayList<String> modifiers = new ArrayList<String>();
        ArrayList<String> body = new ArrayList<String>();
        
        for (Modifier m : pd.modifiers())
            modifiers.add((String) m.visit(this));
        
        __curProtocol = name;
        ArrayList<String> extend = new ArrayList<String>();
        /* We use tags to associate parent and child protocols */
        if (pd.extend().size() > 0) {
            for (Name n : pd.extend()) {
                extend.add(n.getname());
                ProtocolTypeDecl ptd = (ProtocolTypeDecl) __topLvlDecls.get(n.getname());
                for (ProtocolCase pc : ptd.body())
                    __protName2ProtTag.put(String.format("%s.%s", pd.name().getname(),
                            pc.name().getname()), ptd.name().getname());
            }
        }
        
        /* The scope in which all protocol members appear */
        if (pd.body() != null)
            for (ProtocolCase pc : pd.body())
                body.add((String) pc.visit(this));
        
        stProtocolClass.add("extend", extend);
        stProtocolClass.add("name", name);
        stProtocolClass.add("modifiers", modifiers);
        stProtocolClass.add("body", body);
        
        return stProtocolClass.render();
    }
    
    @Override
    public Object visitProtocolCase(ProtocolCase pc) {
        Log.log(pc, "Visiting a ProtocolCase (" + pc.name().getname() + ")");
        
        ST stProtocolType = __stGroup.getInstanceOf("ProtocolType");
        /* Since we are keeping the name of a tag as is, this (in theory)
         * shouldn't cause any name collision */
        String protocName = (String) pc.name().visit(this);
        /* This shouldn't create name collision problems even if we
         * use the same visitor for protocols and records */
        __recMember2Field.clear();
        
        /* The scope in which all members of this tag appeared */
        for (RecordMember rm : pc.body())
            rm.visit(this);
        
        /* The list of fields passed to the constructor of the static
         * class that the record belongs to */
        if (!__recMember2Field.isEmpty()) {
            stProtocolType.add("types", __recMember2Field.values());
            stProtocolType.add("vars", __recMember2Field.keySet());
        }
        
        stProtocolType.add("protType", __curProtocol);
        stProtocolType.add("name", protocName);
        
        return stProtocolType.render();
    }
    
    @Override
    public Object visitProtocolLiteral(ProtocolLiteral pl) {
        Log.log(pl, "Visiting a ProtocolLiteral (" + pl.name().getname() + ")");
        
        ST stProtocolLiteral = __stGroup.getInstanceOf("ProtocolLiteral");
        String type = (String) pl.name().visit(this);
        String tag = (String) pl.tag().visit(this);
        
        /* This map is used to determine the order in which values are
         * used with the constructor of the class associated with this
         * kind of protocol */
        HashMap<String, String> members = new LinkedHashMap<String, String>();
        /* We need the members of the tag currently being used */
        ProtocolTypeDecl pt = (ProtocolTypeDecl) __topLvlDecls.get(type);
        
        if (pt != null) {
            ProtocolCase target = pt.getCase(tag);
            /* Now that we have the target tag, iterate over all of its members */
            for (RecordMember rm : target.body()) {
                String name = rm.name().getname();
                members.put(name, null);
            }
        }
        
        /* A visit to a 'RecordLiteral' returns a string of the form
         * <var> = <val>, where <var> is a record member and <val> is
         * the value assigned to <var>. Instead of parsing the string,
         * we are going to grab the values assigned to each protocol
         * member, one by one, while traversing the tree */
        for (RecordMemberLiteral rm : pl.expressions()) {
            String lhs = rm.name().getname();
            String expr = (String) rm.expr().visit(this);
            if (members.put(lhs, expr) == null)
                Log.log(pl, "> Initializing '" + lhs + "' with '" + expr + "'");
        }
        
        stProtocolLiteral.add("type", type);
        stProtocolLiteral.add("tag", tag);
        stProtocolLiteral.add("vals", members.values());
        
        return stProtocolLiteral.render();
    }
    
    @Override
    public Object visitRecordTypeDecl(RecordTypeDecl rt) {
        Log.log(rt, "Visiting a RecordTypeDecl (" + rt.name().getname() + ")");
        
        ST stRecordType = __stGroup.getInstanceOf("RecordType");
        String recName = (String) rt.name().visit(this);
        ArrayList<String> modifiers = new ArrayList<String>();
        
        for (Modifier m : rt.modifiers())
            modifiers.add((String) m.visit(this));
        
        /* Remove fields from previous record */
        __recMember2Field.clear();
        
        /* The scope in which all members appeared in a record */
        for (RecordMember rm : rt.body())
            rm.visit(this);
        
        /* The list of fields which should be passed to the constructor
         * of the static class that the record belongs to */
        if (!__recMember2Field.isEmpty()) {
            stRecordType.add("types", __recMember2Field.values());
            stRecordType.add("vars", __recMember2Field.keySet());
        }

        ArrayList<String> extend = new ArrayList<String>();
        extend.add(recName);
        if (rt.extend().size() > 0)
            for (Name n : rt.extend())
                extend.add(n.getname());
        
        stRecordType.add("extend", extend);
        stRecordType.add("name", recName);
        stRecordType.add("modifiers", modifiers);
        
        return stRecordType.render();
    }
    
    @Override
    public Object visitRecordMember(RecordMember rm) {
        Log.log(rm, "Visiting a RecordMember (" + rm.type() + " " + rm.name().getname() + ")");
        
        String name = rm.name().getname();
        String type = (String) rm.type().visit(this);
        
        /* Add this field to the collection of record members for reference */
        __recMember2Field.put(name, type);
        
        /* Ignored the value returned by this visitor, as the types
         * and variables are _always_ resolved elsewhere */
        return null;
    }
    
    @Override
    public Object visitRecordLiteral(RecordLiteral rl) {
        Log.log(rl, "Visiting a RecordLiteral (" + rl.name().getname() + ")");
        
        ST stRecordListeral = __stGroup.getInstanceOf("RecordLiteral");
        String type = (String) rl.name().visit(this);
        
        /* This map is used to determine the order in which values
         * are passed to the constructor of the class associated
         * with this record */
        HashMap<String, String> members = new LinkedHashMap<String, String>();
        RecordTypeDecl rt = (RecordTypeDecl) __topLvlDecls.get(type);
        
        if (rt != null) {
            for (RecordMember rm : rt.body()) {
                String name = rm.name().getname();
                members.put(name, null);
            }
        }
        
        /* A visit to a 'RecordMemberLiteral' returns a string of the form
         * <var> = <val>, where <var> is a record member and <val> is the
         * value assigned to <var>. Instead of parsing the string, we are
         * going to grab the values assigned to each record member, one
         * by one, while traversing the tree */
        for (RecordMemberLiteral rm : rl.members()) {
            String lhs = rm.name().getname();
            String expr = (String) rm.expr().visit(this);
            if (members.put(lhs, expr) == null)
                Log.log(rl, "> Initializing '" + lhs + "' with '" + expr + "'");
        }
        
        stRecordListeral.add("type", type);
        stRecordListeral.add("vals", members.values());
        
        return stRecordListeral.render();
    }
    
    @Override
    public Object visitRecordAccess(RecordAccess ra) {
        Log.log(ra, "Visiting a RecordAccess (" + ra + ")");
        
        ST stAccessor = __stGroup.getInstanceOf("RecordAccessor");
        
        if (ra.record().type.isRecordType()) {
            String name = (String) ra.record().visit(this);
            String field = ra.field().getname();
            stAccessor.add("name", name);
            stAccessor.add("member", String.format("%s()", field));
        } else if (ra.record().type.isProtocolType()) {
            stAccessor = __stGroup.getInstanceOf("ProtocolAccess");
            ProtocolTypeDecl pt = (ProtocolTypeDecl) ra.record().type;
            String protocName = (String) pt.name().visit(this); /* Wrapper class */
            String name = (String) ra.record().visit(this); /* Reference to inner class type */
            String field = ra.field().getname(); /* Field in inner class */
            
            /* Cast a protocol to a super-type if needed */
            String key = String.format("%s.%s", protocName, __currProtocTag);
            if (__protName2ProtTag.get(key) != null) 
                protocName = __protName2ProtTag.get(key);
            
            stAccessor.add("protocName", protocName);
            stAccessor.add("tag", __currProtocTag);
            stAccessor.add("var", name);
            stAccessor.add("member", field);
        }
        /* This is for arrays and strings -- ProcessJ has no concept of classes,
         * i.e. it has no concept of objects either. Arrays and strings are
         * therefore treated as primitive data types */
        else {
            String name = (String) ra.record().visit(this);
            stAccessor.add("name", name);
            /* Call the appropriate method to retrieve the number of characters
             * in a string or the number of elements in an N-dimensional array */
            if (ra.isArraySize) /* 'Xxx.size' for N-dimensional array */
                stAccessor.add("member", "length");
            else if (ra.isStringLength) /* 'Xxx.length' for number of characters in a string */
                stAccessor.add("member", "length()");
        }
        
        return stAccessor.render();
    }
    
    @Override
    public Object visitParBlock(ParBlock pb) {
        Log.log(pb, "Visiting a ParBlock with " + pb.stats().size() + " statements.");
        
        /* Don't generate code for an empty par statement */
        if (pb.stats().size() == 0)
            return null;
        ST stParBlock = __stGroup.getInstanceOf("ParBlock");
        /* Save the previous par-block */
        String prevParBlock = __curParBlock;
        /* Save previous barrier expressions */
        ArrayList<String> prevBarrier = __barriers;
        if (!__barriers.isEmpty())
            __barriers = new ArrayList<String>();
        /* Create a name for this new par-block */
        __curParBlock = Helper.makeVariableName(Tag.PAR_BLOCK_NAME.toString(), ++__parDecId, Tag.LOCAL_NAME);
        /* Since this is a new par-block, we need to create a variable
         * inside the process in which this par-block was declared */
        stParBlock.add("name", __curParBlock);
        stParBlock.add("count", pb.stats().size());
        stParBlock.add("process", "this");

        /* Increment the jump label and add it to the switch-stmt list */
        stParBlock.add("jump", ++__jumpLabel);
        __switchCases.add(renderSwitchCase(__jumpLabel));
        /* Add the barrier this par-block enrolls in */
        Sequence<Expression> barriers = pb.barriers();
        if (barriers.size() > 0)
            for (Expression ex : barriers)
                __barriers.add((String) ex.visit(this));
            
        /* Visit the sequence of statements in the par-block */
        Sequence<Statement> statements = pb.stats();
        if (statements.size() > 0) {
            /* Rendered the value of each statement */
            ArrayList<String> stmts = new ArrayList<String>();
            for (Statement st : statements) {
                if (st == null)
                    continue;
                /* An expression is any valid unit of code that resolves to a value,
                 * that is, it can be a combination of variables, operations and values
                 * that yield a result. An statement is a line of code that performs
                 * some action, e.g. print statements, an assignment statement, etc. */
                if (st instanceof ExprStat && ((ExprStat) st).expr() instanceof Invocation) {
                    ExprStat es = (ExprStat) st;
                    Invocation in = (Invocation) es.expr();
                    /* If this invocation is made on a process, then visit the
                     * invocation and return a string representing the wrapper
                     * class for this procedure; e.g.
                     *    (new <classType>(...) {
                     *        @Override
                     *        public synchronized void run() { ... }
                     *        @Override
                     *        public finalize() { ... }
                     *    }.schedule(); */
                    if (Helper.doesProcYield(in.targetProc))
                        stmts.add((String) in.visit(this));
                    else /* Otherwise, the invocation is made through a static Java method */
                        stmts.add((String) createAnonymousProcTypeDecl(st).visit(this));
                } else
                    stmts.add((String) createAnonymousProcTypeDecl(st).visit(this));
            }
            stParBlock.add("body", stmts);
        }
        /* Add the barrier to the par-block */
        if (!__barriers.isEmpty() && pb.barriers().size() > 0)
            stParBlock.add("barrier", __barriers);
        /* Restore the par-block */
        __curParBlock = prevParBlock;
        /* Restore barrier expressions */
        __barriers = prevBarrier;
        
        return stParBlock.render();
    }
    
    @Override
    public Object visitTimeoutStat(TimeoutStat ts) {
        Log.log(ts, "Visiting a TimeoutStat");
        
        ST stTimeoutStat = __stGroup.getInstanceOf("TimeoutStat");
        String timer = (String) ts.timer().visit(this);
        String delay = (String) ts.delay().visit(this);
        
        stTimeoutStat.add("name", timer);
        stTimeoutStat.add("delay", delay);
        
        /* Increment the jump label and add it to the switch-stmt list */
        stTimeoutStat.add("resume0", ++__jumpLabel);
        __switchCases.add(renderSwitchCase(__jumpLabel));
        
        return stTimeoutStat.render();
    }
    
    @Override
    public Object visitSyncStat(SyncStat st) {
        Log.log(st, "Visiting a SyncStat");
        
        ST stSyncStat = __stGroup.getInstanceOf("SyncStat");
        String barrier = (String) st.barrier().visit(this);
        stSyncStat.add("barrier", barrier);
        
        /* Increment the jump label and add it to the switch-stmt list */
        stSyncStat.add("resume0", ++__jumpLabel);
        __switchCases.add(renderSwitchCase(__jumpLabel));
        
        return stSyncStat.render();
    }
    
    @Override
    public Object visitUnaryPostExpr(UnaryPostExpr ue) {
        Log.log(ue, "Visiting a UnaryPostExpr (" + ue.opString() + ")");
        
        ST stUnaryPostExpr = __stGroup.getInstanceOf("UnaryPostExpr");
        String operand = (String) ue.expr().visit(this);
        String op = ue.opString();
        
        stUnaryPostExpr.add("operand", operand);
        stUnaryPostExpr.add("op", op);
        
        return stUnaryPostExpr.render();
    }
    
    @Override
    public Object visitUnaryPreExpr(UnaryPreExpr ue) {
        Log.log(ue, "Visiting a UnaryPreExpr (" + ue.opString() + ")");
        
        ST stUnaryPreExpr = __stGroup.getInstanceOf("UnaryPreExpr");
        String operand = (String) ue.expr().visit(this);
        String op = ue.opString();
        
        stUnaryPreExpr.add("operand", operand);
        stUnaryPreExpr.add("op", op);
        
        return stUnaryPreExpr.render();
    }
    
    @Override
    public Object visitAltCase(AltCase ac) {
        Log.log(ac, "Visiting an AltCase");
        
        ST stAltCase = __stGroup.getInstanceOf("AltCase");
        Statement stat = ac.guard().guard();
        String guard = null;
        String[] stats = (String[]) ac.stat().visit(this);
        
        if (stat instanceof ExprStat)
            guard = (String) stat.visit(this);
        
        stAltCase.add("number", ac.getCaseNumber());
        stAltCase.add("guardExpr", guard);
        stAltCase.add("stats", stats);
        
        return stAltCase.render();
    }
    
    @Override
    public Object visitConstantDecl(ConstantDecl cd) {
        Log.log(cd, "Visting ConstantDecl (" + cd.type().typeName() + " " + cd.var().name().getname() + ")");
        
        ST stConstantDecl = __stGroup.getInstanceOf("ConstantDecl");
        stConstantDecl.add("type", cd.type().visit(this));
        stConstantDecl.add("var", cd.var().visit(this));
        
        return stConstantDecl.render();
    }
    
    @Override
    public Object visitAltStat(AltStat as) {
        Log.log(as, "Visiting an AltStat");
        
        ST stAltStat = __stGroup.getInstanceOf("AltStat");
        ST stBooleanGuards = __stGroup.getInstanceOf("BooleanGuards");
        ST stObjectGuards = __stGroup.getInstanceOf("ObjectGuards");
        
        Sequence<AltCase> cases = as.body();
        ArrayList<String> blocals = new ArrayList<String>();
        ArrayList<String> bguards = new ArrayList<String>();
        ArrayList<String> guards = new ArrayList<String>();
        ArrayList<String> altCases = new ArrayList<String>();
        
        /* Set boolean guards */
        for (int i = 0; i < cases.size(); ++i) {
            AltCase ac = cases.child(i);
            if (ac.precondition() == null)
                bguards.add(String.valueOf(true));
            else {
                if (ac.precondition() instanceof Literal)
                    bguards.add((String) ac.precondition().visit(this));
                else { /* This is an expression */
                    Name n = new Name("btemp");
                    LocalDecl ld = new LocalDecl(
                            new PrimitiveType(PrimitiveType.BooleanKind),
                            new Var(n, ac.precondition()),
                            false);
                    blocals.add((String) ld.visit(this));
                    bguards.add((String) n.visit(this));
                }
            }
        }

        stBooleanGuards.add("constants", bguards);
        stBooleanGuards.add("locals", blocals);
        
        /* Set case number for all AltCases */
        for (int i = 0; i < cases.size(); ++i)
            cases.child(i).setCaseNumber(i);
        /* Visit all guards */
        for (int i = 0; i < cases.size(); ++i) {
            AltCase ac = cases.child(i);
            Statement stat = ac.guard().guard();
            /* Channel read expression? */
            if (stat instanceof ExprStat) {
                Expression e = ((ExprStat) stat).expr();
                ChannelReadExpr cr = null;
                if (e instanceof Assignment)
                    cr = (ChannelReadExpr) ((Assignment) e).right();
                guards.add((String) cr.channel().visit(this));
            } else if (stat instanceof SkipStat)
                guards.add(String.format("%s.SKIP", PJAlt.class.getSimpleName()));
            
            altCases.add((String) ac.visit(this));
        }
        
        stObjectGuards.add("guards", guards);
        
        // <--
        /* This is needed because of the StackMapTable for the generated
         * Java bytecode */
        Name n = new Name("index");
        new LocalDecl(
                new PrimitiveType(PrimitiveType.IntKind),
                new Var(n, null),
                false /* not constant */).visit(this);
        /* Create a tag for this local alt declaration */
        String newName = Helper.makeVariableName("alt", ++__localDecId, Tag.LOCAL_NAME);
        __local2Field.put(newName, "PJAlt");
        __param2VarName.put(newName, newName);
        // -->
        
        stAltStat.add("alt", newName);
        stAltStat.add("count", cases.size());
        stAltStat.add("initBooleanGuards", stBooleanGuards.render());
        stAltStat.add("initGuards", stObjectGuards.render());
        stAltStat.add("bguards", "booleanGuards");
        stAltStat.add("guards", "objectGuards");
        stAltStat.add("jump", ++__jumpLabel);
        stAltStat.add("cases", altCases);
        stAltStat.add("index", n.visit(this));
        
        /* Add the jump label to the switch-stmt list */
        __switchCases.add(renderSwitchCase(__jumpLabel));
        
        return stAltStat.render();
    }
    
    @Override
    public Object visitReturnStat(ReturnStat rs) {
        Log.log(rs, "Visiting a ReturnStat");
        
        ST stReturnStat = __stGroup.getInstanceOf("ReturnStat");
        String expr = null;
        
        if (rs.expr() != null)
            expr = (String) rs.expr().visit(this);
        
        stReturnStat.add("expr", expr);
        
        return stReturnStat.render();
    }
    
    //
    // HELPER METHODS
    //
    
    /**
     * Returns the parameterized type of a Channel object.
     * 
     * @param t
     *          The specified primitive type or user-defined type.
     * @return
     *          The type parameter delimited by angle brackets.
     */
    private String getChannelType(Type t) {
        String baseType = null;
        if (t.isNamedType()) {
            NamedType nt = (NamedType) t;
            baseType = (String) nt.visit(this);
        } else if (t.isPrimitiveType()) {
            /* This is needed because we can only have wrapper class */
            baseType = Helper.getWrapperType(t);
        }
        
        return baseType;
    }
    
    /* This is used for newly-created processes */
    private void resetGlobals() {
        __parDecId = 0;
        __varDecId = 0;
        __localDecId = 0;
        __jumpLabel = 0;

        __local2Field.clear();
        __switchCases.clear();
        __barriers.clear();
        
        __param2Field.clear();
        __param2VarName.clear();
    }
    
    /* Returns a string representation of a jump label */
    private String renderSwitchCase(int jump) {
        ST stSwitchCase = __stGroup.getInstanceOf("SwitchCase");
        stSwitchCase.add("jump", jump);
        return stSwitchCase.render();
    }
    
    /**
     * Creates and returns an anonymous procedure for non-invocations.
     * 
     * @param st
     *          The statement inside the body of a procedure.
     * @return
     *          An 'anonymous' procedure.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private ProcTypeDecl createAnonymousProcTypeDecl(Statement st) {
        return new ProcTypeDecl(
                new Sequence(),               /* Modifiers */
                null,                         /* Return type */
                new Name("Anonymous"),        /* Procedure name */
                new Sequence(),               /* Formal parameters */
                new Sequence(),               /* Implement */
                null,                         /* Annotations */
                new Block(new Sequence(st))); /* Body */
    }
    
    @SuppressWarnings("unchecked")
    private Object createNewArray(String lhs, NewArray na) {
        Log.log(na.line + ": Creating a New Array");
        
        ST stNewArray = __stGroup.getInstanceOf("NewArray");
        String[] dims = (String[]) na.dimsExpr().visit(this);
        String type = (String) na.baseType().visit(this);

        ST stNewArrayLiteral = __stGroup.getInstanceOf("NewArrayLiteral");
        if (na.init() != null) {
            ArrayList<String> inits = new ArrayList<String>();
            Sequence<Expression> seq = na.init().elements();
            for (Expression e : seq) {
                if (e instanceof ArrayLiteral)
                    __isArrayLiteral = true;
                inits.add((String) e.visit(this));
            }
            stNewArrayLiteral.add("dim", String.join("", Collections.nCopies(((ArrayType) na.type).getDepth(), "[]")));
            stNewArrayLiteral.add("vals", inits);
        }
        else
            stNewArrayLiteral.add("dims", dims);
        
        stNewArray.add("name", lhs);
        stNewArray.add("type", type);
        stNewArray.add("init", stNewArrayLiteral.render());
        
        /* Reset value for array literal expression */
        __isArrayLiteral = false;
        
        return stNewArray.render();
    }
    
    private Object createRecordAssignment(Assignment as) {
        Log.log(as, "Creating a Record Assignment");
        
        ST stRecordSetter = __stGroup.getInstanceOf("RecordSetter");
        RecordAccess ra = (RecordAccess) as.left();
        if (ra.record().type.isRecordType()) {
            String name = (String) ra.record().visit(this);
            String field = ra.field().getname();
            stRecordSetter.add("name", name);
            stRecordSetter.add("member", field);
            stRecordSetter.add("val", (String) as.right().visit(this));
        }
        
        return stRecordSetter.render();
    }
    
    private Object createChannelReadExpr(String lhs, String op, ChannelReadExpr cr) {
        Log.log(cr, "Creating Channel Read Expression");
        
        ST stChannelReadExpr = __stGroup.getInstanceOf("ChannelReadExpr");
        /* 'c.read()' is a channel-end expression, where 'c' is the
         * reading end of a channel */
        Expression chanExpr = cr.channel();
        /* 'c' is the name of the channel */
        String chanEndName = (String) chanExpr.visit(this);
        
        /* Is it a timer read expression? */
        if (chanExpr.type.isTimerType()) {
            ST stTimerRedExpr = __stGroup.getInstanceOf("TimerRedExpr");
            stTimerRedExpr.add("name", lhs);
            return stTimerRedExpr.render();
        }
        
        /* One for the 'label' and one for the 'read' operation */
        int countLabel = 2;
        /* Is the reading end of this channel shared? */
        if (chanExpr.type.isChannelEndType() && ((ChannelEndType) chanExpr.type).isShared()) {
            stChannelReadExpr = __stGroup.getInstanceOf("ChannelOne2Many");
            ++countLabel;
        }
        
        /* Do we have an extended rendezvous? */
        if (cr.extRV() != null) {
            Object o = cr.extRV().visit(this);
            stChannelReadExpr.add("extendRv", o);
        }
        
        stChannelReadExpr.add("chanName", chanEndName);
        /* Add the switch block for resumption */
        for (int label = 0; label < countLabel; ++label) {
            /* Increment jump label and add it to the switch-stmt list */
            stChannelReadExpr.add("resume" + label, ++__jumpLabel);
            __switchCases.add(renderSwitchCase(__jumpLabel));
        }
        
        stChannelReadExpr.add("lhs", lhs);
        stChannelReadExpr.add("op", op);
        
        return stChannelReadExpr.render();
    }
    
    /* Returns a string representing the signature of the wrapper
     * class or Java method that encapsulates a PJProcess */
    private String createProcSignature(ProcTypeDecl pd) {
        String s = "";
        for (ParamDecl param : pd.formalParams()) {
            s = s + "$" + param.type().signature();
            /* Array [t; where 't' is the baste type */
            if (param.type().isArrayType())
                s = s.replace("[", "ar").replace(DELIMITER, "");
            else if (param.type().isRecordType()) /* <Rn; 'n' is the name */
                s = s.replace("<", "rc").replace(DELIMITER, "");
            else if (param.type().isProtocolType()) /* <Pn; 'n' is the name */
                s = s.replace("<", "pt").replace(DELIMITER, "");
            else if (param.type().isChannelType()) /* {t; where 't' is the channel type */
                s = s.replace("{", "ct").replace(DELIMITER, "");
            else if (param.type().isChannelEndType()) { /* This is for channel-end types */
                if (((ChannelEndType) param.type()).isRead()) /* {t;? -- A channel read */
                    s = s.replace("{", "cr").replace(DELIMITER, "").replace("?", "");
                else /* {t;! -- A channel write */
                    s = s.replace("{", "cw").replace(DELIMITER, "").replace("!", "");
            } else
                s = s.replace(DELIMITER, "");
        }
        return String.valueOf(s.hashCode()).replace("-", "$");
    }
}