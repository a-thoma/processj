package utilities;

/**
 * The class {@code ColorCodes} is used for ANSI colour
 * manipulation on a terminal console that supports ANSI
 * colour codes.
 * 
 * <p>
 * Usage:
 * <ul>
 * <li> ANSI_PREFIX + (Attribute | Attribute + ANSI_COMMA +
 * (AnsiForeground | AnsiBackground)) + ANSI_POSTFIX</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Example:
 * <ul>
 * <li>\033[ + 0 + m = \033[0m (Ansi Reset) </li>
 * <li> \033[ + 1 + ";" + 31 + "m" = \033[1;31m (Ansi RED) </li>
 * </ul>
 * </p>
 * 
 * @author Ben
 * @version 10/06/2018
 * @since 1.2
 */
public class ColorCodes {
    
    public static final String ANSI_PREFIX = "\033[";
    public static final String ANSI_POSTFIX = "m";
    public static final String ANSI_COMMA = ";";
    public static final String ANSI_RESET = "\033[0m";
    
    /**
     * The enum {@link AnsiForeground} represents each ANSI
     * foreground colour code.
     * 
     * @author Ben
     * @version 10/06/2018
     * @since 1.2
     */
    public enum AnsiForeground {
        
        BLACK       ("30"),
        RED         ("31"),
        GREEN       ("32"),
        YELLOW      ("33"),
        BLUE        ("34"),
        MAGENTA     ("35"),
        CYAN        ("36"),
        WHITE       ("37"),
        NONE        ("")
        ;
        
        private final String code;
        
        AnsiForeground(String code) {
            this.code = code;
        }
        
        @Override
        public String toString() {
            return code;
        }
    }
    
    /**
     * The enum {@link AnsiForeground} represents each ANSI
     * background colour code.
     * 
     * @author Ben
     * @version 10/06/2018
     * @since 1.2
     */
    public enum AnsiBackground {
        
        BLACK       ("40"),
        RED         ("41"),
        GREEN       ("42"),
        YELLOW      ("43"),
        BLUE        ("44"),
        MAGENTA     ("45"),
        CYAN        ("46"),
        WHITE       ("47"),
        NONE        ("")
        ;
        
        private final String code;
        
        AnsiBackground(String code) {
            this.code = code;
        }
        
        @Override
        public String toString() {
            return code;
        }
    }
    
    /**
     * The enum {@link Attribute} represents each ANSI
     * attribute colour code.
     * 
     * @author Ben
     * @version 10/06/2018
     * @since 1.2
     */
    public enum Attribute {
        
        DEFAULT     ("0"),
        BOLD        ("1"),
        LIGHT       ("1"),
        DARK        ("2"),
        UNDERLINE   ("4"),
        HIDDEN      ("8"),
        NONE        ("")
        ;
        
        private final String code;
        
        Attribute(String code) {
            this.code = code;
        }
        
        @Override
        public String toString() {
            return code;
        }
    }
    
    public static String colorTag(String tag, ErrorSeverity severity) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ANSI_PREFIX)
                     .append(Attribute.BOLD.toString())
                     .append(ANSI_COMMA);
        
        switch (severity) {
        case WARNING:
            stringBuilder.append(AnsiForeground.YELLOW.toString());
            break;
        case ERROR:
            stringBuilder.append(AnsiForeground.RED.toString());
            break;
        default:
            break;
        }
        stringBuilder.append(ANSI_POSTFIX)
                     .append(tag)
                     .append(ANSI_RESET);
        return stringBuilder.toString();
    }
}
