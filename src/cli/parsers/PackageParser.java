package cli.parsers;

import cli.OptionParser;

/**
 * This is for importing packages from command line in ProcessJ.
 * 
 * @author Ben
 * @version 07/14/2018
 * @since 1.2
 */
public class PackageParser extends OptionParser<Object> {

    public PackageParser(String optionName) {
        super(optionName);
    }

    @Override
    public Object parseValue(String value) throws Exception {
        // TODO:
        return null;
    }
}
