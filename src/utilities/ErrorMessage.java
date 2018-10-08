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
    // NAME CHECKER (TOP LEVEL) - 2100
    
    NAME_CHECKER_2100(2100, ErrorType.ERROR),
    NAME_CHECKER_2101(2101, ErrorType.ERROR),
    NAME_CHECKER_2102(2102, ErrorType.ERROR),
    NAME_CHECKER_2103(2103, ErrorType.ERROR),
    NAME_CHECKER_2104(2104, ErrorType.ERROR),
    NAME_CHECKER_2105(2105, ErrorType.ERROR),
    NAME_CHECKER_2106(2106, ErrorType.ERROR),
    NAME_CHECKER_2107(2107, ErrorType.ERROR),
    NAME_CHECKER_2108(2108, ErrorType.ERROR),
    NAME_CHECKER_2109(2109, ErrorType.ERROR),
    NAME_CHECKER_2110(2110, ErrorType.ERROR),
    NAME_CHECKER_2111(2111, ErrorType.ERROR),
    
    // -----------------------------------------------------------------------------
    // NAME TYPE RESOLUTION - 2150
    
    NAME_TYPE_RESOLUTION_2150(2150, ErrorType.ERROR),
    
    // -----------------------------------------------------------------------------
    // TYPE CHECKING - 3000
    
    TYPE_RESOLUTION_3001(3001, ErrorType.ERROR),
    TYPE_RESOLUTION_3002(3002, ErrorType.ERROR),
    TYPE_RESOLUTION_3003(3003, ErrorType.ERROR),
    TYPE_RESOLUTION_3004(3004, ErrorType.ERROR),
    TYPE_RESOLUTION_3005(3005, ErrorType.ERROR),
    TYPE_RESOLUTION_3006(3006, ErrorType.ERROR),
    TYPE_RESOLUTION_3007(3007, ErrorType.ERROR),
    TYPE_RESOLUTION_3008(3008, ErrorType.ERROR),
    TYPE_RESOLUTION_3009(3009, ErrorType.ERROR),
    TYPE_RESOLUTION_3010(3010, ErrorType.ERROR),
    TYPE_RESOLUTION_3011(3011, ErrorType.ERROR),
    TYPE_RESOLUTION_3012(3012, ErrorType.ERROR),
    TYPE_RESOLUTION_3013(3013, ErrorType.ERROR),
    TYPE_RESOLUTION_3014(3014, ErrorType.ERROR),
    TYPE_RESOLUTION_3015(3015, ErrorType.ERROR),
    TYPE_RESOLUTION_3016(3016, ErrorType.ERROR),
    TYPE_RESOLUTION_3017(3017, ErrorType.ERROR),
    TYPE_RESOLUTION_3018(3018, ErrorType.ERROR),
    TYPE_RESOLUTION_3019(3019, ErrorType.ERROR),
    TYPE_RESOLUTION_3020(3020, ErrorType.ERROR),
    TYPE_RESOLUTION_3021(3021, ErrorType.ERROR),
    TYPE_RESOLUTION_3022(3022, ErrorType.ERROR),
    TYPE_RESOLUTION_3023(3023, ErrorType.ERROR),
    TYPE_RESOLUTION_3024(3024, ErrorType.ERROR),
    TYPE_RESOLUTION_3025(3025, ErrorType.ERROR),
    TYPE_RESOLUTION_3026(3026, ErrorType.ERROR),
    TYPE_RESOLUTION_3027(3027, ErrorType.ERROR),
    TYPE_RESOLUTION_3028(3028, ErrorType.ERROR),
    TYPE_RESOLUTION_3029(3029, ErrorType.ERROR),
    TYPE_RESOLUTION_3030(3030, ErrorType.ERROR),
    TYPE_RESOLUTION_3031(3031, ErrorType.ERROR),
    TYPE_RESOLUTION_3032(3032, ErrorType.ERROR),
    TYPE_RESOLUTION_3033(3033, ErrorType.ERROR),
    TYPE_RESOLUTION_3034(3034, ErrorType.ERROR),
    TYPE_RESOLUTION_3036(3036, ErrorType.ERROR),
    TYPE_RESOLUTION_3037(3037, ErrorType.ERROR),
    TYPE_RESOLUTION_3038(3038, ErrorType.ERROR),
    TYPE_RESOLUTION_3039(3039, ErrorType.ERROR),
    TYPE_RESOLUTION_3040(3040, ErrorType.ERROR),
    TYPE_RESOLUTION_3041(3041, ErrorType.ERROR),
    TYPE_RESOLUTION_3042(3042, ErrorType.ERROR),
    TYPE_RESOLUTION_3043(3043, ErrorType.ERROR),
    TYPE_RESOLUTION_3044(3044, ErrorType.ERROR),
    TYPE_RESOLUTION_3045(3045, ErrorType.ERROR),
    TYPE_RESOLUTION_3046(3046, ErrorType.ERROR),
    TYPE_RESOLUTION_3047(3047, ErrorType.ERROR),
    TYPE_RESOLUTION_3048(3048, ErrorType.ERROR),
    TYPE_RESOLUTION_3060(3060, ErrorType.ERROR),
    TYPE_RESOLUTION_3061(3061, ErrorType.ERROR),
    TYPE_RESOLUTION_3062(3062, ErrorType.ERROR),
    TYPE_RESOLUTION_3063(3063, ErrorType.ERROR),
    TYPE_RESOLUTION_3064(3064, ErrorType.ERROR),
    TYPE_RESOLUTION_3065(3065, ErrorType.ERROR),
    TYPE_RESOLUTION_3066(3066, ErrorType.ERROR),
    TYPE_RESOLUTION_3067(3067, ErrorType.ERROR),
    TYPE_RESOLUTION_3069(3069, ErrorType.ERROR),
    TYPE_RESOLUTION_3070(3070, ErrorType.ERROR),
    TYPE_RESOLUTION_3071(3071, ErrorType.ERROR),
    TYPE_RESOLUTION_3072(3072, ErrorType.ERROR),
    TYPE_RESOLUTION_3073(3073, ErrorType.ERROR),
    
    // -----------------------------------------------------------------------------
    // NAME RESOLUTION - 2202
    
    NAME_RESOLUTION_2202(2202, ErrorType.ERROR),
    NAME_RESOLUTION_2203(2203, ErrorType.ERROR),
    NAME_RESOLUTION_2206(2206, ErrorType.ERROR),
    NAME_RESOLUTION_2207(2207, ErrorType.ERROR),
    NAME_RESOLUTION_2208(2208, ErrorType.ERROR),
    NAME_RESOLUTION_2210(2210, ErrorType.ERROR),
    NAME_RESOLUTION_2211(2211, ErrorType.ERROR),
    
    // -----------------------------------------------------------------------------
    // REACHABILITY - 5000
    
    REACHABILITY_5000(5000, ErrorType.ERROR),
    REACHABILITY_5001(5001, ErrorType.ERROR),
    REACHABILITY_5002(5002, ErrorType.ERROR),
    REACHABILITY_5003(5003, ErrorType.ERROR),
    REACHABILITY_5004(5004, ErrorType.ERROR),
    REACHABILITY_5005(5005, ErrorType.ERROR),
    REACHABILITY_5006(5006, ErrorType.ERROR),
    REACHABILITY_5008(5008, ErrorType.ERROR),
    REACHABILITY_5009(5009, ErrorType.ERROR),
    REACHABILITY_5011(5011, ErrorType.ERROR),
    REACHABILITY_5012(5012, ErrorType.ERROR),
    REACHABILITY_5013(5013, ErrorType.ERROR),
    REACHABILITY_5014(5014, ErrorType.ERROR),
    
    // -----------------------------------------------------------------------------
    // PARALLEL USAGE CHECKING - 5100
    
    PARALLEL_USAGE_CHECKING_5100(5100, ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_5101(5101, ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_5102(5102, ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_5103(5103, ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_5104(5104, ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_5105(5105, ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_5106(5106, ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_5107(5107, ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_5108(5108, ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_5109(5019, ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_5110(5110, ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_5111(5111, ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_5112(5112, ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_5113(5113, ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_5114(5114, ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_5115(5115, ErrorType.ERROR);
    
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
        Properties property = new Properties();
        try {
            FileInputStream propsFile = new FileInputStream("src/files/Messages.properties");
            property.load(propsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return MessageFormat.format(property.getProperty(name()), objects);
    }
}
