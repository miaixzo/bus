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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import org.miaixz.bus.metrics.Builder;

/**
 * Exponentially Weighted Moving Average rate calculator.
 * <p>
 * Algorithm identical to Dropwizard Metrics EWMA and Linux kernel load average.
 * {@code alpha = 1 - exp(-interval / period)}, ticked every {@code interval} seconds.
 * <p>
 * Tick must be called externally (e.g. by a shared ScheduledExecutorService every 5 s).
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class EwmaRate {

    /**
     * 1-minute EWMA: alpha = 1 - exp(-5/60)
     */
    public static final double M1_ALPHA = Builder.EWMA_M1_ALPHA;
    /**
     * 5-minute EWMA: alpha = 1 - exp(-5/300)
     */
    public static final double M5_ALPHA = Builder.EWMA_M5_ALPHA;
    /**
     * 15-minute EWMA: alpha = 1 - exp(-5/900)
     */
    public static final double M15_ALPHA = Builder.EWMA_M15_ALPHA;

    /**
     * Smoothing factor; determines how quickly the average responds to changes.
     */
    private final double alpha;
    /**
     * Tick interval in seconds; used to compute instantaneous rate.
     */
    private final double intervalSeconds;
    /**
     * Whether the first tick has been processed; false until first {@link #tick()} call.
     */
    private volatile boolean initialized = false;
    /**
     * Current EWMA rate in events/second.
     */
    private volatile double rate = 0.0;
    /**
     * Accumulates event counts between ticks.
     */
    private final LongAdder uncounted = new LongAdder();

    /**
     * Create an EWMA rate calculator.
     *
     * @param alpha        smoothing factor; use {@link #M1_ALPHA}, {@link #M5_ALPHA}, or {@link #M15_ALPHA}
     * @param interval     tick interval value
     * @param intervalUnit tick interval unit
     */
    public EwmaRate(double alpha, long interval, TimeUnit intervalUnit) {
        this.alpha = alpha;
        this.intervalSeconds = intervalUnit.toNanos(interval) / 1_000_000_000.0;
    }

    /** Returns a new EWMA configured for a 1-minute moving average, ticked every 5 seconds. */
    public static EwmaRate oneMinute() {
        return new EwmaRate(M1_ALPHA, 5, TimeUnit.SECONDS);
    }

    /** Returns a new EWMA configured for a 5-minute moving average, ticked every 5 seconds. */
    public static EwmaRate fiveMinutes() {
        return new EwmaRate(M5_ALPHA, 5, TimeUnit.SECONDS);
    }

    /** Returns a new EWMA configured for a 15-minute moving average, ticked every 5 seconds. */
    public static EwmaRate fifteenMinutes() {
        return new EwmaRate(M15_ALPHA, 5, TimeUnit.SECONDS);
    }

    /**
     * Add {@code n} events to the uncounted accumulator.
     *
     * @param n number of events to mark
     */
    public void mark(long n) {
        uncounted.add(n);
    }

    /**
     * Update the moving average. Must be called every {@code intervalSeconds} seconds.
     */
    public void tick() {
        double instantRate = uncounted.sumThenReset() / intervalSeconds;
        if (!initialized) {
            rate = instantRate;
            initialized = true;
        } else {
            rate += alpha * (instantRate - rate);
        }
    }

    /**
     * Returns the current EWMA rate in events/second.
     */
    public double rate() {
        return rate;
    }

}
