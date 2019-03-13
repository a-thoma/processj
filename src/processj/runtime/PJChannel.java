package processj.runtime;

/**
 * @author Ben
 * @version 07/02/19
 * @since 1.2
 *
 * @param <T>
 */
public abstract class PJChannel<T> {
    
    protected T data = null;
    
    protected PJChannelType type;
    
    //
    // The methods below must be defined by the parent
    // class of all channel types
    //
    
    // ************************************
    // One-2-One Channel
    // ************************************
    public abstract void write(PJProcess p, T data);
    
    public abstract T read(PJProcess p);
    
    public abstract boolean isReadyToRead(PJProcess p);
    
    public abstract boolean isReadyToWrite();
    
    //
    // The methods below must be overridden by the appropriate
    // subclass (channel type)
    //
    
    // ************************************
    // One-2-Many Channel: Shared read end
    // ************************************
    public boolean claimRead(PJProcess p) {
        return false;
    }
    
    public void unclaimRead() {
        // empty on purpose
    }
    
    // ************************************
    // Many-2-One Channel: Shared read end
    // ************************************
    public boolean claimWrite(PJProcess p) {
        return false;
    }
    
    public void unclaimWrite() {
        // empty on purpose
    }
}
