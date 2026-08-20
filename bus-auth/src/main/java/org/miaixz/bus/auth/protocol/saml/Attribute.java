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
package org.miaixz.bus.auth.protocol.saml;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import javax.xml.namespace.QName;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Models SAML assertion {@code AttributeType}, preserving typed/nillable AttributeValue XML.
 *
 * @param name         required attribute name
 * @param nameFormat   optional attribute-name format URI
 * @param friendlyName optional human-readable attribute name
 * @param attributes   ordered foreign XML attributes keyed by expanded QName
 * @param values       ordered complete {@code AttributeValue} element bytes
 * @author Kimi Liu
 */
public record Attribute(String name, Optional<String> nameFormat, Optional<String> friendlyName,
        Map<QName, String> attributes, List<byte[]> values) {

    /**
     * Validates required and optional attributes and takes deep immutable ownership of XML values.
     *
     * @throws IllegalArgumentException if a component, item, key, or value is {@code null}
     * @throws ValidateException        if a present URI/attribute or XML value is invalid
     */
    public Attribute {
        name = Assert.notBlank(name, "SAML Attribute Name must not be blank");
        nameFormat = text(nameFormat, "SAML Attribute NameFormat");
        if (nameFormat.isPresent()) {
            absolute(nameFormat.getOrNull(), "SAML Attribute NameFormat");
        }
        friendlyName = text(friendlyName, "SAML Attribute FriendlyName");
        Assert.notNull(attributes, "SAML Attribute foreign attribute map must not be null");
        final Map<QName, String> copiedAttributes = new LinkedHashMap<>();
        attributes.forEach((key, value) -> {
            Assert.notNull(key, "SAML Attribute foreign QName must not be null");
            Assert.notNull(value, "SAML Attribute foreign value must not be null");
            if (key.getNamespaceURI().isEmpty()) {
                throw new ValidateException("SAML Attribute wildcard attributes require a foreign namespace");
            }
            copiedAttributes.put(key, value);
        });
        attributes = Collections.unmodifiableMap(copiedAttributes);
        values = elements(values);
    }

    /**
     * Normalizes an optional non-empty XML attribute.
     *
     * @param value optional XML attribute lexical value
     * @param label diagnostic attribute label
     * @return normalized optional attribute value
     */
    private static Optional<String> text(final Optional<String> value, final String label) {
        Assert.notNull(value, label + " container must not be null");
        final String actual = value.getOrNull();
        if (actual != null) {
            Assert.notEmpty(actual, label + " must not be empty");
        }
        return Optional.ofNullable(actual);
    }

    /**
     * Requires one absolute URI.
     *
     * @param value URI lexical value
     * @param label diagnostic attribute label
     */
    private static void absolute(final String value, final String label) {
        try {
            if (!new URI(value).isAbsolute()) {
                throw new ValidateException(label + " must be absolute");
            }
        } catch (URISyntaxException exception) {
            throw new ValidateException(label + " is not a valid URI", exception);
        }
    }

    /**
     * Deep-copies ordered complete AttributeValue elements.
     *
     * @param source ordered complete AttributeValue element bytes
     * @return immutable ordered list containing detached element bytes
     */
    private static List<byte[]> elements(final List<byte[]> source) {
        Assert.notNull(source, "SAML AttributeValue list must not be null");
        final List<byte[]> result = new ArrayList<>(source.size());
        for (byte[] element : source) {
            final byte[] value = Assert.notNull(element, "SAML AttributeValue XML must not be null");
            if (value.length == 0) {
                throw new ValidateException("SAML AttributeValue XML must not be empty");
            }
            result.add(value.clone());
        }
        return List.copyOf(result);
    }

    /**
     * Returns defensive copies of all AttributeValue XML elements.
     *
     * @return immutable list of copied XML elements
     */
    @Override
    public List<byte[]> values() {
        return elements(values);
    }

}
