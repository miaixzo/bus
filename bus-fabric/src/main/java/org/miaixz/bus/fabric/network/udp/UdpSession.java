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

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.NetKit;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.Wiring;

/**
 * Lightweight UDP session bound to one remote address.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class UdpSession {

    /**
     * Remote address.
     */
    private final Address remote;

    /**
     * Local channel.
     */
    private final UdpChannel channel;

    /**
     * Lifecycle state.
     */
    private final AtomicReference<Status> state;

    /**
     * Lifecycle listener.
     */
    private final Listener<Object> listener;

    /**
     * Pending asynchronous sends owned by this session.
     */
    private final ConcurrentLinkedQueue<CompletableFuture<Integer>> sends;

    /**
     * Close cleanup hook.
     */
    private final Runnable onClose;

    /**
     * Creates a UDP session.
     *
     * @param remote  remote address
     * @param channel channel
     */
    UdpSession(final Address remote, final UdpChannel channel) {
        this(remote, channel, Wiring.noop());
    }

    /**
     * Creates a UDP session.
     *
     * @param remote   remote address
     * @param channel  channel
     * @param listener lifecycle listener
     */
    UdpSession(final Address remote, final UdpChannel channel, final Listener<Object> listener) {
        this(remote, channel, listener, () -> {
        });
    }

    /**
     * Creates a UDP session.
     *
     * @param remote   remote address
     * @param channel  channel
     * @param listener lifecycle listener
     * @param onClose  close cleanup hook
     */
    UdpSession(final Address remote, final UdpChannel channel, final Listener<Object> listener,
               final Runnable onClose) {
        this.remote = Assert.notNull(remote, () -> new ValidateException("UDP remote address must not be null"));
        this.channel = Assert.notNull(channel, () -> new ValidateException("UDP channel must not be null"));
        this.state = new AtomicReference<>(Status.OPENED);
        this.listener = Wiring.safe(listener == null ? Wiring.noop() : listener, null);
        this.sends = new ConcurrentLinkedQueue<>();
        this.onClose = Assert.notNull(onClose, () -> new ValidateException("UDP close hook must not be null"));
    }

    /**
     * Returns the remote address.
     *
     * @return remote address
     */
    public Address remote() {
        return remote;
    }

    /**
     * Sends a payload.
     *
     * @param payload payload
     * @return sent byte count future
     */
    public CompletableFuture<Integer> send(final Payload payload) {
        final Payload checkedPayload = Assert
                .notNull(payload, () -> new ValidateException("UDP payload must not be null"));
        ensureOpened();
        final byte[] bytes = checkedPayload.bytes(UdpChannel.MAX_DATAGRAM + 1L);
        if (bytes.length > UdpChannel.MAX_DATAGRAM) {
            return CompletableFuture.failedFuture(new ProtocolException("UDP payload exceeds maximum datagram size"));
        }
        final CompletableFuture<Integer> future = channel.send(ByteBuffer.wrap(bytes), socket(remote));
        sends.add(future);
        future.whenComplete((ignored, cause) -> sends.remove(future));
        if (state.get() != Status.OPENED) {
            sends.remove(future);
            future.cancel(false);
        }
        return future;
    }

    /**
     * Receives a message.
     *
     * @return message future
     */
    public CompletableFuture<Message> receive() {
        ensureOpened();
        return channel.receive().thenApply(packet -> {
            final InetSocketAddress expected = socket(remote);
            if (!(packet.getSocketAddress() instanceof InetSocketAddress actual)
                    || expected.getPort() != actual.getPort() || !expected.getAddress().equals(actual.getAddress())) {
                throw new ProtocolException("UDP packet remote does not match session");
            }
            return Message.of(
                    Protocol.UDP,
                    remote,
                    Headers.empty(),
                    Payload.of(UdpChannel.bytes(packet)),
                    packet.getSocketAddress());
        });
    }

    /**
     * Closes this session.
     *
     * @return true when closed by this call
     */
    public boolean close() {
        if (state.compareAndSet(Status.OPENED, Status.CLOSING)) {
            for (final CompletableFuture<Integer> future : sends) {
                future.cancel(false);
            }
            sends.clear();
            try {
                channel.close();
            } finally {
                onClose.run();
                state.set(Status.CLOSED);
                listener.close(this);
            }
            return true;
        }
        return false;
    }

    /**
     * Returns the lifecycle state.
     *
     * @return state
     */
    public Status state() {
        return state.get();
    }

    /**
     * Returns pending send count owned by this session.
     *
     * @return pending sends
     */
    public int pendingSends() {
        return sends.size();
    }

    /**
     * Creates a socket address.
     *
     * @param address address
     * @return socket address
     */
    static InetSocketAddress socket(final Address address) {
        return NetKit.createAddress(address.host(), address.port());
    }

    /**
     * Ensures this session is open.
     */
    private void ensureOpened() {
        if (state.get() != Status.OPENED || !channel.opened()) {
            throw new StatefulException("UDP session is closed");
        }
    }

}
