package clp.parsers;

import clp.OptionParser;

/**
 * @author Ben Cisneros
 * @version 06/21/2018
 * @since 1.2
 */
public final class StringParser extends OptionParser<String> {

    public StringParser(String optionName) {
        super(optionName);
    }

    @Override
    public String parseValue(String value) {
        return value;
    }
}
