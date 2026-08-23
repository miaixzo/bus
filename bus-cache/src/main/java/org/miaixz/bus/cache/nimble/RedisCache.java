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
package org.miaixz.bus.cache.nimble;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import jakarta.annotation.PreDestroy;

import org.miaixz.bus.cache.Builder;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cache.Serializer;
import org.miaixz.bus.cache.magic.CacheExpire;
import org.miaixz.bus.cache.serialize.Hessian2Serializer;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.logger.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.params.SetParams;
import redis.clients.jedis.resps.ScanResult;

/**
 * A Redis-based implementation of {@link CacheX} for a single-node setup.
 * <p>
 * This class uses a {@link JedisPool} to provide a distributed caching solution. It handles the serialization and
 * deserialization of cache values. The lifecycle of the {@code JedisPool} is managed via the {@link PreDestroy}
 * annotation, ensuring resources are released.
 * </p>
 *
 * @param <K> The type of keys, which are assumed to be convertible to {@link String}.
 * @param <V> The type of values, which must be serializable.
 * @author Kimi Liu
 */
public class RedisCache<K, V> implements CacheX<K, V>, AutoCloseable {

    /**
     * The serializer used for converting values to and from byte arrays.
     */
    private final Serializer serializer;

    /**
     * The connection pool for Jedis instances.
     */
    private final JedisPool jedisPool;

    /**
     * Optional caller-owned executor that enables the atomic asynchronous entry points.
     */
    private final Executor executor;

    /**
     * Number of admitted asynchronous commands that have not completed.
     */
    private int inFlight;

    /**
     * Whether pool shutdown has been submitted to the executor.
     */
    private boolean closeScheduled;

    /**
     * Shared asynchronous close completion, or {@code null} before close begins.
     */
    private CompletableFuture<Void> closeFuture;

    /**
     * Monitor guarding asynchronous admission and resource shutdown.
     */
    private final Object lifecycle = new Object();

    /**
     * Constructs a {@code RedisCache} with a given Jedis pool and a default {@link Hessian2Serializer}.
     *
     * @param jedisPool The configured {@link JedisPool}.
     */
    public RedisCache(JedisPool jedisPool) {
        this(jedisPool, new Hessian2Serializer());
    }

    /**
     * Constructs a {@code RedisCache} with a given Jedis pool and a custom serializer.
     *
     * @param jedisPool  The configured {@link JedisPool}.
     * @param serializer The {@link Serializer} to use for value serialization.
     */
    public RedisCache(JedisPool jedisPool, Serializer serializer) {
        this(jedisPool, serializer, null);
    }

    /**
     * Constructs a Redis cache with atomic asynchronous operations executed by a caller-owned executor.
     *
     * @param jedisPool  configured Jedis pool owned by this cache
     * @param serializer serializer used for values
     * @param executor   caller-owned executor for blocking Redis commands
     */
    public RedisCache(JedisPool jedisPool, Serializer serializer, Executor executor) {
        this.jedisPool = jedisPool;
        this.serializer = serializer;
        this.executor = executor;
    }

    /**
     * Reads a single value from the cache.
     *
     * @param key The key whose value to retrieve. It is converted to a string.
     * @return The deserialized value, or {@code null} if the key does not exist.
     */
    @Override
    public V read(K key) {
        try (Jedis client = jedisPool.getResource()) {
            byte[] bytes = client.get(key.toString().getBytes());
            return bytes != null ? serializer.deserialize(bytes) : null;
        }
    }

    /**
     * Writes a single key-value pair to the cache with a specified expiration.
     *
     * @param key    The key to write. It is converted to a string.
     * @param value  The value to be serialized and stored.
     * @param expire The expiration time in milliseconds. If {@link CacheExpire#FOREVER}, the key will not expire.
     */
    @Override
    public void write(K key, V value, long expire) {
        try (Jedis client = jedisPool.getResource()) {
            byte[] bytesValue = serializer.serialize(value);
            if (expire == CacheExpire.FOREVER) {
                client.set(key.toString().getBytes(), bytesValue);
            } else {
                client.psetex(key.toString().getBytes(), expire, bytesValue);
            }
        }
    }

