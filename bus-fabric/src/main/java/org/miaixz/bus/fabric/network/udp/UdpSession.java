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
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.NetKit;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Call;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Session;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.observe.ObservationMarker;
import org.miaixz.bus.fabric.runtime.lifecycle.LifecycleScope;

/**
 * Lightweight UDP session bound to one remote address.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class UdpSession implements Session {

    /**
     * Remote address.
     */
    private final Address remote;

    /**
     * Local channel.
     */
    private final UdpChannel channel;

    /**
     * Lifecycle scope.
     */
    private final LifecycleScope scope;

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
        this(remote, channel, null);
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
        this.scope = LifecycleScope.session(
                this,
                "udp-session",
                listener,
                EventObserver.noop(),
                ObservationMarker.SOCKET_OPEN,
                ObservationMarker.SOCKET_CLOSED,
                ObservationMarker.SOCKET_FAILED);
        this.sends = new ConcurrentLinkedQueue<>();
        this.onClose = Assert.notNull(onClose, () -> new ValidateException("UDP close hook must not be null"));
        this.scope.open(this);
    }

    /**
     * Returns the session address.
     *
     * @return session address
     */
    @Override
    public Address address() {
        return remote;
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
     * @return send call
     */
    @Override
    public Call<Void> send(final Payload payload) {
        return new DatagramCall(sendDatagram(payload));
    }

    /**
     * Sends a datagram payload.
     *
     * @param payload payload
     * @return sent byte count future
     */
    public CompletableFuture<Integer> sendDatagram(final Payload payload) {
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
        if (!opened()) {
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
    @Override
    public boolean close() {
        if (scope.state().terminal()) {
            return false;
        }
        scope.closing();
        clearSends();
        final RuntimeException failure = release();
        final boolean changed = scope.close(this);
        if (failure != null) {
            throw failure;
        }
        return changed;
    }

    /**
     * Cancels this session.
     *
     * @return true when cancelled by this call
     */
    @Override
    public boolean cancel() {
        if (scope.state().terminal()) {
            return false;
        }
        clearSends();
        final RuntimeException failure = release();
        final boolean changed = scope.cancel(new StatefulException("UDP session was cancelled"));
        if (failure != null) {
            throw failure;
        }
        return changed;
    }

    /**
     * Returns the lifecycle state.
     *
     * @return state
     */
    @Override
    public Status state() {
        return scope.state();
    }

    /**
     * Returns whether this session is opened.
     *
     * @return true when opened
     */
    @Override
    public boolean opened() {
        return scope.state() == Status.OPENED && channel.opened();
    }

    /**
     * Returns session attributes.
     *
     * @return attributes
     */
    @Override
    public Map<String, Object> attributes() {
        return Map.of("local", channel.local(), "remote", remote);
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
        if (!opened()) {
            throw new StatefulException("UDP session is closed");
        }
    }

    /**
     * Clears pending sends.
     */
    private void clearSends() {
        for (final CompletableFuture<Integer> future : sends) {
            future.cancel(false);
        }
        sends.clear();
    }

    /**
     * Releases the underlying channel and session owner hook.
     */
    private RuntimeException release() {
        RuntimeException failure = null;
        try {
            channel.close();
        } catch (final RuntimeException e) {
            failure = e;
        } finally {
            onClose.run();
        }
        return failure;
    }

    /**
     * Future-backed datagram send call.
     */
    private static final class DatagramCall implements Call<Void> {

        /**
         * Datagram send future.
         */
        private final CompletableFuture<Integer> future;

        /**
         * Creates a call.
         *
         * @param future datagram send future
         */
        private DatagramCall(final CompletableFuture<Integer> future) {
            this.future = Assert.notNull(future, () -> new ValidateException("UDP send future must not be null"));
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
         * Returns this already-started call.
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
                future.get();
                return null;
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new InternalException("Interrupted while waiting for UDP send", e);
            } catch (final ExecutionException e) {
                throw new InternalException("UDP send failed", e.getCause());
            } catch (final CancellationException e) {
                throw new InternalException("UDP send was cancelled", e);
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
                    throw new TimeoutException("UDP send timed out");
                }
                return await();
            }
            try {
                future.get(timeout.toNanos(), TimeUnit.NANOSECONDS);
                return null;
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new InternalException("Interrupted while waiting for UDP send", e);
            } catch (final ExecutionException e) {
                throw new InternalException("UDP send failed", e.getCause());
            } catch (final CancellationException e) {
                throw new InternalException("UDP send was cancelled", e);
            } catch (final java.util.concurrent.TimeoutException e) {
                cancel();
                throw new TimeoutException("UDP send timed out", e);
            } catch (final ArithmeticException e) {
                throw new ValidateException("Timeout is too large");
            }
        }

        /**
         * Cancels the send.
         *
         * @return true when cancelled
         */
        @Override
        public boolean cancel() {
            return future.cancel(false);
        }

        /**
         * Returns cancellation state.
         *
         * @return true when cancelled
         */
        @Override
        public boolean cancelled() {
            return future.isCancelled();
        }

        /**
         * Returns completion state.
         *
         * @return true when complete
         */
        @Override
        public boolean done() {
            return future.isDone();
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
            final Duration checkedTimeout = Assert
                    .notNull(timeout, () -> new ValidateException("Timeout must be non-null and non-negative"));
            Assert.isFalse(
                    checkedTimeout.isNegative(),
                    () -> new ValidateException("Timeout must be non-null and non-negative"));
        }

    }

}
