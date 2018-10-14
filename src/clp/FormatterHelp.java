package clp;

import java.util.ArrayList;
import java.util.Arrays;
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
    
    public boolean sorted = false;
    
    private OptionBuilder optionBuilder;
    
    public FormatterHelp(OptionBuilder optionBuilder) {
        this.optionBuilder = optionBuilder;
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
            stringBuilder.append(" ");
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
                    stringBuilder.append(" ");
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
        length += 4;
        return length;
    }
    
    
    public String formatUsage(String usage) {
        Parameters parameter = optionBuilder.getMainCommand().getAnnotation(Parameters.class); 
        StringBuilder stringBuilder = new StringBuilder();        
        List<String> words = Arrays.asList(usage.split(" "));
        int indent = USAGE_PREFIX.length() + parameter.name().length() + 1;
        int charLeft = DEFAULT_WIDTH - indent;
        int charCount = 0;
        stringBuilder.append(USAGE_PREFIX + parameter.name() + " ");
        for (Iterator<String> it = words.iterator(); it.hasNext(); ) {
            String word = it.next();
            charCount += word.length() + 1;
            if (charCount > charLeft) {
                stringBuilder.append("\n").append(StringUtil.countSpaces(indent));
                charCount = word.length() + 1;
            }
            stringBuilder.append(word);
            if (it.hasNext())
                stringBuilder.append(" ");
        }
        
        return stringBuilder.toString();
    }
    public String buildUsage() {
        StringBuilder stringBuilder = new StringBuilder();
        Map<Class<? extends OptionParameters>, OptionGroup> commandAndOptions = optionBuilder.getCommandAndOptionMap();
        boolean additionalCommands = commandAndOptions.size() > 1;
        // Grab the list of commands declared in the program
        List<String> commands = new ArrayList<>();
        commands.addAll(optionBuilder.getNamedAndCommandMap().keySet());
        // For every option defined in a command
        for (String commandName : commands) {
            // Grab the option and append it to its command
            Class<? extends OptionParameters> command = optionBuilder.getNamedAndCommandMap().get(commandName);
            if (additionalCommands)
                stringBuilder.append("[")
                             .append(OptionBuilder.findCommandName(optionBuilder.getNamedAndCommandMap(), command))
                             .append(": ");
            // For each group of options, grab unique options
            List<OptionValue> options = new ArrayList<>();
            options.addAll(commandAndOptions.get(command).getUniqueOptions());
            // Build a `usage' for this command and its options
            for (Iterator<OptionValue> it = options.iterator(); it.hasNext();) {
                stringBuilder.append(buildOption(it.next()));
                if (it.hasNext())
                    stringBuilder.append(" ");
            }
            if (additionalCommands)
                stringBuilder.append("] ");
        }
        stringBuilder.append(" ");
        // For every argument defined in the main command
        List<PositionalValue> arguments = new ArrayList<>();
        arguments.addAll(commandAndOptions.get(optionBuilder.getMainCommand()).getArguments());
        // Finish the `usage' for the main command
        for (Iterator<PositionalValue> it = arguments.iterator(); it.hasNext();) {
            PositionalValue positional = it.next();
            if (positional.getMetavar().isEmpty())
                stringBuilder.append(positional.getName());
            else
                stringBuilder.append(positional.getMetavar());
            if (it.hasNext())
                stringBuilder.append(" ");
        }
        return formatUsage(stringBuilder.toString());
    }
    
    public String buildHeader() {
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
    
    public String buildFooter() {
        Parameters parameter = optionBuilder.getMainCommand().getAnnotation(Parameters.class);
        StringBuilder stringBuilder = new StringBuilder();
        for (String footer : parameter.footer())
            stringBuilder.append(footer)
                         .append("\n");
        
        return stringBuilder.append("\n").toString();
    }
    
    public StringBuilder buildArguments(StringBuilder stringBuilder, int indent) {
        Map<Class<? extends OptionParameters>, OptionGroup> commandAndOptionMap = optionBuilder.getCommandAndOptionMap();
        // For each argument part of the main command
        for (OptionGroup optionGroup : commandAndOptionMap.values()) {
            for (PositionalValue positionValue : optionGroup.getArguments()) {
                String argumentHelp = positionValue.getOptionOrArgumentHelp(indent, DEFAULT_WIDTH);
                if (argumentHelp != null)
                    stringBuilder.append(argumentHelp)
                                 .append("\n");
            }
            stringBuilder.append("\n");
        }        
        
        return stringBuilder;
    }
    
    public StringBuilder buildCommandAndOptions(StringBuilder stringBuilder, int indent) {
        Map<Class<? extends OptionParameters>, OptionGroup> commandAndOptionMap = optionBuilder.getCommandAndOptionMap();
        Map<String, Class<? extends OptionParameters>> namedAndCommandMap = optionBuilder.getNamedAndCommandMap();
        // For each command defined in the program
        for (Map.Entry<Class<? extends OptionParameters>, OptionGroup> entry : commandAndOptionMap.entrySet()) {
            // If we have more than one command, then append each individually
            if (commandAndOptionMap.size() > 1)
                stringBuilder.append(">")
                             .append(OptionBuilder.findCommandName(namedAndCommandMap, entry.getKey()) + ": ");
            List<OptionValue> optionList = new ArrayList<>();
            optionList.addAll(entry.getValue().getUniqueOptions());
            for (OptionValue optionValue : optionList) {
                String optionHelp = optionValue.getOptionOrArgumentHelp(indent, DEFAULT_WIDTH);
                if (optionHelp != null)
                    stringBuilder.append(optionHelp)
                                 .append("\n");
            }
            stringBuilder.append("\n");
        }
        return stringBuilder;
    }
    
    public String createUsagePage() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n")
                     .append(buildHeader())
                     .append(buildUsage())
                     .append("\n");
        // Find the maximum number of characters in an option. NOTE: arguments (or
        // `PositionalValues') are not taken into account
        int indent = 0;
        for (OptionValue optionValue : optionBuilder.getSharedOptions().getOptions()) {
            int optionIndent = FormatterHelp.getOptionLength(optionValue);
            if (optionIndent > indent)
                indent = optionIndent;
        }
        // Create and build the list of arguments
        stringBuilder.append("\n")
                     .append(PARAMETERS_PREFIX)
                     .append("\n");
        buildArguments(stringBuilder, indent);
        // Create and build the list of commands and options
        stringBuilder.append(OPTIONS_PREFIX)
                     .append("\n");
        buildCommandAndOptions(stringBuilder, indent);
        stringBuilder.append(buildFooter());
        
        return stringBuilder.toString();
    }
    
    /**
     * Concatenates all possible names that an option may take.
     * 
     * @param optionValue
     *          The option a program takes.
     * @return A string representing all possibles names of an option.
     */
    public String buildOption(OptionValue optionValue) {
        StringBuilder stringBuilder = new StringBuilder();
        
        if (!optionValue.isRequired())
            stringBuilder.append("[");
        
        Iterator<String> it = Arrays.asList(optionValue.getNames()).iterator();
        while (it.hasNext()) {
            stringBuilder.append(it.next());
            if (it.hasNext())
                stringBuilder.append(" | ");
        }
        
        if (!optionValue.split.isEmpty())
            stringBuilder.append(optionValue.split)
                         .append(optionValue.metavar);
        else if (!optionValue.metavar.isEmpty())
            stringBuilder.append(" ")
                         .append(optionValue.metavar);
        if (!optionValue.isRequired())
            stringBuilder.append("]");
        
        return stringBuilder.toString();
    }
}
