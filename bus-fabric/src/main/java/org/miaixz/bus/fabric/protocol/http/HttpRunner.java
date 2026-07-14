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

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.network.tls.TlsSettings;
import org.miaixz.bus.fabric.network.tls.context.TlsContext;
import org.miaixz.bus.fabric.observe.ObservationMarker;
import org.miaixz.bus.fabric.observe.event.FabricEvent;
import org.miaixz.bus.fabric.observe.tags.Tags;
import org.miaixz.bus.fabric.protocol.CookieJar;
import org.miaixz.bus.fabric.protocol.http.auth.HttpAuthenticator;
import org.miaixz.bus.fabric.protocol.http.body.HttpBody;
import org.miaixz.bus.fabric.protocol.http.cache.HttpCache;
import org.miaixz.bus.fabric.protocol.http.chain.HttpBridge;
import org.miaixz.bus.fabric.protocol.http.chain.HttpChain;
import org.miaixz.bus.fabric.protocol.http.chain.HttpConnect;
import org.miaixz.bus.fabric.protocol.http.chain.HttpCoordinator;
import org.miaixz.bus.fabric.protocol.http.chain.HttpRetry;
import org.miaixz.bus.fabric.protocol.http.chain.HttpServer;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;
import org.miaixz.bus.logger.Logger;

/**
 * Executes an immutable HTTP exchange snapshot through the HTTP chain.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class HttpRunner {

    /**
     * Logger tag used by the fabric runtime.
     */
    private static final String LOG_TAG = "Fabric";

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
     * Executes this exchange once.
     *
     * @return response
     */
    HttpResponse execute(final Cancellation cancellation) {
        final Cancellation currentCancellation = require(cancellation, "Cancellation");
        markExecuted();
        emit(ObservationMarker.CALL_START, null, null);
        Logger.info(
                true,
                LOG_TAG,
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
            snapshot.callback().success(response);
            emit(ObservationMarker.CALL_SUCCESS, response, null);
            Logger.info(
                    false,
                    LOG_TAG,
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
            emit(ObservationMarker.CALL_FAILED, null, e);
            snapshot.callback().failure(e);
            Logger.warn(
                    false,
                    LOG_TAG,
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
            emit(ObservationMarker.CALL_FAILED, null, e);
            snapshot.callback().failure(e);
            Logger.error(
                    false,
                    LOG_TAG,
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
                request.tag());
        if (snapshot.guard() != null) {
            Logger.debug(
                    true,
                    LOG_TAG,
                    "HTTP guard check started: method={}, scheme={}, host={}, port={}, path={}",
                    request.method().value(),
                    request.url().scheme(),
                    request.url().host(),
                    request.url().port(),
                    request.url().path());
            snapshot.guard().check(message).throwIfRejected();
            Logger.debug(
                    false,
                    LOG_TAG,
                    "HTTP guard check accepted: method={}, scheme={}, host={}, port={}, path={}",
                    request.method().value(),
                    request.url().scheme(),
                    request.url().host(),
                    request.url().port(),
                    request.url().path());
        }
        if (snapshot.filter() != null) {
            Logger.debug(
                    true,
                    LOG_TAG,
                    "HTTP filter started: method={}, scheme={}, host={}, port={}, path={}",
                    request.method().value(),
                    request.url().scheme(),
                    request.url().host(),
                    request.url().port(),
                    request.url().path());
            message = require(snapshot.filter().apply(message, current -> current), "Filtered message");
            Logger.debug(
                    false,
                    LOG_TAG,
                    "HTTP filter completed: method={}, scheme={}, host={}, port={}, path={}",
                    request.method().value(),
                    request.url().scheme(),
                    request.url().host(),
                    request.url().port(),
                    request.url().path());
        } else {
            return request;
        }
        final HttpBody body = HttpBody
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
                LOG_TAG,
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
                        new HttpServer(snapshot.context().reactor().dispatcher())),
                cancellation).proceed(current);
        return materializeLimited(response);
    }

    /**
     * Applies the context materialize threshold to the response body.
     *
     * @param response response
     * @return response with context-limited body
     */
    private HttpResponse materializeLimited(final HttpResponse response) {
        final HttpResponse current = require(response, "HTTP response");
        final HttpBody body = current.body().materializeMaxBytes(snapshot.context().options().materializeMaxBytes());
        return current.toBuilder().body(body).build();
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
        FabricEvent.Builder builder = FabricEvent.builder(marker).tag(Tags.METHOD, snapshot.request().method().value())
                .tag(Tags.URL, snapshot.request().url().encoded());
        if (response != null) {
            builder = builder.tag(Tags.CODE, Integer.toString(response.code()));
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

}
