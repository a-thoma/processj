package clp;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * The class {@link StringUtil} contains helper methods for
 * classes within the {@code cli} package.
 *
 * @author Ben
 * @version 06/21/2018
 * @since 1.2
 */
public final class StringUtil {

    private StringUtil() {
        // Nothing to do
    }

    /**
     * Returns {@code true} if the parameter {@code str} is null or if 
     * it represents an empty string.
     *
     * @param str
     *            The string to be checked for nullability or emptiness.
     * @return {@code true} if the parameter {@code str} represents an empty 
     *         string otherwise {@code false}.
     */
    public static boolean isStringEmpty(String str) {
        return str == null || "".equals(str);
    }

    /**
     * Returns a {@code String} initialized with the list of string sequence
     * that this {@code object} represents if the runtime class of {@code obj} 
     * represents an {@link java.lang.Iterable} class or an array class,
     * otherwise a single string containing the sequence of characters
     * that this {@code obj} represents is created and returned.
     *
     * @param obj
     *            A joint sequence of elements or a single object.
     * @return A {@code String} containing a string representation of this
     *           {@code object}.
     */
    public static String joinStringList(Object obj) {
        final StringBuilder stringBuilder = new StringBuilder();

        if (obj instanceof Iterable) {
            for (Object o : (Iterable<?>) obj) {
                stringBuilder.append(o);
            }
        } else if (obj.getClass().isArray()) {
            for (Object o : (Object[]) obj) {
                stringBuilder.append(o);
            }
        } else {
            stringBuilder.append(obj);
        }

        return stringBuilder.toString();
    }

    /**
     * Returns an array initialized with the list of string values that
     * both {@code str1} and {@code str2} contain.
     *
     * @param str1
     *            An array of strings.
     * @param str2
     *            An array of strings to be combined with {@code str1}.
     * @return An array containing the values of both list {@code str1}
     *         and {@code str2}
     */
    public static String[] joinStringArrays(String[] str1, String[] str2) {
        String[] strResult = new String[str1.length + str2.length];
        System.arraycopy(str1, 0, strResult, 0, str1.length);
        System.arraycopy(str2, 0, strResult, str1.length, str2.length);
        return strResult;
    }

    public static String join(Collection<String> src, String delimiter) {
        StringBuilder stringBuilder = new StringBuilder();
        boolean delim = true;

        for (String str : src) {
            if (delim)
                delim = false;
            else
                stringBuilder.append(delimiter);
            stringBuilder.append(str);
        }

        return stringBuilder.toString();
    }

    /**
     * Removes the leading and trailing quotes form {@code str}.
     *
     * @param str
     *            The string from which leading and trailing quotes are
     *            to be removed.
     * @return A {@code String} without leading and trailing quotes.
     */
    public static String stripLeadingAndTrailingQuotes(String str) {
        if (str.length() > 1 && str.startsWith("\"") && str.endsWith("\""))
            return str.substring(1, str.length() - 1);

        return str;
    }

    /**
     * Splits a {@code word} into a character array whose length is the length
     * of the {@code word} and whose contents are initialized with the character
     * sequence represented by this {@code word}.
     *
     * @throws IllegalArgumentException
     *             When {@code word} is an empty string.
     * @param word
     *            The string to be converted to a new character array.
     * @return A character array containing the character sequence in the
     *         {@code word}.
     */
    public static char[] splitWordIntoCharacters(String word) {
        if (isStringEmpty(word))
            throw new IllegalArgumentException("Cannot split an empty string.");

        return word.toCharArray();
    }

    /**
     * Splits a sequence of contiguous words separated by a {@code delimiter}
     * and then returns a list containing each of these words.
     *
     * @param delimiter
     *            The character that indicates the beginning and end of a word.
     * @param words
     *            The sequence of words to be split.
     * @return A list containing each word.
     */
    public static List<String> splitWordIntoSentences(String words, String delimiter) {
        if (isStringEmpty(delimiter))
            throw new IllegalArgumentException("To split a string, a delimiter must "
                        + "be specified.");

        if (isStringEmpty(words))
            throw new IllegalArgumentException(String.format("Delimiter \"%s\" cannot be used "
                        + "to split an empty string into sentences.", delimiter));

        return Arrays.asList(words.split(delimiter));
    }

    /**
     * Gets a {@code count} number of whitespace characters.
     *
     * @throws IllegalArgumentException
     *             When {@code count} is equal to or less than zero.
     * @param count
     *            The number of whitespace characters.
     * @return A {@code String} containing a sequence of whitespace characters.
     */
    public static String countSpaces(int count) {
        if (count < 1)
            throw new IllegalArgumentException("The number of whitespace characters "
                    + "mut be greater than zero.");

        StringBuilder stringBuilder = new StringBuilder();
        
        while (--count >= 0)
            stringBuilder.append(" ");

        return stringBuilder.toString();
    }
    
    /**
     * Comparator used to sort strings by length.
     * 
     * @author Ben Cisneros
     */
    public static class SortByLength implements Comparator<String> {
        
        @Override
        public int compare(String o1, String o2) {
            return o1.length() - o2.length();
        }
    }
    
    public final static SortByLength SORT_BY_LENGTH = new SortByLength();
}
