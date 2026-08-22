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
package org.miaixz.bus.fabric.network.dns.observe;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.dns.forward.DnsUpstream;
import org.miaixz.bus.fabric.network.dns.message.DnsResponseCode;
import org.miaixz.bus.fabric.network.dns.server.DnsTransport;
import org.miaixz.bus.fabric.observe.metrics.FabricMeter;

/**
 * DNS Server metric facade with fixed {@code dns.server.*} metric names.
 *
 * @author Kimi Liu
 */
public class DnsMetrics {

    /**
     * DNS Server metric prefix.
     */
    public static final String PREFIX = "dns.server.";

    /**
     * Total DNS query counter.
     */
    public static final String QUERY_COUNT = PREFIX + "query.count";

    /**
     * DNS response-code metric prefix.
     */
    public static final String RCODE_PREFIX = PREFIX + "rcode.";

    /**
     * DNS transport metric prefix.
     */
    public static final String TRANSPORT_PREFIX = PREFIX + "transport.";

    /**
     * DNS response-cache hit counter.
     */
    public static final String CACHE_HIT_COUNT = PREFIX + "cache.hit.count";

    /**
     * DNS response-cache miss counter.
     */
    public static final String CACHE_MISS_COUNT = PREFIX + "cache.miss.count";

    /**
     * DNS response-cache stale-hit counter.
     */
    public static final String CACHE_STALE_COUNT = PREFIX + "cache.stale.count";

    /**
     * DNS upstream forwarding latency metric.
     */
    public static final String FORWARD_UPSTREAM_LATENCY = PREFIX + "forward.upstream.latency";

    /**
     * DNS recursive resolution latency metric.
     */
    public static final String RECURSIVE_LATENCY = PREFIX + "recursive.latency";

    /**
     * DNSSEC validation-result metric prefix.
     */
    public static final String DNSSEC_PREFIX = PREFIX + "dnssec.";

    /**
     * DNS rate-limit drop counter.
     */
    public static final String RATE_LIMIT_DROP_COUNT = PREFIX + "rate_limit.drop.count";

    /**
     * Disabled metrics singleton.
     */
    private static final DnsMetrics DISABLED = new DnsMetrics(false, FabricMeter.create());

    /**
     * Whether metric recording is enabled.
     */
    private final boolean enabled;

    /**
     * Fabric meter that owns counters and timings.
     */
    private final FabricMeter meter;

    /**
     * Creates metrics.
     *
     * @param enabled whether recording is enabled
     * @param meter   target fabric meter
     */
    public DnsMetrics(final boolean enabled, final FabricMeter meter) {
        if (meter == null) {
            throw new ValidateException("DNS metrics meter must not be null");
        }
        this.enabled = enabled;
        this.meter = meter;
    }

    /**
     * Returns the disabled metrics singleton.
     *
     * @return disabled metrics facade
     */
    public static DnsMetrics disabled() {
        return DISABLED;
    }

    /**
     * Creates an enabled metrics facade with a new meter.
     *
     * @return enabled metrics facade
     */
    public static DnsMetrics create() {
        return create(FabricMeter.create());
    }

    /**
     * Creates an enabled metrics facade with an explicit meter.
     *
     * @param meter target fabric meter
     * @return enabled metrics facade
     */
    public static DnsMetrics create(final FabricMeter meter) {
        return new DnsMetrics(true, meter);
    }

    /**
     * Returns whether metric recording is enabled.
     *
     * @return true when metrics are enabled
     */
    public boolean enabled() {
        return enabled;
    }

    /**
     * Returns the underlying meter.
     *
     * @return fabric meter
     */
    public FabricMeter meter() {
        return meter;
    }

    /**
     * Returns a weakly consistent metric snapshot.
     *
     * @return metric snapshot from the underlying meter
     */
    public Map<String, Long> snapshot() {
        return meter.snapshot();
    }

    /**
     * Clears all metric values in the underlying meter.
     */
    public void reset() {
        meter.reset();
    }

    /**
     * Records one DNS query and its transport.
     *
     * @param transport listener transport
     */
    public void query(final DnsTransport transport) {
        if (!enabled) {
            return;
        }
        increment(QUERY_COUNT);
        increment(TRANSPORT_PREFIX + transportName(transport) + ".count");
    }

