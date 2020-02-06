package utilities;

import java.util.Collection;

/**
 * This interface serves as the base for all types of maps that hold
 * a collection of values associated with different keys.
 * 
 * @param <K>
 *          The key type.
 * @param <V>
 *          The value type.
 * 
 * @author Ben
 * @version 09/04/2018
 * @since 1.2
 */
public interface Multimap<K, V> {
    
    /**
     * Adds the value to the collection that a key maps to in a MultiKeyMap.
     *
     * @param key
     *            The key that maps to the collection.
     * @param value
     *            The value to add to the collection associated with key.
     */
    void put(K key, V value);
    
    /**
     * Adds a new collection of values to the list of values that key maps
     * to in a MultiKeyMap.
     *
     * @param key
     *            The key that maps to a collection.
     * @param values
     *            The collection to be added the list of values associated
     *            with key.
     */
    void putAll(K key, Collection<V> values);
    
    /**
     * Adds the values stored in array values to the collection the key maps
     * to in a MultiKeyMap.
     *
     * @param key
     *            The key that maps to a collection.
     * @param values
     *            The array to be added the list of values associated with key.
     */
    void putAll(K key, V[] values);
    
    /**
     * Removes the specified value from the collection associated with the
     * given key and then returns true if the mapping was successfully
     * removed from a MultiKeyMap or false otherwise.
     *
     * @param key
     *            The key to remove from.
     * @param value
     *            The value to be removed.
     * @return true if the mapping was removed or false otherwise.
     */
    boolean removeMapping(K key, V value);
    
    /**
     * Gets the collection that key maps to in a MultiKeyMap.
     *
     * @param key
     *            The key that maps to a collection of values of type V.
     * @return The Collection of values that key maps to.
     */
    Collection<V> get(Object key);
    
    /**
     * Returns true if a MultiKeyMap is empty or false otherwise.
     *
     * @return true if a MultiKeyMap is empty or false otherwise.
     */
    boolean isEmpty();
    
    /**
     * Returns a collection of keys stored in a MultiKeyMap.
     *
     * @return A Collection of keys stored in a MultiKeyMap.
     */
    Collection<K> keys();
    
    /**
     * Returns a collection of values stored in a MultiKeyMap.
     *
     * @return A Collection of values stored in a MultiKeyMap.
     */
    Collection<V> values();
    
    /**
     * Returns the number of keys in a MultiKeyMap.
     *
     * @return The number of key stored in a MultiKeyMap.
     */
    int size();
    
    /**
     * Removes all keys and values from a MultiKeyMap.
     */
    void clear();
    
    /**
     * Removes all values associated with key in a MultiKeyMap.
     *
     * @param key
     *            The key whose collection is to be removed.
     */
    void remove(Object key);
    
    /**
     * Adds the keys and collection of values of another map to this
     * MultiKeyMap.
     *
     * @param other
     *            The MultiKeyMap whose keys and collection of values are
     *            to be added.
     */
    void merge(Multimap<K, V> other);
}
