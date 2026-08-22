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
package org.miaixz.bus.fabric.network.dns.server;

import java.time.Duration;
import java.util.List;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.dns.observe.DnsMetrics;
import org.miaixz.bus.fabric.network.dns.observe.DnsQueryLog;
import org.miaixz.bus.fabric.network.dns.provider.DnsDynamicUpdateSink;
import org.miaixz.bus.fabric.network.dns.provider.DnsSnapshotListener;
import org.miaixz.bus.fabric.network.dns.provider.DnsSnapshotProvider;
import org.miaixz.bus.fabric.network.dns.provider.DnsSnapshotRequest;
import org.miaixz.bus.fabric.network.dns.zone.CidrBlock;
import org.miaixz.bus.fabric.network.dns.zone.DnsSnapshot;
import org.miaixz.bus.fabric.network.tls.TlsPolicy;

/**
 * Immutable DNS server startup options.
 *
 * @author Kimi Liu
 */
public class DnsServerOptions {

    /**
     * Default UDP payload size that avoids common fragmentation.
     */
    public static final int DEFAULT_UDP_PAYLOAD_BYTES = 1232;

    /**
     * Default number of cached raw DNS responses.
     */
    public static final int DEFAULT_CACHE_MAX_ENTRIES = 10000;

    /**
     * Default raw response cache TTL.
     */
    public static final Duration DEFAULT_CACHE_TTL = Duration.ofSeconds(30);

    /**
     * Default time during which stale cache entries may be served after refresh failure.
     */
    public static final Duration DEFAULT_CACHE_SERVE_STALE_TTL = Duration.ofMinutes(5);

    /**
     * Default time before cache expiry that active hits trigger background prefetch.
     */
    public static final Duration DEFAULT_CACHE_PREFETCH_BEFORE_EXPIRY = Duration.ofSeconds(5);

    /**
     * Default recursion clients limited to loopback addresses.
     */
    public static final List<CidrBlock> DEFAULT_RECURSION_ALLOWED_CIDRS = List
            .of(CidrBlock.parse("127.0.0.0/8"), CidrBlock.parse("::1/128"));

    /**
     * Default DNS server IO worker thread count.
     */
    public static final int DEFAULT_IO_THREADS = 16;

    /**
     * Default TCP connection idle timeout.
     */
    public static final Duration DEFAULT_TCP_IDLE_TIMEOUT = Duration.ofSeconds(30);

    /**
     * Default maximum in-flight DNS requests per TCP connection.
     */
    public static final int DEFAULT_TCP_MAX_IN_FLIGHT = 16;

    /**
     * Default maximum DNS TCP frame length.
     */
    public static final int DEFAULT_TCP_MAX_FRAME_BYTES = Normal._65535;

    /**
     * Default maximum concurrent QUIC streams.
     */
    public static final int DEFAULT_QUIC_MAX_STREAMS = 128;

    /**
     * Default QUIC connection idle timeout.
     */
    public static final Duration DEFAULT_QUIC_IDLE_TIMEOUT = Duration.ofSeconds(30);

    /**
     * Listener endpoints.
     */
    private final List<DnsEndpoint> endpoints;

    /**
     * Static snapshot used when no provider is configured.
     */
    private final DnsSnapshot snapshot;

    /**
     * External snapshot provider.
     */
    private final DnsSnapshotProvider provider;

    /**
     * Maximum UDP response payload size.
     */
    private final int maxUdpPayloadBytes;

    /**
     * Maximum raw response cache entries.
     */
    private final int cacheMaxEntries;

    /**
     * Raw response cache TTL.
     */
    private final Duration cacheTtl;

    /**
     * Time during which stale cache entries may be served after refresh failure.
     */
    private final Duration cacheServeStaleTtl;

    /**
     * Time before cache expiry that active hits trigger background prefetch.
     */
    private final Duration cachePrefetchBeforeExpiry;

    /**
     * Snapshot lifecycle listeners.
     */
    private final List<DnsSnapshotListener> snapshotListeners;

    /**
     * Client CIDR blocks allowed to use forwarding and recursive resolution.
     */
    private final List<CidrBlock> recursionAllowedCidrs;

    /**
     * Maximum DNS queries allowed per client per second, or zero when disabled.
     */
    private final int rateLimitPerSecond;

    /**
     * Client CIDR blocks allowed to request zone transfers.
     */
    private final List<CidrBlock> zoneTransferAllowedCidrs;

    /**
     * External Dynamic Update sink, or {@code null} when updates are disabled.
     */
    private final DnsDynamicUpdateSink dynamicUpdateSink;

    /**
     * TSIG keys accepted for signed DNS messages.
     */
    private final List<DnsTsigKey> tsigKeys;

    /**
     * TLS policy used by DNS-over-TLS endpoints, or {@code null} when DoT is not configured.
     */
    private final TlsPolicy tlsPolicy;

    /**
     * DNS server IO worker thread count.
     */
    private final int ioThreads;

