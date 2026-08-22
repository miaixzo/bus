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
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.NetKit;
import org.miaixz.bus.fabric.*;
import org.miaixz.bus.fabric.guard.route.AddressGuard;
import org.miaixz.bus.fabric.guard.route.AddressPolicy;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.observe.ObservationMarker;
import org.miaixz.bus.fabric.protocol.MonoCall;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;
import org.miaixz.bus.fabric.runtime.lifecycle.LifecycleScope;

/**
 * Lightweight UDP session bound to one remote address.
 *
 * @author Kimi Liu
 */
public class UdpSession implements Session {

    /**
     * Logical remote endpoint to which the owned UDP channel is bound.
     */
    private final Address remote;

    /**
     * Physical remote endpoint, which may be a proxy relay.
     */
    private final Address transportRemote;

    /**
     * Resolver-validated numeric peer retained by the guarded connection path, or {@code null} for legacy sessions.
     */
    private final Address pinnedPeer;

    /**
     * Immutable address policy used to validate the pinned peer, or {@code null} for legacy sessions.
     */
    private final AddressPolicy addressPolicy;

    /**
     * Optional relay framing codec.
     */
    private final UdpDatagramCodec codec;

    /**
     * Exclusively owned local channel.
     */
    private final UdpChannel channel;

    /**
     * Optional dispatcher for asynchronous Call submission.
     */
    private final Dispatcher dispatcher;

    /**
     * Lifecycle state and listener notification scope.
     */
    private final LifecycleScope scope;

    /**
     * Owner cleanup hook invoked once after channel close is attempted.
     */
    private final Runnable onClose;

    /**
     * Guard ensuring one terminal owner releases the channel.
     */
    private final AtomicBoolean terminating;

    /**
     * Creates a UDP session.
     *
     * @param remote  logical remote endpoint
     * @param channel exclusively owned connected UDP channel
     */
    public UdpSession(final Address remote, final UdpChannel channel) {
        this(remote, channel, null, null, () -> {
        });
    }

    /**
     * Creates a UDP session.
     *
     * @param remote   logical remote endpoint
     * @param channel  exclusively owned connected UDP channel
     * @param listener optional lifecycle listener
     */
    UdpSession(final Address remote, final UdpChannel channel, final Listener<Object> listener) {
        this(remote, channel, listener, null, () -> {
        });
    }

    /**
     * Creates a UDP session.
     *
     * @param remote     logical remote endpoint
     * @param channel    exclusively owned connected UDP channel
     * @param listener   optional lifecycle listener
     * @param dispatcher optional dispatcher for Call submission
     * @param onClose    non-null owner cleanup hook invoked during termination
     */
    UdpSession(final Address remote, final UdpChannel channel, final Listener<Object> listener,
            final Dispatcher dispatcher, final Runnable onClose) {
        this(remote, channel, listener, dispatcher, onClose, null);
    }

    /**
     * Creates a logical UDP session transported through an optional datagram relay codec.
     *
     * @param remote     logical remote endpoint exposed to callers
     * @param channel    exclusively owned UDP channel connected to the target or physical relay
     * @param listener   optional lifecycle listener
     * @param dispatcher optional dispatcher used for asynchronous call submission
     * @param onClose    non-null owner cleanup hook invoked during termination
     * @param codec      logical datagram relay codec, or {@code null} for direct transport
     */
    UdpSession(final Address remote, final UdpChannel channel, final Listener<Object> listener,
            final Dispatcher dispatcher, final Runnable onClose, final UdpDatagramCodec codec) {
        this(remote, null, null, channel, listener, dispatcher, onClose, codec);
    }

    /**
     * Creates a guarded direct UDP session pinned to one resolver-validated numeric peer.
     *
     * @param remote        logical remote endpoint exposed to callers
     * @param pinnedPeer    numeric remote endpoint selected from the complete guarded DNS result
     * @param addressPolicy immutable policy that approved the logical and numeric endpoints
     * @param channel       exclusively owned local UDP channel
     * @param listener      optional lifecycle listener
     * @param dispatcher    optional dispatcher used for asynchronous call submission
     * @param onClose       non-null owner cleanup hook invoked during termination
     */
    UdpSession(final Address remote, final Address pinnedPeer, final AddressPolicy addressPolicy,
            final UdpChannel channel, final Listener<Object> listener, final Dispatcher dispatcher,
            final Runnable onClose) {
        this(remote, pinnedPeer, addressPolicy, channel, listener, dispatcher, onClose, null);
    }

