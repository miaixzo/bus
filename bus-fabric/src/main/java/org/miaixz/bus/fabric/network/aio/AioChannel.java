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
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.buffer.NioBuffer;
import org.miaixz.bus.core.io.buffer.NioBufferAllocator;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Timeout;
import org.miaixz.bus.fabric.protocol.socket.SocketOptions;
import org.miaixz.bus.fabric.runtime.Activity;
import org.miaixz.bus.fabric.runtime.dispatch.DispatchHandle;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;
import org.miaixz.bus.fabric.runtime.lifecycle.LifecycleScope;

/**
 * AIO socket channel with core.io buffer operations.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class AioChannel implements AutoCloseable {

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
        this.channel = Assert.notNull(channel, () -> new ValidateException("AIO channel must not be null"));
        this.dispatcher = Assert.notNull(dispatcher, () -> new ValidateException("AIO dispatcher must not be null"));
        this.options = options == null ? SocketOptions.defaults() : options;
        this.buffers = NioBufferAllocator.heap(this.options.readBufferSize(), Normal._4);
        this.pending = ConcurrentHashMap.newKeySet();
        this.writes = new ArrayDeque<>();
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
            scheduleConnectTimeout(operation, checkedTimeout.connect());
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
                        request.future.completeExceptionally(closedFailure);
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
    boolean opened() {
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
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void applySocketOptions() {
        for (final java.util.Map.Entry<SocketOption<?>, Object> entry : options.socketOptions().entrySet()) {
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
    private void scheduleConnectTimeout(final Operation<Void> operation, final Duration timeout) {
        if (timeout.isZero()) {
            return;
        }
        final DispatchHandle deadline = dispatcher
                .schedule("aio:connect:timeout", timeout, Activity.of("aio:connect:timeout", () -> {
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
            final Operation<Long> operation = new Operation<>("AIO write failed", null);
            operation.future().whenComplete((value, cause) -> finishWrite(request, value, cause));
            writeChunk(request.source, request.byteCount, Normal._0, Normal._0, operation);
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
     */
    private void finishWrite(final WriteRequest request, final Long value, final Throwable cause) {
        synchronized (writes) {
            writes.removeFirstOccurrence(request);
            writeActive = false;
        }
        if (cause == null) {
            request.future.complete(value);
        } else {
            request.future.completeExceptionally(cause);
        }
        drainWrites();
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
    private static final class WriteRequest {

        /**
         * Source whose position advances only as bytes are accepted by the channel.
         */
        private final Buffer source;

        /**
         * Total number of bytes promised by this request.
         */
        private final long byteCount;

        /**
         * Completion carrying the written byte count or terminal failure.
         */
        private final CompletableFuture<Long> future = new CompletableFuture<>();

        /**
         * Creates a queued write request.
         *
         * @param source    caller-owned source
         * @param byteCount bytes to write
         */
        private WriteRequest(final Buffer source, final long byteCount) {
            this.source = source;
            this.byteCount = byteCount;
        }
    }

}
