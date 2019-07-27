package org.aoju.bus.limiter.support.peak.jdk;

import org.aoju.bus.limiter.support.peak.PeakLimiter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.Objects;
import java.util.concurrent.Semaphore;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class JdkPeakLimiter extends PeakLimiter {

    private String limiterName;

    private LoadingCache<CacheKey, Semaphore> cache;

    public JdkPeakLimiter(String limiterName) {
        this.limiterName = limiterName;
        this.cache = CacheBuilder.newBuilder()
                .concurrencyLevel(16)
                .initialCapacity(2048)
                .build(new CacheLoader<CacheKey, Semaphore>() {
                    @Override
                    public Semaphore load(CacheKey key) {
                        return new Semaphore(key.max);
                    }
                });
    }

    @Override
    public boolean acquire(Object key, int max) {
        CacheKey cacheKey = new CacheKey(key, max);
        return cache.getIfPresent(cacheKey).tryAcquire();
    }

    @Override
    public void release(Object key, int max) {
        CacheKey cacheKey = new CacheKey(key, max);
        Semaphore semaphore = cache.getIfPresent(cacheKey);
        if (semaphore != null) {
            semaphore.release();
        }
    }

    @Override
    public String getLimiterName() {
        return limiterName;
    }

    public static class CacheKey {

        private Object key;

        private int max;

        public CacheKey(Object key, int max) {
            this.key = key;
            this.max = max;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CacheKey cacheKey = (CacheKey) o;
            return max == cacheKey.max &&
                    Objects.equals(key, cacheKey.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, max);
        }
    }

}
