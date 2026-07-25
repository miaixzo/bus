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
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Listener;
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
     * @param group open asynchronous channel group owning the client channel
     * @return newly opened asynchronous client channel wrapper
     */
    AioChannel openChannel(AioGroup group);

    /**
     * Opens a client channel with socket options.
     *
     * @param group   open asynchronous channel group owning the client channel
     * @param options socket options
     * @return newly opened asynchronous client channel wrapper
     */
    default AioChannel openChannel(final AioGroup group, final SocketOptions options) {
        return openChannel(group);
    }

    /**
     * Opens a server.
     *
     * @param address  local TCP listening address
     * @param group    open asynchronous group supplying the dispatcher
     * @param listener lifecycle listener
     * @return newly created TCP server bound to the group dispatcher
     */
    TcpServer openServer(Address address, AioGroup group, Listener<Object> listener);

    /**
     * Opens a server with socket options.
     *
     * @param address  local TCP listening address
     * @param group    open asynchronous group supplying the dispatcher
     * @param listener lifecycle listener
     * @param options  socket options
     * @return newly created TCP server using the supplied socket options
     */
    default TcpServer openServer(
            final Address address,
            final AioGroup group,
            final Listener<Object> listener,
            final SocketOptions options) {
        return openServer(address, group, listener);
    }

    /**
     * Opens a server with the default lifecycle listener.
     *
     * @param address local TCP listening address
     * @param group   open asynchronous group supplying the dispatcher
     * @return newly created TCP server without a lifecycle listener
     */
    default TcpServer openServer(final Address address, final AioGroup group) {
        return openServer(address, group, null);
    }

}

/**
 * System provider implementation.
 *
 * @author Kimi Liu
 * @since Java 21+
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
     * @param group open asynchronous channel group owning the client channel
     * @return newly opened channel using default socket options
     */
    @Override
    public AioChannel openChannel(final AioGroup group) {
        return openChannel(group, SocketOptions.defaults());
    }

    /**
     * Opens a client channel.
     *
     * @param group   open asynchronous channel group owning the client channel
     * @param options socket options
     * @return newly opened channel using supplied or default socket options
     */
    @Override
    public AioChannel openChannel(final AioGroup group, final SocketOptions options) {
        final AioGroup checkedGroup = Assert.notNull(group, () -> new ValidateException("AIO group must not be null"));
        ensureOpen(checkedGroup);
        try {
            return new AioChannel(AsynchronousSocketChannel.open(checkedGroup.channelGroup), checkedGroup.dispatcher(),
                    checkedGroup.scope(), options == null ? SocketOptions.defaults() : options);
        } catch (final IOException e) {
            throw new SocketException("Unable to open AIO channel", e);
        }
    }

    /**
     * Opens a server.
     *
     * @param address  local TCP listening address
     * @param group    open asynchronous group supplying the dispatcher
     * @param listener lifecycle listener
     * @return newly created TCP server using default socket options
     */
    @Override
    public TcpServer openServer(final Address address, final AioGroup group, final Listener<Object> listener) {
        return openServer(address, group, listener, SocketOptions.defaults());
    }

    /**
     * Opens a server.
     *
     * @param address  local TCP listening address
     * @param group    open asynchronous group supplying the dispatcher
     * @param listener lifecycle listener
     * @param options  socket options
     * @return newly created TCP server using supplied or default socket options
     */
    @Override
    public TcpServer openServer(
            final Address address,
            final AioGroup group,
            final Listener<Object> listener,
            final SocketOptions options) {
        final Address checkedAddress = Assert
                .notNull(address, () -> new ValidateException("Server address must not be null"));
        final AioGroup checkedGroup = Assert.notNull(group, () -> new ValidateException("AIO group must not be null"));
        ensureOpen(checkedGroup);
        try {
            return new TcpServer(checkedAddress, listener, checkedGroup.dispatcher(),
                    options == null ? SocketOptions.defaults() : options);
        } catch (final RuntimeException e) {
            throw new InternalException("Unable to open TCP server", e);
        }
    }

    /**
     * Rejects resource creation after the group starts closing.
     *
     * @param group channel group
     */
    private static void ensureOpen(final AioGroup group) {
        if (!group.opened()) {
            throw new StatefulException("AIO group is closed");
        }
    }

}
