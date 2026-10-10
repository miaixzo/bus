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

import org.miaixz.bus.metrics.magic.*;
import org.miaixz.bus.metrics.nimble.InstrumentKind;
import org.miaixz.bus.metrics.nimble.MetricDescriptor;
import org.miaixz.bus.metrics.observe.tag.Attributes;

/**
 * Dependency-free encoder for Prometheus text exposition format 0.0.4.
 *
 * @author Kimi Liu
 */
public final class NativePrometheusTextEncoder {

    /**
     * Creates a dependency-free Prometheus text encoder.
     */
    public NativePrometheusTextEncoder() {
        super();
    }

    /**
     * Normalizes a metric or label name to the Prometheus identifier grammar.
     *
     * @param value source name
     * @return normalized name
     * @throws IllegalArgumentException if the source name is blank
     */
    public static String normalizeName(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Prometheus name must not be blank");
        }
        StringBuilder normalized = new StringBuilder(value.length() + 1);
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            boolean allowed = character == '_' || character == ':' || Character.isLetterOrDigit(character);
            normalized.append(allowed ? character : '_');
        }
        char first = normalized.charAt(0);
        if (!(first == '_' || first == ':' || Character.isLetter(first))) {
            normalized.insert(0, '_');
        }
        return normalized.toString();
    }

    /**
     * Appends metadata and samples for one native family.
     *
     * @param output destination buffer
     * @param family metric family
     */
    private static void appendFamily(StringBuilder output, MetricFamilySnapshot family) {
        MetricDescriptor descriptor = family.descriptor();
        String name = exportName(descriptor);
        String type = type(descriptor.kind());
        output.append("# HELP ").append(name).append(' ')
                .append(escapeHelp(descriptor.description().isEmpty() ? descriptor.name() : descriptor.description()))
                .append('\n');
        output.append("# TYPE ").append(name).append(' ').append(type).append('\n');

        List<MetricPoint> points = new ArrayList<>(family.points());
        points.sort(Comparator.comparing(point -> point.attributes().toString()));
        for (MetricPoint point : points) {
            if (point instanceof LongMetricPoint longPoint) {
                appendSample(output, name, longPoint.attributes(), null, Long.toString(longPoint.value()));
            } else if (point instanceof DoubleMetricPoint doublePoint) {
                appendSample(output, name, doublePoint.attributes(), null, formatDouble(doublePoint.value()));
            } else if (point instanceof DistributionMetricPoint distribution) {
                appendDistribution(output, name, distribution);
            }
        }
    }

    /**
     * Appends classic histogram buckets, sum, and count.
     *
     * @param output destination buffer
     * @param name   exported family name
     * @param point  distribution point
     */
    private static void appendDistribution(StringBuilder output, String name, DistributionMetricPoint point) {
        boolean infiniteBucket = false;
        for (HistogramBucket bucket : point.buckets()) {
            String bound = bucket.upperBound() == Double.POSITIVE_INFINITY ? "+Inf" : formatDouble(bucket.upperBound());
            appendSample(
                    output,
                    name + "_bucket",
                    point.attributes(),
                    "le=\"" + bound + "\"",
                    Long.toString(bucket.cumulativeCount()));
            infiniteBucket = bucket.upperBound() == Double.POSITIVE_INFINITY;
        }
        if (!infiniteBucket) {
            appendSample(output, name + "_bucket", point.attributes(), "le=\"+Inf\"", Long.toString(point.count()));
        }
        appendSample(output, name + "_sum", point.attributes(), null, formatDouble(point.sum()));
        appendSample(output, name + "_count", point.attributes(), null, Long.toString(point.count()));
    }

    /**
     * Appends one sample with stable label ordering.
     *
     * @param output     destination buffer
     * @param name       exported sample name
     * @param attributes sample attributes
     * @param extraLabel optional synthetic label
     * @param value      encoded sample value
     */
    private static void appendSample(
            StringBuilder output,
            String name,
            Attributes attributes,
            String extraLabel,
            String value) {
        output.append(name);
        if (!attributes.isEmpty() || extraLabel != null) {
            output.append('{');
            boolean comma = false;
            for (Attributes.Value<?> attribute : attributes.values()) {
                if (comma) {
                    output.append(',');
                }
                output.append(normalizeName(attribute.descriptor().key())).append("=\"")
                        .append(escapeLabel(String.valueOf(attribute.value()))).append('"');
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
     * Applies Prometheus naming, unit suffix, and counter suffix rules.
     *
     * @param descriptor family descriptor
     * @return exported family name
     */
    public static String exportName(MetricDescriptor descriptor) {
        String name = normalizeName(descriptor.name());
        String unit = normalizeUnit(descriptor.unit());
        if (!unit.isEmpty() && !name.endsWith("_" + unit)) {
            name = name + "_" + unit;
        }
        if (descriptor.kind() == InstrumentKind.COUNTER && !name.endsWith("_total")) {
            name = name + "_total";
        }
        return name;
    }

    /**
     * Maps a metric unit to a Prometheus base-unit suffix.
     * <p>
     * OpenTelemetry annotation units such as {@code {thread}} and {@code {operation}} describe the measured item and
     * therefore do not become name suffixes.
     *
     * @param unit metric unit
     * @return normalized suffix, or an empty string when no suffix applies
     */
    private static String normalizeUnit(String unit) {
        if (unit.isEmpty() || "1".equals(unit)) {
            return "";
        }
        if (unit.startsWith("{") && unit.endsWith("}")) {
            return "";
        }
        return switch (unit) {
            case "s" -> "seconds";
            case "By" -> "bytes";
            default -> normalizeName(unit).replace(':', '_');
        };
    }

    /**
     * Maps a Bus instrument kind to a Prometheus family type.
     *
     * @param kind instrument kind
     * @return Prometheus type name
     */
    private static String type(InstrumentKind kind) {
        return switch (kind) {
            case COUNTER -> "counter";
            case TIMER, HISTOGRAM -> "histogram";
            case UP_DOWN_COUNTER, GAUGE -> "gauge";
        };
    }

    /**
     * Formats a floating-point sample using Prometheus infinity tokens.
     *
     * @param value sample value
     * @return encoded value
     */
    static String formatDouble(double value) {
        if (value == Double.POSITIVE_INFINITY) {
            return "+Inf";
        }
        if (value == Double.NEGATIVE_INFINITY) {
            return "-Inf";
        }
        return Double.toString(value);
    }

    /**
     * Escapes text for a Prometheus HELP line.
     *
     * @param value source text
     * @return escaped text
     */
    static String escapeHelp(String value) {
        return value.replace("\\", "\\\\").replace("\n", "\\n");
    }

    /**
     * Escapes a Prometheus label value.
     *
     * @param value source value
     * @return escaped label value
     */
    static String escapeLabel(String value) {
        return escapeHelp(value).replace("\"", "\\\"");
    }

    /**
     * Encodes a snapshot with deterministic family, series, and label ordering.
     *
     * @param snapshot snapshot to encode
     * @return Prometheus text exposition
     */
    public String encode(MetricSnapshot snapshot) {
        List<MetricFamilySnapshot> families = new ArrayList<>(snapshot.families());
        families.sort(
                Comparator.comparing((MetricFamilySnapshot family) -> exportName(family.descriptor()))
                        .thenComparing(family -> family.descriptor().scope()));
        StringBuilder output = new StringBuilder();
        for (MetricFamilySnapshot family : families) {
            appendFamily(output, family);
        }
        return output.toString();
    }

}
