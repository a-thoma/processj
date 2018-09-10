package utilities;

import java.util.Collection;

/**
 * The interface {@link IMultiMap} serves as a base for all types of
 * maps that hold a collection of values associated with different keys.
 * 
 * @param <K>
 *          The key type.
 * @param <V>
 *          The value type.
 * 
 * @author Ben Cisneros
 * @version 09/04/2018
 * @since 1.2
 */
public interface IMultiMap<K, V> {
    
    /**
     * Adds the {@code value} to the collection that {@code key} maps to in a
     * {@link IMultiMap}.
     *
     * @param key
     *            The key that maps to the collection.
     * @param value
     *            The value to add to the collection associated with {@code key}.
     */
    void put(K key, V value);
    
    /**
     * Adds a new collection of values to the list of values that {@code key} maps
     * to in a {@link IMultiMap}.
     *
     * @param key
     *            The key that maps to a collection.
     * @param values
     *            The collection to be added the list of values associated with
     *            {@code key}.
     */
    void putAll(K key, Collection<V> values);
    
    /**
     * Adds the values stored in array {@code values} to the collection the
     * {@code key} maps to in a {@link IMultiMap}.
     *
     * @param key
     *            The key that maps to a collection.
     * @param values
     *            The array to be added the list of values associated with
     *            {@code key}.
     */
    void putAll(K key, V[] values);
    
    /**
     * Removes the specified value from the collection associated with the given
     * {@code key} and then returns {@code true} if the mapping was successfully
     * removed from a {@link IMultiMap} or {@code false} otherwise.
     *
     * @param key
     *            The key to remove from.
     * @param value
     *            The value to be removed.
     * @return {@code true} if the mapping was removed or {@code false} otherwise.
     */
    boolean removeMapping(K key, V value);
    
    /**
     * Gets the collection that {@code key} maps to in a {@link IMultiMap}.
     *
     * @param key
     *            The key that maps to a collection of values of type {@code V}.
     * @return The {@link Collection} of values that {@code key} maps to.
     */
    Collection<V> get(Object key);
    
    /**
     * Returns {@code true} if a {@link IMultiMap} is empty or {@code false}
     * otherwise.
     *
     * @return {@code true} if a {@link IMultiMap} is empty or {@code false}
     *         otherwise.
     */
    boolean isEmpty();
    
    /**
     * Returns a collection of keys stored in a {@link IMultiMap}.
     *
     * @return A {@link Collection} of keys stored in a {@link IMultiMap}.
     */
    Collection<K> keys();
    
    /**
     * Returns a collection of values stored in a {@link IMultiMap}.
     *
     * @return A {@link Collection} of values stored in a {@link IMultiMap}.
     */
    Collection<V> values();
    
    /**
     * Returns the number of keys in a {@link IMultiMap}.
     *
     * @return The number of key stored in a {@link IMultiMap}.
     */
    int size();
    
    /**
     * Removes all keys and values from a {@link IMultiMap}.
     */
    void clear();
    
    /**
     * Removes all values associated with {@code key} in a {@link IMultiMap}.
     *
     * @param key
     *            The key whose collection is to be removed.
     */
    void remove(Object key);
    
    /**
     * Adds the keys and collection of values of an{@code other} map to this
     * {@link IMultiMap}.
     *
     * @param other
     *            The {@link IMultiMap} whose keys and collection of values are to
     *            be added.
     */
    void merge(IMultiMap<K, V> other);
}