    /**
     * Creates a logical UDP session over either a guarded numeric peer or the legacy optional relay path.
     *
     * @param remote        logical remote endpoint exposed to callers
     * @param pinnedPeer    resolver-validated numeric endpoint, or {@code null} for a legacy session
     * @param addressPolicy immutable approving policy, or {@code null} for a legacy session
     * @param channel       exclusively owned local UDP channel
     * @param listener      optional lifecycle listener
     * @param dispatcher    optional dispatcher used for asynchronous call submission
     * @param onClose       non-null owner cleanup hook invoked during termination
     * @param codec         legacy relay codec, or {@code null} for direct transport
     */
    private UdpSession(final Address remote, final Address pinnedPeer, final AddressPolicy addressPolicy,
            final UdpChannel channel, final Listener<Object> listener, final Dispatcher dispatcher,
            final Runnable onClose, final UdpDatagramCodec codec) {
        this.remote = Assert.notNull(remote, () -> new ValidateException("UDP remote address must not be null"));
        this.codec = codec;
        if (pinnedPeer == null) {
            if (addressPolicy != null) {
                throw new ValidateException("UDP address policy requires a validated numeric peer");
            }
            this.pinnedPeer = null;
            this.addressPolicy = null;
            this.transportRemote = codec == null ? this.remote
                    : Assert.notNull(codec.relay(), () -> new ValidateException("UDP relay address must not be null"));
        } else {
            if (codec != null) {
                throw new ValidateException("Guarded UDP sessions do not support relay framing");
            }
            this.addressPolicy = Assert
                    .notNull(addressPolicy, () -> new ValidateException("UDP address policy must not be null"));
            this.pinnedPeer = pinPeer(this.remote, pinnedPeer, this.addressPolicy);
            this.transportRemote = this.pinnedPeer;
        }
        this.channel = Assert.notNull(channel, () -> new ValidateException("UDP channel must not be null"));
        this.dispatcher = dispatcher;
        this.scope = LifecycleScope.session(
                this,
                "udp-session",
                listener,
                EventObserver.noop(),
                ObservationMarker.SOCKET_OPEN,
                ObservationMarker.SOCKET_CLOSED,
                ObservationMarker.SOCKET_FAILED);
        this.onClose = Assert.notNull(onClose, () -> new ValidateException("UDP close hook must not be null"));
        this.terminating = new AtomicBoolean();
        this.scope.open(this);
    }

    /**
     * Returns the session address.
     *
     * @return logical remote endpoint represented by this session
     */
    @Override
    public Address address() {
        return remote;
    }

    /**
     * Returns the remote address.
     *
     * @return logical remote endpoint represented by this session
     */
    public Address remote() {
        return remote;
    }

    /**
     * Sends a payload.
     *
     * @param payload datagram payload materialized when the returned call executes
     * @return deferred send call that discards the transmitted byte count
     */
    @Override
    public Call<Void> send(final Payload payload) {
        final Payload checkedPayload = Assert
                .notNull(payload, () -> new ValidateException("UDP payload must not be null"));
        return MonoCall
                .<Void>create("udp-session-send", "udp:session:send", dispatcher, EventObserver.noop(), null, () -> {
                    sendPayload(checkedPayload);
                    return null;
                }, this::cancel);
    }

    /**
     * Sends a datagram payload.
     *
     * @param payload datagram payload materialized when the returned call executes
     * @return deferred call completed with the transmitted byte count
     */
    public Call<Integer> sendDatagram(final Payload payload) {
        final Payload checkedPayload = Assert
                .notNull(payload, () -> new ValidateException("UDP payload must not be null"));
        return MonoCall.create(
                "udp-session-send-datagram",
                "udp:session:send-datagram",
                dispatcher,
                EventObserver.noop(),
                null,
                () -> sendPayload(checkedPayload),
                this::cancel);
    }

    /**
     * Receives a message.
     *
     * @return deferred call that receives one datagram from the bound remote endpoint
     */
    public Call<Message> receive() {
        return MonoCall.create(
                "udp-session-receive",
                "udp:session:receive",
                dispatcher,
                EventObserver.noop(),
                null,
                this::receiveMessage,
                this::cancel);
    }

    /**
     * Closes this session.
     *
     * @return {@code true} when this invocation performs the terminal close transition
     */
    @Override
    public boolean close() {
        return terminate(false);
    }

    /**
     * Cancels this session.
     *
     * @return {@code true} when this invocation performs the terminal cancellation transition
     */
    @Override
    public boolean cancel() {
        return terminate(true);
    }

    /**
     * Returns the lifecycle state.
     *
     * @return current lifecycle state
     */
    @Override
    public State state() {
        return scope.state();
    }

    /**
     * Returns whether this session remains active.
     *
     * @return {@code true} when both lifecycle scope and owned channel remain open
     */
    @Override
    public boolean active() {
        return scope.state() == State.RUNNING && channel.active();
    }

    /**
     * Returns session attributes.
     *
     * @return immutable attributes containing the channel's local endpoint and logical remote address
     */
    @Override
    public Map<String, Object> attributes() {
        return Map.of("local", channel.local(), "remote", remote);
    }

    /**
     * Returns the pending send count of the exclusively owned channel.
     *
     * @return pending sends
     */
    public int pendingSends() {
        return channel.pendingSends();
    }

