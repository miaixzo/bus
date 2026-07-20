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
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.EnumSet;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.NetKit;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.network.Transport;
import org.miaixz.bus.fabric.network.aio.AioGroup;

/**
 * UDP network entry point backed by datagram channels.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class UdpNetwork implements AutoCloseable {

    /**
     * Supported transports.
     */
    private static final EnumSet<Transport> SUPPORTED = EnumSet.of(Transport.UDP);

    /**
     * Worker group.
     */
    private final AioGroup group;

    /**
     * Managed channels.
     */
    private final ConcurrentLinkedDeque<UdpChannel> channels;

    /**
     * Close flag.
     */
    private final AtomicBoolean closed;

    /**
     * Lifecycle listener.
     */
    private final Listener<Object> listener;

    /**
     * Creates a UDP network.
     *
     * @param group    group
     * @param listener lifecycle listener
     */
    private UdpNetwork(final AioGroup group, final Listener<Object> listener) {
        this.group = Assert.notNull(group, () -> new ValidateException("AIO group must not be null"));
        this.channels = new ConcurrentLinkedDeque<>();
        this.closed = new AtomicBoolean();
        this.listener = listener;
    }

    /**
     * Creates a UDP network.
     *
     * @param group group
     * @return UDP network
     */
    public static UdpNetwork create(final AioGroup group) {
        return new UdpNetwork(group, null);
    }

    /**
     * Creates a UDP network with a lifecycle listener.
     *
     * @param group    group
     * @param listener lifecycle listener
     * @return UDP network
     */
    public static UdpNetwork create(final AioGroup group, final Listener<Object> listener) {
        return new UdpNetwork(group, listener);
    }

    /**
     * Binds a UDP channel.
     *
     * @param address local address
     * @return UDP channel
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
     * Connects to a remote UDP address.
     *
     * @param remote remote address
     * @return UDP session
     */
    public synchronized UdpSession connect(final Address remote) {
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
            return new UdpSession(checkedRemote, channel, listener, group.dispatcher(), () -> channels.remove(channel));
        } catch (final IOException e) {
            if (listener != null) {
                listener.failure(this, e);
            }
            throw new SocketException("Unable to create UDP session", e);
        }
    }

    /**
     * Returns whether this network supports a transport.
     *
     * @param transport transport
     * @return true when supported
     */
    public boolean supports(final Transport transport) {
        return SUPPORTED.contains(Assert.notNull(transport, () -> new ValidateException("Transport must not be null")));
    }

    /**
     * Returns managed channel count.
     *
     * @return managed channels
     */
    public int channelCount() {
        channels.removeIf(channel -> !channel.opened());
        return channels.size();
    }

    /**
     * Closes all channels.
     */
    @Override
    public synchronized void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        RuntimeException failure = null;
        for (final UdpChannel channel : channels) {
            try {
                channel.close();
            } catch (final RuntimeException e) {
                if (failure == null) {
                    failure = e;
                } else if (failure != e) {
                    failure.addSuppressed(e);
                }
            }
        }
        channels.clear();
        if (failure != null) {
            throw failure;
        }
    }

    /**
     * Requires UDP transport.
     *
     * @param address address
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
     * @param address bind address
     * @return socket address
     */
    private static InetSocketAddress socket(final Address address) {
        return NetKit.createAddress(address.host(), address.port());
    }

    /**
     * Rejects channel creation after network closure.
     */
    private void ensureOpen() {
        if (closed.get()) {
            throw new StatefulException("UDP network is closed");
        }
    }

}
