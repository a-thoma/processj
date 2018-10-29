package clp;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The class {@link PositionalValue} is a wrapper class that encapsulates
 * operations on a {@link Argument @Argument} annotation. Information
 * about a the annotation and each of its elements can be accessed
 * dynamically through this wrapper class.
 * 
 * @author Ben
 * @version 08/05/2018
 * @since 1.2
 */
public final class PositionalValue extends OptionWithValue {

    /**
     * The position of this argument on the command line.
     */
    private final int order;

    protected PositionalValue(Builder builder) {
        super(builder);
        order = builder.order;
    }

    public int getOrder() {
        return order;
    }
    
    public String getOptionOrArgumentHelp(int indent, int width) {
        StringBuilder stringBuilder = new StringBuilder(Formatter.DEFAULT_LENGTH + help.length());
        if (required)
            stringBuilder.append("*");
        else
            stringBuilder.append("  ");
        
        if (metavar.isEmpty())
            stringBuilder.append(" ").append(simpleName);
        else
            stringBuilder.append(" ").append(metavar);
        
        while (indent > stringBuilder.length() + 2)
            stringBuilder.append(" ");
        stringBuilder.append(" ");
        
        int charLeft = width - stringBuilder.length();
        if (help.length() <= charLeft)
            return stringBuilder.append(help).toString();
        
        String newHelp = defaultValue.isEmpty() ? help : help + " (default=" + defaultValue + ")";
        List<String> words = Arrays.asList(newHelp.split(" "));
        
        return getFilledParagraph(stringBuilder, words, indent, width);
    }

    @Override
    public int compareTo(OptionWithValue o) {
        PositionalValue p1 = (PositionalValue) this;
        PositionalValue p2 = (PositionalValue) o;
        String p1Name = p1.metavar.isEmpty()? p1.simpleName : p1.metavar;
        String p2Name = p2.metavar.isEmpty()? p2.simpleName : p2.metavar;
        return p1Name.compareToIgnoreCase(p2Name);
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() +
                "(name="        + simpleName +
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
     * @author Ben
     * @version 08/05/2018
     * @since 1.2
     */
    public static final class Builder extends OptionWithValue.Builder<Builder> {
        
        private int order;

        public Builder() {
            order = 0;
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected <O extends OptionWithValue> O build() {
            @SuppressWarnings("unchecked")
            O argument = (O) new PositionalValue(this);
            return argument;
        }

        public Builder addOrder(int order) {
            this.order = order;
            return this;
        }
    }
}
