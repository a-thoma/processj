package utilities;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;

import clp.Util;

/**
 * The class {@link Tuple} represents a sequences of immutable objects.
 *
 * @param <T>
 *          The type of value.
 *
 * @author Ben Cisneros
 * @version 09/04/2018
 * @since 1.2
 */
public class Tuple<T> extends AbstractList<T> {
    private final T[] values;

    public Tuple(T[] values) {
        if (Util.isArrayEmpty(values)) {
            throw new IllegalArgumentException("The array may not be empty.");
        }

        this.values = values;
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        final int size = toIndex - fromIndex;

        @SuppressWarnings("unchecked")
        T[] newValues = (T[]) new Object[size];
        System.arraycopy(values, fromIndex, newValues, 0, size);

        return new Tuple<>(newValues);
    }

    @Override
    public T get(int index) {
        return values[index];
    }

    @Override
    public int size() {
        return values.length;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        for (final T v : values) {
            result = prime * result + v.hashCode();
        }

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        @SuppressWarnings("rawtypes")
        final Tuple other = (Tuple) obj;

        if (size() != other.size()) {
            return false;
        }

        return Arrays.equals(values, other.values);
    }

    @Override
    public String toString() {
        return Arrays.toString(values);
    }
}
