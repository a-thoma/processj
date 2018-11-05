package utilities;

import org.stringtemplate.v4.ST;

/**
 * This class is used to create generic messages
 * in ProcessJ.
 * 
 * @author Ben
 * @since 1.2
 */
public class PJMessage extends CompilerMessage {
    
    private boolean doStackTrace = false;
    
    public PJMessage(Builder builder) {
        super(builder);
        doStackTrace = builder.doStackTrace;
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
        
        // Apply colour code if allowed on terminal. The attribute
        // must be set to either `yes' or `no'
        String tag = stTag.render();
        if (Settings.isAnsiColour)
            tag = ColorCodes.colorTag(stTag.render(), error.getErrorSeverity());
        
        return stMessage.add("tag", tag)
                        .add("message", super.getST().render())
                        .add("location", stFile.render())
                        .add("stack", stStackInfo.render());
    }
    
    public String renderMessage() {
        ST stResult = getST();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(stResult.render());
        // TODO: Add Java's stack trace if needed
        if (doStackTrace)
            ;
        return stringBuilder.toString();
    }
    
    public boolean hasStackTrace() {
        return doStackTrace;
    }
    
    // =====================
    // B U I L D E R
    // =====================
    
    /**
     * Builder for this basic error message type.
     * 
     * @author Ben
     * @version 10/20/2018
     * @since 1.2
     */
    public static final class Builder extends CompilerMessage.Builder<Builder> {
        
        protected boolean doStackTrace;
        
        public Builder() {
            doStackTrace = false;
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        public <E extends CompilerMessage> E build() {
            @SuppressWarnings("unchecked")
            E error = (E) new PJMessage(this);
            return error;
        }
        
        public Builder addStackTrace(boolean doStackTrace) {
            this.doStackTrace = doStackTrace;
            return builder();
        }
    }
}
