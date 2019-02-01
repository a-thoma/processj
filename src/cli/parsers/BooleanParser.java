package cli.parsers;

import cli.OptionParser;

/**
 * @author Ben
 * @version 06/21/2018
 * @since 1.2
 */
public final class BooleanParser extends OptionParser<Boolean> {

    public BooleanParser(String optionName) {
        super(optionName);
    }

    @Override
    public Boolean parseValue(String value) throws Exception {
        if ("true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value) || "1".equals(value)
                || "t".equalsIgnoreCase(value) || "y".equalsIgnoreCase(value) || "si".equalsIgnoreCase(value)) {
            return Boolean.TRUE;
        } else if ("false".equalsIgnoreCase(value) || "no".equalsIgnoreCase(value) || "0".equals(value)
                || "f".equalsIgnoreCase(value) || "n".equalsIgnoreCase(value)) {
            return Boolean.FALSE;
        } else {
            throw new IllegalArgumentException(String.format("'%s' coult convert '%s' to "
                            + "Boolean.", optionName, value));
        }
    }
}
