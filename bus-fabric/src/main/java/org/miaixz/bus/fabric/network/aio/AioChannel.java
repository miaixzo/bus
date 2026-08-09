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
package org.miaixz.bus.fabric.network.aio;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.center.function.BiConsumerX;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.buffer.NioBuffer;
import org.miaixz.bus.core.io.buffer.NioBufferAllocator;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Timeout;
import org.miaixz.bus.fabric.network.Conduit;
import org.miaixz.bus.fabric.protocol.socket.SocketOptions;
import org.miaixz.bus.fabric.runtime.Activity;
import org.miaixz.bus.fabric.runtime.dispatch.DispatchHandle;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;
import org.miaixz.bus.fabric.runtime.lifecycle.LifecycleScope;

/**
 * AIO socket channel with core.io buffer operations.
 *
 * @author Kimi Liu
 */
public final class AioChannel implements Conduit {

    /**
     * JDK socket channel.
     */
    private final AsynchronousSocketChannel channel;

    /**
     * Close flag.
     */
    private final AtomicBoolean closed;

    /**
     * Borrowed runtime dispatcher used only for timeout scheduling.
     */
    private final Dispatcher dispatcher;

    /**
     * Socket tuning options.
     */
    private final SocketOptions options;

    /**
     * Reusable read buffer allocator.
     */
    private final NioBufferAllocator buffers;

    /**
     * Socket-server callback lane read storage. Future-based callers continue to use {@link #buffers}.
     */
    private final ByteBuffer callbackReadBuffer;

    /**
     * Ensures only one callback or future read owns the native channel at a time.
     */
    private final AtomicBoolean callbackReadActive;

    /**
     * Caller-owned target of the active callback read.
     */
    private Buffer callbackReadTarget;

    /**
     * Terminal callback of the active callback read.
     */
    private BiConsumerX<? super Long, ? super Throwable> callbackReadCompletion;

    /**
     * Reusable native completion handler for callback reads.
     */
    private final CompletionHandler<Integer, Void> callbackReadHandler;

    /**
     * Operations that must be failed when this channel closes.
     */
    private final Set<Operation<?>> pending;

    /**
     * FIFO requests retained until their caller-owned bytes are fully written.
     */
    private final ArrayDeque<WriteRequest> writes;

    /**
     * Whether a native asynchronous write currently owns the queue head.
     */
    private boolean writeActive;

    /**
     * Reentrancy guard that turns synchronous completion callbacks into an iterative drain.
     */
    private boolean writeDraining;

    /**
     * Reusable native completion handler for callback writes.
     */
    private final CompletionHandler<Integer, WriteRequest> callbackWriteHandler;

    /**
     * Local socket address.
     */
    private volatile SocketAddress local;

    /**
     * Remote socket address.
     */
    private volatile SocketAddress remote;

    /**
     * Creates an AIO channel.
     *
     * @param channel    JDK channel
     * @param dispatcher runtime dispatcher
     */
    AioChannel(final AsynchronousSocketChannel channel, final Dispatcher dispatcher) {
        this(channel, dispatcher, SocketOptions.defaults());
    }

    /**
     * Creates an AIO channel.
     *
     * @param channel    JDK channel
     * @param dispatcher runtime dispatcher
     * @param options    socket options
     */
    AioChannel(final AsynchronousSocketChannel channel, final Dispatcher dispatcher, final SocketOptions options) {
        this(channel, dispatcher, options, false);
    }

