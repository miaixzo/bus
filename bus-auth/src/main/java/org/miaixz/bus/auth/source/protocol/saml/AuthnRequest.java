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
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Models the SAML 2.0 protocol {@code AuthnRequestType} used by the Web Browser SSO profile.
 * <p>
 * XML Signature, Extensions, and the optional Scoping child retain secure namespace-aware element bytes so signature
 * processing and unsupported optional content are not normalized into unsafe object maps.
 * </p>
 *
 * @param id                             required XML ID
 * @param version                        required protocol version, fixed to {@code 2.0}
 * @param issueInstant                   required issue instant
 * @param destination                    optional destination URI
 * @param consent                        optional consent URI
 * @param issuer                         optional issuer child
 * @param signature                      optional complete {@code ds:Signature} element bytes
 * @param extensions                     ordered complete children of {@code samlp:Extensions}
 * @param subject                        optional requested subject
 * @param nameIdPolicy                   optional Name Identifier policy
 * @param conditions                     optional requested assertion conditions
 * @param requestedAuthnContext          optional requested authentication context
 * @param scoping                        optional complete {@code samlp:Scoping} element bytes
 * @param forceAuthn                     optional lexical-presence value of {@code ForceAuthn}
 * @param passive                        optional lexical-presence value of {@code IsPassive}
 * @param protocolBinding                optional requested response binding URI
 * @param assertionConsumerServiceIndex  optional metadata endpoint index
 * @param assertionConsumerServiceUrl    optional explicit assertion consumer URL
 * @param attributeConsumingServiceIndex optional metadata attribute service index
 * @param providerName                   optional human-readable requester name
 * @author Kimi Liu
 */
