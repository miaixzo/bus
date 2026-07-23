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
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Call;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Session;
import org.miaixz.bus.fabric.observe.ObservationMarker;
import org.miaixz.bus.fabric.observe.event.FabricEvent;
import org.miaixz.bus.fabric.protocol.Mediator;
import org.miaixz.bus.fabric.protocol.Mediator.Type;
import org.miaixz.bus.fabric.protocol.stomp.frame.StompCodec;
import org.miaixz.bus.fabric.protocol.stomp.frame.StompFrame;
import org.miaixz.bus.fabric.protocol.websocket.WebSocketRunner;
import org.miaixz.bus.fabric.runtime.FilterChain;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;
import org.miaixz.bus.logger.Logger;

/**
 * Opens STOMP sessions from an immutable STOMP exchange snapshot.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class StompRunner {

    /**
     * Immutable exchange configuration and borrowed runtime services.
     */
    private final StompSnapshot snapshot;

    /**
     * Creates a runner.
     *
     * @param snapshot execution snapshot
     * @throws ValidateException if {@code snapshot} is {@code null}
     */
    StompRunner(final StompSnapshot snapshot) {
        this.snapshot = require(snapshot, "STOMP exchange snapshot");
    }

    /**
     * Opens a STOMP session over WebSocket.
     *
     * @return connected STOMP session running over a newly opened WebSocket
     */
    StompSession open() {
        return open(Cancellation.create());
    }

    /**
     * Opens a STOMP session within a cancellation scope.
     *
     * @param cancellation scope shared by WebSocket opening, CONNECT send, and CONNECTED wait
     * @return connected STOMP session bound to the supplied cancellation scope
     * @throws CancellationException if opening is cancelled
     * @throws ProtocolException     if the target, WebSocket carrier, or STOMP handshake is invalid
     * @throws ValidateException     if {@code cancellation} is {@code null}
     */
    StompSession open(final Cancellation cancellation) {
        final Cancellation currentCancellation = require(cancellation, "Cancellation");
        final String operationId = ID.objectId();
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
            currentCancellation.throwIfCancelled();
            if (!Protocol.WS.name.equals(snapshot.uri().getScheme())
                    && !Protocol.WSS.name.equals(snapshot.uri().getScheme())) {
                throw new ProtocolException("STOMP open requires ws or wss target");
            }
            final Message opening = prepareOpen();
            final StompCodec inbound = new StompCodec();
            final CompletableFuture<StompFrame> connected = new CompletableFuture<>();
            final AtomicReference<StompSession> session = new AtomicReference<>();
            final Session webSocket = Mediator.convert(
                    Type.STOMP,
                    Type.WEBSOCKET,
                    currentCancellation,
                    current -> WebSocketRunner.create(
                            snapshot.context().withFilter(null),
                            snapshot.uri(),
                            opening.headers(),
                            snapshot.timeout(),
                            (ignored, message) -> {
                                try {
                                    final Buffer input = new Buffer();
                                    input.write(
                                            message.payload()
                                                    .bytes(snapshot.context().options().materializeMaxBytes()));
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
                            }).open(current));
            socket = webSocket;
            final Session openedSocket = socket;
            final Runnable unregisterCancellation = currentCancellation.onCancel(openedSocket::cancel);
            try {
                currentCancellation.throwIfCancelled();
                final StompCodec outbound = new StompCodec();
                final Buffer output = new Buffer();
                outbound.encode(prepareConnectFrame(), output);
                awaitSend(openedSocket.send(Payload.of(output.readByteString())));
                currentCancellation.throwIfCancelled();
                final StompFrame connectedFrame = awaitConnected(connected, currentCancellation);
                currentCancellation.throwIfCancelled();
                final Heartbeats heartbeats = negotiate(connectedFrame);
                Logger.info(
                        false,
                        "Fabric",
                        "STOMP CONNECT accepted: scheme={}, host={}, port={}",
                        snapshot.address().scheme(),
                        snapshot.address().host(),
                        snapshot.address().port());
                final StompSession opened = new StompSession(
                        buffer -> openedSocket.send(Payload.of(buffer.readByteString())), openedSocket::close,
                        openedSocket::cancel, snapshot.handler(), snapshot.address(), snapshot.guard(),
                        snapshot.observer(), FilterChain.compose(snapshot.context().filter(), snapshot.filter()),
                        snapshot.listener(), snapshot.context().options().materializeMaxBytes(),
                        snapshot.context().reactor().dispatcher(), snapshot.context().clock(), currentCancellation,
                        heartbeats.outbound(), heartbeats.inboundDeadline());
                session.set(opened);
                currentCancellation.throwIfCancelled();
                Logger.info(
                        false,
                        "Fabric",
                        "STOMP open completed: scheme={}, host={}, port={}",
                        snapshot.address().scheme(),
                        snapshot.address().host(),
                        snapshot.address().port());
                return opened;
            } finally {
                unregisterCancellation.run();
            }
        } catch (final CancellationException e) {
            emit(ObservationMarker.STOMP_CANCELLED, e, operationId);
            if (socket != null) {
                socket.cancel();
            }
            Logger.warn(
                    false,
                    "Fabric",
                    e,
                    "STOMP open cancelled: scheme={}, host={}, port={}",
                    snapshot.address().scheme(),
                    snapshot.address().host(),
                    snapshot.address().port());
            throw e;
        } catch (final RuntimeException e) {
            emit(ObservationMarker.STOMP_FAILED, e, operationId);
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
     * @return CONNECT frame containing version, host, optional credentials, and configured headers
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
     * @param connected    future completed by the first CONNECTED frame or an opening failure
     * @param cancellation shared cancellation scope
     * @return CONNECTED frame received before cancellation or handshake timeout
     * @throws CancellationException if the shared scope is cancelled
     * @throws TimeoutException      if CONNECTED does not arrive within the selected handshake timeout
     * @throws ProtocolException     if the asynchronous handshake fails with a checked cause
     */
    private StompFrame awaitConnected(final CompletableFuture<StompFrame> connected, final Cancellation cancellation) {
        final Duration connectTimeout = connectTimeout();
        final long started = snapshot.context().clock().nanos();
        final long limit = connectTimeout.isZero() ? Long.MAX_VALUE : durationNanos(connectTimeout);
        try {
            while (!connected.isDone()) {
                cancellation.throwIfCancelled();
                if (limit != Long.MAX_VALUE && elapsed(started, snapshot.context().clock().nanos()) >= limit) {
                    throw new TimeoutException("STOMP CONNECT timed out");
                }
                if (!ThreadKit.sleep(Normal._1)) {
                    throw new InternalException("Interrupted while waiting for STOMP CONNECTED");
                }
            }
            return connected.join();
        } catch (final CompletionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new ProtocolException("STOMP CONNECT failed", cause);
        }
    }

    /**
     * Strictly parses server heartbeat capabilities and calculates negotiated intervals.
     *
     * @param connected validated CONNECTED frame whose heartbeat header is parsed
     * @return negotiated heartbeat values
     * @throws ProtocolException if the heartbeat field is duplicated, malformed, or too large
     * @throws ValidateException if {@code connected} is {@code null}
     */
    private Heartbeats negotiate(final StompFrame connected) {
        final Headers headers = require(connected, "STOMP CONNECTED frame").headers();
        final List<String> values = headers.values(Builder.STOMP_HEADER_HEART_BEAT);
        if (values.size() > Normal._1) {
            throw new ProtocolException("STOMP CONNECTED heart-beat header must be unique");
        }
        final long[] server = heartbeatPair(values.isEmpty() ? null : values.getFirst());
        final long clientSend = heartbeatMillis(snapshot.clientSendHeartbeat(), "Client send heartbeat");
        final long clientReceive = heartbeatMillis(snapshot.clientReceiveHeartbeat(), "Client receive heartbeat");
        final long serverSend = server[Normal._0];
        final long serverReceive = server[Normal._1];
        final Duration outbound = clientSend == Normal.LONG_ZERO || serverReceive == Normal.LONG_ZERO ? Duration.ZERO
                : duration(Math.max(clientSend, serverReceive), "STOMP outbound heartbeat");
        if (clientReceive == Normal.LONG_ZERO || serverSend == Normal.LONG_ZERO) {
            return new Heartbeats(outbound, Duration.ZERO);
        }
        final long inbound = Math.max(clientReceive, serverSend);
        final long tolerance = Math.max(Builder._1000, halfCeiling(inbound));
        final long deadline;
        try {
            deadline = Math.addExact(inbound, tolerance);
        } catch (final ArithmeticException e) {
            throw new ProtocolException("STOMP inbound heartbeat deadline is too large", e);
        }
        return new Heartbeats(outbound, duration(deadline, "STOMP inbound heartbeat deadline"));
    }

    /**
     * Parses a strict {@code sx,sy} CONNECTED heartbeat header.
     *
     * @param value header value, or null when the server disables heartbeats
     * @return server send and receive values
     * @throws ProtocolException if the header is not exactly two non-negative decimal components
     */
    private static long[] heartbeatPair(final String value) {
        if (value == null) {
            return new long[] { Normal.LONG_ZERO, Normal.LONG_ZERO };
        }
        final int comma = value.indexOf(',');
        if (comma <= Normal._0 || comma != value.lastIndexOf(',') || comma == value.length() - Normal._1) {
            throw new ProtocolException("Invalid STOMP CONNECTED heart-beat header");
        }
        return new long[] { unsignedMillis(value, Normal._0, comma),
                unsignedMillis(value, comma + Normal._1, value.length()) };
    }

    /**
     * Parses one non-negative decimal millisecond component.
     *
     * @param value complete heartbeat header text
     * @param start inclusive component start index
     * @param end   exclusive component end index
     * @return non-negative millisecond component
     * @throws ProtocolException if the component is non-decimal or overflows {@code long}
     */
    private static long unsignedMillis(final String value, final int start, final int end) {
        long result = Normal.LONG_ZERO;
        for (int index = start; index < end; index++) {
            final int digit = value.charAt(index) - '0';
            if (digit < Normal._0 || digit > Normal._9 || result > (Long.MAX_VALUE - digit) / Normal._10) {
                throw new ProtocolException("Invalid STOMP CONNECTED heart-beat header");
            }
            result = result * Normal._10 + digit;
        }
        return result;
    }

    /**
     * Converts one client heartbeat Duration to milliseconds.
     *
     * @param value non-null client heartbeat duration
     * @param name  component name
     * @return duration converted to milliseconds
     * @throws ProtocolException if the millisecond conversion overflows
     */
    private static long heartbeatMillis(final Duration value, final String name) {
        try {
            return require(value, name).toMillis();
        } catch (final ArithmeticException e) {
            throw new ProtocolException(name + " is too large", e);
        }
    }

    /**
     * Creates a millisecond Duration while preserving protocol error semantics.
     *
     * @param millis negotiated millisecond interval
     * @param name   component name
     * @return duration representing the interval
     * @throws ProtocolException if the duration cannot represent the interval
     */
    private static Duration duration(final long millis, final String name) {
        try {
            return Duration.ofMillis(millis);
        } catch (final ArithmeticException e) {
            throw new ProtocolException(name + " is too large", e);
        }
    }

    /**
     * Returns half a positive value rounded up.
     *
     * @param value positive integer interval
     * @return half of the interval rounded toward positive infinity
     */
    private static long halfCeiling(final long value) {
        return value / Normal._2 + value % Normal._2;
    }

    /**
     * Converts a Duration to nanoseconds with saturation.
     *
     * @param duration timeout duration to convert
     * @return exact nanoseconds or {@link Long#MAX_VALUE} when conversion overflows
     */
    private static long durationNanos(final Duration duration) {
        try {
            return duration.toNanos();
        } catch (final ArithmeticException e) {
            return Long.MAX_VALUE;
        }
    }

    /**
     * Calculates monotonic elapsed nanoseconds.
     *
     * @param started monotonic start reading
     * @param current monotonic current reading
     * @return non-negative elapsed nanoseconds
     */
    private static long elapsed(final long started, final long current) {
        return Math.max(Normal.LONG_ZERO, current - started);
    }

    /**
     * Waits for the CONNECT frame to be written.
     *
     * @param call deferred CONNECT-frame send operation to await
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
     *
     * @return filtered and guard-approved opening message
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
     * @param message filtered opening or frame message to authorize
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
     * @param frame STOMP frame whose headers and body pass through the filter chain
     * @param tag   lifecycle tag associated with the filtered message
     * @return frame preserving the command and using filtered headers and payload
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
     * @param marker      STOMP lifecycle marker to publish
     * @param cause       failure attached to the event, or {@code null}
     * @param operationId identifier correlating events for this opening operation
     */
    private void emit(final ObservationMarker marker, final Throwable cause, final String operationId) {
        FabricEvent.Builder event = FabricEvent.builder(marker, snapshot.context().clock())
                .tag(Builder.TAG_OPERATION_ID, operationId).tag(Builder.TAG_PROTOCOL, snapshot.address().scheme())
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
     * @param value reference to validate
     * @param name  logical field name included in the validation error
     * @param <T>   reference type
     * @return validated non-null reference
     * @throws ValidateException if {@code value} is {@code null}
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

    /**
     * Negotiated heartbeat settings transferred once to the Session.
     *
     * @param outbound        outbound heartbeat interval
     * @param inboundDeadline inbound heartbeat deadline
     */
    private record Heartbeats(Duration outbound, Duration inboundDeadline) {

    }

}
