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
package org.miaixz.bus.metrics.magic;

import java.util.List;
import java.util.Objects;

import org.miaixz.bus.metrics.observe.tag.Attributes;

/**
 * Immutable timer or histogram distribution data point.
 *
 * @param attributes resource attributes
 * @param count      observation count
 * @param sum        sum of observed values
 * @param max        maximum observed value
 * @param buckets    cumulative histogram buckets in ascending bound order
 * @author Kimi Liu
 */
public record DistributionMetricPoint(Attributes attributes, long count, double sum, double max,
        List<HistogramBucket> buckets) implements MetricPoint {

    /**
     * Validates and copies the distribution.
     */
    public DistributionMetricPoint {
        attributes = Objects.requireNonNull(attributes, "Metric attributes must not be null");
        if (count < 0) {
            throw new IllegalArgumentException("Distribution count must be non-negative");
        }
        if (!Double.isFinite(sum) || !Double.isFinite(max)) {
            throw new IllegalArgumentException("Distribution sum and max must be finite");
        }
        buckets = List.copyOf(Objects.requireNonNull(buckets, "Histogram buckets must not be null"));

        double previousBound = Double.NEGATIVE_INFINITY;
        long previousCount = 0;
        for (int i = 0; i < buckets.size(); i++) {
            HistogramBucket bucket = Objects.requireNonNull(buckets.get(i), "Histogram bucket must not be null");
            if (bucket.upperBound() <= previousBound) {
                throw new IllegalArgumentException("Histogram bucket bounds must be strictly increasing");
            }
            if (Double.isInfinite(bucket.upperBound()) && i != buckets.size() - 1) {
                throw new IllegalArgumentException("Infinite histogram bucket must be terminal");
            }
            if (bucket.cumulativeCount() < previousCount || bucket.cumulativeCount() > count) {
                throw new IllegalArgumentException("Histogram bucket counts must be cumulative and not exceed count");
            }
            previousBound = bucket.upperBound();
            previousCount = bucket.cumulativeCount();
        }
    }

}
