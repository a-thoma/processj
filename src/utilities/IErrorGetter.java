package utilities;

public interface IErrorGetter {
    
    default ErrorType getErrorType() {
        return ErrorType.INFO;
    }
    
    default int getNumber() {
        return -1;
    }
    
    String getText();
    
    String getMessage();
}
