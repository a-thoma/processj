package utilities;

/**
 * The enum {@link ErrorSeverity} represents various types of
 * `severity' error messages in ProcessJ.
 * 
 * @author Ben
 * @version 09/02/2018
 * @since 1.2
 */
public enum ErrorSeverity {
    
    INFO        ("INFO"),
    WARNING     ("WARNING"),
    ERROR       ("ERROR")
    ;
    
    private final String text;
    
    ErrorSeverity(String text) {
        this.text = text;
    }
    
    @Override
    public String toString() {
        return text;
    }
}
