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
package org.miaixz.bus.auth.cache;

import java.time.Duration;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.function.Supplier;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.ExceptionKit;
import org.miaixz.bus.crypto.Builder;

/**
 * Adapts one atomic byte cache to the tenant-aware authentication state port.
 *
 * <p>
 * The adapter performs exactly one matching atomic cache call per state operation. It neither selects a backend nor
 * owns or closes the injected cache.
 * </p>
 *
 * @author Kimi Liu
 */
public final class AtomicCacheStateStore implements StateStore {

    /**
     * Milliseconds in one second.
     */
    private static final long MILLIS_PER_SECOND = 1_000L;

    /**
     * Nanoseconds in one millisecond.
     */
    private static final int NANOS_PER_MILLI = 1_000_000;

    /**
     * Prefix separating authentication state from other cache data.
     */
    private static final String KEY_PREFIX = "auth-state:";

    /**
     * Prefix identifying a complete protected authentication state key.
     */
    private static final String PROTECTED_PREFIX = "auth:9:";

    /**
     * Product-owned cache providing atomic asynchronous entry operations.
     */
    private final CacheX<String, byte[]> cache;

    /**
     * Creates a state adapter over a product-owned atomic cache.
     *
     * @param cache product-owned atomic byte cache
     * @throws NullPointerException if {@code cache} is null
     */
    private AtomicCacheStateStore(final CacheX<String, byte[]> cache) {
        this.cache = Objects.requireNonNull(cache, "Atomic cache must not be null");
    }

    /**
     * Creates a state adapter over a product-owned atomic cache.
     *
     * @param cache product-owned atomic byte cache
     * @return tenant-aware authentication state store
     * @throws NullPointerException if {@code cache} is null
     */
    public static AtomicCacheStateStore create(final CacheX<String, byte[]> cache) {
        return new AtomicCacheStateStore(cache);
    }

    /**
     * Invokes one cache operation and maps its completion without blocking.
     *
     * @param operation single cache operation
     * @param mapper    successful value mapper
     * @param <T>       cache result type
     * @param <R>       state result type
     * @return mapped asynchronous completion
     */
    private static <T, R> CompletionStage<R> invoke(
            final Supplier<CompletionStage<T>> operation,
            final Function<T, R> mapper) {
        final CompletionStage<T> stage;
        try {
            stage = Objects.requireNonNull(operation.get(), "Atomic cache operation returned no stage");
        } catch (final Throwable failure) {
            return CompletableFuture.failedFuture(stateFailure(failure));
        }
        return stage.handle((value, failure) -> {
            if (failure != null) {
                throw stateFailure(failure);
            }
            return mapper.apply(value);
        });
    }

    /**
     * Builds one Redis-Cluster-compatible tenant state key.
     *
     * @param invocation operation context
     * @param key        logical state key
     * @return tenant-scoped cache key containing exactly one hash tag
     */
    private static String cacheKey(final Context invocation, final String key) {
        final Context current = Objects.requireNonNull(invocation, "Context must not be null");
        final String tenant = token(current.tenantId(), "Tenant identifier");
        if (key != null && key.startsWith(PROTECTED_PREFIX)) {
            return protectedKey(tenant, key);
        }
        final String stateKey = token(key, "State key");
        return KEY_PREFIX + Symbol.C_BRACE_LEFT + tenant + Symbol.C_BRACE_RIGHT + Symbol.COLON + stateKey;
    }

    /**
     * Validates and preserves one complete tenant-hashed state key.
     *
     * @param tenant exact invocation tenant
     * @param key    complete protected state key
     * @return unchanged protected state key
     */
    private static String protectedKey(final String tenant, final String key) {
        final String tenantHash = Builder.sha256().digestHex(tenant, Charset.UTF_8).toLowerCase(Locale.ROOT);
        final String prefix = PROTECTED_PREFIX + Symbol.C_BRACE_LEFT + tenantHash + Symbol.C_BRACE_RIGHT
                + Symbol.C_COLON;
        if (!key.startsWith(prefix) || key.indexOf(Symbol.C_BRACE_LEFT, prefix.length()) >= Normal._0
                || key.indexOf(Symbol.C_BRACE_RIGHT, prefix.length()) >= Normal._0) {
            throw new ValidateException("Protected state key must contain the invocation tenant hash tag only");
        }
        final String[] fields = key.substring(prefix.length()).split(String.valueOf(Symbol.C_COLON), -1);
        if (fields.length != Normal._3 || !asciiToken(fields[Normal._0]) || !asciiToken(fields[Normal._1])
                || fields[Normal._2].length() != Normal._64 || !lowerHex(fields[Normal._2])) {
            throw new ValidateException("Protected state key format is invalid");
        }
        return key;
    }

    /**
     * Reports whether a field is a non-empty lowercase ASCII key token.
     *
     * @param value candidate field
     * @return {@code true} for a valid key token
     */
    private static boolean asciiToken(final String value) {
        if (value.isEmpty()) {
            return false;
        }
        for (int index = Normal._0; index < value.length(); index++) {
            final char current = value.charAt(index);
            final boolean letter = current >= 'a' && current <= 'z';
            final boolean digit = current >= Symbol.C_ZERO && current <= '9';
            if (!letter && !digit && current != Symbol.C_MINUS && current != '_') {
                return false;
            }
        }
        return true;
    }

    /**
     * Reports whether a field is lowercase hexadecimal text.
     *
     * @param value candidate field
     * @return {@code true} for lowercase hexadecimal text
     */
    private static boolean lowerHex(final String value) {
        for (int index = Normal._0; index < value.length(); index++) {
            final char current = value.charAt(index);
            if (!(current >= Symbol.C_ZERO && current <= '9' || current >= 'a' && current <= 'f')) {
                return false;
            }
        }
        return true;
    }

