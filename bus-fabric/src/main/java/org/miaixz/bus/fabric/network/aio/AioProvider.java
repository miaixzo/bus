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
package org.miaixz.bus.fabric.network.aio;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.Wiring;
import org.miaixz.bus.fabric.network.tcp.TcpServer;
import org.miaixz.bus.fabric.protocol.socket.SocketOptions;

/**
 * Provider contract for creating AIO channels and servers.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface AioProvider {

    /**
     * Returns the system provider.
     *
     * @return system provider
     */
    static AioProvider system() {
        return SystemAioProvider.instance();
    }

    /**
     * Opens a client channel.
     *
     * @param group group
     * @return AIO channel
     */
    AioChannel openChannel(AioGroup group);

    /**
     * Opens a client channel with socket options.
     *
     * @param group   group
     * @param options socket options
     * @return AIO channel
     */
    default AioChannel openChannel(final AioGroup group, final SocketOptions options) {
        return openChannel(group);
    }

    /**
     * Opens a server.
     *
     * @param address  address
     * @param group    group
     * @param listener lifecycle listener
     * @return TCP server
     */
    TcpServer openServer(Address address, AioGroup group, Listener<Object> listener);

    /**
     * Opens a server with socket options.
     *
     * @param address  address
     * @param group    group
     * @param listener lifecycle listener
     * @param options  socket options
     * @return TCP server
     */
    default TcpServer openServer(
            final Address address,
            final AioGroup group,
            final Listener<Object> listener,
            final SocketOptions options) {
        return openServer(address, group, listener);
    }

    /**
     * Opens a server with no-op lifecycle listener.
     *
     * @param address address
     * @param group   group
     * @return TCP server
     */
    default TcpServer openServer(final Address address, final AioGroup group) {
        return openServer(address, group, Wiring.noop());
    }

}

/**
 * System provider implementation.
 */
final class SystemAioProvider implements AioProvider {

    /**
     * Creates a system provider.
     */
    private SystemAioProvider() {
        // No initialization required.
    }

    /**
     * Returns the shared system provider.
     *
     * @return system provider
     */
    static AioProvider instance() {
        return Instances.get(SystemAioProvider.class.getName(), SystemAioProvider::new);
    }

    /**
     * Opens a client channel.
     *
     * @param group group
     * @return AIO channel
     */
    @Override
    public AioChannel openChannel(final AioGroup group) {
        return openChannel(group, SocketOptions.defaults());
    }

    /**
     * Opens a client channel.
     *
     * @param group   group
     * @param options socket options
     * @return AIO channel
     */
    @Override
    public AioChannel openChannel(final AioGroup group, final SocketOptions options) {
        if (group == null) {
            throw new ValidateException("AIO group must not be null");
        }
        try {
            return new AioChannel(AsynchronousSocketChannel.open(group.channelGroup), group.dispatcher(),
                    options == null ? SocketOptions.defaults() : options);
        } catch (final IOException e) {
            throw new SocketException("Unable to open AIO channel", e);
        }
    }

    /**
     * Opens a server.
     *
     * @param address  address
     * @param group    group
     * @param listener lifecycle listener
     * @return TCP server
     */
    @Override
    public TcpServer openServer(final Address address, final AioGroup group, final Listener<Object> listener) {
        return openServer(address, group, listener, SocketOptions.defaults());
    }

    /**
     * Opens a server.
     *
     * @param address  address
     * @param group    group
     * @param listener lifecycle listener
     * @param options  socket options
     * @return TCP server
     */
    @Override
    public TcpServer openServer(
            final Address address,
            final AioGroup group,
            final Listener<Object> listener,
            final SocketOptions options) {
        if (address == null) {
            throw new ValidateException("Server address must not be null");
        }
        if (group == null) {
            throw new ValidateException("AIO group must not be null");
        }
        try {
            return new TcpServer(address, listener, group.dispatcher(),
                    options == null ? SocketOptions.defaults() : options);
        } catch (final RuntimeException e) {
            throw new InternalException("Unable to open TCP server", e);
        }
    }

}
