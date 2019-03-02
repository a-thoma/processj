package codegeneratorjava;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import ast.*;
import processj.runtime.*;
import utilities.Log;
import utilities.SymbolTable;
import utilities.Visitor;

/**
 * A tree walker that collects data from an {@link AST} object and then
 * pushes this data into a {@code grammarTemplatesJava} to translate a
 * ProcessJ source code to Java.
 *
 * @param <T>
 *          A visitor interface used to perform operations across a
 *          collection of different objects.
 *
 * @author Ben
 * @version 06/10/2018
 * @since 1.2
 */
@SuppressWarnings("unchecked")
public class CodeGeneratorJava<T extends Object> extends Visitor<T> {

    /**
     * String template file locator.
     */
    private final String _stGammarFile = "resources/stringtemplates/java/grammarTemplatesJava.stg";
    
    /**
     * Current java version.
     */
    private final String _currentJVM = System.getProperty("java.version");

    /**
     * Collection of templates, imported templates, and/or groups that
     * contain formal template definitions.
     */
    private STGroup _stGroup;
    
    /**
     * Current compilation unit.
     */
    private Compilation _currCompilation = null;

    /**
     * The user working directory.
     */
    private String _workingDir = null;

    /**
     * Current procedure call.
     */
    private String _currProcName = null;
    
    /**
     * Current 'par' block.
     */
    private String _currParBlock = null;
    
    /**
     * List of imports.
     */
    private Set<String> _importList = new LinkedHashSet<>();
    
    /**
     * Top level declarations.
     */
    private SymbolTable _topLevelDecls = null;

    /**
     * Map of formal parameters transformed to fields.
     */
    private HashMap<String, String> _formalParamFieldMap = new LinkedHashMap<>();
    
    /**
     * Map of formal parameters names to name tags.
     */
    private HashMap<String, String> _paramDeclNameMap = new LinkedHashMap<>();
    
    /**
     * Map of local parameters transformed to fields.
     */
    private HashMap<String, String> _localParamFieldMap = new LinkedHashMap<>();
    
    /**
     * Map of 'par' blocks declared in a process. This map associates the
     * name of a 'par' block with the number of processes invoked within
     * its block.
     */
    private HashMap<String, Integer> _parMap = new LinkedHashMap<>();
    
    /**
     * Map of record names to name tags.
     */
    private HashMap<String, String> _recordMap = new LinkedHashMap<>();
    
    /**
     * Map of records member transformed to fields.
     */
    private HashMap<String, String> _recordFieldMap = new LinkedHashMap<>();
    
    /**
     * Map of records members transformed to fields for records that
     * inherit members from other records.
     */
    private HashMap<String, String> _recordMemberMap = new LinkedHashMap<>();
    
    /**
     * Map of protocol names to name tags.
     */
    private HashMap<String, String> _protocMap = new LinkedHashMap<>();
    
    /**
     * List of switch labels.
     */
    private List<String> _switchLabelList = new ArrayList<>();
    
    /**
     * List of barrier expressions.
     */
    private List<String> _barrierList = new ArrayList<>();

    /**
     * Identifier for a parameter declaration.
     */
    private int _varDecId = 0;
    
    /**
     * Identifier for a 'par' block declaration.
     */
    private int _parDecId = 0;

    /**
     * Identifier for a local variable declaration.
     */
    private int _localDecId = 0;
    
    /**
     * Jump label.
     */
    private int _jumLabel = 0;
    
    /**
     * Channel-end type (e.g. 'read' or 'write')
     */
    private boolean _isChanRead = false;
    
    /**
     * Access to protocol case.
     */
    private boolean _isProtocolCase = false;
    
    /**
     * 
     */
    private String _currProtocolTag = null;
    
    /**
     * This is used to avoid name collisions.
     */
    private boolean _fieldName = true;
    
    /**
     * This is used for uninitialized variables.
     */
    private static final String EMPTY_STRING = "";

    /**
     * Internal constructor that loads a group file containing a collection of
     * templates, imported templates, and/or groups containing formal template
     * definitions. Additionally, the constructor initializes a symbol table
     * with top level types declarations.
     * 
     * @param topLevelDecls
     * 			The top level types which can be procedures, records, protocols,
     * 			constants, and/or external types.
     */
    public CodeGeneratorJava(SymbolTable topLevelDecls) {
        Log.logHeader("******************************************");
        Log.logHeader("*      C O D E   G E N E R A T O R       *");
        Log.logHeader("*                J A V A                 *");
        Log.logHeader("******************************************");
        
        _topLevelDecls = topLevelDecls;
        _stGroup = new STGroupFile(_stGammarFile);
    }
    
    /**
     * Sets the system properties to a current working directory.
     *
     * @param workingDir
     * 			A working directory.
     */
    public void setWorkingDirectory(String workingDir) {
        this._workingDir = workingDir;
    }
    
