/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.auth.source.protocol.scim.codec;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Locale;

import org.miaixz.bus.auth.source.protocol.scim.Filter;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Parses the RFC 7644 filter ABNF with the mandated {@code not}, {@code and}, and {@code or} precedence.
 * <p>
 * The parser is single-use and not thread-safe. It accepts case-insensitive attribute operators and logical operators,
 * preserves attribute-path spelling, and accepts only JSON primitive comparison values. Length and structural nesting
 * limits are enforced before an AST is returned.
 * </p>
 *
 * @author Kimi Liu
 */
public class ScimFilterParser {

    /**
     * Filter source supplied by the caller.
     */
    private final String input;

    /**
     * Maximum accepted grouping and value-path nesting depth.
     */
    private final int maximumDepth;

    /**
     * Current source cursor.
     */
    private int index;

    /**
     * Current structural nesting depth, including the root expression.
     */
    private int depth;

    /**
     * Creates a bounded, single-use filter parser.
     *
     * @param input         RFC 7644 filter expression
     * @param maximumLength positive maximum expression length
     * @param maximumDepth  positive maximum grouping and value-path depth
     * @throws IllegalArgumentException if {@code input} is {@code null}
     * @throws ValidateException        if a limit is not positive or the expression exceeds its length limit
     */
    public ScimFilterParser(final String input, final int maximumLength, final int maximumDepth) {
        this.input = Assert.notNull(input, "SCIM filter input must not be null");
        if (maximumLength <= 0 || maximumDepth <= 0) {
            throw new ValidateException("SCIM filter limits must be positive");
        }
        if (input.length() > maximumLength) {
            throw new ValidateException("SCIM filter exceeds the configured length limit");
        }
        this.maximumDepth = maximumDepth;
    }

    /**
     * Validates one RFC 7644 attribute path without accepting a filter or PATCH value selector.
     *
     * @param input candidate attribute path
     * @throws IllegalArgumentException if {@code input} is {@code null}
     * @throws ValidateException        if the complete value does not match {@code attrPath}
     */
    public static void validateAttributePath(final String input) {
        final String value = Assert.notNull(input, "SCIM attribute path must not be null");
        validateAttributePathValue(value);
    }

    /**
     * Validates the RFC 7644 PATCH path grammar {@code attrPath / valuePath [subAttr]}.
     *
     * @param input candidate PATCH path
     * @throws IllegalArgumentException if {@code input} is {@code null}
     * @throws ValidateException        if the complete value is not a standard PATCH path
     */
    public static void validatePatchPath(final String input) {
        final String value = Assert.notNull(input, "SCIM PATCH path must not be null");
        if (value.isEmpty()) {
            throw new ValidateException("SCIM PATCH path must not be empty");
        }
        final ScimFilterParser parser = new ScimFilterParser(value, value.length(), maximumStaticDepth(value));
        parser.index = 0;
        parser.depth = 1;
        parser.parseAttributePath();
        if (parser.peek(Symbol.C_BRACKET_LEFT)) {
            parser.index++;
            parser.enter();
            parser.parseOr();
            parser.exit();
            parser.require(Symbol.C_BRACKET_RIGHT, "SCIM PATCH valuePath requires a closing bracket");
            if (parser.peek(Symbol.C_DOT)) {
                parser.index++;
                parser.parseAttributeName();
            }
        }
        if (parser.index != value.length()) {
            throw new ValidateException("SCIM PATCH path contains trailing or malformed input");
        }
    }

    /**
     * Maps one canonicalized RFC 7644 comparison operator to the shared AST constant.
     *
     * @param value lowercase operator token
     * @return registered comparison operator
     */
    private static Filter.Operator comparisonOperator(final String value) {
        return switch (value) {
            case "eq" -> Filter.Operator.EQ;
            case "ne" -> Filter.Operator.NE;
            case "co" -> Filter.Operator.CO;
            case "sw" -> Filter.Operator.SW;
            case "ew" -> Filter.Operator.EW;
            case "gt" -> Filter.Operator.GT;
            case "lt" -> Filter.Operator.LT;
            case "ge" -> Filter.Operator.GE;
            case "le" -> Filter.Operator.LE;
            default -> throw new ValidateException("SCIM filter comparison operator is not recognized");
        };
    }

