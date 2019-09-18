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

import org.aoju.bus.cache.entity.Expire;
import org.aoju.bus.cache.support.serialize.BaseSerializer;
import org.aoju.bus.cache.support.serialize.Hessian2Serializer;
import redis.clients.jedis.JedisCluster;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.*;

/**
 * @author Kimi Liu
 * @version 3.5.0
 * @since JDK 1.8
 */
public class RedisClusterCache implements Cache {

    private BaseSerializer serializer;

    private JedisCluster jedisCluster;

    public RedisClusterCache(JedisCluster jedisCluster) {
        this(jedisCluster, new Hessian2Serializer());
    }

    public RedisClusterCache(JedisCluster jedisCluster, BaseSerializer serializer) {
        this.jedisCluster = jedisCluster;
        this.serializer = serializer;
    }

    /* For Write */
    static byte[][] toByteArray(Map<String, Object> keyValueMap, BaseSerializer serializer) {
        byte[][] kvs = new byte[keyValueMap.size() * 2][];
        int index = 0;
        for (Map.Entry<String, Object> entry : keyValueMap.entrySet()) {
            kvs[index++] = entry.getKey().getBytes();
            kvs[index++] = serializer.serialize(entry.getValue());
        }
        return kvs;
    }

    /* For Read */
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
        return serializer.deserialize(jedisCluster.get(key.getBytes()));
    }

    @Override
    public void write(String key, Object value, long expire) {
        byte[] bytes = serializer.serialize(value);
        if (expire == Expire.FOREVER) {
            jedisCluster.set(key.getBytes(), bytes);
        } else {
            jedisCluster.setex(key.getBytes(), (int) (expire / 1000), bytes);
        }
    }

    @Override
    public Map<String, Object> read(Collection<String> keys) {
        if (keys.isEmpty()) {
            return Collections.emptyMap();
        }

        List<byte[]> bytesValues = jedisCluster.mget(toByteArray(keys));
        return toObjectMap(keys, bytesValues, this.serializer);
    }

    @Override
    public void write(Map<String, Object> keyValueMap, long expire) {
        if (keyValueMap.isEmpty()) {
            return;
        }

        if (expire == Expire.FOREVER) {
            jedisCluster.mset(toByteArray(keyValueMap, this.serializer));
        } else {
            for (Map.Entry<String, Object> entry : keyValueMap.entrySet()) {
                write(entry.getKey(), entry.getValue(), expire);
            }
        }
    }

    @Override
    public void remove(String... keys) {
        if (keys.length == 0) {
            return;
        }

        jedisCluster.del(keys);
    }

    @PreDestroy
    public void tearDown() throws IOException {
        if (this.jedisCluster != null) {
            this.jedisCluster.close();
        }
    }

}