    /**
     * TCP connection idle timeout.
     */
    private final Duration tcpIdleTimeout;

    /**
     * Maximum in-flight DNS requests per TCP connection.
     */
    private final int tcpMaxInFlight;

    /**
     * Maximum DNS TCP frame length.
     */
    private final int tcpMaxFrameBytes;

    /**
     * Maximum concurrent QUIC streams.
     */
    private final int quicMaxStreams;

    /**
     * QUIC connection idle timeout.
     */
    private final Duration quicIdleTimeout;

    /**
     * DNS Server metrics facade.
     */
    private final DnsMetrics metrics;

    /**
     * DNS query logger.
     */
    private final DnsQueryLog queryLog;

    /**
     * Creates server options.
     *
     * @param endpoints                 listener endpoints
     * @param snapshot                  static DNS snapshot
     * @param provider                  external snapshot provider
     * @param maxUdpPayloadBytes        maximum UDP response payload size
     * @param cacheMaxEntries           maximum raw response cache entries
     * @param cacheTtl                  raw response cache TTL
     * @param cacheServeStaleTtl        time during which stale cache entries may be served after refresh failure
     * @param cachePrefetchBeforeExpiry time before cache expiry that active hits trigger background prefetch
     * @param snapshotListeners         snapshot lifecycle listeners
     * @param recursionAllowedCidrs     client CIDR blocks allowed to use forwarding and recursive resolution
     * @param rateLimitPerSecond        maximum DNS queries allowed per client per second, or zero when disabled
     * @param zoneTransferAllowedCidrs  client CIDR blocks allowed to request zone transfers
     * @param dynamicUpdateSink         external Dynamic Update sink, or {@code null} when updates are disabled
     * @param tsigKeys                  TSIG keys accepted for signed DNS messages
     * @param tlsPolicy                 TLS policy used by DNS-over-TLS endpoints, or {@code null}
     * @param ioThreads                 DNS server IO worker thread count
     * @param tcpIdleTimeout            TCP connection idle timeout
     * @param tcpMaxInFlight            maximum in-flight DNS requests per TCP connection
     * @param tcpMaxFrameBytes          maximum DNS TCP frame length
     * @param quicMaxStreams            maximum concurrent QUIC streams
     * @param quicIdleTimeout           QUIC connection idle timeout
     * @param metrics                   DNS Server metrics facade
     * @param queryLog                  DNS query logger
     */
    public DnsServerOptions(final List<DnsEndpoint> endpoints, final DnsSnapshot snapshot,
            final DnsSnapshotProvider provider, final int maxUdpPayloadBytes, final int cacheMaxEntries,
            final Duration cacheTtl, final Duration cacheServeStaleTtl, final Duration cachePrefetchBeforeExpiry,
            final List<DnsSnapshotListener> snapshotListeners, final List<CidrBlock> recursionAllowedCidrs,
            final int rateLimitPerSecond, final List<CidrBlock> zoneTransferAllowedCidrs,
            final DnsDynamicUpdateSink dynamicUpdateSink, final List<DnsTsigKey> tsigKeys, final TlsPolicy tlsPolicy,
            final int ioThreads, final Duration tcpIdleTimeout, final int tcpMaxInFlight, final int tcpMaxFrameBytes,
            final int quicMaxStreams, final Duration quicIdleTimeout, final DnsMetrics metrics,
            final DnsQueryLog queryLog) {
        this.endpoints = immutableEndpoints(endpoints);
        this.snapshot = snapshot;
        this.provider = provider;
        this.maxUdpPayloadBytes = validatePayload(maxUdpPayloadBytes);
        this.cacheMaxEntries = validateCacheMaxEntries(cacheMaxEntries);
        this.cacheTtl = validateCacheTtl(cacheTtl);
        this.cacheServeStaleTtl = validateNonNegativeDuration(cacheServeStaleTtl, "DNS cache serve stale ttl");
        this.cachePrefetchBeforeExpiry = validatePrefetchDuration(cachePrefetchBeforeExpiry, this.cacheTtl);
        this.snapshotListeners = immutableSnapshotListeners(snapshotListeners);
        this.recursionAllowedCidrs = immutableCidrs(recursionAllowedCidrs);
        this.rateLimitPerSecond = validateRateLimit(rateLimitPerSecond);
        this.zoneTransferAllowedCidrs = immutableCidrs(zoneTransferAllowedCidrs);
        this.dynamicUpdateSink = dynamicUpdateSink;
        this.tsigKeys = immutableTsigKeys(tsigKeys);
        this.tlsPolicy = tlsPolicy;
        this.ioThreads = validatePositiveInt(ioThreads, "DNS IO threads");
        this.tcpIdleTimeout = validatePositiveDuration(tcpIdleTimeout, "DNS TCP idle timeout");
        this.tcpMaxInFlight = validatePositiveInt(tcpMaxInFlight, "DNS TCP max in-flight");
        this.tcpMaxFrameBytes = validateFrameBytes(tcpMaxFrameBytes);
        this.quicMaxStreams = validatePositiveInt(quicMaxStreams, "DNS QUIC max streams");
        this.quicIdleTimeout = validatePositiveDuration(quicIdleTimeout, "DNS QUIC idle timeout");
        this.metrics = validateMetrics(metrics);
        this.queryLog = validateQueryLog(queryLog);
        if (snapshot == null && provider == null) {
            throw new ValidateException("DNS server requires a snapshot or provider");
        }
    }

