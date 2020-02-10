package processj.runtime;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Ben
 */
public interface PJRecord {
    
    default String messyToString(Object o) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        String self = null;
        Iterator<Field> it = Arrays.asList(o.getClass().getDeclaredFields()).iterator();
        while (it.hasNext()) {
            try {
                Field f = it.next();
                f.setAccessible(true);
                /* Avoid self-reference */
                if (f.get(o) == o) {
                    self = f.getName();
                    continue;
                }
                sb.append(f.getName() + " : " + f.get(o));
                if (it.hasNext())
                    sb.append(", ");
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(PJRecord.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(PJRecord.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (self != null)
            sb.append(self + " : " + sb.toString() + "...}");
        sb.append("}");
        return sb.toString();
    }
}
