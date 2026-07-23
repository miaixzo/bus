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
package org.miaixz.bus.fabric.registry.route;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Clock;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.observe.ObservationMarker;
import org.miaixz.bus.fabric.observe.event.FabricEvent;

/**
 * Route candidate selector that defers failed routes behind fresh candidates.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Selector {

    /**
     * Ready route candidates.
     */
    private final ArrayDeque<Route> ready;

    /**
     * Failed route candidates.
     */
    private final ArrayDeque<Route> failed;

    /**
     * Latest failure and retry schedule indexed by route.
     */
    private final Map<Route, Backoff> failures;

    /**
     * Observer receiving route readiness and backoff events.
     */
    private volatile EventObserver observer;

    /**
     * Runtime clock used for route state and observation events.
     */
    private final Clock clock;

    /**
     * Creates an empty route selector.
     */
    public Selector() {
        this(EventObserver.noop(), Clock.system());
    }

    /**
     * Creates an empty route selector.
     *
     * @param observer observer receiving route state events
     */
    public Selector(final EventObserver observer) {
        this(observer, Clock.system());
    }

    /**
     * Creates an empty route selector with explicit runtime dependencies.
     *
     * @param observer observer receiving route state events
     * @param clock    runtime clock
     */
    public Selector(final EventObserver observer, final Clock clock) {
        this.ready = new ArrayDeque<>();
        this.failed = new ArrayDeque<>();
        this.failures = new HashMap<>();
        this.observer = EventObserver.safe(require(observer, "Route observer"));
        this.clock = require(clock, "Clock");
    }

    /**
     * Sets route observer.
     *
     * @param observer replacement observer receiving route state events
     */
    public void observer(final EventObserver observer) {
        this.observer = EventObserver.safe(require(observer, "Route observer"));
    }

    /**
     * Adds a route candidate.
     *
     * @param route candidate to append when not already tracked
     */
    public synchronized void add(final Route route) {
        require(route);
        if (!ready.contains(route) && !failed.contains(route)) {
            ready.addLast(route);
        }
    }

    /**
     * Returns the next route candidate.
     *
     * @return first ready route, otherwise an eligible failed route, or {@code null} when empty
     */
    public synchronized Route next() {
        return next(clock);
    }

    /**
     * Returns the next route candidate at a point in time.
     *
     * @param clock clock supplying the retry eligibility instant
     * @return first ready route, otherwise an eligible failed route, or {@code null} when empty
     */
    public synchronized Route next(final Clock clock) {
        require(clock);
        if (!ready.isEmpty()) {
            return ready.peekFirst();
        }
        final Instant now = clock.now();
        for (final Route route : failed) {
            final Backoff backoff = failures.get(route);
            if (backoff == null || !backoff.postponed(now)) {
                return route;
            }
        }
        return failed.peekFirst();
    }

    /**
     * Marks a route as failed.
     *
     * @param route route whose failure count and retry deadline are updated
     */
    public synchronized void failed(final Route route) {
        failed(route, clock);
    }

    /**
     * Marks a route as failed at a point in time.
     *
     * @param route route whose failure count and retry deadline are updated
     * @param clock clock supplying failure and event timestamps
     */
    public synchronized void failed(final Route route, final Clock clock) {
        require(route);
        require(clock);
        final Instant now = clock.now();
        final Backoff previous = failures.get(route);
        final int failures = previous == null ? Normal._1 : previous.failures() + Normal._1;
        final Duration delay = backoff(failures);
        this.failures.put(route, new Backoff(route, now, failures, now.plus(delay)));
        ready.remove(route);
        if (!failed.contains(route)) {
            failed.addLast(route);
        }
        emit(ObservationMarker.ROUTE_BACKOFF, route, failures, delay, clock);
    }

    /**
     * Marks a route as connected.
     *
     * @param route successfully connected route to restore to the ready queue
     */
    public synchronized void connected(final Route route) {
        require(route);
        failures.remove(route);
        failed.remove(route);
        if (!ready.contains(route)) {
            ready.addLast(route);
        }
        emit(ObservationMarker.ROUTE_READY, route, Normal._0, Duration.ZERO, clock);
    }

    /**
     * Returns all route candidates.
     *
     * @return immutable route snapshot
     */
    public synchronized List<Route> snapshot() {
        final ArrayList<Route> snapshot = new ArrayList<>(ready);
        snapshot.addAll(failed);
        return List.copyOf(snapshot);
    }

    /**
     * Returns a backoff memory snapshot.
     *
     * @return immutable snapshot of current route backoff records
     */
    public synchronized List<Backoff> failures() {
        return List.copyOf(failures.values());
    }

    /**
     * Returns whether a route is currently under failure backoff.
     *
     * @param route route whose retry deadline is inspected
     * @param clock clock supplying the comparison instant
     * @return true when postponed
     */
    public synchronized boolean postponed(final Route route, final Clock clock) {
        require(route);
        require(clock);
        final Backoff backoff = failures.get(route);
        return backoff != null && backoff.postponed(clock.now());
    }

    /**
     * Returns whether no routes are available.
     *
     * @return true when empty
     */
    public synchronized boolean empty() {
        return ready.isEmpty() && failed.isEmpty();
    }

    /**
     * Validates route references.
     *
     * @param route route reference to validate
     */
    private static void require(final Route route) {
        Assert.notNull(route, () -> new ValidateException("Route must not be null"));
    }

    /**
     * Validates clock references.
     *
     * @param clock clock reference to validate
     */
    private static void require(final Clock clock) {
        Assert.notNull(clock, () -> new ValidateException("Clock must not be null"));
    }

    /**
     * Emits a route event.
     *
     * @param marker   route event marker
     * @param route    route associated with the event
     * @param attempts attempt count
     * @param delay    retry delay associated with the event
     * @param clock    event clock
     */
    private void emit(
            final ObservationMarker marker,
            final Route route,
            final int attempts,
            final Duration delay,
            final Clock clock) {
        observer.emit(
                FabricEvent.builder(marker, clock).tag(Builder.TAG_OPERATION_ID, route.id())
                        .tag(Builder.TAG_KEY, route.id()).tag(Builder.TAG_ATTEMPT, Integer.toString(attempts))
                        .tag(Builder.TAG_DELAY, delay.toString()).build());
    }

    /**
     * Validates required references.
     *
     * @param value reference to validate
     * @param name  field name included in the validation failure
     * @param <T>   reference type
     * @return validated non-null reference
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

    /**
     * Returns exponential backoff for a failure count.
     *
     * @param failures failure count
     * @return exponentially increasing retry delay capped by the selector maximum
     */
    private static Duration backoff(final int failures) {
        final int shift = Math.min(Normal._8, Math.max(Normal._0, failures - Normal._1));
        final Duration backoff = Builder.DURATION_1_SECOND.multipliedBy(1L << shift);
        return backoff.compareTo(Builder.SELECTOR_MAX_BACKOFF) > 0 ? Builder.SELECTOR_MAX_BACKOFF : backoff;
    }

}
