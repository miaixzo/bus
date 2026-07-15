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
package org.miaixz.bus.fabric.protocol.websocket;

import static org.miaixz.bus.fabric.Builder.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Context;
import org.miaixz.bus.fabric.Filter;
import org.miaixz.bus.fabric.Handler;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Lifecycle;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Session;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.guard.GuardRule;
import org.miaixz.bus.fabric.network.Ingress;
import org.miaixz.bus.fabric.network.proxy.ProxyHeader;
import org.miaixz.bus.fabric.network.proxy.ProxyHeaderReader;
import org.miaixz.bus.fabric.network.tcp.TcpServer;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.protocol.Demuxer;
import org.miaixz.bus.fabric.protocol.socket.SocketOptions;
import org.miaixz.bus.fabric.protocol.websocket.frame.WebSocketReader;
import org.miaixz.bus.fabric.protocol.websocket.frame.WebSocketWriter;
import org.miaixz.bus.fabric.protocol.websocket.upgrade.WebSocketUpgrade;
import org.miaixz.bus.fabric.runtime.Activity;
import org.miaixz.bus.fabric.runtime.FilterChain;
import org.miaixz.bus.fabric.runtime.dispatch.DispatchHandle;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;
import org.miaixz.bus.fabric.runtime.lifecycle.LifecycleScope;

