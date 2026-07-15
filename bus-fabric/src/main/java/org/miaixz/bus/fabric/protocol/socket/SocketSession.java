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
package org.miaixz.bus.fabric.protocol.socket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.buffer.NioBuffer;
import org.miaixz.bus.core.io.buffer.NioBufferAllocator;
import org.miaixz.bus.core.io.buffer.SlabBufferAllocator;
import org.miaixz.bus.core.io.buffer.SliceBuffer;
import org.miaixz.bus.core.io.buffer.WriteBuffer;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Call;
import org.miaixz.bus.fabric.Filter;
import org.miaixz.bus.fabric.Handler;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Session;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.codec.frame.Frame;
import org.miaixz.bus.fabric.guard.GuardRule;
import org.miaixz.bus.fabric.network.Connection;
import org.miaixz.bus.fabric.network.kcp.KcpNetwork;
import org.miaixz.bus.fabric.network.kcp.KcpPacket;
import org.miaixz.bus.fabric.network.udp.UdpSession;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.observe.ObservationMarker;
import org.miaixz.bus.fabric.protocol.Demuxer;
import org.miaixz.bus.fabric.protocol.socket.body.SocketBody;
import org.miaixz.bus.fabric.protocol.socket.frame.SocketCodec;
import org.miaixz.bus.fabric.protocol.socket.frame.SocketFrame;
import org.miaixz.bus.fabric.protocol.socket.session.SocketLease;
import org.miaixz.bus.fabric.runtime.FilterChain;
import org.miaixz.bus.fabric.runtime.lifecycle.LifecycleScope;
import org.miaixz.bus.logger.Logger;

