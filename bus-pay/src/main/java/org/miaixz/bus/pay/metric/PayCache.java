package org.miaixz.bus.pay.metric;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.cache.metric.MemoryCache;

/**
 * 默认缓存实现
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum PayCache implements ExtendCache {

    /**
     * 当前实例
     */
    INSTANCE;

    private final CacheX cache;

    PayCache() {
        cache = new MemoryCache();
    }

    /**
     * 存入缓存
     *
     * @param key   缓存key
     * @param value 缓存内容
     */
    @Override
    public void cache(String key, Object value) {
        cache.write(key, value, 3 * 60 * 1000);
    }

    /**
     * 存入缓存
     *
     * @param key     缓存key
     * @param value   缓存内容
     * @param timeout 指定缓存过期时间(毫秒)
     */
    @Override
    public void cache(String key, Object value, long timeout) {
        cache.write(key, value, timeout);
    }

    /**
     * 获取缓存内容
     *
     * @param key 缓存key
     * @return 缓存内容
     */
    @Override
    public Object get(String key) {
        return cache.read(key);
    }

    @Override
    public boolean containsKey(String key) {
        return false;
    }

    @Override
    public void remove(String key) {
        cache.remove(key);
    }

    @Override
    public void pruneCache() {
        ExtendCache.super.pruneCache();
    }

}
