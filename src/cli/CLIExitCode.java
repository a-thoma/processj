package cli;

/**
 * The enum CLIExitCode is used to determine the exit
 * status of the command line parser.
 * 
 * @author Ben
 * @version 08/14/2018
 * @since 1.2
 */
public enum CLIExitCode {
    OK                  ("OK"),
    COMPILATION_ERROR   ("Compilation Error"),
    INTERNAL_ERROR      ("Internal Error"),
    EXECUTION_ERROR     ("Execution Error")
    ;
    
    private final String code;
    
    CLIExitCode(String code) {
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
}
