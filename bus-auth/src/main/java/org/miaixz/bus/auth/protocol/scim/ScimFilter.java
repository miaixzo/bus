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
package org.miaixz.bus.auth.protocol.scim;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Closed immutable SCIM filter tree with exact attribute paths and deterministic scalar or multi-valued comparison
 * semantics. Attribute lookup is case-insensitive as required by SCIM names, while string values remain case exact
 * until a product schema layer explicitly normalizes a case-insensitive attribute.
 *
 * @author Kimi Liu
 */
public sealed interface ScimFilter permits ScimFilter.And, ScimFilter.Or, ScimFilter.Not, ScimFilter.Present,
        ScimFilter.Comparison, ScimFilter.ValuePath {

    /**
     * Maximum direct children in one logical node.
     */
    int MAXIMUM_CHILDREN = Normal._128;

    /**
     * Maximum path characters.
     */
    int MAXIMUM_PATH_LENGTH = Normal._1024;

    /**
     * Evaluates one filter against an immutable resource.
     *
     * @param filter   filter tree
     * @param resource resource
     * @return whether the resource matches
     * @throws ValidateException if either argument is {@code null}
     */
    static boolean evaluate(final ScimFilter filter, final ScimResource resource) {
        final ScimFilter expression = Assert
                .notNull(filter, () -> new ValidateException("SCIM filter must not be null"));
        final ScimResource source = Assert
                .notNull(resource, () -> new ValidateException("SCIM resource must not be null"));
        return evaluate(expression, source.attributes());
    }

    /**
     * Evaluates one filter against a complex attribute object.
     *
     * @param filter     filter tree
     * @param attributes complex attributes
     * @return whether the object matches
     * @throws ValidateException if either argument is {@code null}
     */
    static boolean evaluate(final ScimFilter filter, final Map<String, Object> attributes) {
        Assert.notNull(filter, () -> new ValidateException("SCIM filter must not be null"));
        Assert.notNull(attributes, () -> new ValidateException("SCIM attributes must not be null"));
        return switch (filter) {
            case And value -> value.children().stream().allMatch(child -> evaluate(child, attributes));
            case Or value -> value.children().stream().anyMatch(child -> evaluate(child, attributes));
            case Not value -> !evaluate(value.child(), attributes);
            case Present value -> present(resolve(attributes, value.path()));
            case Comparison value -> compare(resolve(attributes, value.path()), value.operator(), value.value());
            case ValuePath value -> value(resolve(attributes, value.path()), value.filter());
        };
    }

    /**
     * Resolves an exact dotted attribute path using case-insensitive SCIM attribute names.
     *
     * @param resource source resource
     * @param path     attribute path
     * @return resolved value or {@code null}
     * @throws ValidateException if the resource or path is {@code null}
     */
    static Object resolve(final ScimResource resource, final String path) {
        return resolve(
                Assert.notNull(resource, () -> new ValidateException("SCIM resource must not be null")).attributes(),
                path);
    }

    /**
     * Resolves an exact dotted path from a complex attribute object.
     *
     * @param attributes source attributes
     * @param path       attribute path
     * @return resolved value or {@code null}
     * @throws ValidateException if attributes or path is {@code null}
     */
    static Object resolve(final Map<String, Object> attributes, final String path) {
        Object current = Assert.notNull(attributes, () -> new ValidateException("SCIM attributes must not be null"));
        for (final String segment : ScimFilter.path(path).split("\\.")) {
            if (!(current instanceof Map<?, ?> map)) {
                return null;
            }
            current = null;
            for (final Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() instanceof String name && name.equalsIgnoreCase(segment)) {
                    current = entry.getValue();
                    break;
                }
            }
        }
        return current;
    }

    /**
     * Applies a value-path filter with any-match semantics.
     *
     * @param actual resolved multi-valued attribute
     * @param filter nested filter
     * @return whether one complex value matches
     */
    static boolean value(final Object actual, final ScimFilter filter) {
        if (!(actual instanceof List<?> values)) {
            return false;
        }
        for (final Object item : values) {
            if (item instanceof Map<?, ?> map) {
                final java.util.LinkedHashMap<String, Object> attributes = new java.util.LinkedHashMap<>();
                map.forEach((key, value) -> {
                    if (key instanceof String name) {
                        attributes.put(name, value);
                    }
                });
                if (evaluate(filter, attributes)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Tests SCIM presence semantics.
     *
     * @param value resolved value
     * @return whether the value is present and non-empty
     */
    static boolean present(final Object value) {
        return value != null && (!(value instanceof String string) || !string.isEmpty())
                && (!(value instanceof List<?> list) || !list.isEmpty());
    }

    /**
     * Compares one resolved value, applying any-match semantics to multi-valued attributes.
     *
     * @param actual   resolved resource value
     * @param operator comparison operator
     * @param expected filter value
     * @return whether the comparison matches
     * @throws ValidateException if {@code operator} is {@code null}
     */
    static boolean compare(final Object actual, final Operator operator, final Object expected) {
        Assert.notNull(operator, () -> new ValidateException("SCIM comparison operator must not be null"));
        if (actual instanceof List<?> values) {
            return operator == Operator.NE ? values.stream().noneMatch(value -> compare(value, Operator.EQ, expected))
                    : values.stream().anyMatch(value -> compare(value, operator, expected));
        }
        if (actual == null) {
            return operator == Operator.NE;
        }
        if (operator == Operator.CO || operator == Operator.SW || operator == Operator.EW) {
            if (!(actual instanceof String left) || !(expected instanceof String right)) {
                return false;
            }
            return operator == Operator.CO ? left.contains(right)
                    : operator == Operator.SW ? left.startsWith(right) : left.endsWith(right);
        }
        if (!compatible(actual, expected)) {
            return operator == Operator.NE;
        }
        final int order = order(actual, expected);
        return switch (operator) {
            case EQ -> order == Normal._0;
            case NE -> order != Normal._0;
            case GT -> order > Normal._0;
            case GE -> order >= Normal._0;
            case LT -> order < Normal._0;
            case LE -> order <= Normal._0;
            case CO, SW, EW -> false;
        };
    }

    /**
     * Tests whether two scalar values belong to the same SCIM comparison domain.
     *
     * @param actual   resource value
     * @param expected filter value
     * @return whether numeric, Boolean, instant, or string comparison is defined
     */
    private static boolean compatible(final Object actual, final Object expected) {
        return actual instanceof Number && expected instanceof Number
                || actual instanceof Boolean && expected instanceof Boolean
                || actual instanceof Instant && expected instanceof Instant
                || actual instanceof String && expected instanceof String;
    }

    /**
     * Orders two compatible scalar values.
     *
     * @param actual   resource value
     * @param expected filter value
     * @return comparison result, or a non-zero mismatch result for incompatible types
     */
    static int order(final Object actual, final Object expected) {
        if (actual instanceof Number left && expected instanceof Number right) {
            return new BigDecimal(left.toString()).compareTo(new BigDecimal(right.toString()));
        }
        if (actual instanceof Boolean left && expected instanceof Boolean right) {
            return left.compareTo(right);
        }
        if (actual instanceof Instant left && expected instanceof Instant right) {
            return left.compareTo(right);
        }
        if (actual instanceof String left && expected instanceof String right) {
            return left.compareTo(right);
        }
        return Normal._1;
    }

    /**
     * Converts one parsed scalar to the closed comparison domain.
     *
     * @param value parsed value
     * @return immutable scalar
     * @throws ValidateException if the value is null or outside the closed scalar domain
     */
    static Object scalar(final Object value) {
        if (value instanceof String || value instanceof Boolean || value instanceof BigDecimal
                || value instanceof Instant) {
            return value;
        }
        if (value instanceof Number number) {
            return new BigDecimal(number.toString());
        }
        throw new ValidateException("SCIM comparison value type is unsupported");
    }

    /**
     * Parses one SCIM dateTime comparison value.
     *
     * @param value source value
     * @return parsed instant
     * @throws ValidateException if the value is not an RFC 3339 instant
     */
    static Instant instant(final String value) {
        try {
            return Instant.parse(value);
        } catch (final DateTimeParseException failure) {
            throw new ValidateException("SCIM dateTime value is invalid");
        }
    }

    /**
     * Validates and snapshots logical children.
     *
     * @param values source children
     * @param name   node name
     * @return immutable children
     * @throws ValidateException if the list is null, empty, oversized, or contains null
     */
    static List<ScimFilter> children(final List<ScimFilter> values, final String name) {
        final List<ScimFilter> result = List
                .copyOf(Assert.notNull(values, () -> new ValidateException(name + " children must not be null")));
        Assert.isTrue(
                !result.isEmpty() && result.size() <= MAXIMUM_CHILDREN,
                () -> new ValidateException(name + " child count is invalid"));
        return result;
    }

    /**
     * Validates an attribute path.
     *
     * @param value source path
     * @return validated path
     * @throws ValidateException if the path is blank, oversized, or syntactically invalid
     */
    static String path(final String value) {
        final String result = Assert
                .notBlank(value, () -> new ValidateException("SCIM attribute path must not be blank"));
        Assert.isTrue(
                result.length() <= MAXIMUM_PATH_LENGTH && validPath(result),
                () -> new ValidateException("SCIM attribute path is invalid"));
        return result;
    }

    /**
     * Validates SCIM schema-qualified and dotted path characters without a general-purpose regex.
     *
     * @param value source path
     * @return whether the path is valid
     */
    static boolean validPath(final String value) {
        boolean segmentStart = true;
        for (int index = Normal._0; index < value.length(); index++) {
            final char current = value.charAt(index);
            if (current == '.') {
                if (segmentStart || index + Normal._1 == value.length()) {
                    return false;
                }
                segmentStart = true;
            } else if (segmentStart) {
                if (!Character.isLetter(current)) {
                    return false;
                }
                segmentStart = false;
            } else if (!Character.isLetterOrDigit(current) && current != '_' && current != '-' && current != ':') {
                return false;
            }
        }
        return !segmentStart;
    }

    /**
     * Supported SCIM comparison operators.
     *
     * @author Kimi Liu
     */
    enum Operator {

        /**
         * Equal.
         */
        EQ,

        /**
         * Not equal.
         */
        NE,

        /**
         * Contains.
         */
        CO,

        /**
         * Starts with.
         */
        SW,

        /**
         * Ends with.
         */
        EW,

        /**
         * Greater than.
         */
        GT,

        /**
         * Greater than or equal.
         */
        GE,

        /**
         * Less than.
         */
        LT,

        /**
         * Less than or equal.
         */
        LE
    }

    /**
     * Immutable conjunction.
     *
     * @param children non-empty child filters
     * @author Kimi Liu
     */
    record And(List<ScimFilter> children) implements ScimFilter {

        /**
         * Validates conjunction children.
         *
         * @param children child filters
         */
        public And {
            children = ScimFilter.children(children, "SCIM conjunction");
        }
    }

    /**
     * Immutable disjunction.
     *
     * @param children non-empty child filters
     * @author Kimi Liu
     */
    record Or(List<ScimFilter> children) implements ScimFilter {

        /**
         * Validates disjunction children.
         *
         * @param children child filters
         */
        public Or {
            children = ScimFilter.children(children, "SCIM disjunction");
        }
    }

    /**
     * Immutable negation.
     *
     * @param child exact child filter
     * @author Kimi Liu
     */
    record Not(ScimFilter child) implements ScimFilter {

        /**
         * Validates the negated child.
         *
         * @param child child filter
         */
        public Not {
            child = Assert.notNull(child, () -> new ValidateException("SCIM negation child must not be null"));
        }
    }

    /**
     * Immutable presence expression.
     *
     * @param path exact attribute path
     * @author Kimi Liu
     */
    record Present(String path) implements ScimFilter {

        /**
         * Validates the presence path.
         *
         * @param path attribute path
         */
        public Present {
            path = ScimFilter.path(path);
        }
    }

    /**
     * Immutable scalar comparison.
     *
     * @param path     exact attribute path
     * @param operator comparison operator
     * @param value    string, Boolean, number, or Instant comparison value
     * @author Kimi Liu
     */
    record Comparison(String path, Operator operator, Object value) implements ScimFilter {

        /**
         * Validates the comparison expression.
         *
         * @param path     attribute path
         * @param operator comparison operator
         * @param value    comparison value
         */
        public Comparison {
            path = ScimFilter.path(path);
            operator = Assert
                    .notNull(operator, () -> new ValidateException("SCIM comparison operator must not be null"));
            value = scalar(value);
            Assert.isTrue(
                    value instanceof String
                            || operator != Operator.CO && operator != Operator.SW && operator != Operator.EW,
                    () -> new ValidateException("SCIM string operator requires a string value"));
        }
    }

    /**
     * Immutable multi-valued complex attribute filter.
     *
     * @param path   multi-valued attribute path
     * @param filter filter evaluated against each complex value
     * @author Kimi Liu
     */
    record ValuePath(String path, ScimFilter filter) implements ScimFilter {

        /**
         * Validates one value-path expression.
         *
         * @param path   attribute path
         * @param filter nested filter
         */
        public ValuePath {
            path = ScimFilter.path(path);
            filter = Assert.notNull(filter, () -> new ValidateException("SCIM value-path filter must not be null"));
        }
    }

}
