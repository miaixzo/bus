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
package org.miaixz.bus.metrics.metric.indigenous;

import java.util.concurrent.atomic.LongAdder;

import org.miaixz.bus.metrics.metric.Meter;
import org.miaixz.bus.metrics.window.EwmaRate;

/**
 * Counter with 1m/5m/15m EWMA rate tracking. Shares a global tick scheduler provided by {@link NativeProvider}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NativeMeter implements Meter {

    /**
     * Lock-free accumulator for the cumulative event count.
     */
    private final LongAdder adder = new LongAdder();
    /**
     * 1-minute EWMA rate calculator.
     */
    private final EwmaRate m1 = EwmaRate.oneMinute();
    /**
     * 5-minute EWMA rate calculator.
     */
    private final EwmaRate m5 = EwmaRate.fiveMinutes();
    /**
     * 15-minute EWMA rate calculator.
     */
    private final EwmaRate m15 = EwmaRate.fifteenMinutes();
    /**
     * Nanosecond timestamp at creation; used to compute mean rate.
     */
    private final long startNanos = System.nanoTime();

    /** Increment by one. */
    @Override
    public void increment() {
        increment(1);
    }

    /**
     * Increment by the given amount.
     *
     * @param amount number of events to add
     */
    @Override
    public void increment(long amount) {
        adder.add(amount);
        m1.mark(amount);
        m5.mark(amount);
        m15.mark(amount);
    }

    /** Returns the cumulative count since creation. */
    @Override
    public long count() {
        return adder.sum();
    }

    /** Called by the global 5-second tick scheduler in NativeProvider. */
    public void tick() {
        m1.tick();
        m5.tick();
        m15.tick();
    }

    /** Returns the 1-minute EWMA rate in events/second. */
    @Override
    public double oneMinuteRate() {
        return m1.rate();
    }

    /** Returns the 5-minute EWMA rate in events/second. */
    @Override
    public double fiveMinuteRate() {
        return m5.rate();
    }

    /** Returns the 15-minute EWMA rate in events/second. */
    @Override
    public double fifteenMinuteRate() {
        return m15.rate();
    }

    /** Returns the mean rate since creation in events/second. */
    @Override
    public double meanRate() {
        double elapsedSeconds = (System.nanoTime() - startNanos) / 1_000_000_000.0;
        return elapsedSeconds <= 0 ? 0 : adder.sum() / elapsedSeconds;
    }

}
