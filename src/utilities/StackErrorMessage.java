package utilities;

import java.util.ArrayList;
import java.util.List;

public enum StackErrorMessage {
    
    INSTANCE
    ;
    
    private List<BaseErrorMessage> trace;
    
    StackErrorMessage() {
        trace = new ArrayList<>();
    }
    
    // TODO: Build a sequence of error messages in reversed order
}
