package clp;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import utilities.MultiValueMap;

/**
 * @author Ben Cisneros
 * @version 07/21/2018
 * @since 1.2
 */
public class OptionBuilder {
    
    /**
     * The first ever registered command is the parent command.
     */
    Class<? extends Command> mainCommand;
    
    /**
     * Map of names to command types.
     */
    private Map<String, Class<? extends Command>> namedAndCommandMap = new HashMap<>();
    
    /**
     * Map of command types to option group.
     */
    private Map<Class<? extends Command>, OptionGroup> commandAndOptionMap = new HashMap<>();
    
    /**
     * Map of names to option values.
     */
    private Map<String, OptionValue> requiredOptionMap = new HashMap<>();
    
    /**
     * Map of invoked command types.
     */
    List<Class<? extends Command>> invokedCommandList = new ArrayList<>();
    
    /**
     * Collections of shared-options.
     */
    private Options options = new Options();
    
    public OptionBuilder() {
        // Nothing to do
    }
    
    public OptionBuilder handlerArgs(String[] args) {
        handleArgs(expandArgs(args), mainCommand, 0, new ArrayList<>());
        return this;
    }
    
    private void handleArgs(String[] args,
                            Class<? extends Command> type,
                            int currentIndex,
                            List<String> positionArgs) {
        int index = currentIndex;
        // Collect invoked commands - this is to validate required (individual)
        // options
        invokedCommandList.add(type);
        // Options that belong to an invoked command type
        OptionGroup optGroup = commandAndOptionMap.get(type);
        // Indicates a chain of command type invocations
        boolean subParameters = false;
        // Loop over all of the arguments
        while (index < args.length) {
            String argument = args[index];
            // Discard empty arguments
            if (StringUtil.isStringEmpty(argument))
                continue;
            if ("--".equals(argument)) {
                // Throw an error if there are missing optional arguments
                if (++index >= args.length)
                    throw new RuntimeException("Missing positional arguments after '--'.");
                // Consume everything that follows the bare double dash
                while (index < args.length) {
                    positionArgs.add(args[index]);
                    ++index;
                }
            } else if (isOption(argument)) {
                OptionValue optionValue = optGroup.getOption(argument);
                // Throw an error if the option does not belong to the invoked command type
                if (optionValue == null)
                    throw new RuntimeException(String.format("Unknown @Option '%s' for @Parameters '%s'.",
                                argument, findCommandName(namedAndCommandMap, type)));
                index = parseOption(optGroup, optionValue, index, args);
            } else if (isCommand(argument)) {
                subParameters = true;
                break;
            } else if (argument.startsWith("-")) {
                List<String> maybeList = startWithOptionName(argument);
                throw new RuntimeException(String.format("Unknown @Option '%s' for @Parameters '%s'. "
                            + "Did you mean to say?\n%s", argument, findCommandName(namedAndCommandMap, type),
                            String.join("\n", maybeList)));
            } else {
                // Throw an error if the running command takes no arguments
                if (optGroup.getArguments().size() == 0)
                    throw new RuntimeException(String.format("@Parameters '%s' takes zero arguments.",
                                findCommandName(namedAndCommandMap, type)));
                // Unparsed values are treated as positional arguments
                positionArgs.add(argument);
                ++index;
            }
        }
        
        if (!positionArgs.isEmpty())
            // Parse positional arguments if any
            parseArgument(optGroup, positionArgs);
        
        if (subParameters)
            // Sub-commands are ALWAYS invoked last
            handleArgs(args, namedAndCommandMap.get(args[index]), index + 1, new ArrayList<>());
        
        // Validate required command line options
        validateRequiredOptions();
    }
    
