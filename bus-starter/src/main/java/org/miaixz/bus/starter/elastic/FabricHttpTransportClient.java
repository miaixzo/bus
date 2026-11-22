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
package org.miaixz.bus.starter.elastic;

import java.io.*;
import java.net.URI;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ConnectionException;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.thread.ExponentialBackoff;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.*;
import org.miaixz.bus.fabric.network.tls.TlsPolicy;
import org.miaixz.bus.fabric.network.tls.TlsSettings;
import org.miaixz.bus.fabric.network.tls.context.TlsContext;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.protocol.MonoCall;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.protocol.http.HttpX;
import org.miaixz.bus.fabric.protocol.http.auth.HttpAuth;
import org.miaixz.bus.fabric.protocol.http.body.PayloadBody;
import org.miaixz.bus.fabric.protocol.http.chain.HttpChain;
import org.miaixz.bus.fabric.protocol.http.retry.HttpRetryPolicy;
import org.miaixz.bus.fabric.registry.policy.PoolPolicy;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;

import co.elastic.clients.transport.TransportOptions;
import co.elastic.clients.transport.http.TransportHttpClient;
import co.elastic.clients.util.BinaryData;

/**
 * {@link TransportHttpClient} implementation backed by the Bus Fabric HTTP runtime.
 * <p>
 * The adapter keeps the typed Elasticsearch client API while replacing the Apache HttpClient 5 transport. Requests are
 * executed by {@link Fabric}, including connection pooling, TLS, proxy handling, gzip decoding and timeouts. Nodes are
 * selected in round-robin order among eligible nodes, with failed nodes isolated between calls.
 * </p>
 *
 * @author Kimi Liu
 */
final class FabricHttpTransportClient implements TransportHttpClient {

    /**
     * Fabric runtime context owning the connection pool, dispatcher and TLS state.
     */
    private final Context context;

    /**
     * Shared clock; node isolation uses monotonic time while HTTP dates use wall time.
     */
    private final Clock clock;

    /**
     * Opt-in Fabric status retry policy, attached only to safe reads without SDK-owned retries.
     */
    private final HttpRetryPolicy statusRetryPolicy;

    /**
     * Cluster node URIs used for round-robin selection.
     */
    private final List<URI> nodes;

    /**
     * Optional precomputed Basic authorization header.
     */
    private final String authorization;

    /**
     * Executor used to bridge Fabric's blocking exchange into the asynchronous transport contract.
     */
    private final ExecutorService executor;

    /**
     * Round-robin cursor.
     */
    private final AtomicInteger cursor = new AtomicInteger();

    /**
     * Consecutive failures and monotonic retry deadline for each isolated node.
     */
    private final ConcurrentMap<URI, Pair<Integer, Long>> deadNodes = new ConcurrentHashMap<>();

    /**
     * Rest5-compatible node isolation: one minute initially, increasing to at most thirty minutes.
     */
    private static final ExponentialBackoff NODE_BACKOFF = new ExponentialBackoff(Duration.ofMinutes(1), Math.sqrt(2),
            Duration.ofMinutes(30), false);

    /**
     * Active asynchronous requests, cancelled when the adapter closes.
     */
    private final Set<CompletableFuture<Response>> pending = ConcurrentHashMap.newKeySet();

    /**
     * POST endpoints whose request bodies only describe repeatable read operations.
     */
    private static final Set<String> READ_ENDPOINTS = Set.of(
            "es/search",
            "es/msearch",
            "es/count",
            "es/mget",
            "es/field_caps",
            "es/explain",
            "es/search_template",
            "es/msearch_template",
            "es/terms_enum",
            "es/indices.validate_query");

    /**
     * Creates a Fabric-backed HTTP client from the bound Elasticsearch properties.
     *
     * @param properties bound Elasticsearch properties
     */
    FabricHttpTransportClient(ElasticProperties properties) {
        this(properties, null);
    }

