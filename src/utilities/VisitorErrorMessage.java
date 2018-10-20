package utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * The enum {@link VisitorErrorMessage} represents the error
 * messages produced by the visitor pattern.
 * 
 * @author Ben Cisneros
 * @version 09/02/2018
 * @since 1.2
 */
public enum VisitorErrorMessage implements IErrorGetter {
    
    // ===========================================
    // R E S O L V E   I M P O R T S (100-199)
    // ===========================================
    
    RESOLVE_IMPORTS_100("imports", 100, ErrorType.ERROR),
    RESOLVE_IMPORTS_101("imports", 101, ErrorType.ERROR),
    RESOLVE_IMPORTS_102("imports", 102, ErrorType.ERROR),
    RESOLVE_IMPORTS_103("imports", 103, ErrorType.ERROR),
    RESOLVE_IMPORTS_104("imports", 104, ErrorType.ERROR),
    RESOLVE_IMPORTS_105("imports", 105, ErrorType.ERROR),

    // ===========================================
    // T O P   L E V E L   D E C L S (200-299)
    // ===========================================
    
    TOP_LEVEL_DECLS_200("top-level-decls", 200, ErrorType.ERROR),
    TOP_LEVEL_DECLS_201("top-level-decls", 201, ErrorType.ERROR),
    TOP_LEVEL_DECLS_202("top-level-decls", 202, ErrorType.ERROR),
    TOP_LEVEL_DECLS_203("top-level-decls", 203, ErrorType.ERROR),
    TOP_LEVEL_DECLS_204("top-level-decls", 204, ErrorType.ERROR),
    TOP_LEVEL_DECLS_205("top-level-decls", 205, ErrorType.ERROR),
    TOP_LEVEL_DECLS_206("top-level-decls", 206, ErrorType.ERROR),
    TOP_LEVEL_DECLS_207("top-level-decls", 207, ErrorType.ERROR),

    // ==================================================
    // R E S O L V E   N A M E D   T Y P E S (300-399)
    // ==================================================
    
    // TODO: Add error message type

    // ====================================================
    // R E S O L V E   N A M E   C H E C K E R (400-499)
    // ====================================================
    
    NAME_CHECKER_400("name-checker", 400, ErrorType.ERROR),
    NAME_CHECKER_401("name-checker", 401, ErrorType.ERROR),
    NAME_CHECKER_402("name-checker", 402, ErrorType.ERROR),
    NAME_CHECKER_403("name-checker", 403, ErrorType.ERROR),
    NAME_CHECKER_404("name-checker", 404, ErrorType.ERROR),
    NAME_CHECKER_405("name-checker", 405, ErrorType.ERROR),
    NAME_CHECKER_406("name-checker", 406, ErrorType.ERROR),
    NAME_CHECKER_407("name-checker", 407, ErrorType.ERROR),

    // ========================================================
    // A R R A Y   T Y P E   C O N S T R U C T O R (500-599)
    // ========================================================
    
    ARRAY_TYPE_CONSTRUCTOR_500("array-type-constructor", 500, ErrorType.ERROR),
    ARRAY_TYPE_CONSTRUCTOR_501("array-type-constructor", 501, ErrorType.ERROR),
    ARRAY_TYPE_CONSTRUCTOR_502("array-type-constructor", 502, ErrorType.ERROR),
    ARRAY_TYPE_CONSTRUCTOR_503("array-type-constructor", 503, ErrorType.ERROR),
    ARRAY_TYPE_CONSTRUCTOR_504("array-type-constructor", 504, ErrorType.ERROR),

    // ===========================================
    // T Y P E   R E S O L U T I O N (600-699)
    // ===========================================
    
