package utilities;

import org.stringtemplate.v4.ST;

/**
 * This class is used to create generic error messages
 * in ProcessJ.
 * 
 * @author Ben
 * @since 1.2
 */
public class ErrorMessage extends PJErrorMessage {
    
    public ErrorMessage(Builder builder) {
        super(builder);
    }
    
    @Override
    public ST getST() {
        ST stFile = stGroup.getInstanceOf("File");
        ST stTag = stGroup.getInstanceOf("Tag");
        ST stStackInfo = stGroup.getInstanceOf("StackInfo");
        ST stMessage = stGroup.getInstanceOf("Message");
        
        if (ast != null) {
            stFile.add("fileName", fileName);
            stFile.add("lineNumber", ast.line);
        }
        if (error != null) {
            stTag.add("tag", error.getErrorSeverity());
            stTag.add("number", error.getNumber());
        }
        
        if (throwable != null) {
            stStackInfo.add("reason", throwable);
            stStackInfo.add("stack", throwable.getStackTrace());
        }
        
        stMessage.add("tag", stTag.render())
                 .add("message", super.getST().render())
                 .add("location", stFile.render())
                 .add("stack", stStackInfo.render());
        
        return stMessage;
    }
    
    public String render() {
        return getST().render();
    }
    
    // =====================
    // B U I L D E R
    // =====================
    
    /**
     * Builder for this basic error message type.
     * 
     * @author Ben Cisneros
     * @version 10/20/2018
     * @since 1.2
     */
    public static final class Builder extends PJErrorMessage.Builder<Builder> {

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        public <E extends PJErrorMessage> E build() {
            @SuppressWarnings("unchecked")
            E error = (E) new ErrorMessage(this);
            return error;
        }
    }
}
