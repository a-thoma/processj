package utilities;

import org.stringtemplate.v4.ST;

/**
 * This class is used to create generic messages for the ProcessJ compiler.
 * 
 * @author ben
 * @since 1.2
 */
public class ProcessJMessage extends ProcessJBugMessage {
    
    private boolean doStackTrace = false;
    
    public ProcessJMessage(Builder builder) {
        super(builder);
        doStackTrace = builder.doStackTrace;
    }
    
    @Override
    public ST stTemplate() {
        ST stFile = stGroup.getInstanceOf("File");
        ST stTag = stGroup.getInstanceOf("Tag");
        ST stStackInfo = stGroup.getInstanceOf("StackInfo");
        ST stMessage = stGroup.getInstanceOf("Message");
        
        if (d_ast != null) {
            stFile.add("fileName", d_fileName);
            stFile.add("lineNumber", d_ast.line);
        }
        
        if (d_errorNumber != null) {
            stTag.add("tag", d_errorNumber.getErrorSeverity());
            stTag.add("number", d_errorNumber.getNumber());
        }
        
        if (d_throwable != null) {
            stStackInfo.add("reason", d_throwable);
            stStackInfo.add("stack", d_throwable.getStackTrace());
        }
        
        /* Apply color code if allowed on terminal */
        String tag = stTag.render();
        if (Settings.showColor)
            tag = ColorCodes.colorTag(stTag.render(), d_errorNumber.getErrorSeverity());
        
        stMessage.add("tag", tag);
        stMessage.add("message", super.stTemplate().render());
        stMessage.add("location", stFile.render());
        stMessage.add("stack", stStackInfo.render());
        
        return stMessage;
    }
    
    public String renderMessage() {
        ST stResult = stTemplate();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(stResult.render());
        if (doStackTrace && d_throwable != null)
            stringBuilder.append(d_throwable.toString());
        return stringBuilder.toString();
    }
    
    public boolean hasStackTrace() {
        return doStackTrace;
    }
    
    //
    // BUILDER
    //
    
    /**
     * Builder for this basic error message type.
     * 
     * @author Ben
     * @version 10/20/2018
     * @since 1.2
     */
    public static final class Builder extends ProcessJBugMessage.Builder<Builder> {
        
        protected boolean doStackTrace;
        
        public Builder() {
            doStackTrace = false;
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        public <E extends ProcessJBugMessage> E build() {
            @SuppressWarnings("unchecked")
            E error = (E) new ProcessJMessage(this);
            return error;
        }
        
        public Builder addStackTrace(boolean doStackTrace) {
            this.doStackTrace = doStackTrace;
            return builder();
        }
    }
}
