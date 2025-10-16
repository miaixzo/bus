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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cache.magic.CacheExpire;
import org.miaixz.bus.cache.support.serialize.BaseSerializer;
import org.miaixz.bus.cache.support.serialize.Hessian2Serializer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

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
 * @since Java 17+
 */
public class RedisCache<K, V> implements CacheX<K, V> {

    /**
     * The serializer used for converting values to and from byte arrays.
     */
    private final BaseSerializer serializer;

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
     * @param serializer The {@link BaseSerializer} to use for value serialization.
     */
    public RedisCache(JedisPool jedisPool, BaseSerializer serializer) {
        this.jedisPool = jedisPool;
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
        try (Jedis client = jedisPool.getResource()) {
            byte[] bytes = client.get(key.toString().getBytes());
            return serializer.deserialize(bytes);
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
            List<byte[]> bytesValues = client.mget(toByteArray((Collection<String>) keys));
            return (Map<K, V>) toObjectMap((Collection<String>) keys, bytesValues, this.serializer);
        }
    }

    /**
     * Writes multiple key-value pairs to the cache in a batch.
     *
     * @param keyValueMap A map of key-value pairs to store.
     * @param expire      The expiration time in milliseconds. If {@link CacheExpire#FOREVER}, `MSET` is used.
     *                    Otherwise, a pipeline of `PSETEX` commands is used.
     */
    public void write(Map<K, V> keyValueMap, long expire) {
        try (Jedis client = jedisPool.getResource()) {
            byte[][] kvs = toByteArray((Map<String, Object>) keyValueMap, serializer);
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
        try (Jedis client = jedisPool.getResource()) {
            client.del(keys.toString());
        }
    }

    /**
     * Destroys the underlying connection pool.
     * <p>
     * <strong>Warning:</strong> This is a destructive operation that permanently closes the {@link JedisPool},
     * rendering this cache instance unusable. It does not clear the data from Redis, but rather shuts down the
     * connection manager.
     * </p>
     */
    @Override
    public void clear() {
        tearDown();
    }

    /**
     * A lifecycle method to destroy the {@link JedisPool}.
     * <p>
     * Annotated with {@link PreDestroy}, this method is typically invoked by a dependency injection container when the
     * bean is being destroyed.
     * </p>
     */
    @PreDestroy
    public void tearDown() {
        if (null != jedisPool && !jedisPool.isClosed()) {
            jedisPool.destroy();
        }
    }

}
