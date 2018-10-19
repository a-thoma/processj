package utilities;

public interface IErrorGetter {
    
    default ErrorType getErrorType() {
        return ErrorType.INFO;
    }
    
    default int getErrorNumber() {
        return -1;
    }
    
    String getMessage();
}
