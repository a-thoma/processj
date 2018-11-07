package clp;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;

import utilities.Assert;

/**
 * The class {@link ArityRange} represents a list of indexes
 * from some starting position to some ending position on the
 * command line.
 * 
 * @author Ben
 * @version 08/23/2018
 * @since 1.2
 */
public class ArityRange extends AbstractList<Integer> {
    
    /**
     * The first number in the range of values.
     */
    private int from;
    
    /**
     * The last number in the range of values.
     */
    private int to;
    
    private ArityRange(int from, int to) {
        if (from > to) {
            this.from = to;
            this.to = to;
        } else {
            this.from = from;
            this.to = to;
        }
    }
    
    public int getFrom() {
        if (from <= to)
            return from;
        return to;
    }
    
    public int getTo() {
        if (from <= to)
            return to;
        return from;
    }
    
    public boolean hasFixedArity() {
        return from == to;
    }
    
    public static ArityRange createArity(String range) {
        range = Assert.nonNull(range, "Parameter range cannot be null.");
        
        if (StringUtil.isStringEmpty(range) || range.length() == 0)
            throw new RuntimeException("Invalid or empty range.");
        
        boolean takesUnlimitedArgs = range.endsWith("..*");
        int fromValue = -1;
        int toValue = -1;
        int separatorIndex = range.indexOf("..");
        if (separatorIndex != -1) {
            if (separatorIndex == 0)
                throw new RuntimeException("Invalid range. A range must be of the form \"0..n\".");
            // TODO: Validate range!
            fromValue = Integer.parseInt(range.substring(0, separatorIndex));
            if (!takesUnlimitedArgs)
                toValue = Integer.parseInt(range.substring(separatorIndex + 2));
            else
                toValue = Integer.MAX_VALUE;
        } else if (range.equals("+")) {
            fromValue = 1;
            toValue = Integer.MAX_VALUE;
        } else {
            fromValue = Integer.parseInt(range);
            toValue = fromValue;
        }
        
        if (fromValue < 0 || toValue < 0)
            throw new RuntimeException(String.format("A range must be positive. Found [%d..%d].", fromValue, toValue));
        
        return new ArityRange(fromValue, toValue);
    }

    @Override
    public Integer get(int index) {
        if (index < 0)
            throw new RuntimeException();
        if (index >= size())
            throw new RuntimeException();
        return index + getFrom();
    }

    @Override
    public int size() {
        return getTo() - getFrom();
    }
    
    @Override
    public Iterator<Integer> iterator() {
        return new ArityRangeIterator();
    }
    
    @Override
    public boolean contains(Object obj) {
        if (obj instanceof Integer) {
            Integer value = (Integer) obj;
            return value >= getFrom() && value <= getTo();
        }
        
        return false;
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public boolean containsAll(Collection obj) {
        if (!(obj instanceof ArityRange))
            super.containsAll(obj);
        ArityRange arity = (ArityRange) obj;
        return getFrom() <= arity.getFrom() && arity.getTo() <= getTo();
    }
    
    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + from;
        result = prime * result + to;
        return result;
    }
    
    @Override
    public final boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;
        
        ArityRange other = (ArityRange) obj;
        if (from != other.from || to != other.to)
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        return "[" + from + ".." + to + "]";
    }
    
    // =================
    // H E L P E R S
    // =================
    
    /**
     * A helper iterator class.
     * 
     * @author Ben
     * @version 08/23/2018
     * @since 1.2
     */
    private class ArityRangeIterator implements Iterator<Integer> {
        
        /**
         * A count from zero to N-1.
         */
        private int position = 0;
        
        /**
         * The number of values within range.
         */
        private int numValues = size();
        
        /**
         * The next number to return from a range of values.
         */
        private int nextValue;
        
        @Override
        public boolean hasNext() {
            return position < numValues && numValues > 0;
        }

        @Override
        public Integer next() {
            if (!hasNext())
                throw new RuntimeException("Index out of bound!");
            
            if (position++ > 0)
                nextValue++;
            return nextValue;
        }
    }
}
