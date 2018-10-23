package utilities;

import java.net.URL;

/**
 * @author Ben Cisneros
 * @version 10/22/2018
 * @since 1.2
 */
public class PropertiesLoader {
    
    public static URL getURL(String fileName) {
        URL url;
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        url = cl.getResource(fileName);
        if (url == null) {
            cl = IErrorGetter.class.getClassLoader();
            url = cl.getResource(fileName);
        }
        return url;
    }
}
