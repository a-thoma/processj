package cli;

import java.lang.reflect.AccessibleObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class contains helper methods for classes within
 * the command line processor package.
 *
 * @author Ben
 * @version 06/21/2018
 * @since 1.2
 */
public final class Util {

    private Util() {
        // Nothing to do
    }

    /**
     * Returns 'true' if the parameter is null, or if if the runtime class
     * of parameter does not represent an array class, or if the length of
     * the array (after casting) is zero, or if all elements in the array
     * are null.
     *
     * @throws IllegalArgumentException
     *             When the class type of parameter is not an array class.
     * @param obj
     *            The array to be checked for nullability or emptiness.
     * @return true if the parameter parameter represents an empty
     *         array or false otherwise.
     */
    public static boolean isArrayEmpty(Object obj) {
        if (obj == null) {
            return true;
        } else if (!obj.getClass().isArray()) {
            throw new IllegalArgumentException(String.format("Invalid object \"%s\" type found.",
                        getTypeName(obj.getClass())));
        } else {
            Object[] objects = (Object[]) obj;
            if (objects.length == 0) {
                return true;
            } else {
                for (Object o : objects) {
                    if (o == null)
                        return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns the name of a runtime class.
     *
     * @param type
     *            A class type.
     * @return The name of a class type.
     */
    public static String getTypeName(Class<?> type) {
        String typeName = type.getName();
        int dotIndex = typeName.lastIndexOf(".");
        typeName = typeName.substring(dotIndex + 1);
        return typeName;
    }

    /**
     * Returns the name of a field, method, or constructor.
     * 
     * @param accessibleObject
     *            The field, method, or constructor.
     * @return The name of the accessible object.
     */
    public static String getAccessibleObjectTypeName(AccessibleObject accessibleObject) {
        String objectName = String.valueOf(accessibleObject);
        int dotIndex = objectName.lastIndexOf(".");
        objectName = objectName.substring(dotIndex + 1);
        return objectName;
    }

    /**
     * Returns an instance represented by a Collection type if the parameter
     * represents some kind of group of objects. Otherwise, an exception is
     * thrown if the parameter is an unknown type.
     * 
     * @throws RuntimeException
     *            If the class type is unknown.
     * @param type
     *            The class type to be checked.
     * @return An instance of a {@link Collection}.
     */
    public static Collection<Object> createCollectionInstance(Class<?> type) {
        if (List.class.isAssignableFrom(type))
            return new ArrayList<>();
        else
            throw new RuntimeException(String.format("Invalid or unknown Collection type \"%s\" "
                        + "found.", getTypeName(type)));
    }
    
    /**
     * Returns and casts an instance represented by a specified class.
     * 
     * @param objects
     *          A list of objects containing an instance of type.
     * @param type
     *          The class type used for casting an object in the given list.
     * @return An instance after casting or null if the list of objects does
     *         not contain an instance of specified type.
     */
    public static <T> T find(List<?> objects, Class<T> type) {
        for (Object obj : objects) {
            if (type.isInstance(obj))
                return type.cast(obj);
        }
        return null;
    }

    /**
     * Returns true if the string represents an integer or
     * floating-point number.
     * 
     * @param str
     *            A string representation of an integer or floating-point
     *            number.
     * @return true if the string is an integer or double number or
     *         false otherwise.
     */
    public static boolean isNumber(String str) {
        return str.matches("^[0-9]+\\.?[0-9]+");
    }
    
    /**
     * Returns true if 'lhs' is equal to 'rhs' or false otherwise.
     * 
     * @param lhs
     *          The object to be compared for equality.
     * @param rhs
     *          Another object used entirely as the comparing object. 
     * @return true if both 'lhs' and 'rhs' are equal or false otherwise.
     */
    public static boolean objectEquals(Object lhs, Object rhs) {
        return lhs == null && rhs == null || lhs != null && lhs.equals(rhs);
    }
    
    /**
     * Find the Levenshtein distance between two Strings -- naive approach.
     * 
     * @param src
     *          The source string.
     * @param target
     *          The destination string.
     * @return The distance between source and target.
     */
    public static int distance(String src, String target) {
        int dist[][];
        int n;
        int m;
        int cost = 0;
        
        n = src.length();
        m = target.length();
        
        if (n == 0)
            return m;
        if (m == 0)
            return n;
        
        dist = new int[n + 1][m + 1];
        
        for (int i = 0; i <= n; ++i)
            dist[i][0] = i;
        
        for (int j = 0; j <= m; ++j)
            dist[0][j] = j;
        
        for (int i = 1; i <= n; ++i) {
            char srcChar = src.charAt(i - 1);
            for (int j = 1; j <= m; ++j) {
                char targetChar = target.charAt(j - 1);
                
                if (srcChar == targetChar)
                    cost = 0;
                else
                    cost = 1;
                
                // Get minimum.
                int min = dist[i - 1][j] + 1;
                if (min > dist[i][j - 1] + 1)
                    min = dist[i][j - 1] + 1;
                
                if (min > dist[i - 1][j - 1] + cost)
                    min = dist[i - 1][j - 1] + cost;
                
                dist[i][j] = min;
            }
        }
        
        return dist[n][m];
    }
}
