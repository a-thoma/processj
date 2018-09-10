package processj.runtime;

/**
 * The {@link PJChannelType} represents channel type constants.
 * 
 * @author Ben Cisneros
 * @version 08/29/2018
 * @since 1.2
 */
public enum PJChannelType {
    /**
     * Specifies a one-to-one {@link PJChannel} object for use by one
     * writer and one reader.
     */
    ONE2ONE ("one-to-one PJChannel object for use by one writer and one reader"),
    
    /**
     * Specifies a one-to-many {@link PJChannel} object for use by one
     * writer and many readers.
     */
    ONE2MANY ("one-to-many PJChannel object for use by one writer and many readers"),
    
    /**
     * Specifies a many-to-one {@link PJChannel} object for use by many
     * writers and one reader.
     */
    MANY2ONE ("many-to-one PJChannel object for use by many writers and one reader"),
    
    /**
     * Specifies a many-to-many {@link PJChannel} object for use by many
     * writers and many readers.
     */
    MANY2MANY ("many-to-many PJChannel object for use by many writers and many readers");
    
    private final String text;
    
    PJChannelType(String text) {
        this.text = text;
    }
    
    public String getText() {
        return text;
    }
}
