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
package org.miaixz.bus.auth.metric.jwt;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.auth.metric.AuthMetric.ClockSource;
import org.miaixz.bus.auth.metric.AuthMetric.Limits;
import org.miaixz.bus.auth.metric.AuthMetric.RandomSource;
import org.miaixz.bus.auth.metric.AuthMetric.Runtime;
import org.miaixz.bus.auth.metric.JWT;
import org.miaixz.bus.auth.metric.JWT.VerificationPolicy;
import org.miaixz.bus.auth.metric.jwt.signature.JWTSigner;
import org.miaixz.bus.auth.metric.jwt.signature.JWTSignerBuilder;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.extra.json.JsonProvider;

/**
 * Creates compatibility JWTs and hardened compact JWTs from product-selected policy and injected runtime sources.
 * <p>
 * The hardened path derives time exclusively from {@link ClockSource}, identifiers exclusively from
 * {@link RandomSource}, JSON exclusively from the product {@link JsonProvider}, and the protected algorithm exclusively
 * from trusted configuration.
 * </p>
 *
 * @author Kimi Liu
 */
public class JWTCreator {

    /**
     * Constructs a new JWTCreator instance.
     */
    public JWTCreator() {
        // No initialization required.
    }

    /**
     * Creates an HS256 (HmacSHA256) JWT Token.
     *
     * @param payload the payload claims of the JWT
     * @param key     the HS256 (HmacSHA256) secret key
     * @return the generated JWT Token string
     */
    public static String create(final Map<String, ?> payload, final byte[] key) {
        return create(MapKit.of(JWTHeader.TYPE, "JWT"), payload, key);
    }

    /**
     * Creates an HS256 (HmacSHA256) JWT Token with custom headers.
     *
     * @param headers the header claims of the JWT
     * @param payload the payload claims of the JWT
     * @param key     the HS256 (HmacSHA256) secret key
     * @return the generated JWT Token string
     */
    public static String create(final Map<String, ?> headers, final Map<String, ?> payload, final byte[] key) {
        return JWT.of().addHeaders(headers).addPayloads(payload).setKey(key).sign();
    }

    /**
     * Creates a JWT Token using a custom signer.
     *
     * @param payload the payload claims of the JWT
     * @param signer  the {@link JWTSigner} to use for signing
     * @return the generated JWT Token string
     */
    public static String create(final Map<String, Object> payload, final JWTSigner signer) {
        return create(null, payload, signer);
    }

    /**
     * Creates a JWT Token with custom headers and a custom signer.
     *
     * @param headers the header claims of the JWT
     * @param payload the payload claims of the JWT
     * @param signer  the {@link JWTSigner} to use for signing
     * @return the generated JWT Token string
     */
    public static String create(
            final Map<String, Object> headers,
            final Map<String, Object> payload,
            final JWTSigner signer) {
        return JWT.of().addHeaders(headers).addPayloads(payload).setSigner(signer).sign();
    }

    /**
     * Creates a hardened compact JWT using ports from one validated authentication runtime.
     *
     * @param payload caller-supplied private and public claims
     * @param policy  trusted product-selected JWT policy
     * @param runtime validated authentication runtime
     * @param signer  signer selected independently of token input
     * @return signed compact JWT
     */
    public static String create(
            final Map<String, ?> payload,
            final VerificationPolicy policy,
            final Runtime runtime,
            final JWTSigner signer) {
        final Runtime current = Assert.notNull(runtime, "Authentication runtime must be not null!");
        return create(payload, policy, current.clock(), current.random(), current.json(), current.limits(), signer);
    }

    /**
     * Creates a hardened compact JWT from trusted policy and explicitly injected security sources.
     *
     * @param payload  caller-supplied private and public claims
     * @param policy   trusted product-selected JWT policy
     * @param clock    security clock
     * @param random   cryptographically secure random source
     * @param provider product JSON provider
     * @param limits   authentication input limits
     * @param signer   signer selected independently of token input
     * @return signed compact JWT
     */
    public static String create(
            final Map<String, ?> payload,
            final VerificationPolicy policy,
            final ClockSource clock,
            final RandomSource random,
            final JsonProvider provider,
            final Limits limits,
            final JWTSigner signer) {
        final VerificationPolicy trusted = Assert.notNull(policy, "JWT policy must be not null!");
        final ClockSource time = Assert.notNull(clock, "Clock source must be not null!");
        final RandomSource entropy = Assert.notNull(random, "Random source must be not null!");
        final JsonProvider json = Assert.notNull(provider, "JSON provider must be not null!");
        final Limits bounds = Assert.notNull(limits, "Limits must be not null!");
        final JWTSigner selected = Assert.notNull(signer, "JWT signer must be not null!");
        if (!trusted.algorithm().identifier().equals(JWTSignerBuilder.getId(selected.getAlgorithm()))) {
            reject();
        }
        final Instant issuedAt = Assert.notNull(time.now(), "Clock value must be not null!")
                .truncatedTo(ChronoUnit.SECONDS);
        final Instant expiresAt;
        try {
            expiresAt = issuedAt.plus(trusted.maximumLifetime());
        } catch (final DateTimeException | ArithmeticException failure) {
            throw new ProtocolException(ErrorCode._100301.getKey(), ErrorCode._100301.getValue(), failure);
        }
        final byte[] identifier = entropy.nextBytes(Normal._32);
        if (identifier == null || identifier.length != Normal._32) {
            reject();
        }
        final List<String> audiences = new ArrayList<>(trusted.audiences());
        audiences.sort(String::compareTo);
        final LinkedHashMap<String, Object> claims = new LinkedHashMap<>();
        if (payload != null) {
            claims.putAll(payload);
        }
        claims.put(JWTRegister.ISSUER, trusted.issuer());
        claims.put(JWTRegister.AUDIENCE, List.copyOf(audiences));
        claims.put(JWTRegister.ISSUED_AT, issuedAt.getEpochSecond());
        claims.put(JWTRegister.EXPIRES_AT, expiresAt.getEpochSecond());
        claims.put(JWTRegister.JWT_ID, Base64.encodeUrlSafe(identifier));
        final Map<String, Object> headers = Map
                .of(JWTHeader.ALGORITHM, trusted.algorithm().identifier(), JWTHeader.TYPE, "JWT");
        final byte[] headerJson = json.write(headers);
        final byte[] payloadJson = json.write(JWTPayload.immutable(claims).claims());
        if (headerJson == null || headerJson.length == 0 || headerJson.length > bounds.maxHeaderBytes()
                || payloadJson == null || payloadJson.length == 0 || payloadJson.length > bounds.maxJsonBytes()) {
            reject();
        }
        final String headerSegment = Base64.encodeUrlSafe(headerJson);
        final String payloadSegment = Base64.encodeUrlSafe(payloadJson);
        final String signatureSegment = selected.sign(headerSegment, payloadSegment);
        if (signatureSegment == null || signatureSegment.isEmpty()) {
            reject();
        }
        final String token = headerSegment + Symbol.DOT + payloadSegment + Symbol.DOT + signatureSegment;
        if (token.length() > bounds.maxJwtBytes()) {
            throw new ProtocolException(ErrorCode._100530);
        }
        return token;
    }

    /**
     * Rejects invalid trusted creation inputs with the shared token-format error.
     */
    private static void reject() {
        throw new ProtocolException(ErrorCode._100533);
    }

}
