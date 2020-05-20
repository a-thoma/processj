package utilities;

/**
 * This enum defines various types of programming languages
 * available in the ProcessJ compiler.
 * 
 * @author Ben
 * @version 08/30/2018
 * @since 1.2
 */
public enum Language {
    
    JVM     ("JVM"),
    CPLUS   ("C++"), // could we maybe change this to CPP or CPLUSPLUS or something better?
    JS      ("JS")
    ;
    
    private final String language;
    
    Language(String language) {
        this.language = language;
    }
    
    public String getLanguage() {
        return language;
    }
    
    @Override
    public String toString() {
        return language;
    }
}
