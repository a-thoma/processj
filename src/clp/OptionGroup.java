package clp;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import clp.parsers.EnumParser;
import clp.parsers.MapParser;

/**
 * The class {@link OptionGroup} represents a collection of command
 * line options that {@link ClpBuilder} instantiates in order to
 * updated the values that {@link Option @Option} fields and
 * {@link Argument @Argument} fields hold at compile or runtime.
 * 
 * @author Ben
 * @version 06/21/2018
 * @since 1.2
 */
public final class OptionGroup {
    
    /**
     * Map of {@link Parameters @Parameters} types to their constructors.
     */
    private Map<Class<? extends Command>, Constructor<? extends Command>> constructorsMap = new HashMap<>();
    
    /**
     * Map of option names to {@link Option @Option} fields.
     */
    private SortedMap<String, OptionValue> namedOptionMap = new TreeMap<>();
    
    /**
     * Map of fields to {@link OptionValue}s and {@link PositionalValue}s.
     */
    private Map<Field, OptionWithValue> fieldOptionMap = new HashMap<>();
    
    /**
     * This map contains a collection of fields declared by a class or set of classes.
     */
    private Map<Class<? extends Command>, List<Field>> classFieldMap = new HashMap<>();

    /**
     * List of {@link Argument @Argument} fields.
     */
    private List<PositionalValue> argumentList = new ArrayList<>();
    
    public OptionGroup(Set<Class<? extends Command>> setClass) {
        setClass = Assert.nonNull(setClass, "Set of classes cannot be null.");
        create(setClass);
    }

    public OptionValue getOption(String optName) {
        return namedOptionMap.get(optName);
    }
    
    public OptionWithValue getOptionOrArgument(Field field) {
        return fieldOptionMap.get(field);
    }

    public Collection<OptionValue> getOptionValues() {
        return namedOptionMap.values();
    }
    
    public Collection<String> getOptionNames() {
        return namedOptionMap.keySet();
    }
    
    public List<PositionalValue> getArguments() {
        return argumentList;
    }
    
    public Collection<Field> getFieldList(Class<? extends Command> type) {
        return classFieldMap.get(type);
    }
    
    public Set<OptionValue> getUniqueOptions() {
        SortedSet<OptionValue> optSet = new TreeSet<>();
        for (OptionValue optionValue : namedOptionMap.values())
            optSet.add(optionValue);
        return optSet;
    }
    
    public <T extends Command> Constructor<T> getConstructor(Class<T> type) {
        @SuppressWarnings("unchecked")
        Constructor<T> result = (Constructor<T>) constructorsMap.get(type);
        return result;
    }
    
