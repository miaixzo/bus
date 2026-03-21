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
 * A counter with in-process EWMA rate calculation (1m/5m/15m), similar to Dropwizard Metrics' Meter and Linux kernel
 * load average.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Meter extends Counter {

    /**
     * Rolling 1-minute average event rate (events/second).
     */
    double oneMinuteRate();

    /**
     * Rolling 5-minute average event rate (events/second).
     */
    double fiveMinuteRate();

    /**
     * Rolling 15-minute average event rate (events/second).
     */
    double fifteenMinuteRate();

    /**
     * Mean rate since creation (events/second).
     */
    double meanRate();

}
