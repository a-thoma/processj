package cli;

/**
 * The interface {@link IOptionParser} provides a simple behavior
 * for parsing strings as type {@code T}.
 *
 * @param <T>
 *          The type which a {@code String} should be parsed to.
 *
 * @author Ben
 * @version 06/21/2018
 * @since 1.2
 */
public interface IOptionParser<T> {

    /**
     * Converts a {@code value} to the appropriate type {@code T}.
     *
     * @param value
     *          The {@code String} to be parsed.
     * @return A value of type {@code T}.
     */
    default T parseValue(String value) throws Exception {
        throw new RuntimeException(String.format("Cannot parse a value with \"%s\".",
                    Util.getTypeName(getClass())));
    }
}
