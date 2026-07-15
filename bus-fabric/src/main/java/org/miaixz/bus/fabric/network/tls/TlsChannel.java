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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.SSLEngineResult;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.buffer.NioBuffer;
import org.miaixz.bus.core.io.buffer.NioBufferAllocator;
import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.crypto.builtin.TlsHandshake;
import org.miaixz.bus.fabric.Lifecycle;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.network.Conduit;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.observe.ObservationMarker;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;
import org.miaixz.bus.fabric.runtime.lifecycle.LifecycleScope;

/**
 * TLS channel wrapper over a network conduit and SSLEngine adapter.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class TlsChannel implements Lifecycle, AutoCloseable {

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
     * Lifecycle scope.
     */
    private final LifecycleScope scope;

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
     * Source view for protocol readers.
     */
    private final Source source;

    /**
     * Sink view for protocol writers.
     */
    private final Sink sink;

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
                .heap(this.engine.engine().getSession().getPacketBufferSize(), Normal._4);
        this.applicationBuffers = NioBufferAllocator
                .heap(this.engine.engine().getSession().getApplicationBufferSize(), Normal._4);
        this.scope = LifecycleScope
                .session(this, "tls-channel", listener, EventObserver.noop(), null, null, ObservationMarker.TLS_FAILED);
        this.dispatcher = Assert.notNull(dispatcher, () -> new ValidateException("TLS dispatcher must not be null"));
        this.ownsDispatcher = ownsDispatcher;
        this.notified = new AtomicBoolean();
        this.source = new TlsSource();
        this.sink = new TlsSink();
    }

    /**
     * Wraps a network conduit.
     *
     * @param conduit network conduit
     * @param engine  TLS engine
     * @return TLS channel
     */
    public static TlsChannel wrap(final Conduit conduit, final TlsEngine engine) {
        return new TlsChannel(conduit, engine, null, Dispatcher.create(), true);
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
        return new TlsChannel(conduit, engine, listener, Dispatcher.create(), true);
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
        return new TlsChannel(conduit, engine, listener,
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
        return dispatcher.supply("tls:handshake", this::driveHandshake);
    }

    /**
     * Reads and unwraps TLS bytes into a core.io buffer.
     *
     * @param target    target buffer
     * @param byteCount maximum byte count
     * @return read future
     */
    public CompletableFuture<Long> read(final Buffer target, final long byteCount) {
        final Buffer checkedTarget = Assert
                .notNull(target, () -> new ValidateException("TLS read target must not be null"));
        Assert.isTrue(byteCount >= Normal._0, () -> new ValidateException("TLS read byte count must not be negative"));
        if (!opened()) {
            return CompletableFuture.failedFuture(new StatefulException("TLS channel is closed"));
        }
        if (byteCount == Normal._0) {
            return CompletableFuture.completedFuture(0L);
        }
        return dispatcher.supply("tls:read", () -> {
            final ByteBuffer plain = ByteBuffer.allocate(toIntSize(byteCount));
            final int read = readPlain(plain);
            if (read > Normal._0) {
                plain.flip();
                try {
                    checkedTarget.write(plain);
                } catch (final IOException e) {
                    throw new SocketException("Unable to stage TLS plain read", e);
                }
            }
            return (long) read;
        });
    }

    /**
     * Wraps and writes TLS bytes from a core.io buffer.
     *
     * @param source    source buffer
     * @param byteCount byte count to write
     * @return write future
     */
    public CompletableFuture<Long> write(final Buffer source, final long byteCount) {
        final Buffer checkedSource = Assert
                .notNull(source, () -> new ValidateException("TLS write source must not be null"));
        Assert.isTrue(byteCount >= Normal._0, () -> new ValidateException("TLS write byte count must not be negative"));
        Assert.isTrue(
                byteCount <= checkedSource.size(),
                () -> new ValidateException("TLS write byte count must not exceed source size"));
        if (!opened()) {
            return CompletableFuture.failedFuture(new StatefulException("TLS channel is closed"));
        }
        final ByteBuffer view = checkedSource.nioBuffer(toIntSize(byteCount));
        if (!view.hasRemaining()) {
            return CompletableFuture.completedFuture(0L);
        }
        final NioBuffer packetLease = packetBuffers.allocate(packetSize(view.remaining()));
        final ByteBuffer packet = packetLease.buffer();
        final SSLEngineResult result;
        try {
            result = engine.wrap(view, packet);
            packet.flip();
            if (!packet.hasRemaining()) {
                packetLease.close();
                consumePlaintext(checkedSource, result.bytesConsumed());
                return CompletableFuture.completedFuture((long) result.bytesConsumed());
            }
        } catch (final RuntimeException e) {
            packetLease.close();
            throw e;
        }
        try {
            return dispatcher.supply("tls:write", () -> {
                try {
                    writeAll(packet);
                    consumePlaintext(checkedSource, result.bytesConsumed());
                    return (long) result.bytesConsumed();
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
     * Returns the protocol-layer source.
     *
     * @return source view
     */
    public Source source() {
        return source;
    }

    /**
     * Returns the protocol-layer sink.
     *
     * @return sink view
     */
    public Sink sink() {
        return sink;
    }

    /**
     * Returns whether this channel is open.
     *
     * @return true when opened
     */
    @Override
    public boolean opened() {
        final Status current = scope.state();
        return (current == Status.QUEUED || current == Status.OPENED) && conduit.opened();
    }

    /**
     * Returns lifecycle state.
     *
     * @return state
     */
    @Override
    public Status state() {
        return scope.state();
    }

    /**
     * Closes TLS and network resources once.
     */
    @Override
    public void close() {
        if (scope.state().terminal()) {
            return;
        }
        scope.closing();
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
        scope.close(this);
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
        return Math.max(engine.engine().getSession().getPacketBufferSize(), plainBytes + Normal._1024);
    }

    /**
     * Drives the TLS handshake state machine.
     *
     * @return handshake metadata
     */
    private TlsHandshake driveHandshake() {
        if (scope.state().terminal()) {
            throw new StatefulException("TLS channel is closed");
        }
        scope.start();
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
            if (notified.compareAndSet(false, true)) {
                scope.open(this);
            }
            return engine.handshake();
        } catch (final RuntimeException e) {
            closeAfterFailure(e);
            scope.fail(e);
            throw e;
        } catch (final Exception e) {
            final SocketException failure = new SocketException("TLS handshake failed", e);
            closeAfterFailure(failure);
            scope.fail(failure);
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
        final Buffer payload = new Buffer();
        try {
            payload.write(source);
        } catch (final IOException e) {
            throw new SocketException("Unable to stage TLS network write", e);
        }
        while (payload.size() > Normal._0) {
            final long written = await(conduit.write(payload, payload.size()));
            if (written < Normal._0) {
                throw new SocketException("TLS write reached EOF");
            }
            if (written == Normal._0) {
                Thread.yield();
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
        final Buffer payload = new Buffer();
        final long read = await(conduit.read(payload, target.remaining()));
        if (read > 0) {
            try {
                payload.read(target);
            } catch (final IOException e) {
                throw new SocketException("Unable to stage TLS network read", e);
            }
        }
        return toIntSize(read);
    }

    /**
     * Waits for a channel operation.
     *
     * @param future future
     * @return operation result
     */
    private static long await(final CompletableFuture<Long> future) {
        try {
            return future.get(Normal._5, TimeUnit.SECONDS);
        } catch (final java.util.concurrent.TimeoutException e) {
            throw new TimeoutException("TLS channel operation timed out", e);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InternalException("Interrupted while waiting for TLS channel operation", e);
        } catch (final ExecutionException e) {
            throw new SocketException("TLS channel operation failed", e.getCause());
        }
    }

    /**
     * Converts a long byte count to an int size accepted by JDK buffers.
     *
     * @param byteCount byte count
     * @return int size
     */
    private static int toIntSize(final long byteCount) {
        return (int) Math.min(byteCount, Integer.MAX_VALUE);
    }

    /**
     * Consumes plaintext bytes after they have been accepted by the TLS engine.
     *
     * @param source source buffer
     * @param count  byte count
     */
    private static void consumePlaintext(final Buffer source, final int count) {
        if (count <= Normal._0) {
            return;
        }
        try {
            source.skip(count);
        } catch (final IOException e) {
            throw new SocketException("Unable to consume TLS plaintext", e);
        }
    }

    /**
     * Source backed by this TLS channel.
     */
    private final class TlsSource implements Source {

        /**
         * Reads bytes through the enclosing TLS channel.
         *
         * @param sink      target buffer
         * @param byteCount maximum byte count
         * @return read byte count
         * @throws IOException when reading fails
         */
        @Override
        public long read(final Buffer sink, final long byteCount) throws IOException {
            return await(TlsChannel.this.read(sink, byteCount));
        }

        /**
         * Returns the no-op timeout.
         *
         * @return timeout
         */
        @Override
        public Timeout timeout() {
            return Timeout.NONE;
        }

        /**
         * Closes the enclosing TLS channel.
         */
        @Override
        public void close() {
            TlsChannel.this.close();
        }

    }

    /**
     * Sink backed by this TLS channel.
     */
    private final class TlsSink implements Sink {

        /**
         * Writes bytes through the enclosing TLS channel.
         *
         * @param source    source buffer
         * @param byteCount byte count
         * @throws IOException when writing fails
         */
        @Override
        public void write(final Buffer source, final long byteCount) throws IOException {
            await(TlsChannel.this.write(source, byteCount));
        }

        /**
         * Flushes this TLS sink.
         */
        @Override
        public void flush() {
            // TLS records are written immediately by write(Buffer,long).
        }

        /**
         * Returns the no-op timeout.
         *
         * @return timeout
         */
        @Override
        public Timeout timeout() {
            return Timeout.NONE;
        }

        /**
         * Closes the enclosing TLS channel.
         */
        @Override
        public void close() {
            TlsChannel.this.close();
        }

    }

}
