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

import org.miaixz.bus.auth.shared.jwt.JWT;
import org.miaixz.bus.auth.shared.jwt.JwtService;

import java.time.Duration;
import java.util.Set;

import org.miaixz.bus.core.lang.Optional;

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
     * Verifies signature, issuer, audience, temporal claims and subject.
     *
     * @param compact compact token
     * @return whether the token is valid for the configured subject
     */
    public boolean accepts(final String compact) {
        if (compact == null || compact.isEmpty()) {
            return false;
        }
        for (String subject : subjects) {
            try {
                final JWT jwt = service.validate(compact, new JWT.Requirements(subject, Set.of(audience), skew, true,
                        true, true, Optional.empty()));
                if (subject.equals(jwt.claims().subject().orElse(null))) {
                    return true;
                }
            } catch (RuntimeException rejected) {
                // Try the next accepted subject. The rejection reason never reaches the caller.
            }
        }
        return false;
    }

}
