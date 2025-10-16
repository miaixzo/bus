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
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.exception.MemcachedException;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cache.magic.CacheExpire;
import org.miaixz.bus.cache.support.serialize.BaseSerializer;
import org.miaixz.bus.cache.support.serialize.Hessian2Serializer;

/**
 * A Memcached-based implementation of {@link CacheX} using the XMemcached client.
 * <p>
 * This class provides a distributed caching solution. It handles the serialization and deserialization of cache values.
 * The lifecycle of the {@link MemcachedClient} is managed via the {@link PreDestroy} annotation, ensuring resources are
 * properly released.
 * </p>
 *
 * @param <K> The type of keys, which are assumed to be convertible to {@link String}.
 * @param <V> The type of values, which must be serializable.
 * @author Kimi Liu
 * @since Java 17+
 */
public class MemcachedCache<K, V> implements CacheX<K, V> {

    /**
     * The maximum expiration time supported by Memcached, in seconds (30 days). Memcached does not support a true
     * "forever" expiration.
     */
    private static final int _30_DAYS = 30 * 24 * 60 * 60;

    /**
     * The XMemcached client instance.
     */
    private final MemcachedClient client;

    /**
     * The serializer used for converting values to and from byte arrays.
     */
    private final BaseSerializer serializer;

    /**
     * Constructs a {@code MemcachedCache} with specified server addresses and a default {@link Hessian2Serializer}.
     *
     * @param ipPorts A comma-separated string of server addresses (e.g., "host1:port1,host2:port2").
     * @throws IOException if the client cannot be initialized.
     */
    public MemcachedCache(String ipPorts) throws IOException {
        this(ipPorts, new Hessian2Serializer());
    }

    /**
     * Constructs a {@code MemcachedCache} with specified server addresses and a custom serializer.
     *
     * @param addressList A comma-separated string of server addresses.
     * @param serializer  The {@link BaseSerializer} to use for value serialization.
     * @throws IOException if the client cannot be initialized.
     */
    public MemcachedCache(String addressList, BaseSerializer serializer) throws IOException {
        this.client = new XMemcachedClientBuilder(addressList).build();
        this.serializer = serializer;
    }

    /**
     * Reads a single value from the cache.
     *
     * @param key The key whose value to retrieve. It is converted to a string.
     * @return The deserialized value, or {@code null} if the key does not exist.
     * @throws RuntimeException if a Memcached operation fails.
     */
    @Override
    public V read(K key) {
        try {
            byte[] bytes = client.get(key.toString());
            return this.serializer.deserialize(bytes);
        } catch (TimeoutException | InterruptedException | MemcachedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Writes a single key-value pair to the cache with a specified expiration.
     * <p>
     * The expiration time is converted from milliseconds to seconds. If {@link CacheExpire#FOREVER} is provided, the
     * expiration is set to 30 days, the maximum allowed by Memcached.
     * </p>
     *
     * @param key    The key to write. It is converted to a string.
     * @param value  The value to be serialized and stored.
     * @param expire The expiration time in milliseconds.
     * @throws RuntimeException if a Memcached operation fails.
     */
    @Override
    public void write(K key, V value, long expire) {
        byte[] byteValue = this.serializer.serialize(value);
        try {
            int expiryInSeconds = (expire == CacheExpire.FOREVER) ? _30_DAYS : (int) (expire / 1000);
            this.client.set(key.toString(), expiryInSeconds, byteValue);
        } catch (TimeoutException | InterruptedException | MemcachedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads multiple values from the cache in a batch.
     *
     * @param keys A collection of keys to retrieve.
     * @return A map of keys to their deserialized values for all keys found in the cache.
     * @throws RuntimeException if a Memcached operation fails.
     */
    @Override
    public Map<K, V> read(Collection<K> keys) {
        try {
            Map<String, byte[]> byteMap = client.get((Collection<String>) keys);
            Map<K, V> resultMap = new HashMap<>(byteMap.size());
            for (Map.Entry<String, byte[]> entry : byteMap.entrySet()) {
                String key = entry.getKey();
                V value = serializer.deserialize(entry.getValue());
                resultMap.put((K) key, value);
            }
            return resultMap;
        } catch (TimeoutException | InterruptedException | MemcachedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Writes multiple key-value pairs to the cache.
     * <p>
     * This is implemented by iterating over the map and calling the single `write` method for each entry.
     * </p>
     *
     * @param keyValueMap A map of key-value pairs to store.
     * @param expire      The expiration time in milliseconds, applied to each entry.
     */
    @Override
    public void write(Map<K, V> keyValueMap, long expire) {
        for (Map.Entry<K, V> entry : keyValueMap.entrySet()) {
            this.write(entry.getKey(), entry.getValue(), expire);
        }
    }

    /**
     * Removes one or more entries from the cache.
     *
     * @param keys The keys of the entries to remove.
     * @throws RuntimeException if a Memcached operation fails.
     */
    @Override
    public void remove(K... keys) {
        try {
            for (K key : keys) {
                this.client.delete(key.toString());
            }
        } catch (TimeoutException | InterruptedException | MemcachedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Flushes all data from all servers in the Memcached cluster.
     * <p>
     * <strong>Warning:</strong> This is a destructive operation that will wipe all data from the connected servers.
     * </p>
     *
     * @throws RuntimeException if a Memcached operation fails.
     */
    @Override
    public void clear() {
        try {
            this.client.flushAll();
        } catch (TimeoutException | InterruptedException | MemcachedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * A lifecycle method to shut down the {@link MemcachedClient}.
     * <p>
     * Annotated with {@link PreDestroy}, this method is typically invoked by a dependency injection container when the
     * bean is being destroyed.
     * </p>
     */
    @PreDestroy
    public void tearDown() {
        if (null != this.client && !this.client.isShutdown()) {
            try {
                this.client.shutdown();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
