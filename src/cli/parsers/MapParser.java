package cli.parsers;

import cli.IOptionParser;

/**
 * Default parser for keys and values.
 * 
 * @author Ben
 * @version 08/20/2018
 * @since 1.2
 */
public class MapParser implements IOptionParser<String[]> {
    
    private static String EQUALS_SEPARATOR = "=";

    @Override
    public String[] parseValue(String value) throws Exception {
        int indexOfSeparator = value.indexOf(EQUALS_SEPARATOR);
        if (indexOfSeparator == -1 || indexOfSeparator == 0) {
            throw new IllegalArgumentException(String.format("'%s' could not split '%s' into "
                            + "key=value.", value));
        }
        
        return value.split(EQUALS_SEPARATOR);
    }
    
    public static final MapParser PARSER = new MapParser();
}
