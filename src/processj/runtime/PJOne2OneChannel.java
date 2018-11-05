package processj.runtime;

/**
 * The runtime representation of a one-to-one channel.
 * 
 * @author Ben
 * @author Cabel Shrestha
 * @version 08/29/2018
 * @since 1.2
 */

public class PJOne2OneChannel<T> extends PJChannel<T> {
    
    /**
     * A reference to the writer of the channel
     */
    private PJProcess writer = null;

    /**
     * A reference to the reader of the channel
     */
    private PJProcess reader = null;

    /**
     * Constructor.
     */
    public PJOne2OneChannel() {
        type = PJChannelType.ONE2ONE;
    }

    /**
     * Writes a data item value of type T to the channel.
     *
     * @param p
     *          The writing process.
     * @param item
     *          The data item to be exchanged.
     */
    @Override
    synchronized public void write(PJProcess p, T item) {
        data = item;
        writer = p;
        writer.setNotReady();
        hasData = true;
        if (reader != null) {
            reader.setReady();
        }
    }

    /**
     * Reads a data item value item of type T from the channel.
     *
     * @param p
     *          The reading process.
     * @return T The read value.
     */
    @Override
    synchronized public T read(PJProcess p) {
        hasData = false;
        // We need to set the writer ready as the synchronization has happened
        // when the data was read.
        writer.setReady();
        // clear the writer and reader
        writer = null;
        reader = null;
        return data;
    }

    /**
     * First part of an extended rendezvous read. Returns the data item but does
     * not set the writer ready.
     *
     * @param p
     *          The reading process.
     * @return T The read value.
     */
    @Override
    synchronized public T readPreRendezvous(PJProcess p) {
        T myData = data;
        data = null;
        return myData;
    }

    /**
     * Second part of an extended rendezvous read. Sets the writer ready to run.
     *
     * @param p
     *          The reading process.
     */
    @Override
    synchronized public void readPostRendezvous(PJProcess p) {
        hasData = false;
        writer.setReady();
        writer = null;
        reader = null;
    }
    
    // TODO: delete everything that comes after this comment!!!!!
    // We can't delete these methods because of the abstract class contract

    @Override
    synchronized public void addReader(PJProcess p) {
        reader = p;
    }

    /**
     * Throws {@link UnsopportedOperationException} when invoked.
     *
     * @param p
     *          A process.
     */
    @Override
    synchronized public void addWriter(PJProcess p) {
        throw new UnsupportedOperationException(String.format("Invalid operation for a %s", type.getText()));
    }
}