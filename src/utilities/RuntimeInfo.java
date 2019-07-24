package utilities;

/**
 * This class checks the current version of the ProcessJ compiler and
 * its runtime system. Starting from version 2.1.x, the runtime system
 * invokes the verifyVersion() method to notify the user of any possible
 * mismatch between the 'current' version of the ProcessJ compiler and
 * the runtime system being used. Note, the runtime system should be
 * downloaded and manually linked to the ProcessJ compiler.
 * 
 * @author Ben
 */
public final class RuntimeInfo {
    
    // Contains the current version of the ProcessJ runtime system.
    protected static final String VERSION = "2.1.1";
    
    public static String getRuntimeVersion() {
        return VERSION;
    }
    
    /**
     * This methods detects mismatches between the version of the runtime
     * system and the current version of the ProcessJ compiler.
     * 
     * @param runtimeVersion
     *          The version of the rumtime system used to create processes.
     */
    public static void verifyVersion(String runtimeVersion) {
        String versionMajorMinor = substringMajorMinorVersion(VERSION);
        boolean runtimeVersionMismatch = false;
        
        if (runtimeVersion != null && runtimeVersion.length() > 0) {
            String runtimeMajorMinor = substringMajorMinorVersion(runtimeVersion);
            runtimeVersionMismatch = !VERSION.equals(runtimeVersion) && !versionMajorMinor.equals(runtimeMajorMinor);
        }
        
        if (runtimeVersionMismatch)
            System.err.println("Runtime version '" + runtimeVersion + "' does not match current version.");
    }
    
    /**
     * Returns a string representing the major and minor version of the
     * runtime system or compiler version. For example, given a string
     * 'x.y.z', this function will return 'x.y' -- the major and minor
     * version.
     * 
     * @param v
     *          The version string.
     */
    public static String substringMajorMinorVersion(String v) {
        int majorPart = v.indexOf(".");
        int minorPart = majorPart >= 0 ? v.indexOf(".", majorPart + 1) : -1;
        int len = v.length();
        if (minorPart >= 0)
            len = Math.min(len, minorPart);
        return v.substring(0,len);
    }
}
