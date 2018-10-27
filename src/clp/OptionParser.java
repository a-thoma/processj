package clp;

/**
 * The class {@link OptionParser} represents different types of
 * {@link Option @Option}s and {@link Argument @Argument}s read from
 * command line. An exception is thrown when the value (or values) an
 * {@link Option @Option} field or {@link Argument @Argument} field
 * consumes cannot be parsed as type {@code T}.
 *
 * @param <T>
 *          The type which a {@code String} should be parsed to.
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
