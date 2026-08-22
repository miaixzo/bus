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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.EnumSet;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.NetKit;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.guard.route.AddressGuard;
import org.miaixz.bus.fabric.guard.route.AddressPolicy;
import org.miaixz.bus.fabric.network.Transport;
import org.miaixz.bus.fabric.network.aio.AioGroup;
import org.miaixz.bus.fabric.network.dns.DnsResolver;
import org.miaixz.bus.logger.Logger;

/**
 * UDP network entry point backed by datagram channels.
 *
 * @author Kimi Liu
 */
public class UdpNetwork implements AutoCloseable {

    /**
     * Transport set accepted by this network implementation.
     */
    private static final EnumSet<Transport> SUPPORTED = EnumSet.of(Transport.UDP);

    /**
     * AIO group that supplies the dispatcher used by created channels and sessions.
     */
    private final AioGroup group;

    /**
     * Channels created by this network and retained until closed or pruned.
     */
    private final ConcurrentLinkedDeque<UdpChannel> channels;

    /**
     * Active server bindings created by the asynchronous guarded API.
     */
    private final ConcurrentLinkedDeque<ServerBinding> bindings;

    /**
     * One-way network-closure flag.
     */
    private final AtomicBoolean closed;

    /**
     * Optional listener notified by connected UDP sessions and connection setup failures.
     */
    private final Listener<Object> listener;

    /**
     * Optional codec applied to subsequently created logical sessions.
     */
    private volatile UdpDatagramCodec relayCodec;

    /**
     * Cleanup paired with the optional relay codec.
     */
    private volatile Runnable relayCloseHook = () -> {
    };

    /**
     * Creates a UDP network with an optional lifecycle listener.
     *
     * @param group    AIO group that supplies channel dispatchers
     * @param listener lifecycle listener, or {@code null} when notifications are not required
     */
    public UdpNetwork(final AioGroup group, final Listener<Object> listener) {
        this.group = Assert.notNull(group, () -> new ValidateException("AIO group must not be null"));
        this.channels = new ConcurrentLinkedDeque<>();
        this.bindings = new ConcurrentLinkedDeque<>();
        this.closed = new AtomicBoolean();
        this.listener = listener;
    }

    /**
     * Creates a UDP network.
     *
     * @param group AIO group that supplies channel dispatchers
     * @return UDP network without a lifecycle listener
     */
    public static UdpNetwork create(final AioGroup group) {
        return new UdpNetwork(group, null);
    }

    /**
     * Creates a UDP network with a lifecycle listener.
     *
     * @param group    AIO group that supplies channel dispatchers
     * @param listener lifecycle listener, or {@code null} to disable notifications
     * @return UDP network configured with the supplied listener
     */
    public static UdpNetwork create(final AioGroup group, final Listener<Object> listener) {
        return new UdpNetwork(group, listener);
    }

    /**
     * Opens and binds a managed datagram channel to a local UDP address.
     *
     * @param address local UDP address to bind
     * @return managed channel bound to the local address
     */
    public synchronized UdpChannel bind(final Address address) {
        final Address checkedAddress = Assert
                .notNull(address, () -> new ValidateException("UDP bind address must not be null"));
        requireUdp(checkedAddress);
        ensureOpen();
        try {
            final DatagramChannel datagram = DatagramChannel.open();
            datagram.bind(socket(checkedAddress));
            final UdpChannel channel = new UdpChannel(checkedAddress, datagram, group.dispatcher());
            channels.add(channel);
            return channel;
        } catch (final IOException e) {
            throw new SocketException("Unable to bind UDP channel", e);
        }
    }

