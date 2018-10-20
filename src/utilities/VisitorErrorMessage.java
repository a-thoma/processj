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
    
    RESOLVE_IMPORTS_100("visit-imports", 100, ErrorType.ERROR),
    RESOLVE_IMPORTS_101("visit-imports", 101, ErrorType.ERROR),
    RESOLVE_IMPORTS_102("visit-imports", 102, ErrorType.ERROR),
    RESOLVE_IMPORTS_103("visit-imports", 103, ErrorType.ERROR),
    RESOLVE_IMPORTS_104("visit-imports", 104, ErrorType.ERROR),
    RESOLVE_IMPORTS_105("visit-imports", 105, ErrorType.ERROR),

    // ===========================================
    // T O P   L E V E L   D E C L S (200-299)
    // ===========================================
    
    TOP_LEVEL_DECLS_200("visit-top-decls", 200, ErrorType.ERROR),
    TOP_LEVEL_DECLS_201("visit-top-decls", 201, ErrorType.ERROR),
    TOP_LEVEL_DECLS_202("visit-top-decls", 202, ErrorType.ERROR),
    TOP_LEVEL_DECLS_203("visit-top-decls", 203, ErrorType.ERROR),
    TOP_LEVEL_DECLS_204("visit-top-decls", 204, ErrorType.ERROR),
    TOP_LEVEL_DECLS_205("visit-top-decls", 205, ErrorType.ERROR),
    TOP_LEVEL_DECLS_206("visit-top-decls", 206, ErrorType.ERROR),
    TOP_LEVEL_DECLS_207("visit-top-decls", 207, ErrorType.ERROR),

    // ==================================================
    // R E S O L V E   N A M E D   T Y P E S (300-399)
    // ==================================================
    
    // TODO: Add error message type

    // ====================================================
    // R E S O L V E   N A M E   C H E C K E R (400-499)
    // ====================================================
    
    NAME_CHECKER_400("visit-name-checker", 400, ErrorType.ERROR),
    NAME_CHECKER_401("visit-name-checker", 401, ErrorType.ERROR),
    NAME_CHECKER_402("visit-name-checker", 402, ErrorType.ERROR),
    NAME_CHECKER_403("visit-name-checker", 403, ErrorType.ERROR),
    NAME_CHECKER_404("visit-name-checker", 404, ErrorType.ERROR),
    NAME_CHECKER_405("visit-name-checker", 405, ErrorType.ERROR),
    NAME_CHECKER_406("visit-name-checker", 406, ErrorType.ERROR),
    NAME_CHECKER_407("visit-name-checker", 407, ErrorType.ERROR),

    // ========================================================
    // A R R A Y   T Y P E   C O N S T R U C T O R (500-599)
    // ========================================================
    
    ARRAY_TYPE_CONSTRUCTOR_500("visit-array", 500, ErrorType.ERROR),
    ARRAY_TYPE_CONSTRUCTOR_501("visit-array", 501, ErrorType.ERROR),
    ARRAY_TYPE_CONSTRUCTOR_502("visit-array", 502, ErrorType.ERROR),
    ARRAY_TYPE_CONSTRUCTOR_503("visit-array", 503, ErrorType.ERROR),
    ARRAY_TYPE_CONSTRUCTOR_504("visit-array", 504, ErrorType.ERROR),

    // ===========================================
    // T Y P E   R E S O L U T I O N (600-699)
    // ===========================================
    
    TYPE_RESOLUTION_600("visit-type-checker", 600, ErrorType.ERROR),
    TYPE_RESOLUTION_601("visit-type-checker", 601, ErrorType.ERROR),
    TYPE_RESOLUTION_602("visit-type-checker", 602, ErrorType.ERROR),
    TYPE_RESOLUTION_603("visit-type-checker", 603, ErrorType.ERROR),
    TYPE_RESOLUTION_604("visit-type-checker", 604, ErrorType.ERROR),
    TYPE_RESOLUTION_605("visit-type-checker", 605, ErrorType.ERROR),
    TYPE_RESOLUTION_606("visit-type-checker", 606, ErrorType.ERROR),
    TYPE_RESOLUTION_607("visit-type-checker", 607, ErrorType.ERROR),
    TYPE_RESOLUTION_608("visit-type-checker", 608, ErrorType.ERROR),
    TYPE_RESOLUTION_609("visit-type-checker", 609, ErrorType.ERROR),
    TYPE_RESOLUTION_610("visit-type-checker", 610, ErrorType.ERROR),
    TYPE_RESOLUTION_611("visit-type-checker", 611, ErrorType.ERROR),
    TYPE_RESOLUTION_612("visit-type-checker", 612, ErrorType.ERROR),
    TYPE_RESOLUTION_613("visit-type-checker", 613, ErrorType.ERROR),
    TYPE_RESOLUTION_614("visit-type-checker", 614, ErrorType.ERROR),
    TYPE_RESOLUTION_615("visit-type-checker", 615, ErrorType.ERROR),
    TYPE_RESOLUTION_616("visit-type-checker", 616, ErrorType.ERROR),
    TYPE_RESOLUTION_617("visit-type-checker", 617, ErrorType.ERROR),
    TYPE_RESOLUTION_618("visit-type-checker", 618, ErrorType.ERROR),
    TYPE_RESOLUTION_619("visit-type-checker", 619, ErrorType.ERROR),
    TYPE_RESOLUTION_620("visit-type-checker", 620, ErrorType.ERROR),
    TYPE_RESOLUTION_621("visit-type-checker", 621, ErrorType.ERROR),
    TYPE_RESOLUTION_622("visit-type-checker", 622, ErrorType.ERROR),
    TYPE_RESOLUTION_623("visit-type-checker", 623, ErrorType.ERROR),
    TYPE_RESOLUTION_624("visit-type-checker", 624, ErrorType.ERROR),
    TYPE_RESOLUTION_625("visit-type-checker", 625, ErrorType.ERROR),
    TYPE_RESOLUTION_626("visit-type-checker", 626, ErrorType.ERROR),
    TYPE_RESOLUTION_627("visit-type-checker", 627, ErrorType.ERROR),
    TYPE_RESOLUTION_628("visit-type-checker", 628, ErrorType.ERROR),
    TYPE_RESOLUTION_629("visit-type-checker", 629, ErrorType.ERROR),
    TYPE_RESOLUTION_630("visit-type-checker", 630, ErrorType.ERROR),
    TYPE_RESOLUTION_631("visit-type-checker", 631, ErrorType.ERROR),
    TYPE_RESOLUTION_632("visit-type-checker", 632, ErrorType.ERROR),
    TYPE_RESOLUTION_633("visit-type-checker", 633, ErrorType.ERROR),
    TYPE_RESOLUTION_634("visit-type-checker", 634, ErrorType.ERROR),
    TYPE_RESOLUTION_635("visit-type-checker", 635, ErrorType.ERROR),
    TYPE_RESOLUTION_636("visit-type-checker", 636, ErrorType.ERROR),
    TYPE_RESOLUTION_637("visit-type-checker", 637, ErrorType.ERROR),
    TYPE_RESOLUTION_638("visit-type-checker", 638, ErrorType.ERROR),
    TYPE_RESOLUTION_639("visit-type-checker", 639, ErrorType.ERROR),
    TYPE_RESOLUTION_640("visit-type-checker", 640, ErrorType.ERROR),
    TYPE_RESOLUTION_641("visit-type-checker", 641, ErrorType.ERROR),
    TYPE_RESOLUTION_642("visit-type-checker", 642, ErrorType.ERROR),
    TYPE_RESOLUTION_643("visit-type-checker", 643, ErrorType.ERROR),
    TYPE_RESOLUTION_644("visit-type-checker", 644, ErrorType.ERROR),
    TYPE_RESOLUTION_645("visit-type-checker", 645, ErrorType.ERROR),
    TYPE_RESOLUTION_646("visit-type-checker", 646, ErrorType.ERROR),
    TYPE_RESOLUTION_647("visit-type-checker", 647, ErrorType.ERROR),
    TYPE_RESOLUTION_648("visit-type-checker", 648, ErrorType.ERROR),
    TYPE_RESOLUTION_649("visit-type-checker", 649, ErrorType.ERROR),
    TYPE_RESOLUTION_650("visit-type-checker", 650, ErrorType.ERROR),
    TYPE_RESOLUTION_651("visit-type-checker", 651, ErrorType.ERROR),
    TYPE_RESOLUTION_652("visit-type-checker", 652, ErrorType.ERROR),
    TYPE_RESOLUTION_653("visit-type-checker", 653, ErrorType.ERROR),
    TYPE_RESOLUTION_654("visit-type-checker", 654, ErrorType.ERROR),

    // ===========================================================
    // P A R A L L E L   U S A G E   C H E C K I N G (700-799)
    // ===========================================================
    
    PARALLEL_USAGE_CHECK_700("visit-parallel-check", 700, ErrorType.ERROR),
    PARALLEL_USAGE_CHECK_701("visit-parallel-check", 701, ErrorType.ERROR),
    PARALLEL_USAGE_CHECK_702("visit-parallel-check", 702, ErrorType.ERROR),
    PARALLEL_USAGE_CHECK_703("visit-parallel-check", 703, ErrorType.ERROR),
    PARALLEL_USAGE_CHECK_704("visit-parallel-check", 704, ErrorType.ERROR),
    PARALLEL_USAGE_CHECK_705("visit-parallel-check", 705, ErrorType.ERROR),
    PARALLEL_USAGE_CHECK_706("visit-parallel-check", 706, ErrorType.ERROR),
    PARALLEL_USAGE_CHECK_707("visit-parallel-check", 707, ErrorType.ERROR),
    PARALLEL_USAGE_CHECK_708("visit-parallel-check", 708, ErrorType.ERROR),
    PARALLEL_USAGE_CHECK_709("visit-parallel-check", 709, ErrorType.ERROR),
    PARALLEL_USAGE_CHECK_710("visit-parallel-check", 710, ErrorType.ERROR),
    PARALLEL_USAGE_CHECK_711("visit-parallel-check", 711, ErrorType.ERROR),
    PARALLEL_USAGE_CHECK_712("visit-parallel-check", 712, ErrorType.ERROR),
    PARALLEL_USAGE_CHECK_713("visit-parallel-check", 713, ErrorType.ERROR),
    PARALLEL_USAGE_CHECK_714("visit-parallel-check", 714, ErrorType.ERROR),
    PARALLEL_USAGE_CHECK_715("visit-parallel-check", 715, ErrorType.ERROR)
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
    
    @Override
    public String getText() {
        return text;
    }
    
    public int getNumber() {
        return number;
    }
    
    public ErrorType getErrorType() {
        return type;
    }
    
    @Override
    public String getMessage() {
        return localizable.getProperty(name());
    }
    
    static {
        localizable = new Properties();
        try {
            FileInputStream propsFile = new FileInputStream("resources/properties/VisitorErrorMessages.properties");
            localizable.load(propsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}