package clp;

import java.util.Arrays;
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
    private String fieldName;

    /**
     * The position of this argument on the command line.
     */
    private int order;

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
    
    @Override
    public String toString() {
        return getClass().getSimpleName() +
                "(name=" + fieldName +
                ", order=" + order +
                ", help=" + help +
                ", field= " + field.getName() +
                ", arity= " + arity +
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

        public Builder setName(String fieldName) {
            this.fieldName = fieldName;
            return this;
        }

        public Builder setOrder(int order) {
            this.order = order;
            return this;
        }
        
        public Builder setArity(ArityRange arity) {
            this.arity = arity;
            return this;
        }
    }
}