    public void create(Set<Class<? extends Command>> setClass) {
        // Loop over all the class hierarchy if any
        for (Class<? extends Command> type : setClass) {
            // Parse all annotated fields
            List<Field> fields = findAnnotatedFields(type);
            for (Field field : fields) {
                // Multiple annotations attached to a field is not allowed
                if (field.getAnnotations().length > 1) {
                    throw new RuntimeException(String.format("More than one annotation is "
                                + "attached to '%s'. Only one annotation per field is allowed.",
                                field.getName()));
                }
                
                if (isOption(field)) {
                    OptionValue option = buildOptionValue(field);
                    if (fieldOptionMap.put(field, option) != null)
                        throw new RuntimeException(String.format("Field '%s' was found multiple times.",
                                    field.getName()));
                    
                    for (String name : option.getNames()) {
                        if (namedOptionMap.put(name, option) != null)
                            throw new RuntimeException(String.format("@Option '%s' was found multiple "
                                        + "times.", name));
                    }
                } else {
                    // Arguments are added in any other, they are sorted later
                    PositionalValue argument = buildOptionArgument(field);
                    if (fieldOptionMap.put(field, argument) != null)
                        throw new RuntimeException(String.format("Field '%s' was found multiple times.",
                                    field.getName()));
                    
                    argumentList.add(argument);
                }
            }
            
            constructorsMap.put(type, findConstructor(type));
            classFieldMap.put(type, fields);
        }
        
        Collections.sort(argumentList, ARGUMENT_COMPARATOR);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private OptionValue buildOptionValue(Field field) {
        OptionValue.Builder builder = new OptionValue.Builder();
        Option annotation = field.getAnnotation(Option.class);
        
        if (Util.isArrayEmpty(annotation.names()))
            throw new RuntimeException(String.format("The \"names\" attribute for @Option field "
                        + "'%s' cannot be empty.", field.getName()));
        
        for (String name : annotation.names()) {
            if (!name.startsWith("-"))
                throw new RuntimeException(String.format("Annotated options must start with a single "
                            + "\"-\". Found @Option '%s'.", name));
        }
        
        String optName = findOptionLongName(field);
        builder.addSimpleName(optName);
        builder.addNames(annotation.names());
        builder.addField(field);
        
        if (annotation.hidden() && annotation.required()) {
            // Required arguments cannot be hidden, they should always be provided
            throw new RuntimeException(String.format("A required @Option or @Argument attached "
                        + "to '%s' cannot be hidden.", field.getName()));
        }
        
        builder.addHidden(annotation.hidden());
        builder.addRequired(annotation.required());
        
        // This is to retrieve information about the type of data each field
        // holds at compile-time or run-time. Basic types such as byte, short,
        // int, long, float, double, String and parameterized fields (of basic
        // types) can be inferred. Complex data types such as user-defined
        // types must be explicitly specified
        Class<?>[] fieldFinalTypes = findFieldFinalTypes(field);
        if (fieldFinalTypes.length == 2) {
            // Must be a field of type Map
            List<Class<? extends OptionParser>> handlerList = new ArrayList<>();
            if (Util.isArrayEmpty(annotation.handlers())) {
                // No handler type was provided, then find two handlers compatible
                // with the annotated field's type variables
                handlerList.add(ParserFactory.INSTANCE.inferHandlerType(fieldFinalTypes[0]));
                handlerList.add(ParserFactory.INSTANCE.inferHandlerType(fieldFinalTypes[1]));
            } else {
                if (annotation.handlers().length != 2)
                    throw new RuntimeException(String.format("Insufficient handlers assigned or more than "
                                + "one handler was assigned to Multivalue-Map @Option '%s'.", optName));
                
                handlerList.add(annotation.handlers()[0]);
                handlerList.add(annotation.handlers()[1]);
            }
            
            OptionParser<?> keyParser = findFieldTypeParser(field.getName(), fieldFinalTypes[0],
                                        handlerList.get(0), optName);
            OptionParser<?> valueParser = findFieldTypeParser(field.getName(), fieldFinalTypes[1],
                                        handlerList.get(1), optName);
            
            // Throw an exception if an instance of OptionParser could not be create
            // from either handler type
            if (keyParser == null || valueParser == null)
                throw new RuntimeException(String.format("A parser could not be created for '%s'.", optName));
            
            builder.addParsers(new OptionParser<?>[] { keyParser, valueParser });
            builder.addHandlers(handlerList.toArray(new Class[0]));
        } else {
            // Must be some atomic or user-defined type
            Class<? extends OptionParser> handler = null;
            if (Util.isArrayEmpty(annotation.handlers())) {
                // No handler type was provided, find one compatible with the annotated
                // field's type variable
                handler = ParserFactory.INSTANCE.inferHandlerType(fieldFinalTypes[0]);
            } else {
                // Otherwise, throw an error if more than one handler was provided
                if (annotation.handlers().length != 1)
                    throw new RuntimeException(String.format("More than one handler was assigned to "
                                + "@Option '%s'.", optName));
                handler = annotation.handlers()[0];
            }
            OptionParser<?> parser = findFieldTypeParser(field.getName(), fieldFinalTypes[0],
                                     handler, optName);
            
            // Throw an exception if an instance of OptionParser could not be created
            // from the specified handler type
            if (parser == null)
                throw new RuntimeException(String.format("A parser could not be created for '%s'.", optName));
            
            builder.addParsers(new OptionParser<?>[] { parser });
            builder.addHandlers(new Class[] { handler });
        }
        
        ArityRange arity = null;
        if (StringUtil.isStringEmpty(annotation.arity())) {
            if (isBooleanField(field.getType()) && StringUtil.isStringEmpty(annotation.split()))
                arity = ArityRange.createArity("0");
            else
                arity = ArityRange.createArity("1");
        } else {
            arity = ArityRange.createArity(annotation.arity());
            if (arity.getFrom() > 1 && !isCollectionOrMapOrArrayField(field.getType())) {
                // Options that appear multiple times must be attached to a parameterized List
                throw new RuntimeException(String.format("Multivalue @Option '%s' must be assigned "
                            + "to a field of type List.", optName));
            }
        }
        
        builder.addArity(arity);

        OptionType type = OptionType.SINGLEVALUE;
        if (isCollectionOrMapOrArrayField(field.getType()))
            type = OptionType.MULTIVALUE;
        else if (isBooleanField(field.getType()) && arity.hasFixedArity() && (arity.getFrom() == 0))
            type = OptionType.NONE;
        
        builder.addOptionType(type);

        // Build remaining attributes values
        builder.addHelp(annotation.help());
        builder.addValueSeparator(annotation.split());
        builder.addMetavar(annotation.metavar());
        builder.addDefaultValue(annotation.defaultValue());
        
        // Build option and parse the value assigned to `defaultValue'
        OptionValue option = builder.build();
        if (!StringUtil.isStringEmpty(annotation.defaultValue()))
            addValue(option, annotation.defaultValue());

        return option;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private PositionalValue buildOptionArgument(Field field) {
        PositionalValue.Builder builder = new PositionalValue.Builder();
        Argument annotation = field.getAnnotation(Argument.class);
        String fieldName = field.getName();
        builder.addSimpleName(fieldName);
        builder.addField(field);
        
        ArityRange order = ArityRange.createArity(annotation.order());
        if (!order.hasFixedArity() && !isCollectionOrMapOrArrayField(field.getType()))
            throw new RuntimeException(String.format("Multivalue @Argument '%s' must be assigned "
                    + "to a field of type List.", fieldName));
        
        builder.addArity(order);
        
        // Same as `Options' except that arguments cannot be of type Map.
        // Only one type should be returned and one parser instance should
        // be created. Complex data types such as user-defined types must
        // be explicitly specified
        Class<?>[] fieldFinalTypes = findFieldFinalTypes(field);
        if (fieldFinalTypes.length == 2) {
            throw new RuntimeException(String.format("@Argument '%s' cannot have more than one handler.",
                        fieldName));
        } else {
            Class<? extends OptionParser> handler = null;
            if (annotation.handler() == OptionParser.class)
                // No handler type was provided, find one compatible with the
                // annotated field type
                handler = ParserFactory.INSTANCE.inferHandlerType(fieldFinalTypes[0]);
            else
                handler = annotation.handler();
            OptionParser<?> parser = findFieldTypeParser(field.getName(), fieldFinalTypes[0],
                                     handler, fieldName);
            
            // Throw exception if an instance of OptionParser could not be created
            // from the specified handler type
            if (parser == null)
                throw new RuntimeException(String.format("A parser could not be created for '%s'.", fieldName));
            
            builder.addParsers(new OptionParser<?>[] { parser });
            builder.addHandlers(new Class[] { handler });
        }
        
        OptionType type = OptionType.SINGLEVALUE;
        if (isCollectionOrMapOrArrayField(field.getType()))
            type = OptionType.MULTIVALUE;
        
        builder.addOptionType(type);

        // Build remaining attributes values
        builder.addHelp(annotation.help());
        builder.addHidden(annotation.hidden());
        builder.addRequired(annotation.required());
        builder.addValueSeparator(annotation.split());
        builder.addMetavar(annotation.metavar());
        builder.addDefaultValue(annotation.defaultValue());
        
        // Build argument and parse the value assigned to `defaultValue'
        PositionalValue argument = builder.build();
        if (!StringUtil.isStringEmpty(annotation.defaultValue()))
            addValue(argument, annotation.defaultValue());

        return argument;
    }

    public void addValue(OptionWithValue option, String value) {
        Class<?> type = option.getField().getType();
        if (option.isFlagOption()) {
            addBooleanValue(option, value);
        } else if (option.isMultiValue()) {
            if (isArrayField(type))
                addArrayValue(option, value);
            else if (isMapField(type))
                addMapValue(option, value);
            else
                addListValue(option, value);
        } else {
            // Single value option
            addValueType(option, value);
        }
    }

    private void addBooleanValue(OptionWithValue option, String value) {
        try {
            // If `null' then this could be a stand alone option, e.g. a flag
            // option that doesn't consume values
            if (value == null) {
                // Check the value of this field and flip its value if the field
                // was initialize. E.g. if `true' change it to `false', if `false'
                // change it to `true'
                Boolean oldValue = (Boolean) option.getValue();
                if (oldValue != null)
                    option.addValue(oldValue ? Boolean.FALSE : Boolean.TRUE);
                else
                    // No, then it must be null so set it to `true'
                    option.addValue(Boolean.TRUE);
            } else {
                // Parse the given boolean value
                OptionParser<?> parser = option.getParsers()[0];
                Boolean newValue = (Boolean) parser.parseValue(value);
                option.addValue(newValue);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void addListValue(OptionWithValue option, String value) {
        try {
            OptionParser<?> parser = option.getParsers()[0];
            Object parsedValue = parser.parseValue(value);
            // Grab the List and check if it was initialized to avoid object creation
            Collection<Object> collection = (Collection<Object>) option.getValue();
            if (collection == null)
                // No, then create an ArrayList
                collection = Util.createCollectionInstance(option.getField().getType());
            // If the parsed value is a collection then add all of it
            if (isCollectionOrMapOrArrayField(parsedValue.getClass())) {
                @SuppressWarnings("rawtypes")
                Collection parsedResult = (Collection) parsedValue;
                collection.addAll(parsedResult);
            } else {
                // No, then it is a single value
                collection.add(parsedValue);
            }
            option.addValue(collection);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    private void addArrayValue(OptionWithValue option, String value) {
        try {
            OptionParser<?> parser = option.getParsers()[0];
            Object parsedValue = parser.parseValue(value);
            // Grab the array and check if it was initialized to avoid
            // object creation
            Object objArray = option.getValue();
            if (objArray == null) {
                // No, then create a new array of size 1
                objArray = Array.newInstance(option.getField().getType().getComponentType(), 1);
                Array.set(objArray, 0, parsedValue);
            } else {
                // Because arrays cannot be resized, an array of `length + 1' size
                // is created before the values are copied into this new array
                int length = Array.getLength(objArray);
                Object newObjArray = Array.newInstance(objArray.getClass().getComponentType(), length + 1);
                System.arraycopy(objArray, 0, newObjArray, 0, length);
                Array.set(newObjArray, length, parsedValue);
                objArray = newObjArray;
            }
            option.addValue(objArray);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    private void addMapValue(OptionWithValue option, String keyAndValue) {
        try {
            String[] splitKeyValue = MapParser.PARSER.parseValue(keyAndValue);
            OptionParser<?> keyParser = option.getParsers()[0];
            OptionParser<?> valueParser = option.getParsers()[1];
            Object keyMap = keyParser.parseValue(splitKeyValue[0]);
            Object valueMap = valueParser.parseValue(splitKeyValue[1]);
            // Grab the map and check if it was initialized to avoid object creation
            @SuppressWarnings("unchecked")
            Map<Object, Object> objMap = (Map<Object, Object>) option.getValue();
            if (objMap == null)
                objMap = new HashMap<>();
            // NOTE: This will override previous values for keys that occurred
            // multiple times
            objMap.put(keyMap, valueMap);
            option.addValue(objMap);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void addValueType(OptionWithValue option, String value) {
        try {
            OptionParser<?> parser = option.getParsers()[0];
            Object parsedValue = parser.parseValue(value);
            option.addValue(parsedValue);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private OptionParser<?> findFieldTypeParser(String fieldName, Class<?> fieldType,
                                                Class<? extends OptionParser> handler,
                                                String optName) {
        if (OptionParser.class == handler)
            throw new RuntimeException(String.format("Illegal handler type found! Illegal '%s' type "
                        + "assigned to field '%s'.", Util.getTypeName(handler), fieldName));
        
        // Is this handler an Enum type? Yes, then create a parser for this Enum type
        if (handler == null && Enum.class.isAssignableFrom(fieldType))
            return new EnumParser(optName, fieldType);
        // No, then attempt to create a parse from the given handler or return
        // `null' (for exception handling) if we fail to create a parser
        OptionParser<?> parser = ParserFactory.INSTANCE.getParserTypeForClassType(fieldType, optName);
        if (parser == null) {
            try {
                ParserFactory.INSTANCE.addParserTypeForClassType(fieldType, handler);
                parser = ParserFactory.INSTANCE.getParserTypeForClassType(fieldType, optName);
            } catch (Exception e) {
                return null;
            }
        }

        return parser;
    }
    
    private Class<?>[] findFieldFinalTypes(Field field) {
        Class<?>[] fieldTypeResult = null;
        Type fieldType = field.getGenericType();
        if (fieldType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) fieldType;
            if (parameterizedType.getRawType() == List.class || parameterizedType.getRawType() == Map.class) {
                // Retrieve the type(s) declared in a List or Map
                fieldTypeResult = findFieldGenericTypes(field);
            } else {
                throw new RuntimeException(String.format("Unknow type for a multivalue field. Found "
                        + "type '%s' for '%s'.", fieldType, field.getName()));
            }
        } else if (field.getType().isArray()) {
            fieldTypeResult = new Class<?>[] { field.getType() };
        } else {
            // Could be an atomic or user-defined type
            fieldTypeResult = new Class<?>[] { (Class<?>) fieldType };
        }
        return fieldTypeResult;
    }
    
    private Class<?>[] findFieldGenericTypes(Field field) {
        // This will fail if the field's data type is not a ParameterizedType
        // such as a List or a Map type
        ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
        Type[] paramTypes = parameterizedType.getActualTypeArguments();
        Class<?>[] paramTypesResult = new Class<?>[paramTypes.length];
        for (int i = 0; i < paramTypes.length; ++i) {
            if (paramTypes[i] instanceof Class) {
                paramTypesResult[i] = (Class<?>) paramTypes[i];
            } else if (paramTypes[i] instanceof WildcardType) {
                WildcardType wildcardType = (WildcardType) paramTypes[i];
                Type[] lowerBound = wildcardType.getLowerBounds();
                if (lowerBound.length > 0 && lowerBound[0] instanceof Class)
                    paramTypesResult[i] = (Class<?>) lowerBound[0];
                
                Type[] upperBound = wildcardType.getUpperBounds();
                if (upperBound.length > 0 && upperBound[0] instanceof Class)
                    paramTypesResult[i] = (Class<?>) upperBound[0];
            }
        }
        return paramTypesResult;
    }
    
    protected static String findOptionLongName(Field field) {
        Option annotation = field.getAnnotation(Option.class);
        String name = "";
        for (String n : annotation.names())
            if (n.length() > name.length())
                name = n;
        return name;
    }
    
    protected static List<Field> findAnnotatedFields(Class<? extends Command> type) {
        List<Field> fields = new ArrayList<>();
        for (Field field : type.getDeclaredFields())
            if (isOption(field) || isArgument(field))
                fields.add(field);
        
        if (fields.isEmpty())
            throw new RuntimeException("At least one field must have an annotation "
                        + "attached to it.");
        return fields;
    }

    protected static Field setFieldAccess(Field field) {
        if (Modifier.isFinal(field.getModifiers()))
            throw new RuntimeException(String.format("Final (constant) field '%s' cannot "
                        + "be modified.", field.getName()));
        
        field.setAccessible(true);
        return field;
    }
    
    protected static Constructor<? extends Command> findConstructor(Class<? extends Command> type) {
        try {
            // This will fail if the extended class has no default constructor.
            // One must ALWAYS be provided!
            return type.getConstructor(new Class[0]);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalArgumentException(String.format("Missing default constructor in class '%s'.",
                            Util.getTypeName(type.getClass())));
        }
    }

    protected static boolean isAnnotation(Object obj, Class<? extends Annotation> type) {
        obj = Assert.nonNull(obj, "Annotated object cannot be null.");
        if (obj instanceof AccessibleObject)
            return ((AccessibleObject) obj).isAnnotationPresent(type);
        else if (obj instanceof Class)
            return ((Class<?>) obj).isAnnotationPresent(type);
        
        return obj.getClass().isAnnotationPresent(type);
    }
    
    protected static boolean isParameters(Object obj) {
        return isAnnotation(obj, Parameters.class);
    }
    
    protected static boolean isOption(Object obj) {
        return isAnnotation(obj, Option.class);
    }
    
    protected static boolean isArgument(Object obj) {
        return isAnnotation(obj, Argument.class);
    }
    
    protected static boolean isBooleanField(Class<?> type) {
        return type.equals(Boolean.class) || type.equals(boolean.class);
    }
    
    protected static boolean isArrayField(Class<?> type) {
        return type.isArray();
    }
    
    protected static boolean isMapField(Class<?> type) {
        return Map.class.isAssignableFrom(type);
    }
    
    protected static boolean isCollectionOrMapOrArrayField(Class<?> type) {
        return Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type) || type.isArray();
    }
    
    /**
     * Compares {@link Argument @Argument} fields by their index or,
     * id their indices have the same values, by {@link Argument#order()}.
     * 
     * @author Ben
     * @version 08/16/2018
     * @since 1.2
     */
    private static final class ArgumentComparator implements Comparator<PositionalValue> {
        
        @Override
        public int compare(PositionalValue arg1, PositionalValue arg2) {
            int result = arg1.getArity().getFrom() - arg2.getArity().getFrom();
            if (result == 0)
                result = arg1.getArity().getTo() - arg2.getArity().getTo();
            return result;
        }
    }
    
    private static final ArgumentComparator ARGUMENT_COMPARATOR = new ArgumentComparator();
}
