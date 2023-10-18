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
package org.aoju.bus.goalie.metric;

import com.google.common.util.concurrent.RateLimiter;
import lombok.Data;

/**
 * 限流器
 *
 * @author Justubborn
 * @since Java 17+
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
        if (null == rateLimiter) {
            synchronized (this) {
                if (null == rateLimiter) {
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
