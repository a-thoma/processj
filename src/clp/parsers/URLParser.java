package clp.parsers;

import java.net.MalformedURLException;
import java.net.URL;

import clp.OptionParser;

/**
 * @author Ben Cisneros
 * @version 07/14/2018
 * @since 1.2
 */
public class URLParser extends OptionParser<URL> {

    public URLParser(String optionName) {
        super(optionName);
    }

    @Override
    public URL parseValue(String value) throws Exception {
        try {
            return new URL(value);
        } catch (MalformedURLException e) {
            throw new RuntimeException(String.format("'%s' coult convert '%s' to "
                        + "URL.", optionName, value));
        }
    }
}
