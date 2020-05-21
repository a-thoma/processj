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
//     public static Language language = Language.JVM;
    public static Language language = Language.CPLUS;
    public static boolean ansiColor = false;
    public static final String IMPORT_FILE_EXTENSSION = "pj";
    public static final String LEGACY_FILE_EXTENSION = "processj";
}