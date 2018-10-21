package utilities;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

/**
 * This class is used to create generic error messages
 * in ProcessJ.
 * 
 * @author Ben Cisneros
 * @since 1.2
 */
public class ErrorMessage extends BaseErrorMessage {

    /**
     * String template file locator.
     */
    private final String stErrorFile = "resources/stringtemplates/messages/errorTemplate.stg";
    
    private final STGroup stGroup = new STGroupFile(stErrorFile);
    
    public ErrorMessage(Builder builder) {
        super(builder);
    }
    
    @Override
    public ST getMessage() {
        ST stFile = stGroup.getInstanceOf("File");
        ST stTag = stGroup.getInstanceOf("Tag");
        ST stStackInfo = stGroup.getInstanceOf("StackInfo");
        ST stMessage = stGroup.getInstanceOf("Message");
        
        if (fileName != null)
            stFile.add("fileName", fileName);
        if (ast != null)
            stFile.add("lineNumber", ast.line);
        if (errorMessage != null) {
            stTag.add("tag", errorMessage.getErrorType());
            stTag.add("errorName", errorMessage.getText());
            stTag.add("errorType", errorMessage.getNumber());
        }
        if (throwable != null) {
            stStackInfo.add("reason", throwable);
            stStackInfo.add("stack", throwable.getStackTrace());
        }
        
        stMessage.add("tag", stTag.render())
                 .add("errorMessage", super.getMessage().render())
                 .add("location", stFile.render())
                 .add("stackInfo", stStackInfo.render());
        
        return stMessage;
    }

    @Override
    public String renderMessage() {
        // TODO: For additional esthetic look, make changes here
        String renderMsg = getMessage().render();
        return renderMsg;
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
    public static final class Builder extends BaseErrorMessage.Builder<Builder> {

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        public <E extends BaseErrorMessage> E build() {
            @SuppressWarnings("unchecked")
            E error = (E) new ErrorMessage(this);
            return error;
        }
    }
    
    public static void main(String[] args) {
        ErrorMessage.builder = new ErrorMessage.Builder();
        builder.addErrorMessage(VisitorErrorMessage.RESOLVE_IMPORTS_105);
        builder.addArguments("B.pj", "path/Pkg");
        builder.addThrowable(new RuntimeException("<some text here>"));
        System.out.println(ErrorMessage.builder.build().renderMessage());
    }
}