    /**
     * Creates an AIO channel with an optional allocation-light callback lane.
     *
     * @param channel      JDK channel
     * @param dispatcher   runtime dispatcher
     * @param options      socket options
     * @param callbackLane whether callback read and write state is enabled
     */
    private AioChannel(final AsynchronousSocketChannel channel, final Dispatcher dispatcher,
            final SocketOptions options, final boolean callbackLane) {
        this.channel = Assert.notNull(channel, () -> new ValidateException("AIO channel must not be null"));
        this.dispatcher = Assert.notNull(dispatcher, () -> new ValidateException("AIO dispatcher must not be null"));
        this.options = options == null ? SocketOptions.defaults() : options;
        this.buffers = NioBufferAllocator.heap(this.options.readBufferSize(), Normal._4);
        this.callbackReadBuffer = callbackLane ? ByteBuffer.allocateDirect(this.options.readBufferSize()) : null;
        this.callbackReadActive = callbackLane ? new AtomicBoolean() : null;
        this.callbackReadHandler = callbackLane ? new CompletionHandler<>() {

            /**
             * Publishes a successful native callback read.
             *
             * @param count   completed byte count
             * @param ignored unused attachment
             */
            @Override
            public void completed(final Integer count, final Void ignored) {
                completeCallbackRead(count, null);
            }

            /**
             * Publishes a failed native callback read.
             *
             * @param cause   native failure
             * @param ignored unused attachment
             */
            @Override
            public void failed(final Throwable cause, final Void ignored) {
                completeCallbackRead(null, socketFailure("AIO read failed", cause));
            }

        } : null;
        this.pending = ConcurrentHashMap.newKeySet();
        this.writes = new ArrayDeque<>();
        this.callbackWriteHandler = callbackLane ? new CompletionHandler<>() {

            /**
             * Continues a successful callback write.
             *
             * @param count   completed byte count
             * @param request active write request
             */
            @Override
            public void completed(final Integer count, final WriteRequest request) {
                completeCallbackWrite(request, count, null);
            }

            /**
             * Terminates a failed callback write.
             *
             * @param cause   native failure
             * @param request active write request
             */
            @Override
            public void failed(final Throwable cause, final WriteRequest request) {
                completeCallbackWrite(request, null, socketFailure("AIO write failed", cause));
            }

        } : null;
        this.closed = new AtomicBoolean();
        applySocketOptions();
    }

    /**
     * Creates an AIO channel registered with its owning group scope.
     *
     * @param channel    JDK channel
     * @param dispatcher borrowed runtime dispatcher
     * @param scope      owning group scope
     * @param options    socket options
     */
    AioChannel(final AsynchronousSocketChannel channel, final Dispatcher dispatcher, final LifecycleScope scope,
            final SocketOptions options) {
        this(channel, dispatcher, options);
        Assert.notNull(scope, () -> new ValidateException("AIO lifecycle scope must not be null")).own(this);
    }

    /**
     * Wraps an already accepted native channel and captures its addresses.
     *
     * @param channel    accepted JDK channel
     * @param dispatcher borrowed runtime dispatcher
     * @param scope      lifecycle owning the accepted channel
     * @param options    accepted socket options
     * @return callback-enabled accepted channel
     */
    static AioChannel accepted(
            final AsynchronousSocketChannel channel,
            final Dispatcher dispatcher,
            final LifecycleScope scope,
            final SocketOptions options) {
        final AioChannel accepted = new AioChannel(channel, dispatcher, options, true);
        Assert.notNull(scope, () -> new ValidateException("AIO lifecycle scope must not be null")).own(accepted);
        try {
            accepted.local = channel.getLocalAddress();
            accepted.remote = channel.getRemoteAddress();
            return accepted;
        } catch (final IOException e) {
            accepted.closeAfterFailure();
            throw new SocketException("Unable to read accepted AIO channel addresses", e);
        }
    }

