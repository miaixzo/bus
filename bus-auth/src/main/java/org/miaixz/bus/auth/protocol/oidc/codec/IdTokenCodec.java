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
package org.miaixz.bus.auth.protocol.oidc.codec;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.auth.protocol.oidc.IdToken;
import org.miaixz.bus.auth.protocol.oidc.IdTokenClaims;
import org.miaixz.bus.auth.protocol.oidc.OpenIdConnect;
import org.miaixz.bus.auth.shared.jwt.Jwt;
import org.miaixz.bus.auth.shared.jwt.JwtClaims;
import org.miaixz.bus.auth.shared.jwt.JwtVerifier;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Converts between verified shared JWT values and the typed OpenID Connect ID Token boundary.
 * <p>
 * Decoding always delegates cryptographic verification to {@link JwtVerifier} before extracting claims with exact JSON
 * types. This codec does not resolve keys, choose algorithms, validate issuer/audience/time/nonce/hash semantics, or
 * implement encrypted ID Token issuance.
 * </p>
 *
 * @author Kimi Liu
 */
public final class IdTokenCodec {

    /**
     * Shared cryptographic JWT verifier.
     */
    private final JwtVerifier verifier;

    /**
     * Creates an ID Token codec over the shared JWT verifier.
     *
     * @param verifier shared explicit-key JWT verifier
     * @throws IllegalArgumentException if {@code verifier} is {@code null}
     */
    public IdTokenCodec(final JwtVerifier verifier) {
        this.verifier = Assert.notNull(verifier, "OpenID Connect ID Token JWT verifier must not be null");
    }

    /**
     * Extracts an optional registered string claim without coercion.
     *
     * @param claims verified JWT Claims Set
     * @param name   exact claim name
     * @return optional exact string value
     * @throws ValidateException if a present value is not a non-blank JSON string
     */
    private static Optional<String> optionalString(final JwtClaims claims, final String name) {
        final JsonValue value = claims.values().values().get(name);
        if (value == null) {
            return Optional.empty();
        }
        if (!(value instanceof JsonValue.StringValue string) || string.value().isBlank()) {
            throw new ValidateException("OpenID Connect ID Token string claim has an invalid JSON type: " + name);
        }
        return Optional.of(string.value());
    }

    /**
     * Extracts an optional NumericDate by reusing the shared JWT NumericDate conversion.
     *
     * @param claims verified JWT Claims Set
     * @param name   exact OIDC NumericDate claim name
     * @return optional converted instant
     * @throws ValidateException if a present value is not a valid JWT NumericDate
     */
    private static Optional<Instant> optionalNumericDate(final JwtClaims claims, final String name) {
        final JsonValue value = claims.values().values().get(name);
        if (value == null) {
            return Optional.empty();
        }
        final JwtClaims conversion = new JwtClaims(new JsonValue.ObjectValue(Map.of(JwtClaims.ISSUED_AT, value)));
        return Optional.of(
                conversion.issuedAt().orElseThrow(
                        () -> new ValidateException("OpenID Connect NumericDate conversion produced no value")));
    }

    /**
     * Extracts an optional array of non-blank strings without scalar coercion.
     *
     * @param claims verified JWT Claims Set
     * @param name   exact claim name
     * @return immutable string list, empty when absent
     * @throws ValidateException if a present value is not an array of non-blank strings
     */
    private static List<String> stringArray(final JwtClaims claims, final String name) {
        final JsonValue value = claims.values().values().get(name);
        if (value == null) {
            return List.of();
        }
        if (!(value instanceof JsonValue.ArrayValue array)) {
            throw new ValidateException("OpenID Connect ID Token array claim has an invalid JSON type: " + name);
        }
        final List<String> result = new ArrayList<>(array.values().size());
        for (JsonValue element : array.values()) {
            if (!(element instanceof JsonValue.StringValue string) || string.value().isBlank()) {
                throw new ValidateException(
                        "OpenID Connect ID Token array claim must contain non-blank strings: " + name);
            }
            result.add(string.value());
        }
        return List.copyOf(result);
    }

