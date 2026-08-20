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
import java.time.Instant;
import java.util.*;

import javax.xml.namespace.QName;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Models SAML {@code SubjectConfirmationDataType}, including mixed wildcard content and foreign attributes.
 *
 * @param notBefore    optional lower time bound
 * @param notOnOrAfter optional exclusive upper time bound
 * @param recipient    optional recipient URI
 * @param inResponseTo optional request NCName
 * @param address      optional network address lexical value
 * @param content      ordered secure serializations of mixed child nodes
 * @param attributes   ordered foreign attributes keyed by expanded QName
 * @author Kimi Liu
 */
public record SubjectConfirmationData(Optional<Instant> notBefore, Optional<Instant> notOnOrAfter,
        Optional<String> recipient, Optional<String> inResponseTo, Optional<String> address, List<byte[]> content,
        Map<QName, String> attributes) {

    /**
     * Validates temporal/URI/NCName constraints and takes deep immutable ownership of XML content.
     *
     * @throws IllegalArgumentException if a component, item, key, or value is {@code null}
     * @throws ValidateException        if time bounds, URI, NCName, or XML content are invalid
     */
    public SubjectConfirmationData {
        notBefore = optional(notBefore, "SAML SubjectConfirmationData NotBefore container must not be null");
        notOnOrAfter = optional(notOnOrAfter, "SAML SubjectConfirmationData NotOnOrAfter container must not be null");
        if (notBefore.isPresent() && notOnOrAfter.isPresent()
                && !notBefore.getOrNull().isBefore(notOnOrAfter.getOrNull())) {
            throw new ValidateException("SAML SubjectConfirmationData NotBefore must precede NotOnOrAfter");
        }
        recipient = text(recipient, "SAML SubjectConfirmationData Recipient");
        if (recipient.isPresent()) {
            absolute(recipient.getOrNull(), "SAML SubjectConfirmationData Recipient");
        }
        inResponseTo = text(inResponseTo, "SAML SubjectConfirmationData InResponseTo");
        if (inResponseTo.isPresent() && !inResponseTo.getOrNull().matches("[A-Za-z_][A-Za-z0-9._-]*")) {
            throw new ValidateException("SAML SubjectConfirmationData InResponseTo must be an XML NCName");
        }
        address = text(address, "SAML SubjectConfirmationData Address");
        content = nodes(content);
        Assert.notNull(attributes, "SAML SubjectConfirmationData foreign attribute map must not be null");
        final Map<QName, String> copied = new LinkedHashMap<>();
        attributes.forEach((name, value) -> {
            Assert.notNull(name, "SAML SubjectConfirmationData foreign attribute QName must not be null");
            Assert.notNull(value, "SAML SubjectConfirmationData foreign attribute value must not be null");
            if (name.getNamespaceURI().isEmpty()) {
                throw new ValidateException(
                        "SAML SubjectConfirmationData wildcard attributes must use a foreign namespace");
            }
            copied.put(name, value);
        });
        attributes = Collections.unmodifiableMap(copied);
    }

    /**
     * Normalizes a generic optional container.
     *
     * @param <T>     optional value type
     * @param value   optional container
     * @param message null-container diagnostic message
     * @return normalized optional preserving the contained value
     */
    private static <T> Optional<T> optional(final Optional<T> value, final String message) {
        Assert.notNull(value, message);
        return Optional.ofNullable(value.getOrNull());
    }

    /**
     * Normalizes one optional non-empty string.
     *
     * @param value optional string value
     * @param label diagnostic confirmation-data component label
     * @return normalized optional non-empty string
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
     * @param label diagnostic confirmation-data component label
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
     * Deep-copies secure serialized mixed XML nodes.
     *
     * @param values ordered serialized child nodes
     * @return immutable ordered list containing detached node bytes
     */
    private static List<byte[]> nodes(final List<byte[]> values) {
        Assert.notNull(values, "SAML SubjectConfirmationData content list must not be null");
        final List<byte[]> copy = new ArrayList<>(values.size());
        for (byte[] value : values) {
            final byte[] actual = Assert.notNull(value, "SAML SubjectConfirmationData content node must not be null");
            if (actual.length == 0) {
                throw new ValidateException("SAML SubjectConfirmationData content node must not be empty");
            }
            copy.add(actual.clone());
        }
        return List.copyOf(copy);
    }

    /**
     * Returns deep copies of retained mixed XML child nodes.
     *
     * @return immutable ordered node list whose byte arrays are caller-owned
     */
    @Override
    public List<byte[]> content() {
        return nodes(content);
    }

}
