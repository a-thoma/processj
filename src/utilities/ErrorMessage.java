package utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

/**
 * The enum {@link ErrorMessage} all the error messages that
 * the ProcessJ compiler can issue.
 * 
 * @author Ben Cisneros
 * @version 09/02/2018
 * @since 1.2
 */
public enum ErrorMessage {
    
    // -----------------------------------------------------------------------------
    // RESOLVE IMPORTS (100-199)
    
    RESOLVE_IMPORTS_100(100, ErrorType.ERROR),
    RESOLVE_IMPORTS_101(101, ErrorType.ERROR),
    RESOLVE_IMPORTS_102(102, ErrorType.ERROR),
    RESOLVE_IMPORTS_103(103, ErrorType.ERROR),
    RESOLVE_IMPORTS_104(104, ErrorType.ERROR),
    RESOLVE_IMPORTS_105(105, ErrorType.ERROR),
    
    // -----------------------------------------------------------------------------
    // TOP LEVEL DECLS (200-299)
    
    TOP_LEVEL_DECLS_200(200, ErrorType.ERROR),
    TOP_LEVEL_DECLS_201(201, ErrorType.ERROR),
    TOP_LEVEL_DECLS_202(202, ErrorType.ERROR),
    TOP_LEVEL_DECLS_203(203, ErrorType.ERROR),
    TOP_LEVEL_DECLS_204(204, ErrorType.ERROR),
    TOP_LEVEL_DECLS_205(205, ErrorType.ERROR),
    TOP_LEVEL_DECLS_206(206, ErrorType.ERROR),
    TOP_LEVEL_DECLS_207(207, ErrorType.ERROR),
    
    // -----------------------------------------------------------------------------
    // RESOLVE NAMED TYPES (300-399)
    
    // TODO
    
    // -----------------------------------------------------------------------------
    // RESOLVE NAMED TYPES (300-399)
    
    NAME_CHECKER_400(400, ErrorType.ERROR),
    NAME_CHECKER_401(401, ErrorType.ERROR),
    NAME_CHECKER_402(402, ErrorType.ERROR),
    NAME_CHECKER_403(403, ErrorType.ERROR),
    NAME_CHECKER_404(404, ErrorType.ERROR),
    NAME_CHECKER_405(405, ErrorType.ERROR),
    NAME_CHECKER_406(406, ErrorType.ERROR),
    NAME_CHECKER_407(407, ErrorType.ERROR),
    
    // -----------------------------------------------------------------------------
    // ARRAY TYPE CONSTRUCTOR (500-599)
    
    ARRAY_TYPE_CONSTRUCTOR_500(500, ErrorType.ERROR),
    ARRAY_TYPE_CONSTRUCTOR_501(501, ErrorType.ERROR),
    ARRAY_TYPE_CONSTRUCTOR_502(502, ErrorType.ERROR),
    ARRAY_TYPE_CONSTRUCTOR_503(503, ErrorType.ERROR),
    ARRAY_TYPE_CONSTRUCTOR_504(504, ErrorType.ERROR),
    
    // -----------------------------------------------------------------------------
    // TYPE RESOLUTION (600-699)
    
