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
 * file, or for processing the command line options and arguments.
 * 
 * @author Ben Cisneros
 * @version 10/07/2018
 * @since 1.2
 */
public abstract class BaseErrorMessage {
    
    protected AST ast;
    protected IErrorGetter errorMessage;
    protected Object[] arguments;
    protected Throwable throwable;
    protected String fileName = "";
    protected String packageName = "";
    
    public BaseErrorMessage(Object... arguments) {
        this(null, null, null, arguments);
    }
    
    public BaseErrorMessage(AST ast, Object... arguments) {
        this(ast, null, null, arguments);
    }
    
    public BaseErrorMessage(AST ast, IErrorGetter errorMessage) {
        this(ast, errorMessage, null, new Object[0]);
    }
    
    public BaseErrorMessage(AST ast, IErrorGetter errorMessage, Object... arguments) {
        this(ast, errorMessage, null, arguments);
    }
    
    public BaseErrorMessage(AST ast, IErrorGetter errorMessage, Throwable throwable, Object... arguments) {
        this.ast = ast;
        this.errorMessage = errorMessage;
        this.throwable = throwable;
        this.arguments = arguments;
    }
    
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
    
    public void setPackageName(String name) {
        // First strip the `.pj' part
        String str = name.replaceAll("\\.pj$", "");
        // Now remove the absolute path
        String absPath = new File("").getAbsolutePath() + "/";
        str = str.replaceAll(absPath, "");
        // Replace all `/' with .
        str = str.replaceAll("/", "\\.");
        packageName = str;
    }
    
    public void setFileName(String name) {
        // Remove all double `//:'
        String str = name.replaceAll("//","/");
        // Now remove the absolute path
        String absPath = new File("").getAbsolutePath() + "/";
        str = str.replaceAll(absPath,"");
        fileName = str;
    }
    
    public ST getMessage() {
        int argCount = 0;
        ST message = new ST(errorMessage.getMessage());
        if (arguments != null && arguments.length > 0)
            argCount = arguments.length;
        String str = "arg";
        for (int i = 0; i < argCount; ++i)
            message.add(str + i, arguments[i]);
//        if (throwable != null) {
//            message.add("exception", throwable.getCause() + "");
//            message.add("stackTrace", throwable.getStackTrace());
//        }
        return message;
    }
    
    public abstract String renderMessage();
    
    @Override
    public String toString() {
        return getClass().getSimpleName() +
                "(filename=" + (fileName.isEmpty() ? "none" : fileName) +
                ", package=" + (packageName.isEmpty() ? "none" : packageName) +
                ", errorNumber=" + errorMessage.getNumber() +
                ", errorMessage=" + errorMessage.getMessage() +
                ", arguments=" + (arguments != null ? "{" +
                                 Arrays.stream(arguments)
                                       .map(arg -> arg + "")
                                       .collect(Collectors.joining(",")) + "}"
                                 : "none") +
                ", reason=" + (throwable != null ?
                              throwable.getMessage()
                              : "none") +
                ")";
    }
}
