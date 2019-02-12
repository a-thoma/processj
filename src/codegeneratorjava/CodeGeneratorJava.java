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

import ast.AST;
import ast.ArrayType;
import ast.Assignment;
import ast.BinaryExpr;
import ast.Block;
import ast.ChannelEndExpr;
import ast.ChannelEndType;
import ast.ChannelType;
import ast.Compilation;
import ast.ExprStat;
import ast.Expression;
import ast.Invocation;
import ast.LocalDecl;
import ast.Modifier;
import ast.Name;
import ast.NameExpr;
import ast.NamedType;
import ast.ParBlock;
import ast.ParamDecl;
import ast.PrimitiveLiteral;
import ast.PrimitiveType;
import ast.ProcTypeDecl;
import ast.Sequence;
import ast.Statement;
import ast.Type;
import ast.Var;
import processj.runtime.PJBarrier;
import processj.runtime.PJChannelType;
//import processj.runtime.PJMany2ManyChannel;
//import processj.runtime.PJMany2OneChannel;
//import processj.runtime.PJOne2ManyChannel;
import processj.runtime.PJOne2OneChannel;
import processj.runtime.PJTimer;
import utilities.ErrorSeverity;
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
    private final String m_stGammarFile = "resources/stringtemplates/java/grammarTemplatesJava.stg";
    
    /**
     * Current java version.
     */
    private final String m_currentJVM = System.getProperty("java.version");

    /**
     * Collection of templates, imported templates, and/or groups that
     * contain formal template definitions.
     */
    private STGroup m_stGroup;

    /**
     * The source filename.
     */
    private String m_sourceFile = null;
    
    /**
     * The package name.
     */
    private String m_packageName = null;

    /**
     * The user working directory.
     */
    private String m_workingDir = null;

    /**
     * Current procedure call.
     */
    private String m_currProcName = null;
    
    /**
     * Current 'par' block.
     */
    private String m_currParBlock = null;
    
    /**
     * List of imports.
     */
    private Set<String> m_importList = null;

    /**
     * Map of formal parameters transformed to fields.
     */
    private HashMap<String, String> m_formalParamFieldMap = null;
    
    /**
     * Map of formal parameters names to name tags.
     */
    private HashMap<String, String> m_formalParamNameMap = null;
    
    /**
     * Map of local parameters transformed to fields.
     */
    private HashMap<String, String> m_localParamFieldMap = null;
    
    /**
     * Map of 'par' blocks declared in a process. This map associates the
     * name of a 'par' block with the number of processes invoked within
     * its block.
     */
    private HashMap<String, Integer> m_parMap = null;
    
    /**
     * List of switch labels.
     */
    private List<String> m_switchLabelList = null;

    /**
     * Identifier for a parameter declaration.
     */
    private int m_varDecId = 0;
    
    /**
     * Identifier for a 'par' block declaration.
     */
    private int m_parDecId = 0;

    /**
     * Identifier for a local variable declaration.
     */
    private int m_localDecId = 0;
    
    /**
     * Jump label.
     */
    private int m_jumLabel = 0;
    
    /**
     * Current 'chan' type.
     */
    private PJChannelType m_currChanType = null;

    /**
     * Internal constructor that loads a group file containing a collection of
     * templates, imported templates, and/or groups containing formal template
     * definitions. Additionally, the constructor initializes a symbol table
     * with top level types declarations.
     * 
     * @param topLevelDeclsTable
     * 			The top level types which can be procedures, records, protocols,
     * 			constants, and/or external types.
     */
    public CodeGeneratorJava(SymbolTable topLevelDeclsTable) {
        Log.log("==========================================");
        Log.log("*      C O D E   G E N E R A T O R       *");
        Log.log("*                J A V A                 *");
        Log.log("==========================================");
        
        m_stGroup = new STGroupFile(m_stGammarFile);
        m_formalParamFieldMap = new LinkedHashMap<>();
        m_localParamFieldMap = new LinkedHashMap<>();
        m_importList = new LinkedHashSet<>();
        m_switchLabelList = new ArrayList<>();
        m_parMap = new LinkedHashMap<>();
        
        m_formalParamNameMap = new LinkedHashMap<>();
    }

    /**
     * Initializes the source file of a ProcessJ program with a given
     * pathname string.
     *
     * @param sourceFile
     *          A ProcessJ source file.
     */
    public void setSourceFile(String sourceFile) {
        this.m_sourceFile = sourceFile;
    }
    
    /**
     * @return
     */
    public String getSourceFile() {
        return m_sourceFile;
    }
    
    /**
     * Sets the system properties to a current working directory.
     *
     * @param workingDir
     * 			A working directory.
     */
    public void setWorkingDirectory(String workingDir) {
        this.m_workingDir = workingDir;
    }
    
    /**
     * @return
     */
    public String getWorkingDirectory() {
        return m_workingDir;
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
        
        // Code generated by the template
        String templateResult = null;
        // Instance of Compilation template to fill in
        ST stCompilation = m_stGroup.getInstanceOf("Compilation");

        
        // TODO: Reference to all the top level types
        // Here..

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
            String declStr = (String) decls.visit(this);
            if (declStr != null) {
                body.add(declStr);
            }
        }

        stCompilation.add("packageName", m_packageName);
        stCompilation.add("fileName", m_sourceFile);
        stCompilation.add("name", m_sourceFile);
        stCompilation.add("body", body);
        stCompilation.add("version", m_currentJVM);
        
        if (m_importList.size() > 0)
            stCompilation.add("imports", m_importList);

        // Rendered code
        templateResult = stCompilation.render();
        // Debug code generated by the ProcessJ JVM compiler
        Log.log(String.format("Executable Java source file generated by the ProcessJ JVM compiler."));
        Log.log(new StringBuilder()
                    .append("\n--------------------------------------------------------------------\n")
                    .append(templateResult)
                    .append("\n--------------------------------------------------------------------\n")
                    .toString());
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
        String prevProcName = m_currProcName;
        // Save previous jump labels
        List<String> prevLabels = m_switchLabelList;
        if (!m_switchLabelList.isEmpty())
            m_switchLabelList = new ArrayList<>();
        // Name of invoked procedure
        m_currProcName = (String) pd.name().visit(this);
        // Procedures are static classes which belong to the same package. To avoid
        // having classes with the same name, we generate a new name for this procedure
        String procName = null;
        // For non-invocations, that is, for anything other that procedure that
        // 'yields', we need to extends the PJProcess class 'anonymously'
        if (m_currProcName.equals("Anonymous")) {
            // Preserve current 'jump' label
            int prevJumLabel = m_jumLabel;
            m_jumLabel = 0;
            // Grab the instance for an anonymous procedure
            stProcTypeDecl = m_stGroup.getInstanceOf("AnonymousProcess");
            // Statements that appear in the procedure
            String[] body = (String[]) pd.body().visit(this);
            
            stProcTypeDecl.add("parBlock", m_currParBlock);
            stProcTypeDecl.add("syncBody", body);
            // Add the 'switch' block
            if (!m_switchLabelList.isEmpty()) {
                ST stSwitchBlock = m_stGroup.getInstanceOf("SwitchBlock");
                stSwitchBlock.add("jumps", m_switchLabelList);
                stProcTypeDecl.add("switchBlock", stSwitchBlock.render());
            }
            // Restore 'jump' label
            m_jumLabel = prevJumLabel;
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
                procName = Helper.makeVariableName(m_currProcName + changeSignature(pd), 0, Tag.PROCEDURE_NAME);
                stProcTypeDecl = m_stGroup.getInstanceOf("ProcClass");
                stProcTypeDecl.add("name", procName);
                // The statements that appear in the body of the procedure
                stProcTypeDecl.add("syncBody", body);
            } else {
                // Otherwise, grab the instance of a non-yielding procedure instead
                // to define a new static Java method
                procName = Helper.makeVariableName(m_currProcName + changeSignature(pd), 0, Tag.METHOD_NAME);
                stProcTypeDecl = m_stGroup.getInstanceOf("Method");
                stProcTypeDecl.add("name", procName);
                stProcTypeDecl.add("type", procType);
                // Do we have any modifier?
                if (modifiers != null && modifiers.length > 0)
                    stProcTypeDecl.add("modifier", modifiers);
                stProcTypeDecl.add("body", body);
            }
            
            // Create an entry point for the ProcessJ program which is just a Java main
            // method that is called by the JVM
            if ("main".equals(m_currProcName) && pd.signature().equals(Tag.MAIN_NAME.getTag())) {
                // Grab package name
                m_packageName = pd.myPackage.substring(0, pd.myPackage.lastIndexOf("."));
                // Create an instance of a Java main method template
                ST stMain = m_stGroup.getInstanceOf("Main");
                stMain.add("class", m_sourceFile);
                stMain.add("name", procName);
                // Pass the list of command line arguments to this main method
                if (!m_formalParamFieldMap.isEmpty()) {
                    stMain.add("types", m_formalParamFieldMap.values());
                    stMain.add("vars", m_formalParamFieldMap.keySet());
                }
                // Add entry point of the program
                stProcTypeDecl.add("main", stMain.render());
            }
            
            // The list of command line arguments should be passed to the constructor
            // of the static class that the main method belongs to (some procedure class)
            // or should be passed to the Java method (some static method)
            if (!m_formalParamFieldMap.isEmpty()) {
                stProcTypeDecl.add("types", m_formalParamFieldMap.values());
                stProcTypeDecl.add("vars", m_formalParamFieldMap.keySet());
            }
            // The list of local variables defined in the body of a procedure become
            // the member variables of the procedure class
            if (!m_localParamFieldMap.isEmpty()) {
                stProcTypeDecl.add("ltypes", m_localParamFieldMap.values());
                stProcTypeDecl.add("lvars", m_localParamFieldMap.keySet());
            }
            // Add the 'switch' block
            if (!m_switchLabelList.isEmpty()) {
                ST stSwitchBlock = m_stGroup.getInstanceOf("SwitchBlock");
                stSwitchBlock.add("jumps", m_switchLabelList);
                stProcTypeDecl.add("switchBlock", stSwitchBlock.render());
            }
        }
        
        // Restore and reset previous values
        m_currProcName = prevProcName;
        // Restore previous jump labels
        m_switchLabelList = prevLabels;

        return (T) stProcTypeDecl.render();
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT BINARY_EXPR
     */
    public T visitBinaryExpr(BinaryExpr be) {
        Log.log(be.line + ": Visiting a BinaryExpr");

        // TODO:

        return null;
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT ASSIGNMENT
     */
    public T visitAssignment(Assignment as) {
        Log.log(as.line + ": Visiting an Assignment");

        // TODO:

        String op = (String) as.opString();
        String lhs = (String) as.left().visit(this);
        String rhs = (String) as.right().visit(this);

        return null;
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT PARAM_DECL
     */
    public T visitParamDecl(ParamDecl pd) {
        Log.log(pd.line + ": Visiting a ParamDecl (" + pd.type().typeName() + " " + pd.paramName().getname() + ")");
        
        // Grab the type and name of a declared variable
        String type = (String) pd.type().visit(this);
        String name = (String) pd.paramName().getname();
        
        // Create a tag for this parameter and then add it to the collection
        // of parameters for reference
        String newName = Helper.makeVariableName(name, ++m_varDecId, Tag.PARAM_NAME);
        m_formalParamFieldMap.put(newName, type);
        m_formalParamNameMap.put(name, newName);
        
        // Ignored the value returned by this visitor. The reason for this
        // is that templates for methods and procedures take a list of types
        // and variable names. If you want to return the name and type of this
        // formal parameter (e.g. some_type + " " + some_name), you must change
        // the arguments of 'Method' and 'ProcClass' in the grammarTempalteJava
        // file.
        return null;
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT LOCAL_DECL
     */
    public T visitLocalDecl(LocalDecl ld) {
        Log.log(ld.line + ": Visting a LocalDecl (" + ld.type().typeName() + " " + ld.var().name().getname() + ")");

        // We could have the following targets:
        // x = in.read();                  , a single channel read
        // x = b.read() + c.read() + ...;  , multiple channel reads
        // x = read();                     , a Java method that returns a value
        // x = a + b;                      , a binary expression
        // x = a = b ...;                  , a complex assignment

        // Returning values for a local declaration
        String name = (String) ld.var().name().visit(this);
        // Create a tag for this local channel expr parameter
        name = Helper.makeVariableName(name, ++m_localDecId, Tag.LOCAL_NAME);
        String type = (String) ld.type().visit(this);
        String val = null;
        // This variable could be initialized, e.g., through an assignment operator
        Expression expr = ld.var().init();
        // Visit the expressions associated with this variable
        if (expr != null) {
            if (expr.type instanceof PrimitiveType) {
                val = (String) expr.visit(this);
            } else if (expr.type instanceof ChannelEndType) {
                // TODO:
            }
        }
        
        if (val == null) {
            // Must be a simple declaration that requires allocating memory for a new object.
            if (m_currChanType == PJChannelType.ONE2ONE) {
                ST stChannelDecl = m_stGroup.getInstanceOf("ChannelDecl");
                stChannelDecl.add("type", type);
                val = stChannelDecl.render();
            }
        }

        // If we reach this section, then we have a simple variable declaration
        // inside the body of a procedure or a static Java method. This declaration
        // becomes a member variable of a procedure
        ST stVar = m_stGroup.getInstanceOf("Var");
        //stVar.add("type", type);
        stVar.add("name", name);
        stVar.add("val", val);
        
        if (m_currProcName != null) {
            m_localParamFieldMap.put(name, type);
        }

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
        
        if (Helper.isInvalidJavaIdentifier(na.getname())) {
            Log.log(String.format("%s: Special keyword '%s' found.", ErrorSeverity.WARNING, na.getname()));
        }
        
        String name = na.getname();
        if (!m_formalParamNameMap.isEmpty() && m_formalParamNameMap.containsKey(name))
            name = m_formalParamNameMap.get(name);
        
        return (T) name;
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT NAME_EXPR
     */
    public T visitNameExpr(NameExpr ne) {
        Log.log(ne.line + ": Visiting NameExpr (" + ne.name().getname() + ")");
        // TODO: NameExpr always points to a 'Decl'aration so check in here!!
        if (ne.myDecl instanceof ParamDecl)
            System.out.println("ParamDecl!@##$");
        return (T) ne.name().visit(this);
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT NAMED_TYPE
     */
    public T visitNamedType(NamedType nt) {
        Log.log(nt.line + ": Visiting a NamedType (" + nt.name().getname() + ")");

        return (T) nt.name().getname();
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

        return (T) li.getText();
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT CHANNEL_TYPE
     */
    public T visitChannelType(ChannelType ct) {
        Log.log(ct.line + ": Visiting a Channel Type (" + ct + ")");
        
        String chanType = null;
        
        switch (ct.shared()) {
        case ChannelType.NOT_SHARED:
            chanType = PJOne2OneChannel.class.getSimpleName();
            m_currChanType = PJChannelType.ONE2ONE;
            break;
        case ChannelType.SHARED_READ:
//            chanType = PJOne2ManyChannel.class.getSimpleName(); break;
        case ChannelType.SHARED_WRITE:
//            chanType = PJMany2OneChannel.class.getSimpleName(); break;
        case ChannelType.SHARED_READ_WRITE:
//            chanType = PJMany2ManyChannel.class.getSimpleName(); break;
        }
        // Resolve parameterized type for channel
        //  e.g. chan<int> ...; where 'int' is the Type to be resolved
        String type = getChannelType(ct.baseType());
        
        return (T) (chanType + "<" + type + ">");
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT CHANNEL_END_EXPR
     */
    public T visitChannelEndExpr(ChannelEndExpr ce) {
        Log.log(ce.line + ": Visiting a Channel End Expr (" + (ce.isRead() ? "read" : "write") + ")");
        
        return null;
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT VAR
     */
    public T visitVar(Var va) {
        Log.log(va.line + ": Visiting a Var (" + va.name().getname() + ")");

        // Generated template after evaluating this visitor
        ST stVar = m_stGroup.getInstanceOf("Var");
        // Returned values for name and expression
        String name = (String) va.name().visit(this);
        String exprStr = "";
        // This variable could be initialized, e.g., through an assignment operator
        Expression expr = va.init();
        // Visits the expressions associated with this variable
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
        
        // Type of modifiers: public, protected, private, etc..
        return (T) mo.toString();
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT BLOCK
     */
    public T visitBlock(Block bl) {
        Log.log(bl.line + ": Visiting a Block");
        
        // The scope in which declarations appears, starting with their own
        // initializers and including any further declarators such invocations
        // or sequence of statements
        String[] stats = (String[]) bl.stats().visit(this);

        return (T) stats;
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT SEQUENCE
     */
    public T visitSequence(Sequence se) {
        Log.log(se.line + ": Visiting a Sequence");
        
        // Sequence of statements enclosed in a Block statement
        List<String> sequenceStr = new ArrayList<>();
        // Iterate through every statement
        for (int i = 0; i < se.size(); ++i) {
            if (se.child(i) != null) {
                T stats = se.child(i).visit(this);
                if (stats == null)
                    continue;
                // These are either
                //      1) a sequence of statements, or
                //      2) a single statement
                // found in a Block statement, e.g. local declarations, variable
                // declarations, invocations, etc..
                if (stats instanceof String[]) {
                    String[] statsStr = (String[]) stats;
                    sequenceStr.addAll(Arrays.asList(statsStr));
                } else {
                    sequenceStr.add((String) stats);
                }
            }
        }
        
        return (T) sequenceStr.toArray(new String[0]);
    }
    
    /**
     * -----------------------------------------------------------------------------
     * VISIT INVOCATION
     */
    public T visitInvocation(Invocation in) {
        ProcTypeDecl invokedProc = in.targetProc;
        String invokedProcName = invokedProc.name().getname();
        Log.log(in.line + ": Visiting Invocation (" + invokedProcName + ")");
        // Generated template after evaluating this invocation
        ST stInvocation = null;
        // An import is done when an invocation comes from a different package
        String packageName = Helper.getPackage(invokedProc.myPackage, m_sourceFile);
        // Check local procedures, if none is found then the procedure must come
        // from a different file (and package) 
        if (invokedProc.myPackage.contains(m_sourceFile)) {
            // The procedure is looked up by its signature.
            // Note that this should never return 'null'!
            String signature = "";
            if (Helper.doesProcedureYield(invokedProc))
                signature = Helper.makeVariableName(
                        invokedProcName + changeSignature(invokedProc), 0, Tag.PROCEDURE_NAME);
            else
                signature = Helper.makeVariableName(
                        invokedProcName + changeSignature(invokedProc), 0, Tag.METHOD_NAME);
            invokedProcName = packageName + "." + signature;
        } else if (invokedProc.isNative) {
            // Make the package visible on import by using the qualified name of the
            // class the procedure belongs to and the name of the folder the procedure's
            // class belongs to, e.g., std.io.println(), where
            //      'std' is the name of the package,
            //      'io' is the name of the class/file,
            //      'println' is the method declared in the class
            invokedProcName = invokedProc.filename + "." + invokedProcName;
            m_importList.add("import " + invokedProc.library + ";");
        } else
            ; // TODO: non-native procedures??
        
        // These are the formal parameters of a procedure/method which are specified
        // by a list of comma-separated arguments
        Sequence<Expression> parameters = in.params();
        String[] paramsList = (String[]) parameters.visit(this);
        
        // Does this procedure yield?
        if (Helper.doesProcedureYield(invokedProc)) {
            stInvocation = m_stGroup.getInstanceOf("InvocationProcType");
            stInvocation.add("parBlock", m_currParBlock);
        } else
            // Must be an invocation made through a static Java method
            stInvocation = m_stGroup.getInstanceOf("Invocation");
        
        stInvocation.add("name", invokedProcName);
        stInvocation.add("vars", paramsList);
        
        return (T) stInvocation.render();
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
        ST stParBlock = m_stGroup.getInstanceOf("ParBlock");
        // Save previous 'par' block
        String prevParBlock = m_currParBlock;
        // Create a name for this new 'par' block
        m_currParBlock = Helper.makeVariableName(Tag.PAR_BLOCK_NAME.getTag(), ++m_parDecId, Tag.LOCAL_NAME);
        // Is this 'par' block new?
        if (m_parMap.get(m_currParBlock) == null) {
            // Yes! register this block.
            m_parMap.put(m_currProcName, pb.stats().size());
            // Since this is a new 'par' block, we need to create a
            // variable inside the process in which this 'par' block
            // was declared
            if (m_currProcName != null) { // This can never be 'null'!!
                stParBlock.add("name", m_currParBlock);
                stParBlock.add("count", pb.stats().size());
                stParBlock.add("process", "this");
            }
        } else
            ; // Nothing to do for now
        
        // Increment jump label
        stParBlock.add("jump", ++m_jumLabel);
        // Add jump label to the 'switch' list
        m_switchLabelList.add(renderSwitchLabel(m_jumLabel));
        
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
                // some action, e.g., print statements, an assignment statement,etc.
                if (st instanceof ExprStat && ((ExprStat) st).expr() instanceof Invocation) {
                    ExprStat es = (ExprStat) st;
                    Invocation in = (Invocation) es.expr();
                    // If this invocation is made on a process then visit the invocation
                    // and return a 'string' representing the wrapper class for this procedure
                    // e.g.
                    //      (new <className>(...) {
                    //          @Override public synchronized void run() { ... }
                    //          @Override public finalize() { ... }
                    //      }.schedule();
                    if (Helper.doesProcedureYield(in.targetProc))
                        stmts.add((String) in.visit(this));
                    else
                        // Otherwise, the invocation is made through a static Java method
                        stmts.add((String) createAnonymousProcTypeDecl(st).visit(this));
                } else
                    stmts.add((String) createAnonymousProcTypeDecl(st).visit(this));
            }
            stParBlock.add("body", stmts);
        }
        
        // Restore 'par' block
        m_currParBlock = prevParBlock;
        
        return (T) stParBlock.render();
    }
    
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
            baseType = Helper.getWrapperType(t);
        }
        
        return baseType;
    }
    
    /**
     * This is used for newly-created processes.
     */
    private void resetGlobals() {
        m_parDecId = 0;
        m_varDecId = 0;
        m_localDecId = 0;
        m_jumLabel = 0;

        m_localParamFieldMap.clear();
        m_switchLabelList.clear();
        
        m_formalParamFieldMap.clear();
        m_formalParamNameMap.clear();
    }
    
    /**
     * Returns a string representation of a jump label.
     */
    private String renderSwitchLabel(int jump) {
        ST stSwitchCase = m_stGroup.getInstanceOf("SwitchCase");
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
    
    private String changeSignature(ProcTypeDecl pd) {
        String s = "";
        for (ParamDecl param : pd.formalParams()) {
            s = s + "$" + param.type().signature();
            // Array [t; where 't' is the baste type
            if (param.type().isArrayType())
                s = s.replace("[", "ar").replace(";", "");
            // <Rn; 'n' is the name
            else if (param.type().isRecordType())
                s = s.replace("<", "r").replace(";", "");
            // <Pn; 'n' is the name
            else if (param.type().isProtocolType())
                s = s.replace("<", "p").replace(";", "");
            // {t
            else if (param.type().isChannelType())
                s = s.replace("{", "ct").replace(";", "");
        }
        return s;
    }
}
