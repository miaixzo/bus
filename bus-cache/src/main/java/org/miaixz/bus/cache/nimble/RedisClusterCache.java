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

import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.params.SetParams;
import redis.clients.jedis.resps.ScanResult;

/**
 * A Redis Cluster implementation of {@link CacheX} using the Jedis client.
 * <p>
 * This class provides a distributed caching solution for a Redis Cluster environment. It handles serialization and
 * deserialization of cache values. The lifecycle of the {@link JedisCluster} connection is managed via the
 * {@link PreDestroy} annotation.
 * </p>
 *
 * @param <K> The type of keys, which are assumed to be convertible to {@link String}.
 * @param <V> The type of values, which must be serializable.
 * @author Kimi Liu
 */
public class RedisClusterCache<K, V> implements CacheX<K, V>, AutoCloseable {

    /**
     * The serializer used for converting values to and from byte arrays.
     */
    private final Serializer serializer;

    /**
     * The Jedis Cluster client instance.
     */
    private final JedisCluster jedisCluster;

    /**
     * Optional caller-owned executor that enables atomic asynchronous operations.
     */
    private final Executor executor;

    /**
     * Number of admitted asynchronous commands that have not completed.
     */
    private int inFlight;

    /**
     * Whether cluster shutdown has been submitted to the executor.
     */
    private boolean closeScheduled;

    /**
     * Shared asynchronous close completion, or {@code null} before close begins.
     */
    private CompletableFuture<Void> closeFuture;

    /**
     * Monitor guarding asynchronous admission and cluster shutdown.
     */
    private final Object lifecycle = new Object();

    /**
     * Constructs a {@code RedisClusterCache} with a given Jedis cluster client and a default
     * {@link Hessian2Serializer}.
     *
     * @param jedisCluster The configured {@link JedisCluster}.
     */
    public RedisClusterCache(JedisCluster jedisCluster) {
        this(jedisCluster, new Hessian2Serializer());
    }

    /**
     * Constructs a {@code RedisClusterCache} with a given Jedis cluster client and a custom serializer.
     *
     * @param jedisCluster The configured {@link JedisCluster}.
     * @param serializer   The {@link Serializer} to use for value serialization.
     */
    public RedisClusterCache(JedisCluster jedisCluster, Serializer serializer) {
        this(jedisCluster, serializer, null);
    }

    /**
     * Constructs a Redis Cluster cache with atomic commands executed by a caller-owned executor.
     *
     * @param jedisCluster configured cluster client owned by this cache
     * @param serializer   serializer used for values
     * @param executor     caller-owned executor for blocking cluster commands
     */
    public RedisClusterCache(JedisCluster jedisCluster, Serializer serializer, Executor executor) {
        this.jedisCluster = jedisCluster;
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
        byte[] bytes = jedisCluster.get(key.toString().getBytes());
        return serializer.deserialize(bytes);
    }

    /**
     * Writes a single key-value pair to the cache with a specified expiration.
     * <p>
     * Uses {@code PSETEX} (millisecond precision) for consistency with {@link RedisCache}. When {@code expire} is
     * {@link CacheExpire#FOREVER} a plain {@code SET} without expiry is issued instead.
     * </p>
     *
     * @param key    The key to write. It is converted to a string.
     * @param value  The value to be serialized and stored.
     * @param expire The expiration time in milliseconds. If {@link CacheExpire#FOREVER}, the key will not expire.
     */
    @Override
    public void write(K key, V value, long expire) {
        byte[] bytes = serializer.serialize(value);
        if (expire == CacheExpire.FOREVER) {
            jedisCluster.set(key.toString().getBytes(), bytes);
        } else {
            jedisCluster.psetex(key.toString().getBytes(), expire, bytes);
        }
    }

    /**
     * Reads multiple values from the cache in a batch.
     * <p>
     * Note: `MGET` is not natively supported in Redis Cluster for keys that hash to different slots. This
     * implementation may fail or behave unexpectedly if the keys are not in the same slot.
     * </p>
     *
     * @param keys A collection of keys to retrieve.
     * @return A map of keys to their deserialized values for all keys found in the cache.
     */
    @Override
    public Map<K, V> read(Collection<K> keys) {
        if (keys.isEmpty()) {
            return Collections.emptyMap();
        }
        List<byte[]> bytesValues = jedisCluster.mget(Builder.toByteArray((Collection<String>) keys));
        return (Map<K, V>) Builder.toObjectMap((Collection<String>) keys, bytesValues, this.serializer);
    }

    /**
     * Writes multiple key-value pairs to the cache in a batch.
     * <p>
     * If `expire` is {@link CacheExpire#FOREVER}, this method attempts to use `MSET`, which is not supported across
     * different slots in Redis Cluster and will fail. Otherwise, it iterates and performs a `SETEX` for each key
     * individually.
     * </p>
     *
     * @param keyValueMap A map of key-value pairs to store.
     * @param expire      The expiration time in milliseconds.
     */
    @Override
    public void write(Map<K, V> keyValueMap, long expire) {
        if (keyValueMap.isEmpty()) {
            return;
        }
        if (expire == CacheExpire.FOREVER) {
            jedisCluster.mset(Builder.toByteArray((Map<String, Object>) keyValueMap, this.serializer));
        } else {
            for (Map.Entry<K, V> entry : keyValueMap.entrySet()) {
                write(entry.getKey(), entry.getValue(), expire);
            }
        }
    }

    /**
     * Removes entries from the cache.
     * <p>
     * Redis Cluster does not support multi-key {@code DEL} across different hash slots, so keys are deleted
     * individually.
     * </p>
     *
     * @param keys The keys to remove.
     */
    @Override
    public void remove(K... keys) {
        if (keys.length == 0) {
            return;
        }
        for (K key : keys) {
            jedisCluster.del(key.toString());
        }
    }

