package utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Stack;

/**
 * This class is used to track down various kind of error/warning
 * messages produced by the ProcessJ compiler at compile-time or
 * runtime.
 * 
 * @author Ben
 * @version 21/10/2018
 * @since 1.2
 */
public enum CompilerMessageManager {
    
    INSTANCE
    ;
    
    /**
     * Number of errors/warning messages generated by the compiler.
     */
    private int errorCount;
    
    /**
     * Last registered message
     */
    private CompilerMessage myPostPonedMessage = null;
    
    /**
     * List of errors/warning messages.
     */
    private Stack<CompilerMessage> stackTrace;
    
    /**
     * Input file that contains error/warning messages.
     */
    public String fileName = "";
    
    /**
     * Location of input file.
     */
    public String packageName = "";
    
    CompilerMessageManager() {
        errorCount = 0;
        stackTrace = new Stack<CompilerMessage>();
    }
    
    public void add(CompilerMessage cm, MessageNumber m) {
        ErrorSeverity severity = m.getErrorSeverity();
        switch (severity) {
        case INFO:
        case WARNING:
            break;
        case ERROR:
            ++errorCount;
            break;
        }
        stackTrace.push(cm);
        myPostPonedMessage = cm;
    }
    
    public void reportMessage(CompilerMessage cm, MessageType mt) {
        cm = Assert.nonNull(cm, "Compiler message cannot be null.");
        boolean doStop = false;
        add(cm, cm.getMessageNumber());
        
        switch(mt) {
        case PRINT_STOP:
            doStop = true;
        case PRINT_CONTINUE:
            // Throw the first error that occurred
            if (myPostPonedMessage == null)
                myPostPonedMessage = cm;
            System.out.println(cm.renderMessage());
            if (doStop)
                System.exit(0);
        case DONT_PRINT_CONTINUE:
            break;
        }
    }
    
    public void reportMessage(CompilerMessage cm) {
        reportMessage(cm, cm.getMessageNumber().getMessageType());
    }
    
    public int getErrorCount() {
        return errorCount;
    }
    
    public Stack<CompilerMessage> getTrace() {
        return stackTrace;
    }
    
    public CompilerMessage getPostPonedMessage() {
        return myPostPonedMessage;
    }
    
    public void printTrace(String source) {
        System.out.println("****************************************");
        System.out.println("*        E R R O R   R E P O R T       *");
        System.out.println("****************************************");
        System.out.println(String.format("%d error(s) in '%s'", errorCount, source));
        Iterator<CompilerMessage> it = stackTrace.iterator();
        while (it.hasNext()) {
            System.out.println(it.next().renderMessage());
            if (it.hasNext())
                System.out.println("-------------------------------------------------------");
        }
    }
    
    public void setPackageName(String name) {
        // First strip the '.pj' part
        String str = name.replaceAll("\\.pj$", "");
        // Now remove the absolute path
        String absPath = new File("").getAbsolutePath() + "/";
        str = str.replaceAll(absPath, "");
        // replace all '/' with .
        str = str.replaceAll("/", "\\.");
        packageName = str;
    }
    
    public void setFileName(String name) {
        // remove all double '//:'
        String str = name.replaceAll("//","/");
        // Now remove the absolute path
        String absPath = new File("").getAbsolutePath() + "/";
        str = str.replaceAll(absPath,"");
        fileName = str;
    }
    
    public void writeToFile(String outputFile) {
        // TODO: Should the error file be written to the source directory?
        String javafile = "/Users/Ben/Documents/" + outputFile + ".txt";
        StringBuilder stringBuilder = new StringBuilder();
        for (CompilerMessage cm : stackTrace)
            stringBuilder.append(cm.getMessageNumber().getNumber()).append("\n");
        Writer writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(javafile), "utf-8"));
            writer.write(stringBuilder.toString());
            writer.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
