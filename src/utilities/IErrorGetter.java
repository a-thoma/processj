package utilities;

import java.net.URL;

/**
 * The interface {@code IErrorGetter} declares and defines
 * methods that when building useful error messages would
 * enforce {@code enum}s to provide an implementation for
 * both {@link #getText()} and {@link #getMessage()}, and
 * for {@link #getErrorSeverity()} or {@link #getNumber()}
 * if needed.
 * 
 * @author Ben Cisneros
 * @version 10/21/2018
 * @since 1.2
 */
public interface IErrorGetter {
    
    default ErrorSeverity getErrorSeverity() {
        return ErrorSeverity.INFO;
    }
    
    default int getNumber() {
        return -1;
    }
    
    String getMessage();
    
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
