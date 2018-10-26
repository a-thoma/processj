package utilities;

import java.io.File;
import java.net.URL;
import java.util.Properties;

/**
 * The class {@link CLPErrorNumber} defines error
 * messages generated by the command line processor.
 * 
 * @author Ben Cisneros
 * @version 10/18/2018
 */
public enum CLPErrorNumber implements IErrorGetter {
    
    // ============================================
    // A N N O T A T I O N   E R R O R (100-199)
    // ============================================
    
    ANNOTATION_ERROR_100(100, ErrorSeverity.ERROR),
    ANNOTATION_ERROR_101(101, ErrorSeverity.ERROR),
    ANNOTATION_ERROR_102(102, ErrorSeverity.ERROR),
    ANNOTATION_ERROR_103(103, ErrorSeverity.ERROR),
    ANNOTATION_ERROR_104(104, ErrorSeverity.ERROR),
    ANNOTATION_ERROR_105(105, ErrorSeverity.ERROR),
    ANNOTATION_ERROR_106(106, ErrorSeverity.ERROR),
    ANNOTATION_ERROR_107(107, ErrorSeverity.ERROR),
    
    // =====================================
    // O P T I O N   E R R O R (200-299)
    // =====================================
    
    OPTION_ERROR_200(200, ErrorSeverity.ERROR),
    OPTION_ERROR_201(201, ErrorSeverity.ERROR),
    OPTION_ERROR_202(202, ErrorSeverity.ERROR),
    OPTION_ERROR_203(203, ErrorSeverity.ERROR),
    OPTION_ERROR_204(204, ErrorSeverity.ERROR),
    OPTION_ERROR_205(205, ErrorSeverity.ERROR),
    OPTION_ERROR_206(206, ErrorSeverity.ERROR),
    
    // =========================================
    // A R G U M E N T   E R R O R (300-399)
    // =========================================
    
    ARGUMENT_ERROR_300(300, ErrorSeverity.ERROR),
    ARGUMENT_ERROR_301(301, ErrorSeverity.ERROR),
    
    // =====================================
    // C L A S S   E R R O R (400-499)
    // =====================================
    
    CLASS_ERROR_400(400, ErrorSeverity.ERROR),
    CLASS_ERROR_401(401, ErrorSeverity.ERROR),
    CLASS_ERROR_402(402, ErrorSeverity.ERROR),
    CLASS_ERROR_403(403, ErrorSeverity.ERROR)
    ;
    
    /**
     * File loader.
     */
    private static Properties localizable;
    
    /**
     * Local path.
     */
    private final static String PATH = "resources/properties/CLPErrorMessages.properties";
    
    /**
     * The error number.
     */
    private final int number;
    
    /**
     * The severity level of the error message.
     */
    private ErrorSeverity type;
    
    private CLPErrorNumber(int number, ErrorSeverity type) {
        this.number = number;
        this.type = type;
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
        URL url = PropertiesLoader.getURL(PATH);        
        String path = PATH;
        if (url != null)
            path = url.getFile();
        localizable = PropertiesLoader.loadProperties(new File(path));
    }
}
