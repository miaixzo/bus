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

import org.miaixz.bus.metrics.magic.TimerSnapshot;

/**
 * A histogram (distribution summary) for recording arbitrary numeric values.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Histogram {

    /**
     * Record a single observed value.
     *
     * @param value the observed value (e.g. response size in bytes)
     */
    void record(double value);

    /**
     * Returns the total number of recorded values.
     */
    long count();

    /**
     * Returns the sum of all recorded values.
     */
    double totalAmount();

    /**
     * Returns the maximum recorded value.
     */
    double max();

    /**
     * Returns the estimated percentile value over the lifetime of this histogram.
     *
     * @param p percentile 0.0–1.0 (e.g. 0.99 for P99)
     * @return estimated percentile value
     */
    double percentile(double p);

    /** Returns an atomic snapshot for cross-instance aggregation. */
    TimerSnapshot snapshot();

}
