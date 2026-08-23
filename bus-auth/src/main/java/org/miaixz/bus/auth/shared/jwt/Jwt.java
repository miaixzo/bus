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
package org.miaixz.bus.auth.shared.jwt;

import org.miaixz.bus.auth.shared.jose.JoseHeader;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Carries one verified or locally issued compact JWT with its parsed JOSE Header and claims.
 * <p>
 * The class deliberately overrides diagnostic rendering so a compact bearer value cannot be exposed by an implicit
 * record or object string representation. Construction identifies serialization shape only; JwtIssuer and JwtVerifier
 * remain responsible for cryptographic and claim validation.
 * </p>
 *
 * @author Kimi Liu
 */
public class Jwt {

    /**
     * Sensitive compact JWS or JWE representation.
     */
    private final String compact;
    /**
     * Parsed outer JOSE Header.
     */
    private final JoseHeader header;
    /**
     * Parsed implementation-neutral JWT Claims Set.
     */
    private final JwtClaims claims;
    /**
     * Serialization kind derived from exact compact segment count.
     */
    private final Kind kind;

    /**
     * Creates an immutable JWT value after validating compact serialization shape.
     *
     * @param compact sensitive compact JWS or JWE value
     * @param header  parsed outer JOSE Header
     * @param claims  parsed JWT Claims Set
     * @throws IllegalArgumentException if a required component is {@code null} or compact is blank
     * @throws ValidateException        if compact contains neither three nor five segments
     */
    public Jwt(final String compact, final JoseHeader header, final JwtClaims claims) {
        this.compact = Assert.notBlank(compact, "JWT compact value must not be blank");
        this.header = Assert.notNull(header, "JWT JOSE Header must not be null");
        this.claims = Assert.notNull(claims, "JWT claims must not be null");
        final int segments = compact.split("\\.", -1).length;
        this.kind = switch (segments) {
            case 3 -> Kind.SIGNED;
            case 5 -> Kind.ENCRYPTED;
            default -> throw new ValidateException("JWT compact value must contain three or five segments");
        };
    }

    /**
     * Returns the sensitive compact value for an immediate protocol operation.
     *
     * @return compact JWS or JWE representation
     */
    public String compact() {
        return compact;
    }

    /**
     * Returns the parsed outer JOSE Header.
     *
     * @return immutable JOSE Header
     */
    public JoseHeader header() {
        return header;
    }

    /**
     * Returns the parsed JWT Claims Set.
     *
     * @return immutable claims
     */
    public JwtClaims claims() {
        return claims;
    }

    /**
     * Returns whether the compact value is a JWS or JWE representation.
     *
     * @return serialization kind
     */
    public Kind kind() {
        return kind;
    }

    /**
     * Returns a fixed non-sensitive diagnostic representation.
     *
     * @return redacted JWT label and serialization kind
     */
    @Override
    public String toString() {
        return "Jwt[kind=" + kind + ", compact=[REDACTED]]";
    }

    /**
     * Identifies the outer JOSE compact serialization used by a JWT.
     *
     * @author Kimi Liu
     */
    public enum Kind {
        /**
         * JWT represented as a three-segment compact JWS.
         */
        SIGNED,
        /**
         * JWT represented as a five-segment compact JWE.
         */
        ENCRYPTED

    }

}
