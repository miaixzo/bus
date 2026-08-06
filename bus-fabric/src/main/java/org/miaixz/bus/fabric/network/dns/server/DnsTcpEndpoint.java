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
import java.util.ArrayDeque;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.core.Lifecycle;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.network.aio.AioChannel;
import org.miaixz.bus.fabric.network.aio.AioGroup;
import org.miaixz.bus.fabric.network.aio.AioProvider;
import org.miaixz.bus.fabric.network.aio.AioServer;
import org.miaixz.bus.fabric.network.dns.message.DnsCodec;
import org.miaixz.bus.fabric.protocol.socket.SocketOptions;

/**
 * DNS-over-TCP endpoint backed by the shared AIO server infrastructure.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class DnsTcpEndpoint implements AutoCloseable, Lifecycle {

    /**
     * PROXY protocol v1 maximum line length.
     */
    private static final int PROXY_V1_MAX_LINE_BYTES = 108;

    /**
     * PROXY protocol v2 fixed header length.
     */
    private static final int PROXY_V2_HEADER_BYTES = 16;

    /**
     * PROXY protocol v2 binary signature.
     */
    private static final byte[] PROXY_V2_SIGNATURE = new byte[] { 0x0d, 0x0a, 0x0d, 0x0a, 0x00, 0x0d, 0x0a, 0x51, 0x55,
            0x49, 0x54, 0x0a };

    /**
     * TCP endpoint definition.
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
     * Active accepted channels.
     */
    private final Set<AioChannel> channels;

    /**
     * Start guard.
     */
    private final java.util.concurrent.atomic.AtomicBoolean started;

    /**
     * Close guard.
     */
    private final java.util.concurrent.atomic.AtomicBoolean closed;

    /**
     * AIO channel group owned by this endpoint after startup.
     */
    private volatile AioGroup group;

    /**
     * AIO server owned by this endpoint after startup.
     */
    private volatile AioServer server;

    /**
     * Creates a DNS TCP endpoint.
     *
     * @param endpoint TCP endpoint definition
     * @param options  DNS server startup options
     * @param handler  DNS request handler
     */
    public DnsTcpEndpoint(final DnsEndpoint endpoint, final DnsServerOptions options, final QueryHandler handler) {
        if (endpoint == null) {
            throw new ValidateException("DNS TCP endpoint must not be null");
        }
        if (endpoint.transport() != DnsTransport.TCP) {
            throw new ValidateException("DNS TCP endpoint requires TCP transport");
        }
        if (options == null) {
            throw new ValidateException("DNS TCP endpoint options must not be null");
        }
        if (handler == null) {
            throw new ValidateException("DNS TCP query handler must not be null");
        }
        this.endpoint = endpoint;
        this.options = options;
        this.handler = handler;
        this.channels = ConcurrentHashMap.newKeySet();
        this.started = new java.util.concurrent.atomic.AtomicBoolean();
        this.closed = new java.util.concurrent.atomic.AtomicBoolean();
    }

    /**
     * Starts the TCP endpoint.
     *
     * @return this endpoint
     */
    public DnsTcpEndpoint start() {
        if (closed.get()) {
            throw new StatefulException("DNS TCP endpoint is closed");
        }
        if (!started.compareAndSet(false, true)) {
            throw new StatefulException("DNS TCP endpoint can only be started once");
        }
        AioGroup openedGroup = null;
        AioServer openedServer = null;
        try {
            openedGroup = AioGroup.create(options.ioThreads());
            openedServer = AioProvider.system().openAsyncServer(address(), openedGroup, null, socketOptions())
                    .start(this::accept);
            group = openedGroup;
            server = openedServer;
            return this;
        } catch (final RuntimeException e) {
            closeQuietly(openedServer);
            closeQuietly(openedGroup);
            started.set(false);
            throw e;
        }
    }

    /**
     * Closes the TCP endpoint and all accepted channels.
     */
    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        final AioServer currentServer = server;
        final AioGroup currentGroup = group;
        server = null;
        group = null;
        closeQuietly(currentServer);
        for (final AioChannel channel : Set.copyOf(channels)) {
            closeQuietly(channel);
        }
        channels.clear();
        closeQuietly(currentGroup);
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
     * Accepts one AIO channel and starts its connection loop.
     *
     * @param channel accepted AIO channel
     */
    private void accept(final AioChannel channel) {
        if (closed.get()) {
            closeQuietly(channel);
            return;
        }
        channels.add(channel);
        final Thread thread = ThreadKit
                .newThread(() -> clientLoop(channel), "fabric-dns-tcp-client-" + endpoint.port(), true);
        thread.start();
    }

    /**
     * Serves one accepted TCP client channel.
     *
     * @param channel accepted AIO channel
     */
    private void clientLoop(final AioChannel channel) {
        try {
            final PrefetchedInput input = new PrefetchedInput(channel);
            final InetAddress clientAddress = clientAddress(input);
            while (!closed.get() && channel.opened()) {
                final byte[] prefix = input.readExact(DnsCodec.STREAM_LENGTH_PREFIX_BYTES);
                if (prefix == null) {
                    return;
                }
                final int length = DnsCodec.readUnsignedShort(prefix, 0);
                if (length <= 0 || length > options.tcpMaxFrameBytes()) {
                    return;
                }
                final byte[] request = input.readExact(length);
                if (request == null) {
                    return;
                }
                final byte[] response = handler.handle(request, clientAddress);
                if (response.length > options.tcpMaxFrameBytes()) {
                    return;
                }
                writeFrame(channel, response);
            }
        } catch (final IOException | RuntimeException e) {
            // Connection failures terminate only the affected client channel.
        } finally {
            channels.remove(channel);
            closeQuietly(channel);
        }
    }

    /**
     * Resolves the effective TCP client address, consuming a leading PROXY protocol header when present.
     *
     * @param input prefetched channel input
     * @return effective client address
     * @throws IOException if a PROXY header is malformed or truncated
     */
    private InetAddress clientAddress(final PrefetchedInput input) throws IOException {
        final Integer first = input.readByte();
        if (first == null) {
            return remoteAddress(input.channel);
        }
        final Integer second = input.readByte();
        if (second == null) {
            input.unread(first);
            return remoteAddress(input.channel);
        }
        if (first == 'P' && second == 'R') {
            return proxyV1Address(input, first, second);
        }
        if (first == 0x0d && second == 0x0a) {
            return proxyV2Address(input, first, second);
        }
        input.unread(second);
        input.unread(first);
        return remoteAddress(input.channel);
    }

    /**
     * Reads a PROXY protocol v1 header when the stream begins with {@code PR}.
     *
     * @param input  prefetched channel input
     * @param first  first consumed byte
     * @param second second consumed byte
     * @return proxy-provided client address, or the channel remote address when no v1 header is present
     * @throws IOException if a v1 header is malformed or truncated
     */
    private InetAddress proxyV1Address(final PrefetchedInput input, final int first, final int second)
            throws IOException {
        final byte[] suffix = input.readExact(4);
        if (suffix == null) {
            input.unread(second);
            input.unread(first);
            return remoteAddress(input.channel);
        }
        final byte[] prefix = new byte[] { (byte) first, (byte) second, suffix[0], suffix[1], suffix[2], suffix[3] };
        if (prefix[2] != 'O' || prefix[3] != Symbol.C_X || prefix[4] != 'Y' || prefix[5] != Symbol.C_SPACE) {
            input.unread(prefix);
            return remoteAddress(input.channel);
        }
        final String line = readProxyV1Line(input);
        final String[] parts = line.split("\\s+");
        if (parts.length == 1 && "UNKNOWN".equalsIgnoreCase(parts[0])) {
            return remoteAddress(input.channel);
        }
        if (parts.length < 5 || (!"TCP4".equalsIgnoreCase(parts[0]) && !"TCP6".equalsIgnoreCase(parts[0]))) {
            throw new IOException("PROXY protocol v1 header is malformed");
        }
        return InetAddress.getByName(parts[1]);
    }

    /**
     * Reads the remaining PROXY protocol v1 line after {@code PROXY }.
     *
     * @param input prefetched channel input
     * @return ASCII line without CRLF
     * @throws IOException if the line is truncated or exceeds the maximum length
     */
    private String readProxyV1Line(final PrefetchedInput input) throws IOException {
        final StringBuilder line = new StringBuilder();
        for (int index = 0; index < PROXY_V1_MAX_LINE_BYTES; index++) {
            final Integer value = input.readByte();
            if (value == null) {
                throw new IOException("PROXY protocol v1 header is truncated");
            }
            if (value == Symbol.C_LF) {
                return line.toString().trim();
            }
            if (value != Symbol.C_CR) {
                line.append((char) value.intValue());
            }
        }
        throw new IOException("PROXY protocol v1 header is too long");
    }

    /**
     * Reads a PROXY protocol v2 header when the stream begins with CRLF.
     *
     * @param input  prefetched channel input
     * @param first  first consumed byte
     * @param second second consumed byte
     * @return proxy-provided client address, or the channel remote address when no v2 header is present
     * @throws IOException if a v2 header is malformed or truncated
     */
    private InetAddress proxyV2Address(final PrefetchedInput input, final int first, final int second)
            throws IOException {
        final byte[] rest = input.readExact(PROXY_V2_HEADER_BYTES - 2);
        if (rest == null) {
            input.unread(second);
            input.unread(first);
            return remoteAddress(input.channel);
        }
        final byte[] header = new byte[PROXY_V2_HEADER_BYTES];
        header[0] = (byte) first;
        header[1] = (byte) second;
        System.arraycopy(rest, 0, header, 2, rest.length);
        if (!proxyV2Signature(header)) {
            input.unread(header);
            return remoteAddress(input.channel);
        }
        final int versionAndCommand = DnsCodec.readUnsignedByte(header, 12);
        final int familyAndProtocol = DnsCodec.readUnsignedByte(header, 13);
        final int length = DnsCodec.readUnsignedShort(header, 14);
        final byte[] payload = input.readExact(length);
        if (payload == null) {
            throw new IOException("PROXY protocol v2 header is truncated");
        }
        if ((versionAndCommand >>> 4) != 2 || (versionAndCommand & 0x0f) == 0) {
            return remoteAddress(input.channel);
        }
        if (familyAndProtocol == 0x11 && length >= 12) {
            return InetAddress.getByAddress(new byte[] { payload[0], payload[1], payload[2], payload[3] });
        }
        if (familyAndProtocol == 0x21 && length >= 36) {
            final byte[] address = new byte[16];
            System.arraycopy(payload, 0, address, 0, address.length);
            return InetAddress.getByAddress(address);
        }
        return remoteAddress(input.channel);
    }

    /**
     * Returns whether a candidate header contains the PROXY protocol v2 signature.
     *
     * @param header candidate v2 header
     * @return true when the signature matches
     */
    private static boolean proxyV2Signature(final byte[] header) {
        for (int index = 0; index < PROXY_V2_SIGNATURE.length; index++) {
            if (header[index] != PROXY_V2_SIGNATURE[index]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Writes a DNS TCP response frame.
     *
     * @param channel  accepted AIO channel
     * @param response DNS response wire bytes
     * @throws IOException if the channel write fails
     */
    private void writeFrame(final AioChannel channel, final byte[] response) throws IOException {
        final byte[] frame = new byte[response.length + DnsCodec.STREAM_LENGTH_PREFIX_BYTES];
        DnsCodec.writeUnsignedShort(frame, 0, response.length);
        System.arraycopy(response, 0, frame, DnsCodec.STREAM_LENGTH_PREFIX_BYTES, response.length);
        final Buffer output = new Buffer().write(frame);
        final Long written = await(channel.write(output, frame.length));
        if (written == null || written != frame.length) {
            throw new IOException("DNS TCP write ended before the frame completed");
        }
    }

    /**
     * Waits for an AIO operation with the configured idle timeout.
     *
     * @param future operation future
     * @return operation result
     * @throws IOException if the operation fails, is interrupted, or times out
     */
    private Long await(final CompletableFuture<Long> future) throws IOException {
        try {
            return future.get(timeoutMillis(), TimeUnit.MILLISECONDS);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("DNS TCP operation was interrupted", e);
        } catch (final ExecutionException e) {
            throw new IOException("DNS TCP operation failed", e.getCause());
        } catch (final java.util.concurrent.TimeoutException e) {
            throw new IOException("DNS TCP operation timed out", e);
        }
    }

    /**
     * Returns the configured idle timeout in milliseconds.
     *
     * @return positive idle timeout milliseconds
     */
    private long timeoutMillis() {
        return Math.max(1L, options.tcpIdleTimeout().toMillis());
    }

    /**
     * Creates a Fabric address for the AIO server.
     *
     * @return TCP address
     */
    private Address address() {
        return endpoint.fabricAddress(Protocol.TCP);
    }

    /**
     * Creates AIO socket options from DNS TCP settings.
     *
     * @return socket options
     */
    private SocketOptions socketOptions() {
        return SocketOptions.builder().ioThreads(options.ioThreads())
                .readBufferSize(Math.min(options.tcpMaxFrameBytes(), 8192))
                .writeChunkSize(Math.min(options.tcpMaxFrameBytes(), 8192)).idleTimeout(options.tcpIdleTimeout())
                .build();
    }

    /**
     * Returns the peer address of an accepted channel.
     *
     * @param channel accepted AIO channel
     * @return peer IP address, or {@code null} when unavailable
     */
    private static InetAddress remoteAddress(final AioChannel channel) {
        final SocketAddress remote = channel.remote();
        return remote instanceof InetSocketAddress address ? address.getAddress() : null;
    }

    /**
     * Closes a resource while preserving the caller's primary failure.
     *
     * @param closeable resource to close
     */
    private static void closeQuietly(final AutoCloseable closeable) {
        IoKit.closeQuietly(closeable);
    }

    /**
     * DNS TCP request handler.
     *
     * @since Java 21+
     */
    @FunctionalInterface
    public interface QueryHandler {

        /**
         * Handles one DNS TCP message.
         *
         * @param request       DNS request wire bytes
         * @param clientAddress effective client address, or {@code null} when unavailable
         * @return DNS response wire bytes
         */
        byte[] handle(byte[] request, InetAddress clientAddress);

    }

    /**
     * AIO-backed input with a small unread buffer for PROXY protocol probing.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private final class PrefetchedInput {

        /**
         * Source AIO channel.
         */
        private final AioChannel channel;

        /**
         * Bytes returned to the front of the input stream.
         */
        private final ArrayDeque<Integer> prefetched;

        /**
         * Creates prefetched input for one accepted channel.
         *
         * @param channel source AIO channel
         */
        private PrefetchedInput(final AioChannel channel) {
            this.channel = channel;
            this.prefetched = new ArrayDeque<>();
        }

        /**
         * Reads one unsigned byte.
         *
         * @return unsigned byte value, or {@code null} when the channel reached EOF
         * @throws IOException if the read fails
         */
        private Integer readByte() throws IOException {
            final byte[] bytes = readExact(1);
            return bytes == null ? null : DnsCodec.readUnsignedByte(bytes, 0);
        }

        /**
         * Reads exactly the requested number of bytes.
         *
         * @param count byte count to read
         * @return exact byte array, or {@code null} when the channel reached EOF
         * @throws IOException if the read fails
         */
        private byte[] readExact(final int count) throws IOException {
            if (count < 0) {
                throw new ValidateException("DNS TCP read count must not be negative");
            }
            final byte[] result = new byte[count];
            int offset = 0;
            while (offset < count) {
                if (!prefetched.isEmpty()) {
                    result[offset++] = (byte) (prefetched.removeFirst() & DnsCodec.UNSIGNED_BYTE_MAX);
                    continue;
                }
                final Buffer buffer = new Buffer();
                final Long read = await(channel.read(buffer, count - offset));
                if (read == null || read < 0L) {
                    return null;
                }
                if (read == 0L) {
                    continue;
                }
                final byte[] bytes = buffer.readByteArray(read);
                final int copied = Math.min(bytes.length, count - offset);
                System.arraycopy(bytes, 0, result, offset, copied);
                offset += copied;
                if (copied < bytes.length) {
                    unreadTail(bytes, copied);
                }
            }
            return result;
        }

        /**
         * Pushes one byte back to the front of the input.
         *
         * @param value unsigned byte value
         */
        private void unread(final int value) {
            prefetched.addFirst(value & DnsCodec.UNSIGNED_BYTE_MAX);
        }

        /**
         * Pushes bytes back to the front of the input while preserving their order.
         *
         * @param bytes bytes to unread
         */
        private void unread(final byte[] bytes) {
            for (int index = bytes.length - 1; index >= 0; index--) {
                unread(bytes[index]);
            }
        }

        /**
         * Pushes a suffix of a byte array back to the front of the input while preserving order.
         *
         * @param bytes source bytes
         * @param start first suffix byte index
         */
        private void unreadTail(final byte[] bytes, final int start) {
            for (int index = bytes.length - 1; index >= start; index--) {
                unread(bytes[index]);
            }
        }

    }

}
