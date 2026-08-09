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
package org.miaixz.bus.fabric.network.dns.secure;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.List;
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
import org.miaixz.bus.fabric.Timeout;
import org.miaixz.bus.fabric.network.Conduit;
import org.miaixz.bus.fabric.network.aio.AioChannel;
import org.miaixz.bus.fabric.network.aio.AioGroup;
import org.miaixz.bus.fabric.network.aio.AioProvider;
import org.miaixz.bus.fabric.network.aio.AioServer;
import org.miaixz.bus.fabric.network.dns.message.DnsCodec;
import org.miaixz.bus.fabric.network.dns.server.DnsEndpoint;
import org.miaixz.bus.fabric.network.dns.server.DnsServerOptions;
import org.miaixz.bus.fabric.network.dns.server.DnsTransport;
import org.miaixz.bus.fabric.network.tls.TlsChannel;
import org.miaixz.bus.fabric.network.tls.TlsEngine;
import org.miaixz.bus.fabric.network.tls.TlsPolicy;
import org.miaixz.bus.fabric.network.tls.TlsSettings;
import org.miaixz.bus.fabric.protocol.socket.SocketOptions;

/**
 * DNS-over-TLS endpoint backed by the shared AIO listener and Fabric TLS channel.
 *
 * @author Kimi Liu
 */
public final class DnsDotEndpoint implements AutoCloseable, Lifecycle {

    /**
     * DNS-over-TLS ALPN identifier.
     */
    public static final String ALPN = DnsTransport.DOT.alpn();

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
     * DoT endpoint definition.
     */
    private final DnsEndpoint endpoint;

    /**
     * DNS server startup options.
     */
    private final DnsServerOptions options;

    /**
     * Effective TLS policy whose ALPN list is fixed to {@link #ALPN}.
     */
    private final TlsPolicy tlsPolicy;

    /**
     * DNS request handler supplied by the owning server.
     */
    private final QueryHandler handler;

    /**
     * Active accepted raw AIO channels.
     */
    private final Set<AioChannel> rawChannels;

    /**
     * Active TLS channels.
     */
    private final Set<TlsChannel> tlsChannels;

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
     * Creates a DNS-over-TLS endpoint.
     *
     * @param endpoint DoT endpoint definition
     * @param options  DNS server startup options
     * @param handler  DNS request handler
     */
    public DnsDotEndpoint(final DnsEndpoint endpoint, final DnsServerOptions options, final QueryHandler handler) {
        if (endpoint == null) {
            throw new ValidateException("DNS-over-TLS endpoint must not be null");
        }
        if (endpoint.transport() != DnsTransport.DOT) {
            throw new ValidateException("DNS-over-TLS endpoint requires DOT transport");
        }
        if (options == null) {
            throw new ValidateException("DNS-over-TLS endpoint options must not be null");
        }
        if (options.tlsPolicy() == null) {
            throw new ValidateException("DNS-over-TLS endpoint requires a TLS policy");
        }
        if (handler == null) {
            throw new ValidateException("DNS-over-TLS query handler must not be null");
        }
        this.endpoint = endpoint;
        this.options = options;
        this.tlsPolicy = dotPolicy(options.tlsPolicy());
        this.handler = handler;
        this.rawChannels = ConcurrentHashMap.newKeySet();
        this.tlsChannels = ConcurrentHashMap.newKeySet();
        this.started = new java.util.concurrent.atomic.AtomicBoolean();
        this.closed = new java.util.concurrent.atomic.AtomicBoolean();
    }

