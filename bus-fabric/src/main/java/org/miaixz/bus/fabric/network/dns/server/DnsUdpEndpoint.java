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
package org.miaixz.bus.fabric.network.dns.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.core.Lifecycle;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.fabric.network.dns.message.DnsCodec;

/**
 * DNS-over-UDP endpoint backed by dedicated datagram receive loops.
 *
 * @author Kimi Liu
 */
public final class DnsUdpEndpoint implements AutoCloseable, Lifecycle {

    /**
     * Maximum bound UDP channels per endpoint.
     */
    private static final int MAX_CHANNELS = 16;

    /**
     * Thread-local send buffer reused by datagram loops.
     */
    private static final ThreadLocal<ByteBuffer> SEND_BUFFER = ThreadLocal
            .withInitial(() -> ByteBuffer.allocate(DnsCodec.MAX_MESSAGE_BYTES));

    /**
     * UDP endpoint definition.
     */
    private final DnsEndpoint endpoint;

    /**
     * DNS server startup options.
     */
    private final DnsServerOptions options;

    /**
     * DNS request handler supplied by the owning server.
     */
    private final QueryHandler handler;

    /**
     * Bound datagram channels.
     */
    private final List<DatagramChannel> channels;

    /**
     * Datagram loop threads.
     */
    private final List<Thread> threads;

    /**
     * Start guard.
     */
    private final AtomicBoolean started;

    /**
     * Close guard.
     */
    private final AtomicBoolean closed;

    /**
     * Creates a DNS UDP endpoint.
     *
     * @param endpoint UDP endpoint definition
     * @param options  DNS server startup options
     * @param handler  DNS request handler
     */
    public DnsUdpEndpoint(final DnsEndpoint endpoint, final DnsServerOptions options, final QueryHandler handler) {
        if (endpoint == null) {
            throw new ValidateException("DNS UDP endpoint must not be null");
        }
        if (endpoint.transport() != DnsTransport.UDP) {
            throw new ValidateException("DNS UDP endpoint requires UDP transport");
        }
        if (options == null) {
            throw new ValidateException("DNS UDP endpoint options must not be null");
        }
        if (handler == null) {
            throw new ValidateException("DNS UDP query handler must not be null");
        }
        this.endpoint = endpoint;
        this.options = options;
        this.handler = handler;
        this.channels = new ArrayList<>();
        this.threads = new ArrayList<>();
        this.started = new AtomicBoolean();
        this.closed = new AtomicBoolean();
    }

    /**
     * Starts all UDP datagram loops.
     *
     * @return this endpoint
     */
    public DnsUdpEndpoint start() {
        if (closed.get()) {
            throw new StatefulException("DNS UDP endpoint is closed");
        }
        if (!started.compareAndSet(false, true)) {
            throw new StatefulException("DNS UDP endpoint can only be started once");
        }
        try {
            for (int index = 0; index < channelCount(); index++) {
                final DatagramChannel channel = openChannel();
                final AtomicBoolean active = new AtomicBoolean(true);
                final Thread thread = ThreadKit.newThread(
                        () -> datagramLoop(active, channel),
                        "fabric-dns-udp-" + endpoint.host() + Symbol.MINUS + endpoint.port() + Symbol.MINUS + index,
                        true);
                channels.add(channel);
                threads.add(thread);
                thread.start();
            }
            return this;
        } catch (final RuntimeException | IOException e) {
            close();
            started.set(false);
            throw e instanceof RuntimeException runtime ? runtime
                    : new SocketException("Unable to start DNS UDP endpoint", e);
        }
    }

