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

/**
 * Computes error budget values from a given SLO target and observed compliance.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ErrorBudget {

    /**
     * SLO target fraction, e.g. 0.999 for 99.9%.
     */
    private final double target;
    /**
     * Observation window length in milliseconds.
     */
    private final long windowMillis;
    /**
     * Cumulative count of good (successful) requests.
     */
    private long goodRequests = 0;
    /**
     * Cumulative count of all requests (good + bad).
     */
    private long totalRequests = 0;
    /**
     * Epoch millisecond timestamp when this budget was created.
     */
    private final long startMs = System.currentTimeMillis();

    /**
     * Create a new error budget tracker.
     *
     * @param target       SLO target as a fraction, e.g. 0.999 for 99.9%
     * @param windowMillis observation window length in milliseconds
     */
    public ErrorBudget(double target, long windowMillis) {
        this.target = target;
        this.windowMillis = windowMillis;
    }

    /** Record one good (successful) request. */
    public synchronized void recordGood() {
        goodRequests++;
        totalRequests++;
    }

    /** Record one bad (failed) request. */
    public synchronized void recordBad() {
        totalRequests++;
    }

    /**
     * Returns the observed compliance as a fraction (1.0 = perfect, 0.0 = all bad). Returns 1.0 if no requests have
     * been recorded yet.
     */
    public synchronized double compliance() {
        return totalRequests == 0 ? 1.0 : (double) goodRequests / totalRequests;
    }

    /**
     * Remaining error budget as a fraction (1.0 = full, 0.0 = exhausted).
     */
    public synchronized double errorBudgetRemaining() {
        if (totalRequests == 0) {
            return 1.0;
        }
        double allowed = 1.0 - target;
        double actual = 1.0 - compliance();
        if (allowed <= 0) {
            return actual > 0 ? 0.0 : 1.0;
        }
        return Math.max(0.0, 1.0 - actual / allowed);
    }

    /**
     * Burn rate = (actual error rate) / (allowed error rate). Value > 1 means budget is burning faster than allowed.
     */
    public synchronized double burnRate() {
        if (totalRequests == 0) {
            return 0.0;
        }
        double allowed = 1.0 - target;
        if (allowed <= 0) {
            return 0.0;
        }
        double actual = 1.0 - compliance();
        return actual / allowed;
    }

    /**
     * Returns the SLO target fraction, e.g. 0.999.
     */
    public double target() {
        return target;
    }

}
