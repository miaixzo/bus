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

import java.io.Serial;
import java.nio.charset.Charset;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.*;

import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.StatefulException;

/**
 * JWT Payload information. The payload is the part where effective information is stored. This name is like referring
 * to the cargo carried on an airplane. This effective information includes three parts:
 *
 * <ul>
 * <li>Standard registered claims</li>
 * <li>Public claims</li>
 * <li>Private claims</li>
 * </ul>
 *
 * @author Kimi Liu
 */
public class JWTPayload extends Claims implements JWTRegister<JWTPayload> {

    /**
     * Serialization version identifier.
     */
    @Serial
    private static final long serialVersionUID = 2852289330860L;
    /**
     * Whether hardened parsing has permanently frozen this payload.
     */
    private boolean frozen;

    /**
     * Constructs a new JWTPayload instance.
     */
    public JWTPayload() {
        // No initialization required.
    }

    /**
     * Creates a defensively copied and permanently immutable hardened payload.
     *
     * @param claims parsed standard and private claims
     * @return immutable payload
     */
    public static JWTPayload immutable(final Map<String, ?> claims) {
        if (claims == null) {
            throw new ProtocolException(ErrorCode._100533);
        }
        final JWTPayload payload = new JWTPayload();
        for (final Map.Entry<String, ?> entry : claims.entrySet()) {
            payload.setClaim(entry.getKey(), copy(entry.getValue()));
        }
        payload.frozen = true;
        return payload;
    }

    /**
     * Creates an immutable recursive copy of an accepted JSON value.
     *
     * @param value source JSON value
     * @return independent immutable value
     */
    private static Object copy(final Object value) {
        if (value == null || value instanceof String || value instanceof Number || value instanceof Boolean) {
            return value;
        }
        if (value instanceof Map<?, ?> values) {
            final LinkedHashMap<String, Object> result = new LinkedHashMap<>();
            for (final Map.Entry<?, ?> entry : values.entrySet()) {
                if (!(entry.getKey() instanceof String key) || result.put(key, copy(entry.getValue())) != null) {
                    reject();
                }
            }
            return Collections.unmodifiableMap(result);
        }
        if (value instanceof Collection<?> values) {
            final List<Object> result = new ArrayList<>(values.size());
            for (final Object item : values) {
                result.add(copy(item));
            }
            return Collections.unmodifiableList(result);
        }
        if (value instanceof Object[] values) {
            final List<Object> result = new ArrayList<>(values.length);
            for (final Object item : values) {
                result.add(copy(item));
            }
            return Collections.unmodifiableList(result);
        }
        reject();
        return null;
    }

    /**
     * Rejects a malformed standard claim with the stable token-format error.
     */
    private static void reject() {
        throw new ProtocolException(ErrorCode._100533);
    }

    /**
     * Adds custom JWT authentication payload information.
     *
     * @param payloadClaims a map containing multiple payload claims to add
     * @return this {@link JWTPayload} instance
     */
    public JWTPayload addPayloads(final Map<String, ?> payloadClaims) {
        mutable();
        putAll(payloadClaims);
        return this;
    }

    /**
     * Sets a specific payload claim with the given name and value.
     *
     * @param name  the name of the claim
     * @param value the value of the claim
     * @return this {@link JWTPayload} instance
     */
    @Override
    public JWTPayload setPayload(final String name, final Object value) {
        mutable();
        setClaim(name, value);
        return this;
    }

    /**
     * Preserves compatibility mutation for ordinary payloads and rejects it after hardening.
     *
     * @param name  claim name
     * @param value claim value
     */
    @Override
    public void setClaim(final String name, final Object value) {
        mutable();
        super.setClaim(name, value);
    }

    /**
     * Preserves compatibility bulk mutation for ordinary payloads and rejects it after hardening.
     *
     * @param claims added claims
     */
    @Override
    public void putAll(final Map<String, ?> claims) {
        mutable();
        super.putAll(claims);
    }