    /**
     * Creates a Fabric-backed HTTP client with an optional custom TLS policy.
     *
     * @param properties bound Elasticsearch properties
     * @param tlsPolicy  custom TLS policy, or {@code null} to use the Elasticsearch defaults
     */
    FabricHttpTransportClient(ElasticProperties properties, TlsPolicy tlsPolicy) {
        List<URI> hosts = new ArrayList<>();
        for (String host : properties.getHostList()) {
            hosts.add(new Node(ElasticProperties.nodeUri(properties.getSchema(), host)).uri());
        }
        if (hosts.isEmpty()) {
            throw new InternalException("Elasticsearch cluster host information is not configured.");
        }
        this.nodes = List.copyOf(new LinkedHashSet<>(hosts));
        this.authorization = basicAuthorization(properties.getUsername(), properties.getPassword());
        this.context = buildContext(properties, tlsPolicy, this.nodes.size());
        this.clock = this.context.clock();
        this.statusRetryPolicy = HttpRetryPolicy.builder().maxAttempts(1).maxStaleRetries(0).maxRedirects(0)
                .retryOnConnectionFailure(false).retryStatuses(Set.of(429)).replaySafe(true)
                .maxDelay(Duration.ofMillis(properties.getSocketTimeout())).build();
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * Builds the Fabric context, mapping the Elasticsearch timeout, pool, TLS and keepalive settings.
     *
     * @param properties bound Elasticsearch properties
     * @param customTls  custom TLS policy, or {@code null}
     * @param nodeCount  number of distinct configured nodes
     * @return configured Fabric context
     */
    private static Context buildContext(ElasticProperties properties, TlsPolicy customTls, int nodeCount) {
        int total = properties.getMaxConnectTotal();
        // Keep capacity for a new node instead of filling the global pool with other nodes' idle connections.
        int maxIdle = Math.min(Normal._16, nodeCount > 1 ? total - 1 : total);
        PoolPolicy pool = PoolPolicy.builder().maxIdle(maxIdle).maxConnections(total)
                .maxConnectionsPerDestination(properties.getMaxConnectPerRoute())
                .acquireTimeout(Duration.ofMillis(properties.getConnectionRequestTimeout())).build();
        Timeout timeout = Timeout.builder().connect(Duration.ofMillis(properties.getConnectTimeout()))
                .read(Duration.ofMillis(properties.getSocketTimeout()))
                .call(Duration.ofMillis((long) properties.getConnectTimeout() + properties.getSocketTimeout())).build();
        List<String> protocols = protocols(properties.getHttpVersion());
        TlsPolicy tlsPolicy;
        if (customTls == null) {
            // Offer h2 first and fall back to HTTP/1.1 when the peer does not negotiate ALPN.
            // Keep TLS 1.2 for Elasticsearch deployments that have not enabled TLS 1.3, and use the
            // platform cipher suites instead of the single TLS 1.3 suite selected by the Fabric default.
            TlsSettings tls = TlsPolicy.defaults().settings().toBuilder().versions(List.of("TLSv1.3", "TLSv1.2"))
                    .allEnabledCipherSuites().applicationProtocols(protocols).build();
            tlsPolicy = TlsPolicy.of(TlsContext.defaults(), tls);
        } else {
            // Keep the caller's trust and protocol policy, but apply the configured ALPN preference.
            TlsSettings tls = customTls.settings().toBuilder().applicationProtocols(protocols).build();
            tlsPolicy = TlsPolicy.of(customTls.context(), tls);
        }
        Options options = Options.of(PoolPolicy.OPTION, pool).with(TlsPolicy.OPTION, tlsPolicy)
                .with(
                        HttpRetryPolicy.OPTION,
                        HttpRetryPolicy.builder().maxAttempts(0).maxStaleRetries(0).maxRedirects(0)
                                .retryOnConnectionFailure(false).build())
                .with(Builder.HTTP2_PING_INTERVAL, properties.getHttp2PingInterval())
                .with(Builder.HTTP2_PING_TIMEOUT, properties.getHttp2PingTimeout());
        return Context.builder().options(options).timeout(timeout).build();
    }

    /**
     * Resolves ALPN identifiers from the configured HTTP version.
     *
     * @param version {@code auto}, {@code 1.1} or {@code 2}
     * @return ALPN identifiers in preference order
     */
    private static List<String> protocols(String version) {
        if ("1.1".equals(version)) {
            return List.of("http/1.1");
        }
        if ("2".equals(version)) {
            return List.of("h2");
        }
        return List.of("h2", "http/1.1");
    }

    /**
     * Executes a blocking request through Fabric.
     *
     * @param endpointId Elasticsearch endpoint identifier
     * @param node       target node, or {@code null} to let this client choose one
     * @param request    HTTP request
     * @param options    transport options whose headers and parameters take precedence
     * @return HTTP response
     * @throws IOException if the exchange fails
     */
    @Override
    public Response performRequest(String endpointId, Node node, Request request, TransportOptions options)
            throws IOException {
        return execute(
                endpointId,
                candidates(node, options),
                request,
                options,
                toBytes(request.body()),
                Cancellation.none());
    }

    /**
     * Builds a Fabric exchange for one Elasticsearch request.
     *
     * @param selected   target node
     * @param request    HTTP request
     * @param options    transport options
     * @param body       serialized request body, or {@code null} when absent
     * @param safeReplay whether the operation can be repeated after a response
     * @return Fabric HTTP builder
     */
    private HttpX.Builder builder(
            Node selected,
            Request request,
            TransportOptions options,
            byte[] body,
            boolean safeReplay) {
        URI base = selected.uri();
        Map<String, String> query = new LinkedHashMap<>(request.queryParams());
        if (options != null && options.queryParameters() != null) {
            query.putAll(options.queryParameters());
        }
        HttpX.Builder builder = Fabric.http(context).method(request.method() == null ? "GET" : request.method())
                .url(base.resolve(request.path()).toString()).query(query);
        if (safeReplay && !sdkRetries(options)) {
            builder.retryPolicy(this.statusRetryPolicy);
        }
        Headers.Builder merged = Headers.builder();
        request.headers().forEach(merged::set);
        if (options != null && options.headers() != null) {
            for (Map.Entry<String, String> entry : options.headers()) {
                merged.set(entry.getKey(), entry.getValue());
            }
        }
        Headers headers = merged.build();
        if (this.authorization != null && !headers.contains("Authorization")) {
            headers = headers.with("Authorization", this.authorization);
        }
        builder.headers(headers);
        String contentType = headers.get("Content-Type");
        if (body != null && body.length > 0) {
            builder.body(body, mediaType(contentType));
        }
        if (contentType != null) {
            builder.replaceHeader("Content-Type", contentType);
        }
        return builder;
    }

    /**
     * Wraps a Fabric response in the transport contract without materializing the body.
     *
     * @param selected serving node
     * @param response Fabric response
     * @return transport response whose body is streamed from Fabric
     */
    private static Response toResponse(Node selected, HttpResponse response) {
        String contentType = response.headers().get(Http.Header.CONTENT_TYPE);
        return new FabricResponse(selected, response.code(), response.headers(), response.body(),
                StringKit.isBlank(contentType) ? MediaType.APPLICATION_JSON : contentType, response);
    }

    /**
     * Executes a request asynchronously through a Fabric call.
     * <p>
     * The returned future is backed by {@link Call#cancel()}, so cancelling it aborts the in-flight exchange instead of
     * only marking the future as cancelled. A virtual thread waits for the call result, keeping the blocking wait
     * cheap.
     * </p>
     *
     * @param endpointId Elasticsearch endpoint identifier
     * @param node       target node, or {@code null}
     * @param request    HTTP request
     * @param options    transport options
     * @return future completed with the HTTP response
     */
    @Override
    public CompletableFuture<Response> performRequestAsync(
            String endpointId,
            Node node,
            Request request,
            TransportOptions options) {
        try {
            final List<Node> candidates = candidates(node, options);
            final byte[] body = toBytes(request.body());
            final Cancellation cancellation = Cancellation.create();
            Call<Response> operation = MonoCall.create(
                    "elastic-request",
                    "elastic-request",
                    this.context.reactor().dispatcher(),
                    EventObserver.noop(),
                    null,
                    () -> execute(endpointId, candidates, request, options, body, cancellation),
                    cancellation::cancel);
            CompletableFuture<Response> future = operation.toFuture(
                    this.executor,
                    IoKit::closeQuietly,
                    failure -> failure instanceof InternalException && failure.getCause() instanceof IOException
                            ? failure.getCause()
                            : failure);
            this.pending.add(future);
            future.whenComplete((response, failure) -> this.pending.remove(future));
            if (this.executor.isShutdown()) {
                future.cancel(true);
            }
            return future;
        } catch (Throwable exception) {
            return CompletableFuture.failedFuture(exception);
        }
    }

    /**
     * Executes the same bounded node traversal for blocking and asynchronous callers.
     *
     * @param endpointId   Elasticsearch endpoint identifier
     * @param candidates   target nodes in attempt order
     * @param request      HTTP request
     * @param options      effective Elasticsearch transport options
     * @param body         serialized request body, or {@code null} when absent
     * @param cancellation cancellation scope shared with the caller
     * @return response from the node whose exchange completed
     * @throws IOException           if no candidate yields a deliverable response
     * @throws CancellationException if the operation is cancelled or the current thread is interrupted
     */
    private Response execute(
            String endpointId,
            List<Node> candidates,
            Request request,
            TransportOptions options,
            byte[] body,
            Cancellation cancellation) throws IOException {
        boolean safeReplay = safeReplay(endpointId, request.method());
        List<IOException> failures = new ArrayList<>();
        for (int attempt = 0; attempt < candidates.size(); attempt++) {
            cancellation.throwIfCancelled();
            if (Thread.currentThread().isInterrupted()) {
                throw new CancellationException("Elasticsearch request was cancelled");
            }
            if (this.nodes.size() > 1) {
                this.context.reactor().directory().connectionPool().evictIdle();
            }
            Node selected = candidates.get(attempt);
            Call<HttpResponse> call = builder(selected, request, options, body, safeReplay).call();
            Runnable unregister = cancellation.onCancel(call::cancel);
            try {
                HttpResponse response;
                try {
                    response = call.execute();
                } catch (RuntimeException exception) {
                    if (retryable(exception, true)) {
                        failed(selected);
                    }
                    IOException failure = new IOException("Elasticsearch HTTP exchange failed", exception);
                    if (attempt + 1 < candidates.size() && retryable(exception, safeReplay)) {
                        failures.add(failure);
                        continue;
                    }
                    failures.forEach(failure::addSuppressed);
                    throw failure;
                }
                boolean unavailable = response.code() == 502 || response.code() == 503 || response.code() == 504;
                if (unavailable) {
                    failed(selected);
                } else {
                    this.deadNodes.remove(selected.uri());
                }
                if (attempt + 1 < candidates.size() && safeReplay && unavailable) {
                    discard(response, failures);
                    continue;
                }
                try {
                    return toResponse(selected, response);
                } catch (RuntimeException | Error exception) {
                    response.close();
                    throw exception;
                }
            } finally {
                unregister.run();
            }
        }
        throw new IOException("No Elasticsearch nodes available");
    }

    /**
     * Selects eligible nodes once, or probes the earliest isolated node if none are eligible.
     *
     * @param node    explicit node, or {@code null} to select from the configured nodes
     * @param options transport options controlling SDK retry ownership
     * @return ordered candidates for this request
     */
    private List<Node> candidates(Node node, TransportOptions options) {
        if (node != null) {
            return List.of(node);
        }
        long now = this.clock.nanos();
        List<URI> eligible = new ArrayList<>();
        URI earliest = null;
        long earliestDelay = Long.MAX_VALUE;
        for (URI uri : this.nodes) {
            Pair<Integer, Long> state = this.deadNodes.get(uri);
            if (state == null || now - state.getRight() >= 0) {
                eligible.add(uri);
            } else if (state.getRight() - now < earliestDelay) {
                earliest = uri;
                earliestDelay = state.getRight() - now;
            }
        }
        if (eligible.isEmpty()) {
            return List.of(new Node(earliest));
        }
        int start = Math.floorMod(this.cursor.getAndIncrement(), eligible.size());
        int count = sdkRetries(options) ? 1 : eligible.size();
        List<Node> selected = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            selected.add(new Node(eligible.get((start + index) % eligible.size())));
        }
        return selected;
    }

