package codegen.cpp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
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

// required for finding imports
import java.io.File;

/**
 * A tree walker that collects data from an AST object and then
 * pushes this data into a template to translate a ProcessJ source
 * file to Java code.
 *
 * @param <T>
 *          A visitor interface used to perform operations across a
 *          collection of different objects.
 * 
 * @author Ben
 * @version 06/10/2018
 * @since 1.2
 */
public class CodeGeneratorCPP extends Visitor<Object> {

    // String template file locator.
    private final String stGrammarFile = "resources/stringtemplates/cpp/grammarTemplatesCPP.stg";
    
    // Current java version.
    private final String currentJVM = System.getProperty("java.version");

    // Collection of templates, imported templates, and/or groups that
    // contain formal template definitions.
    private STGroup stGroup;
    
    // Current compilation unit.
    private Compilation currentCompilation = null;

    // The user working directory.
    private String workingDir = null;

    // Current procedure call.
    private String currentProcName = null;
    
    // Current par-block.
    private String currentParBlock = null;
    
    // All imports are kept in this table.
    private HashSet<String> importFiles = new LinkedHashSet<String>();
    
    // Top level declarations.
    private SymbolTable topLevelDecls = null;

    // Contains formal parameters transformed to fields.
    private HashMap<String, String> formalParams = new LinkedHashMap<String, String>();
    
    // Contains formal parameter names transformed to name tags.
    private HashMap<String, String> paramDeclNames = new LinkedHashMap<String, String>();
    
    // Contains local parameters transformed to fields.
    private HashMap<String, String> localParams = new LinkedHashMap<String, String>();
    
    // Contains record members transformed to fields.
    private HashMap<String, String> recordFields = new LinkedHashMap<String, String>();

    // Contains protocol names and the corresponding tags currently switched on.
    private Hashtable<String, String> protocolTagsSwitchedOn = new Hashtable<String, String>();
    
    // List of switch labels.
    private ArrayList<String> switchLabelList = new ArrayList<String>();
    
    // List of barrier expressions.
    private ArrayList<String> barrierList = new ArrayList<String>();

    // Identifier for parameter declaration.
    private int varDecId = 0;
    
    // Identifier for par-block declaration.
    private int parDecId = 0;

    // Identifier for local variable declaration.
    private int localDecId = 0;
    
    // Jump label used when procedures yield.
    private int jumpLabel = 0;
    
    // Access to protocol case.
    private boolean isProtocolCase = false;
    
    // Access to protocol tag.
    private String currentProtocolTag = null;
    
    // This is used for arrays of N-dimensions.
    private boolean isArrayLiteral = false;
    
    private final static String DELIMITER = ";";

    /**
     * Internal constructor that loads a group file containing a collection of
     * templates, imported templates, and/or groups containing formal template
     * definitions. Additionally, the constructor initializes a symbol table
     * with top level types declarations.
     * 
     * @param s
     * 			The top level types which can be procedures, records, protocols,
     * 			constants, and/or external types.
     */
    public CodeGeneratorCPP(SymbolTable s) {
        Log.logHeader("*****************************************");
        Log.logHeader("* C O D E - G E N E R A T O R   C P P   *");
        Log.logHeader("*****************************************");
        
        topLevelDecls = s;
        stGroup = new STGroupFile(stGrammarFile);
    }
    
