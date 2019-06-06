package processj.runtime;

public class PJAlt {
    
    /**
     * Can be skips, timers or channel-reads.
     */
    private Object[] guards;
    
    /**
     * Boolean guards.
     */
    private boolean[] bguards;
    
    /**
     * Process declaring the 'alt'.
     */
    private PJProcess process;
    
    public static final String SKIP = "skip";
    
    public PJAlt(int count, PJProcess p) {
        process = p;
        guards = new Object[count];
        bguards = new boolean[count];
    }
    
    public boolean setGuards(boolean[] bguards, Object[] guards) {
        this.guards = guards;
        this.bguards = bguards;
        
        for (boolean b : bguards) {
            if (b)
                return true;
        }
        
        return false;
    }
    
    @SuppressWarnings("rawtypes")
    public int enable() {
        int i;
        for (i = 0; i < guards.length; ++i) {
            if (!bguards[i])
                continue;
            // A skip?
            if (guards[i] == PJAlt.SKIP) {
                process.setReady();
                return i;
            }
            // A channel?
            if (guards[i] instanceof PJChannel) {
                PJChannel chan = (PJChannel) guards[i];
                if (chan.altGetWriter(process) != null) {
                    process.setReady();
                    return i;
                }
            }
        }
        return -1;
    }
    
    @SuppressWarnings("rawtypes")
    public int disable(int i) {
        int selected = -1;
        if (i == -1)
            i = guards.length - 1;
        for (int j = i; j >= 0; --j) {
            // A skip?
            if (guards[j] == PJAlt.SKIP)
                selected = j;
            // A channel?
            if (guards[j] instanceof PJChannel) {
                PJChannel chan = (PJChannel) guards[j];
                if (chan.setReaderGetWriter(null) != null)
                    selected = j;
            }
        }
        return selected;
    }
}
