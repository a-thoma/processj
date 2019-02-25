package utilities;

/**
 * The interface {@link IMessageNumber} declares and defines
 * methods that when building useful compiler messages would
 * enforce {@code enum}s to provide an implementation for
 * {@link #getMessage()}, and for {@link #getErrorSeverity()}
 * or {@link #getNumber()} and {@link #getMessageType()} if
 * needed.
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
