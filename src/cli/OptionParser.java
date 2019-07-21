package cli;

/**
 * The class OptionParser represents different types of Options
 * and Arguments read from command line. An exception is thrown
 * when the value (or values) an Option field or Argument field
 * consumes cannot be parsed as type 'T'.
 *
 * @param <T>
 *          The type which a string should be parsed to.
 *
 * @author Ben
 * @version 06/21/2018
 * @since 1.2
 */
public abstract class OptionParser<T> implements IOptionParser<T> {

    protected final String optionName;

    public OptionParser(String optionName) {
        this.optionName = optionName;
    }

    public String getParam() {
        return optionName;
    }
}
