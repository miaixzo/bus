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
import java.net.ProxySelector;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.io.source.AssignSource;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Context;
import org.miaixz.bus.fabric.Filter;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Payload;
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
     * Execution snapshot.
     */
    private final HttpSnapshot snapshot;

    /**
     * Single execution guard.
     */
    private final AtomicBoolean executed;

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
     * @param snapshot execution snapshot
     */
    HttpRunner(final HttpSnapshot snapshot) {
        this(snapshot, true);
    }

    /**
     * Creates an HTTP runner and records whether lifecycle events have a real consumer. The public factory supplies the
     * known no-op observer directly, while builder-created snapshots retain their configured observer.
     *
     * @param snapshot execution snapshot
     * @param observed whether events have a consumer
     */
    private HttpRunner(final HttpSnapshot snapshot, final boolean observed) {
        this.snapshot = require(snapshot, "HTTP exchange snapshot");
        this.executed = new AtomicBoolean();
        this.observed = observed;
        this.operationId = observed ? ID.objectId() : null;
        this.filter = FilterChain.compose(snapshot.context().filter(), snapshot.filter());
        this.cache = cache();
        this.cookies = cookieJar();
        this.authenticator = authenticator();
        this.userAgent = userAgent();
        this.tlsContext = tlsContext();
        this.tlsSettings = tlsSettings();
        this.proxy = proxy();
        this.materializeMaxBytes = snapshot.context().options().materializeMaxBytes();
        this.stages = List.of(
                new HttpRetry(this.authenticator),
                new HttpBridge(this.cookies, this.userAgent),
                this.cache == null ? HttpCoordinator.disabled(snapshot.context().reactor().clock())
                        : HttpCoordinator.create(this.cache, snapshot.context().reactor().clock()),
                new HttpConnect(snapshot.context().directory().connectionPool(), this.tlsContext, this.tlsSettings,
                        snapshot.context().listener(), snapshot.context().resolver(),
                        snapshot.context().reactor().dispatcher()),
                new HttpTransport(snapshot.context().reactor().dispatcher()));
    }

    /**
     * Creates an HTTP runner with default optional execution components.
     *
     * @param context shared context
     * @param request immutable HTTP request
     * @return HTTP runner
     */
    public static HttpRunner create(final Context context, final HttpRequest request) {
        return new HttpRunner(new HttpSnapshot(require(context, "Context"), require(request, "HTTP request"),
                EventObserver.noop(), null, null), false);
    }

    /**
     * Opens an HTTP response stream with a new cancellation scope.
     *
     * @param context shared context
     * @param request immutable HTTP request
     * @return owned HTTP stream
     */
    public static Stream stream(final Context context, final HttpRequest request) {
        return stream(context, request, Cancellation.create());
    }

    /**
     * Opens an HTTP response stream.
     *
     * @param context      shared context
     * @param request      immutable HTTP request
     * @param cancellation cancellation scope
     * @return owned HTTP stream
     */
    public static Stream stream(final Context context, final HttpRequest request, final Cancellation cancellation) {
        final HttpResponse response = create(context, request).run(require(cancellation, "Cancellation"));
        return new Stream(response.code(), response.headers(), response.body().payload(), response);
    }

    /**
     * Performs an HTTP/1.1 upgrade with a new cancellation scope.
     *
     * @param context shared context
     * @param request immutable HTTP upgrade request
     * @return HTTP upgrade result
     */
    public static Upgrade upgrade(final Context context, final HttpRequest request) {
        return upgrade(context, request, Cancellation.create());
    }

    /**
     * Performs an HTTP/1.1 upgrade.
     *
     * @param context      shared context
     * @param request      immutable HTTP upgrade request
     * @param cancellation cancellation scope
     * @return HTTP upgrade result
     */
    public static Upgrade upgrade(final Context context, final HttpRequest request, final Cancellation cancellation) {
        return create(context, request).upgrade(require(cancellation, "Cancellation"));
    }

    /**
     * Executes this exchange once with a new cancellation scope.
     *
     * @return response
     */
    public HttpResponse run() {
        return run(Cancellation.create());
    }

    /**
     * Executes this exchange once.
     *
     * @param cancellation cancellation scope
     * @return response
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
        final HttpRequest request = new HttpBridge(cookies, userAgent).prepare(snapshot.request());
        final HttpConnect connect = new HttpConnect(snapshot.context().directory().connectionPool(), tlsContext,
                tlsSettings, snapshot.context().listener(), snapshot.context().resolver(),
                snapshot.context().reactor().dispatcher());
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
        if (!executed.compareAndSet(false, true)) {
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
     * @param current current request
     * @return response
     */
    private HttpResponse exchange(final HttpRequest current, final Cancellation cancellation) {
        final HttpResponse response = HttpChain.create(stages, cancellation).proceed(current);
        return filterResponse(materializeLimited(response));
    }

    /**
     * Applies the context materialize threshold to the response body.
     *
     * @param response response
     * @return response with context-limited body
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
     * @param response response
     * @return filtered response
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
     * @param request request
     * @return response filter tag
     */
    private static String responseTag(final HttpRequest request) {
        return Builder.HTTP_TAG_SOAP_REQUEST.equals(request.tag()) ? Builder.HTTP_TAG_SOAP_RESPONSE
                : Builder.HTTP_TAG_RESPONSE;
    }

    /**
     * Returns configured HTTP cache, if present.
     *
     * @return cache or null
     */
    private HttpCache cache() {
        return snapshot.context().options().get(Builder.OPTION_HTTP_CACHE);
    }

    /**
     * Returns configured cookie jar, if present.
     *
     * @return cookie jar or null
     */
    private CookieJar cookieJar() {
        if (snapshot.context().options().contains(Builder.OPTION_HTTP_COOKIE_JAR)) {
            return snapshot.context().options().get(Builder.OPTION_HTTP_COOKIE_JAR);
        }
        return snapshot.context().directory().service(
                Builder.OPTION_HTTP_COOKIE_JAR.name(),
                CookieJar.class,
                () -> CookieJar.memory(snapshot.context().clock()));
    }

    /**
     * Returns configured HTTP authenticator.
     *
     * @return authenticator
     */
    private HttpAuthenticator authenticator() {
        final HttpAuthenticator value = snapshot.context().options().get(Builder.OPTION_HTTP_AUTHENTICATOR);
        return value == null ? HttpAuthenticator.none() : value;
    }

    /**
     * Returns configured HTTP User-Agent.
     *
     * @return User-Agent
     */
    private String userAgent() {
        final String value = snapshot.context().options().get(Builder.OPTION_HTTP_USER_AGENT);
        return value == null || value.isBlank() ? HttpBridge.defaultUserAgent() : value;
    }

    /**
     * Returns configured TLS context.
     *
     * @return TLS context
     */
    private TlsContext tlsContext() {
        final TlsContext value = snapshot.context().options().get(Builder.OPTION_TLS_CONTEXT);
        return value == null ? TlsContext.defaults() : value;
    }

    /**
     * Returns configured TLS settings.
     *
     * @return TLS settings
     */
    private TlsSettings tlsSettings() {
        final TlsSettings value = snapshot.context().options().get(Builder.OPTION_TLS_SETTINGS);
        return value == null ? TlsSettings.defaults() : value;
    }

    /**
     * Resolves the configured or system-selected HTTP proxy plan.
     *
     * @return proxy plan
     */
    private ProxyPlan proxy() {
        if (snapshot.context().options().contains(Builder.OPTION_HTTP_PROXY)) {
            final ProxyPlan configured = snapshot.context().options().get(Builder.OPTION_HTTP_PROXY);
            return configured == null ? ProxyPlan.direct() : configured;
        }
        final ProxySelector selector = ProxySelector.getDefault();
        if (selector == null) {
            return ProxyPlan.direct();
        }
        final List<ProxyPlan> selected = ProxySelectorAdapter.of(selector).select(snapshot.request().url());
        return selected.isEmpty() ? ProxyPlan.direct() : selected.get(0);
    }

    /**
     * Emits an observation event.
     *
     * @param marker   marker
     * @param response response
     * @param cause    failure cause
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
     * @param value value
     * @param name  field name
     * @param <T>   value type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

    /**
     * Owned HTTP response stream.
     *
     * @param status  response status
     * @param headers response headers
     * @param body    response body
     * @param owner   response owner
     */
    public record Stream(int status, Headers headers, Payload body, AutoCloseable owner) implements AutoCloseable {

        /**
         * Creates a validated stream result.
         */
        public Stream {
            headers = require(headers, "Headers");
            body = require(body, "Payload");
            owner = require(owner, "Stream owner");
        }

        /**
         * Opens the response body source and binds it to the response owner.
         *
         * @return owned response source
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
     * @param status     response status
     * @param headers    response headers
     * @param connection upgraded connection
     * @param lease      connection lease
     */
    public record Upgrade(int status, Headers headers, Connection connection, ConnectionLease lease)
            implements AutoCloseable {

        /**
         * Creates a validated upgrade result.
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