    /**
     * Validates one complete RFC 7644 attrPath lexical value.
     *
     * @param value candidate complete path
     */
    private static void validateAttributePathValue(final String value) {
        if (!validAttributePath(value)) {
            throw new ValidateException("SCIM attribute path does not match RFC 7644 attrPath");
        }
    }

    /**
     * Tests one candidate against {@code [URI ":"] ATTRNAME *1subAttr}.
     *
     * @param value candidate lexical value
     * @return whether the complete candidate is a valid attrPath
     */
    private static boolean validAttributePath(final String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        final int separator = value.lastIndexOf(Symbol.C_COLON);
        final String attribute;
        if (separator >= 0) {
            if (separator == 0 || separator == value.length() - 1 || !absoluteUri(value.substring(0, separator))) {
                return false;
            }
            attribute = value.substring(separator + 1);
        } else {
            attribute = value;
        }
        final int dot = attribute.indexOf(Symbol.C_DOT);
        if (dot >= 0 && attribute.indexOf(Symbol.C_DOT, dot + 1) >= 0) {
            return false;
        }
        return dot < 0 ? attributeName(attribute)
                : attributeName(attribute.substring(0, dot)) && attributeName(attribute.substring(dot + 1));
    }

    /**
     * Tests whether a text value is one RFC 7644 ATTRNAME.
     *
     * @param value candidate attribute name
     * @return whether the name uses the standard ASCII grammar
     */
    private static boolean attributeName(final String value) {
        if (value.isEmpty() || !alpha(value.charAt(0))) {
            return false;
        }
        for (int offset = 1; offset < value.length(); offset++) {
            if (!nameCharacter(value.charAt(offset))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tests whether a schema prefix is a syntactically valid absolute URI.
     *
     * @param value candidate schema URI
     * @return whether Java's RFC 3986 URI parser accepts an absolute URI
     */
    private static boolean absoluteUri(final String value) {
        try {
            return URI.create(value).isAbsolute();
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    /**
     * Returns a finite structural limit for static PATCH path validation.
     *
     * @param value bounded in-memory PATCH path
     * @return positive nesting bound no larger than the source length plus the root
     */
    private static int maximumStaticDepth(final String value) {
        return value.length() == Integer.MAX_VALUE ? Integer.MAX_VALUE : value.length() + 1;
    }

    /**
     * Tests an ASCII alphabetic character.
     *
     * @param value candidate UTF-16 code unit
     * @return whether it is A-Z or a-z
     */
    private static boolean alpha(final char value) {
        return value >= Symbol.C_UPPER_A && value <= Symbol.C_UPPER_Z
                || value >= Symbol.C_LOWER_A && value <= Symbol.C_LOWER_Z;
    }

    /**
     * Tests an ASCII decimal digit.
     *
     * @param value candidate UTF-16 code unit
     * @return whether it is 0-9
     */
    private static boolean digit(final char value) {
        return value >= Symbol.C_ZERO && value <= Symbol.C_NINE;
    }

    /**
     * Tests an RFC 7644 nameChar.
     *
     * @param value candidate UTF-16 code unit
     * @return whether it is an ASCII letter, digit, hyphen, or underscore
     */
    private static boolean nameCharacter(final char value) {
        return alpha(value) || digit(value) || value == Symbol.C_MINUS || value == Symbol.C_UNDERLINE;
    }

    /**
     * Tests a comparison-value boundary.
     *
     * @param value next source character
     * @return whether the character can terminate a JSON primitive in the filter grammar
     */
    private static boolean boundary(final char value) {
        return value == Symbol.C_SPACE || value == Symbol.C_PARENTHESE_RIGHT || value == Symbol.C_BRACKET_RIGHT;
    }

    /**
     * Parses the complete source into an immutable RFC 7644 filter AST.
     *
     * @return parsed filter expression
     * @throws ValidateException if the source is empty, malformed, unsupported, too deep, or has trailing input
     */
    public Filter parse() {
        if (input.isEmpty()) {
            throw new ValidateException("SCIM filter must contain an expression");
        }
        index = 0;
        depth = 1;
        final Filter result = parseOr();
        if (index != input.length()) {
            throw new ValidateException("SCIM filter contains trailing or malformed input");
        }
        return result;
    }

    /**
     * Parses the lowest-precedence logical OR chain.
     *
     * @return parsed OR expression or its single operand
     */
    private Filter parseOr() {
        Filter left = parseAnd();
        while (true) {
            final int checkpoint = index;
            if (!spaces() || !keyword("or")) {
                index = checkpoint;
                return left;
            }
            requireSpaces("SCIM logical or requires surrounding spaces");
            left = new Filter.Or(left, parseAnd());
        }
    }

    /**
     * Parses the middle-precedence logical AND chain.
     *
     * @return parsed AND expression or its single operand
     */
    private Filter parseAnd() {
        Filter left = parseUnary();
        while (true) {
            final int checkpoint = index;
            if (!spaces() || !keyword("and")) {
                index = checkpoint;
                return left;
            }
            requireSpaces("SCIM logical and requires surrounding spaces");
            left = new Filter.And(left, parseUnary());
        }
    }

    /**
     * Parses a logical NOT expression or delegates to a grouped/attribute primary expression.
     *
     * @return parsed unary expression
     */
    private Filter parseUnary() {
        final int checkpoint = index;
        if (keyword("not")) {
            spaces();
            require(Symbol.C_PARENTHESE_LEFT, "SCIM logical not requires a parenthesized operand");
            enter();
            final Filter operand = parseOr();
            exit();
            require(Symbol.C_PARENTHESE_RIGHT, "SCIM logical not requires a closing parenthesis");
            return new Filter.Not(operand);
        }
        index = checkpoint;
        return parsePrimary();
    }

    /**
     * Parses an explicit grouping, valuePath, presence expression, or comparison expression.
     *
     * @return parsed primary expression
     */
    private Filter parsePrimary() {
        if (peek(Symbol.C_PARENTHESE_LEFT)) {
            index++;
            enter();
            final Filter grouped = parseOr();
            exit();
            require(Symbol.C_PARENTHESE_RIGHT, "SCIM filter grouping requires a closing parenthesis");
            return grouped;
        }
        final Filter.AttributePath path = parseAttributePath();
        if (peek(Symbol.C_BRACKET_LEFT)) {
            index++;
            enter();
            final Filter nested = parseOr();
            exit();
            require(Symbol.C_BRACKET_RIGHT, "SCIM valuePath requires a closing bracket");
            return new Filter.ValuePath(path, nested);
        }
        requireSpaces("SCIM attribute expression requires a spaced operator");
        final String operator = parseWord().toLowerCase(Locale.ROOT);
        if ("pr".equals(operator)) {
            return new Filter.Present(path);
        }
        final Filter.Operator comparisonOperator = comparisonOperator(operator);
        requireSpaces("SCIM comparison operator requires a spaced value");
        return new Filter.Compare(path, comparisonOperator, parseComparisonValue());
    }

    /**
     * Parses one JSON primitive comparison value.
     *
     * @return typed immutable comparison value
     */
    private Filter.ComparisonValue parseComparisonValue() {
        if (peek(Symbol.C_DOUBLE_QUOTES)) {
            return new Filter.StringValue(parseJsonString());
        }
        if (literal(Normal.TRUE)) {
            return new Filter.BooleanValue(true);
        }
        if (literal(Normal.FALSE)) {
            return new Filter.BooleanValue(false);
        }
        if (literal(Normal.NULL)) {
            return new Filter.NullValue();
        }
        return new Filter.NumberValue(parseJsonNumber());
    }

    /**
     * Parses one JSON string including all RFC 8259 escapes.
     *
     * @return decoded Unicode string
     */
    private String parseJsonString() {
        require(Symbol.C_DOUBLE_QUOTES, "SCIM filter string requires an opening quote");
        final StringBuilder value = new StringBuilder();
        while (index < input.length()) {
            final char character = input.charAt(index++);
            if (character == Symbol.C_DOUBLE_QUOTES) {
                return value.toString();
            }
            if (character < 0x20) {
                throw new ValidateException("SCIM filter string contains an unescaped control character");
            }
            if (character != Symbol.C_BACKSLASH) {
                value.append(character);
                continue;
            }
            if (index >= input.length()) {
                throw new ValidateException("SCIM filter string ends inside an escape sequence");
            }
            final char escaped = input.charAt(index++);
            switch (escaped) {
                case Symbol.C_DOUBLE_QUOTES, Symbol.C_BACKSLASH, Symbol.C_SLASH -> value.append(escaped);
                case 'b' -> value.append('\b');
                case Symbol.C_LOWER_F -> value.append('\f');
                case 'n' -> value.append(Symbol.C_LF);
                case 'r' -> value.append(Symbol.C_CR);
                case 't' -> value.append(Symbol.C_TAB);
                case 'u' -> value.append(parseUnicodeEscape());
                default -> throw new ValidateException("SCIM filter string contains an invalid JSON escape");
            }
        }
        throw new ValidateException("SCIM filter string requires a closing quote");

    }

    /**
     * Parses the four hexadecimal digits following a JSON Unicode escape.
     *
     * @return decoded UTF-16 code unit
     */
    private char parseUnicodeEscape() {
        if (index + 4 > input.length()) {
            throw new ValidateException("SCIM filter Unicode escape requires four hexadecimal digits");
        }
        int value = 0;
        for (int offset = 0; offset < 4; offset++) {
            final int digit = Character.digit(input.charAt(index++), 16);
            if (digit < 0) {
                throw new ValidateException("SCIM filter Unicode escape contains a non-hexadecimal digit");
            }
            value = value * 16 + digit;
        }
        return (char) value;
    }

    /**
     * Parses one exact JSON number and preserves arbitrary decimal precision.
     *
     * @return exact decimal value
     */
    private BigDecimal parseJsonNumber() {
        final int start = index;
        if (peek(Symbol.C_MINUS)) {
            index++;
        }
        if (peek(Symbol.C_ZERO)) {
            index++;
            if (index < input.length() && digit(input.charAt(index))) {
                throw new ValidateException("SCIM filter number must not contain a leading zero");
            }
        } else {
            requireDigits("SCIM filter number requires an integer part");
        }
        if (peek(Symbol.C_DOT)) {
            index++;
            requireDigits("SCIM filter number fraction requires digits");
        }
        if (peek('e') || peek('E')) {
            index++;
            if (peek(Symbol.C_PLUS) || peek(Symbol.C_MINUS)) {
                index++;
            }
            requireDigits("SCIM filter number exponent requires digits");
        }
        if (index == start || index == start + 1 && input.charAt(start) == Symbol.C_MINUS) {
            throw new ValidateException("SCIM filter comparison requires a JSON primitive value");
        }
        try {
            return new BigDecimal(input.substring(start, index));
        } catch (NumberFormatException exception) {
            throw new ValidateException("SCIM filter number is invalid", exception);
        }
    }

    /**
     * Parses and validates one complete attribute path token at the current cursor.
     *
     * @return original attribute path lexical value
     */
    private Filter.AttributePath parseAttributePath() {
        final int start = index;
        while (index < input.length()) {
            final char character = input.charAt(index);
            if (character == Symbol.C_SPACE) {
                break;
            }
            if (character == Symbol.C_BRACKET_LEFT && validAttributePath(input.substring(start, index))) {
                break;
            }
            if (character == Symbol.C_PARENTHESE_RIGHT || character == Symbol.C_BRACKET_RIGHT) {
                break;
            }
            index++;
        }
        if (index == start) {
            throw new ValidateException("SCIM filter requires an attribute path");
        }
        final String value = input.substring(start, index);
        validateAttributePathValue(value);
        return Filter.AttributePath.parse(value);
    }

    /**
     * Parses one ASCII SCIM attribute name from the current cursor.
     *
     * @return original attribute name
     */
    private String parseAttributeName() {
        final int start = index;
        if (index >= input.length() || !alpha(input.charAt(index))) {
            throw new ValidateException("SCIM attribute name must begin with an ASCII letter");
        }
        index++;
        while (index < input.length() && nameCharacter(input.charAt(index))) {
            index++;
        }
        return input.substring(start, index);
    }

    /**
     * Parses an ASCII alphabetic operator token.
     *
     * @return operator lexical value
     */
    private String parseWord() {
        final int start = index;
        while (index < input.length() && alpha(input.charAt(index))) {
            index++;
        }
        if (index == start) {
            throw new ValidateException("SCIM filter requires an attribute operator");
        }
        return input.substring(start, index);
    }

    /**
     * Matches a case-insensitive operator keyword at the current cursor with a token boundary.
     *
     * @param value lowercase keyword
     * @return whether the keyword was consumed
     */
    private boolean keyword(final String value) {
        if (index + value.length() > input.length() || !input.regionMatches(true, index, value, 0, value.length())) {
            return false;
        }
        final int end = index + value.length();
        if (end < input.length() && nameCharacter(input.charAt(end))) {
            return false;
        }
        index = end;
        return true;
    }

    /**
     * Matches a case-sensitive JSON literal at the current cursor with a value boundary.
     *
     * @param value lowercase JSON literal
     * @return whether the literal was consumed
     */
    private boolean literal(final String value) {
        if (!input.startsWith(value, index)) {
            return false;
        }
        final int end = index + value.length();
        if (end < input.length() && !boundary(input.charAt(end))) {
            return false;
        }
        index = end;
        return true;
    }

    /**
     * Consumes one or more ASCII spaces.
     *
     * @param message validation message used when no space is present
     */
    private void requireSpaces(final String message) {
        if (!spaces()) {
            throw new ValidateException(message);
        }
    }

    /**
     * Consumes consecutive ASCII spaces.
     *
     * @return whether at least one space was consumed
     */
    private boolean spaces() {
        final int start = index;
        while (peek(Symbol.C_SPACE)) {
            index++;
        }
        return index > start;
    }

    /**
     * Requires one or more decimal digits.
     *
     * @param message validation message used when no digit is present
     */
    private void requireDigits(final String message) {
        final int start = index;
        while (index < input.length() && digit(input.charAt(index))) {
            index++;
        }
        if (index == start) {
            throw new ValidateException(message);
        }
    }

    /**
     * Requires one exact punctuation character at the current cursor.
     *
     * @param character required punctuation
     * @param message   validation message used on mismatch
     */
    private void require(final char character, final String message) {
        if (!peek(character)) {
            throw new ValidateException(message);
        }
        index++;
    }

    /**
     * Tests the current source character.
     *
     * @param character candidate character
     * @return whether the cursor points to the candidate
     */
    private boolean peek(final char character) {
        return index < input.length() && input.charAt(index) == character;
    }

    /**
     * Enters one grouping or valuePath nesting level.
     */
    private void enter() {
        depth++;
        if (depth > maximumDepth) {
            throw new ValidateException("SCIM filter exceeds the configured nesting depth");
        }
    }

    /**
     * Leaves one grouping or valuePath nesting level.
     */
    private void exit() {
        depth--;
    }

}