    /**
     * Records one DNS response code.
     *
     * @param responseCode response code
     */
    public void responseCode(final DnsResponseCode responseCode) {
        if (!enabled) {
            return;
        }
        increment(RCODE_PREFIX + rcodeName(responseCode) + ".count");
    }

    /**
     * Records a response-cache hit.
     */
    public void cacheHit() {
        if (enabled) {
            increment(CACHE_HIT_COUNT);
        }
    }

    /**
     * Records a response-cache miss.
     */
    public void cacheMiss() {
        if (enabled) {
            increment(CACHE_MISS_COUNT);
        }
    }

    /**
     * Records a response-cache stale hit.
     */
    public void cacheStale() {
        if (enabled) {
            increment(CACHE_STALE_COUNT);
        }
    }

    /**
     * Records one upstream forwarding attempt latency.
     *
     * @param upstream     upstream server definition
     * @param elapsedNanos non-negative elapsed nanoseconds
     */
    public void forwardUpstreamLatency(final DnsUpstream upstream, final long elapsedNanos) {
        if (!enabled || elapsedNanos < 0L) {
            return;
        }
        final String transport = upstream == null ? "unknown" : upstream.transport().name().toLowerCase(Locale.ROOT);
        final Duration elapsed = Duration.ofNanos(elapsedNanos);
        timing(FORWARD_UPSTREAM_LATENCY, elapsed);
        timing(PREFIX + "forward.upstream." + transport + ".latency", elapsed);
    }

    /**
     * Records one recursive-resolution latency.
     *
     * @param elapsedNanos non-negative elapsed nanoseconds
     */
    public void recursiveLatency(final long elapsedNanos) {
        if (enabled && elapsedNanos >= 0L) {
            timing(RECURSIVE_LATENCY, Duration.ofNanos(elapsedNanos));
        }
    }

    /**
     * Records one DNSSEC validation result.
     *
     * @param result DNSSEC validation result
     */
    public void dnssecResult(final DnssecResult result) {
        if (!enabled) {
            return;
        }
        if (result == null) {
            throw new ValidateException("DNSSEC metric result must not be null");
        }
        increment(DNSSEC_PREFIX + result.metricToken() + ".count");
    }

    /**
     * Records one rate-limit drop.
     */
    public void rateLimitDrop() {
        if (enabled) {
            increment(RATE_LIMIT_DROP_COUNT);
        }
    }

    /**
     * Increments a metric counter.
     *
     * @param name metric name
     */
    private void increment(final String name) {
        meter.increment(name);
    }

    /**
     * Records a metric duration.
     *
     * @param name     metric name
     * @param duration non-negative duration
     */
    private void timing(final String name, final Duration duration) {
        meter.timing(name, duration);
    }

    /**
     * Returns the normalized transport token.
     *
     * @param transport listener transport
     * @return lower-case transport token
     */
    private static String transportName(final DnsTransport transport) {
        if (transport == null) {
            throw new ValidateException("DNS metric transport must not be null");
        }
        return transport.name().toLowerCase(Locale.ROOT);
    }

    /**
     * Returns the normalized response-code token.
     *
     * @param responseCode response code
     * @return lower-case response-code token
     */
    private static String rcodeName(final DnsResponseCode responseCode) {
        if (responseCode == null) {
            throw new ValidateException("DNS metric response code must not be null");
        }
        return responseCode.name().toLowerCase(Locale.ROOT);
    }

    /**
     * DNSSEC validation result categories.
     *
     * @author Kimi Liu
     */
    public enum DnssecResult {

        /**
         * DNSSEC validation was skipped because the query did not request DNSSEC data.
         */
        SKIPPED("skipped"),

        /**
         * DNSSEC validation completed and the response is authenticated.
         */
        VALIDATED("validated"),

        /**
         * DNSSEC validation found an insecure but usable response.
         */
        INSECURE("insecure"),

        /**
         * DNSSEC validation failed and the response is unusable.
         */
        FAILED("failed");

        /**
         * Metric-name token.
         */
        private final String metricToken;

        /**
         * Creates a result category.
         *
         * @param metricToken metric-name token
         */
        DnssecResult(final String metricToken) {
            this.metricToken = metricToken;
        }

        /**
         * Returns the metric-name token.
         *
         * @return metric-name token
         */
        private String metricToken() {
            return metricToken;
        }

    }

}