    /**
     * Records a node failure and sets the time at which the node can be selected again.
     *
     * @param node node whose exchange failed
     */
    private void failed(Node node) {
        this.deadNodes.compute(node.uri(), (uri, previous) -> {
            int count = previous == null ? 1
                    : previous.getLeft() == Integer.MAX_VALUE ? Integer.MAX_VALUE : previous.getLeft() + 1;
            return Pair.of(count, this.clock.nanos() + NODE_BACKOFF.next(count).toNanos());
        });
    }

    /**
     * Checks whether the SDK controls additional attempts.
     *
     * @param options transport options, or {@code null} when none are supplied
     * @return {@code true} if the supplied options enable SDK retries
     */
    private static boolean sdkRetries(TransportOptions options) {
        return options != null && options.retryConfig() != null && options.retryConfig().isEnabled();
    }

    /**
     * Releases the previous response before a follow-up attempt and captures any close failure.
     *
     * @param response response being discarded
     * @param failures prior failures that will be attached if the exchange ultimately fails
     */
    private static void discard(HttpResponse response, List<IOException> failures) {
        try {
            response.close();
        } catch (RuntimeException exception) {
            failures.add(new IOException("Failed to close discarded Elasticsearch response", exception));
        }
    }

    /**
     * Checks whether the HTTP operation is eligible for replay after a possible delivery.
     *
     * @param endpointId Elasticsearch endpoint identifier, or {@code null} if unspecified
     * @param method     HTTP method, defaulting to {@code GET} when {@code null}
     * @return {@code true} for safe read methods or a recognized read-only {@code POST} endpoint
     */
    static boolean safeReplay(String endpointId, String method) {
        method = method == null ? "GET" : method;
        return "GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method)
                || "POST".equalsIgnoreCase(method) && endpointId != null && READ_ENDPOINTS.contains(endpointId);
    }

