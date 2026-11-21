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
import org.miaixz.bus.auth.shared.jwt.JwtClaims;
import org.miaixz.bus.auth.shared.jwt.JwtService;

import java.time.Duration;
import java.util.Map;

/**
 * Issues and caches one short-lived service identity token.
 * <p>
 * The token proves which service is calling. It never carries a user identity and never replaces the credential
 * verification performed by the target service.
 * </p>
 *
 * @author Kimi Liu
 */
public final class ServiceToken {

    /**
     * Signing service bound to the deployment secret.
     */
    private final JwtService service;

    /**
     * Issued and required subject.
     */
    private final String subject;

    /**
     * Required audience.
     */
    private final String audience;

    /**
     * Token lifetime.
     */
    private final Duration lifetime;

    /**
     * Current token.
     */
    private volatile JWT cached;

    /**
     * Epoch milliseconds at which the cached token must be replaced.
     */
    private volatile long refreshAt;

    /**
     * Creates an issuer.
     *
     * @param subject  exact issuer and subject
     * @param audience exact audience
     * @param secret   raw secret bytes, at least 32
     * @param lifetime positive lifetime, at most 15 minutes
     */
    public ServiceToken(final String subject, final String audience, final byte[] secret, final Duration lifetime) {
        if (subject == null || subject.isBlank() || audience == null || audience.isBlank()) {
            throw new IllegalArgumentException("Service token subject and audience are required");
        }
        if (secret == null || secret.length < 32) {
            throw new IllegalArgumentException("Service token secret must contain at least 32 bytes");
        }
        if (lifetime == null || lifetime.isZero() || lifetime.isNegative() || lifetime.toSeconds() > 900) {
            throw new IllegalArgumentException("Service token lifetime must be between 1 and 900 seconds");
        }
        this.service = JwtService.hs256(secret);
        this.subject = subject;
        this.audience = audience;
        this.lifetime = lifetime;
    }

    /**
     * Returns a token that remains valid for at least one fifth of its lifetime.
     *
     * @return compact service token
     */
    public String value() {
        final JWT current = cached;
        if (current != null && System.currentTimeMillis() < refreshAt) {
            return current.compact();
        }
        synchronized (this) {
            if (cached != null && System.currentTimeMillis() < refreshAt) {
                return cached.compact();
            }
            final JWT issued = service.issue(JwtClaims.of(Map.of(JwtClaims.SUBJECT, subject)), subject, audience, lifetime);
            cached = issued;
            refreshAt = System.currentTimeMillis() + lifetime.toMillis() * 4 / 5;
            return issued.compact();
        }
    }

}
