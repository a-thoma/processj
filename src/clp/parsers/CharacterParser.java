package clp.parsers;

import clp.OptionParser;

/**
 * @author Ben Cisneros
 * @version 06/24/2018
 * @since 1.2
 */
public class CharacterParser extends OptionParser<Character> {

    public CharacterParser(String optionName) {
        super(optionName);
    }

    @Override
    public Character parseValue(String value) throws Exception {
        try {
            if (value.length() != 1) {
                throw new IllegalArgumentException(String.format("'%s' is unable to convert '%s' to "
                        + "Character", optionName, value));
            }

            return new Character(value.charAt(0));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format("'%s' could not convert '%s' to "
                        + "Character.", optionName, value));
        }
    }
}
