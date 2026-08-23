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
 * Represents the RFC 4511 {@code IntermediateResponse} protocol operation with application tag 25.
 *
 * @param responseName  optional numeric object identifier identifying the intermediate response syntax
 * @param responseValue optional opaque operation-specific value
 * @author Kimi Liu
 */
public record IntermediateResponse(Optional<String> responseName, Optional<byte[]> responseValue)
        implements LdapMessage.ProtocolOp {

    /**
     * Creates an immutable intermediate response while retaining independently optional name and value fields.
     *
     * @param responseName  optional response OID
     * @param responseValue optional response value
     * @throws IllegalArgumentException if an option is null or a present OID is malformed
     */
    public IntermediateResponse {
        Assert.notNull(responseName, "LDAP intermediate response name option must not be null");
        Assert.notNull(responseValue, "LDAP intermediate response value option must not be null");
        if (!responseName.isEmpty()) {
            Control.requireNumericOid(responseName.getOrThrow());
        }
        responseValue = copy(responseValue);
    }

    /**
     * Clones optional intermediate-response bytes.
     *
     * @param source source optional value
     * @return independent optional value
     */
    private static Optional<byte[]> copy(final Optional<byte[]> source) {
        return source.isEmpty() ? Optional.empty() : Optional.of(source.getOrThrow().clone());
    }

    /**
     * Returns a separately owned optional intermediate-response value.
     *
     * @return empty option or newly allocated response octets
     */
    @Override
    public Optional<byte[]> responseValue() {
        return copy(responseValue);
    }

    /**
     * Compares intermediate responses using response-value content.
     *
     * @param other candidate object
     * @return {@code true} when response name, presence, and bytes are equal
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof IntermediateResponse that) || !responseName.equals(that.responseName)
                || responseValue.isEmpty() != that.responseValue.isEmpty()) {
            return false;
        }
        return responseValue.isEmpty() || Arrays.equals(responseValue.getOrThrow(), that.responseValue.getOrThrow());
    }

    /**
     * Computes a content-based intermediate-response hash.
     *
     * @return response-name and response-value hash
     */
    @Override
    public int hashCode() {
        return 31 * Objects.hashCode(responseName)
                + (responseValue.isEmpty() ? 0 : Arrays.hashCode(responseValue.getOrThrow()));
    }

    /**
     * Returns a structural response description without exposing opaque bytes.
     *
     * @return response name and value-presence description
     */
    @Override
    public String toString() {
        return "IntermediateResponse[responseName=" + responseName + ", responseValuePresent="
                + !responseValue.isEmpty() + Symbol.BRACKET_RIGHT;
    }

}
