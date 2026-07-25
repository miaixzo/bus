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
package org.miaixz.bus.fabric.protocol.http.chain;

import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.crypto.builtin.TlsHandshake;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.Timeout;
import org.miaixz.bus.fabric.network.Connection;
import org.miaixz.bus.fabric.network.tls.TlsChannel;
import org.miaixz.bus.fabric.network.tls.TlsEngine;
import org.miaixz.bus.fabric.network.tls.TlsSettings;
import org.miaixz.bus.fabric.network.tls.TlsSocketChannel;
import org.miaixz.bus.fabric.network.tls.context.TlsContext;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;

/**
 * Upgrades connected HTTP transports to TLS with shared timeout, dispatcher, and cancellation ownership.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class HttpTlsConnector {

    /**
     * TLS context.
     */
    private final TlsContext context;

    /**
     * TLS settings.
     */
    private final TlsSettings settings;

    /**
     * Lifecycle listener.
     */
    private final Listener<Object> listener;

    /**
     * Runtime dispatcher.
     */
    private final Dispatcher dispatcher;

    /**
     * Creates a TLS connector.
     *
     * @param context    TLS context
     * @param settings   TLS settings
     * @param listener   lifecycle listener
     * @param dispatcher runtime dispatcher
     */
    HttpTlsConnector(final TlsContext context, final TlsSettings settings, final Listener<Object> listener,
            final Dispatcher dispatcher) {
        this.context = context;
        this.settings = settings;
        this.listener = listener;
        this.dispatcher = dispatcher;
    }

    /**
     * Upgrades a conduit-based connection.
     *
     * @param raw          connected transport
     * @param target       TLS peer
     * @param timeout      timeout policy
     * @param cancellation cancellation scope
     * @return negotiated channel
     */
    ChannelUpgrade channel(
            final Connection raw,
            final Address target,
            final Timeout timeout,
            final Cancellation cancellation) {
        cancellation.throwIfCancelled();
        final TlsEngine engine = TlsEngine.create(context, target, settings);
        final TlsChannel channel = TlsChannel.wrap(raw.conduit(), engine, listener, dispatcher, timeout);
        final Runnable unregister = cancellation.onCancel(channel::close);
        try {
            final TlsHandshake handshake = await(channel.handshake(), timeout, cancellation);
            return new ChannelUpgrade(channel, handshake, protocol(engine.applicationProtocol()));
        } finally {
            unregister.run();
        }
    }

    /**
     * Upgrades a blocking socket.
     *
     * @param raw          connected socket
     * @param target       TLS peer
     * @param timeout      timeout policy
     * @param cancellation cancellation scope
     * @return negotiated socket channel
     */
    SocketUpgrade socket(
            final Socket raw,
            final Address target,
            final Timeout timeout,
            final Cancellation cancellation) {
        final TlsSocketChannel channel = TlsSocketChannel
                .wrap(context, raw, target, settings, timeout, dispatcher, cancellation);
        final Runnable unregister = cancellation.onCancel(channel::close);
        try {
            cancellation.throwIfCancelled();
            channel.handshakeSessionSynchronously();
            cancellation.throwIfCancelled();
            return new SocketUpgrade(channel, protocol(channel.applicationProtocol()));
        } finally {
            unregister.run();
        }
    }

    /**
     * Waits for the Dispatcher-owned handshake future.
     */
    private static TlsHandshake await(
            final CompletableFuture<TlsHandshake> future,
            final Timeout timeout,
            final Cancellation cancellation) {
        final Runnable unregister = cancellation.onCancel(() -> future.cancel(true));
        try {
            return timeout.connect().isZero() ? future.get()
                    : future.get(timeout.connect().toNanos(), TimeUnit.NANOSECONDS);
        } catch (final java.util.concurrent.TimeoutException e) {
            future.cancel(true);
            throw new TimeoutException("TLS handshake timed out", e);
        } catch (final InterruptedException e) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            throw new SocketException("TLS handshake interrupted", e);
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            throw cause instanceof RuntimeException runtime ? runtime
                    : new SocketException("TLS handshake failed", cause);
        } finally {
            unregister.run();
        }
    }

    /**
     * Maps negotiated ALPN to the supported HTTP protocol.
     */
    private static Protocol protocol(final String value) {
        if (value == null || value.isBlank() || Protocol.HTTP_1_1.name.equalsIgnoreCase(value)) {
            return Protocol.HTTP_1_1;
        }
        if (Protocol.HTTP_2.name.equalsIgnoreCase(value)) {
            return Protocol.HTTP_2;
        }
        throw new ProtocolException("Unsupported negotiated application protocol: " + value);
    }

    /**
     * Conduit TLS result.
     *
     * @param channel   TLS channel
     * @param handshake handshake metadata
     * @param protocol  negotiated HTTP protocol
     */
    record ChannelUpgrade(TlsChannel channel, TlsHandshake handshake, Protocol protocol) {
    }

    /**
     * Socket TLS result.
     *
     * @param channel  TLS socket channel
     * @param protocol negotiated HTTP protocol
     */
    record SocketUpgrade(TlsSocketChannel channel, Protocol protocol) {
    }

}
