package cli;

import java.lang.annotation.*;

/**
 * The annotation Parameters is used to specify default settings
 * for commands. This Parameters can be place only on types that
 * can be either classes, an interfaces, or an enums.
 *
 * @author Ben
 * @version 06/14/2018
 * @since 1.2
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Parameters {

    /**
     * The name of this command.
     */
    String name() default "";

    /**
     * Specify that this command should or should not be included in
     * the help information.
     */
    boolean hidden() default false;

    /**
     * The descriptive text messaged used in help information.
     */
    String help() default "";

    /**
     * Optional summary description of this command.
     */
    String[] header() default {};

    /**
     * Additional description of this command.
     */
    String[] notes() default {};

    /**
     * Optional text after the list of options.
     */
    String[] footer() default {};

    /**
     * Version information of this command.
     */
    String[] version() default {};

    /**
     * A custom version information that gets instantiated when
     * provided. If none is provided then the default specified
     * version information in ProcessJ is used.
     */
    Class<? extends IVersionPrinter> versionPrinter() default IVersionPrinter.class;
}