    /**
     * Resolves, validates, and binds a managed UDP server without blocking the caller on DNS resolution.
     *
     * @param local            logical local UDP address
     * @param addressPolicy    explicit target and peer address policy
     * @param resolver         runtime-bound asynchronous resolver
     * @param maxDatagramBytes maximum accepted datagram payload from 1 through 65507
     * @param maxInFlight      maximum concurrent handler stages from 1 through 1024
     * @param handler          asynchronous datagram handler
     * @return stage containing the running server binding
     */
    public CompletionStage<ServerBinding> bind(
            final Address local,
            final AddressPolicy addressPolicy,
            final DnsResolver resolver,
            final int maxDatagramBytes,
            final int maxInFlight,
            final DatagramHandler handler) {
        final Address checkedLocal = requireAddress(local, "UDP bind address");
        final AddressPolicy checkedPolicy = require(addressPolicy, "UDP address policy");
        final DnsResolver checkedResolver = require(resolver, "UDP DNS resolver");
        final DatagramHandler checkedHandler = require(handler, "UDP datagram handler");
        requireUdp(checkedLocal);
        validateLimits(maxDatagramBytes, maxInFlight);
        ensureOpen();
        final AddressGuard guard = new AddressGuard(checkedPolicy);
        return checkedResolver.resolveAsync(checkedLocal.host()).thenApply(result -> {
            final InetAddress numeric = guard.checkBinding(checkedLocal, result);
            final UdpChannel channel = bindNumeric(checkedLocal, numeric);
            final ReceiveLoop loop = new ReceiveLoop(channel, guard, maxDatagramBytes, maxInFlight, checkedHandler);
            final ServerBinding binding = new ServerBinding(channel.local(), loop);
            bindings.add(binding);
            loop.closed().whenComplete((ignored, cause) -> {
                bindings.remove(binding);
                channels.remove(channel);
            });
            loop.start();
            return binding;
        });
    }

    /**
     * Opens an ephemeral managed datagram channel and creates a session targeting a remote UDP address.
     *
     * @param remote remote UDP address targeted by the session
     * @return session backed by the newly opened datagram channel
     */
    public synchronized UdpSession connect(final Address remote) {
        return connect(remote, relayCodec, relayCloseHook);
    }

    /**
     * Resolves and validates every DNS answer before creating a direct UDP session to one numeric peer.
     *
     * @param remote        logical remote UDP address
     * @param addressPolicy explicit target address policy retained by the session
     * @param resolver      runtime-bound asynchronous resolver
     * @return stage containing a session pinned to the validated numeric peer
     */
    public CompletionStage<UdpSession> connect(
            final Address remote,
            final AddressPolicy addressPolicy,
            final DnsResolver resolver) {
        final Address checkedRemote = requireAddress(remote, "UDP remote address");
        final AddressPolicy checkedPolicy = require(addressPolicy, "UDP address policy");
        final DnsResolver checkedResolver = require(resolver, "UDP DNS resolver");
        requireUdp(checkedRemote);
        ensureOpen();
        final AddressGuard guard = new AddressGuard(checkedPolicy);
        return checkedResolver.resolveAsync(checkedRemote.host()).thenApply(result -> {
            final InetAddress numeric = guard.checkTarget(checkedRemote, result);
            return connectNumeric(checkedRemote, numeric, checkedPolicy);
        });
    }

    /**
     * Configures relay framing for subsequently opened UDP and KCP sessions.
     *
     * @param codec     non-null logical-to-physical datagram codec
     * @param closeHook non-null cleanup invoked when the relayed session closes
     * @return this network
     */
    public synchronized UdpNetwork relay(final UdpDatagramCodec codec, final Runnable closeHook) {
        this.relayCodec = Assert.notNull(codec, () -> new ValidateException("UDP relay codec must not be null"));
        this.relayCloseHook = Assert
                .notNull(closeHook, () -> new ValidateException("UDP relay close hook must not be null"));
        return this;
    }

