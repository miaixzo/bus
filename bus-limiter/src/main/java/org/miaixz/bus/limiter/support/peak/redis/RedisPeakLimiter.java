/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.limiter.support.peak.redis;

import org.miaixz.bus.limiter.support.peak.PeakLimiter;
import org.redisson.Redisson;
import org.redisson.api.RSemaphore;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class RedisPeakLimiter extends PeakLimiter {

    private Redisson redisson;

    private String limiterName;

    public RedisPeakLimiter(Redisson redisson, String limiterName) {
        this.redisson = redisson;
        this.limiterName = limiterName;
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
