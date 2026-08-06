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

import java.io.IOException;
import java.net.InetAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.Lifecycle;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.fabric.network.dns.cache.DnsResponseCache;
import org.miaixz.bus.fabric.network.dns.forward.DnsForwarder;
import org.miaixz.bus.fabric.network.dns.forward.DnsUpstream;
import org.miaixz.bus.fabric.network.dns.message.DnsCodec;
import org.miaixz.bus.fabric.network.dns.message.DnsDecodedResponse;
import org.miaixz.bus.fabric.network.dns.message.DnsExtendedError;
import org.miaixz.bus.fabric.network.dns.message.DnsQuery;
import org.miaixz.bus.fabric.network.dns.message.DnsResponse;
import org.miaixz.bus.fabric.network.dns.message.DnsResponseCode;
import org.miaixz.bus.fabric.network.dns.message.DnsTsig;
import org.miaixz.bus.fabric.network.dns.observe.DnsMetrics;
import org.miaixz.bus.fabric.network.dns.observe.DnsMetrics.DnssecResult;
import org.miaixz.bus.fabric.network.dns.observe.DnsQueryLog;
import org.miaixz.bus.fabric.network.dns.policy.DnsPolicyRule;
import org.miaixz.bus.fabric.network.dns.provider.DnsSnapshotListener;
import org.miaixz.bus.fabric.network.dns.recursive.DnsRecursiveResolver;
import org.miaixz.bus.fabric.network.dns.resolve.DnsAuthoritativeResolver;
import org.miaixz.bus.fabric.network.dns.resolve.DnsResolution;
import org.miaixz.bus.fabric.network.dns.resolve.RuntimeIndex;
import org.miaixz.bus.fabric.network.dns.secure.DnsDohServer;
import org.miaixz.bus.fabric.network.dns.secure.DnsDotEndpoint;
import org.miaixz.bus.fabric.network.dns.secure.quic.DnsQuicRuntime;
import org.miaixz.bus.fabric.network.dns.update.DnsDynamicUpdateHandler;
import org.miaixz.bus.fabric.network.dns.xfer.DnsNotifyHandler;
import org.miaixz.bus.fabric.network.dns.xfer.DnsZoneTransferHandler;
import org.miaixz.bus.fabric.network.dns.zone.CidrBlock;
import org.miaixz.bus.fabric.network.dns.zone.DnsSnapshot;
import org.miaixz.bus.fabric.network.dns.zone.DnsZone;
import org.miaixz.bus.fabric.network.dns.zone.DnsZoneMode;

