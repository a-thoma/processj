package utilities;

import java.util.Collection;

/**
 * Utility class.
 * 
 * @author Ben
 */
public final class Util {
    
    /**
     * Returns 'true' if the parameter is null, or if the runtime class
     * of the parameter does not represent an array type, or if the length
     * of the array (after casting the parameter) is zero, or if all elements
     * in the array are null.
     *
     * @throws IllegalArgumentException
     *             When the class type of parameter is not an array class.
     * @param obj
     *            The array to be checked for nullability or emptiness.
     * @return true if the parameter parameter represents an empty
     *         array, or false otherwise.
     */
    public static boolean isArrayEmpty(Object obj) {
        if (obj == null)
            return true;
        else if (!obj.getClass().isArray())
            throw new IllegalArgumentException("Invalid object '" + obj.getClass().getSimpleName() + "' type found.");
        else {
            Object[] objects = (Object[]) obj;
            if (objects.length == 0)
                return true;
            else {
                for (Object o : objects) {
                    if (o == null)
                        return true;
                }
            }
        }
        return false;
    }
    
    public static String join(Collection<String> src, String delimiter) {
        StringBuilder sb = new StringBuilder();
        boolean delim = true;

        for (String str : src) {
            if (delim)
                delim = false;
            else
                sb.append(delimiter);
            sb.append(str);
        }

        return sb.toString();
    }
}
