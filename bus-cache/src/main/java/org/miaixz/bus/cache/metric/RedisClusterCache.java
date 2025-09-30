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

import java.util.*;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cache.magic.CacheExpire;
import org.miaixz.bus.cache.support.serialize.BaseSerializer;
import org.miaixz.bus.cache.support.serialize.Hessian2Serializer;
import jakarta.annotation.PreDestroy;
import redis.clients.jedis.JedisCluster;

/**
 * Redis 集群缓存支持
 * <p>
 * 基于Jedis客户端实现的Redis集群缓存接口，提供分布式缓存功能。 支持序列化和反序列化操作，并提供批量读写操作。 使用@PreDestroy注解确保资源正确释放。
 * </p>
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class RedisClusterCache<K, V> implements CacheX<K, V> {

    /**
     * 序列化器
     */
    private BaseSerializer serializer;

    /**
     * Jedis集群客户端
     */
    private JedisCluster jedisCluster;

    /**
     * 构造方法
     * <p>
     * 使用指定的Jedis集群客户端和默认的Hessian2序列化器创建缓存实例
     * </p>
     *
     * @param jedisCluster Jedis集群客户端
     */
    public RedisClusterCache(JedisCluster jedisCluster) {
        this(jedisCluster, new Hessian2Serializer());
    }

    /**
     * 构造方法
     * <p>
     * 使用指定的Jedis集群客户端和序列化器创建缓存实例
     * </p>
     *
     * @param jedisCluster Jedis集群客户端
     * @param serializer   序列化器
     */
    public RedisClusterCache(JedisCluster jedisCluster, BaseSerializer serializer) {
        this.jedisCluster = jedisCluster;
        this.serializer = serializer;
    }

    /**
     * 将键值映射转换为字节数组
     *
     * @param keyValueMap 键值映射
     * @param serializer  序列化器
     * @return 字节数组
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
     * 将键集合转换为字节数组
     *
     * @param keys 键集合
     * @return 字节数组
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
     * 将字节数组列表转换为对象映射
     *
     * @param keys        键集合
     * @param bytesValues 字节数组列表
     * @param serializer  序列化器
     * @return 对象映射
     */
    static Map<String, Object> toObjectMap(
            Collection<String> keys,
            List<byte[]> bytesValues,
            BaseSerializer serializer) {
        int index = 0;
        Map<String, Object> result = new HashMap<>(keys.size());
        for (String key : keys) {
            Object value = serializer.deserialize(bytesValues.get(index++));
            result.put(key, value);
        }
        return result;
    }

    /**
     * 从缓存中读取单个值
     *
     * @param key 键
     * @return 值，如果不存在则返回null
     */
    @Override
    public V read(K key) {
        return serializer.deserialize(jedisCluster.get(((String) key).getBytes()));
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
        byte[] bytes = serializer.serialize(value);
        if (expire == CacheExpire.FOREVER) {
            jedisCluster.set(((String) key).getBytes(), bytes);
        } else {
            jedisCluster.setex(((String) key).getBytes(), (int) (expire / 1000), bytes);
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
        if (keys.isEmpty()) {
            return Collections.emptyMap();
        }
        List<byte[]> bytesValues = jedisCluster.mget(toByteArray((Collection<String>) keys));
        return (Map<K, V>) toObjectMap((Collection<String>) keys, bytesValues, this.serializer);
    }

    /**
     * 向缓存中批量写入键值对
     *
     * @param keyValueMap 键值映射
     * @param expire      过期时间（毫秒）
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
     * 从缓存中移除指定的键
     *
     * @param keys 要移除的键
     */
    @Override
    public void remove(K... keys) {
        if (keys.length == 0) {
            return;
        }
        jedisCluster.del(keys.toString());
    }

    /**
     * 清空缓存
     */
    @Override
    public void clear() {
        tearDown();
    }

    /**
     * 销毁方法
     * <p>
     * 使用@PreDestroy注解，在Bean销毁时关闭Jedis集群连接
     * </p>
     */
    @PreDestroy
    public void tearDown() {
        if (null != this.jedisCluster) {
            this.jedisCluster.close();
        }
    }

}