/**
 * DNS server runtime that serves UDP and TCP queries from an immutable snapshot index.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class DnsServer implements AutoCloseable, Lifecycle {

    /**
     * Startup options.
     */
    private final DnsServerOptions options;

    /**
     * Active runtime index.
     */
    private final AtomicReference<RuntimeIndex> index;

    /**
     * Started endpoint cleanup handles.
     */
    private final CopyOnWriteArrayList<AutoCloseable> handles;

    /**
     * Raw response cache partitioned by transport style.
     */
    private final DnsResponseCache cache;

    /**
     * Optional DNS Server metrics facade.
     */
    private final DnsMetrics metrics;

    /**
     * Optional DNS query logger.
     */
    private final DnsQueryLog queryLog;

    /**
     * AXFR and IXFR response handler.
     */
    private final DnsZoneTransferHandler zoneTransferHandler;

    /**
     * DNS NOTIFY validation and refresh handler.
     */
    private final DnsNotifyHandler notifyHandler;

    /**
     * DNS Dynamic Update validation and external sink handler.
     */
    private final DnsDynamicUpdateHandler dynamicUpdateHandler;

    /**
     * Per-client DNS query rate limiter.
     */
    private final DnsRateLimiter rateLimiter;

    /**
     * Start guard.
     */
    private final AtomicBoolean started;

    /**
     * Close guard.
     */
    private final AtomicBoolean closed;

    /**
     * Creates a DNS server.
     *
     * @param options startup options
     */
    private DnsServer(final DnsServerOptions options) {
        if (options == null) {
            throw new ValidateException("DNS server options must not be null");
        }
        this.options = options;
        this.index = new AtomicReference<>();
        this.handles = new CopyOnWriteArrayList<>();
        this.cache = new DnsResponseCache(options.cacheMaxEntries(), options.cacheTtl(), options.cacheServeStaleTtl(),
                options.cachePrefetchBeforeExpiry(), options.metrics());
        this.metrics = options.metrics();
        this.queryLog = options.queryLog();
        this.zoneTransferHandler = new DnsZoneTransferHandler(options.zoneTransferAllowedCidrs());
        this.notifyHandler = new DnsNotifyHandler(options.zoneTransferAllowedCidrs());
        this.dynamicUpdateHandler = new DnsDynamicUpdateHandler(options.dynamicUpdateSink());
        this.rateLimiter = options.rateLimitPerSecond() == 0 ? DnsRateLimiter.disabled()
                : new DnsRateLimiter(options.rateLimitPerSecond());
        this.started = new AtomicBoolean();
        this.closed = new AtomicBoolean();
    }

    /**
     * Creates a DNS server.
     *
     * @param options startup options
     * @return DNS server runtime
     */
    public static DnsServer create(final DnsServerOptions options) {
        return new DnsServer(options);
    }

    /**
     * Starts configured endpoints after loading and compiling the initial snapshot.
     *
     * @return this server
     * @throws StatefulException if the server is closed or already started
     * @throws SocketException   if an endpoint cannot bind
     */
    public DnsServer start() {
        if (closed.get()) {
            throw new StatefulException("DNS server is closed");
        }
        if (!started.compareAndSet(false, true)) {
            throw new StatefulException("DNS server can only be started once");
        }
        reload(options.loadSnapshot(null));
        final ArrayList<AutoCloseable> opened = new ArrayList<>();
        try {
            for (final DnsEndpoint endpoint : options.endpoints()) {
                final AutoCloseable handle = startEndpoint(endpoint);
                opened.add(handle);
                handles.add(handle);
            }
            return this;
        } catch (final RuntimeException e) {
            closeHandles(opened);
            handles.removeAll(opened);
            started.set(false);
            throw e;
        }
    }

    /**
     * Reloads the runtime snapshot atomically.
     *
     * @param snapshot complete DNS snapshot
     */
    public void reload(final DnsSnapshot snapshot) {
        final RuntimeIndex previous = index.get();
        try {
            RuntimeIndex.replace(index, snapshot);
            cache.clear();
            rateLimiter.clear();
            notifyAccepted(snapshot);
        } catch (final RuntimeException e) {
            notifyRejected(snapshot, e);
            if (previous != null) {
                notifyRolledBack(previous.snapshot(), e);
            }
            throw e;
        }
    }

    /**
     * Returns whether this server has started and not closed.
     *
     * @return true when endpoints are active
     */
    public boolean running() {
        return active();
    }

    /**
     * Returns the current DNS server lifecycle state.
     *
     * @return server lifecycle state
     */
    @Override
    public State state() {
        if (closed.get()) {
            return State.CLOSED;
        }
        return started.get() ? State.RUNNING : State.NEW;
    }

    /**
     * Returns the active snapshot.
     *
     * @return active DNS snapshot
     */
    public DnsSnapshot snapshot() {
        final RuntimeIndex current = requireIndex();
        return current.snapshot();
    }

    /**
     * Loads a fresh snapshot through the configured provider and installs it atomically.
     */
    public void refresh() {
        final RuntimeIndex current = index.get();
        reload(options.loadSnapshot(current == null ? null : current.snapshot()));
    }

    /**
     * Closes all endpoints.
     */
    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        closeHandles(new ArrayList<>(handles));
        handles.clear();
    }

    /**
     * Starts one endpoint.
     *
     * @param endpoint endpoint definition
     * @return cleanup handle
     */
    private AutoCloseable startEndpoint(final DnsEndpoint endpoint) {
        if (endpoint.transport() == DnsTransport.UDP) {
            return startUdp(endpoint);
        }
        if (endpoint.transport() == DnsTransport.TCP) {
            return startTcp(endpoint);
        }
        if (endpoint.transport() == DnsTransport.DOH) {
            return startDoh(endpoint);
        }
        if (endpoint.transport() == DnsTransport.DOQ) {
            return startDoq(endpoint);
        }
        return startDot(endpoint);
    }

    /**
     * Starts a UDP endpoint.
     *
     * @param endpoint UDP endpoint definition
     * @return cleanup handle
     */
    private AutoCloseable startUdp(final DnsEndpoint endpoint) {
        return new DnsUdpEndpoint(endpoint, options, this::handleUdp).start();
    }

    /**
     * Starts a TCP endpoint.
     *
     * @param endpoint TCP endpoint definition
     * @return cleanup handle
     */
    private AutoCloseable startTcp(final DnsEndpoint endpoint) {
        return new DnsTcpEndpoint(endpoint, options, this::handleTcp).start();
    }

    /**
     * Starts a DNS-over-TLS endpoint.
     *
     * @param endpoint DoT endpoint definition
     * @return cleanup handle
     */
    private AutoCloseable startDot(final DnsEndpoint endpoint) {
        if (options.tlsPolicy() == null) {
            throw new ValidateException("DNS-over-TLS endpoint requires a TLS policy");
        }
        return new DnsDotEndpoint(endpoint, options, this::handleDot).start();
    }

    /**
     * Starts a DNS-over-HTTPS endpoint.
     *
     * @param endpoint DoH endpoint definition
     * @return cleanup handle
     */
    private AutoCloseable startDoh(final DnsEndpoint endpoint) {
        try {
            return new DnsDohServer(endpoint.socketAddress(), this::handleDohDns).start();
        } catch (final IOException e) {
            throw new SocketException("Unable to start DNS-over-HTTPS endpoint", e);
        }
    }

    /**
     * Starts a DNS-over-QUIC endpoint.
     *
     * @param endpoint DoQ endpoint definition
     * @return cleanup handle
     * @throws ValidateException when the isolated QUIC runtime is unavailable
     */
    private AutoCloseable startDoq(final DnsEndpoint endpoint) {
        final String label = "endpoint " + endpoint.authority();
        DnsQuicRuntime.requireAvailable(label);
        throw DnsQuicRuntime.adapterUnavailable(label);
    }

    /**
     * Handles one DNS message carried by DNS-over-HTTPS.
     *
     * @param request       request wire bytes
     * @param clientAddress client address, or {@code null} when unavailable
     * @return response wire bytes
     */
    private byte[] handleDohDns(final byte[] request, final InetAddress clientAddress) {
        try {
            return handle(request, true, false, clientAddress, DnsTransport.DOH);
        } catch (final RuntimeException e) {
            return DnsCodec.encodeFormatError(request);
        }
    }

    /**
     * Handles a UDP DNS request.
     *
     * @param request       request wire bytes
     * @param clientAddress client address, or {@code null} when unavailable
     * @return response wire bytes
     */
    private byte[] handleUdp(final byte[] request, final InetAddress clientAddress) {
        try {
            return handle(request, false, false, clientAddress, DnsTransport.UDP);
        } catch (final RuntimeException e) {
            return DnsCodec.encodeFormatError(request);
        }
    }

    /**
     * Handles a TCP DNS request.
     *
     * @param request       request wire bytes
     * @param clientAddress client address, or {@code null} when unavailable
     * @return response wire bytes
     */
    private byte[] handleTcp(final byte[] request, final InetAddress clientAddress) {
        try {
            return handle(request, true, true, clientAddress, DnsTransport.TCP);
        } catch (final RuntimeException e) {
            return DnsCodec.encodeFormatError(request);
        }
    }

    /**
     * Handles a DNS-over-TLS DNS request.
     *
     * @param request       request wire bytes
     * @param clientAddress client address, or {@code null} when unavailable
     * @return response wire bytes
     */
    private byte[] handleDot(final byte[] request, final InetAddress clientAddress) {
        try {
            return handle(request, true, true, clientAddress, DnsTransport.DOT);
        } catch (final RuntimeException e) {
            return DnsCodec.encodeFormatError(request);
        }
    }

    /**
     * Resolves one wire-format DNS request.
     *
     * @param request                  request wire bytes
     * @param stream                   true when the caller expects a TCP-style full response
     * @param transferCapableTransport true when the caller supports zone-transfer responses
     * @param clientAddress            client address, or {@code null} when unavailable
     * @param transport                listener transport
     * @return response wire bytes
     */
    private byte[] handle(
            final byte[] request,
            final boolean stream,
            final boolean transferCapableTransport,
            final InetAddress clientAddress,
            final DnsTransport transport) {
        final long startedNanos = queryLog.enabled() ? System.nanoTime() : 0L;
        final DnsQuery query = DnsCodec.decodeQuery(request);
        metrics.query(transport);
        final byte[] response = handleDecoded(request, stream, transferCapableTransport, clientAddress, query);
        recordResponseMetrics(query, response);
        recordQueryLog(query, response, clientAddress, transport, startedNanos);
        return response;
    }

    /**
     * Resolves one decoded DNS request.
     *
     * @param request                  request wire bytes
     * @param stream                   true when the caller expects a TCP-style full response
     * @param transferCapableTransport true when the caller supports zone-transfer responses
     * @param clientAddress            client address, or {@code null} when unavailable
     * @param query                    decoded DNS query
     * @return response wire bytes
     */
    private byte[] handleDecoded(
            final byte[] request,
            final boolean stream,
            final boolean transferCapableTransport,
            final InetAddress clientAddress,
            final DnsQuery query) {
        final RuntimeIndex current = requireIndex();
        final DnsTsigKey tsigKey = authenticatedTsig(current, query);
        if (query.tsigPresent() && tsigKey == null) {
            return encode(
                    DnsResponse.empty(query, DnsResponseCode.REFUSED, false),
                    stream,
                    options.maxUdpPayloadBytes());
        }
        if (!rateLimiter.allow(clientAddress)) {
            metrics.rateLimitDrop();
            return encodeSigned(
                    DnsResponse.empty(query, DnsResponseCode.REFUSED, false),
                    stream,
                    options.maxUdpPayloadBytes(),
                    tsigKey);
        }
        if (query.opcode() == DnsQuery.OPCODE_NOTIFY) {
            return encodeSigned(
                    notifyHandler.handle(current, query, clientAddress, this::refresh),
                    stream,
                    options.maxUdpPayloadBytes(),
                    tsigKey);
        }
        if (query.opcode() == DnsQuery.OPCODE_UPDATE) {
            return encodeSigned(
                    dynamicUpdateHandler.handle(current, query, request, clientAddress, this::reload),
                    stream,
                    options.maxUdpPayloadBytes(),
                    tsigKey);
        }
        if (query.opcode() != DnsQuery.OPCODE_QUERY) {
            return encodeSigned(
                    DnsResponse.empty(query, DnsResponseCode.NOTIMP, false),
                    stream,
                    options.maxUdpPayloadBytes(),
                    tsigKey);
        }
        if (DnsZoneTransferHandler.transferQuery(query)) {
            return encodeSigned(
                    zoneTransferHandler.handle(current, query, transferCapableTransport, clientAddress),
                    stream,
                    options.maxUdpPayloadBytes(),
                    tsigKey);
        }
        final InetAddress routingAddress = effectiveClientAddress(query, clientAddress);
        final String scope = current.viewName(routingAddress);
        final DnsResponse policy = policyResponse(current, query, routingAddress);
        if (policy != null) {
            return encodeSigned(policy, stream, options.maxUdpPayloadBytes(), tsigKey);
        }
        final byte[] resolverRequest = tsigKey == null ? request : query.tsigRecord().unsignedMessage();
        if (tsigKey != null) {
            return resolveSigned(current, resolverRequest, query, stream, clientAddress, routingAddress, tsigKey);
        }
        final DnsResponseCache.CachedResponse cached = cache.lookup(query, stream, scope);
        if (cached != null && !cached.stale()) {
            startPrefetch(current, request, query, stream, clientAddress, routingAddress, scope, cached);
            return cached.response();
        }
        final byte[] response;
        try {
            response = resolveAndEncode(current, request, query, stream, clientAddress, routingAddress);
        } catch (final RuntimeException e) {
            if (cached != null && cached.stale()) {
                return cached.response();
            }
            return encode(
                    DnsResponse.empty(query, DnsResponseCode.SERVFAIL, false),
                    stream,
                    options.maxUdpPayloadBytes());
        }
        if (cached != null && cached.stale() && servfail(response)) {
            return cached.response();
        }
        cache.put(query, stream, scope, response);
        return response;
    }

    /**
     * Records response-code and DNSSEC result metrics.
     *
     * @param query    decoded DNS query
     * @param response encoded DNS response
     */
    private void recordResponseMetrics(final DnsQuery query, final byte[] response) {
        if (!metrics.enabled()) {
            return;
        }
        try {
            final DnsDecodedResponse decoded = DnsCodec.decodeResponse(response);
            metrics.responseCode(decoded.responseCode());
            metrics.dnssecResult(dnssecResult(query, decoded));
        } catch (final RuntimeException e) {
            metrics.responseCode(DnsResponseCode.FORMERR);
        }
    }

    /**
     * Classifies a DNSSEC validation result for metrics.
     *
     * @param query    decoded DNS query
     * @param response decoded DNS response
     * @return DNSSEC result category
     */
    private static DnssecResult dnssecResult(final DnsQuery query, final DnsDecodedResponse response) {
        if (!query.dnssecOk()) {
            return DnssecResult.SKIPPED;
        }
        if (response.authenticData()) {
            return DnssecResult.VALIDATED;
        }
        if (response.responseCode() == DnsResponseCode.SERVFAIL) {
            return DnssecResult.FAILED;
        }
        return DnssecResult.INSECURE;
    }

    /**
     * Records one query log entry when query logging is enabled.
     *
     * @param query         decoded DNS query
     * @param response      encoded DNS response
     * @param clientAddress client address, or {@code null} when unavailable
     * @param transport     listener transport
     * @param startedNanos  monotonic start timestamp
     */
    private void recordQueryLog(
            final DnsQuery query,
            final byte[] response,
            final InetAddress clientAddress,
            final DnsTransport transport,
            final long startedNanos) {
        if (!queryLog.enabled()) {
            return;
        }
        try {
            final RuntimeIndex current = index.get();
            final InetAddress routingAddress = effectiveClientAddress(query, clientAddress);
            final DnsResponseCode responseCode = responseCode(response);
            queryLog.log(
                    Instant.now(),
                    clientAddress,
                    transport,
                    current == null ? "none" : current.viewName(routingAddress),
                    query,
                    responseCode,
                    System.nanoTime() - startedNanos,
                    policyAction(current, query, routingAddress),
                    upstreamSummary(current, query, routingAddress));
        } catch (final RuntimeException ignored) {
            return;
        }
    }

    /**
     * Extracts the response code from a wire-format response.
     *
     * @param response encoded DNS response
     * @return decoded response code, or FORMERR when decoding fails
     */
    private static DnsResponseCode responseCode(final byte[] response) {
        try {
            return DnsCodec.decodeResponse(response).responseCode();
        } catch (final RuntimeException e) {
            return DnsResponseCode.FORMERR;
        }
    }

    /**
     * Returns the matched policy action for query logging.
     *
     * @param current        active runtime index, or {@code null}
     * @param query          decoded DNS query
     * @param routingAddress address used for view selection
     * @return policy action token
     */
    private static String policyAction(
            final RuntimeIndex current,
            final DnsQuery query,
            final InetAddress routingAddress) {
        if (current == null) {
            return "none";
        }
        final DnsPolicyRule policy = current.policyIndex(routingAddress)
                .match(query.question(), routingAddress, current.viewName(routingAddress), null);
        return policy == null ? "none" : policy.action().name();
    }

    /**
     * Returns the upstream summary for query logging.
     *
     * @param current        active runtime index, or {@code null}
     * @param query          decoded DNS query
     * @param routingAddress address used for view selection
     * @return upstream summary token
     */
    private static String upstreamSummary(
            final RuntimeIndex current,
            final DnsQuery query,
            final InetAddress routingAddress) {
        if (current == null || query.opcode() != DnsQuery.OPCODE_QUERY
                || !shouldForward(current, query, routingAddress)) {
            return "local";
        }
        final List<DnsUpstream> selected = upstreams(current, query, routingAddress);
        if (selected.isEmpty()) {
            return "none";
        }
        final StringBuilder summary = new StringBuilder();
        for (final DnsUpstream upstream : selected) {
            if (!summary.isEmpty()) {
                summary.append(Symbol.C_OR);
            }
            summary.append(upstream.transport().name()).append("://").append(upstream.host()).append(Symbol.C_COLON)
                    .append(upstream.port());
        }
        return summary.toString();
    }

    /**
     * Returns whether a wire-format response is SERVFAIL.
     *
     * @param response response wire bytes
     * @return true when the response code is SERVFAIL
     */
    private static boolean servfail(final byte[] response) {
        try {
            return DnsCodec.decodeResponse(response).responseCode() == DnsResponseCode.SERVFAIL;
        } catch (final RuntimeException e) {
            return false;
        }
    }

    /**
     * Authenticates a TSIG-signed query.
     *
     * @param query decoded DNS query
     * @return verified TSIG key, or {@code null} when the query is unsigned or invalid
     */
    private DnsTsigKey authenticatedTsig(final RuntimeIndex current, final DnsQuery query) {
        return query.tsigPresent() ? DnsTsig.verify(query, tsigKeys(current)) : null;
    }

    /**
     * Returns TSIG keys from startup options and the active snapshot.
     *
     * @param current active runtime index
     * @return immutable or short-lived combined TSIG keys
     */
    private List<DnsTsigKey> tsigKeys(final RuntimeIndex current) {
        final List<DnsTsigKey> optionKeys = options.tsigKeys();
        final List<DnsTsigKey> snapshotKeys = current.tsigKeys();
        if (optionKeys.isEmpty()) {
            return snapshotKeys;
        }
        if (snapshotKeys.isEmpty()) {
            return optionKeys;
        }
        final ArrayList<DnsTsigKey> keys = new ArrayList<>(optionKeys.size() + snapshotKeys.size());
        keys.addAll(optionKeys);
        keys.addAll(snapshotKeys);
        return List.copyOf(keys);
    }

    /**
     * Resolves a signed query without using the shared raw response cache and signs the response.
     *
     * @param current        active runtime index
     * @param request        unsigned request wire bytes used for forwarding
     * @param query          decoded signed DNS query
     * @param stream         true when the caller expects a TCP-style full response
     * @param clientAddress  transport client address used for ACL checks
     * @param routingAddress address used for view selection
     * @param tsigKey        verified TSIG key used to sign the response
     * @return signed response wire bytes
     */
    private byte[] resolveSigned(
            final RuntimeIndex current,
            final byte[] request,
            final DnsQuery query,
            final boolean stream,
            final InetAddress clientAddress,
            final InetAddress routingAddress,
            final DnsTsigKey tsigKey) {
        try {
            return sign(
                    resolveAndEncode(current, request, query, stream, clientAddress, routingAddress),
                    query,
                    tsigKey);
        } catch (final RuntimeException e) {
            return encodeSigned(
                    DnsResponse.empty(query, DnsResponseCode.SERVFAIL, false),
                    stream,
                    options.maxUdpPayloadBytes(),
                    tsigKey);
        }
    }

    /**
     * Starts a background cache refresh when the cached entry is inside the prefetch window.
     *
     * @param current        active runtime index
     * @param request        original request wire bytes
     * @param query          decoded DNS query
     * @param stream         true when the caller expects a TCP-style full response
     * @param clientAddress  client address, or {@code null} when unavailable
     * @param routingAddress address used for view selection
     * @param scope          cache scope such as the selected view name
     * @param cached         active cache hit metadata
     */
    private void startPrefetch(
            final RuntimeIndex current,
            final byte[] request,
            final DnsQuery query,
            final boolean stream,
            final InetAddress clientAddress,
            final InetAddress routingAddress,
            final String scope,
            final DnsResponseCache.CachedResponse cached) {
        if (!cached.beginPrefetch()) {
            return;
        }
        final Thread thread = ThreadKit.newThread(
                () -> refreshCacheEntry(current, request, query, stream, clientAddress, routingAddress, scope, cached),
                "fabric-dns-prefetch-" + query.question().typeCode(),
                true);
        thread.start();
    }

    /**
     * Refreshes one cache entry without blocking the client response path.
     *
     * @param current        active runtime index
     * @param request        original request wire bytes
     * @param query          decoded DNS query
     * @param stream         true when the caller expects a TCP-style full response
     * @param clientAddress  client address, or {@code null} when unavailable
     * @param routingAddress address used for view selection
     * @param scope          cache scope such as the selected view name
     * @param cached         active cache hit metadata
     */
    private void refreshCacheEntry(
            final RuntimeIndex current,
            final byte[] request,
            final DnsQuery query,
            final boolean stream,
            final InetAddress clientAddress,
            final InetAddress routingAddress,
            final String scope,
            final DnsResponseCache.CachedResponse cached) {
        try {
            cache.put(
                    query,
                    stream,
                    scope,
                    resolveAndEncode(current, request, query, stream, clientAddress, routingAddress));
        } catch (final RuntimeException ignored) {
            return;
        } finally {
            cached.finishPrefetch();
        }
    }

    /**
     * Applies snapshot policy rules to one query.
     *
     * @param current       active runtime index
     * @param query         decoded DNS query
     * @param clientAddress client address, or {@code null} when unavailable
     * @return policy response, or {@code null}
     */
    private static DnsResponse policyResponse(
            final RuntimeIndex current,
            final DnsQuery query,
            final InetAddress clientAddress) {
        final DnsPolicyRule policy = current.policyIndex(clientAddress)
                .match(query.question(), clientAddress, current.viewName(clientAddress), null);
        if (policy != null) {
            return new DnsResponse(query, policy.responseCode(), true, false, false, policy.answers(query.question()),
                    List.of(), List.of(), policyExtendedError(policy, query));
        }
        return null;
    }

    /**
     * Returns the EDNS Extended DNS Error attached to a policy response.
     *
     * @param policy matched policy rule
     * @param query  decoded DNS query
     * @return EDE metadata, or {@code null} when the response must not include EDE
     */
    private static DnsExtendedError policyExtendedError(final DnsPolicyRule policy, final DnsQuery query) {
        if (!query.edns()) {
            return null;
        }
        return policy.extendedError();
    }

    /**
     * Resolves a query against the local runtime index.
     *
     * @param current       active runtime index
     * @param query         decoded DNS query
     * @param clientAddress client address, or {@code null} when unavailable
     * @return DNS response model
     */
    private static DnsResponse resolveLocal(
            final RuntimeIndex current,
            final DnsQuery query,
            final InetAddress clientAddress) {
        final DnsAuthoritativeResolver resolver = new DnsAuthoritativeResolver(current);
        final DnsResolution resolution = resolver.resolve(query.question(), clientAddress, query.dnssecOk());
        return new DnsResponse(query, resolution.responseCode(), resolution.authoritative(), false, false,
                resolution.answers(), resolution.authorities(), List.of());
    }

    /**
     * Returns whether a query should be forwarded to upstream DNS servers.
     *
     * @param current       active runtime index
     * @param query         decoded DNS query
     * @param clientAddress client address, or {@code null} when unavailable
     * @return true when the query is outside local authority or targets a forward zone
     */
    private static boolean shouldForward(
            final RuntimeIndex current,
            final DnsQuery query,
            final InetAddress clientAddress) {
        final DnsZone zone = current.findZone(query.question().name(), clientAddress);
        final boolean forwardZone = zone == null || zone.mode() == DnsZoneMode.FORWARD
                || zone.mode() == DnsZoneMode.STUB;
        return forwardZone && !upstreams(current, query, clientAddress).isEmpty();
    }

    /**
     * Resolves and encodes a response without reading or writing the response cache.
     *
     * @param current        active runtime index
     * @param request        original request wire bytes
     * @param query          decoded DNS query
     * @param stream         true when the caller expects a TCP-style full response
     * @param clientAddress  transport client address used for ACL checks
     * @param routingAddress address used for view selection
     * @return response wire bytes
     */
    private byte[] resolveAndEncode(
            final RuntimeIndex current,
            final byte[] request,
            final DnsQuery query,
            final boolean stream,
            final InetAddress clientAddress,
            final InetAddress routingAddress) {
        if (shouldForward(current, query, routingAddress)) {
            return recursionAllowed(clientAddress) ? forward(current, request, query, routingAddress, stream)
                    : encode(
                            DnsResponse.empty(query, DnsResponseCode.REFUSED, false),
                            stream,
                            options.maxUdpPayloadBytes());
        }
        return encode(resolveLocal(current, query, routingAddress), stream, options.maxUdpPayloadBytes());
    }

    /**
     * Forwards a query to upstream DNS servers.
     *
     * @param current       active runtime index
     * @param request       original request wire bytes
     * @param query         decoded DNS query
     * @param clientAddress client address, or {@code null} when unavailable
     * @param stream        true when the caller expects a TCP-style full response
     * @return response wire bytes
     */
    private byte[] forward(
            final RuntimeIndex current,
            final byte[] request,
            final DnsQuery query,
            final InetAddress clientAddress,
            final boolean stream) {
        final DnsZone zone = current.findZone(query.question().name(), clientAddress);
        final List<DnsUpstream> upstreams = upstreams(current, query, clientAddress);
        if (zone != null && zone.mode() == DnsZoneMode.FORWARD) {
            return new DnsForwarder(upstreams, metrics).forward(request);
        }
        final long started = System.nanoTime();
        try {
            return encode(
                    new DnsRecursiveResolver(upstreams, current.dnssecTrustAnchors()).resolve(query, request),
                    stream,
                    options.maxUdpPayloadBytes());
        } finally {
            metrics.recursiveLatency(System.nanoTime() - started);
        }
    }

    /**
     * Selects upstream DNS servers for a forwarded query.
     *
     * @param current       active runtime index
     * @param query         decoded DNS query
     * @param clientAddress client address, or {@code null} when unavailable
     * @return zone-specific upstreams or global upstreams
     */
    private static List<DnsUpstream> upstreams(
            final RuntimeIndex current,
            final DnsQuery query,
            final InetAddress clientAddress) {
        final DnsZone zone = current.findZone(query.question().name(), clientAddress);
        if (zone != null && (zone.mode() == DnsZoneMode.FORWARD || zone.mode() == DnsZoneMode.STUB)
                && !zone.upstreams().isEmpty()) {
            return zone.upstreams();
        }
        return current.upstreams();
    }

    /**
     * Selects the address used for view and policy routing.
     *
     * @param query         decoded DNS query
     * @param clientAddress transport client address, or {@code null} when unavailable
     * @return EDNS Client Subnet address when present, otherwise the transport client address
     */
    private static InetAddress effectiveClientAddress(final DnsQuery query, final InetAddress clientAddress) {
        return query.clientSubnet() == null ? clientAddress : query.clientSubnet().address();
    }

    /**
     * Returns whether a client address is allowed to use forwarding or recursive resolution.
     *
     * @param clientAddress client address, or {@code null} when unavailable
     * @return true when the client is allowed
     */
    private boolean recursionAllowed(final InetAddress clientAddress) {
        if (clientAddress == null) {
            return false;
        }
        for (final CidrBlock cidr : options.recursionAllowedCidrs()) {
            if (cidr.contains(clientAddress)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Encodes a DNS response model.
     *
     * @param response           response model
     * @param stream             true when a TCP-style full response is required
     * @param maxUdpPayloadBytes maximum UDP payload size
     * @return response wire bytes
     */
    private static byte[] encode(final DnsResponse response, final boolean stream, final int maxUdpPayloadBytes) {
        return stream ? DnsCodec.encodeResponse(response) : DnsCodec.encodeUdpResponse(response, maxUdpPayloadBytes);
    }

    /**
     * Encodes and TSIG-signs a DNS response when the request was authenticated.
     *
     * @param response           response model
     * @param stream             true when a TCP-style full response is required
     * @param maxUdpPayloadBytes maximum UDP payload size
     * @param tsigKey            verified TSIG key, or {@code null} for unsigned responses
     * @return response wire bytes
     */
    private static byte[] encodeSigned(
            final DnsResponse response,
            final boolean stream,
            final int maxUdpPayloadBytes,
            final DnsTsigKey tsigKey) {
        return sign(encode(response, stream, maxUdpPayloadBytes), response.query(), tsigKey);
    }

    /**
     * TSIG-signs response bytes when a verified key is available.
     *
     * @param response response wire bytes
     * @param query    decoded DNS query
     * @param tsigKey  verified TSIG key, or {@code null} for unsigned responses
     * @return original or signed response wire bytes
     */
    private static byte[] sign(final byte[] response, final DnsQuery query, final DnsTsigKey tsigKey) {
        return tsigKey == null ? response : DnsTsig.signResponse(response, query, tsigKey);
    }

    /**
     * Notifies snapshot listeners that a snapshot was accepted.
     *
     * @param snapshot accepted snapshot
     */
    private void notifyAccepted(final DnsSnapshot snapshot) {
        for (final DnsSnapshotListener listener : options.snapshotListeners()) {
            listener.onAccepted(snapshot);
        }
    }

    /**
     * Notifies snapshot listeners that a snapshot was rejected.
     *
     * @param snapshot rejected snapshot
     * @param cause    rejection cause
     */
    private void notifyRejected(final DnsSnapshot snapshot, final RuntimeException cause) {
        for (final DnsSnapshotListener listener : options.snapshotListeners()) {
            listener.onRejected(snapshot, cause);
        }
    }

    /**
     * Notifies snapshot listeners that the previous snapshot remained active.
     *
     * @param snapshot active snapshot retained after failure
     * @param cause    failure that prevented replacement
     */
    private void notifyRolledBack(final DnsSnapshot snapshot, final RuntimeException cause) {
        for (final DnsSnapshotListener listener : options.snapshotListeners()) {
            listener.onRolledBack(snapshot, cause);
        }
    }

    /**
     * Returns the active runtime index.
     *
     * @return active runtime index
     */
    private RuntimeIndex requireIndex() {
        final RuntimeIndex current = index.get();
        if (current == null) {
            throw new StatefulException("DNS server snapshot has not been loaded");
        }
        return current;
    }

    /**
     * Closes endpoint handles and suppresses later failures onto the first failure.
     *
     * @param closing handles to close
     */
    private static void closeHandles(final List<AutoCloseable> closing) {
        RuntimeException failure = null;
        for (final AutoCloseable handle : closing) {
            try {
                handle.close();
            } catch (final Exception e) {
                final RuntimeException runtime = e instanceof RuntimeException existing ? existing
                        : new StatefulException("Unable to close DNS endpoint", e);
                if (failure == null) {
                    failure = runtime;
                } else {
                    failure.addSuppressed(runtime);
                }
            }
        }
        if (failure != null) {
            throw failure;
        }
    }

}