    /**
     * Opens a logical UDP session through an optional physical relay codec.
     *
     * @param remote    non-null logical UDP destination exposed by the returned session
     * @param codec     relay codec, or {@code null} for direct datagram transport
     * @param closeHook non-null cleanup invoked after the session channel is released
     * @return connected logical UDP session
     */
    public synchronized UdpSession connect(
            final Address remote,
            final UdpDatagramCodec codec,
            final Runnable closeHook) {
        final Address checkedRemote = Assert
                .notNull(remote, () -> new ValidateException("UDP remote address must not be null"));
        requireUdp(checkedRemote);
        ensureOpen();
        try {
            final DatagramChannel datagram = DatagramChannel.open();
            datagram.bind(null);
            final InetSocketAddress local = (InetSocketAddress) datagram.getLocalAddress();
            final Address localAddress = new Address(Transport.UDP.scheme(), Protocol.HOST_IPV4, local.getPort(), null);
            final UdpChannel channel = new UdpChannel(localAddress, datagram, group.dispatcher());
            channels.add(channel);
            final Runnable checkedClose = Assert
                    .notNull(closeHook, () -> new ValidateException("UDP close hook must not be null"));
            return new UdpSession(checkedRemote, channel, listener, group.dispatcher(), () -> {
                channels.remove(channel);
                checkedClose.run();
            }, codec);
        } catch (final IOException e) {
            if (listener != null) {
                listener.failure(this, e);
            }
            throw new SocketException("Unable to create UDP session", e);
        }
    }

