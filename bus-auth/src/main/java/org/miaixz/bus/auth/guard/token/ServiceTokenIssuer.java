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
package org.miaixz.bus.auth.guard.token;

import java.time.Duration;
import java.util.HashMap;

import org.miaixz.bus.auth.shared.jwt.JWT;
import org.miaixz.bus.auth.shared.jwt.JwtClaims;
import org.miaixz.bus.auth.shared.jwt.JwtService;

/**
 * Issues and caches one short-lived service identity token.
 * <p>
 * The token proves which service is calling. It never carries a user identity and never replaces the credential
 * verification performed by the target service.
 * </p>
 *
 * @author Kimi Liu
 */
public final class ServiceTokenIssuer {

    /**
     * Signed claim containing the service that authenticated to the forwarding hop.
     */
    public static final String ORIGIN = "origin";

    /**
     * Signed claim identifying the registered route selected by the forwarding hop.
     */
    public static final String ASSET = "asset";

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
     * Verified subject of the original caller when signing for a forwarded request.
     */
    private final String origin;

    /**
     * Selected route identifier when the forwarding hop binds its assertion to a registered asset.
     */
    private final String asset;

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
    public ServiceTokenIssuer(final String subject, final String audience, final byte[] secret,
            final Duration lifetime) {
        this(subject, audience, secret, lifetime, null);
    }

    /**
     * Creates an issuer for an optional verified caller at the previous hop.
     *
     * @param subject  exact issuer and subject
     * @param audience exact next-hop audience
     * @param secret   raw signing key
     * @param lifetime token lifetime
     * @param origin   verified caller code, or null for a direct request
     */
    public ServiceTokenIssuer(String subject, String audience, byte[] secret, Duration lifetime, String origin) {
        this(subject, audience, secret, lifetime, origin, null);
    }

    /**
     * Creates an issuer for an authenticated forwarding hop and its selected route.
     *
     * @param subject  exact issuer and subject
     * @param audience exact next-hop audience
     * @param secret   raw signing key
     * @param lifetime token lifetime
     * @param origin   verified caller code, or null for a direct request
     * @param asset    selected registered asset identifier, or null when omitted
     */
    public ServiceTokenIssuer(String subject, String audience, byte[] secret, Duration lifetime, String origin,
            String asset) {
        if (origin != null && origin.isBlank()) {
            throw new IllegalArgumentException("Service token origin must be a service code");
        }
        if (asset != null && (origin == null || asset.isBlank())) {
            throw new IllegalArgumentException(
                    "A service token asset requires a verified origin and a route identifier");
        }
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
        this.origin = origin;
        this.asset = asset;
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
            final JWT issued = issue();
            cached = issued;
            refreshAt = System.currentTimeMillis() + lifetime.toMillis() * 4 / 5;
            return issued.compact();
        }
    }

    /**
     * Signs a fresh token for callers that manage their own signing cache.
     *
     * @return compact signed service token
     */
    public String sign() {
        return issue().compact();
    }

    /**
     * Builds and signs the configured service claims.
     *
     * @return the newly signed JWT
     */
    private JWT issue() {
        var claims = new HashMap<String, String>();
        claims.put(JwtClaims.SUBJECT, subject);
        if (origin != null)
            claims.put(ORIGIN, origin);
        if (asset != null)
            claims.put(ASSET, asset);
        return service.issue(JwtClaims.of(claims), subject, audience, lifetime);
    }

}
