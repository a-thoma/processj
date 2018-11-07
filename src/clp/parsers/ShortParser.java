package clp.parsers;

import clp.OptionParser;

/**
 * @author Ben
 * @version 07/03/2018
 * @since 1.2
 */
public class ShortParser extends OptionParser<Short> {

    public ShortParser(String optionName) {
        super(optionName);
    }

    @Override
    public Short parseValue(String value) throws Exception {
        try {
            return Short.parseShort(value);
        } catch (NumberFormatException e) {
            throw new NumberFormatException(String.format("'%s' could not convert '%s' to "
                        + "Short.", optionName, value));
        }
    }
}
