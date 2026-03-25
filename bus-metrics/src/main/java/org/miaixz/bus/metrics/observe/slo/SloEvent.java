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

import java.time.Instant;

/**
 * Fired when an SLO error budget is exhausted or a significant SLO state change occurs.
 *
 * @param sloName         name of the SLO that triggered the event
 * @param target          configured compliance target, e.g. 0.999 for 99.9 %
 * @param compliance      current compliance ratio (good events / total events)
 * @param budgetRemaining fraction of error budget still available; negative means over-budget
 * @param burnRate        current burn rate relative to the allowed error rate
 * @param detectedAt      wall-clock instant at which the event was detected
 * @author Kimi Liu
 * @since Java 21+
 */
public record SloEvent(String sloName, double target, double compliance, double budgetRemaining, double burnRate,
        Instant detectedAt) {
}
