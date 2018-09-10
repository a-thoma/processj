package codegeneratorjava;

import static codegeneratorjava.Tag.CHANNEL_NAME;
import static codegeneratorjava.Tag.CHANNEL_READ_NAME;
import static codegeneratorjava.Tag.CHANNEL_WRITE_NAME;
import static codegeneratorjava.Tag.LOCAL_NAME;
import static codegeneratorjava.Tag.METHOD_NAME;
import static codegeneratorjava.Tag.PARAM_NAME;
import static codegeneratorjava.Tag.PROCEDURE_NAME;
import static codegeneratorjava.Tag.PROTOCOL_NAME;
import static codegeneratorjava.Tag.RECORD_NAME;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import ast.ProcTypeDecl;
import clp.Assert;
import utilities.Settings;

/**
 * The class {@link Helper} contains helper methods for the {@link CodeGeneratorJava}.
 *
 * @author Ben Cisneros
 * @version 06/21/2018
 * @since 1.2
 */
public class Helper {

    /**
     * Changes the name of a procedure, method, protocol, record, channel or local
     * variable so that the JVM can separate common names which belong to the same
     * compiled class. For now, the process of encoding types and variables into
     * unique names is trivial.
     * 
     * <p>
     * The ProcessJ naming convention is as follows:
     * </p>
     * 
     * <ul>
     * <li>
     * For a procedure, the procedure is encoded as `{@code _proc$nameX}' where
     * {@code name} is the procedure's unique identifier.
     * </li>
     *
     * <li>
     * For a Java method, the method is encoded as `{@code _method$nameX}' where
     * {@code name} is the method's unique identifier.
     * </li>
     *
     * <li>
     * For parameters, the parameter is encoded as `{@code _pd$nameX}' where
     * {@code name} is the name of the argument and `{@code X}' represents the
     * position of the parameter in the argument list.
     * <ul>
     * <li>For example: foo(_pd$bar0, _pd$foo1, ..., _pd@nameX)</li>
     * </ul>
     * </li>
     *
     * <li>
     * For locals, the local is encoded as `{@code _ld$nameX}' where {@code name}
     * is the name of the local variable and `{@code X}' is the local's unique
     * identifier.
     * </li>
     *
     * <li>
     * For protocols, the protocol is encoded as `{@code _prot$nameX}' where
     * {@code name} is a protocol tag and `{@code X}' is the protocol's unique
     * identifier.
     * </li>
     *
     * <li>
     * For records, the record is encoded as `{@code _rec$nameX}' where
     * {@code name} is a record tag and `{@code X}' is the records' unique identifier.
     * </li>
     *
     * <li>For channels, the channel is encoded as `{@code _chan$nameX}' where
     * `{@code name}' is either `{@code READ}' or `{@code WRITE}' and `{@code X}' is
     * the channel's unique identifier.
     * </li>
     * </ul>
     *
     * @param name
     *            The name or tag of a procedure, method, protocol, record,
     *            parameter, or local variable.
     * @param X
     *            A unique identifier or position in a procedure's argument list.
     * @param type
     *            A tag to encode in a procedure, method, parameter, local variable,
     *            protocol, record, or channel.
     * @return A symbolic encoded name that represents an identifier/variable.
     */
    public static String makeVariableName(final String name, int X, Tag type) {
        String varName = "";

        switch (type) {
        case MAIN_NAME:
            // TODO: Nothing to do
            break;
        case PROCEDURE_NAME:
            varName = PROCEDURE_NAME.getTag() + name;
            break;
        case METHOD_NAME:
            varName = METHOD_NAME.getTag() + name;
            break;
        case PARAM_NAME:
            varName = PARAM_NAME.getTag() + name + X;
            break;
        case LOCAL_NAME:
            varName = LOCAL_NAME.getTag() + name + X;
            break;
        case PROTOCOL_NAME:
            varName = PROTOCOL_NAME.getTag() + name + X;
            break;
        case RECORD_NAME:
            varName = RECORD_NAME.getTag() + name + X;
            break;
        case CHANNEL_NAME:
            varName = CHANNEL_NAME.getTag() + name + X;
            break;
        case CHANNEL_READ_NAME:
            varName = CHANNEL_NAME.getTag() + name + X + CHANNEL_READ_NAME.getTag();
            break;
        case CHANNEL_WRITE_NAME:
            varName = CHANNEL_NAME.getTag() + name + X + CHANNEL_WRITE_NAME.getTag();
            break;
        default:
            break;
        }

        return varName;
    }

