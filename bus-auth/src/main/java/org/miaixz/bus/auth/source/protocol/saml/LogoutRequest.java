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
 * Models SAML protocol {@code LogoutRequestType} over its inherited request attributes.
 *
 * @param id             required XML ID
 * @param version        required protocol version, fixed to {@code 2.0}
 * @param issueInstant   required issue instant
 * @param destination    optional destination URI
 * @param consent        optional consent URI
 * @param issuer         optional issuer child
 * @param signature      optional complete {@code ds:Signature} element bytes
 * @param extensions     ordered complete protocol extension elements
 * @param identifier     required BaseID, NameID, or EncryptedID choice
 * @param sessionIndexes ordered optional session index values
 * @param reason         optional logout-reason lexical value
 * @param notOnOrAfter   optional request expiration
 * @author Kimi Liu
 */
public record LogoutRequest(String id, String version, Instant issueInstant, Optional<String> destination,
        Optional<String> consent, Optional<Issuer> issuer, Optional<byte[]> signature, List<byte[]> extensions,
        Subject.Identifier identifier, List<String> sessionIndexes, Optional<String> reason,
        Optional<Instant> notOnOrAfter) {

    /**
     * Validates inherited request fields and freezes the identifier/session content.
     *
     * @throws IllegalArgumentException if a required component, item, or optional container is {@code null}
     * @throws ValidateException        if version, ID, URI, expiration, or retained XML is invalid
     */
    public LogoutRequest {
        id = ncName(id, "SAML LogoutRequest ID");
        if (!"2.0".equals(version)) {
            throw new ValidateException("SAML LogoutRequest Version must be 2.0");
        }
        Assert.notNull(issueInstant, "SAML LogoutRequest IssueInstant must not be null");
        destination = uri(destination, "SAML LogoutRequest Destination");
        consent = uri(consent, "SAML LogoutRequest Consent");
        issuer = optional(issuer, "SAML LogoutRequest Issuer container must not be null");
        signature = bytes(signature, "SAML LogoutRequest Signature");
        extensions = elements(extensions, "SAML LogoutRequest extension");
        Assert.notNull(identifier, "SAML LogoutRequest identifier must not be null");
        Assert.notNull(sessionIndexes, "SAML LogoutRequest SessionIndex list must not be null");
        for (String sessionIndex : sessionIndexes) {
            Assert.notEmpty(
                    Assert.notNull(sessionIndex, "SAML LogoutRequest SessionIndex must not be null"),
                    "SAML LogoutRequest SessionIndex must not be empty");
        }
        sessionIndexes = List.copyOf(sessionIndexes);
        reason = text(reason, "SAML LogoutRequest Reason");
        Assert.notNull(notOnOrAfter, "SAML LogoutRequest NotOnOrAfter container must not be null");
        notOnOrAfter = Optional.ofNullable(notOnOrAfter.getOrNull());
        if (notOnOrAfter.isPresent() && !issueInstant.isBefore(notOnOrAfter.getOrNull())) {
            throw new ValidateException("SAML LogoutRequest NotOnOrAfter must follow IssueInstant");
        }
    }

    /**
     * Validates one XML NCName.
     *
     * @param value XML identifier lexical value
     * @param label diagnostic request component label
     * @return validated non-blank NCName
     */
    private static String ncName(final String value, final String label) {
        final String actual = Assert.notBlank(value, label + " must not be blank");
        if (!actual.matches("[A-Za-z_][A-Za-z0-9._-]*")) {
            throw new ValidateException(label + " must be an XML NCName");
        }
        return actual;
    }

    /**
     * Normalizes one optional absolute URI.
     *
     * @param value optional URI lexical value
     * @param label diagnostic request component label
     * @return normalized optional absolute URI
     */
    private static Optional<String> uri(final Optional<String> value, final String label) {
        final Optional<String> result = text(value, label);
        final String actual = result.getOrNull();
        if (actual != null) {
            try {
                if (!new URI(actual).isAbsolute()) {
                    throw new ValidateException(label + " must be absolute");
                }
            } catch (URISyntaxException exception) {
                throw new ValidateException(label + " is not a valid URI", exception);
            }
        }
        return result;
    }

    /**
     * Normalizes one optional non-empty string.
     *
     * @param value optional string value
     * @param label diagnostic request component label
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
     * Normalizes one generic optional container.
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
     * Deep-copies one optional complete XML element.
     *
     * @param value optional XML element bytes
     * @param label diagnostic request component label
     * @return normalized optional with detached element bytes
     */
    private static Optional<byte[]> bytes(final Optional<byte[]> value, final String label) {
        Assert.notNull(value, label + " container must not be null");
        final byte[] actual = value.getOrNull();
        if (actual != null && actual.length == 0) {
            throw new ValidateException(label + " must not be empty");
        }
        return Optional.ofNullable(actual == null ? null : actual.clone());
    }

    /**
     * Deep-copies ordered complete XML elements.
     *
     * @param source ordered extension element bytes
     * @param label  diagnostic request component label
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
     * Returns a defensive copy of optional Signature XML.
     *
     * @return optional copied Signature element
     */
    @Override
    public Optional<byte[]> signature() {
        final byte[] value = signature.getOrNull();
        return Optional.ofNullable(value == null ? null : value.clone());
    }

    /**
     * Returns defensive copies of ordered Extensions children.
     *
     * @return immutable list of copied XML elements
     */
    @Override
    public List<byte[]> extensions() {
        return elements(extensions, "SAML LogoutRequest extension");
    }

}