/**
 * Open socket session.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class SocketSession implements Session {

    /**
     * Session attribute key for original request headers.
     */
    public static final String ATTRIBUTE_HEADERS = "headers";

    /**
     * Session attribute key for observation observer.
     */
    public static final String ATTRIBUTE_OBSERVER = "observer";

    /**
     * Session attribute key for socket guard.
     */
    public static final String ATTRIBUTE_GUARD = "guard";

    /**
     * Session attribute key for socket filter.
     */
    public static final String ATTRIBUTE_FILTER = "filter";

    /**
     * Session attribute key for socket options.
     */
    public static final String ATTRIBUTE_SOCKET_OPTIONS = "socketOptions";

    /**
     * Session attribute key for parsed PROXY protocol metadata.
     */
    public static final String ATTRIBUTE_PROXY_HEADER = "proxyHeader";

    /**
     * Remote address.
     */
    private final Address address;

    /**
     * Network connection.
     */
    private final Connection connection;

    /**
     * Datagram session for UDP/KCP transports.
     */
    private final UdpSession datagram;

    /**
     * KCP packet endpoint when this session uses KCP over UDP.
     */
    private final KcpNetwork kcp;

    /**
     * Socket codec.
     */
    private final SocketCodec codec;

    /**
     * Message handler.
     */
    private final Handler handler;

    /**
     * Decoded frames waiting for receive calls.
     */
    private final ArrayDeque<SocketFrame> pendingFrames;

    /**
     * Attributes.
     */
    private final Map<String, Object> attributes;

    /**
     * Close owner.
     */
    private final AutoCloseable owner;

    /**
     * Maximum bytes allowed when materializing session payloads.
     */
    private final long materializeMaxBytes;

    /**
     * Socket tuning options.
     */
    private final SocketOptions socketOptions;

    /**
     * Allocator for connection read buffers.
     */
    private final NioBufferAllocator readAllocator;

    /**
     * Retained read buffer lease when configured.
     */
    private final NioBuffer retainedReadBuffer;

    /**
     * Slab allocator for connection write slices.
     */
    private final SlabBufferAllocator writeAllocator;

    /**
     * Core write queue for stream connections.
     */
    private final WriteBuffer writeBuffer;

    /**
     * Tail future used to serialize stream writes.
     */
    private final AtomicReference<CompletableFuture<Void>> writeTail;

    /**
     * Active write future currently bound to the physical write queue.
     */
    private final AtomicReference<CompletableFuture<Void>> activeWrite;

    /**
     * Last activity time.
     */
    private volatile long lastActivityNanos;

    /**
     * Lifecycle scope.
     */
    private final LifecycleScope scope;

    /**
     * Creates a current connection-backed socket session for framework integrations.
     *
     * @param address             address
     * @param connection          connection
     * @param codec               codec
     * @param handler             handler
     * @param attributes          attributes
     * @param owner               owner
     * @param listener            lifecycle listener
     * @param materializeMaxBytes materialize byte threshold
     * @param socketOptions       socket options
     * @return socket session
     */
    public static SocketSession create(
            final Address address,
            final Connection connection,
            final SocketCodec codec,
            final Handler handler,
            final Map<String, Object> attributes,
            final AutoCloseable owner,
            final Listener<? super SocketSession> listener,
            final long materializeMaxBytes,
            final SocketOptions socketOptions) {
        return new SocketSession(address, connection, codec, handler, attributes, owner, listener, materializeMaxBytes,
                socketOptions);
    }

    /**
     * Creates an opened session.
     *
     * @param address    address
     * @param connection connection
     * @param codec      codec
     * @param handler    handler
     * @param attributes attributes
     * @param owner      owner
     */
    SocketSession(final Address address, final Connection connection, final SocketCodec codec, final Handler handler,
                  final Map<String, Object> attributes, final AutoCloseable owner) {
        this(address, connection, codec, handler, attributes, owner, null);
    }

    /**
     * Creates an opened session.
     *
     * @param address    address
     * @param connection connection
     * @param codec      codec
     * @param handler    handler
     * @param attributes attributes
     * @param owner      owner
     * @param listener   lifecycle listener
     */
    SocketSession(final Address address, final Connection connection, final SocketCodec codec, final Handler handler,
                  final Map<String, Object> attributes, final AutoCloseable owner,
                  final Listener<? super SocketSession> listener) {
        this(address, connection, codec, handler, attributes, owner, listener, Options.DEFAULT_MATERIALIZE_MAX_BYTES);
    }

    /**
     * Creates an opened session.
     *
     * @param address             address
     * @param connection          connection
     * @param codec               codec
     * @param handler             handler
     * @param attributes          attributes
     * @param owner               owner
     * @param listener            lifecycle listener
     * @param materializeMaxBytes materialize byte threshold
     */
    SocketSession(final Address address, final Connection connection, final SocketCodec codec, final Handler handler,
                  final Map<String, Object> attributes, final AutoCloseable owner,
                  final Listener<? super SocketSession> listener, final long materializeMaxBytes) {
        this(address, connection, codec, handler, attributes, owner, listener, materializeMaxBytes,
                SocketOptions.defaults());
    }

    /**
     * Creates an opened session.
     *
     * @param address             address
     * @param connection          connection
     * @param codec               codec
     * @param handler             handler
     * @param attributes          attributes
     * @param owner               owner
     * @param listener            lifecycle listener
     * @param materializeMaxBytes materialize byte threshold
     * @param socketOptions       socket options
     */
    SocketSession(final Address address, final Connection connection, final SocketCodec codec, final Handler handler,
                  final Map<String, Object> attributes, final AutoCloseable owner,
                  final Listener<? super SocketSession> listener, final long materializeMaxBytes,
                  final SocketOptions socketOptions) {
        this(address, connection, null, null, codec, handler, attributes, owner, listener, materializeMaxBytes,
                socketOptions);
    }

    /**
     * Creates an opened datagram session.
     *
     * @param address    address
     * @param datagram   datagram session
     * @param kcp        KCP packet endpoint or null for plain UDP
     * @param codec      codec
     * @param handler    handler
     * @param attributes attributes
     * @param owner      owner
     */
    SocketSession(final Address address, final UdpSession datagram, final KcpNetwork kcp, final SocketCodec codec,
                  final Handler handler, final Map<String, Object> attributes, final AutoCloseable owner) {
        this(address, datagram, kcp, codec, handler, attributes, owner, null);
    }

    /**
     * Creates an opened datagram session.
     *
     * @param address    address
     * @param datagram   datagram session
     * @param kcp        KCP packet endpoint or null for plain UDP
     * @param codec      codec
     * @param handler    handler
     * @param attributes attributes
     * @param owner      owner
     * @param listener   lifecycle listener
     */
    SocketSession(final Address address, final UdpSession datagram, final KcpNetwork kcp, final SocketCodec codec,
                  final Handler handler, final Map<String, Object> attributes, final AutoCloseable owner,
                  final Listener<? super SocketSession> listener) {
        this(address, datagram, kcp, codec, handler, attributes, owner, listener,
                Options.DEFAULT_MATERIALIZE_MAX_BYTES);
    }

    /**
     * Creates an opened datagram session.
     *
     * @param address             address
     * @param datagram            datagram session
     * @param kcp                 KCP packet endpoint or null for plain UDP
     * @param codec               codec
     * @param handler             handler
     * @param attributes          attributes
     * @param owner               owner
     * @param listener            lifecycle listener
     * @param materializeMaxBytes materialize byte threshold
     */
    SocketSession(final Address address, final UdpSession datagram, final KcpNetwork kcp, final SocketCodec codec,
                  final Handler handler, final Map<String, Object> attributes, final AutoCloseable owner,
                  final Listener<? super SocketSession> listener, final long materializeMaxBytes) {
        this(address, datagram, kcp, codec, handler, attributes, owner, listener, materializeMaxBytes,
                SocketOptions.defaults());
    }

    /**
     * Creates an opened datagram session.
     *
     * @param address             address
     * @param datagram            datagram session
     * @param kcp                 KCP packet endpoint or null for plain UDP
     * @param codec               codec
     * @param handler             handler
     * @param attributes          attributes
     * @param owner               owner
     * @param listener            lifecycle listener
     * @param materializeMaxBytes materialize byte threshold
     * @param socketOptions       socket options
     */
    SocketSession(final Address address, final UdpSession datagram, final KcpNetwork kcp, final SocketCodec codec,
                  final Handler handler, final Map<String, Object> attributes, final AutoCloseable owner,
                  final Listener<? super SocketSession> listener, final long materializeMaxBytes,
                  final SocketOptions socketOptions) {
        this(address, null, datagram, kcp, codec, handler, attributes, owner, listener, materializeMaxBytes,
                socketOptions);
    }

    /**
     * Creates an opened session.
     *
     * @param address    address
     * @param connection connection or null for datagram
     * @param datagram   datagram session or null for connection
     * @param kcp        KCP packet endpoint or null
     * @param codec      codec
     * @param handler    handler
     * @param attributes attributes
     * @param owner      owner
     * @param listener   lifecycle listener
     */
    private SocketSession(final Address address, final Connection connection, final UdpSession datagram,
                          final KcpNetwork kcp, final SocketCodec codec, final Handler handler, final Map<String, Object> attributes,
                          final AutoCloseable owner, final Listener<? super SocketSession> listener) {
        this(address, connection, datagram, kcp, codec, handler, attributes, owner, listener,
                Options.DEFAULT_MATERIALIZE_MAX_BYTES);
    }

    /**
     * Creates an opened session.
     *
     * @param address             address
     * @param connection          connection or null for datagram
     * @param datagram            datagram session or null for connection
     * @param kcp                 KCP packet endpoint or null
     * @param codec               codec
     * @param handler             handler
     * @param attributes          attributes
     * @param owner               owner
     * @param listener            lifecycle listener
     * @param materializeMaxBytes materialize byte threshold
     */
    private SocketSession(final Address address, final Connection connection, final UdpSession datagram,
                          final KcpNetwork kcp, final SocketCodec codec, final Handler handler, final Map<String, Object> attributes,
                          final AutoCloseable owner, final Listener<? super SocketSession> listener, final long materializeMaxBytes) {
        this(address, connection, datagram, kcp, codec, handler, attributes, owner, listener, materializeMaxBytes,
                SocketOptions.defaults());
    }

    /**
     * Creates an opened session.
     *
     * @param address             address
     * @param connection          connection or null for datagram
     * @param datagram            datagram session or null for connection
     * @param kcp                 KCP packet endpoint or null
     * @param codec               codec
     * @param handler             handler
     * @param attributes          attributes
     * @param owner               owner
     * @param listener            lifecycle listener
     * @param materializeMaxBytes materialize byte threshold
     * @param socketOptions       socket options
     */
    private SocketSession(final Address address, final Connection connection, final UdpSession datagram,
                          final KcpNetwork kcp, final SocketCodec codec, final Handler handler, final Map<String, Object> attributes,
                          final AutoCloseable owner, final Listener<? super SocketSession> listener, final long materializeMaxBytes,
                          final SocketOptions socketOptions) {
        this.address = require(address, "Socket address");
        if (connection == null && datagram == null) {
            throw new ValidateException("Socket transport must not be null");
        }
        this.connection = connection;
        this.datagram = datagram;
        this.kcp = kcp;
        this.codec = require(codec, "Socket codec");
        this.handler = handler == null ? Demuxer.noop() : handler;
        this.pendingFrames = new ArrayDeque<>();
        this.attributes = new LinkedHashMap<>(attributes == null ? Map.of() : attributes);
        this.owner = owner;
        final Object observer = this.attributes.get(ATTRIBUTE_OBSERVER);
        final EventObserver currentObserver = observer instanceof EventObserver current ? EventObserver.safe(current)
                : EventObserver.noop();
        this.scope = LifecycleScope.session(this, "socket-session", listener, currentObserver,
                ObservationMarker.SOCKET_OPEN, ObservationMarker.SOCKET_CLOSED, ObservationMarker.SOCKET_FAILED);
        Payload.validateMaterializeMaxBytes(materializeMaxBytes);
        this.materializeMaxBytes = materializeMaxBytes;
        this.socketOptions = socketOptions == null ? SocketOptions.defaults() : socketOptions;
        this.readAllocator = connection == null ? null
                : NioBufferAllocator.heap(
                this.socketOptions.readBufferSize(),
                this.socketOptions.retainReadBuffer() ? 1 : NioBufferAllocator.DEFAULT_MAX_IDLE);
        this.retainedReadBuffer = this.readAllocator != null && this.socketOptions.retainReadBuffer()
                ? this.readAllocator.allocate()
                : null;
        this.writeAllocator = connection == null ? null
                : new SlabBufferAllocator(writeSlabSize(this.socketOptions), 1, false);
        this.writeBuffer = connection == null ? null
                : new WriteBuffer(writeAllocator.allocate(), this::writeSlice, this.socketOptions.writeChunkSize(),
                this.socketOptions.writeChunkCount());
        this.writeTail = new AtomicReference<>(CompletableFuture.completedFuture(null));
        this.activeWrite = new AtomicReference<>();
        this.lastActivityNanos = System.nanoTime();
        this.scope.open(this);
    }

    /**
     * Returns address.
     *
     * @return address
     */
    public Address address() {
        return address;
    }

    /**
     * Returns lifecycle state.
     *
     * @return state
     */
    public Status state() {
        return scope.state();
    }

    /**
     * Sends payload.
     *
     * @param payload payload
     * @return send call
     */
    public Call<Void> send(final Payload payload) {
        require(payload, "Socket payload");
        return send(SocketFrame.of(ByteString.of(materialize(payload, "SocketSession.send(Payload)"))));
    }

    /**
     * Sends a socket body.
     *
     * @param body body
     * @return send call
     */
    public Call<Void> send(final SocketBody body) {
        require(body, "Socket body");
        return send(body.payload());
    }

    /**
     * Sends a shared frame.
     *
     * @param frame frame
     * @return send call
     */
    public Call<Void> send(final Frame frame) {
        require(frame, "Frame");
        return send(SocketFrame.of(frame.payload()));
    }

    /**
     * Sends a socket frame.
     *
     * @param frame frame
     * @return send call
     */
    private Call<Void> send(final SocketFrame frame) {
        ensureOpen();
        final Message outgoing = filter(Payload.of(frame.payload()), "socket-write");
        checkGuard(outgoing);
        final Payload payload = outgoing.payload();
        final SocketFrame filteredFrame = SocketFrame.of(ByteString.of(materialize(payload, "SocketSession.send")));
        final Buffer output = new Buffer();
        codec.encode(filteredFrame, output);
        final ByteBuffer encoded = ByteBuffer.wrap(output.readByteArray()).asReadOnlyBuffer();
        final long byteCount = encoded.remaining();
        Logger.debug(
                true,
                "Fabric",
                "Socket send scheduled: scheme={}, host={}, port={}, bytes={}",
                address.scheme(),
                address.host(),
                address.port(),
                byteCount);
        final CompletableFuture<Void> future = connection == null ? sendDatagram(encoded).thenAccept(written -> {
            if (written == null || written < Normal._0) {
                throw new SocketException("Socket write failed");
            }
        }) : writeConnection(encoded);
        future.whenComplete((ignored, cause) -> {
            final Throwable failure = unwrap(cause);
            if (cause == null) {
                touch();
            }
            emit(cause == null ? ObservationMarker.SOCKET_WRITE : ObservationMarker.SOCKET_FAILED, byteCount, failure);
            if (failure != null) {
                Logger.warn(
                        false,
                        "Fabric",
                        failure,
                        "Socket send failed: scheme={}, host={}, port={}, bytes={}, exception={}",
                        address.scheme(),
                        address.host(),
                        address.port(),
                        byteCount,
                        failure.getClass().getSimpleName());
                notifyFailure(failure);
            } else {
                Logger.debug(
                        false,
                        "Fabric",
                        "Socket send completed: scheme={}, host={}, port={}, bytes={}",
                        address.scheme(),
                        address.host(),
                        address.port(),
                        byteCount);
            }
        });
        return new FutureCall(future);
    }

    /**
     * Receives the next message.
     *
     * @return message future
     */
    public CompletableFuture<Message> receive() {
        ensureOpen();
        Logger.debug(
                true,
                "Fabric",
                "Socket receive requested: scheme={}, host={}, port={}",
                address.scheme(),
                address.host(),
                address.port());
        final CompletableFuture<Message> future = new CompletableFuture<>();
        if (connection == null) {
            readDatagram(future);
        } else if (!completePendingFrame(future)) {
            readUntilFrame(future);
        } else {
            return future;
        }
        return future;
    }

    /**
     * Closes this session.
     *
     * @return true when state changed
     */
    public boolean close() {
        final Status current = scope.state();
        if (current != Status.OPENED && current != Status.RUNNING && current != Status.CLOSING) {
            return false;
        }
        if (current != Status.CLOSING) {
            scope.closing();
        }
        Logger.info(
                true,
                "Fabric",
                "Socket session close started: scheme={}, host={}, port={}",
                address.scheme(),
                address.host(),
                address.port());
        closeResources(true);
        final boolean changed = scope.close(this);
        if (changed) {
            handler.closed(this);
            Logger.info(
                    false,
                    "Fabric",
                    "Socket session closed: scheme={}, host={}, port={}",
                    address.scheme(),
                    address.host(),
                    address.port());
        }
        return changed;
    }

    /**
     * Cancels this session.
     *
     * @return true when state changed
     */
    public boolean cancel() {
        final Status current = scope.state();
        if (current == Status.CANCELLED || current == Status.CLOSED || current == Status.DONE) {
            return false;
        }
        final StatefulException cancelled = new StatefulException("Socket session was cancelled");
        Logger.info(
                true,
                "Fabric",
                "Socket session cancel started: scheme={}, host={}, port={}",
                address.scheme(),
                address.host(),
                address.port());
        closeResources(false);
        final boolean changed = scope.cancel(cancelled);
        if (changed) {
            notifyFailure(cancelled);
            Logger.info(
                    false,
                    "Fabric",
                    "Socket session cancelled: scheme={}, host={}, port={}",
                    address.scheme(),
                    address.host(),
                    address.port());
        }
        return changed;
    }

    /**
     * Returns attributes snapshot.
     *
     * @return attributes
     */
    public Map<String, Object> attributes() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(attributes));
    }

    /**
     * Returns socket tuning options.
     *
     * @return socket options
     */
    public SocketOptions socketOptions() {
        return socketOptions;
    }

    /**
     * Reads until one frame is decoded.
     *
     * @param future target future
     */
    private void readUntilFrame(final CompletableFuture<Message> future) {
        final NioBuffer readLease = readBuffer();
        final ByteBuffer buffer = readLease.buffer();
        connection.read(buffer).whenComplete((read, cause) -> {
            try {
                if (cause != null) {
                    final SocketException failure = new SocketException("Unable to read socket message", cause);
                    emit(ObservationMarker.SOCKET_FAILED, Payload.empty(), failure);
                    notifyFailure(failure);
                    future.completeExceptionally(failure);
                    close();
                    return;
                }
                if (read == null || read < Normal._0) {
                    final SocketException failure = new SocketException("Socket stream closed");
                    emit(ObservationMarker.SOCKET_FAILED, Payload.empty(), failure);
                    notifyFailure(failure);
                    future.completeExceptionally(failure);
                    close();
                    return;
                }
                emit(ObservationMarker.SOCKET_READ, read, null);
                Logger.debug(
                        false,
                        "Fabric",
                        "Socket stream read completed: scheme={}, host={}, port={}, bytes={}",
                        address.scheme(),
                        address.host(),
                        address.port(),
                        read);
                buffer.position(0);
                buffer.limit(read);
                final java.util.List<SocketFrame> frames;
                try {
                    frames = codec.decode(coreBuffer(buffer));
                } catch (final RuntimeException e) {
                    emit(ObservationMarker.SOCKET_FAILED, read, e);
                    notifyFailure(e);
                    future.completeExceptionally(e);
                    close();
                    return;
                }
                if (frames.isEmpty()) {
                    readUntilFrame(future);
                    return;
                }
                enqueuePending(frames, Normal._1);
                completeFrame(future, frames.get(Normal._0), null);
            } finally {
                releaseReadBuffer(readLease);
            }
        });
    }

    /**
     * Writes all encoded bytes to a stream connection.
     *
     * @param encoded encoded bytes
     * @return completion
     */
    private CompletableFuture<Void> writeConnection(final ByteBuffer encoded) {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        CompletableFuture<Void> previous;
        do {
            previous = writeTail.get();
        } while (!writeTail.compareAndSet(previous, future));
        previous.whenComplete((ignored, cause) -> {
            if (cause != null) {
                future.completeExceptionally(unwrap(cause));
                return;
            }
            activeWrite.set(future);
            try {
                writeBuffer.transferFrom(encoded.asReadOnlyBuffer(), current -> {
                    activeWrite.compareAndSet(future, null);
                    future.complete(null);
                });
            } catch (final IOException | RuntimeException e) {
                activeWrite.compareAndSet(future, null);
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    /**
     * Starts a physical write for a queued slice.
     *
     * @param slice write slice
     */
    private void writeSlice(final SliceBuffer slice) {
        writeSlice(slice, slice.buffer());
    }

    /**
     * Continues a physical slice write until all slice bytes are sent.
     *
     * @param slice  write slice
     * @param source slice source
     */
    private void writeSlice(final SliceBuffer slice, final ByteBuffer source) {
        if (!source.hasRemaining()) {
            completeSlice(slice);
            return;
        }
        final ByteBuffer chunk = source.asReadOnlyBuffer();
        if (chunk.remaining() > socketOptions.writeChunkSize()) {
            chunk.limit(chunk.position() + socketOptions.writeChunkSize());
        }
        connection.write(chunk).whenComplete((written, cause) -> {
            if (cause != null) {
                failSlice(slice, new SocketException("Socket write failed", cause));
                return;
            }
            if (written == null || written <= Normal._0) {
                failSlice(slice, new SocketException("Socket write made no progress"));
                return;
            }
            source.position(source.position() + written);
            writeSlice(slice, source);
        });
    }

    /**
     * Completes a physical slice write and flushes the next queued slice.
     *
     * @param slice completed slice
     */
    private void completeSlice(final SliceBuffer slice) {
        slice.release();
        writeBuffer.finishWrite();
        try {
            writeBuffer.flush();
        } catch (final RuntimeException e) {
            failActiveWrite(e);
        }
    }

    /**
     * Fails the active physical write.
     *
     * @param slice write slice
     * @param cause failure
     */
    private void failSlice(final SliceBuffer slice, final RuntimeException cause) {
        try {
            slice.release();
        } catch (final RuntimeException ignored) {
            // Preserve the write failure.
        } finally {
            writeBuffer.finishWrite();
        }
        failActiveWrite(cause);
    }

    /**
     * Fails the active write future.
     *
     * @param cause failure
     */
    private void failActiveWrite(final Throwable cause) {
        final Throwable failure = unwrap(cause);
        final CompletableFuture<Void> future = activeWrite.getAndSet(null);
        if (future != null) {
            future.completeExceptionally(failure);
        }
    }

    /**
     * Completes a receive from already decoded frames.
     *
     * @param future target future
     * @return true when a pending frame was delivered
     */
    private boolean completePendingFrame(final CompletableFuture<Message> future) {
        final SocketFrame frame;
        synchronized (pendingFrames) {
            frame = pendingFrames.pollFirst();
        }
        if (frame == null) {
            return false;
        }
        completeFrame(future, frame, null);
        return true;
    }

    /**
     * Sends a UDP or KCP datagram.
     *
     * @param encoded encoded socket frame
     * @return write future
     */
    private CompletableFuture<Integer> sendDatagram(final ByteBuffer encoded) {
        final byte[] payload = bytes(encoded);
        final Payload outgoing = kcp == null ? Payload.of(payload)
                : Payload.of(kcp.pack(kcp.encode(Payload.of(payload))));
        return datagram.sendDatagram(outgoing);
    }

    /**
     * Reads one UDP or KCP datagram.
     *
     * @param future target future
     */
    private void readDatagram(final CompletableFuture<Message> future) {
        datagram.receive().whenComplete((message, cause) -> {
            if (cause != null) {
                final SocketException failure = new SocketException("Unable to read socket datagram", cause);
                emit(ObservationMarker.SOCKET_FAILED, Payload.empty(), failure);
                notifyFailure(failure);
                future.completeExceptionally(failure);
                close();
                return;
            }
            if (kcp != null) {
                readKcpDatagram(future, message);
                return;
            }
            // Datagram payloads are bounded by UDP/KCP packet limits before this materialization point.
            final ByteString payload = ByteString.of(
                    Payload.materialize(
                            message.payload(),
                            materializeMaxBytes,
                            "SocketSession.readKcpDatagram(Payload)"));
            emit(ObservationMarker.SOCKET_READ, payload.size(), null);
            completeDatagram(future, payload, message.tag());
        });
    }

    /**
     * Reads one KCP datagram and skips ACK-only packets.
     *
     * @param future  target future
     * @param message UDP message
     */
    private void readKcpDatagram(final CompletableFuture<Message> future, final Message message) {
        final KcpPacket packet;
        try {
            emit(ObservationMarker.SOCKET_READ, message.payload().length(), null);
            packet = kcp.unpack(message.payload(), materializeMaxBytes);
        } catch (final RuntimeException e) {
            emit(ObservationMarker.SOCKET_FAILED, message.payload(), e);
            notifyFailure(e);
            future.completeExceptionally(e);
            return;
        }
        final KcpNetwork.Inbound inbound = kcp.receive(packet);
        if (inbound.ack() != null) {
            datagram.send(Payload.of(kcp.pack(inbound.ack())));
        }
        if (inbound.payloads().isEmpty()) {
            readDatagram(future);
            return;
        }
        completeDatagram(future, inbound.payloads().get(Normal._0), message.tag());
    }

    /**
     * Decodes one datagram payload into a socket message.
     *
     * @param future  target future
     * @param payload payload
     * @param tag     message tag
     */
    private void completeDatagram(final CompletableFuture<Message> future, final ByteString payload, final Object tag) {
        final java.util.List<SocketFrame> frames;
        try {
            frames = codec.decode(new Buffer().write(payload));
        } catch (final RuntimeException e) {
            emit(ObservationMarker.SOCKET_FAILED, payload.size(), e);
            notifyFailure(e);
            future.completeExceptionally(e);
            return;
        }
        if (frames.isEmpty()) {
            final SocketException failure = new SocketException("Socket datagram did not contain a complete frame");
            emit(ObservationMarker.SOCKET_FAILED, payload.size(), failure);
            notifyFailure(failure);
            future.completeExceptionally(failure);
            return;
        }
        completeFrame(future, frames.get(Normal._0), tag);
    }

    /**
     * Completes a receive future from a decoded socket frame.
     *
     * @param future target future
     * @param frame  decoded frame
     * @param tag    message tag
     */
    private void completeFrame(final CompletableFuture<Message> future, final SocketFrame frame, final Object tag) {
        final Message received = filter(Payload.of(frame.payload()), tag == null ? "socket-read" : tag);
        try {
            checkGuard(received);
            handler.message(this, received);
            touch();
            future.complete(received);
            Logger.debug(
                    false,
                    "Fabric",
                    "Socket receive completed: scheme={}, host={}, port={}, bytes={}",
                    address.scheme(),
                    address.host(),
                    address.port(),
                    received.payload().length());
        } catch (final RuntimeException e) {
            emit(ObservationMarker.SOCKET_FAILED, received.payload(), e);
            notifyFailure(e);
            future.completeExceptionally(e);
        }
    }

    /**
     * Builds a received message.
     *
     * @param payload payload
     * @param tag     tag
     * @return message
     */
    private Message message(final Payload payload, final Object tag) {
        return Message
                .of(connection == null ? address.protocol() : Protocol.SOCKET, address, Headers.empty(), payload, tag);
    }

    /**
     * Checks the optional session guard against a socket payload.
     *
     * @param payload payload
     * @param tag     direction tag
     */
    private void checkGuard(final Message message) {
        final Object value = attributes.get(ATTRIBUTE_GUARD);
        if (value instanceof GuardRule current) {
            current.check(message).throwIfRejected();
        }
    }

    /**
     * Applies the optional session filter to a socket payload.
     *
     * @param payload payload
     * @param tag     direction tag
     * @return filtered message
     */
    private Message filter(final Payload payload, final Object tag) {
        final Message message = message(payload, tag);
        final Object value = attributes.get(ATTRIBUTE_FILTER);
        return value instanceof Filter current ? FilterChain.apply(message, current) : message;
    }

    /**
     * Emits a session observation event.
     *
     * @param marker  marker
     * @param payload payload
     * @param cause   failure cause
     */
    private void emit(final ObservationMarker marker, final Payload payload, final Throwable cause) {
        emit(marker, payload == null ? Normal.__1 : payload.length(), cause);
    }

    /**
     * Emits a session observation event with a measured byte count.
     *
     * @param marker marker
     * @param bytes  measured bytes, or negative when absent
     * @param cause  failure cause
     */
    private void emit(final ObservationMarker marker, final long bytes, final Throwable cause) {
        scope.emit(marker, cause);
    }

    /**
     * Notifies handler and listener about a session failure.
     *
     * @param cause failure cause
     */
    private void notifyFailure(final Throwable cause) {
        final Throwable current = cause == null ? new StatefulException("Socket session failed") : cause;
        handler.failure(this, current);
    }

    /**
     * Ensures session is open.
     */
    private void ensureOpen() {
        if (!opened()) {
            throw new StatefulException("Socket session is not open");
        }
        if (!socketOptions.idleTimeout().isZero()
                && System.nanoTime() - lastActivityNanos > socketOptions.idleTimeout().toNanos()) {
            Logger.debug(
                    false,
                    "Fabric",
                    "Socket idle timeout reached: scheme={}, host={}, port={}, idleTimeout={}",
                    address.scheme(),
                    address.host(),
                    address.port(),
                    socketOptions.idleTimeout());
            close();
            throw new TimeoutException("Socket session idle timeout");
        }
    }

    /**
     * Returns a configured read buffer lease.
     *
     * @return read buffer lease
     */
    private NioBuffer readBuffer() {
        if (retainedReadBuffer != null) {
            return retainedReadBuffer.clear();
        }
        return readAllocator.allocate(socketOptions.readBufferSize());
    }

    /**
     * Releases a temporary read buffer lease.
     *
     * @param readLease read buffer lease
     */
    private void releaseReadBuffer(final NioBuffer readLease) {
        if (readLease != retainedReadBuffer) {
            readLease.close();
        }
    }

    /**
     * Records socket activity.
     */
    private void touch() {
        lastActivityNanos = System.nanoTime();
    }

    /**
     * Closes owned resources.
     */
    private void closeResources(final boolean reusable) {
        clearPendingFrames();
        codec.reset();
        if (writeBuffer != null) {
            try {
                writeBuffer.close();
            } catch (final RuntimeException ignored) {
                // Best-effort cleanup keeps the original close path.
            }
        }
        if (writeAllocator != null) {
            writeAllocator.release();
        }
        if (retainedReadBuffer != null) {
            retainedReadBuffer.close();
        }
        if (readAllocator != null) {
            readAllocator.close();
        }
        if (owner instanceof SocketLease.Owner lease) {
            if (reusable) {
                lease.release();
            } else {
                lease.close();
            }
            return;
        }
        RuntimeException failure = null;
        if (connection != null) {
            try {
                connection.close();
            } catch (final RuntimeException e) {
                failure = e;
            }
        }
        if (datagram != null) {
            try {
                datagram.close();
            } catch (final RuntimeException e) {
                if (failure == null) {
                    failure = e;
                }
            }
        }
        if (owner != null) {
            try {
                owner.close();
            } catch (final Exception e) {
                if (failure == null) {
                    failure = new InternalException("Unable to close socket owner", e);
                }
            }
        }
        if (failure != null) {
            Logger.warn(
                    false,
                    "Fabric",
                    failure,
                    "Socket resource close failed: scheme={}, host={}, port={}, reusable={}, exception={}",
                    address.scheme(),
                    address.host(),
                    address.port(),
                    reusable,
                    failure.getClass().getSimpleName());
            throw failure;
        }
    }

    /**
     * Caches decoded frames that did not have a matching receive call yet.
     *
     * @param frames decoded frames
     * @param offset first frame to cache
     */
    private void enqueuePending(final java.util.List<SocketFrame> frames, final int offset) {
        if (frames.size() <= offset) {
            return;
        }
        synchronized (pendingFrames) {
            for (int i = offset; i < frames.size(); i++) {
                pendingFrames.addLast(frames.get(i));
            }
            Logger.debug(
                    false,
                    "Fabric",
                    "Socket pending frames queued: scheme={}, host={}, port={}, queued={}",
                    address.scheme(),
                    address.host(),
                    address.port(),
                    pendingFrames.size());
        }
    }

    /**
     * Clears locally decoded but undelivered frames.
     */
    private void clearPendingFrames() {
        synchronized (pendingFrames) {
            pendingFrames.clear();
        }
    }

    /**
     * Copies buffer bytes.
     *
     * @param buffer buffer
     * @return bytes
     */
    private static byte[] bytes(final ByteBuffer buffer) {
        final ByteBuffer duplicate = buffer.duplicate();
        final byte[] data = new byte[duplicate.remaining()];
        duplicate.get(data);
        return data;
    }

    /**
     * Adapts remaining NIO bytes to a core buffer.
     *
     * @param source source buffer
     * @return core buffer
     */
    private static Buffer coreBuffer(final ByteBuffer source) {
        final Buffer target = new Buffer();
        try {
            target.write(source.duplicate());
        } catch (final IOException e) {
            throw new InternalException("Unable to adapt socket buffer", e);
        }
        return target;
    }

    /**
     * Materializes a payload through the configured session limit.
     *
     * @param payload   payload
     * @param operation operation name
     * @return payload bytes
     */
    private byte[] materialize(final Payload payload, final String operation) {
        try {
            return Payload.materialize(payload, materializeMaxBytes, operation);
        } catch (final RuntimeException e) {
            throw new SocketException("Unable to materialize socket payload for " + operation, e);
        }
    }

    /**
     * Computes the slab size for connection write buffering.
     *
     * @param options socket options
     * @return slab size
     */
    private static int writeSlabSize(final SocketOptions options) {
        final long size = (long) options.writeChunkSize() * options.writeChunkCount();
        return size > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) size;
    }

    /**
     * Unwraps completion causes.
     *
     * @param cause completion cause
     * @return unwrapped cause
     */
    private static Throwable unwrap(final Throwable cause) {
        return cause instanceof java.util.concurrent.CompletionException completion ? completion.getCause() : cause;
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
     * Future-backed send call.
     */
    private static final class FutureCall implements Call<Void> {

        /**
         * Future.
         */
        private final CompletableFuture<Void> future;

        /**
         * Creates a call.
         *
         * @param future future
         */
        private FutureCall(final CompletableFuture<Void> future) {
            this.future = future;
        }

        /**
         * Waits for the already-started send to complete.
         *
         * @return null
         */
        @Override
        public Void execute() {
            return await();
        }

        /**
         * Returns this already-started send call.
         *
         * @return this call
         */
        @Override
        public Call<Void> enqueue() {
            return this;
        }

        /**
         * Waits for completion.
         *
         * @return null
         */
        @Override
        public Void await() {
            try {
                return future.get();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new InternalException("Interrupted while waiting for socket send", e);
            } catch (final ExecutionException e) {
                throw new InternalException("Socket send failed", e.getCause());
            } catch (final CancellationException e) {
                throw new InternalException("Socket send was cancelled", e);
            }
        }

        /**
         * Waits for completion within a timeout.
         *
         * @param timeout timeout
         * @return null
         */
        @Override
        public Void await(final Duration timeout) {
            validateTimeout(timeout);
            if (timeout.isZero()) {
                if (!future.isDone()) {
                    cancel();
                    throw new TimeoutException("Socket send timed out");
                }
                return await();
            }
            try {
                return future.get(timeout.toNanos(), TimeUnit.NANOSECONDS);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new InternalException("Interrupted while waiting for socket send", e);
            } catch (final ExecutionException e) {
                throw new InternalException("Socket send failed", e.getCause());
            } catch (final CancellationException e) {
                throw new InternalException("Socket send was cancelled", e);
            } catch (final java.util.concurrent.TimeoutException e) {
                cancel();
                throw new TimeoutException("Socket send timed out", e);
            } catch (final ArithmeticException e) {
                throw new ValidateException("Timeout is too large");
            }
        }

        /**
         * Cancels the asynchronous socket send future.
         *
         * @return true when cancelled
         */
        @Override
        public boolean cancel() {
            return future.cancel(false);
        }

        /**
         * Returns whether the socket send future is cancelled.
         *
         * @return true when cancelled
         */
        @Override
        public boolean cancelled() {
            return future.isCancelled();
        }

        /**
         * Returns whether the socket send future is complete.
         *
         * @return true when complete
         */
        @Override
        public boolean done() {
            return state().terminal();
        }

        /**
         * Returns lifecycle state.
         *
         * @return state
         */
        @Override
        public Status state() {
            if (future.isCancelled()) {
                return Status.CANCELLED;
            }
            if (future.isCompletedExceptionally()) {
                return Status.FAILED;
            }
            return future.isDone() ? Status.DONE : Status.RUNNING;
        }

        /**
         * Validates timeout.
         *
         * @param timeout timeout
         */
        private static void validateTimeout(final Duration timeout) {
            Assert.notNull(timeout, () -> new ValidateException("Timeout must be non-null and non-negative"));
            Assert.isTrue(
                    !timeout.isNegative(),
                    () -> new ValidateException("Timeout must be non-null and non-negative"));
        }

    }

}
