package utilities;

import java.util.HashSet;
import java.util.Set;

public enum StackErrorMessage {
    
    INSTANCE
    ;
    
    private Set<IErrorGetter> messages;
    
    StackErrorMessage() {
        messages = new HashSet<IErrorGetter>();
    }
    
    // TODO: Build a sequence of error messages in reversed order
}
