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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.*;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Represents an RFC 7519 JWT Claims Set through explicit registered claims and isolated extension values.
 * <p>
 * The model retains whether {@code aud} used its string or array wire form. JSON values remain available at the codec
 * boundary, while internal users consume typed registered components instead of a field-name schema.
 * </p>
 *
 * @author Kimi Liu
 */
public final class JwtClaims {

    /**
     * Registered issuer claim name.
     */
    public static final String ISSUER = "iss";
    /**
     * Registered subject claim name.
     */
    public static final String SUBJECT = "sub";
    /**
     * Registered audience claim name.
     */
    public static final String AUDIENCE = "aud";
    /**
     * Registered expiration time claim name.
     */
    public static final String EXPIRATION = "exp";
    /**
     * Registered not-before claim name.
     */
    public static final String NOT_BEFORE = "nbf";
    /**
     * Registered issued-at claim name.
     */
    public static final String ISSUED_AT = "iat";
    /**
     * Registered JWT identifier claim name.
     */
    public static final String JWT_ID = "jti";

    /**
     * Explicit RFC 7519 registered claim values.
     */
    private final Registered registered;
    /**
     * Private or public extension claims not registered by RFC 7519.
     */
    private final JsonValue.ObjectValue extensions;

    /**
     * Decodes a complete JSON Claims Set into registered components and isolated extensions.
     *
     * @param values complete provider-neutral JWT Claims Set
     * @throws IllegalArgumentException if {@code values} is {@code null}
     * @throws ValidateException        if a registered claim has an invalid JSON type or value
     */
    public JwtClaims(final JsonValue.ObjectValue values) {
        this(decode(values));
    }

    /**
     * Creates a Claims Set from typed registered claims and non-conflicting extension values.
     *
     * @param registered explicit RFC 7519 claims
     * @param extensions unregistered claims
     * @throws IllegalArgumentException if either argument is {@code null}
     * @throws ValidateException        if an extension attempts to replace a registered claim
     */
    public JwtClaims(final Registered registered, final JsonValue.ObjectValue extensions) {
        this.registered = Assert.notNull(registered, "JWT registered claims must not be null");
        Assert.notNull(extensions, "JWT extension claims must not be null");
        for (String name : extensions.values().keySet()) {
            Assert.notBlank(name, "JWT extension claim name must not be blank");
            if (registered(name)) {
                throw new ValidateException("JWT extensions must not replace a registered claim");
            }
        }
        this.extensions = new JsonValue.ObjectValue(extensions.values());
    }

    /**
     * Creates a Claims Set from a completed decode result.
     *
     * @param decoded typed registered and extension values
     */
    private JwtClaims(final Decoded decoded) {
        this(decoded.registered(), decoded.extensions());
    }

    /**
     * Decodes registered members without maintaining a parallel field-name collection.
     *
     * @param values complete source Claims Set
     * @return typed decode result
     */
    private static Decoded decode(final JsonValue.ObjectValue values) {
        Assert.notNull(values, "JWT Claims Set must not be null");
        final Audience audience = audience(values.values().get(AUDIENCE));
        final Map<String, JsonValue> extensions = new LinkedHashMap<>();
        for (Map.Entry<String, JsonValue> entry : values.values().entrySet()) {
            Assert.notBlank(entry.getKey(), "JWT claim name must not be blank");
            if (!registered(entry.getKey())) {
                extensions.put(entry.getKey(), entry.getValue());
            }
        }
        return new Decoded(
                new Registered(optionalString(values, ISSUER), optionalString(values, SUBJECT), audience.values(),
                        audience.array(), optionalInstant(values, EXPIRATION), optionalInstant(values, NOT_BEFORE),
                        optionalInstant(values, ISSUED_AT), optionalString(values, JWT_ID)),
                new JsonValue.ObjectValue(extensions));
    }

