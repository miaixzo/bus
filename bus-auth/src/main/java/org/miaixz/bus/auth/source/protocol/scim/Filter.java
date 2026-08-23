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
package org.miaixz.bus.auth.source.protocol.scim;

import java.math.BigDecimal;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Seals the immutable RFC 7644 filter abstract syntax tree.
 *
 * @author Kimi Liu
 */
public interface Filter {

    /**
     * Represents one of the registered lowercase RFC 7644 comparison operators.
     *
     * @author Kimi Liu
     */
    enum Operator {

        /**
         * Equality comparison operator.
         */
        EQ("eq"),
        /**
         * Inequality comparison operator.
         */
        NE("ne"),
        /**
         * String contains comparison operator.
         */
        CO("co"),
        /**
         * String starts-with comparison operator.
         */
        SW("sw"),
        /**
         * String ends-with comparison operator.
         */
        EW("ew"),
        /**
         * Greater-than comparison operator.
         */
        GT("gt"),
        /**
         * Less-than comparison operator.
         */
        LT("lt"),
        /**
         * Greater-than-or-equal comparison operator.
         */
        GE("ge"),
        /**
         * Less-than-or-equal comparison operator.
         */
        LE("le");

        /**
         * Canonical lowercase RFC 7644 lexical value.
         */
        private final String value;

        /**
         * Associates one enum constant with its canonical wire token.
         *
         * @param value canonical lowercase operator token
         */
        Operator(final String value) {
            this.value = value;
        }

        /**
         * Returns the canonical lowercase wire value.
         *
         * @return RFC 7644 operator token
         */
        public String value() {
            return value;
        }

    }

    /**
     * Seals filter comparison values to RFC 7644 JSON primitive values.
     *
     * @author Kimi Liu
     */
    interface ComparisonValue {

    }

    /**
     * Tests whether an attribute has a non-empty value.
     *
     * @param attributePath standard SCIM attribute path
     * @author Kimi Liu
     */
    record Present(AttributePath attributePath) implements Filter {

        /**
         * Validates the attribute path.
         *
         * @throws IllegalArgumentException if {@code attributePath} is {@code null}
         * @throws ValidateException        if the attribute path syntax is invalid
         */
        public Present {
            attributePath = Assert.notNull(attributePath, "SCIM present attribute path must not be null");
        }

    }

    /**
     * Compares an attribute value with one standard primitive comparison value.
     *
     * @param attributePath   standard SCIM attribute path
     * @param operator        standard comparison operator
     * @param comparisonValue JSON primitive comparison value
     * @author Kimi Liu
     */
    record Compare(AttributePath attributePath, Operator operator, ComparisonValue comparisonValue) implements Filter {

        /**
         * Validates the attribute path and required comparison operands.
         *
         * @throws IllegalArgumentException if an operand is {@code null}
         * @throws ValidateException        if the attribute path syntax is invalid
         */
        public Compare {
            attributePath = Assert.notNull(attributePath, "SCIM comparison attribute path must not be null");
            operator = Assert.notNull(operator, "SCIM filter comparison operator must not be null");
            comparisonValue = Assert.notNull(comparisonValue, "SCIM filter comparison value must not be null");
        }

    }

    /**
     * Negates one filter operand.
     *
     * @param operand filter being negated
     * @author Kimi Liu
     */
    record Not(Filter operand) implements Filter {

        /**
         * Requires a non-null operand.
         */
        public Not {
            operand = Assert.notNull(operand, "SCIM not operand must not be null");
        }

    }

    /**
     * Requires both filter operands to match.
     *
     * @param left  left filter operand
     * @param right right filter operand
     * @author Kimi Liu
     */
    record And(Filter left, Filter right) implements Filter {

        /**
         * Requires two non-null operands.
         */
        public And {
            left = Assert.notNull(left, "SCIM and left operand must not be null");
            right = Assert.notNull(right, "SCIM and right operand must not be null");
        }

    }

    /**
     * Requires at least one filter operand to match.
     *
     * @param left  left filter operand
     * @param right right filter operand
     * @author Kimi Liu
     */
    record Or(Filter left, Filter right) implements Filter {

        /**
         * Requires two non-null operands.
         */
        public Or {
            left = Assert.notNull(left, "SCIM or left operand must not be null");
            right = Assert.notNull(right, "SCIM or right operand must not be null");
        }

    }

    /**
     * Applies a nested value filter to a multi-valued complex attribute.
     *
     * @param attributePath standard path of the multi-valued complex attribute
     * @param valueFilter   nested filter evaluated against one complex value
     * @author Kimi Liu
     */
    record ValuePath(AttributePath attributePath, Filter valueFilter) implements Filter {

        /**
         * Validates the outer path and requires a nested filter.
         *
         * @throws IllegalArgumentException if a required operand is {@code null}
         * @throws ValidateException        if the attribute path syntax is invalid
         */
        public ValuePath {
            attributePath = Assert.notNull(attributePath, "SCIM valuePath attribute path must not be null");
            valueFilter = Assert.notNull(valueFilter, "SCIM valuePath filter must not be null");
        }

    }

    /**
     * Represents a parsed RFC 7644 attribute path without a resource-field allow-list.
     *
     * @param schemaUri    optional schema URI prefix
     * @param attribute    primary attribute name
     * @param subAttribute optional sub-attribute name
     * @author Kimi Liu
     */
    record AttributePath(Optional<String> schemaUri, String attribute, Optional<String> subAttribute) {

