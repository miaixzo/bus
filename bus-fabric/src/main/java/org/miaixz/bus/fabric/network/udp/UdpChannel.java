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
package org.miaixz.bus.fabric.network.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.buffer.NioBuffer;
import org.miaixz.bus.core.io.buffer.NioBufferAllocator;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Lifecycle;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.network.Transport;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.runtime.Activity;
import org.miaixz.bus.fabric.runtime.dispatch.DispatchHandle;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;
import org.miaixz.bus.fabric.runtime.lifecycle.LifecycleScope;

/**
 * UDP datagram channel backed by a JDK datagram channel.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class UdpChannel implements Lifecycle, AutoCloseable {

    /**
     * Local address.
     */
    private final Address local;

    /**
     * Datagram channel.
     */
    private final DatagramChannel channel;

    /**
     * Runtime dispatcher for blocking datagram operations.
     */
    private final Dispatcher dispatcher;

    /**
     * Receive buffer allocator.
     */
    private final NioBufferAllocator buffers;

    /**
     * Lifecycle scope.
     */
    private final LifecycleScope scope;

    /**
     * Pending send count.
     */
    private final AtomicInteger pendingSends;

    /**
     * Creates a UDP channel.
     *
     * @param local      local address
     * @param channel    datagram channel
     * @param dispatcher runtime dispatcher
     */
    UdpChannel(final Address local, final DatagramChannel channel, final Dispatcher dispatcher) {
        this.local = Assert.notNull(local, () -> new ValidateException("UDP local address must not be null"));
        this.channel = Assert.notNull(channel, () -> new ValidateException("UDP datagram channel must not be null"));
        this.dispatcher = Assert.notNull(dispatcher, () -> new ValidateException("UDP dispatcher must not be null"));
        this.buffers = NioBufferAllocator.heap(Normal._65535 - Normal._28, Normal._4);
        this.scope = LifecycleScope.resource(this, "udp-channel", null, EventObserver.noop());
        this.pendingSends = new AtomicInteger();
        this.scope.open(this);
    }

    /**
     * Sends a datagram.
     *
     * @param source    source buffer
     * @param byteCount exact datagram byte count
     * @param remote    remote address
     * @return sent byte count future
     */
    public CompletableFuture<Integer> send(final Buffer source, final long byteCount, final SocketAddress remote) {
        final Buffer checkedSource = Assert
                .notNull(source, () -> new ValidateException("UDP source buffer must not be null"));
        final SocketAddress checkedRemote = Assert
                .notNull(remote, () -> new ValidateException("UDP remote address must not be null"));
        ensureOpened();
        if (byteCount < Normal.LONG_ZERO || byteCount > checkedSource.size()) {
            return CompletableFuture.failedFuture(new ValidateException("UDP byte count exceeds source bytes"));
        }
        if (byteCount > Normal._65535 - Normal._28) {
            return CompletableFuture.failedFuture(new ProtocolException("UDP datagram exceeds maximum payload"));
        }
        final int pending = pendingSends.incrementAndGet();
        if (pending > Normal._1024) {
            pendingSends.decrementAndGet();
            return CompletableFuture.failedFuture(new StatefulException("UDP send queue is full"));
        }
        final AtomicBoolean released = new AtomicBoolean();
        try {
            final CompletableFuture<Integer> future = background("udp:send", () -> {
                final Buffer snapshot = new Buffer();
                checkedSource.copyTo(snapshot, Normal.LONG_ZERO, byteCount);
                try {
                    ensureOpened();
                    try (NioBuffer lease = buffers.allocate()) {
                        final var nio = lease.buffer();
                        nio.limit((int) byteCount);
                        lease.readFrom(snapshot, (int) byteCount);
                        nio.flip();
                        final int written = channel.send(nio, checkedRemote);
                        if (written != byteCount || nio.hasRemaining()) {
                            throw new SocketException("UDP datagram was not sent atomically");
                        }
                        checkedSource.skip(byteCount);
                        return written;
                    }
                } catch (final IOException e) {
                    throw new SocketException("Unable to send UDP datagram", e);
                } finally {
                    releasePending(released);
                }
            });
            future.whenComplete((ignored, cause) -> releasePending(released));
            return future;
        } catch (final RuntimeException e) {
            releasePending(released);
            throw e;
        }
    }

    /**
     * Receives a datagram.
     *
     * @return datagram future
     */
    public CompletableFuture<Message> receive() {
        ensureOpened();
        return background("udp:receive", () -> {
            try (NioBuffer lease = buffers.allocate()) {
                final var nio = lease.buffer();
                final SocketAddress remote = channel.receive(nio);
                if (!(remote instanceof InetSocketAddress socket)) {
                    throw new SocketException("UDP datagram did not provide an internet remote address");
                }
                nio.flip();
                final Buffer payload = new Buffer();
                lease.writeTo(payload);
                final Address address = new Address(Transport.UDP.scheme(), socket.getHostString(), socket.getPort(),
                        null);
                return Message.of(
                        Protocol.UDP,
                        address,
                        Headers.empty(),
                        payload.size() == Normal.LONG_ZERO ? Payload.empty() : Payload.of(payload.readByteString()),
                        local);
            } catch (final IOException e) {
                throw new SocketException("Unable to receive UDP datagram", e);
            }
        });
    }

    /**
     * Returns the local address.
     *
     * @return local address
     */
    public Address local() {
        return local;
    }

    /**
     * Returns whether this channel is opened.
     *
     * @return true when opened
     */
    @Override
    public boolean opened() {
        return Lifecycle.super.opened() && channel.isOpen();
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
     * Returns pending asynchronous send count.
     *
     * @return pending sends
     */
    public int pendingSends() {
        return pendingSends.get();
    }

    /**
     * Closes this channel.
     */
    @Override
    public void close() {
        if (scope.state().terminal()) {
            return;
        }
        RuntimeException failure = null;
        try {
            channel.close();
        } catch (final IOException e) {
            failure = new SocketException("Unable to close UDP channel", e);
        } finally {
            buffers.close();
            scope.close(this);
        }
        if (failure != null) {
            throw failure;
        }
    }

    /**
     * Ensures this channel is open.
     */
    private void ensureOpened() {
        if (!opened()) {
            throw new StatefulException("UDP channel is closed");
        }
    }

    /**
     * Releases one pending-send slot exactly once after completion, failure, or cancellation.
     *
     * @param released per-send guard
     */
    private void releasePending(final AtomicBoolean released) {
        if (released.compareAndSet(false, true)) {
            pendingSends.decrementAndGet();
        }
    }

    /**
     * Runs one blocking datagram operation on the dispatcher's background executor.
     *
     * @param key      dispatch key
     * @param supplier operation supplier
     * @param <T>      result type
     * @return operation future
     */
    private <T> CompletableFuture<T> background(final String key, final Supplier<T> supplier) {
        final CompletableFuture<T> result = new CompletableFuture<>();
        final DispatchHandle handle = dispatcher.background(key, this, Activity.of(key, () -> {
            try {
                result.complete(supplier.get());
            } catch (final RuntimeException | Error e) {
                result.completeExceptionally(e);
                throw e;
            }
        }));
        handle.future().whenComplete((ignored, cause) -> {
            if (cause != null && !result.isDone()) {
                result.completeExceptionally(cause);
            }
        });
        return result;
    }

}
