package utilities;

import java.util.HashSet;
import java.util.Set;

public enum ErrorBuilder {
    
    INSTANCE
    ;
    
    private Set<IErrorGetter> messages;
    
    ErrorBuilder() {
        messages = new HashSet<IErrorGetter>();
        // TODO:
    }
}
