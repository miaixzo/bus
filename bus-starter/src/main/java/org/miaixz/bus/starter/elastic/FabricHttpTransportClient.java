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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.*;
import org.miaixz.bus.fabric.network.tls.TlsPolicy;
import org.miaixz.bus.fabric.network.tls.TlsSettings;
import org.miaixz.bus.fabric.network.tls.context.TlsContext;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.protocol.http.HttpX;
import org.miaixz.bus.fabric.protocol.http.body.PayloadBody;
import org.miaixz.bus.fabric.registry.policy.PoolPolicy;

import co.elastic.clients.transport.TransportOptions;
import co.elastic.clients.transport.http.TransportHttpClient;
import co.elastic.clients.util.BinaryData;

/**
 * {@link TransportHttpClient} implementation backed by the Bus Fabric HTTP runtime.
 * <p>
 * The adapter keeps the typed Elasticsearch client API while replacing the Apache HttpClient 5 transport. Requests are
 * executed by {@link Fabric}, including connection pooling, TLS, proxy handling, gzip decoding and timeouts. Nodes are
 * selected in round-robin order because the Elasticsearch transport always passes a {@code null} node.
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
            hosts.add(buildUri(properties, host));
        }
        if (hosts.isEmpty()) {
            throw new InternalException("Elasticsearch cluster host information is not configured.");
        }
        this.nodes = List.copyOf(hosts);
        this.authorization = basicAuthorization(properties.getUsername(), properties.getPassword());
        this.context = buildContext(properties, tlsPolicy);
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * Builds the Fabric context, mapping the Elasticsearch timeout, pool, TLS and keepalive settings.
     *
     * @param properties bound Elasticsearch properties
     * @param customTls  custom TLS policy, or {@code null}
     * @return configured Fabric context
     */
    private static Context buildContext(ElasticProperties properties, TlsPolicy customTls) {
        int total = properties.getMaxConnectTotal();
        PoolPolicy pool = PoolPolicy.builder().maxIdle(Math.min(Normal._16, total)).maxConnections(total)
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
        Node selected = node == null ? new Node(selectNode()) : node;
        return toResponse(selected, builder(selected, request, options).execute());
    }

    /**
     * Builds a Fabric exchange for one Elasticsearch request.
     *
     * @param selected target node
     * @param request  HTTP request
     * @param options  transport options
     * @return Fabric HTTP builder
     */
    private HttpX.Builder builder(Node selected, Request request, TransportOptions options) {
        URI base = selected.uri();
        Map<String, String> query = new LinkedHashMap<>(request.queryParams());
        if (options != null && options.queryParameters() != null) {
            query.putAll(options.queryParameters());
        }
        HttpX.Builder builder = Fabric.http(context).method(request.method() == null ? "GET" : request.method())
                .url(base.resolve(request.path()).toString()).query(query);
        request.headers().forEach(builder::header);
        if (options != null && options.headers() != null) {
            for (Map.Entry<String, String> entry : options.headers()) {
                builder.replaceHeader(entry.getKey(), entry.getValue());
            }
        }
        if (this.authorization != null) {
            builder.replaceHeader("Authorization", this.authorization);
        }
        byte[] body = toBytes(request.body());
        String contentType = header(request.headers(), "Content-Type");
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
        final Node selected = node == null ? new Node(selectNode()) : node;
        final Call<HttpResponse> call = builder(selected, request, options).enqueue();
        final CompletableFuture<Response> future = new CompletableFuture<>() {

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                boolean cancelled = super.cancel(mayInterruptIfRunning);
                if (cancelled) {
                    call.cancel();
                }
                return cancelled;
            }

        };
        this.executor.execute(() -> {
            try {
                future.complete(toResponse(selected, call.await()));
            } catch (Throwable exception) {
                future.completeExceptionally(exception);
            }
        });
        return future;
    }

    /**
     * Releases the Fabric runtime and the asynchronous executor.
     *
     * @throws IOException if the context cannot be closed
     */
    @Override
    public void close() throws IOException {
        this.executor.shutdownNow();
        this.context.close();
    }

    /**
     * Selects the next node in round-robin order.
     *
     * @return selected node URI
     */
    private URI selectNode() {
        return this.nodes.get(Math.floorMod(this.cursor.getAndIncrement(), this.nodes.size()));
    }

    /**
     * Builds a node URI from a configured {@code host:port} entry.
     *
     * @param properties bound Elasticsearch properties
     * @param host       configured host entry
     * @return node URI
     */
    private static URI buildUri(ElasticProperties properties, String host) {
        if (StringKit.isBlank(host) || !host.contains(Symbol.COLON)) {
            throw new InternalException(
                    "Incorrect Elasticsearch cluster node information configuration. Correct format is [ip1:port,ip2:port...]");
        }
        List<String> hostPort = StringKit.split(host, Symbol.COLON);
        if (hostPort.size() < 2) {
            throw new InternalException(
                    "Incorrect Elasticsearch cluster node information configuration. Correct format is [ip1:port,ip2:port...]");
        }
        try {
            return new URI(properties.getSchema(), null, hostPort.get(Consts.INTEGER_ZERO),
                    Integer.parseInt(hostPort.get(Consts.INTEGER_ONE)), null, null, null);
        } catch (URISyntaxException exception) {
            throw new InternalException(
                    "Incorrect Elasticsearch cluster node information configuration. Correct format is [ip1:port,ip2:port...]",
                    exception);
        }
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
        String credentials = username + Symbol.COLON + (password == null ? "" : password);
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
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
     * Looks up a header value ignoring case.
     *
     * @param headers header map
     * @param name    header name
     * @return header value, or {@code null}
     */
    private static String header(Map<String, String> headers, String name) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (name.equalsIgnoreCase(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
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

        @Override
        public Node node() {
            return this.node;
        }

        @Override
        public int statusCode() {
            return this.status;
        }

        @Override
        public String header(String name) {
            return this.headers.get(name);
        }

        @Override
        public List<String> headers(String name) {
            return this.headers.values(name);
        }

        @Override
        public BinaryData body() {
            return new FabricBinaryData(this.body, this.contentType);
        }

        @Override
        public Object originalResponse() {
            return this.original;
        }

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

        @Override
        public String contentType() {
            return this.contentType;
        }

        @Override
        public void writeTo(OutputStream output) throws IOException {
            try (InputStream input = asInputStream()) {
                input.transferTo(output);
            }
        }

        @Override
        public ByteBuffer asByteBuffer() throws IOException {
            return ByteBuffer.wrap(this.body.bytes(MAX_MATERIALIZE_BYTES));
        }

        @Override
        public InputStream asInputStream() throws IOException {
            return new SourceInputStream(this.body.payload().source());
        }

        @Override
        public boolean isRepeatable() {
            return false;
        }

        @Override
        public long size() {
            return this.body.length();
        }

    }

    /**
     * Adapts a Fabric {@link Source} to a JDK input stream.
     */
    private static final class SourceInputStream extends InputStream {

        /**
         * Fabric source.
         */
        private final Source source;

        /**
         * Creates a source-backed input stream.
         *
         * @param source Fabric source
         */
        private SourceInputStream(Source source) {
            this.source = source;
        }

        @Override
        public int read() throws IOException {
            byte[] single = new byte[1];
            int read = read(single, 0, 1);
            return read == -1 ? -1 : single[0] & 0xff;
        }

        @Override
        public int read(byte[] bytes, int offset, int length) throws IOException {
            if (length == 0) {
                return 0;
            }
            Buffer buffer = new Buffer();
            long read = this.source.read(buffer, length);
            if (read == -1) {
                return -1;
            }
            if (read == 0) {
                return 0;
            }
            return buffer.read(bytes, offset, (int) read);
        }

        @Override
        public void close() throws IOException {
            this.source.close();
        }

    }

}
