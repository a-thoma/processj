package codegeneratorjava;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;

import ast.ProcTypeDecl;
import ast.Type;
import utilities.Assert;
import utilities.Settings;

/**
 * This class contains helper methods for the CodeGenerator.
 *
 * @author Ben
 * @version 06/21/2018
 * @since 1.2
 */
public class Helper {

    /**
     * Changes the name of a procedure, method, protocol, record, channel
     * or local variable so that the JVM can separate common names which
     * belong to the same compiled class. The ProcessJ naming convention
     * is as follows:
     * 1.) For a procedure, the procedure is encoded as '_proc$name' where
     * name is the procedure's unique identifier.
     * 2.) For a Java method, the method is encoded as '_method$name' where
     * name is the method's unique identifier.
     * 3.) For parameters, the parameter is encoded as '_pd$nameX' where
     * name is the name of the argument and 'X' represents the position of
     * the parameter in the argument list; e.g.,
     *              foo(_pd$bar0, _pd$foo1, ..., _pd@nameN)
     * 4.) For locals, the local is encoded as '_ld$nameX' where name is
     * the name of the local variable and 'X' is the local's unique
     * identifier.
     * 5.) For protocols, the protocol is encoded as '_prot$name' where
     * name is a protocol tag and 'X' is the protocol's unique identifier.
     * 
     * @param name
     *            The name or tag of a procedure, method, protocol, record,
     *            parameter, or local variable.
     * @param X
     *            A unique identifier or position in a procedure's argument
     *            list.
     * @param type
     *            A tag to encode in a procedure, method, parameter, local
     *            variable, protocol, record, or channel.
     * @return A symbolic encoded name that represents an identifier/variable.
     */
    public static String makeVariableName(final String name, int X, Tag type) {
        String varName = "";

        switch (type) {
        case MAIN_NAME:
            break; // Do nothing for now.
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
     * Returns true if a procedure is capable of yielding or false
     * otherwise. Note that 'yieldability' is determined by checking
     * the procedure's annotation through annotations().
     *
     * @param pd
     *            The procure whose annotation is to be checked.
     * @return true if the procedure can yield or false otherwise.
     */
    public static boolean doesProcedureYield(final ProcTypeDecl pd) {
        if (pd == null)
            return false;
        
        return pd.yields ||
               (pd.annotations().isDefined("yield") &&
               Boolean.valueOf(pd.annotations().get("yield")));
    }
    
    /**
     * Returns the wrapper class for the given class type.
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
     * @return A String representation of class type.
     */
    public static String getWrapperType(Type type) {
        return getWrapperClass(type).getSimpleName();
    }
    
    // The idea was to used this set of special keyword for
    // name conflicts. Note, this should be taken care of in
    // namechecker
    private static final HashSet<String> INVALID_NAMES = new HashSet<String>(Arrays.asList(
            new String[] {
                    /* Java keywords */
                    "abstract", "assert", "class",
                    "catch", "enum", "extends", "final",
                    "goto", "instanceof", "interface",
                    "static", "super", "synchronized",
                    "this", "throw", "throws", "try",
                    "null",
                    /* ProcessJ keywords */
                    "label", "jump", "terminate", "yield"
            }));
    
    /**
     * Returns true if the name of a variable, method, class,
     * etc. represents an invalid Java identifier or false
     * otherwise.
     * 
     * @param identifier
     *              The name of a variable, method, class, etc.
     * @return true if an identifier contains valid Java characters.
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
        // An invocation comes from a external file (an import)
        // if the source file from which the invocation is made
        // is different to the package
        if (!packageName.contains(sourceFile)) {
            String includePath = Settings.includeDir + File.separator + Settings.targetLanguage + File.separator;
            // The following replaces all '/' with '.'
            includePath = includePath.replaceAll(File.separator, "\\.");
            packageName = packageName.replaceAll(includePath, "");
            return packageName;
        }
        
        // Otherwise, the invocation must come from the same
        // source file and package
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