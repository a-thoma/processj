package clp;

import java.lang.reflect.Field;

/**
 * The class {@link OptionWithValues} is a wrapper class that serves
 * as the base class for all shared attributes that belong to
 * {@link OptionValue} and {@link PositionalValue}.
 * 
 * @author Ben Cisneros
 * @version 08/20/2018
 * @since 1.2
 */
public abstract class OptionWithValues implements Comparable<OptionWithValues> {
    
    /**
     * The descriptive text messaged used in the help information.
     */
    protected String help;

    /**
     * How many values an option or argument has to consume.
     */
    protected ArityRange arity;

    /**
     * The string used to describe what the value of an option or
     * argument is.
     */
    protected String metavar;

    /**
     * Indicates whether an option or argument is required or not.
     */
    protected boolean required;

    /**
     * Indicates whether an option or argument should be included in
     * the help information or not.
     */
    protected boolean hidden;

    /**
     * The separator between an option or argument and its actual value.
     */
    protected String split;

    /**
     * The handlers used to parse the values for this option or argument.
     */
    @SuppressWarnings("rawtypes")
    protected Class<? extends OptionParser>[] handlers;
    
    /**
     * The instances used to parser the values of a field.
     */
    protected OptionParser<?>[] parsers;
    
    /**
     * Indicates the type of an option.
     */
    protected OptionType type;
    
    /**
     * The annotated-field.
     */
    protected Field field;
    
    /**
     * The default value for this option or argument.
     */
    protected Object value;
    
    public OptionWithValues(Builder<?> builder) {
        help = builder.help;
        metavar = builder.metavar;
        required = builder.required;
        hidden = builder.hidden;
        split = builder.split;
        type = builder.type;
        handlers = builder.handlers;
        field = builder.field;
        parsers = builder.parsers;
        arity = builder.arity;
    }

    // -----------------------------------------------------------------------------
    // SETTERS
    
    public void addValue(Object newValue) {
        value = newValue;
    }

    // -----------------------------------------------------------------------------
    // GETTERS
    
    public final String getHelp() {
        return help;
    }

    public final String getMetavar() {
        return metavar;
    }

    public final boolean isRequired() {
        return required;
    }

    public final boolean isHidden() {
        return hidden;
    }

    public final String getSplit() {
        return split;
    }

    public final boolean isMultiValue() {
        return type == OptionType.MULTIVALUE;
    }
    
    public final boolean isFlagOption() {
        return type == OptionType.NONE;
    }
    
    public final boolean isSingleValue() {
        return type == OptionType.SINGLEVALUE;
    }
    
    @SuppressWarnings("rawtypes")
    public final Class<? extends OptionParser>[] getHandlers() {
        return handlers;
    }
    
    public final OptionParser<?>[] getParsers() {
        return parsers;
    }
    
    public final Object getValue() {
        return value;
    }
    
    public final Field getField() {
        return field;
    }
    
    public final ArityRange getArity() {
        return arity;
    }
    
    public abstract String getOptionOrArgumentHelp(int indent, int width);
    
    /**
     * The class {@link Builder} uses descriptive methods to create options
     * with default or initial values.
     * 
     * @author Ben Cisneros
     * @version 08/20/2018
     * @since 1.2
     *
     * @param <B>
     *            The builder type.
     */
    public static abstract class Builder<B> {
        
        protected String help;
        protected String metavar;
        protected boolean required;
        protected boolean hidden;
        protected String split;
        @SuppressWarnings("rawtypes")
        protected Class<? extends OptionParser>[] handlers;
        protected OptionParser<?>[] parsers;
        protected OptionType type;
        protected Field field;
        protected ArityRange arity;
        
        public Builder() {
            help = null;
            metavar = null;
            required = false;
            hidden = false;
            split = null;
            handlers = null;
            parsers = null;
            type = OptionType.SINGLEVALUE;
            field = null;
            arity = null;
        }
        
        protected abstract B builder();

        protected abstract <O extends OptionWithValues> O build();
        
        public B setHelp(String help) {
            this.help = help;
            return builder();
        }
        
        public B setMetavar(String metavar) {
            this.metavar = metavar;
            return builder();
        }
        
        public B setRequired(boolean required) {
            this.required = required;
            return builder();
        }
        
        public B setHidden(boolean hidden) {
            this.hidden = hidden;
            return builder();
        }
        
        public B setValueSeparator(String split) {
            this.split = split;
            return builder();
        }
        
        public B setHandlers(@SuppressWarnings("rawtypes") Class<? extends OptionParser>[] handlers) {
            this.handlers = handlers;
            return builder();
        }
        
        public B setParsers(OptionParser<?>[] parsers) {
            this.parsers = parsers;
            return builder();
        }
        
        public B setOptionType(OptionType type) {
            this.type = type;
            return builder();
        }
        
        public B setField(Field field) {
            this.field = field;
            return builder();
        }
        
        public B setArity(String arity) {
            this.arity = ArityRange.createArity(arity);
            return builder();
        }
    }
}
