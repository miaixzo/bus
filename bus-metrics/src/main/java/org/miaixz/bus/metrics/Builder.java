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
package org.miaixz.bus.metrics;

/**
 * Shared constants for bus-metrics.
 * <p>
 * Centralises metric names, tag keys, scheduler parameters, histogram bucket boundaries, EWMA alpha values, and
 * cardinality defaults so that all classes reference a single source of truth.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class Builder {

    // ── Scheduler ─────────────────────────────────────────────────────────

    /**
     * Background tick interval in seconds (EWMA + window rotation).
     */
    public static final int TICK_INTERVAL_SECONDS = 5;

    /**
     * Number of ticks before rotating the 1-minute digest window (60 s / 5 s = 12).
     */
    public static final int TICKS_PER_1M = 12;

    /**
     * Number of ticks before rotating the 5-minute digest window (300 s / 5 s = 60).
     */
    public static final int TICKS_PER_5M = 60;

    /**
     * Daemon thread name for the native provider tick scheduler.
     */
    public static final String THREAD_NAME_TICK = "bus-metrics-tick";

    /**
     * Daemon thread name for the CortexExporter push scheduler.
     */
    public static final String THREAD_NAME_CORTEX = "bus-metrics-cortex-exporter";

    /**
     * Daemon thread name for the HealthMetrics CPU refresh scheduler.
     */
    public static final String THREAD_NAME_HEALTH = "bus-metrics-health-refresh";

    // ── EWMA alpha values ─────────────────────────────────────────────────

    /**
     * 1-minute EWMA alpha: {@code 1 - exp(-5/60)}.
     */
    public static final double EWMA_M1_ALPHA = 1 - Math.exp(-5.0 / 60);

    /**
     * 5-minute EWMA alpha: {@code 1 - exp(-5/300)}.
     */
    public static final double EWMA_M5_ALPHA = 1 - Math.exp(-5.0 / 300);

    /**
     * 15-minute EWMA alpha: {@code 1 - exp(-5/900)}.
     */
    public static final double EWMA_M15_ALPHA = 1 - Math.exp(-5.0 / 900);

    // ── Histogram bucket boundaries ───────────────────────────────────────

    /**
     * Standard Prometheus-compatible histogram bucket boundaries in seconds. Used by {@code NativeTimer} for
     * bucket-count tracking.
     */
    public static final double[] HISTOGRAM_BUCKET_BOUNDS_SECS = { 0.001, 0.005, 0.01, 0.025, 0.05, 0.075, 0.1, 0.25,
            0.5, 0.75, 1.0, 2.5, 5.0, 10.0 };

    // ── Cardinality defaults ──────────────────────────────────────────────

    /**
     * Default maximum distinct tag values per key before cardinality guard kicks in.
     */
    public static final int CARDINALITY_DEFAULT_MAX = 100;

    /**
     * Minimum interval between cardinality violation log messages per tag key (ms).
     */
    public static final long CARDINALITY_LOG_THROTTLE_MS = 60_000L;

    // ── CortexExporter ────────────────────────────────────────────────────

    /**
     * CacheX key prefix for metric snapshots pushed by {@code CortexExporter}.
     */
    public static final String CORTEX_KEY_PREFIX = "metrics:";

    /**
     * TTL multiplier: snapshot TTL = {@code intervalSeconds * CORTEX_TTL_MULTIPLIER * 1000} ms.
     */
    public static final int CORTEX_TTL_MULTIPLIER = 4;

    // ── HealthMetrics ─────────────────────────────────────────────────────

    /**
     * Default CPU/hardware refresh interval in seconds for {@code HealthMetrics}.
     */
    public static final int HEALTH_DEFAULT_REFRESH_SECONDS = 5;

    // ── HttpMetrics ───────────────────────────────────────────────────────

    /**
     * Servlet request attribute key used to store the in-flight timer sample.
     */
    public static final String HTTP_ATTR_SAMPLE = "bus.metrics.http.sample";

    /**
     * Maximum URI length before truncation to prevent cardinality explosion.
     */
    public static final int HTTP_URI_MAX_LENGTH = 100;

    /**
     * Metric name for HTTP server request timers.
     */
    public static final String HTTP_SERVER_REQUESTS = "http.server.requests";

    /**
     * Metric name for HTTP server request rate meters.
     */
    public static final String HTTP_SERVER_REQUESTS_RATE = "http.server.requests.rate";

    // ── Metric name suffixes (LlmTimer) ───────────────────────────────────

    /**
     * Suffix for total call duration timer.
     */
    public static final String LLM_SUFFIX_DURATION = ".call.duration";

    /**
     * Suffix for time-to-first-token timer.
     */
    public static final String LLM_SUFFIX_TTFT = ".call.ttft";

    /**
     * Suffix for inter-token latency timer.
     */
    public static final String LLM_SUFFIX_ITL = ".call.itl";

    /**
     * Suffix for token usage counter.
     */
    public static final String LLM_SUFFIX_TOKENS = ".tokens";

    /**
     * Suffix for cost counter (stored as micro-dollars: USD * 1_000_000).
     */
    public static final String LLM_SUFFIX_COST = ".cost.usd";

    /**
     * Suffix for error counter.
     */
    public static final String LLM_SUFFIX_ERRORS = ".errors";

    /**
     * Cost scale factor: USD stored as {@code cost * LLM_COST_SCALE} long.
     */
    public static final long LLM_COST_SCALE = 1_000_000L;

    // ── Tag keys ──────────────────────────────────────────────────────────

    /**
     * Tag key for LLM model name.
     */
    public static final String TAG_MODEL = "model";

    /**
     * Tag key for LLM/service provider name.
     */
    public static final String TAG_PROVIDER = "provider";

    /**
     * Tag key for operation name.
     */
    public static final String TAG_OPERATION = "operation";

    /**
     * Tag key for finish/stop reason.
     */
    public static final String TAG_FINISH_REASON = "finish_reason";

    /**
     * Tag key for token type (input/output).
     */
    public static final String TAG_TYPE = "type";

    /**
     * Tag key for error type (exception class name).
     */
    public static final String TAG_ERROR_TYPE = "error_type";

    /**
     * Tag key for HTTP method.
     */
    public static final String TAG_METHOD = "method";

    /**
     * Tag key for HTTP URI.
     */
    public static final String TAG_URI = "uri";

    /**
     * Tag key for HTTP status code.
     */
    public static final String TAG_STATUS = "status";

    /**
     * Tag key for exception class name in HTTP metrics.
     */
    public static final String TAG_EXCEPTION = "exception";

    /**
     * Tag key for cache name.
     */
    public static final String TAG_CACHE = "cache";

    /**
     * Tag key for cache result (hit/miss).
     */
    public static final String TAG_RESULT = "result";

    /**
     * Tag key for GC collector name.
     */
    public static final String TAG_GC = "gc";

    // ── Instrumentation scope ─────────────────────────────────────────────

    /**
     * Default OpenTelemetry instrumentation scope name.
     */
    public static final String OTEL_SCOPE = "bus-metrics";

    /** Private constructor; this is a static constants class. */
    private Builder() {
    }

}
