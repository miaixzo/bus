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
import java.util.Objects;
import java.util.regex.Pattern;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;

/**
 * Represents the RFC 4511 {@code SaslCredentials} sequence used by the bind authentication choice.
 *
 * @param mechanism   registered RFC 4422 SASL mechanism name
 * @param credentials optional opaque mechanism-specific client response
 * @author Kimi Liu
 */
public record SaslCredentials(String mechanism, Optional<byte[]> credentials) {

    /**
     * RFC 4422 SASL mechanism-name grammar and twenty-character length bound.
     */
    private static final Pattern MECHANISM = Pattern.compile("[A-Z0-9_-]{1,20}");

    /**
     * Creates immutable SASL credentials while preserving absence separately from a present empty response.
     *
     * @param mechanism   SASL mechanism name
     * @param credentials optional mechanism credentials
     * @throws IllegalArgumentException if the mechanism or optional value is invalid
     */
    public SaslCredentials {
        Assert.notBlank(mechanism, "LDAP SASL mechanism must not be blank");
        Assert.isTrue(MECHANISM.matcher(mechanism).matches(), "LDAP SASL mechanism name is malformed");
        Assert.notNull(credentials, "LDAP SASL credential option must not be null");
        credentials = copy(credentials);
    }

    /**
     * Clones an optional SASL response while retaining present-empty semantics.
     *
     * @param source source optional bytes
     * @return independent optional bytes
     */
    private static Optional<byte[]> copy(final Optional<byte[]> source) {
        return source.isEmpty() ? Optional.empty() : Optional.of(source.getOrThrow().clone());
    }

    /**
     * Returns a separately owned optional SASL response.
     *
     * @return empty option or newly allocated credentials
     */
    @Override
    public Optional<byte[]> credentials() {
        return copy(credentials);
    }

    /**
     * Compares SASL credentials using OCTET STRING content.
     *
     * @param other candidate object
     * @return {@code true} when mechanism, presence, and credential bytes are equal
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SaslCredentials that) || !mechanism.equals(that.mechanism)
                || credentials.isEmpty() != that.credentials.isEmpty()) {
            return false;
        }
        return credentials.isEmpty() || Arrays.equals(credentials.getOrThrow(), that.credentials.getOrThrow());
    }

    /**
     * Computes a content-based hash without exposing credentials.
     *
     * @return SASL credential hash
     */
    @Override
    public int hashCode() {
        return 31 * Objects.hashCode(mechanism)
                + (credentials.isEmpty() ? 0 : Arrays.hashCode(credentials.getOrThrow()));
    }

    /**
     * Returns a redacted SASL credential description.
     *
     * @return mechanism and credential-presence description
     */
    @Override
    public String toString() {
        return "SaslCredentials[mechanism=" + mechanism + ", credentialsPresent=" + !credentials.isEmpty()
                + Symbol.BRACKET_RIGHT;
    }

}
