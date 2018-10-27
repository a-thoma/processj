package clp.parsers;

import clp.OptionParser;

/**
 * @author Ben
 * @version 06/21/2018
 * @since 1.2
 */
public class DoubleParser extends OptionParser<Double> {

    public DoubleParser(String optionName) {
        super(optionName);
    }

    @Override
    public Double parseValue(String value) throws Exception {
        try {
            return new Double(value);
        } catch (NumberFormatException e) {
            throw new NumberFormatException(String.format("'%s' could not convert '%s' to "
                        + "Double.", optionName, value));
        }
    }
}