    /**
     * Connects this channel.
     *
     * @param address socket address
     * @param timeout timeout policy
     * @return connection future
     */
    public CompletableFuture<Void> connect(final SocketAddress address, final Timeout timeout) {
        final SocketAddress checkedAddress = Assert
                .notNull(address, () -> new ValidateException("Socket address must not be null"));
        final Timeout checkedTimeout = Assert.notNull(timeout, () -> new ValidateException("Timeout must not be null"));
        final Operation<Void> operation = new Operation<>("AIO connect failed", null);
        if (!operation.active()) {
            return operation.future();
        }
        try {
            scheduleConnectTimeout(operation, checkedTimeout);
            channel.connect(checkedAddress, operation, new CompletionHandler<>() {

                /**
                 * Completes the connection and captures both socket addresses.
                 *
                 * @param ignored   unused result
                 * @param completed completed operation
                 */
                @Override
                public void completed(final Void ignored, final Operation<Void> completed) {
                    if (!completed.active()) {
                        return;
                    }
                    try {
                        local = channel.getLocalAddress();
                        remote = channel.getRemoteAddress();
                        completed.complete(null);
                    } catch (final IOException e) {
                        completed.fail(new SocketException("Unable to read AIO channel addresses", e));
                        closeAfterFailure();
                    }
                }

                /**
                 * Fails the connection attempt.
                 *
                 * @param cause     connection failure
                 * @param completed completed operation
                 */
                @Override
                public void failed(final Throwable cause, final Operation<Void> completed) {
                    completed.fail(socketFailure("AIO connect failed", cause));
                    closeAfterFailure();
                }

            });
        } catch (final RuntimeException e) {
            operation.fail(socketFailure("AIO connect failed", e));
            closeAfterFailure();
        }
        return operation.future();
    }

    /**
     * Reads bytes into a core.io buffer.
     *
     * @param target    target buffer
     * @param byteCount maximum byte count
     * @return read future
     */
    public CompletableFuture<Long> read(final Buffer target, final long byteCount) {
        final Buffer checkedTarget = Assert
                .notNull(target, () -> new ValidateException("Read target must not be null"));
        Assert.isTrue(byteCount >= Normal._0, () -> new ValidateException("Read byte count must not be negative"));
        if (byteCount == Normal._0) {
            return CompletableFuture.completedFuture(0L);
        }
        final NioBuffer lease;
        try {
            lease = buffers.allocate(readCapacity(byteCount));
        } catch (final RuntimeException e) {
            return CompletableFuture.failedFuture(new SocketException("AIO read failed", e));
        }
        final Operation<Long> operation = new Operation<>("AIO read failed", lease);
        if (!operation.active()) {
            return operation.future();
        }
        try {
            lease.buffer().limit(readCapacity(byteCount));
            channel.read(lease.buffer(), operation, new CompletionHandler<>() {

                /**
                 * Copies completed bytes into the core buffer and releases the lease.
                 *
                 * @param count     native read count
                 * @param completed completed operation
                 */
                @Override
                public void completed(final Integer count, final Operation<Long> completed) {
                    if (!completed.active()) {
                        return;
                    }
                    try {
                        if (count > Normal._0) {
                            lease.flip();
                            final int copied = lease.writeTo(checkedTarget, count);
                            if (copied != count) {
                                throw new SocketException("AIO read did not append the completed byte count");
                            }
                        }
                        completed.complete(count.longValue());
                    } catch (final RuntimeException e) {
                        completed.fail(socketFailure("AIO read failed", e));
                    }
                }

                /**
                 * Fails the read operation.
                 *
                 * @param cause     read failure
                 * @param completed completed operation
                 */
                @Override
                public void failed(final Throwable cause, final Operation<Long> completed) {
                    completed.fail(socketFailure("AIO read failed", cause));
                }

            });
        } catch (final RuntimeException e) {
            operation.fail(socketFailure("AIO read failed", e));
        }
        return operation.future();
    }

    /**
     * Completion-driven read used by transports that already serialize their data plane.
     *
     * @param target     destination receiving completed bytes
     * @param byteCount  maximum byte count
     * @param completion terminal completion callback
     */
    @Override
    public void read(
            final Buffer target,
            final long byteCount,
            final BiConsumerX<? super Long, ? super Throwable> completion) {
        if (callbackReadBuffer == null) {
            Conduit.super.read(target, byteCount, completion);
            return;
        }
        final BiConsumerX<? super Long, ? super Throwable> callback = Assert
                .notNull(completion, () -> new ValidateException("AIO read completion must not be null"));
        final Buffer checkedTarget;
        try {
            checkedTarget = Assert.notNull(target, () -> new ValidateException("Read target must not be null"));
            Assert.isTrue(byteCount >= Normal._0, () -> new ValidateException("Read byte count must not be negative"));
            if (byteCount == Normal._0) {
                callback.accept(0L, null);
                return;
            }
            if (!opened()) {
                callback.accept(null, new SocketException("AIO read failed: channel is closed"));
                return;
            }
        } catch (final RuntimeException e) {
            callback.accept(null, e);
            return;
        }
        if (!callbackReadActive.compareAndSet(false, true)) {
            callback.accept(null, new SocketException("AIO read already in progress"));
            return;
        }
        try {
            callbackReadBuffer.clear();
            callbackReadBuffer.limit(readCapacity(byteCount));
            callbackReadTarget = checkedTarget;
            callbackReadCompletion = callback;
            channel.read(callbackReadBuffer, null, callbackReadHandler);
        } catch (final RuntimeException e) {
            callbackReadTarget = null;
            callbackReadCompletion = null;
            callbackReadActive.set(false);
            callback.accept(null, socketFailure("AIO read failed", e));
        }
    }

