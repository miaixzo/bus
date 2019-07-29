package org.aoju.bus.cache.support.cache;

import org.aoju.bus.cache.entity.Expire;
import org.aoju.bus.cache.support.serialize.BaseSerializer;
import org.aoju.bus.cache.support.serialize.Hessian2Serializer;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.exception.MemcachedException;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class MemcachedCache implements Cache {

    private static final int _30_DAYS = 30 * 24 * 60 * 60;

    private MemcachedClient client;

    private BaseSerializer serializer;

    public MemcachedCache(String ipPorts) throws IOException {
        this(ipPorts, new Hessian2Serializer());
    }

    public MemcachedCache(String addressList, BaseSerializer serializer) throws IOException {
        client = new XMemcachedClientBuilder(addressList).build();
        this.serializer = serializer;
    }

    @Override
    public Object read(String key) {
        try {
            byte[] bytes = client.get(key);
            return serializer.deserialize(bytes);
        } catch (TimeoutException | InterruptedException | MemcachedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(String key, Object value, long expire) {
        byte[] byteValue = serializer.serialize(value);
        try {
            if (expire == Expire.FOREVER) {
                client.set(key, _30_DAYS, byteValue);
            } else {
                client.set(key, (int) (expire / 1000), byteValue);
            }
        } catch (TimeoutException | InterruptedException | MemcachedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> read(Collection<String> keys) {
        try {
            Map<String, byte[]> byteMap = client.get(keys);
            Map<String, Object> resultMap = new HashMap<>(byteMap.size());
            for (Map.Entry<String, byte[]> entry : byteMap.entrySet()) {
                String key = entry.getKey();
                Object value = serializer.deserialize(entry.getValue());

                resultMap.put(key, value);
            }

            return resultMap;
        } catch (TimeoutException | InterruptedException | MemcachedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(Map<String, Object> keyValueMap, long expire) {
        for (Map.Entry<String, Object> entry : keyValueMap.entrySet()) {
            this.write(entry.getKey(), entry.getValue(), expire);
        }
    }

    @Override
    public void remove(String... keys) {
        try {
            for (String key : keys) {
                client.delete(key);
            }
        } catch (TimeoutException | InterruptedException | MemcachedException e) {
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void tearDown() {
        if (client != null && !client.isShutdown()) {
            try {
                client.shutdown();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
