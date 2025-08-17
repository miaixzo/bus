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

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cache.magic.CacheExpire;
import org.miaixz.bus.cache.support.serialize.BaseSerializer;
import org.miaixz.bus.cache.support.serialize.Hessian2Serializer;
import jakarta.annotation.PreDestroy;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.exception.MemcachedException;

/**
 * Memcached 缓存支持
 * <p>
 * 基于XMemcached客户端实现的Memcached缓存接口，提供分布式缓存功能。 支持序列化和反序列化操作，并提供批量读写操作。 使用@PreDestroy注解确保资源正确释放。
 * </p>
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class MemcachedCache<K, V> implements CacheX<K, V> {

    /**
     * 30天的秒数，用于设置最大过期时间
     */
    private static final int _30_DAYS = 30 * 24 * 60 * 60;

    /**
     * Memcached客户端实例
     */
    private MemcachedClient client;

    /**
     * 序列化器
     */
    private BaseSerializer serializer;

    /**
     * 构造方法
     * <p>
     * 使用指定的IP端口和默认的Hessian2序列化器创建缓存实例
     * </p>
     *
     * @param ipPorts Memcached服务器的IP和端口，格式为"host1:port1,host2:port2"
     * @throws IOException 如果连接Memcached服务器失败
     */
    public MemcachedCache(String ipPorts) throws IOException {
        this(ipPorts, new Hessian2Serializer());
    }

    /**
     * 构造方法
     * <p>
     * 使用指定的IP端口和序列化器创建缓存实例
     * </p>
     *
     * @param addressList Memcached服务器的地址列表，格式为"host1:port1,host2:port2"
     * @param serializer  序列化器
     * @throws IOException 如果连接Memcached服务器失败
     */
    public MemcachedCache(String addressList, BaseSerializer serializer) throws IOException {
        this.client = new XMemcachedClientBuilder(addressList).build();
        this.serializer = serializer;
    }

    /**
     * 从缓存中读取单个值
     *
     * @param key 键
     * @return 值，如果不存在则返回null
     */
    @Override
    public V read(K key) {
        try {
            byte[] bytes = client.get((String) key);
            return this.serializer.deserialize(bytes);
        } catch (TimeoutException | InterruptedException | MemcachedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 向缓存中写入单个键值对
     *
     * @param key    键
     * @param value  值
     * @param expire 过期时间（毫秒）
     */
    @Override
    public void write(K key, V value, long expire) {
        byte[] byteValue = this.serializer.serialize(value);
        try {
            if (expire == CacheExpire.FOREVER) {
                this.client.set((String) key, _30_DAYS, byteValue);
            } else {
                this.client.set((String) key, (int) (expire / 1000), byteValue);
            }
        } catch (TimeoutException | InterruptedException | MemcachedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从缓存中批量读取值
     *
     * @param keys 键集合
     * @return 键值映射
     */
    @Override
    public Map<K, V> read(Collection<K> keys) {
        try {
            Map<String, byte[]> byteMap = client.get((Collection<String>) keys);
            Map<K, V> resultMap = new HashMap<>(byteMap.size());
            for (Map.Entry<String, byte[]> entry : byteMap.entrySet()) {
                String key = entry.getKey();
                Object value = serializer.deserialize(entry.getValue());
                resultMap.put((K) key, (V) value);
            }
            return resultMap;
        } catch (TimeoutException | InterruptedException | MemcachedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 向缓存中批量写入键值对
     *
     * @param keyValueMap 键值映射
     * @param expire      过期时间（毫秒）
     */
    @Override
    public void write(Map<K, V> keyValueMap, long expire) {
        for (Map.Entry<K, V> entry : keyValueMap.entrySet()) {
            this.write(entry.getKey(), entry.getValue(), expire);
        }
    }

    /**
     * 从缓存中移除指定的键
     *
     * @param keys 要移除的键
     */
    @Override
    public void remove(K... keys) {
        try {
            for (K key : keys) {
                this.client.delete((String) key);
            }
        } catch (TimeoutException | InterruptedException | MemcachedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 清空缓存
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
     * 销毁方法
     * <p>
     * 使用@PreDestroy注解，在Bean销毁时关闭Memcached客户端连接
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