/**
 * WebSocket server listener that upgrades accepted TCP channels into WebSocket sessions.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class WebSocketServer implements Lifecycle {

    /**
     * Shared context.
     */
    private final Context context;

    /**
     * Bind address.
     */
    private final Address address;

    /**
     * WebSocket path.
     */
    private final String path;

    /**
     * Socket options.
     */
    private final SocketOptions socketOptions;

    /**
     * Response headers.
     */
    private final Headers responseHeaders;

    /**
     * Ping interval.
     */
    private final Duration ping;

    /**
     * Message handler.
     */
    private final Handler handler;

    /**
     * Guard rule.
     */
    private final GuardRule guard;

    /**
     * Message filter.
     */
    private final Filter filter;

    /**
     * Event observer.
     */
    private final EventObserver observer;

    /**
     * Server lifecycle listener.
     */
    private final Listener<? super WebSocketServer> listener;

    /**
     * Session lifecycle listener.
     */
    private final Listener<? super WebSocketSession> sessionListener;

    /**
     * Dispatcher.
     */
    private final Dispatcher dispatcher;

    /**
     * TCP server.
     */
    private volatile TcpServer tcpServer;

    /**
     * Lifecycle.
     */
    private final LifecycleScope lifecycle;

    /**
     * Accepted sessions.
     */
    private final Queue<WebSocketSession> sessions;

    /**
     * Accepted dispatch handles.
     */
    private final Queue<DispatchHandle> handles;

    /**
     * Creates a server.
     *
     * @param builder builder
     */
    private WebSocketServer(final Builder builder) {
        this.context = require(builder.context, "Context");
        this.address = require(builder.address, "WebSocket bind address");
        this.path = builder.path;
        this.socketOptions = builder.socketOptions;
        this.responseHeaders = builder.responseHeaders;
        this.ping = builder.ping;
        this.handler = builder.handler();
        this.guard = builder.guard;
        this.filter = builder.filter;
        this.observer = EventObserver.safe(builder.observer);
        this.listener = builder.listener;
        this.sessionListener = builder.sessionListener();
        this.dispatcher = context.reactor().dispatcher();
        this.lifecycle = LifecycleScope.resource(this, "websocket-server", listener, observer);
        this.sessions = new ConcurrentLinkedQueue<>();
        this.handles = new ConcurrentLinkedQueue<>();
    }

    /**
     * Creates a builder.
     *
     * @param context context
     * @return builder
     */
    public static Builder builder(final Context context) {
        return new Builder(require(context, "Context"));
    }

    /**
     * Starts this server.
     *
     * @return this server
     */
    public WebSocketServer start() {
        if (running()) {
            return this;
        }
        if (lifecycle.state().terminal()) {
            throw new StatefulException("WebSocket server is already closed");
        }
        final TcpServer current = new TcpServer(address, tcpListener(), dispatcher, socketOptions);
        current.accept(acceptedHandler());
        current.start();
        tcpServer = current;
        lifecycle.open(this);
        return this;
    }

    /**
     * Executes this server.
     *
     * @return this server
     */
    public WebSocketServer execute() {
        return start();
    }

    /**
     * Closes this server.
     *
     * @return true when lifecycle changed
     */
    public boolean close() {
        cancelHandles();
        final TcpServer current = tcpServer;
        tcpServer = null;
        if (current != null) {
            current.close();
        }
        WebSocketSession session = sessions.poll();
        while (session != null) {
            session.close();
            session = sessions.poll();
        }
        return lifecycle.close(this);
    }

    /**
     * Cancels this server.
     *
     * @return true when lifecycle changed
     */
    public boolean cancel() {
        cancelHandles();
        final TcpServer current = tcpServer;
        tcpServer = null;
        if (current != null) {
            current.close();
        }
        WebSocketSession session = sessions.poll();
        while (session != null) {
            session.cancel();
            session = sessions.poll();
        }
        return lifecycle.cancel();
    }

    /**
     * Returns lifecycle state.
     *
     * @return state
     */
    @Override
    public Status state() {
        return lifecycle.state();
    }

    /**
     * Returns whether this server is running.
     *
     * @return true when running
     */
    @Override
    public boolean running() {
        final TcpServer current = tcpServer;
        return lifecycle.state() == Status.OPENED && current != null && current.running();
    }

    /**
     * Returns bind address.
     *
     * @return address
     */
    public Address address() {
        return address;
    }

    /**
     * Returns attributes.
     *
     * @return attributes
     */
    public Map<String, Object> attributes() {
        return Map.of(ATTRIBUTE_OBSERVER, observer, ATTRIBUTE_SOCKET_OPTIONS, socketOptions);
    }

    /**
     * Creates a TCP listener.
     *
     * @return listener
     */
    private Listener<Object> tcpListener() {
        return new Listener<>() {

            @Override
            public void failure(final Object source, final Throwable cause) {
                lifecycle.fail(cause);
            }
        };
    }

    /**
     * Creates accepted handler.
     *
     * @return handler
     */
    private Handler acceptedHandler() {
        return (accepted, message) -> {
            final Object tag = message.tag();
            if (!(tag instanceof SocketChannel channel)) {
                final ProtocolException cause = new ProtocolException("TCP accept message tag must be SocketChannel");
                accepted.close();
                lifecycle.fail(cause);
                handler.failure(accepted, cause);
                return;
            }
            track(
                    dispatcher.enqueue(
                            WEBSOCKET_ACTIVITY_ACCEPT,
                            Activity.of(WEBSOCKET_ACTIVITY_ACCEPT, () -> handleAccepted(accepted, channel))));
        };
    }

    /**
     * Handles one accepted socket as an ingress.
     *
     * @param accepted accepted TCP session
     * @param channel  accepted channel
     */
    private void handleAccepted(final Session accepted, final SocketChannel channel) {
        Ingress ingress = null;
        try {
            final ProxyHeaderReader.Result proxy = ProxyHeaderReader.read(channel);
            final UpgradeContext upgrade = parseUpgrade(channel, proxy);
            Message opening = Message.of(
                    upgrade.peerAddress().protocol(),
                    upgrade.peerAddress(),
                    upgrade.request().headers(),
                    Payload.empty(),
                    WEBSOCKET_OPEN);
            opening = FilterChain.apply(opening, context.filter(), filter);
            if (guard != null) {
                guard.check(opening).throwIfRejected();
            }
            final Headers response = mergeResponseHeaders(
                    WebSocketUpgrade.responseHeaders(upgrade.request().headers()));
            ingress = Ingress.of(upgrade.peerAddress(), channel, upgrade.request().payload());
            writeHandshakeResponse(ingress.sink(), HTTP.HTTP_SWITCHING_PROTOCOL, response, false);
            final Ingress currentIngress = ingress;
            final WebSocketSession session = new WebSocketSession(upgrade.peerAddress(),
                    new WebSocketWriter(currentIngress.sink(), WebSocketRole.SERVER.writerMask()),
                    new WebSocketReader(currentIngress.source(), WebSocketRole.SERVER.readerExpectMasked(),
                            upgrade.peerAddress()),
                    null, handler, dispatcher, dispatchKey(upgrade.peerAddress()), ping, guard, WebSocketRole.SERVER,
                    attributes(upgrade), () -> {
                        currentIngress.close();
                        accepted.close();
                    }, FilterChain.compose(context.filter(), filter), observer, sessionListener,
                    context.options().materializeMaxBytes());
            ingress = null;
            sessions.add(session);
        } catch (final RuntimeException e) {
            handler.failure(accepted, e);
            if (ingress != null) {
                ingress.close();
            }
            writeFailure(channel, e);
            accepted.close();
        }
    }

    /**
     * Parses an HTTP upgrade request.
     *
     * @param channel socket channel
     * @param proxy   proxy read result
     * @return upgrade context
     */
    private UpgradeContext parseUpgrade(final SocketChannel channel, final ProxyHeaderReader.Result proxy) {
        final UpgradeReader reader = new UpgradeReader(proxy.payload(), new HandshakeSource(channel));
        final int maxHeaderBytes = Math.toIntExact(Normal._16 * Normal.KIBI);
        final ByteArrayOutputStream headerBytes = new ByteArrayOutputStream(maxHeaderBytes);
        while (!endsHeaders(headerBytes)) {
            if (headerBytes.size() >= maxHeaderBytes) {
                throw new ProtocolException("WebSocket upgrade header is too large");
            }
            final int value = reader.read();
            if (value < Normal._0) {
                throw new ProtocolException("WebSocket upgrade request ended before headers");
            }
            headerBytes.write(value);
        }
        final String text = headerBytes.toString(Charset.ISO_8859_1);
        final String head = text.substring(Normal._0, text.length() - Normal._4);
        final String[] lines = head.split(Symbol.CR + Symbol.LF, Normal.__1);
        if (lines.length == Normal._0) {
            throw new ProtocolException("WebSocket upgrade request line is missing");
        }
        final String[] requestLine = lines[Normal._0].split(Symbol.SPACE, Normal._3);
        if (requestLine.length != Normal._3) {
            throw new ProtocolException("WebSocket upgrade request line is invalid");
        }
        final Headers headers = parseHeaders(lines);
        validateUpgrade(requestLine, headers);
        return new UpgradeContext(peerAddress(channel, proxy.header()), proxy.header(), new UpgradeRequest(
                requestLine[Normal._0], requestPath(requestLine[Normal._1]), headers, reader.remaining()));
    }

    /**
     * Parses headers from request lines.
     *
     * @param lines request lines
     * @return headers
     */
    private Headers parseHeaders(final String[] lines) {
        final Headers.Builder builder = Headers.builder();
        for (int i = Normal._1; i < lines.length; i++) {
            final String line = lines[i];
            final int index = line.indexOf(Symbol.C_COLON);
            if (index <= Normal._0) {
                throw new ProtocolException("WebSocket upgrade header is invalid");
            }
            builder.add(line.substring(Normal._0, index), line.substring(index + Normal._1).trim());
        }
        return builder.build();
    }

    /**
     * Validates an upgrade request.
     *
     * @param requestLine request line
     * @param headers     headers
     */
    private void validateUpgrade(final String[] requestLine, final Headers headers) {
        if (!HTTP.GET.equals(requestLine[Normal._0])) {
            throw new ProtocolException("WebSocket upgrade method must be GET");
        }
        if (!Protocol.HTTP_1_1.name.equals(requestLine[Normal._2])) {
            throw new ProtocolException("WebSocket upgrade version must be HTTP/1.1");
        }
        final String targetPath = requestPath(requestLine[Normal._1]);
        if (!path.equals(targetPath)) {
            throw new ProtocolException("WebSocket upgrade path does not match server path");
        }
        if (!HTTP.WEBSOCKET.equalsIgnoreCase(headers.get(HTTP.UPGRADE))) {
            throw new ProtocolException("WebSocket upgrade header is invalid");
        }
        if (!containsToken(headers.get(HTTP.CONNECTION), HTTP.UPGRADE)) {
            throw new ProtocolException("WebSocket connection header must contain Upgrade");
        }
        if (!HTTP.SEC_WEBSOCKET_VERSION_13.equals(headers.get(HTTP.SEC_WEBSOCKET_VERSION))) {
            throw new ProtocolException("WebSocket version must be 13");
        }
        WebSocketUpgrade.acceptKey(headers.get(HTTP.SEC_WEBSOCKET_KEY));
        final String protocol = responseHeaders.get(HTTP.SEC_WEBSOCKET_PROTOCOL);
        if (StringKit.isNotBlank(protocol) && !headers.values(HTTP.SEC_WEBSOCKET_PROTOCOL).contains(protocol)) {
            throw new ProtocolException("WebSocket subprotocol was not requested");
        }
    }

    /**
     * Returns request path without query.
     *
     * @param target request target
     * @return path
     */
    private String requestPath(final String target) {
        if (StringKit.isBlank(target) || StringKit.containsAny(target, Symbol.C_CR, Symbol.C_LF)
                || !target.startsWith(Symbol.SLASH)) {
            throw new ProtocolException("WebSocket upgrade target must be origin-form");
        }
        final int query = target.indexOf(Symbol.C_QUESTION_MARK);
        return query < Normal._0 ? target : target.substring(Normal._0, query);
    }

    /**
     * Merges response headers.
     *
     * @param base base headers
     * @return merged headers
     */
    private Headers mergeResponseHeaders(final Headers base) {
        Headers merged = base;
        for (final Map.Entry<String, List<String>> entry : responseHeaders.asMap().entrySet()) {
            for (final String value : entry.getValue()) {
                merged = merged.with(entry.getKey(), value);
            }
        }
        return merged;
    }

    /**
     * Writes failure response.
     *
     * @param channel channel
     * @param cause   cause
     */
    private void writeFailure(final SocketChannel channel, final RuntimeException cause) {
        final int status = cause instanceof StatefulException || cause instanceof ValidateException
                ? HTTP.HTTP_FORBIDDEN
                : HTTP.HTTP_BAD_REQUEST;
        writeHandshakeResponse(new HandshakeSink(channel), status, Headers.empty(), true);
    }

    /**
     * Writes a handshake response.
     *
     * @param sink    sink
     * @param status  HTTP status
     * @param headers headers
     * @param close   close sink after write
     */
    private void writeHandshakeResponse(final Sink sink, final int status, final Headers headers, final boolean close) {
        final Sink output = require(sink, "WebSocket handshake sink");
        Headers current = headers;
        if (close || status != HTTP.HTTP_SWITCHING_PROTOCOL) {
            current = current.with(HTTP.CONNECTION, HTTP.CONNECTION_CLOSE)
                    .with(HTTP.CONTENT_LENGTH, Long.toString(Normal.LONG_ZERO));
        }
        final StringBuilder response = new StringBuilder(128);
        response.append(Protocol.HTTP_1_1.name).append(Symbol.C_SPACE).append(status).append(Symbol.C_SPACE)
                .append(reason(status)).append(Symbol.CR).append(Symbol.LF);
        for (final Map.Entry<String, List<String>> entry : current.asMap().entrySet()) {
            for (final String value : entry.getValue()) {
                response.append(entry.getKey()).append(Symbol.C_COLON).append(Symbol.C_SPACE).append(value)
                        .append(Symbol.CR).append(Symbol.LF);
            }
        }
        response.append(Symbol.CR).append(Symbol.LF);
        final Buffer buffer = new Buffer().write(response.toString().getBytes(Charset.ISO_8859_1));
        try {
            output.write(buffer, buffer.size());
        } catch (final IOException e) {
            throw new SocketException("Unable to write WebSocket handshake response", e);
        } finally {
            if (close) {
                try {
                    output.close();
                } catch (final IOException e) {
                    throw new SocketException("Unable to close rejected WebSocket channel", e);
                }
            }
        }
    }

    /**
     * Returns reason phrase.
     *
     * @param status status
     * @return reason
     */
    private String reason(final int status) {
        return switch (status) {
            case HTTP.HTTP_SWITCHING_PROTOCOL -> "Switching Protocols";
            case HTTP.HTTP_BAD_REQUEST -> "Bad Request";
            case HTTP.HTTP_FORBIDDEN -> "Forbidden";
            default -> "Unknown";
        };
    }

    /**
     * Tracks a dispatch handle.
     *
     * @param handle handle
     */
    private void track(final DispatchHandle handle) {
        handles.add(handle);
        handle.future().whenComplete((ignored, cause) -> handles.remove(handle));
    }

    /**
     * Cancels tracked handles.
     */
    private void cancelHandles() {
        DispatchHandle handle = handles.poll();
        while (handle != null) {
            handle.cancel();
            handle = handles.poll();
        }
    }

    /**
     * Returns session attributes.
     *
     * @param upgrade upgrade context
     * @return attributes
     */
    private Map<String, Object> attributes(final UpgradeContext upgrade) {
        final LinkedHashMap<String, Object> values = new LinkedHashMap<>();
        values.put(ATTRIBUTE_HEADERS, upgrade.request().headers());
        values.put(ATTRIBUTE_OBSERVER, observer);
        values.put(ATTRIBUTE_SOCKET_OPTIONS, socketOptions);
        if (upgrade.proxyHeader() != null) {
            values.put(ATTRIBUTE_PROXY_HEADER, upgrade.proxyHeader());
        }
        return Map.copyOf(values);
    }

    /**
     * Resolves peer address.
     *
     * @param channel     channel
     * @param proxyHeader proxy header
     * @return peer address
     */
    private Address peerAddress(final SocketChannel channel, final ProxyHeader proxyHeader) {
        if (proxyHeader != null && proxyHeader.sourceAddress() != null) {
            return proxyHeader.sourceAddress();
        }
        try {
            final SocketAddress remote = channel.getRemoteAddress();
            if (!(remote instanceof InetSocketAddress socket)) {
                throw new ProtocolException("Accepted socket remote address must be InetSocketAddress");
            }
            return new Address(address.scheme(), socket.getHostString(), socket.getPort(), Symbol.SLASH);
        } catch (final IOException e) {
            throw new ProtocolException("Unable to resolve accepted WebSocket peer address", e);
        }
    }

    /**
     * Builds a dispatch key.
     *
     * @param peerAddress peer address
     * @return dispatch key
     */
    private String dispatchKey(final Address peerAddress) {
        return peerAddress.scheme() + Symbol.COLON + Symbol.FORWARDSLASH + peerAddress.host() + Symbol.C_COLON
                + peerAddress.port();
    }

    /**
     * Returns whether a comma-separated header contains a token.
     *
     * @param header header
     * @param token  token
     * @return true when present
     */
    private boolean containsToken(final String header, final String token) {
        if (StringKit.isBlank(header)) {
            return false;
        }
        for (final String value : header.split(Symbol.COMMA)) {
            if (token.equals(value.trim().toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether bytes end with CRLFCRLF.
     *
     * @param bytes bytes
     * @return true when ended
     */
    private boolean endsHeaders(final ByteArrayOutputStream bytes) {
        final byte[] value = bytes.toByteArray();
        final int length = value.length;
        return length >= Normal._4 && value[length - Normal._4] == Symbol.C_CR
                && value[length - Normal._3] == Symbol.C_LF && value[length - Normal._2] == Symbol.C_CR
                && value[length - Normal._1] == Symbol.C_LF;
    }

    /**
     * Validates a required value.
     *
     * @param value value
     * @param name  name
     * @param <T>   value type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

    /**
     * Converts a long byte count to an int size accepted by JDK buffers.
     *
     * @param byteCount byte count
     * @return int size
     */
    private static int toIntSize(final long byteCount) {
        return (int) Math.min(byteCount, Integer.MAX_VALUE);
    }

    /**
     * Upgrade request.
     *
     * @param method  method
     * @param path    path
     * @param headers headers
     * @param payload remaining payload
     */
    private record UpgradeRequest(String method, String path, Headers headers, Buffer payload) {
    }

    /**
     * Upgrade context.
     *
     * @param peerAddress peer address
     * @param proxyHeader proxy header
     * @param request     request
     */
    private record UpgradeContext(Address peerAddress, ProxyHeader proxyHeader, UpgradeRequest request) {
    }

    /**
     * Source view used while reading the HTTP upgrade from a raw accepted channel.
     */
    private static final class HandshakeSource implements Source {

        /**
         * Raw socket channel.
         */
        private final SocketChannel channel;

        /**
         * Creates a source.
         *
         * @param channel socket channel
         */
        private HandshakeSource(final SocketChannel channel) {
            this.channel = require(channel, "Handshake channel");
        }

        /**
         * Reads bytes from the raw socket channel.
         *
         * @param sink      target buffer
         * @param byteCount maximum byte count
         * @return read byte count or EOF
         * @throws IOException when reading fails
         */
        @Override
        public long read(final Buffer sink, final long byteCount) throws IOException {
            final Buffer target = require(sink, "Handshake read target");
            Assert.isTrue(byteCount >= Normal._0, () -> new ValidateException("Read byte count must not be negative"));
            if (byteCount == Normal._0) {
                return Normal._0;
            }
            final byte[] bytes = new byte[toIntSize(Math.min(byteCount, Normal._8192))];
            final ByteBuffer buffer = ByteBuffer.wrap(bytes);
            int read = channel.read(buffer);
            while (read == Normal._0) {
                ThreadKit.sleep(Normal._1);
                read = channel.read(buffer);
            }
            if (read < Normal._0) {
                return Normal.__1;
            }
            target.write(bytes, Normal._0, read);
            return read;
        }

        /**
         * Returns the no-op timeout.
         *
         * @return timeout
         */
        @Override
        public Timeout timeout() {
            return Timeout.NONE;
        }

        /**
         * Closes the raw channel.
         *
         * @throws IOException when closing fails
         */
        @Override
        public void close() throws IOException {
            channel.close();
        }

    }

    /**
     * Sink view used while writing an HTTP upgrade response to a raw accepted channel.
     */
    private static final class HandshakeSink implements Sink {

        /**
         * Raw socket channel.
         */
        private final SocketChannel channel;

        /**
         * Creates a sink.
         *
         * @param channel socket channel
         */
        private HandshakeSink(final SocketChannel channel) {
            this.channel = require(channel, "Handshake channel");
        }

        /**
         * Writes bytes to the raw socket channel.
         *
         * @param source    source buffer
         * @param byteCount byte count
         * @throws IOException when writing fails
         */
        @Override
        public void write(final Buffer source, final long byteCount) throws IOException {
            final Buffer payload = require(source, "Handshake write source");
            Assert.isTrue(byteCount >= Normal._0, () -> new ValidateException("Write byte count must not be negative"));
            Assert.isTrue(
                    byteCount <= payload.size(),
                    () -> new ValidateException("Write byte count must not exceed source size"));
            long remaining = byteCount;
            while (remaining > Normal._0) {
                final ByteBuffer view = payload.nioBuffer(toIntSize(remaining));
                final int written = channel.write(view);
                if (written == Normal._0) {
                    ThreadKit.sleep(Normal._1);
                    continue;
                }
                payload.skip(written);
                remaining -= written;
            }
        }

        /**
         * Flushes this sink.
         */
        @Override
        public void flush() {
            // SocketChannel writes are flushed by the operating system.
        }

        /**
         * Returns the no-op timeout.
         *
         * @return timeout
         */
        @Override
        public Timeout timeout() {
            return Timeout.NONE;
        }

        /**
         * Closes the raw channel.
         *
         * @throws IOException when closing fails
         */
        @Override
        public void close() throws IOException {
            channel.close();
        }

    }

    /**
     * Reader for upgrade bytes.
     */
    private static final class UpgradeReader {

        /**
         * Prefix bytes.
         */
        private final Buffer prefix;

        /**
         * Socket source.
         */
        private final Source source;

        /**
         * Creates a reader.
         *
         * @param prefix prefix
         * @param source source
         */
        private UpgradeReader(final Buffer prefix, final Source source) {
            this.prefix = prefix == null ? new Buffer() : prefix;
            this.source = require(source, "Upgrade source");
        }

        /**
         * Reads one byte.
         *
         * @return byte value or -1
         */
        private int read() {
            if (prefix.size() > Normal._0) {
                return prefix.readByte() & org.miaixz.bus.fabric.Builder.UNSIGNED_BYTE_MASK;
            }
            final Buffer one = new Buffer();
            try {
                final long read = source.read(one, Normal._1);
                if (read < Normal._0) {
                    return Normal.__1;
                }
                return one.readByte() & org.miaixz.bus.fabric.Builder.UNSIGNED_BYTE_MASK;
            } catch (final IOException e) {
                throw new SocketException("Unable to read WebSocket upgrade", e);
            }
        }

        /**
         * Returns remaining prefix bytes.
         *
         * @return remaining bytes
         */
        private Buffer remaining() {
            return prefix;
        }

    }

    /**
     * Builder for WebSocket servers.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static final class Builder {

        /**
         * Shared context.
         */
        private final Context context;

        /**
         * Bind address.
         */
        private Address address;

        /**
         * Route path.
         */
        private String path = Symbol.SLASH;

        /**
         * Socket options.
         */
        private SocketOptions socketOptions = SocketOptions.defaults();

        /**
         * Response headers.
         */
        private Headers responseHeaders = Headers.empty();

        /**
         * Ping interval.
         */
        private Duration ping = Duration.ZERO;

        /**
         * Message handler.
         */
        private Handler handler = Demuxer.noop();

        /**
         * Demuxer builder.
         */
        private Demuxer.Builder demuxer;

        /**
         * Guard rule.
         */
        private GuardRule guard;

        /**
         * Message filter.
         */
        private Filter filter;

        /**
         * Observer.
         */
        private EventObserver observer = EventObserver.noop();

        /**
         * Server listener.
         */
        private Listener<? super WebSocketServer> listener;

        /**
         * Session listener.
         */
        private Listener<? super WebSocketSession> sessionListener;

        /**
         * Open handler.
         */
        private Consumer<WebSocketSession> openHandler = session -> {
        };

        /**
         * Error handler.
         */
        private Consumer<Throwable> errorHandler = cause -> {
        };

        /**
         * Creates a builder.
         *
         * @param context context
         */
        private Builder(final Context context) {
            this.context = context;
        }

        /**
         * Sets bind host and port.
         *
         * @param host host
         * @param port port
         * @return this builder
         */
        public Builder bind(final String host, final int port) {
            validateHost(host);
            validatePort(port);
            return bind(new Address(Protocol.WS.name, host, port, Symbol.SLASH));
        }

        /**
         * Sets bind address.
         *
         * @param address bind address
         * @return this builder
         */
        public Builder bind(final Address address) {
            final Address current = require(address, "WebSocket bind address");
            if (!Protocol.WS.name.equalsIgnoreCase(current.scheme()) || !Symbol.SLASH.equals(current.path())) {
                throw new ValidateException("WebSocket server bind address must be ws://host:port/");
            }
            this.address = current;
            return this;
        }

        /**
         * Sets route path.
         *
         * @param value path
         * @return this builder
         */
        public Builder path(final String value) {
            if (StringKit.isBlank(value) || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF)
                    || !value.startsWith(Symbol.SLASH)) {
                throw new ValidateException("WebSocket server path must be absolute and single-line");
            }
            this.path = value;
            return this;
        }

        /**
         * Sets listen backlog.
         *
         * @param value backlog
         * @return this builder
         */
        public Builder backlog(final int value) {
            return socketOptions(copySocketOptions().backlog(value).build());
        }

        /**
         * Sets I/O thread count.
         *
         * @param value I/O thread count
         * @return this builder
         */
        public Builder ioThreads(final int value) {
            return socketOptions(copySocketOptions().ioThreads(value).build());
        }

        /**
         * Sets socket options.
         *
         * @param options options
         * @return this builder
         */
        public Builder socketOptions(final SocketOptions options) {
            this.socketOptions = require(options, "Socket options");
            return this;
        }

        /**
         * Adds a response header.
         *
         * @param name  name
         * @param value value
         * @return this builder
         */
        public Builder header(final String name, final String value) {
            rejectReservedHeader(name);
            responseHeaders = responseHeaders.with(name, value);
            return this;
        }

        /**
         * Merges response headers.
         *
         * @param headers headers
         * @return this builder
         */
        public Builder headers(final Headers headers) {
            final Headers current = require(headers, "Headers");
            for (final Map.Entry<String, List<String>> entry : current.asMap().entrySet()) {
                rejectReservedHeader(entry.getKey());
                for (final String value : entry.getValue()) {
                    responseHeaders = responseHeaders.with(entry.getKey(), value);
                }
            }
            return this;
        }

        /**
         * Sets accepted subprotocol.
         *
         * @param protocol subprotocol
         * @return this builder
         */
        public Builder subprotocol(final String protocol) {
            if (StringKit.isBlank(protocol) || StringKit.containsAny(protocol, Symbol.C_CR, Symbol.C_LF)) {
                throw new ValidateException("WebSocket subprotocol must be non-blank and single-line");
            }
            responseHeaders = responseHeaders.with(HTTP.SEC_WEBSOCKET_PROTOCOL, protocol);
            return this;
        }

        /**
         * Sets ping interval.
         *
         * @param value ping interval
         * @return this builder
         */
        public Builder ping(final Duration value) {
            final Duration checked = require(value, "WebSocket ping interval");
            if (checked.isNegative()) {
                throw new ValidateException("WebSocket ping interval must be non-negative");
            }
            this.ping = checked;
            return this;
        }

        /**
         * Sets message handler.
         *
         * @param handler handler
         * @return this builder
         */
        public Builder onMessage(final Handler handler) {
            this.handler = handler == null ? Demuxer.noop() : handler;
            this.demuxer = null;
            return this;
        }

        /**
         * Adds channel handler.
         *
         * @param channel channel
         * @param handler handler
         * @return this builder
         */
        public Builder channel(final String channel, final Handler handler) {
            demuxer().channel(channel, handler);
            return this;
        }

        /**
         * Sets fallback handler.
         *
         * @param handler handler
         * @return this builder
         */
        public Builder fallback(final Handler handler) {
            demuxer().fallback(handler);
            return this;
        }

        /**
         * Sets channel header.
         *
         * @param name header name
         * @return this builder
         */
        public Builder channelHeader(final String name) {
            demuxer().header(name);
            return this;
        }

        /**
         * Sets channel resolver.
         *
         * @param resolver resolver
         * @return this builder
         */
        public Builder resolver(final Function<Message, String> resolver) {
            demuxer().resolver(resolver);
            return this;
        }

        /**
         * Sets text handler.
         *
         * @param handler text handler
         * @return this builder
         */
        public Builder onText(final BiConsumer<WebSocketSession, String> handler) {
            this.demuxer = null;
            this.handler = handler == null ? Demuxer.noop()
                    : (session, message) -> handler
                            .accept((WebSocketSession) session, message.payload().text(Charset.UTF_8));
            return this;
        }

        /**
         * Sets open handler.
         *
         * @param handler open handler
         * @return this builder
         */
        public Builder onOpen(final Consumer<WebSocketSession> handler) {
            this.openHandler = handler == null ? session -> {
            } : handler;
            return composeSessionListener();
        }

        /**
         * Sets error handler.
         *
         * @param handler error handler
         * @return this builder
         */
        public Builder onError(final Consumer<Throwable> handler) {
            this.errorHandler = handler == null ? cause -> {
            } : handler;
            return composeSessionListener();
        }

        /**
         * Sets guard.
         *
         * @param guard guard
         * @return this builder
         */
        public Builder guard(final GuardRule guard) {
            this.guard = guard;
            return this;
        }

        /**
         * Sets filter.
         *
         * @param filter filter
         * @return this builder
         */
        public Builder filter(final Filter filter) {
            this.filter = filter;
            return this;
        }

        /**
         * Sets observer.
         *
         * @param observer observer
         * @return this builder
         */
        public Builder observe(final EventObserver observer) {
            this.observer = observer == null ? EventObserver.noop() : observer;
            return this;
        }

        /**
         * Sets server listener.
         *
         * @param listener listener
         * @return this builder
         */
        public Builder listener(final Listener<? super WebSocketServer> listener) {
            this.listener = listener;
            return this;
        }

        /**
         * Sets session listener.
         *
         * @param listener listener
         * @return this builder
         */
        public Builder sessionListener(final Listener<? super WebSocketSession> listener) {
            this.sessionListener = listener;
            return this;
        }

        /**
         * Builds a server.
         *
         * @return server
         */
        public WebSocketServer build() {
            if (address == null) {
                throw new ValidateException("WebSocket server bind address must be set");
            }
            return new WebSocketServer(this);
        }

        /**
         * Builds and starts a server.
         *
         * @return server
         */
        public WebSocketServer start() {
            return build().start();
        }

        /**
         * Returns effective handler.
         *
         * @return handler
         */
        private Handler handler() {
            return demuxer == null ? handler : demuxer.build();
        }

        /**
         * Returns effective session listener.
         *
         * @return listener
         */
        private Listener<? super WebSocketSession> sessionListener() {
            return sessionListener == null ? new Listener<>() {
            } : sessionListener;
        }

        /**
         * Composes callback listener.
         *
         * @return this builder
         */
        private Builder composeSessionListener() {
            this.sessionListener = new Listener<>() {

                @Override
                public void open(final WebSocketSession source) {
                    openHandler.accept(source);
                }

                @Override
                public void failure(final WebSocketSession source, final Throwable cause) {
                    errorHandler.accept(cause);
                }
            };
            return this;
        }

        /**
         * Returns demuxer builder.
         *
         * @return demuxer builder
         */
        private Demuxer.Builder demuxer() {
            if (demuxer == null) {
                demuxer = Demuxer.builder();
            }
            return demuxer;
        }

        /**
         * Copies socket options.
         *
         * @return builder
         */
        private SocketOptions.Builder copySocketOptions() {
            return SocketOptions.builder().readBufferSize(socketOptions.readBufferSize())
                    .writeChunkSize(socketOptions.writeChunkSize()).writeChunkCount(socketOptions.writeChunkCount())
                    .backlog(socketOptions.backlog()).ioThreads(socketOptions.ioThreads())
                    .socketOptions(socketOptions.socketOptions()).retainReadBuffer(socketOptions.retainReadBuffer())
                    .connectTimeout(socketOptions.connectTimeout()).idleTimeout(socketOptions.idleTimeout());
        }

        /**
         * Rejects reserved response headers.
         *
         * @param name header name
         */
        private static void rejectReservedHeader(final String name) {
            if (HTTP.UPGRADE.equalsIgnoreCase(name) || HTTP.CONNECTION.equalsIgnoreCase(name)
                    || HTTP.SEC_WEBSOCKET_ACCEPT.equalsIgnoreCase(name)) {
                throw new ValidateException("WebSocket server response header is reserved: " + name);
            }
        }

        /**
         * Validates host.
         *
         * @param host host
         */
        private static void validateHost(final String host) {
            if (StringKit.isBlank(host) || StringKit.containsAny(host, Symbol.C_CR, Symbol.C_LF)) {
                throw new ValidateException("WebSocket server host must be non-blank and single-line");
            }
        }

        /**
         * Validates port.
         *
         * @param port port
         */
        private static void validatePort(final int port) {
            if (port < Normal._1 || port > Normal._65535) {
                throw new ValidateException("WebSocket server port must be between 1 and 65535");
            }
        }

    }

}