    /**
     * Reads multiple values from the cache in a batch.
     *
     * @param keys A collection of keys to retrieve. Each key is converted to a string.
     * @return A map of keys to their deserialized values for all keys found in the cache.
     */
    @Override
    public Map<K, V> read(Collection<K> keys) {
        try (Jedis client = jedisPool.getResource()) {
            List<byte[]> bytesValues = client.mget(Builder.toByteArray((Collection<String>) keys));
            return (Map<K, V>) Builder.toObjectMap((Collection<String>) keys, bytesValues, this.serializer);
        }
    }

    /**
     * Writes multiple key-value pairs to the cache in a batch.
     *
     * @param keyValueMap A map of key-value pairs to store.
     * @param expire      The expiration time in milliseconds. If {@link CacheExpire#FOREVER}, `MSET` is used.
     *                    Otherwise, a pipeline of `PSETEX` commands is used.
     */
    @Override
    public void write(Map<K, V> keyValueMap, long expire) {
        try (Jedis client = jedisPool.getResource()) {
            byte[][] kvs = Builder.toByteArray((Map<String, Object>) keyValueMap, serializer);
            if (expire == CacheExpire.FOREVER) {
                client.mset(kvs);
            } else {
                try (Pipeline pipeline = client.pipelined()) {
                    for (int i = 0; i < kvs.length; i += 2) {
                        pipeline.psetex(kvs[i], expire, kvs[i + 1]);
                    }
                    pipeline.sync();
                }
            }
        }
    }

    /**
     * Removes entries from the cache.
     *
     * @param keys The keys to remove.
     */
    @Override
    public void remove(K... keys) {
        if (keys.length == 0) {
            return;
        }
        try (Jedis client = jedisPool.getResource()) {
            byte[][] rawKeys = new byte[keys.length][];
            for (int i = 0; i < keys.length; i++) {
                rawKeys[i] = keys[i].toString().getBytes();
            }
            client.del(rawKeys);
        }
    }

    /**
     * Flushes all data from the current Redis database.
     * <p>
     * <strong>Warning:</strong> This clears every key in the current DB. Use {@code scan() + remove()} if only a
     * specific key prefix should be removed. To shut down the connection pool use {@link #close()}.
     * </p>
     */
    @Override
    public void clear() {
        Logger.info(true, "Cache", "Redis cache clear started");
        try (Jedis client = jedisPool.getResource()) {
            client.flushDB();
        }
        Logger.info(false, "Cache", "Redis cache clear completed");
    }

    /**
     * Atomically increments the counter stored at the given key and returns the new value.
     * <p>
     * Uses the Redis {@code INCR} command. If the key does not exist it is created with value {@code 0} and then
     * incremented, returning {@code 1}. The counter has no TTL and persists until explicitly removed.
     * </p>
     *
     * @param key the counter key
     * @return the new counter value after increment
     */
    @Override
    public long increment(K key) {
        try (Jedis client = jedisPool.getResource()) {
            return client.incr(key.toString());
        }
    }

    /**
     * Scans and returns all key-value pairs whose keys start with the given prefix.
     * <p>
     * Uses the non-blocking Redis {@code SCAN} cursor command followed by a batched {@code MGET} to retrieve values in
     * minimal round-trips. {@code count(200)} is a hint to the server about scan granularity per cursor step.
     * </p>
     *
     * @param prefix the key prefix to match
     * @return a map of all matching key-value pairs
     */
    @Override
    public Map<K, V> scan(K prefix) {
        Map<K, V> result = new LinkedHashMap<>();
        String pattern = prefix.toString() + Symbol.STAR;
        Logger.debug(true, "Cache", "Redis cache scan started: prefixPresent={}", prefix != null);
        try (Jedis client = jedisPool.getResource()) {
            String cursor = Symbol.ZERO;
            ScanParams params = new ScanParams().match(pattern).count(200);
            do {
                ScanResult<String> batch = client.scan(cursor, params);
                cursor = batch.getCursor();
                List<String> batchKeys = batch.getResult();
                if (!batchKeys.isEmpty()) {
                    byte[][] rawKeys = batchKeys.stream().map(String::getBytes).toArray(byte[][]::new);
                    List<byte[]> values = client.mget(rawKeys);
                    for (int i = 0; i < batchKeys.size(); i++) {
                        if (values.get(i) != null) {
                            result.put((K) batchKeys.get(i), serializer.deserialize(values.get(i)));
                        }
                    }
                }
            } while (!Symbol.ZERO.equals(cursor));
        }
        Logger.debug(
                false,
                "Cache",
                "Redis cache scan completed: prefixPresent={}, resultCount={}",
                prefix != null,
                result.size());
        return result;
    }

