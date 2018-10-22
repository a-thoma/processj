package utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * The class {@link ClpErrorMessage} defines error
 * messages generated by the command line processor.
 * 
 * @author Ben Cisneros
 * @version 10/18/2018
 */
public enum ClpErrorMessage implements IErrorGetter {
    
    // ============================================
    // A N N O T A T I O N   E R R O R (100-199)
    // ============================================
    
    ANNOTATION_ERROR_100("clp-annotation", 100, ErrorSeverity.ERROR),
    ANNOTATION_ERROR_101("clp-annotation", 101, ErrorSeverity.ERROR),
    ANNOTATION_ERROR_102("clp-annotation", 102, ErrorSeverity.ERROR),
    ANNOTATION_ERROR_103("clp-annotation", 103, ErrorSeverity.ERROR),
    ANNOTATION_ERROR_104("clp-annotation", 104, ErrorSeverity.ERROR),
    ANNOTATION_ERROR_105("clp-annotation", 105, ErrorSeverity.ERROR),
    ANNOTATION_ERROR_106("clp-annotation", 106, ErrorSeverity.ERROR),
    ANNOTATION_ERROR_107("clp-annotation", 107, ErrorSeverity.ERROR),
    
    // =====================================
    // O P T I O N   E R R O R (200-299)
    // =====================================
    
    OPTION_ERROR_200("clp-option", 200, ErrorSeverity.ERROR),
    OPTION_ERROR_201("clp-option", 201, ErrorSeverity.ERROR),
    OPTION_ERROR_202("clp-option", 202, ErrorSeverity.ERROR),
    OPTION_ERROR_203("clp-option", 203, ErrorSeverity.ERROR),
    OPTION_ERROR_204("clp-option", 204, ErrorSeverity.ERROR),
    OPTION_ERROR_205("clp-option", 205, ErrorSeverity.ERROR),
    OPTION_ERROR_206("clp-option", 206, ErrorSeverity.ERROR),
    
    // =========================================
    // A R G U M E N T   E R R O R (300-399)
    // =========================================
    
    ARGUMENT_ERROR_300("clp-argument", 300, ErrorSeverity.ERROR),
    ARGUMENT_ERROR_301("clp-argument", 301, ErrorSeverity.ERROR),
    
    // =====================================
    // C L A S S   E R R O R (400-499)
    // =====================================
    
    CLASS_ERROR_400("clp-class", 400, ErrorSeverity.ERROR),
    CLASS_ERROR_401("clp-class", 401, ErrorSeverity.ERROR),
    CLASS_ERROR_402("clp-class", 402, ErrorSeverity.ERROR),
    CLASS_ERROR_403("clp-class", 403, ErrorSeverity.ERROR)
    ;
    
    /**
     * File loader.
     */
    private static Properties localizable;
    
    /**
     * Tag name.
     */
    private final String text;
    
    /**
     * The error number.
     */
    private final int number;
    
    /**
     * The severity level of the error message.
     */
    private ErrorSeverity type;
    
    private ClpErrorMessage(String text, int number, ErrorSeverity type) {
        this.text = text;
        this.number = number;
        this.type = type;
    }
    
    @Override
    public String getText() {
        return text;
    }
    
    public int getNumber() {
        return number;
    }
    
    public ErrorSeverity getErrorSeverity() {
        return type;
    }

    @Override
    public String getMessage() {
        return localizable.getProperty(name());
    }
    
    static {
        localizable = new Properties();
        try {
            FileInputStream propsFile = new FileInputStream(
                    "/Users/Ben/Desktop/processj/resources/properties/ClpErrorMessages.properties");
            localizable.load(propsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
