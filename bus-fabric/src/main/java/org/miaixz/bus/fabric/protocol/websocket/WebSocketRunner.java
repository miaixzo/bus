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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.network.Connection;
import org.miaixz.bus.fabric.observe.ObservationMarker;
import org.miaixz.bus.fabric.observe.event.FabricEvent;
import org.miaixz.bus.fabric.observe.tag.Tags;
import org.miaixz.bus.fabric.protocol.Mediator;
import org.miaixz.bus.fabric.protocol.websocket.frame.WebSocketReader;
import org.miaixz.bus.fabric.protocol.websocket.frame.WebSocketWriter;
import org.miaixz.bus.fabric.protocol.websocket.upgrade.WebSocketUpgrade;
import org.miaixz.bus.fabric.registry.connection.ConnectionLease;
import org.miaixz.bus.logger.Logger;

/**
 * Opens WebSocket sessions from an immutable exchange snapshot.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class WebSocketRunner {

    /**
     * Logger tag used by the fabric runtime.
     */
    private static final String LOG_TAG = "Fabric";

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
                LOG_TAG,
                "WebSocket open started: scheme={}, host={}, port={}",
                snapshot.address().scheme(),
                snapshot.address().host(),
                snapshot.address().port());
        try {
            checkGuard();
            final WebSocketUpgrade upgrade = new WebSocketUpgrade();
            upgraded = Mediator.upgradeHttp1(
                    snapshot.context(),
                    upgrade.httpUri(snapshot.uri()),
                    upgrade.headers(snapshot.headers()),
                    snapshot.timeout());
            upgrade.validate(upgraded.status(), upgraded.headers());
            final Connection connection = upgraded.connection();
            lease = upgraded.lease();
            upgraded = null;
            final WebSocketSession session = new WebSocketSession(snapshot.address(),
                    new WebSocketWriter(new ConnectionOutputStream(connection), true),
                    new WebSocketReader(new ConnectionInputStream(connection), false, snapshot.address()), lease,
                    snapshot.handler(), snapshot.context().reactor().dispatcher(), dispatchKey(),
                    snapshot.timeout().ping(), snapshot.guard(), snapshot.observer(), snapshot.listener(),
                    snapshot.context().options().materializeMaxBytes());
            lease = null;
            emit(ObservationMarker.WEBSOCKET_OPEN, null);
            snapshot.listener().open(session);
            snapshot.callback().success(session);
            Logger.info(
                    false,
                    LOG_TAG,
                    "WebSocket open completed: scheme={}, host={}, port={}",
                    snapshot.address().scheme(),
                    snapshot.address().host(),
                    snapshot.address().port());
            return session;
        } catch (final RuntimeException e) {
            closeUpgrade(upgraded);
            closeLease(lease);
            final RuntimeException failure = socketFailure(e);
            emit(ObservationMarker.WEBSOCKET_FAILED, failure);
            snapshot.callback().failure(failure);
            Logger.error(
                    false,
                    LOG_TAG,
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
        return snapshot.address().scheme() + "://" + snapshot.address().host() + ':' + snapshot.address().port();
    }

    /**
     * Checks the optional guard.
     */
    private void checkGuard() {
        if (snapshot.guard() == null) {
            return;
        }
        Logger.debug(
                true,
                LOG_TAG,
                "WebSocket guard check started: host={}, port={}",
                snapshot.address().host(),
                snapshot.address().port());
        snapshot.guard().check(Message.of(Protocol.WS, snapshot.address(), snapshot.headers(), Payload.empty(), null))
                .throwIfRejected();
        Logger.debug(
                false,
                LOG_TAG,
                "WebSocket guard check accepted: host={}, port={}",
                snapshot.address().host(),
                snapshot.address().port());
    }

    /**
     * Emits a WebSocket exchange event.
     *
     * @param marker marker
     * @param cause  failure cause
     */
    private void emit(final ObservationMarker marker, final Throwable cause) {
        final FabricEvent.Builder event = FabricEvent.builder(marker).tag(Tags.PROTOCOL, snapshot.address().scheme())
                .tag(Tags.HOST, snapshot.address().host()).tag(Tags.PORT, Integer.toString(snapshot.address().port()));
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
     * Waits for connection IO.
     *
     * @param future  IO future
     * @param message failure message
     * @return transferred bytes
     */
    private static int await(final CompletableFuture<Integer> future, final String message) {
        try {
            return future.get();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SocketException(message, e);
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new SocketException(message, cause);
        }
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
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value;
    }

    /**
     * Input stream backed by a fabric network connection.
     */
    private static final class ConnectionInputStream extends InputStream {

        /**
         * Connection.
         */
        private final Connection connection;

        /**
         * One-byte buffer.
         */
        private final byte[] one;

        /**
         * Creates a stream.
         *
         * @param connection network connection
         */
        private ConnectionInputStream(final Connection connection) {
            this.connection = require(connection, "Network connection");
            this.one = new byte[1];
        }

        @Override
        public int read() throws IOException {
            final int read = read(one, 0, 1);
            return read < 0 ? -1 : one[0] & 0xFF;
        }

        @Override
        public int read(final byte[] bytes, final int offset, final int length) throws IOException {
            if (bytes == null) {
                throw new ValidateException("WebSocket read buffer must not be null");
            }
            if (length == 0) {
                return 0;
            }
            final ByteBuffer target = ByteBuffer.wrap(bytes, offset, length);
            int read = await(connection.read(target), "Unable to read WebSocket frame");
            while (read == 0) {
                Thread.yield();
                read = await(connection.read(target), "Unable to read WebSocket frame");
            }
            return read;
        }

    }

    /**
     * Output stream backed by a fabric network connection.
     */
    private static final class ConnectionOutputStream extends OutputStream {

        /**
         * Connection.
         */
        private final Connection connection;

        /**
         * Creates a stream.
         *
         * @param connection network connection
         */
        private ConnectionOutputStream(final Connection connection) {
            this.connection = require(connection, "Network connection");
        }

        @Override
        public void write(final int value) throws IOException {
            write(new byte[] { (byte) value }, 0, 1);
        }

        @Override
        public void write(final byte[] bytes, final int offset, final int length) throws IOException {
            if (bytes == null) {
                throw new ValidateException("WebSocket write buffer must not be null");
            }
            final ByteBuffer source = ByteBuffer.wrap(bytes, offset, length);
            while (source.hasRemaining()) {
                final int position = source.position();
                final int written = await(connection.write(source), "Unable to write WebSocket frame");
                if (written < 0) {
                    throw new SocketException("WebSocket write reached EOF");
                }
                if (written == 0) {
                    Thread.yield();
                } else {
                    source.position(position + written);
                }
            }
        }

    }

}
