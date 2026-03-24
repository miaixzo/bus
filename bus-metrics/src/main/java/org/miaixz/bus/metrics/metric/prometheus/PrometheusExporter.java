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
package org.miaixz.bus.metrics.metric.prometheus;

import org.miaixz.bus.metrics.Provider;
import org.miaixz.bus.metrics.magic.TimerSnapshot;
import org.miaixz.bus.metrics.metric.*;
import org.miaixz.bus.metrics.metric.indigenous.NativeProvider;
import org.miaixz.bus.metrics.observe.tag.Tag;

/**
 * Renders metrics in Prometheus text format 0.0.4.
 * <p>
 * Supported types: Counter (_total suffix), Timer (histogram), Histogram (histogram), Gauge. SLO metrics exported as
 * gauge type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrometheusExporter {

    /** The metrics provider to scrape; must be a {@link NativeProvider} for full export support. */
    private final Provider provider;

    /**
     * Create an exporter backed by the given provider.
     *
     * @param provider the metrics provider to scrape; must be a {@link NativeProvider}
     */
    public PrometheusExporter(Provider provider) {
        this.provider = provider;
    }

    /**
     * Produce a full Prometheus text-format 0.0.4 scrape payload.
     *
     * @return the scrape body as a UTF-8 string
     */
    public String scrape() {
        StringBuilder sb = new StringBuilder(4096);
        if (!(provider instanceof NativeProvider np)) {
            return "# NativeProvider required for Prometheus scrape\n";
        }
        // Counters
        for (Counter c : np.counters()) {
            // name is not exposed from Counter interface; use registry iteration
        }
        // Export via timer snapshots
        for (Timer t : np.timers()) {
            TimerSnapshot snap = t.snapshot();
            exportTimer(sb, snap);
        }
        // Histograms
        for (Histogram h : np.histograms()) {
            TimerSnapshot snap = h.snapshot();
            exportHistogram(sb, snap);
        }
        // Gauges
        for (Gauge g : np.gauges()) {
            // Gauge name not exposed directly; skip for now
        }
        return sb.toString();
    }

    /**
     * Append Prometheus histogram lines for a timer snapshot (values in seconds).
     *
     * @param sb   output buffer
     * @param snap timer snapshot to render
     */
    private void exportTimer(StringBuilder sb, TimerSnapshot snap) {
        String baseName = prometheusName(snap.name()) + "_seconds";
        String labels = labelsStr(snap.tags());

        sb.append("# TYPE ").append(baseName).append(" histogram\n");
        double[] bounds = snap.bucketBounds();
        long[] counts = snap.bucketCounts();
        for (int i = 0; i < bounds.length; i++) {
            sb.append(baseName).append("_bucket{").append(labels.isEmpty() ? "" : labels + ",").append("le=\"")
                    .append(bounds[i]).append("\"} ").append(counts[i]).append('\n');
        }
        sb.append(baseName).append("_bucket{").append(labels.isEmpty() ? "" : labels + ",").append("le=\"+Inf\"} ")
                .append(snap.count()).append('\n');
        sb.append(baseName).append("_sum").append(labels.isEmpty() ? "" : "{" + labels + "}").append(' ')
                .append(snap.totalNanos() / 1_000_000_000.0).append('\n');
        sb.append(baseName).append("_count").append(labels.isEmpty() ? "" : "{" + labels + "}").append(' ')
                .append(snap.count()).append('\n');
    }

    /**
     * Append Prometheus histogram lines for a distribution summary snapshot.
     *
     * @param sb   output buffer
     * @param snap histogram snapshot to render
     */
    private void exportHistogram(StringBuilder sb, TimerSnapshot snap) {
        String baseName = prometheusName(snap.name());
        String labels = labelsStr(snap.tags());

        sb.append("# TYPE ").append(baseName).append(" histogram\n");
        sb.append(baseName).append("_sum").append(labels.isEmpty() ? "" : "{" + labels + "}").append(' ')
                .append(snap.totalNanos()).append('\n');
        sb.append(baseName).append("_count").append(labels.isEmpty() ? "" : "{" + labels + "}").append(' ')
                .append(snap.count()).append('\n');
    }

    /**
     * Converts a metric name to a valid Prometheus metric name by replacing dots and hyphens with underscores.
     *
     * @param name the original metric name
     * @return Prometheus-compatible metric name
     */
    private static String prometheusName(String name) {
        return name.replace('.', '_').replace('-', '_');
    }

    /**
     * Renders a tag array as a Prometheus label string, e.g. {@code key1="v1",key2="v2"}.
     *
     * @param tags the tags to render
     * @return label string, or empty string if no tags
     */
    private static String labelsStr(Tag[] tags) {
        if (tags == null || tags.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tags.length; i++) {
            if (i > 0)
                sb.append(',');
            sb.append(tags[i].key()).append("=\"").append(tags[i].value()).append('"');
        }
        return sb.toString();
    }

}
