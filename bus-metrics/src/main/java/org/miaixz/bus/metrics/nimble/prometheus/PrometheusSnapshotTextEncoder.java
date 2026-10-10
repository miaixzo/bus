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
package org.miaixz.bus.metrics.nimble.prometheus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import io.prometheus.metrics.model.snapshots.*;

/**
 * Core-only Prometheus snapshot encoder for text exposition format 0.0.4.
 *
 * @author Kimi Liu
 */
public final class PrometheusSnapshotTextEncoder {

    /**
     * Creates a Prometheus snapshot text encoder.
     */
    public PrometheusSnapshotTextEncoder() {
        // No initialization required.
    }

    /**
     * Appends metadata and samples for one Prometheus model family.
     *
     * @param output destination buffer
     * @param family metric family
     */
    private static void appendFamily(StringBuilder output, MetricSnapshot family) {
        String name = family.getMetadata().getPrometheusName();
        String type = type(family);
        String help = family.getMetadata().getHelp();
        output.append("# HELP ").append(name).append(' ')
                .append(NativePrometheusTextEncoder.escapeHelp(help == null ? name : help)).append('\n');
        output.append("# TYPE ").append(name).append(' ').append(type).append('\n');
        if (family instanceof CounterSnapshot counter) {
            String counterName = name.endsWith("_total") ? name : name + "_total";
            sorted(counter.getDataPoints()).forEach(
                    point -> appendSample(
                            output,
                            counterName,
                            point.getLabels(),
                            null,
                            NativePrometheusTextEncoder.formatDouble(point.getValue())));
        } else if (family instanceof GaugeSnapshot gauge) {
            sorted(gauge.getDataPoints()).forEach(
                    point -> appendSample(
                            output,
                            name,
                            point.getLabels(),
                            null,
                            NativePrometheusTextEncoder.formatDouble(point.getValue())));
        } else if (family instanceof HistogramSnapshot histogram) {
            sorted(histogram.getDataPoints()).forEach(point -> appendHistogram(output, name, point));
        } else if (family instanceof SummarySnapshot summary) {
            sorted(summary.getDataPoints()).forEach(point -> appendSummary(output, name, point));
        } else if (family instanceof InfoSnapshot info) {
            sorted(info.getDataPoints()).forEach(point -> appendSample(output, name, point.getLabels(), null, "1"));
        } else if (family instanceof StateSetSnapshot stateSet) {
            sorted(stateSet.getDataPoints()).forEach(
                    point -> point.stream().sorted(Comparator.comparing(StateSetSnapshot.State::getName)).forEach(
                            state -> appendSample(
                                    output,
                                    name,
                                    point.getLabels(),
                                    name + "=\"" + NativePrometheusTextEncoder.escapeLabel(state.getName()) + "\"",
                                    state.isTrue() ? "1" : "0")));
        }
    }

    /**
     * Appends classic histogram buckets, sum, and count.
     *
     * @param output destination buffer
     * @param name   exported family name
     * @param point  histogram point
     */
    private static void appendHistogram(
            StringBuilder output,
            String name,
            HistogramSnapshot.HistogramDataPointSnapshot point) {
        boolean infinity = false;
        long cumulative = 0;
        if (point.hasClassicHistogramData()) {
            for (ClassicHistogramBucket bucket : point.getClassicBuckets()) {
                cumulative = saturatedAdd(cumulative, bucket.getCount());
                String bound = bucket.getUpperBound() == Double.POSITIVE_INFINITY ? "+Inf"
                        : NativePrometheusTextEncoder.formatDouble(bucket.getUpperBound());
                appendSample(
                        output,
                        name + "_bucket",
                        point.getLabels(),
                        "le=\"" + bound + "\"",
                        Long.toString(cumulative));
                infinity = bucket.getUpperBound() == Double.POSITIVE_INFINITY;
            }
        }
        if (!infinity) {
            appendSample(output, name + "_bucket", point.getLabels(), "le=\"+Inf\"", Long.toString(point.getCount()));
        }
        appendSample(
                output,
                name + "_sum",
                point.getLabels(),
                null,
                NativePrometheusTextEncoder.formatDouble(point.getSum()));
        appendSample(output, name + "_count", point.getLabels(), null, Long.toString(point.getCount()));
    }

