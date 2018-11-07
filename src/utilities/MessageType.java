package utilities;

/**
 * The enum {@link MessageType} allows the ProcessJ
 * compiler to register messages at compile-time or
 * run-time that can be (1) displayed on the screen,
 * (2) not displayed on the screen, or (3) displayed
 * before terminating the execution of the program.
 * A message of type {@code PRINT_CONTINUE} instructs
 * the compiler to display a message and resume program
 * execution; type {@code PRINT_STOP} instructs the
 * compiler to display a message and terminate the
 * execution of the program; and type {@code DONT_PRINT_CONTINUE}
 * instructs the compiler to resume program execution
 * at the point where the program last stopped.
 * 
 * @author Ben
 * @version 11/05/2018
 * @since 1.2
 */
public enum MessageType {
    
    PRINT_CONTINUE,
    PRINT_STOP,
    DONT_PRINT_CONTINUE
    ;
}
