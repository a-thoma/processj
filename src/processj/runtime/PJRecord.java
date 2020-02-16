package processj.runtime;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author ben
 */
public class PJRecord {
    
    private static int s_id = 0;
    private int id;
    
    public PJRecord() {
        id = ++s_id;
    }
    
    public int getID() {
        return id;
    }
    
    public String toString1() {
        StringBuilder sb = new StringBuilder("*:[");
        Iterator<Field> it = Arrays.asList(getClass().getDeclaredFields()).iterator();
        boolean isSelfRef = false;
        while (it.hasNext()) {
            try {
                Field f = it.next();
                f.setAccessible(true);
                if (f.get(this) instanceof PJRecord) {
                    if (f.get(this) == this) {
                        sb.append(f.getName()).append(":").append(String.format("[(%s)]", id));
                        isSelfRef = true;
                    } else {
                        String str = ((PJRecord) f.get(this)).toString1();
                        if (str.startsWith("*"))
                            str = str.substring(1, str.length());
                        else {
                            int star = str.indexOf(")*");
                            str = str.substring(0, star + 1) + str.substring(star + 2, str.length());
                        }
                        sb.append(f.getName()).append(str);
                    }
                } else
                    sb.append(f.getName()).append(":").append(f.get(this));
                if (it.hasNext())
                    sb.append(", ");
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(PJRecord.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        sb.append("]");
        String s = sb.toString();
        s = isSelfRef ? String.format("(%s)", id) + s : s;
        return s;
    }
}