    /**
     * Transfers one native callback read into the caller buffer and publishes its terminal outcome.
     *
     * @param count completed byte count, or {@code null} on failure
     * @param cause terminal failure, or {@code null} on success
     */
    private void completeCallbackRead(final Integer count, final Throwable cause) {
        final Buffer target = callbackReadTarget;
        final BiConsumerX<? super Long, ? super Throwable> callback = callbackReadCompletion;
        Throwable failure = cause;
        if (failure == null && count == null) {
            failure = new SocketException("AIO read completed without a byte count");
        }
        if (failure == null && count != null && count > Normal._0) {
            try {
                callbackReadBuffer.flip();
                target.write(callbackReadBuffer);
            } catch (final IOException | RuntimeException e) {
                failure = e;
            }
        }
        callbackReadTarget = null;
        callbackReadCompletion = null;
        callbackReadActive.set(false);
        callback.accept(failure == null ? count.longValue() : null, failure);
    }

    /**
     * Writes bytes from a core.io buffer.
     *
     * @param source    source buffer
     * @param byteCount byte count to write
     * @return write future
     */
    public CompletableFuture<Long> write(final Buffer source, final long byteCount) {
        final Buffer checkedSource = Assert
                .notNull(source, () -> new ValidateException("Write source must not be null"));
        Assert.isTrue(byteCount >= Normal._0, () -> new ValidateException("Write byte count must not be negative"));
        Assert.isTrue(
                byteCount <= checkedSource.size(),
                () -> new ValidateException("Write byte count must not exceed source size"));
        if (byteCount == Normal._0) {
            return CompletableFuture.completedFuture(0L);
        }
        final WriteRequest request = new WriteRequest(checkedSource, byteCount);
        synchronized (writes) {
            if (writes.size() >= 1024) {
                return CompletableFuture.failedFuture(new SocketException("AIO write queue is full"));
            }
            writes.addLast(request);
        }
        drainWrites();
        return request.future;
    }

    /**
     * Completion-driven write that avoids a future and completion-stage allocation.
     *
     * @param source     source whose bytes are consumed
     * @param byteCount  requested byte count
     * @param completion terminal completion callback
     */
    @Override
    public void write(
            final Buffer source,
            final long byteCount,
            final BiConsumerX<? super Long, ? super Throwable> completion) {
        if (callbackWriteHandler == null) {
            Conduit.super.write(source, byteCount, completion);
            return;
        }
        final BiConsumerX<? super Long, ? super Throwable> callback = Assert
                .notNull(completion, () -> new ValidateException("AIO write completion must not be null"));
        final Buffer checkedSource;
        try {
            checkedSource = Assert.notNull(source, () -> new ValidateException("Write source must not be null"));
            Assert.isTrue(byteCount >= Normal._0, () -> new ValidateException("Write byte count must not be negative"));
            Assert.isTrue(
                    byteCount <= checkedSource.size(),
                    () -> new ValidateException("Write byte count must not exceed source size"));
            if (byteCount == Normal._0) {
                callback.accept(0L, null);
                return;
            }
            if (!opened()) {
                callback.accept(null, new SocketException("AIO write failed: channel is closed"));
                return;
            }
        } catch (final RuntimeException e) {
            callback.accept(null, e);
            return;
        }
        final CallbackWriteRequest request = new CallbackWriteRequest(checkedSource, byteCount, callback);
        synchronized (writes) {
            if (writes.size() >= 1024) {
                callback.accept(null, new SocketException("AIO write queue is full"));
                return;
            }
            writes.addLast(request);
        }
        drainWrites();
    }

