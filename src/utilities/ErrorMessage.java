package utilities;

/**
 * The enum {@link ErrorMessage} all the error messages that the ProcessJ
 * compiler can issue.
 * 
 * @author Ben Cisneros
 * @version 09/02/2018
 * @since 1.2
 */
public enum ErrorMessage {
    
    // -----------------------------------------------------------------------------
    // NAME CHECKER (TOP LEVEL) - 2100
    
    NAME_CHECKER_2100(2100, "Type with name '<var>' already declared in this scope.", ErrorType.ERROR),
    NAME_CHECKER_2101(2101, "Non-procedure type with name '<var>' already declared in this scope.", ErrorType.ERROR),
    NAME_CHECKER_2102(2102, "Type with name '<var>' already declared in this scope.", ErrorType.ERROR),
    NAME_CHECKER_2103(2103, "Type with name '<var>' already declared in this scope.", ErrorType.ERROR),
    NAME_CHECKER_2104(2104, "File not found: '<var>'.", ErrorType.ERROR),
    NAME_CHECKER_2105(2105, "Something went wrong while trying to parser '<var>'.", ErrorType.ERROR),
    NAME_CHECKER_2106(2106, "Package '<var>' does not exist.", ErrorType.ERROR),
    NAME_CHECKER_2107(2107, "File '<var>' does not exists.", ErrorType.ERROR),
    NAME_CHECKER_2108(2108, "File '<var>' not found in package '<pkg>'.", ErrorType.ERROR),
    NAME_CHECKER_2109(2109, "Mobile procedure '<var>' must have a void return type.", ErrorType.ERROR),
    NAME_CHECKER_2110(2110, "Only one declaration of mobile procedure '<var>' may exists.", ErrorType.ERROR),
    NAME_CHECKER_2111(2111, "Type with name '<var>' already declared in this scope.", ErrorType.ERROR),
    
    // -----------------------------------------------------------------------------
    // NAME TYPE RESOLUTION - 2150
    
    NAME_TYPE_RESOLUTION_2150(2150, "Cannot resolve '<var>' as local file or library.", ErrorType.ERROR),
    
    // -----------------------------------------------------------------------------
    // TYPE CHECKING - 3000
    
