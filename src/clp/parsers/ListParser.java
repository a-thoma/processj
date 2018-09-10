package clp.parsers;

import java.util.Arrays;
import java.util.List;

import clp.OptionParser;

/**
 * Default parser for multiple string values.
 * 
 * @author Ben Cisneros
 * @version 07/11/2018
 * @since 1.2
 */
public class ListParser extends OptionParser<List<String>> {
    
    private static String COMMA_SEPARATOR = ",";

    public ListParser(String optionName) {
        super(optionName);
    }

    @Override
    public List<String> parseValue(String value) throws Exception {
        // At least 2 or more
        if (!value.matches(".*?,.+")) {
            throw new IllegalArgumentException(String.format("'%s' could not split '%s' into "
                        + "a List.", optionName, value));
        }
        return Arrays.asList(value.split(COMMA_SEPARATOR));
    }
}
