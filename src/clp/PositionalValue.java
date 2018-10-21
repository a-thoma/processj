package clp;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The class {@link PositionalValue} is a wrapper class that encapsulates
 * operations on a {@link Argument @Argument} annotation. Information
 * about a the annotation and each of its elements can be accessed
 * dynamically through this wrapper class.
 * 
 * @author Ben Cisneros
 * @version 08/05/2018
 * @since 1.2
 */
public final class PositionalValue extends OptionWithValues {

    /**
     * The name of an {@link Argument @Argument} field.
     */
    private final String fieldName;

    /**
     * The position of this argument on the command line.
     */
    private final int order;

    protected PositionalValue(Builder builder) {
        super(builder);
        fieldName = builder.fieldName;
        order = builder.order;
    }

    public String getName() {
        return fieldName;
    }

    public int getOrder() {
        return order;
    }
    
    public String getOptionOrArgumentHelp(int indent, int width) {
        int defaultLength = Formatter.DEFAULT_LENGTH;
        defaultLength += help.length();
        StringBuilder stringBuilder = new StringBuilder(defaultLength);
        stringBuilder.append(" ");
        
        if (metavar.isEmpty())
            stringBuilder.append(" ").append(fieldName);
        else
            stringBuilder.append(" ").append(metavar);
        
        while (indent > stringBuilder.length() + 2)
            stringBuilder.append(" ");
        stringBuilder.append(" ");
        
        int charLeft = width - stringBuilder.length();
        if (help.length() <= charLeft)
            return stringBuilder.append(help).toString();
        
        String newHelp = defaultValue.isEmpty() ? help : help + " (default: " + defaultValue + ")";
        List<String> words = Arrays.asList(newHelp.split(" "));
        int charCount = 0;
        for (Iterator<String> it = words.iterator(); it.hasNext(); ) {
            String word = it.next();
            charCount += word.length() + 1;
            if (charCount > charLeft) {
                stringBuilder.append("\n")
                             .append(StringUtil.countSpaces(indent - 1));
                charCount = word.length() + 1;
            }
            stringBuilder.append(word);
            if (it.hasNext())
                stringBuilder.append(" ");
        }
        
        return stringBuilder.toString();
    }

    @Override
    public int compareTo(OptionWithValues o) {
        PositionalValue p1 = (PositionalValue) this;
        PositionalValue p2 = (PositionalValue) o;
        String p1Name = p1.metavar.isEmpty()? p1.fieldName : p1.metavar;
        String p2Name = p2.metavar.isEmpty()? p2.fieldName : p2.metavar;
        return (p1Name.compareToIgnoreCase(p2Name));
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() +
                "(name="        + fieldName +
                ", order="      + order +
                ", help="       + help +
                ", field= "     + field.getName() +
                ", arity= "     + arity +
                ", metavar="    + metavar +
                ", required="   + required +
                ", hidden="     + hidden +
                ", split="      + "\"" + split + "\"" +
                ", handlers="   + Arrays.stream(handlers)
                                        .map(handler -> handler + "")
                                        .collect(Collectors.joining(",")) +
                ", type="       + type +
                ", handlers={"  + Arrays.stream(parsers)
                                        .map(parser -> parser + "")
                                        .collect(Collectors.joining(",")) + "}" +
                ")";
    }
    
    // =====================
    // B U I L D E R
    // =====================

    /**
     * Builder for this {@link PositionalValue}.
     * 
     * @since 1.2
     * @version 08/05/2018
     * @author Ben Cisneros
     */
    public static final class Builder extends OptionWithValues.Builder<Builder> {

        private String fieldName;
        private int order;

        public Builder() {
            fieldName = null;
            order = 0;
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected <O extends OptionWithValues> O build() {
            @SuppressWarnings("unchecked")
            O argument = (O) new PositionalValue(this);
            return argument;
        }

        public Builder addName(String fieldName) {
            this.fieldName = fieldName;
            return this;
        }

        public Builder addOrder(int order) {
            this.order = order;
            return this;
        }
    }
}