    /**
     * @return
     */
    public String getWorkingDirectory() {
        return _workingDir;
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT COMPILATION
     * 
     * Visits a single compilation unit that starts with an optional package
     * declaration, followed by zero or more import declarations, followed
     * by zero or more type declarations.
     *
     * @param compilation
     * 			An {@code AST} that represents the entire compilation unit.
     * @return A text generated after evaluating this compilation unit.
     */
    public T visitCompilation(Compilation compilation) {
        Log.log(compilation.line + ": Visiting a Compilation");
        
        _currCompilation = compilation;
        // Code generated by the template
        String templateResult = null;
        // Instance of Compilation template to fill in
        ST stCompilation = _stGroup.getInstanceOf("Compilation");

        // Reference to all remaining types
        List<String> body = new ArrayList<>();
        // Holds all top level types declarations
        Sequence<Type> typeDecls = compilation.typeDecls();
        // TODO: Collect procedures, records, protocols, constants, and
        // external types (if any) before iterating over remaining items
        // Here..

        // Iterate over remaining declarations which is anything that
        // comes after
        for (Type decls : typeDecls) {
            String decl = (String) decls.visit(this);
            if (decl != null) {
                body.add(decl);
            }
        }

        stCompilation.add("packageName", compilation.packageNoName());
        stCompilation.add("fileName", compilation.sourceFile);
        stCompilation.add("name", compilation.fileNoExtension());
        stCompilation.add("body", body);
        stCompilation.add("version", _currentJVM);
        
        if (_importList.size() > 0)
            stCompilation.add("imports", _importList);

        // Rendered code for log
        templateResult = stCompilation.render();
        
        // Debug code generated by the ProcessJ JVM compiler
        Log.log("****************************************");
        Log.log("*         O U T P U T   C O D E        *");
        Log.log("****************************************");
        Log.log("\n" + templateResult);
        
        return (T) templateResult;
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT PROC_TYPE_DECL
     */
    public T visitProcTypeDecl(ProcTypeDecl pd) {
        Log.log(pd.line + ": Visiting a ProcTypeDecl (" + pd.name().getname() + ")");
        
        // Generated template after evaluating this visitor
        ST stProcTypeDecl = null;
        // Save previous procedure
        String prevProcName = _currProcName;
        // Save previous jump labels
        List<String> prevLabels = _switchLabelList;
        if (!_switchLabelList.isEmpty())
            _switchLabelList = new ArrayList<>();
        // Name of invoked procedure
        _currProcName = (String) pd.name().visit(this);
        // Procedures are static classes which belong to the same package. To avoid
        // having classes with the same name, we generate a new name for this procedure
        String procName = null;
        // For non-invocations, that is, for anything other than a procedure that
        // 'yields', we need to extends the PJProcess class 'anonymously'
        if (_currProcName.equals("Anonymous")) {
            // Preserve current 'jump' label
            int prevJumLabel = _jumLabel;
            _jumLabel = 0;
            // Grab the instance for an anonymous procedure
            stProcTypeDecl = _stGroup.getInstanceOf("AnonymousProcess");
            // Statements that appear in the procedure
            String[] body = (String[]) pd.body().visit(this);
            
            stProcTypeDecl.add("parBlock", _currParBlock);
            stProcTypeDecl.add("syncBody", body);
            // Add the 'barrier' expression
            if (!_barrierList.isEmpty())
                stProcTypeDecl.add("barrier", _barrierList);
            // Add the 'switch' block
            if (!_switchLabelList.isEmpty()) {
                ST stSwitchBlock = _stGroup.getInstanceOf("SwitchBlock");
                stSwitchBlock.add("jumps", _switchLabelList);
                stProcTypeDecl.add("switchBlock", stSwitchBlock.render());
            }
            // Restore 'jump' label
            _jumLabel = prevJumLabel;
        } else {
            // Restore global variables for a new PJProcess class
            resetGlobals();
            // Formal parameters that must be passed to the procedure
            Sequence<ParamDecl> formals = pd.formalParams();

            if (formals != null && formals.size() > 0) {
                // Iterate through and visit every parameter declaration
                for (int i = 0; i < formals.size(); ++i) {
                    ParamDecl actualParam = formals.child(i);
                    // Retrieve the name and type of a parameter in the parameter list;
                    // note that we ignored the value returned by this visitor.
                    actualParam.visit(this);
                }
            } else
                ; // Procedure does not take any parameters
            
            // The scope in which all declarations appear in a procedure
            String[] body = (String[]) pd.body().visit(this);

            // Retrieve modifier(s) attached to invoked procedure such as private,
            // public, protected, etc.
            String[] modifiers = (String[]) pd.modifiers().visit(this);
            // Grab the return type of the invoked procedure
            String procType = (String) pd.returnType().visit(this);
            // The procedure's annotation determines if we have a yielding procedure
            // or a Java method (a non-yielding procedure)
            boolean doYield = Helper.doesProcedureYield(pd);
            // Set the template to the correct instance value and then initialize
            // its attributes
            if (doYield) {
                // This procedure yields! Grab the instance of a yielding procedure
                // from the string template in order to define a new class
                procName = Helper.makeVariableName(_currProcName + signature(pd), 0, Tag.PROCEDURE_NAME);
                stProcTypeDecl = _stGroup.getInstanceOf("ProcClass");
                stProcTypeDecl.add("name", procName);
                // The statements that appear in the body of the procedure
                stProcTypeDecl.add("syncBody", body);
            } else {
                // Otherwise, grab the instance of a non-yielding procedure instead
                // to define a new static Java method
                procName = Helper.makeVariableName(_currProcName + signature(pd), 0, Tag.METHOD_NAME);
                stProcTypeDecl = _stGroup.getInstanceOf("Method");
                stProcTypeDecl.add("name", procName);
                stProcTypeDecl.add("type", procType);
                // Do we have any modifier?
                if (modifiers != null && modifiers.length > 0)
                    stProcTypeDecl.add("modifier", modifiers);
                stProcTypeDecl.add("body", body);
            }
            
            // Create an entry point for the ProcessJ program which is just a Java main
            // method that is called by the JVM
            if ("main".equals(_currProcName) && pd.signature().equals(Tag.MAIN_NAME.getTag())) {
                // Create an instance of a Java main method template
                ST stMain = _stGroup.getInstanceOf("Main");
                stMain.add("class", _currCompilation.fileNoExtension());
                stMain.add("name", procName);
                // Pass the list of command line arguments to this main method
                if (!_formalParamFieldMap.isEmpty()) {
                    stMain.add("types", _formalParamFieldMap.values());
                    stMain.add("vars", _formalParamFieldMap.keySet());
                }
                // Add entry point of the program
                stProcTypeDecl.add("main", stMain.render());
            }
            
            // The list of command line arguments should be passed to the constructor
            // of the static class that the main method belongs to (some procedure class)
            // or should be passed to the Java method (some static method)
            if (!_formalParamFieldMap.isEmpty()) {
                stProcTypeDecl.add("types", _formalParamFieldMap.values());
                stProcTypeDecl.add("vars", _formalParamFieldMap.keySet());
            }
            // The list of local variables defined in the body of a procedure become
            // the member variables of the procedure class
            if (!_localParamFieldMap.isEmpty()) {
                stProcTypeDecl.add("ltypes", _localParamFieldMap.values());
                stProcTypeDecl.add("lvars", _localParamFieldMap.keySet());
            }
            // Add the 'switch' block for resumption
            if (!_switchLabelList.isEmpty()) {
                ST stSwitchBlock = _stGroup.getInstanceOf("SwitchBlock");
                stSwitchBlock.add("jumps", _switchLabelList);
                stProcTypeDecl.add("switchBlock", stSwitchBlock.render());
            }
        }
        
        // Restore and reset previous values
        _currProcName = prevProcName;
        // Restore previous jump labels
        _switchLabelList = prevLabels;

        return (T) stProcTypeDecl.render();
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT BINARY_EXPR
     */
    public T visitBinaryExpr(BinaryExpr be) {
        Log.log(be.line + ": Visiting a BinaryExpr");

        // Generated template after evaluating this visitor
        ST stBinaryExpr = _stGroup.getInstanceOf("BinaryExpr");
        String op = be.opString();
        String lhs = (String) be.left().visit(this);
        lhs = be.left().hasParens ? "(" + lhs + ")" : lhs;
        String rhs = (String) be.right().visit(this);
        rhs = be.right().hasParens ? "(" + rhs + ")" : rhs;
        stBinaryExpr.add("lhs", lhs);
        stBinaryExpr.add("rhs", rhs);
        stBinaryExpr.add("op", op);

        return (T) stBinaryExpr.render();
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT_WHILE_STAT
     */
    public T visitWhileStat(WhileStat ws) {
        Log.log(ws.line + ": Visiting a WhileStat");
        
        // Generated template after evaluating this visitor
        ST stWhileStat = _stGroup.getInstanceOf("WhileStat");
        // Sequence of statements enclosed in a 'block' statement
        String[] stats = null;
        
        String condExpr = null;
        
        if (ws.expr() != null)
            condExpr = (String) ws.expr().visit(this);
        
        if (ws.stat() != null)
            stats = (String[]) ws.stat().visit(this);
        else // The body of a 'while' could be empty
            stats = new String[] { ";" };
        
        stWhileStat.add("expr", condExpr);
        stWhileStat.add("body", stats);
        
        return (T) stWhileStat.render();
    }
    
    public T visitForStat(ForStat fs) {
        Log.log(fs.line + ": Visiting a ForStat");
        
        return null;
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT_IF_STAT
     */
    public T visitIfStat(IfStat is) {
        Log.log(is.line + ": Visiting a IfStat");

        // Generated template after evaluating this visitor
        ST stIfStat = _stGroup.getInstanceOf("IfStat");
        // Sequence of statements enclosed in a 'block' statement
        String[] thenStats = null;
        
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
        
        stIfStat.add("expr", condExpr);
        stIfStat.add("thenPart", thenStats);
        
        return (T) stIfStat.render();
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT ASSIGNMENT
     */
    public T visitAssignment(Assignment as) {
        Log.log(as.line + ": Visiting an Assignment");
        
        // Generated template after evaluating this visitor
        ST stVar = _stGroup.getInstanceOf("Var");
        
        String op = (String) as.opString();
        String lhs = (String) as.left().visit(this);
        String rhs = null;
        
        if (as.right() instanceof ChannelReadExpr)
            return (T) createChannelReadExpr(lhs, op, ((ChannelReadExpr) as.right()));
        else
            rhs = (String) as.right().visit(this);
        
        stVar.add("name", lhs);
        stVar.add("val", rhs);
        stVar.add("op", op);
        
        return (T) stVar.render();
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT PARAM_DECL
     */
    public T visitParamDecl(ParamDecl pd) {
        Log.log(pd.line + ": Visiting a ParamDecl (" + pd.type().typeName() + " " + pd.paramName().getname() + ")");
        
        // Grab the type and name of a declared variable
        String name = (String) pd.paramName().visit(this);
        String type = (String) pd.type().visit(this);
        
        // Create a tag for this parameter and then add it to the collection
        // of parameters for reference
        String newName = Helper.makeVariableName(name, ++_varDecId, Tag.PARAM_NAME);
        _formalParamFieldMap.put(newName, type);
        _paramDeclNameMap.put(name, newName);
        
        // Ignored the value returned by this visitor. The reason for this
        // is that templates for methods and classes take a list of types
        // and variable names.
        return null;
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT LOCAL_DECL
     */
    public T visitLocalDecl(LocalDecl ld) {
        Log.log(ld.line + ": Visting a LocalDecl (" + ld.type().typeName() + " " + ld.var().name().getname() + ")");

        // We could have the following targets:
        //      x = in.read();                  , a single channel read
        //      x = b.read() + c.read() + ...;  , multiple channel reads
        //      x = read();                     , a Java method that returns a value
        //      x = a + b;                      , a binary expression
        //      x = a = b ...;                  , a complex assignment

        // Returning values for a local declaration
        String name = (String) ld.var().name().getname();
        String type = (String) ld.type().visit(this);
        String val = null;

        // Create a tag for this local channel expr parameter
        String newName = Helper.makeVariableName(name, ++_localDecId, Tag.LOCAL_NAME);
        _localParamFieldMap.put(newName, type);
        _paramDeclNameMap.put(name, newName);
        
        // This variable could be initialized, e.g., through an
        // assignment operator
        Expression expr = ld.var().init();
        // Visit the expressions associated with this variable
        if (expr != null) {
            if (ld.type() instanceof PrimitiveType)
                val = (String) expr.visit(this);
            else if (ld.type() instanceof NamedType) // Must be a record or protocol
                val = (String) expr.visit(this);
        }
        
        // Is it a barrier declaration? If so, we must generate
        // code that creates a barrier object.
        if (ld.type().isBarrierType() && expr == null) {
            ST stBarrierDecl = _stGroup.getInstanceOf("BarrierDecl");
            val = stBarrierDecl.render();
        }
        // Is it a simple declaration for a channel type? If so, and since
        // channels cannot be created using the operator 'new', we generate
        // code to create a channel object.
        if (ld.type().isChannelType() && expr == null) {
            ST stChannelDecl = _stGroup.getInstanceOf("ChannelDecl");
            stChannelDecl.add("type", type);
            val = stChannelDecl.render();
        }
        
        // After making this local declaration a field of the procedure in
        // which it was declared, we return the 'empty' string if and only
        // if this local variable is not initialized
        if (expr == null) {
            if (!ld.type().isBarrierType() && (ld.type().isPrimitiveType() ||
                ld.type().isNamedType()))    // Could be records or protocols
                return (T) EMPTY_STRING;
        }
        
        // If we reach this section, then we have a variable declaration with
        // some initial value
        ST stVar = _stGroup.getInstanceOf("Var");
        stVar.add("name", newName);
        stVar.add("val", val);
        
        return (T) stVar.render();
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT EXPR_STAT
     */
    public T visitExprStat(ExprStat es) {
        Log.log(es.line + ": Visiting an ExprStat");

        return (T) es.expr().visit(this);
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT NAME
     */
    public T visitName(Name na) {
        Log.log(na.line + ": Visiting a Name (" + na.getname() + ")");
        
        String name = null;
        if (_fieldName && !_paramDeclNameMap.isEmpty()) {
            if (_paramDeclNameMap.containsKey(na.getname()))
                name = _paramDeclNameMap.get(na.getname());
        }
        
        if (name == null && _recordMemberMap.containsKey(name))
            name = _recordFieldMap.get(name);
        
        if (name == null && _recordMap.containsKey(name))
            name = _recordMap.get(name);
        
        if (name == null && _protocMap.containsKey(name))
            name = _protocMap.get(name);
        
        if (name == null)
            name = na.getname();
        
        return (T) name;
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT NAME_EXPR
     */
    public T visitNameExpr(NameExpr ne) {
        Log.log(ne.line + ": Visiting NameExpr (" + ne.name().getname() + ")");
        
        // NameExpr always points to 'myDecl'
        return (T) ne.name().visit(this);
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT NAMED_TYPE
     */
    public T visitNamedType(NamedType nt) {
        Log.log(nt.line + ": Visiting a NamedType (" + nt.name().getname() + ")");
        
        String type = (String) nt.name().getname();
        
        if (nt.type().isProtocolType()) // This is for protocol 'inheritance'
            type = PJProtocolCase.class.getSimpleName();

        return (T) type;
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT PRIMITIVE_TYPE
     */
    public T visitPrimitiveType(PrimitiveType py) {
        Log.log(py.line + ": Visiting a Primitive Type (" + py.typeName() + ")");
        
        // ProcessJ primitive types that do not translate
        // directly to Java primitive types
        String typeStr = py.typeName();
        if (py.isStringType()) {
            typeStr = "String";
        } else if (py.isTimerType()) {
            typeStr = PJTimer.class.getSimpleName();
        } else if (py.isBarrierType()) {
            typeStr = PJBarrier.class.getSimpleName();
        }

        return (T) typeStr;
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT PRIMITIVE_LITERAL
     */
    public T visitPrimitiveLiteral(PrimitiveLiteral li) {
        Log.log(li.line + ": Visiting a Primitive Literal (" + li.getText() + ")");
        
        ST stPrimitiveLiteral = _stGroup.getInstanceOf("PrimitiveLiteral");
        if (li.isSuffixed())
            stPrimitiveLiteral.add("type", li.suffix());
        stPrimitiveLiteral.add("value", li.getText());
        
        return (T) stPrimitiveLiteral.render();
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT CHANNEL_TYPE
     */
    public T visitChannelType(ChannelType ct) {
        Log.log(ct.line + ": Visiting a ChannelType (" + ct + ")");
        
        // Channel class type
        String chanType = "";
        switch (ct.shared()) {
        case ChannelType.NOT_SHARED:
            chanType = PJOne2OneChannel.class.getSimpleName();
            break;
        case ChannelType.SHARED_READ:
//            chanType = PJOne2ManyChannel.class.getSimpleName(); break;
        case ChannelType.SHARED_WRITE:
//            chanType = PJMany2OneChannel.class.getSimpleName(); break;
        case ChannelType.SHARED_READ_WRITE:
//            chanType = PJMany2ManyChannel.class.getSimpleName(); break;
        }
        // Resolve parameterized type for channel, e.g., chan<T>
        // where 'T' is the type to be resolved
        String type = getChannelType(ct.baseType());
        
        return (T) (chanType + "<" + type + ">");
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT CHANNEL_END_EXPR
     */
    public T visitChannelEndExpr(ChannelEndExpr ce) {
        Log.log(ce.line + ": Visiting a ChannelEndExpr (" + (ce.isRead() ? "read" : "write") + ")");
        
        String channel = (String) ce.channel().visit(this);
        _isChanRead = ce.isRead() ? true : false;
        
        return (T) channel;
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT CHANNEL_END_TYPE
     */
    public T visitChannelEndType(ChannelEndType ct) {
        Log.log(ct.line + ": Visiting a ChannelEndType (" + ct.typeName() + ")");
        
        // Channel class type
        String chanType = PJOne2OneChannel.class.getSimpleName();
        // Resolve parameterized type for channel, e.g., chan<T>
        // where 'T' is the type to be resolved
        String type = getChannelType(ct.baseType());
        
        return (T) (chanType + "<" + type + ">");
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT CHANNEL_WRITE_STAT
     */
    public T visitChannelWriteStat(ChannelWriteStat cw) {
        Log.log(cw.line + ": Visiting a ChannelWriteStat");
        
        // Generated template after evaluating this visitor
        ST stChanWriteStat = _stGroup.getInstanceOf("ChanWriteStat");
        // 'c.write(x)' is a channel-end expression, where 'c'
        // is the writing end of a channel.
        Expression chanExpr = cw.channel();
        // 'c' is the name of the channel
        String chanWriteName = (String) chanExpr.visit(this);
        stChanWriteStat.add("chanName", chanWriteName);
        // Expression sent through channel
        String expr = (String) cw.expr().visit(this);
        stChanWriteStat.add("writeExpr", expr);
        
        // Increment jump label
        stChanWriteStat.add("resume0", ++_jumLabel);
        // Add jump label to the 'switch' list
        _switchLabelList.add(renderSwitchLabel(_jumLabel));
        
        return (T) stChanWriteStat.render();
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT CHANNEL_READ_EXPR
     */
    public T visitChannelReadExpr(ChannelReadExpr cr) {
        Log.log(cr.line + ": Visiting a ChannelReadExpr");
        
        // Generated template after evaluating this visitor
        ST stChannelReadExpr = _stGroup.getInstanceOf("ChannelReadExpr");
        // 'c.read()' is a channel-end expression, where 'c'
        // is the reading end of a channel.
        Expression chanExpr = cr.channel();
        // 'c' is the name of the channel
        String chanEndName = (String) chanExpr.visit(this);
        stChannelReadExpr.add("chanName", chanEndName);
        // Add the 'switch' block for resumption
        for (int label = 0; label < 2; ++label) {
            // Increment jump label
            stChannelReadExpr.add("resume" + label, ++_jumLabel);
            // Add jump label to the 'switch' list
            _switchLabelList.add(renderSwitchLabel(_jumLabel));
        }
        
        return (T) stChannelReadExpr.render();
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT VAR
     */
    public T visitVar(Var va) {
        Log.log(va.line + ": Visiting a Var (" + va.name().getname() + ")");

        // Generated template after evaluating this visitor
        ST stVar = _stGroup.getInstanceOf("Var");
        // Returned values for name and expression (if any)
        String name = (String) va.name().visit(this);
        String exprStr = null;
        // This variable could be initialized, e.g., through an assignment operator
        Expression expr = va.init();
        // Visit the expressions associated with this variable
        if (expr != null) {
            // This is safe for when our target is not too complicated, e.g.,
            // initializing variables with primitives or string literals
            exprStr = (String) expr.visit(this);
            stVar.add("val", exprStr);
        }

        stVar.add("name", name);

        return (T) stVar.render();
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT ARRAY_TYPE
     */
    public T visitArrayType(ArrayType at) {
        Log.log(at.line + ": Visiting an ArrayType (" + at.typeName() + ")");

        String stArrayType = (String) at.baseType().visit(this) + "[]";

        return (T) stArrayType;
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT MODIFIER
     */
    public T visitModifier(Modifier mo) {
        Log.log(mo.line + ": Visiting a Modifier (" + mo + ")");
        
        // Type of modifiers: public, protected, private, etc.
        return (T) mo.toString();
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT BLOCK
     */
    public T visitBlock(Block bl) {
        Log.log(bl.line + ": Visiting a Block");
        
        // The scope in which declarations appear, starting with their
        // own initializers and including any further declarations such
        // invocations or sequence of statements
        String[] stats = (String[]) bl.stats().visit(this);

        return (T) stats;
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT SEQUENCE
     */
    @SuppressWarnings("rawtypes")
    public T visitSequence(Sequence se) {
        Log.log(se.line + ": Visiting a Sequence");
        
        // Sequence of statements enclosed in a 'block' statement
        List<String> seqstr = new ArrayList<>();
        // Iterate through every statement
        for (int i = 0; i < se.size(); ++i) {
            if (se.child(i) != null) {
                T stats = se.child(i).visit(this);
                if (stats == null)
                    continue;
                // These are either
                //      1) a sequence of statements, or
                //      2) a single statement
                // found in a 'block' statement, e.g. local declarations,
                // variable declarations, invocations, etc.
                if (stats instanceof String[]) {
                    String[] statsStr = (String[]) stats;
                    seqstr.addAll(Arrays.asList(statsStr));
                } else {
                    seqstr.add((String) stats);
                }
            }
        }
        
        return (T) seqstr.toArray(new String[0]);
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT BREAK_STAT
     */
    public T visitBreakStat(BreakStat bs) {
        Log.log(bs.line + ": Visiting a BreakStat");
        
        // Generated template after evaluating this visitor
        ST stBreakStat = _stGroup.getInstanceOf("BreakStat");
        
        if (bs.target() != null) // No parse tree for 'break'
            stBreakStat.add("name", bs.target().visit(this));
        
        return (T) stBreakStat.render();
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT SWITCH_LABEL
     */
    public T visitSwitchLabel(SwitchLabel sl) {
        Log.log(sl.line + ": Visiting a SwitchLabel");
        
        // Generated template after evaluating this visitor
        ST stSwitchLabel = _stGroup.getInstanceOf("SwitchLabel");
        
        // This could be a 'default' label, in this case, expr()
        // is 'null'
        String label = null;
        if (!sl.isDefault())
            label = (String) sl.expr().visit(this);
        if (_isProtocolCase) {
            // Silly way to keep track of a protocol 'tag', however, this
            // should (in theory) _always_ work. The type checker should
            // catch any invalid 'tag' in a switch label for a protocol
            _currProtocolTag = label;
            label = "\"" + label + "\"";
        }
        
        stSwitchLabel.add("label", label);
        
        return (T) stSwitchLabel.render();
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT SWITCH_GROUP
     */
    public T visitSwitchGroup(SwitchGroup sg) {
        Log.log(sg.line + ": Visit a SwitchGroup");
        
        // Generated template after evaluating this visitor
        ST stSwitchGroup = _stGroup.getInstanceOf("SwitchGroup");
        
        List<String> labels = new ArrayList<>();
        for (SwitchLabel sl : sg.labels())
            labels.add((String) sl.visit(this));
        
        List<String> stats = new ArrayList<>();
        for (Statement st : sg.statements()) {
            if (st == null)
                continue;
            stats.add((String) st.visit(this));
        }
        
        stSwitchGroup.add("labels", labels);
        stSwitchGroup.add("stats", stats);
        
        return (T) stSwitchGroup.render();
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT SWITCH_STAT
     */
    public T visitSwitchStat(SwitchStat st) {
        Log.log(st.line + ": Visiting a SwitchStat");
        
        // Generated template after evaluating this visitor
        ST stSwitchStat = _stGroup.getInstanceOf("SwitchStat");
        // Is this a protocol 'tag'?
        if (st.expr().type.isProtocolType())
            _isProtocolCase = true;
        
        String expr = (String) st.expr().visit(this);
        List<String> switchGroup = new ArrayList<>();
        
        for (SwitchGroup sg : st.switchBlocks())
            switchGroup.add((String) sg.visit(this));
        
        stSwitchStat.add("tag", _isProtocolCase);
        stSwitchStat.add("expr", expr);
        stSwitchStat.add("block", switchGroup);
        
        // Reset value
        _isProtocolCase = false;
        
        return (T) stSwitchStat.render();
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT CAST_EXPR
     */
    public T visitCastExpr(CastExpr ce) {
        Log.log(ce.line + ": Visiting a CastExpr");
        
        // Generated template after evaluating this invocation
        ST stCastExpr = _stGroup.getInstanceOf("CastExpr");
        // This result in
        //      ((type) (expr))
        String type = (String) ce.type().visit(this);
        String expr = (String) ce.expr().visit(this);
        
        stCastExpr.add("type", type);
        stCastExpr.add("expr", expr);
        
        return (T) stCastExpr.render();
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT INVOCATION
     */
    @SuppressWarnings("rawtypes")
    public T visitInvocation(Invocation in) {
        Log.log(in.line + ": Visiting Invocation (" + in.targetProc.name().getname() + ")");
        
        // Generated template after evaluating this invocation
        ST stInvocation = null;
        ST stImport = null;
        // Target procedure
        ProcTypeDecl invokedProc = in.targetProc;
        // Name of invoked procedure
        String invokedProcName = invokedProc.name().getname();
        // Check local procedures, if none is found then the procedure must come
        // from a different file (and package)
        if (_currCompilation.packageName.equals(invokedProc.myCompilation.packageName)) {
            String name = "";
            if (Helper.doesProcedureYield(invokedProc))
                name = Helper.makeVariableName(invokedProcName + signature(invokedProc),
                        0, Tag.PROCEDURE_NAME);
            else
                name = Helper.makeVariableName(invokedProcName + signature(invokedProc),
                        0, Tag.METHOD_NAME);
            invokedProcName = invokedProc.myCompilation.fileNoExtension() + "." + name;
        } else if (invokedProc.isNative) {
            // Make the package visible on import by using the qualified name of the
            // class the procedure belongs to and the name of the folder the procedure's
            // class belongs to, e.g., std.io.println(), where
            //      'std' is the name of the package,
            //      'io' is the name of the class/file,
            //      'println' is the method declared in the class
            invokedProcName = invokedProc.filename + "." + invokedProcName;
            stImport = _stGroup.getInstanceOf("Import");
            stImport.add("package", invokedProc.library);
            _importList.add(stImport.render());
        } else {
            // TODO: Procedures called from other packages
        }
        
        // These are the formal parameters of a procedure/method which are specified
        // by a list of comma-separated arguments
        Sequence<Expression> parameters = in.params();
        String[] paramsList = (String[]) parameters.visit(this);
        
        // For an invocation of a procedure that 'yields' and one which
        // is not inside par block, we wrap the procedure in a 'par' block
        if (Helper.doesProcedureYield(invokedProc) && _currParBlock == null) {
            return (new ParBlock(
                    new Sequence(new ExprStat(in)), // statements
                    new Sequence()))                // barriers
                    .visit(this);   // return a procedure wrapped in a 'par' block
        }
        
        // Does this procedure yield?
        if (Helper.doesProcedureYield(invokedProc)) {
            stInvocation = _stGroup.getInstanceOf("InvocationProcType");
            stInvocation.add("parBlock", _currParBlock);
            if (!_barrierList.isEmpty())
                stInvocation.add("barrier", _barrierList);
        } else
            // Must be an invocation made through a static Java method
            stInvocation = _stGroup.getInstanceOf("Invocation");
        
        stInvocation.add("name", invokedProcName);
        stInvocation.add("vars", paramsList);
        
        return (T) stInvocation.render();
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT PROTOCOL_TYPE_DECL
     */
    public T visitProtocolTypeDecl(ProtocolTypeDecl pd) {
        Log.log(pd.line + ": Visiting a ProtocolTypeDecl (" + pd.name().getname() + ")");
        
        // Generated template after evaluating this visitor
        ST stProtocolClass = _stGroup.getInstanceOf("ProtocolClass");
        
        String name = (String) pd.name().visit(this);
        List<String> modifiers = new ArrayList<>();
        List<String> body = new ArrayList<>();
        
        for (Modifier m : pd.modifiers())
            modifiers.add((String) m.visit(this));
        
        // Add this protocol to the collection of protocols for reference
        _protocMap.put(name, name);
        
        // TODO: annotations??
        
        // The scope in which all members appear in a protocol
        for (ProtocolCase pc : pd.body())
            body.add((String) pc.visit(this));
        
        stProtocolClass.add("name", name);
        stProtocolClass.add("modifiers", modifiers);
        stProtocolClass.add("body", body);
        
        return (T) stProtocolClass.render();
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT PROTOCOL_CASE
     */
    public T visitProtocolCase(ProtocolCase pc) {
        Log.log(pc.line + ": Visiting a ProtocolCase (" + pc.name().getname() + ")");
        
        // Generated template after evaluating this visitor
        ST stProtocolCase = _stGroup.getInstanceOf("ProtocolCase");
        
        // Since we are keeping the name of a tag as it is, this
        // shouldn't cause any name collision
        String protocName = (String) pc.name().visit(this);
        // This may create name collision problems because we use 'RecordAccess'
        // for protocols and records
        _recordFieldMap.clear();
        
        // The scope in which all members of this tag appeared
        for (RecordMember rm : pc.body())
            rm.visit(this);
        
        // The list of fields that should be passed to the constructor
        // of the static class that the record belongs to
        if (!_recordFieldMap.isEmpty()) {
            stProtocolCase.add("types", _recordFieldMap.values());
            stProtocolCase.add("vars", _recordFieldMap.keySet());
        }
        
        stProtocolCase.add("name", protocName);
        
        return (T) stProtocolCase.render();
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT PROTOCOL_LITERAL
     */
    public T visitProtocolLiteral(ProtocolLiteral pl) {
        Log.log(pl.line + ": Visiting a ProtocolLiteral (" + pl.name().getname() + ")");
        
        // Generated template after evaluating this visitor
        ST stProtocolLiteral = _stGroup.getInstanceOf("ProtocolLiteral");
        String type = (String) pl.name().visit(this);
        String tag = (String) pl.tag().visit(this);
        
        // This map is used to determine the order in which values are
        // used with the constructor of the class associated with this
        // protocol's 'type'
        HashMap<String, String> members = new LinkedHashMap<>();
        // We only need the members of the 'tag' being used
        ProtocolCase target = null;
        ProtocolTypeDecl pt = (ProtocolTypeDecl) _topLevelDecls.get(type);
        if (pt != null) { // This should never be 'null'
            for (ProtocolCase pc : pt.body()) {
                if (pc.name().getname().equals(tag)) {
                    target = pc;
                    break;
                }
            }
            // Now that we have the target 'tag', iterate over all
            // of its members
            for (RecordMember rm : target.body()) {
                String name = (String) rm.name().visit(this);
                members.put(name, null);
            }
        }
        
        // Similar to a 'RecordLiteral', a visit to a 'RecordMemberLiteral'
        // would returns a string "z = 3", where 'z' is the member of a protocol
        // and '3' is the literal value used to initialized 'z' with. 
        for (RecordMemberLiteral rm : pl.expressions()) {
            String lhs = (String) rm.name().visit(this);
            String expr = (String) rm.expr().visit(this);
            if (members.put(lhs, expr) == null)
                Log.log(pl.line + ":    Settings '" + lhs + "' with '" + expr + "'");
            else
                Log.log(pl.line + ":    Updating '" + lhs + "' with '" + expr + "'");
        }
        
        stProtocolLiteral.add("type", type);
        stProtocolLiteral.add("tag", tag);
        stProtocolLiteral.add("vals", members.values());
        
        return (T) stProtocolLiteral.render();
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT RECORD_TYPE_DECL
     */
    public T visitRecordTypeDecl(RecordTypeDecl rt) {
        Log.log(rt.line + ": Visiting a RecordTypeDecl (" + rt.name().getname() + ")");
        
        // Generated template after evaluating this visitor
        ST stRecordClass = _stGroup.getInstanceOf("RecordClass");
        String recName = (String) rt.name().visit(this);
        List<String> modifiers = new ArrayList<>();
        
        for (Modifier m : rt.modifiers())
            modifiers.add((String) m.visit(this));
        
        // Create a tag for this record and then add it to the collection
        // of records for reference
        _recordMap.put(rt.name().getname(), recName);
        _recordFieldMap.clear();
        
        // The scope in which all members appeared in a record
        for (RecordMember rm : rt.body())
            rm.visit(this);
        
        // The list of fields that should be passed to the constructor
        // of the static class that the record belongs to
        if (!_recordFieldMap.isEmpty()) {
            stRecordClass.add("types", _recordFieldMap.values());
            stRecordClass.add("vars", _recordFieldMap.keySet());
        }
        
        stRecordClass.add("name", recName);
        stRecordClass.add("modifiers", modifiers);
        
        return (T) stRecordClass.render();
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT RECORD_MEMBER
     */
    public T visitRecordMember(RecordMember rm) {
        Log.log(rm.line + ": Visiting a RecordMember (" + rm.type() + " " + rm.name().getname() + ")");
        
        // Grab the type and name of a declared variable
        String name = (String) rm.name().visit(this);
        String type = (String) rm.type().visit(this);
        
        // Add this field to the collection of record members for reference
        _recordFieldMap.put(name, type);
        _recordMemberMap.put(name, name);
        
        // Ignored the value returned by this visitor. The reason for this
        // is that the template for records take a list of types and variable
        // names.
        return null;
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT RECORD_LITERAL
     */
    public T visitRecordLiteral(RecordLiteral rl) {
        Log.log(rl.line + ": Visiting a RecordLiteral (" + rl.name().getname() + ")");
        
        // Generated template after evaluating this visitor
        ST stRecordListeral = _stGroup.getInstanceOf("RecordLiteral");
        String type = (String) rl.name().visit(this);
        
        // This map is used to determine the order in which values are
        // used with the constructor of the class associated with this
        // record's 'type'
        HashMap<String, String> members = new LinkedHashMap<>();
        RecordTypeDecl rt = (RecordTypeDecl) _topLevelDecls.get(type);
        if (rt != null) { // This should never be 'null'
            for (RecordMember rm : rt.body()) {
                String name = (String) rm.name().visit(this);
                members.put(name, null);
            }
        }
        
        // This can get hairy! A visit to a 'RecordMemberLiteral' would
        // return a string "z = 3", where 'z' is the member of a record
        // and '3' is the literal value used to initialized 'z' with. This
        // is something we don't want to do. Instead, we need to return
        // the literal value assigned to 'z'        
        for (RecordMemberLiteral rm : rl.members()) {
            String lhs = (String) rm.name().visit(this);
            String expr = (String) rm.expr().visit(this);
            if (members.put(lhs, expr) == null)
                Log.log(rl.line + ":    Settings '" + lhs + "' with '" + expr + "'");
            else
                Log.log(rl.line + ":    Updating '" + lhs + "' with '" + expr + "'");
        }
        
        stRecordListeral.add("type", type);
        stRecordListeral.add("vals", members.values());
        
        return (T) stRecordListeral.render();
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT RECORD_ACCESS
     */
    public T visitRecordAccess(RecordAccess ra) {
        Log.log(ra.line + ": Visiting a RecordAccess (" + ra + ")");

        // Generated template after evaluating this visitor
        ST stRecordAccess = null;
        
        if (ra.record().type.isRecordType()) {
            stRecordAccess = _stGroup.getInstanceOf("RecordAccess");
            String name = (String) ra.record().visit(this);
            String field = (String) ra.field().visit(this);
            
            stRecordAccess.add("name", name);
            stRecordAccess.add("member", field);
        } else if (ra.record().type.isProtocolType()) {
            stRecordAccess = _stGroup.getInstanceOf("ProtocolAccess");
            ProtocolTypeDecl pt = (ProtocolTypeDecl) ra.record().type;
            String protocName = (String) pt.name().visit(this); // wrapper class
            String name = (String) ra.record().visit(this);     // reference to inner class type
            String field = (String) ra.field().visit(this);     // field in inner class
            
            stRecordAccess.add("protocName", protocName);
            stRecordAccess.add("tag", _currProtocolTag);
            stRecordAccess.add("var", name);
            stRecordAccess.add("member", field);
        }
        
        return (T) stRecordAccess.render();
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT PAR_BLOCK
     */
    public T visitParBlock(ParBlock pb) {
        Log.log(pb.line + ": Visiting a ParBlock with " + pb.stats().size() + " statements.");
        
        // Report a warning message for having an empty 'par' block?
        if (pb.stats().size() == 0)
            return null;
        // Generated template after evaluating this visitor
        ST stParBlock = _stGroup.getInstanceOf("ParBlock");
        // Save previous 'par' block
        String prevParBlock = _currParBlock;
        // Save previous 'barrier' expressions
        List<String> prevBarrier = _barrierList;
        if (!_barrierList.isEmpty())
            _barrierList = new ArrayList<>();
        // Create a name for this new 'par' block
        _currParBlock = Helper.makeVariableName(Tag.PAR_BLOCK_NAME.getTag(), ++_parDecId, Tag.LOCAL_NAME);
        // Is this 'par' block new?
        if (_parMap.get(_currParBlock) == null) {
            // Yes! register this block.
            _parMap.put(_currProcName, pb.stats().size());
            // Since this is a new 'par' block, we need to create a
            // variable inside the process in which this 'par' block
            // was declared
            if (_currProcName != null) { // This should never be 'null'
                stParBlock.add("name", _currParBlock);
                stParBlock.add("count", pb.stats().size());
                stParBlock.add("process", "this");
            }
        } else
            ; // Nothing to do for now
        
        // Increment jump label
        stParBlock.add("jump", ++_jumLabel);
        // Add jump label to the 'switch' list
        _switchLabelList.add(renderSwitchLabel(_jumLabel));
        // Add the 'barrier' this par block enrolls in
        Sequence<Expression> barriers = pb.barriers();
        if (barriers != null) {
            for (Expression ex : barriers)
                _barrierList.add((String) ex.visit(this));
        }
        
        // Visit the sequence of statements in the 'par' block
        Sequence<Statement> statements = pb.stats();
        if (statements.size() > 0) {
            // Rendered value of each statement
            List<String> stmts = new ArrayList<>();
            for (Statement st : statements) {
                if (st == null)
                    continue;
                // An 'expression' is any valid unit of code that resolves to a value,
                // that is, it can be a combination of variables, operations and values
                // that yield a result. An 'statement' is a line of code that performs
                // some action, e.g., print statements, an assignment statement, etc.
                if (st instanceof ExprStat && ((ExprStat) st).expr() instanceof Invocation) {
                    ExprStat es = (ExprStat) st;
                    Invocation in = (Invocation) es.expr();
                    // If this invocation is made on a process then visit the invocation
                    // and return a _string_ representing the wrapper class for this
                    // procedure; e.g.,
                    //      (new <className>(...) {
                    //          @Override public synchronized void run() { ... }
                    //          @Override public finalize() { ... }
                    //      }.schedule();
                    if (Helper.doesProcedureYield(in.targetProc))
                        stmts.add((String) in.visit(this));
                    else // Otherwise, the invocation is made through a static Java method
                        stmts.add((String) createAnonymousProcTypeDecl(st).visit(this));
                } else
                    stmts.add((String) createAnonymousProcTypeDecl(st).visit(this));
            }
            stParBlock.add("body", stmts);
        }
        // Add 'barrier' to par block
        if (!_barrierList.isEmpty())
            stParBlock.add("barrier", _barrierList);
        
        // Restore 'par' block
        _currParBlock = prevParBlock;
        // Restore 'barrier' expressions
        _barrierList = prevBarrier;
        
        return (T) stParBlock.render();
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT UNARY_POST_EXPR
     */
    public T visitUnaryPostExpr(UnaryPostExpr ue) {
        Log.log(ue.line + ": Visiting a UnaryPostExpr (" + ue.opString() + ")");
        
        // Generated template after evaluating this visitor
        ST stUnaryPostExpr = _stGroup.getInstanceOf("UnaryPostExpr");
        String operand = (String) ue.expr().visit(this);
        String op = ue.opString();
        
        stUnaryPostExpr.add("operand", operand);
        stUnaryPostExpr.add("op", op);
        
        return (T) stUnaryPostExpr.render();
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT UNARY_PRE_EXPR
     */
    public T visitUnaryPreExpr(UnaryPreExpr ue) {
        Log.log(ue.line + ": Visiting a UnaryPreExpr (" + ue.opString() + ")");
        
        // Generated template after evaluating this visitor
        ST stUnaryPreExpr = _stGroup.getInstanceOf("UnaryPreExpr");
        String operand = (String) ue.expr().visit(this);
        String op = ue.opString();
        
        stUnaryPreExpr.add("operand", operand);
        stUnaryPreExpr.add("op", op);
        
        return (T) stUnaryPreExpr.render();
    }
    
    // **********************************************************************
    // * Helper-methods are declared below
    // *
    // **********************************************************************
    
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
        } else if (t.isPrimitiveType()) // This is needed because we can only have wrapper class
            baseType = Helper.getWrapperType(t);
        
        return baseType;
    }
    
    /**
     * This is used for newly-created processes.
     */
    private void resetGlobals() {
        _parDecId = 0;
        _varDecId = 0;
        _localDecId = 0;
        _jumLabel = 0;

        _localParamFieldMap.clear();
        _switchLabelList.clear();
        
        _formalParamFieldMap.clear();
        _paramDeclNameMap.clear();
    }
    
    /**
     * Returns a string representation of a jump label.
     */
    private String renderSwitchLabel(int jump) {
        ST stSwitchCase = _stGroup.getInstanceOf("SwitchCase");
        stSwitchCase.add("jump", jump);
        return stSwitchCase.render();
    }
    
    /**
     * Creates and returns an anonymous procedure for non-invocations.
     * 
     * @param st
     *      The statement inside the body of a procedure.
     * @return
     *      An 'anonymous' procedure.
     */
    @SuppressWarnings("rawtypes")
    private ProcTypeDecl createAnonymousProcTypeDecl(Statement st) {
        return new ProcTypeDecl(
                new Sequence(),               // modifiers
                null,                         // return type
                new Name("Anonymous"),        // procedure name
                new Sequence(),               // formal parameters
                new Sequence(),               // implement
                null,                         // annotations
                new Block(new Sequence(st))); // body
    }
    
    private T createChannelReadExpr(String lhs, String op, ChannelReadExpr cr) {
        Log.log(cr.line + ": Creating Channel Read Expression");
        
        // Generated template after evaluating this visitor
        ST stChannelReadExpr = _stGroup.getInstanceOf("ChannelReadExpr");
        // 'c.read()' is a channel-end expression, where 'c'
        // is the reading end of a channel.
        Expression chanExpr = cr.channel();
        // 'c' is the name of the channel
        String chanEndName = "";
        if (chanExpr instanceof NameExpr)
            chanEndName = (String) chanExpr.visit(this);
        stChannelReadExpr.add("chanName", chanEndName);
        // Add the 'switch' block for resumption
        for (int label = 0; label < 2; ++label) {
            // Increment jump label
            stChannelReadExpr.add("resume" + label, ++_jumLabel);
            // Add jump label to the 'switch' list
            _switchLabelList.add(renderSwitchLabel(_jumLabel));
        }
        
        stChannelReadExpr.add("lhs", lhs);
        stChannelReadExpr.add("op", op);
        
        return (T) stChannelReadExpr.render();
    }
    
    /**
     * Type              | Signature
     * ------------------+------------------------------
     * boolean           | Z
     * byte              | B
     * short             | S
     * char              | C
     * int               | I
     * long              | J
     * float             | F
     * double            | D
     * void              | V
     * barrier           | R
     * timer             | M
     * array             | [T;
     * record            | <Rn;
     * protocol          | <Pn;
     * procedure         | (parameter list signature) t
     * named type        | Lname;
     * channel type      | {t;
     * channeld end type | {t;! or {t;?
     */
    private String signature(ProcTypeDecl pd) {
        String s = "";
        for (ParamDecl param : pd.formalParams()) {
            s = s + "$" + param.type().signature();
            // Array [t; where 't' is the baste type
            if (param.type().isArrayType())
                s = s.replace("[", "ar").replace(";", "");
            // <Rn; 'n' is the name
            else if (param.type().isRecordType())
                s = s.replace("<", "rc").replace(";", "");
            // <Pn; 'n' is the name
            else if (param.type().isProtocolType())
                s = s.replace("<", "pt").replace(";", "");
            // {t;
            else if (param.type().isChannelType())
                s = s.replace("{", "ct").replace(";", "");
            // channel end type
            else if (param.type().isChannelEndType()) {
                if (((ChannelEndType) param.type()).isRead()) // {t;? channel read
                    s = s.replace("{", "cr").replace(";", "").replace("?", "");
                else // {t;! channel write
                    s = s.replace("{", "cw").replace(";", "").replace("!", "");
            } else
                s = s.replace(";", "");
        }
        return s;
    }
}
