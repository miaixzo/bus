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
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;

/**
 * UDP datagram channel backed by a JDK datagram channel.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class UdpChannel implements AutoCloseable {

    /**
     * Receive buffer size.
     */
    static final int MAX_DATAGRAM = 65_507;

    /**
     * Maximum pending sends.
     */
    private static final int MAX_PENDING_SENDS = 1024;

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
     * Lifecycle state.
     */
    private final AtomicReference<Status> state;

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
        if (local == null) {
            throw new ValidateException("UDP local address must not be null");
        }
        if (channel == null) {
            throw new ValidateException("UDP datagram channel must not be null");
        }
        if (dispatcher == null) {
            throw new ValidateException("UDP dispatcher must not be null");
        }
        this.local = local;
        this.channel = channel;
        this.dispatcher = dispatcher;
        this.state = new AtomicReference<>(Status.OPENED);
        this.pendingSends = new AtomicInteger();
    }

    /**
     * Sends a datagram.
     *
     * @param source source buffer
     * @param remote remote address
     * @return sent byte count future
     */
    public CompletableFuture<Integer> send(final ByteBuffer source, final SocketAddress remote) {
        if (source == null) {
            throw new ValidateException("UDP source buffer must not be null");
        }
        if (remote == null) {
            throw new ValidateException("UDP remote address must not be null");
        }
        ensureOpened();
        if (source.remaining() > MAX_DATAGRAM) {
            return CompletableFuture.failedFuture(new ProtocolException("UDP datagram exceeds maximum payload"));
        }
        final int pending = pendingSends.incrementAndGet();
        if (pending > MAX_PENDING_SENDS) {
            pendingSends.decrementAndGet();
            return CompletableFuture.failedFuture(new StatefulException("UDP send queue is full"));
        }
        final AtomicBoolean released = new AtomicBoolean();
        try {
            final CompletableFuture<Integer> future = dispatcher.supply("udp:send", () -> {
                try {
                    ensureOpened();
                    return channel.send(source.asReadOnlyBuffer(), remote);
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
    public CompletableFuture<DatagramPacket> receive() {
        ensureOpened();
        return dispatcher.supply("udp:receive", () -> {
            try {
                final ByteBuffer buffer = ByteBuffer.allocate(MAX_DATAGRAM);
                final SocketAddress remote = channel.receive(buffer);
                buffer.flip();
                final byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                final DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
                if (remote instanceof InetSocketAddress socket) {
                    packet.setSocketAddress(socket);
                }
                return packet;
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
    public boolean opened() {
        return state.get() == Status.OPENED && channel.isOpen();
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
        if (state.getAndSet(Status.CLOSED) != Status.CLOSED) {
            try {
                channel.close();
            } catch (final IOException e) {
                throw new SocketException("Unable to close UDP channel", e);
            }
        }
    }

    /**
     * Creates a packet payload snapshot.
     *
     * @param packet packet
     * @return bytes
     */
    static byte[] bytes(final DatagramPacket packet) {
        return ArrayKit.sub(packet.getData(), packet.getOffset(), packet.getOffset() + packet.getLength());
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

}
