package cli.parsers;

import java.io.File;

import cli.OptionParser;

/**
 * @author Ben
 * @version 07/14/2018
 * @since 1.2
 */
public class FileParser extends OptionParser<File> {

    public FileParser(String optionName) {
        super(optionName);
    }

    @Override
    public File parseValue(String value) throws Exception {
        File inFile = null;

        try {
            inFile = new File(value);

            if (!inFile.exists()) {
                throw new IllegalArgumentException(String.format("'%s' could not open file '%s' because it "
                            + "does not exist.", optionName, value));
            }

            if (inFile.exists() && inFile.isDirectory()) {
                throw new IllegalArgumentException(String.format("'%s' could not open file '%s' because "
                            + "it is a directory.", value));
            }
        } catch (NullPointerException e) {
            throw new NumberFormatException(String.format("'%s' could not '%s' to "
                            + "File.", optionName, value));
        }

        return inFile;
    }
}
