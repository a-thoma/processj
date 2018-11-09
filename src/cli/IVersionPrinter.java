package cli;

/**
 * The interface {@link IVersionPrinter} represents a program's
 * custom version information which gets instantiated when provided.
 * 
 * <p>
 * The element below is used for retrieving a string containing
 * the version information of the program.
 * </p>
 * 
 * <ul>
 * <li>{@link #getVersionPrinter()}</li>
 * </ul>
 * 
 * @author Ben
 * @version 07/11/2018
 * @since 1.2
 */
public interface IVersionPrinter {

    /**
     * Gets the version string of the program.
     * 
     * @return A string containing version information.
     */
    default String[] getVersionPrinter() throws Exception {
        throw new RuntimeException(String.format("Cannot retrieve version printer from \"%s\".",
                    Util.getTypeName(getClass())));
    }
}
