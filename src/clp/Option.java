package clp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation {@link Option @Option} is used to specified default
 * settings for command line options. This annotation should be placed
 * only on fields.
 * <p>
 * The elements below are used to initialize and validate the elements
 * of an {@link Option @Option} and to make up the help information.
 * </p>
 *
 * <ul>
 * <li>{@link Option#names()}</li>
 * <li>{@link Option#help()}</li>
 * <li>{@link Option#defaultValue()}</li>
 * <li>{@link Option#arity()}</li>
 * <li>{@link Option#metavar()}</li>
 * <li>{@link Option#hidden()}</li>
 * <li>{@link Option#required()}</li>
 * <li>{@link Option#split()}</li>
 * <li>{@link Option#handler()}</li>
 * </ul>
 *
 * @author Ben
 * @version 06/21/2018
 * @since 1.2
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Option {

    /**
     * The name (or names) of this option.
     */
    String[] names() default {};
    
    /**
     * The descriptive text messaged used in the help information.
     */
    String help() default "";
    
    /**
     * The default value for this option as a string.
     */
    String defaultValue() default "";
    
    /**
     * Specifies the minimum (and maximum) number of command line argument
     * values an {@link Option @Option} should consume.
     */
    String arity() default "";
    
    /**
     * A name that represents the values for this argument.
     */
    String metavar() default "";
    
    /**
     * Indicates whether this option is required.
     */
    boolean required() default false;
    
    /**
     * Specify that this option should or should not be included in the help
     * information.
     */
    boolean hidden() default false;
    
    /**
     * The separator between this option and its actual value.
     */
    String split() default "";
    
    /**
     * The converter used to parse the value for this option.
     */
    @SuppressWarnings("rawtypes")
    Class<? extends OptionParser>[] handlers() default {};
}
