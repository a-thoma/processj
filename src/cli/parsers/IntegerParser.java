package cli.parsers;

import cli.OptionParser;

/**
 * @author Ben
 * @version 06/21/2018
 * @since 1.2
 */
public final class IntegerParser extends OptionParser<Integer> {

    public IntegerParser(String optionName) {
        super(optionName);
    }

    @Override
    public Integer parseValue(String value) throws Exception {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new NumberFormatException(String.format("'%s' could not convert '%s' to "
                        + "Integer.", optionName, value));
        }
    }
}
