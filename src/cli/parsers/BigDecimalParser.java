package cli.parsers;

import java.math.BigDecimal;

import cli.OptionParser;

/**
 * @author Ben
 * @version 07/13/2018
 * @since 1.2
 */
public class BigDecimalParser extends OptionParser<BigDecimal> {

    public BigDecimalParser(String optionName) {
        super(optionName);
    }

    @Override
    public BigDecimal parseValue(String value) throws Exception {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new NumberFormatException(String.format("'%s' could not convert '%s' to "
                        + "BigDecimal.", optionName, value));
        }
    }
}