    private int parseOption(OptionGroup optGroup,
                            OptionValue option,
                            int index,
                            String[] args) {
        if (option.isFlagOption()) {
            optGroup.addValue(option, null);
            ++index;
        } else {
            ArityRange arity = option.getArity();
            // The minimum number of values to consume
            int endIndex = arity.getFrom();
            // Number of consumed values
            int consumedArgs = 0;
            // Indicates when a value could not be parsed
            boolean consumedValue = false;
            // Current argument on the command line
            index += 1;
            int j = 0;
            if (option.isSingleValue()) {
                // Throw error if there are missing arguments
                if (index >= args.length)
                    throw new RuntimeException(String.format("@Option '%s' requires at least %d value.",
                            option.getName(), arity.getFrom()));
                // Consume single value
                optGroup.addValue(option, args[index++]);
            } else {
                // Consume the minimum (and required) number of values
                while (j < endIndex && index < args.length && !consumedValue) {
                    if (isOption(args[index]))
                        break;
                    try {
                        optGroup.addValue(option, args[index++]);
                        ++consumedArgs;
                        ++j;
                    } catch (Exception e) {
                        consumedValue = true;
                    }
                }
                // Have we consumed all required values?
                if (j < endIndex)
                    throw new RuntimeException(String.format("@Option '%s' requires at least %d value(s), "
                                + "only %d value(s) consumed.", option.getName(), arity.getFrom(), consumedArgs));
                
                // Consume remaining values if any is available and can be parsed
                while (j < arity.getTo() && index < args.length && !consumedValue) {
                    if (isOption(args[index]))
                        break;
                    try {
                        optGroup.addValue(option, args[index++]);
                        ++consumedArgs;
                        ++j;
                    } catch (Exception e) {
                        consumedValue = true;
                    }
                }
            }
        }
        // Remove required option after successfully parsing its value
        requiredOptionMap.remove(option.getName());
        
        return index;
    }
    
    private void parseArgument(OptionGroup optGroup,
                               List<String> argList) {
        // Current position of an argument
        int index = 0;
        for (PositionalValue argument : optGroup.getArguments()) {
            ArityRange order = argument.getArity();
            // The minimum number of values to consume
            int getFrom = order.getFrom();
            // The maximum number of values to consume
            int getTo = order.getTo();
            // Number of consumed values
            int consumedArgs = 0;
            // Indicates when a value could not be parsed
            boolean consumedValue = false;
            
            if (argument.isSingleValue()) {
                optGroup.addValue(argument, argList.get(index++));
            } else {
                while (getFrom <= getTo && !consumedValue) {
                    try {
                        optGroup.addValue(argument, argList.get(index++));
                        ++consumedArgs;
                        ++getFrom;
                    } catch (Exception e) {
                        getFrom = getTo != Integer.MAX_VALUE ? getFrom - 1 : getFrom;
                        consumedValue = true;
                    }
                }
                // Have we consumed all required values?
                if (getFrom < index && getTo != Integer.MAX_VALUE)
                    throw new RuntimeException(String.format("@Argument '%s' requires %s value(s), "
                                + "only %d value(s) consumed.", argument.getName(), order, consumedArgs));
            }
        }
        
        if (argList.size() - index > 0)
            throw new RuntimeException(String.format("%d arguments were parsed. Found %d extra "
                        + "arguments.", index, argList.size() - index));
    }
    