    /**
     * Creates options backed by a static snapshot.
     *
     * @param snapshot  static DNS snapshot
     * @param endpoints listener endpoints
     * @return DNS server options
     */
    public static DnsServerOptions snapshot(final DnsSnapshot snapshot, final List<DnsEndpoint> endpoints) {
        if (snapshot == null) {
            throw new ValidateException("DNS server snapshot must not be null");
        }
        return new DnsServerOptions(endpoints, snapshot, null, DEFAULT_UDP_PAYLOAD_BYTES, DEFAULT_CACHE_MAX_ENTRIES,
                DEFAULT_CACHE_TTL, DEFAULT_CACHE_SERVE_STALE_TTL, DEFAULT_CACHE_PREFETCH_BEFORE_EXPIRY, List.of(),
                DEFAULT_RECURSION_ALLOWED_CIDRS, 0, List.of(), null, List.of(), null, DEFAULT_IO_THREADS,
                DEFAULT_TCP_IDLE_TIMEOUT, DEFAULT_TCP_MAX_IN_FLIGHT, DEFAULT_TCP_MAX_FRAME_BYTES,
                DEFAULT_QUIC_MAX_STREAMS, DEFAULT_QUIC_IDLE_TIMEOUT, DnsMetrics.disabled(), DnsQueryLog.disabled());
    }

    /**
     * Creates options backed by an external provider.
     *
     * @param provider  external snapshot provider
     * @param endpoints listener endpoints
     * @return DNS server options
     */
    public static DnsServerOptions provider(final DnsSnapshotProvider provider, final List<DnsEndpoint> endpoints) {
        if (provider == null) {
            throw new ValidateException("DNS snapshot provider must not be null");
        }
        return new DnsServerOptions(endpoints, null, provider, DEFAULT_UDP_PAYLOAD_BYTES, DEFAULT_CACHE_MAX_ENTRIES,
                DEFAULT_CACHE_TTL, DEFAULT_CACHE_SERVE_STALE_TTL, DEFAULT_CACHE_PREFETCH_BEFORE_EXPIRY, List.of(),
                DEFAULT_RECURSION_ALLOWED_CIDRS, 0, List.of(), null, List.of(), null, DEFAULT_IO_THREADS,
                DEFAULT_TCP_IDLE_TIMEOUT, DEFAULT_TCP_MAX_IN_FLIGHT, DEFAULT_TCP_MAX_FRAME_BYTES,
                DEFAULT_QUIC_MAX_STREAMS, DEFAULT_QUIC_IDLE_TIMEOUT, DnsMetrics.disabled(), DnsQueryLog.disabled());
    }

    /**
     * Returns a copy with a replacement UDP payload size.
     *
     * @param maxUdpPayloadBytes maximum UDP response payload size
     * @return DNS server options
     */
    public DnsServerOptions withMaxUdpPayloadBytes(final int maxUdpPayloadBytes) {
        return new DnsServerOptions(endpoints, snapshot, provider, maxUdpPayloadBytes, cacheMaxEntries, cacheTtl,
                cacheServeStaleTtl, cachePrefetchBeforeExpiry, snapshotListeners, recursionAllowedCidrs,
                rateLimitPerSecond, zoneTransferAllowedCidrs, dynamicUpdateSink, tsigKeys, tlsPolicy, ioThreads,
                tcpIdleTimeout, tcpMaxInFlight, tcpMaxFrameBytes, quicMaxStreams, quicIdleTimeout, metrics, queryLog);
    }

    /**
     * Returns a copy with replacement raw response cache settings.
     *
     * @param maxEntries maximum cache entries, or zero to disable caching
     * @param ttl        cache TTL
     * @return DNS server options
     */
    public DnsServerOptions withCache(final int maxEntries, final Duration ttl) {
        final Duration checkedTtl = validateCacheTtl(ttl);
        final Duration prefetch = cachePrefetchBeforeExpiry.compareTo(checkedTtl) < 0 ? cachePrefetchBeforeExpiry
                : Duration.ZERO;
        return withCache(maxEntries, checkedTtl, cacheServeStaleTtl, prefetch);
    }

