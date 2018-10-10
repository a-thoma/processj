package clp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The format help message for command line options in
 * ProcessJ.
 * 
 * @author Ben Cisneros
 * @version 10/09/2018
 * @since 1.2
 */
public class FormatterHelp {
    
    public static final int DEFAULT_WIDTH = 80;
    
    public static final int DEFAULT_LENGTH = 32;
    
    public static final String USAGE_PREFIX = "Usage: ";
    
    public static final String PARAMETERS_PREFIX = "Parameters: ";
    
    public static final String OPTIONS_PREFIX = "Options: ";
    
    public static final String DEFAULT_SEPARATOR = " ";
    
    public boolean sorted = false;
    
    public void setSorted(boolean sorted) {
        this.sorted = sorted;
    }
    
    /**
     * Count the numbers of characters in both the long name and short name of
     * an option including the number of characters in the option's argument.
     * 
     * @param option
     *          The long and short names of an option.
     * @return The number of characters specified in the argument.
     */
    public static int getOptionLength(OptionValue option) {
        int length = 1;
        for (String name : option.getNames()) {
            length += name.length();
            length += 1;
        }
        
        length += option.getMetavar().length();
        length += 3;    // Change this as required
        return length;
    }
    
    /**
     * Builds a usage clause that shows how to invoke all commands and options
     * from a shell prompt in ProcessJ.
     * 
     * @param optionBuilder
     *          The group of commands and their list of options.
     * @return The usage statement that shows how to use commands and options
     *         in ProcessJ.
     */
    public String buildUsage(OptionBuilder optionBuilder) {
        StringBuffer stringBuffer = new StringBuffer().append(USAGE_PREFIX);
        Map<Class<? extends OptionParameters>, OptionGroup> commandAndOptions = optionBuilder.getCommandAndOptionMap();
        boolean additionalCommands = commandAndOptions.size() > 1;
        
        // Grab the list of commands and sort them
        List<String> commands = new ArrayList<>();
        commands.addAll(optionBuilder.getNamedAndCommandMap().keySet());
        if (sorted)
            Collections.sort(commands);
        // For every command in the list
        for (String commandName : commands) {
            // Grab the command and append it to the `stringBuilder'
            Class<? extends OptionParameters> command = optionBuilder.getNamedAndCommandMap().get(commandName);
            if (additionalCommands)
                stringBuffer.append("[")
                            .append(OptionBuilder.findCommandName(optionBuilder.getNamedAndCommandMap(), command))
                            .append(": ");
            // For every group of options in the command, sort its list of
            // options in lexicographic order
            List<OptionValue> options = new ArrayList<>();
            options.addAll(commandAndOptions.get(command).getOptionSet());
            if (sorted)
                Collections.sort(options, new OptionComparator());
            // Build a `usage' for this command and its options
            for (Iterator<OptionValue> it = options.iterator(); it.hasNext();) {
                stringBuffer.append(buildOption(it.next()));
                if (it.hasNext())
                    stringBuffer.append(" ");
            }
            if (additionalCommands)
                stringBuffer.append("]");
        }
        
        return formatUsage(stringBuffer.append(DEFAULT_SEPARATOR).append("<FILE>").toString());
    }
    
    public String formatUsage(String usage) {
        int count = 0;
        int pos = 0;
        StringBuffer stringBuffer = new StringBuffer();
        
        for (int i = 0; i < usage.length(); ++i) {
            if (count == DEFAULT_WIDTH - 1) {
                pos = stringBuffer.toString().lastIndexOf(" ");
                stringBuffer.delete(pos, stringBuffer.length());
                stringBuffer.append("\n");
                i = pos + 1;
                count = 0;
            }
            stringBuffer.append(usage.charAt(i));
            ++count;
        }
        
        return stringBuffer.toString();
    }
    
    public String usagePage(OptionBuilder optionBuilder) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(buildUsage(optionBuilder))
                     .append("\n\n");
        
        int indent = 0;
        // Find maximum length
        for (OptionValue optionValue : optionBuilder.getSharedOptions().getOptions()) {
            int optionIndent = FormatterHelp.getOptionLength(optionValue);
            if (optionIndent > indent)
                indent = optionIndent;
        }
        
        Map<String, Class<? extends OptionParameters>> namedAndCommandMap = optionBuilder.getNamedAndCommandMap();
        Map<Class<? extends OptionParameters>, OptionGroup> commandAndOptionMap = optionBuilder.getCommandAndOptionMap();
        
        stringBuilder.append(PARAMETERS_PREFIX)
                     .append("\n");
        for (OptionGroup optionGroup : commandAndOptionMap.values()) {
            for (PositionalValue positionValue : optionGroup.getPositionalArgs()) {
                String argumentHelp = positionValue.getOptionHelp(indent, DEFAULT_WIDTH);
                if (argumentHelp != null)
                    stringBuilder.append(argumentHelp)
                                 .append("\n");
            }
            stringBuilder.append("\n");
        }
        
        // This is added for future commands that precede their options
        // For example: command -option <arguments>
        stringBuilder.append(OPTIONS_PREFIX)
                     .append("\n");
        for (Class<? extends OptionParameters> command : commandAndOptionMap.keySet()) {
            if (commandAndOptionMap.size() > 1)
                // If we have more than one command, then print it!
                stringBuilder.append(OptionBuilder.findCommandName(namedAndCommandMap, command) + ":");
            
            OptionGroup optionGroup = commandAndOptionMap.get(command);
            List<OptionValue> optionList = new ArrayList<>();
            optionList.addAll(optionGroup.getOptionSet());
            if (sorted)
                Collections.sort(optionList, new OptionComparator());
            for (OptionValue optionValue : optionList) {
                String optionHelp = optionValue.getOptionHelp(indent, DEFAULT_WIDTH);
                if (optionHelp != null)
                    stringBuilder.append(optionHelp)
                                 .append("\n");
            }
            stringBuilder.append("\n");
        }
        
        return stringBuilder.toString().trim();
    }
    
    /**
     * Builds a description of all the possible names that an option may
     * take.
     * 
     * @param optionValue
     *          The option a program takes.
     * @return A description of an command line option.
     */
    public String buildOption(OptionValue optionValue) {
        StringBuffer stringBuffer = new StringBuffer();
        
        if (!optionValue.isRequired())
            stringBuffer.append("[");
        
        Iterator<String> it = Arrays.asList(optionValue.getNames()).iterator();
        while (it.hasNext()) {
            stringBuffer.append(it.next());
            if (it.hasNext())
                stringBuffer.append(" | ");
        }
        
        if (!optionValue.split.isEmpty())
            stringBuffer.append(optionValue.split)
                        .append(optionValue.metavar);
        else if (!optionValue.metavar.isEmpty())
            stringBuffer.append(DEFAULT_SEPARATOR)
                        .append(optionValue.metavar);
        if (!optionValue.isRequired())
            stringBuffer.append("]");
        
        return stringBuffer.toString();
    }
    
    private static final class OptionComparator implements Comparator<OptionValue> {
        @Override
        public int compare(OptionValue o1, OptionValue o2) {
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    }
}
