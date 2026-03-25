/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.metrics.window;

import java.util.concurrent.atomic.LongAdder;

/**
 * A ring-buffer bucket window for computing rolling counts over 1-minute and 5-minute windows.
 * <p>
 * Each bucket covers one second. The ring advances every second via {@link #advance()}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class BucketWindow {

    /**
     * Number of one-second buckets in the ring; covers a 5-minute window.
     */
    private static final int RING_SIZE = 300; // 5 minutes of 1-second buckets

    /**
     * Ring buffer of per-second accumulators.
     */
    private final LongAdder[] buckets;
    /**
     * Index of the currently active bucket; advances each second.
     */
    private volatile int currentBucket = 0;

    /**
     * Creates a new BucketWindow with 300 one-second buckets (5-minute capacity).
     */
    public BucketWindow() {
        buckets = new LongAdder[RING_SIZE];
        for (int i = 0; i < RING_SIZE; i++) {
            buckets[i] = new LongAdder();
        }
    }

    /**
     * Record a value into the current bucket.
     *
     * @param value the value to add
     */
    public void record(long value) {
        buckets[currentBucket].add(value);
    }

    /** Advance time by one second. Called by NativeTimer's background scheduler. */
    public void advance() {
        int next = (currentBucket + 1) % RING_SIZE;
        buckets[next].reset();
        currentBucket = next;
    }

    /** Sum of all values in the last {@code seconds} seconds. */
    public long sum(int seconds) {
        int cur = currentBucket;
        long total = 0;
        for (int i = 0; i < seconds && i < RING_SIZE; i++) {
            int idx = (cur - i + RING_SIZE) % RING_SIZE;
            total += buckets[idx].sum();
        }
        return total;
    }

    /**
     * Returns the sum of all values recorded in the last 60 seconds.
     */
    public long oneMinuteSum() {
        return sum(60);
    }

    /**
     * Returns the sum of all values recorded in the last 300 seconds.
     */
    public long fiveMinuteSum() {
        return sum(300);
    }

}
