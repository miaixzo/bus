/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.cache.support.cache;

import org.aoju.bus.cache.entity.CacheExpire;
import org.aoju.bus.cache.support.serialize.BaseSerializer;
import org.aoju.bus.cache.support.serialize.Hessian2Serializer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

import javax.annotation.PreDestroy;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Redis 单机缓存支持
 *
 * @author Kimi Liu
 * @version 5.0.8
 * @since JDK 1.8+
 */
public class RedisCache implements Cache {

    private BaseSerializer serializer;

    private JedisPool jedisPool;

    public RedisCache(JedisPool jedisPool) {
        this(jedisPool, new Hessian2Serializer());
    }

    public RedisCache(JedisPool jedisPool, BaseSerializer serializer) {
        this.jedisPool = jedisPool;
        this.serializer = serializer;
    }

    static byte[][] toByteArray(Map<String, Object> keyValueMap, BaseSerializer serializer) {
        byte[][] kvs = new byte[keyValueMap.size() * 2][];
        int index = 0;
        for (Map.Entry<String, Object> entry : keyValueMap.entrySet()) {
            kvs[index++] = entry.getKey().getBytes();
            kvs[index++] = serializer.serialize(entry.getValue());
        }
        return kvs;
    }

    static byte[][] toByteArray(Collection<String> keys) {
        byte[][] array = new byte[keys.size()][];
        int index = 0;
        for (String str : keys) {
            array[index++] = str.getBytes();
        }
        return array;
    }

    static Map<String, Object> toObjectMap(Collection<String> keys, List<byte[]> bytesValues, BaseSerializer serializer) {
        int index = 0;
        Map<String, Object> result = new HashMap<>(keys.size());
        for (String key : keys) {
            Object value = serializer.deserialize(bytesValues.get(index++));
            result.put(key, value);
        }
        return result;
    }

    @Override
    public Object read(String key) {
        try (Jedis client = jedisPool.getResource()) {
            byte[] bytes = client.get(key.getBytes());
            return serializer.deserialize(bytes);
        }
    }

    @Override
    public void write(String key, Object value, long expire) {
        try (Jedis client = jedisPool.getResource()) {
            byte[] bytesValue = serializer.serialize(value);
            if (expire == CacheExpire.FOREVER) {
                client.set(key.getBytes(), bytesValue);
            } else {
                client.psetex(key.getBytes(), expire, bytesValue);
            }
        }
    }

    @Override
    public Map<String, Object> read(Collection<String> keys) {
        try (Jedis client = jedisPool.getResource()) {
            List<byte[]> bytesValues = client.mget(toByteArray(keys));
            return toObjectMap(keys, bytesValues, this.serializer);
        }
    }

    @Override
    public void write(Map<String, Object> keyValueMap, long expire) {
        try (Jedis client = jedisPool.getResource()) {
            byte[][] kvs = toByteArray(keyValueMap, serializer);
            if (expire == CacheExpire.FOREVER) {
                client.mset(kvs);
            } else {
                Pipeline pipeline = client.pipelined();
                for (int i = 0; i < kvs.length; i += 2) {
                    pipeline.psetex(kvs[i], expire, kvs[i + 1]);
                }
                pipeline.sync();
            }
        }
    }

    @Override
    public void remove(String... keys) {
        try (Jedis client = jedisPool.getResource()) {
            client.del(keys);
        }
    }

    @Override
    public void clear() {
        tearDown();
    }

    @PreDestroy
    public void tearDown() {
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.destroy();
        }
    }

}
