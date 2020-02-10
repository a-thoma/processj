package utilities;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import ast.AST;

/**
 * This class is used to create messages during a tree-traversal node
 * when processing the contents of a ProcessJ file, when processing
 * the syntax and/or semantics errors when compiling or generating
 * Java source code from a ProcessJ file, or when processing command
 * line options and/or arguments.
 * 
 * @author Ben
 * @version 10/07/2018
 * @since 1.2
 */
public abstract class ProcessJBugMessage {
    
    private static final Object[] EMPTY_ARGUMENTS = new Object[0];
    private static final String EMPTY_STRING = "";

    /* String template file locator */
    protected static final String ERROR_FILE = "resources/stringtemplates/messages/errorTemplate.stg";
    
    /* Template for error messages */
    protected static final STGroup stGroup = new STGroupFile(ERROR_FILE);
    
    /* Current running AST */
    protected final AST d_ast;
    
    /* Type of error message */
    protected final MessageNumber d_errorNumber;
    
    /* Attributes used in templates */
    protected final Object[] d_arguments;
    
    /* Reason for the error message */
    protected final Throwable d_throwable;
    
    /* Source of the message */
    protected final String d_fileName;
    
    /* Location of the input file */
    protected final String d_packageName;
    
    /* Line in file */
    protected int d_rowNum;
    
    /* Character that generated the error/warning */
    protected int d_colNum;
    
    public ProcessJBugMessage(Builder<?> builder) {
        d_ast = builder.ast;
        d_errorNumber = builder.error;
        d_arguments = builder.arguments;
        d_throwable = builder.throwable;
        d_fileName = builder.fileName == null ? new File(ProcessJBugManager.INSTANCE.fileName).getAbsolutePath() :
                                                new File(builder.fileName).getAbsolutePath();
        d_packageName = builder.packageName == null ? ProcessJBugManager.INSTANCE.fileName : builder.packageName;
        d_rowNum = builder.rowNum;
        d_colNum = builder.colNum;
    }
    
    // 
    // GETTERS
    // 
    
    public AST ast() {
        return d_ast;
    }
    
    public MessageNumber messageNumber() {
        return d_errorNumber;
    }
    
    public Object[] arguments() {
        return d_arguments;
    }
    
    public Throwable throwable() {
        return d_throwable;
    }
    
    public String fileName() {
        return d_fileName;
    }
    
    public String packageName() {
        return d_packageName;
    }
    
    public int rowNumber() {
        return d_rowNum;
    }
    
    public int columnNumber() {
        return d_colNum;
    }
    
    public ST st() {
        int argCount = 0;
        ST message = null;
        if (d_errorNumber != null)
            message = new ST(d_errorNumber.getMessage());
        else
            message = new ST(EMPTY_STRING);
        if (d_arguments != null && d_arguments.length > 0)
            argCount = d_arguments.length;
        for (int i = 0; i < argCount; ++i)
            message.add("arg" + i, d_arguments[i]);
        return message;
    }
    
    public abstract String renderMessage();
    
    @Override
    public String toString() {
        return getClass().getSimpleName() +
                "(filename="        + (d_fileName.isEmpty() ? "none" : d_fileName) +
                ", package="        + (d_packageName.isEmpty() ? "none" : d_packageName) +
                ", errorNumber="    + d_errorNumber.getNumber() +
                ", errorMessage="   + d_errorNumber.getMessage() +
                ", arguments="      + (d_arguments != null ? "{" +
                                      Arrays.stream(d_arguments)
                                            .map(arg -> arg + "")
                                            .collect(Collectors.joining(",")) + "}"
                                            : "none") +
                ", reason="         + (d_throwable != null ?
                                            d_throwable.getMessage()
                                            : "none") +
                ", row="            + d_rowNum +
                ", column="         + d_colNum +
                ")";
    }
    
    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime + result + d_ast.hashCode();
        result = prime + result + d_errorNumber.hashCode();
        result = prime + result + Arrays.hashCode(d_arguments);
        result = prime + result + d_throwable.hashCode();
        result = prime + result + d_fileName.hashCode();
        result = prime + result + d_packageName.hashCode();
        result = prime + result + d_rowNum;
        result = prime + result + d_colNum;
        return result;
    }
    
    @Override
    public final boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        
        ProcessJBugMessage other = (ProcessJBugMessage) obj;
        if (this.d_rowNum != other.d_rowNum || this.d_colNum != other.d_colNum)
            return false;
        if (!this.d_fileName.equals(other.d_fileName) || !this.d_packageName.equals(other.d_packageName))
            return false;
        if (this.d_ast != other.d_ast) /* This should be ok */
            return false;
        if (!this.d_errorNumber.equals(other.d_errorNumber))
            return false;
        if (!Arrays.equals(this.d_arguments, other.d_arguments))
            return false;
        if (!this.d_throwable.equals(other.d_throwable))
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
        protected MessageNumber error;
        protected Object[] arguments;
        protected Throwable throwable;
        protected String fileName;
        protected String packageName;
        protected int rowNum;
        protected int colNum;
        
        public Builder() {
            ast = null;
            error = null;
            arguments = EMPTY_ARGUMENTS;
            throwable = null;
            fileName = null;
            packageName = null;
        }
        
        protected abstract B builder();
        
        public abstract <E extends ProcessJBugMessage> E build();
        
        public B addAST(AST ast) {
            this.ast = ast;
            return builder();
        }
        
        public B addError(MessageNumber error) {
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
        
        public B addRowNumber(int rowNumber) {
            this.rowNum = rowNumber;
            return builder();
        }
        
        public B addColNumber(int colNumber) {
            this.colNum = colNumber;
            return builder();
        }
    }
}
