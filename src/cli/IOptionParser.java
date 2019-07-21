package cli;

/**
 * The interface IOptionParser provides a simple behavior
 * for parsing strings as type 'T'.
 *
 * @param <T>
 *          The type which a string should be parsed to.
 *
 * @author Ben
 * @version 06/21/2018
 * @since 1.2
 */
public interface IOptionParser<T> {

    /**
     * Converts a value to the appropriate type 'T'.
     *
     * @param value
     *          The string to be parsed.
     * @return A value of type 'T'.
     */
    default T parseValue(String value) throws Exception {
        throw new RuntimeException(String.format("Cannot parse a value with \"%s\".",
                    Util.getTypeName(getClass())));
    }
}
