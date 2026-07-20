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
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Call;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Session;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.observe.ObservationMarker;
import org.miaixz.bus.fabric.protocol.MonoCall;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;
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
     * Exclusively owned local channel.
     */
    private final UdpChannel channel;

    /**
     * Optional dispatcher for asynchronous Call submission.
     */
    private final Dispatcher dispatcher;

    /**
     * Lifecycle scope.
     */
    private final LifecycleScope scope;

    /**
     * Close cleanup hook.
     */
    private final Runnable onClose;

    /**
     * Guard ensuring one terminal owner releases the channel.
     */
    private final AtomicBoolean terminating;

    /**
     * Creates a UDP session.
     *
     * @param remote  remote address
     * @param channel channel
     */
    UdpSession(final Address remote, final UdpChannel channel) {
        this(remote, channel, null, null, () -> {
        });
    }

    /**
     * Creates a UDP session.
     *
     * @param remote   remote address
     * @param channel  channel
     * @param listener lifecycle listener
     */
    UdpSession(final Address remote, final UdpChannel channel, final Listener<Object> listener) {
        this(remote, channel, listener, null, () -> {
        });
    }

    /**
     * Creates a UDP session.
     *
     * @param remote     remote address
     * @param channel    channel
     * @param listener   lifecycle listener
     * @param dispatcher optional dispatcher for Call submission
     * @param onClose    close cleanup hook
     */
    UdpSession(final Address remote, final UdpChannel channel, final Listener<Object> listener,
            final Dispatcher dispatcher, final Runnable onClose) {
        this.remote = Assert.notNull(remote, () -> new ValidateException("UDP remote address must not be null"));
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
     * @param payload payload
     * @return datagram send call
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
     * @return receive call
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
     * @return true when closed by this call
     */
    @Override
    public boolean close() {
        return terminate(false);
    }

    /**
     * Cancels this session.
     *
     * @return true when cancelled by this call
     */
    @Override
    public boolean cancel() {
        return terminate(true);
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
     * Materializes and sends one datagram through the channel exactly once.
     *
     * @param payload payload
     * @return sent byte count
     */
    private int sendPayload(final Payload payload) {
        ensureOpened();
        if (payload.length() > Normal._65535 - Normal._28) {
            throw new ProtocolException("UDP payload exceeds maximum datagram size");
        }
        final byte[] bytes = payload.bytes(Normal._65535 - Normal._28 + 1L);
        if (bytes.length > Normal._65535 - Normal._28) {
            throw new ProtocolException("UDP payload exceeds maximum datagram size");
        }
        final Buffer source = new Buffer().write(bytes);
        final long requested = source.size();
        final int written = await(channel.send(source, requested, socket(remote)), "Unable to send UDP datagram");
        if (written != requested || source.size() != Normal._0) {
            throw new ProtocolException("UDP channel did not fully consume the datagram payload");
        }
        return written;
    }

    /**
     * Receives one message through the channel exactly once.
     *
     * @return received message
     */
    private Message receiveMessage() {
        ensureOpened();
        final Message message = await(channel.receive(), "Unable to receive UDP datagram");
        if (!socket(remote).equals(socket(message.address()))) {
            throw new ProtocolException("UDP packet remote does not match session");
        }
        return message;
    }

    /**
     * Terminates the session after releasing its exclusively owned channel.
     *
     * @param cancelled true to cancel instead of close
     * @return true when this invocation terminated the session
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
     * @return release failure, or null
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
     * @param future  channel operation
     * @param message failure message
     * @param <T>     result type
     * @return operation result
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
