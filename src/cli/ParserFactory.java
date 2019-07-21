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
 * The enum ParserFactory contains a factory of converters and
 * handlers types that are used for parsing values that belong
 * to various command line options.
 *
 * @author Ben
 * @version 06/21/2018
 * @since 1.2
 */
public enum ParserFactory {

    INSTANCE
    ;

    /**
     * Maps all types of OptionParser.
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
     * Returns the class associated with class type, or null
     * if the given type does not map to any hander type.
     * 
     * @param type
     *          A class type that maps to a OptionParser.
     * @return An OptionParser or null if none was found.
     */
    @SuppressWarnings("rawtypes")
    public Class<? extends OptionParser> inferHandlerType(Class<?> type) {
        if (parserMap.get(type) != null)
            return parserMap.get(type).getType();
        
        return null;
    }

    /**
     * Gets the OptionParser instance associated with the given
     * class that belongs to option option.
     *
     * @param classType
     *            The value type which maps to an OptionParser.
     * @param optionName
     *            The name of the option.
     * @return An OptionParser instance or null if none is found.
     */
    @SuppressWarnings("rawtypes")
    public OptionParser<?> getParserTypeForClassType(Class<?> classType, String optionName, Class<? extends OptionParser> handler) {
        if (parserMap.get(classType) == null)
            return null;
        else {
            // First check if we are dealing with an user-defined class.
            Class<?> clazz = findUserDefinedClass(handler);
            if (clazz == null) {
                // No, then add it to the factory.
                addParserTypeForClassType(handler, handler);
                classType = handler;
            }
        }
        // Otherwise, return whatever type the factory has.
        return parserMap.get(classType).getOptionParser(optionName);
    }
    
    /**
     * Gets the handler corresponding to the actual type specified by
     * the user. Return null if the actual type is not found.
     * 
     * @param handler
     *          Some user-defined type.
     * @return The corresponding actual type, or null if not found.
     */
    @SuppressWarnings("rawtypes")
    public Class<?> findUserDefinedClass(Class<? extends OptionParser> handler) {
        for (Class<?> clazz : parserMap.keySet()) {
            if (parserMap.get(clazz).getType().equals(handler))
                return clazz;
        }
        return null;
    }

    /**
     * Registers an instance of type OptionParser associated with
     * the given class type.
     *
     * @throws IllegalArgumentException
     *             When the converter is not a subtype of OptionParser.
     * @param classType
     *            The value type which maps to an OptionParser instance.
     * @param converter
     *            The instance used to parse a value of class type.
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
     * The class ParameterConverter is used to search for and invoke a
     * constructor defined in a class that implements IOptionParser and
     * extends OptionParser. After the constructor is found, it is invoked
     * to create and return an instance of OptionParser. An Exception is
     * thrown when no default constructor is found.
     *
     * @author Ben
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
         * Gets the constructor of the class that implements OptionParser.
         *
         * @throws IllegalArgumentException
         *             When a constructor with a parameter is not defined.
         * @param handlerType
         *            The class type that the constructor belongs to.
         * @return A constructor that belong to converter class.
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
         * Creates and returns an instance of OptionParser.
         *
         * @param optionName
         *            The name of a command line option.
         * @return An instances of OptionParser.
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
