package clp.parsers;

import clp.OptionParser;

/**
 * @author Ben Cisneros
 * @version 06/21/2018
 * @since 1.2
 */
public class FloatParser extends OptionParser<Float> {

    public FloatParser(String optionName) {
        super(optionName);
    }

    @Override
    public Float parseValue(String value) throws Exception {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            throw new NumberFormatException(String.format("'%s' could not convert '%s' to "
                        + "Float.", optionName, value));
        }
    }
}
