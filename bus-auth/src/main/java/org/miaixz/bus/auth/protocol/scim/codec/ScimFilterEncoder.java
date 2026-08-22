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
package org.miaixz.bus.auth.protocol.scim.codec;

import org.miaixz.bus.auth.protocol.scim.Filter;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;

/**
 * Deterministically encodes an immutable RFC 7644 Filter AST using canonical lowercase operators and JSON literals.
 * <p>
 * Parentheses are emitted only where required to preserve the standard precedence of {@code not}, {@code and}, and
 * {@code or}. Attribute path spelling and string code points are preserved while JSON control characters are escaped.
 * </p>
 *
 * @author Kimi Liu
 */
public class ScimFilterEncoder {

    /**
     * Lowest precedence assigned to logical OR.
     */
    private static final int OR_PRECEDENCE = Normal._1;

    /**
     * Middle precedence assigned to logical AND.
     */
    private static final int AND_PRECEDENCE = Normal._2;

    /**
     * Unary precedence assigned to logical NOT.
     */
    private static final int NOT_PRECEDENCE = Normal._3;

    /**
     * Highest precedence assigned to attribute and valuePath expressions.
     */
    private static final int PRIMARY_PRECEDENCE = Normal._4;

    /**
     * Creates a stateless SCIM filter encoder.
     */
    public ScimFilterEncoder() {
        // No initialization required.
    }

    /**
     * Encodes one complete Filter AST into a standard expression.
     *
     * @param filter immutable RFC 7644 filter AST
     * @return canonical filter expression
     * @throws IllegalArgumentException if {@code filter} is {@code null}
     */
    public static String encode(final Filter filter) {
        return expression(Assert.notNull(filter, "SCIM filter must not be null"), 0);
    }

    /**
     * Encodes one expression and groups it when its precedence is below the parent context.
     *
     * @param filter           expression node
     * @param parentPrecedence precedence required by the parent
     * @return encoded expression
     */
    private static String expression(final Filter filter, final int parentPrecedence) {
        final int precedence = precedence(filter);
        final String encoded = switch (filter) {
            case Filter.Present present -> present.attributePath().value() + " pr";
            case Filter.Compare compare -> compare.attributePath().value() + Symbol.SPACE + compare.operator().value()
                    + Symbol.SPACE + comparisonValue(compare.comparisonValue());
            case Filter.Not not -> "not (" + expression(not.operand(), 0) + Symbol.PARENTHESE_RIGHT;
            case Filter.And and -> expression(and.left(), AND_PRECEDENCE) + " and "
                    + expression(and.right(), AND_PRECEDENCE);
            case Filter.Or or -> expression(or.left(), OR_PRECEDENCE) + " or " + expression(or.right(), OR_PRECEDENCE);
            case Filter.ValuePath valuePath -> valuePath.attributePath().value() + Symbol.BRACKET_LEFT
                    + expression(valuePath.valueFilter(), 0) + Symbol.BRACKET_RIGHT;
            default -> throw new IllegalStateException("Unsupported protocol model implementation");
        };
        return precedence < parentPrecedence ? Symbol.PARENTHESE_LEFT + encoded + Symbol.PARENTHESE_RIGHT : encoded;
    }

    /**
     * Returns the standard operator precedence for one AST node.
     *
     * @param filter expression node
     * @return precedence from one through four
     */
    private static int precedence(final Filter filter) {
        return switch (filter) {
            case Filter.Or ignored -> OR_PRECEDENCE;
            case Filter.And ignored -> AND_PRECEDENCE;
            case Filter.Not ignored -> NOT_PRECEDENCE;
            case Filter.Present ignored -> PRIMARY_PRECEDENCE;
            case Filter.Compare ignored -> PRIMARY_PRECEDENCE;
            case Filter.ValuePath ignored -> PRIMARY_PRECEDENCE;
            default -> throw new IllegalStateException("Unsupported protocol model implementation");
        };
    }

    /**
     * Encodes one JSON primitive comparison value.
     *
     * @param value typed comparison value
     * @return standard JSON primitive lexical value
     */
    private static String comparisonValue(final Filter.ComparisonValue value) {
        return switch (value) {
            case Filter.StringValue string -> jsonString(string.value());
            case Filter.BooleanValue bool -> Boolean.toString(bool.value());
            case Filter.NumberValue number -> number.value().toString();
            case Filter.NullValue ignored -> Normal.NULL;
            default -> throw new IllegalStateException("Unsupported protocol model implementation");
        };
    }

    /**
     * Encodes one Unicode string with RFC 8259 quoting and escaping.
     *
     * @param value decoded string value
     * @return quoted JSON string literal
     */
    private static String jsonString(final String value) {
        final StringBuilder encoded = new StringBuilder(value.length() + 2).append(Symbol.C_DOUBLE_QUOTES);
        for (int index = 0; index < value.length(); index++) {
            final char character = value.charAt(index);
            switch (character) {
                case Symbol.C_DOUBLE_QUOTES -> encoded.append("\\\"");
                case Symbol.C_BACKSLASH -> encoded.append("\\\\");
                case '\b' -> encoded.append("\\b");
                case '\f' -> encoded.append("\\f");
                case Symbol.C_LF -> encoded.append("\\n");
                case Symbol.C_CR -> encoded.append("\\r");
                case Symbol.C_TAB -> encoded.append("\\t");
                default -> {
                    if (character < 0x20) {
                        encoded.append("\\u");
                        hexadecimal(encoded, character);
                    } else {
                        encoded.append(character);
                    }
                }
            }
        }
        return encoded.append(Symbol.C_DOUBLE_QUOTES).toString();
    }

    /**
     * Appends one UTF-16 code unit as four lowercase hexadecimal digits.
     *
     * @param target destination builder
     * @param value  code unit to encode
     */
    private static void hexadecimal(final StringBuilder target, final char value) {
        final char[] digits = "0123456789abcdef".toCharArray();
        target.append(digits[value >>> 12 & 0x0f]).append(digits[value >>> 8 & 0x0f]).append(digits[value >>> 4 & 0x0f])
                .append(digits[value & 0x0f]);
    }

}
