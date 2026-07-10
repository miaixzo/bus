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

import java.time.Duration;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Clock;
import org.miaixz.bus.fabric.observe.event.FabricEvent;
import org.miaixz.bus.fabric.observe.nimble.FabricMeter;
import org.miaixz.bus.fabric.observe.tag.Tags;
import org.miaixz.bus.fabric.observe.watch.StopWatch;
import org.miaixz.bus.fabric.observe.window.RollingWindow;

/**
 * Event observer that records marker counters, failure counters, rolling event rate, and terminal durations.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class MeterEventObserver implements EventObserver {

    /**
     * Default rolling window duration.
     */
    private static final Duration DEFAULT_WINDOW = Duration.ofMinutes(1);

    /**
     * Default rolling bucket duration.
     */
    private static final Duration DEFAULT_BUCKET = Duration.ofSeconds(1);

    /**
     * Failure counter name.
     */
    private static final String FAILURE = "failure";

    /**
     * Duration metric suffix.
     */
    private static final String DURATION = ".duration";

    /**
     * Meter.
     */
    private final FabricMeter meter;

    /**
     * Rolling event window.
     */
    private final RollingWindow window;

    /**
     * Clock.
     */
    private final Clock clock;

    /**
     * Active watches keyed by marker family and event tags.
     */
    private final ConcurrentHashMap<String, StopWatch> watches;

    /**
     * Creates an observer.
     *
     * @param clock  clock
     * @param window rolling window
     */
    private MeterEventObserver(final Clock clock, final RollingWindow window) {
        this.clock = require(clock, "Clock");
        this.window = require(window, "Rolling window");
        this.meter = new FabricMeter();
        this.watches = new ConcurrentHashMap<>();
    }

    /**
     * Creates an observer with a system clock and one-minute rolling window.
     *
     * @return observer
     */
    public static MeterEventObserver create() {
        return create(Clock.system(), RollingWindow.of(DEFAULT_WINDOW, DEFAULT_BUCKET));
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
        final FabricEvent current = require(event, "Event");
        final ObservationMarker marker = current.marker();
        meter.increment(marker.code());
        if (marker.failure() || current.cause() != null) {
            meter.increment(FAILURE);
            meter.increment(marker.code() + "." + FAILURE);
        }
        window.add(1, current.time());
        watch(current);
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
     * Starts or stops a watch for the event family and tag set.
     *
     * @param event event
     */
    private void watch(final FabricEvent event) {
        final String key = watchKey(event);
        if (event.marker().terminal()) {
            final StopWatch watch = watches.remove(key);
            if (watch != null) {
                meter.timing(event.marker().code() + DURATION, watch.stop());
            }
            return;
        }
        watches.putIfAbsent(key, StopWatch.start(clock));
    }

    /**
     * Creates a stable stopwatch key from marker family and event tags.
     *
     * @param event event
     * @return key
     */
    private static String watchKey(final FabricEvent event) {
        final String code = event.marker().code();
        final int dot = code.indexOf('.');
        final String family = dot < 0 ? code : code.substring(0, dot);
        final StringJoiner joiner = new StringJoiner("&", family + "|", "");
        event.tags().asMap().entrySet().stream().filter(entry -> stableWatchTag(entry.getKey()))
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> joiner.add(entry.getKey() + "=" + entry.getValue()));
        return joiner.toString();
    }

    /**
     * Returns whether a tag is stable across start and terminal events.
     *
     * @param key tag key
     * @return true when stable
     */
    private static boolean stableWatchTag(final String key) {
        return !Tags.PHASE.equals(key) && !Tags.RESULT.equals(key) && !Tags.EXCEPTION.equals(key);
    }

    /**
     * Validates required references.
     *
     * @param value value
     * @param name  name
     * @param <T>   value type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value;
    }

}
