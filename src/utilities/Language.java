package utilities;

/**
 * The enum {@link Language} defines various types of programming
 * languages available in ProcessJ.
 * 
 * @author Ben Cisneros
 * @version 08/30/2018
 * @since 1.2
 */
public enum Language {
    
    JVM     ("JVM"),
    C       ("C"),
    CPLUS   ("C++"),
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