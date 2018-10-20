package utilities;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import ast.AST;

/**
 * @author Ben Cisneros
 * @since 1.2
 */
public class ErrorMessage extends BaseErrorMessage {

    /**
     * String template file locator.
     */
    private final String stErrorFile = "resources/stringtemplates/messages/errorTemplate.stg";
    
    private final STGroup stGroup;
    
    private ErrorType errorType;
    
    public ErrorMessage(ErrorType errorType, AST ast, IErrorGetter errorMessage,
                        Throwable throwable, Object... arguments) {
        super(ast, errorMessage, throwable, arguments);
        this.errorType = errorType;
        stGroup = new STGroupFile(stErrorFile);
        
    }
    
    @Override
    public ST getMessage() {
        ST stFile = stGroup.getInstanceOf("File");
        ST stTag = stGroup.getInstanceOf("Tag");
        ST stStackInfo = stGroup.getInstanceOf("StackInfo");
        ST stMessage = stGroup.getInstanceOf("Message");
        
        if (!fileName.isEmpty())
            stFile.add("fileName", fileName);
        if (ast != null)
            stFile.add("lineNumber", ast.line);
        if (errorMessage != null) {
            stTag.add("tag", errorMessage.getErrorType());
            stTag.add("errorName", errorMessage.getText());
            stTag.add("errorType", errorMessage.getNumber());
        }
        if (throwable != null)
            stStackInfo.add("stack", throwable.getStackTrace());
        
        stMessage.add("tag", stTag.render())
                 .add("errorMessage", super.getMessage().render())
                 .add("location", stFile.render())
                 .add("stackInfo", stStackInfo.render());
        
        return stMessage;
    }
    
    public ErrorType getErrorType() {
        return errorType;
    }

    @Override
    public String renderMessage() {
        return this.getMessage().render();
    }
    
    //
    public static class Builder {
        // TODO:
    }
}