    /**
     * Returns a copy with replacement raw response cache settings.
     *
     * @param maxEntries           maximum cache entries, or zero to disable caching
     * @param ttl                  cache TTL
     * @param serveStaleTtl        time during which stale cache entries may be served after refresh failure
     * @param prefetchBeforeExpiry time before cache expiry that active hits trigger background prefetch
     * @return DNS server options
     */
    public DnsServerOptions withCache(
            final int maxEntries,
            final Duration ttl,
            final Duration serveStaleTtl,
            final Duration prefetchBeforeExpiry) {
        return new DnsServerOptions(endpoints, snapshot, provider, maxUdpPayloadBytes, maxEntries, ttl, serveStaleTtl,
                prefetchBeforeExpiry, snapshotListeners, recursionAllowedCidrs, rateLimitPerSecond,
                zoneTransferAllowedCidrs, dynamicUpdateSink, tsigKeys, tlsPolicy, ioThreads, tcpIdleTimeout,
                tcpMaxInFlight, tcpMaxFrameBytes, quicMaxStreams, quicIdleTimeout, metrics, queryLog);
    }

    /**
     * Returns a copy with replacement snapshot lifecycle listeners.
     *
     * @param listeners snapshot lifecycle listeners
     * @return DNS server options
     */
    public DnsServerOptions withSnapshotListeners(final List<DnsSnapshotListener> listeners) {
        return new DnsServerOptions(endpoints, snapshot, provider, maxUdpPayloadBytes, cacheMaxEntries, cacheTtl,
                cacheServeStaleTtl, cachePrefetchBeforeExpiry, listeners, recursionAllowedCidrs, rateLimitPerSecond,
                zoneTransferAllowedCidrs, dynamicUpdateSink, tsigKeys, tlsPolicy, ioThreads, tcpIdleTimeout,
                tcpMaxInFlight, tcpMaxFrameBytes, quicMaxStreams, quicIdleTimeout, metrics, queryLog);
    }

    /**
     * Returns a copy with replacement recursion ACL CIDR blocks.
     *
     * @param cidrs client CIDR blocks allowed to use forwarding and recursive resolution
     * @return DNS server options
     */
    public DnsServerOptions withRecursionAllowedCidrs(final List<CidrBlock> cidrs) {
        return new DnsServerOptions(endpoints, snapshot, provider, maxUdpPayloadBytes, cacheMaxEntries, cacheTtl,
                cacheServeStaleTtl, cachePrefetchBeforeExpiry, snapshotListeners, cidrs, rateLimitPerSecond,
                zoneTransferAllowedCidrs, dynamicUpdateSink, tsigKeys, tlsPolicy, ioThreads, tcpIdleTimeout,
                tcpMaxInFlight, tcpMaxFrameBytes, quicMaxStreams, quicIdleTimeout, metrics, queryLog);
    }

    /**
     * Returns a copy with a replacement per-client query rate limit.
     *
     * @param limitPerSecond maximum DNS queries allowed per client per second, or zero to disable
     * @return DNS server options
     */
    public DnsServerOptions withRateLimitPerSecond(final int limitPerSecond) {
        return new DnsServerOptions(endpoints, snapshot, provider, maxUdpPayloadBytes, cacheMaxEntries, cacheTtl,
                cacheServeStaleTtl, cachePrefetchBeforeExpiry, snapshotListeners, recursionAllowedCidrs, limitPerSecond,
                zoneTransferAllowedCidrs, dynamicUpdateSink, tsigKeys, tlsPolicy, ioThreads, tcpIdleTimeout,
                tcpMaxInFlight, tcpMaxFrameBytes, quicMaxStreams, quicIdleTimeout, metrics, queryLog);
    }

    /**
     * Returns a copy with replacement zone-transfer ACL CIDR blocks.
     *
     * @param cidrs client CIDR blocks allowed to request zone transfers
     * @return DNS server options
     */
    public DnsServerOptions withZoneTransferAllowedCidrs(final List<CidrBlock> cidrs) {
        return new DnsServerOptions(endpoints, snapshot, provider, maxUdpPayloadBytes, cacheMaxEntries, cacheTtl,
                cacheServeStaleTtl, cachePrefetchBeforeExpiry, snapshotListeners, recursionAllowedCidrs,
                rateLimitPerSecond, cidrs, dynamicUpdateSink, tsigKeys, tlsPolicy, ioThreads, tcpIdleTimeout,
                tcpMaxInFlight, tcpMaxFrameBytes, quicMaxStreams, quicIdleTimeout, metrics, queryLog);
    }

    /**
     * Returns a copy with a replacement Dynamic Update sink.
     *
     * @param sink external Dynamic Update sink, or {@code null} to disable updates
     * @return DNS server options
     */
    public DnsServerOptions withDynamicUpdateSink(final DnsDynamicUpdateSink sink) {
        return new DnsServerOptions(endpoints, snapshot, provider, maxUdpPayloadBytes, cacheMaxEntries, cacheTtl,
                cacheServeStaleTtl, cachePrefetchBeforeExpiry, snapshotListeners, recursionAllowedCidrs,
                rateLimitPerSecond, zoneTransferAllowedCidrs, sink, tsigKeys, tlsPolicy, ioThreads, tcpIdleTimeout,
                tcpMaxInFlight, tcpMaxFrameBytes, quicMaxStreams, quicIdleTimeout, metrics, queryLog);
    }

