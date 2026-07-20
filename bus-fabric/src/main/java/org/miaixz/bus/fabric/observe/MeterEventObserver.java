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
     * Meter.
     */
    private final FabricMeter meter;

    /**
     * Rolling event window.
     */
    private final RollingWindow window;

    /**
     * Creates an observer.
     *
     * @param clock  clock
     * @param window rolling window
     */
    private MeterEventObserver(final Clock clock, final RollingWindow window) {
        final Clock checkedClock = Assert.notNull(clock, "Clock must not be null");
        this.window = Assert.notNull(window, "Rolling window must not be null");
        this.meter = FabricMeter.create(checkedClock);
    }

    /**
     * Creates an observer with a system clock and one-minute rolling window.
     *
     * @return observer
     */
    public static MeterEventObserver create() {
        return create(Clock.system(), RollingWindow.of(Builder.DURATION_60_SECONDS, Builder.DURATION_1_SECOND));
    }

    /**
     * Creates an observer with explicit time dependencies.
     *
     * @param clock  clock
     * @param window rolling window
     * @return observer
     */
    public static MeterEventObserver create(final Clock clock, final RollingWindow window) {
        return new MeterEventObserver(clock, window);
    }

    /**
     * Emits an event into the meter and rolling window.
     *
     * @param event event
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
     * @return meter
     */
    public FabricMeter meter() {
        return meter;
    }

    /**
     * Returns the rolling event window.
     *
     * @return rolling window
     */
    public RollingWindow window() {
        return window;
    }

    /**
     * Starts or stops timing for the event family and tag set.
     *
     * @param event event
     */
    private void measure(final FabricEvent event) {
        final ObservationMarker marker = event.marker();
        meter.observe(marker.timing(), event.tags().get(Builder.TAG_OPERATION_ID), marker.family());
    }

}
