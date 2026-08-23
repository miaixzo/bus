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
 * Models SAML protocol {@code LogoutResponse}, whose type is {@code StatusResponseType}.
 *
 * @param id           required XML ID
 * @param inResponseTo optional request correlation NCName
 * @param version      required protocol version, fixed to {@code 2.0}
 * @param issueInstant required issue instant
 * @param destination  optional destination URI
 * @param consent      optional consent URI
 * @param issuer       optional issuer child
 * @param signature    optional complete {@code ds:Signature} element bytes
 * @param extensions   ordered complete protocol extension elements
 * @param status       required protocol status
 * @author Kimi Liu
 */
public record LogoutResponse(String id, Optional<String> inResponseTo, String version, Instant issueInstant,
        Optional<String> destination, Optional<String> consent, Optional<Issuer> issuer, Optional<byte[]> signature,
        List<byte[]> extensions, Status status) {

    /**
     * Validates status-response attributes and takes immutable ownership of XML elements.
     *
     * @throws IllegalArgumentException if a required component, item, or optional container is {@code null}
     * @throws ValidateException        if version, NCName, URI, or retained XML is invalid
     */
    public LogoutResponse {
        id = ncName(id, "SAML LogoutResponse ID");
        inResponseTo = text(inResponseTo, "SAML LogoutResponse InResponseTo");
        if (inResponseTo.isPresent()) {
            ncName(inResponseTo.getOrNull(), "SAML LogoutResponse InResponseTo");
        }
        if (!"2.0".equals(version)) {
            throw new ValidateException("SAML LogoutResponse Version must be 2.0");
        }
        Assert.notNull(issueInstant, "SAML LogoutResponse IssueInstant must not be null");
        destination = uri(destination, "SAML LogoutResponse Destination");
        consent = uri(consent, "SAML LogoutResponse Consent");
        Assert.notNull(issuer, "SAML LogoutResponse Issuer container must not be null");
        issuer = Optional.ofNullable(issuer.getOrNull());
        signature = bytes(signature, "SAML LogoutResponse Signature");
        extensions = elements(extensions, "SAML LogoutResponse extension");
        Assert.notNull(status, "SAML LogoutResponse Status must not be null");
    }

    /**
     * Validates one XML NCName.
     *
     * @param value XML identifier lexical value
     * @param label diagnostic response component label
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
     * Normalizes one optional non-empty string.
     *
     * @param value optional string value
     * @param label diagnostic response component label
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
     * Normalizes one optional absolute URI.
     *
     * @param value optional URI lexical value
     * @param label diagnostic response component label
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
     * Deep-copies one optional complete XML element.
     *
     * @param value optional XML element bytes
     * @param label diagnostic response component label
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
     * @param label  diagnostic response component label
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
     * @return optional complete Signature element with caller-owned bytes
     */
    @Override
    public Optional<byte[]> signature() {
        final byte[] value = signature.getOrNull();
        return Optional.ofNullable(value == null ? null : value.clone());
    }

    /**
     * Returns defensive copies of ordered Extensions children.
     *
     * @return immutable ordered extension list whose byte arrays are caller-owned
     */
    @Override
    public List<byte[]> extensions() {
        return elements(extensions, "SAML LogoutResponse extension");
    }

}
