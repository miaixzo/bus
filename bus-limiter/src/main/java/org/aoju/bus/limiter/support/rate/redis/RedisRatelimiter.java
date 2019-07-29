package org.aoju.bus.limiter.support.rate.redis;

import org.aoju.bus.limiter.support.rate.RateLimiter;
import org.redisson.Redisson;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class RedisRatelimiter extends RateLimiter {

    private static final Logger logger = LoggerFactory.getLogger(RedisRatelimiter.class);

    private String limiterName;

    private RateLimiterRedission ratelimiterRedission;

    /**
     * @param limiterName
     * @param config
     */
    public RedisRatelimiter(String limiterName, Config config) {
        this.limiterName = limiterName;
        this.ratelimiterRedission = new RateLimiterRedission(config);
        logger.info("RedisRateLimiter named {} start success!", limiterName);

    }

    @Override
    public boolean acquire(Object key, double rate, long capacity) {
        RedisRatelimiterObject rateLimiterObject = ratelimiterRedission.getRedisRatelimiterObject(key.toString());
        return rateLimiterObject.tryAcquire(1, rate, capacity);
    }

    @Override
    public String getLimiterName() {
        return limiterName;
    }


    /**
     * 继承自Redisson 实现自定义api
     */
    public static class RateLimiterRedission extends Redisson {


        public RateLimiterRedission(Config config) {
            super(config);
        }

        public RedisRatelimiterObject getRedisRatelimiterObject(String name) {
            return new RedisRatelimiterObject(connectionManager.getCommandExecutor(), name);
        }
    }

}
