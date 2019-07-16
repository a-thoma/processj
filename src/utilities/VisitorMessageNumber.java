package utilities;

import java.io.File;
import java.net.URL;
import java.util.Properties;

/**
 * The enum VisitorMessageNumber defines error messages generated
 * by a tree-traversal node.
 * 
 * @author Ben
 * @version 09/02/2018
 * @since 1.2
 */
public enum VisitorMessageNumber implements IMessageNumber {
    
    // ===========================================
    // R E S O L V E   I M P O R T S (100-199)
    // ===========================================
    
    RESOLVE_IMPORTS_100(100, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    RESOLVE_IMPORTS_101(101, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    RESOLVE_IMPORTS_102(102, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    RESOLVE_IMPORTS_103(103, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    RESOLVE_IMPORTS_104(104, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    RESOLVE_IMPORTS_105(105, ErrorSeverity.WARNING, MessageType.PRINT_CONTINUE),
    RESOLVE_IMPORTS_106(106, ErrorSeverity.ERROR, MessageType.PRINT_STOP),

    // ===========================================
    // T O P   L E V E L   D E C L S (200-299)
    // ===========================================
    
    TOP_LEVEL_DECLS_200(200, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    TOP_LEVEL_DECLS_201(201, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    TOP_LEVEL_DECLS_202(202, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    TOP_LEVEL_DECLS_203(203, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    TOP_LEVEL_DECLS_204(204, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TOP_LEVEL_DECLS_205(205, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    TOP_LEVEL_DECLS_206(206, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    TOP_LEVEL_DECLS_207(207, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    TOP_LEVEL_DECLS_208(208, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    
    // =========================================================
    // N A M E   T Y P E   R E S O L U T I O N (300-399)
    // =========================================================
    
    NAME_TYPE_RESOLUTION_300(300, ErrorSeverity.ERROR, MessageType.PRINT_STOP),

    // ======================================
    // N A M E   C H E C K E R (400-499)
    // ======================================
    
    NAME_CHECKER_400(400, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    NAME_CHECKER_401(401, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    NAME_CHECKER_402(402, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    NAME_CHECKER_403(403, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    NAME_CHECKER_404(404, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    NAME_CHECKER_405(405, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    NAME_CHECKER_406(406, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    NAME_CHECKER_407(407, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    NAME_CHECKER_408(408, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    NAME_CHECKER_409(409, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    NAME_CHECKER_410(410, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    NAME_CHECKER_411(411, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    NAME_CHECKER_412(412, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    NAME_CHECKER_413(413, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    NAME_CHECKER_414(414, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    NAME_CHECKER_415(415, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    NAME_CHECKER_416(416, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    NAME_CHECKER_417(417, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    NAME_CHECKER_418(418, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    NAME_CHECKER_419(419, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    NAME_CHECKER_420(420, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    NAME_CHECKER_421(421, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),

    // ====================================
    // T Y P E   C H E C K E R (600-699)
    // ====================================
    
    TYPE_CHECKER_600(600, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_601(601, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_602(602, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_603(603, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_604(604, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_605(605, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_606(606, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_607(607, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_608(608, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_609(609, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_610(610, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_611(611, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_612(612, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_613(613, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_614(614, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_615(615, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_616(616, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_617(617, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_618(618, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_619(619, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_620(620, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_621(621, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_622(622, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_623(623, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_624(624, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_625(625, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_626(626, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_627(627, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_628(628, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_629(629, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_630(630, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_631(631, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_632(632, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_633(633, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_634(634, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_635(635, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_636(636, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_637(637, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_638(638, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_639(639, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_640(640, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_641(641, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_642(642, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_643(643, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_644(644, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_645(645, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_646(646, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_647(647, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_648(648, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_649(649, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_650(650, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_651(651, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_652(652, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_653(653, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_654(654, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_655(655, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_656(656, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_657(657, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_658(658, ErrorSeverity.ERROR, MessageType.PRINT_STOP),
    TYPE_CHECKER_659(659, ErrorSeverity.ERROR, MessageType.PRINT_STOP),

    // ===========================================================
    // P A R A L L E L   U S A G E   C H E C K E R (700-799)
    // ===========================================================
    
    PARALLEL_USAGE_CHECKER_700(700, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    PARALLEL_USAGE_CHECKER_701(701, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    PARALLEL_USAGE_CHECKER_702(702, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    PARALLEL_USAGE_CHECKER_703(703, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    PARALLEL_USAGE_CHECKER_704(704, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    PARALLEL_USAGE_CHECKER_705(705, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    PARALLEL_USAGE_CHECKER_706(706, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    PARALLEL_USAGE_CHECKER_707(707, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    PARALLEL_USAGE_CHECKER_708(708, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    PARALLEL_USAGE_CHECKER_709(709, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    PARALLEL_USAGE_CHECKER_710(710, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    PARALLEL_USAGE_CHECKER_711(711, ErrorSeverity.WARNING, MessageType.PRINT_CONTINUE),
    PARALLEL_USAGE_CHECKER_712(712, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    PARALLEL_USAGE_CHECKER_713(713, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    PARALLEL_USAGE_CHECKER_714(714, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    PARALLEL_USAGE_CHECKER_715(715, ErrorSeverity.WARNING, MessageType.PRINT_CONTINUE),
    
    // =====================================
    // R E A C H A B I L I T Y (800-899)
    // =====================================
    
    REACHABILITY_800(800, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    REACHABILITY_801(801, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    REACHABILITY_802(802, ErrorSeverity.WARNING, MessageType.PRINT_CONTINUE),
    REACHABILITY_803(803, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    REACHABILITY_804(804, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    REACHABILITY_805(805, ErrorSeverity.WARNING, MessageType.PRINT_CONTINUE),
    REACHABILITY_806(806, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    REACHABILITY_807(807, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    REACHABILITY_808(808, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    REACHABILITY_809(809, ErrorSeverity.WARNING, MessageType.PRINT_CONTINUE),
    REACHABILITY_810(810, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    REACHABILITY_811(811, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    REACHABILITY_812(812, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    

    REWRITE_900(900, ErrorSeverity.ERROR, MessageType.PRINT_CONTINUE),
    ;

    
    /**
     * File loader.
     */
    private static Properties localizable;
    
    /**
     * Local path.
     */
    private final static String PATH = "resources/properties/VisitorMessageNumber.properties";
    
    /**
     * The error number.
     */
    private final int number;
    
    /**
     * The severity level of the error message.
     */
    private ErrorSeverity type;
    
    /**
     * Message instructed by the compiler.
     */
    private MessageType message;
    
    private VisitorMessageNumber(int number, ErrorSeverity type, MessageType message) {
        this.number = number;
        this.type = type;
        this.message = message;
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
    
    @Override
    public MessageType getMessageType() {
        return message;
    }
    
    static {
        URL url = PropertiesLoader.getURL(PATH);
        String path = PATH;
        if (url != null)
            path = url.getFile();
        localizable = PropertiesLoader.loadProperties(new File(path));
    }
}
