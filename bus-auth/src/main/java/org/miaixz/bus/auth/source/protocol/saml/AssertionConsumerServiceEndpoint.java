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
package org.miaixz.bus.auth.source.protocol.saml;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import javax.xml.namespace.QName;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Models an indexed SAML Metadata {@code AssertionConsumerService} endpoint.
 *
 * @param binding          required binding URI
 * @param location         required endpoint URI
 * @param responseLocation optional response endpoint URI
 * @param extensions       ordered foreign child elements
 * @param attributes       ordered foreign attributes keyed by expanded QName
 * @param index            required unsigned-short endpoint index
 * @param defaultEndpoint  optional lexical-presence value of {@code isDefault}
 * @author Kimi Liu
 */
public record AssertionConsumerServiceEndpoint(SamlBinding binding, String location, Optional<String> responseLocation,
        List<byte[]> extensions, Map<QName, String> attributes, int index, Optional<Boolean> defaultEndpoint) {

    /**
     * Validates IndexedEndpointType fields and takes deep immutable ownership of extensions.
     *
     * @throws IllegalArgumentException if a component, item, key, value, or optional container is {@code null}
     * @throws ValidateException        if a URI, index, or extension value is invalid
     */
    public AssertionConsumerServiceEndpoint {
        Assert.notNull(binding, "SAML AssertionConsumerService Binding must not be null");
        absolute(location, "SAML AssertionConsumerService Location");
        responseLocation = optionalUri(responseLocation, "SAML AssertionConsumerService ResponseLocation");
        extensions = elements(extensions, "SAML AssertionConsumerService extension");
        attributes = attributes(attributes, "SAML AssertionConsumerService");
        if (index < 0 || index > 65_535) {
            throw new ValidateException("SAML AssertionConsumerService index must be an XML unsignedShort");
        }
        Assert.notNull(defaultEndpoint, "SAML AssertionConsumerService isDefault container must not be null");
        defaultEndpoint = Optional.ofNullable(defaultEndpoint.getOrNull());
    }

    /**
     * Normalizes one optional absolute URI.
     *
     * @param value optional URI lexical value
     * @param label diagnostic endpoint component label
     * @return normalized optional absolute URI
     */
    private static Optional<String> optionalUri(final Optional<String> value, final String label) {
        Assert.notNull(value, label + " container must not be null");
        final String actual = value.getOrNull();
        if (actual != null) {
            absolute(actual, label);
        }
        return Optional.ofNullable(actual);
    }

    /**
     * Requires one non-empty absolute URI.
     *
     * @param value URI lexical value
     * @param label diagnostic endpoint component label
     */
    private static void absolute(final String value, final String label) {
        final String actual = Assert.notBlank(value, label + " must not be blank");
        try {
            if (!new URI(actual).isAbsolute()) {
                throw new ValidateException(label + " must be absolute");
            }
        } catch (URISyntaxException exception) {
            throw new ValidateException(label + " is not a valid URI", exception);
        }
    }

    /**
     * Deep-copies ordered foreign elements.
     *
     * @param source ordered extension element bytes
     * @param label  diagnostic extension label
     * @return immutable ordered list containing detached element bytes
     */
    private static List<byte[]> elements(final List<byte[]> source, final String label) {
        Assert.notNull(source, label + " list must not be null");
        final List<byte[]> result = new ArrayList<>(source.size());
        for (byte[] element : source) {
            final byte[] value = Assert.notNull(element, label + " must not be null");
            if (value.length == 0) {
                throw new ValidateException(label + " must not be empty");
            }
            result.add(value.clone());
        }
        return List.copyOf(result);
    }

    /**
     * Freezes foreign attributes and requires non-empty namespaces.
     *
     * @param source foreign attribute map
     * @param label  diagnostic endpoint label
     * @return immutable insertion-ordered attribute map
     */
    private static Map<QName, String> attributes(final Map<QName, String> source, final String label) {
        Assert.notNull(source, label + " foreign attribute map must not be null");
        final Map<QName, String> result = new LinkedHashMap<>();
        source.forEach((key, value) -> {
            Assert.notNull(key, label + " foreign attribute QName must not be null");
            Assert.notNull(value, label + " foreign attribute value must not be null");
            if (key.getNamespaceURI().isEmpty()) {
                throw new ValidateException(label + " wildcard attribute must use a foreign namespace");
            }
            result.put(key, value);
        });
        return Collections.unmodifiableMap(result);
    }

    /**
     * Returns defensive copies of endpoint extension elements.
     *
     * @return immutable ordered extension list whose byte arrays are caller-owned
     */
    @Override
    public List<byte[]> extensions() {
        return elements(extensions, "SAML AssertionConsumerService extension");
    }

}
