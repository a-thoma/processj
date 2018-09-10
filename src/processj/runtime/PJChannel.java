package processj.runtime;

/**
 * The runtime representation of a channel. All four different channel types
 * subclass this class.
 *
 * @author Cabel Shrestha
 * @author Ben Cisneros
 * @version 08/29/2018
 * @since 1.2
 */

public abstract class PJChannel<T> {
    
    /**
     * The data item communicated on the channel.
     */
    protected T data;

    /**
     * True if there is data available on the channel; that is, the writer has
     * already committed to a communication
     */
    protected boolean hasData = false;

    /**
     * True if the reading end of the channel has been reserved for an alt.
     */
    protected boolean reservedForAlt = false;

    /**
     * The type of the channel (one-to-one, one-to-many, many-to-one, or
     * many-to-many).
     */
    protected PJChannelType type;

    /**
     * Determines if the reading end of the channel has been reserved for a specific
     * reader.
     */
    protected PJProcess reservedForReader = null;

    /**
     * Abstract write method.
     *
     * @param p
     *            A reference to the process calling write.
     * @param item
     *            The data item communicated on the channel.
     */
    public abstract void write(PJProcess p, T item);

    /**
     * Abstract read method.
     *
     * @param p
     *            A reference to the process calling read.
     * @return T The data item communicated on the channel.
     */
    public abstract T read(PJProcess p);

    /**
     * Abstract read (pre rendez-vous) method.
     *
     * @param p
     *            A reference to the process calling read.
     * @return T the data item communicated on the channel.
     */
    public abstract T readPreRendezvous(PJProcess p);

    /**
     * Abstract read method (post rendez-vous) method.
     *
     * @param p
     *            A reference to the process calling read.
     */
    public abstract void readPostRendezvous(PJProcess p);

    /**
     * Abstract method to add writers to a queue (used for shared channel ends
     * only).
     *
     * @param p
     *            A reference to the process to be added to the reader list.
     */
    public abstract void addReader(PJProcess p);

    /**
     * Abstract method to add readers to a queue (used for shared channel ends
     * only(.
     *
     * @param p
     *            A reference to the process to be added to the writer list.
     */
    public abstract void addWriter(PJProcess p);

    /* ======= NEW CODE ======= */
    protected boolean claimedForRead = false;
    protected boolean claimedForWrite = false;

    synchronized public boolean claimReadingEnd() {
        boolean success = false;
        if (!this.claimedForRead) {
            this.claimedForRead = true;
            success = true;
        }
        return success;
    }

    synchronized public boolean claimWritingEnd() {
        boolean success = false;
        if (!this.claimedForWrite) {
            this.claimedForWrite = true;
            success = true;
        }
        return success;
    }

    synchronized public void unclaimReadingEnd() {
        this.claimedForRead = false;
    }

    synchronized public void unclaimWritingEnd() {
        this.claimedForWrite = false;
    }

    /*
     * Note: to avoid busy wait on a claim: if a claim fails add yourself to a list
     * of processes interested in claiming read or write end (we need two lists),
     * and when an unclaim happens, wake the first process in the queue up
     * 
     * /* ======== END NEW CODE ========
     */

    /**
     * Returns true if the channel is ready for a read; that is, there must be data
     * from a writer present and if the channel is reserved for someone else or
     * reserved for an alt, false is returned.
     *
     * @param p
     *            A reference to the process requesting the status.
     * @return True or false depending on whether the channel is ready for reading.
     *         All calls to isReadyToRead and to read() must happen in the same
     *         synchronized block.
     *
     *         <pre>
     * {@code
     * L2:
     *     synchronized (c) {
     *       if (c.isReadyToRead(this)) {
     *         ... = c.read(this);
     *       } else {
     *         setNotReady();
     *         yield(2);
     *         return;
     *      }
     *    }
     * }
     *         </pre>
     */
    synchronized public boolean isReadyToRead(PJProcess p) {
        // Data present and reserved for a specific reader.
        if (hasData && reservedForReader != null) {
            return (reservedForReader == p);
        }
        // Data present but is reserved for an alt.
        else if (reservedForAlt) {
            return false;
        }
        // Not reserved for alt nor for reader, ready determines if ready or not.
        else {
            return hasData;
        }
    }

    /**
     * Returns true if the channel is ready for a read. If it is mark the channel
     * as reserved for a read in an alt.
     *
     * @return True or false depending on whether the channel is ready for reading.
     */
    synchronized public boolean isReadyToReadAltAndReserve() {
        if (hasData && !reservedForAlt && reservedForReader == null) {

            /*
             * NOTE: Setting this reservedForAlt is necessary so that, in the time space
             * between if(c.readyToRead()) and c.read() Some faster process will not read
             * the data in channel c due to processor thread interleaving.
             *
             * say call1: A.c.readyToRead (ret true) B.c.readyToReady (ret true) B.c.read
             * A.c.read <= What will happen here??
             */
            reservedForAlt = true;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Unreserve the reading end of a channel that was previously reserved for
     * reading in an alt. This typically happens if the channel read guard in
     * an alt was ready but not chosen.
     */
    synchronized public void unreserve() {
        reservedForAlt = false;
    }

    /**
     * Returns true if there is not data waiting to be read on the channel.
     *
     * @return True if the channel is read to be written to, false otherwise.
     */
    synchronized public boolean isReadyToWrite() {
        return !hasData;
    }

    /**
     * Returns true if the reading end of this channel is shared.
     *
     * @return Returns true if the channel has a shared read end.
     */
    public boolean isSharedRead() {
        return type == PJChannelType.ONE2MANY || type == PJChannelType.MANY2MANY;
    }

    /**
     * Returns true if the writing end of this channel is shared.
     *
     * @return Returns true if the channel has a shared writing end.
     */
    public boolean isSharedWrite() {
        return type == PJChannelType.MANY2ONE || type == PJChannelType.MANY2MANY;
    }

    /**
     * Returns the type of the channel.
     * NOTE: This method is currently not used.
     *
     * @return The type of the channel (One-to-one, One-to-Many, Many-to-One, or
     *         Many-to-Many).
     */
    public PJChannelType getType() {
        return type;
    }
}