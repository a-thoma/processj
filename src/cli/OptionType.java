package cli;

/**
 * The enum {@link OptionType} defines the type of an option.
 * An option of type {@code NONE} defines boolean options
 * with no additional data needed; type {@code MULTIVALUE}
 * defines mutlivalue options that may take more than one value;
 * and type {@code SINGLEVALUE} defines single value options
 * that take only one value.
 * 
 * @author Ben
 * @version 08/14/2018
 * @since 1.2
 */
public enum OptionType {
    
    NONE,
    
    MULTIVALUE,
    
    SINGLEVALUE
}
