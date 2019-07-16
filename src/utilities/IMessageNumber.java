package utilities;

/**
 * The interface IMessageNumber declares and defines methods
 * that when building useful compiler messages would enforce
 * enums to provide an implementation for getMessage(), and
 * for getErrorSeverity() or getNumber() and getMessageType()
 * if needed.
 * 
 * @author Ben
 * @version 10/21/2018
 * @since 1.2
 */
public interface IMessageNumber {
    
    default ErrorSeverity getErrorSeverity() {
        return ErrorSeverity.INFO;
    }
    
    default int getNumber() {
        return -1;
    }
    
    String getMessage();
    
    MessageType getMessageType();
}
