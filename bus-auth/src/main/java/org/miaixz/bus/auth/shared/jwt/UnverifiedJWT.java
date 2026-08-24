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

import java.security.Key;

import org.miaixz.bus.auth.shared.jose.JoseHeader;
import org.miaixz.bus.auth.shared.jose.JwaAlgorithm;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Carries a structurally parsed compact JWS whose signature has not been verified.
 * <p>
 * Header and claim values exposed by this type are attacker-controlled and must only be used for bounded key selection
 * or routing. Authorization decisions must use the immutable {@link JWT} returned by one of the {@code verify} methods.
 * </p>
 *
 * @author Kimi Liu
 */
public final class UnverifiedJWT {

    /**
     * Sensitive original compact representation used for later verification.
     */
    private final String compact;
    /**
     * Untrusted parsed protected JOSE Header.
     */
    private final JoseHeader header;
    /**
     * Untrusted parsed JWT Claims Set.
     */
    private final JwtClaims claims;

    /**
     * Creates one immutable unverified token from a parser-owned representation.
     *
     * @param compact original compact JWS
     * @param header  parsed untrusted protected Header
     * @param claims  parsed untrusted Claims Set
     */
    UnverifiedJWT(final String compact, final JoseHeader header, final JwtClaims claims) {
        this.compact = Assert.notBlank(compact, "Unverified JWT compact value must not be blank");
        this.header = Assert.notNull(header, "Unverified JWT JOSE Header must not be null");
        this.claims = Assert.notNull(claims, "Unverified JWT claims must not be null");
    }

    /**
     * Returns the untrusted protected JOSE Header.
     *
     * @return parsed Header
     */
    public JoseHeader header() {
        return header;
    }

    /**
     * Returns the untrusted Claims Set.
     *
     * @return parsed claims
     */
    public JwtClaims claims() {
        return claims;
    }

    /**
     * Looks up one untrusted exact claim without coercion.
     *
     * @param name exact case-sensitive claim name
     * @return claim when present
     */
    public Optional<JsonValue> claim(final String name) {
        return claims.claim(name);
    }

    /**
     * Converts one untrusted exact claim to a caller-selected Java type.
     *
     * @param name exact case-sensitive claim name
     * @param type requested result type
     * @param <T>  requested result type
     * @return converted claim when present
     */
    public <T> Optional<T> claim(final String name, final Class<T> type) {
        return claims.claim(name, type);
    }

    /**
     * Verifies the original compact token with the deterministic HS256 String-key profile.
     *
     * @param secret non-empty String key material
     * @return cryptographically verified JWT
     */
    public JWT verify(final String secret) {
        return JWT.verify(compact, secret);
    }

    /**
     * Verifies the original compact token with strict caller-owned HS256 key bytes.
     *
     * @param secret raw HMAC key containing at least 256 bits
     * @return cryptographically verified JWT
     */
    public JWT verify(final byte[] secret) {
        return JWT.verify(compact, secret);
    }

    /**
     * Verifies the original compact token with an explicit trusted algorithm and key.
     *
     * @param expectedAlgorithm exact trusted JWS algorithm
     * @param key               public or symmetric verification key
     * @return cryptographically verified JWT
     */
    public JWT verify(final JwaAlgorithm expectedAlgorithm, final Key key) {
        return JWT.verify(compact, expectedAlgorithm, key);
    }

    /**
     * Returns a redacted diagnostic representation that emphasizes the trust boundary.
     *
     * @return fixed non-sensitive description
     */
    @Override
    public String toString() {
        return "UnverifiedJWT[compact=[REDACTED]]";
    }

}