    /**
     * Identifies RFC 7519 registered claim names without defining a field-name Set.
     *
     * @param name exact case-sensitive claim name
     * @return {@code true} for an explicit registered component
     */
    private static boolean registered(final String name) {
        return switch (name) {
            case ISSUER, SUBJECT, AUDIENCE, EXPIRATION, NOT_BEFORE, ISSUED_AT, JWT_ID -> true;
            default -> false;
        };
    }

    /**
     * Decodes the optional string-or-array audience claim and preserves its wire shape.
     *
     * @param value audience JSON value, or {@code null} when absent
     * @return typed audience values and shape
     */
    private static Audience audience(final JsonValue value) {
        if (value == null) {
            return new Audience(List.of(), false);
        }
        if (value instanceof JsonValue.StringValue string) {
            return new Audience(List.of(string.value()), false);
        }
        if (!(value instanceof JsonValue.ArrayValue array)) {
            throw new ValidateException("JWT audience must be a string or non-empty string array");
        }
        final List<String> result = new ArrayList<>(array.values().size());
        for (JsonValue element : array.values()) {
            if (!(element instanceof JsonValue.StringValue string)) {
                throw new ValidateException("JWT audience array entries must be strings");
            }
            result.add(string.value());
        }
        return new Audience(result, true);
    }

    /**
     * Returns an optional non-blank registered string claim.
     *
     * @param values complete Claims Set
     * @param name   registered claim name
     * @return decoded string when present
     */
    private static Optional<String> optionalString(final JsonValue.ObjectValue values, final String name) {
        final JsonValue value = values.values().get(name);
        if (value == null) {
            return Optional.empty();
        }
        if (!(value instanceof JsonValue.StringValue string) || string.value().isBlank()) {
            throw new ValidateException("JWT registered string claim must be a non-blank string");
        }
        return Optional.of(string.value());
    }

    /**
     * Returns an optional registered NumericDate claim as an Instant.
     *
     * @param values complete Claims Set
     * @param name   registered NumericDate claim name
     * @return decoded instant when present
     */
    private static Optional<Instant> optionalInstant(final JsonValue.ObjectValue values, final String name) {
        final JsonValue value = values.values().get(name);
        if (value == null) {
            return Optional.empty();
        }
        if (!(value instanceof JsonValue.NumberValue number)) {
            throw new ValidateException("JWT NumericDate claim must be a JSON number");
        }
        return Optional.of(numericDate(number.value()));
    }

    /**
     * Converts decimal seconds since the Unix epoch without millisecond inference.
     *
     * @param value exact JSON number
     * @return corresponding Instant
     */
    private static Instant numericDate(final BigDecimal value) {
        final BigDecimal secondsFloor = value.setScale(0, RoundingMode.FLOOR);
        final BigDecimal fractional = value.subtract(secondsFloor);
        try {
            return Instant.ofEpochSecond(secondsFloor.longValueExact(), fractional.movePointRight(9).intValueExact());
        } catch (ArithmeticException cause) {
            throw new ValidateException("JWT NumericDate exceeds Instant range or nanosecond precision", cause);
        }
    }

    /**
     * Encodes an Instant as an RFC 7519 seconds-based NumericDate.
     *
     * @param value exact instant
     * @return JSON number retaining nanosecond precision when present
     */
    private static JsonValue.NumberValue numericDate(final Instant value) {
        final BigDecimal seconds = BigDecimal.valueOf(value.getEpochSecond())
                .add(BigDecimal.valueOf(value.getNano(), 9));
        return new JsonValue.NumberValue(seconds.stripTrailingZeros());
    }

    /**
     * Validates the additional URI requirement for a StringOrURI value containing a colon.
     *
     * @param value issuer or subject value
     */
    private static void validateStringOrUri(final String value) {
        if (value.indexOf(Symbol.C_COLON) < 0) {
            return;
        }
        try {
            new URI(value);
        } catch (URISyntaxException cause) {
            throw new ValidateException("JWT StringOrURI claim syntax is invalid", cause);
        }
    }

