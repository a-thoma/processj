package utilities;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.stringtemplate.v4.ST;

import ast.AST;

/**
 * The class {@link BaseErrorMessage} is used to track down the
 * visitor pattern when processing the contents in a ProcessJ
 * file, for processing the syntax and/or semantics errors
 * when compiling or generating Java source code from a ProcessJ
 * file, or when processing the command line options and arguments.
 * 
 * @author Ben Cisneros
 * @version 10/07/2018
 * @since 1.2
 */
public abstract class BaseErrorMessage {
    
    public static Builder<?> builder = null;
    private static final Object[] EMPTY_ARGUMENTS = new Object[0];
    private static final String EMPTY_STRING = "";
    
    protected final AST ast;
    protected final IErrorGetter errorMessage;
    protected final Object[] arguments;
    protected final Throwable throwable;
    protected final String fileName;
    protected final String packageName;
    
    public BaseErrorMessage(Builder<?> builder) {
        ast = builder.ast;
        errorMessage = builder.errorMessage;
        arguments = builder.arguments;
        throwable = builder.throwable;
        fileName = builder.fileName;
        packageName = builder.packageName;
    }
    
    public String createPackageName(String name) {
        // First strip the `.pj' part
        String str = name.replaceAll("\\.pj$", "");
        // Now remove the absolute path
        String absPath = new File("").getAbsolutePath() + "/";
        str = str.replaceAll(absPath, "");
        // Replace all `/' with .
        str = str.replaceAll("/", "\\.");
        return str;
    }
    
    public String createFileName(String name) {
        // Remove all double `//:'
        String str = name.replaceAll("//","/");
        // Now remove the absolute path
        String absPath = new File("").getAbsolutePath() + "/";
        str = str.replaceAll(absPath,"");
        return str;
    }
    
    // ================
    // G E T T E R S
    // ================
    
    public AST getAST() {
        return ast;
    }
    
    public IErrorGetter getErrorMessage() {
        return errorMessage;
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
    
    public ST getMessage() {
        int argCount = 0;
        ST message = null;
        if (errorMessage != null)
            message = new ST(errorMessage.getMessage());
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
                ", errorNumber="    + errorMessage.getNumber() +
                ", errorMessage="   + errorMessage.getMessage() +
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
     * @author Ben Cisneros
     * @version 10/20/2018
     * @since 1.2
     *
     * @param <B>
     *            The builder type.
     */
    public static abstract class Builder<B> {
        
        protected AST ast;
        protected IErrorGetter errorMessage;
        protected Object[] arguments;
        protected Throwable throwable;
        protected String fileName;
        protected String packageName;
        
        public Builder() {
            ast = null;
            errorMessage = null;
            arguments = EMPTY_ARGUMENTS;
            throwable = null;
            fileName = null;
            packageName = null;
        }
        
        protected abstract B builder();
        
        public abstract <E extends BaseErrorMessage> E build();
        
        public B addAST(AST ast) {
            this.ast = ast;
            return builder();
        }
        
        public B addErrorMessage(IErrorGetter errorMessage) {
            this.errorMessage = errorMessage;
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