    /**
     * Validates text that must not introduce another Redis hash tag.
     *
     * @param value source text
     * @param name  diagnostic name
     * @return validated text
     */
    private static String token(final String value, final String name) {
        if (value == null || value.isBlank()) {
            throw new ValidateException(name + " must not be blank");
        }
        if (value.indexOf(Symbol.C_BRACE_LEFT) >= 0 || value.indexOf(Symbol.C_BRACE_RIGHT) >= 0) {
            throw new ValidateException(name + " must not contain braces");
        }
        return value;
    }

    /**
     * Converts a positive duration to a positive millisecond ceiling without overflow.
     *
     * @param ttl source duration
     * @return positive cache lifetime in milliseconds
     */
    private static long ttlMillis(final Duration ttl) {
        final Duration current = Objects.requireNonNull(ttl, "State lifetime must not be null");
        if (current.isZero() || current.isNegative()) {
            throw new ValidateException("State lifetime must be positive");
        }
        try {
            final long seconds = Math.multiplyExact(current.getSeconds(), MILLIS_PER_SECOND);
            final long nanos = (current.getNano() + (long) NANOS_PER_MILLI - 1L) / NANOS_PER_MILLI;
            return Math.addExact(seconds, nanos);
        } catch (final ArithmeticException failure) {
            throw new ValidateException("State lifetime exceeds the millisecond range");
        }
    }

    /**
     * Returns an independent required byte array.
     *
     * @param value source bytes
     * @param name  diagnostic name
     * @return independent bytes
     */
    private static byte[] bytes(final byte[] value, final String name) {
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value.clone();
    }

    /**
     * Converts nullable cache bytes to an optional independent snapshot.
     *
     * @param value nullable cache bytes
     * @return optional independent bytes
     */
    private static Optional<byte[]> optionalBytes(final byte[] value) {
        return value == null ? Optional.empty() : Optional.of(value.clone());
    }

    /**
     * Rejects an invalid null Boolean returned by a cache implementation.
     *
     * @param value cache Boolean
     * @return primitive cache result
     */
    private static boolean requiredBoolean(final Boolean value) {
        if (value == null) {
            throw stateFailure(new NullPointerException("Atomic cache operation returned no Boolean result"));
        }
        return value;
    }

    /**
     * Maps one cache failure to the stable internal-processing error without exposing backend details.
     *
     * @param failure cache failure
     * @return stable protocol exception retaining the original cause
     */
    private static ProtocolException stateFailure(final Throwable failure) {
        final Throwable cause = ExceptionKit.unwrap(Objects.requireNonNull(failure, "Cache failure must not be null"));
        return new ProtocolException(ErrorCode._100805.getKey(), ErrorCode._100805.getValue(), cause);
    }

    /**
     * Creates tenant-scoped state when no unexpired value exists.
     *
     * @param invocation operation context
     * @param key        state key
     * @param value      state bytes
     * @param ttl        positive state lifetime
     * @return stage containing whether the state was created
     */
    @Override
    public CompletionStage<Boolean> putIfAbsent(
            final Context invocation,
            final String key,
            final byte[] value,
            final Duration ttl) {
        final String cacheKey = cacheKey(invocation, key);
        final byte[] snapshot = bytes(value, "State value");
        final long ttlMillis = ttlMillis(ttl);
        return invoke(() -> cache.create(cacheKey, snapshot, ttlMillis), AtomicCacheStateStore::requiredBoolean);
    }

    /**
     * Reads tenant-scoped state without consuming it.
     *
     * @param invocation operation context
     * @param key        state key
     * @return stage containing an independent value or an empty optional
     */
    @Override
    public CompletionStage<Optional<byte[]>> get(final Context invocation, final String key) {
        final String cacheKey = cacheKey(invocation, key);
        return invoke(() -> cache.get(cacheKey), AtomicCacheStateStore::optionalBytes);
    }

    /**
     * Atomically consumes tenant-scoped state.
     *
     * @param invocation operation context
     * @param key        state key
     * @return stage containing an independent consumed value or an empty optional
     */
    @Override
    public CompletionStage<Optional<byte[]>> take(final Context invocation, final String key) {
        final String cacheKey = cacheKey(invocation, key);
        return invoke(() -> cache.take(cacheKey), AtomicCacheStateStore::optionalBytes);
    }

    /**
     * Atomically replaces matching tenant-scoped state.
     *
     * @param invocation operation context
     * @param key        state key
     * @param expected   expected state bytes
     * @param update     replacement state bytes
     * @param ttl        positive replacement lifetime
     * @return stage containing whether the state was replaced
     */
    @Override
    public CompletionStage<Boolean> compareAndSet(
            final Context invocation,
            final String key,
            final byte[] expected,
            final byte[] update,
            final Duration ttl) {
        final String cacheKey = cacheKey(invocation, key);
        final byte[] expectedSnapshot = bytes(expected, "Expected state value");
        final byte[] updateSnapshot = bytes(update, "Replacement state value");
        final long ttlMillis = ttlMillis(ttl);
        return invoke(
                () -> cache.replace(cacheKey, expectedSnapshot, updateSnapshot, ttlMillis),
                AtomicCacheStateStore::requiredBoolean);
    }

    /**
     * Atomically deletes tenant-scoped state.
     *
     * @param invocation operation context
     * @param key        state key
     * @return stage containing whether the state was deleted
     */
    @Override
    public CompletionStage<Boolean> remove(final Context invocation, final String key) {
        final String cacheKey = cacheKey(invocation, key);
        return invoke(() -> cache.delete(cacheKey), AtomicCacheStateStore::requiredBoolean);
    }

}
