package clp.parsers;

import clp.OptionParser;

/**
 * @author Ben
 * @version 06/21/2018
 * @since 1.2
 */
public class ByteParser extends OptionParser<Byte> {

    public ByteParser(String optionName) {
        super(optionName);
    }

    @Override
    public Byte parseValue(String value) throws Exception {
        try {
            return Byte.parseByte(value);
        } catch (NumberFormatException e) {
            throw new NumberFormatException(String.format("'%s' could not convert '%s' to "
                        + "Byte.", optionName, value));
        }
    }
}
