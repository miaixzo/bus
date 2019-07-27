package org.aoju.bus.cache;

import org.aoju.bus.cache.entity.CacheKeys;
import org.aoju.bus.cache.entity.Pair;
import org.aoju.bus.cache.support.cache.Cache;
import org.aoju.bus.core.lang.exception.CommonException;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.logger.Logger;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Singleton
public class CacheManager {

    // defaultCache和cachePool直接使用Pair实现, 减小new Object的损耗
    private Pair<String, Cache> defaultCache;

    private Map<String, Pair<String, Cache>> cachePool = new ConcurrentHashMap<>();

    @Inject
    public void setCachePool(Map<String, Cache> caches) {
        // default cache impl
        Map.Entry<String, Cache> entry = caches.entrySet().iterator().next();
        this.defaultCache = Pair.of(entry.getKey(), entry.getValue());

        caches.forEach((name, cache) -> this.cachePool.put(name, Pair.of(name, cache)));
    }

    public Object readSingle(String cache, String key) {
        try {
            Pair<String, Cache> cacheImpl = getCacheImpl(cache);

            long start = System.currentTimeMillis();
            Object result = cacheImpl.getRight().read(key);
            Logger.info("cache [{}] read single cost: [{}] ms",
                    cacheImpl.getLeft(),
                    (System.currentTimeMillis() - start));

            return result;
        } catch (Throwable e) {
            Logger.error("read single cache failed, key: {} ", key, e);
            return null;
        }
    }

    public void writeSingle(String cache, String key, Object value, int expire) {
        if (value != null) {
            try {
                Pair<String, Cache> cacheImpl = getCacheImpl(cache);

                long start = System.currentTimeMillis();
                cacheImpl.getRight().write(key, value, expire);
                Logger.info("cache [{}] write single cost: [{}] ms",
                        cacheImpl.getLeft(),
                        (System.currentTimeMillis() - start));

            } catch (Throwable e) {
                Logger.error("write single cache failed, key: {} ", key, e);
            }
        }
    }

    public CacheKeys readBatch(String cache, Collection<String> keys) {
        CacheKeys cacheKeys;
        if (keys.isEmpty()) {
            cacheKeys = new CacheKeys();
        } else {
            try {
                Pair<String, Cache> cacheImpl = getCacheImpl(cache);

                long start = System.currentTimeMillis();
                Map<String, Object> cacheMap = cacheImpl.getRight().read(keys);
                Logger.info("cache [{}] read batch cost: [{}] ms",
                        cacheImpl.getLeft(),
                        (System.currentTimeMillis() - start));

                // collect not nit keys, keep order when full shooting
                Map<String, Object> hitValueMap = new LinkedHashMap<>();
                Set<String> notHitKeys = new LinkedHashSet<>();
                for (String key : keys) {
                    Object value = cacheMap.get(key);

                    if (value == null) {
                        notHitKeys.add(key);
                    } else {
                        hitValueMap.put(key, value);
                    }
                }

                cacheKeys = new CacheKeys(hitValueMap, notHitKeys);
            } catch (Throwable e) {
                Logger.error("read multi cache failed, keys: {}", keys, e);
                cacheKeys = new CacheKeys();
            }
        }

        return cacheKeys;
    }

    public void writeBatch(String cache, Map<String, Object> keyValueMap, int expire) {
        try {
            Pair<String, Cache> cacheImpl = getCacheImpl(cache);

            long start = System.currentTimeMillis();
            cacheImpl.getRight().write(keyValueMap, expire);
            Logger.info("cache [{}] write batch cost: [{}] ms",
                    cacheImpl.getLeft(),
                    (System.currentTimeMillis() - start));

        } catch (Exception e) {
            Logger.error("write map multi cache failed, keys: {}", keyValueMap.keySet(), e);
        }
    }

    public void remove(String cache, String... keys) {
        if (keys != null && keys.length != 0) {
            try {
                Pair<String, Cache> cacheImpl = getCacheImpl(cache);

                long start = System.currentTimeMillis();
                cacheImpl.getRight().remove(keys);
                Logger.info("cache [{}] remove cost: [{}] ms",
                        cacheImpl.getLeft(),
                        (System.currentTimeMillis() - start));

            } catch (Throwable e) {
                Logger.error("remove cache failed, keys: {}: ", keys, e);
            }
        }
    }

    private Pair<String, Cache> getCacheImpl(String cacheName) {
        if (StringUtils.isEmpty(cacheName)) {
            return defaultCache;
        } else {
            return cachePool.computeIfAbsent(cacheName, (key) -> {
                throw new CommonException(StringUtils.format("no cache implementation named [%s].", key));
            });
        }
    }
}
