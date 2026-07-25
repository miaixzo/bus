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

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Handler;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.Timeout;
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
     * Transport set accepted by this connector and listener facade.
     */
    private static final EnumSet<Transport> SUPPORTED = EnumSet.of(Transport.TCP);

    /**
     * Owned AIO network that performs connection and server operations.
     */
    private final AioNetwork aio;

    /**
     * One-way flag ensuring the owned AIO network is closed once.
     */
    private final AtomicBoolean closed;

    /**
     * Optional listener forwarded to connection and server operations.
     */
    private final Listener<Object> listener;

    /**
     * Creates a TCP facade over an owned AIO network.
     *
     * @param aio      AIO network
     * @param listener lifecycle listener, or {@code null} when notifications are disabled
     */
    private TcpNetwork(final AioNetwork aio, final Listener<Object> listener) {
        this.aio = Assert.notNull(aio, () -> new ValidateException("AIO network must not be null"));
        this.closed = new AtomicBoolean();
        this.listener = listener;
    }

    /**
     * Creates a TCP network.
     *
     * @param aio AIO network whose ownership is transferred to the TCP facade
     * @return TCP network without a lifecycle listener
     * @throws ValidateException if {@code aio} is {@code null}
     */
    public static TcpNetwork create(final AioNetwork aio) {
        return new TcpNetwork(aio, null);
    }

    /**
     * Creates a TCP network with a lifecycle listener.
     *
     * @param aio      AIO network whose ownership is transferred to the TCP facade
     * @param listener lifecycle listener, or {@code null} to disable notifications
     * @return TCP network configured with the supplied listener
     * @throws ValidateException if {@code aio} is {@code null}
     */
    public static TcpNetwork create(final AioNetwork aio, final Listener<Object> listener) {
        return new TcpNetwork(aio, listener);
    }

    /**
     * Delegates an asynchronous TCP connection attempt to the owned AIO network.
     *
     * @param address remote address whose scheme must resolve to TCP
     * @param timeout timeout policy applied by the AIO connection attempt
     * @return future completed with the opened connection or its connection failure
     * @throws ValidateException if {@code address} or {@code timeout} is {@code null}
     * @throws ProtocolException if the address scheme does not resolve to TCP
     */
    @Override
    public CompletableFuture<Connection> connect(final Address address, final Timeout timeout) {
        final Address checkedAddress = Assert.notNull(address, () -> new ValidateException("Address must not be null"));
        final Timeout checkedTimeout = Assert.notNull(timeout, () -> new ValidateException("Timeout must not be null"));
        final Transport transport = Transport.fromScheme(checkedAddress.scheme());
        if (!supports(transport)) {
            throw new ProtocolException("TCP network does not support transport: " + transport);
        }
        return aio.connect(checkedAddress, checkedTimeout, listener);
    }

    /**
     * Opens a TCP server through the owned AIO network.
     *
     * @param address local address whose scheme must resolve to TCP
     * @param handler callback that handles accepted sessions
     * @return opened TCP server bound by the AIO network
     * @throws ValidateException if {@code address} or {@code handler} is {@code null}
     * @throws ProtocolException if the address scheme does not resolve to TCP
     */
    public TcpServer listen(final Address address, final Handler handler) {
        final Address checkedAddress = Assert
                .notNull(address, () -> new ValidateException("Listen address must not be null"));
        final Handler checkedHandler = Assert
                .notNull(handler, () -> new ValidateException("Listen handler must not be null"));
        final Transport transport = Transport.fromScheme(checkedAddress.scheme());
        if (!supports(transport)) {
            throw new ProtocolException("TCP listener does not support transport: " + transport);
        }
        return aio.server(checkedAddress, checkedHandler, listener);
    }

    /**
     * Returns whether a transport is supported.
     *
     * @param transport transport to test
     * @return {@code true} only for {@link Transport#TCP}
     * @throws ValidateException if {@code transport} is {@code null}
     */
    @Override
    public boolean supports(final Transport transport) {
        return SUPPORTED.contains(Assert.notNull(transport, () -> new ValidateException("Transport must not be null")));
    }

    /**
     * Closes the owned AIO network once.
     */
    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            aio.close();
        }
    }

}
