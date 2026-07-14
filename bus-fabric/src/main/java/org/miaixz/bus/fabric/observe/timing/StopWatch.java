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
package org.miaixz.bus.fabric.observe.timing;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Clock;

/**
 * Runtime-clock based stopwatch.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class StopWatch {

    /**
     * Unset stop marker.
     */
    private static final long UNSET = -1L;

    /**
     * Runtime clock.
     */
    private final Clock clock;

    /**
     * Start nanoseconds.
     */
    private final long startNanos;

    /**
     * Stop nanoseconds.
     */
    private final AtomicLong stopNanos;

    /**
     * Creates a stopwatch.
     *
     * @param clock      runtime clock
     * @param startNanos start nanoseconds
     */
    private StopWatch(final Clock clock, final long startNanos) {
        this.clock = clock;
        this.startNanos = startNanos;
        this.stopNanos = new AtomicLong(UNSET);
    }

    /**
     * Starts a stopwatch.
     *
     * @param clock runtime clock
     * @return stopwatch
     */
    public static StopWatch start(final Clock clock) {
        final Clock checked = Assert.notNull(clock, () -> new ValidateException("Runtime clock must not be null"));
        return new StopWatch(checked, checked.nanos());
    }

    /**
     * Returns elapsed duration.
     *
     * @return elapsed duration
     */
    public Duration elapsed() {
        final long stopped = stopNanos.get();
        final long end = stopped == UNSET ? clock.nanos() : stopped;
        return duration(end);
    }

    /**
     * Stops the watch and returns elapsed duration.
     *
     * @return elapsed duration
     */
    public Duration stop() {
        final long stopped = stopNanos.get();
        if (stopped != UNSET) {
            return duration(stopped);
        }
        final long now = clock.nanos();
        duration(now);
        stopNanos.compareAndSet(UNSET, now);
        return duration(stopNanos.get());
    }

    /**
     * Returns whether the watch is stopped.
     *
     * @return true when stopped
     */
    public boolean stopped() {
        return stopNanos.get() != UNSET;
    }

    /**
     * Converts an end time into a duration.
     *
     * @param endNanos end nanoseconds
     * @return duration
     */
    private Duration duration(final long endNanos) {
        Assert.isFalse(endNanos < startNanos, () -> new StatefulException("Runtime clock moved backwards"));
        return Duration.ofNanos(endNanos - startNanos);
    }

}
