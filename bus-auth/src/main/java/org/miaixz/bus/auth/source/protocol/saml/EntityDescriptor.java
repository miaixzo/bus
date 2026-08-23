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
import java.time.Instant;
import java.util.*;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Models the SAML 2.0 Metadata {@code EntityDescriptorType} subset for IdP and SP roles.
 *
 * @param entityId                    required entity identifier URI, limited to 1024 characters
 * @param validUntil                  optional metadata expiration
 * @param cacheDuration               optional XML Schema duration lexical value
 * @param id                          optional XML ID
 * @param signature                   optional complete {@code ds:Signature} element bytes
 * @param extensions                  ordered complete children of {@code md:Extensions}
 * @param identityProviders           ordered IdP SSO role descriptors
 * @param serviceProviders            ordered SP SSO role descriptors
 * @param organization                optional complete {@code md:Organization} element bytes
 * @param contacts                    ordered complete {@code md:ContactPerson} element bytes
 * @param additionalMetadataLocations ordered complete {@code md:AdditionalMetadataLocation} element bytes
 * @param attributes                  ordered foreign attributes keyed by expanded QName
 * @author Kimi Liu
 */
public record EntityDescriptor(String entityId, Optional<Instant> validUntil, Optional<String> cacheDuration,
        Optional<String> id, Optional<byte[]> signature, List<byte[]> extensions,
        List<IdpSsoDescriptor> identityProviders, List<SpSsoDescriptor> serviceProviders, Optional<byte[]> organization,
        List<byte[]> contacts, List<byte[]> additionalMetadataLocations, Map<QName, String> attributes) {

    /**
     * Validates metadata identity/lifetime/cardinality and takes deep immutable ownership of XML fragments.
     *
     * @throws IllegalArgumentException if a required component, item, key, value, or optional container is null
     * @throws ValidateException        if entity ID, duration, XML ID, role cardinality, or retained XML is invalid
     */
    public EntityDescriptor {
        entityId = absolute(entityId, "SAML Metadata entityID");
        if (entityId.length() > 1024) {
            throw new ValidateException("SAML Metadata entityID exceeds 1024 characters");
        }
        validUntil = optional(validUntil, "SAML Metadata validUntil container must not be null");
        cacheDuration = duration(cacheDuration, "SAML Metadata cacheDuration");
        id = optionalId(id, "SAML Metadata ID");
        signature = bytes(signature, "SAML Metadata Signature");
        extensions = elements(extensions, "SAML Metadata extension");
        identityProviders = objects(identityProviders, "SAML IDPSSODescriptor");
        serviceProviders = objects(serviceProviders, "SAML SPSSODescriptor");
        if (identityProviders.isEmpty() && serviceProviders.isEmpty()) {
            throw new ValidateException("SAML EntityDescriptor requires at least one IdP or SP role");
        }
        organization = bytes(organization, "SAML Metadata Organization");
        contacts = elements(contacts, "SAML Metadata ContactPerson");
        additionalMetadataLocations = elements(additionalMetadataLocations, "SAML AdditionalMetadataLocation");
        attributes = attributes(attributes, "SAML EntityDescriptor");
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
     * Requires an absolute URI and returns it unchanged.
     *
     * @param value URI lexical value
     * @param label diagnostic metadata component label
     * @return validated absolute URI lexical value
     */
    private static String absolute(final String value, final String label) {
        final String actual = Assert.notBlank(value, label + " must not be blank");
        try {
            if (!new URI(actual).isAbsolute()) {
                throw new ValidateException(label + " must be absolute");
            }
        } catch (URISyntaxException exception) {
            throw new ValidateException(label + " is not a valid URI", exception);
        }
        return actual;
    }

    /**
     * Normalizes and validates an optional XML Schema duration.
     *
     * @param value optional duration lexical value
     * @param label diagnostic metadata component label
     * @return normalized optional XML Schema duration
     */
    private static Optional<String> duration(final Optional<String> value, final String label) {
        Assert.notNull(value, label + " container must not be null");
        final String actual = value.getOrNull();
        if (actual != null) {
            try {
                DatatypeFactory.newInstance().newDuration(Assert.notEmpty(actual, label + " must not be empty"));
            } catch (DatatypeConfigurationException | IllegalArgumentException exception) {
                throw new ValidateException(label + " is not a valid XML Schema duration", exception);
            }
        }
        return Optional.ofNullable(actual);
    }

    /**
     * Normalizes and validates an optional XML NCName ID.
     *
     * @param value optional identifier lexical value
     * @param label diagnostic metadata component label
     * @return normalized optional XML NCName
     */
    private static Optional<String> optionalId(final Optional<String> value, final String label) {
        Assert.notNull(value, label + " container must not be null");
        final String actual = value.getOrNull();
        if (actual != null
                && !Assert.notBlank(actual, label + " must not be blank").matches("[A-Za-z_][A-Za-z0-9._-]*")) {
            throw new ValidateException(label + " must be an XML NCName");
        }
        return Optional.ofNullable(actual);
    }

    /**
     * Deep-copies one optional XML element.
     *
     * @param source optional XML element bytes
     * @param label  diagnostic metadata component label
     * @return normalized optional with detached element bytes
     */
    private static Optional<byte[]> bytes(final Optional<byte[]> source, final String label) {
        Assert.notNull(source, label + " container must not be null");
        final byte[] value = source.getOrNull();
        if (value != null && value.length == 0) {
            throw new ValidateException(label + " must not be empty");
        }
        return Optional.ofNullable(value == null ? null : value.clone());
    }

    /**
     * Returns a defensive copy of one normalized optional XML element.
     *
     * @param source normalized optional element bytes
     * @return optional element bytes detached from retained metadata state
     */
    private static Optional<byte[]> copied(final Optional<byte[]> source) {
        final byte[] value = source.getOrNull();
        return Optional.ofNullable(value == null ? null : value.clone());
    }

    /**
     * Deep-copies ordered XML elements.
     *
     * @param source ordered complete XML element bytes
     * @param label  diagnostic metadata component label
     * @return immutable ordered list containing detached element bytes
     */
    private static List<byte[]> elements(final List<byte[]> source, final String label) {
        Assert.notNull(source, label + " list must not be null");
        final List<byte[]> result = new ArrayList<>(source.size());
        for (byte[] item : source) {
            final byte[] value = Assert.notNull(item, label + " must not be null");
            if (value.length == 0) {
                throw new ValidateException(label + " must not be empty");
            }
            result.add(value.clone());
        }
        return List.copyOf(result);
    }

    /**
     * Freezes a non-null typed model list.
     *
     * @param <T>    role descriptor type
     * @param source ordered role descriptors
     * @param label  diagnostic role label
     * @return immutable ordered role descriptor list
     */
    private static <T> List<T> objects(final List<T> source, final String label) {
        Assert.notNull(source, label + " list must not be null");
        for (T item : source) {
            Assert.notNull(item, label + " must not be null");
        }
        return List.copyOf(source);
    }

    /**
     * Freezes foreign attributes and requires foreign namespaces.
     *
     * @param source foreign attribute map
     * @param label  diagnostic metadata element label
     * @return immutable insertion-ordered foreign attribute map
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
     * Returns a defensive copy of optional Signature XML.
     *
     * @return optional complete Signature element with caller-owned bytes
     */
    @Override
    public Optional<byte[]> signature() {
        return copied(signature);
    }

    /**
     * Returns defensive copies of Metadata extension elements.
     *
     * @return immutable ordered extension list whose byte arrays are caller-owned
     */
    @Override
    public List<byte[]> extensions() {
        return elements(extensions, "SAML Metadata extension");
    }

    /**
     * Returns a defensive copy of optional Organization XML.
     *
     * @return optional complete Organization element with caller-owned bytes
     */
    @Override
    public Optional<byte[]> organization() {
        return copied(organization);
    }

    /**
     * Returns defensive copies of ContactPerson XML elements.
     *
     * @return immutable ordered contact list whose byte arrays are caller-owned
     */
    @Override
    public List<byte[]> contacts() {
        return elements(contacts, "SAML Metadata ContactPerson");
    }

    /**
     * Returns defensive copies of AdditionalMetadataLocation XML elements.
     *
     * @return immutable ordered metadata-location list whose byte arrays are caller-owned
     */
    @Override
    public List<byte[]> additionalMetadataLocations() {
        return elements(additionalMetadataLocations, "SAML AdditionalMetadataLocation");
    }

}
