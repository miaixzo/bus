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
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

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
import org.miaixz.bus.logger.Logger;

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
     * Creates an HTTP runner.
     *
     * @param snapshot execution snapshot
     */
    HttpRunner(final HttpSnapshot snapshot) {
        this.snapshot = require(snapshot, "HTTP exchange snapshot");
        this.executed = new AtomicBoolean();
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
                EventObserver.noop(), null, null));
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
        Logger.info(
                true,
                "Fabric",
                "HTTP exchange started: method={}, scheme={}, host={}, port={}, path={}",
                snapshot.request().method().value(),
                snapshot.request().url().scheme(),
                snapshot.request().url().host(),
                snapshot.request().url().port(),
                snapshot.request().url().path());
        try {
            currentCancellation.throwIfCancelled();
            final HttpRequest current = prepareRequest();
            currentCancellation.throwIfCancelled();
            emit(ObservationMarker.HTTP_REQUEST, null, null);
            final HttpResponse response = exchange(current, currentCancellation);
            currentCancellation.throwIfCancelled();
            emit(ObservationMarker.HTTP_RESPONSE, response, null);
            Logger.info(
                    false,
                    "Fabric",
                    "HTTP exchange completed: method={}, scheme={}, host={}, port={}, path={}, code={}",
                    current.method().value(),
                    current.url().scheme(),
                    current.url().host(),
                    current.url().port(),
                    current.url().path(),
                    response.code());
            return response;
        } catch (final CancellationException e) {
            emit(ObservationMarker.HTTP_FAILED, null, e);
            Logger.warn(
                    false,
                    "Fabric",
                    e,
                    "HTTP exchange cancelled: method={}, scheme={}, host={}, port={}, path={}",
                    snapshot.request().method().value(),
                    snapshot.request().url().scheme(),
                    snapshot.request().url().host(),
                    snapshot.request().url().port(),
                    snapshot.request().url().path());
            throw e;
        } catch (final RuntimeException e) {
            emit(ObservationMarker.HTTP_FAILED, null, e);
            Logger.error(
                    false,
                    "Fabric",
                    e,
                    "HTTP exchange failed: method={}, scheme={}, host={}, port={}, path={}, exception={}",
                    snapshot.request().method().value(),
                    snapshot.request().url().scheme(),
                    snapshot.request().url().host(),
                    snapshot.request().url().port(),
                    snapshot.request().url().path(),
                    e.getClass().getSimpleName());
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
        final CookieJar cookies = cookieJar();
        final HttpRequest request = new HttpBridge(cookies, userAgent()).prepare(snapshot.request());
        final HttpConnect connect = new HttpConnect(snapshot.context().directory().connectionPool(), tlsContext(),
                tlsSettings(), snapshot.context().listener(), snapshot.context().resolver(),
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
        final HttpRequest request = snapshot.request();
        Message message = Message.of(
                request.url().address().protocol(),
                request.url().address(),
                request.headers(),
                request.body().payload(),
                request.tag() == null ? Builder.HTTP_TAG_REQUEST : request.tag());
        final Filter filter = FilterChain.compose(snapshot.context().filter(), snapshot.filter());
        if (filter != null) {
            Logger.debug(
                    true,
                    "Fabric",
                    "HTTP filter started: method={}, scheme={}, host={}, port={}, path={}",
                    request.method().value(),
                    request.url().scheme(),
                    request.url().host(),
                    request.url().port(),
                    request.url().path());
            message = FilterChain.apply(message, filter);
            Logger.debug(
                    false,
                    "Fabric",
                    "HTTP filter completed: method={}, scheme={}, host={}, port={}, path={}",
                    request.method().value(),
                    request.url().scheme(),
                    request.url().host(),
                    request.url().port(),
                    request.url().path());
        }
        if (snapshot.guard() != null) {
            Logger.debug(
                    true,
                    "Fabric",
                    "HTTP guard check started: method={}, scheme={}, host={}, port={}, path={}",
                    request.method().value(),
                    request.url().scheme(),
                    request.url().host(),
                    request.url().port(),
                    request.url().path());
            snapshot.guard().check(message).throwIfRejected();
            Logger.debug(
                    false,
                    "Fabric",
                    "HTTP guard check accepted: method={}, scheme={}, host={}, port={}, path={}",
                    request.method().value(),
                    request.url().scheme(),
                    request.url().host(),
                    request.url().port(),
                    request.url().path());
        }
        if (filter == null) {
            return request;
        }
        final PayloadBody body = PayloadBody
                .of(message.payload(), request.body().media(), snapshot.context().options().materializeMaxBytes());
        return request.toBuilder().headers(message.headers()).body(body).tag(message.tag()).build();
    }

    /**
     * Executes a prepared request through the native fabric HTTP chain.
     *
     * @param current current request
     * @return response
     */
    private HttpResponse exchange(final HttpRequest current, final Cancellation cancellation) {
        final HttpCache cache = cache();
        final CookieJar currentCookieJar = cookieJar();
        final HttpAuthenticator currentAuthenticator = authenticator();
        final String currentUserAgent = userAgent();
        final TlsContext currentTlsContext = tlsContext();
        final TlsSettings currentTlsSettings = tlsSettings();
        Logger.debug(
                true,
                "Fabric",
                "HTTP chain prepared: cacheEnabled={}, cookieJarEnabled={}, authenticator={}, tlsContext={}, tlsSettings={}",
                cache != null,
                currentCookieJar != null,
                currentAuthenticator.getClass().getName(),
                currentTlsContext.getClass().getName(),
                currentTlsSettings.getClass().getName());
        final HttpResponse response = HttpChain.create(
                List.of(
                        new HttpRetry(currentAuthenticator),
                        new HttpBridge(currentCookieJar, currentUserAgent),
                        cache == null ? HttpCoordinator.disabled(snapshot.context().reactor().clock())
                                : HttpCoordinator.create(cache, snapshot.context().reactor().clock()),
                        new HttpConnect(snapshot.context().directory().connectionPool(), currentTlsContext,
                                currentTlsSettings, snapshot.context().listener(), snapshot.context().resolver(),
                                snapshot.context().reactor().dispatcher()),
                        new HttpTransport(snapshot.context().reactor().dispatcher())),
                cancellation).proceed(current);
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
        final PayloadBody body = current.body().materializeMaxBytes(snapshot.context().options().materializeMaxBytes());
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
        final Filter filter = FilterChain.compose(snapshot.context().filter(), snapshot.filter());
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
        final PayloadBody body = PayloadBody
                .of(filtered.payload(), current.body().media(), snapshot.context().options().materializeMaxBytes());
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
        if (snapshot.context().options().contains("http.cache")) {
            return snapshot.context().options().get("http.cache", HttpCache.class);
        }
        if (snapshot.context().options().contains("cache")) {
            return snapshot.context().options().get("cache", HttpCache.class);
        }
        return null;
    }

    /**
     * Returns configured cookie jar, if present.
     *
     * @return cookie jar or null
     */
    private CookieJar cookieJar() {
        if (snapshot.context().options().contains("http.cookieJar")) {
            return snapshot.context().options().get("http.cookieJar", CookieJar.class);
        }
        if (snapshot.context().options().contains("cookieJar")) {
            return snapshot.context().options().get("cookieJar", CookieJar.class);
        }
        if (snapshot.context().options().contains("http.cookieStore")) {
            return snapshot.context().options().get("http.cookieStore", CookieJar.class);
        }
        if (snapshot.context().options().contains("cookieStore")) {
            return snapshot.context().options().get("cookieStore", CookieJar.class);
        }
        return null;
    }

    /**
     * Returns configured HTTP authenticator.
     *
     * @return authenticator
     */
    private HttpAuthenticator authenticator() {
        if (snapshot.context().options().contains("http.authenticator")) {
            final HttpAuthenticator value = snapshot.context().options()
                    .get("http.authenticator", HttpAuthenticator.class);
            return value == null ? HttpAuthenticator.none() : value;
        }
        if (snapshot.context().options().contains("authenticator")) {
            final HttpAuthenticator value = snapshot.context().options().get("authenticator", HttpAuthenticator.class);
            return value == null ? HttpAuthenticator.none() : value;
        }
        return HttpAuthenticator.none();
    }

    /**
     * Returns configured HTTP User-Agent.
     *
     * @return User-Agent
     */
    private String userAgent() {
        if (snapshot.context().options().contains("http.userAgent")) {
            final String value = snapshot.context().options().get("http.userAgent", String.class);
            return value == null ? HttpBridge.defaultUserAgent() : value;
        }
        if (snapshot.context().options().contains("userAgent")) {
            final String value = snapshot.context().options().get("userAgent", String.class);
            return value == null ? HttpBridge.defaultUserAgent() : value;
        }
        return HttpBridge.defaultUserAgent();
    }

    /**
     * Returns configured TLS context.
     *
     * @return TLS context
     */
    private TlsContext tlsContext() {
        if (snapshot.context().options().contains("http.tlsContext")) {
            return snapshot.context().options().get("http.tlsContext", TlsContext.class);
        }
        if (snapshot.context().options().contains("tlsContext")) {
            return snapshot.context().options().get("tlsContext", TlsContext.class);
        }
        return TlsContext.defaults();
    }

    /**
     * Returns configured TLS settings.
     *
     * @return TLS settings
     */
    private TlsSettings tlsSettings() {
        if (snapshot.context().options().contains("http.tlsSettings")) {
            return snapshot.context().options().get("http.tlsSettings", TlsSettings.class);
        }
        if (snapshot.context().options().contains("tlsSettings")) {
            return snapshot.context().options().get("tlsSettings", TlsSettings.class);
        }
        return TlsSettings.defaults();
    }

    /**
     * Emits an observation event.
     *
     * @param marker   marker
     * @param response response
     * @param cause    failure cause
     */
    private void emit(final ObservationMarker marker, final HttpResponse response, final Throwable cause) {
        FabricEvent.Builder builder = FabricEvent.builder(marker)
                .tag(Builder.TAG_METHOD, snapshot.request().method().value())
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
