package utilities;

import java.util.HashSet;
import java.util.Set;

public enum StackErrorBuilder {
    
    INSTANCE
    ;
    
    private Set<IErrorGetter> messages;
    
    StackErrorBuilder() {
        messages = new HashSet<IErrorGetter>();
    }
    
    // TODO: Build a sequence of error messages in reversed order
}