    TYPE_RESOLUTION_600(600, ErrorType.ERROR),
    TYPE_RESOLUTION_601(601, ErrorType.ERROR),
    TYPE_RESOLUTION_602(602, ErrorType.ERROR),
    TYPE_RESOLUTION_603(603, ErrorType.ERROR),
    TYPE_RESOLUTION_604(604, ErrorType.ERROR),
    TYPE_RESOLUTION_605(605, ErrorType.ERROR),
    TYPE_RESOLUTION_606(606, ErrorType.ERROR),
    TYPE_RESOLUTION_607(607, ErrorType.ERROR),
    TYPE_RESOLUTION_608(608, ErrorType.ERROR),
    TYPE_RESOLUTION_609(609, ErrorType.ERROR),
    TYPE_RESOLUTION_610(610, ErrorType.ERROR),
    TYPE_RESOLUTION_611(611, ErrorType.ERROR),
    TYPE_RESOLUTION_612(612, ErrorType.ERROR),
    TYPE_RESOLUTION_613(613, ErrorType.ERROR),
    TYPE_RESOLUTION_614(614, ErrorType.ERROR),
    TYPE_RESOLUTION_615(615, ErrorType.ERROR),
    TYPE_RESOLUTION_616(616, ErrorType.ERROR),
    TYPE_RESOLUTION_617(617, ErrorType.ERROR),
    TYPE_RESOLUTION_618(618, ErrorType.ERROR),
    TYPE_RESOLUTION_619(619, ErrorType.ERROR),
    TYPE_RESOLUTION_620(620, ErrorType.ERROR),
    TYPE_RESOLUTION_621(621, ErrorType.ERROR),
    TYPE_RESOLUTION_622(622, ErrorType.ERROR),
    TYPE_RESOLUTION_623(623, ErrorType.ERROR),
    TYPE_RESOLUTION_624(624, ErrorType.ERROR),
    TYPE_RESOLUTION_625(625, ErrorType.ERROR),
    TYPE_RESOLUTION_626(626, ErrorType.ERROR),
    TYPE_RESOLUTION_627(627, ErrorType.ERROR),
    TYPE_RESOLUTION_628(628, ErrorType.ERROR),
    TYPE_RESOLUTION_629(629, ErrorType.ERROR),
    TYPE_RESOLUTION_630(630, ErrorType.ERROR),
    TYPE_RESOLUTION_631(631, ErrorType.ERROR),
    TYPE_RESOLUTION_632(632, ErrorType.ERROR),
    TYPE_RESOLUTION_633(633, ErrorType.ERROR),
    TYPE_RESOLUTION_634(634, ErrorType.ERROR),
    TYPE_RESOLUTION_635(635, ErrorType.ERROR),
    TYPE_RESOLUTION_636(636, ErrorType.ERROR),
    TYPE_RESOLUTION_637(637, ErrorType.ERROR),
    TYPE_RESOLUTION_638(638, ErrorType.ERROR),
    TYPE_RESOLUTION_639(639, ErrorType.ERROR),
    TYPE_RESOLUTION_640(640, ErrorType.ERROR),
    TYPE_RESOLUTION_641(641, ErrorType.ERROR),
    TYPE_RESOLUTION_642(642, ErrorType.ERROR),
    TYPE_RESOLUTION_643(643, ErrorType.ERROR),
    TYPE_RESOLUTION_644(644, ErrorType.ERROR),
    TYPE_RESOLUTION_645(645, ErrorType.ERROR),
    TYPE_RESOLUTION_646(646, ErrorType.ERROR),
    TYPE_RESOLUTION_647(647, ErrorType.ERROR),
    TYPE_RESOLUTION_648(648, ErrorType.ERROR),
    TYPE_RESOLUTION_649(649, ErrorType.ERROR),
    TYPE_RESOLUTION_650(650, ErrorType.ERROR),
    TYPE_RESOLUTION_651(651, ErrorType.ERROR),
    TYPE_RESOLUTION_652(652, ErrorType.ERROR),
    TYPE_RESOLUTION_653(653, ErrorType.ERROR),
    TYPE_RESOLUTION_654(654, ErrorType.ERROR),
    
    // -----------------------------------------------------------------------------
    // PARALLEL USAGE CHECKING (700-799)
    
    PARALLEL_USAGE_CHECKING_700(700, ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_701(701, ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_702(702, ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_703(703, ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_704(704, ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_705(705, ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_706(706, ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_707(707, ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_708(708, ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_709(709, ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_710(710, ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_711(711, ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_712(712, ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_713(713, ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_714(714, ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_715(715, ErrorType.ERROR)
    ;
    
    /**
     * The error number.
     */
    private final int number;
    
    /**
     * The severity level of the error message.
     */
    private ErrorType type;
    
    private ErrorMessage(int number, ErrorType type) {
        this.number = number;
        this.type = type;
    }
    
    public int getNumber() {
        return number;
    }
    
    public ErrorType getErrorType() {
        return type;
    }
    
    public String format(Object... objects) {
        // TODO: Make propsFile static
        Properties property = new Properties();
        try {
            FileInputStream propsFile = new FileInputStream("resources/properties/Messages.properties");
            property.load(propsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return MessageFormat.format(property.getProperty(name()), objects);
    }
}
