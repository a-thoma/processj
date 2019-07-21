package cli;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation Argument is similar to an Option except that
 * an Argument has an index which represents its exact position
 * on the command line. This annotation should be placed only on
 * member fields.
 * 
 * @author Ben
 * @version 07/11/2018
 * @since 1.2
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Argument {

    /**
     * The descriptive text messaged used in the help information.
     */
    String help() default "";
    
    /**
     * The default value for this argument as a string.
     */
    String defaultValue() default "";
    
    /**
     * The position of this argument on the command line.
     * <p>
     * A field annotated with {@link Argument @Argument} must have a specific
     * order, e.g. {@code order=0, order=1, ..., order=n or order=0..n}, in
     * order to grab the exact position of the positional argument from command
     * line.
     * </p>
     */
    String order() default "";
    
    /**
     * A name that represents the values for this argument.
     */
    String metavar() default "";
    
    /**
     * Indicates whether this argument is required.
     */
    boolean required() default false;
    
    /**
     * Specify that this argument should or should not be included in the help
     * information.
     */
    boolean hidden() default true;
    
    /**
     * The separator between this argument and its actual value.
     */
    String split() default " ";
    
    /**
     * The converter used to parse the value for this argument.
     */
    @SuppressWarnings("rawtypes")
    Class<? extends OptionParser> handler() default OptionParser.class;
}
