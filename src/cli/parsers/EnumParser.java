package cli.parsers;

import cli.OptionParser;

/**
 * @author Ben
 * @version 08/30/2018
 * @since 1.2
 *
 * @param <T>
 *          The enum type.
 */
public class EnumParser<T extends Enum<T>> extends OptionParser<T> {
    
    private Class<T> type;
    
    public EnumParser(String optName, Class<T> type) {
        super(optName);
        this.type = type;
    }

    @Override
    public T parseValue(String value) throws Exception {
        for (T enumValue : type.getEnumConstants()) {
            if (enumValue.toString().equalsIgnoreCase(value) ) {
                return enumValue;
            }
        }
        
        throw new RuntimeException(String.format("'%s' could not convert '%s' to "
                + "Enum.", optionName, value));
    }
}
