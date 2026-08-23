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
package org.miaixz.bus.auth.source.protocol.ldap;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;

/**
 * Represents the RFC 4511 {@code PartialAttribute} sequence used by search, add, and modify operations.
 * <p>
 * A partial attribute may have no values. Operations that use the stricter ASN.1 {@code Attribute} subtype enforce its
 * one-or-more value constraint in their own model. Values use a set because ASN.1 declares {@code SET OF}; the
 * content-based value wrapper prevents Java array identity from corrupting set equality.
 * </p>
 *
 * @param type   RFC 4512 attribute description, including any options
 * @param values unordered attribute values
 * @author Kimi Liu
 */
public record LdapAttribute(String type, Set<AttributeValue> values) {

    /**
     * RFC 4512 AttributeDescription lexical grammar for descriptor and numeric-OID forms.
     */
    private static final Pattern ATTRIBUTE_DESCRIPTION = Pattern
            .compile("(?:[A-Za-z][A-Za-z0-9-]*|(?:0|[1-9][0-9]*)(?:\\.(?:0|[1-9][0-9]*))+)(?:;[A-Za-z0-9-]+)*");

    /**
     * RFC 4512 object-identifier grammar allowing a descriptor or numeric OID without attribute options.
     */
    private static final Pattern OBJECT_IDENTIFIER = Pattern
            .compile("(?:[A-Za-z][A-Za-z0-9-]*|(?:0|[1-9][0-9]*)(?:\\.(?:0|[1-9][0-9]*))+)");

    /**
     * Creates an immutable LDAP partial attribute.
     *
     * @param type   attribute description
     * @param values attribute values, possibly empty
     * @throws IllegalArgumentException if the type is malformed or the set or one of its members is {@code null}
     */
    public LdapAttribute {
        requireType(type);
        Assert.notNull(values, "LDAP attribute values must not be null");
        final LinkedHashSet<AttributeValue> copy = new LinkedHashSet<>(values.size());
        for (AttributeValue value : values) {
            copy.add(Assert.notNull(value, "LDAP attribute value must not be null"));
        }
        values = Collections.unmodifiableSet(copy);
    }

    /**
     * Validates an RFC 4512 AttributeDescription for reuse by LDAP protocol models in this package.
     *
     * @param type attribute-description lexical value
     * @throws IllegalArgumentException if the value is null, blank, or malformed
     */
    static void requireType(final String type) {
        Assert.notBlank(type, "LDAP attribute description must not be blank");
        Assert.isTrue(
                ATTRIBUTE_DESCRIPTION.matcher(type).matches(),
                "LDAP attribute description is malformed: {}",
                type);
    }

    /**
     * Validates an RFC 4512 object identifier in descriptor or numeric-OID form.
     *
     * @param value object-identifier lexical value
     * @throws IllegalArgumentException if the value is null, blank, or malformed
     */
    static void requireObjectIdentifier(final String value) {
        Assert.notBlank(value, "LDAP object identifier must not be blank");
        Assert.isTrue(OBJECT_IDENTIFIER.matcher(value).matches(), "LDAP object identifier is malformed: {}", value);
    }

    /**
     * Represents one opaque RFC 4511 {@code AttributeValue} OCTET STRING.
     * <p>
     * The value may contain text, binary syntax, or credentials. It is defensively copied and never rendered by
     * {@link #toString()}.
     * </p>
     *
     * @param value exact attribute-value octets
     * @author Kimi Liu
     */
    public record AttributeValue(byte[] value) {

        /**
         * Creates an immutable opaque LDAP attribute value.
         *
         * @param value exact octets, which may be empty
         * @throws IllegalArgumentException if {@code value} is {@code null}
         */
        public AttributeValue {
            value = Assert.notNull(value, "LDAP attribute value octets must not be null").clone();
        }

        /**
         * Returns a defensive copy of the opaque attribute value.
         *
         * @return newly allocated attribute-value octets
         */
        @Override
        public byte[] value() {
            return value.clone();
        }

        /**
         * Compares attribute values by octet content.
         *
         * @param other candidate object
         * @return {@code true} when both wrappers contain identical octets
         */
        @Override
        public boolean equals(final Object other) {
            return this == other || other instanceof AttributeValue that && Arrays.equals(value, that.value);
        }

        /**
         * Computes the content-based hash required by the enclosing ASN.1 set.
         *
         * @return hash of the opaque octets
         */
        @Override
        public int hashCode() {
            return Arrays.hashCode(value);
        }

        /**
         * Returns a non-sensitive structural description.
         *
         * @return description containing only the octet count
         */
        @Override
        public String toString() {
            return "AttributeValue[length=" + value.length + Symbol.BRACKET_RIGHT;
        }

    }

}