    /**
     * Returns a copy with replacement TSIG keys.
     *
     * @param keys TSIG keys accepted for signed DNS messages
     * @return DNS server options
     */
    public DnsServerOptions withTsigKeys(final List<DnsTsigKey> keys) {
        return new DnsServerOptions(endpoints, snapshot, provider, maxUdpPayloadBytes, cacheMaxEntries, cacheTtl,
                cacheServeStaleTtl, cachePrefetchBeforeExpiry, snapshotListeners, recursionAllowedCidrs,
                rateLimitPerSecond, zoneTransferAllowedCidrs, dynamicUpdateSink, keys, tlsPolicy, ioThreads,
                tcpIdleTimeout, tcpMaxInFlight, tcpMaxFrameBytes, quicMaxStreams, quicIdleTimeout, metrics, queryLog);
    }

    /**
     * Returns a copy with a replacement DNS-over-TLS policy.
     *
     * @param policy TLS policy used by DoT endpoints, or {@code null} to disable DoT startup
     * @return DNS server options
     */
    public DnsServerOptions withTlsPolicy(final TlsPolicy policy) {
        return new DnsServerOptions(endpoints, snapshot, provider, maxUdpPayloadBytes, cacheMaxEntries, cacheTtl,
                cacheServeStaleTtl, cachePrefetchBeforeExpiry, snapshotListeners, recursionAllowedCidrs,
                rateLimitPerSecond, zoneTransferAllowedCidrs, dynamicUpdateSink, tsigKeys, policy, ioThreads,
                tcpIdleTimeout, tcpMaxInFlight, tcpMaxFrameBytes, quicMaxStreams, quicIdleTimeout, metrics, queryLog);
    }

    /**
     * Returns a copy with a replacement DNS server IO worker thread count.
     *
     * @param threads DNS server IO worker thread count
     * @return DNS server options
     */
    public DnsServerOptions withIoThreads(final int threads) {
        return new DnsServerOptions(endpoints, snapshot, provider, maxUdpPayloadBytes, cacheMaxEntries, cacheTtl,
                cacheServeStaleTtl, cachePrefetchBeforeExpiry, snapshotListeners, recursionAllowedCidrs,
                rateLimitPerSecond, zoneTransferAllowedCidrs, dynamicUpdateSink, tsigKeys, tlsPolicy, threads,
                tcpIdleTimeout, tcpMaxInFlight, tcpMaxFrameBytes, quicMaxStreams, quicIdleTimeout, metrics, queryLog);
    }

    /**
     * Returns a copy with a replacement TCP connection idle timeout.
     *
     * @param timeout TCP connection idle timeout
     * @return DNS server options
     */
    public DnsServerOptions withTcpIdleTimeout(final Duration timeout) {
        return new DnsServerOptions(endpoints, snapshot, provider, maxUdpPayloadBytes, cacheMaxEntries, cacheTtl,
                cacheServeStaleTtl, cachePrefetchBeforeExpiry, snapshotListeners, recursionAllowedCidrs,
                rateLimitPerSecond, zoneTransferAllowedCidrs, dynamicUpdateSink, tsigKeys, tlsPolicy, ioThreads,
                timeout, tcpMaxInFlight, tcpMaxFrameBytes, quicMaxStreams, quicIdleTimeout, metrics, queryLog);
    }

    /**
     * Returns a copy with a replacement maximum in-flight DNS request count per TCP connection.
     *
     * @param maxInFlight maximum in-flight DNS request count per TCP connection
     * @return DNS server options
     */
    public DnsServerOptions withTcpMaxInFlight(final int maxInFlight) {
        return new DnsServerOptions(endpoints, snapshot, provider, maxUdpPayloadBytes, cacheMaxEntries, cacheTtl,
                cacheServeStaleTtl, cachePrefetchBeforeExpiry, snapshotListeners, recursionAllowedCidrs,
                rateLimitPerSecond, zoneTransferAllowedCidrs, dynamicUpdateSink, tsigKeys, tlsPolicy, ioThreads,
                tcpIdleTimeout, maxInFlight, tcpMaxFrameBytes, quicMaxStreams, quicIdleTimeout, metrics, queryLog);
    }

    /**
     * Returns a copy with a replacement maximum DNS TCP frame length.
     *
     * @param frameBytes maximum DNS TCP frame length
     * @return DNS server options
     */
    public DnsServerOptions withTcpMaxFrameBytes(final int frameBytes) {
        return new DnsServerOptions(endpoints, snapshot, provider, maxUdpPayloadBytes, cacheMaxEntries, cacheTtl,
                cacheServeStaleTtl, cachePrefetchBeforeExpiry, snapshotListeners, recursionAllowedCidrs,
                rateLimitPerSecond, zoneTransferAllowedCidrs, dynamicUpdateSink, tsigKeys, tlsPolicy, ioThreads,
                tcpIdleTimeout, tcpMaxInFlight, frameBytes, quicMaxStreams, quicIdleTimeout, metrics, queryLog);
    }