    /**
     * Binds one channel to a resolver-supplied numeric address.
     *
     * @param logical logical local address retaining the configured scheme and port
     * @param numeric validated numeric bind address
     * @return managed bound channel
     */
    private synchronized UdpChannel bindNumeric(final Address logical, final InetAddress numeric) {
        ensureOpen();
        DatagramChannel datagram = null;
        try {
            datagram = DatagramChannel.open();
            datagram.bind(new InetSocketAddress(numeric, logical.port()));
            final InetSocketAddress bound = (InetSocketAddress) datagram.getLocalAddress();
            final Address local = numericAddress(logical, bound.getAddress(), bound.getPort());
            final UdpChannel channel = new UdpChannel(local, datagram, group.dispatcher());
            channels.add(channel);
            return channel;
        } catch (final IOException | RuntimeException failure) {
            closeQuietly(datagram);
            if (failure instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new SocketException("Unable to bind guarded UDP channel", failure);
        }
    }

    /**
     * Creates one direct session pinned to a resolver-supplied numeric address.
     *
     * @param logical logical remote address retained for protocol identity
     * @param numeric validated numeric peer address
     * @param policy  immutable policy retained by the session
     * @return managed UDP session
     */
    private synchronized UdpSession connectNumeric(
            final Address logical,
            final InetAddress numeric,
            final AddressPolicy policy) {
        ensureOpen();
        DatagramChannel datagram = null;
        try {
            datagram = DatagramChannel.open();
            datagram.bind(null);
            final InetSocketAddress bound = (InetSocketAddress) datagram.getLocalAddress();
            final Address local = new Address(Transport.UDP.scheme(), bound.getAddress().getHostAddress(),
                    bound.getPort(), null);
            final UdpChannel channel = new UdpChannel(local, datagram, group.dispatcher());
            channels.add(channel);
            final Address peer = numericAddress(logical, numeric, logical.port());
            return new UdpSession(logical, peer, policy, channel, listener, group.dispatcher(), () -> {
                channels.remove(channel);
            });
        } catch (final IOException | RuntimeException failure) {
            closeQuietly(datagram);
            if (listener != null) {
                listener.failure(this, failure);
            }
            if (failure instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new SocketException("Unable to create guarded UDP session", failure);
        }
    }

    /**
     * Returns whether this network supports a transport.
     *
     * @param transport transport to test
     * @return {@code true} only for {@link Transport#UDP}
     */
    public boolean supports(final Transport transport) {
        return SUPPORTED.contains(Assert.notNull(transport, () -> new ValidateException("Transport must not be null")));
    }

    /**
     * Prunes closed channels and returns the number of managed channels that remain open.
     *
     * @return number of currently open managed channels
     */
    public int channelCount() {
        channels.removeIf(channel -> !channel.active());
        return channels.size();
    }

    /**
     * Permanently closes this network and all currently managed channels.
     *
     */
    @Override
    public synchronized void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        RuntimeException failure = null;
        for (final ServerBinding binding : bindings) {
            try {
                binding.shutdown();
            } catch (final RuntimeException e) {
                failure = append(failure, e);
            }
        }
        bindings.clear();
        for (final UdpChannel channel : channels) {
            try {
                channel.close();
            } catch (final RuntimeException e) {
                failure = append(failure, e);
            }
        }
        channels.clear();
        if (failure != null) {
            throw failure;
        }
    }

    /**
     * Validates that an address resolves to the UDP transport.
     *
     * @param address address whose scheme is validated
     */
    private static void requireUdp(final Address address) {
        final Transport transport = Transport.fromScheme(address.scheme());
        if (transport != Transport.UDP) {
            throw new ProtocolException("UDP network does not support transport: " + transport);
        }
    }

    /**
     * Creates a bind socket address.
     *
     * @param address fabric address containing the bind host and port
     * @return internet socket address created from the fabric address
     */
    private static InetSocketAddress socket(final Address address) {
        return NetKit.createAddress(address.host(), address.port());
    }

    /**
     * Creates an address retaining the logical scheme and path while replacing its host and port with numeric values.
     *
     * @param logical logical source address
     * @param numeric validated numeric address
     * @param port    effective numeric port
     * @return numeric immutable address
     */
    private static Address numericAddress(final Address logical, final InetAddress numeric, final int port) {
        return new Address(logical.scheme(), numeric.getHostAddress(), port, logical.path());
    }

    /**
     * Validates server payload and concurrency limits.
     *
     * @param maxDatagramBytes maximum datagram bytes
     * @param maxInFlight      maximum concurrent handlers
     */
    private static void validateLimits(final int maxDatagramBytes, final int maxInFlight) {
        if (maxDatagramBytes < Normal._1 || maxDatagramBytes > Normal._65535 - Normal._28) {
            throw new ValidateException("Maximum UDP datagram bytes must be between 1 and 65507");
        }
        if (maxInFlight < Normal._1 || maxInFlight > Normal._1024) {
            throw new ValidateException("Maximum UDP in-flight handlers must be between 1 and 1024");
        }
    }

    /**
     * Validates a required address.
     *
     * @param address candidate address
     * @param name    argument label
     * @return validated address
     */
    private static Address requireAddress(final Address address, final String name) {
        return require(address, name);
    }

    /**
     * Validates a required object.
     *
     * @param value candidate value
     * @param name  argument label
     * @param <T>   value type
     * @return validated value
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

    /**
     * Closes a channel opened by a failed construction path.
     *
     * @param channel channel to close, or {@code null}
     */
    private static void closeQuietly(final DatagramChannel channel) {
        if (channel != null) {
            try {
                channel.close();
            } catch (final IOException ignored) {
                // The construction failure remains authoritative.
            }
        }
    }

    /**
     * Appends a cleanup failure without suppressing an exception onto itself.
     *
     * @param current current failure, or {@code null}
     * @param next    next failure
     * @return accumulated failure
     */
    private static RuntimeException append(final RuntimeException current, final RuntimeException next) {
        if (current == null) {
            return next;
        }
        if (current != next) {
            current.addSuppressed(next);
        }
        return current;
    }

    /**
     * Handles one validated UDP datagram and optionally produces one atomic response to its original peer.
     *
     * @author Kimi Liu
     */
    @FunctionalInterface
    public interface DatagramHandler {

        /**
         * Handles one complete validated datagram.
         *
         * @param datagram immutable received datagram message
         * @return stage containing a response payload or an empty value for no response
         */
        CompletionStage<Optional<Payload>> onDatagram(Message datagram);

    }

    /**
     * Managed UDP server binding with idempotent asynchronous shutdown and handler drain.
     *
     * @author Kimi Liu
     */
    public static class ServerBinding {

        /**
         * Numeric local address selected and bound by the network.
         */
        private final Address local;

        /**
         * Owned receive loop.
         */
        private final ReceiveLoop loop;

        /**
         * Creates a managed server binding.
         *
         * @param local numeric local address
         * @param loop  owned receive loop
         */
        public ServerBinding(final Address local, final ReceiveLoop loop) {
            this.local = local;
            this.loop = loop;
        }

        /**
         * Returns the numeric local address.
         *
         * @return bound local address
         */
        public Address local() {
            return local;
        }

        /**
         * Stops receiving, closes the channel, and completes after entered handler stages and responses drain.
         *
         * @return shared shutdown stage
         */
        public CompletionStage<Void> shutdown() {
            return loop.shutdown();
        }

    }

    /**
     * Completion-driven server receive loop that owns peer validation, handler admission, response send, and drain.
     *
     * @author Kimi Liu
     */
    private static final class ReceiveLoop {

        /**
         * Owned bound datagram channel.
         */
        private final UdpChannel channel;

        /**
         * Address guard applied to every numeric packet peer.
         */
        private final AddressGuard guard;

        /**
         * Maximum accepted request and response payload size.
         */
        private final int maxDatagramBytes;

        /**
         * Maximum concurrent entered handler stages.
         */
        private final int maxInFlight;

        /**
         * Application handler.
         */
        private final DatagramHandler handler;

        /**
         * Number of handler stages whose optional responses have not finished sending.
         */
        private final AtomicInteger inFlight;

        /**
         * One-way receive admission flag.
         */
        private final AtomicBoolean accepting;

        /**
         * Shared shutdown completion.
         */
        private final CompletableFuture<Void> closed;

        /**
         * Fatal receive or shutdown failure completed after handler drain.
         */
        private volatile Throwable terminalFailure;

        /**
         * Creates a server receive loop.
         *
         * @param channel          owned channel
         * @param guard            peer guard
         * @param maxDatagramBytes maximum payload bytes
         * @param maxInFlight      maximum concurrent handlers
         * @param handler          application handler
         */
        private ReceiveLoop(final UdpChannel channel, final AddressGuard guard, final int maxDatagramBytes,
                final int maxInFlight, final DatagramHandler handler) {
            this.channel = channel;
            this.guard = guard;
            this.maxDatagramBytes = maxDatagramBytes;
            this.maxInFlight = maxInFlight;
            this.handler = handler;
            this.inFlight = new AtomicInteger();
            this.accepting = new AtomicBoolean(true);
            this.closed = new CompletableFuture<>();
        }

        /**
         * Starts the first asynchronous receive.
         */
        private void start() {
            receive();
        }

        /**
         * Returns the shared close future for network bookkeeping.
         *
         * @return shared close future
         */
        private CompletableFuture<Void> closed() {
            return closed;
        }

        /**
         * Submits one receive while admission remains open.
         */
        private void receive() {
            if (!accepting.get()) {
                completeIfDrained();
                return;
            }
            try {
                channel.receiveDatagram().whenComplete(this::received);
            } catch (final RuntimeException failure) {
                fail(failure);
            }
        }

        /**
         * Processes one receive completion and immediately keeps the receive lane active.
         *
         * @param datagram received datagram
         * @param cause    receive failure
         */
        private void received(final UdpChannel.ReceivedDatagram datagram, final Throwable cause) {
            if (cause != null) {
                if (accepting.get()) {
                    fail(cause);
                } else {
                    completeIfDrained();
                }
                return;
            }
            if (!accepting.get()) {
                completeIfDrained();
                return;
            }
            receive();
            if (datagram == null || datagram.message().payload().length() > maxDatagramBytes) {
                return;
            }
            validatePeer(datagram);
        }

        /**
         * Validates the numeric peer before handler admission.
         *
         * @param datagram received datagram and numeric peer
         */
        private void validatePeer(final UdpChannel.ReceivedDatagram datagram) {
            try {
                final InetAddress peer = guard.checkPeer(datagram.peer());
                if (acquire()) {
                    invoke(datagram.message(), peer);
                }
            } catch (final RuntimeException ignored) {
                // Peer-policy rejection is a silent datagram drop.
            }
        }

        /**
         * Atomically admits one handler without racing shutdown or exceeding the configured limit.
         *
         * @return true when one handler slot was acquired
         */
        private synchronized boolean acquire() {
            if (!accepting.get() || inFlight.get() >= maxInFlight) {
                return false;
            }
            inFlight.incrementAndGet();
            return true;
        }

        /**
         * Invokes the handler and owns its optional response through send completion.
         *
         * @param message validated datagram
         * @param peer    validated numeric peer
         */
        private void invoke(final Message message, final InetAddress peer) {
            final CompletionStage<Optional<Payload>> stage;
            try {
                stage = require(handler.onDatagram(message), "UDP handler stage");
            } catch (final RuntimeException failure) {
                handlerFailed(failure);
                release();
                return;
            }
            stage.whenComplete((response, cause) -> {
                if (cause != null) {
                    handlerFailed(cause);
                    release();
                    return;
                }
                if (response == null) {
                    handlerFailed(new ValidateException("UDP handler result must not be null"));
                    release();
                    return;
                }
                if (response.isEmpty()) {
                    release();
                    return;
                }
                send(response.get(), peer, message.address().port());
            });
        }

        /**
         * Sends one optional handler response atomically to the original numeric peer.
         *
         * @param payload response payload
         * @param peer    original numeric peer
         * @param port    original peer port
         */
        private void send(final Payload payload, final InetAddress peer, final int port) {
            try {
                if (payload.length() > maxDatagramBytes) {
                    throw new ProtocolException("UDP response exceeds maximum datagram bytes");
                }
                final byte[] bytes = payload.bytes((long) maxDatagramBytes + Normal._1);
                final Buffer source = new Buffer().write(bytes);
                channel.send(source, bytes.length, new InetSocketAddress(peer, port)).whenComplete((written, cause) -> {
                    if (cause != null || written == null || written != bytes.length) {
                        fail(cause == null ? new SocketException("UDP response send was incomplete") : cause);
                    }
                    release();
                });
            } catch (final RuntimeException failure) {
                handlerFailed(failure);
                release();
            }
        }

        /**
         * Logs one generic handler or response failure and keeps the receive loop alive.
         *
         * @param cause handler or response failure
         */
        private void handlerFailed(final Throwable cause) {
            Logger.warn(
                    false,
                    "Fabric",
                    cause,
                    "UDP datagram processing failed: exception={}",
                    cause.getClass().getSimpleName());
        }

        /**
         * Releases one entered handler slot and completes shutdown after the final drain.
         */
        private void release() {
            inFlight.decrementAndGet();
            completeIfDrained();
        }

        /**
         * Initiates idempotent shutdown and returns the shared drain stage.
         *
         * @return shared shutdown stage
         */
        private CompletionStage<Void> shutdown() {
            if (accepting.compareAndSet(true, false)) {
                try {
                    channel.close();
                } catch (final RuntimeException failure) {
                    terminalFailure = failure;
                }
            }
            completeIfDrained();
            return closed;
        }

        /**
         * Terminates receive admission after a fatal channel failure.
         *
         * @param cause fatal channel failure
         */
        private void fail(final Throwable cause) {
            terminalFailure = cause;
            shutdown();
        }

        /**
         * Completes the shared shutdown stage after admission is closed and all entered handlers drain.
         */
        private synchronized void completeIfDrained() {
            if (accepting.get() || inFlight.get() != Normal._0 || closed.isDone()) {
                return;
            }
            final Throwable failure = terminalFailure;
            if (failure == null) {
                closed.complete(null);
            } else {
                closed.completeExceptionally(failure);
            }
        }

    }

    /**
     * Rejects channel creation after network closure.
     *
     */
    private void ensureOpen() {
        if (closed.get()) {
            throw new StatefulException("UDP network is closed");
        }
    }

}
