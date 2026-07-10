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
package org.miaixz.bus.fabric.network.tcp;

import java.util.EnumSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Handler;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.Timeout;
import org.miaixz.bus.fabric.Wiring;
import org.miaixz.bus.fabric.network.Connection;
import org.miaixz.bus.fabric.network.Connector;
import org.miaixz.bus.fabric.network.Transport;
import org.miaixz.bus.fabric.network.aio.AioNetwork;

/**
 * TCP network entry point backed by the AIO network.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class TcpNetwork implements Connector {

    /**
     * Supported transports.
     */
    private static final EnumSet<Transport> SUPPORTED = EnumSet.of(Transport.TCP);

    /**
     * AIO network.
     */
    private final AioNetwork aio;

    /**
     * Close flag.
     */
    private final AtomicBoolean closed;

    /**
     * Lifecycle listener.
     */
    private final Listener<Object> listener;

    /**
     * Creates a TCP network.
     *
     * @param aio      AIO network
     * @param listener lifecycle listener
     */
    private TcpNetwork(final AioNetwork aio, final Listener<Object> listener) {
        if (aio == null) {
            throw new ValidateException("AIO network must not be null");
        }
        this.aio = aio;
        this.closed = new AtomicBoolean();
        this.listener = Wiring.safe(listener == null ? Wiring.noop() : listener, null);
    }

    /**
     * Creates a TCP network.
     *
     * @param aio AIO network
     * @return TCP network
     */
    public static TcpNetwork create(final AioNetwork aio) {
        return new TcpNetwork(aio, Wiring.noop());
    }

    /**
     * Creates a TCP network with a lifecycle listener.
     *
     * @param aio      AIO network
     * @param listener lifecycle listener
     * @return TCP network
     */
    public static TcpNetwork create(final AioNetwork aio, final Listener<Object> listener) {
        return new TcpNetwork(aio, listener == null ? Wiring.noop() : listener);
    }

    /**
     * Opens a TCP connection.
     *
     * @param address address
     * @param timeout timeout policy
     * @return connection future
     */
    @Override
    public CompletableFuture<Connection> connect(final Address address, final Timeout timeout) {
        if (address == null) {
            throw new ValidateException("Address must not be null");
        }
        if (timeout == null) {
            throw new ValidateException("Timeout must not be null");
        }
        final Transport transport = Transport.fromScheme(address.scheme());
        if (!supports(transport)) {
            throw new ProtocolException("TCP network does not support transport: " + transport);
        }
        return aio.connect(address, timeout, listener);
    }

    /**
     * Opens a TCP listener.
     *
     * @param address address
     * @param handler handler
     * @return TCP server
     */
    public TcpServer listen(final Address address, final Handler handler) {
        if (address == null) {
            throw new ValidateException("Listen address must not be null");
        }
        if (handler == null) {
            throw new ValidateException("Listen handler must not be null");
        }
        final Transport transport = Transport.fromScheme(address.scheme());
        if (!supports(transport)) {
            throw new ProtocolException("TCP listener does not support transport: " + transport);
        }
        return aio.server(address, handler, listener);
    }

    /**
     * Returns whether a transport is supported.
     *
     * @param transport transport
     * @return true when supported
     */
    @Override
    public boolean supports(final Transport transport) {
        if (transport == null) {
            throw new ValidateException("Transport must not be null");
        }
        return SUPPORTED.contains(transport);
    }

    /**
     * Closes this network.
     */
    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            aio.close();
        }
    }

}