    /**
     * Classifies a transport failure using the exchange delivery state or the connection's route-switch eligibility.
     *
     * @param failure    failure chain to classify
     * @param safeReplay whether the operation can be repeated after possible delivery
     * @return {@code true} if another node can be tried
     */
    private static boolean retryable(Throwable failure, boolean safeReplay) {
        if (Thread.currentThread().isInterrupted()) {
            return false;
        }
        HttpChain.ExchangeFailure exchange = null;
        ConnectionException connection = null;
        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Throwable cause = failure; cause != null && visited.add(cause); cause = cause.getCause()) {
            if (cause instanceof CancellationException || cause instanceof InterruptedException
                    || cause instanceof InterruptedIOException && !(cause instanceof java.net.SocketTimeoutException)) {
                return false;
            }
            if (exchange == null) {
                exchange = HttpChain.ExchangeFailure.from(cause);
            }
            if (connection == null && cause instanceof ConnectionException current) {
                connection = current;
            }
        }
        if (exchange != null) {
            return switch (exchange.reason()) {
                case CANCELLED, TLS, PROTOCOL -> false;
                case DNS, CONNECT, IO, TIMEOUT, REFUSED_STREAM, GOAWAY_UNPROCESSED -> exchange
                        .deliveryState() != HttpChain.DeliveryState.MAYBE_PROCESSED || safeReplay;
                default -> false;
            };
        }
        return connection != null && connection.canSwitchRoute();
    }

    /**
     * Stops the asynchronous executor, cancels pending exchanges, and releases the Fabric runtime.
     */
    @Override
    public void close() {
        this.executor.shutdownNow();
        this.pending.forEach(future -> future.cancel(true));
        this.context.close();
    }

    /**
     * Creates a Basic authorization header when credentials are configured.
     *
     * @param username configured username
     * @param password configured password
     * @return authorization header value, or {@code null}
     */
    private static String basicAuthorization(String username, String password) {
        if (StringKit.isBlank(username)) {
            return null;
        }
        return HttpAuth.basic(username, password == null ? "" : password).value();
    }

    /**
     * Concatenates the request body buffers.
     *
     * @param body request body buffers, or {@code null}
     * @return concatenated bytes, or {@code null}
     */
    private static byte[] toBytes(Iterable<ByteBuffer> body) {
        if (body == null) {
            return null;
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (ByteBuffer buffer : body) {
            ByteBuffer copy = buffer.duplicate();
            byte[] chunk = new byte[copy.remaining()];
            copy.get(chunk);
            output.write(chunk, 0, chunk.length);
        }
        return output.toByteArray();
    }

    /**
     * Parses a content type with a safe JSON fallback.
     *
     * @param contentType raw content type
     * @return parsed media type
     */
    private static MediaType mediaType(String contentType) {
        if (StringKit.isBlank(contentType)) {
            return MediaType.APPLICATION_JSON_TYPE;
        }
        try {
            return MediaType.valueOf(contentType);
        } catch (RuntimeException ignored) {
            return MediaType.APPLICATION_JSON_TYPE;
        }
    }

    /**
     * Transport response backed by a streaming Fabric response body.
     */
    private static final class FabricResponse implements Response {

        /**
         * Node that served the request.
         */
        private final Node node;

        /**
         * HTTP status code.
         */
        private final int status;

        /**
         * Response headers.
         */
        private final Headers headers;

        /**
         * Streaming response body.
         */
        private final PayloadBody body;

        /**
         * Response content type.
         */
        private final String contentType;

        /**
         * Original Fabric response.
         */
        private final HttpResponse original;

        /**
         * Creates a transport response.
         *
         * @param node        serving node
         * @param status      HTTP status code
         * @param headers     response headers
         * @param body        streaming response body
         * @param contentType response content type
         * @param original    original Fabric response
         */
        private FabricResponse(Node node, int status, Headers headers, PayloadBody body, String contentType,
                HttpResponse original) {
            this.node = node;
            this.status = status;
            this.headers = headers;
            this.body = body;
            this.contentType = contentType;
            this.original = original;
        }

        /**
         * Identifies the Elasticsearch node that served the request.
         *
         * @return responding node
         */
        @Override
        public Node node() {
            return this.node;
        }

        /**
         * Returns the status of the underlying HTTP response.
         *
         * @return HTTP status code
         */
        @Override
        public int statusCode() {
            return this.status;
        }

        /**
         * Looks up the last value of a response header.
         *
         * @param name case-insensitive header name
         * @return last matching value, or {@code null} when absent
         */
        @Override
        public String header(String name) {
            return this.headers.get(name);
        }

        /**
         * Looks up all values for a response header.
         *
         * @param name case-insensitive header name
         * @return header values in wire order, or an empty list when absent
         */
        @Override
        public List<String> headers(String name) {
            return this.headers.values(name);
        }

        /**
         * Wraps the response payload for the Elasticsearch decoder.
         *
         * @return streaming view of the response body
         */
        @Override
        public BinaryData body() {
            return new FabricBinaryData(this.body, this.contentType);
        }

        /**
         * Exposes the underlying Fabric response.
         *
         * @return original Fabric HTTP response
         */
        @Override
        public Object originalResponse() {
            return this.original;
        }

        /**
         * Closes the underlying response and releases its transport resources.
         */
        @Override
        public void close() {
            this.original.close();
        }

    }

    /**
     * Streaming {@link BinaryData} backed by a Fabric payload.
     * <p>
     * The Elasticsearch transport parses JSON directly from {@link #asInputStream()}, so successful responses are never
     * buffered as a single byte array. Materializing accessors remain available for bounded fallback paths such as
     * error bodies and binary endpoints.
     * </p>
     */
    private static final class FabricBinaryData implements BinaryData {

        /**
         * Upper bound for fallback materialization.
         */
        private static final long MAX_MATERIALIZE_BYTES = Normal.MEBI_64;

        /**
         * Streaming payload body.
         */
        private final PayloadBody body;

        /**
         * Response content type.
         */
        private final String contentType;

        /**
         * Creates a streaming binary data view.
         *
         * @param body        payload body
         * @param contentType content type
         */
        private FabricBinaryData(PayloadBody body, String contentType) {
            this.body = body;
            this.contentType = contentType;
        }

        /**
         * Returns the media type used to decode the response.
         *
         * @return response content type
         */
        @Override
        public String contentType() {
            return this.contentType;
        }

        /**
         * Transfers the response payload and closes its input stream.
         *
         * @param output destination stream, which remains open
         * @throws IOException if the payload cannot be copied or closed
         */
        @Override
        public void writeTo(OutputStream output) throws IOException {
            try (InputStream input = asInputStream()) {
                input.transferTo(output);
            }
        }

        /**
         * Materializes the response body within the configured fallback limit.
         *
         * @return buffer containing the body bytes
         */
        @Override
        public ByteBuffer asByteBuffer() {
            return ByteBuffer.wrap(this.body.bytes(MAX_MATERIALIZE_BYTES));
        }

        /**
         * Opens a stream over the response payload.
         *
         * @return stream that the consumer must close
         */
        @Override
        public InputStream asInputStream() {
            return IoKit.buffer(this.body.payload().source()).inputStream();
        }

        /**
         * Indicates whether the streaming body can be read repeatedly.
         *
         * @return {@code false} for this single-use body
         */
        @Override
        public boolean isRepeatable() {
            return false;
        }

        /**
         * Returns the reported response payload length.
         *
         * @return body length in bytes, or {@code -1} if unknown
         */
        @Override
        public long size() {
            return this.body.length();
        }

    }

}
