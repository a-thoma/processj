package cli;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The class {@link OptionValue} is a wrapper class that encapsulates
 * operations on an {@link Option @Option} annotation. Information
 * about an the annotation and each of its elements can be accessed
 * dynamically through this wrapper class.
 * 
 * @author Ben
 * @version 08/05/2018
 * @since 1.2
 */
public final class OptionValue extends OptionWithValue {
    
    /**
     * The name (or names) of this option.
     */
    private final List<String> names;
    
    private OptionValue(Builder builder) {
        super(builder);
        names = builder.names;
    }
    
    public List<String> getNames() {
        return names;
    }
    
    public String getOptionOrArgumentHelp(int indent, int width) {
        StringBuilder stringBuilder = new StringBuilder(Formatter.DEFAULT_LENGTH + help.length());
        if (required)
            stringBuilder.append("*");
        else
            stringBuilder.append("  ");
        
        Iterator<String> itNames = names.iterator();
        while (itNames.hasNext()) {
            stringBuilder.append(itNames.next());
            if (itNames.hasNext())
                stringBuilder.append(", ");
        }
        
        if (metavar.isEmpty())
            stringBuilder.append(" ");
        else if (!split.isEmpty())
            stringBuilder.append("=").append(metavar);
        else
            stringBuilder.append(" ").append(metavar);
        
        while (indent > stringBuilder.length() + 2)
            stringBuilder.append(" ");
        stringBuilder.append(" ");

        String newHelp = defaultValue.isEmpty() ? help : help + " (default=" + defaultValue + ")";
        List<String> words = Arrays.asList(newHelp.split(" "));
        
        return getFilledParagraph(stringBuilder, words, indent, width);
    }

    @Override
    public int compareTo(OptionWithValue o) {
        OptionValue o1 = (OptionValue) this;
        OptionValue o2 = (OptionValue) o;
        String o1Name = o1.simpleName;
        String o2Name = o2.simpleName;
        return o1Name.compareToIgnoreCase(o2Name);
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() +
                "(name="        + simpleName +
                ", names="      + StringUtil.join(names, ",") +
                ", help="       + help +
                ", field= "     + field.getName() +
                ", nargs= "     + arity +
                ", metavar="    + metavar +
                ", required="   + required +
                ", hidden="     + hidden +
                ", split="      + "\"" + split + "\"" +
                ", handlers={"  + Arrays.stream(handlers)
                                        .map(handler -> handler + "")
                                        .collect(Collectors.joining(",")) + "}" +
                ", type="       + type +
                ", handlers={"  + Arrays.stream(parsers)
                                        .map(parser -> parser + "")
                                        .collect(Collectors.joining(",")) + "}" +
                ")";
    }
    
    // 
    // B U I L D E R
    // 

    /**
     * Builder for this {@link OptionValue}.
     * 
     * @author Ben
     * @version 08/05/2018
     * @since 1.2
     */
    public static final class Builder extends OptionWithValue.Builder<Builder> {
        
        private List<String> names;

        public Builder() {
            super();
            names = null;
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected <O extends OptionWithValue> O build() {
            @SuppressWarnings("unchecked")
            O option = (O) new OptionValue(this);
            return option;
        }

        public Builder addNames(String[] names) {
            List<String> sortedNames = Arrays.asList(names);
            if (names.length > 1)
                Collections.sort(sortedNames, StringUtil.SORT_BY_LENGTH);
            this.names = sortedNames;
            return this;
        }
    }
}
