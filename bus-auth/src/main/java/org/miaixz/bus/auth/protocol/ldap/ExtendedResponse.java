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
 * Represents the RFC 4511 {@code ExtendedResponse} protocol operation with application tag 24.
 *
 * @param result        common LDAP result components
 * @param responseName  optional numeric object identifier identifying the response syntax
 * @param responseValue optional opaque operation-specific response value
 * @author Kimi Liu
 */
public record ExtendedResponse(LdapResult result, Optional<String> responseName, Optional<byte[]> responseValue)
        implements LdapMessage.ProtocolOp {

    /**
     * RFC 4511 Notice of Disconnection unsolicited-notification object identifier.
     */
    public static final String NOTICE_OF_DISCONNECTION_OID = Ldap.NOTICE_OF_DISCONNECTION_OID;

    /**
     * Creates an immutable extended response.
     *
     * @param result        common LDAP result
     * @param responseName  optional response OID
     * @param responseValue optional response value
     * @throws IllegalArgumentException if an option is null or a present OID is malformed
     */
    public ExtendedResponse {
        Assert.notNull(result, "LDAP extended result must not be null");
        Assert.notNull(responseName, "LDAP extended response name option must not be null");
        Assert.notNull(responseValue, "LDAP extended response value option must not be null");
        if (!responseName.isEmpty()) {
            Control.requireNumericOid(responseName.getOrThrow());
        }
        responseValue = copy(responseValue);
    }

    /**
     * Clones optional extended-response bytes.
     *
     * @param source source optional value
     * @return independent optional value
     */
    private static Optional<byte[]> copy(final Optional<byte[]> source) {
        return source.isEmpty() ? Optional.empty() : Optional.of(source.getOrThrow().clone());
    }

    /**
     * Returns a separately owned optional extended-response value.
     *
     * @return empty option or newly allocated response octets
     */
    @Override
    public Optional<byte[]> responseValue() {
        return copy(responseValue);
    }

    /**
     * Compares extended responses using response-value content.
     *
     * @param other candidate object
     * @return {@code true} when all standard response fields are equal
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ExtendedResponse that) || !result.equals(that.result)
                || !responseName.equals(that.responseName) || responseValue.isEmpty() != that.responseValue.isEmpty()) {
            return false;
        }
        return responseValue.isEmpty() || Arrays.equals(responseValue.getOrThrow(), that.responseValue.getOrThrow());
    }

    /**
     * Computes a content-based extended-response hash.
     *
     * @return hash of the result, response name, and response-value octets
     */
    @Override
    public int hashCode() {
        return 31 * Objects.hash(result, responseName)
                + (responseValue.isEmpty() ? 0 : Arrays.hashCode(responseValue.getOrThrow()));
    }

    /**
     * Returns a structural response description without exposing opaque value bytes.
     *
     * @return result, response name, and value-presence description
     */
    @Override
    public String toString() {
        return "ExtendedResponse[result=" + result + ", responseName=" + responseName + ", responseValuePresent="
                + !responseValue.isEmpty() + Symbol.BRACKET_RIGHT;
    }

}