    /**
     * Returns a copy with a replacement maximum concurrent QUIC stream count.
     *
     * @param maxStreams maximum concurrent QUIC stream count
     * @return DNS server options
     */
    public DnsServerOptions withQuicMaxStreams(final int maxStreams) {
        return new DnsServerOptions(endpoints, snapshot, provider, maxUdpPayloadBytes, cacheMaxEntries, cacheTtl,
                cacheServeStaleTtl, cachePrefetchBeforeExpiry, snapshotListeners, recursionAllowedCidrs,
                rateLimitPerSecond, zoneTransferAllowedCidrs, dynamicUpdateSink, tsigKeys, tlsPolicy, ioThreads,
                tcpIdleTimeout, tcpMaxInFlight, tcpMaxFrameBytes, maxStreams, quicIdleTimeout, metrics, queryLog);
    }

    /**
     * Returns a copy with a replacement QUIC connection idle timeout.
     *
     * @param timeout QUIC connection idle timeout
     * @return DNS server options
     */
    public DnsServerOptions withQuicIdleTimeout(final Duration timeout) {
        return new DnsServerOptions(endpoints, snapshot, provider, maxUdpPayloadBytes, cacheMaxEntries, cacheTtl,
                cacheServeStaleTtl, cachePrefetchBeforeExpiry, snapshotListeners, recursionAllowedCidrs,
                rateLimitPerSecond, zoneTransferAllowedCidrs, dynamicUpdateSink, tsigKeys, tlsPolicy, ioThreads,
                tcpIdleTimeout, tcpMaxInFlight, tcpMaxFrameBytes, quicMaxStreams, timeout, metrics, queryLog);
    }

    /**
     * Returns a copy with replacement DNS Server metrics.
     *
     * @param dnsMetrics DNS Server metrics facade
     * @return DNS server options
     */
    public DnsServerOptions withMetrics(final DnsMetrics dnsMetrics) {
        return new DnsServerOptions(endpoints, snapshot, provider, maxUdpPayloadBytes, cacheMaxEntries, cacheTtl,
                cacheServeStaleTtl, cachePrefetchBeforeExpiry, snapshotListeners, recursionAllowedCidrs,
                rateLimitPerSecond, zoneTransferAllowedCidrs, dynamicUpdateSink, tsigKeys, tlsPolicy, ioThreads,
                tcpIdleTimeout, tcpMaxInFlight, tcpMaxFrameBytes, quicMaxStreams, quicIdleTimeout, dnsMetrics,
                queryLog);
    }

    /**
     * Returns a copy with replacement DNS query logger.
     *
     * @param dnsQueryLog DNS query logger
     * @return DNS server options
     */
    public DnsServerOptions withQueryLog(final DnsQueryLog dnsQueryLog) {
        return new DnsServerOptions(endpoints, snapshot, provider, maxUdpPayloadBytes, cacheMaxEntries, cacheTtl,
                cacheServeStaleTtl, cachePrefetchBeforeExpiry, snapshotListeners, recursionAllowedCidrs,
                rateLimitPerSecond, zoneTransferAllowedCidrs, dynamicUpdateSink, tsigKeys, tlsPolicy, ioThreads,
                tcpIdleTimeout, tcpMaxInFlight, tcpMaxFrameBytes, quicMaxStreams, quicIdleTimeout, metrics,
                dnsQueryLog);
    }

    /**
     * Returns listener endpoints.
     *
     * @return immutable listener endpoints
     */
    public List<DnsEndpoint> endpoints() {
        return endpoints;
    }

    /**
     * Loads the active startup snapshot.
     *
     * @return complete DNS snapshot
     */
    public DnsSnapshot loadSnapshot() {
        return loadSnapshot(null);
    }

    /**
     * Loads a snapshot using the active snapshot as provider context.
     *
     * @param activeSnapshot active snapshot visible before a refresh, or {@code null} during startup
     * @return complete DNS snapshot
     */
    public DnsSnapshot loadSnapshot(final DnsSnapshot activeSnapshot) {
        final DnsSnapshot loaded = provider == null ? snapshot : provider.load(request(activeSnapshot));
        if (loaded == null) {
            throw new ValidateException("DNS snapshot provider returned null");
        }
        return loaded;
    }

    /**
     * Creates a provider request from the active snapshot.
     *
     * @param activeSnapshot active snapshot visible before refresh, or {@code null} during startup
     * @return snapshot provider request
     */
    private static DnsSnapshotRequest request(final DnsSnapshot activeSnapshot) {
        return activeSnapshot == null ? DnsSnapshotRequest.startup() : DnsSnapshotRequest.refresh(activeSnapshot);
    }

    /**
     * Returns the maximum UDP response payload size.
     *
     * @return maximum UDP payload bytes
     */
    public int maxUdpPayloadBytes() {
        return maxUdpPayloadBytes;
    }

