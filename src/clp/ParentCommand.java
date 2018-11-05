package clp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Useful when creating complex commands that
 * required subcommands.
 * 
 * @author Ben
 * @version 10/14/2018
 * @since 1.2
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ParentCommand {
}
