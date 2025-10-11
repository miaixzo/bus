/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.cache.metric;

import jakarta.annotation.PreDestroy;
import java.util.*;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cache.magic.CacheExpire;
import org.miaixz.bus.cache.support.serialize.BaseSerializer;
import org.miaixz.bus.cache.support.serialize.Hessian2Serializer;
import redis.clients.jedis.JedisCluster;

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
 * @since Java 17+
 */
public class RedisClusterCache<K, V> implements CacheX<K, V> {

    /**
     * The serializer used for converting values to and from byte arrays.
     */
    private final BaseSerializer serializer;

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
     * @param serializer   The {@link BaseSerializer} to use for value serialization.
     */
    public RedisClusterCache(JedisCluster jedisCluster, BaseSerializer serializer) {
        this.jedisCluster = jedisCluster;
        this.serializer = serializer;
    }

    /**
     * Converts a map of string keys and object values into a flat byte array for Redis `MSET`.
     *
     * @param keyValueMap The map to convert.
     * @param serializer  The serializer to use for values.
     * @return A byte array of interleaved keys and serialized values.
     */
    static byte[][] toByteArray(Map<String, Object> keyValueMap, BaseSerializer serializer) {
        byte[][] kvs = new byte[keyValueMap.size() * 2][];
        int index = 0;
        for (Map.Entry<String, Object> entry : keyValueMap.entrySet()) {
            kvs[index++] = entry.getKey().getBytes();
            kvs[index++] = serializer.serialize(entry.getValue());
        }
        return kvs;
    }

    /**
     * Converts a collection of string keys into a 2D byte array for Redis commands.
     *
     * @param keys The collection of keys.
     * @return A 2D byte array where each inner array is the byte representation of a key.
     */
    static byte[][] toByteArray(Collection<String> keys) {
        byte[][] array = new byte[keys.size()][];
        int index = 0;
        for (String text : keys) {
            array[index++] = text.getBytes();
        }
        return array;
    }

    /**
     * Converts a list of byte array values back into a map of string keys to objects.
     *
     * @param keys        The original collection of keys, used for mapping.
     * @param bytesValues The list of serialized values returned from Redis.
     * @param serializer  The serializer to use for deserialization.
     * @return A map of keys to their deserialized object values.
     */
    static Map<String, Object> toObjectMap(
            Collection<String> keys,
            List<byte[]> bytesValues,
            BaseSerializer serializer) {
        int index = 0;
        Map<String, Object> result = new HashMap<>(keys.size());
        for (String key : keys) {
            byte[] valueBytes = bytesValues.get(index++);
            if (valueBytes != null) {
                Object value = serializer.deserialize(valueBytes);
                result.put(key, value);
            }
        }
        return result;
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
     * The expiration time is converted from milliseconds to seconds.
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
            jedisCluster.setex(key.toString().getBytes(), (int) (expire / 1000), bytes);
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
        List<byte[]> bytesValues = jedisCluster.mget(toByteArray((Collection<String>) keys));
        return (Map<K, V>) toObjectMap((Collection<String>) keys, bytesValues, this.serializer);
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
            jedisCluster.mset(toByteArray((Map<String, Object>) keyValueMap, this.serializer));
        } else {
            for (Map.Entry<K, V> entry : keyValueMap.entrySet()) {
                write(entry.getKey(), entry.getValue(), expire);
            }
        }
    }

    /**
     * Removes entries from the cache.
     * <p>
     * <strong>Note:</strong> This implementation has a flaw. It attempts to delete a single key that is the string
     * representation of the entire key array (e.g., "[Ljava.lang.String;@12345") instead of deleting each key within
     * the array.
     * </p>
     *
     * @param keys The keys to remove.
     */
    @Override
    public void remove(K... keys) {
        if (keys.length == 0) {
            return;
        }
        jedisCluster.del(keys.toString());
    }

    /**
     * Closes the underlying Jedis cluster connection.
     * <p>
     * <strong>Warning:</strong> This is a destructive operation that permanently closes the {@link JedisCluster}
     * connection, rendering this cache instance unusable. It does not clear the data from Redis, but rather shuts down
     * the connection.
     * </p>
     */
    @Override
    public void clear() {
        tearDown();
    }

    /**
     * A lifecycle method to close the {@link JedisCluster} connection.
     * <p>
     * Annotated with {@link PreDestroy}, this method is typically invoked by a dependency injection container when the
     * bean is being destroyed.
     * </p>
     */
    @PreDestroy
    public void tearDown() {
        if (null != this.jedisCluster) {
            this.jedisCluster.close();
        }
    }

}
