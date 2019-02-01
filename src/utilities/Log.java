package utilities;

import ast.AST;

public class Log {
    public static boolean doLog = false;

    public static void startLogging() {
        doLog = true;
    }

    public static void stopLogging() {
        doLog = false;
    }

    private Log() {
        // Avoid creating instances
    }

    public static void log(String s) {
        if (doLog)
            System.out.println(CompilerMessageManager.INSTANCE.fileName + ":" + s);
    }

    public static void log(AST a, String s) {
        if (doLog)
            System.out.println(CompilerMessageManager.INSTANCE.fileName + ":" + a.line + " " + s);
    }
    
    public static void logHeader(String s) {
        if (doLog)
            System.out.println(s);
    }

    public static void logNoNewline(String s) {
        if (doLog)
            System.out.print(CompilerMessageManager.INSTANCE.fileName + ":" + s);
    }

    public static void logNoNewline(AST a, String s) {
        if (doLog)
            System.out.print(CompilerMessageManager.INSTANCE.fileName + ":" + a.line + " " + s);
    }
}