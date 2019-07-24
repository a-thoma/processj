package utilities;

/**
 * This class represents a pair of two elements of the same or
 * different data type.
 *
 * @see Tuple
 * @param <K>
 *            The left element type.
 * @param <V>
 *            The right element type.
 *
 * @author Ben
 * @version 09/04/2018
 * @since 1.2
 */
@SuppressWarnings("rawtypes")
public class Pair<K, V> extends Tuple {
    
    /** The key. */
    private final K key;

    /** The value. */
    private final V value;

    @SuppressWarnings("unchecked")
    public Pair(final K key, final V value) {
        super(new Object[] { key, value });
        this.key = key;
        this.value = value;
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public Object get(int index) {
        Object value = null;

        if (index == 0) {
            value = get(0);
        } else if (index == 1) {
            value = get(1);
        } else if (index >= 2) {
            throw new IndexOutOfBoundsException("Index out of range.");
        }

        return value;
    }

    /**
     * Gets the first pair of two elements.
     *
     * @return the first pair of two elements.
     */
    public K getFirst() {
        return key;
    }

    /**
     * Gets the second pair of two elements.
     *
     * @return the second pair of two elements.
     */
    public V getSecond() {
        return value;
    }

    /**
     * Gets the key.
     *
     * @return The first pair of two elements.
     */
    public K getKey() {
        return key;
    }

    /**
     * Get the value.
     *
     * @return The second pair of two elements.
     */
    public V getValue() {
        return value;
    }

    /**
     * Creates a new Pair containing elements left and right.
     *
     * @param left
     *            The first (or key) element.
     * @param right
     *            The second (or value) element.
     * @param <K>
     *            The key type.
     * @param <V>
     *            The value type.
     * @return A new Pair initialized with left and right elements.
     */
    public static <K, V> Pair<K, V> create(K left, V right) {
        return new Pair<K, V>(left, right);
    }
    
    @Override
    public String toString() {
        return String.format("(%s, %s)", key, value);
    }
}
