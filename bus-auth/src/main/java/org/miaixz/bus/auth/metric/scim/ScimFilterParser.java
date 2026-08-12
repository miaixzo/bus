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
package org.miaixz.bus.auth.metric.scim;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.auth.metric.scim.ScimFilter.*;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Parses the complete bounded SCIM filter grammar without regular expressions or general-purpose splitting. Logical
 * precedence is {@code not}, {@code and}, then {@code or}; a value-path expression evaluates its nested filter against
 * each complex value of the selected multi-valued attribute.
 *
 * @author Kimi Liu
 */
public final class ScimFilterParser {

    /**
     * Maximum accepted filter characters.
     */
    public static final int MAXIMUM_CHARACTERS = Normal._8192;

    /**
     * Maximum parser nesting depth.
     */
    public static final int MAXIMUM_DEPTH = Normal._16;

    /**
     * Maximum AST node count.
     */
    public static final int MAXIMUM_NODES = Normal._1024;

    /**
     * Maximum individual token characters.
     */
    public static final int MAXIMUM_TOKEN_CHARACTERS = Normal._1024;

    /**
     * Prevents construction of the stateless parser.
     */
    private ScimFilterParser() {
        // No initialization required.
    }

    /**
     * Parses one complete SCIM filter.
     *
     * @param source filter text
     * @return immutable filter tree
     */
    public static ScimFilter parse(final String source) {
        final String filter = Assert.notBlank(source, () -> new ValidateException("SCIM filter must not be blank"));
        Assert.isTrue(
                filter.length() <= MAXIMUM_CHARACTERS,
                () -> new ValidateException("SCIM filter exceeds its character limit"));
        final Parser parser = new Parser(filter);
        final ScimFilter result = parser.expression(Normal._1);
        parser.space();
        Assert.isTrue(parser.end(), () -> new ValidateException("SCIM filter contains trailing input"));
        return result;
    }

    /**
     * Stateful bounded recursive-descent parser.
     */
    private static final class Parser {

        /**
         * Source filter.
         */
        private final String source;

        /**
         * Current character index.
         */
        private int index;

        /**
         * Constructed AST node count.
         */
        private int nodes;

        /**
         * Creates one parser.
         *
         * @param source source filter
         */
        private Parser(final String source) {
            this.source = source;
        }

        /**
         * Parses one disjunction.
         *
         * @param depth current nesting depth
         * @return parsed expression
         */
        private ScimFilter expression(final int depth) {
            depth(depth);
            final ArrayList<ScimFilter> values = new ArrayList<>();
            values.add(conjunction(depth));
            while (keyword("or")) {
                values.add(conjunction(depth));
                children(values);
            }
            return values.size() == Normal._1 ? values.getFirst() : node(new Or(values));
        }

        /**
         * Parses one conjunction.
         *
         * @param depth current nesting depth
         * @return parsed conjunction
         */
        private ScimFilter conjunction(final int depth) {
            final ArrayList<ScimFilter> values = new ArrayList<>();
            values.add(unary(depth));
            while (keyword("and")) {
                values.add(unary(depth));
                children(values);
            }
            return values.size() == Normal._1 ? values.getFirst() : node(new And(values));
        }

        /**
         * Parses negation or a primary expression.
         *
         * @param depth current nesting depth
         * @return parsed expression
         */
        private ScimFilter unary(final int depth) {
            if (keyword("not")) {
                return node(new Not(unary(depth + Normal._1)));
            }
            return primary(depth);
        }

        /**
         * Parses a parenthesized, value-path, presence, or comparison expression.
         *
         * @param depth current nesting depth
         * @return parsed expression
         */
        private ScimFilter primary(final int depth) {
            depth(depth);
            space();
            if (consume('(')) {
                final ScimFilter result = expression(depth + Normal._1);
                required(')');
                return result;
            }
            final String path = path();
            space();
            if (consume('[')) {
                final ScimFilter result = expression(depth + Normal._1);
                required(']');
                return node(new ValuePath(path, result));
            }
            final String operator = word();
            if ("pr".equalsIgnoreCase(operator)) {
                return node(new Present(path));
            }
            final Operator resolved;
            try {
                resolved = Operator.valueOf(operator.toUpperCase(java.util.Locale.ROOT));
            } catch (final IllegalArgumentException failure) {
                throw new ValidateException("SCIM filter operator is invalid");
            }
            return node(new Comparison(path, resolved, scalar()));
        }

        /**
         * Parses one comparison scalar.
         *
         * @return parsed scalar
         */
        private Object scalar() {
            space();
            if (end()) {
                throw new ValidateException("SCIM filter comparison value is missing");
            }
            if (source.charAt(index) == '"') {
                return string();
            }
            final String value = word();
            if ("true".equals(value)) {
                return Boolean.TRUE;
            }
            if ("false".equals(value)) {
                return Boolean.FALSE;
            }
            if ("null".equals(value)) {
                throw new ValidateException("SCIM filter null comparison is unsupported");
            }
            try {
                return new BigDecimal(value);
            } catch (final NumberFormatException failure) {
                throw new ValidateException("SCIM filter comparison value is invalid");
            }
        }

