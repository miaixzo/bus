package org.aoju.bus.limiter.support.peak;

import org.aoju.bus.limiter.Limiter;
import org.aoju.bus.limiter.annotation.HPeak;

import java.util.Map;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public abstract class PeakLimiter implements Limiter<HPeak> {

    public abstract boolean acquire(Object key, int max);

    public abstract void release(Object key, int max);

    @Override
    public boolean limit(Object key, Map<String, Object> args) {
        return acquire(key, (int) args.get("max"));
    }

    @Override
    public void release(Object key, Map<String, Object> args) {
        release(key, (int) args.get("max"));
    }

}
