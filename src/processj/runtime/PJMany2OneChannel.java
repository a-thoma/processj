package processj.runtime;

import java.util.LinkedList;

/**
 * The runtime representation of a many-to-one channel.
 *
 * @author Cabel Shrestha
 * @version 1.0
 * @since 2016-05-01
 */

public class PJMany2OneChannel<T> extends PJChannel<T> {
    /**
     * A reference to the reader of the channel
     */
    private PJProcess reader = null;
    /**
     * A list of references to the writers wishing to read from this channel.
     */
    private LinkedList<PJProcess> writers = new LinkedList<PJProcess>();

    /**
     * Constructor
     */
    public PJMany2OneChannel() {
//        this.type = TYPE_MANY2ONE;
        type = PJChannelType.MANY2ONE;
    }

    /**
     * Writes a data item value of type T to the channel.
     *
     * @param p
     *            The writing process.
     * @param item
     *            The data item to be exchanged.
     */
    @Override
    synchronized public void write(PJProcess p, T item) {
        hasData = true;
        data = item;
        writers.addFirst(p);
        p.setNotReady();

        if (reader != null) {
            reader.setReady();
        }
    }

    /**
     * Reads a data item value item of type T from the channel.
     *
     * @param p
     *            The reading process.
     * @return T The read value.
     */
    @Override
    synchronized public T read(PJProcess p) {
        T myData = data;
        data = null;
        hasData = false;
        if (writers.size() > 0) {
            PJProcess writer = writers.removeFirst();
            writer.setReady();
        }
        return myData;
    }

    /**
     * First part of an extended rendez-vous read. Returns the data item but does
     * not set the writer ready.
     *
     * @param p
     *            The reading process.
     * @return T The read value.
     */
    @Override
    synchronized public T readPreRendezvous(PJProcess p) {
        T myData = data;
        data = null;
        return myData;
    }

    /**
     * Second part of an extended rendez-voud read. Sets the first waiting writer
     * ready to run.
     *
     * @param p
     *            The reading process.
     */
    @Override
    synchronized public void readPostRendezvous(PJProcess p) {
        hasData = false;
        if (writers.size() > 0) {
            PJProcess writer = writers.removeFirst();
            writer.setReady();
        }
    }

    /**
     * Adds a writer to the writer queue.
     *
     * @param p
     *            The process waiting to write.
     */
    @Override
    synchronized public void addWriter(PJProcess p) {
        writers.add(p);
    }

    /**
     * Sets the reader (Probably never called!)
     *
     * @param p
     *            A process.
     */
    @Override
    synchronized public void addReader(PJProcess p) {
        reader = p;
    }
}