    /**
     * Preserves compatibility parsing for ordinary payloads and rejects it after hardening.
     *
     * @param tokenPart encoded payload segment
     * @param charset   decoding charset
     */
    @Override
    public void parse(final String tokenPart, final Charset charset) {
        mutable();
        super.parse(tokenPart, charset);
    }

    /**
     * Returns compatibility storage while mutable and an immutable deep snapshot after hardening.
     *
     * @return claim map
     */
    @Override
    public Map<String, Object> getClaimsJson() {
        return frozen ? claims() : super.getClaimsJson();
    }

    /**
     * Returns an immutable deep claim snapshot.
     *
     * @return recursively immutable claims
     */
    public Map<String, Object> claims() {
        final LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        for (final Map.Entry<String, Object> entry : super.getClaimsJson().entrySet()) {
            result.put(entry.getKey(), copy(entry.getValue()));
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Returns the optional exact issuer string.
     *
     * @return optional issuer
     */
    public Optional<String> issuer() {
        return text(ISSUER);
    }

    /**
     * Returns the optional exact subject string.
     *
     * @return optional subject
     */
    public Optional<String> subject() {
        return text(SUBJECT);
    }

    /**
     * Returns the exact immutable audience set from a string or string array claim.
     *
     * @return immutable audience set
     */
    public Set<String> audiences() {
        final Object value = getClaim(AUDIENCE);
        if (value == null) {
            return Set.of();
        }
        if (value instanceof String audience && !audience.isBlank()) {
            return Set.of(audience);
        }
        if (!(value instanceof Collection<?>)) {
            reject();
        }
        final Collection<?> values = (Collection<?>) value;
        if (values.isEmpty()) {
            reject();
        }
        final LinkedHashSet<String> result = new LinkedHashSet<>();
        for (final Object item : values) {
            if (!(item instanceof String audience) || audience.isBlank() || !result.add(audience)) {
                reject();
            }
        }
        return Set.copyOf(result);
    }

    /**
     * Returns the optional expiration instant.
     *
     * @return optional expiration
     */
    public Optional<Instant> expiresAt() {
        return numericDate(EXPIRES_AT);
    }

    /**
     * Returns the optional not-before instant.
     *
     * @return optional not-before time
     */
    public Optional<Instant> notBefore() {
        return numericDate(NOT_BEFORE);
    }

    /**
     * Returns the optional issued-at instant.
     *
     * @return optional issued-at time
     */
    public Optional<Instant> issuedAt() {
        return numericDate(ISSUED_AT);
    }

    /**
     * Returns the optional exact JWT identifier.
     *
     * @return optional JWT identifier
     */
    public Optional<String> jwtId() {
        return text(JWT_ID);
    }

    /**
     * Reads one optional non-blank string claim.
     *
     * @param name claim name
     * @return optional exact string
     */
    private Optional<String> text(final String name) {
        final Object value = getClaim(name);
        if (value == null) {
            return Optional.empty();
        }
        if (!(value instanceof String)) {
            reject();
        }
        final String text = (String) value;
        if (text.isBlank()) {
            reject();
        }
        return Optional.of(text);
    }

    /**
     * Reads one optional integral NumericDate claim without narrowing or truncation.
     *
     * @param name claim name
     * @return optional instant
     */
    private Optional<Instant> numericDate(final String name) {
        final Object value = getClaim(name);
        if (value == null) {
            return Optional.empty();
        }
        if (!(value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long)) {
            reject();
        }
        try {
            return Optional.of(Instant.ofEpochSecond(((Number) value).longValue()));
        } catch (final DateTimeException failure) {
            throw new ProtocolException(ErrorCode._100301.getKey(), ErrorCode._100301.getValue(), failure);
        }
    }

    /**
     * Requires this compatibility payload to remain mutable.
     */
    private void mutable() {
        if (frozen) {
            throw new StatefulException("JWT payload is immutable");
        }
    }

}
