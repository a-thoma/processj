package utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The class {@link MultiValueMap} represents a collection of values
 * associated with a key.
 * 
 * @author Ben Cisneros
 * @version 09/04/2018
 * @since 1.2
 */
public class MultiValueMap<K, V> implements IMultiMap<K, V> {
    
    /** The map containing keys and values. */
    private final Map<K, Collection<V>> hashMap;

    public MultiValueMap() {
        hashMap = new HashMap<>();
    }

    @Override
    public void put(K key, V value) {
        Collection<V> collection = hashMap.get(key);

        if (collection == null) {
            collection = new ArrayList<>();
            hashMap.put(key, collection);
        }

        collection.add(value);
    }

    @Override
    public void putAll(K key, Collection<V> values) {
        Collection<V> collection = hashMap.get(key);

        if (collection == null) {
            collection = new ArrayList<>();
            hashMap.put(key, collection);
        }

        collection.addAll(values);
    }

    @Override
    public void putAll(K key, V[] values) {
        Collection<V> collection = hashMap.get(key);

        if (collection == null) {
            collection = new ArrayList<>();
            hashMap.put(key, collection);
        }

        collection.addAll(Arrays.asList(values));
    }

    @Override
    public boolean removeMapping(K key, V value) {
        if (!hashMap.containsKey(key)) {
            return false;
        }
        Iterator<V> it = hashMap.get(key).iterator();

        while (it.hasNext()) {
            if (it.next().equals(value)) {
                it.remove();
                return true;
            }
        }

        return false;
    }

    @Override
    public Collection<V> get(Object key) {
        if (hashMap.get(key) == null) {
            return Collections.<V>emptyList();
        }

        return hashMap.get(key);
    }

    @Override
    public boolean isEmpty() {
        return hashMap.isEmpty();
    }

    @Override
    public Collection<K> keys() {
        return hashMap.keySet();
    }

    @Override
    public Collection<V> values() {
        Collection<V> result = new ArrayList<>();

        for (Collection<V> collection : hashMap.values()) {
            result.addAll(collection);
        }

        return result;
    }

    @Override
    public int size() {
        return hashMap.keySet().size();
    }

    @Override
    public void clear() {
        hashMap.clear();
    }

    @Override
    public void remove(Object key) {
        hashMap.remove(key);
    }

    @Override
    public void merge(IMultiMap<K, V> other) {
        for (K k : other.keys()) {
            putAll(k, other.get(k));
        }
    }

    @Override
    public String toString() {
        return hashMap.toString();
    }

    @Override
    public int hashCode() {
        return hashMap.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (this == obj || getClass() != obj.getClass()) {
            return false;
        }

        @SuppressWarnings("rawtypes")
        MultiValueMap other = (MultiValueMap) obj;

        if (!hashMap.equals(other.hashMap)) {
            return false;
        }

        return true;
    }

    public List<Pair<K, V>> getPairs() {
        List<Pair<K, V>> pairs = new ArrayList<>();

        for (K key : keys()) {
            for (V value : get(key)) {
                pairs.add(new Pair<>(key, value));
            }
        }

        return pairs;
    }

    /**
     * Creates an empty {@link MultiValueMap}.
     *
     * @param <K>
     *            The key type.
     * @param <V>
     *            The value type.
     * @return An empty {@link MultiValueMap}.
     */
    public static <K, V> MultiValueMap<K, V> create() {
        return new MultiValueMap<>();
    }

    /**
     * Creates and initializes a new {@link MultiValueMap} with the keys and values
     * from another {@code multiMap}.
     *
     * @param multiMap
     *            The map whose keys and values are used to initialize a new
     *            {@link MultiValueMap} with.
     * @param <K>
     *            The key type.
     * @param <V>
     *            The value type.
     * @return An new {@link MultiValueMap} initialized with {@code multiMap}.
     */
    public static <K, V> MultiValueMap<K, V> create(IMultiMap<K, V> multiMap) {
        MultiValueMap<K, V> multiHashMap = new MultiValueMap<>();
        multiHashMap.merge(multiMap);

        return multiHashMap;
    }
}