    public OptionBuilder addCommand(Class<? extends Command> type) {
        type = Assert.nonNull(type, "The specified class cannot be null.");
        Parameters parameters = type.getAnnotation(Parameters.class);
        
        if (parameters == null || StringUtil.isStringEmpty(parameters.name()))
            throw new RuntimeException(String.format("@Parameters annotation is either not attached to '%s' "
                        + "or its 'name' attribute is not defined.", Util.getTypeName(type)));
        
        // Set main (parent) command
        if (mainCommand == null)
            mainCommand = type;

        // Register command type by name
        String paramsName = parameters.name();
        if (namedAndCommandMap.put(paramsName, type) != null)
            throw new RuntimeException(String.format("Name '%s' with possible duplicate @Parameters "
                        + "have been found.", paramsName));
        
        // Associate options by @Parameter type
        OptionGroup optGroup = new OptionGroup(findAllExtendedClasses(type));
        if (commandAndOptionMap.put(type, optGroup) != null)
            throw new RuntimeException(String.format("Class '%s' with possible duplicate values "
                        + "have been found.", Util.getTypeName(type)));
        
        // Register all options that belong to this command
        for (String optName : optGroup.getOptionNames()) {
            try {
                OptionValue optionValue = optGroup.getOption(optName);
                options.add(optName, optionValue);
                if (optionValue.isRequired())
                    // Keep track of every required @Option
                    requiredOptionMap.put(optionValue.getName(), optionValue);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }
        
        return this;
    }
    
    public Map<String, Class<? extends Command>> getNamedAndCommandMap() {
        return namedAndCommandMap;
    }
    
    public Map<Class<? extends Command>, OptionGroup> getCommandAndOptionMap() {
        return commandAndOptionMap;
    }
    
    public Options getSharedOptions() {
        return options;
    }
    
    public Class<? extends Command> getMainCommand() {
        return mainCommand;
    }
    
    private void validateRequiredOptions() {
        List<String> optNames = new ArrayList<>();
        
        for (Class<? extends Command> type : invokedCommandList) {
            OptionGroup optGroup = commandAndOptionMap.get(type);
            if (optGroup != null) {
                Set<OptionValue> optSet = new HashSet<>();
                for (OptionValue optValue : optGroup.getOptionValues()) {
                    if (optValue.isRequired())
                        optSet.add(optValue);
                }
                
                List<String> names = new ArrayList<>();
                for (OptionValue optValue : optSet) {
                    if (requiredOptionMap.get(optValue.getName()) != null)
                        names.add("[" + String.join("|", optValue.getNames()) + "]");
                }
                
                if (!names.isEmpty())
                    optNames.add(findCommandName(namedAndCommandMap, type) + String.format(":\n%3s", " ")
                                + String.join(",", names));
            }
        }
        
        if (!optNames.isEmpty())
            throw new RuntimeException(String.format("Missing required options: \n%s", String.join("\n", optNames)));
    }
    
    private String[] expandArgs(String[] args) {
        List<String> expandedArgs = new ArrayList<>();

        for (int i = 0; i < args.length; ++i) {
            String argument = args[i];
            if ("--".equals(argument)) {
                expandedArgs.add(argument);
                continue;
            }
            
            if (isOption(argument)) {
                for (String optName : options.getNames()) {
                    if (argument.equals(optName)) {
                        expandedArgs.add(argument);
                        break;
                    } else if (!argument.equals(optName) && argument.startsWith(optName)) {
                        String separator = getValueSeparator(optName);
                        if (separator != null && !separator.isEmpty()) {
                            String[] splitResult = argument.split(separator);
                            expandedArgs.addAll(Arrays.asList(splitResult));
                            break;
                        }
                    }
                }
            } else {
                expandedArgs.add(argument);
            }
        }

        return expandedArgs.toArray(new String[0]);
    }
    
    private String getValueSeparator(String argName) {
        OptionValue optionValue = options.get(argName);
        
        if (optionValue != null)
            return optionValue.getSplit();

        return null;
    }
    
    private boolean isOption(String argName) {
        if (StringUtil.isStringEmpty(argName))
            return false;
        
        for (String optName : options.getNames()) {
            if (argName.equals(optName)) {
                return true;
            } else if (argName.startsWith(optName)) {
                final String separator = getValueSeparator(optName);
                if (separator != null) {
                    int indexOf = argName.indexOf(separator);
                    if (indexOf > 0) {
                        if (argName.substring(0, indexOf).equals(optName))
                            return true;
                    }
                }
            }
        }

        return false;
    }
    
    private boolean isCommand(String paramsName) {
        if (StringUtil.isStringEmpty(paramsName))
            return false;
        
        return namedAndCommandMap.get(paramsName) != null;
    }
    
    private List<String> startWithOptionName(String argName) {
        final int MAX_CANDIDATES = 4;
        MultiValueMap<Integer, String> result = new MultiValueMap<>();
        List<String> candidateList = new ArrayList<>();
        for (String optName : options.getNames()) {
            if (optName.contains(argName)) {
                candidateList.add(optName);
                continue;
            }
        }
        // Check for possible matches
        if (!candidateList.isEmpty()) {
            for (String candidate : candidateList)
                result.put(Util.distance(candidate, argName), candidate);
            result = sortCandidateOptions(result);
            return new ArrayList<>(result.values());
        }
        // Unknown option! Use Levenshtein to compute possible candidates
        for (String optName : options.getNames()) {
            String[] optSplit = optName.split("-");
            result.put(Util.distance("-" + optSplit[1], argName), optName);
        }
        result = sortCandidateOptions(result);
        Collection<Integer> keys = result.keys();
        for (Integer k : keys) {
            for (String name : result.get(k))
                candidateList.add(name);
        }
        if (candidateList.size() >= MAX_CANDIDATES)
            return candidateList.subList(0, MAX_CANDIDATES);
        return candidateList;
    }
    
    protected static MultiValueMap<Integer, String> sortCandidateOptions(MultiValueMap<Integer, String> options) {
        MultiValueMap<Integer, String> sortedOptions = new MultiValueMap<>();
        List<Integer> keys = new ArrayList<>(options.keys());
        Collections.sort(keys);
        for (Integer key : keys)
            sortedOptions.putAll(key, options.get(key));
        return sortedOptions;
    }
    
    public <T extends Command> T getCommand(Class<T> type) {
        OptionGroup optGroup = commandAndOptionMap.get(type);
        Constructor<T> constructor = optGroup.getConstructor(type);
        T instanceObj = null;
        try {
            instanceObj = constructor.newInstance(new Object[0]);
            Set<Class<? extends Command>> classes = findAllExtendedClasses(type);
            for (Class<? extends Command> clazz : classes) {
                for (Field field : optGroup.getFieldList(clazz)) {
                    field.setAccessible(true);
                    OptionWithValues optWithValues = optGroup.getOptionOrArgument(field);
                    if (optWithValues.getValue() != null)
                        field.set(instanceObj, optWithValues.getValue());
                }
            }
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new RuntimeException(e.getMessage());
        }
        
        return instanceObj;
    }
    
    protected static String findCommandName(Map<String, Class<? extends Command>> namedHashMap,
                                            Class<? extends Command> command) {
        return namedHashMap.entrySet().stream()
                           .filter(entry -> entry.getValue().equals(command))
                           .map(Map.Entry::getKey).findFirst()
                           .orElse("<UNKNOWN>");
    }
    
    protected static <T> T createNewInstance(Class<T> objClass) {
        T objInstance = null;
        try {
            objInstance = objClass.newInstance();
        } catch (Exception ex) {
            // Try one more time
            try {
                Constructor<T> constructor;
                constructor = objClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                objInstance = constructor.newInstance(new Object[0]);
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                    | IllegalArgumentException | InvocationTargetException e) {
                throw new RuntimeException(String.format("Unable to instantiate an object of type '%s'.",
                            Util.getTypeName(objClass)));
            }
        }

        return objInstance;
    }
    
    @SuppressWarnings("unchecked")
    protected static Set<Class<? extends Command>> findAllExtendedClasses(Class<? extends Command> type) {
        Set<Class<? extends Command>> setClass = new LinkedHashSet<>();
        setClass.add(type);
        
        Class<?> superClass = type.getSuperclass();
        if (superClass == null || Object.class.equals(superClass))
            return setClass;
        
        if (!Command.class.equals(superClass)) {
            Class<? extends Command> classCast = (Class<? extends Command>) superClass;
            setClass.addAll(findAllExtendedClasses(classCast));
        }
        
        return setClass;
    }
    
    /**
     * Collections of shared-options between command types.
     * 
     * @author Ben Cisneros
     * @version 08/15/2018
     * @since 1.2
     */
    public static final class Options {
        
        /**
         * Map of all shared-options.
         */
        private SortedMap<String, OptionValue> sharedOptions = new TreeMap<>();
        
        public void add(String optName, OptionValue value) throws Exception {
            if (sharedOptions.put(optName, value) != null)
                throw new RuntimeException(String.format("@Option '%s' found multiple times.", optName));
        }
        
        public OptionValue get(String optName) {
            return sharedOptions.get(optName);
        }
        
        public Collection<String> getNames() {
            return sharedOptions.keySet();
        }
        
        public Set<OptionValue> getOptions() {
            SortedSet<OptionValue> optSet = new TreeSet<>();
            for (OptionValue optionValue : sharedOptions.values())
                optSet.add(optionValue);
            
            return optSet;
        }
    }
}
