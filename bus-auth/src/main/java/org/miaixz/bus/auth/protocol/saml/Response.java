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
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Models the SAML 2.0 protocol {@code ResponseType} and preserves its repeated assertion CHOICE order.
 *
 * @param id           required XML ID
 * @param inResponseTo optional correlated request NCName
 * @param version      required protocol version, fixed to {@code 2.0}
 * @param issueInstant required issue instant
 * @param destination  optional destination URI
 * @param consent      optional consent URI
 * @param issuer       optional issuer child
 * @param signature    optional complete {@code ds:Signature} element bytes
 * @param extensions   ordered complete children of {@code samlp:Extensions}
 * @param status       required protocol status
 * @param assertions   ordered plain or encrypted assertion children
 * @author Kimi Liu
 */
public record Response(String id, Optional<String> inResponseTo, String version, Instant issueInstant,
        Optional<String> destination, Optional<String> consent, Optional<Issuer> issuer, Optional<byte[]> signature,
        List<byte[]> extensions, Status status, List<AssertionContent> assertions) {

    /**
     * Validates status-response attributes and freezes the repeated assertion choice.
     *
     * @throws IllegalArgumentException if a required component, item, or optional container is {@code null}
     * @throws ValidateException        if a protocol version, NCName, URI, or XML byte element is invalid
     */
    public Response {
        id = ncName(id, "SAML Response ID");
        inResponseTo = optionalText(inResponseTo, "SAML Response InResponseTo");
        if (inResponseTo.isPresent()) {
            ncName(inResponseTo.getOrNull(), "SAML Response InResponseTo");
        }
        if (!"2.0".equals(version)) {
            throw new ValidateException("SAML Response Version must be 2.0");
        }
        Assert.notNull(issueInstant, "SAML Response IssueInstant must not be null");
        destination = uri(destination, "SAML Response Destination");
        consent = uri(consent, "SAML Response Consent");
        Assert.notNull(issuer, "SAML Response Issuer container must not be null");
        issuer = Optional.ofNullable(issuer.getOrNull());
        signature = bytes(signature, "SAML Response Signature");
        extensions = elements(extensions, "SAML Response extension");
        Assert.notNull(status, "SAML Response Status must not be null");
        Assert.notNull(assertions, "SAML Response assertion content list must not be null");
        for (AssertionContent assertion : assertions) {
            Assert.notNull(assertion, "SAML Response assertion content must not be null");
        }
        assertions = List.copyOf(assertions);
    }

    /**
     * Validates one SAML XML NCName.
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
     * Normalizes one optional non-empty string.
     *
     * @param value optional source value
     * @param label safe diagnostic label
     * @return normalized optional value
     */
    private static Optional<String> optionalText(final Optional<String> value, final String label) {
        Assert.notNull(value, label + " container must not be null");
        final String actual = value.getOrNull();
        if (actual != null) {
            Assert.notEmpty(actual, label + " must not be empty");
        }
        return Optional.ofNullable(actual);
    }

    /**
     * Normalizes one optional absolute URI.
     *
     * @param value optional URI
     * @param label safe diagnostic label
     * @return normalized optional URI
     */
    private static Optional<String> uri(final Optional<String> value, final String label) {
        final Optional<String> normalized = optionalText(value, label);
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
     * Deep-copies an optional XML element.
     *
     * @param value optional XML bytes
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
     * Returns a defensive copy of the optional XML Signature element.
     *
     * @return optional copied signature bytes
     */
    @Override
    public Optional<byte[]> signature() {
        final byte[] value = signature.getOrNull();
        return Optional.ofNullable(value == null ? null : value.clone());
    }

    /**
     * Returns defensive copies of the ordered protocol extension elements.
     *
     * @return immutable list of copied XML elements
     */
    @Override
    public List<byte[]> extensions() {
        return elements(extensions, "SAML Response extension");
    }

    /**
     * Seals the repeated assertion-schema CHOICE accepted by {@code ResponseType}.
     *
     * @author Kimi Liu
     */
    public interface AssertionContent {

    }

    /**
     * Preserves one decoded {@code saml:Assertion} child.
     *
     * @param assertion typed assertion
     * @author Kimi Liu
     */
    public record PlainAssertion(Assertion assertion) implements AssertionContent {

        /**
         * Requires a non-null typed assertion.
         *
         * @throws IllegalArgumentException if {@code assertion} is {@code null}
         */
        public PlainAssertion {
            Assert.notNull(assertion, "SAML plain Assertion must not be null");
        }

    }

    /**
     * Preserves one complete {@code saml:EncryptedAssertion} element for explicit decryption.
     *
     * @param xml secure namespace-aware XML serialization
     * @author Kimi Liu
     */
    public record EncryptedAssertion(byte[] xml) implements AssertionContent {

        /**
         * Takes ownership through defensive copying.
         *
         * @throws IllegalArgumentException if {@code xml} is {@code null}
         * @throws ValidateException        if {@code xml} is empty
         */
        public EncryptedAssertion {
            final byte[] value = Assert.notNull(xml, "SAML EncryptedAssertion XML must not be null");
            if (value.length == 0) {
                throw new ValidateException("SAML EncryptedAssertion XML must not be empty");
            }
            xml = value.clone();
        }

        /**
         * Returns a defensive copy of the encrypted XML element.
         *
         * @return copied XML bytes
         */
        @Override
        public byte[] xml() {
            return xml.clone();
        }

    }

}