    /**
     * Returns the local socket address.
     *
     * @return local address
     */
    public SocketAddress local() {
        return local;
    }

    /**
     * Returns the remote socket address.
     *
     * @return remote address
     */
    public SocketAddress remote() {
        return remote;
    }

    /**
     * Closes this channel.
     */
    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            RuntimeException failure = null;
            try {
                channel.close();
            } catch (final IOException e) {
                failure = new SocketException("Unable to close AIO channel", e);
            } finally {
                synchronized (writes) {
                    final SocketException closedFailure = new SocketException("AIO channel is closed");
                    for (final WriteRequest request : writes) {
                        if (request instanceof CallbackWriteRequest callback) {
                            if (callback.terminate()) {
                                callback.complete(null, closedFailure);
                            }
                        } else {
                            request.future.completeExceptionally(closedFailure);
                        }
                    }
                    writes.clear();
                }
                final SocketException closedFailure = new SocketException("AIO channel is closed");
                for (final Operation<?> operation : Set.copyOf(pending)) {
                    operation.fail(closedFailure);
                }
                buffers.close();
            }
            if (failure != null) {
                throw failure;
            }
        }
    }

    /**
     * Returns whether the channel is open.
     *
     * @return true when open
     */
    public boolean opened() {
        return !closed.get() && channel.isOpen();
    }

    /**
     * Returns socket options.
     *
     * @return socket options
     */
    SocketOptions options() {
        return options;
    }

    /**
     * Applies configured JDK socket options.
     */
    private void applySocketOptions() {
        for (final Map.Entry<SocketOption<?>, Object> entry : options.socketOptions().entrySet()) {
            try {
                channel.setOption((SocketOption) entry.getKey(), entry.getValue());
            } catch (final IOException e) {
                throw new SocketException("Unable to apply AIO socket option", e);
            }
        }
    }

    /**
     * Schedules the connect deadline without blocking a dispatcher worker.
     *
     * @param operation connect operation
     * @param timeout   connect timeout
     */
    private void scheduleConnectTimeout(final Operation<Void> operation, final Timeout timeout) {
        final Duration connectTimeout = timeout.connect();
        if (connectTimeout.isZero()) {
            return;
        }
        final DispatchHandle deadline = dispatcher
                .schedule("aio:connect:timeout", connectTimeout, Activity.of("aio:connect:timeout", () -> {
                    if (operation.fail(new TimeoutException("AIO connect timed out"))) {
                        closeAfterFailure();
                    }
                }));
        operation.deadline(deadline);
    }

    /**
     * Starts or continues a complete asynchronous write.
     *
     * @param source       source buffer
     * @param byteCount    requested byte count
     * @param written      bytes already written
     * @param zeroProgress consecutive zero-progress completions
     * @param operation    write operation
     */
    private void writeChunk(
            final Buffer source,
            final long byteCount,
            final long written,
            final int zeroProgress,
            final Operation<Long> operation) {
        if (!operation.active()) {
            return;
        }
        if (written == byteCount) {
            operation.complete(byteCount);
            return;
        }
        final int chunk = toIntSize(Math.min(byteCount - written, options.writeChunkSize()));
        final ByteBuffer view;
        try {
            view = source.nioBuffer(chunk);
        } catch (final RuntimeException e) {
            operation.fail(socketFailure("AIO write failed", e));
            return;
        }
        try {
            channel.write(view, operation, new CompletionHandler<>() {

                /**
                 * Consumes completed bytes and submits the next chunk.
                 *
                 * @param count     native write count
                 * @param completed completed operation
                 */
                @Override
                public void completed(final Integer count, final Operation<Long> completed) {
                    if (!completed.active()) {
                        return;
                    }
                    if (count < Normal._0 || count > chunk) {
                        completed.fail(new SocketException("AIO write returned an invalid byte count"));
                        return;
                    }
                    if (count == Normal._0) {
                        final int stalled = zeroProgress + Normal._1;
                        if (stalled >= Normal._16) {
                            completed.fail(new SocketException("AIO write made no progress after 16 attempts"));
                        } else {
                            writeChunk(source, byteCount, written, stalled, completed);
                        }
                        return;
                    }
                    try {
                        source.skip(count);
                    } catch (final IOException e) {
                        completed.fail(new SocketException("AIO write failed", e));
                        return;
                    }
                    writeChunk(source, byteCount, written + count, Normal._0, completed);
                }

                /**
                 * Fails the write operation.
                 *
                 * @param cause     write failure
                 * @param completed completed operation
                 */
                @Override
                public void failed(final Throwable cause, final Operation<Long> completed) {
                    completed.fail(socketFailure("AIO write failed", cause));
                }

            });
        } catch (final RuntimeException e) {
            operation.fail(socketFailure("AIO write failed", e));
        }
    }

    /**
     * Starts queued writes one at a time and absorbs synchronous callback recursion.
     */
    private void drainWrites() {
        synchronized (writes) {
            if (writeDraining) {
                return;
            }
            writeDraining = true;
        }
        while (true) {
            final WriteRequest request;
            synchronized (writes) {
                if (writeActive || (request = writes.peekFirst()) == null) {
                    writeDraining = false;
                    return;
                }
                writeActive = true;
            }
            if (!(request instanceof CallbackWriteRequest callback)) {
                final Operation<Long> operation = new Operation<>("AIO write failed", null);
                operation.future().whenComplete((value, cause) -> finishWrite(request, value, cause));
                writeChunk(request.source, request.byteCount, Normal._0, Normal._0, operation);
            } else {
                writeCallbackChunk(callback, Normal._0, Normal._0);
            }
            synchronized (writes) {
                if (writeActive) {
                    writeDraining = false;
                    return;
                }
            }
        }
    }

    /**
     * Completes one queued request and schedules the next drain.
     *
     * @param request completed queued write request
     * @param value   completed byte count, or {@code null} on failure
     * @param cause   terminal failure, or {@code null} on success
     */
    private void finishWrite(final WriteRequest request, final Long value, final Throwable cause) {
        if (request instanceof CallbackWriteRequest callback && !callback.terminate()) {
            return;
        }
        synchronized (writes) {
            writes.removeFirstOccurrence(request);
            writeActive = false;
        }
        if (request instanceof CallbackWriteRequest callback) {
            callback.complete(value, cause);
        } else if (cause == null) {
            request.future.complete(value);
        } else {
            request.future.completeExceptionally(cause);
        }
        drainWrites();
    }

    /**
     * Starts or continues one callback write chunk.
     *
     * @param request      active callback request
     * @param written      bytes already accepted
     * @param zeroProgress consecutive zero-progress completions
     */
    private void writeCallbackChunk(final CallbackWriteRequest request, final long written, final int zeroProgress) {
        if (request.terminal()) {
            return;
        }
        if (closed.get()) {
            finishWrite(request, null, new SocketException("AIO channel is closed"));
            return;
        }
        if (written == request.byteCount) {
            finishWrite(request, request.byteCount, null);
            return;
        }
        final int chunk = toIntSize(Math.min(request.byteCount - written, options.writeChunkSize()));
        final ByteBuffer view;
        try {
            view = request.source.nioBuffer(chunk);
        } catch (final RuntimeException e) {
            finishWrite(request, null, socketFailure("AIO write failed", e));
            return;
        }
        request.written = written;
        request.zeroProgress = zeroProgress;
        request.chunk = chunk;
        try {
            channel.write(view, request, callbackWriteHandler);
        } catch (final RuntimeException e) {
            finishWrite(request, null, socketFailure("AIO write failed", e));
        }
    }

    /**
     * Processes one native callback write completion.
     *
     * @param queued active queued request
     * @param count  completed byte count, or {@code null} on failure
     * @param cause  terminal failure, or {@code null} on success
     */
    private void completeCallbackWrite(final WriteRequest queued, final Integer count, final Throwable cause) {
        final CallbackWriteRequest request = (CallbackWriteRequest) queued;
        if (request.terminal()) {
            return;
        }
        if (cause != null) {
            finishWrite(request, null, cause);
            return;
        }
        if (count == null || count < Normal._0 || count > request.chunk) {
            finishWrite(request, null, new SocketException("AIO write returned an invalid byte count"));
            return;
        }
        if (count == Normal._0) {
            final int stalled = request.zeroProgress + Normal._1;
            if (stalled >= Normal._16) {
                finishWrite(request, null, new SocketException("AIO write made no progress after 16 attempts"));
            } else {
                writeCallbackChunk(request, request.written, stalled);
            }
            return;
        }
        try {
            request.source.skip(count);
        } catch (final IOException e) {
            finishWrite(request, null, new SocketException("AIO write failed", e));
            return;
        }
        writeCallbackChunk(request, request.written + count, Normal._0);
    }

    /**
     * Closes this channel after an operation failure without masking the original failure.
     */
    private void closeAfterFailure() {
        try {
            close();
        } catch (final RuntimeException ignored) {
            // The operation already carries the authoritative failure.
        }
    }

    /**
     * Maps native asynchronous failures to the Fabric socket exception contract.
     *
     * @param message failure message
     * @param cause   native failure
     * @return mapped failure
     */
    private static RuntimeException socketFailure(final String message, final Throwable cause) {
        return cause instanceof RuntimeException runtime ? runtime : new SocketException(message, cause);
    }

    /**
     * Returns a bounded read capacity.
     *
     * @param byteCount requested byte count
     * @return read capacity
     */
    private int readCapacity(final long byteCount) {
        return toIntSize(Math.min(byteCount, options.readBufferSize()));
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
     * Atomic state shared by one native asynchronous operation and its terminal paths.
     *
     * @param <T> operation result type
     */
    private final class Operation<T> {

        /**
         * Result future exposed to the caller.
         */
        private final CompletableFuture<T> future;

        /**
         * Terminal guard for completion, failure, cancellation, timeout, and close.
         */
        private final AtomicBoolean terminal;

        /**
         * Scheduled timeout handle, when present.
         */
        private final AtomicReference<DispatchHandle> deadline;

        /**
         * Buffer lease released on every terminal path, when present.
         */
        private final AutoCloseable lease;

        /**
         * Failure message used when registration observes a closed channel.
         */
        private final String message;

        /**
         * Creates and registers an operation.
         *
         * @param message failure message
         * @param lease   optional operation lease
         */
        private Operation(final String message, final AutoCloseable lease) {
            this.future = new CompletableFuture<>();
            this.terminal = new AtomicBoolean();
            this.deadline = new AtomicReference<>();
            this.lease = lease;
            this.message = message;
            pending.add(this);
            this.future.whenComplete((value, cause) -> {
                if (this.future.isCancelled() && terminate()) {
                    closeAfterFailure();
                }
            });
            if (closed.get()) {
                fail(new SocketException(message + ": channel is closed"));
            }
        }

        /**
         * Returns the caller-visible future.
         *
         * @return operation future
         */
        private CompletableFuture<T> future() {
            return future;
        }

        /**
         * Returns whether the operation may still touch native or leased state.
         *
         * @return true while active
         */
        private boolean active() {
            return !terminal.get();
        }

        /**
         * Stores a timeout handle or cancels it when the operation already terminated.
         *
         * @param handle timeout handle
         */
        private void deadline(final DispatchHandle handle) {
            if (!deadline.compareAndSet(null, handle)) {
                dispatcher.cancel(handle);
                return;
            }
            if (terminal.get() && deadline.compareAndSet(handle, null)) {
                dispatcher.cancel(handle);
            }
        }

        /**
         * Completes this operation once.
         *
         * @param value result value
         * @return true when this call completed the operation
         */
        private boolean complete(final T value) {
            if (!terminate()) {
                return false;
            }
            future.complete(value);
            return true;
        }

        /**
         * Fails this operation once.
         *
         * @param cause failure cause
         * @return true when this call failed the operation
         */
        private boolean fail(final Throwable cause) {
            if (!terminate()) {
                return false;
            }
            future.completeExceptionally(cause);
            return true;
        }

        /**
         * Performs shared terminal cleanup exactly once.
         *
         * @return true when this call won the terminal race
         */
        private boolean terminate() {
            if (!terminal.compareAndSet(false, true)) {
                return false;
            }
            pending.remove(this);
            final DispatchHandle handle = deadline.getAndSet(null);
            if (handle != null) {
                dispatcher.cancel(handle);
            }
            if (lease != null) {
                try {
                    lease.close();
                } catch (final Exception e) {
                    if (!future.isDone()) {
                        future.completeExceptionally(new SocketException(message, e));
                    }
                }
            }
            return true;
        }

    }

    /**
     * Caller-owned write retained until its requested bytes are fully drained.
     * <p>
     * Access is serialized by the enclosing channel write monitor, while the future may be observed by any caller
     * thread.
     * </p>
     */
    private static class WriteRequest {

        /**
         * Source whose position advances only as bytes are accepted by the channel.
         */
        final Buffer source;

        /**
         * Total number of bytes promised by this request.
         */
        final long byteCount;

        /**
         * Completion carrying the written byte count or terminal failure.
         */
        private final CompletableFuture<Long> future;

        /**
         * Creates a queued write request.
         *
         * @param source    caller-owned source
         * @param byteCount bytes to write
         */
        private WriteRequest(final Buffer source, final long byteCount) {
            this(source, byteCount, true);
        }

        /**
         * Creates a queued request and optionally allocates its compatibility future.
         *
         * @param source    caller-owned source
         * @param byteCount bytes to write
         * @param future    whether to allocate a caller-visible future
         */
        private WriteRequest(final Buffer source, final long byteCount, final boolean future) {
            this.source = source;
            this.byteCount = byteCount;
            this.future = future ? new CompletableFuture<>() : null;
        }
    }

    /**
     * Allocation-light write request completed through a reusable callback.
     */
    private static final class CallbackWriteRequest extends WriteRequest {

        /**
         * Atomic terminal-state handle.
         */
        private static final VarHandle TERMINAL;

        /**
         * Caller completion callback.
         */
        private final BiConsumerX<? super Long, ? super Throwable> completion;

        /**
         * Zero while active and one after terminal ownership is claimed.
         */
        private volatile int terminal;

        /**
         * Bytes accepted before the active native write.
         */
        private long written;

        /**
         * Consecutive zero-progress native completions.
         */
        private int zeroProgress;

        /**
         * Byte count submitted by the active native write.
         */
        private int chunk;

        /**
         * Creates a callback write request.
         *
         * @param source     caller-owned source
         * @param byteCount  bytes to write
         * @param completion terminal completion callback
         */
        private CallbackWriteRequest(final Buffer source, final long byteCount,
                final BiConsumerX<? super Long, ? super Throwable> completion) {
            super(source, byteCount, false);
            this.completion = completion;
        }

        /**
         * Publishes the terminal outcome.
         *
         * @param value completed byte count
         * @param cause terminal failure
         */
        private void complete(final Long value, final Throwable cause) {
            completion.accept(cause == null ? value : null, cause);
        }

        /**
         * Returns whether terminal ownership was already claimed.
         *
         * @return true after terminal ownership is claimed
         */
        private boolean terminal() {
            return terminal != Normal._0;
        }

        /**
         * Claims terminal ownership exactly once.
         *
         * @return true when this invocation claimed ownership
         */
        private boolean terminate() {
            return TERMINAL.compareAndSet(this, Normal._0, Normal._1);
        }

        static {
            try {
                TERMINAL = MethodHandles.lookup().findVarHandle(CallbackWriteRequest.class, "terminal", int.class);
            } catch (final ReflectiveOperationException e) {
                throw new ExceptionInInitializerError(e);
            }
        }
    }

}
