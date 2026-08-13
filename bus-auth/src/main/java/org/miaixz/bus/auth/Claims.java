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
package org.miaixz.bus.auth;

import java.net.URI;
import java.time.Instant;
import java.util.*;

import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Immutable protocol-neutral authenticated claim snapshot.
 *
 * <p>
 * Claim containers are copied recursively and sensitive values are never rendered by {@link #toString()}.
 * </p>
 *
 * @author Kimi Liu
 */
public final class Claims {

    /**
     * Shared immutable empty claim snapshot.
     */
    private static final Claims EMPTY = new Claims(Map.of());

    /**
     * Recursively snapshotted claim values indexed by validated claim name.
     */
    private final Map<String, Object> values;

    /**
     * Creates a claim snapshot from already copied values.
     *
     * @param values recursively immutable claim values
     */
    private Claims(final Map<String, Object> values) {
        this.values = Collections.unmodifiableMap(values);
    }

    /**
     * Returns the shared empty claim snapshot.
     *
     * @return immutable empty claims
     */
    public static Claims empty() {
        return EMPTY;
    }

    /**
     * Recursively snapshots a claim map.
     *
     * @param source source claim map
     * @return immutable defensive claim snapshot
     * @throws ValidateException if a claim name or value type is invalid
     */
    public static Claims from(final Map<String, ?> source) {
        if (source == null) {
            throw new ValidateException("Claims must not be null");
        }
        if (source.isEmpty()) {
            return empty();
        }
        final LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        source.forEach((name, value) -> result.put(name(name), snapshot(value)));
        return new Claims(result);
    }

    /**
     * Validates and trims a claim name.
     *
     * @param value claim name
     * @return trimmed claim name
     * @throws ValidateException if the name is null or blank
     */
    private static String name(final String value) {
        if (value == null || value.isBlank()) {
            throw new ValidateException("Claim name must not be blank");
        }
        return value.trim();
    }

    /**
     * Recursively creates a defensive claim value snapshot.
     *
     * @param value claim value
     * @return immutable or safely copied claim value
     * @throws ValidateException if the value type is unsupported
     */
    private static Object snapshot(final Object value) {
        if (value == null || value instanceof String || value instanceof Number || value instanceof Boolean
                || value instanceof Character || value instanceof Enum<?> || value instanceof Instant
                || value instanceof URI) {
            return value;
        }
        if (value instanceof byte[]) {
            return ((byte[]) value).clone();
        }
        if (value instanceof Map<?, ?>) {
            final LinkedHashMap<String, Object> result = new LinkedHashMap<>();
            ((Map<?, ?>) value).forEach((key, item) -> {
                if (!(key instanceof String)) {
                    throw new ValidateException("Claim object keys must be strings");
                }
                result.put(name((String) key), snapshot(item));
            });
            return Collections.unmodifiableMap(result);
        }
        if (value instanceof Collection<?>) {
            final List<Object> result = new ArrayList<>();
            for (final Object item : (Collection<?>) value) {
                result.add(snapshot(item));
            }
            return Collections.unmodifiableList(result);
        }
        if (value instanceof Object[]) {
            final List<Object> result = new ArrayList<>();
            for (final Object item : (Object[]) value) {
                result.add(snapshot(item));
            }
            return Collections.unmodifiableList(result);
        }
        throw new ValidateException("Unsupported claim value type: " + value.getClass().getName());
    }

    /**
     * Creates a defensive external view of a recursively snapshotted value.
     *
     * @param value internally owned claim value
     * @return immutable or defensively copied external value
     */
    private static Object external(final Object value) {
        if (value instanceof byte[]) {
            return ((byte[]) value).clone();
        }
        if (value instanceof Map<?, ?> map) {
            final LinkedHashMap<String, Object> result = new LinkedHashMap<>();
            map.forEach((key, item) -> result.put((String) key, external(item)));
            return Collections.unmodifiableMap(result);
        }
        if (value instanceof Collection<?> collection) {
            final List<Object> result = new ArrayList<>();
            collection.forEach(item -> result.add(external(item)));
            return Collections.unmodifiableList(result);
        }
        return value;
    }

    /**
     * Returns the registered issuer claim.
     *
     * @return optional issuer
     * @throws ValidateException if the stored issuer is not text
     */
    public Optional<String> issuer() {
        return find("iss", String.class);
    }

    /**
     * Returns the registered subject claim.
     *
     * @return optional subject
     * @throws ValidateException if the stored subject is not text
     */
    public Optional<String> subject() {
        return find("sub", String.class);
    }

    /**
     * Returns the registered audience claim as an immutable set.
     *
     * @return immutable audience values
     * @throws ValidateException if the stored audience shape is invalid
     */
    public Set<String> audience() {
        final Object audience = values.get("aud");
        if (audience == null) {
            return Set.of();
        }
        if (audience instanceof String) {
            return Set.of((String) audience);
        }
        if (audience instanceof Collection<?>) {
            final LinkedHashSet<String> result = new LinkedHashSet<>();
            for (final Object entry : (Collection<?>) audience) {
                if (!(entry instanceof String)) {
                    throw new ValidateException("Audience claim must contain strings");
                }
                result.add((String) entry);
            }
            return Collections.unmodifiableSet(result);
        }
        throw new ValidateException("Audience claim has an invalid type");
    }

    /**
     * Returns the registered issued-at instant.
     *
     * @return optional issued-at instant
     * @throws ValidateException if the value is not an instant or epoch seconds
     */
    public Optional<Instant> issuedAt() {
        return instant("iat");
    }

    /**
     * Returns the registered expiration instant.
     *
     * @return optional expiration instant
     * @throws ValidateException if the value is not an instant or epoch seconds
     */
    public Optional<Instant> expiresAt() {
        return instant("exp");
    }

    /**
     * Looks up a claim with a required runtime type.
     *
     * @param name claim name
     * @param type required value type
     * @param <T>  required value type
     * @return optional typed claim
     * @throws ValidateException if the name or type is invalid, or the value has another type
     */
    public <T> Optional<T> find(final String name, final Class<T> type) {
        if (type == null) {
            throw new ValidateException("Claim type must not be null");
        }
        final Object value = values.get(name(name));
        if (value == null) {
            return Optional.empty();
        }
        if (!type.isInstance(value)) {
            throw new ValidateException("Claim does not match requested type: " + name);
        }
        return Optional.of(type.cast(external(value)));
    }

    /**
     * Returns a required typed claim.
     *
     * @param name claim name
     * @param type required value type
     * @param <T>  required value type
     * @return typed claim value
     * @throws ValidateException if the claim is absent or has another type
     */
    public <T> T require(final String name, final Class<T> type) {
        return find(name, type).orElseThrow(() -> new ValidateException("Required claim is missing: " + name));
    }

    /**
     * Returns a new snapshot containing a replaced claim.
     *
     * @param name  claim name
     * @param value supported claim value
     * @return immutable updated claims
     * @throws ValidateException if the name or value is invalid
     */
    public Claims with(final String name, final Object value) {
        final LinkedHashMap<String, Object> result = new LinkedHashMap<>(values);
        result.put(name(name), snapshot(value));
        return new Claims(result);
    }

    /**
     * Returns a snapshot without the named claim.
     *
     * @param name claim name
     * @return this instance when absent, otherwise an immutable updated snapshot
     * @throws ValidateException if {@code name} is invalid
     */
    public Claims without(final String name) {
        final String key = name(name);
        if (!values.containsKey(key)) {
            return this;
        }
        final LinkedHashMap<String, Object> result = new LinkedHashMap<>(values);
        result.remove(key);
        return result.isEmpty() ? empty() : new Claims(result);
    }

    /**
     * Returns the recursively immutable claim map.
     *
     * @return immutable claim map
     */
    public Map<String, Object> snapshot() {
        final LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        values.forEach((name, value) -> result.put(name, external(value)));
        return Collections.unmodifiableMap(result);
    }

    /**
     * Reads an instant claim represented as {@link Instant} or epoch seconds.
     *
     * @param name registered claim name
     * @return optional instant
     * @throws ValidateException if the claim has an invalid type
     */
    private Optional<Instant> instant(final String name) {
        final Object value = values.get(name);
        if (value == null) {
            return Optional.empty();
        }
        if (value instanceof Instant) {
            return Optional.of((Instant) value);
        }
        if (value instanceof Number) {
            return Optional.of(Instant.ofEpochSecond(((Number) value).longValue()));
        }
        throw new ValidateException("Claim is not an epoch timestamp: " + name);
    }

    /**
     * Returns a redacted representation that never exposes claim values.
     *
     * @return redacted representation
     */
    @Override
    public String toString() {
        return "Claims[REDACTED]";
    }

}
