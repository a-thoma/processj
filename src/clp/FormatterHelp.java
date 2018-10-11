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
     * Returns the string with the longest sequence of characters.
     * 
     * @param list
     *          An array of strings.
     * @return A string with the largest length.
     */
    public int findMaxLength(String[] list) {
        int max = 0;
        for (String str : list)
            if (str.length() > max)
                max = str.length();
        return max;
    }
    
    /**
     * Builds a sentence.
     * 
     * @param stringBuilder
     *          The series of words in a paragraph.
     * @param word
     *          A string being added to the sentence.
     * @param numWords
     *          The number of words in a sentence.
     * @return The number of words in this sentence so far.
     */
    public int buildLine(StringBuilder stringBuilder, String word, int numWords) {
        if (numWords > 0)
            stringBuilder.append(DEFAULT_SEPARATOR);
        stringBuilder.append(word);
        
        return numWords + 1;
    }
    
    /**
     * Responsible for justifying a sentence.
     * 
     * @param line
     *          A copy of the sentence being formatted.
     * @param numWords
     *          A constant reference variable containing the maximum
     *          number of words in a sentence.
     * @return A newly-formatted string.
     */
    public String justifyLine(String line, int numWords) {
        // The formatted sentence
        StringBuilder stringBuilder = new StringBuilder();
        // Amount of spaces needed
        int extraSpaces = DEFAULT_WIDTH - line.length();
        // Amount of spaces needed for each word
        int insertSpaces = 0;
        
        // Iterate through the sentence
        for (int i = 0; i < line.length(); ++i) {
            // Add a letter to the new line till a white space character
            // is encountered
            if (line.charAt(i) != ' ')
                stringBuilder.append(line.charAt(i));
            else {
                insertSpaces = extraSpaces / (numWords - 1);
                int j = 0;
                // Add spaces based on the amount needed for the line
                while (j <= insertSpaces) {
                    stringBuilder.append(DEFAULT_SEPARATOR);
                    ++j;
                }
                // Reduce number of spaces needed in the sentence
                extraSpaces -= insertSpaces;
                // Decrement the number of words as we justify the
                // sentence
                --numWords;
            }
        }
        
        return  stringBuilder.toString();
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
        length += 4;    // Magic (padding) number
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
        StringBuilder stringBuilder = new StringBuilder();
        
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
                stringBuilder.append("[")
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
                stringBuilder.append(buildOption(it.next()));
                if (it.hasNext())
                    stringBuilder.append(" ");
            }
            if (additionalCommands)
                stringBuilder.append("]");
        }
        
        return formatUsage(stringBuilder.append(DEFAULT_SEPARATOR)
                                        .append("<FILE>").toString());
    }
    
    public String formatHeader(OptionBuilder optionBuilder) {
        Parameters parameter = optionBuilder.getMainCommand().getAnnotation(Parameters.class);
        StringBuilder stringBuilder = new StringBuilder();
        
        int indent = findMaxLength(parameter.header());
        indent = (DEFAULT_WIDTH - indent) / 2;
        for (String header : parameter.header())
            stringBuilder.append(StringUtil.countSpaces(indent - 1))
                         .append(header)
                         .append("\n");
        
        stringBuilder.append("\n");
        for (String note : parameter.notes())
            stringBuilder.append(note)
                         .append("\n");
        
        return stringBuilder.append("\n").toString();
    }
    
    public String formatFooter(OptionBuilder optionBuilder) {
        Parameters parameter = optionBuilder.getMainCommand().getAnnotation(Parameters.class);
        StringBuilder stringBuilder = new StringBuilder();
        for (String footer : parameter.footer())
            stringBuilder.append(footer)
                         .append("\n");
        
        return stringBuilder.append("\n").toString();
    }
    
    public String formatUsage(String usage) {
        StringBuilder stringBuilder = new StringBuilder();        
        List<String> words = Arrays.asList(usage.split(" "));
        int charLeft = DEFAULT_WIDTH - USAGE_PREFIX.length();
        int charCount = 0;
        stringBuilder.append(USAGE_PREFIX);
        for (Iterator<String> it = words.iterator(); it.hasNext(); ) {
            String word = it.next();
            charCount += word.length() + 1;
            if (charCount > charLeft) {
                stringBuilder.append("\n").append(StringUtil.countSpaces(USAGE_PREFIX.length()));
                charCount = word.length() + 1;
            }
            stringBuilder.append(word);
            if (it.hasNext())
                stringBuilder.append(" ");
        }
        
        return stringBuilder.toString();
    }
    
    public String usagePage(OptionBuilder optionBuilder) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n")
                     .append(formatHeader(optionBuilder))
                     .append(buildUsage(optionBuilder))
                     .append("\n");
        // Find maximum length
        int indent = 0;
        for (OptionValue optionValue : optionBuilder.getSharedOptions().getOptions()) {
            int optionIndent = FormatterHelp.getOptionLength(optionValue);
            if (optionIndent > indent)
                indent = optionIndent;
        }
        
        Map<String, Class<? extends OptionParameters>> namedAndCommandMap = optionBuilder.getNamedAndCommandMap();
        Map<Class<? extends OptionParameters>, OptionGroup> commandAndOptionMap = optionBuilder.getCommandAndOptionMap();
        
        stringBuilder.append("\n")
                     .append(PARAMETERS_PREFIX)
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
                // If we have more than one command, then output each command individually
                stringBuilder.append(">")
                             .append(OptionBuilder.findCommandName(namedAndCommandMap, command) + ":");
            
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
        
        stringBuilder.append(formatFooter(optionBuilder));
        
        return stringBuilder.toString();
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
        StringBuilder stringBuilder = new StringBuilder();
        
        if (!optionValue.isRequired())
            stringBuilder.append("[");
        
        Iterator<String> it = Arrays.asList(optionValue.getNames()).iterator();
        while (it.hasNext()) {
            stringBuilder.append(it.next());
            if (it.hasNext())
                stringBuilder.append("|");
        }
        
        if (!optionValue.split.isEmpty())
            stringBuilder.append(optionValue.split)
                        .append(optionValue.metavar);
        else if (!optionValue.metavar.isEmpty())
            stringBuilder.append(DEFAULT_SEPARATOR)
                        .append(optionValue.metavar);
        if (!optionValue.isRequired())
            stringBuilder.append("]");
        
        return stringBuilder.toString();
    }
    
    private static final class OptionComparator implements Comparator<OptionValue> {
        @Override
        public int compare(OptionValue o1, OptionValue o2) {
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    }
}
