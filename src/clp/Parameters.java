package clp;

import java.lang.annotation.*;

/**
 * The annotation {@link Parameters @Parameters} is used to specify
 * default settings for commands. This {@link Parameters @Parameters}
 * can be place only on types that can be either {@code class}s, an
 * {@code interface}s, or an {@code enum}s.
 *
 * <p>
 * The elements below are used to validate the elements of a
 * {@link Parameters @Parameters} and to make up the help information.
 * </p>
 *
 * <ul>
 * <li>{@link Parameters#name()}</li>
 * <li>{@link Parameters#hidden()}</li>
 * <li>{@link Parameters#header()}</li>
 * <li>{@link Parameters#help()}</li>
 * <li>{@link Parameters#notes()}</li>
 * <li>{@link Parameters#footer()}</li>
 * <li>{@link Parameters#version()}</li>
 * <li>{@link Parameters#order()}</li>
 * </ul>
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
    String[] help() default {};

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
     * A custom version information that gets instantiated when provided.
     * If none is provided then the default specified version information
     * in ProcessJ is used.
     */
    Class<? extends IVersionPrinter> versionPrinter() default IVersionPrinter.class;
}
