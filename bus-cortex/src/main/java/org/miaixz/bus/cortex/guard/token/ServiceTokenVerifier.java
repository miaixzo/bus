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
package org.miaixz.bus.cortex.guard.token;

import java.time.Duration;
import java.util.Set;

import org.miaixz.bus.auth.shared.jwt.JWT;
import org.miaixz.bus.auth.shared.jwt.JwtClaims;
import org.miaixz.bus.auth.shared.jwt.JwtService;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Verifies a service identity token against the configured shared secret.
 *
 * @author Kimi Liu
 */
public final class ServiceTokenVerifier {

    /**
     * Accepted signing key.
     */
    private final JwtService service;

    /**
     * Accepted subjects.
     */
    private final Set<String> subjects;

    /**
     * Required audience.
     */
    private final String audience;

    /**
     * Accepted clock displacement.
     */
    private final Duration skew;

    /**
     * Creates a verifier.
     *
     * @param subjects accepted issuers and subjects
     * @param audience required audience
     * @param skew     accepted clock displacement
     * @param secret   raw secret, at least 32 bytes
     */
    public ServiceTokenVerifier(final Set<String> subjects, final String audience, final Duration skew,
            final byte[] secret) {
        if (secret == null || secret.length < 32) {
            throw new IllegalArgumentException("Service token verifier requires a secret of at least 32 bytes");
        }
        if (subjects == null || subjects.isEmpty()) {
            throw new IllegalArgumentException("Service token verifier requires at least one subject");
        }
        this.subjects = Set.copyOf(subjects);
        this.audience = audience;
        this.skew = skew;
        this.service = JwtService.hs256(secret);
    }

    /**
     * Reads one optional, nonblank signed forwarding claim.
     *
     * @param claims registered and extension claims
     * @param name   forwarding claim name
     * @return claim value or null when omitted
     * @throws IllegalArgumentException if a supplied claim has an invalid shape
     */
    public static String forwardingClaim(JwtClaims claims, String name) {
        JsonValue claim = claims.claim(name).orElse(null);
        if (claim == null)
            return null;
        if (claim instanceof JsonValue.StringValue value && !value.value().isBlank())
            return value.value();
        throw new IllegalArgumentException("Invalid service token forwarding claim");
    }

    /**
     * Authenticates a compact service token against one trusted HS256 key. The caller applies its claim policy.
     *
     * @param compact compact service token
     * @param secret  trusted HS256 verification key
     * @return token with an authenticated signature, or null if the compact token is invalid
     */
    public static JWT verifySignature(String compact, byte[] secret) {
        var verifier = JwtService.hs256(secret);
        try {
            return verifier.verify(compact);
        } catch (IllegalArgumentException | ValidateException rejected) {
            return null;
        }
    }

    /**
     * Verifies signature, issuer, audience, temporal claims and subject.
     *
     * @param compact compact token
     * @return whether the token is valid for the configured subject
     */
    public boolean accepts(final String compact) {
        return subject(compact) != null;
    }

    /**
     * Returns the validated subject after verifying the signature, issuer, audience, and lifetime.
     *
     * @param compact compact service token
     * @return verified caller code or null if verification fails
     */
    public String subject(final String compact) {
        JWT verified = verify(compact);
        return verified == null ? null : verified.claims().subject().orElseThrow();
    }

    /**
     * Returns the authenticated token so the caller can use its signed service claims.
     *
     * @param compact service token presented by the caller
     * @return validated token, or null if verification fails
     */
    public JWT verify(final String compact) {
        if (compact == null || compact.isEmpty()) {
            return null;
        }
        for (String subject : subjects) {
            try {
                final JWT jwt = service.validate(
                        compact,
                        new JWT.Requirements(subject, Set.of(audience), skew, true, true, true, Optional.empty()));
                if (subject.equals(jwt.claims().subject().orElse(null))) {
                    return jwt;
                }
            } catch (RuntimeException rejected) {
                // Try the next accepted subject. The rejection reason never reaches the caller.
            }
        }
        return null;
    }

}