    /**
     * Identifies claims represented by typed ID Token components.
     *
     * @param name exact claim name
     * @return {@code true} for a registered JWT or OpenID Connect ID Token claim
     */
    private static boolean typed(final String name) {
        return switch (name) {
            case JwtClaims.ISSUER, JwtClaims.SUBJECT, JwtClaims.AUDIENCE, JwtClaims.EXPIRATION, JwtClaims.ISSUED_AT, OpenIdConnect.Claims.AUTH_TIME, OpenIdConnect.Claims.NONCE, OpenIdConnect.Claims.ACR, OpenIdConnect.Claims.AMR, OpenIdConnect.Claims.AUTHORIZED_PARTY, OpenIdConnect.Claims.ACCESS_TOKEN_HASH, OpenIdConnect.Claims.CODE_HASH, OpenIdConnect.Claims.STATE_HASH, OpenIdConnect.Claims.SESSION_ID -> true;
            default -> false;
        };
    }

    /**
     * Encodes the caller-owned portion of a typed ID Token Claims Set for the shared JWT issuer.
     * <p>
     * The shared issuer owns {@code iss}, {@code aud}, {@code exp}, and {@code iat}; this boundary therefore emits the
     * subject, OpenID Connect claims, and extension claims only. Keeping the wire mapping in the codec prevents token
     * issuers from rebuilding registered claim names with ad hoc maps or string literals.
     * </p>
     *
     * @param claims validated typed ID Token Claims Set
     * @return immutable caller-owned JWT claim object
     * @throws IllegalArgumentException if {@code claims} is {@code null}
     */
    public JsonValue.ObjectValue encodeClaims(final IdTokenClaims claims) {
        Assert.notNull(claims, "OpenID Connect ID Token claims must not be null");
        final Map<String, JsonValue> values = new LinkedHashMap<>(claims.extensions().values());
        values.put(JwtClaims.SUBJECT, new JsonValue.StringValue(claims.subject()));
        claims.authenticatedAt().ifPresent(
                value -> values.put(
                        OpenIdConnect.Claims.AUTH_TIME,
                        new JsonValue.NumberValue(BigDecimal.valueOf(value.getEpochSecond()))));
        claims.nonce().ifPresent(value -> values.put(OpenIdConnect.Claims.NONCE, new JsonValue.StringValue(value)));
        claims.authenticationContextClass()
                .ifPresent(value -> values.put(OpenIdConnect.Claims.ACR, new JsonValue.StringValue(value)));
        if (!claims.authenticationMethods().isEmpty()) {
            final List<JsonValue> methods = claims.authenticationMethods().stream().map(JsonValue.StringValue::new)
                    .map(JsonValue.class::cast).toList();
            values.put(OpenIdConnect.Claims.AMR, new JsonValue.ArrayValue(methods));
        }
        claims.authorizedParty().ifPresent(
                value -> values.put(OpenIdConnect.Claims.AUTHORIZED_PARTY, new JsonValue.StringValue(value)));
        claims.accessTokenHash().ifPresent(
                value -> values.put(OpenIdConnect.Claims.ACCESS_TOKEN_HASH, new JsonValue.StringValue(value)));
        claims.codeHash()
                .ifPresent(value -> values.put(OpenIdConnect.Claims.CODE_HASH, new JsonValue.StringValue(value)));
        claims.stateHash()
                .ifPresent(value -> values.put(OpenIdConnect.Claims.STATE_HASH, new JsonValue.StringValue(value)));
        claims.sessionId()
                .ifPresent(value -> values.put(OpenIdConnect.Claims.SESSION_ID, new JsonValue.StringValue(value)));
        return new JsonValue.ObjectValue(values);
    }

