package clp;

import java.util.Collection;

/**
 * The class {@link Assert} provides a convenient way for
 * checking {@code null} objects.
 * 
 * @author Ben Cisneros
 * @version 08/06/2018
 * @since 1.2
 */
public class Assert {
    
    /**
     * Throws a {@link NullPointerException} if a given object is null.
     * 
     * @param obj
     *          The object to be cheked.
     * @return The same object.
     */
    public static <T> T noNull(T obj) {
        return nonNull(obj, "Null object.");
    }

    /**
     * Throws a {@link NullPointerException} if a given object is null.
     * 
     * @param obj
     *          The object to be checked.
     * @param message
     *          The detail message.
     * @return The same object.
     */
    public static <T> T nonNull(T obj, String message) {
        if (obj == null)
            throw new NullPointerException(message);
        return obj;
    }
    
    /**
     * Throws a {@link NullPointerException} if a given object is null.
     * 
     * @param obj
     *          The object to be checked.
     * @param args
     *          A collection of messages.
     * @return The same object.
     */
    public static <T> T nonNull(T obj, Collection<String> args) {
        return nonNull(obj, StringUtil.join(args, ", "));
    }
}