    /**
     * Encodes the typed Claims Set at the JWT JSON boundary.
     *
     * @return detached immutable complete Claims Set
     */
    public JsonValue.ObjectValue values() {
        final Map<String, JsonValue> values = new LinkedHashMap<>();
        registered.issuer().ifPresent(value -> values.put(ISSUER, new JsonValue.StringValue(value)));
        registered.subject().ifPresent(value -> values.put(SUBJECT, new JsonValue.StringValue(value)));
        if (!registered.audiences().isEmpty()) {
            if (!registered.audienceArray()) {
                values.put(AUDIENCE, new JsonValue.StringValue(registered.audiences().get(0)));
            } else {
                values.put(
                        AUDIENCE,
                        new JsonValue.ArrayValue(registered.audiences().stream().map(JsonValue.StringValue::new)
                                .map(JsonValue.class::cast).toList()));
            }
        }
        registered.expiration().ifPresent(value -> values.put(EXPIRATION, numericDate(value)));
        registered.notBefore().ifPresent(value -> values.put(NOT_BEFORE, numericDate(value)));
        registered.issuedAt().ifPresent(value -> values.put(ISSUED_AT, numericDate(value)));
        registered.jwtId().ifPresent(value -> values.put(JWT_ID, new JsonValue.StringValue(value)));
        values.putAll(extensions.values());
        return new JsonValue.ObjectValue(values);
    }

    /**
     * Looks up one exact claim without type coercion.
     *
     * @param name exact case-sensitive claim name
     * @return provider-neutral claim value when present
     */
    public Optional<JsonValue> claim(final String name) {
        Assert.notBlank(name, "JWT claim name must not be blank");
        return Optional.ofNullable(values().values().get(name));
    }

    /**
     * Returns the optional issuer StringOrURI value.
     *
     * @return issuer when present
     */
    public Optional<String> issuer() {
        return registered.issuer();
    }

    /**
     * Returns the optional subject StringOrURI value.
     *
     * @return subject when present
     */
    public Optional<String> subject() {
        return registered.subject();
    }

    /**
     * Returns audience values as a uniform immutable list.
     *
     * @return audiences in wire order
     */
    public List<String> audiences() {
        return registered.audiences();
    }

    /**
     * Reports whether the audience claim uses the JSON array form.
     *
     * @return {@code true} only when a present {@code aud} is encoded as an array
     */
    public boolean audienceArray() {
        return registered.audienceArray();
    }

    /**
     * Returns the optional expiration NumericDate as an Instant.
     *
     * @return expiration instant when present
     */
    public Optional<Instant> expiration() {
        return registered.expiration();
    }

    /**
     * Returns the optional not-before NumericDate as an Instant.
     *
     * @return not-before instant when present
     */
    public Optional<Instant> notBefore() {
        return registered.notBefore();
    }

    /**
     * Returns the optional issued-at NumericDate as an Instant.
     *
     * @return issued-at instant when present
     */
    public Optional<Instant> issuedAt() {
        return registered.issuedAt();
    }

    /**
     * Returns the optional JWT identifier.
     *
     * @return JWT identifier when present
     */
    public Optional<String> jwtId() {
        return registered.jwtId();
    }

    /**
     * Returns the unregistered claims for profile-specific processing or reissuance.
     *
     * @return detached immutable extension claim object
     */
    public JsonValue.ObjectValue extensions() {
        return new JsonValue.ObjectValue(extensions.values());
    }

    /**
     * Compares registered and extension claims as one immutable value.
     *
     * @param other comparison value
     * @return {@code true} when all claims and audience shape are equal
     */
    @Override
    public boolean equals(final Object other) {
        return this == other || other instanceof JwtClaims claims && registered.equals(claims.registered)
                && extensions.equals(claims.extensions);
    }