    /**
     * Refreshes the TTL of an existing entry using the Redis {@code PEXPIRE} command.
     * <p>
     * More efficient than the default read-then-write approach as it issues a single command without touching the
     * value.
     * </p>
     *
     * @param key    the key whose TTL to refresh
     * @param expire the new expiration time in milliseconds
     * @return {@code true} if the TTL was set; {@code false} if the key does not exist
     */
    @Override
    public boolean renew(K key, long expire) {
        try (Jedis client = jedisPool.getResource()) {
            boolean renewed = client.pexpire(key.toString(), expire) == 1L;
            Logger.debug(
                    false,
                    "Cache",
                    "Redis cache ttl renewed: keyPresent={}, expireMs={}, renewed={}",
                    key != null,
                    expire,
                    renewed);
            return renewed;
        }
    }

    /**
     * Atomically creates a serialized entry with NX and PX semantics.
     *
     * @param key       cache key
     * @param value     cache value
     * @param ttlMillis positive time to live in milliseconds
     * @return stage containing whether the entry was created
     */
    @Override
    public CompletionStage<Boolean> create(K key, V value, long ttlMillis) {
        if (executor == null) {
            return CacheX.super.create(key, value, ttlMillis);
        }
        requirePositiveTtl(ttlMillis);
        byte[] keyBytes = keyBytes(key);
        byte[] valueBytes = serializer.serialize(copyValue(Objects.requireNonNull(value, "value")));
        return submit(() -> {
            try (Jedis client = jedisPool.getResource()) {
                return client.set(keyBytes, valueBytes, SetParams.setParams().nx().px(ttlMillis)) != null;
            }
        });
    }

    /**
     * Reports whether a caller-owned executor was supplied for the complete atomic command set.
     *
     * @return {@code true} when asynchronous atomic operations are configured
     */
    @Override
    public boolean supports() {
        return executor != null;
    }

    /**
     * Reads and deserializes one value on the caller-owned executor.
     *
     * @param key cache key
     * @return stage containing a defensive value copy or {@code null}
     */
    @Override
    public CompletionStage<V> get(K key) {
        if (executor == null) {
            return CacheX.super.get(key);
        }
        byte[] keyBytes = keyBytes(key);
        return submit(() -> {
            try (Jedis client = jedisPool.getResource()) {
                byte[] value = client.get(keyBytes);
                return value == null ? null : copyValue(serializer.deserialize(value));
            }
        });
    }

    /**
     * Atomically reads, removes, and deserializes one value.
     *
     * @param key cache key
     * @return stage containing the removed value or {@code null}
     */
    @Override
    public CompletionStage<V> take(K key) {
        if (executor == null) {
            return CacheX.super.take(key);
        }
        byte[] keyBytes = keyBytes(key);
        return submit(() -> {
            try (Jedis client = jedisPool.getResource()) {
                byte[] value = client.getDel(keyBytes);
                return value == null ? null : copyValue(serializer.deserialize(value));
            }
        });
    }

    /**
     * Atomically replaces a serialized value when it equals the expected serialized value.
     *
     * @param key       cache key
     * @param expected  expected cache value
     * @param update    replacement cache value
     * @param ttlMillis positive replacement time to live in milliseconds
     * @return stage containing whether the entry was replaced
     */
    @Override
    public CompletionStage<Boolean> replace(K key, V expected, V update, long ttlMillis) {
        if (executor == null) {
            return CacheX.super.replace(key, expected, update, ttlMillis);
        }
        requirePositiveTtl(ttlMillis);
        byte[] keyBytes = keyBytes(key);
        byte[] expectedBytes = serializer.serialize(copyValue(Objects.requireNonNull(expected, "expected")));
        byte[] updateBytes = serializer.serialize(copyValue(Objects.requireNonNull(update, "update")));
        byte[] ttlBytes = Long.toString(ttlMillis).getBytes(Charset.US_ASCII);
        return submit(() -> {
            try (Jedis client = jedisPool.getResource()) {
                byte[] script = ("if redis.call('GET', KEYS[1]) == ARGV[1] then "
                        + "redis.call('SET', KEYS[1], ARGV[2], 'PX', ARGV[3]); return 1 else return 0 end")
                                .getBytes(Charset.UTF_8);
                Object result = client.eval(script, List.of(keyBytes), List.of(expectedBytes, updateBytes, ttlBytes));
                return Long.valueOf(1L).equals(result);
            }
        });
    }

