package utilities;

import org.stringtemplate.v4.ST;

/**
 * The class {@code ProcessJMessage} is used to track down syntax
 * or semantics errors when compiling or generating Java source
 * code from a ProcessJ file.
 * 
 * @author Ben Cisneros
 * @version 10/07/2018
 * @since 1.2
 */
public class ProcessJMessage {
    
    private ErrorMessage errorMessage;
    private Object[] arguments;
    private Throwable throwable;
    
    public ProcessJMessage(ErrorMessage errorMessage, Throwable throwable, Object... arguments) {
        this.errorMessage = errorMessage;
        this.throwable = throwable;
        this.arguments = arguments;
    }
    
    public ErrorMessage getErrorMessage() {
        return errorMessage;
    }
    
    public Object[] getArguments() {
        return arguments;
    }
    
    public Throwable getThrowable() {
        return throwable;
    }
    
    // TODO: Finish building error message
    
    @Override
    public String toString() {
        return "";
    }
}
