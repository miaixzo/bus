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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Models the fields of SAML Metadata {@code IDPSSODescriptorType} consumed and emitted by the metadata codec.
 *
 * @param id                         optional role XML ID
 * @param validUntil                 optional role expiration
 * @param cacheDuration              optional XML Schema duration lexical value retained for MetadataCodec validation
 * @param protocolSupportEnumeration non-empty supported protocol namespace URI list
 * @param errorUrl                   optional error-reporting URI
 * @param signature                  optional complete role Signature element bytes
 * @param extensions                 ordered role extension elements
 * @param keys                       ordered key descriptors
 * @param organization               optional complete Organization element bytes
 * @param contacts                   ordered complete ContactPerson element bytes
 * @param singleLogoutServices       ordered supported logout endpoints
 * @param nameIdFormats              ordered supported NameID format URIs
 * @param singleSignOnServices       one or more SSO endpoints
 * @param wantAuthnRequestsSigned    optional lexical-presence signing requirement
 * @author Kimi Liu
 */
public record IdpSsoDescriptor(Optional<String> id, Optional<Instant> validUntil, Optional<String> cacheDuration,
        List<String> protocolSupportEnumeration, Optional<String> errorUrl, Optional<byte[]> signature,
        List<byte[]> extensions, List<KeyDescriptor> keys, Optional<byte[]> organization, List<byte[]> contacts,
        List<SingleLogoutServiceEndpoint> singleLogoutServices, List<String> nameIdFormats,
        List<SingleSignOnServiceEndpoint> singleSignOnServices, Optional<Boolean> wantAuthnRequestsSigned) {

    /**
     * SAML 2.0 protocol namespace required by the represented IdP SSO role.
     */
    public static final String PROTOCOL = Saml.Namespaces.PROTOCOL;

    /**
     * Validates role cardinality and freezes all lists and XML values.
     *
     * @throws IllegalArgumentException if a component, item, or optional container is null
     * @throws ValidateException        if the descriptor cannot represent an executable SAML 2.0 IdP role
     */
    public IdpSsoDescriptor {
        id = text(id, "SAML IDPSSODescriptor ID");
        validUntil = optional(validUntil, "SAML IDPSSODescriptor validUntil container must not be null");
        cacheDuration = text(cacheDuration, "SAML IDPSSODescriptor cacheDuration");
        protocolSupportEnumeration = strings(
                protocolSupportEnumeration,
                "SAML IDPSSODescriptor protocolSupportEnumeration",
                true);
        if (!protocolSupportEnumeration.contains(PROTOCOL)) {
            throw new ValidateException("SAML IDPSSODescriptor must support the SAML 2.0 protocol namespace");
        }
        errorUrl = text(errorUrl, "SAML IDPSSODescriptor errorURL");
        signature = bytes(signature, "SAML IDPSSODescriptor Signature");
        extensions = elements(extensions, "SAML IDPSSODescriptor extension");
        keys = objects(keys, "SAML IDPSSODescriptor KeyDescriptor", false);
        organization = bytes(organization, "SAML IDPSSODescriptor Organization");
        contacts = elements(contacts, "SAML IDPSSODescriptor ContactPerson");
        singleLogoutServices = objects(singleLogoutServices, "SAML IDPSSODescriptor SingleLogoutService", false);
        nameIdFormats = strings(nameIdFormats, "SAML IDPSSODescriptor NameIDFormat", false);
        singleSignOnServices = objects(singleSignOnServices, "SAML IDPSSODescriptor SingleSignOnService", true);
        Assert.notNull(
                wantAuthnRequestsSigned,
                "SAML IDPSSODescriptor WantAuthnRequestsSigned container must not be null");
        wantAuthnRequestsSigned = Optional.ofNullable(wantAuthnRequestsSigned.getOrNull());
    }

    /**
     * Normalizes one optional non-empty string.
     *
     * @param value optional string value
     * @param label diagnostic role component label
     * @return normalized optional non-empty string
     */
    private static Optional<String> text(final Optional<String> value, final String label) {
        Assert.notNull(value, label + " container must not be null");
        final String actual = value.getOrNull();
        if (actual != null)
            Assert.notEmpty(actual, label + " must not be empty");
        return Optional.ofNullable(actual);
    }

    /**
     * Normalizes a generic optional value.
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
     * Deep-copies one optional XML element.
     *
     * @param value optional XML element bytes
     * @param label diagnostic role component label
     * @return normalized optional with detached element bytes
     */
    private static Optional<byte[]> bytes(final Optional<byte[]> value, final String label) {
        Assert.notNull(value, label + " container must not be null");
        final byte[] actual = value.getOrNull();
        if (actual != null && actual.length == 0)
            throw new ValidateException(label + " must not be empty");
        return Optional.ofNullable(actual == null ? null : actual.clone());
    }

    /**
     * Returns a defensive copy of one normalized optional XML element.
     *
     * @param value normalized optional element bytes
     * @return optional element bytes detached from retained role state
     */
    private static Optional<byte[]> copy(final Optional<byte[]> value) {
        final byte[] actual = value.getOrNull();
        return Optional.ofNullable(actual == null ? null : actual.clone());
    }

    /**
     * Deep-copies ordered XML elements.
     *
     * @param source ordered complete XML element bytes
     * @param label  diagnostic role component label
     * @return immutable ordered list containing detached element bytes
     */
    private static List<byte[]> elements(final List<byte[]> source, final String label) {
        Assert.notNull(source, label + " list must not be null");
        final List<byte[]> result = new ArrayList<>(source.size());
        for (byte[] item : source) {
            final byte[] actual = Assert.notNull(item, label + " must not be null");
            if (actual.length == 0)
                throw new ValidateException(label + " must not be empty");
            result.add(actual.clone());
        }
        return List.copyOf(result);
    }

    /**
     * Freezes a typed list and optionally requires content.
     *
     * @param <T>      list element type
     * @param source   ordered source elements
     * @param label    diagnostic role component label
     * @param required whether at least one element is required
     * @return immutable ordered element list
     */
    private static <T> List<T> objects(final List<T> source, final String label, final boolean required) {
        Assert.notNull(source, label + " list must not be null");
        if (required && source.isEmpty())
            throw new ValidateException(label + " list must not be empty");
        for (T item : source)
            Assert.notNull(item, label + " must not be null");
        return List.copyOf(source);
    }

    /**
     * Freezes a string list and optionally requires content.
     *
     * @param source   ordered string values
     * @param label    diagnostic role component label
     * @param required whether at least one value is required
     * @return immutable ordered non-blank string list
     */
    private static List<String> strings(final List<String> source, final String label, final boolean required) {
        Assert.notNull(source, label + " list must not be null");
        if (required && source.isEmpty())
            throw new ValidateException(label + " list must not be empty");
        for (String item : source)
            Assert.notBlank(item, label + " must not contain blank values");
        return List.copyOf(source);
    }

    /**
     * Returns a defensive copy of optional Signature XML.
     *
     * @return optional complete Signature element with caller-owned bytes
     */
    @Override
    public Optional<byte[]> signature() {
        return copy(signature);
    }

    /**
     * Returns defensive copies of role extension elements.
     *
     * @return immutable ordered extension list whose byte arrays are caller-owned
     */
    @Override
    public List<byte[]> extensions() {
        return elements(extensions, "SAML IDPSSODescriptor extension");
    }

    /**
     * Returns a defensive copy of optional Organization XML.
     *
     * @return optional complete Organization element with caller-owned bytes
     */
    @Override
    public Optional<byte[]> organization() {
        return copy(organization);
    }

    /**
     * Returns defensive copies of ContactPerson XML elements.
     *
     * @return immutable ordered contact list whose byte arrays are caller-owned
     */
    @Override
    public List<byte[]> contacts() {
        return elements(contacts, "SAML IDPSSODescriptor ContactPerson");
    }

}
