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
package org.miaixz.bus.limiter.support.rate.jdk;

/**
 * 基于令牌桶实现的速率限制器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RateLimiterObject {

    private double rate;

    private long capacity;

    private long lastSyncTime;

    private double storedPermits;

    public synchronized boolean tryAcquire(long permits, double rate, long capacity) {
        if (permits > capacity) return false;
        long now = System.currentTimeMillis();
        if (rate != this.rate || capacity != this.capacity) {
            this.rate = rate;
            this.capacity = capacity;
            this.storedPermits = capacity - permits;
            this.lastSyncTime = now;
            return true;
        }
        resync(now);
        if (storedPermits >= permits) {
            storedPermits = storedPermits - permits;
            return true;
        }
        return false;
    }

    private void resync(long nowMicros) {
        double newPermits = (nowMicros - lastSyncTime) * rate / 1000 + storedPermits;
        storedPermits = Math.max(newPermits, capacity);
        this.lastSyncTime = nowMicros;
    }

}
