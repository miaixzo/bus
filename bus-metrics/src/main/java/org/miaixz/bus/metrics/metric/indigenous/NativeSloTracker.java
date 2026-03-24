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

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.miaixz.bus.metrics.observe.slo.ErrorBudget;
import org.miaixz.bus.metrics.observe.slo.SloEvent;
import org.miaixz.bus.metrics.observe.slo.SloTracker;
import org.miaixz.bus.metrics.observe.tag.Tag;

/**
 * Native SloTracker implementation backed by {@link ErrorBudget}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class NativeSloTracker implements SloTracker {

    /**
     * Registered SLO definitions keyed by SLO name.
     */
    private final ConcurrentHashMap<String, SloDefinition> definitions = new ConcurrentHashMap<>();
    /**
     * Callbacks fired when the error budget for a named SLO is exhausted.
     */
    private final ConcurrentHashMap<String, Consumer<SloEvent>> exhaustedCallbacks = new ConcurrentHashMap<>();

    /**
     * Immutable definition of a registered SLO, including its associated {@link ErrorBudget}.
     *
     * @param timerOrMeterName name of the timer or meter metric being observed
     * @param isLatency        true for latency-based SLOs; false for availability-based
     * @param thresholdMs      latency threshold in milliseconds (latency SLOs only)
     * @param maxErrorRatio    maximum allowed error ratio (availability SLOs only)
     * @param target           SLO target fraction, e.g. 0.999
     * @param tags             optional tag filters
     * @param budget           the error budget tracking instance
     */
    private record SloDefinition(String timerOrMeterName, boolean isLatency, long thresholdMs, double maxErrorRatio,
            double target, Tag[] tags, ErrorBudget budget) {
    }

    /**
     * Register a latency-based SLO.
     *
     * @param sloName     unique SLO name
     * @param timerName   name of the timer metric to observe
     * @param thresholdMs latency threshold in milliseconds; requests below this count as good
     * @param target      SLO target fraction, e.g. 0.999 for 99.9%
     * @param tags        optional tag filters
     * @return this (fluent)
     */
    @Override
    public SloTracker trackLatency(String sloName, String timerName, long thresholdMs, double target, Tag... tags) {
        definitions.put(
                sloName,
                new SloDefinition(timerName, true, thresholdMs, 0, target, tags,
                        new ErrorBudget(target, 30 * 60 * 1000L)));
        return this;
    }

    /**
     * Register an availability SLO.
     *
     * @param sloName       unique SLO name
     * @param meterName     name of the meter/ratePair metric
     * @param maxErrorRatio maximum allowed error ratio, e.g. 0.001 for 0.1%
     * @param target        SLO target fraction
     * @param tags          optional tag filters
     * @return this (fluent)
     */
    @Override
    public SloTracker trackAvailability(
            String sloName,
            String meterName,
            double maxErrorRatio,
            double target,
            Tag... tags) {
        definitions.put(
                sloName,
                new SloDefinition(meterName, false, 0, maxErrorRatio, target, tags,
                        new ErrorBudget(target, 30 * 60 * 1000L)));
        return this;
    }

    /**
     * Returns the current compliance ratio for the named SLO. Returns 1.0 if unknown.
     *
     * @param sloName SLO name
     * @return compliance fraction 0.0–1.0
     */
    @Override
    public double compliance(String sloName) {
        SloDefinition def = definitions.get(sloName);
        return def == null ? 1.0 : def.budget().compliance();
    }

    /**
     * Returns the remaining error budget fraction for the named SLO. Returns 1.0 if unknown.
     *
     * @param sloName SLO name
     * @return remaining budget fraction 0.0–1.0
     */
    @Override
    public double errorBudgetRemaining(String sloName) {
        SloDefinition def = definitions.get(sloName);
        return def == null ? 1.0 : def.budget().errorBudgetRemaining();
    }

    /**
     * Returns the burn rate for the named SLO. Returns 0.0 if unknown.
     *
     * @param sloName SLO name
     * @return burn rate; &gt;1.0 means budget is burning faster than allowed
     */
    @Override
    public double burnRate(String sloName) {
        SloDefinition def = definitions.get(sloName);
        return def == null ? 0.0 : def.budget().burnRate();
    }

    /**
     * Register a callback fired when the error budget for the named SLO is exhausted.
     *
     * @param sloName  SLO name to watch
     * @param callback invoked with the {@link SloEvent} when budget reaches zero
     * @return this (fluent)
     */
    @Override
    public SloTracker onBudgetExhausted(String sloName, Consumer<SloEvent> callback) {
        exhaustedCallbacks.put(sloName, callback);
        return this;
    }

    /**
     * Record a single request observation for the named SLO.
     *
     * @param sloName    SLO name
     * @param durationMs measured request duration in milliseconds
     * @param error      true if the request ended in error
     */
    @Override
    public void record(String sloName, long durationMs, boolean error) {
        SloDefinition def = definitions.get(sloName);
        if (def == null) {
            return;
        }
        boolean good;
        if (def.isLatency()) {
            good = !error && durationMs <= def.thresholdMs();
        } else {
            good = !error;
        }
        if (good) {
            def.budget().recordGood();
        } else {
            def.budget().recordBad();
        }
        // Check budget exhaustion
        if (def.budget().errorBudgetRemaining() <= 0) {
            Consumer<SloEvent> cb = exhaustedCallbacks.get(sloName);
            if (cb != null) {
                cb.accept(
                        new SloEvent(sloName, def.target(), def.budget().compliance(), 0.0, def.budget().burnRate(),
                                Instant.now()));
            }
        }
    }

}
