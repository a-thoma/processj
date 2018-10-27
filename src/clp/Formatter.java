package clp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The format help message for command line options in
 * ProcessJ.
 * 
 * @author Ben
 * @version 10/09/2018
 * @since 1.2
 */
public class Formatter {
    
    public static final int DEFAULT_WIDTH = 80;
    
    public static final int DEFAULT_LENGTH = 32;
    
    public static final int MAX_CHAR_COUNT = 27;
    
    public static final String USAGE_PREFIX = "USAGE: ";
    
    public static final String PARAMETERS_PREFIX = "PARAMETERS: ";
    
    public static final String OPTIONS_PREFIX = "OPTIONS: ";
    
    private OptionBuilder optionBuilder;
    
    public Formatter(OptionBuilder optionBuilder) {
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
     * Count the numbers of characters in both the long name and short
     * name of an option including the number of characters in the option's
     * argument.
     * 
     * @param option
     *          The long and short names of an option.
     * @return The number of characters specified in the argument.
     */
    public int getOptionLength(OptionValue option) {
        int length = 1;
        for (String name : option.getNames()) {
            length += name.length();
            length += 1;
        }
        length += option.getMetavar().length();
        length += 4;
        return length;
    }
    
    /**
     * Neatly formats a string containing a list of (appended) options
     * and returns a new string containing the following format:
     * 
     * USAGE: <main-command> [option0] [option1] [option2] ... [optionN]
     */
    public String formatUsage(String usage) {
        Parameters parameter = optionBuilder.getMainCommand().getAnnotation(Parameters.class); 
        StringBuilder stringBuilder = new StringBuilder();
        // Split words by `[..]' or `word' or `<..>' 
        Pattern pattern = Pattern.compile("\\[.*?\\]|\\<.*?\\>|\\w+");
        Matcher matcher = pattern.matcher(usage);
        List<String> words = new ArrayList<>();
        while (matcher.find())
            words.add(matcher.group());
        
        int indent = USAGE_PREFIX.length() + parameter.name().length() + 1;
        int charLeft = DEFAULT_WIDTH - indent;
        int charCount = 0;
        stringBuilder.append(USAGE_PREFIX + parameter.name() + " ");
        for (Iterator<String> it = words.iterator(); it.hasNext();) {
            String word = it.next().trim();
            charCount += word.length() + 1;
            if (charCount > charLeft) {
                stringBuilder.append("\n")
                             .append(StringUtil.countSpaces(indent));
                charCount = word.length() + 1;
            }
            stringBuilder.append(word);
            if (it.hasNext())
                stringBuilder.append(" ");
        }
        stringBuilder.append(" ");        
        return stringBuilder.toString();
    }
    
    /**
     * Returns a string containing a list of options appended in
     * the following format:
     * 
     * [option0] [option1] [option2 | options2] ... [optionN]
     */
    public String appendAllOptions() {
        StringBuilder stringBuilder = new StringBuilder();
        Map<Class<? extends Command>, OptionGroup> commandAndOptions = optionBuilder.getCommandAndOptionMap();
        boolean hasCommands = commandAndOptions.size() > 1;
        // Grab the list of commands defined in the program
        List<String> commands = new ArrayList<>();
        commands.addAll(optionBuilder.getCommandAndNameMap().values());
        // For each command defined
        for (String commandName : commands) {
            // Grab the command
            Class<? extends Command> command = optionBuilder.getCommandByName(commandName);
            if (hasCommands)
                stringBuilder.append("[")
                             .append(commandName)
                             .append(": ");
            List<OptionValue> options = new ArrayList<>();
            options.addAll(commandAndOptions.get(command).getUniqueOptions());
            // Build and append all of its options to it
            for (Iterator<OptionValue> it = options.iterator(); it.hasNext();) {
                stringBuilder.append(buildOptions(it.next()));
                if (it.hasNext())
                    stringBuilder.append(" ");
            }
            if (hasCommands)
                stringBuilder.append("] ");
        }
        return formatUsage(stringBuilder.toString());
    }
    
    /**
     * Returns a list of arguments appended in the following format:
     * 
     * <arg0> <arg1> <arg2> ... <argN>
     */
    public String appendAllArguments() {
        StringBuilder stringBuilder = new StringBuilder();
        Map<Class<? extends Command>, OptionGroup> commandAndOptions = optionBuilder.getCommandAndOptionMap();
        // For every argument defined in the main command
        List<PositionalValue> arguments = new ArrayList<>();
        arguments.addAll(commandAndOptions.get(optionBuilder.getMainCommand()).getArguments());
        // Build and append all of its arguments to it
        for (Iterator<PositionalValue> it = arguments.iterator(); it.hasNext();) {
            PositionalValue positional = it.next();
            if (positional.getMetavar().isEmpty())
                stringBuilder.append(positional.getName());
            else
                stringBuilder.append(positional.getMetavar());
            if (it.hasNext())
                stringBuilder.append(" ");
        }
        return stringBuilder.toString();
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
        Map<Class<? extends Command>, OptionGroup> commandAndOptionMap = optionBuilder.getCommandAndOptionMap();
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
        Map<Class<? extends Command>, OptionGroup> commandAndOptionMap = optionBuilder.getCommandAndOptionMap();
        // For each command defined in the program
        for (Map.Entry<Class<? extends Command>, OptionGroup> entry : commandAndOptionMap.entrySet()) {
            // If we have more than one command, then append each individually
            if (commandAndOptionMap.size() > 1)
                stringBuilder.append(optionBuilder.getCommandAndNameMap().get(entry.getKey()) + ":\n");
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
    
    public String buildUsagePage() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n")
                     .append(buildHeader())
                     .append(appendAllOptions())
                     .append(appendAllArguments())
                     .append("\n");
        
        // Create and build the list of arguments
        stringBuilder.append("\n")
                     .append(PARAMETERS_PREFIX)
                     .append("\n");
        buildArguments(stringBuilder, MAX_CHAR_COUNT);
        // Create and build the list of commands and options
        stringBuilder.append(OPTIONS_PREFIX)
                     .append("\n");
        buildCommandAndOptions(stringBuilder, MAX_CHAR_COUNT);
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
    public String buildOptions(OptionValue optionValue) {
        StringBuilder stringBuilder = new StringBuilder();
        
        if (!optionValue.isRequired())
            stringBuilder.append("[");
        
        Iterator<String> it = Arrays.asList(optionValue.getNames()).iterator();
        while (it.hasNext()) {
            stringBuilder.append(it.next());
            if (it.hasNext())
                stringBuilder.append(" | ");
        }
        
        if (!optionValue.getSplit().isEmpty())
            stringBuilder.append(optionValue.getSplit())
                         .append(optionValue.getMetavar());
        else if (!optionValue.getMetavar().isEmpty())
            stringBuilder.append(" ")
                         .append(optionValue.getMetavar());
        if (!optionValue.isRequired())
            stringBuilder.append("]");
        
        return stringBuilder.toString();
    }
}
