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
package org.miaixz.bus.auth.protocol.ldap;

import java.util.Arrays;
import java.util.Objects;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;

/**
 * Represents the RFC 4511 {@code ExtendedRequest} protocol operation with application tag 23.
 *
 * @param requestName  numeric object identifier naming the extended operation
 * @param requestValue optional opaque operation-specific request value
 * @author Kimi Liu
 */
public record ExtendedRequest(String requestName, Optional<byte[]> requestValue) implements LdapMessage.ProtocolOp {

    /**
     * RFC 4511 StartTLS extended-operation object identifier.
     */
    public static final String START_TLS_OID = "1.3.6.1.4.1.1466.20037";

    /**
     * Creates an immutable extended request and applies the standard StartTLS value constraint.
     *
     * @param requestName  extended-operation OID
     * @param requestValue optional operation value
     * @throws IllegalArgumentException if the OID or option is invalid, or StartTLS carries a request value
     */
    public ExtendedRequest {
        Control.requireNumericOid(requestName);
        Assert.notNull(requestValue, "LDAP extended request value option must not be null");
        Assert.isTrue(
                !START_TLS_OID.equals(requestName) || requestValue.isEmpty(),
                "LDAP StartTLS request must not carry a request value");
        requestValue = copy(requestValue);
    }

    /**
     * Clones optional extended-operation bytes.
     *
     * @param source source optional value
     * @return independent optional value
     */
    private static Optional<byte[]> copy(final Optional<byte[]> source) {
        return source.isEmpty() ? Optional.empty() : Optional.of(source.getOrThrow().clone());
    }

    /**
     * Returns a separately owned optional extended-operation value.
     *
     * @return empty option or newly allocated value octets
     */
    @Override
    public Optional<byte[]> requestValue() {
        return copy(requestValue);
    }

    /**
     * Compares extended requests using request-value content.
     *
     * @param other candidate object
     * @return {@code true} when OID, presence, and octets are equal
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ExtendedRequest that) || !requestName.equals(that.requestName)
                || requestValue.isEmpty() != that.requestValue.isEmpty()) {
            return false;
        }
        return requestValue.isEmpty() || Arrays.equals(requestValue.getOrThrow(), that.requestValue.getOrThrow());
    }

    /**
     * Computes a content-based extended-request hash.
     *
     * @return OID and request-value hash
     */
    @Override
    public int hashCode() {
        return 31 * Objects.hashCode(requestName)
                + (requestValue.isEmpty() ? 0 : Arrays.hashCode(requestValue.getOrThrow()));
    }

    /**
     * Returns a structural description without exposing the request value.
     *
     * @return request OID and value-presence description
     */
    @Override
    public String toString() {
        return "ExtendedRequest[requestName=" + requestName + ", requestValuePresent=" + !requestValue.isEmpty()
                + Symbol.BRACKET_RIGHT;
    }

}