    TYPE_RESOLUTION_3001(3001, "Array access index must be of integral type.", ErrorType.ERROR),
    TYPE_RESOLUTION_3002(3002, "Array literal with the keyword 'new'.", ErrorType.ERROR),
    TYPE_RESOLUTION_3003(3003, "Cannot assign value of type '<var>' to variable of type '<var>'.", ErrorType.ERROR),
    TYPE_RESOLUTION_3004(3004, "Cannot assign value of type '<var>' to variable of type '<var>'.", ErrorType.ERROR),
    TYPE_RESOLUTION_3005(3005, "Right hand side operand of operator '<var>' must be of numeric type.", ErrorType.ERROR),
    TYPE_RESOLUTION_3006(3006, "Left hand side operand of operator '<var>' must be of numeric type.", ErrorType.ERROR),
    TYPE_RESOLUTION_3007(3007, "Left hand side operand of operator '<var>' must be of integral type.", ErrorType.ERROR),
    TYPE_RESOLUTION_3008(3008, "Right hand side operand of operator '<var>' must be of integral type.", ErrorType.ERROR),
    TYPE_RESOLUTION_3009(3009, "Both right and left-hand side operands of operato '<var>' must be of boolean or integral type.", ErrorType.ERROR),
    TYPE_RESOLUTION_3010(3010, "Operator '<var>' requires operands of numeric type.", ErrorType.ERROR),
    TYPE_RESOLUTION_3011(3011, "Void type cannot be used here.", ErrorType.ERROR),
    TYPE_RESOLUTION_3012(3012, "Operator '<var>' requires operands of the same type.", ErrorType.ERROR),
    TYPE_RESOLUTION_3013(3013, "Operator '<var>' requires operands of boolean type.", ErrorType.ERROR),
    TYPE_RESOLUTION_3014(3014, "Operator '<var>' requires both operands of either integral or boolean type.", ErrorType.ERROR),
    TYPE_RESOLUTION_3015(3015, "Operator '<var>' requires of numeric type or string/boolean, string/numeric, or string/string type.", ErrorType.ERROR),
    TYPE_RESOLUTION_3016(3016, "Operator '<var>' requires left operand of integral type.", ErrorType.ERROR),
    TYPE_RESOLUTION_3017(3017, "Operator '<var>' requires right operand of integral type.", ErrorType.ERROR),
    TYPE_RESOLUTION_3018(3018, "Unknown Operator '<var>'.", ErrorType.ERROR),
    TYPE_RESOLUTION_3019(3019, "Channel end expression requires channel type.", ErrorType.ERROR),
    TYPE_RESOLUTION_3020(3020, "Unknown sharing status for channel and expression.", ErrorType.ERROR),
    TYPE_RESOLUTION_3021(3021, "Channel or Time type required in channel/time read.", ErrorType.ERROR),
    TYPE_RESOLUTION_3022(3022, "Timer read cannot habe extended rendez-vous block.", ErrorType.ERROR),
    TYPE_RESOLUTION_3023(3023, "Cannot write to a non-channel end.", ErrorType.ERROR),
    TYPE_RESOLUTION_3024(3024, "Non boolean Expression found as test in do-statement.", ErrorType.ERROR),
    TYPE_RESOLUTION_3025(3025, "Barrier type expected, found '<var>'.", ErrorType.ERROR),
    TYPE_RESOLUTION_3026(3026, "Non-boolean expression found in for-statement.", ErrorType.ERROR),
    TYPE_RESOLUTION_3027(3027, "Non-boolean expression found as test in if-statement.", ErrorType.ERROR),
    TYPE_RESOLUTION_3028(3028, "Undefined named type '<var>'.", ErrorType.ERROR),
    TYPE_RESOLUTION_3029(3029, "Unknown name expression.", ErrorType.ERROR),
    TYPE_RESOLUTION_3030(3030, "Cannot assign value '<var>' to type '<other>'.", ErrorType.ERROR),
    TYPE_RESOLUTION_3031(3031, "Array dimension must be of integral type.", ErrorType.ERROR),
    TYPE_RESOLUTION_3032(3032, "Array initializer is not compatible with '<var>'.", ErrorType.ERROR),
    TYPE_RESOLUTION_3033(3033, "Incorrect number of expression in protocol literal '<var>'.", ErrorType.ERROR),
    TYPE_RESOLUTION_3034(3034, "Cannot assign value of type '<var>' to protocol field '<other>' of type '<other>'.", ErrorType.ERROR),
    TYPE_RESOLUTION_3036(3036, "Left hand side of assignment not assignable.", ErrorType.ERROR),
    TYPE_RESOLUTION_3037(3037, "No suitable procedure found.", ErrorType.ERROR),
    TYPE_RESOLUTION_3038(3038, "Found more than once candidate - cannot chose between them!.", ErrorType.ERROR),
    TYPE_RESOLUTION_3039(3039, "Cannot assign non-array to array type '<var>'.", ErrorType.ERROR),
    TYPE_RESOLUTION_3040(3040, "Procedure return type is void; return statement cannot return a value.", ErrorType.ERROR),
    TYPE_RESOLUTION_3041(3041, "Procedure return type is '<var>' but procedure return type is void.", ErrorType.ERROR),
    TYPE_RESOLUTION_3042(3042, "Incompatible type in return statement.", ErrorType.ERROR),
    TYPE_RESOLUTION_3043(3043, "Non-mobile procedure cannot suspend.", ErrorType.ERROR),
    TYPE_RESOLUTION_3044(3044, "Switch lables must be of type int or protocol tag.", ErrorType.ERROR),
    TYPE_RESOLUTION_3045(3045, "Switch lables must be constants.", ErrorType.ERROR),
    TYPE_RESOLUTION_3046(3046, "Duplicate default lable.", ErrorType.ERROR),
    TYPE_RESOLUTION_3047(3047, "Duplicate case lable.", ErrorType.ERROR),
    TYPE_RESOLUTION_3048(3048, "Non-barrier type in sync statement.", ErrorType.ERROR),
    TYPE_RESOLUTION_3060(3060, "Protocol tag '<var>' not found in protocol '<other>'.", ErrorType.ERROR),
    TYPE_RESOLUTION_3061(3061, "Request for memeber '<var>' in something not a record or protocol type.", ErrorType.ERROR),
    TYPE_RESOLUTION_3062(3062, "Switch statement expects value of type int or protocol tag.", ErrorType.ERROR),
    TYPE_RESOLUTION_3063(3063, "Nested switch statememtns on the same protocol type is not allowed.", ErrorType.ERROR),
    TYPE_RESOLUTION_3064(3064, "Fall-through cases in protocol switch statement not allowed.", ErrorType.ERROR),
    TYPE_RESOLUTION_3065(3065, "Default case not allowed in protocol switch.", ErrorType.ERROR),
    TYPE_RESOLUTION_3066(3066, "Switch label must be of integer type.", ErrorType.ERROR),
    TYPE_RESOLUTION_3067(3067, "Switch label must be a protocol case name.", ErrorType.ERROR),
    TYPE_RESOLUTION_3069(3069, "Fall-through cases in protocol switch statement not allowed.", ErrorType.ERROR),
    TYPE_RESOLUTION_3070(3070, "Non-boolean Expression found as test in ternary expression.", ErrorType.ERROR),
    TYPE_RESOLUTION_3071(3071, "Both branches of a ternary expression must be of assignment compatible types.", ErrorType.ERROR),
    TYPE_RESOLUTION_3072(3072, "Both branches of a ternary expression must be of assignment compatible types.", ErrorType.ERROR),
    TYPE_RESOLUTION_3073(3073, "Unknown field reference '<var>' in protocol tag '<other>' in protocol '<other>'.", ErrorType.ERROR),
    
