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
package org.miaixz.bus.metrics.observe.slo;

import java.util.function.Consumer;

import org.miaixz.bus.metrics.observe.tag.Tag;

/**
 * Tracks SLO (Service Level Objectives) with error-budget accounting.
 * <p>
 * Bridges technical metrics (SLI) to business commitments (SLA) without requiring external Prometheus recording rules.
 * Exposes {@code compliance()} and {@code errorBudgetRemaining()} for direct application use.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface SloTracker {

    /**
     * Register a latency-based SLO: requests below {@code thresholdMs} count as good.
     *
     * @param sloName     unique SLO name
     * @param timerName   name of the timer metric to observe
     * @param thresholdMs latency threshold in milliseconds
     * @param target      SLO target (e.g. 0.999 = 99.9%)
     * @param tags        optional tag filters
     * @return this (fluent)
     */
    SloTracker trackLatency(String sloName, String timerName, long thresholdMs, double target, Tag... tags);

    /**
     * Register an availability SLO: requests with error ratio below {@code maxErrorRatio} count as good.
     *
     * @param sloName       unique SLO name
     * @param meterName     name of the meter/ratePair metric
     * @param maxErrorRatio maximum allowed error ratio (e.g. 0.001 = 0.1%)
     * @param target        SLO target
     * @param tags          optional tag filters
     * @return this (fluent)
     */
    SloTracker trackAvailability(String sloName, String meterName, double maxErrorRatio, double target, Tag... tags);

    /** Current compliance ratio (0.0–1.0) for the given SLO. Returns 1.0 if unknown. */
    double compliance(String sloName);

    /** Remaining error budget fraction (1.0 = full, 0.0 = exhausted). Returns 1.0 if unknown. */
    double errorBudgetRemaining(String sloName);

    /**
     * Error burn rate: > 1.0 means budget is burning faster than the SLO allows. Returns 0.0 if unknown.
     */
    double burnRate(String sloName);

    /**
     * Register a callback fired when the error budget is exhausted (budgetRemaining ≤ 0).
     *
     * @param sloName  name of the SLO to watch
     * @param callback callback invoked with the SloEvent
     * @return this (fluent)
     */
    SloTracker onBudgetExhausted(String sloName, Consumer<SloEvent> callback);

    /**
     * Record a single request observation for the SLO named {@code sloName}.
     *
     * @param sloName    SLO name
     * @param durationMs measured request duration in milliseconds
     * @param error      true if the request ended in error
     */
    void record(String sloName, long durationMs, boolean error);

}
