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
package org.aoju.bus.cache;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.aoju.bus.cache.entity.CacheKeys;
import org.aoju.bus.cache.entity.Pair;
import org.aoju.bus.cache.support.cache.Cache;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.logger.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
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
                throw new InstrumentException(StringUtils.format("no cache implementation named [%s].", key));
            });
        }
    }

}
