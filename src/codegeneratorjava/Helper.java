package codegeneratorjava;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import ast.ProcTypeDecl;
import ast.Type;
import utilities.Assert;
import utilities.Settings;

/**
 * The class {@link Helper} contains helper methods for
 * the {@link CodeGeneratorJava}.
 *
 * @author Ben
 * @version 06/21/2018
 * @since 1.2
 */
public class Helper {

    /**
     * Changes the name of a procedure, method, protocol, record, channel or local
     * variable so that the JVM can separate common names which belong to the same
     * compiled class.
     * 
     * <p>
     * The ProcessJ naming convention is as follows:
     * </p>
     * 
     * <ul>
     * <li>
     * For a procedure, the procedure is encoded as '{@code _proc$nameX}' where
     * {@code name} is the procedure's unique identifier.
     * </li>
     *
     * <li>
     * For a Java method, the method is encoded as '{@code _method$nameX}' where
     * {@code name} is the method's unique identifier.
     * </li>
     *
     * <li>
     * For parameters, the parameter is encoded as '{@code _pd$nameX}' where
     * {@code name} is the name of the argument and '{@code X}' represents the
     * position of the parameter in the argument list.
     * <ul>
     * <li>For example: foo(_pd$bar0, _pd$foo1, ..., _pd@nameX)</li>
     * </ul>
     * </li>
     *
     * <li>
     * For locals, the local is encoded as '{@code _ld$nameX}' where {@code name}
     * is the name of the local variable and '{@code X}' is the local's unique
     * identifier.
     * </li>
     *
     * <li>
     * For protocols, the protocol is encoded as '{@code _prot$nameX}' where
     * {@code name} is a protocol tag and '{@code X}' is the protocol's unique
     * identifier.
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
            // Ignore
            break;
        case PROCEDURE_NAME:
            varName = Tag.PROCEDURE_NAME.getTag() + name; break;
        case METHOD_NAME:
            varName = Tag.METHOD_NAME.getTag() + name; break;
        case PARAM_NAME:
            varName = Tag.PARAM_NAME.getTag() + name + X; break;
        case LOCAL_NAME:
            varName = Tag.LOCAL_NAME.getTag() + name + X; break;
        case PROTOCOL_NAME:
            varName = Tag.PROTOCOL_NAME.getTag() + name; break;
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
        if (pd == null)
            return false;
        
        return pd.yields ||
               (pd.annotations().isDefined("yield") &&
               Boolean.valueOf(pd.annotations().get("yield")));
    }
    
    /**
     * Returns the wrapper class for the given class {@code type}.
     * 
     * @param type
     *          A primitive class type or the class itself.
     * @return The type instances represented by a class.
     */
    public static Class<?> getWrapperClass(Type type) {
        type = Assert.nonNull(type, "The parameter type cannot be null.");
        Class<?> typeName = null;
        if (type.isIntegerType()) {
            typeName = Integer.class;
        } else if (type.isByteType()) {
            typeName = Byte.class;
        } else if (type.isLongType()) {
            typeName = Long.class;
        } else if (type.isDoubleType()) {
            typeName = Double.class;
        } else if (type.isFloatType()) {
            typeName = Float.class;
        } else if (type.isBooleanType()) {
            typeName = Boolean.class;
        } else if (type.isCharType()) {
            typeName = Character.class;
        } else if (type.isShortType()) {
            typeName = Short.class;
        }
        
        return typeName;
    }
    
    /**
     * Returns a string representing a primitive wrapper class
     * or the class itself.
     * 
     * @param type
     *          A primitive class type or the class itself.
     * @return A {@code String} representation of class {@code type}.
     */
    public static String getWrapperType(Type type) {
        return getWrapperClass(type).getSimpleName();
    }
    
    // ==========================================
    // I N V A L I D   I D E N T I F I E R S
    // ==========================================
    
    // This is to prevent collision of names with special keywords
    // when generating Java class files
    private static final Set<String> INVALID_NAMES = new HashSet<>(Arrays.asList(
            new String[] {
                    /* Java keywords */
                    "abstract", "assert", "class", "catch", "enum", "extends", "final",
                    "goto", "instanceof", "interface", "static", "super", "synchronized",
                    "this", "throw", "throws", "try", "null",
                    /* ProcessJ keywords */
                    "label", "jump", "terminate", "yield"
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
        // An invocation comes from a external file (an import) if the
        // source file from which the invocation is made is different to
        // the package
        if (!packageName.contains(sourceFile)) {
            String includePath = Settings.includeDir + File.separator + Settings.targetLanguage + File.separator;
            // The following replaces all '/' with '.'
            includePath = includePath.replaceAll(File.separator, "\\.");
            packageName = packageName.replaceAll(includePath, "");
            return packageName;
        }
        
        // Otherwise, the invocation must come from the same source
        // file (and package)
        return sourceFile;
    }
    
    public static void writeToFile(String output, String sourceFile) {
        // TODO: Write to home folder
        String javafile = "/Users/Ben/Desktop/processj/tests/" + sourceFile + ".java";
        Writer writer = null;

        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(javafile), "utf-8"));
            writer.write(output);
            writer.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}