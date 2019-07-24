package utilities;

/**
 * This interface declares and defines methods that when building
 * useful compiler messages would enforce enum's to provide an
 * implementation for getMessage(), and for getErrorSeverity()
 * or getNumber() and getMessageType() if needed.
 * 
 * @author Ben
 * @version 10/21/2018
 * @since 1.2
 */
public interface MessageNumber {
    
    default ErrorSeverity getErrorSeverity() {
        return ErrorSeverity.INFO;
    }
    
    default int getNumber() {
        return -1;
    }
    
    String getMessage();
    
    MessageType getMessageType();
}
