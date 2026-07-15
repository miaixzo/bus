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

import java.util.Map;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.network.Connection;
import org.miaixz.bus.fabric.observe.ObservationMarker;
import org.miaixz.bus.fabric.observe.event.FabricEvent;
import org.miaixz.bus.fabric.protocol.Mediator;
import org.miaixz.bus.fabric.protocol.websocket.frame.WebSocketReader;
import org.miaixz.bus.fabric.protocol.websocket.frame.WebSocketWriter;
import org.miaixz.bus.fabric.protocol.websocket.upgrade.WebSocketUpgrade;
import org.miaixz.bus.fabric.registry.connection.ConnectionLease;
import org.miaixz.bus.fabric.runtime.FilterChain;
import org.miaixz.bus.logger.Logger;

/**
 * Opens WebSocket sessions from an immutable exchange snapshot.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class WebSocketRunner {

    /**
     * WebSocket open filter tag.
     */

    /**
     * Execution snapshot.
     */
    private final WebSocketSnapshot snapshot;

    /**
     * Creates a runner.
     *
     * @param snapshot execution snapshot
     */
    WebSocketRunner(final WebSocketSnapshot snapshot) {
        this.snapshot = require(snapshot, "WebSocket exchange snapshot");
    }

    /**
     * Opens the WebSocket synchronously.
     *
     * @return opened session
     */
    WebSocketSession open() {
        ConnectionLease lease = null;
        Mediator.HttpUpgrade upgraded = null;
        Logger.info(
                true,
                "Fabric",
                "WebSocket open started: scheme={}, host={}, port={}",
                snapshot.address().scheme(),
                snapshot.address().host(),
                snapshot.address().port());
        try {
            final Message opening = prepareOpen();
            final WebSocketUpgrade upgrade = new WebSocketUpgrade();
            upgraded = Mediator.upgradeHttp1(
                    snapshot.context(),
                    upgrade.httpUri(snapshot.uri()),
                    upgrade.headers(opening.headers()),
                    snapshot.timeout());
            upgrade.validate(upgraded.status(), upgraded.headers());
            final Connection connection = upgraded.connection();
            lease = upgraded.lease();
            upgraded = null;
            final WebSocketSession session = new WebSocketSession(snapshot.address(),
                    new WebSocketWriter(connection.sink(), WebSocketRole.CLIENT.writerMask()),
                    new WebSocketReader(connection.source(), WebSocketRole.CLIENT.readerExpectMasked(),
                            snapshot.address()),
                    lease, snapshot.handler(), snapshot.context().reactor().dispatcher(), dispatchKey(),
                    snapshot.timeout().ping(), snapshot.guard(), WebSocketRole.CLIENT,
                    Map.of(
                            Builder.ATTRIBUTE_HEADERS,
                            opening.headers(),
                            Builder.ATTRIBUTE_OBSERVER,
                            snapshot.observer()),
                    null, FilterChain.compose(snapshot.context().filter(), snapshot.filter()), snapshot.observer(),
                    snapshot.listener(), snapshot.context().options().materializeMaxBytes());
            lease = null;
            Logger.info(
                    false,
                    "Fabric",
                    "WebSocket open completed: scheme={}, host={}, port={}",
                    snapshot.address().scheme(),
                    snapshot.address().host(),
                    snapshot.address().port());
            return session;
        } catch (final RuntimeException e) {
            closeUpgrade(upgraded);
            closeLease(lease);
            final RuntimeException failure = socketFailure(e);
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
     * Builds a stable dispatch key for asynchronous opens.
     *
     * @return dispatch key
     */
    String dispatchKey() {
        return snapshot.address().scheme() + Symbol.COLON + Symbol.FORWARDSLASH + snapshot.address().host()
                + Symbol.C_COLON + snapshot.address().port();
    }

    /**
     * Checks the optional guard.
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
     * @param marker marker
     * @param cause  failure cause
     */
    private void emit(final ObservationMarker marker, final Throwable cause) {
        final FabricEvent.Builder event = FabricEvent.builder(marker)
                .tag(Builder.TAG_PROTOCOL, snapshot.address().scheme()).tag(Builder.HOST, snapshot.address().host())
                .tag(Builder.TAG_PORT, Integer.toString(snapshot.address().port()));
        if (cause != null) {
            event.cause(cause);
        }
        snapshot.observer().emit(event.build());
    }

    /**
     * Closes a failed upgrade lease.
     *
     * @param lease connection lease
     */
    private static void closeLease(final ConnectionLease lease) {
        if (lease != null) {
            lease.close();
        }
    }

    /**
     * Closes a failed upgrade result.
     *
     * @param upgrade upgrade result
     */
    private static void closeUpgrade(final Mediator.HttpUpgrade upgrade) {
        if (upgrade != null) {
            upgrade.close();
        }
    }

    /**
     * Converts failures to bus exceptions.
     *
     * @param cause cause
     * @return runtime exception
     */
    private static RuntimeException socketFailure(final Throwable cause) {
        if (cause instanceof RuntimeException runtime) {
            return runtime;
        }
        return new SocketException("Unable to open WebSocket", cause);
    }

    /**
     * Validates a required value.
     *
     * @param value value
     * @param name  name
     * @param <T>   type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

}
