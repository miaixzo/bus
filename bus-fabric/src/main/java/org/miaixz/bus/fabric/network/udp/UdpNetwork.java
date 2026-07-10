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

import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.Wiring;
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
        if (group == null) {
            throw new ValidateException("AIO group must not be null");
        }
        this.group = group;
        this.channels = new ConcurrentLinkedDeque<>();
        this.closed = new AtomicBoolean();
        this.listener = Wiring.safe(listener == null ? Wiring.noop() : listener, null);
    }

    /**
     * Creates a UDP network.
     *
     * @param group group
     * @return UDP network
     */
    public static UdpNetwork create(final AioGroup group) {
        return new UdpNetwork(group, Wiring.noop());
    }

    /**
     * Creates a UDP network with a lifecycle listener.
     *
     * @param group    group
     * @param listener lifecycle listener
     * @return UDP network
     */
    public static UdpNetwork create(final AioGroup group, final Listener<Object> listener) {
        return new UdpNetwork(group, listener == null ? Wiring.noop() : listener);
    }

    /**
     * Binds a UDP channel.
     *
     * @param address local address
     * @return UDP channel
     */
    public UdpChannel bind(final Address address) {
        if (address == null) {
            throw new ValidateException("UDP bind address must not be null");
        }
        requireUdp(address);
        try {
            final DatagramChannel datagram = DatagramChannel.open();
            datagram.bind(socket(address));
            final UdpChannel channel = new UdpChannel(address, datagram, group.dispatcher());
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
    public UdpSession connect(final Address remote) {
        if (remote == null) {
            throw new ValidateException("UDP remote address must not be null");
        }
        requireUdp(remote);
        try {
            final DatagramChannel datagram = DatagramChannel.open();
            datagram.bind(null);
            final InetSocketAddress local = (InetSocketAddress) datagram.getLocalAddress();
            final Address localAddress = Address.parse("udp://127.0.0.1:" + local.getPort());
            final UdpChannel channel = new UdpChannel(localAddress, datagram, group.dispatcher());
            channels.add(channel);
            final UdpSession session = new UdpSession(remote, channel, listener, () -> channels.remove(channel));
            listener.open(session);
            return session;
        } catch (final IOException e) {
            listener.failure(this, e);
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
        if (transport == null) {
            throw new ValidateException("Transport must not be null");
        }
        return SUPPORTED.contains(transport);
    }

    /**
     * Returns managed channel count.
     *
     * @return managed channels
     */
    public int channelCount() {
        return channels.size();
    }

    /**
     * Closes all channels.
     */
    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            UdpChannel channel = channels.pollLast();
            while (channel != null) {
                channel.close();
                channel = channels.pollLast();
            }
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
        return new InetSocketAddress(address.host(), address.port());
    }

}
