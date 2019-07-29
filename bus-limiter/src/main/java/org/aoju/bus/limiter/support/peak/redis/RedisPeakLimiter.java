package org.aoju.bus.limiter.support.peak.redis;

import org.aoju.bus.limiter.support.peak.PeakLimiter;
import org.redisson.Redisson;
import org.redisson.api.RSemaphore;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class RedisPeakLimiter extends PeakLimiter {

    private Redisson redisson;

    private String limiterName;

    public RedisPeakLimiter(Redisson redisson, String limiterName) {
        this.redisson = redisson;
        this.limiterName = limiterName;
        try {

        } finally {

        }
    }

    @Override
    public boolean acquire(Object key, int max) {
        RSemaphore rSemaphore = redisson.getSemaphore(key.toString());
        return rSemaphore.tryAcquire();
    }

    @Override
    public void release(Object key, int max) {
        RSemaphore rSemaphore = redisson.getSemaphore(key.toString());
        rSemaphore.release();
    }

    @Override
    public String getLimiterName() {
        return limiterName;
    }
}
