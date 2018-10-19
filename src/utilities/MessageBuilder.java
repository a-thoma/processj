package utilities;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.stringtemplate.v4.ST;

import ast.AST;

/**
 * The class {@link MessageBuilder} is used to track down the
 * visitor pattern when processing the contents in a ProcessJ
 * file, or for processing the syntax and/or semantics errors
 * when compiling or generating Java source code from a ProcessJ
 * file.
 * 
 * @author Ben Cisneros
 * @version 10/07/2018
 * @since 1.2
 */
public class MessageBuilder {
    
    private AST ast;
    private IErrorGetter errorMessage;
    private Object[] arguments;
    private Throwable throwable;
    private String fileName = "";
    private String packageName = "";
    
    public MessageBuilder(Object... arguments) {
        this(null, null, null, arguments);
    }
    
    public MessageBuilder(AST ast, Object... arguments) {
        this(ast, null, null, arguments);
    }
    
    public MessageBuilder(AST ast, IErrorGetter errorMessage) {
        this(ast, errorMessage, null, new Object[0]);
    }
    
    public MessageBuilder(AST ast, IErrorGetter errorMessage, Object... arguments) {
        this(ast, errorMessage, null, arguments);
    }
    
    public MessageBuilder(AST ast, IErrorGetter errorMessage, Throwable throwable, Object... arguments) {
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
        if (throwable != null) {
            message.add("exception", throwable.getCause() + "");
            message.add("stackTrace", throwable.getStackTrace());
        }
        return message;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() +
                "(filename=" + (fileName.isEmpty() ? "none" : fileName) +
                ", package=" + (packageName.isEmpty() ? "none" : packageName) +
                ", errorNumber=" + errorMessage.getErrorNumber() +
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
    
    public static void main(String[] args) {
        MessageBuilder messageBuilder = new MessageBuilder(null, VisitorErrorMessage.RESOLVE_IMPORTS_105,
                new NullPointerException(), (Object[])new String[] {"blah1", "blah2"});
//        System.out.println(new NullPointerException());
        System.out.println("!!!! " + messageBuilder.getMessage().render());
        System.out.println(messageBuilder + "");
    }
}
