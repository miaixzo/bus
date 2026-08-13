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
package org.miaixz.bus.auth.protocol.jwt;

import java.security.SecureRandom;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.auth.Claims;
import org.miaixz.bus.auth.protocol.jwt.JWT.VerificationPolicy;
import org.miaixz.bus.auth.protocol.jwt.signature.JWTSigner;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.fabric.Clock;

/**
 * Creates bounded compact JWTs from root claims and explicit product-owned security dependencies.
 *
 * @author Kimi Liu
 */
public final class JWTCreator {

    /**
     * Prevents construction of the stateless creator.
     */
    private JWTCreator() {
        // No initialization required.
    }

    /**
     * Creates one signed compact JWT using a product-selected algorithm and signer.
     *
     * @param payload             immutable caller claims
     * @param policy              trusted verification policy mirrored by issuance
     * @param clock               Fabric security clock
     * @param random              product-owned secure random source
     * @param provider            product JSON provider
     * @param maximumHeaderBytes  maximum serialized header bytes
     * @param maximumPayloadBytes maximum serialized payload bytes
     * @param maximumTokenBytes   maximum compact token bytes
     * @param signer              signer selected independently of token input
     * @return signed compact JWT
     */
    public static String create(
            final Claims payload,
            final VerificationPolicy policy,
            final Clock clock,
            final SecureRandom random,
            final JsonProvider provider,
            final int maximumHeaderBytes,
            final int maximumPayloadBytes,
            final int maximumTokenBytes,
            final JWTSigner signer) {
        Claims claims = Assert.notNull(payload, "JWT claims must be not null!");
        final VerificationPolicy trusted = Assert.notNull(policy, "JWT policy must be not null!");
        final Clock time = Assert.notNull(clock, "Clock source must be not null!");
        final SecureRandom entropy = Assert.notNull(random, "Random source must be not null!");
        final JsonProvider json = Assert.notNull(provider, "JSON provider must be not null!");
        final JWTSigner selected = Assert.notNull(signer, "JWT signer must be not null!");
        if (maximumHeaderBytes <= 0 || maximumPayloadBytes <= 0 || maximumTokenBytes <= 0
                || trusted.algorithm() != selected.algorithm()) {
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
        final byte[] identifier = new byte[Normal._32];
        entropy.nextBytes(identifier);
        final List<String> audiences = new ArrayList<>(trusted.audiences());
        audiences.sort(String::compareTo);
        claims = claims.with(RegisteredClaims.ISSUER, trusted.issuer())
                .with(RegisteredClaims.AUDIENCE, List.copyOf(audiences))
                .with(RegisteredClaims.ISSUED_AT, issuedAt.getEpochSecond())
                .with(RegisteredClaims.EXPIRES_AT, expiresAt.getEpochSecond())
                .with(RegisteredClaims.JWT_ID, Base64.encodeUrlSafe(identifier));
        final Map<String, Object> headers = Map
                .of(JWTHeader.ALGORITHM, trusted.algorithm().identifier(), JWTHeader.TYPE, "JWT");
        final byte[] headerJson = json.write(headers);
        final byte[] payloadJson = json.write(claims.snapshot());
        if (headerJson == null || headerJson.length == 0 || headerJson.length > maximumHeaderBytes
                || payloadJson == null || payloadJson.length == 0 || payloadJson.length > maximumPayloadBytes) {
            reject();
        }
        final String headerSegment = Base64.encodeUrlSafe(headerJson);
        final String payloadSegment = Base64.encodeUrlSafe(payloadJson);
        final byte[] signingInput = (headerSegment + Symbol.DOT + payloadSegment)
                .getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        final byte[] signature = selected.sign(signingInput);
        if (signature == null || trusted.algorithm() != JWT.TrustedAlgorithm.NONE && signature.length == 0) {
            reject();
        }
        final String signatureSegment = Base64.encodeUrlSafe(signature);
        final String token = headerSegment + Symbol.DOT + payloadSegment + Symbol.DOT + signatureSegment;
        if (token.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > maximumTokenBytes) {
            throw new ProtocolException(ErrorCode._100530);
        }
        return token;
    }

    /**
     * Rejects invalid issuance inputs with the stable token error.
     */
    private static void reject() {
        throw new ProtocolException(ErrorCode._100533);
    }

}