    /**
     * Closes all UDP channels and interrupts datagram loop threads.
     */
    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        for (final DatagramChannel channel : List.copyOf(channels)) {
            IoKit.closeQuietly(channel);
        }
        channels.clear();
        for (final Thread thread : List.copyOf(threads)) {
            thread.interrupt();
        }
        threads.clear();
    }

    /**
     * Returns the current endpoint lifecycle state.
     *
     * @return endpoint lifecycle state
     */
    @Override
    public State state() {
        if (closed.get()) {
            return State.CLOSED;
        }
        return started.get() ? State.RUNNING : State.NEW;
    }

    /**
     * Runs one dedicated UDP datagram loop.
     *
     * @param active  loop active flag
     * @param channel bound datagram channel
     */
    private void datagramLoop(final AtomicBoolean active, final DatagramChannel channel) {
        final ByteBuffer receiveBuffer = ByteBuffer.allocate(DnsCodec.MAX_MESSAGE_BYTES);
        while (active.get() && !closed.get()) {
            try {
                receiveBuffer.clear();
                final SocketAddress remote = channel.receive(receiveBuffer);
                if (remote == null) {
                    continue;
                }
                receiveBuffer.flip();
                final byte[] request = new byte[receiveBuffer.remaining()];
                receiveBuffer.get(request);
                final byte[] response = handler.handle(request, clientAddress(remote));
                if (response.length > 0) {
                    channel.send(sendBuffer(response), remote);
                }
            } catch (final IOException e) {
                if (!closed.get() && active.get()) {
                    throw new SocketException("DNS UDP endpoint failed", e);
                }
                return;
            } catch (final RuntimeException e) {
                if (closed.get() || !active.get()) {
                    return;
                }
            }
        }
    }

    /**
     * Opens and binds one datagram channel.
     *
     * @return bound datagram channel
     * @throws IOException if the channel cannot bind
     */
    private DatagramChannel openChannel() throws IOException {
        final DatagramChannel channel = DatagramChannel.open();
        try {
            channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            configureReusePort(channel);
            channel.bind(endpoint.socketAddress());
            return channel;
        } catch (final IOException | RuntimeException e) {
            IoKit.closeQuietly(channel);
            throw e;
        }
    }

    /**
     * Configures SO_REUSEPORT when available and requires it on Linux.
     *
     * @param channel datagram channel
     * @throws IOException if the option write fails
     */
    private static void configureReusePort(final DatagramChannel channel) throws IOException {
        if (channel.supportedOptions().contains(StandardSocketOptions.SO_REUSEPORT)) {
            channel.setOption(StandardSocketOptions.SO_REUSEPORT, true);
            return;
        }
        if (linux()) {
            throw new SocketException("DNS UDP endpoint requires SO_REUSEPORT on Linux");
        }
    }

    /**
     * Returns the number of channels bound by this endpoint.
     *
     * @return channel count
     */
    private int channelCount() {
        return Math.min(options.ioThreads(), MAX_CHANNELS);
    }

    /**
     * Prepares the thread-local send buffer for one response.
     *
     * @param response response bytes
     * @return send buffer ready for writing
     */
    private static ByteBuffer sendBuffer(final byte[] response) {
        final ByteBuffer buffer = SEND_BUFFER.get();
        buffer.clear();
        buffer.put(response);
        buffer.flip();
        return buffer;
    }

    /**
     * Extracts a client address from a socket address.
     *
     * @param remote remote socket address
     * @return client address, or {@code null} when unavailable
     */
    private static InetAddress clientAddress(final SocketAddress remote) {
        return remote instanceof InetSocketAddress address ? address.getAddress() : null;
    }

    /**
     * Returns whether the process is running on Linux.
     *
     * @return true on Linux
     */
    private static boolean linux() {
        return System.getProperty("os.name", Normal.EMPTY).toLowerCase(Locale.ROOT).contains("linux");
    }

    /**
     * DNS UDP query handler.
     *
     * @author Kimi Liu
     */
    public interface QueryHandler {

        /**
         * Handles one decoded UDP datagram payload.
         *
         * @param request       DNS request bytes
         * @param clientAddress client address, or {@code null} when unavailable
         * @return DNS response bytes
         */
        byte[] handle(byte[] request, InetAddress clientAddress);

    }

}
