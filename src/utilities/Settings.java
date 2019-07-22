package utilities;
import java.io.File;

/**
 * @author Ben
 * @version 08/30/2018
 * @since 1.2
 */
public class Settings {
    
    public static String absolutePath = new File("").getAbsolutePath() + "/";
    public static String includeDir = "include";
    public static Language targetLanguage = Language.JVM;
    public static boolean isAnsiColor = false;
    public static String importFileExtension = "pj";
}