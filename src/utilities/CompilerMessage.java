package utilities;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import ast.AST;

/**
 * The class CompilerMessage is used to create messages during a
 * tree-traversal node when processing the contents in a ProcessJ
 * file, when processing the syntax and/or semantics errors when
 * compiling or generating Java source code from a ProcessJ file,
 * or when processing command line options and/or arguments.
 * 
 * @author Ben
 * @version 10/07/2018
 * @since 1.2
 */
public abstract class CompilerMessage {
    
    private static final Object[] EMPTY_ARGUMENTS = new Object[0];
    private static final String EMPTY_STRING = "";

    /**
     * String template file locator.
     */
    protected static final String stErrorFile = "resources/stringtemplates/messages/errorTemplate.stg";
    
    /**
     * Template for error messages.
     */
    protected static final STGroup stGroup = new STGroupFile(stErrorFile);
    
    /**
     * Current running AST.
     */
    protected final AST ast;
    
    /**
     * Type of error message.
     */
    protected final IMessageNumber error;
    
    /**
     * Attributes used in templates.
     */
    protected final Object[] arguments;
    
    /**
     * Reason for the error message.
     */
    protected final Throwable throwable;
    
    /**
     * Source of the message.
     */
    protected final String fileName;
    
    /**
     * Location of the input file.
     */
    protected final String packageName;
    
    /**
     * Line in file.
     */
    protected int rowNum;
    
    /**
     * Character that generated the error/warning.
     */
    protected int columnNum;
    
    public CompilerMessage(Builder<?> builder) {
        ast = builder.ast;
        error = builder.error;
        arguments = builder.arguments;
        throwable = builder.throwable;
        fileName = builder.fileName == null ? CompilerMessageManager.INSTANCE.fileName : builder.fileName;
        packageName = builder.packageName == null ? CompilerMessageManager.INSTANCE.fileName : builder.packageName;
        rowNum = builder.myRow;
        columnNum = builder.myColumn;
    }
    
    // 
    // GETTERS
    // 
    
    public AST getAST() {
        return ast;
    }
    
    public IMessageNumber getMessageNumber() {
        return error;
    }
    
    public Object[] getArguments() {
        return arguments;
    }
    
    public Throwable getThrowable() {
        return throwable;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    public int getLine() {
        return rowNum;
    }
    
    public int getColumn() {
        return columnNum;
    }
    
    public ST getST() {
        int argCount = 0;
        ST message = null;
        if (error != null)
            message = new ST(error.getMessage());
        else
            message = new ST(EMPTY_STRING);
        if (arguments != null && arguments.length > 0)
            argCount = arguments.length;
        for (int i = 0; i < argCount; ++i)
            message.add("arg" + i, arguments[i]);
        return message;
    }
    
    public abstract String renderMessage();
    
    @Override
    public String toString() {
        return getClass().getSimpleName() +
                "(filename="        + (fileName.isEmpty() ? "none" : fileName) +
                ", package="        + (packageName.isEmpty() ? "none" : packageName) +
                ", errorNumber="    + error.getNumber() +
                ", errorMessage="   + error.getMessage() +
                ", arguments="      + (arguments != null ? "{" +
                                      Arrays.stream(arguments)
                                            .map(arg -> arg + "")
                                            .collect(Collectors.joining(",")) + "}"
                                            : "none") +
                ", reason="         + (throwable != null ?
                                            throwable.getMessage()
                                            : "none") +
                ", row="            + rowNum +
                ", column="         + columnNum +
                ")";
    }
    
    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime + result + ast.hashCode();
        result = prime + result + error.hashCode();
        result = prime + result + Arrays.hashCode(arguments);
        result = prime + result + throwable.hashCode();
        result = prime + result + fileName.hashCode();
        result = prime + result + packageName.hashCode();
        result = prime + result + rowNum;
        result = prime + result + columnNum;
        return result;
    }
    
    @Override
    public final boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        
        CompilerMessage other = (CompilerMessage) obj;
        if (this.rowNum != other.rowNum || this.columnNum != other.columnNum)
            return false;
        if (!this.fileName.equals(other.fileName) || !this.packageName.equals(other.packageName))
            return false;
        if (this.ast != other.ast) // This is ok!
            return false;
        if (!this.error.equals(other.error))
            return false;
        if (!Arrays.equals(this.arguments, other.arguments))
            return false;
        if (!this.throwable.equals(other.throwable))
            return false;
        
        return true;
    }
    
    // 
    // BUILDER
    // 
    
    /**
     * 
     * The class Builder uses descriptive methods to create error
     * messages with default or initial values.
     * 
     * @author Ben
     * @version 10/20/2018
     * @since 1.2
     *
     * @param <B>
     *            The builder type.
     */
    public static abstract class Builder<B> {
        
        protected AST ast;
        protected IMessageNumber error;
        protected Object[] arguments;
        protected Throwable throwable;
        protected String fileName;
        protected String packageName;
        protected int myRow;
        protected int myColumn;
        
        public Builder() {
            ast = null;
            error = null;
            arguments = EMPTY_ARGUMENTS;
            throwable = null;
            fileName = null;
            packageName = null;
        }
        
        protected abstract B builder();
        
        public abstract <E extends CompilerMessage> E build();
        
        public B addAST(AST ast) {
            this.ast = ast;
            return builder();
        }
        
        public B addError(IMessageNumber error) {
            this.error = error;
            return builder();
        }
        
        public B addArguments(Object... arguments) {
            this.arguments = arguments;
            return builder();
        }
        
        public B addThrowable(Throwable throwable) {
            this.throwable = throwable;
            return builder();
        }
        
        public B addFileName(String fileName) {
            this.fileName = fileName;
            return builder();
        }
        
        public B addPackageName(String packageName) {
            this.packageName = packageName;
            return builder();
        }
        
        public B addRow(int myRow) {
            this.myRow = myRow;
            return builder();
        }
        
        public B addColumn(int myColumn) {
            this.myColumn = myColumn;
            return builder();
        }
    }
}
