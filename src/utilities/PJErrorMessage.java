package utilities;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import ast.AST;

/**
 * The class {@link PJErrorMessage} is used to create messages
 * for the visitor pattern when processing the contents in a
 * ProcessJ file, when processing the syntax and/or semantics
 * errors when compiling or generating Java source code from a
 * ProcessJ file, or when processing command line options and/or
 * arguments.
 * 
 * @author Ben
 * @version 10/07/2018
 * @since 1.2
 */
public abstract class PJErrorMessage {
    
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
    protected final IErrorGetter error;
    
    /**
     * Attributes used in templates.
     */
    protected final Object[] arguments;
    
    /**
     * Reason for the error message.
     */
    protected final Throwable throwable;
    
    /**
     * Source of the error message.
     */
    protected final String fileName;
    
    /**
     * Source of the input file.
     */
    protected final String packageName;
    
    public PJErrorMessage(Builder<?> builder) {
        ast = builder.ast;
        error = builder.error;
        arguments = builder.arguments;
        throwable = builder.throwable;
        fileName = builder.fileName;
        packageName = builder.packageName;
    }
    
    // ================
    // G E T T E R S
    // ================
    
    public AST getAST() {
        return ast;
    }
    
    public IErrorGetter getError() {
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
    
    public abstract String render();
    
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
                ")";
    }
    
    // =====================
    // B U I L D E R
    // =====================
    
    /**
     * 
     * The class {@link Builder} uses descriptive methods to create
     * error messages with default or initial values.
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
        protected IErrorGetter error;
        protected Object[] arguments;
        protected Throwable throwable;
        protected String fileName;
        protected String packageName;
        
        public Builder() {
            ast = null;
            error = null;
            arguments = EMPTY_ARGUMENTS;
            throwable = null;
            fileName = null;
            packageName = null;
        }
        
        protected abstract B builder();
        
        public abstract <E extends PJErrorMessage> E build();
        
        public B addAST(AST ast) {
            this.ast = ast;
            return builder();
        }
        
        public B addError(IErrorGetter error) {
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
    }
}