    /**
     * Sets the system properties to a current working directory.
     *
     * @param workingDir
     * 			A working directory.
     */
    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }
    
    /**
     * Return a string representing the current working directory.
     */
    public String getWorkingDir() {
        return workingDir;
    }
    
    /**
     * Visit a single compilation unit which starts with an optional package
     * declaration, followed by zero or more import declarations, followed
     * by zero or more type declarations.
     *
     * @param co
     * 			An AST that represents the entire compilation unit.
     * @return A text generated after evaluating this compilation unit.
     */
    @Override
    public Object visitCompilation(Compilation co) {
        Log.log(co, "Visiting a Compilation");
        
        currentCompilation = co;
        // Code generated by the template.
        String code = null;
        // Instance of Compilation template to fill in.
        ST stCompilation = stGroup.getInstanceOf("Compilation");
        // Reference to all remaining types.
        ArrayList<String> body = new ArrayList<String>();
        // Holds all top level types declarations.
        Sequence<Type> typeDecls = co.typeDecls();
        // Package name for this source file.
        String packagename = co.packageNoName();
        
        for (Import im : co.imports()) {
        	if (im != null)
        		importFiles.add((String) im.visit(this));
        }
        
        for (AST decl : typeDecls) {
            if (decl instanceof Type) {
                // Collect procedures, records, protocols, and external types (if any).
                String t = (String) ((Type) decl).visit(this);
                if (t != null)
                    body.add(t);
            } else if (decl instanceof ConstantDecl) {
                // Iterate over remaining declarations, which is anything that
                // comes after top-level declarations.
                String cd = (String) ((ConstantDecl) decl).visit(this);
                if (cd != null)
                    body.add(cd);
            }
        }

        stCompilation.add("pathName", packagename);
        stCompilation.add("fileName", co.fileName);
        stCompilation.add("name", co.fileNoExtension());
        stCompilation.add("body", body);
        stCompilation.add("version", currentJVM);
        
        // Add the import statements to the file.
        if (importFiles.size() > 0)
            stCompilation.add("imports", importFiles);

        // Rendered code for debbuging.
        code = stCompilation.render();
        
        // Code generated by the ProcessJ-CPP compiler.
        Log.logHeader("****************************************");
        Log.logHeader("*      G E N E R A T E D   C O D E     *");
        Log.logHeader("****************************************");
        Log.logHeader(code);
        
        return code;
    }
    
    @Override
    public Object visitProcTypeDecl(ProcTypeDecl pd) {
        Log.log(pd, "Visiting a ProcTypeDecl (" + pd.name().getname() + ")");
        
        // Generated template after evaluating this visitor.
        ST stProcTypeDecl = null;
        // Save previous procedure.
        String prevProcName = currentProcName;
        // Save previous jump labels.
        ArrayList<String> prevLabels = switchLabelList;
        if (!switchLabelList.isEmpty())
            switchLabelList = new ArrayList<String>();
        // Name of invoked procedure.
        currentProcName = (String) pd.name().visit(this);
        // Procedures are static classes which belong to the same package. To avoid
        // having classes with the same name, we generate a new name for this procedure.
        String procName = null;
        // For non-invocations, that is, for anything other than a procedure
        // that yields, we need to extends the PJProcess class anonymously.
        if ("Anonymous".equals(currentProcName)) {
            // Preserve current jump label.
            int prevJumLabel = jumpLabel;
            jumpLabel = 0;
            // Grab the instance for an anonymous procedure.
            stProcTypeDecl = stGroup.getInstanceOf("AnonymousProcess");
            // Statements that appear in the procedure.
            String[] body = (String[]) pd.body().visit(this);
            
            stProcTypeDecl.add("parBlock", currentParBlock);
            stProcTypeDecl.add("syncBody", body);
            // Add the barrier this procedure should resign from.
            if (!barrierList.isEmpty())
                stProcTypeDecl.add("barrier", barrierList);
            // Add the switch block.
            if (!switchLabelList.isEmpty()) {
                ST stSwitchBlock = stGroup.getInstanceOf("SwitchBlock");
                stSwitchBlock.add("jumps", switchLabelList);
                stProcTypeDecl.add("switchBlock", stSwitchBlock.render());
            }

            // Restore jump label.
            jumpLabel = prevJumLabel;
        } else {
            // Restore global variables for a new PJProcess class.
            resetGlobals();
            // Formal parameters that must be passed to the procedure.
            Sequence<ParamDecl> formals = pd.formalParams();

            if (formals != null && formals.size() > 0) {
                // Iterate through and visit every parameter declaration.
                for (int i = 0; i < formals.size(); ++i) {
                    ParamDecl actualParam = formals.child(i);
                    // Retrieve the name and type of a parameter in the parameter list.
                    // Note that we ignored the value returned by this visitor.
                    actualParam.visit(this);
                }
            } else
                ; // The procedure does not take any parameters.
            
            // Visit all declarations that appear in the procedure.
            String[] body = (String[]) pd.body().visit(this);

            // Retrieve modifier(s) attached to invoked procedure such as private,
            // public, protected, etc.
            String[] modifiers = (String[]) pd.modifiers().visit(this);
            // Grab the return type of the invoked procedure.
            String procType = (String) pd.returnType().visit(this);
            // The procedure's annotation determines if we have a yielding procedure
            // or a Java method (a non-yielding procedure).
            boolean doYield = Helper.doesProcedureYield(pd);
            // Set the template to the correct instance value and then initialize
            // its attributes.
            if (doYield) {
                // This procedure yields! Grab the instance of a yielding procedure
                // from the string template in order to define a new class.
                procName = Helper.makeVariableName(currentProcName + signature(pd), 0, Tag.PROCEDURE_NAME);
                stProcTypeDecl = stGroup.getInstanceOf("ProcClass");
                stProcTypeDecl.add("name", procName);
                // The statements that appear in the body of the procedure.
                stProcTypeDecl.add("syncBody", body);
            } else {
                // Otherwise, grab the instance of a non-yielding procedure instead
                // to define a new static Java method.
                procName = Helper.makeVariableName(currentProcName + signature(pd), 0, Tag.METHOD_NAME);
                stProcTypeDecl = stGroup.getInstanceOf("Method");
                stProcTypeDecl.add("name", procName);
                stProcTypeDecl.add("type", procType);
                // Do we have any modifier?
                if (modifiers != null && modifiers.length > 0)
                    stProcTypeDecl.add("modifier", modifiers);
                stProcTypeDecl.add("body", body);
            }
            
            // Create an entry point for the ProcessJ program, which is just
            // a Java main method that is called by the JVM.
            if ("main".equals(currentProcName) && pd.signature().equals(Tag.MAIN_NAME.toString())) {
                // Create an instance of a Java main method template.
                ST stMain = stGroup.getInstanceOf("Main");
                stMain.add("class", currentCompilation.fileNoExtension());
                stMain.add("name", procName);
                // Pass the list of command line arguments to this main method.
                if (!formalParams.isEmpty()) {
                    stMain.add("types", formalParams.values());
                    stMain.add("vars", formalParams.keySet());
                }
                // Add entry point of the program.
                stProcTypeDecl.add("main", stMain.render());
            }
            
            // The list of command line arguments should be passed to the constructor
            // of the static class that the main method belongs to (some procedure class)
            // or should be passed to the Java method (some static method).
            if (!formalParams.isEmpty()) {
                stProcTypeDecl.add("types", formalParams.values());
                stProcTypeDecl.add("vars", formalParams.keySet());
            }
            // The list of local variables defined in the body of a procedure becomes
            // the member variables of the procedure class.
            if (!localParams.isEmpty()) {
                stProcTypeDecl.add("ltypes", localParams.values());
                stProcTypeDecl.add("lvars", localParams.keySet());
            }
            // Add the switch block for resumption.
            if (!switchLabelList.isEmpty()) {
                ST stSwitchBlock = stGroup.getInstanceOf("SwitchBlock");
                // stSwitchBlock.add("jumps", switchLabelList);

                Log.log(pd, "the switchLabelList is (before):\n");
                for (int i = 0; i < switchLabelList.size(); i++) {
                    Log.log(switchLabelList.get(i));
                }
                // Append the name of the process to the labels
                String str;
                ArrayList<String> newSwitchLabelList = new ArrayList<String>();
                for(int i = 0; i < switchLabelList.size(); i++) {
                    /* TODO: split label like:
                     * "case 0: goto " + procName +  "LN; break;"
                     * by making a substring upto LN and wrapping them
                     * around the procName
                     */
                    // find end index of the substring, "case <num>: goto " by finding
                    // where "goto" is and going 1 past that (usable whitespace)
                    int gotoIndex = switchLabelList.get(i).lastIndexOf("goto") + 5;
                    // make our first substring from that index
                    str = switchLabelList.get(i).substring(0, gotoIndex);
                    Log.log(pd, "my substring is " + str);
                    // make the new string by combining our beginning substring,
                    // the procName variable, and the last index of "goto" onwards
                    String newstr = str +
                                    procName +
                                    switchLabelList.get(i).substring(gotoIndex, switchLabelList.get(i).length());
                    Log.log(pd, "my new label is " + newstr);
                    newSwitchLabelList.add(i, newstr);
                }
                Log.log(pd, "the switchLabelList is (after):\n");
                for (int i = 0; i < newSwitchLabelList.size(); i++) {
                    Log.log(newSwitchLabelList.get(i));
                }

                stSwitchBlock.add("jumps", newSwitchLabelList);
                stSwitchBlock.add("name", procName);
                stProcTypeDecl.add("switchBlock", stSwitchBlock.render());
            }
        }
        
        // Restore and reset previous values.
        currentProcName = prevProcName;

        // Restore previous jump labels.
        switchLabelList = prevLabels;

        return stProcTypeDecl.render();
    }
    
    @Override
    public Object visitBinaryExpr(BinaryExpr be) {
        Log.log(be, "Visiting a BinaryExpr");

        // Generated template after evaluating this visitor.
        ST stBinaryExpr = stGroup.getInstanceOf("BinaryExpr");
        String op = be.opString();
        String lhs = (String) be.left().visit(this);
        lhs = lhs.replace(DELIMITER, "");
        lhs = be.left().hasParens ? "(" + lhs + ")" : lhs;
        String rhs = (String) be.right().visit(this);
        rhs = be.right().hasParens ? "(" + rhs + ")" : rhs;
        rhs = rhs.replace(DELIMITER, "");
        
        stBinaryExpr.add("lhs", lhs);
        stBinaryExpr.add("rhs", rhs);
        stBinaryExpr.add("op", op);

        return stBinaryExpr.render();
    }
    
    @Override
    public Object visitWhileStat(WhileStat ws) {
        Log.log(ws, "Visiting a WhileStat");
        
        // Generated template after evaluating this visitor.
        ST stWhileStat = stGroup.getInstanceOf("WhileStat");
        // Sequence of statements enclosed in a block statement.
        String[] stats = null;
        String condExpr = null;
        
        if (ws.expr() != null)
            condExpr = ((String) ws.expr().visit(this)).replace(DELIMITER, "");
        
        if (ws.stat() != null) {
            Object o = ws.stat().visit(this);
            if (o instanceof String)
            	stats = new String[] { (String) o };
            else
            	stats = (String[]) o;
        } else // The body of a while-loop could be empty.
            stats = new String[] { DELIMITER };
        
        stWhileStat.add("expr", condExpr);
        stWhileStat.add("body", stats);
        
        return stWhileStat.render();
    }
    
    @Override
    public Object visitDoStat(DoStat ds) {
        Log.log(ds, "Visiting a DoStat");
        
        // Generated template after evaluating this visitor.
        ST stDoStat = stGroup.getInstanceOf("DoStat");
        // Sequence of statements enclosed in a block statement.
        String[] stats = null;
        String condExpr = null;
        
        if (ds.expr() != null)
            condExpr = ((String) ds.expr().visit(this)).replace(DELIMITER, "");
        
        if (ds.stat() != null) {
            Object o = ds.stat().visit(this);
            if (o instanceof String)
            	stats = new String[] { (String) o };
            else
            	stats = (String[]) o;
        }
        
        stDoStat.add("expr", condExpr);
        stDoStat.add("body", stats);
        
        return stDoStat.render();
    }
    
    @Override
    public Object visitForStat(ForStat fs) {
        Log.log(fs, "Visiting a ForStat");
        
        // Generated template after evaluating this visitor.
        ST stForStat = stGroup.getInstanceOf("ForStat");
        ArrayList<String> init = new ArrayList<String>();  // Initialization part.
        ArrayList<String> incr = new ArrayList<String>();  // Increment part.
        
        if (!fs.isPar()) { // Is it a regular for loop?
            if (fs.init() != null) {
                for (Statement st : fs.init()) {
                    if (st != null)
                        init.add(((String) st.visit(this)).replace(";", ""));  // Remove the ';' added in LocalDecl.
                }
            }
            
            if (fs.incr() != null) {
                for (ExprStat expr : fs.incr())
                    incr.add(((String) expr.visit(this)).replace(";", ""));
            }
            
            if (fs.expr() != null) {
                String expr = ((String) fs.expr().visit(this)).replace(";", "");
                stForStat.add("expr", expr);
            }

            // Sequence of statements enclosed in a block statement.
            if (fs.stats() != null) {
                Object o = fs.stats().visit(this);
                stForStat.add("stats", o);
            }
        } else // No! then this is a par-for.
            ;
        
        if (!init.isEmpty())
            stForStat.add("init", init);
        if (!incr.isEmpty())
            stForStat.add("incr", incr);
        
        return stForStat.render();
    }
    
    @Override
    public Object visitContinueStat(ContinueStat cs) {
        Log.log(cs, "Visiting a ContinueStat");
        
        // Generated template after evaluating this visitor.
        ST stContinueStat = stGroup.getInstanceOf("ContinueStat");
        String name = null;
        // If target is not 'null' then we have a label to jump to.
        if (cs.target() != null) {
            name = (String) cs.target().visit(this);
            stContinueStat.add("name", name);
        }
        
        return stContinueStat.render();
    }
    
    @Override
    public Object visitIfStat(IfStat is) {
        Log.log(is, "Visiting a IfStat");

        // Generated template after evaluating this visitor.
        ST stIfStat = stGroup.getInstanceOf("IfStat");
        // Sequence of statements enclosed in a block statement.
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
        
        // Generated template after evaluating this visitor.
        ST stVar = stGroup.getInstanceOf("Var");
        
        String op = (String) as.opString();
        String lhs = null;
        String rhs = null;
        
        if (as.left() != null)
            lhs =  (String) as.left().visit(this);
        
        if (as.right() instanceof NewArray)
            return createNewArray(lhs, ((NewArray) as.right()));
        else if (as.right() instanceof ChannelReadExpr)
            return createChannelReadExpr(lhs, op, ((ChannelReadExpr) as.right()));
        else {
            if (as.right() != null) {
                rhs = (String) as.right().visit(this);
                rhs = rhs.replace(DELIMITER, "");
            }
        }
        
        stVar.add("name", lhs);
        stVar.add("val", rhs);
        stVar.add("op", op);
        
        return stVar.render();
    }
    
    @Override
    public Object visitParamDecl(ParamDecl pd) {
        Log.log(pd, "Visiting a ParamDecl (" + pd.type().typeName() + " " + pd.paramName().getname() + ")");
        
        // Grab the type and name of a declared variable.
        String name = (String) pd.paramName().visit(this);
        String type = (String) pd.type().visit(this);
        
        // Temporary _silly_ fixed for channel types.
        if (pd.type().isChannelType() || pd.type().isChannelEndType())
            type = PJChannel.class.getSimpleName() + type.substring(type.indexOf("<"), type.length());
        
        // Create a tag for this parameter and then add it to the collection
        // of parameters for reference.
        String newName = Helper.makeVariableName(name, ++varDecId, Tag.PARAM_NAME);
        formalParams.put(newName, type);
        paramDeclNames.put(name, newName);
        
        // Ignored the value returned by this visitor. The types and variables
        // are _always_ resolved elsewhere.
        return null;
    }
    
    @Override
    public Object visitLocalDecl(LocalDecl ld) {
        Log.log(ld, "Visting a LocalDecl (" + ld.type().typeName() + " " + ld.var().name().getname() + ")");

        // We could have the following targets:
        //      T x;                              -> a declaration
        //      T x = 4;                          -> a simple declaration
        //      T x = in.read();                  -> a single channel read
        //      T x = b.read() + c.read() + ...;  -> multiple channel reads
        //      T x = read();                     -> a Java method that returns a value
        //      T x = a + b;                      -> a binary expression
        //      T x = a = b ...;                  -> a complex assignment
        String name = (String) ld.var().name().getname();
        String type = (String) ld.type().visit(this);
        String val = null;
        
        String chantype = type;
        // The channel type, e.g., one-2-one, one-2-many, many-2-one, many-to-many.
        if (ld.type().isChannelType() || ld.type().isChannelEndType())
            type = PJChannel.class.getSimpleName() + type.substring(type.indexOf("<"), type.length());

        // Create a tag for this local channel expression parameter.
        String newName = Helper.makeVariableName(name, ++localDecId, Tag.LOCAL_NAME);
        localParams.put(newName, type);
        paramDeclNames.put(name, newName);
        
        // This variable could be initialized, e.g., through an assignment operator.
        Expression expr = ld.var().init();
        // Visit the expressions associated with this variable.
        if (expr != null) {
            if (ld.type() instanceof PrimitiveType)
                val = (String) expr.visit(this);
            else if (ld.type() instanceof NamedType) // Must be a record or protocol.
                val = (String) expr.visit(this);
            else if (ld.type() instanceof ArrayType)
                val = (String) expr.visit(this);
        }
        
        // Is it a barrier declaration? If so, we must generate code that
        // creates a barrier object.
        if (ld.type().isBarrierType() && expr == null) {
            ST stBarrierDecl = stGroup.getInstanceOf("BarrierDecl");
            val = stBarrierDecl.render();
        }
        // Is it a simple declaration for a channel type? If so, and since
        // channels cannot be created using the operator 'new', we generate
        // code to create a channel object.
        if (ld.type().isChannelType() && expr == null) {
            ST stChannelDecl = stGroup.getInstanceOf("ChannelDecl");
            stChannelDecl.add("type", chantype);
            val = stChannelDecl.render();
        }
        // After making this local declaration a field of the procedure
        // in which it was declared, we return if and only if this local
        // variable is not initialized.
        if (expr == null) {
            if (!ld.type().isBarrierType() && (ld.type().isPrimitiveType() ||
                ld.type().isArrayType() ||  // Could be an uninitialized array declaration.
                ld.type().isNamedType()))   // Could be records or protocols.
                return null;                // The 'null' is used to removed empty sequences.
        }
        
        // If we reach this section of code, then we have a variable
        // declaration with some initial value(s).
        if (val != null)
            val = val.replace(DELIMITER, "");
        
        ST stVar = stGroup.getInstanceOf("Var");
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
        
        if (!paramDeclNames.isEmpty() && paramDeclNames.containsKey(na.getname()))
            name = paramDeclNames.get(na.getname());
        
        if (name == null)
            name = na.getname();
        
        return name;
    }
    
    @Override
    public Object visitNameExpr(NameExpr ne) {
        Log.log(ne, "Visiting a NameExpr (" + ne.name().getname() + ")");
        
        // NameExpr always points to 'myDecl'.
        return ne.name().visit(this);
    }
    
    @Override
    public Object visitNamedType(NamedType nt) {
        Log.log(nt, "Visiting a NamedType (" + nt.name().getname() + ")");
        
        String type = (String) nt.name().getname();
        // This is for protocol inheritance.
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
        
        // ProcessJ primitive types that do not translate directly
        // to Java primitive types.
        String typeStr = py.typeName();
        if (py.isStringType())
            typeStr = "std::string";
        else if (py.isTimerType())
            typeStr = PJTimer.class.getSimpleName();
        else if (py.isBarrierType())
            typeStr = PJBarrier.class.getSimpleName();

        return typeStr;
    }
    
    @Override
    public Object visitPrimitiveLiteral(PrimitiveLiteral li) {
        Log.log(li, "Visiting a Primitive Literal (" + li.getText() + ")");
        
        ST stPrimitiveLiteral = stGroup.getInstanceOf("PrimitiveLiteral");
        if (li.isSuffixed())
            stPrimitiveLiteral.add("type", li.suffix());
        stPrimitiveLiteral.add("value", li.getText());
        
        return stPrimitiveLiteral.render();
    }
    
    @Override
    public Object visitChannelType(ChannelType ct) {
        Log.log(ct, "Visiting a ChannelType (" + ct + ")");
        
        // Channel class type.
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
        // Resolve parameterized type for channel, e.g., chan<T>
        // where 'T' is the type to be resolved.
        String type = getChannelType(ct.baseType());
        
        return chantype + "<" + type + ">";
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
        
        // Channel class type.
        String chanType = PJOne2OneChannel.class.getSimpleName();
        if (ct.isShared()) {  // Is it a shared channel?
            if (ct.isRead())  // One-2-many channel.
                chanType = PJOne2ManyChannel.class.getSimpleName();
            else if (ct.isWrite()) // Many-2-one channel.
                chanType = PJMany2OneChannel.class.getSimpleName();
            else // Many-2-many channel.
                chanType = PJMany2ManyChannel.class.getSimpleName();
        }
        // Resolve parameterized type for channels, e.g., chan<T>
        // where 'T' is the type to be resolved.
        String type = getChannelType(ct.baseType());
        
        return chanType + "<" + type + ">";
    }
    
    @Override
    public Object visitChannelWriteStat(ChannelWriteStat cw) {
        Log.log(cw, "Visiting a ChannelWriteStat");
        
        // Generated template after evaluating this visitor.
        ST stChanWriteStat = stGroup.getInstanceOf("ChanWriteStat");
        // 'c.write(x)' is a channel-end expression, where 'c'
        // is the writing end of a channel.
        Expression chanExpr = cw.channel();
        // 'c' is the name of the channel.
        String chanWriteName = (String) chanExpr.visit(this);
        // Expression sent through channel.
        String expr = (String) cw.expr().visit(this);
        expr = expr.replace(DELIMITER, "");
        
        int countLabel = 1; // One for the 'runLabel'.
        // Is the writing end of this channel shared?
        if (chanExpr.type.isChannelEndType() && ((ChannelEndType) chanExpr.type).isShared()) {
            stChanWriteStat = stGroup.getInstanceOf("ChannelMany2One");
            ++countLabel;
        }
        
        stChanWriteStat.add("chanName", chanWriteName);
        stChanWriteStat.add("writeExpr", expr);
        
        // Add the switch block for resumption.
        for (int label = 0; label < countLabel; ++label) {
            // Increment the jump label.
            stChanWriteStat.add("resume" + label, ++jumpLabel);
            // Add the jump label to the switch list.
            switchLabelList.add(renderSwitchLabel(jumpLabel));
        }
        
        return stChanWriteStat.render();
    }
    
    @Override
    public Object visitChannelReadExpr(ChannelReadExpr cr) {
        Log.log(cr, "Visiting a ChannelReadExpr");
        
        // Generated template after evaluating this visitor.
        ST stChannelReadExpr = stGroup.getInstanceOf("ChannelReadExpr");
        // 'c.read()' is a channel-end expression, where 'c'
        // is the reading end of a channel.
        Expression chanExpr = cr.channel();
        // 'c' is the name of the channel.
        String chanEndName = (String) chanExpr.visit(this);
        stChannelReadExpr.add("chanName", chanEndName);
        // Add the 'switch' block for resumption.
        for (int label = 0; label < 2; ++label) {
            // Increment jump label.
            stChannelReadExpr.add("resume" + label, ++jumpLabel);
            // Add jump label to the switch list.
            switchLabelList.add(renderSwitchLabel(jumpLabel));
        }
        
        return stChannelReadExpr.render();
    }
    
    @Override
    public Object visitVar(Var va) {
        Log.log(va, "Visiting a Var (" + va.name().getname() + ")");

        // Generated template after evaluating this visitor.
        ST stVar = stGroup.getInstanceOf("Var");
        // Returned values for name and expression (if any).
        String name = (String) va.name().visit(this);
        String exprStr = null;
        // This variable could be initialized, e.g., through an assignment
        // operator.
        Expression expr = va.init();
        // Visit the expressions associated with this variable.
        if (expr != null) {
            // This is safe for when our target is not too complicated, e.g.,
            // initializing variables with primitives or string literals.
            exprStr = (String) expr.visit(this);
            stVar.add("val", exprStr);
        }

        stVar.add("name", name);

        return stVar.render();
    }
    
    @Override
    public Object visitArrayAccessExpr(ArrayAccessExpr ae) {
        Log.log(ae, "Visiting an ArrayAccessExpr");
        
        // Generated template after evaluating this visitor.
        ST stArrayAccessExpr = stGroup.getInstanceOf("ArrayAccessExpr");
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
        
        // Is the array initialize at compile time? If so, create a
        // list of values separated by commas and enclosed between
        // braces (note, the syntax for array literals in ProcessJ
        // is different to Java's).
        if (al.elements().size() > 1 || isArrayLiteral) {
            // The following extends naturally to two-dimensional, and
            // even higher-dimensional arrays -- but they are not used
            // very often in practice.
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

        /* NOTE: still need to add brackets _after_ the name of the array,
         * but this isn't the place to do it
         */
        // String stArrayType = (String) at.baseType().visit(this) + "[]";
        String stArrayType = (String) at.baseType().visit(this);

        return stArrayType;
    }
    
    @Override
    public Object visitModifier(Modifier mo) {
        Log.log(mo, "Visiting a Modifier (" + mo + ")");
        
        // Type of modifiers: public, protected, private, etc.
        return mo.toString();
    }
    
    @Override
    public Object visitBlock(Block bl) {
        Log.log(bl, "Visiting a Block");
        
        // The scope in which declarations appear, starting with their
        // own initializers and including any further declarations such
        // invocations or sequence of statements.
        String[] stats = (String[]) bl.stats().visit(this);

        return stats;
    }
    
    @Override
    @SuppressWarnings("rawtypes")
    public Object visitSequence(Sequence se) {
        Log.log(se, "Visiting a Sequence");
        
        // Sequence of statements enclosed in a block statement.
        ArrayList<String> seqs = new ArrayList<String>();
        // Iterate through every statement.
        for (int i = 0; i < se.size(); ++i) {
            if (se.child(i) != null) {
                Object stats = se.child(i).visit(this);
                if (stats == null)
                    continue;
                // These are either
                //      1.) a sequence of statements, or
                //      2.) a single statement
                // found in a block statement, e.g. local declarations,
                // variable declarations, invocations, etc.
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
        
        // Generated template after evaluating this visitor.
        ST stBreakStat = stGroup.getInstanceOf("BreakStat");
        
        if (bs.target() != null) // No parse tree for 'break'.
            stBreakStat.add("name", bs.target().visit(this));
        
        return stBreakStat.render();
    }
    
    @Override
    public Object visitSwitchLabel(SwitchLabel sl) {
        Log.log(sl, "Visiting a SwitchLabel");
        
        // Generated template after evaluating this visitor.
        ST stSwitchLabel = stGroup.getInstanceOf("SwitchLabel");
        
        // This could be a default label, in which case, expr()
        // would be 'null'.
        String label = null;
        if (!sl.isDefault())
            label = (String) sl.expr().visit(this);
        if (isProtocolCase) {
            // Silly way to keep track of a protocol tag, however, this
            // should (in theory) _always_ work.
            currentProtocolTag = label;
            label = "\"" + label + "\"";
        }
        stSwitchLabel.add("label", label);
        
        return stSwitchLabel.render();
    }
    
    @Override
    public Object visitSwitchGroup(SwitchGroup sg) {
        Log.log(sg, "Visit a SwitchGroup");
        
        // Generated template after evaluating this visitor.
        ST stSwitchGroup = stGroup.getInstanceOf("SwitchGroup");

        ArrayList<String> labels = new ArrayList<String>();
        for (SwitchLabel sl : sg.labels())
            labels.add((String) sl.visit(this));
        
        ArrayList<String> stats = new ArrayList<String>();
        for (Statement st : sg.statements()) {
            if (st == null)
                continue;
            stats.add((String) st.visit(this));
        }
        
        stSwitchGroup.add("labels", labels);
        stSwitchGroup.add("stats", stats);
        
        return stSwitchGroup.render();
    }
    
    @Override
    public Object visitSwitchStat(SwitchStat st) {
        Log.log(st, "Visiting a SwitchStat");
        
        // Generated template after evaluating this visitor.
        ST stSwitchStat = stGroup.getInstanceOf("SwitchStat");
        // Is this a protocol tag?
        if (st.expr().type.isProtocolType())
            isProtocolCase = true;
        
        String expr = (String) st.expr().visit(this);
        ArrayList<String> switchGroup = new ArrayList<String>();
        
        for (SwitchGroup sg : st.switchBlocks())
            switchGroup.add((String) sg.visit(this));
        
        stSwitchStat.add("tag", isProtocolCase);
        stSwitchStat.add("expr", expr);
        stSwitchStat.add("block", switchGroup);
        
        // Reset the value for this protocol tag.
        isProtocolCase = false;
        
        return stSwitchStat.render();
    }
    
    @Override
    public Object visitCastExpr(CastExpr ce) {
        Log.log(ce, "Visiting a CastExpr");
        
        // Generated template after evaluating this invocation.
        ST stCastExpr = stGroup.getInstanceOf("CastExpr");
        // This result in:
        //      ((<type>) (<expr>))
        String type = (String) ce.type().visit(this);
        String expr = (String) ce.expr().visit(this);
        
        stCastExpr.add("type", type);
        stCastExpr.add("expr", expr);
        
        return stCastExpr.render();
    }
    
    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object visitInvocation(Invocation in) {
    	// Ignore anything that is not a procedure invocation.
    	if (in.ignore) {
    		Log.log(in, "Visiting a label/goto point.");
    		
    		ST stIgnore = stGroup.getInstanceOf("InvocationIgnore");
    		stIgnore.add("name", in.procedureName().visit(this));
    		stIgnore.add("var", in.params().visit(this));
    		
    		return stIgnore.render();
    	}
    	
        Log.log(in, "Visiting Invocation (" + in.targetProc.name().getname() + ")");
        
        // Generated template after evaluating this invocation.
        ST stInvocation = null;
        // Target procedure.
        ProcTypeDecl pd = in.targetProc;
        // Name of invoked procedure.
        String pdName = pd.name().getname();
        // Check local procedures, if none is found then the procedure must come
        // from a different file and maybe package.
        if (currentCompilation.fileName.equals(pd.myCompilation.fileName)) {
            String name = pdName + signature(pd);
            if (Helper.doesProcedureYield(pd))
                name = Helper.makeVariableName(name, 0, Tag.PROCEDURE_NAME);
            else
                name = Helper.makeVariableName(name, 0, Tag.METHOD_NAME);
            // pdName = pd.myCompilation.fileNoExtension() + "." + name;
            pdName = name;
        } else if (pd.isNative) {
            // Make the package visible on import by using the qualified name of
            // the class the procedure belongs to and the name of the directory
            // the procedure's class belongs to, e.g., std.io.println(), where
            //      1.) 'std' is the name of the package,
            //      2.) 'io' is the name of the class/file,
            //      3.) 'println' is the method declared in the class
            pdName = pd.filename + "." + pdName;
        } else
            ; // This procedure is called from another package.
        
        // These are the formal parameters of a procedure/method which are specified
        // by a list of comma-separated arguments.
        Sequence<Expression> parameters = in.params();
        String[] paramsList = (String[]) parameters.visit(this);
        if (paramsList != null) {
            for (int i = 0; i < paramsList.length; ++i)
                paramsList[i] = paramsList[i].replace(DELIMITER, "");
        }
        
        // For an invocation of a procedure that yields and one which
        // is not inside par-block, we wrap the procedure in a par-block.
        if (Helper.doesProcedureYield(pd) && currentParBlock == null) {
            return (new ParBlock(
                    new Sequence(new ExprStat(in)), // Statements.
                    new Sequence()))                // Barriers.
                    .visit(this);                   // Return a procedure wrapped in a par-block.
        }
        
        // Does this procedure yield?
        if (Helper.doesProcedureYield(pd)) {
            stInvocation = stGroup.getInstanceOf("InvocationProcType");
            stInvocation.add("parBlock", currentParBlock);
            // Add the barrier this procedure should resign from.
            if (!barrierList.isEmpty())
                stInvocation.add("barrier", barrierList);
        } else
            // Must be an invocation made through a static Java method.
            stInvocation = stGroup.getInstanceOf("Invocation");
        
        stInvocation.add("name", pdName);
        stInvocation.add("vars", paramsList);
        
        return stInvocation.render();
    }

    @Override
    public Object visitImport(Import im) {
        Log.log(im, "Visiting an import statement (" + im + ")");
        Log.log(im, "IMPORTANT: this visitor is not finished yet");
        Log.log(im, "import statement is: " + im.toString());
        
        // Generated template after evaluating this visitor.
        ST stImport = stGroup.getInstanceOf("Import");

        Log.log(im, "import stringtemplate instance is " + stImport.render());
        stImport = stGroup.getInstanceOf("Import");
        // NOTE: is the below commented-out line important still...?
        // i.e. does removing this have repercussions? find out...
        // stImport.add("package", im.toString());

        // replace dots with slashes to build the path to the file we
        // want to include
        String importPath = im.toString().replace('.', '/');

        // debug logging -- delete later
        Log.log(im, "import path is " + importPath);

        // declare our include line -- this is our return value
        String includeLine = "";

        // if the import line has a wild card
        if (importPath.endsWith("*")) {

            // we need to build a string with _all_ of the headers
            // we want to include
            Log.log(im, "wildcard detected, building import list");

            // look through the directory/ies
            // ---
            // these come back as blah.pj, but might need to be
            // modified in findImports() to blah.hpp later.
            // depending on what ben says in the email reply
            // i get back from him, these could be .pj and then
            // i could add a pragma to include native c++,
            // or some other solution. check back later...
            String[] foundImports = findImports(importPath);
            if (foundImports != null) {
                // build the full import
                Log.log(im, "building import statement(s)");
                for (int i = 0; i < foundImports.length; i++) {
                    includeLine += "#include <" + foundImports[i] + ">\n";
                }
            }

            return includeLine;
        }

        // otherwise just build the include for that file
        includeLine = "#include <" + importPath + ".hpp>";

        // debug logging -- delete later and just return the raw string
        // ---
        // maybe add some error checking later if necessary (i.e. the
        // file we want to include may not exist)
        Log.log(im, "includeLine is " + includeLine);
        return includeLine;
        
        // NOTE: leaving the old return here just in case...?
        // return stImport.render();
    }
    
    @Override
    public Object visitProtocolTypeDecl(ProtocolTypeDecl pd) {
        Log.log(pd, "Visiting a ProtocolTypeDecl (" + pd.name().getname() + ")");
        
        // Generated template after evaluating this visitor.
        ST stProtocolClass = stGroup.getInstanceOf("ProtocolClass");
        String name = (String) pd.name().visit(this);
        ArrayList<String> modifiers = new ArrayList<String>();
        ArrayList<String> body = new ArrayList<String>();
        
        for (Modifier m : pd.modifiers())
            modifiers.add((String) m.visit(this));
        
        // Add extended protocols (if any).
        if (pd.extend().size() > 0) {
            for (Name n : pd.extend()) {
                ProtocolTypeDecl ptd = (ProtocolTypeDecl) topLevelDecls.get(n.getname());
                for (ProtocolCase pc : ptd.body())
                    protocolTagsSwitchedOn.put(pd.name().getname() + "." + pc.name().getname(),
                            ptd.name().getname());
            }
        }
        
        // The scope in which all members appear in a protocol.
        if (pd.body() != null) {
            for (ProtocolCase pc : pd.body())
                body.add((String) pc.visit(this));
        }
        
        stProtocolClass.add("name", name);
        stProtocolClass.add("modifiers", modifiers);
        stProtocolClass.add("body", body);
        
        return stProtocolClass.render();
    }
    
    @Override
    public Object visitProtocolCase(ProtocolCase pc) {
        Log.log(pc, "Visiting a ProtocolCase (" + pc.name().getname() + ")");
        
        // Generated template after evaluating this visitor.
        ST stProtocolCase = stGroup.getInstanceOf("ProtocolCase");
        
        // Since we are keeping the name of a tag as is, this (in theory)
        // shouldn't cause any name collision.
        String protocName = (String) pc.name().visit(this);
        // This shouldn't create name collision problems even if we
        // use the same visitor for protocols and records.
        recordFields.clear();
        
        // The scope in which all members of this tag appeared.
        for (RecordMember rm : pc.body())
            rm.visit(this);
        
        // The list of fields that should be passed to the constructor
        // of the static class that the record belongs to.
        if (!recordFields.isEmpty()) {
            stProtocolCase.add("types", recordFields.values());
            stProtocolCase.add("vars", recordFields.keySet());
        }
        
        stProtocolCase.add("name", protocName);
        
        return stProtocolCase.render();
    }
    
    @Override
    public Object visitProtocolLiteral(ProtocolLiteral pl) {
        Log.log(pl, "Visiting a ProtocolLiteral (" + pl.name().getname() + ")");
        
        // Generated template after evaluating this visitor.
        ST stProtocolLiteral = stGroup.getInstanceOf("ProtocolLiteral");
        String type = (String) pl.name().visit(this);
        String tag = (String) pl.tag().visit(this);
        
        // This map is used to determine the order in which values are
        // used with the constructor of the class associated with this
        // kind of protocol.
        HashMap<String, String> members = new LinkedHashMap<String, String>();
        // We need the members of the tag currently being used.
        ProtocolCase target = null;
        ProtocolTypeDecl pt = (ProtocolTypeDecl) topLevelDecls.get(type);
        if (pt != null) { // This should never be 'null'.
            target = pt.getCase(tag);
            // Now that we have the target tag, iterate over all of its members.
            for (RecordMember rm : target.body()) {
                String name = (String) rm.name().visit(this);
                members.put(name, null);
            }
        }
        
        // A visit to a RecordLiteral would return a string, e.g., 'z = 3',
        // where 'z' is the protocol member and '3' is the literal value
        // used to initialized 'z' with. 
        for (RecordMemberLiteral rm : pl.expressions()) {
            String lhs = (String) rm.name().getname();
            String expr = (String) rm.expr().visit(this);
            if (members.put(lhs, expr) == null)
                Log.log(pl, "> Initializing '" + lhs + "' with '" + expr + "'");
            else
                ; // We should never get here.
        }
        
        stProtocolLiteral.add("type", type);
        stProtocolLiteral.add("tag", tag);
        stProtocolLiteral.add("vals", members.values());
        
        return stProtocolLiteral.render();
    }
    
    @Override
    public Object visitRecordTypeDecl(RecordTypeDecl rt) {
        Log.log(rt, "Visiting a RecordTypeDecl (" + rt.name().getname() + ")");
        
        // Generated template after evaluating this visitor.
        ST stRecordClass = stGroup.getInstanceOf("RecordClass");
        String recName = (String) rt.name().visit(this);
        ArrayList<String> modifiers = new ArrayList<String>();
        
        for (Modifier m : rt.modifiers())
            modifiers.add((String) m.visit(this));
        
        // Remove fields from record.
        recordFields.clear();
        
        // The scope in which all members appeared in a record.
        for (RecordMember rm : rt.body())
            rm.visit(this);
        
        // The list of fields that should be passed to the constructor
        // of the static class that the record belongs to.
        if (!recordFields.isEmpty()) {
            stRecordClass.add("types", recordFields.values());
            stRecordClass.add("vars", recordFields.keySet());
        }
        
        stRecordClass.add("name", recName);
        stRecordClass.add("modifiers", modifiers);
        
        return stRecordClass.render();
    }
    
    @Override
    public Object visitRecordMember(RecordMember rm) {
        Log.log(rm, "Visiting a RecordMember (" + rm.type() + " " + rm.name().getname() + ")");
        
        // Grab the type and name of a declared variable.
        String name = (String) rm.name().visit(this);
        String type = (String) rm.type().visit(this);
        
        // Add this field to the collection of record members for reference.
        recordFields.put(name, type);
        
        // Ignored the value returned by this visitor. The types and variables
        // are _always_ resolved elsewhere.
        return null;
    }
    
    @Override
    public Object visitRecordLiteral(RecordLiteral rl) {
        Log.log(rl, "Visiting a RecordLiteral (" + rl.name().getname() + ")");
        
        // Generated template after evaluating this visitor.
        ST stRecordListeral = stGroup.getInstanceOf("RecordLiteral");
        String type = (String) rl.name().visit(this);
        
        // This map is used to determine the order in which values
        // are passed to the constructor of the class associated with
        // this record.
        HashMap<String, String> members = new LinkedHashMap<String, String>();
        RecordTypeDecl rt = (RecordTypeDecl) topLevelDecls.get(type);
        
        if (rt != null) { // This should never be 'null'.
            for (RecordMember rm : rt.body()) {
                String name = (String) rm.name().visit(this);
                members.put(name, null);
            }
        }
        
        // A visit to a RecordMemberLiteral would return a string, e.g., 'z = 3',
        // where 'z' is the record member and '3' is the literal value used to
        // initialized 'z' with. This is something we don't want to do. Instead,
        // we need to return the literal value assigned to 'z'.
        for (RecordMemberLiteral rm : rl.members()) {
            String lhs = (String) rm.name().getname();
            String expr = (String) rm.expr().visit(this);
            if (members.put(lhs, expr) == null)
                Log.log(rl, "> Initializing '" + lhs + "' with '" + expr + "'");
            else
                ; // We should never get here.
        }
        
        stRecordListeral.add("type", type);
        stRecordListeral.add("vals", members.values());
        
        return stRecordListeral.render();
    }
    
    @Override
    public Object visitRecordAccess(RecordAccess ra) {
        Log.log(ra, "Visiting a RecordAccess (" + ra + ")");

        // Generated template after evaluating this visitor.
        ST stRecordAccess = stGroup.getInstanceOf("RecordAccess");
        
        if (ra.record().type.isRecordType()) {
            String name = (String) ra.record().visit(this);
            String field = (String) ra.field().getname();
            
            stRecordAccess.add("name", name);
            stRecordAccess.add("member", field);
        } else if (ra.record().type.isProtocolType()) {
            stRecordAccess = stGroup.getInstanceOf("ProtocolAccess");
            ProtocolTypeDecl pt = (ProtocolTypeDecl) ra.record().type;
            String protocName = (String) pt.name().visit(this); // Wrapper class.
            String name = (String) ra.record().visit(this);     // Reference to inner class type.
            String field = (String) ra.field().getname();       // Field in inner class.
            
            // Cast a protocol to a supertype if needed.
            if (protocolTagsSwitchedOn.containsKey(protocName + "." + currentProtocolTag))
                protocName = protocolTagsSwitchedOn.get(protocName + "." + currentProtocolTag);
            
            stRecordAccess.add("protocName", protocName);
            stRecordAccess.add("tag", currentProtocolTag);
            stRecordAccess.add("var", name);
            stRecordAccess.add("member", field);
        } else { // This is for arrays and strings.
            String name = (String) ra.record().visit(this);
            stRecordAccess.add("name", name);
            // Call the appropriate method to retrieve the number of characters
            // in a string or the number of elements in an N-dimensional array.
            if (ra.isArraySize) // '...'.size for N-dimensional array.
                stRecordAccess.add("member", "length");
            else if (ra.isStringLength) // '...'.length for number of characters in a string.
                stRecordAccess.add("member", "length()");
        }
        
        return stRecordAccess.render();
    }
    
    @Override
    public Object visitParBlock(ParBlock pb) {
        Log.log(pb, "Visiting a ParBlock with " + pb.stats().size() + " statements.");
        
        // Report a warning message for having an empty par-block?
        // TODO: should this be done in 'reachability'?
        if (pb.stats().size() == 0)
            return null;
        // Generated template after evaluating this visitor.
        ST stParBlock = stGroup.getInstanceOf("ParBlock");
        // Save previous par-block.
        String prevParBlock = currentParBlock;
        // Save previous barrier expressions.
        ArrayList<String> prevBarrier = barrierList;
        if (!barrierList.isEmpty())
            barrierList = new ArrayList<String>();
        // Create a name for this new par-block.
        currentParBlock = Helper.makeVariableName(Tag.PAR_BLOCK_NAME.toString(), ++parDecId, Tag.LOCAL_NAME);
        // Since this is a new par-block, we need to create a variable inside
        // the process in which this par-block was declared.
        stParBlock.add("name", currentParBlock);
        stParBlock.add("count", pb.stats().size());
        stParBlock.add("process", "this");
        
        // Increment the jump label.
        stParBlock.add("jump", ++jumpLabel);
        // Add the jump label to the switch list.
        switchLabelList.add(renderSwitchLabel(jumpLabel));
        // Add the barrier this par block enrolls in.
        Sequence<Expression> barriers = pb.barriers();
        if (barriers.size() > 0) {
            for (Expression ex : barriers)
                barrierList.add((String) ex.visit(this));
        }
        
        // Visit the sequence of statements in the par-block.
        Sequence<Statement> statements = pb.stats();
        if (statements.size() > 0) {
            // Rendered value of each statement.
            ArrayList<String> stmts = new ArrayList<String>();
            for (Statement st : statements) {
                if (st == null)
                    continue;
                // An expression is any valid unit of code that resolves to a value,
                // that is, it can be a combination of variables, operations and values
                // that yield a result. An statement is a line of code that performs
                // some action, e.g., print statements, an assignment statement, etc.
                if (st instanceof ExprStat && ((ExprStat) st).expr() instanceof Invocation) {
                    ExprStat es = (ExprStat) st;
                    Invocation in = (Invocation) es.expr();
                    // If this invocation is made on a process, then visit the
                    // invocation and return a string representing the wrapper
                    // class for this procedure; e.g.,
                    //      (new <classType>(...) {
                    //          @Override public synchronized void run() { ... }
                    //          @Override public finalize() { ... }
                    //      }.schedule();
                    if (Helper.doesProcedureYield(in.targetProc))
                        stmts.add((String) in.visit(this));
                    else // Otherwise, the invocation is made through a static Java method.
                        stmts.add((String) createAnonymousProcTypeDecl(st).visit(this));
                } else
                    stmts.add((String) createAnonymousProcTypeDecl(st).visit(this));
            }
            stParBlock.add("body", stmts);
        }
        // Add the barrier to the par-block.
        if (!barrierList.isEmpty() && pb.barriers().size() > 0)
            stParBlock.add("barrier", barrierList);
        
        // Restore the par-block.
        currentParBlock = prevParBlock;
        // Restore barrier expressions.
        barrierList = prevBarrier;
        
        return stParBlock.render();
    }
    
    @Override
    public Object visitTimeoutStat(TimeoutStat ts) {
        Log.log(ts, "Visiting a TimeoutStat");
        
        // Generated template after evaluating this visitor.
        ST stTimeoutStat = stGroup.getInstanceOf("TimeoutStat");
        String timer = (String) ts.timer().visit(this);
        String delay = (String) ts.delay().visit(this);
        
        stTimeoutStat.add("name", timer);
        stTimeoutStat.add("delay", delay);
        
        // Increment the jump label.
        stTimeoutStat.add("resume0", ++jumpLabel);
        // Add the jump label to the switch list.
        switchLabelList.add(renderSwitchLabel(jumpLabel));
        
        return stTimeoutStat.render();
    }
    
    @Override
    public Object visitSyncStat(SyncStat st) {
        Log.log(st, "Visiting a SyncStat");
        
        // Generated template after evaluating this visitor.
        ST stSyncStat = stGroup.getInstanceOf("SyncStat");
        String barrier = (String) st.barrier().visit(this);
        stSyncStat.add("barrier", barrier);
        
        // Increment the jump label.
        stSyncStat.add("resume0", ++jumpLabel);
        // Add the jump label to the switch list.
        switchLabelList.add(renderSwitchLabel(jumpLabel));
        
        return stSyncStat.render();
    }
    
    @Override
    public Object visitUnaryPostExpr(UnaryPostExpr ue) {
        Log.log(ue, "Visiting a UnaryPostExpr (" + ue.opString() + ")");
        
        // Generated template after evaluating this visitor.
        ST stUnaryPostExpr = stGroup.getInstanceOf("UnaryPostExpr");
        String operand = (String) ue.expr().visit(this);
        String op = ue.opString();
        
        stUnaryPostExpr.add("operand", operand);
        stUnaryPostExpr.add("op", op);
        
        return stUnaryPostExpr.render();
    }
    
    @Override
    public Object visitUnaryPreExpr(UnaryPreExpr ue) {
        Log.log(ue, "Visiting a UnaryPreExpr (" + ue.opString() + ")");
        
        // Generated template after evaluating this visitor.
        ST stUnaryPreExpr = stGroup.getInstanceOf("UnaryPreExpr");
        String operand = (String) ue.expr().visit(this);
        String op = ue.opString();
        
        stUnaryPreExpr.add("operand", operand);
        stUnaryPreExpr.add("op", op);
        
        return stUnaryPreExpr.render();
    }
    
    @Override
    public Object visitAltCase(AltCase ac) {
        Log.log(ac, "Visiting an AltCase");
        
        // Generated template after evaluating this visitor.
        ST stAltCase = stGroup.getInstanceOf("AltCase");
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
        
        // Generated template after evaluating this visitor.
        ST stConstantDecl = stGroup.getInstanceOf("ConstantDecl");
        stConstantDecl.add("type", cd.type().visit(this));
        stConstantDecl.add("var", cd.var().visit(this));
        
        return stConstantDecl.render();
    }
    
    @Override
    public Object visitAltStat(AltStat as) {
        Log.log(as, "Visiting an AltStat");
        
        // Generated template after evaluating this visitor.
        ST stAltStat = stGroup.getInstanceOf("AltStat");
        ST stBooleanGuards = stGroup.getInstanceOf("BooleanGuards");
        ST stObjectGuards = stGroup.getInstanceOf("ObjectGuards");
        
        Sequence<AltCase> cases = as.body();
        ArrayList<String> blocals = new ArrayList<String>();
        ArrayList<String> bguards = new ArrayList<String>();
        ArrayList<String> guards = new ArrayList<String>();
        ArrayList<String> altCases = new ArrayList<String>();
        
        // Set boolean guards.
        for (int i = 0; i < cases.size(); ++i) {
            AltCase ac = cases.child(i);
            if (ac.precondition() == null)
                bguards.add(String.valueOf(true));
            else {
                if (ac.precondition() instanceof Literal)
                    bguards.add((String) ac.precondition().visit(this));
                else { // This is an expression.
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
        
        // Set case number for all AltCases.
        for (int i = 0; i < cases.size(); ++i)
            cases.child(i).setCaseNumber(i);
        // Visit all guards.
        for (int i = 0; i < cases.size(); ++i) {
            AltCase ac = cases.child(i);
            Statement stat = ac.guard().guard();
            // Channel read expression?
            if (stat instanceof ExprStat) {
                Expression e = ((ExprStat) stat).expr();
                ChannelReadExpr cr = null;
                if (e instanceof Assignment)
                    cr = (ChannelReadExpr) ((Assignment) e).right();
                guards.add((String) cr.channel().visit(this));
            } else if (stat instanceof SkipStat)
                guards.add(PJAlt.class.getSimpleName() + ".SKIP");
            
            altCases.add((String) ac.visit(this));
        }
        
        stObjectGuards.add("guards", guards);
        
        // <--
        // This is needed because of the 'StackMapTable' for the generated Java bytecode.
        Name n = new Name("index");
        new LocalDecl(
                new PrimitiveType(PrimitiveType.IntKind),
                new Var(n, null),
                false /* not constant */).visit(this);
        
        // Create a tag for this local alt declaration.
        String newName = Helper.makeVariableName("alt", ++localDecId, Tag.LOCAL_NAME);
        localParams.put(newName, "PJAlt");
        paramDeclNames.put(newName, newName);
        // -->
        
        stAltStat.add("alt", newName);
        stAltStat.add("count", cases.size());
        stAltStat.add("initBooleanGuards", stBooleanGuards.render());
        stAltStat.add("initGuards", stObjectGuards.render());
        stAltStat.add("bguards", "booleanGuards");
        stAltStat.add("guards", "objectGuards");
        stAltStat.add("jump", ++jumpLabel);
        stAltStat.add("cases", altCases);
        stAltStat.add("index", n.visit(this));
        
        // Add the jump label to the switch list.
        switchLabelList.add(renderSwitchLabel(jumpLabel));
        
        return stAltStat.render();
    }
    
    @Override
    public Object visitReturnStat(ReturnStat rs) {
        Log.log(rs, "Visiting a ReturnStat");
        
        ST stReturnStat = stGroup.getInstanceOf("ReturnStat");
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
        } else if (t.isPrimitiveType()) // This is needed because we can only have wrapper class.
            baseType = Helper.getWrapperType(t);
        
        return baseType;
    }
    
    /**
     * This is used for newly-created processes.
     */
    private void resetGlobals() {
        parDecId = 0;
        varDecId = 0;
        localDecId = 0;
        jumpLabel = 0;

        localParams.clear();
        switchLabelList.clear();
        barrierList.clear();
        
        formalParams.clear();
        paramDeclNames.clear();
    }
    
    /**
     * Returns a string representation of a jump label.
     */
    private String renderSwitchLabel(int jump) {
        ST stSwitchCase = stGroup.getInstanceOf("SwitchCase");
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
                new Sequence(),               // Modifiers.
                null,                         // Return type.
                new Name("Anonymous"),        // Procedure name.
                new Sequence(),               // Formal parameters.
                new Sequence(),               // Implement.
                null,                         // Annotations.
                new Block(new Sequence(st))); // Body.
    }
    
    @SuppressWarnings("unchecked")
    private Object createNewArray(String lhs, NewArray na) {
        Log.log(na.line + ": Creating a New Array");
        
        // Generated template after evaluating this visitor.
        ST stNewArray = stGroup.getInstanceOf("NewArray");
        String[] dims = (String[]) na.dimsExpr().visit(this);
        String type = (String) na.baseType().visit(this);

        ST stNewArrayLiteral = stGroup.getInstanceOf("NewArrayLiteral");
        if (na.init() != null) {
            ArrayList<String> inits = new ArrayList<String>();
            Sequence<Expression> seq = na.init().elements();
            for (Expression e : seq) {
                isArrayLiteral = e instanceof ArrayLiteral ? true : false;
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
        
        // Reset value for array literal expressions.
        isArrayLiteral = false;
        
        return stNewArray.render();
    }
    
    private Object createChannelReadExpr(String lhs, String op, ChannelReadExpr cr) {
        Log.log(cr, "Creating Channel Read Expression");
        
        // Generated template after evaluating this visitor.
        ST stChannelReadExpr = stGroup.getInstanceOf("ChannelReadExpr");
        // 'c.read()' is a channel-end expression, where 'c'
        // is the reading end of a channel.
        Expression chanExpr = cr.channel();
        // 'c' is the name of the channel.
        String chanEndName = (String) chanExpr.visit(this);
        
        // Is it a timer read expression?
        if (chanExpr.type.isTimerType()) {
            ST stTimerRedExpr = stGroup.getInstanceOf("TimerRedExpr");
            stTimerRedExpr.add("name", lhs);
            return stTimerRedExpr.render();
        }
        
        int countLabel = 2;  // One for the 'runLabel' and one for the 'read' operation.
        // Is the reading end of this channel shared?
        if (chanExpr.type.isChannelEndType() && ((ChannelEndType) chanExpr.type).isShared()) {
            stChannelReadExpr = stGroup.getInstanceOf("ChannelOne2Many");
            ++countLabel;
        }
        
        // Do we have an extended rendezvous?
        if (cr.extRV() != null) {
            Object o = cr.extRV().visit(this);
            stChannelReadExpr.add("extendRv", o);
        }
        
        stChannelReadExpr.add("chanName", chanEndName);        
        // Add the switch block for resumption.
        for (int label = 0; label < countLabel; ++label) {
            // Increment jump label.
            stChannelReadExpr.add("resume" + label, ++jumpLabel);
            // Add jump label to the switch list.
            switchLabelList.add(renderSwitchLabel(jumpLabel));
        }
        
        stChannelReadExpr.add("lhs", lhs);
        stChannelReadExpr.add("op", op);
        
        return stChannelReadExpr.render();
    }
    
    /**
     * Returns a string representing the signature of the wrapper
     * class or Java method that encapsulates a PJProcess.
     */
    private String signature(ProcTypeDecl pd) {
        String s = "";
        for (ParamDecl param : pd.formalParams()) {
            s = s + "$" + param.type().signature();
            // Array [t; where 't' is the baste type.
            if (param.type().isArrayType())
                s = s.replace("[", "ar").replace(DELIMITER, "");
            // <Rn; 'n' is the name.
            else if (param.type().isRecordType())
                s = s.replace("<", "rc").replace(DELIMITER, "");
            // <Pn; 'n' is the name.
            else if (param.type().isProtocolType())
                s = s.replace("<", "pt").replace(DELIMITER, "");
            // {t;
            else if (param.type().isChannelType())
                s = s.replace("{", "ct").replace(DELIMITER, "");
            // channel end type.
            else if (param.type().isChannelEndType()) {
                if (((ChannelEndType) param.type()).isRead()) // {t;? channel read.
                    s = s.replace("{", "cr").replace(DELIMITER, "").replace("?", "");
                else // {t;! channel write.
                    s = s.replace("{", "cw").replace(DELIMITER, "").replace("!", "");
            } else
                s = s.replace(";", "");
        }
        return String.valueOf(s.hashCode()).replace("-", "$");
    }

    /**
     * Returns an array of filenames to import in the
     * event of a wildcard import
     */
    private String[] findImports(String importPath) {
        Log.log("in findImports");
        Log.log("NOTE: current directory is " + System.getProperty("user.dir") + ".");
        // build fully qualified path to our include directory
        String dir = "include/C++/" + importPath;
        Log.log("dir length is " + dir.length() + ".");
        // chop off the last /*
        dir = dir.substring(0, dir.length() - 2);
        Log.log("Finding files in directory " + dir + "...");

        File f = new File(dir);


        String[] imports = f.list();

        if (imports == null) {
            Log.log("Found no imports.");
        } else {
            for(String str : imports) {
                Log.log("Found file " + str + ".");
            }
        }

        return imports;
    }
}
