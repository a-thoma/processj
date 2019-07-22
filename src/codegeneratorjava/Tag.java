package codegeneratorjava;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The enum Tag represents and identifies various types such as
 * procedures, methods, parameters, local variables, protocols,
 * records, channels, etc.
 *
 * @author Ben
 * @version 06/15/2018
 * @since 1.2
 */
public enum Tag {
    
    // Signatures and types. These labels can be used
    // for debugging.
    MAIN_NAME           ("([T;)V"   , "mainProcedureType"),
    PROCEDURE_NAME      ("_proc$"   , "procedureType"),
    METHOD_NAME         ("_method$" , "methodType"),
    PARAM_NAME          ("_pd$"     , "parameterType"),
    LOCAL_NAME          ("_ld$"     , "localVariableType"),
    PAR_BLOCK_NAME      ("par"      , "parBlockType"),
    PROTOCOL_NAME       ("_prot$"   , "protocolType")
    ;

    private final String tag;
    private final String label;

    Tag(final String tag, final String label) {
        this.tag = tag;
        this.label = label;
    }

    public static Tag get(final String tag) {
        return findValueOf(tag);
    }

    public static boolean has(final String tag) {
        try {
            return findValueOf(tag) != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static Tag findValueOf(final String name) {
        try {
            return Tag.valueOf(name);
        } catch (IllegalArgumentException e) { // Try one more time.
            Tag result = null;
            for (Tag t : values()) {
                if (t.tag.startsWith(name)) {
                    if (result == null)
                        result = t;
                    else
                        throw new IllegalArgumentException("Unable to find ambiguous tag '" +
                                name + "' in " + getTags());
                }
            }
            if (result == null)
                throw new IllegalArgumentException("Unable to find ambiguous tag '" +
                            name + "' in " + getTags());
            return result;
        }
    }

    public static List<String> getTags() {
        return Arrays.stream(values())
                     .map(tag -> tag.toString())
                     .collect(Collectors.toList());
    }

    public String getLabel() {
        return label;
    }
    
    @Override
    public String toString() {
        return tag;
    }
}
