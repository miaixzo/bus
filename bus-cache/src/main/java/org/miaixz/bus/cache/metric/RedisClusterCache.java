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

import java.util.*;

import org.miaixz.bus.cache.Builder;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cache.magic.CacheExpire;
import org.miaixz.bus.cache.Serializer;
import org.miaixz.bus.cache.serialize.Hessian2Serializer;
import org.miaixz.bus.core.lang.Symbol;

import jakarta.annotation.PreDestroy;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.params.ScanParams;
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
 * @since Java 21+
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
        this.jedisCluster = jedisCluster;
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
        jedisCluster.flushDB();
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
        return jedisCluster.pexpire(key.toString(), expire) == 1L;
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
        if (null != this.jedisCluster) {
            this.jedisCluster.close();
        }
    }

}