        /**
         * Validates and freezes one parsed attribute path.
         *
         * @throws IllegalArgumentException if a component or optional container is {@code null}
         * @throws ValidateException        if the reconstructed path violates RFC 7644 syntax
         */
        public AttributePath {
            Assert.notNull(schemaUri, "SCIM attribute path schema URI container must not be null");
            schemaUri = Optional.ofNullable(schemaUri.getOrNull());
            attribute = Assert.notBlank(attribute, "SCIM attribute path attribute must not be blank");
            Assert.notNull(subAttribute, "SCIM attribute path sub-attribute container must not be null");
            subAttribute = Optional.ofNullable(subAttribute.getOrNull());
            validateName(attribute, "SCIM attribute path attribute");
            subAttribute.ifPresent(value -> validateName(value, "SCIM attribute path sub-attribute"));
            schemaUri.ifPresent(AttributePath::validateSchema);
        }

        /**
         * Parses one RFC 7644 attribute path into explicit components.
         *
         * @param value canonical or extension attribute path
         * @return parsed immutable attribute path
         * @throws ValidateException if the path syntax is invalid
         */
        public static AttributePath parse(final String value) {
            final String path = Assert.notBlank(value, "SCIM attribute path must not be blank");
            final int colon = path.lastIndexOf(Symbol.C_COLON);
            final String schema = colon < 0 ? null : path.substring(0, colon);
            final String local = colon < 0 ? path : path.substring(colon + 1);
            final int dot = local.indexOf(Symbol.C_DOT);
            return new AttributePath(Optional.ofNullable(schema), dot < 0 ? local : local.substring(0, dot),
                    Optional.ofNullable(dot < 0 ? null : local.substring(dot + 1)));
        }

        /**
         * Validates one RFC 7644 ATTRNAME token without consulting resource schemas.
         *
         * @param value candidate attribute name
         * @param label safe diagnostic label
         */
        private static void validateName(final String value, final String label) {
            if (!asciiLetter(value.charAt(0))) {
                throw new ValidateException(label + " must begin with an ASCII letter");
            }
            for (int index = 1; index < value.length(); index++) {
                final char character = value.charAt(index);
                if (!asciiLetter(character) && !(character >= Symbol.C_ZERO && character <= Symbol.C_NINE)
                        && character != Symbol.C_MINUS && character != Symbol.C_UNDERLINE) {
                    throw new ValidateException(label + " contains an invalid character");
                }
            }
        }

        /**
         * Tests an ASCII alphabetic character used by ATTRNAME.
         *
         * @param value candidate character
         * @return {@code true} for an ASCII letter
         */
        private static boolean asciiLetter(final char value) {
            return value >= Symbol.C_UPPER_A && value <= Symbol.C_UPPER_Z
                    || value >= Symbol.C_LOWER_A && value <= Symbol.C_LOWER_Z;
        }

        /**
         * Validates an optional absolute schema URI prefix.
         *
         * @param value schema URI lexical value
         */
        private static void validateSchema(final String value) {
            try {
                if (!java.net.URI.create(Assert.notBlank(value, "SCIM attribute path schema URI must not be blank"))
                        .isAbsolute()) {
                    throw new ValidateException("SCIM attribute path schema URI must be absolute");
                }
            } catch (IllegalArgumentException exception) {
                throw new ValidateException("SCIM attribute path schema URI is invalid", exception);
            }
        }

        /**
         * Reconstructs one path from validated components.
         *
         * @param schemaUri    optional schema prefix
         * @param attribute    primary attribute
         * @param subAttribute optional sub-attribute
         * @return complete attribute path
         */
        private static String wire(
                final Optional<String> schemaUri,
                final String attribute,
                final Optional<String> subAttribute) {
            final StringBuilder value = new StringBuilder();
            schemaUri.ifPresent(schema -> value.append(schema).append(Symbol.C_COLON));
            value.append(attribute);
            subAttribute.ifPresent(sub -> value.append(Symbol.C_DOT).append(sub));
            return value.toString();
        }

        /**
         * Returns the exact RFC 7644 attribute-path representation.
         *
         * @return schema-qualified attribute path
         */
        public String value() {
            return wire(schemaUri, attribute, subAttribute);
        }

    }

    /**
     * Carries a decoded JSON string comparison value.
     *
     * @param value decoded Unicode string
     * @author Kimi Liu
     */
    record StringValue(String value) implements ComparisonValue {

        /**
         * Requires a non-null string value.
         */
        public StringValue {
            value = Assert.notNull(value, "SCIM filter string value must not be null");
        }

    }

    /**
     * Carries a JSON boolean comparison value.
     *
     * @param value boolean comparison value
     * @author Kimi Liu
     */
    record BooleanValue(boolean value) implements ComparisonValue {

    }

    /**
     * Carries an exact JSON number comparison value.
     *
     * @param value arbitrary-precision decimal number
     * @author Kimi Liu
     */
    record NumberValue(BigDecimal value) implements ComparisonValue {

        /**
         * Requires a non-null exact decimal value.
         */
        public NumberValue {
            value = Assert.notNull(value, "SCIM filter number value must not be null");
        }

    }

    /**
     * Represents the JSON {@code null} comparison value.
     *
     * @author Kimi Liu
     */
    record NullValue() implements ComparisonValue {

    }

}