public record AuthnRequest(String id, String version, Instant issueInstant, Optional<String> destination,
        Optional<String> consent, Optional<Issuer> issuer, Optional<byte[]> signature, List<byte[]> extensions,
        Optional<Subject> subject, Optional<NameIDPolicy> nameIdPolicy, Optional<Conditions> conditions,
        Optional<RequestedAuthnContext> requestedAuthnContext, Optional<byte[]> scoping, Optional<Boolean> forceAuthn,
        Optional<Boolean> passive, Optional<SamlBinding> protocolBinding,
        Optional<Integer> assertionConsumerServiceIndex, Optional<String> assertionConsumerServiceUrl,
        Optional<Integer> attributeConsumingServiceIndex, Optional<String> providerName) {

    /**
     * Validates protocol identity, URI, index, and mutually exclusive consumer-service addressing rules.
     *
     * @throws IllegalArgumentException if a component or optional container is {@code null}
     * @throws ValidateException        if a protocol invariant or URI/index constraint is invalid
     */
    public AuthnRequest {
        id = ncName(id, "SAML AuthnRequest ID");
        if (!"2.0".equals(version)) {
            throw new ValidateException("SAML AuthnRequest Version must be 2.0");
        }
        Assert.notNull(issueInstant, "SAML AuthnRequest IssueInstant must not be null");
        destination = uri(destination, "SAML AuthnRequest Destination");
        consent = uri(consent, "SAML AuthnRequest Consent");
        issuer = optional(issuer, "SAML AuthnRequest Issuer container must not be null");
        signature = bytes(signature, "SAML AuthnRequest Signature");
        extensions = elements(extensions, "SAML AuthnRequest extension");
        subject = optional(subject, "SAML AuthnRequest Subject container must not be null");
        nameIdPolicy = optional(nameIdPolicy, "SAML AuthnRequest NameIDPolicy container must not be null");
        conditions = optional(conditions, "SAML AuthnRequest Conditions container must not be null");
        requestedAuthnContext = optional(
                requestedAuthnContext,
                "SAML AuthnRequest RequestedAuthnContext container must not be null");
        scoping = bytes(scoping, "SAML AuthnRequest Scoping");
        forceAuthn = optional(forceAuthn, "SAML AuthnRequest ForceAuthn container must not be null");
        passive = optional(passive, "SAML AuthnRequest IsPassive container must not be null");
        protocolBinding = optional(protocolBinding, "SAML AuthnRequest ProtocolBinding container must not be null");
        assertionConsumerServiceIndex = index(
                assertionConsumerServiceIndex,
                "SAML AuthnRequest AssertionConsumerServiceIndex");
        assertionConsumerServiceUrl = uri(assertionConsumerServiceUrl, "SAML AuthnRequest AssertionConsumerServiceURL");
        attributeConsumingServiceIndex = index(
                attributeConsumingServiceIndex,
                "SAML AuthnRequest AttributeConsumingServiceIndex");
        providerName = text(providerName, "SAML AuthnRequest ProviderName");
        if (assertionConsumerServiceIndex.isPresent() && assertionConsumerServiceUrl.isPresent()) {
            throw new ValidateException("SAML AuthnRequest cannot contain both AssertionConsumerServiceIndex and URL");
        }
        if (assertionConsumerServiceIndex.isPresent() && protocolBinding.isPresent()) {
            throw new ValidateException(
                    "SAML AuthnRequest ProtocolBinding cannot accompany AssertionConsumerServiceIndex");
        }
    }

    /**
     * Validates one XML NCName using the subset required for SAML IDs.
     *
     * @param value candidate value
     * @param label safe diagnostic label
     * @return unchanged validated value
     */
    private static String ncName(final String value, final String label) {
        final String actual = Assert.notBlank(value, label + " must not be blank");
        if (!actual.matches("[A-Za-z_][A-Za-z0-9._-]*")) {
            throw new ValidateException(label + " must be an XML NCName");
        }
        return actual;
    }

    /**
     * Normalizes an optional absolute URI.
     *
     * @param value optional URI
     * @param label safe diagnostic label
     * @return normalized optional URI
     */
    private static Optional<String> uri(final Optional<String> value, final String label) {
        final Optional<String> normalized = text(value, label);
        final String actual = normalized.getOrNull();
        if (actual != null) {
            try {
                if (!new URI(actual).isAbsolute()) {
                    throw new ValidateException(label + " must be absolute");
                }
            } catch (URISyntaxException exception) {
                throw new ValidateException(label + " is not a valid URI", exception);
            }
        }
        return normalized;
    }

    /**
     * Normalizes a schema-optional non-empty string.
     *
     * @param value optional value
     * @param label safe diagnostic label
     * @return normalized optional value
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
     * Normalizes an optional unsigned-short metadata index.
     *
     * @param value optional index
     * @param label safe diagnostic label
     * @return normalized optional index
     */
    private static Optional<Integer> index(final Optional<Integer> value, final String label) {
        Assert.notNull(value, label + " container must not be null");
        final Integer actual = value.getOrNull();
        if (actual != null && (actual < 0 || actual > 65_535)) {
            throw new ValidateException(label + " must be an XML unsignedShort");
        }
        return Optional.ofNullable(actual);
    }

    /**
     * Normalizes a generic optional container.
     *
     * @param value   optional container
     * @param message null-container diagnostic
     * @param <T>     contained value type
     * @return normalized optional value
     */
    private static <T> Optional<T> optional(final Optional<T> value, final String message) {
        Assert.notNull(value, message);
        return Optional.ofNullable(value.getOrNull());
    }

    /**
     * Deep-copies an optional XML element.
     *
     * @param value optional bytes
     * @param label safe element label
     * @return normalized copied optional bytes
     */
    private static Optional<byte[]> bytes(final Optional<byte[]> value, final String label) {
        Assert.notNull(value, label + " container must not be null");
        final byte[] actual = value.getOrNull();
        if (actual != null && actual.length == 0) {
            throw new ValidateException(label + " element must not be empty");
        }
        return Optional.ofNullable(actual == null ? null : actual.clone());
    }

    /**
     * Deep-copies ordered XML elements.
     *
     * @param values source elements
     * @param label  safe item label
     * @return immutable deep copy
     */
    private static List<byte[]> elements(final List<byte[]> values, final String label) {
        Assert.notNull(values, label + " list must not be null");
        final List<byte[]> copy = new ArrayList<>(values.size());
        for (byte[] value : values) {
            final byte[] actual = Assert.notNull(value, label + " must not be null");
            if (actual.length == 0) {
                throw new ValidateException(label + " must not be empty");
            }
            copy.add(actual.clone());
        }
        return List.copyOf(copy);
    }

    /**
     * Copies an already normalized optional byte sequence.
     *
     * @param value optional owned byte sequence
     * @return optional defensive copy
     */
    private static Optional<byte[]> copied(final Optional<byte[]> value) {
        final byte[] actual = value.getOrNull();
        return Optional.ofNullable(actual == null ? null : actual.clone());
    }

    /**
     * Returns a defensive copy of the optional XML Signature element.
     *
     * @return optional copied signature bytes
     */
    @Override
    public Optional<byte[]> signature() {
        return copied(signature);
    }

    /**
     * Returns defensive copies of all ordered extension elements.
     *
     * @return immutable list of copied XML elements
     */
    @Override
    public List<byte[]> extensions() {
        return elements(extensions, "SAML AuthnRequest extension");
    }

    /**
     * Returns a defensive copy of the optional Scoping element.
     *
     * @return optional copied Scoping bytes
     */
    @Override
    public Optional<byte[]> scoping() {
        return copied(scoping);
    }

}
