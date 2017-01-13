package com.deathnerd.JavaTwig;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Created by wes on 1/12/17.
 */
public class Twig_Token {
    private String value;
    private TokenType type;
    private int line_no;

    /**
     * @param type    The type of token
     * @param value   The token value
     * @param line_no The line position in the source
     */
    Twig_Token(TokenType type, String value, int line_no) {
        this.value = value;
        this.type = type;
        this.line_no = line_no;
    }

    @Override
    public String toString() {
        try {
            return String.format("%s(%s)", Twig_Token.typeToString(this.type, true), this.value);
        } catch (LogicException e) {
            Twig.logger.error(e.getMessage());
        }
        return "";
    }

    /**
     * <p>Returns the constant representation (internal) of a given type. This is a convenience method
     * for returning a short representation</p>
     *
     * @param type The type to check
     * @return The short representation of the type as a string
     * @throws LogicException If (for some reason) the type isn't found in the {@link TokenType} enum
     */
    public static String typeToString(TokenType type) throws LogicException {
        return Twig_Token.typeToString(type, false);
    }

    /**
     * <p>Returns the constant representation (internal) of a given type</p>
     *
     * @param type     The type to check
     * @param retShort If true, the return value will be prepended with `Twig_Token::`, otherwise
     *                 it will not
     * @return The representation of the type as a string
     * @throws LogicException If (for some reason) the type isn't found in the {@link TokenType} enum
     */
    public static String typeToString(TokenType type, boolean retShort) throws LogicException {
        String name;
        switch (type) {
            case EOF_TYPE:
            case BLOCK_END_TYPE:
            case BLOCK_START_TYPE:
            case INTERPOLATION_END_TYPE:
            case INTERPOLATION_START_TYPE:
            case NAME_TYPE:
            case NUMBER_TYPE:
            case OPERATOR_TYPE:
            case PUNCTUATION_TYPE:
            case STRING_TYPE:
            case TEXT_TYPE:
            case VAR_END_TYPE:
            case VAR_START_TYPE:
                name = type.name();
                break;
            default:
                throw new LogicException(String.format("Token of type \"%s\" does not exist.", type));
        }
        return retShort ? name : "Twig_Token::" + name;
    }

    /**
     * <p>Returns the English representation of a given type.</p>
     * @param type The type to return as English
     * @return The English representation of the given type
     * @throws LogicException If (for some reason) the type doesn't exist inside {@link TokenType}
     */
    public static String typeToEnglish(TokenType type) throws LogicException {
        switch (type) {
            case EOF_TYPE:
            case BLOCK_END_TYPE:
            case BLOCK_START_TYPE:
            case INTERPOLATION_END_TYPE:
            case INTERPOLATION_START_TYPE:
            case NAME_TYPE:
            case NUMBER_TYPE:
            case OPERATOR_TYPE:
            case PUNCTUATION_TYPE:
            case STRING_TYPE:
            case TEXT_TYPE:
            case VAR_END_TYPE:
            case VAR_START_TYPE:
                return type.getHumanReadable();
            default:
                throw new LogicException(String.format("Token of type \"%s\" does not exist.", type));
        }
    }

    /**
     * <p>Tests the current token for a type and/or a value</p>
     *
     * @param values The token value
     * @return Whether the token is a {@link TokenType#NAME_TYPE} and its value is one of the given values
     */
    public boolean test(String[] values) {
        TokenType type = TokenType.NAME_TYPE;
        return this.type == type && (values == null || (ArrayUtils.contains(values, value)));
    }

    /**
     * <p>Tests the current token for a type and/or a value</p>
     *
     * @param value The token value
     * @return Whether the current token is a {@link TokenType#NAME_TYPE} with the given value
     */
    public boolean test(String value) {
        TokenType type = TokenType.NAME_TYPE;
        return this.type == type && (value == null || this.value.equals(value));
    }

    /**
     * <p>Tests the current token for a type and/or a value</p>
     *
     * @param type   The type to test
     * @param values The token value
     * @return Whether the token is of the given type and has a value equal to one of the given values
     */
    public boolean test(TokenType type, @NotNull String[] values) {
        return this.type == type && ArrayUtils.contains(values, value);
    }

    /**
     * <p>Tests the current token for a type and/or a value</p>
     *
     * @param type  The type to test
     * @param value The token value
     * @return Whether the token is of the value and type
     */
    public boolean test(TokenType type, @NotNull String value) {
        return this.type == type && this.value.equals(value);
    }

    public String getValue() {
        return this.value;
    }

    public TokenType getType() {
        return this.type;
    }

    public enum TokenType {
        EOF_TYPE("end of template"),
        TEXT_TYPE("text"),
        BLOCK_START_TYPE("begin of statement block"),
        VAR_START_TYPE("begin of print statement"),
        BLOCK_END_TYPE("end of statement block"),
        VAR_END_TYPE("end of print statement"),
        NAME_TYPE("name"),
        NUMBER_TYPE("number"),
        STRING_TYPE("string"),
        OPERATOR_TYPE("operator"),
        PUNCTUATION_TYPE("punctuation"),
        INTERPOLATION_START_TYPE("begin of string interpolation"),
        INTERPOLATION_END_TYPE("end of string interpolation");

        private final String humanReadable;

        TokenType(String value) {
            humanReadable = value;
        }

        private String getHumanReadable() {
            return humanReadable;
        }
    }


}
