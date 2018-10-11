package clp;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The class {@link OptionValue} is a wrapper class that encapsulates
 * operations on an {@link Option @Option} annotation. Information
 * about an the annotation and each of its elements can be accessed
 * dynamically through this wrapper class.
 * 
 * @author Ben Cisneros
 * @version 08/05/2018
 * @since 1.2
 */
public final class OptionValue extends OptionWithValues {
    
    /**
     * Default (long) name of this option.
     */
    private String name;

    /**
     * The name (or names) of this option.
     */
    private String[] names;

    private OptionValue(Builder builder) {
        super(builder);
        name = builder.name;
        names = builder.names;
    }
    
    public String getName() {
        return name;
    }
    
    public String[] getNames() {
        return names;
    }
    
    public String getOptionHelp(int indent, int width) {
        StringBuilder stringBuilder = new StringBuilder(FormatterHelp.DEFAULT_LENGTH + help.length());
        stringBuilder.append(" ");
        
        Iterator<String> itNames = Arrays.asList(names).iterator();
        while (itNames.hasNext()) {
            stringBuilder.append(itNames.next());
            if (itNames.hasNext())
                stringBuilder.append(",");
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
        
        int charLeft = width - stringBuilder.length();
        if (help.length() <= charLeft)
            return stringBuilder.append(help).toString();
        
        List<String> words = Arrays.asList(help.split(" "));
        int charCount = 0;
        for (Iterator<String> it = words.iterator(); it.hasNext(); ) {
            String word = it.next();
            charCount += word.length() + 1;
            if (charCount > charLeft) {
                stringBuilder.append("\n").append(StringUtil.countSpaces(indent - 1));
                charCount = word.length() + 1;
            }
            stringBuilder.append(word);
            if (it.hasNext())
                stringBuilder.append(" ");
        }
        
        return stringBuilder.toString();
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() +
                "(name=" + name +
                ", names=" + StringUtil.join(Arrays.asList(names), ", ") +
                ", help=" + help +
                ", field= " + field.getName() +
                ", nargs= " + arity +
                ", metavar=" + metavar +
                ", required=" + required +
                ", hidden=" + hidden +
                ", split=" + "\"" + split + "\"" +
                ", handlers=" + Arrays.stream(handlers)
                                      .map(handler -> handler + "")
                                      .collect(Collectors.joining()) +
                ", type=" + type +
                ", handlers=" + Arrays.stream(parsers)
                                      .map(parser -> parser + "")
                                      .collect(Collectors.joining()) +
                ")";
    }

    /**
     * Builder for this {@link OptionValue}.
     * 
     * @author Ben Cisneros
     * @version 08/05/2018
     * @since 1.2
     */
    public static final class Builder extends OptionWithValues.Builder<Builder> {

        private String name;
        private String[] names;

        public Builder() {
            super();
            name = null;
            names = null;
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected <O extends OptionWithValues> O build() {
            @SuppressWarnings("unchecked")
            O option = (O) new OptionValue(this);
            return option;
        }
        
        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setNames(String[] names) {
            this.names = names;
            return this;
        }
        
        public Builder setArity(ArityRange arity) {
            this.arity = arity;
            return this;
        }
    }
}
