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
package org.miaixz.bus.fabric.observe.nimble;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Thread-safe fabric metric counter and timer.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class FabricMeter {

    /**
     * Counter values.
     */
    private final ConcurrentHashMap<String, LongAdder> counters;

    /**
     * Timing values.
     */
    private final ConcurrentHashMap<String, Timing> timings;

    /**
     * Creates an empty meter.
     */
    public FabricMeter() {
        this.counters = new ConcurrentHashMap<>();
        this.timings = new ConcurrentHashMap<>();
    }

    /**
     * Increments a counter by one.
     *
     * @param name metric name
     */
    public void increment(final String name) {
        add(name, 1);
    }

    /**
     * Adds a delta to a counter.
     *
     * @param name  metric name
     * @param delta delta
     */
    public void add(final String name, final long delta) {
        counters.computeIfAbsent(validateName(name), key -> new LongAdder()).add(delta);
    }

    /**
     * Returns a counter value.
     *
     * @param name metric name
     * @return counter value
     */
    public long count(final String name) {
        final String checked = validateName(name);
        final LongAdder counter = counters.get(checked);
        if (counter != null) {
            return counter.sum();
        }
        final Timing timing = timings.get(checked);
        return timing == null ? 0L : timing.count();
    }

    /**
     * Records a duration.
     *
     * @param name     metric name
     * @param duration duration
     */
    public void timing(final String name, final Duration duration) {
        if (duration == null || duration.isNegative()) {
            throw new ValidateException("Duration must be non-null and non-negative");
        }
        timings.computeIfAbsent(validateName(name), key -> new Timing()).record(duration.toNanos());
    }

    /**
     * Returns an immutable metric snapshot.
     *
     * @return metric snapshot
     */
    public Map<String, Long> snapshot() {
        final LinkedHashMap<String, Long> snapshot = new LinkedHashMap<>();
        counters.forEach((name, value) -> snapshot.put(name, value.sum()));
        timings.forEach((name, timing) -> {
            snapshot.put(name + ".count", timing.count());
            snapshot.put(name + ".totalNanos", timing.totalNanos());
            snapshot.put(name + ".maxNanos", timing.maxNanos());
        });
        return Map.copyOf(snapshot);
    }

    /**
     * Resets all metrics.
     */
    public void reset() {
        counters.clear();
        timings.clear();
    }

    /**
     * Validates a metric name.
     *
     * @param name metric name
     * @return metric name
     */
    private static String validateName(final String name) {
        if (StringKit.isBlank(name) || StringKit.containsAny(name, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("Metric name must be non-blank and single-line");
        }
        return name;
    }

    /**
     * Timing aggregate values.
     */
    private static final class Timing {

        /**
         * Timing count.
         */
        private final LongAdder count = new LongAdder();

        /**
         * Total nanoseconds.
         */
        private final LongAdder totalNanos = new LongAdder();

        /**
         * Maximum nanoseconds.
         */
        private final AtomicLong maxNanos = new AtomicLong();

        /**
         * Records nanoseconds.
         *
         * @param nanos nanoseconds
         */
        private void record(final long nanos) {
            count.increment();
            totalNanos.add(nanos);
            maxNanos.accumulateAndGet(nanos, Math::max);

        }

        /**
         * Returns timing count.
         *
         * @return timing count
         */
        private long count() {
            return count.sum();
        }

        /**
         * Returns total nanoseconds.
         *
         * @return total nanoseconds
         */
        private long totalNanos() {
            return totalNanos.sum();
        }

        /**
         * Returns maximum nanoseconds.
         *
         * @return maximum nanoseconds
         */
        private long maxNanos() {
            return maxNanos.get();
        }

    }

}
