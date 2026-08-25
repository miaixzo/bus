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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.crypto.spec.SecretKeySpec;

import org.miaixz.bus.auth.FabricX;
import org.miaixz.bus.auth.FabricX.Clock;
import org.miaixz.bus.auth.guard.AlgorithmGuard;
import org.miaixz.bus.auth.guard.AudienceValidator;
import org.miaixz.bus.auth.guard.IssuerValidator;
import org.miaixz.bus.auth.guard.TimeGuard;
import org.miaixz.bus.auth.shared.jose.JoseHeader;
import org.miaixz.bus.auth.shared.jose.JwaAlgorithm;
import org.miaixz.bus.auth.shared.jose.JwsService;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Coordinates the existing JWT issuer, verifier, validator, JOSE services, shared clock, and explicit key.
 * <p>
 * {@link JWT} is the simple application-facing static entry point. This service remains available to Bus protocol and
 * Vendor implementations that want to reuse one immutable algorithm, key, and clock binding across multiple operations
 * without exposing low-level service assembly to their callers.
 * </p>
 * <p>
 * One instance binds exactly one trusted JWS algorithm. It never derives an accepted algorithm from a received JOSE
 * Header, never enables unsecured JWTs, and does not create or retain a JWE service for signed-only operations.
 * </p>
 *
 * @author Kimi Liu
 */
public class JwtService {

    /**
     * Trusted JWS algorithm bound to this service instance.
     */
    private final JwaAlgorithm algorithm;
    /**
     * Explicit signing or verification key bound by the caller.
     */
    private final Key key;
    /**
     * Shared Fabric time source used for issuance and validation.
     */
    private final Clock clock;
    /**
     * Profile-scoped JWS execution service.
     */
    private final JwsService jwsService;
    /**
     * JWT issuer backed by the shared JWS service and clock.
     */
    private final JwtIssuer issuer;
    /**
     * Signed-JWT verifier without an unrelated JWE dependency.
     */
    private final JwtVerifier verifier;

    /**
     * Creates an immutable signed-JWT service with the shared Fabric clock.
     *
     * @param algorithm exact trusted JWS algorithm
     * @param key       explicit private, public, or symmetric operation key
     */
    public JwtService(final JwaAlgorithm algorithm, final Key key) {
        this(algorithm, key, FabricX.clock());
    }

    /**
     * Creates an immutable signed-JWT service with an explicit clock for deterministic protocol execution and testing.
     *
     * @param algorithm exact trusted JWS algorithm
     * @param key       explicit private, public, or symmetric operation key
     * @param clock     shared or deterministic Fabric clock
     */
    public JwtService(final JwaAlgorithm algorithm, final Key key, final Clock clock) {
        this(algorithm, key, clock, true);
    }

    /**
     * Creates an immutable signed-JWT service with an explicit minimum-key-strength policy.
     *
     * @param algorithm                  exact trusted JWS algorithm
     * @param key                        explicit private, public, or symmetric operation key
     * @param clock                      shared or deterministic Fabric clock
     * @param minimumKeyStrengthEnforced whether registered JWA minimum key sizes are enforced
     */
    private JwtService(
            final JwaAlgorithm algorithm,
            final Key key,
            final Clock clock,
            final boolean minimumKeyStrengthEnforced) {
        this.algorithm = Assert.notNull(algorithm, "JWT service algorithm must not be null");
        this.algorithm.require(JwaAlgorithm.Kind.SIGNATURE);
        this.key = Assert.notNull(key, "JWT service key must not be null");
        this.clock = Assert.notNull(clock, "JWT service clock must not be null");
        this.jwsService = new JwsService(
                new AlgorithmGuard(),
                Set.of(algorithm.name()),
                minimumKeyStrengthEnforced);
        this.issuer = new JwtIssuer(jwsService, clock);
        this.verifier = new JwtVerifier(jwsService);
    }

    /**
     * Creates an HS256 service from caller-owned secret bytes.
     * <p>
     * The input array is copied before the JCA key is created. The factory rejects secrets shorter than the RFC 7518
     * minimum for HS256 instead of deferring a deterministic configuration error to a later operation.
     * </p>
     *
     * @param secret HMAC secret containing at least 256 bits of key material
     * @return immutable HS256 JWT service
     */
    public static JwtService hs256(final byte[] secret) {
        Assert.notNull(secret, "JWT HMAC secret must not be null");
        final int minimumBits = JwaAlgorithm.HS256.require(JwaAlgorithm.Kind.SIGNATURE).minimumKeyBits();
        if ((long) secret.length * Byte.SIZE < minimumBits) {
            throw new ValidateException("JWT HS256 secret must contain at least 256 bits");
        }
        final byte[] copy = Arrays.copyOf(secret, secret.length);
        return new JwtService(JwaAlgorithm.HS256, new SecretKeySpec(copy, Algorithm.HMACSHA256.getValue()));
    }

    /**
     * Creates an HS256 service from arbitrary non-empty String key material.
     * <p>
     * Every String, regardless of length, is converted through the immutable {@link HkdfJwtKeyDeriver} profile. The
     * resulting 256-bit key is deterministic across JVMs and cluster nodes. This overload accepts short values for
     * application compatibility, but derivation cannot add entropy to a weak secret.
     * </p>
     *
     * @param secret non-empty String key material preserved exactly as supplied
     * @return immutable HS256 JWT service
     */
    public static JwtService hs256(final String secret) {
        return hs256(secret, JWT.Mode.HKDF_SHA256_V1);
    }