    TYPE_RESOLUTION_600("type-resolution", 600, ErrorType.ERROR),
    TYPE_RESOLUTION_601("type-resolution", 601, ErrorType.ERROR),
    TYPE_RESOLUTION_602("type-resolution", 602, ErrorType.ERROR),
    TYPE_RESOLUTION_603("type-resolution", 603, ErrorType.ERROR),
    TYPE_RESOLUTION_604("type-resolution", 604, ErrorType.ERROR),
    TYPE_RESOLUTION_605("type-resolution", 605, ErrorType.ERROR),
    TYPE_RESOLUTION_606("type-resolution", 606, ErrorType.ERROR),
    TYPE_RESOLUTION_607("type-resolution", 607, ErrorType.ERROR),
    TYPE_RESOLUTION_608("type-resolution", 608, ErrorType.ERROR),
    TYPE_RESOLUTION_609("type-resolution", 609, ErrorType.ERROR),
    TYPE_RESOLUTION_610("type-resolution", 610, ErrorType.ERROR),
    TYPE_RESOLUTION_611("type-resolution", 611, ErrorType.ERROR),
    TYPE_RESOLUTION_612("type-resolution", 612, ErrorType.ERROR),
    TYPE_RESOLUTION_613("type-resolution", 613, ErrorType.ERROR),
    TYPE_RESOLUTION_614("type-resolution", 614, ErrorType.ERROR),
    TYPE_RESOLUTION_615("type-resolution", 615, ErrorType.ERROR),
    TYPE_RESOLUTION_616("type-resolution", 616, ErrorType.ERROR),
    TYPE_RESOLUTION_617("type-resolution", 617, ErrorType.ERROR),
    TYPE_RESOLUTION_618("type-resolution", 618, ErrorType.ERROR),
    TYPE_RESOLUTION_619("type-resolution", 619, ErrorType.ERROR),
    TYPE_RESOLUTION_620("type-resolution", 620, ErrorType.ERROR),
    TYPE_RESOLUTION_621("type-resolution", 621, ErrorType.ERROR),
    TYPE_RESOLUTION_622("type-resolution", 622, ErrorType.ERROR),
    TYPE_RESOLUTION_623("type-resolution", 623, ErrorType.ERROR),
    TYPE_RESOLUTION_624("type-resolution", 624, ErrorType.ERROR),
    TYPE_RESOLUTION_625("type-resolution", 625, ErrorType.ERROR),
    TYPE_RESOLUTION_626("type-resolution", 626, ErrorType.ERROR),
    TYPE_RESOLUTION_627("type-resolution", 627, ErrorType.ERROR),
    TYPE_RESOLUTION_628("type-resolution", 628, ErrorType.ERROR),
    TYPE_RESOLUTION_629("type-resolution", 629, ErrorType.ERROR),
    TYPE_RESOLUTION_630("type-resolution", 630, ErrorType.ERROR),
    TYPE_RESOLUTION_631("type-resolution", 631, ErrorType.ERROR),
    TYPE_RESOLUTION_632("type-resolution", 632, ErrorType.ERROR),
    TYPE_RESOLUTION_633("type-resolution", 633, ErrorType.ERROR),
    TYPE_RESOLUTION_634("type-resolution", 634, ErrorType.ERROR),
    TYPE_RESOLUTION_635("type-resolution", 635, ErrorType.ERROR),
    TYPE_RESOLUTION_636("type-resolution", 636, ErrorType.ERROR),
    TYPE_RESOLUTION_637("type-resolution", 637, ErrorType.ERROR),
    TYPE_RESOLUTION_638("type-resolution", 638, ErrorType.ERROR),
    TYPE_RESOLUTION_639("type-resolution", 639, ErrorType.ERROR),
    TYPE_RESOLUTION_640("type-resolution", 640, ErrorType.ERROR),
    TYPE_RESOLUTION_641("type-resolution", 641, ErrorType.ERROR),
    TYPE_RESOLUTION_642("type-resolution", 642, ErrorType.ERROR),
    TYPE_RESOLUTION_643("type-resolution", 643, ErrorType.ERROR),
    TYPE_RESOLUTION_644("type-resolution", 644, ErrorType.ERROR),
    TYPE_RESOLUTION_645("type-resolution", 645, ErrorType.ERROR),
    TYPE_RESOLUTION_646("type-resolution", 646, ErrorType.ERROR),
    TYPE_RESOLUTION_647("type-resolution", 647, ErrorType.ERROR),
    TYPE_RESOLUTION_648("type-resolution", 648, ErrorType.ERROR),
    TYPE_RESOLUTION_649("type-resolution", 649, ErrorType.ERROR),
    TYPE_RESOLUTION_650("type-resolution", 650, ErrorType.ERROR),
    TYPE_RESOLUTION_651("type-resolution", 651, ErrorType.ERROR),
    TYPE_RESOLUTION_652("type-resolution", 652, ErrorType.ERROR),
    TYPE_RESOLUTION_653("type-resolution", 653, ErrorType.ERROR),
    TYPE_RESOLUTION_654("type-resolution", 654, ErrorType.ERROR),

    // ===========================================================
    // P A R A L L E L   U S A G E   C H E C K I N G (700-799)
    // ===========================================================
    
    PARALLEL_USAGE_CHECK_700("parallel-usage-check", 700, ErrorType.ERROR),
    PARALLEL_USAGE_CHECK_701("parallel-usage-check", 701, ErrorType.ERROR),
    PARALLEL_USAGE_CHECK_702("parallel-usage-check", 702, ErrorType.ERROR),
    PARALLEL_USAGE_CHECK_703("parallel-usage-check", 703, ErrorType.ERROR),
    PARALLEL_USAGE_CHECK_704("parallel-usage-check", 704, ErrorType.ERROR),
    PARALLEL_USAGE_CHECK_705("parallel-usage-check", 705, ErrorType.ERROR),
    PARALLEL_USAGE_CHECK_706("parallel-usage-check", 706, ErrorType.ERROR),
    PARALLEL_USAGE_CHECK_707("parallel-usage-check", 707, ErrorType.ERROR),
    PARALLEL_USAGE_CHECK_708("parallel-usage-check", 708, ErrorType.ERROR),
    PARALLEL_USAGE_CHECK_709("parallel-usage-check", 709, ErrorType.ERROR),
    PARALLEL_USAGE_CHECK_710("parallel-usage-check", 710, ErrorType.ERROR),
    PARALLEL_USAGE_CHECK_711("parallel-usage-check", 711, ErrorType.ERROR),
    PARALLEL_USAGE_CHECK_712("parallel-usage-check", 712, ErrorType.ERROR),
    PARALLEL_USAGE_CHECK_713("parallel-usage-check", 713, ErrorType.ERROR),
    PARALLEL_USAGE_CHECK_714("parallel-usage-check", 714, ErrorType.ERROR),
    PARALLEL_USAGE_CHECK_715("parallel-usage-check", 715, ErrorType.ERROR)
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
    private ErrorType type;
    
    private VisitorErrorMessage(String text, int number, ErrorType type) {
        this.text = text;
        this.number = number;
        this.type = type;
    }
    
    public String getText() {
        return text;
    }
    
    public int getNumber() {
        return number;
    }
    
    public ErrorType getErrorType() {
        return type;
    }
    
    public String getMessage() {
        return localizable.getProperty(name());
    }
    
    static {
        localizable = new Properties();
        try {
            FileInputStream propsFile = new FileInputStream("resources/properties/VisitorMessages.properties");
            localizable.load(propsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
