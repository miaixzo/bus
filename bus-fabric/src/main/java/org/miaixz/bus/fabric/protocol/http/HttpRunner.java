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
package org.miaixz.bus.fabric.protocol.http;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.net.ProxySelector;
import java.util.List;
import java.util.concurrent.CancellationException;

import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.io.source.AssignSource;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Context;
import org.miaixz.bus.fabric.Filter;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.guard.GuardRule;
import org.miaixz.bus.fabric.network.Connection;
import org.miaixz.bus.fabric.network.proxy.ProxyPlan;
import org.miaixz.bus.fabric.network.proxy.ProxySelectorAdapter;
import org.miaixz.bus.fabric.network.tls.TlsSettings;
import org.miaixz.bus.fabric.network.tls.context.TlsContext;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.observe.ObservationMarker;
import org.miaixz.bus.fabric.observe.event.FabricEvent;
import org.miaixz.bus.fabric.protocol.CookieJar;
import org.miaixz.bus.fabric.protocol.http.auth.HttpAuthenticator;
import org.miaixz.bus.fabric.protocol.http.body.PayloadBody;
import org.miaixz.bus.fabric.protocol.http.cache.HttpCache;
import org.miaixz.bus.fabric.protocol.http.chain.HttpBridge;
import org.miaixz.bus.fabric.protocol.http.chain.HttpChain;
import org.miaixz.bus.fabric.protocol.http.chain.HttpConnect;
import org.miaixz.bus.fabric.protocol.http.chain.HttpCoordinator;
import org.miaixz.bus.fabric.protocol.http.chain.HttpRetry;
import org.miaixz.bus.fabric.protocol.http.chain.HttpTransport;
import org.miaixz.bus.fabric.protocol.http.codec.Http1Codec;
import org.miaixz.bus.fabric.registry.connection.ConnectionLease;
import org.miaixz.bus.fabric.runtime.FilterChain;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;

