package org.aoju.bus.limiter.support.rate.jdk;

import org.aoju.bus.limiter.support.rate.RateLimiter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 一个RateLimiter组件
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class JdkRateLimiter extends RateLimiter {

    private String rateLimiterName;

    private LoadingCache<CacheKey, RateLimiterObject>
            cache;

    /**
     * @param rateLimiterName
     * @param expireAfterAccess     过期时间
     * @param expireAfterAccessUnit
     */
    public JdkRateLimiter(String rateLimiterName, long expireAfterAccess, TimeUnit expireAfterAccessUnit) {
        this.rateLimiterName = rateLimiterName;
        this.cache = CacheBuilder.newBuilder()
                .expireAfterAccess(expireAfterAccess, expireAfterAccessUnit)
                .concurrencyLevel(16)
                .build(new CacheLoader<CacheKey, RateLimiterObject>() {
                    @Override
                    public RateLimiterObject load(CacheKey key) {
                        return new RateLimiterObject();
                    }
                });
    }

    @Override
    public boolean acquire(Object key, double rate, long capacity) {
        CacheKey cacheKey = new CacheKey(key, rate, capacity);
        RateLimiterObject rateLimiterObject = cache.getUnchecked(cacheKey);
        return rateLimiterObject.tryAcquire(1, rate, capacity);
    }

    @Override
    public String getLimiterName() {
        return rateLimiterName;
    }

    private static class CacheKey {

        private Object key;
        private double rate;
        private long capacity;

        public CacheKey(Object key, double rate, long capacity) {
            this.key = key;
            this.rate = rate;
            this.capacity = capacity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CacheKey cacheKey = (CacheKey) o;
            return Double.compare(cacheKey.rate, rate) == 0 &&
                    capacity == cacheKey.capacity &&
                    Objects.equals(key, cacheKey.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, rate, capacity);
        }
    }

}
