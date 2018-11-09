package cli;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import cli.parsers.*;

/**
 * The enum {@link ParserFactory} contains a factory of converters
 * and handlers types that are used for parsing values that belong
 * to various command line options.
 * <p>
 * This factory provides the following methods:
 * </p>
 *
 * <ul>
 * <li>{@link #getParserTypeForClassType(Class, String)}</li>
 * <li>{@link #addParserTypeForClassType(Class, Class)}</li>
 * </ul>
 *
 * @author Ben
 * @version 06/21/2018
 * @since 1.2
 */
public enum ParserFactory {

    INSTANCE
    ;

    /**
     * Maps all types of {@link OptionParser}.
     */
    private final Map<Class<?>, ParameterConverter> parserMap;

    ParserFactory() {
        parserMap = new HashMap<>(20);
        initParsers();
    }

    private void initParsers() {
        addParserTypeForClassType(Boolean.class, BooleanParser.class);
        addParserTypeForClassType(Boolean.TYPE, BooleanParser.class);
        addParserTypeForClassType(BigDecimal.class, BigDecimalParser.class);
        addParserTypeForClassType(Byte.class, ByteParser.class);
        addParserTypeForClassType(Byte.TYPE, ByteParser.class);
        addParserTypeForClassType(Character.class, CharacterParser.class);
        addParserTypeForClassType(Character.TYPE, CharacterParser.class);
        addParserTypeForClassType(Integer.class, IntegerParser.class);
        addParserTypeForClassType(Integer.TYPE, IntegerParser.class);
        addParserTypeForClassType(Long.class, LongParser.class);
        addParserTypeForClassType(Long.TYPE, LongParser.class);
        addParserTypeForClassType(String.class, StringParser.class);
        addParserTypeForClassType(Float.class, FloatParser.class);
        addParserTypeForClassType(Float.TYPE, FloatParser.class);
        addParserTypeForClassType(Double.class, DoubleParser.class);
        addParserTypeForClassType(Double.TYPE, DoubleParser.class);
        addParserTypeForClassType(Short.class, ShortParser.class);
        addParserTypeForClassType(Short.TYPE, ShortParser.class);
        addParserTypeForClassType(File.class, FileParser.class);
        addParserTypeForClassType(URL.class, URLParser.class);
    }
    
    /**
     * Returns the class associated with class {@code type}, or {@code null}
     * if the given {@code type} does not map to any hander type.
     * 
     * @param type
     *          A class type that maps to a {@link OptionParser}.
     * @return An {@OptionParser} or {@code null} if none was found.
     */
    @SuppressWarnings("rawtypes")
    public Class<? extends OptionParser> inferHandlerType(Class<?> type) {
        if (parserMap.get(type) != null)
            return parserMap.get(type).getType();
        
        return null;
    }

    /**
     * Gets the {@link OptionParser} instance associated with the {@code classType}
     * that belongs to option {@code optionName}.
     *
     * @param classType
     *            The value type which maps to an {@link OptionParser}.
     * @param optionName
     *            The name of the option.
     * @return An {@link OptionParser} instance or {@code null} if none is found.
     */
    public OptionParser<?> getParserTypeForClassType(Class<?> classType, String optionName) {
        if (parserMap.get(classType) == null)
            return null;
        
        return parserMap.get(classType).getOptionParser(optionName);
    }

    /**
     * Registers an instance of type {@link OptionParser} associated with
     * {@code classType.}
     *
     * @throws IllegalArgumentException
     *             When the {@code converter} is not a subtype of {@link OptionParser}.
     * @param classType
     *            The value type which maps to an {@link OptionParser} instance.
     * @param converter
     *            The instance used to parse a value of type {@code classType}.
     */
    public void addParserTypeForClassType(Class<?> classType,
            @SuppressWarnings("rawtypes") Class<? extends OptionParser> converter) {
        if (OptionParser.class == converter) {
            throw new IllegalArgumentException(String.format("The class \"%s\" is not the same "
                        + "as or not a subtype of \"%s\".", Util.getTypeName(converter),
                        Util.getTypeName(OptionParser.class)));
        }

        parserMap.put(classType, new ParameterConverter(converter));
    }

    /**
     * The class {@link ParameterConverter} is used to search for and
     * invoke a constructor defined in a class that implements
     * {@link IOptionParser} and extends {@link OptionParser}. After
     * the constructor is found, it is invoked to create and return
     * an instance of {@link OptionParser}. An {@link Exception} is
     * thrown when no default constructor is found.
     *
     * @author Ben Cisneros
     * @version 06/21/2018
     * @since 1.2
     */
    private final class ParameterConverter {

        @SuppressWarnings("rawtypes")
        private final Constructor<? extends OptionParser> constructor;
        
        @SuppressWarnings("rawtypes")
        private Class<? extends OptionParser> type;

        public ParameterConverter(@SuppressWarnings("rawtypes") Class<? extends OptionParser> handlerType) {
            type = handlerType;
            constructor = findConstructor(handlerType);
        }

        /**
         * Gets the constructor of the class that implements {@link OptionParser}.
         *
         * @throws IllegalArgumentException
         *             When a constructor with a parameter {@code String} is not
         *             defined.
         * @param converter
         *            The class type that the constructor belongs to.
         * @return A constructor that belong to class {@code converter}.
         */
        @SuppressWarnings("rawtypes")
        private Constructor<? extends OptionParser> findConstructor(Class<? extends OptionParser> handlerType) {
            try {
                return handlerType.getConstructor(String.class);
            } catch (NoSuchMethodException | SecurityException e) {
                throw new IllegalArgumentException(String.format("No constructor found for \"%s\".",
                            Util.getTypeName(handlerType.getClass())));
            }
        }

        /**
         * Creates and returns an instance of {@link OptionParser}.
         *
         * @param optionName
         *            The name of a command line option.
         * @return An instances of {@link OptionParser}.
         */
        public OptionParser<?> getOptionParser(String optionName) {
            try {
                return constructor.newInstance(optionName);
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
        
        /**
         * Returns the class in which the constructor is defined.
         * 
         * @return The class type that the constructor belongs to.
         */
        @SuppressWarnings("rawtypes")
        public Class<? extends OptionParser> getType() {
            return type;
        }
    }
}