/**
 * Executes an immutable HTTP exchange snapshot through the HTTP chain.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class HttpRunner {

    /**
     * Atomic single-execution guard updater.
     */
    private static final VarHandle EXECUTED;

    static {
        try {
            EXECUTED = MethodHandles.lookup().findVarHandle(HttpRunner.class, "executed", boolean.class);
        } catch (final ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Context-scoped immutable pipeline service prefix.
     */
    private static final String PIPELINE_SERVICE = HttpRunner.class.getName() + ".pipeline.";

    /** Most recently resolved context pipeline, avoiding service-key construction on repeated calls. */
    private static volatile PipelineCache pipelineCache;

    /**
     * Execution snapshot.
     */
    private final HttpSnapshot snapshot;

    /**
     * Single execution guard.
     */
    private volatile boolean executed;

    /**
     * Stable operation identifier shared by every event in this exchange.
     */
    private final String operationId;

    /**
     * Immutable request/response filter captured from the execution snapshot.
     */
    private final Filter filter;

    /**
     * Optional HTTP cache used for lookup, validation, and response persistence.
     */
    private final HttpCache cache;

    /**
     * Cookie jar used to load request cookies and retain response cookies.
     */
    private final CookieJar cookies;

    /**
     * Optional authenticator invoked for origin or proxy challenges.
     */
    private final HttpAuthenticator authenticator;

    /**
     * User-Agent header value applied when the request does not provide one.
     */
    private final String userAgent;

    /**
     * Optional TLS context used when the selected route requires a secure connection.
     */
    private final TlsContext tlsContext;

    /**
     * TLS protocol and cipher policy captured for this exchange.
     */
    private final TlsSettings tlsSettings;

    /**
     * Proxy routing plan resolved before the exchange starts.
     */
    private final ProxyPlan proxy;

    /**
     * Maximum bytes allowed when materializing a request or response payload.
     */
    private final long materializeMaxBytes;

    /**
     * Shared bridge stage used by regular and upgrade exchanges.
     */
    private final HttpBridge bridge;

    /**
     * Shared connection stage used by regular and upgrade exchanges.
     */
    private final HttpConnect connect;

    /**
     * Ordered immutable HTTP stage snapshot executed for this exchange.
     */
    private final List<org.miaixz.bus.fabric.protocol.http.chain.HttpStage> stages;

    /**
     * Whether lifecycle events have a non-noop observer and therefore require publication.
     */
    private final boolean observed;

    /**
     * Creates an HTTP runner.
     *
     * @param snapshot immutable request and exchange-hook snapshot
     * @throws ValidateException if {@code snapshot} is {@code null}
     */
    HttpRunner(final HttpSnapshot snapshot) {
        this(snapshot, true);
    }

    /**
     * Creates an HTTP runner and records whether lifecycle events have a real consumer. The public factory supplies the
     * known no-op observer directly, while builder-created snapshots retain their configured observer.
     *
     * @param snapshot immutable request and exchange-hook snapshot
     * @param observed whether lifecycle events have a non-noop consumer
     * @throws ValidateException if {@code snapshot} is {@code null}
     */
    HttpRunner(final HttpSnapshot snapshot, final boolean observed) {
        this.snapshot = require(snapshot, "HTTP exchange snapshot");
        final Context context = snapshot.context();
        final Pipeline pipeline = pipeline(context);
        this.observed = observed;
        this.operationId = observed ? ID.objectId() : null;
        final Filter contextFilter = context.filter();
        final Filter exchangeFilter = snapshot.filter();
        this.filter = contextFilter == null && exchangeFilter == null ? null
                : FilterChain.compose(contextFilter, exchangeFilter);
        this.cache = pipeline.cache;
        this.cookies = pipeline.cookies;
        this.authenticator = pipeline.authenticator;
        this.userAgent = pipeline.userAgent;
        this.tlsContext = pipeline.tlsContext;
        this.tlsSettings = pipeline.tlsSettings;
        this.proxy = pipeline.proxy(snapshot.request());
        this.materializeMaxBytes = pipeline.materializeMaxBytes;
        this.bridge = pipeline.bridge;
        this.connect = pipeline.connect;
        this.stages = pipeline.stages;
    }

    /**
     * Resolves the immutable pipeline with an identity fast path for repeated calls on one context.
     *
     * @param context execution context
     * @return context-owned pipeline
     */
    private static Pipeline pipeline(final Context context) {
        final PipelineCache cached = pipelineCache;
        if (cached != null && cached.context == context) {
            return cached.pipeline;
        }
        final Pipeline resolved = context.directory().service(
                PIPELINE_SERVICE + Integer.toUnsignedString(System.identityHashCode(context)),
                Pipeline.class,
                () -> Pipeline.create(context));
        pipelineCache = new PipelineCache(context, resolved);
        return resolved;
    }

    /**
     * Creates an HTTP runner with default optional execution components.
     *
     * @param context shared context supplying the context-scoped HTTP pipeline
     * @param request immutable HTTP request
     * @return HTTP runner
     * @throws ValidateException if the context or request is {@code null}
     */
    public static HttpRunner create(final Context context, final HttpRequest request) {
        return new HttpRunner(new HttpSnapshot(require(context, "Context"), require(request, "HTTP request"),
                EventObserver.noop(), null, null), false);
    }

    /**
     * Executes a builder-owned synchronous exchange without allocating an intermediate {@link HttpX} facade.
     *
     * @param context  pipeline owner and runtime configuration
     * @param request  immutable request to execute
     * @param observer exchange lifecycle observer
     * @param filter   optional response filter
     * @param guard    optional attempt guard
     * @return final response after pipeline processing
     */
    static HttpResponse executeSync(
            final Context context,
            final HttpRequest request,
            final EventObserver observer,
            final Filter filter,
            final GuardRule guard) {
        final HttpSnapshot snapshot = new HttpSnapshot(context, request, observer, filter, guard);
        return new HttpRunner(snapshot, observer != EventObserver.noop()).run(Cancellation.none());
    }

    /**
     * Opens an HTTP response stream with a new cancellation scope.
     *
     * @param context shared context supplying the HTTP pipeline
     * @param request immutable HTTP request
     * @return owned HTTP stream
     * @throws ValidateException if the context or request is {@code null}
     */
    public static Stream stream(final Context context, final HttpRequest request) {
        return stream(context, request, Cancellation.create());
    }

    /**
     * Opens an HTTP response stream.
     *
     * @param context      shared context supplying the HTTP pipeline
     * @param request      immutable HTTP request
     * @param cancellation scope governing the complete exchange
     * @return response metadata and payload whose owner is released by the stream
     * @throws ValidateException if any argument is {@code null}
     */
    public static Stream stream(final Context context, final HttpRequest request, final Cancellation cancellation) {
        final HttpResponse response = create(context, request).run(require(cancellation, "Cancellation"));
        return new Stream(response.code(), response.headers(), response.body().payload(), response);
    }

    /**
     * Performs an HTTP/1.1 upgrade with a new cancellation scope.
     *
     * @param context shared context supplying connection and bridge services
     * @param request immutable HTTP upgrade request
     * @return HTTP upgrade result
     * @throws ValidateException if the context or request is {@code null}
     */
    public static Upgrade upgrade(final Context context, final HttpRequest request) {
        return upgrade(context, request, Cancellation.create());
    }

    /**
     * Performs an HTTP/1.1 upgrade.
     *
     * @param context      shared context supplying connection and bridge services
     * @param request      immutable HTTP upgrade request
     * @param cancellation scope governing connection acquisition and handshake I/O
     * @return response metadata, upgraded physical connection, and its owning lease
     * @throws ValidateException if any argument is {@code null}
     */
    public static Upgrade upgrade(final Context context, final HttpRequest request, final Cancellation cancellation) {
        return create(context, request).upgrade(require(cancellation, "Cancellation"));
    }

    /**
     * Executes this exchange once with a new cancellation scope.
     *
     * @return filtered HTTP response from the configured context-scoped stage chain
     */
    public HttpResponse run() {
        return run(Cancellation.none());
    }

    /**
     * Executes this exchange once.
     *
     * @param cancellation scope governing the one permitted exchange execution
     * @return filtered HTTP response from the configured context-scoped stage chain
     * @throws CancellationException if the scope is cancelled before completion
     * @throws StatefulException     if this runner has already executed
     * @throws ValidateException     if {@code cancellation} is {@code null}
     */
    public HttpResponse run(final Cancellation cancellation) {
        final Cancellation currentCancellation = require(cancellation, "Cancellation");
        markExecuted();
        try {
            currentCancellation.throwIfCancelled();
            final HttpRequest current = prepareRequest();
            currentCancellation.throwIfCancelled();
            emit(ObservationMarker.HTTP_REQUEST, null, null);
            final HttpResponse response = exchange(current, currentCancellation);
            currentCancellation.throwIfCancelled();
            emit(ObservationMarker.HTTP_RESPONSE, response, null);
            return response;
        } catch (final CancellationException e) {
            emit(ObservationMarker.HTTP_FAILED, null, e);
            throw e;
        } catch (final RuntimeException e) {
            emit(ObservationMarker.HTTP_FAILED, null, e);
            throw e;
        }
    }

    /**
     * Performs the configured HTTP/1.1 upgrade.
     *
     * @param cancellation cancellation scope
     * @return HTTP upgrade result
     */
    private Upgrade upgrade(final Cancellation cancellation) {
        final Cancellation currentCancellation = require(cancellation, "Cancellation");
        markExecuted();
        currentCancellation.throwIfCancelled();
        final HttpRequest request = bridge.prepare(snapshot.request());
        final ConnectionLease lease = connect.acquire(request, currentCancellation);
        try {
            currentCancellation.throwIfCancelled();
            final Connection connection = lease.connection();
            final Http1Codec codec = new Http1Codec(connection);
            codec.writeRequest(request);
            final HttpResponse response = codec.readResponse(request);
            currentCancellation.throwIfCancelled();
            if (cookies != null) {
                cookies.save(request.url(), response.headers());
            }
            return new Upgrade(response.code(), response.headers(), connection, lease);
        } catch (final RuntimeException e) {
            lease.close();
            throw e;
        }
    }

    /**
     * Marks this exchange as started.
     */
    private void markExecuted() {
        if (!(boolean) EXECUTED.compareAndSet(this, false, true)) {
            throw new StatefulException("HTTP exchange can only be executed once");
        }
    }

    /**
     * Applies guard and filter hooks before network execution.
     *
     * @return prepared request
     */
    private HttpRequest prepareRequest() {
        final HttpRequest source = snapshot.request();
        final HttpRequest request = source.proxy() == proxy ? source : source.toBuilder().proxy(proxy).build();
        if (filter == null && snapshot.guard() == null) {
            return request;
        }
        Message message = Message.of(
                request.url().address().protocol(),
                request.url().address(),
                request.headers(),
                request.body().payload(),
                request.tag() == null ? Builder.HTTP_TAG_REQUEST : request.tag());
        if (filter != null) {
            message = FilterChain.apply(message, filter);
        }
        if (snapshot.guard() != null) {
            snapshot.guard().check(message).throwIfRejected();
        }
        if (filter == null) {
            return request;
        }
        final PayloadBody body = PayloadBody.of(message.payload(), request.body().media(), materializeMaxBytes);
        return request.toBuilder().headers(message.headers()).body(body).tag(message.tag()).build();
    }

    /**
     * Executes a prepared request through the native fabric HTTP chain.
     *
     * @param current      request already prepared by proxy, filter, and guard hooks
     * @param cancellation cancellation scope governing the exchange
     * @return materialization-limited and response-filtered chain result
     */
    private HttpResponse exchange(final HttpRequest current, final Cancellation cancellation) {
        final HttpResponse response = HttpChain.create(stages, cancellation).proceed(current);
        return filterResponse(materializeLimited(response));
    }

    /**
     * Applies the context materialize threshold to the response body.
     *
     * @param response chain response whose body policy is inspected
     * @return original response when already limited, otherwise a copy with the context materialization limit
     */
    private HttpResponse materializeLimited(final HttpResponse response) {
        final HttpResponse current = require(response, "HTTP response");
        if (current.body().materializeMaxBytes() == materializeMaxBytes) {
            return current;
        }
        final PayloadBody body = current.body().materializeMaxBytes(materializeMaxBytes);
        return current.toBuilder().body(body).build();
    }

    /**
     * Applies configured response filters without materializing the response body.
     *
     * @param response materialization-limited response to pass through configured filters
     * @return original response when no filter exists, otherwise a copy using filtered headers and payload
     */
    private HttpResponse filterResponse(final HttpResponse response) {
        final HttpResponse current = require(response, "HTTP response");
        if (filter == null) {
            return current;
        }
        final Message filtered = FilterChain.apply(
                Message.of(
                        current.protocol(),
                        current.request().url().address(),
                        current.headers(),
                        current.body().payload(),
                        responseTag(current.request())),
                filter);
        final PayloadBody body = PayloadBody.of(filtered.payload(), current.body().media(), materializeMaxBytes);
        return current.toBuilder().headers(filtered.headers()).body(body).build();
    }

    /**
     * Returns the response filter tag for a request.
     *
     * @param request request whose lifecycle tag selects ordinary HTTP or SOAP response tagging
     * @return response filter tag
     */
    private static String responseTag(final HttpRequest request) {
        return Builder.HTTP_TAG_SOAP_REQUEST.equals(request.tag()) ? Builder.HTTP_TAG_SOAP_RESPONSE
                : Builder.HTTP_TAG_RESPONSE;
    }

    /**
     * Returns configured HTTP cache, if present.
     *
     * @param context runtime context containing HTTP options
     * @return cache or null
     */
    private static HttpCache cache(final Context context) {
        return context.options().get(Builder.OPTION_HTTP_CACHE);
    }

    /**
     * Returns configured cookie jar, if present.
     *
     * @param context runtime context containing HTTP options and services
     * @return cookie jar or null
     */
    private static CookieJar cookieJar(final Context context) {
        if (context.options().contains(Builder.OPTION_HTTP_COOKIE_JAR)) {
            return context.options().get(Builder.OPTION_HTTP_COOKIE_JAR);
        }
        return context.directory().service(
                Builder.OPTION_HTTP_COOKIE_JAR.name(),
                CookieJar.class,
                () -> CookieJar.memory(context.clock()));
    }

    /**
     * Returns configured HTTP authenticator.
     *
     * @param context runtime context containing HTTP options
     * @return authenticator
     */
    private static HttpAuthenticator authenticator(final Context context) {
        final HttpAuthenticator value = context.options().get(Builder.OPTION_HTTP_AUTHENTICATOR);
        return value == null ? HttpAuthenticator.none() : value;
    }

    /**
     * Returns configured HTTP User-Agent.
     *
     * @param context runtime context containing HTTP options
     * @return User-Agent
     */
    private static String userAgent(final Context context) {
        final String value = context.options().get(Builder.OPTION_HTTP_USER_AGENT);
        return value == null || value.isBlank() ? HttpBridge.defaultUserAgent() : value;
    }

    /**
     * Returns configured TLS context.
     *
     * @param context runtime context containing TLS options
     * @return TLS context
     */
    private static TlsContext tlsContext(final Context context) {
        final TlsContext value = context.options().get(Builder.OPTION_TLS_CONTEXT);
        return value == null ? TlsContext.defaults() : value;
    }

    /**
     * Returns configured TLS settings.
     *
     * @param context runtime context containing TLS options
     * @return TLS settings
     */
    private static TlsSettings tlsSettings(final Context context) {
        final TlsSettings value = context.options().get(Builder.OPTION_TLS_SETTINGS);
        return value == null ? TlsSettings.defaults() : value;
    }

    /**
     * Emits an observation event.
     *
     * @param marker   HTTP lifecycle marker to publish
     * @param response response supplying a status-code tag, or {@code null}
     * @param cause    failure attached to the event, or {@code null}
     */
    private void emit(final ObservationMarker marker, final HttpResponse response, final Throwable cause) {
        if (!observed) {
            return;
        }
        FabricEvent.Builder builder = FabricEvent.builder(marker, snapshot.context().clock())
                .tag(Builder.TAG_OPERATION_ID, operationId).tag(Builder.TAG_METHOD, snapshot.request().method().value())
                .tag(Builder.TAG_URL, snapshot.request().url().encoded());
        if (response != null) {
            builder = builder.tag(Builder.TAG_CODE, Integer.toString(response.code()));
        }
        if (cause != null) {
            builder = builder.cause(cause);
        }
        snapshot.observer().emit(builder.build());
    }

    /**
     * Validates required references.
     *
     * @param value reference to validate
     * @param name  logical field name included in the validation error
     * @param <T>   reference type
     * @return validated non-null reference
     * @throws ValidateException if {@code value} is {@code null}
     */
    private static <T> T require(final T value, final String name) {
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value;
    }

    /**
     * Most-recent context and pipeline identity pair.
     *
     * @param context  context identity used for the lookup
     * @param pipeline immutable pipeline associated with the context
     */
    private record PipelineCache(Context context, Pipeline pipeline) {
    }

    /**
     * Immutable context-scoped HTTP execution pipeline.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private static final class Pipeline {

        /**
         * Context that owns the pipeline services and options.
         */
        private final Context context;

        /** Most recent system proxy decision, keyed by selector identity and immutable URL text. */
        private volatile ProxySelection proxySelection;

        /**
         * Optional cache shared by pipeline exchanges.
         */
        private final HttpCache cache;

        /**
         * Cookie jar shared by the bridge stage.
         */
        private final CookieJar cookies;

        /**
         * Authenticator used when retrying challenged requests.
         */
        private final HttpAuthenticator authenticator;

        /**
         * Default User-Agent value applied by the bridge stage.
         */
        private final String userAgent;

        /**
         * TLS context used by connection establishment.
         */
        private final TlsContext tlsContext;

        /**
         * TLS settings used by connection establishment.
         */
        private final TlsSettings tlsSettings;

        /**
         * Maximum payload size accepted during materialization.
         */
        private final long materializeMaxBytes;

        /**
         * Request and response normalization stage.
         */
        private final HttpBridge bridge;

        /**
         * Connection acquisition stage owned by this pipeline.
         */
        private final HttpConnect connect;

        /**
         * Immutable ordered stage chain.
         */
        private final List<org.miaixz.bus.fabric.protocol.http.chain.HttpStage> stages;

        /**
         * Creates one frozen pipeline.
         *
         * @param context shared context supplying pipeline collaborators
         */
        private Pipeline(final Context context) {
            this.context = context;
            this.cache = cache(context);
            this.cookies = cookieJar(context);
            this.authenticator = authenticator(context);
            this.userAgent = userAgent(context);
            this.tlsContext = tlsContext(context);
            this.tlsSettings = tlsSettings(context);
            this.materializeMaxBytes = context.options().materializeMaxBytes();
            this.bridge = new HttpBridge(cookies, userAgent);
            this.connect = new HttpConnect(context.directory().connectionPool(), tlsContext, tlsSettings,
                    context.listener(), context.resolver(), context.reactor().dispatcher());
            this.stages = List.of(
                    new HttpRetry(authenticator),
                    bridge,
                    cache == null ? HttpCoordinator.disabled(context.reactor().clock())
                            : HttpCoordinator.create(cache, context.reactor().clock()),
                    connect,
                    new HttpTransport(context.reactor().dispatcher()));
        }

        /** Resolves explicit proxy policy first and caches the common stable system-selector decision. */
        private ProxyPlan proxy(final HttpRequest request) {
            if (!request.proxy().isDirect()) {
                return request.proxy();
            }
            if (context.options().contains(Builder.OPTION_HTTP_PROXY)) {
                final ProxyPlan configured = context.options().get(Builder.OPTION_HTTP_PROXY);
                return configured == null ? ProxyPlan.direct() : configured;
            }
            final ProxySelector selector = ProxySelector.getDefault();
            if (selector == null) {
                return ProxyPlan.direct();
            }
            final String url = request.url().toString();
            final ProxySelection cached = proxySelection;
            if (cached != null && cached.selector == selector && cached.url.equals(url)) {
                return cached.plan;
            }
            final List<ProxyPlan> selected = ProxySelectorAdapter.of(selector).select(request.url());
            final ProxyPlan plan = selected.isEmpty() ? ProxyPlan.direct() : selected.get(0);
            proxySelection = new ProxySelection(selector, url, plan);
            return plan;
        }

        /**
         * Creates a context-scoped pipeline.
         *
         * @param context shared context
         * @return pipeline
         */
        private static Pipeline create(final Context context) {
            return new Pipeline(context);
        }
    }

    /**
     * Cached system proxy decision for one context pipeline.
     *
     * @param selector selector identity that produced the plan
     * @param url      immutable request URL text
     * @param plan     normalized proxy plan
     */
    private record ProxySelection(ProxySelector selector, String url, ProxyPlan plan) {
    }

    /**
     * Owned HTTP response stream.
     *
     * @param status  HTTP response status code
     * @param headers immutable response headers
     * @param body    streaming response payload
     * @param owner   resource retaining the response connection or stream
     */
    public record Stream(int status, Headers headers, Payload body, AutoCloseable owner) implements AutoCloseable {

        /**
         * Creates a validated stream result.
         *
         * @param status  HTTP response status code
         * @param headers response headers
         * @param body    streaming response payload
         * @param owner   resource released when the stream closes
         * @throws ValidateException if headers, body, or owner is {@code null}
         */
        public Stream {
            headers = require(headers, "Headers");
            body = require(body, "Payload");
            owner = require(owner, "Stream owner");
        }

        /**
         * Opens the response body source and binds it to the response owner.
         *
         * @return source wrapper that closes the response owner after its body source
         */
        public Source source() {
            return new OwnedSource(body.source(), this);
        }

        /**
         * Closes the response owner.
         */
        @Override
        public void close() {
            try {
                owner.close();
            } catch (final RuntimeException e) {
                throw e;
            } catch (final Exception e) {
                throw new InternalException("Unable to close HTTP stream", e);
            }
        }

    }

    /**
     * HTTP/1.1 upgrade result with its leased connection.
     *
     * @param status     HTTP upgrade response status code
     * @param headers    immutable upgrade response headers
     * @param connection upgraded physical connection
     * @param lease      lease retaining ownership of that connection
     */
    public record Upgrade(int status, Headers headers, Connection connection, ConnectionLease lease)
            implements AutoCloseable {

        /**
         * Creates a validated upgrade result.
         *
         * @param status     HTTP upgrade response status code
         * @param headers    upgrade response headers
         * @param connection upgraded physical connection
         * @param lease      lease owning the upgraded connection
         * @throws ValidateException if headers, connection, or lease is {@code null}
         */
        public Upgrade {
            headers = require(headers, "Headers");
            connection = require(connection, "Network connection");
            lease = require(lease, "Connection lease");
        }

        /**
         * Releases the upgraded connection lease.
         */
        @Override
        public void close() {
            lease.close();
        }

    }

    /**
     * Source that closes its protocol owner after the response body source.
     */
    private static final class OwnedSource extends AssignSource {

        /**
         * Close owner.
         */
        private final AutoCloseable owner;

        /**
         * Creates an owned source.
         *
         * @param source response body source
         * @param owner  response owner
         */
        private OwnedSource(final Source source, final AutoCloseable owner) {
            super(require(source, "Body source"));
            this.owner = require(owner, "Stream owner");
        }

        /**
         * Closes the body source and then closes its owner.
         *
         * @throws IOException when source closing fails
         */
        @Override
        public void close() throws IOException {
            IOException failure = null;
            try {
                super.close();
            } catch (final IOException e) {
                failure = e;
            }
            try {
                owner.close();
            } catch (final RuntimeException e) {
                if (failure != null) {
                    e.addSuppressed(failure);
                }
                throw e;
            } catch (final Exception e) {
                final InternalException wrapped = new InternalException("Unable to close owned stream", e);
                if (failure != null) {
                    wrapped.addSuppressed(failure);
                }
                throw wrapped;
            }
            if (failure != null) {
                throw failure;
            }
        }

    }

}