    /**
     * Creates an HS256 service from arbitrary non-empty String key material under an explicit conversion strategy.
     * <p>
     * {@link JWT.Mode#HKDF_SHA256_V1} applies the deterministic framework derivation profile. {@link JWT.Mode#RAW}
     * preserves the legacy cross-system contract by using the exact UTF-8 bytes of the String.
     * Neither strategy trims, normalizes, or otherwise rewrites caller material.
     * </p>
     *
     * @param secret   non-empty String key material preserved exactly as supplied
     * @param mode   explicit String-to-key mode
     * @return immutable HS256 JWT service
     */
    public static JwtService hs256(final String secret, final JWT.Mode mode) {
        final JWT.Mode selected = Assert.notNull(mode, "JWT String key mode must not be null");
        if (JWT.Mode.HKDF_SHA256_V1 == selected) {
            return new JwtService(
                    JwaAlgorithm.HS256,
                    HkdfJwtKeyDeriver.INSTANCE.derive(JwaAlgorithm.HS256, secret));
        }

        final byte[] material = Assert.notEmpty(secret, "JWT String secret must not be empty").getBytes(Charset.UTF_8);
        try {
            return new JwtService(
                    JwaAlgorithm.HS256,
                    new SecretKeySpec(material, Algorithm.HMACSHA256.getValue()),
                    FabricX.clock(),
                    false);
        } finally {
            Arrays.fill(material, (byte) 0);
        }
    }

    /**
     * Signs a caller-owned Claims Set without generating or replacing registered claims.
     *
     * @param claims validated implementation-neutral Claims Set
     * @return immutable locally signed JWT
     */
    public JWT sign(final JwtClaims claims) {
        Assert.notNull(claims, "JWT signing claims must not be null");
        final JoseHeader header = header();
        final byte[] payload = JsonKit.writeValue(claims.values());
        final JwsService.Signature signature = jwsService.sign(header, payload, key);
        final String compact = jwsService.compact(new JwsService.Jws(payload, List.of(signature)));
        return new JWT(compact, header, claims);
    }

    /**
     * Issues a JWT after generating issuer, audience, issued-at, expiration, and JWT ID claims.
     *
     * @param claims   subject and application claims that do not replace issuer-generated claims
     * @param issuer   exact issuer StringOrURI
     * @param audience exact audience StringOrURI
     * @param lifetime positive token lifetime
     * @return immutable locally issued JWT
     */
    public JWT issue(final JwtClaims claims, final String issuer, final String audience, final Duration lifetime) {
        Assert.notNull(claims, "JWT issue claims must not be null");
        final JwtIssuer.Profile profile = new JwtIssuer.Profile(issuer, List.of(audience), lifetime, false);
        final JwtIssuer.Request request = new JwtIssuer.Request(profile, header(), claims.values());
        return this.issuer.issue(request, key);
    }

    /**
     * Cryptographically verifies one signed compact JWT against the trusted algorithm and key.
     *
     * @param compact signed compact JWT
     * @return immutable cryptographically verified JWT
     */
    public JWT verify(final String compact) {
        return verifier.verify(compact, new JwtVerifier.Signed(key, Set.of()));
    }

    /**
     * Verifies one signed compact JWT and validates every registered temporal claim that is present.
     *
     * @param compact signed compact JWT
     * @return immutable cryptographically and temporally validated JWT
     */
    public JWT validate(final String compact) {
        final JWT jwt = verify(compact);
        return validator(Duration.ZERO).validate(jwt);
    }

    /**
     * Verifies one signed compact JWT and applies explicit common-claim requirements.
     *
     * @param compact      signed compact JWT
     * @param requirements public issuer, audience, temporal, and subject requirements
     * @return immutable fully validated JWT
     */
    public JWT validate(final String compact, final JWT.Requirements requirements) {
        Assert.notNull(requirements, "JWT validation requirements must not be null");
        final JWT jwt = verify(compact);
        final JwtValidator.Requirements internal = new JwtValidator.Requirements(requirements.issuer(),
                requirements.audiences(), requirements.subjectRequired(), requirements.expirationRequired(),
                requirements.issuedAtRequired(), false, requirements.maximumAge(), Optional.empty(),
                new JsonValue.ObjectValue(Map.of()));
        return validator(requirements.clockSkew()).validate(jwt, internal);
    }

    /**
     * Tests whether cryptographic verification and registered temporal validation succeed.
     *
     * @param compact signed compact JWT
     * @return {@code true} when the token is valid
     */
    public boolean isValid(final String compact) {
        try {
            validate(compact);
            return true;
        } catch (IllegalArgumentException | ValidateException ignored) {
            return false;
        }
    }

    /**
     * Tests whether one signed compact JWT satisfies explicit common-claim requirements.
     *
     * @param compact      signed compact JWT
     * @param requirements public issuer, audience, temporal, and subject requirements
     * @return {@code true} when complete validation succeeds
     */
    public boolean isValid(final String compact, final JWT.Requirements requirements) {
        try {
            validate(compact, requirements);
            return true;
        } catch (IllegalArgumentException | ValidateException ignored) {
            return false;
        }
    }

    /**
     * Builds one protected-only JWS Header from the trusted service algorithm.
     *
     * @return immutable JOSE Header with exact {@code alg} and {@code typ=JWT}
     */
    private JoseHeader header() {
        return JoseHeader.jws(
                algorithm,
                Optional.empty(),
                Optional.empty(),
                Optional.of("JWT"),
                new JsonValue.ObjectValue(Map.of()));
    }

    /**
     * Creates a synchronous common-claim validator for one explicit clock-skew allowance.
     *
     * @param clockSkew non-negative accepted clock displacement
     * @return synchronous JWT validator without replay-cache dependencies
     */
    private JwtValidator validator(final Duration clockSkew) {
        return new JwtValidator(new IssuerValidator(), new AudienceValidator(), new TimeGuard(clock, clockSkew));
    }

}
