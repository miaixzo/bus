package org.aoju.bus.limiter.support.lock;

import org.aoju.bus.limiter.Limiter;
import org.aoju.bus.limiter.annotation.HLock;

import java.util.Map;

/**
 * Lock
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public abstract class Lock implements Limiter<HLock> {

    public abstract boolean lock(Object key);

    public abstract void unlock(Object key);

    @Override
    public boolean limit(Object key, Map<String, Object> args) {
        return lock(key);
    }

    @Override
    public void release(Object key, Map<String, Object> args) {
        unlock(key);
    }
}
