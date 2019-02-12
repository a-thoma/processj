package processj.runtime;

public class PJOne2OneChannel<T> extends PJChannel<T> {
    
    /**
     * TODO
     */
    protected PJProcess writer;
    
    /**
     * TODO
     */
    protected PJProcess reader;
    
    public PJOne2OneChannel() {
        writer = null;
        reader = null;
        type = PJChannelType.ONE2ONE;
    }

    @Override
    synchronized public void write(PJProcess p, T data) {
        this.data = data;       // set data on channel
        writer = p;             // register the writer
        writer.setNotReady();   // set writer not ready
        if (reader != null)     // if a reader is there
            reader.setReady();  // set it ready to run
    }

    @Override
    synchronized public T read(PJProcess p) {
        writer.setReady();      // set writer ready
        writer = null;          // clear writer field
        reader = null;          // clear reader field
        return data;            // return data
    }

    @Override
    synchronized public boolean isReadyToRead(PJProcess p) {
        if (writer != null)         // if a writer is present
            return true;            // return true
        else {                      // otherwise
            reader = p;             // register 'p' as the reader
            reader.setNotReady();   // set reader not ready
        }
        return false;
    }

    @Override
    public boolean isReadyToWrite() {
        return true;
    }
}
