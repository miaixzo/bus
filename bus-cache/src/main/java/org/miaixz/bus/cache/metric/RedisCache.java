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
package org.miaixz.bus.cache.metric;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.cache.Builder;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cache.magic.CacheExpire;
import org.miaixz.bus.cache.Serializer;
import org.miaixz.bus.cache.serialize.Hessian2Serializer;
import org.miaixz.bus.core.lang.Symbol;

import jakarta.annotation.PreDestroy;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.params.ScanParams;
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
 * @since Java 21+
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
        this.jedisPool = jedisPool;
        this.serializer = serializer;
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
        try (Jedis client = jedisPool.getResource()) {
            client.flushDB();
        }
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
            return client.pexpire(key.toString(), expire) == 1L;
        }
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
        if (null != jedisPool && !jedisPool.isClosed()) {
            jedisPool.destroy();
        }
    }

}