    /**
     * Verifies and decodes a compact ID Token into exact typed claims.
     *
     * @param token        sensitive compact ID Token
     * @param verification explicit JOSE verification inputs
     * @return verified shared JWT and typed OIDC claims
     * @throws IllegalArgumentException if an argument is {@code null}
     * @throws ValidateException        if protection, required claims, or registered JSON types are invalid
     */
    public Decoded decode(final IdToken token, final JwtVerifier.Verification verification) {
        Assert.notNull(token, "OpenID Connect ID Token must not be null");
        Assert.notNull(verification, "OpenID Connect ID Token verification input must not be null");
        final Jwt jwt = verifier.verify(token.compact(), verification);
        final JwtClaims claims = jwt.claims();
        final String issuer = claims.issuer()
                .orElseThrow(() -> new ValidateException("OpenID Connect ID Token requires iss"));
        final String subject = claims.subject()
                .orElseThrow(() -> new ValidateException("OpenID Connect ID Token requires sub"));
        final List<String> audience = claims.audiences();
        if (audience.isEmpty()) {
            throw new ValidateException("OpenID Connect ID Token requires aud");
        }
        final Instant expiration = claims.expiration()
                .orElseThrow(() -> new ValidateException("OpenID Connect ID Token requires exp"));
        final Instant issuedAt = claims.issuedAt()
                .orElseThrow(() -> new ValidateException("OpenID Connect ID Token requires iat"));
        final Map<String, JsonValue> extensions = new LinkedHashMap<>();
        claims.values().values().forEach((name, value) -> {
            if (!typed(name)) {
                extensions.put(name, value);
            }
        });
        final IdTokenClaims typed = new IdTokenClaims(issuer, subject, audience, expiration, issuedAt,
                optionalNumericDate(claims, OpenIdConnect.Claims.AUTH_TIME),
                optionalString(claims, OpenIdConnect.Claims.NONCE), optionalString(claims, OpenIdConnect.Claims.ACR),
                stringArray(claims, OpenIdConnect.Claims.AMR),
                optionalString(claims, OpenIdConnect.Claims.AUTHORIZED_PARTY),
                optionalString(claims, OpenIdConnect.Claims.ACCESS_TOKEN_HASH),
                optionalString(claims, OpenIdConnect.Claims.CODE_HASH),
                optionalString(claims, OpenIdConnect.Claims.STATE_HASH),
                optionalString(claims, OpenIdConnect.Claims.SESSION_ID), new JsonValue.ObjectValue(extensions));
        return new Decoded(jwt, typed);
    }

    /**
     * Wraps one locally issued signed JWT as an ID Token wire value.
     *
     * @param jwt locally issued signed JWT
     * @return compact ID Token
     * @throws IllegalArgumentException if {@code jwt} is {@code null}
     * @throws ValidateException        if the JWT is not a three-segment signed compact JWT
     */
    public IdToken encode(final Jwt jwt) {
        Assert.notNull(jwt, "OpenID Connect issued JWT must not be null");
        if (jwt.kind() != Jwt.Kind.SIGNED) {
            throw new ValidateException(
                    "OpenID Connect ID Token codec supports signed tokens but not encrypted tokens");
        }
        return new IdToken(jwt.compact());
    }

    /**
     * Carries the cryptographically verified JWT and its exact typed OIDC claims.
     *
     * @param jwt    verified shared JWT
     * @param claims typed OpenID Connect ID Token claims
     * @author Kimi Liu
     */
    public record Decoded(Jwt jwt, IdTokenClaims claims) {

        /**
         * Creates an immutable decoded ID Token result.
         *
         * @throws IllegalArgumentException if a component is {@code null}
         */
        public Decoded {
            Assert.notNull(jwt, "Decoded OpenID Connect JWT must not be null");
            Assert.notNull(claims, "Decoded OpenID Connect ID Token claims must not be null");
        }

        /**
         * Returns a fixed diagnostic label without compact token or claim values.
         *
         * @return redacted decoded ID Token label
         */
        @Override
        public String toString() {
            return "Decoded[jwt=[REDACTED], claims=[REDACTED]]";
        }

    }

}
