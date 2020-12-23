package org.aoju.bus.goalie.metric;

import com.google.common.util.concurrent.RateLimiter;
import lombok.Data;

/**
 * 限流器
 *
 * @author Justubborn
 * @since 2020/12/21
 */
@Data
public class Limiter {

    private String ip;

    private String method;

    private String version;

    private int tokenCount;
    /**
     * 令牌桶
     */
    private volatile RateLimiter rateLimiter;

    public synchronized void initRateLimiter() {
        rateLimiter = RateLimiter.create(tokenCount);
    }

    /**
     * 获取令牌桶
     *
     * @return 限流器
     */
    public RateLimiter fetchRateLimiter() {
        if (rateLimiter == null) {
            synchronized (this) {
                if (rateLimiter == null) {
                    rateLimiter = RateLimiter.create(tokenCount);
                }
            }
        }
        return rateLimiter;
    }

    public double acquire() {
        return fetchRateLimiter().acquire();
    }


}
