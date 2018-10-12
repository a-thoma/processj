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
 * @author Ben Cisneros
 * @version 10/06/2018
 * @since 1.2
 */
public class ColorCodes {
    
    public static final String ANSI_PREFIX = "\033[";
    public static final String ANSI_POSTFIX = "m";
    public static final String ANSI_COMMA = ";";
    
    /**
     * The enum {@code AnsiForeground} represents each ANSI
     * foreground colour code.
     * 
     * @author Ben Cisneros
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
     * The enum {@code AnsiForeground} represents each ANSI
     * background colour code.
     * 
     * @author Ben Cisneros
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
     * The enum {@code Attribute} represents each ANSI
     * attribute colour code.
     * 
     * @author Ben Cisneros
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
}