    /**
     * Returns the maximum raw response cache entries.
     *
     * @return maximum cache entries, or zero when disabled
     */
    public int cacheMaxEntries() {
        return cacheMaxEntries;
    }

    /**
     * Returns the raw response cache TTL.
     *
     * @return positive cache TTL
     */
    public Duration cacheTtl() {
        return cacheTtl;
    }

    /**
     * Returns the stale cache serve window.
     *
     * @return non-negative stale cache serve window
     */
    public Duration cacheServeStaleTtl() {
        return cacheServeStaleTtl;
    }

    /**
     * Returns the cache prefetch window before expiry.
     *
     * @return non-negative cache prefetch window before expiry
     */
    public Duration cachePrefetchBeforeExpiry() {
        return cachePrefetchBeforeExpiry;
    }

    /**
     * Returns snapshot lifecycle listeners.
     *
     * @return immutable snapshot lifecycle listeners
     */
    public List<DnsSnapshotListener> snapshotListeners() {
        return snapshotListeners;
    }

    /**
     * Returns client CIDR blocks allowed to use forwarding and recursive resolution.
     *
     * @return immutable recursion ACL CIDR blocks
     */
    public List<CidrBlock> recursionAllowedCidrs() {
        return recursionAllowedCidrs;
    }

    /**
     * Returns the maximum DNS queries allowed per client per second.
     *
     * @return rate limit, or zero when disabled
     */
    public int rateLimitPerSecond() {
        return rateLimitPerSecond;
    }

    /**
     * Returns client CIDR blocks allowed to request zone transfers.
     *
     * @return immutable zone-transfer ACL CIDR blocks
     */
    public List<CidrBlock> zoneTransferAllowedCidrs() {
        return zoneTransferAllowedCidrs;
    }

    /**
     * Returns the external Dynamic Update sink.
     *
     * @return Dynamic Update sink, or {@code null} when updates are disabled
     */
    public DnsDynamicUpdateSink dynamicUpdateSink() {
        return dynamicUpdateSink;
    }

    /**
     * Returns TSIG keys accepted for signed DNS messages.
     *
     * @return immutable TSIG key list
     */
    public List<DnsTsigKey> tsigKeys() {
        return tsigKeys;
    }

    /**
     * Returns the DNS-over-TLS policy.
     *
     * @return TLS policy, or {@code null} when DoT startup is disabled
     */
    public TlsPolicy tlsPolicy() {
        return tlsPolicy;
    }

    /**
     * Returns the DNS server IO worker thread count.
     *
     * @return positive IO worker thread count
     */
    public int ioThreads() {
        return ioThreads;
    }

    /**
     * Returns the TCP connection idle timeout.
     *
     * @return positive TCP idle timeout
     */
    public Duration tcpIdleTimeout() {
        return tcpIdleTimeout;
    }

    /**
     * Returns the maximum in-flight DNS request count per TCP connection.
     *
     * @return positive maximum in-flight request count
     */
    public int tcpMaxInFlight() {
        return tcpMaxInFlight;
    }

    /**
     * Returns the maximum DNS TCP frame length.
     *
     * @return maximum DNS TCP frame length from 1 through 65535
     */
    public int tcpMaxFrameBytes() {
        return tcpMaxFrameBytes;
    }

    /**
     * Returns the maximum concurrent QUIC stream count.
     *
     * @return positive maximum concurrent QUIC stream count
     */
    public int quicMaxStreams() {
        return quicMaxStreams;
    }

    /**
     * Returns the QUIC connection idle timeout.
     *
     * @return positive QUIC idle timeout
     */
    public Duration quicIdleTimeout() {
        return quicIdleTimeout;
    }

    /**
     * Returns DNS Server metrics.
     *
     * @return DNS Server metrics facade
     */
    public DnsMetrics metrics() {
        return metrics;
    }

    /**
     * Returns the DNS query logger.
     *
     * @return DNS query logger
     */
    public DnsQueryLog queryLog() {
        return queryLog;
    }

    /**
     * Validates and copies endpoints.
     *
     * @param endpoints source endpoints
     * @return immutable endpoints
     */
    private static List<DnsEndpoint> immutableEndpoints(final List<DnsEndpoint> endpoints) {
        if (endpoints == null || endpoints.isEmpty()) {
            throw new ValidateException("DNS server endpoints must not be empty");
        }
        for (final DnsEndpoint endpoint : endpoints) {
            if (endpoint == null) {
                throw new ValidateException("DNS server endpoints must not contain null");
            }
        }
        return List.copyOf(endpoints);
    }

    /**
     * Validates and copies snapshot lifecycle listeners.
     *
     * @param listeners source listeners
     * @return immutable listeners
     */
    private static List<DnsSnapshotListener> immutableSnapshotListeners(final List<DnsSnapshotListener> listeners) {
        if (listeners == null) {
            throw new ValidateException("DNS snapshot listeners must not be null");
        }
        for (final DnsSnapshotListener listener : listeners) {
            if (listener == null) {
                throw new ValidateException("DNS snapshot listeners must not contain null");
            }
        }
        return List.copyOf(listeners);
    }

