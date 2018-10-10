package clp;

import java.util.Arrays;
import java.util.Iterator;
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
        int defaultLength = FormatterHelp.DEFAULT_LENGTH;
        defaultLength += help.length();
        StringBuffer stringBuffer = new StringBuffer(defaultLength);
        stringBuffer.append(" ");
        
        Iterator<String> itNames = Arrays.asList(names).iterator();
        while (itNames.hasNext()) {
            stringBuffer.append(itNames.next());
            if (itNames.hasNext())
                stringBuffer.append(",");
        }
        
        if (metavar.isEmpty())
            stringBuffer.append(" ");
        else if (!split.isEmpty())
            stringBuffer.append("=").append(metavar);
        else
            stringBuffer.append(" ").append(metavar);
        
        while (indent > stringBuffer.length() + 2)
            stringBuffer.append(" ");
        stringBuffer.append(" ");
        
        int descriptionPos = 0;
        int charLeft = width - stringBuffer.length();
        for (int line = 0; descriptionPos < help.length(); ++line) {
            int end = descriptionPos + charLeft;
            if (end > help.length())
                end = help.length();
            else {
                if (help.charAt(end) == ' ')
                    ;
                else if (help.lastIndexOf(' ', end) > descriptionPos)
                    end = help.lastIndexOf(' ', end);
                else if (help.indexOf(' ', end) != -1)
                    end = help.lastIndexOf(' ', end);
                else
                    end = help.length();
            }
            
            if (line != 0)
                stringBuffer.append("\n          ");
            stringBuffer.append(help.substring(descriptionPos, end).trim());
            descriptionPos = end + 1;
            charLeft = width - 10;
        }
        
        return stringBuffer.toString();
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
