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
package org.miaixz.bus.fabric.protocol.stomp;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Call;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Session;
import org.miaixz.bus.fabric.observe.ObservationMarker;
import org.miaixz.bus.fabric.observe.event.FabricEvent;
import org.miaixz.bus.fabric.protocol.Mediator;
import org.miaixz.bus.fabric.protocol.stomp.frame.StompCodec;
import org.miaixz.bus.fabric.protocol.stomp.frame.StompFrame;
import org.miaixz.bus.fabric.runtime.FilterChain;
import org.miaixz.bus.logger.Logger;

/**
 * Opens STOMP sessions from an immutable snapshot snapshot.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class StompRunner {

    /**
     * Execution snapshot.
     */
    private final StompSnapshot snapshot;

    /**
     * Creates a runner.
     *
     * @param snapshot execution snapshot
     */
    StompRunner(final StompSnapshot snapshot) {
        this.snapshot = require(snapshot, "STOMP exchange snapshot");
    }

    /**
     * Opens a STOMP session over WebSocket.
     *
     * @return session
     */
    StompSession open() {
        Session socket = null;
        Logger.info(
                true,
                "Fabric",
                "STOMP open started: scheme={}, host={}, port={}, destinationPresent={}",
                snapshot.address().scheme(),
                snapshot.address().host(),
                snapshot.address().port(),
                snapshot.destination() != null);
        try {
            if (!Protocol.WS.name.equals(snapshot.uri().getScheme())
                    && !Protocol.WSS.name.equals(snapshot.uri().getScheme())) {
                throw new ProtocolException("STOMP open requires ws or wss target");
            }
            final Message opening = prepareOpen();
            final StompCodec inbound = new StompCodec();
            final CompletableFuture<StompFrame> connected = new CompletableFuture<>();
            final AtomicReference<StompSession> session = new AtomicReference<>();
            final Session webSocket = Mediator.openWebSocket(
                    snapshot.context().withFilter(null),
                    snapshot.uri(),
                    opening.headers(),
                    snapshot.timeout(),
                    (ignored, message) -> {
                        try {
                            final Buffer input = new Buffer();
                            input.write(message.payload().bytes(snapshot.context().options().materializeMaxBytes()));
                            for (final StompFrame frame : inbound.decode(input)) {
                                if (Builder.STOMP_COMMAND_CONNECTED.equals(frame.command())) {
                                    final StompFrame filtered = filter(frame, Builder.STOMP_TAG_CONNECTED);
                                    connected.complete(filtered);
                                } else if (Builder.STOMP_COMMAND_ERROR.equals(frame.command())) {
                                    final StompFrame filtered = filter(frame, Builder.STOMP_TAG_ERROR);
                                    connected.completeExceptionally(
                                            new ProtocolException(filtered.body().text(Charset.UTF_8)));
                                } else {
                                    final StompSession opened = session.get();
                                    if (opened != null) {
                                        opened.dispatch(frame);
                                    }
                                }
                            }
                        } catch (final RuntimeException e) {
                            connected.completeExceptionally(e);
                        }
                    });
            socket = webSocket;
            final Session openedSocket = socket;
            final StompCodec outbound = new StompCodec();
            final Buffer output = new Buffer();
            outbound.encode(prepareConnectFrame(), output);
            awaitSend(openedSocket.send(Payload.of(output.readByteString())));
            awaitConnected(connected);
            Logger.info(
                    false,
                    "Fabric",
                    "STOMP CONNECT accepted: scheme={}, host={}, port={}",
                    snapshot.address().scheme(),
                    snapshot.address().host(),
                    snapshot.address().port());
            final StompSession opened = new StompSession(
                    buffer -> openedSocket.send(Payload.of(buffer.readByteString())), openedSocket::close,
                    openedSocket::cancel, snapshot.handler(), snapshot.address(), snapshot.guard(), snapshot.observer(),
                    FilterChain.compose(snapshot.context().filter(), snapshot.filter()), snapshot.listener(),
                    snapshot.context().options().materializeMaxBytes());
            session.set(opened);
            Logger.info(
                    false,
                    "Fabric",
                    "STOMP open completed: scheme={}, host={}, port={}",
                    snapshot.address().scheme(),
                    snapshot.address().host(),
                    snapshot.address().port());
            return opened;
        } catch (final RuntimeException e) {
            if (socket != null) {
                socket.cancel();
            }
            Logger.error(
                    false,
                    "Fabric",
                    e,
                    "STOMP open failed: scheme={}, host={}, port={}, exception={}",
                    snapshot.address().scheme(),
                    snapshot.address().host(),
                    snapshot.address().port(),
                    e.getClass().getSimpleName());
            throw e;
        }
    }

    /**
     * Builds the CONNECT frame.
     *
     * @return connect frame
     */
    StompFrame connectFrame() {
        final Headers.Builder builder = Headers.builder()
                .add(Builder.STOMP_HEADER_ACCEPT_VERSION, Builder.STOMP_VERSION_1_2)
                .add(Builder.HOST, snapshot.address().host());
        if (snapshot.login() != null) {
            builder.add(Builder.STOMP_HEADER_LOGIN, snapshot.login());
        }
        if (snapshot.passcode() != null) {
            builder.add(Builder.STOMP_HEADER_PASSCODE, snapshot.passcode());
        }
        for (final Map.Entry<String, List<String>> entry : snapshot.headers().asMap().entrySet()) {
            for (final String value : entry.getValue()) {
                builder.add(entry.getKey(), value);
            }
        }
        return StompFrame.of(Builder.STOMP_COMMAND_CONNECT, builder.build(), Payload.empty());
    }

    /**
     * Waits for CONNECTED.
     *
     * @param connected connected future
     */
    private void awaitConnected(final CompletableFuture<StompFrame> connected) {
        final Duration connectTimeout = connectTimeout();
        try {
            if (connectTimeout.isZero()) {
                connected.get();
            } else {
                connected.get(connectTimeout.toNanos(), java.util.concurrent.TimeUnit.NANOSECONDS);
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InternalException("Interrupted while waiting for STOMP CONNECTED", e);
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new ProtocolException("STOMP CONNECT failed", cause);
        } catch (final java.util.concurrent.TimeoutException e) {
            throw new TimeoutException("STOMP CONNECT timed out", e);
        }
    }

    /**
     * Waits for the CONNECT frame to be written.
     *
     * @param call send call
     */
    private void awaitSend(final Call<Void> call) {
        final Duration writeTimeout = snapshot.timeout().write();
        if (writeTimeout.isZero()) {
            call.await();
        } else {
            call.await(writeTimeout);
        }
    }

    /**
     * Returns the timeout used for the STOMP CONNECTED handshake.
     *
     * @return handshake timeout
     */
    private Duration connectTimeout() {
        return snapshot.timeout().call().isZero() ? snapshot.timeout().connect() : snapshot.timeout().call();
    }

    /**
     * Checks the optional guard.
     */
    private Message prepareOpen() {
        final Message opening = FilterChain.apply(
                Message.of(
                        snapshot.address().protocol(),
                        snapshot.address(),
                        snapshot.headers(),
                        Payload.empty(),
                        Builder.STOMP_TAG_OPEN),
                snapshot.context().filter(),
                snapshot.filter());
        checkGuard(opening);
        return opening;
    }

    /**
     * Creates the filtered CONNECT frame.
     *
     * @return filtered CONNECT frame
     */
    private StompFrame prepareConnectFrame() {
        return filter(connectFrame(), Builder.STOMP_TAG_CONNECT);
    }

    /**
     * Checks the optional guard.
     *
     * @param message message
     */
    private void checkGuard(final Message message) {
        if (snapshot.guard() == null) {
            return;
        }
        Logger.debug(
                true,
                "Fabric",
                "STOMP guard check started: host={}, port={}, destinationPresent={}",
                snapshot.address().host(),
                snapshot.address().port(),
                snapshot.destination() != null);
        snapshot.guard().check(message).throwIfRejected();
        Logger.debug(
                false,
                "Fabric",
                "STOMP guard check accepted: host={}, port={}, destinationPresent={}",
                snapshot.address().host(),
                snapshot.address().port(),
                snapshot.destination() != null);
    }

    /**
     * Applies configured STOMP filters to a frame.
     *
     * @param frame frame
     * @param tag   tag
     * @return filtered frame
     */
    private StompFrame filter(final StompFrame frame, final Object tag) {
        final Message filtered = FilterChain.apply(
                Message.of(Protocol.STOMP, snapshot.address(), frame.headers(), frame.body(), tag),
                snapshot.context().filter(),
                snapshot.filter());
        checkGuard(filtered);
        return StompFrame.of(frame.command(), filtered.headers(), filtered.payload());
    }

    /**
     * Emits a STOMP event.
     *
     * @param marker marker
     * @param cause  cause
     */
    private void emit(final ObservationMarker marker, final Throwable cause) {
        FabricEvent.Builder event = FabricEvent.builder(marker).tag(Builder.TAG_PROTOCOL, snapshot.address().scheme())
                .tag(Builder.HOST, snapshot.address().host())
                .tag(Builder.TAG_PORT, Integer.toString(snapshot.address().port()));
        if (cause != null) {
            event = event.cause(cause);
        }
        snapshot.observer().emit(event.build());
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
