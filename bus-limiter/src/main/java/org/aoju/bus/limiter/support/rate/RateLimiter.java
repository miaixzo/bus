package org.aoju.bus.limiter.support.rate;

import org.aoju.bus.limiter.Limiter;
import org.aoju.bus.limiter.annotation.HRateLimiter;

import java.util.Map;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public abstract class RateLimiter implements Limiter<HRateLimiter> {

    public abstract boolean acquire(Object key, double rate, long capacity);

    @Override
    public boolean limit(Object key, Map<String, Object> args) {
        double pps = (double) args.get("rate");
        long capacity = (long) args.get("capacity");
        return acquire(key, pps, capacity);
    }

    @Override
    public void release(Object key, Map<String, Object> args) {
    }

}
