package org.aoju.bus.cache.support.cache;

import org.aoju.bus.cache.entity.Expire;
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
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
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
        try (Jedis client = jedisPool.getResource()) {
            byte[] bytes = client.get(key.getBytes());
            return serializer.deserialize(bytes);
        }
    }

    @Override
    public void write(String key, Object value, long expire) {
        try (Jedis client = jedisPool.getResource()) {
            byte[] bytesValue = serializer.serialize(value);
            if (expire == Expire.FOREVER) {
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
            if (expire == Expire.FOREVER) {
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

    @PreDestroy
    public void tearDown() {
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.destroy();
        }
    }

}