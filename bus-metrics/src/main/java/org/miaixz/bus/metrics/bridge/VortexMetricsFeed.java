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
package org.miaixz.bus.metrics.bridge;

import java.util.concurrent.TimeUnit;

import org.miaixz.bus.metrics.Metrics;

/**
 * Provides per-route performance metrics to bus-vortex for dynamic weight adjustment.
 * <p>
 * VortexHandler records duration and outcome per asset; this class accumulates the rolling statistics that
 * DynamicWeightAdjuster consumes every 10 seconds.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class VortexMetricsFeed {

    /** Private constructor; this is a static utility class. */
    private VortexMetricsFeed() {

    }

    /**
     * Record a single route call outcome. Called from VortexHandler after each request completes.
     *
     * @param assetId    unique asset identifier
     * @param durationNs call duration in nanoseconds
     * @param success    true if call succeeded
     */
    public static void record(String assetId, long durationNs, boolean success) {
        Metrics.timer("vortex.route.duration", "asset", assetId).record(durationNs, TimeUnit.NANOSECONDS);
        Metrics.ratePair("vortex.route.rate", "asset", assetId).recordSuccess();
        if (!success) {
            Metrics.ratePair("vortex.route.rate", "asset", assetId).recordError();
        }
    }

    /**
     * Get the current P95 latency for an asset in milliseconds. Used by DynamicWeightAdjuster to compute relative
     * weight.
     *
     * @param assetId asset identifier
     * @return P95 latency in milliseconds, or 0 if no data
     */
    public static double p95Ms(String assetId) {
        return Metrics.timer("vortex.route.duration", "asset", assetId).percentile(0.95, TimeUnit.MILLISECONDS);
    }

    /**
     * Get the 1-minute error rate for an asset.
     *
     * @param assetId asset identifier
     * @return error rate [0.0, 1.0]
     */
    public static double errorRate(String assetId) {
        return Metrics.ratePair("vortex.route.rate", "asset", assetId).errorRate();
    }

}
