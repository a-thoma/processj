package utilities;

import java.io.File;
import java.util.Iterator;
import java.util.Stack;

import org.stringtemplate.v4.ST;

/**
 * The class {@link ErrorTracker} is used to track down various
 * types of error messages generated at compile-time or run-time.
 * 
 * @author Ben Cisneros
 * @version 21/10/2018
 * @since 1.2
 */
public enum ErrorTracker {
    
    INSTANCE
    ;
    
    /**
     * Number of errors messages generated by the compiler.
     */
    private int errorCount;
    
    /**
     * List of errors messages.
     */
    private Stack<PJErrorMessage> errorTrace;
    
    /**
     * Current source of error/warning message.
     */
    public String fileName = "";
    
    /**
     * Package of examined input file.
     */
    public String packageName = "";
    
    ErrorTracker() {
        errorCount = 0;
        errorTrace = new Stack<>();
    }
    
    public void printContinue(PJErrorMessage errorMsg) {
        ST msg = errorMsg.getST();
        System.out.println(msg.render());
        errorTrace.push(errorMsg);
    }
    
    public void dontPrintContinue(PJErrorMessage errorMsg) {
        errorTrace.push(errorMsg);
    }
    
    public void printStop(PJErrorMessage errorMsg) {
        errorTrace.push(errorMsg);
        ST msg = errorMsg.getST();
        System.out.println(msg.render());
        System.exit(0);
    }
    
    public int getErrorCount() {
        return errorCount;
    }
    
    public Stack<PJErrorMessage> getTrace() {
        return errorTrace;
    }
    
    public void printTrace() {
        Iterator<PJErrorMessage> it = errorTrace.iterator();
        while (it.hasNext()) {
            System.out.println(it.next().render());
            if (it.hasNext())
                System.out.println();
        }
    }
    
    public void setPackageName(String name) {
        // First strip the `.pj' part
        String str = name.replaceAll("\\.pj$", "");
        // Now remove the absolute path
        String absPath = new File("").getAbsolutePath() + "/";
        str = str.replaceAll(absPath, "");
        // replace all `/' with .
        str = str.replaceAll("/", "\\.");
        packageName = str;
    }
    
    public void setFileName(String name) {
        // remove all double `//:'
        String str = name.replaceAll("//","/");
        // Now remove the absolute path
        String absPath = new File("").getAbsolutePath() + "/";
        str = str.replaceAll(absPath,"");
        fileName = str;
    }
}
