package clp.parsers;

import clp.OptionParser;

/**
 * @author Ben
 * @version 07/03/2018
 * @since 1.2
 */
public class LongParser extends OptionParser<Long> {

    public LongParser(String optionName) {
        super(optionName);
    }

    @Override
    public Long parseValue(String value) throws Exception {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new NumberFormatException(String.format("'%s' could not convert '%s' to "
                        + "Long.", optionName, value));
        }
    }
}
