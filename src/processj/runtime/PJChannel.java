package processj.runtime;

/**
 * @author Ben
 * @version 07/02/19
 * @since 1.2
 *
 * @param <T>
 */
public abstract class PJChannel<T> {
    
    /**
     * Value in channel.
     */
    protected T data;
    
    protected PJChannelType type;
    
    /**
     * TODO
     * 
     * @param p
     * @param data
     */
    public abstract void write(PJProcess p, T data);
    
    /**
     * TODO
     * 
     * @param p
     */
    public abstract T read(PJProcess p);
    
    /**
     * TODO
     * 
     * @param p
     * @return
     */
    public abstract boolean isReadyToRead(PJProcess p);
    
    /**
     * TODO
     * 
     * @param p
     * @return
     */
    public abstract boolean isReadyToWrite();
}
