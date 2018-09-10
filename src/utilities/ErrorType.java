package utilities;

/**
 * The enum {@link ErrorType} represents various types of error messages
 * in ProcessJ
 * 
 * @author Ben Cisneros
 * @version 09/02/2018
 * @since 1.2
 */
public enum ErrorType {
    
    INFO    ("INFO"),
    WARNING ("WARNING"),
    ERROR   ("ERROR"),
    FATAL   ("FATAL");
    
    private final String text;
    
    ErrorType(String text) {
        this.text = text;
    }
    
    @Override
    public String toString() {
        return text;
    }
}