        /**
         * Parses one JSON string literal.
         *
         * @return decoded string
         */
        private String string() {
            required('"');
            final StringBuilder result = new StringBuilder();
            while (!end()) {
                final char value = source.charAt(index++);
                if (value == '"') {
                    Assert.isTrue(
                            result.length() <= MAXIMUM_TOKEN_CHARACTERS,
                            () -> new ValidateException("SCIM filter string exceeds its limit"));
                    return result.toString();
                }
                if (value < 0x20) {
                    throw new ValidateException("SCIM filter string contains a control character");
                }
                if (value != '\\') {
                    result.append(value);
                    continue;
                }
                if (end()) {
                    throw new ValidateException("SCIM filter string escape is incomplete");
                }
                final char escape = source.charAt(index++);
                switch (escape) {
                    case '"', '\\', '/' -> result.append(escape);
                    case 'b' -> result.append('\b');
                    case 'f' -> result.append('\f');
                    case 'n' -> result.append('\n');
                    case 'r' -> result.append('\r');
                    case 't' -> result.append('\t');
                    case 'u' -> result.append(unicode());
                    default -> throw new ValidateException("SCIM filter string escape is invalid");
                }
            }
            throw new ValidateException("SCIM filter string is unterminated");
        }

        /**
         * Parses one four-digit Unicode escape.
         *
         * @return decoded character
         */
        private char unicode() {
            if (index + Normal._4 > source.length()) {
                throw new ValidateException("SCIM filter Unicode escape is incomplete");
            }
            int value = Normal._0;
            for (int count = Normal._0; count < Normal._4; count++) {
                final int digit = Character.digit(source.charAt(index++), Normal._16);
                if (digit < Normal._0) {
                    throw new ValidateException("SCIM filter Unicode escape is invalid");
                }
                value = value * Normal._16 + digit;
            }
            return (char) value;
        }

        /**
         * Parses and validates one attribute path.
         *
         * @return attribute path
         */
        private String path() {
            return ScimFilter.path(word());
        }

        /**
         * Parses one unquoted token.
         *
         * @return token
         */
        private String word() {
            space();
            final int start = index;
            while (!end()) {
                final char value = source.charAt(index);
                if (Character.isWhitespace(value) || value == '(' || value == ')' || value == '[' || value == ']') {
                    break;
                }
                index++;
                if (index - start > MAXIMUM_TOKEN_CHARACTERS) {
                    throw new ValidateException("SCIM filter token exceeds its limit");
                }
            }
            if (start == index) {
                throw new ValidateException("SCIM filter token is missing");
            }
            return source.substring(start, index);
        }

        /**
         * Consumes one delimited case-insensitive keyword.
         *
         * @param value keyword
         * @return whether the keyword was consumed
         */
        private boolean keyword(final String value) {
            space();
            final int end = index + value.length();
            if (end > source.length() || !source.regionMatches(true, index, value, Normal._0, value.length())) {
                return false;
            }
            if (end < source.length()) {
                final char next = source.charAt(end);
                if (!Character.isWhitespace(next) && next != '(') {
                    return false;
                }
            }
            index = end;
            return true;
        }

        /**
         * Skips grammar whitespace.
         */
        private void space() {
            while (!end() && Character.isWhitespace(source.charAt(index))) {
                index++;
            }
        }

        /**
         * Consumes one exact character.
         *
         * @param value character
         * @return whether consumed
         */
        private boolean consume(final char value) {
            space();
            if (!end() && source.charAt(index) == value) {
                index++;
                return true;
            }
            return false;
        }

        /**
         * Requires one exact character.
         *
         * @param value required character
         */
        private void required(final char value) {
            if (!consume(value)) {
                throw new ValidateException("SCIM filter delimiter is missing");
            }
        }

        /**
         * Registers one AST node.
         *
         * @param <T>   node type
         * @param value node
         * @return node
         */
        private <T extends ScimFilter> T node(final T value) {
            nodes++;
            Assert.isTrue(nodes <= MAXIMUM_NODES, () -> new ValidateException("SCIM filter exceeds its node limit"));
            return value;
        }

        /**
         * Validates nesting depth.
         *
         * @param value depth
         */
        private void depth(final int value) {
            Assert.isTrue(value <= MAXIMUM_DEPTH, () -> new ValidateException("SCIM filter exceeds its nesting limit"));
        }

        /**
         * Validates one logical child collection during assembly.
         *
         * @param values child collection
         */
        private void children(final List<ScimFilter> values) {
            Assert.isTrue(
                    values.size() <= ScimFilter.MAXIMUM_CHILDREN,
                    () -> new ValidateException("SCIM logical expression exceeds its child limit"));
        }

        /**
         * Returns whether all input was consumed.
         *
         * @return whether at end
         */
        private boolean end() {
            return index == source.length();
        }
    }

}
