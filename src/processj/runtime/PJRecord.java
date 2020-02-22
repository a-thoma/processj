package processj.runtime;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author ben
 */
public class PJRecord extends HashMap<String, Object> {
    
    private int hashCode;
    
    public PJRecord(String[] keys, Object[] values) {
        for (int i = 0; i < values.length; ++i)
            super.put(keys[i], values[i]);
    }
    
    @SuppressWarnings("unchecked")
    public PJRecord(Map map) {
        super(map);
    }
    
    public Object get(String key) {
        return super.get(key);
    }
    
    @Override
    public Object put(String key, Object value) {
        return super.put(key, value);
    }
    
    @Override
    public Object remove(Object key) {
        throw new RuntimeException(String.format("Record %s is immutable.", this));
    }
    
    @Override
    public void putAll(Map map) {
        throw new RuntimeException(String.format("Record %s is immutable.", this));
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PJRecord))
            return false;
        
        PJRecord other = (PJRecord) o;
        if (size() == other.size()) {
            for (Object e : entrySet()) {
                Map.Entry entry = (Map.Entry) e;
                Object key = entry.getKey();
                if (!entry.getValue().equals(other.get(key)))
                    return false;
            }
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        if (hashCode == 0) {
            for (Object key : keySet()) {
                int hash = (key != null) ? key.hashCode() : 0xdadd;
                hashCode ^= hash;
            }
        }
        return hashCode;
    }
    
    private String findKeyFor(HashMap<String, Object> entries, Object o) {
        for (Map.Entry<String, Object> e : entries.entrySet())
            if (e.getValue() == o)
                return e.getKey();
        return null;
    }
    
    public String toString(HashMap<String, Object> entries) {
        if (entries == null)
            entries = new HashMap<String, Object>();
        System.out.println(">> " + entries.size() + ":" + entries );
        StringBuilder sb = new StringBuilder("*:[");
        Iterator<String> it = keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            Object value = get(key);
            if (get(key) instanceof PJRecord) {
                if (value == this) {
                    String varName = findKeyFor(entries, value);
                    varName = varName != null? "*" + varName : "*";
                    sb.append(key).append(":").append(varName);
                } else {
                    entries.put(key, value);
                    String s = ((PJRecord) value).toString(entries);
                    if (s.startsWith("*"))
                        s = s.substring(1, s.length());
                    sb.append(key).append(s);
                }
            } else
                sb.append(key).append(":").append(value);
            if (it.hasNext())
                sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
}
