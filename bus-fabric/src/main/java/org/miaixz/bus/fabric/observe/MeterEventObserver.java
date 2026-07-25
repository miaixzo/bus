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
package org.miaixz.bus.fabric.observe;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Clock;
import org.miaixz.bus.fabric.observe.event.FabricEvent;
import org.miaixz.bus.fabric.observe.metrics.FabricMeter;
import org.miaixz.bus.fabric.observe.window.RollingWindow;

/**
 * Event observer that records marker counters, failure counters, rolling event rate, and terminal durations.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class MeterEventObserver implements EventObserver {

    /**
     * Counter and timing registry updated by emitted events.
     */
    private final FabricMeter meter;

    /**
     * Rolling count window receiving one sample per emitted event.
     */
    private final RollingWindow window;

    /**
     * Creates an observer with explicit time and rolling-window dependencies.
     *
     * @param clock  clock used by operation timers in the meter
     * @param window rolling window receiving event-count samples
     */
    private MeterEventObserver(final Clock clock, final RollingWindow window) {
        final Clock checkedClock = Assert.notNull(clock, "Clock must not be null");
        this.window = Assert.notNull(window, "Rolling window must not be null");
        this.meter = FabricMeter.create(checkedClock);
    }

    /**
     * Creates an observer with a system clock and one-minute rolling window.
     *
     * @return observer using the system clock and one-second buckets across one minute
     */
    public static MeterEventObserver create() {
        return create(Clock.system(), RollingWindow.of(Builder.DURATION_60_SECONDS, Builder.DURATION_1_SECOND));
    }

    /**
     * Creates an observer with explicit time dependencies.
     *
     * @param clock  clock used by operation timers in the meter
     * @param window rolling window receiving event-count samples
     * @return observer backed by a new meter and the supplied rolling window
     * @throws IllegalArgumentException if {@code clock} or {@code window} is {@code null}
     */
    public static MeterEventObserver create(final Clock clock, final RollingWindow window) {
        return new MeterEventObserver(clock, window);
    }

    /**
     * Records marker counters, optional failure counters, one rolling event sample, and timing state for an event.
     *
     * @param event immutable event to record
     * @throws IllegalArgumentException if {@code event} is {@code null}
     */
    @Override
    public void emit(final FabricEvent event) {
        final FabricEvent current = Assert.notNull(event, "Event must not be null");
        final ObservationMarker marker = current.marker();
        meter.increment(marker.code());
        if (marker.failure() || current.cause() != null) {
            meter.increment(Builder.METER_EVENT_OBSERVER_FAILURE);
            meter.increment(marker.code() + Symbol.DOT + Builder.METER_EVENT_OBSERVER_FAILURE);
        }
        window.add(1, current.time());
        measure(current);
    }

    /**
     * Returns the meter.
     *
     * @return mutable meter updated by this observer
     */
    public FabricMeter meter() {
        return meter;
    }

    /**
     * Returns the rolling event window.
     *
     * @return rolling window supplied when this observer was created
     */
    public RollingWindow window() {
        return window;
    }

    /**
     * Applies the marker's timing role using its family and the event operation identifier.
     *
     * @param event event supplying marker timing metadata and tags
     */
    private void measure(final FabricEvent event) {
        final ObservationMarker marker = event.marker();
        meter.observe(marker.timing(), event.tags().get(Builder.TAG_OPERATION_ID), marker.family());
    }

}
