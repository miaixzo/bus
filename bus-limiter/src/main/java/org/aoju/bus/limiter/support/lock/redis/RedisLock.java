package org.aoju.bus.limiter.support.lock.redis;

import org.aoju.bus.limiter.support.lock.Lock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class RedisLock extends Lock {

    private RedissonClient redisson;

    private String lockName;

    public RedisLock(RedissonClient redisson, String lockName) {
        this.redisson = redisson;
        this.lockName = lockName;
    }

    @Override
    public boolean lock(Object key) {
        RLock rLock = redisson.getLock(key.toString());
        return rLock.tryLock();
    }

    @Override
    public void unlock(Object key) {
        RLock rLock = redisson.getLock(key.toString());
        rLock.unlock();
    }

    @Override
    public String getLimiterName() {
        return lockName;
    }
}
