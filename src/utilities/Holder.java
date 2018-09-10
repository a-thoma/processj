package utilities;

/**
 * A place holder for an object.
 * 
 * @author Ben Cisneros
 * @version 08/30/2018
 * @since 1.2
 *
 * @param <T>
 *          The object's type.
 */
public class Holder<T> {
    
    T obj;
    
    private Holder() {
        // Nothing to do
    }
    
    public Holder(T obj) {
        this.obj = obj;
    }
    
    public T getObject() {
        return obj;
    }
    
    public void setObject(T obj) {
        this.obj = obj;
    }
}