    /**
     * Atomically deletes one entry.
     *
     * @param key cache key
     * @return stage containing whether an entry was deleted
     */
    @Override
    public CompletionStage<Boolean> delete(K key) {
        if (executor == null) {
            return CacheX.super.delete(key);
        }
        byte[] keyBytes = keyBytes(key);
        return submit(() -> {
            try (Jedis client = jedisPool.getResource()) {
                return client.del(keyBytes) == 1L;
            }
        });
    }

    /**
     * Closes and destroys the {@link JedisPool}, releasing all pooled connections.
     * <p>
     * Annotated with {@link PreDestroy} so a DI container invokes it automatically on bean destruction. Also implements
     * {@link AutoCloseable} so the cache can be used in a try-with-resources block.
     * </p>
     */
    @PreDestroy
    @Override
    public void close() {
        if (executor != null) {
            beginClose().toCompletableFuture().join();
            return;
        }
        if (null != jedisPool && !jedisPool.isClosed()) {
            jedisPool.destroy();
        }
    }

    /**
     * Begins an idempotent close sequence for atomic mode.
     *
     * @return shared close completion
     */
    private CompletionStage<Void> beginClose() {
        boolean schedule;
        CompletableFuture<Void> result;
        synchronized (lifecycle) {
            if (closeFuture == null) {
                closeFuture = new CompletableFuture<>();
            }
            result = closeFuture;
            schedule = inFlight == 0 && !closeScheduled;
            if (schedule) {
                closeScheduled = true;
            }
        }
        if (schedule) {
            scheduleClose();
        }
        return result;
    }

    /**
     * Submits one blocking Redis operation after atomic lifecycle admission.
     *
     * @param command blocking Redis command
     * @param <T>     result type
     * @return independently completable operation stage
     */
    private <T> CompletionStage<T> submit(Supplier<T> command) {
        CompletableFuture<T> result = new CompletableFuture<>();
        synchronized (lifecycle) {
            if (closeFuture != null) {
                return CompletableFuture.failedFuture(new IllegalStateException("Atomic cache is closed"));
            }
            inFlight++;
        }
        try {
            executor.execute(() -> {
                T value = null;
                Throwable failure = null;
                try {
                    value = command.get();
                } catch (Throwable caught) {
                    failure = caught;
                }
                finishOperation();
                if (failure == null) {
                    result.complete(value);
                } else {
                    result.completeExceptionally(failure);
                }
            });
        } catch (Throwable failure) {
            finishOperation();
            result.completeExceptionally(failure);
        }
        return result;
    }

    /**
     * Completes one admitted command and triggers deferred resource shutdown.
     */
    private void finishOperation() {
        boolean schedule;
        synchronized (lifecycle) {
            inFlight--;
            schedule = inFlight == 0 && closeFuture != null && !closeScheduled;
            if (schedule) {
                closeScheduled = true;
            }
        }
        if (schedule) {
            scheduleClose();
        }
    }

    /**
     * Submits Jedis pool shutdown without taking ownership of the caller's executor.
     */
    private void scheduleClose() {
        try {
            executor.execute(() -> {
                try {
                    jedisPool.close();
                    closeFuture.complete(null);
                } catch (Throwable failure) {
                    closeFuture.completeExceptionally(failure);
                }
            });
        } catch (Throwable failure) {
            closeFuture.completeExceptionally(failure);
        }
    }

    /**
     * Validates the positive TTL required by atomic create and replace.
     *
     * @param ttlMillis time to live in milliseconds
     */
    private static void requirePositiveTtl(long ttlMillis) {
        if (ttlMillis <= 0L) {
            throw new IllegalArgumentException("ttlMillis must be greater than zero");
        }
    }

    /**
     * Encodes one key using the framework UTF-8 constant.
     *
     * @param key cache key
     * @return UTF-8 key bytes
     */
    private static byte[] keyBytes(Object key) {
        return Objects.requireNonNull(key, "key").toString().getBytes(Charset.UTF_8);
    }

    /**
     * Copies mutable byte arrays while preserving other value types.
     *
     * @param value source value
     * @param <T>   value type
     * @return defensive byte-array copy or the original non-array value
     */
    private static <T> T copyValue(T value) {
        if (value instanceof byte[] bytes) {
            return (T) Arrays.copyOf(bytes, bytes.length);
        }
        return value;
    }

}