    /**
     * Creates a socket address.
     *
     * @param address logical address whose host and port are converted
     * @return unresolved or resolved socket endpoint created by the network endpoint resolver
     */
    static InetSocketAddress socket(final Address address) {
        return NetKit.createAddress(address.host(), address.port());
    }

    /**
     * Revalidates and normalizes the resolver-selected numeric peer before it becomes session state.
     *
     * @param logical logical remote endpoint
     * @param peer    numeric peer candidate
     * @param policy  immutable address policy
     * @return normalized numeric peer pinned to the session
     */
    private static Address pinPeer(final Address logical, final Address peer, final AddressPolicy policy) {
        if (logical.protocol() != org.miaixz.bus.core.net.Protocol.UDP
                || peer.protocol() != org.miaixz.bus.core.net.Protocol.UDP || logical.port() != peer.port()) {
            throw new ValidateException("UDP validated peer must match the logical protocol and port");
        }
        final InetSocketAddress socket = socket(peer);
        if (socket.isUnresolved() || socket.getAddress() == null) {
            throw new ValidateException("UDP validated peer must be numeric");
        }
        final java.net.InetAddress numeric = new AddressGuard(policy)
                .checkTarget(logical, List.of(socket.getAddress()));
        return new Address(peer.scheme(), numeric.getHostAddress(), peer.port(), peer.path());
    }

    /**
     * Ensures this session is open.
     */
    private void ensureOpened() {
        if (!active()) {
            throw new StatefulException("UDP session is closed");
        }
    }

    /**
     * Materializes and sends one datagram through the channel exactly once.
     *
     * @param payload payload to materialize within the maximum safe UDP body size
     * @return sent byte count
     */
    private int sendPayload(final Payload payload) {
        ensureOpened();
        if (payload.length() > Normal._65535 - Normal._28) {
            throw new ProtocolException("UDP payload exceeds maximum datagram size");
        }
        final Payload encoded = codec == null ? payload : codec.encode(remote, payload);
        final byte[] bytes = encoded.bytes(Normal._65535 - Normal._28 + 1L);
        if (bytes.length > Normal._65535 - Normal._28) {
            throw new ProtocolException("UDP payload exceeds maximum datagram size");
        }
        final Buffer source = new Buffer().write(bytes);
        final long requested = source.size();
        final int written = await(
                channel.send(source, requested, socket(transportRemote)),
                "Unable to send UDP datagram");
        if (written != requested || source.size() != Normal._0) {
            throw new ProtocolException("UDP channel did not fully consume the datagram payload");
        }
        return written;
    }

    /**
     * Receives one message through the channel exactly once.
     *
     * @return one datagram message verified to originate from the bound remote endpoint
     */
    private Message receiveMessage() {
        ensureOpened();
        final Message message = await(channel.receive(), "Unable to receive UDP datagram");
        if (!socket(transportRemote).equals(socket(message.address()))) {
            throw new ProtocolException("UDP packet remote does not match session");
        }
        if (codec == null) {
            return message;
        }
        return Message.of(
                message.protocol(),
                remote,
                message.headers(),
                codec.decode(remote, message.payload()),
                message.tag());
    }

    /**
     * Terminates the session after releasing its exclusively owned channel.
     *
     * @param cancelled {@code true} to report cancellation, or {@code false} for an ordinary close
     * @return {@code true} when this invocation performs the terminal lifecycle transition
     */
    private boolean terminate(final boolean cancelled) {
        if (!terminating.compareAndSet(false, true)) {
            return false;
        }
        scope.closing();
        final RuntimeException failure = release();
        final boolean changed = cancelled ? scope.cancel(new StatefulException("UDP session was cancelled"))
                : scope.close(this);
        if (failure != null) {
            throw failure;
        }
        return changed;
    }

    /**
     * Releases the underlying channel and session owner hook.
     *
     * @return first channel or cleanup-hook failure with any second failure suppressed, or {@code null}
     */
    private RuntimeException release() {
        RuntimeException failure = null;
        try {
            channel.close();
        } catch (final RuntimeException e) {
            failure = e;
        }
        try {
            onClose.run();
        } catch (final RuntimeException e) {
            if (failure == null) {
                failure = e;
            } else if (failure != e) {
                failure.addSuppressed(e);
            }
        }
        return failure;
    }

    /**
     * Awaits a private channel operation and preserves runtime failures.
     *
     * @param future  non-null private channel operation to join
     * @param message context used when a missing or checked result must be wrapped
     * @param <T>     non-null completion type
     * @return non-null channel operation result
     */
    private static <T> T await(final CompletableFuture<T> future, final String message) {
        try {
            final T result = Assert.notNull(future, () -> new ValidateException("UDP channel future must not be null"))
                    .join();
            return Assert.notNull(result, () -> new InternalException(message + ": missing result"));
        } catch (final CompletionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtime) {
                throw runtime;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new InternalException(message, cause);
        }
    }

}