    /**
     * Returns {@code true} if a procedure is capable of yielding or {@code false}
     * otherwise. Note that {@code "yieldability"} is determined by checking the
     * procedure's annotation through {@link ProcTypeDecl#annotations()}.
     *
     * @see ProcTypeDecl#annotations()
     * @param pd
     *            The procure whose annotation is to be checked.
     * @return {@code true} if the procedure can yield or {@code false} otherwise.
     */
    public static boolean doesProcedureYield(final ProcTypeDecl pd) {
        if (pd == null) {
            return false;
        }
        
        return pd.annotations().isDefined("yield") && Boolean.valueOf(pd.annotations().get("yield"));
    }
    
    /**
     * Returns the wrapper class for the given class {@code type}.
     * 
     * @param type
     *          A primitive class type or the class itself.
     * @return The type instances represented by a class.
     */
    public static Class<?> getWrapperClass(Class<?> type) {
        type = Assert.nonNull(type, "The parameter type cannot be null.");
        if (type == Integer.TYPE) {
            type = Integer.TYPE;
        } else if (type == Byte.TYPE) {
            type = Byte.TYPE;
        } else if (type == Long.TYPE) {
            type = Long.TYPE;
        } else if (type == Double.TYPE) {
            type = Double.TYPE;
        } else if (type == Float.TYPE) {
            type = Float.TYPE;
        } else if (type == Boolean.TYPE) {
            type = Boolean.TYPE;
        } else if (type == Character.TYPE) {
            type = Character.TYPE;
        } else if (type == Short.TYPE) {
            type = Short.TYPE;
        }
        
        return type;
    }
    
    /**
     * Returns a string representing a primitive wrapper class
     * or the class itself.
     * 
     * @param type
     *          A primitive class type or the class itself.
     * @return A {@code String} representation of class {@code type}.
     */
    public static String getWrapperType(Class<?> type) {
        return getWrapperClass(type).toString();
    }
    
    // -----------------------------------------------------------------------------
    // INVALID IDENTIFIERS
    
    // This is to prevent collision of names with special keywords in Java
    // when generating Java class files
    private static final Set<String> INVALID_NAMES = new HashSet<>(Arrays.asList(
            new String[] { "abstract", "assert", "class", "catch", "enum", "extends", "final",
                    "goto", "instanceof", "interface", "static", "super", "synchronized",
                    "this", "throw", "throws", "try", "null"
            }));
    
    /**
     * Returns {@code true} if the name of a variable, method, class, etc. represents
     * an invalid Java identifier or {@code false} otherwise.
     * 
     * @param identifier
     *              The name of a variable, method, class, etc.
     * @return {@code true} if an {@code identifier} contains valid Java characters.
     */
    public static boolean isInvalidJavaIdentifier(String identifier) {
        if (identifier.length() != 0 && !INVALID_NAMES.contains(identifier)) {
            if (!Character.isJavaIdentifierStart(identifier.charAt(0))) {
                return true;
            }
            
            char[] letters = identifier.toCharArray();
            for (char ch : letters) {
                if (!Character.isJavaIdentifierPart(ch)) {
                    return true;
                }
            }
            
            return false;
        }
        
        return true;
    }
    
    public static String getPackage(String packageName, String sourceFile) {
        // An invocation comes from a different file (an import) if the source file
        // from which an invocation is made is different to the package
        if (!packageName.contains(sourceFile)) {
            String includePath = Settings.includeDir + File.separator + Settings.targetLanguage + File.separator;
            // The following replaces all `/' with `.'
            includePath = includePath.replaceAll(File.separator, "\\.");
            packageName = packageName.replaceAll(includePath, "");
            return packageName;
        }
        
        // Otherwise, the invocation must come from the same source file (and package)
        return sourceFile;
    }
    
    public static void writeToFile(String output, String sourceFile) {
        String javafile = "/Users/Ben/Documents/" + sourceFile + ".java";
        Writer writer = null;

        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(javafile), "utf-8"));
            writer.write(output);
            writer.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    
    // -----------------------------------------------------------------------------
    // TYPE SYSTEM
    
    public static boolean isRangeInt(Class<?> type) {
        return type == Integer.class || type == Integer.TYPE ||
               type == Byte.class || type == Byte.TYPE ||
               type == Short.class || type == Short.TYPE;
    }
    
    public static boolean isRangeLong(Class<?> type) {
        return type == Long.class || type == Long.TYPE || isRangeInt(type);
    }
    
    public static boolean isRangeDouble(Class<?> type) {
        return type == Float.class || type == Float.TYPE ||
               type == Double.class || type == Double.TYPE;
    }
}