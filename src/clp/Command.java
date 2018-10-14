package clp;

/**
 * The class {@link Command} serves as the base class
 * for all extended commands in ProcessJ.
 * <p>
 * See {@link Parameters @Parameters}.
 * </p>
 * 
 * @author Ben Cisneros
 * @version 08/11/2018
 * @since 1.2
 */
public abstract class Command {
    
    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + getClass().hashCode();
        return result;
    }
    
    @Override
    public final boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        
        return true;
    }
}
