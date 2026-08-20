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

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Models an RFC 7643 Schema discovery resource and its recursive attribute definitions.
 *
 * @param schemas     singleton standard Schema resource schema URI
 * @param id          absolute schema URI described by this resource
 * @param name        human-readable schema name when supplied
 * @param description human-readable schema description when supplied
 * @param attributes  top-level schema attribute definitions
 * @param meta        service-provider-maintained discovery resource metadata
 * @author Kimi Liu
 */
public record Schema(List<String> schemas, String id, Optional<String> name, Optional<String> description,
        List<AttributeDefinition> attributes, Optional<Resource.Meta> meta) implements Resource {

    /**
     * Enforces the discovery schema, described schema URI, and unique top-level attributes.
     *
     * @throws IllegalArgumentException if a required value, container, or attribute is {@code null}
     * @throws ValidateException        if a schema URI, optional text, or attribute name is invalid
     */
    public Schema {
        Assert.notNull(schemas, "SCIM Schema resource schemas must not be null");
        schemas = List.copyOf(schemas);
        if (!schemas.equals(List.of(Scim.SCHEMA_SCHEMA))) {
            throw new ValidateException("SCIM Schema resource must use only the standard Schema schema URI");
        }
        id = absoluteUri(id, "SCIM Schema id");
        name = optionalText(name, "SCIM Schema name");
        description = optionalText(description, "SCIM Schema description");
        attributes = copyAttributes(attributes, "SCIM Schema attributes");
        Assert.notNull(meta, "SCIM Schema meta container must not be null");
        meta = Optional.ofNullable(meta.getOrNull());
    }

    /**
     * Copies an attribute list and requires case-insensitive sibling-name uniqueness.
     *
     * @param values source attribute definitions
     * @param label  validation label
     * @return immutable attribute list
     */
    private static List<AttributeDefinition> copyAttributes(
            final List<AttributeDefinition> values,
            final String label) {
        Assert.notNull(values, label + " must not be null");
        final Set<String> names = new HashSet<>(values.size());
        for (AttributeDefinition attribute : values) {
            final AttributeDefinition item = Assert.notNull(attribute, label + " entry must not be null");
            if (!names.add(item.name().toLowerCase(Locale.ROOT))) {
                throw new ValidateException(label + " names must be unique ignoring case");
            }
        }
        return List.copyOf(values);
    }

    /**
     * Copies a text list after requiring non-blank unique values.
     *
     * @param values source strings
     * @param label  validation label
     * @return immutable text list
     */
    private static List<String> copyText(final List<String> values, final String label) {
        Assert.notNull(values, label + " list must not be null");
        final Set<String> unique = new HashSet<>(values.size());
        for (String value : values) {
            final String text = Assert.notBlank(value, label + " must not be blank");
            if (!unique.add(text)) {
                throw new ValidateException(label + " values must be unique");
            }
        }
        return List.copyOf(values);
    }

    /**
     * Normalizes a required Bus Optional containing non-blank text.
     *
     * @param value required optional container
     * @param label validation label
     * @return independent optional with the same text value
     */
    private static Optional<String> optionalText(final Optional<String> value, final String label) {
        Assert.notNull(value, label + " container must not be null");
        if (!value.isEmpty()) {
            Assert.notBlank(value.getOrThrow(), label + " must not be blank");
        }
        return Optional.ofNullable(value.getOrNull());
    }

    /**
     * Parses and requires one absolute URI.
     *
     * @param value URI lexical value
     * @param label validation label
     * @return unchanged valid URI text
     */
    private static String absoluteUri(final String value, final String label) {
        final String text = Assert.notBlank(value, label + " must not be blank");
        try {
            if (!URI.create(text).isAbsolute()) {
                throw new ValidateException(label + " must be absolute");
            }
            return text;
        } catch (IllegalArgumentException exception) {
            throw new ValidateException(label + " must be a valid absolute URI", exception);
        }
    }

    /**
     * Enumerates RFC 7643 attribute data types.
     *
     * @author Kimi Liu
     */
    public enum Type {

        /**
         * Unicode string value.
         */
        STRING("string"),
        /**
         * Boolean value.
         */
        BOOLEAN("boolean"),
        /**
         * Decimal numeric value.
         */
        DECIMAL("decimal"),
        /**
         * Integer numeric value.
         */
        INTEGER("integer"),
        /**
         * RFC 3339 date-time value.
         */
        DATETIME("dateTime"),
        /**
         * URI reference value.
         */
        REFERENCE("reference"),
        /**
         * Base64-encoded binary value.
         */
        BINARY("binary"),
        /**
         * Structured value composed from sub-attributes.
         */
        COMPLEX("complex");

        /**
         * Canonical RFC 7643 wire value.
         */
        private final String value;

        /**
         * Associates one type with its canonical wire value.
         *
         * @param value canonical RFC 7643 wire value
         */
        Type(final String value) {
            this.value = value;
        }

        /**
         * Returns the canonical RFC 7643 wire value.
         *
         * @return canonical attribute type value
         */
        public String value() {
            return value;
        }

    }

    /**
     * Enumerates RFC 7643 attribute mutability values.
     *
     * @author Kimi Liu
     */
    public enum Mutability {

        /**
         * Attribute cannot be changed by a client.
         */
        READ_ONLY("readOnly"),
        /**
         * Attribute can be read and written.
         */
        READ_WRITE("readWrite"),
        /**
         * Attribute can be set only while the resource is created.
         */
        IMMUTABLE("immutable"),
        /**
         * Attribute accepts writes but is never returned.
         */
        WRITE_ONLY("writeOnly");

        /**
         * Canonical RFC 7643 wire value.
         */
        private final String value;

        /**
         * Associates one mutability with its canonical wire value.
         *
         * @param value canonical RFC 7643 wire value
         */
        Mutability(final String value) {
            this.value = value;
        }

        /**
         * Returns the canonical RFC 7643 wire value.
         *
         * @return canonical mutability value
         */
        public String value() {
            return value;
        }

    }

    /**
     * Enumerates RFC 7643 attribute return behaviors.
     *
     * @author Kimi Liu
     */
    public enum Returned {

        /**
         * Attribute is always returned.
         */
        ALWAYS("always"),
        /**
         * Attribute is never returned.
         */
        NEVER("never"),
        /**
         * Attribute is returned by default.
         */
        DEFAULT("default"),
        /**
         * Attribute is returned only when requested.
         */
        REQUEST("request");

        /**
         * Canonical RFC 7643 wire value.
         */
        private final String value;

        /**
         * Associates one return behavior with its canonical wire value.
         *
         * @param value canonical RFC 7643 wire value
         */
        Returned(final String value) {
            this.value = value;
        }

        /**
         * Returns the canonical RFC 7643 wire value.
         *
         * @return canonical return behavior value
         */
        public String value() {
            return value;
        }

    }

    /**
     * Enumerates RFC 7643 attribute uniqueness constraints.
     *
     * @author Kimi Liu
     */
    public enum Uniqueness {

        /**
         * No uniqueness guarantee.
         */
        NONE("none"),
        /**
         * Value is unique within one service provider.
         */
        SERVER("server"),
        /**
         * Value is globally unique.
         */
        GLOBAL("global");

        /**
         * Canonical RFC 7643 wire value.
         */
        private final String value;

        /**
         * Associates one uniqueness constraint with its canonical wire value.
         *
         * @param value canonical RFC 7643 wire value
         */
        Uniqueness(final String value) {
            this.value = value;
        }

        /**
         * Returns the canonical RFC 7643 wire value.
         *
         * @return canonical uniqueness value
         */
        public String value() {
            return value;
        }

    }

    /**
     * Models one RFC 7643 schema attribute definition.
     *
     * @param name            case-insensitive attribute name
     * @param type            standard SCIM attribute data type
     * @param multiValued     whether the attribute contains a value array
     * @param description     human-readable attribute description when supplied
     * @param required        whether resource representations require the attribute
     * @param caseExact       whether string comparison is case-sensitive
     * @param mutability      standard attribute mutability
     * @param returned        standard response-return behavior
     * @param uniqueness      standard uniqueness constraint
     * @param canonicalValues permitted canonical string values
     * @param referenceTypes  permitted reference resource types or {@code external}/{@code uri}
     * @param subAttributes   recursive definitions for a complex attribute
     * @author Kimi Liu
     */
    public record AttributeDefinition(String name, Type type, boolean multiValued, Optional<String> description,
            boolean required, boolean caseExact, Mutability mutability, Returned returned, Uniqueness uniqueness,
            List<String> canonicalValues, List<String> referenceTypes, List<AttributeDefinition> subAttributes) {

        /**
         * Enforces type-specific schema attribute definition invariants.
         *
         * @throws IllegalArgumentException if a required value, container, or list entry is {@code null}
         * @throws ValidateException        if type-specific canonical, reference, or sub-attribute rules are violated
         */
        public AttributeDefinition {
            name = Assert.notBlank(name, "SCIM schema attribute name must not be blank");
            type = Assert.notNull(type, "SCIM schema attribute type must not be null");
            description = optionalText(description, "SCIM schema attribute description");
            mutability = Assert.notNull(mutability, "SCIM schema attribute mutability must not be null");
            returned = Assert.notNull(returned, "SCIM schema attribute returned value must not be null");
            uniqueness = Assert.notNull(uniqueness, "SCIM schema attribute uniqueness must not be null");
            canonicalValues = copyText(canonicalValues, "SCIM schema canonical value");
            referenceTypes = copyText(referenceTypes, "SCIM schema reference type");
            subAttributes = copyAttributes(subAttributes, "SCIM schema subAttributes");
            if (type == Type.COMPLEX) {
                if (subAttributes.isEmpty()) {
                    throw new ValidateException("SCIM complex attribute requires subAttributes");
                }
                if (!canonicalValues.isEmpty() || !referenceTypes.isEmpty() || uniqueness != Uniqueness.NONE) {
                    throw new ValidateException(
                            "SCIM complex attribute prohibits canonical, reference, and uniqueness values");
                }
            } else if (!subAttributes.isEmpty()) {
                throw new ValidateException("SCIM non-complex attribute prohibits subAttributes");
            }
            if (type != Type.REFERENCE && !referenceTypes.isEmpty()) {
                throw new ValidateException("SCIM referenceTypes are permitted only for reference attributes");
            }
        }

    }

}
