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
import java.time.Duration;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.buffer.NioBuffer;
import org.miaixz.bus.core.io.buffer.NioBufferAllocator;
import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.crypto.builtin.TlsHandshake;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Lifecycle;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.Timeout;
import org.miaixz.bus.fabric.network.Conduit;
import org.miaixz.bus.fabric.observe.metrics.FabricMeter;
import org.miaixz.bus.fabric.observe.metrics.FabricMeter.Counter;
import org.miaixz.bus.fabric.runtime.Activity;
import org.miaixz.bus.fabric.runtime.dispatch.DispatchHandle;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;

/**
 * Concurrent TLS state machine exposing one plaintext {@link Conduit} boundary.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class TlsChannel implements Conduit, Lifecycle {

    /**
     * Empty plaintext used for handshake and close records.
     */
    private static final ByteBuffer EMPTY = ByteBuffer.wrap(new byte[Normal._0]).asReadOnlyBuffer();

    /**
     * Underlying encrypted conduit.
     */
    private final Conduit conduit;

    /**
     * TLS engine adapter.
     */
    private final TlsEngine engine;

    /**
     * Borrowed runtime dispatcher.
     */
    private final Dispatcher dispatcher;

    /**
     * Operation timeout policy.
     */
    private final Timeout timeout;

    /**
     * Borrowed runtime meter, or null when metrics are disabled.
     */
    private final FabricMeter meter;

    /**
     * Optional lifecycle listener.
     */
    private final Listener<Object> listener;

    /**
     * Serializes handshake ownership.
     */
    private final ReentrantLock handshakeLock;

    /**
     * Serializes unwrap and encrypted reads.
     */
    private final ReentrantLock readLock;

    /**
     * Serializes wrap, encrypted writes, and close_notify.
     */
    private final ReentrantLock writeLock;

    /**
     * Protects state, failure, and staging-buffer references.
     */
    private final ReentrantLock stateLock;

    /**
     * Allocator for persistent encrypted staging buffers.
     */
    private final NioBufferAllocator packetBuffers;

    /**
     * Allocator for the persistent plaintext staging buffer.
     */
    private final NioBufferAllocator applicationBuffers;

    /**
     * Current encrypted-input lease.
     */
    private NioBuffer encryptedInputLease;

    /**
     * Current pending-plaintext lease.
     */
    private NioBuffer pendingPlaintextLease;

    /**
     * Current encrypted-output lease.
     */
    private NioBuffer encryptedOutputLease;

    /**
     * Persistent encrypted input in read mode.
     */
    private ByteBuffer encryptedInput;

    /**
     * Persistent pending plaintext in read mode.
     */
    private ByteBuffer pendingPlaintext;

    /**
     * Persistent encrypted output in read mode while idle.
     */
    private ByteBuffer encryptedOutput;

    /**
     * Current TLS state.
     */
    private TlsState tlsState;

    /**
     * First terminal failure.
     */
    private Throwable failure;

    /**
     * Ensures persistent leases and allocators close once.
     */
    private final AtomicBoolean buffersClosed;

    /**
     * Ensures close_notify is emitted once.
     */
    private final AtomicBoolean closeNotifySent;

    /**
     * Ensures the open listener fires once.
     */
    private final AtomicBoolean openNotified;

    /**
     * Ensures the close listener fires once.
     */
    private final AtomicBoolean closeNotified;

    /**
     * Plaintext source view.
     */
    private final Source source;

    /**
     * Plaintext sink view.
     */
    private final Sink sink;

    /**
     * Creates a TLS state machine borrowing all runtime resources.
     *
     * @param conduit    encrypted conduit
     * @param engine     TLS engine
     * @param listener   lifecycle listener
     * @param dispatcher borrowed dispatcher
     * @param timeout    operation timeouts
     */
    private TlsChannel(final Conduit conduit, final TlsEngine engine, final Listener<Object> listener,
            final Dispatcher dispatcher, final Timeout timeout, final FabricMeter meter) {
        if (conduit == null) {
            throw new ValidateException("Network conduit must not be null");
        }
        if (engine == null) {
            throw new ValidateException("TLS engine must not be null");
        }
        if (dispatcher == null) {
            throw new ValidateException("TLS dispatcher must not be null");
        }
        if (timeout == null) {
            throw new ValidateException("TLS timeout must not be null");
        }
        this.conduit = conduit;
        this.engine = engine;
        this.listener = listener;
        this.dispatcher = dispatcher;
        this.timeout = timeout;
        this.meter = meter;
        this.handshakeLock = new ReentrantLock();
        this.readLock = new ReentrantLock();
        this.writeLock = new ReentrantLock();
        this.stateLock = new ReentrantLock();
        final int packetSize = checkedInitialSize(engine.packetBufferSize());
        final int applicationSize = checkedInitialSize(engine.applicationBufferSize());
        this.packetBuffers = NioBufferAllocator.heap(packetSize, Normal._4);
        this.applicationBuffers = NioBufferAllocator.heap(applicationSize, Normal._2);
        this.encryptedInputLease = packetBuffers.allocate();
        this.pendingPlaintextLease = applicationBuffers.allocate();
        this.encryptedOutputLease = packetBuffers.allocate();
        this.encryptedInput = emptyReadBuffer(encryptedInputLease.buffer());
        this.pendingPlaintext = emptyReadBuffer(pendingPlaintextLease.buffer());
        this.encryptedOutput = emptyReadBuffer(encryptedOutputLease.buffer());
        this.tlsState = TlsState.NEW;
        this.buffersClosed = new AtomicBoolean();
        this.closeNotifySent = new AtomicBoolean();
        this.openNotified = new AtomicBoolean();
        this.closeNotified = new AtomicBoolean();
        this.source = new TlsSource();
        this.sink = new TlsSink();
    }

    /**
     * Wraps a conduit with default TLS timeouts.
     *
     * @param conduit    encrypted conduit
     * @param engine     TLS engine
     * @param listener   lifecycle listener
     * @param dispatcher borrowed dispatcher
     * @return TLS channel
     */
    public static TlsChannel wrap(
            final Conduit conduit,
            final TlsEngine engine,
            final Listener<Object> listener,
            final Dispatcher dispatcher) {
        return wrap(conduit, engine, listener, dispatcher, Timeout.defaults(), null);
    }

    /**
     * Wraps a conduit with explicit TLS timeouts.
     *
     * @param conduit    encrypted conduit
     * @param engine     TLS engine
     * @param listener   lifecycle listener
     * @param dispatcher borrowed dispatcher
     * @param timeout    operation timeouts
     * @return TLS channel
     */
    public static TlsChannel wrap(
            final Conduit conduit,
            final TlsEngine engine,
            final Listener<Object> listener,
            final Dispatcher dispatcher,
            final Timeout timeout) {
        return wrap(conduit, engine, listener, dispatcher, timeout, null);
    }

    /**
     * Wraps a conduit while borrowing the owning Reactor meter.
     *
     * @param conduit    encrypted conduit
     * @param engine     TLS engine
     * @param listener   lifecycle listener
     * @param dispatcher borrowed dispatcher
     * @param timeout    operation timeouts
     * @param meter      borrowed runtime meter, or null
     * @return TLS channel
     */
    public static TlsChannel wrap(
            final Conduit conduit,
            final TlsEngine engine,
            final Listener<Object> listener,
            final Dispatcher dispatcher,
            final Timeout timeout,
            final FabricMeter meter) {
        return new TlsChannel(conduit, engine, listener, dispatcher, timeout, meter);
    }

    /**
     * Starts or joins the TLS handshake.
     *
     * @return handshake future
     */
    public CompletableFuture<TlsHandshake> handshake() {
        if (!availableForHandshake()) {
            return CompletableFuture.failedFuture(closedFailure("TLS channel cannot handshake"));
        }
        return background("tls:handshake", () -> {
            try {
                return ensureHandshake();
            } catch (final TimeoutException e) {
                fail(e);
                throw e;
            } catch (final RuntimeException e) {
                final TlsException mapped = e instanceof TlsException tls ? tls
                        : new TlsException("TLS handshake failed", e);
                fail(mapped);
                throw mapped;
            }
        });
    }

    /**
     * Reads plaintext while preserving unused TLS records and plaintext.
     *
     * @param target    plaintext target
     * @param byteCount maximum byte count
     * @return read future
     */
    @Override
    public CompletableFuture<Long> read(final Buffer target, final long byteCount) {
        if (target == null) {
            return CompletableFuture.failedFuture(new ValidateException("TLS read target must not be null"));
        }
        if (byteCount < Normal._0) {
            return CompletableFuture.failedFuture(new ValidateException("TLS read byte count must not be negative"));
        }
        if (byteCount == Normal._0) {
            return CompletableFuture.completedFuture(0L);
        }
        if (!availableForIo()) {
            return CompletableFuture.failedFuture(closedFailure("TLS channel cannot read"));
        }
        return background("tls:read", () -> {
            try {
                ensureHandshake();
                readLock.lock();
                try {
                    requireOpenIo("TLS channel cannot read");
                    return readPlaintext(target, byteCount);
                } finally {
                    readLock.unlock();
                }
            } catch (final TimeoutException e) {
                fail(e);
                throw e;
            } catch (final RuntimeException e) {
                if (e instanceof ProtocolException protocol) {
                    fail(protocol);
                    throw protocol;
                }
                final SocketException mapped = e instanceof SocketException socket ? socket
                        : new SocketException("TLS read failed", e);
                fail(mapped);
                throw mapped;
            }
        });
    }

    /**
     * Wraps and completely consumes the requested plaintext bytes.
     *
     * @param source    plaintext source
     * @param byteCount exact byte count
     * @return write future
     */
    @Override
    public CompletableFuture<Long> write(final Buffer source, final long byteCount) {
        if (source == null) {
            return CompletableFuture.failedFuture(new ValidateException("TLS write source must not be null"));
        }
        if (byteCount < Normal._0 || byteCount > source.size()) {
            return CompletableFuture
                    .failedFuture(new ValidateException("TLS write byte count must be between zero and source size"));
        }
        if (byteCount == Normal._0) {
            return CompletableFuture.completedFuture(0L);
        }
        if (!availableForIo()) {
            return CompletableFuture.failedFuture(closedFailure("TLS channel cannot write"));
        }
        return background("tls:write", () -> {
            try {
                ensureHandshake();
                writeLock.lock();
                try {
                    requireOpenIo("TLS channel cannot write");
                    writePlaintext(source, byteCount);
                    return byteCount;
                } finally {
                    writeLock.unlock();
                }
            } catch (final TimeoutException e) {
                fail(e);
                throw e;
            } catch (final RuntimeException e) {
                if (e instanceof ProtocolException protocol) {
                    fail(protocol);
                    throw protocol;
                }
                final SocketException mapped = e instanceof SocketException socket ? socket
                        : new SocketException("TLS write failed", e);
                fail(mapped);
                throw mapped;
            }
        });
    }

    /**
     * Returns the shared plaintext source view.
     *
     * @return source view
     */
    @Override
    public Source source() {
        return source;
    }

    /**
     * Returns the shared plaintext sink view.
     *
     * @return sink view
     */
    @Override
    public Sink sink() {
        return sink;
    }

    /**
     * Returns whether the TLS state machine and conduit can accept work.
     *
     * @return true when available
     */
    @Override
    public boolean opened() {
        stateLock.lock();
        try {
            return (tlsState == TlsState.NEW || tlsState == TlsState.HANDSHAKING || tlsState == TlsState.OPEN)
                    && conduit.opened();
        } finally {
            stateLock.unlock();
        }
    }

    /**
     * Returns the public lifecycle state mapped from the TLS state machine.
     *
     * @return lifecycle state
     */
    @Override
    public Status state() {
        stateLock.lock();
        try {
            return switch (tlsState) {
                case NEW -> Status.QUEUED;
                case HANDSHAKING -> Status.RUNNING;
                case OPEN -> Status.OPENED;
                case CLOSING -> Status.CLOSING;
                case CLOSED -> Status.CLOSED;
                case FAILED -> Status.FAILED;
            };
        } finally {
            stateLock.unlock();
        }
    }

    /**
     * Sends close_notify once and releases owned TLS buffers without owning the dispatcher.
     */
    @Override
    public void close() {
        if (!beginClosing()) {
            return;
        }
        RuntimeException closeFailure = null;
        handshakeLock.lock();
        try {
            writeLock.lock();
            try {
                sendCloseNotify();
            } catch (final RuntimeException e) {
                closeFailure = e instanceof TimeoutException ? e : new TimeoutException("TLS close failed", e);
            } finally {
                writeLock.unlock();
            }
        } finally {
            handshakeLock.unlock();
        }
        try {
            engine.close();
        } catch (final RuntimeException e) {
            if (closeFailure == null) {
                closeFailure = new TimeoutException("TLS engine close failed", e);
            } else {
                closeFailure.addSuppressed(e);
            }
        }
        try {
            conduit.close();
        } catch (final RuntimeException e) {
            if (closeFailure == null) {
                closeFailure = new TimeoutException("TLS conduit close failed", e);
            } else {
                closeFailure.addSuppressed(e);
            }
        } finally {
            releaseBuffers();
            finishClosed();
        }
        if (closeFailure != null) {
            throw closeFailure;
        }
    }

    /**
     * Performs or joins the serialized handshake.
     *
     * @return handshake metadata
     */
    private TlsHandshake ensureHandshake() {
        handshakeLock.lock();
        try {
            final TlsState current = currentState();
            if (current == TlsState.OPEN) {
                return engine.handshake();
            }
            if (current != TlsState.NEW) {
                throw closedFailure("TLS handshake is unavailable");
            }
            transition(TlsState.HANDSHAKING);
            engine.engine().beginHandshake();
            HandshakeStatus status = engine.engine().getHandshakeStatus();
            while (status != HandshakeStatus.FINISHED && status != HandshakeStatus.NOT_HANDSHAKING) {
                status = switch (status) {
                    case NEED_TASK -> runTasks();
                    case NEED_WRAP -> handshakeWrap();
                    case NEED_UNWRAP, NEED_UNWRAP_AGAIN -> handshakeUnwrap(status);
                    default -> throw new TlsException("Unexpected TLS handshake status: " + status);
                };
            }
            transition(TlsState.OPEN);
            recordHandshake(engine.sessionReuse());
            notifyOpen();
            return engine.handshake();
        } catch (final IOException e) {
            throw new TlsException("TLS handshake failed", e);
        } finally {
            handshakeLock.unlock();
        }
    }

    /**
     * Produces and writes one handshake record.
     *
     * @return next handshake status
     */
    private HandshakeStatus handshakeWrap() {
        writeLock.lock();
        try {
            while (true) {
                prepareEncryptedOutput();
                final SSLEngineResult result = engine.wrap(EMPTY.duplicate(), encryptedOutput);
                if (result.getStatus() == SSLEngineResult.Status.BUFFER_OVERFLOW) {
                    growEncryptedOutput();
                    continue;
                }
                encryptedOutput.flip();
                writeEncryptedOutput(timeout.connect(), "TLS handshake write timed out");
                if (result.getStatus() == SSLEngineResult.Status.CLOSED) {
                    throw new TlsException("TLS engine closed during handshake write");
                }
                return result.getHandshakeStatus();
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Reads and consumes handshake records while retaining early plaintext.
     *
     * @param requested current handshake status
     * @return next handshake status
     */
    private HandshakeStatus handshakeUnwrap(final HandshakeStatus requested) {
        readLock.lock();
        try {
            if (requested != HandshakeStatus.NEED_UNWRAP_AGAIN && !encryptedInput.hasRemaining()) {
                if (readEncryptedInput(timeout.connect(), "TLS handshake read timed out") == Normal.__1) {
                    throw new TlsException("TLS peer closed during handshake");
                }
            }
            while (true) {
                preparePendingPlaintextForAppend();
                final SSLEngineResult result = engine.unwrap(encryptedInput, pendingPlaintext);
                pendingPlaintext.flip();
                if (result.getStatus() == SSLEngineResult.Status.BUFFER_OVERFLOW) {
                    growPendingPlaintext();
                    continue;
                }
                if (result.getStatus() == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
                    ensureEncryptedInputCapacity();
                    if (readEncryptedInput(timeout.connect(), "TLS handshake read timed out") == Normal.__1) {
                        throw new TlsException("TLS peer closed during handshake");
                    }
                    continue;
                }
                if (result.getStatus() == SSLEngineResult.Status.CLOSED) {
                    throw new TlsException("TLS peer closed during handshake");
                }
                return result.getHandshakeStatus();
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Reads plaintext under the read lock.
     *
     * @param target    plaintext target
     * @param byteCount maximum byte count
     * @return produced byte count or EOF
     */
    private long readPlaintext(final Buffer target, final long byteCount) {
        final long pending = drainPlaintext(target, byteCount);
        if (pending > Normal._0) {
            return pending;
        }
        while (true) {
            if (!encryptedInput.hasRemaining()) {
                final long read = readEncryptedInput(timeout.read(), "TLS read timed out");
                if (read == Normal.__1) {
                    return Normal.__1;
                }
                if (read == Normal._0) {
                    return Normal._0;
                }
            }
            preparePendingPlaintextForAppend();
            final SSLEngineResult result = engine.unwrap(encryptedInput, pendingPlaintext);
            pendingPlaintext.flip();
            if (result.getStatus() == SSLEngineResult.Status.BUFFER_OVERFLOW) {
                growPendingPlaintext();
                continue;
            }
            if (result.getStatus() == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
                ensureEncryptedInputCapacity();
                final long read = readEncryptedInput(timeout.read(), "TLS read timed out");
                if (read == Normal.__1) {
                    return Normal.__1;
                }
                continue;
            }
            if (result.getHandshakeStatus() == HandshakeStatus.NEED_TASK) {
                runTasks();
            }
            final long produced = drainPlaintext(target, byteCount);
            if (produced > Normal._0) {
                return produced;
            }
            if (result.getStatus() == SSLEngineResult.Status.CLOSED) {
                return Normal.__1;
            }
            if (result.bytesConsumed() == Normal._0 && result.bytesProduced() == Normal._0) {
                throw new SocketException("TLS read made no engine progress");
            }
        }
    }

    /**
     * Completely wraps and writes plaintext under the write lock.
     *
     * @param source    plaintext source
     * @param byteCount exact byte count
     */
    private void writePlaintext(final Buffer source, final long byteCount) {
        long remaining = byteCount;
        while (remaining > Normal._0) {
            final ByteBuffer plaintext = source.nioBuffer(toIntSize(remaining));
            prepareEncryptedOutput();
            final SSLEngineResult result = engine.wrap(plaintext, encryptedOutput);
            if (result.getStatus() == SSLEngineResult.Status.BUFFER_OVERFLOW) {
                growEncryptedOutput();
                continue;
            }
            encryptedOutput.flip();
            writeEncryptedOutput(timeout.write(), "TLS write timed out");
            consume(source, result.bytesConsumed());
            remaining -= result.bytesConsumed();
            if (result.getHandshakeStatus() == HandshakeStatus.NEED_TASK) {
                runTasks();
            }
            if (result.getStatus() == SSLEngineResult.Status.CLOSED) {
                throw new SocketException("TLS engine closed during write");
            }
            if (result.bytesConsumed() == Normal._0 && result.bytesProduced() == Normal._0) {
                throw new SocketException("TLS write made no engine progress");
            }
        }
    }

    /**
     * Sends one close_notify sequence.
     */
    private void sendCloseNotify() {
        if (!closeNotifySent.compareAndSet(false, true) || !conduit.opened()) {
            return;
        }
        engine.engine().closeOutbound();
        while (!engine.engine().isOutboundDone()) {
            prepareEncryptedOutput();
            final SSLEngineResult result = engine.wrap(EMPTY.duplicate(), encryptedOutput);
            if (result.getStatus() == SSLEngineResult.Status.BUFFER_OVERFLOW) {
                growEncryptedOutput();
                continue;
            }
            encryptedOutput.flip();
            writeEncryptedOutput(timeout.close(), "TLS close_notify timed out");
            if (result.getHandshakeStatus() == HandshakeStatus.NEED_TASK) {
                runTasks();
            }
            if (result.bytesProduced() == Normal._0 && result.getHandshakeStatus() != HandshakeStatus.NEED_WRAP) {
                break;
            }
        }
    }

    /**
     * Runs all currently delegated engine tasks in order.
     *
     * @return resulting handshake status
     */
    private HandshakeStatus runTasks() {
        engine.task().run();
        return engine.engine().getHandshakeStatus();
    }

    /**
     * Reads encrypted bytes while retaining any unconsumed prefix.
     *
     * @param duration timeout duration
     * @param message  timeout message
     * @return network read count
     */
    private long readEncryptedInput(final Duration duration, final String message) {
        encryptedInput.compact();
        if (!encryptedInput.hasRemaining()) {
            encryptedInput.flip();
            growEncryptedInput();
            encryptedInput.compact();
        }
        final int writable = encryptedInput.remaining();
        final long read = await(conduit.read(encryptedInput), duration, message);
        if (read > Normal._0) {
            if (read > writable) {
                throw new InternalException("Encrypted conduit returned an invalid read count");
            }
            addBytes(Counter.BYTES_READ, read);
        }
        encryptedInput.flip();
        return read;
    }

    /**
     * Writes all bytes currently staged in encrypted output.
     *
     * @param duration timeout duration
     * @param message  timeout message
     */
    private void writeEncryptedOutput(final Duration duration, final String message) {
        if (!encryptedOutput.hasRemaining()) {
            return;
        }
        final Buffer payload = new Buffer();
        final int bytes = encryptedOutput.remaining();
        transfer(encryptedOutput, payload);
        final long written = await(conduit.write(payload, bytes), duration, message);
        if (written != bytes || payload.size() != Normal._0) {
            throw new InternalException("Encrypted conduit did not completely consume TLS output");
        }
        addBytes(Counter.BYTES_WRITTEN, written);
        encryptedOutput.clear();
        encryptedOutput.flip();
    }

    /**
     * Drains retained plaintext into a core buffer.
     *
     * @param target    target buffer
     * @param byteCount maximum byte count
     * @return drained byte count
     */
    private long drainPlaintext(final Buffer target, final long byteCount) {
        if (!pendingPlaintext.hasRemaining()) {
            return Normal._0;
        }
        final int count = toIntSize(Math.min(byteCount, pendingPlaintext.remaining()));
        final ByteBuffer view = pendingPlaintext.slice();
        view.limit(count);
        transfer(view, target);
        pendingPlaintext.position(pendingPlaintext.position() + count);
        return count;
    }

    /**
     * Changes pending plaintext from read mode to append mode.
     */
    private void preparePendingPlaintextForAppend() {
        pendingPlaintext.compact();
    }

    /**
     * Changes encrypted output from idle read mode to write mode.
     */
    private void prepareEncryptedOutput() {
        encryptedOutput.compact();
    }

    /**
     * Ensures encrypted input can accept at least one more packet fragment.
     */
    private void ensureEncryptedInputCapacity() {
        if (encryptedInput.limit() == encryptedInput.capacity()
                || encryptedInput.capacity() < engine.packetBufferSize()) {
            growEncryptedInput();
        }
    }

    /**
     * Expands encrypted input while preserving its read-mode contents.
     */
    private void growEncryptedInput() {
        final int capacity = grownCapacity(encryptedInput.capacity(), engine.packetBufferSize(), "encrypted input");
        final NioBuffer replacementLease = packetBuffers.allocate(capacity);
        final ByteBuffer replacement = replacementLease.buffer();
        replacement.put(encryptedInput);
        replacement.flip();
        final NioBuffer retired;
        stateLock.lock();
        try {
            retired = encryptedInputLease;
            encryptedInputLease = replacementLease;
            encryptedInput = replacement;
        } finally {
            stateLock.unlock();
        }
        retired.close();
    }

    /**
     * Expands pending plaintext while preserving its read-mode contents.
     */
    private void growPendingPlaintext() {
        final int capacity = grownCapacity(
                pendingPlaintext.capacity(),
                engine.applicationBufferSize(),
                "pending plaintext");
        final NioBuffer replacementLease = applicationBuffers.allocate(capacity);
        final ByteBuffer replacement = replacementLease.buffer();
        replacement.put(pendingPlaintext);
        replacement.flip();
        final NioBuffer retired;
        stateLock.lock();
        try {
            retired = pendingPlaintextLease;
            pendingPlaintextLease = replacementLease;
            pendingPlaintext = replacement;
        } finally {
            stateLock.unlock();
        }
        retired.close();
    }

    /**
     * Expands encrypted output after engine overflow.
     */
    private void growEncryptedOutput() {
        encryptedOutput.flip();
        final int capacity = grownCapacity(encryptedOutput.capacity(), engine.packetBufferSize(), "encrypted output");
        final NioBuffer replacementLease = packetBuffers.allocate(capacity);
        final ByteBuffer replacement = replacementLease.buffer();
        replacement.put(encryptedOutput);
        replacement.flip();
        final NioBuffer retired;
        stateLock.lock();
        try {
            retired = encryptedOutputLease;
            encryptedOutputLease = replacementLease;
            encryptedOutput = replacement;
        } finally {
            stateLock.unlock();
        }
        retired.close();
    }

    /**
     * Returns a bounded doubled staging capacity.
     *
     * @param current current capacity
     * @param session current SSL session recommendation
     * @param name    buffer name
     * @return expanded capacity
     */
    private static int grownCapacity(final int current, final int session, final String name) {
        final long requested = Math.max((long) current * Normal._2, session);
        if (requested > Builder.TLS_MAX_STAGING_BUFFER_BYTES || requested <= current) {
            throw new ProtocolException("TLS " + name + " exceeded the 1 MiB staging limit");
        }
        return (int) requested;
    }

    /**
     * Waits for one conduit operation with its configured deadline.
     *
     * @param future   operation future
     * @param duration timeout duration
     * @param message  timeout message
     * @return operation byte count
     */
    private long await(
            final CompletableFuture<? extends Number> future,
            final Duration duration,
            final String message) {
        if (future == null) {
            throw new InternalException("TLS conduit returned a null future");
        }
        DispatchHandle deadline = null;
        try {
            if (!duration.isZero()) {
                deadline = dispatcher.schedule("tls:timeout", duration, Activity.of("tls:timeout", () -> {
                    final TimeoutException timedOut = new TimeoutException(message,
                            new java.util.concurrent.TimeoutException(message));
                    if (future.completeExceptionally(timedOut)) {
                        closeConduit(timedOut);
                    }
                }));
            }
            final Number value = future.get();
            if (value == null) {
                throw new InternalException("TLS conduit returned a null byte count");
            }
            return value.longValue();
        } catch (final InterruptedException e) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            final SocketException interrupted = new SocketException("TLS operation was interrupted", e);
            closeConduit(interrupted);
            throw interrupted;
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof TimeoutException timedOut) {
                throw timedOut;
            }
            if (cause instanceof RuntimeException runtime) {
                throw runtime;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new SocketException("TLS conduit operation failed", cause);
        } catch (final CancellationException e) {
            final SocketException cancelled = new SocketException("TLS conduit operation was cancelled", e);
            closeConduit(cancelled);
            throw cancelled;
        } finally {
            if (deadline != null) {
                dispatcher.cancel(deadline);
            }
        }
    }

    /**
     * Submits a blocking TLS operation to the dispatcher's background channel.
     *
     * @param name      activity name
     * @param operation operation supplier
     * @param <T>       result type
     * @return operation future
     */
    private <T> CompletableFuture<T> background(final String name, final Supplier<T> operation) {
        final CompletableFuture<T> result = new CompletableFuture<>();
        final Activity activity = Activity.of(name, () -> {
            if (result.isCancelled()) {
                return;
            }
            try {
                result.complete(operation.get());
            } catch (final Throwable e) {
                result.completeExceptionally(e);
            }
        });
        final DispatchHandle handle;
        try {
            handle = dispatcher.background(name, this, activity);
        } catch (final RuntimeException e) {
            result.completeExceptionally(e);
            return result;
        }
        result.whenComplete((value, cause) -> {
            if (result.isCancelled()) {
                dispatcher.cancel(handle);
                final SocketException cancelled = new SocketException("TLS operation was cancelled",
                        new CancellationException(name));
                fail(cancelled);
            }
        });
        return result;
    }

    /**
     * Marks a nonterminal state machine failed and releases transport resources.
     *
     * @param cause failure cause
     */
    private void fail(final RuntimeException cause) {
        boolean changed = false;
        boolean handshakeFailure = false;
        stateLock.lock();
        try {
            if (tlsState != TlsState.CLOSED && tlsState != TlsState.FAILED) {
                handshakeFailure = tlsState == TlsState.HANDSHAKING;
                tlsState = TlsState.FAILED;
                failure = cause;
                changed = true;
            }
        } finally {
            stateLock.unlock();
        }
        if (!changed) {
            return;
        }
        if (handshakeFailure && meter != null) {
            meter.incrementCounter(Counter.TLS_HANDSHAKE_FAILURES);
        }
        closeConduit(cause);
        readLock.lock();
        try {
            // Wait for any read that was already inside the engine or conduit.
        } finally {
            readLock.unlock();
        }
        writeLock.lock();
        try {
            // Wait for any write that was already inside the engine or conduit.
        } finally {
            writeLock.unlock();
        }
        try {
            engine.close();
        } catch (final RuntimeException e) {
            cause.addSuppressed(e);
        }
        releaseBuffers();
        notifyFailure(cause);
    }

    /**
     * Closes the underlying conduit and preserves cleanup failure.
     *
     * @param cause primary failure
     */
    private void closeConduit(final RuntimeException cause) {
        try {
            conduit.close();
        } catch (final RuntimeException e) {
            cause.addSuppressed(e);
        }
    }

    /**
     * Releases every current persistent lease and allocator once.
     */
    private void releaseBuffers() {
        if (!buffersClosed.compareAndSet(false, true)) {
            return;
        }
        final NioBuffer input;
        final NioBuffer plaintext;
        final NioBuffer output;
        stateLock.lock();
        try {
            input = encryptedInputLease;
            plaintext = pendingPlaintextLease;
            output = encryptedOutputLease;
        } finally {
            stateLock.unlock();
        }
        input.close();
        plaintext.close();
        output.close();
        packetBuffers.close();
        applicationBuffers.close();
    }

    /**
     * Starts the single close transition.
     *
     * @return true for the close owner
     */
    private boolean beginClosing() {
        stateLock.lock();
        try {
            if (tlsState == TlsState.CLOSED || tlsState == TlsState.CLOSING || tlsState == TlsState.FAILED) {
                return false;
            }
            tlsState = TlsState.CLOSING;
            return true;
        } finally {
            stateLock.unlock();
        }
    }

    /**
     * Completes the close transition and notification.
     */
    private void finishClosed() {
        stateLock.lock();
        try {
            tlsState = TlsState.CLOSED;
        } finally {
            stateLock.unlock();
        }
        if (listener != null && closeNotified.compareAndSet(false, true)) {
            try {
                listener.close(this);
            } catch (final RuntimeException ignored) {
                // Listener failures do not reopen a closed transport.
            }
        }
    }

    /**
     * Changes the TLS state under the state lock.
     *
     * @param next next state
     */
    private void transition(final TlsState next) {
        stateLock.lock();
        try {
            tlsState = next;
        } finally {
            stateLock.unlock();
        }
    }

    /**
     * Returns the TLS state under the state lock.
     *
     * @return current TLS state
     */
    private TlsState currentState() {
        stateLock.lock();
        try {
            return tlsState;
        } finally {
            stateLock.unlock();
        }
    }

    /**
     * Returns whether handshake submission is allowed.
     *
     * @return true when allowed
     */
    private boolean availableForHandshake() {
        final TlsState current = currentState();
        return (current == TlsState.NEW || current == TlsState.HANDSHAKING || current == TlsState.OPEN)
                && conduit.opened();
    }

    /**
     * Returns whether plaintext IO submission is allowed.
     *
     * @return true when allowed
     */
    private boolean availableForIo() {
        final TlsState current = currentState();
        return (current == TlsState.NEW || current == TlsState.HANDSHAKING || current == TlsState.OPEN)
                && conduit.opened();
    }

    /**
     * Rejects an operation that lost a race with close or failure after submission.
     *
     * @param message failure message
     */
    private void requireOpenIo(final String message) {
        if (currentState() != TlsState.OPEN || !conduit.opened()) {
            throw closedFailure(message);
        }
    }

    /**
     * Creates a state failure retaining the first terminal cause.
     *
     * @param message failure message
     * @return state failure
     */
    private StatefulException closedFailure(final String message) {
        stateLock.lock();
        try {
            return failure == null ? new StatefulException(message) : new StatefulException(message, failure);
        } finally {
            stateLock.unlock();
        }
    }

    /**
     * Records the single successful handshake classification.
     */
    private void recordHandshake(final TlsEngine.SessionReuse reuse) {
        if (meter == null) {
            return;
        }
        meter.incrementCounter(switch (reuse) {
            case FULL -> Counter.TLS_FULL_HANDSHAKES;
            case RESUMED -> Counter.TLS_RESUMED_HANDSHAKES;
            case UNKNOWN -> Counter.TLS_UNKNOWN_HANDSHAKES;
        });
    }

    /**
     * Adds encrypted transport bytes to the borrowed meter.
     */
    private void addBytes(final Counter counter, final long bytes) {
        if (meter != null && bytes > 0L) {
            meter.addCounter(counter, bytes);
        }
    }

    /**
     * Notifies the listener of the open transition once.
     */
    private void notifyOpen() {
        if (listener != null && openNotified.compareAndSet(false, true)) {
            try {
                listener.open(this);
            } catch (final RuntimeException ignored) {
                // Listener failures do not invalidate a completed TLS handshake.
            }
        }
    }

    /**
     * Notifies the listener of a failure without replacing the primary cause.
     *
     * @param cause primary failure
     */
    private void notifyFailure(final RuntimeException cause) {
        if (listener == null) {
            return;
        }
        try {
            listener.failure(this, cause);
        } catch (final RuntimeException e) {
            cause.addSuppressed(e);
        }
    }

    /**
     * Validates an initial SSL session buffer size.
     *
     * @param size session buffer size
     * @return validated size
     */
    private static int checkedInitialSize(final int size) {
        if (size <= Normal._0 || size > Builder.TLS_MAX_STAGING_BUFFER_BYTES) {
            throw new ProtocolException("TLS session buffer size exceeds the 1 MiB staging limit: " + size);
        }
        return size;
    }

    /**
     * Initializes a newly allocated buffer as an empty read-mode buffer.
     *
     * @param buffer allocated buffer
     * @return empty read-mode buffer
     */
    private static ByteBuffer emptyReadBuffer(final ByteBuffer buffer) {
        buffer.clear();
        buffer.flip();
        return buffer;
    }

    /**
     * Transfers a core buffer into a NIO target.
     *
     * @param source core source
     * @param target NIO target
     * @param count  exact byte count
     */
    private static void transfer(final Buffer source, final ByteBuffer target, final long count) {
        final int before = target.position();
        try {
            source.read(target);
        } catch (final IOException e) {
            throw new SocketException("Unable to transfer TLS input", e);
        }
        if (target.position() - before != count) {
            throw new InternalException("TLS input transfer count mismatch");
        }
    }

    /**
     * Transfers a NIO source into a core target.
     *
     * @param source NIO source
     * @param target core target
     */
    private static void transfer(final ByteBuffer source, final Buffer target) {
        try {
            target.write(source);
        } catch (final IOException e) {
            throw new SocketException("Unable to transfer TLS output", e);
        }
    }

    /**
     * Consumes plaintext only after its complete TLS record was written.
     *
     * @param source plaintext source
     * @param count  consumed byte count
     */
    private static void consume(final Buffer source, final int count) {
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
     * Converts a non-negative byte count to a bounded NIO size.
     *
     * @param count byte count
     * @return NIO size
     */
    private static int toIntSize(final long count) {
        return (int) Math.min(count, Integer.MAX_VALUE);
    }

    /**
     * Waits for an adapter-view future and preserves checked IO semantics.
     *
     * @param future  operation future
     * @param message failure message
     * @return byte count
     * @throws IOException when the operation fails
     */
    private static long awaitView(final CompletableFuture<Long> future, final String message) throws IOException {
        try {
            final Long result = future.get();
            if (result == null) {
                throw new IOException(message + ": null result");
            }
            return result;
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(message, e);
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof IOException io) {
                throw io;
            }
            if (cause instanceof RuntimeException runtime) {
                throw runtime;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new IOException(message, cause);
        }
    }

    /**
     * Plaintext source backed by the enclosing TLS state machine.
     */
    private final class TlsSource implements Source {

        /**
         * Reads plaintext synchronously through the shared channel lifecycle.
         *
         * @param sink      plaintext target
         * @param byteCount maximum byte count
         * @return read byte count or EOF
         * @throws IOException when reading fails
         */
        @Override
        public long read(final Buffer sink, final long byteCount) throws IOException {
            return awaitView(TlsChannel.this.read(sink, byteCount), "TLS source read failed");
        }

        /**
         * Returns the no-op core timeout because Fabric timeouts are applied internally.
         *
         * @return no-op timeout
         */
        @Override
        public org.miaixz.bus.core.io.timout.Timeout timeout() {
            return org.miaixz.bus.core.io.timout.Timeout.NONE;
        }

        /**
         * Closes the shared TLS lifecycle.
         */
        @Override
        public void close() {
            TlsChannel.this.close();
        }

    }

    /**
     * Plaintext sink backed by the enclosing TLS state machine.
     */
    private final class TlsSink implements Sink {

        /**
         * Writes and completely consumes plaintext through the shared channel lifecycle.
         *
         * @param source    plaintext source
         * @param byteCount exact byte count
         * @throws IOException when writing fails
         */
        @Override
        public void write(final Buffer source, final long byteCount) throws IOException {
            final long before = source == null ? Normal._0 : source.size();
            final long written = awaitView(TlsChannel.this.write(source, byteCount), "TLS sink write failed");
            if (written != byteCount || before - source.size() != byteCount) {
                throw new IOException("TLS sink did not completely consume requested plaintext");
            }
        }

        /**
         * Flushes a sink whose records are already written eagerly.
         */
        @Override
        public void flush() {
            // TLS records are written eagerly.
        }

        /**
         * Returns the no-op core timeout because Fabric timeouts are applied internally.
         *
         * @return no-op timeout
         */
        @Override
        public org.miaixz.bus.core.io.timout.Timeout timeout() {
            return org.miaixz.bus.core.io.timout.Timeout.NONE;
        }

        /**
         * Closes the shared TLS lifecycle.
         */
        @Override
        public void close() {
            TlsChannel.this.close();
        }

    }

    /**
     * Internal TLS state sequence.
     */
    private enum TlsState {

        /**
         * New state.
         */
        NEW,

        /**
         * Handshake in progress.
         */
        HANDSHAKING,

        /**
         * Plaintext IO open.
         */
        OPEN,

        /**
         * Graceful close in progress.
         */
        CLOSING,

        /**
         * Fully closed.
         */
        CLOSED,

        /**
         * Terminal failure.
         */
        FAILED

    }

    /**
     * TLS-specific handshake and protocol failure.
     */
    private static final class TlsException extends ProtocolException {

        /**
         * Serialization identifier.
         */
        private static final long serialVersionUID = 2853944150347250250L;

        /**
         * Creates a TLS failure.
         *
         * @param message failure message
         */
        private TlsException(final String message) {
            super(message);
        }

        /**
         * Creates a TLS failure retaining its cause.
         *
         * @param message failure message
         * @param cause   failure cause
         */
        private TlsException(final String message, final Throwable cause) {
            super(message, cause);
        }

    }

}
