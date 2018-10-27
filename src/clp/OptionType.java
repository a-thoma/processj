package clp;

/**
 * The enum {@link OptionType} defines the type of an option.
 * An option of type {@link OptionType#NONE} defines boolean options
 * with no additional data needed; type {@link OptionType#MULTIVALUE}
 * defines mutlivalue options that may take more than one value;
 * and type {@link OptionType#SINGLEVALUE} defines single value options
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