    // -----------------------------------------------------------------------------
    // NAME RESOLUTION - 2202
    
    NAME_RESOLUTION_2202(2202, "'<var>' already declared in this scope.", ErrorType.ERROR),
    NAME_RESOLUTION_2203(2203, "Symbol '<var>' not found.", ErrorType.ERROR),
    NAME_RESOLUTION_2206(2206, "'<var>' already declared in this scope.", ErrorType.ERROR),
    NAME_RESOLUTION_2207(2207, "Procedure '<var>' not found.", ErrorType.ERROR),
    NAME_RESOLUTION_2208(2208, "Cannot invoke non-procedure '<var>'.", ErrorType.ERROR),
    NAME_RESOLUTION_2210(2210, "Symbol '<var>' not found.", ErrorType.ERROR),
    NAME_RESOLUTION_2211(2211, "Symbol '<var>' not found.", ErrorType.ERROR),
    
    // -----------------------------------------------------------------------------
    // REACHABILITY - 5000
    
    REACHABILITY_5000(5000, "Else-part of if-statement unreachable.", ErrorType.ERROR),
    REACHABILITY_5001(5001, "Then-part of if-statement unreachable.", ErrorType.ERROR),
    REACHABILITY_5002(5002, "While-statement is an infinite loop.", ErrorType.ERROR),
    REACHABILITY_5003(5003, "Unreachable code following statement beginning on line '<var>'.", ErrorType.ERROR),
    REACHABILITY_5004(5004, "Body of for-statement unreachable.", ErrorType.ERROR),
    REACHABILITY_5005(5005, "For-statement is an infinite loop.", ErrorType.ERROR),
    REACHABILITY_5006(5006, "Break statement outside loop or switch construct.", ErrorType.ERROR),
    REACHABILITY_5008(5008, "Return-statement inside par-block is not legal.", ErrorType.ERROR),
    REACHABILITY_5009(5009, "Break-statement inside par-block is not legal.", ErrorType.ERROR),
    REACHABILITY_5011(5011, "Do-statement is an infinite loop.", ErrorType.ERROR),
    REACHABILITY_5012(5012, "Body of while-statement unreachable.", ErrorType.ERROR),
    REACHABILITY_5013(5013, "Continue-statement inside par-block is not legal.", ErrorType.ERROR),
    REACHABILITY_5014(5014, "Continue statement outside loop construct.", ErrorType.ERROR),
    
    // -----------------------------------------------------------------------------
    // PARALLEL USAGE CHECKING - 5100
    
    PARALLEL_USAGE_CHECKING_5100(5100, "Parallel read and write access to record memeber '<var>' illegal.", ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_5101(5101, "Parallel read and write access to array member '<var>' illegal.", ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_5102(5102, "Parallel usage checking is not fully implemented for array access.", ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_5103(5103, "Parallel write access to variable '<var>' illegal.", ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_5104(5104, "Parallel write access to record member '<var>' illegal.", ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_5105(5105, "Parallel write access to array memeber '<var>' illegal.", ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_5106(5106, "Parallel usage checking is not fully implemented for array access.", ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_5107(5107, "Parallel read and write access to variable '<var>' illegal.", ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_5108(5108, "Parallel write access to variable '<var>' illegal.", ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_5109(5019, "Parallel write access to record member '<var>' illegal.", ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_5110(5110, "Parallel write access to array memeber '<var>' illegal.", ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_5111(5111, "Parallel usage checking is not fully implemented for array access.", ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_5112(5112, "Parallel write access to variable '<var>' illegal.", ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_5113(5113, "Parallel write access to record member '<var>' illegal.", ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_5114(5114, "Parallel write access to array member '<var>' illegal.", ErrorType.ERROR),
    PARALLEL_USAGE_CHECKING_5115(5115, "Parallel usage checking is not fully implemented for array access.", ErrorType.ERROR);
    
    /**
     * The error number.
     */
    private final int number;
    
    /**
     * A brief description of the message.
     */
    private final String text;
    
    /**
     * The severity level of the error message.
     */
    private ErrorType type;
    
    private ErrorMessage(int number, String text, ErrorType type) {
        this.number = number;
        this.text = text;
        this.type = type;
    }
    
    public int getNumber() {
        return number;
    }
    
    public String getText() {
        return text;
    }
    
    public ErrorType getErrorType() {
        return type;
    }
}