    /**
     * Appends ordered summary quantiles, sum, and count.
     *
     * @param output destination buffer
     * @param name   exported family name
     * @param point  summary point
     */
    private static void appendSummary(
            StringBuilder output,
            String name,
            SummarySnapshot.SummaryDataPointSnapshot point) {
        List<Quantile> quantiles = new ArrayList<>();
        point.getQuantiles().forEach(quantiles::add);
        quantiles.sort(Comparator.comparingDouble(Quantile::getQuantile));
        for (Quantile quantile : quantiles) {
            appendSample(
                    output,
                    name,
                    point.getLabels(),
                    "quantile=\"" + NativePrometheusTextEncoder.formatDouble(quantile.getQuantile()) + "\"",
                    NativePrometheusTextEncoder.formatDouble(quantile.getValue()));
        }
        appendSample(
                output,
                name + "_sum",
                point.getLabels(),
                null,
                NativePrometheusTextEncoder.formatDouble(point.getSum()));
        appendSample(output, name + "_count", point.getLabels(), null, Long.toString(point.getCount()));
    }

    /**
     * Appends one sample with stable label ordering.
     *
     * @param output     destination buffer
     * @param name       exported sample name
     * @param labels     sample labels
     * @param extraLabel optional synthetic label
     * @param value      encoded sample value
     */
    private static void appendSample(
            StringBuilder output,
            String name,
            Labels labels,
            String extraLabel,
            String value) {
        output.append(name);
        if (!labels.isEmpty() || extraLabel != null) {
            output.append('{');
            List<Integer> indexes = new ArrayList<>(labels.size());
            for (int i = 0; i < labels.size(); i++) {
                indexes.add(i);
            }
            indexes.sort(Comparator.comparing(labels::getPrometheusName));
            boolean comma = false;
            for (int index : indexes) {
                if (comma) {
                    output.append(',');
                }
                output.append(labels.getPrometheusName(index)).append("=\"")
                        .append(NativePrometheusTextEncoder.escapeLabel(labels.getValue(index))).append('"');
                comma = true;
            }
            if (extraLabel != null) {
                if (comma) {
                    output.append(',');
                }
                output.append(extraLabel);
            }
            output.append('}');
        }
        output.append(' ').append(value).append('\n');
    }

    /**
     * Sorts data points by their stable label representation.
     *
     * @param points points to order
     * @param <T>    point type
     * @return ordered immutable list
     */
    private static <T extends DataPointSnapshot> List<T> sorted(List<T> points) {
        return points.stream().sorted(Comparator.comparing(point -> point.getLabels().toString())).toList();
    }

    /**
     * Maps a Prometheus model snapshot to its exposition type.
     *
     * @param snapshot family snapshot
     * @return exposition type name
     */
    private static String type(MetricSnapshot snapshot) {
        if (snapshot instanceof CounterSnapshot) {
            return "counter";
        }
        if (snapshot instanceof GaugeSnapshot) {
            return "gauge";
        }
        if (snapshot instanceof HistogramSnapshot) {
            return "histogram";
        }
        if (snapshot instanceof SummarySnapshot) {
            return "summary";
        }
        if (snapshot instanceof InfoSnapshot) {
            return "info";
        }
        if (snapshot instanceof StateSetSnapshot) {
            return "stateset";
        }
        throw new IllegalArgumentException("Unsupported Prometheus snapshot type: " + snapshot.getClass().getName());
    }

    /**
     * Adds bucket counts without overflowing.
     *
     * @param left  cumulative count
     * @param right bucket count
     * @return saturated sum
     */
    private static long saturatedAdd(long left, long right) {
        return left > Long.MAX_VALUE - right ? Long.MAX_VALUE : left + right;
    }

    /**
     * Encodes registry snapshots using stable family, point, and label ordering.
     *
     * @param snapshots Prometheus model snapshots
     * @return text exposition
     */
    public String encode(MetricSnapshots snapshots) {
        List<MetricSnapshot> families = snapshots.stream()
                .sorted(Comparator.comparing(value -> value.getMetadata().getPrometheusName())).toList();
        StringBuilder output = new StringBuilder();
        for (MetricSnapshot family : families) {
            appendFamily(output, family);
        }
        return output.toString();
    }

}
