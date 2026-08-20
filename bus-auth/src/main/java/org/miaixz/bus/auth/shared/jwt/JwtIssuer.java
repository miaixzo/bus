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
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.shared.jose.JoseHeader;
import org.miaixz.bus.auth.shared.jose.JwsService;
import org.miaixz.bus.core.data.id.UUID;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.Clock;

/**
 * Issues profile-bounded compact signed JWTs using the runtime JSON provider, Fabric clock, and shared JWS service.
 *
 * @author Kimi Liu
 */
public final class JwtIssuer {

    /**
     * Provider-neutral JSON codec used for exact payload serialization.
     */
    private final JsonProvider jsonProvider;
    /**
     * Profile-scoped JWS execution service.
     */
    private final JwsService jwsService;
    /**
     * Shared Fabric time source.
     */
    private final Clock clock;

    /**
     * Creates a signed JWT issuer with no system-clock or global-JSON fallback.
     *
     * @param jsonProvider runtime JSON provider
     * @param jwsService   profile-scoped JWS service
     * @param clock        shared Fabric clock
     */
    public JwtIssuer(final JsonProvider jsonProvider, final JwsService jwsService, final Clock clock) {
        this.jsonProvider = Assert.notNull(jsonProvider, "JWT issuer JSON provider must not be null");
        this.jwsService = Assert.notNull(jwsService, "JWT issuer JWS service must not be null");
        this.clock = Assert.notNull(clock, "JWT issuer clock must not be null");
    }

    /**
     * Generates registered claims and signs one compact JWT with the explicit key.
     *
     * @param request profile, protected JOSE Header, and caller-supplied claims
     * @param key     explicit private or symmetric signing key
     * @return immutable issued JWT
     */
    public Jwt issue(final Request request, final Key key) {
        Assert.notNull(request, "JWT issue request must not be null");
        Assert.notNull(key, "JWT signing key must not be null");
        final JwtClaims supplied = new JwtClaims(request.claims());
        if (supplied.issuer().isPresent() || !supplied.audiences().isEmpty() || supplied.issuedAt().isPresent()
                || supplied.expiration().isPresent() || supplied.jwtId().isPresent()) {
            throw new ValidateException("JWT issue request must not override issuer-generated claims");
        }
        if (request.profile().subjectRequired() && supplied.subject().isEmpty()) {
            throw new ValidateException("JWT issue profile requires a subject claim");
        }
        final Instant issuedAt = clock.now();
        final Instant expiresAt;
        try {
            expiresAt = issuedAt.plus(request.profile().lifetime());
        } catch (ArithmeticException cause) {
            throw new ValidateException("JWT lifetime exceeds the supported Instant range", cause);
        }
        final JwtClaims.Registered registered = new JwtClaims.Registered(Optional.of(request.profile().issuer()),
                supplied.subject(), request.profile().audiences(), request.profile().audiences().size() > 1,
                Optional.of(expiresAt), supplied.notBefore(), Optional.of(issuedAt),
                Optional.of(UUID.randomUUID().toString()));
        final JwtClaims claims = new JwtClaims(registered, supplied.extensions());
        final byte[] payload = jsonProvider.writeValue(claims.values());
        final JwsService.Signature signature = jwsService.sign(request.header(), payload, key);
        final String compact = jwsService.compact(new JwsService.Jws(payload, List.of(signature)));
        return new Jwt(compact, request.header(), claims);
    }

    /**
     * Defines generated registration claims for one JWT use profile.
     *
     * @param issuer          exact issuer StringOrURI
     * @param audiences       non-empty, duplicate-free audiences in output order
     * @param lifetime        positive validity duration
     * @param subjectRequired whether caller claims must contain {@code sub}
     * @author Kimi Liu
     */
    public record Profile(String issuer, List<String> audiences, Duration lifetime, boolean subjectRequired) {

        /**
         * Validates and freezes the issue profile.
         *
         * @throws IllegalArgumentException if a required component or audience is {@code null}
         * @throws ValidateException        if issuer/audience is blank, audiences repeat, or lifetime is not positive
         */
        public Profile {
            Assert.notBlank(issuer, "JWT issue profile issuer must not be blank");
            Assert.notNull(audiences, "JWT issue profile audiences must not be null");
            Assert.notNull(lifetime, "JWT issue profile lifetime must not be null");
            audiences = List.copyOf(audiences);
            if (audiences.isEmpty() || lifetime.isZero() || lifetime.isNegative()) {
                throw new ValidateException("JWT issue profile requires audiences and a positive lifetime");
            }
            final Set<String> unique = new LinkedHashSet<>(audiences.size());
            for (String audience : audiences) {
                Assert.notBlank(audience, "JWT issue profile audience must not be blank");
                if (!unique.add(audience)) {
                    throw new ValidateException("JWT issue profile audiences must not contain duplicates");
                }
            }
        }

    }

    /**
     * Carries one issue profile, JOSE Header, and caller-owned claims.
     *
     * @param profile immutable generated-claim profile
     * @param header  JOSE Header containing an allowed protected signing algorithm
     * @param claims  subject, optional not-before, and protocol/application extension claims
     * @author Kimi Liu
     */
    public record Request(Profile profile, JoseHeader header, JsonValue.ObjectValue claims) {

        /**
         * Validates and freezes caller-owned issue inputs.
         *
         * @throws IllegalArgumentException if a component is {@code null}
         */
        public Request {
            Assert.notNull(profile, "JWT issue profile must not be null");
            Assert.notNull(header, "JWT issue JOSE Header must not be null");
            Assert.notNull(claims, "JWT issue claims must not be null");
            claims = new JsonValue.ObjectValue(claims.values());
        }

    }

}
