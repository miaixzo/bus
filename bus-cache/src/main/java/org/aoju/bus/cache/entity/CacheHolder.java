package org.aoju.bus.cache.entity;

import org.aoju.bus.cache.annotation.CacheKey;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class CacheHolder {

    private Method method;

    //  cached 内容
    private String cache;
    private String prefix;
    private int expire;

    // @CacheKey
    private Map<Integer, CacheKey> cacheKeyMap;

    private int multiIndex = -1;

    private String id;

    private CacheHolder(Method method,
                        String cache, String prefix, int expire,
                        Map<Integer, CacheKey> cacheKeyMap, int multiIndex, String id) {
        this.method = method;
        this.cache = cache;
        this.prefix = prefix;
        this.expire = expire;
        this.cacheKeyMap = cacheKeyMap;
        this.multiIndex = multiIndex;
        this.id = id;
    }

    public Method getMethod() {
        return method;
    }

    public String getCache() {
        return cache;
    }

    public String getPrefix() {
        return prefix;
    }

    public int getExpire() {
        return expire;
    }

    public Map<Integer, CacheKey> getCacheKeyMap() {
        return cacheKeyMap;
    }

    public int getMultiIndex() {
        return multiIndex;
    }

    public boolean isMulti() {
        return multiIndex != -1;
    }

    public String getId() {
        return id;
    }

    public static class Builder {

        private Method method;

        private String cache;

        private String prefix;

        private int expire;

        private Map<Integer, CacheKey> cacheKeyMap;

        private int multiIndex = -1;

        private String id;

        private Builder(Method method) {
            this.method = method;
        }

        public static Builder newBuilder(Method method) {
            return new Builder(method);
        }

        public Builder setCache(String cache) {
            this.cache = cache;
            return this;
        }

        public Builder setPrefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public Builder setExpire(int expire) {
            this.expire = expire;
            return this;
        }

        public Builder setMultiIndex(int multiIndex) {
            this.multiIndex = multiIndex;
            return this;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setCacheKeyMap(Map<Integer, CacheKey> cacheKeyMap) {
            this.cacheKeyMap = cacheKeyMap;
            return this;
        }

        public CacheHolder build() {
            return new CacheHolder(method, cache, prefix, expire, cacheKeyMap, multiIndex, id);
        }
    }

}
