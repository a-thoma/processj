package clp.parsers;

import clp.OptionParser;

/**
 * This is to override the user ProcessJ library path.
 * 
 * @author Ben Cisneros
 * @version 07/14/2018
 * @since 1.2
 */
public class PathParser extends OptionParser<Object> {

    public PathParser(String optionName) {
        super(optionName);
    }

    @Override
    public Object parseValue(String value) throws Exception {
        // TODO: Talk to Dr. Pedersen
        return null;
    }
}
