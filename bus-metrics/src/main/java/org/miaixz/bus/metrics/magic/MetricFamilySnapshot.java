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

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.miaixz.bus.metrics.nimble.InstrumentKind;
import org.miaixz.bus.metrics.nimble.MetricDescriptor;
import org.miaixz.bus.metrics.nimble.NumberKind;
import org.miaixz.bus.metrics.observe.tag.Attributes;

/**
 * Immutable snapshot of all resource series in one metric family.
 *
 * @param descriptor family schema and metadata
 * @param points     resource data points
 * @author Kimi Liu
 */
public record MetricFamilySnapshot(MetricDescriptor descriptor, List<MetricPoint> points) {

    /**
     * Validates and copies the family snapshot.
     */
    public MetricFamilySnapshot {
        descriptor = Objects.requireNonNull(descriptor, "Metric descriptor must not be null");
        points = List.copyOf(Objects.requireNonNull(points, "Metric points must not be null"));
        Set<Attributes> identities = new HashSet<>();
        for (MetricPoint point : points) {
            MetricPoint checked = Objects.requireNonNull(point, "Metric point must not be null");
            descriptor.validateAttributes(checked.attributes());
            validatePointType(descriptor, checked);
            if (!identities.add(checked.attributes())) {
                throw new IllegalArgumentException("Duplicate attributes in metric family " + descriptor.name());
            }
        }
    }

    /**
     * Verifies that a point representation matches its family schema.
     *
     * @param descriptor family descriptor
     * @param point      point to validate
     */
    private static void validatePointType(MetricDescriptor descriptor, MetricPoint point) {
        boolean distribution = descriptor.kind() == InstrumentKind.TIMER
                || descriptor.kind() == InstrumentKind.HISTOGRAM;
        if (distribution) {
            if (!(point instanceof DistributionMetricPoint) || descriptor.numberKind() != NumberKind.DOUBLE) {
                throw new IllegalArgumentException(
                        "Distribution family requires double distribution points: " + descriptor.name());
            }
            return;
        }
        if (point instanceof DistributionMetricPoint
                || descriptor.numberKind() == NumberKind.LONG && !(point instanceof LongMetricPoint)
                || descriptor.numberKind() == NumberKind.DOUBLE && !(point instanceof DoubleMetricPoint)) {
            throw new IllegalArgumentException("Metric point type does not match descriptor: " + descriptor.name());
        }
    }

}
