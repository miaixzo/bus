/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.limiter.support.rate.redis;

import org.aoju.bus.limiter.support.rate.RateLimiter;
import org.aoju.bus.logger.Logger;
import org.redisson.Redisson;
import org.redisson.config.Config;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class RedisRatelimiter extends RateLimiter {

    private String limiterName;

    private RateLimiterRedission ratelimiterRedission;

    /**
     * @param limiterName 名称
     * @param config      配置
     */
    public RedisRatelimiter(String limiterName, Config config) {
        this.limiterName = limiterName;
        this.ratelimiterRedission = new RateLimiterRedission(config);
        Logger.info("RedisRateLimiter named {} start success!", limiterName);

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
            return new RedisRatelimiterObject(commandExecutor, name);
        }
    }

}