    /**
     * Validates and copies CIDR blocks.
     *
     * @param cidrs source CIDR blocks
     * @return immutable CIDR blocks
     */
    private static List<CidrBlock> immutableCidrs(final List<CidrBlock> cidrs) {
        if (cidrs == null) {
            throw new ValidateException("DNS recursion ACL CIDRs must not be null");
        }
        for (final CidrBlock cidr : cidrs) {
            if (cidr == null) {
                throw new ValidateException("DNS recursion ACL CIDRs must not contain null");
            }
        }
        return List.copyOf(cidrs);
    }

    /**
     * Validates and copies TSIG keys.
     *
     * @param keys source TSIG keys
     * @return immutable TSIG keys
     */
    private static List<DnsTsigKey> immutableTsigKeys(final List<DnsTsigKey> keys) {
        if (keys == null) {
            throw new ValidateException("DNS TSIG keys must not be null");
        }
        for (final DnsTsigKey key : keys) {
            if (key == null) {
                throw new ValidateException("DNS TSIG keys must not contain null");
            }
        }
        return List.copyOf(keys);
    }

    /**
     * Validates the UDP payload limit.
     *
     * @param value candidate payload size
     * @return validated payload size
     */
    private static int validatePayload(final int value) {
        if (value < Normal._512 || value > Normal._65535) {
            throw new ValidateException("DNS UDP payload size must be from 512 through 65535");
        }
        return value;
    }

    /**
     * Validates the cache size.
     *
     * @param value candidate cache size
     * @return validated cache size
     */
    private static int validateCacheMaxEntries(final int value) {
        if (value < 0) {
            throw new ValidateException("DNS cache max entries must be non-negative");
        }
        return value;
    }

    /**
     * Validates the cache TTL.
     *
     * @param value candidate cache TTL
     * @return validated cache TTL
     */
    private static Duration validateCacheTtl(final Duration value) {
        if (value == null || value.isNegative() || value.isZero()) {
            throw new ValidateException("DNS cache ttl must be positive");
        }
        return value;
    }

    /**
     * Validates a non-negative duration.
     *
     * @param value candidate duration
     * @param name  diagnostic name
     * @return validated duration
     */
    private static Duration validateNonNegativeDuration(final Duration value, final String name) {
        if (value == null || value.isNegative()) {
            throw new ValidateException(name + " must be non-negative");
        }
        return value;
    }

    /**
     * Validates the cache prefetch window.
     *
     * @param value candidate prefetch window
     * @param ttl   cache TTL used as the upper bound
     * @return validated prefetch window
     */
    private static Duration validatePrefetchDuration(final Duration value, final Duration ttl) {
        final Duration checked = validateNonNegativeDuration(value, "DNS cache prefetch window");
        if (!checked.isZero() && checked.compareTo(ttl) >= 0) {
            throw new ValidateException("DNS cache prefetch window must be shorter than cache ttl");
        }
        return checked;
    }

    /**
     * Validates a rate limit.
     *
     * @param value candidate rate limit
     * @return validated rate limit
     */
    private static int validateRateLimit(final int value) {
        if (value < 0) {
            throw new ValidateException("DNS rate limit must be non-negative");
        }
        return value;
    }

    /**
     * Validates DNS Server metrics.
     *
     * @param value candidate metrics facade
     * @return validated metrics facade
     */
    private static DnsMetrics validateMetrics(final DnsMetrics value) {
        if (value == null) {
            throw new ValidateException("DNS metrics must not be null");
        }
        return value;
    }

    /**
     * Validates DNS query logging.
     *
     * @param value candidate query logger
     * @return validated query logger
     */
    private static DnsQueryLog validateQueryLog(final DnsQueryLog value) {
        if (value == null) {
            throw new ValidateException("DNS query log must not be null");
        }
        return value;
    }

    /**
     * Validates a positive integer option.
     *
     * @param value candidate integer
     * @param name  diagnostic option name
     * @return validated positive integer
     */
    private static int validatePositiveInt(final int value, final String name) {
        if (value <= 0) {
            throw new ValidateException(name + " must be positive");
        }
        return value;
    }

    /**
     * Validates a positive duration option.
     *
     * @param value candidate duration
     * @param name  diagnostic option name
     * @return validated positive duration
     */
    private static Duration validatePositiveDuration(final Duration value, final String name) {
        if (value == null || value.isNegative() || value.isZero()) {
            throw new ValidateException(name + " must be positive");
        }
        return value;
    }

    /**
     * Validates the DNS TCP frame length limit.
     *
     * @param value candidate frame length
     * @return validated frame length
     */
    private static int validateFrameBytes(final int value) {
        if (value < Normal._1 || value > Normal._65535) {
            throw new ValidateException("DNS TCP max frame bytes must be from 1 through 65535");
        }
        return value;
    }

}