    /**
     * Returns a hash derived from registered and extension claims.
     *
     * @return stable value hash
     */
    @Override
    public int hashCode() {
        return Objects.hash(registered, extensions);
    }

    /**
     * Returns a structural summary without rendering claim values.
     *
     * @return non-sensitive Claims Set summary
     */
    @Override
    public String toString() {
        return "JwtClaims[registered=" + values().values().keySet().stream().filter(JwtClaims::registered).toList()
                + ",extensions=" + extensions.values().keySet() + "]";
    }

    /**
     * Holds the seven registered RFC 7519 claims as explicit immutable components.
     *
     * @param issuer        optional issuer StringOrURI
     * @param subject       optional subject StringOrURI
     * @param audiences     ordered audience StringOrURI values
     * @param audienceArray whether a present audience uses the JSON array form
     * @param expiration    optional expiration NumericDate
     * @param notBefore     optional not-before NumericDate
     * @param issuedAt      optional issued-at NumericDate
     * @param jwtId         optional JWT identifier
     * @author Kimi Liu
     */
    public record Registered(Optional<String> issuer, Optional<String> subject, List<String> audiences,
            boolean audienceArray, Optional<Instant> expiration, Optional<Instant> notBefore,
            Optional<Instant> issuedAt, Optional<String> jwtId) {

        /**
         * Validates and freezes all registered claim components.
         *
         * @throws IllegalArgumentException if a component container is {@code null}
         * @throws ValidateException        if StringOrURI, audience, or wire-shape semantics are invalid
         */
        public Registered {
            Assert.notNull(issuer, "JWT issuer container must not be null");
            Assert.notNull(subject, "JWT subject container must not be null");
            Assert.notNull(audiences, "JWT audience list must not be null");
            Assert.notNull(expiration, "JWT expiration container must not be null");
            Assert.notNull(notBefore, "JWT not-before container must not be null");
            Assert.notNull(issuedAt, "JWT issued-at container must not be null");
            Assert.notNull(jwtId, "JWT identifier container must not be null");
            issuer.ifPresent(value -> {
                Assert.notBlank(value, "JWT issuer must not be blank");
                validateStringOrUri(value);
            });
            subject.ifPresent(value -> {
                Assert.notBlank(value, "JWT subject must not be blank");
                validateStringOrUri(value);
            });
            jwtId.ifPresent(value -> Assert.notBlank(value, "JWT identifier must not be blank"));
            final Set<String> unique = new LinkedHashSet<>();
            for (String audience : audiences) {
                Assert.notBlank(audience, "JWT audience must not be blank");
                validateStringOrUri(audience);
                if (!unique.add(audience)) {
                    throw new ValidateException("JWT audience array must not contain duplicates");
                }
            }
            audiences = List.copyOf(audiences);
            if (audiences.isEmpty() && audienceArray || audiences.size() > 1 && !audienceArray) {
                throw new ValidateException("JWT audience values do not match the selected wire shape");
            }
        }

    }

    /**
     * Preserves the decoded audience values and original JSON shape before registered-claim construction.
     *
     * @param values ordered audience values
     * @param array  whether the original claim used an array
     * @author Kimi Liu
     */
    private record Audience(List<String> values, boolean array) {

        /**
         * Freezes the decoded audience values.
         */
        private Audience {
            Assert.notNull(values, "Decoded JWT audience values must not be null");
            values = List.copyOf(values);
        }

    }

    /**
     * Carries one completed JSON-to-component Claims Set decode.
     *
     * @param registered decoded RFC 7519 claims
     * @param extensions decoded unregistered claims
     * @author Kimi Liu
     */
    private record Decoded(Registered registered, JsonValue.ObjectValue extensions) {

        /**
         * Validates the completed decode result.
         */
        private Decoded {
            Assert.notNull(registered, "Decoded JWT registered claims must not be null");
            Assert.notNull(extensions, "Decoded JWT extension claims must not be null");
        }

    }

}