    /**
     * Starts the DoT endpoint.
     *
     * @return this endpoint
     */
    public DnsDotEndpoint start() {
        if (closed.get()) {
            throw new StatefulException("DNS-over-TLS endpoint is closed");
        }
        if (!started.compareAndSet(false, true)) {
            throw new StatefulException("DNS-over-TLS endpoint can only be started once");
        }
        AioGroup openedGroup = null;
        AioServer openedServer = null;
        try {
            openedGroup = AioGroup.create(options.ioThreads());
            openedServer = AioProvider.system().openAsyncServer(bindAddress(), openedGroup, null, socketOptions())
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
     * Closes the DoT endpoint and every accepted channel.
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
        for (final TlsChannel channel : Set.copyOf(tlsChannels)) {
            closeQuietly(channel);
        }
        tlsChannels.clear();
        for (final AioChannel channel : Set.copyOf(rawChannels)) {
            closeQuietly(channel);
        }
        rawChannels.clear();
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
     * Accepts one AIO channel and starts its TLS connection loop.
     *
     * @param channel accepted AIO channel
     */
    private void accept(final AioChannel channel) {
        if (closed.get()) {
            closeQuietly(channel);
            return;
        }
        rawChannels.add(channel);
        final Thread thread = ThreadKit
                .newThread(() -> clientLoop(channel), "fabric-dns-dot-client-" + endpoint.port(), true);
        thread.start();
    }

    /**
     * Serves one accepted DNS-over-TLS client.
     *
     * @param rawChannel accepted raw AIO channel
     */
    private void clientLoop(final AioChannel rawChannel) {
        TlsChannel tlsChannel = null;
        try {
            final TlsEngine engine = TlsEngine
                    .createServer(tlsPolicy.context(), peerAddress(rawChannel), tlsPolicy.settings());
            tlsChannel = TlsChannel.wrap(rawChannel, engine, null, group.dispatcher(), timeout());
            tlsChannels.add(tlsChannel);
            await(tlsChannel.handshake());
            if (!ALPN.equals(engine.applicationProtocol())) {
                tlsChannel.abort();
                return;
            }
            serve(tlsChannel, rawChannel);
        } catch (final IOException | RuntimeException e) {
            // Connection failures terminate only the affected client channel.
        } finally {
            if (tlsChannel != null) {
                tlsChannels.remove(tlsChannel);
                closeQuietly(tlsChannel);
            }
            rawChannels.remove(rawChannel);
            closeQuietly(rawChannel);
        }
    }

    /**
     * Serves length-prefixed DNS messages over an established TLS channel.
     *
     * @param tlsChannel established TLS channel
     * @param rawChannel accepted raw AIO channel used for peer metadata
     * @throws IOException if DNS stream IO fails
     */
    private void serve(final TlsChannel tlsChannel, final AioChannel rawChannel) throws IOException {
        final PrefetchedInput input = new PrefetchedInput(tlsChannel);
        final InetAddress clientAddress = clientAddress(input, rawChannel);
        while (!closed.get() && tlsChannel.opened()) {
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
            writeFrame(tlsChannel, response);
        }
    }

    /**
     * Resolves the effective client address from a TLS plaintext PROXY header or raw peer address.
     *
     * @param input      TLS plaintext input
     * @param rawChannel accepted raw AIO channel
     * @return effective client IP address, or {@code null} when unavailable
     * @throws IOException if PROXY probing fails
     */
    private InetAddress clientAddress(final PrefetchedInput input, final AioChannel rawChannel) throws IOException {
        final Integer first = input.readByte();
        if (first == null) {
            return remoteAddress(rawChannel);
        }
        final Integer second = input.readByte();
        if (second == null) {
            input.unread(first);
            return remoteAddress(rawChannel);
        }
        if (first == 'P' && second == 'R') {
            return proxyV1Address(input, first, second, rawChannel);
        }
        if (first == 0x0d && second == 0x0a) {
            return proxyV2Address(input, first, second, rawChannel);
        }
        input.unread(second);
        input.unread(first);
        return remoteAddress(rawChannel);
    }

    /**
     * Reads a PROXY protocol v1 header when the TLS plaintext begins with {@code PR}.
     *
     * @param input      TLS plaintext input
     * @param first      first consumed byte
     * @param second     second consumed byte
     * @param rawChannel accepted raw AIO channel
     * @return proxy-provided client address, or the channel remote address when no v1 header is present
     * @throws IOException if the proxy header is malformed
     */
    private InetAddress proxyV1Address(
            final PrefetchedInput input,
            final int first,
            final int second,
            final AioChannel rawChannel) throws IOException {
        final byte[] prefix = input.readExact(3);
        if (prefix == null) {
            input.unread(second);
            input.unread(first);
            return remoteAddress(rawChannel);
        }
        if (prefix[0] != 'O' || prefix[1] != Symbol.C_X || prefix[2] != 'Y') {
            input.unread(prefix);
            input.unread(second);
            input.unread(first);
            return remoteAddress(rawChannel);
        }
        final StringBuilder line = new StringBuilder("PROXY");
        for (int count = 0; count < PROXY_V1_MAX_LINE_BYTES; count++) {
            final Integer value = input.readByte();
            if (value == null) {
                return remoteAddress(rawChannel);
            }
            if (value == Symbol.C_LF) {
                break;
            }
            if (value != Symbol.C_CR) {
                line.append((char) (value & 0xff));
            }
        }
        final String[] parts = line.toString().trim().split("\\s+");
        if (parts.length < 5 || (!"TCP4".equalsIgnoreCase(parts[1]) && !"TCP6".equalsIgnoreCase(parts[1]))) {
            return remoteAddress(rawChannel);
        }
        return InetAddress.getByName(parts[2]);
    }

    /**
     * Reads a PROXY protocol v2 header when the TLS plaintext begins with CRLF.
     *
     * @param input      TLS plaintext input
     * @param first      first consumed byte
     * @param second     second consumed byte
     * @param rawChannel accepted raw AIO channel
     * @return proxy-provided client address, or the channel remote address when no v2 header is present
     * @throws IOException if the proxy header is malformed
     */
    private InetAddress proxyV2Address(
            final PrefetchedInput input,
            final int first,
            final int second,
            final AioChannel rawChannel) throws IOException {
        final byte[] header = new byte[PROXY_V2_HEADER_BYTES];
        header[0] = (byte) first;
        header[1] = (byte) second;
        final byte[] rest = input.readExact(PROXY_V2_HEADER_BYTES - 2);
        if (rest == null) {
            input.unread(second);
            input.unread(first);
            return remoteAddress(rawChannel);
        }
        System.arraycopy(rest, 0, header, 2, rest.length);
        if (!proxyV2Signature(header)) {
            input.unread(header);
            return remoteAddress(rawChannel);
        }
        final int family = DnsCodec.readUnsignedByte(header, 13);
        final int length = DnsCodec.readUnsignedShort(header, 14);
        final byte[] payload = input.readExact(length);
        if (payload == null) {
            return remoteAddress(rawChannel);
        }
        if (family == 0x11 && payload.length >= 12) {
            return InetAddress.getByAddress(new byte[] { payload[0], payload[1], payload[2], payload[3] });
        }
        if (family == 0x21 && payload.length >= 36) {
            final byte[] address = new byte[16];
            System.arraycopy(payload, 0, address, 0, address.length);
            return InetAddress.getByAddress(address);
        }
        return remoteAddress(rawChannel);
    }

    /**
     * Compares a candidate v2 header with the fixed PROXY protocol signature.
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
     * Writes a DNS TCP-style response frame over TLS.
     *
     * @param conduit  established TLS channel
     * @param response DNS response wire bytes
     * @throws IOException if the channel write fails
     */
    private void writeFrame(final Conduit conduit, final byte[] response) throws IOException {
        final byte[] frame = new byte[response.length + DnsCodec.STREAM_LENGTH_PREFIX_BYTES];
        DnsCodec.writeUnsignedShort(frame, 0, response.length);
        System.arraycopy(response, 0, frame, DnsCodec.STREAM_LENGTH_PREFIX_BYTES, response.length);
        final Buffer output = new Buffer().write(frame);
        final Long written = await(conduit.write(output, frame.length));
        if (written == null || written != frame.length) {
            throw new IOException("DNS-over-TLS write ended before the frame completed");
        }
    }

    /**
     * Waits for an asynchronous operation with the configured idle timeout.
     *
     * @param future operation future
     * @param <T>    operation result type
     * @return operation result
     * @throws IOException if the operation fails, is interrupted, or times out
     */
    private <T> T await(final CompletableFuture<T> future) throws IOException {
        try {
            return future.get(timeoutMillis(), TimeUnit.MILLISECONDS);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("DNS-over-TLS operation was interrupted", e);
        } catch (final ExecutionException e) {
            throw new IOException("DNS-over-TLS operation failed", e.getCause());
        } catch (final java.util.concurrent.TimeoutException e) {
            throw new IOException("DNS-over-TLS operation timed out", e);
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
     * Creates TLS timeouts from DNS transport settings.
     *
     * @return timeout policy for TLS handshakes and plaintext IO
     */
    private Timeout timeout() {
        final Duration timeout = options.tcpIdleTimeout();
        return Timeout.of(timeout);
    }

    /**
     * Creates a Fabric address for the AIO listener.
     *
     * @return TCP bind address
     */
    private Address bindAddress() {
        return endpoint.fabricAddress(Protocol.TCP);
    }

    /**
     * Creates peer metadata for the TLS server engine.
     *
     * @param rawChannel accepted raw AIO channel
     * @return TLS peer address metadata
     */
    private Address peerAddress(final AioChannel rawChannel) {
        final SocketAddress remote = rawChannel.remote();
        if (remote instanceof InetSocketAddress address) {
            return new Address(Protocol.TLS.name, address.getHostString(), address.getPort(), Symbol.SLASH);
        }
        return endpoint.fabricAddress(Protocol.TLS);
    }

    /**
     * Creates AIO socket options from DNS DoT settings.
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
     * Creates a TLS policy whose ALPN list is fixed to DNS-over-TLS.
     *
     * @param policy configured TLS policy
     * @return policy with preserved TLS material and fixed DoT ALPN
     */
    private static TlsPolicy dotPolicy(final TlsPolicy policy) {
        final TlsSettings source = policy.settings();
        final TlsSettings.Builder builder = TlsSettings.builder().versions(source.versions())
                .clientAuth(source.clientAuthMode()).verifyHostname(source.verifyHostname())
                .certificate(source.certificate()).applicationProtocols(List.of(ALPN)).supportsTlsExtensions(true);
        if (source.ciphers().isEmpty()) {
            builder.allEnabledCipherSuites();
        } else {
            builder.ciphers(source.ciphers());
        }
        return TlsPolicy.of(policy.context(), builder.build());
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
     * DNS-over-TLS request handler.
     *
     */
    @FunctionalInterface
    public interface QueryHandler {

        /**
         * Handles one DNS-over-TLS message.
         *
         * @param request       DNS request wire bytes
         * @param clientAddress effective client address, or {@code null} when unavailable
         * @return DNS response wire bytes
         */
        byte[] handle(byte[] request, InetAddress clientAddress);

    }

    /**
     * TLS-backed input with a small unread buffer for PROXY protocol probing.
     *
     * @author Kimi Liu
     */
    private final class PrefetchedInput {

        /**
         * Source TLS plaintext conduit.
         */
        private final Conduit conduit;

        /**
         * Bytes returned to the front of the input stream.
         */
        private final ArrayDeque<Integer> prefetched;

        /**
         * Creates prefetched input for one TLS channel.
         *
         * @param conduit source TLS plaintext conduit
         */
        private PrefetchedInput(final Conduit conduit) {
            this.conduit = conduit;
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
                throw new ValidateException("DNS-over-TLS read count must not be negative");
            }
            final byte[] result = new byte[count];
            int offset = 0;
            while (offset < count) {
                if (!prefetched.isEmpty()) {
                    result[offset++] = (byte) (prefetched.removeFirst() & DnsCodec.UNSIGNED_BYTE_MAX);
                    continue;
                }
                final Buffer buffer = new Buffer();
                final Long read = await(conduit.read(buffer, count - offset));
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
