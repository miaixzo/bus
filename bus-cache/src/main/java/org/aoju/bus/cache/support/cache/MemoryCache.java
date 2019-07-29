package org.aoju.bus.cache.support.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class MemoryCache implements Cache {

    private ConcurrentMap<String, Object> map = new ConcurrentHashMap<>();

    @Override
    public Object read(String key) {
        return map.get(key);
    }

    @Override
    public void write(String key, Object value, long expire) {
        map.put(key, value);
    }

    @Override
    public Map<String, Object> read(Collection<String> keys) {
        Map<String, Object> subCache = new HashMap<>(keys.size());
        for (String key : keys) {
            subCache.put(key, read(key));
        }

        return subCache;
    }

    @Override
    public void write(Map<String, Object> keyValueMap, long expire) {
        map.putAll(keyValueMap);
    }

    @Override
    public void remove(String... keys) {
        for (String key : keys) {
            map.remove(key);
        }
    }
}
