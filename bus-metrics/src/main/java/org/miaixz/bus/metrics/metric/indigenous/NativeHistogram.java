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

import org.miaixz.bus.metrics.metric.Histogram;
import org.miaixz.bus.metrics.magic.TimerSnapshot;
import org.miaixz.bus.metrics.observe.tag.Tag;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;

/**
 * Histogram backed by T-Digest for accurate quantile estimation.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NativeHistogram implements Histogram {

    /**
     * Metric name used in snapshots and registry keys.
     */
    private final String name;
    /**
     * Tags associated with this histogram instance.
     */
    private final Tag[] tags;
    /**
     * T-Digest for accurate quantile estimation over the lifetime of this histogram.
     */
    private final TDigest digest = new TDigest();
    /**
     * Total number of recorded values.
     */
    private final AtomicLong countTotal = new AtomicLong();
    /**
     * Running sum of all recorded values.
     */
    private final DoubleAdder sumTotal = new DoubleAdder();
    /**
     * Maximum recorded value; updated on each {@link #record(double)} call.
     */
    private volatile double maxValue = Double.NEGATIVE_INFINITY;

    /**
     * Create a histogram with the given name and tags.
     *
     * @param name metric name
     * @param tags associated tags
     */
    public NativeHistogram(String name, Tag[] tags) {
        this.name = name;
        this.tags = tags;
    }

    /**
     * Record a single observed value.
     *
     * @param value the observed value (e.g. response size in bytes)
     */
    @Override
    public void record(double value) {
        countTotal.incrementAndGet();
        sumTotal.add(value);
        digest.add(value);
        synchronized (this) {
            if (value > maxValue) {
                maxValue = value;
            }
        }
    }

    /** Returns the total number of recorded values. */
    @Override
    public long count() {
        return countTotal.get();
    }

    /** Returns the sum of all recorded values. */
    @Override
    public double totalAmount() {
        return sumTotal.sum();
    }

    /** Returns the maximum recorded value. */
    @Override
    public double max() {
        return maxValue;
    }

    /**
     * Returns the estimated percentile value over the lifetime of this histogram.
     *
     * @param p percentile 0.0–1.0 (e.g. 0.99 for P99)
     * @return estimated percentile value
     */
    @Override
    public double percentile(double p) {
        return digest.quantile(p);
    }

    /** Returns an atomic snapshot for cross-instance aggregation. */
    @Override
    public TimerSnapshot snapshot() {
        return new TimerSnapshot(name, tags, countTotal.get(), sumTotal.sum(), maxValue, new long[0], new double[0]);
    }

}
