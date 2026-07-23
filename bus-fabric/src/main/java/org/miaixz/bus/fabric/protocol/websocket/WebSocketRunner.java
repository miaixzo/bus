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
package org.miaixz.bus.fabric.protocol.websocket;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.CancellationException;

import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Context;
import org.miaixz.bus.fabric.Handler;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Timeout;
import org.miaixz.bus.fabric.UnoUrl;
import org.miaixz.bus.fabric.network.Connection;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.observe.ObservationMarker;
import org.miaixz.bus.fabric.observe.event.FabricEvent;
import org.miaixz.bus.fabric.protocol.Mediator;
import org.miaixz.bus.fabric.protocol.Mediator.Type;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpRunner;
import org.miaixz.bus.fabric.protocol.websocket.upgrade.WebSocketUpgrade;
import org.miaixz.bus.fabric.registry.connection.ConnectionLease;
import org.miaixz.bus.fabric.runtime.FilterChain;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;
import org.miaixz.bus.logger.Logger;

/**
 * Opens WebSocket sessions from an immutable exchange snapshot.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class WebSocketRunner {

    /**
     * Immutable exchange configuration used by every open attempt.
     */
    private final WebSocketSnapshot snapshot;

    /**
     * Creates a runner from a validated exchange snapshot.
     *
     * @param snapshot immutable WebSocket exchange configuration
     */
    WebSocketRunner(final WebSocketSnapshot snapshot) {
        this.snapshot = require(snapshot, "WebSocket exchange snapshot");
    }

    /**
     * Creates a runner for an internal WebSocket transport operation.
     *
     * @param context shared runtime context used for HTTP upgrade and filtering
     * @param uri     target WebSocket URI
     * @param headers headers included in the opening handshake
     * @param timeout timeout policy applied to the upgrade and session
     * @param handler handler for messages received by the opened session
     * @return runner backed by a new immutable exchange snapshot
     * @throws ValidateException if a required argument is {@code null}
     */
    public static WebSocketRunner create(
            final Context context,
            final URI uri,
            final Headers headers,
            final Timeout timeout,
            final Handler handler) {
        final URI currentUri = require(uri, "WebSocket URI");
        return new WebSocketRunner(new WebSocketSnapshot(require(context, "Context"), currentUri,
                Address.from(currentUri), require(headers, "Headers"), require(timeout, "Timeout"), null, null,
                EventObserver.noop(), require(handler, "Handler"), null));
    }

    /**
     * Opens the WebSocket synchronously with a new cancellation scope.
     *
     * @return client session created from a validated HTTP upgrade
     * @throws CancellationException if the newly created scope is cancelled during the open lifecycle
     * @throws RuntimeException      if filtering, guarding, upgrading, validation, or session creation fails
     */
    public WebSocketSession open() {
        return open(Cancellation.create());
    }

    /**
     * Filters and guards the opening message, performs the HTTP upgrade, and creates a client session.
     *
     * @param cancellation scope controlling the opening lifecycle and the resulting session
     * @return opened client session that owns the upgraded connection lease
     * @throws ValidateException     if {@code cancellation} is {@code null}
     * @throws CancellationException if cancellation wins before the session is created
     * @throws RuntimeException      if filtering, guarding, upgrading, validation, or session creation fails
     */
    public WebSocketSession open(final Cancellation cancellation) {
        final Cancellation currentCancellation = require(cancellation, "Cancellation");
        final String operationId = ID.objectId();
        ConnectionLease lease = null;
        HttpRunner.Upgrade upgraded = null;
        emit(ObservationMarker.WEBSOCKET_OPEN, null, operationId);
        Logger.info(
                true,
                "Fabric",
                "WebSocket open started: scheme={}, host={}, port={}",
                snapshot.address().scheme(),
                snapshot.address().host(),
                snapshot.address().port());
        try {
            currentCancellation.throwIfCancelled();
            final Message opening = prepareOpen();
            currentCancellation.throwIfCancelled();
            final WebSocketUpgrade upgrade = new WebSocketUpgrade();
            final HttpRequest request = HttpRequest.builder().method(Http.Method.GET)
                    .url(UnoUrl.parse(upgrade.httpUri(snapshot.uri()).toString()))
                    .headers(upgrade.headers(opening.headers())).timeout(snapshot.timeout()).build();
            upgraded = Mediator.convert(
                    Type.WEBSOCKET,
                    Type.HTTP_UPGRADE,
                    currentCancellation,
                    current -> HttpRunner.upgrade(snapshot.context(), request, current));
            currentCancellation.throwIfCancelled();
            upgrade.validate(upgraded.status(), upgraded.headers());
            final Connection connection = upgraded.connection();
            lease = upgraded.lease();
            upgraded = null;
            final WebSocketSession session = new WebSocketSession(snapshot.address(), connection.source(),
                    connection.sink(), lease, snapshot.handler(), snapshot.context(), snapshot.timeout(), dispatchKey(),
                    snapshot.guard(), WebSocketRole.CLIENT,
                    Map.of(
                            Builder.ATTRIBUTE_HEADERS,
                            opening.headers(),
                            Builder.ATTRIBUTE_OBSERVER,
                            snapshot.observer(),
                            Builder.TAG_OPERATION_ID,
                            operationId),
                    null, FilterChain.compose(snapshot.context().filter(), snapshot.filter()), snapshot.observer(),
                    snapshot.listener(), currentCancellation);
            lease = null;
            Logger.info(
                    false,
                    "Fabric",
                    "WebSocket open completed: scheme={}, host={}, port={}",
                    snapshot.address().scheme(),
                    snapshot.address().host(),
                    snapshot.address().port());
            return session;
        } catch (final CancellationException e) {
            closeUpgrade(upgraded);
            closeLease(lease);
            emit(ObservationMarker.WEBSOCKET_CANCELLED, e, operationId);
            Logger.warn(
                    false,
                    "Fabric",
                    e,
                    "WebSocket open cancelled: scheme={}, host={}, port={}",
                    snapshot.address().scheme(),
                    snapshot.address().host(),
                    snapshot.address().port());
            throw e;
        } catch (final RuntimeException e) {
            closeUpgrade(upgraded);
            closeLease(lease);
            final RuntimeException failure = socketFailure(e);
            emit(ObservationMarker.WEBSOCKET_FAILED, failure, operationId);
            Logger.error(
                    false,
                    "Fabric",
                    failure,
                    "WebSocket open failed: scheme={}, host={}, port={}, exception={}",
                    snapshot.address().scheme(),
                    snapshot.address().host(),
                    snapshot.address().port(),
                    failure.getClass().getSimpleName());
            throw failure;
        }
    }

    /**
     * Builds the origin-style dispatch key used by the resulting session.
     *
     * @return scheme, host, and port joined as a stable dispatch key
     */
    String dispatchKey() {
        return snapshot.address().scheme() + Symbol.COLON + Symbol.FORWARDSLASH + snapshot.address().host()
                + Symbol.C_COLON + snapshot.address().port();
    }

    /**
     * Creates the opening message, applies configured filters, and checks the optional WebSocket guard.
     *
     * @return filtered opening message accepted by the guard, or the filtered message when no guard is configured
     */
    private Message prepareOpen() {
        Message opening = Message
                .of(Protocol.WS, snapshot.address(), snapshot.headers(), Payload.empty(), Builder.WEBSOCKET_OPEN);
        opening = FilterChain.apply(opening, snapshot.context().filter(), snapshot.filter());
        if (snapshot.guard() == null) {
            return opening;
        }
        Logger.debug(
                true,
                "Fabric",
                "WebSocket guard check started: host={}, port={}",
                snapshot.address().host(),
                snapshot.address().port());
        snapshot.guard().check(opening).throwIfRejected();
        Logger.debug(
                false,
                "Fabric",
                "WebSocket guard check accepted: host={}, port={}",
                snapshot.address().host(),
                snapshot.address().port());
        return opening;
    }

    /**
     * Emits a WebSocket exchange event.
     *
     * @param marker      lifecycle marker recorded by the event
     * @param cause       failure associated with the event, or {@code null} for a successful stage
     * @param operationId stable identifier for this open lifecycle
     */
    private void emit(final ObservationMarker marker, final Throwable cause, final String operationId) {
        final FabricEvent.Builder event = FabricEvent.builder(marker, snapshot.context().clock())
                .tag(Builder.TAG_OPERATION_ID, operationId).tag(Builder.TAG_PROTOCOL, snapshot.address().scheme())
                .tag(Builder.HOST, snapshot.address().host())
                .tag(Builder.TAG_PORT, Integer.toString(snapshot.address().port()));
        if (cause != null) {
            event.cause(cause);
        }
        snapshot.observer().emit(event.build());
    }

    /**
     * Closes a connection lease retained by a failed open attempt.
     *
     * @param lease connection lease to close, or {@code null} when ownership was not acquired
     */
    private static void closeLease(final ConnectionLease lease) {
        if (lease != null) {
            lease.close();
        }
    }

    /**
     * Closes an upgrade result retained by a failed open attempt.
     *
     * @param upgrade upgrade result to close, or {@code null} when no result was returned
     */
    private static void closeUpgrade(final HttpRunner.Upgrade upgrade) {
        if (upgrade != null) {
            upgrade.close();
        }
    }

    /**
     * Preserves runtime failures and wraps checked failures as socket exceptions.
     *
     * @param cause opening failure to normalize
     * @return the original runtime exception, or a socket exception wrapping a checked failure
     */
    private static RuntimeException socketFailure(final Throwable cause) {
        if (cause instanceof RuntimeException runtime) {
            return runtime;
        }
        return new SocketException("Unable to open WebSocket", cause);
    }

    /**
     * Validates and returns a required reference.
     *
     * @param value reference to validate
     * @param name  logical reference name used in the validation message
     * @param <T>   reference type
     * @return the validated non-null reference
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

}