    /**
     * Flushes all data from the cluster.
     * <p>
     * <strong>Warning:</strong> This clears every key across all cluster nodes. Use {@code scan() + remove()} if only a
     * specific key prefix should be removed. To shut down the cluster connection use {@link #close()}.
     * </p>
     */
    @Override
    public void clear() {
        Logger.info(true, "Cache", "Redis cluster cache clear started");
        jedisCluster.flushDB();
        Logger.info(false, "Cache", "Redis cluster cache clear completed");
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
        return jedisCluster.incr(key.toString());
    }

    /**
     * Scans and returns all key-value pairs whose keys start with the given prefix.
     * <p>
     * Uses {@code JedisCluster.scan()} which in cluster mode advances the cursor within the shard selected by the
     * initial cursor. A full cross-node scan is not possible through the standard {@code JedisCluster} API in Jedis 6;
     * this implementation performs a best-effort cursor loop that covers the shard reachable from cursor {@code "0"}.
     * For deployments where complete coverage across all shards is required, consider routing requests through a
     * namespace hash tag (e.g. {@code {ns}:key}) so all keys land on the same slot.
     * </p>
     *
     * @param prefix the key prefix to match
     * @return a map of all matching key-value pairs reachable from the initial cursor
     */
    @Override
    public Map<K, V> scan(K prefix) {
        Map<K, V> result = new LinkedHashMap<>();
        String pattern = prefix.toString() + Symbol.STAR;
        Logger.debug(true, "Cache", "Redis cluster cache scan started: prefixPresent={}", prefix != null);
        String cursor = Symbol.ZERO;
        ScanParams params = new ScanParams().match(pattern).count(200);
        do {
            ScanResult<String> batch = jedisCluster.scan(cursor, params);
            cursor = batch.getCursor();
            for (String k : batch.getResult()) {
                byte[] val = jedisCluster.get(k.getBytes());
                if (val != null) {
                    result.put((K) k, serializer.deserialize(val));
                }
            }
        } while (!Symbol.ZERO.equals(cursor));
        Logger.debug(
                false,
                "Cache",
                "Redis cluster cache scan completed: prefixPresent={}, resultCount={}",
                prefix != null,
                result.size());
        return result;
    }

    /**
     * Refreshes the TTL of an existing entry using the Redis {@code PEXPIRE} command.
     *
     * @param key    the key whose TTL to refresh
     * @param expire the new expiration time in milliseconds
     * @return {@code true} if the TTL was set; {@code false} if the key does not exist
     */
    @Override
    public boolean renew(K key, long expire) {
        boolean renewed = jedisCluster.pexpire(key.toString(), expire) == 1L;
        Logger.debug(
                false,
                "Cache",
                "Redis cluster cache ttl renewed: keyPresent={}, expireMs={}, renewed={}",
                key != null,
                expire,
                renewed);
        return renewed;
    }

    /**
     * Atomically creates one cluster entry with NX and PX semantics.
     * <p>
     * This command addresses exactly one key. A Redis hash tag is therefore optional and remains a caller-controlled
     * routing choice; the cache does not force unrelated keys into one cluster slot.
     * </p>
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
        return submit(() -> jedisCluster.set(keyBytes, valueBytes, SetParams.setParams().nx().px(ttlMillis)) != null);
    }

    /**
     * Reports whether a caller-owned executor was supplied for the complete atomic command set.
     *
     * @return {@code true} when asynchronous atomic operations are configured
     */
    @Override
    public boolean atomic() {
        return executor != null;
    }

    /**
     * Reads and deserializes one cluster entry at its owning slot.
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
            byte[] value = jedisCluster.get(keyBytes);
            return value == null ? null : copyValue(serializer.deserialize(value));
        });
    }

    /**
     * Atomically reads, removes, and deserializes one cluster entry.
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
            byte[] value = jedisCluster.getDel(keyBytes);
            return value == null ? null : copyValue(serializer.deserialize(value));
        });
    }

    /**
     * Atomically replaces one cluster entry whose serialized value equals the expected value.
     * <p>
     * The compare-and-replace script uses only {@code KEYS[1]}, so Redis Cluster can route it without requiring a
     * shared hash tag.
     * </p>
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
            byte[] script = ("if redis.call('GET', KEYS[1]) == ARGV[1] then "
                    + "redis.call('SET', KEYS[1], ARGV[2], 'PX', ARGV[3]); return 1 else return 0 end")
                            .getBytes(Charset.UTF_8);
            return Long.valueOf(1L).equals(
                    jedisCluster.eval(script, List.of(keyBytes), List.of(expectedBytes, updateBytes, ttlBytes)));
        });
    }

    /**
     * Atomically deletes one cluster entry.
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
        return submit(() -> jedisCluster.del(keyBytes) == 1L);
    }

    /**
     * Closes the {@link JedisCluster} connection, releasing all resources.
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
        if (null != this.jedisCluster) {
            this.jedisCluster.close();
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
     * Submits one blocking cluster operation after lifecycle admission.
     *
     * @param command blocking cluster command
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
     * Completes one admitted command and triggers deferred cluster shutdown.
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
     * Submits cluster shutdown without taking ownership of the caller's executor.
     */
    private void scheduleClose() {
        try {
            executor.execute(() -> {
                try {
                    jedisCluster.close();
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
     * Encodes one non-null cluster key without imposing a hash-tag layout.
     *
     * @param key cluster cache key
     * @return UTF-8 key bytes
     */
    private static byte[] keyBytes(Object key) {
        return Objects.requireNonNull(key, "key").toString().getBytes(Charset.UTF_8);
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
