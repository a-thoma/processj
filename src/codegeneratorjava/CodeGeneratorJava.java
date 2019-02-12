package codegeneratorjava;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import ast.AST;
import ast.ArrayType;
import ast.Assignment;
import ast.BinaryExpr;
import ast.Block;
import ast.ChannelType;
import ast.Compilation;
import ast.ExprStat;
import ast.Expression;
import ast.Invocation;
import ast.LocalDecl;
import ast.Modifier;
import ast.Name;
import ast.NamedType;
import ast.ParBlock;
import ast.ParamDecl;
import ast.PrimitiveLiteral;
import ast.PrimitiveType;
import ast.ProcTypeDecl;
import ast.Sequence;
import ast.Type;
import ast.Var;
import namechecker.ResolveImports;
import processj.runtime.PJBarrier;
import processj.runtime.PJMany2ManyChannel;
import processj.runtime.PJMany2OneChannel;
import processj.runtime.PJOne2ManyChannel;
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
    private String m_currParBloc = null;
    
    /**
     * Map of imports.
     */
    private List<String> m_importList = null;

    /**
     * Map of formal parameters transformed to fields.
     */
    private HashMap<String, String> m_formalParamFieldMap = null;
    
    /**
     * Map of local parameters transformed to fields.
     */
    private HashMap<String, String> m_localParamFieldMap = null;

    /**
     * Map of yielding and non-yielding procedures transformed to classes
     * or Java methods.
     */
    private HashMap<String, String> m_procMap = null;
    
    /**
     * Map of 'par' blocks declared in a process. This map associates the
     * name of a 'par' block with the number of processes invoked within
     * its block.
     */
    private HashMap<String, Integer> m_parMap = null;

    /**
     * Identifier for a procedure declaration.
     */
    private int m_procDecId = 0;

    /**
     * Identifier for a method declaration.
     */
    private int m_methodDecdId = 0;

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
        m_procMap = new LinkedHashMap<>();
        m_formalParamFieldMap = new LinkedHashMap<>();
        m_localParamFieldMap = new LinkedHashMap<>();
        m_importList = new ArrayList<>();
        m_parMap = new LinkedHashMap<>();
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
        Log.log(String.format("Executable Java source file generated by the ProcessJ "
                    + "compiler."));
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
        // Name of invoked procedure
        m_currProcName = (String) pd.name().visit(this);
        // Procedures are static classes which belong to the same package. To avoid
        // having classes with the same name, we generate a new name for this procedure
        String procName = null;
        // Initialize global vars for new PJProcess class
        resetGlobals();
        // Restore local parameters tables for this procedure
        m_localParamFieldMap.clear();
        // Formal parameters that must be passed to the procedure
        Sequence<ParamDecl> formals = pd.formalParams();
        // The scope in which all declarations appear in a procedure
        String[] body = (String[]) pd.body().visit(this);

        if (formals != null && formals.size() > 0) {
            // Iterate through and visit every parameter declaration
            for (int i = 0; i < formals.size(); ++i) {
                ParamDecl actualParam = formals.child(i);
                // Retrieve the name and type of a parameter in the parameter list
                String name = (String) actualParam.paramName().visit(this);
                String type = (String) actualParam.type().visit(this);
                // Create a tag for this parameter and then add it to the collection
                // of parameters for reference
                name = Helper.makeVariableName(name, ++m_varDecId, Tag.PARAM_NAME);
                m_formalParamFieldMap.put(name, type);
            }
        } else {
            // Procedure does not take any parameters
            m_formalParamFieldMap.clear();
        }

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
            procName = Helper.makeVariableName(m_currProcName, ++m_procDecId, Tag.PROCEDURE_NAME);
            stProcTypeDecl = m_stGroup.getInstanceOf("ProcClass");
            stProcTypeDecl.add("name", procName);
        } else {
            // Otherwise, grab the instance of a non-yielding procedure instead
            // to define a new static Java method
            procName = Helper.makeVariableName(m_currProcName, ++m_methodDecdId, Tag.METHOD_NAME);
            stProcTypeDecl = m_stGroup.getInstanceOf("Method");
            stProcTypeDecl.add("name", procName);
            stProcTypeDecl.add("type", procType);
            // Do we have any modifier?
            if (modifiers != null && modifiers.length > 0)
                stProcTypeDecl.add("modifier", modifiers);
            stProcTypeDecl.add("body", body);
        }

        // Add this procedure to the collection of procedures for reference
        m_procMap.put(m_currProcName + pd.signature(), procName);
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
            // TODO: this needs to change (for now this is to test `println')
            stProcTypeDecl.add("syncBody", body);
            
            stProcTypeDecl.add("main", stMain.render());
        }
        
        // The list of command line arguments should be passed to the constructor
        // of the static class that the main method belongs to (some procedure class)
        // or should be passed to the Java method (some static method)
        if (!m_formalParamFieldMap.isEmpty()) {
            stProcTypeDecl.add("types", m_formalParamFieldMap.values());
            stProcTypeDecl.add("vars", m_formalParamFieldMap.keySet());
        }
        
        if (!m_localParamFieldMap.isEmpty()) {
            stProcTypeDecl.add("ltypes", m_localParamFieldMap.values());
            stProcTypeDecl.add("lvars", m_localParamFieldMap.keySet());
        }
        
        // Restore and reset previous values
        m_currProcName = prevProcName;

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
        String name = (String) pd.paramName().visit(this);
        // Generated template after evaluating this visitor
        ST stParamDecl = m_stGroup.getInstanceOf("Var");
        stParamDecl.add("type", type);
        stParamDecl.add("name", name);

        return (T) stParamDecl.render();
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
        String type = (String) ld.type().visit(this);
        String val = null;
        // This variable could be initialized, e.g., through an assignment operator
        Expression expr = ld.var().init();
        // Visit the expressions associated with this variable
        if (expr != null && expr.type instanceof PrimitiveType) {
            val = (String) expr.visit(this);
        }
        
        // TODO:

        // If we reach this section, then we have a simple variable declaration
        // inside the body of a procedure or a static Java method
        ST stVar = m_stGroup.getInstanceOf("Var");
        stVar.add("type", type);
        stVar.add("name", name);
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
        
        if (Helper.isInvalidJavaIdentifier(na.getname())) {
            Log.log(String.format("%s: Special keyword '%s' found.", ErrorSeverity.WARNING, na.getname()));
        }
        
        return (T) na.getname();
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
            chanType = PJOne2OneChannel.class.getSimpleName(); break;
        case ChannelType.SHARED_READ:
            chanType = PJOne2ManyChannel.class.getSimpleName(); break;
        case ChannelType.SHARED_WRITE:
            chanType = PJMany2OneChannel.class.getSimpleName(); break;
        case ChannelType.SHARED_READ_WRITE:
            chanType = PJMany2ManyChannel.class.getSimpleName(); break;
        }
        // TODO: resolve parameterized type of channel
        Type type = ct.baseType();
        
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
	System.out.println(invokedProc);
	System.out.println(invokedProc.name());

        String invokedProcName = invokedProc.name().getname();
        Log.log(in.line + ": Visiting Invocation (" + invokedProcName + ")");
        // Generated template after evaluating this invocation
        ST stInvocation = m_stGroup.getInstanceOf("Invocation");
        // An import is done when an invocation comes from a different package
        String packageName = Helper.getPackage(invokedProc.myPackage, m_sourceFile);
        // Check local procedures, if none is found then the procedure must come
        // from a different file (and package) 
        if (invokedProc.myPackage.contains(m_sourceFile)) {
            // The procedure is looked up by its signature.
            // Note:this should never return 'null'!
            invokedProcName = m_procMap.get(invokedProcName + invokedProc.signature());
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
            ; // TODO: non-native procedures...
        
        // These are the formal parameters of a procedure/method which are specified
        // by a list of comma-separated arguments
        Sequence<Expression> parameters = in.params();
        String[] paramsList = (String[]) parameters.visit(this);
        
        // Does this procedure yield?
        if (Helper.doesProcedureYield(invokedProc)) {
            // TODO: for now do nothing!
        }
        
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
        String prevParBlock = m_currParBloc;
        // Create a name for this new 'par' block
        m_currParBloc = Helper.makeVariableName(Tag.PAR_BLOCK_NAME.getTag(), ++m_parDecId, Tag.LOCAL_NAME);
        // Is this 'par' block new?
        if (m_parMap.get(m_currParBloc) == null) {
            // Yes! register this block.
            m_parMap.put(m_currProcName, 0);
            // Since this is a new 'par' block, we need to create a
            // member field inside the process in which this 'par'
            // block was declared
            if (m_currProcName != null) { // This can never be 'null. The CodeGen will break if it is!!
                ST stVar = m_stGroup.getInstanceOf("Var");
                stVar.add("name", m_currParBloc);
                stVar.add("val", "new PJPar(" + pb.stats().size() + ", this)");
                m_localParamFieldMap.put(stVar.render(), "PJPar");
            }
        } else
            ; // No...
        
        // Restore 'par' block
        m_currParBloc = prevParBlock;
        
        return null;
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
        return null;
    }
    
    /**
     * This is used for newly-created processes.
     */
    private void resetGlobals() {
        m_parDecId = 0;
        m_varDecId = 0;
        m_localDecId = 0;
    }
}
