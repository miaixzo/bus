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
package org.miaixz.bus.fabric.network.tls;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.SSLEngineResult;

import org.miaixz.bus.core.io.buffer.NioBuffer;
import org.miaixz.bus.core.io.buffer.NioBufferAllocator;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.crypto.builtin.TlsHandshake;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.Wiring;
import org.miaixz.bus.fabric.network.Conduit;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;

/**
 * TLS channel wrapper over a network conduit and SSLEngine adapter.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class TlsChannel implements AutoCloseable {

    /**
     * Operation timeout seconds for internal TLS driving.
     */
    private static final long OPERATION_TIMEOUT_SECONDS = Normal._5;

    /**
     * Maximum idle buffers retained per TLS channel allocator.
     */
    private static final int BUFFER_CACHE_SIZE = Normal._4;

    /**
     * Extra packet bytes reserved when wrapping oversized plaintext.
     */
    private static final int EXTRA_PACKET_BYTES = Normal._1024;

    /**
     * Underlying network conduit.
     */
    private final Conduit conduit;

    /**
     * TLS engine adapter.
     */
    private final TlsEngine engine;

    /**
     * Reusable TLS packet buffers.
     */
    private final NioBufferAllocator packetBuffers;

    /**
     * Reusable TLS application buffers.
     */
    private final NioBufferAllocator applicationBuffers;

    /**
     * Lifecycle state.
     */
    private final AtomicReference<Status> state;

    /**
     * Lifecycle listener.
     */
    private final Listener<Object> listener;

    /**
     * Runtime dispatcher for TLS operations.
     */
    private final Dispatcher dispatcher;

    /**
     * Whether this channel owns the dispatcher lifecycle.
     */
    private final boolean ownsDispatcher;

    /**
     * Open notification flag.
     */
    private final AtomicBoolean notified;

    /**
     * Creates a TLS channel.
     *
     * @param conduit        network conduit
     * @param engine         TLS engine
     * @param listener       lifecycle listener
     * @param dispatcher     runtime dispatcher
     * @param ownsDispatcher true when close should stop dispatcher
     */
    private TlsChannel(final Conduit conduit, final TlsEngine engine, final Listener<Object> listener,
                       final Dispatcher dispatcher, final boolean ownsDispatcher) {
        this.conduit = Assert.notNull(conduit, () -> new ValidateException("Network conduit must not be null"));
        this.engine = Assert.notNull(engine, () -> new ValidateException("TLS engine must not be null"));
        this.packetBuffers = NioBufferAllocator
                .heap(this.engine.engine().getSession().getPacketBufferSize(), BUFFER_CACHE_SIZE);
        this.applicationBuffers = NioBufferAllocator
                .heap(this.engine.engine().getSession().getApplicationBufferSize(), BUFFER_CACHE_SIZE);
        this.state = new AtomicReference<>(Status.OPENED);
        this.listener = Wiring.safe(listener == null ? Wiring.noop() : listener, null);
        this.dispatcher = Assert.notNull(dispatcher, () -> new ValidateException("TLS dispatcher must not be null"));
        this.ownsDispatcher = ownsDispatcher;
        this.notified = new AtomicBoolean();
    }

    /**
     * Wraps a network conduit.
     *
     * @param conduit network conduit
     * @param engine  TLS engine
     * @return TLS channel
     */
    public static TlsChannel wrap(final Conduit conduit, final TlsEngine engine) {
        return new TlsChannel(conduit, engine, Wiring.noop(), Dispatcher.create(), true);
    }

    /**
     * Wraps a network conduit.
     *
     * @param conduit  network conduit
     * @param engine   TLS engine
     * @param listener lifecycle listener
     * @return TLS channel
     */
    public static TlsChannel wrap(final Conduit conduit, final TlsEngine engine, final Listener<Object> listener) {
        return new TlsChannel(conduit, engine, listener == null ? Wiring.noop() : listener, Dispatcher.create(), true);
    }

    /**
     * Wraps a network conduit.
     *
     * @param conduit    network conduit
     * @param engine     TLS engine
     * @param listener   lifecycle listener
     * @param dispatcher shared dispatcher
     * @return TLS channel
     */
    public static TlsChannel wrap(
            final Conduit conduit,
            final TlsEngine engine,
            final Listener<Object> listener,
            final Dispatcher dispatcher) {
        return new TlsChannel(conduit, engine, listener == null ? Wiring.noop() : listener,
                Assert.notNull(dispatcher, () -> new ValidateException("Dispatcher must not be null")), false);
    }

    /**
     * Returns current handshake metadata.
     *
     * @return handshake future
     */
    public CompletableFuture<TlsHandshake> handshake() {
        if (!opened()) {
            return CompletableFuture.failedFuture(new StatefulException("TLS channel is closed"));
        }
        return dispatcher.supply("tls:handshake", this::driveHandshake).whenComplete((handshake, cause) -> {
            if (cause == null) {
                if (notified.compareAndSet(false, true)) {
                    listener.open(this);
                }
            } else {
                listener.failure(this, cause);
            }
        });
    }

    /**
     * Reads and unwraps TLS bytes.
     *
     * @param target target buffer
     * @return read future
     */
    public CompletableFuture<Integer> read(final ByteBuffer target) {
        Assert.notNull(target, () -> new ValidateException("TLS read target must not be null"));
        if (!opened()) {
            return CompletableFuture.failedFuture(new StatefulException("TLS channel is closed"));
        }
        return dispatcher.supply("tls:read", () -> readPlain(target));
    }

    /**
     * Wraps and writes TLS bytes.
     *
     * @param source source buffer
     * @return write future
     */
    public CompletableFuture<Integer> write(final ByteBuffer source) {
        Assert.notNull(source, () -> new ValidateException("TLS write source must not be null"));
        if (!opened()) {
            return CompletableFuture.failedFuture(new StatefulException("TLS channel is closed"));
        }
        final ByteBuffer view = source.asReadOnlyBuffer();
        if (!view.hasRemaining()) {
            return CompletableFuture.completedFuture(Normal._0);
        }
        final NioBuffer packetLease = packetBuffers.allocate(packetSize(view.remaining()));
        final ByteBuffer packet = packetLease.buffer();
        final SSLEngineResult result;
        try {
            result = engine.wrap(view, packet);
            packet.flip();
            if (!packet.hasRemaining()) {
                packetLease.close();
                return CompletableFuture.completedFuture(result.bytesConsumed());
            }
        } catch (final RuntimeException e) {
            packetLease.close();
            throw e;
        }
        try {
            return dispatcher.supply("tls:write", () -> {
                try {
                    writeAll(packet);
                    return result.bytesConsumed();
                } finally {
                    packetLease.close();
                }
            });
        } catch (final RuntimeException e) {
            packetLease.close();
            throw e;
        }
    }

    /**
     * Returns whether this channel is open.
     *
     * @return true when opened
     */
    public boolean opened() {
        return state.get() == Status.OPENED && conduit.opened();
    }

    /**
     * Closes TLS and network resources once.
     */
    @Override
    public void close() {
        if (state.getAndSet(Status.CLOSED) == Status.CLOSED) {
            return;
        }
        RuntimeException failure = null;
        try {
            sendCloseNotify();
        } catch (final RuntimeException e) {
            failure = e;
        }
        try {
            engine.close();
        } catch (final RuntimeException e) {
            failure = failure == null ? e : failure;
        }
        try {
            conduit.close();
        } catch (final RuntimeException e) {
            if (failure == null) {
                failure = new SocketException("Unable to close TLS channel", e);
            }
        }
        if (ownsDispatcher) {
            try {
                dispatcher.close();
            } catch (final RuntimeException e) {
                if (failure == null) {
                    failure = e;
                }
            }
        }
        packetBuffers.close();
        applicationBuffers.close();
        listener.close(this);
        if (failure != null) {
            throw failure;
        }
    }

    /**
     * Returns a packet buffer size for wrapping data.
     *
     * @param plainBytes plain byte count
     * @return packet buffer size
     */
    private int packetSize(final int plainBytes) {
        return Math.max(engine.engine().getSession().getPacketBufferSize(), plainBytes + EXTRA_PACKET_BYTES);
    }

    /**
     * Drives the TLS handshake state machine.
     *
     * @return handshake metadata
     */
    private TlsHandshake driveHandshake() {
        final Status previous = state.getAndSet(Status.RUNNING);
        if (previous == Status.CLOSED) {
            throw new StatefulException("TLS channel is closed");
        }
        final ByteBuffer empty = ByteBuffer.allocate(Normal._0);
        final NioBuffer inboundLease = packetBuffers.allocate();
        NioBuffer applicationLease = applicationBuffers.allocate();
        final ByteBuffer inbound = inboundLease.buffer();
        ByteBuffer application = applicationLease.buffer();
        inbound.flip();
        try {
            engine.engine().beginHandshake();
            javax.net.ssl.SSLEngineResult.HandshakeStatus status = engine.engine().getHandshakeStatus();
            while (status != javax.net.ssl.SSLEngineResult.HandshakeStatus.FINISHED
                    && status != javax.net.ssl.SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
                switch (status) {
                    case NEED_WRAP -> {
                        final ByteBuffer outbound = ByteBuffer
                                .allocate(engine.engine().getSession().getPacketBufferSize());
                        final SSLEngineResult result = engine.wrap(empty, outbound);
                        outbound.flip();
                        writeAll(outbound);
                        status = result.getHandshakeStatus();
                    }
                    case NEED_UNWRAP -> {
                        if (!inbound.hasRemaining()) {
                            inbound.compact();
                            final int read = readInto(inbound);
                            if (read < Normal._0) {
                                throw new SocketException("TLS handshake reached EOF");
                            }
                            inbound.flip();
                        }
                        final SSLEngineResult result = engine.unwrap(inbound, application);
                        status = result.getHandshakeStatus();
                        if (result.getStatus() == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
                            inbound.compact();
                            final int read = readInto(inbound);
                            if (read < Normal._0) {
                                throw new SocketException("TLS handshake reached EOF");
                            }
                            inbound.flip();
                        } else if (result.getStatus() == SSLEngineResult.Status.BUFFER_OVERFLOW) {
                            applicationLease.close();
                            applicationLease = applicationBuffers.allocate(application.capacity() * Normal._2);
                            application = applicationLease.buffer();
                        }
                    }
                    case NEED_TASK -> {
                        engine.task().run();
                        status = engine.engine().getHandshakeStatus();
                    }
                    default -> throw new StatefulException("Unexpected TLS handshake status: " + status);
                }
            }
            state.set(Status.OPENED);
            return engine.handshake();
        } catch (final RuntimeException e) {
            state.set(Status.FAILED);
            closeAfterFailure(e);
            throw e;
        } catch (final Exception e) {
            state.set(Status.FAILED);
            final SocketException failure = new SocketException("TLS handshake failed", e);
            closeAfterFailure(failure);
            throw failure;
        } finally {
            inboundLease.close();
            applicationLease.close();
        }
    }

    /**
     * Sends TLS close_notify before closing the underlying conduit.
     */
    private void sendCloseNotify() {
        if (!conduit.opened()) {
            return;
        }
        engine.engine().closeOutbound();
        final ByteBuffer empty = ByteBuffer.allocate(Normal._0);
        try (NioBuffer outboundLease = packetBuffers.allocate()) {
            final ByteBuffer outbound = outboundLease.buffer();
            while (!engine.engine().isOutboundDone()) {
                final SSLEngineResult result = engine.wrap(empty, outbound);
                if (result.getHandshakeStatus() == javax.net.ssl.SSLEngineResult.HandshakeStatus.NEED_TASK) {
                    engine.task().run();
                }
                outbound.flip();
                if (outbound.hasRemaining()) {
                    writeAll(outbound);
                }
                outbound.clear();
                if (result.bytesProduced() == Normal._0
                        && result.getHandshakeStatus() != javax.net.ssl.SSLEngineResult.HandshakeStatus.NEED_WRAP) {
                    break;
                }
            }
        }
    }

    /**
     * Closes transport resources after handshake failure.
     *
     * @param cause failure cause
     */
    private void closeAfterFailure(final RuntimeException cause) {
        try {
            engine.close();
        } catch (final RuntimeException e) {
            cause.addSuppressed(e);
        }
        try {
            conduit.close();
        } catch (final RuntimeException e) {
            cause.addSuppressed(e);
        }
        packetBuffers.close();
        applicationBuffers.close();
    }

    /**
     * Reads plain bytes by unwrapping TLS records.
     *
     * @param target target buffer
     * @return bytes produced
     */
    private int readPlain(final ByteBuffer target) {
        try (NioBuffer inboundLease = packetBuffers.allocate()) {
            final ByteBuffer inbound = inboundLease.buffer();
            final ByteBuffer view = target.duplicate();
            int produced = Normal._0;
            while (view.hasRemaining()) {
                inbound.clear();
                final int read = readInto(inbound);
                if (read <= Normal._0) {
                    return produced > Normal._0 ? produced : read;
                }
                inbound.flip();
                while (inbound.hasRemaining()) {
                    final SSLEngineResult result = engine.unwrap(inbound, view);
                    produced += result.bytesProduced();
                    if (produced > Normal._0) {
                        return produced;
                    }
                    if (result.getHandshakeStatus() == javax.net.ssl.SSLEngineResult.HandshakeStatus.NEED_TASK) {
                        engine.task().run();
                    }
                    if (result.getStatus() == SSLEngineResult.Status.CLOSED) {
                        return Normal.__1;
                    }
                    if (result.getStatus() == SSLEngineResult.Status.BUFFER_UNDERFLOW
                            || result.getStatus() == SSLEngineResult.Status.BUFFER_OVERFLOW
                            || (result.bytesConsumed() == Normal._0 && result.bytesProduced() == Normal._0)) {
                        break;
                    }
                }
            }
            return produced;
        }
    }

    /**
     * Writes all bytes from a buffer.
     *
     * @param source source buffer
     */
    private void writeAll(final ByteBuffer source) {
        while (source.hasRemaining()) {
            final int position = source.position();
            final int written = await(conduit.write(source));
            if (written < Normal._0) {
                throw new SocketException("TLS write reached EOF");
            }
            if (written == Normal._0) {
                Thread.yield();
            } else {
                source.position(position + written);
            }
        }
    }

    /**
     * Reads bytes into a buffer and advances its position.
     *
     * @param target target buffer
     * @return bytes read
     */
    private int readInto(final ByteBuffer target) {
        final int position = target.position();
        final int read = await(conduit.read(target));
        if (read > 0) {
            target.position(position + read);
        }
        return read;
    }

    /**
     * Waits for a channel operation.
     *
     * @param future future
     * @return operation result
     */
    private static int await(final CompletableFuture<Integer> future) {
        try {
            return future.get(OPERATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (final java.util.concurrent.TimeoutException e) {
            throw new TimeoutException("TLS channel operation timed out", e);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InternalException("Interrupted while waiting for TLS channel operation", e);
        } catch (final ExecutionException e) {
            throw new SocketException("TLS channel operation failed", e.getCause());
        }
    }

}
