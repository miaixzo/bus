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

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;

/**
 * Represents the RFC 4511 {@code BindResponse} protocol operation with application tag 1.
 *
 * @param result                common LDAP result components
 * @param serverSaslCredentials optional opaque SASL challenge or final server data
 * @author Kimi Liu
 */
public record BindResponse(LdapResult result, Optional<byte[]> serverSaslCredentials)
        implements LdapMessage.ProtocolOp {

    /**
     * Creates an immutable bind response and preserves present-empty SASL credentials.
     *
     * @param result                common LDAP result
     * @param serverSaslCredentials optional server SASL credentials
     * @throws IllegalArgumentException if a component is {@code null}
     */
    public BindResponse {
        Assert.notNull(result, "LDAP bind result must not be null");
        Assert.notNull(serverSaslCredentials, "LDAP server SASL credential option must not be null");
        serverSaslCredentials = copy(serverSaslCredentials);
    }

    /**
     * Clones optional server SASL credentials.
     *
     * @param source source optional credentials
     * @return independent optional credentials
     */
    private static Optional<byte[]> copy(final Optional<byte[]> source) {
        return source.isEmpty() ? Optional.empty() : Optional.of(source.getOrThrow().clone());
    }

    /**
     * Returns separately owned server SASL credentials.
     *
     * @return empty option or a new copy of the credential octets
     */
    @Override
    public Optional<byte[]> serverSaslCredentials() {
        return copy(serverSaslCredentials);
    }

    /**
     * Compares bind responses using server credential content.
     *
     * @param other candidate object
     * @return {@code true} when result, presence, and credential octets are equal
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof BindResponse that) || !result.equals(that.result)
                || serverSaslCredentials.isEmpty() != that.serverSaslCredentials.isEmpty()) {
            return false;
        }
        return serverSaslCredentials.isEmpty()
                || Arrays.equals(serverSaslCredentials.getOrThrow(), that.serverSaslCredentials.getOrThrow());
    }

    /**
     * Computes a content-based bind-response hash.
     *
     * @return result and credential-content hash
     */
    @Override
    public int hashCode() {
        return 31 * Objects.hashCode(result)
                + (serverSaslCredentials.isEmpty() ? 0 : Arrays.hashCode(serverSaslCredentials.getOrThrow()));
    }

    /**
     * Returns a redacted bind-response description.
     *
     * @return result and credential-presence description
     */
    @Override
    public String toString() {
        return "BindResponse[result=" + result + ", serverSaslCredentialsPresent=" + !serverSaslCredentials.isEmpty()
                + Symbol.BRACKET_RIGHT;
    }

}
