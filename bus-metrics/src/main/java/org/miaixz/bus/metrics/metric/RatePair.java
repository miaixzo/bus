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
package org.miaixz.bus.metrics.metric;

/**
 * Combines a success Meter and an error Meter, providing direct access to error rate and success rate. Useful for
 * circuit-breaker decisions without external PromQL.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface RatePair {

    /**
     * Record one successful event into both the total and success meters.
     */
    void recordSuccess();

    /**
     * Record one failed event into both the total and error meters.
     */
    void recordError();

    /**
     * error.oneMinuteRate() / total.oneMinuteRate(); returns 0 if no events.
     */
    double errorRate();

    /**
     * success.oneMinuteRate() / total.oneMinuteRate(); returns 1 if no events.
     */
    double successRate();

    /**
     * Returns the combined total meter (successes + errors).
     */
    Meter total();

    /**
     * Returns the error-only meter.
     */
    Meter errors();

    /**
     * Returns the success-only meter.
     */
    Meter successes();

}
