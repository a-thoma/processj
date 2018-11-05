package clp;

import java.lang.reflect.AccessibleObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The class {@link Util} contains helper methods for classes
 * within the command line processor package.
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
     * Returns {@code true} if the parameter {@code obj} is null, or
     * if the runtime class of {@code obj} does not represent an array
     * class, or if the length of the array (after casting) is zero, or
     * if all elements in the array are {@code null}.
     *
     * @throws IllegalArgumentException
     *             When the class type of {@code obj} is not an array class.
     * @param obj
     *            The array to be checked for nullability or emptiness.
     * @return {@code true} if the parameter {@code obj} represents an empty
     *         array or {@code false} otherwise.
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
     * @return The name of an {@code accessibleObject}.
     */
    public static String getAccessibleObjectTypeName(AccessibleObject accessibleObject) {
        String objectName = String.valueOf(accessibleObject);
        int dotIndex = objectName.lastIndexOf(".");
        objectName = objectName.substring(dotIndex + 1);
        return objectName;
    }

    /**
     * Returns an instance represented by a type {@link Collection} if {@code type}
     * represents some type of group of objects. Otherwise, an exception is thrown
     * if {@code type} is an unknown type.
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
     * Returns and casts an instance represented by a class {@code type}.
     * 
     * @param objects
     *          A list of objects containing an instance of type {@code type}.
     * @param type
     *          The class type used for casting an object in {@code objects}.
     * @return An instance after casting or null if the list of {@code objects} does
     *         not contain an instance of type {@code type}.
     */
    public static <T> T find(List<?> objects, Class<T> type) {
        for (Object obj : objects) {
            if (type.isInstance(obj))
                return type.cast(obj);
        }
        return null;
    }

    /**
     * Returns {@code true} if the string {@code str} represents an integer
     * or floating-point number.
     * 
     * @param str
     *            A string representation of an integer or floating-point number.
     * @return {@code true} if the string is an integer or double number or
     *         {@code false} otherwise.
     */
    public static boolean isNumber(String str) {
        return str.matches("^[0-9]+\\.?[0-9]+");
    }
    
    /**
     * Returns {@code true} if {@code lhs} is equal to {@code rhs} or {@code false}
     * otherwise.
     * 
     * @param lhs
     *          The object to be compared for equality.
     * @param rhs
     *          Another object used entirely as the comparing object. 
     * @return {@code true} if both {@code lhs} and {@code rhs} are equal or {@code false}
     *         otherwise.
     */
    public static boolean objectEquals(Object lhs, Object rhs) {
        return lhs == null && rhs == null || lhs != null && lhs.equals(rhs);
    }
    
    /**
     * Find the Levenshtein distance between two Strings (Naive approach).
     * 
     * @param src
     *          The source string.
     * @param target
     *          The destination string.
     * @return The distance between {@code src} and {@code target}.
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
                
                // Get minimum
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
