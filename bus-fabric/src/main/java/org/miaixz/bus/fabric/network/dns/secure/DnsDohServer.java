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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.core.Lifecycle;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.network.dns.message.DnsCodec;
import org.miaixz.bus.fabric.protocol.http.http2.HpackCodec;
import org.miaixz.bus.fabric.protocol.http.http2.Http2Header;

/**
 * Small DNS-over-HTTPS server that serves HTTP/1.1 and cleartext HTTP/2 prior-knowledge requests.
 * <p>
 * Instances own one bound server socket and short-lived per-connection daemon threads. The implementation is only
 * started when a DNS-over-HTTPS endpoint is explicitly configured. It does not modify the shared HTTP client stack and
 * it delegates every DNS message to a caller-provided resolver.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class DnsDohServer implements AutoCloseable, Lifecycle {

    /**
     * DNS-over-HTTPS request path.
     */
    public static final String PATH = Symbol.SLASH + "dns-query";

    /**
     * HTTP/2 client connection preface.
     */
    private static final byte[] HTTP2_PREFACE = Builder.HTTP2_CONNECTION_PREFACE.getBytes(StandardCharsets.US_ASCII);

    /**
     * Maximum HTTP/1.1 request-line bytes.
     */
    private static final int MAX_REQUEST_LINE_BYTES = 8192;

    /**
     * Maximum HTTP/1.1 header-line bytes.
     */
    private static final int MAX_HEADER_LINE_BYTES = 8192;

    /**
     * HTTP/2 DATA frame type.
     */
    private static final int H2_DATA = 0;

    /**
     * HTTP/2 HEADERS frame type.
     */
    private static final int H2_HEADERS = 1;

    /**
     * HTTP/2 RST_STREAM frame type.
     */
    private static final int H2_RST_STREAM = 3;

    /**
     * HTTP/2 SETTINGS frame type.
     */
    private static final int H2_SETTINGS = 4;

    /**
     * HTTP/2 PING frame type.
     */
    private static final int H2_PING = 6;

    /**
     * HTTP/2 GOAWAY frame type.
     */
    private static final int H2_GOAWAY = 7;

    /**
     * HTTP/2 WINDOW_UPDATE frame type.
     */
    private static final int H2_WINDOW_UPDATE = 8;

    /**
     * HTTP/2 CONTINUATION frame type.
     */
    private static final int H2_CONTINUATION = 9;

    /**
     * HTTP/2 END_STREAM flag.
     */
    private static final int H2_FLAG_END_STREAM = 0x1;

    /**
     * HTTP/2 ACK flag.
     */
    private static final int H2_FLAG_ACK = 0x1;

    /**
     * HTTP/2 END_HEADERS flag.
     */
    private static final int H2_FLAG_END_HEADERS = 0x4;

    /**
     * HTTP/2 PADDED flag.
     */
    private static final int H2_FLAG_PADDED = 0x8;

    /**
     * HTTP/2 PRIORITY flag.
     */
    private static final int H2_FLAG_PRIORITY = 0x20;

    /**
     * HTTP/2 NO_ERROR code.
     */
    private static final int H2_NO_ERROR = 0;

    /**
     * HTTP/2 PROTOCOL_ERROR code.
     */
    private static final int H2_PROTOCOL_ERROR = 1;

    /**
     * Resolver invoked with the decoded DNS wire message.
     */
    private final Resolver resolver;

    /**
     * Bound TCP server socket.
     */
    private final java.net.ServerSocket server;

    /**
     * Endpoint activity flag shared by the accept and client loops.
     */
    private final AtomicBoolean active;

    /**
     * Close guard.
     */
    private final AtomicBoolean closed;

    /**
     * Child connection cleanup handles.
     */
    private final CopyOnWriteArrayList<AutoCloseable> clients;

    /**
     * Accept loop thread.
     */
    private Thread acceptThread;

    /**
     * Creates a DNS-over-HTTPS server.
     *
     * @param address  socket address to bind
     * @param resolver DNS resolver callback
     * @throws IOException       if the server socket cannot be created or bound
     * @throws ValidateException if an argument is invalid
     */
    public DnsDohServer(final InetSocketAddress address, final Resolver resolver) throws IOException {
        if (address == null) {
            throw new ValidateException("DNS-over-HTTPS bind address must not be null");
        }
        if (resolver == null) {
            throw new ValidateException("DNS-over-HTTPS resolver must not be null");
        }
        this.resolver = resolver;
        this.server = new java.net.ServerSocket();
        this.server.setReuseAddress(true);
        this.server.bind(address);
        this.active = new AtomicBoolean();
        this.closed = new AtomicBoolean();
        this.clients = new CopyOnWriteArrayList<>();
    }

    /**
     * Starts the accept loop.
     *
     * @return this server
     */
    public DnsDohServer start() {
        if (closed.get()) {
            throw new StatefulException("DNS-over-HTTPS server is closed");
        }
        if (!active.compareAndSet(false, true)) {
            return this;
        }
        acceptThread = ThreadKit.newThread(this::acceptLoop, "fabric-dns-doh-" + server.getLocalPort(), true);
        acceptThread.start();
        return this;
    }

    /**
     * Stops the accept loop and closes all open client sockets.
     */
    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        active.set(false);
        IoKit.closeQuietly(server);
        if (acceptThread != null) {
            acceptThread.interrupt();
        }
        for (final AutoCloseable client : clients) {
            IoKit.closeQuietly(client);
        }
        clients.clear();
    }

    /**
     * Returns the current server lifecycle state.
     *
     * @return server lifecycle state
     */
    @Override
    public State state() {
        if (closed.get()) {
            return State.CLOSED;
        }
        return active.get() ? State.RUNNING : State.NEW;
    }

    /**
     * Runs the accept loop for the bound endpoint.
     */
    private void acceptLoop() {
        while (active.get()) {
            try {
                final java.net.Socket socket = server.accept();
                clients.add(socket);
                final Thread thread = ThreadKit
                        .newThread(() -> clientLoop(socket), "fabric-dns-doh-client-" + server.getLocalPort(), true);
                thread.start();
            } catch (final IOException e) {
                if (active.get()) {
                    throw new SocketException("DNS-over-HTTPS accept failed", e);
                }
                return;
            }
        }
    }

    /**
     * Serves one accepted client socket.
     *
     * @param socket accepted client socket
     */
    private void clientLoop(final java.net.Socket socket) {
        try (java.net.Socket current = socket;
                PushbackInputStream input = new PushbackInputStream(new BufferedInputStream(current.getInputStream()),
                        HTTP2_PREFACE.length);
                OutputStream output = new BufferedOutputStream(current.getOutputStream())) {
            if (http2Preface(input)) {
                handleHttp2(input, output, current.getInetAddress());
            } else {
                handleHttp1(input, output, current.getInetAddress());
            }
        } catch (final IOException e) {
            if (active.get()) {
                throw new SocketException("DNS-over-HTTPS client failed", e);
            }
        } finally {
            clients.remove(socket);
        }
    }

    /**
     * Returns whether the next bytes are an HTTP/2 client preface.
     *
     * @param input client input
     * @return true when the preface was consumed
     * @throws IOException if the socket cannot be read
     */
    private static boolean http2Preface(final PushbackInputStream input) throws IOException {
        final byte[] candidate = input.readNBytes(HTTP2_PREFACE.length);
        if (candidate.length == HTTP2_PREFACE.length && matches(candidate, HTTP2_PREFACE)) {
            return true;
        }
        input.unread(candidate);
        return false;
    }

    /**
     * Handles one HTTP/1.1 request and closes the connection.
     *
     * @param input         client input
     * @param output        client output
     * @param clientAddress client address
     * @throws IOException if the socket cannot be read or written
     */
    private void handleHttp1(final InputStream input, final OutputStream output, final InetAddress clientAddress)
            throws IOException {
        try {
            final Http1RequestLine request = Http1RequestLine.parse(readAsciiLine(input, MAX_REQUEST_LINE_BYTES));
            final Map<String, String> headers = readHttp1Headers(input);
            final byte[] dns = switch (request.method) {
                case GET -> getDnsFromPath(request.target);
                case POST -> postDnsFromHttp1(input, headers);
                default -> throw new StatefulException(Http.Status.METHOD_NOT_ALLOWED,
                        "HTTP " + Http.Status.METHOD_NOT_ALLOWED);
            };
            sendHttp1Dns(output, resolve(dns, clientAddress));
        } catch (final StatefulException e) {
            sendHttp1Error(output, e.getStatus());
        } catch (final IllegalArgumentException | ProtocolException e) {
            sendHttp1Error(output, Http.Status.BAD_REQUEST);
        }
    }

    /**
     * Handles an HTTP/2 prior-knowledge connection.
     *
     * @param input         client input after the preface
     * @param output        client output
     * @param clientAddress client address
     * @throws IOException if the socket cannot be read or written
     */
    private void handleHttp2(final InputStream input, final OutputStream output, final InetAddress clientAddress)
            throws IOException {
        final HpackCodec hpack = new HpackCodec();
        final ConcurrentHashMap<Integer, Http2RequestState> streams = new ConcurrentHashMap<>();
        writeFrame(output, H2_SETTINGS, 0, 0, Normal.EMPTY_BYTE_ARRAY);
        output.flush();
        while (active.get()) {
            final Http2FrameHeader header;
            try {
                header = Http2FrameHeader.read(input);
            } catch (final EOFException e) {
                return;
            }
            final byte[] payload = input.readNBytes(header.length);
            if (payload.length != header.length) {
                return;
            }
            switch (header.type) {
                case H2_SETTINGS -> handleHttp2Settings(output, header, payload);
                case H2_HEADERS -> handleHttp2Headers(input, output, hpack, streams, header, payload, clientAddress);
                case H2_DATA -> handleHttp2Data(output, hpack, streams, header, payload, clientAddress);
                case H2_PING -> handleHttp2Ping(output, header, payload);
                case H2_GOAWAY -> {
                    return;
                }
                case H2_WINDOW_UPDATE -> {
                    continue;
                }
                default -> {
                    continue;
                }
            }
            output.flush();
        }
    }

    /**
     * Handles an HTTP/2 SETTINGS frame.
     *
     * @param output  client output
     * @param header  frame header
     * @param payload frame payload
     * @throws IOException if the socket cannot be written
     */
    private static void handleHttp2Settings(
            final OutputStream output,
            final Http2FrameHeader header,
            final byte[] payload) throws IOException {
        if (header.streamId != 0 || (header.flags & H2_FLAG_ACK) != 0 && payload.length != 0
                || (header.flags & H2_FLAG_ACK) == 0 && payload.length % 6 != 0) {
            writeGoaway(output, H2_PROTOCOL_ERROR);
            return;
        }
        if ((header.flags & H2_FLAG_ACK) == 0) {
            writeFrame(output, H2_SETTINGS, H2_FLAG_ACK, 0, Normal.EMPTY_BYTE_ARRAY);
        }
    }

    /**
     * Handles an HTTP/2 HEADERS frame.
     *
     * @param input         client input for continuation frames
     * @param output        client output
     * @param hpack         connection HPACK state
     * @param streams       active stream state
     * @param header        frame header
     * @param payload       frame payload
     * @param clientAddress client address
     * @throws IOException if the socket cannot be read or written
     */
    private void handleHttp2Headers(
            final InputStream input,
            final OutputStream output,
            final HpackCodec hpack,
            final ConcurrentHashMap<Integer, Http2RequestState> streams,
            final Http2FrameHeader header,
            final byte[] payload,
            final InetAddress clientAddress) throws IOException {
        if (header.streamId <= 0) {
            writeGoaway(output, H2_PROTOCOL_ERROR);
            return;
        }
        final byte[] block = completeHeaderBlock(input, header, payload);
        final List<Http2Header> decoded = hpack.decode(new Buffer().write(block));
        final Http2RequestState state = Http2RequestState.from(decoded);
        streams.put(header.streamId, state);
        if ((header.flags & H2_FLAG_END_STREAM) != 0) {
            processHttp2Request(output, hpack, streams, header.streamId, clientAddress);
        }
    }

    /**
     * Handles an HTTP/2 DATA frame.
     *
     * @param output        client output
     * @param streams       active stream state
     * @param header        frame header
     * @param payload       frame payload
     * @param clientAddress client address
     * @throws IOException if the socket cannot be written
     */
    private void handleHttp2Data(
            final OutputStream output,
            final HpackCodec hpack,
            final ConcurrentHashMap<Integer, Http2RequestState> streams,
            final Http2FrameHeader header,
            final byte[] payload,
            final InetAddress clientAddress) throws IOException {
        final Http2RequestState state = streams.get(header.streamId);
        if (state == null || header.streamId <= 0) {
            writeRstStream(output, header.streamId, H2_PROTOCOL_ERROR);
            return;
        }
        try {
            state.append(dataPayload(header.flags, payload));
            if ((header.flags & H2_FLAG_END_STREAM) != 0) {
                processHttp2Request(output, hpack, streams, header.streamId, clientAddress);
            }
        } catch (final StatefulException e) {
            streams.remove(header.streamId);
            sendHttp2(
                    output,
                    hpack,
                    header.streamId,
                    e.getStatus(),
                    MediaType.TEXT_PLAIN,
                    httpStatusBody(e.getStatus()));
        }
    }

    /**
     * Handles an HTTP/2 PING frame.
     *
     * @param output  client output
     * @param header  frame header
     * @param payload frame payload
     * @throws IOException if the socket cannot be written
     */
    private static void handleHttp2Ping(final OutputStream output, final Http2FrameHeader header, final byte[] payload)
            throws IOException {
        if (header.streamId == 0 && payload.length == 8 && (header.flags & H2_FLAG_ACK) == 0) {
            writeFrame(output, H2_PING, H2_FLAG_ACK, 0, payload);
        }
    }

    /**
     * Processes a completed HTTP/2 request stream.
     *
     * @param output        client output
     * @param hpack         connection HPACK state
     * @param streams       active stream state
     * @param streamId      stream identifier
     * @param clientAddress client address
     * @throws IOException if the socket cannot be written
     */
    private void processHttp2Request(
            final OutputStream output,
            final HpackCodec hpack,
            final ConcurrentHashMap<Integer, Http2RequestState> streams,
            final int streamId,
            final InetAddress clientAddress) throws IOException {
        final Http2RequestState state = streams.remove(streamId);
        if (state == null) {
            return;
        }
        try {
            final byte[] dns = switch (state.method()) {
                case GET -> getDnsFromPath(state.path());
                case POST -> postDnsFromHttp2(state);
                default -> throw new StatefulException(Http.Status.METHOD_NOT_ALLOWED,
                        "HTTP " + Http.Status.METHOD_NOT_ALLOWED);
            };
            sendHttp2(
                    output,
                    hpack,
                    streamId,
                    Http.Status.OK,
                    MediaType.APPLICATION_DNS_MESSAGE,
                    resolve(dns, clientAddress));
        } catch (final StatefulException e) {
            sendHttp2(output, hpack, streamId, e.getStatus(), MediaType.TEXT_PLAIN, httpStatusBody(e.getStatus()));
        } catch (final IllegalArgumentException | ProtocolException e) {
            sendHttp2(
                    output,
                    hpack,
                    streamId,
                    Http.Status.BAD_REQUEST,
                    MediaType.TEXT_PLAIN,
                    httpStatusBody(Http.Status.BAD_REQUEST));
        }
    }

    /**
     * Resolves one DNS message and validates the request size.
     *
     * @param request       DNS query wire message
     * @param clientAddress client address
     * @return DNS response wire message
     */
    private byte[] resolve(final byte[] request, final InetAddress clientAddress) {
        if (request.length == 0 || request.length > DnsCodec.MAX_MESSAGE_BYTES) {
            throw new StatefulException(Http.Status.CONTENT_TOO_LARGE, "HTTP " + Http.Status.CONTENT_TOO_LARGE);
        }
        return resolver.resolve(request, clientAddress);
    }

    /**
     * Reads a DNS message from an HTTP/1.1 POST request.
     *
     * @param input   client input
     * @param headers normalized headers
     * @return DNS query wire bytes
     * @throws IOException if the socket cannot be read
     */
    private static byte[] postDnsFromHttp1(final InputStream input, final Map<String, String> headers)
            throws IOException {
        final String contentType = headers.get(Http.Header.CONTENT_TYPE.toLowerCase(Locale.ROOT));
        if (!isDnsMessageContentType(contentType)) {
            throw new StatefulException(Http.Status.UNSUPPORTED_MEDIA_TYPE,
                    "HTTP " + Http.Status.UNSUPPORTED_MEDIA_TYPE);
        }
        final int length = contentLength(headers.get(Http.Header.CONTENT_LENGTH.toLowerCase(Locale.ROOT)));
        if (length <= 0 || length > DnsCodec.MAX_MESSAGE_BYTES) {
            throw new StatefulException(Http.Status.CONTENT_TOO_LARGE, "HTTP " + Http.Status.CONTENT_TOO_LARGE);
        }
        final byte[] body = input.readNBytes(length);
        if (body.length != length) {
            throw new StatefulException(Http.Status.BAD_REQUEST, "HTTP " + Http.Status.BAD_REQUEST);
        }
        return body;
    }

    /**
     * Reads a DNS message from an HTTP/2 POST request.
     *
     * @param state completed request state
     * @return DNS query wire bytes
     */
    private static byte[] postDnsFromHttp2(final Http2RequestState state) {
        final String contentType = state.headers.get(Http.Header.CONTENT_TYPE.toLowerCase(Locale.ROOT));
        if (!isDnsMessageContentType(contentType)) {
            throw new StatefulException(Http.Status.UNSUPPORTED_MEDIA_TYPE,
                    "HTTP " + Http.Status.UNSUPPORTED_MEDIA_TYPE);
        }
        return state.body.toByteArray();
    }

    /**
     * Returns whether a Content-Type value denotes a DNS message.
     *
     * @param contentType raw Content-Type header value
     * @return true when the media type is exactly {@code application/dns-message}
     */
    private static boolean isDnsMessageContentType(final String contentType) {
        if (contentType == null) {
            return false;
        }
        final int separator = contentType.indexOf(Symbol.C_SEMICOLON);
        final String mediaType = separator < 0 ? contentType : contentType.substring(0, separator);
        return MediaType.APPLICATION_DNS_MESSAGE.equals(mediaType.trim().toLowerCase(Locale.ROOT));
    }

    /**
     * Reads a DNS message from a DoH GET path.
     *
     * @param target request target
     * @return DNS query wire bytes
     */
    private static byte[] getDnsFromPath(final String target) {
        final URI uri = URI.create(target);
        if (!PATH.equals(uri.getPath())) {
            throw new StatefulException(Http.Status.NOT_FOUND, "HTTP " + Http.Status.NOT_FOUND);
        }
        final String encoded = queryParameter(uri.getRawQuery(), "dns");
        if (encoded == null || encoded.isBlank()) {
            throw new StatefulException(Http.Status.BAD_REQUEST, "HTTP " + Http.Status.BAD_REQUEST);
        }
        return Base64.getUrlDecoder().decode(encoded);
    }

    /**
     * Sends one HTTP/1.1 DNS response.
     *
     * @param output   client output
     * @param response DNS response wire bytes
     * @throws IOException if the socket cannot be written
     */
    private static void sendHttp1Dns(final OutputStream output, final byte[] response) throws IOException {
        writeHttp1(output, Http.Status.OK, MediaType.APPLICATION_DNS_MESSAGE, response);
    }

    /**
     * Sends one HTTP/1.1 error response.
     *
     * @param output client output
     * @param status HTTP status
     * @throws IOException if the socket cannot be written
     */
    private static void sendHttp1Error(final OutputStream output, final int status) throws IOException {
        writeHttp1(output, status, MediaType.TEXT_PLAIN, httpStatusBody(status));
    }

    /**
     * Writes one HTTP/1.1 response.
     *
     * @param output      client output
     * @param status      HTTP status
     * @param contentType response content type
     * @param body        response body
     * @throws IOException if the socket cannot be written
     */
    private static void writeHttp1(
            final OutputStream output,
            final int status,
            final String contentType,
            final byte[] body) throws IOException {
        output.write(
                ("HTTP/1.1 " + status + Symbol.SPACE + reason(status) + Symbol.CRLF)
                        .getBytes(StandardCharsets.US_ASCII));
        output.write((Http.Header.CONTENT_TYPE + ": " + contentType + Symbol.CRLF).getBytes(StandardCharsets.US_ASCII));
        output.write(
                (Http.Header.CONTENT_LENGTH + ": " + body.length + Symbol.CRLF).getBytes(StandardCharsets.US_ASCII));
        output.write(
                (Http.Header.CONNECTION + ": " + Http.Header.CONNECTION_CLOSE + Symbol.CRLF + Symbol.CRLF)
                        .getBytes(StandardCharsets.US_ASCII));
        output.write(body);
        output.flush();
    }

    /**
     * Sends one HTTP/2 response.
     *
     * @param output      client output
     * @param hpack       connection HPACK state
     * @param streamId    stream identifier
     * @param status      HTTP status
     * @param contentType response content type
     * @param body        response body
     * @throws IOException if the socket cannot be written
     */
    private static void sendHttp2(
            final OutputStream output,
            final HpackCodec hpack,
            final int streamId,
            final int status,
            final String contentType,
            final byte[] body) throws IOException {
        final ArrayList<Http2Header> headers = new ArrayList<>();
        headers.add(Http2Header.of(":status", Integer.toString(status)));
        headers.add(Http2Header.of(Http.Header.CONTENT_TYPE.toLowerCase(Locale.ROOT), contentType));
        headers.add(Http2Header.of(Http.Header.CONTENT_LENGTH.toLowerCase(Locale.ROOT), Integer.toString(body.length)));
        final byte[] encoded = hpack.encodeBuffer(headers).readByteArray();
        final int headerFlags = H2_FLAG_END_HEADERS | (body.length == 0 ? H2_FLAG_END_STREAM : 0);
        writeFrame(output, H2_HEADERS, headerFlags, streamId, encoded);
        if (body.length > 0) {
            writeFrame(output, H2_DATA, H2_FLAG_END_STREAM, streamId, body);
        }
    }

    /**
     * Reads a CRLF-terminated ASCII line.
     *
     * @param input client input
     * @param limit maximum line bytes
     * @return line without CRLF
     * @throws IOException if the socket cannot be read
     */
    private static String readAsciiLine(final InputStream input, final int limit) throws IOException {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        for (int index = 0; index < limit; index++) {
            final int value = input.read();
            if (value < 0) {
                throw new EOFException();
            }
            if (value == Symbol.C_LF) {
                final byte[] line = bytes.toByteArray();
                final int length = line.length > Normal._0 && line[line.length - Normal._1] == Symbol.C_CR
                        ? line.length - Normal._1
                        : line.length;
                return new String(line, Normal._0, length, StandardCharsets.US_ASCII);
            }
            bytes.write(value);
        }
        throw new StatefulException(Http.Status.BAD_REQUEST, "HTTP " + Http.Status.BAD_REQUEST);
    }

    /**
     * Reads HTTP/1.1 headers.
     *
     * @param input client input
     * @return lowercase header map
     * @throws IOException if the socket cannot be read
     */
    private static Map<String, String> readHttp1Headers(final InputStream input) throws IOException {
        final HashMap<String, String> headers = new HashMap<>();
        while (true) {
            final String line = readAsciiLine(input, MAX_HEADER_LINE_BYTES);
            if (line.isEmpty()) {
                return headers;
            }
            final int separator = line.indexOf(Symbol.C_COLON);
            if (separator <= 0) {
                throw new StatefulException(Http.Status.BAD_REQUEST, "HTTP " + Http.Status.BAD_REQUEST);
            }
            headers.put(line.substring(0, separator).toLowerCase(Locale.ROOT), line.substring(separator + 1).trim());
        }
    }

    /**
     * Parses a content-length value.
     *
     * @param value raw value
     * @return parsed length
     */
    private static int contentLength(final String value) {
        if (value == null) {
            throw new StatefulException(Http.Status.LENGTH_REQUIRED, "HTTP " + Http.Status.LENGTH_REQUIRED);
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (final NumberFormatException e) {
            throw new StatefulException(Http.Status.BAD_REQUEST, "HTTP " + Http.Status.BAD_REQUEST);
        }
    }

    /**
     * Completes a HEADERS block by consuming CONTINUATION frames when required.
     *
     * @param input   client input
     * @param header  first HEADERS frame header
     * @param payload first HEADERS frame payload
     * @return encoded HPACK block
     * @throws IOException if the socket cannot be read
     */
    private static byte[] completeHeaderBlock(
            final InputStream input,
            final Http2FrameHeader header,
            final byte[] payload) throws IOException {
        final ByteArrayOutputStream block = new ByteArrayOutputStream();
        block.write(headersPayload(header.flags, payload));
        int flags = header.flags;
        while ((flags & H2_FLAG_END_HEADERS) == 0) {
            final Http2FrameHeader continuation = Http2FrameHeader.read(input);
            if (continuation.type != H2_CONTINUATION || continuation.streamId != header.streamId) {
                throw new ProtocolException("HTTP/2 continuation frame is invalid");
            }
            final byte[] next = input.readNBytes(continuation.length);
            if (next.length != continuation.length) {
                throw new EOFException();
            }
            block.write(next);
            flags = continuation.flags;
        }
        return block.toByteArray();
    }

    /**
     * Extracts the HPACK payload from a HEADERS frame.
     *
     * @param flags   frame flags
     * @param payload frame payload
     * @return HPACK bytes
     */
    private static byte[] headersPayload(final int flags, final byte[] payload) {
        int offset = 0;
        int length = payload.length;
        if ((flags & H2_FLAG_PADDED) != 0) {
            if (length == 0) {
                throw new ProtocolException("HTTP/2 padded headers frame is truncated");
            }
            final int padding = payload[0] & 0xff;
            offset++;
            length--;
            if (padding > length) {
                throw new ProtocolException("HTTP/2 header padding exceeds payload");
            }
            length -= padding;
        }
        if ((flags & H2_FLAG_PRIORITY) != 0) {
            if (length < 5) {
                throw new ProtocolException("HTTP/2 priority header is truncated");
            }
            offset += 5;
            length -= 5;
        }
        final byte[] block = new byte[length];
        System.arraycopy(payload, offset, block, 0, length);
        return block;
    }

    /**
     * Extracts the body payload from a DATA frame.
     *
     * @param flags   frame flags
     * @param payload frame payload
     * @return body bytes
     */
    private static byte[] dataPayload(final int flags, final byte[] payload) {
        int offset = 0;
        int length = payload.length;
        if ((flags & H2_FLAG_PADDED) != 0) {
            if (length == 0) {
                throw new ProtocolException("HTTP/2 padded data frame is truncated");
            }
            final int padding = payload[0] & 0xff;
            offset++;
            length--;
            if (padding > length) {
                throw new ProtocolException("HTTP/2 data padding exceeds payload");
            }
            length -= padding;
        }
        final byte[] data = new byte[length];
        System.arraycopy(payload, offset, data, 0, length);
        return data;
    }

    /**
     * Writes one HTTP/2 frame.
     *
     * @param output   client output
     * @param type     frame type
     * @param flags    frame flags
     * @param streamId stream identifier
     * @param payload  frame payload
     * @throws IOException if the socket cannot be written
     */
    private static void writeFrame(
            final OutputStream output,
            final int type,
            final int flags,
            final int streamId,
            final byte[] payload) throws IOException {
        output.write((payload.length >>> 16) & 0xff);
        output.write((payload.length >>> 8) & 0xff);
        output.write(payload.length & 0xff);
        output.write(type & 0xff);
        output.write(flags & 0xff);
        output.write((streamId >>> 24) & 0x7f);
        output.write((streamId >>> 16) & 0xff);
        output.write((streamId >>> 8) & 0xff);
        output.write(streamId & 0xff);
        output.write(payload);
    }

    /**
     * Writes an HTTP/2 RST_STREAM frame.
     *
     * @param output   client output
     * @param streamId stream identifier
     * @param error    HTTP/2 error code
     * @throws IOException if the socket cannot be written
     */
    private static void writeRstStream(final OutputStream output, final int streamId, final int error)
            throws IOException {
        final byte[] payload = intBytes(error);
        writeFrame(output, H2_RST_STREAM, 0, streamId, payload);
    }

    /**
     * Writes an HTTP/2 GOAWAY frame.
     *
     * @param output client output
     * @param error  HTTP/2 error code
     * @throws IOException if the socket cannot be written
     */
    private static void writeGoaway(final OutputStream output, final int error) throws IOException {
        final byte[] payload = new byte[8];
        final byte[] errorBytes = intBytes(error);
        System.arraycopy(errorBytes, 0, payload, 4, errorBytes.length);
        writeFrame(output, H2_GOAWAY, 0, 0, payload);
    }

    /**
     * Converts an integer to network-order bytes.
     *
     * @param value integer value
     * @return four-byte network-order representation
     */
    private static byte[] intBytes(final int value) {
        return new byte[] { (byte) ((value >>> 24) & 0xff), (byte) ((value >>> 16) & 0xff),
                (byte) ((value >>> 8) & 0xff), (byte) (value & 0xff) };
    }

    /**
     * Extracts one query parameter value from a raw query string.
     *
     * @param rawQuery raw query string
     * @param name     parameter name
     * @return decoded parameter value, or {@code null}
     */
    private static String queryParameter(final String rawQuery, final String name) {
        if (rawQuery == null || rawQuery.isEmpty()) {
            return null;
        }
        for (final String parameter : rawQuery.split("&")) {
            final int separator = parameter.indexOf('=');
            final String parameterName = separator < 0 ? parameter : parameter.substring(0, separator);
            if (name.equals(URLDecoder.decode(parameterName, StandardCharsets.UTF_8))) {
                return separator < 0 ? "" : parameter.substring(separator + 1);
            }
        }
        return null;
    }

    /**
     * Returns a small HTTP status body.
     *
     * @param status HTTP status code
     * @return response body
     */
    private static byte[] httpStatusBody(final int status) {
        return ("HTTP " + status).getBytes(StandardCharsets.US_ASCII);
    }

    /**
     * Returns a reason phrase for generated HTTP/1.1 responses.
     *
     * @param status HTTP status code
     * @return reason phrase
     */
    private static String reason(final int status) {
        return switch (status) {
            case Http.Status.OK -> "OK";
            case Http.Status.BAD_REQUEST -> "Bad Request";
            case Http.Status.NOT_FOUND -> "Not Found";
            case Http.Status.METHOD_NOT_ALLOWED -> "Method Not Allowed";
            case Http.Status.LENGTH_REQUIRED -> "Length Required";
            case Http.Status.CONTENT_TOO_LARGE -> "Payload Too Large";
            case Http.Status.UNSUPPORTED_MEDIA_TYPE -> "Unsupported Media Type";
            default -> "Error";
        };
    }

    /**
     * Returns whether two byte arrays are equal.
     *
     * @param left  first array
     * @param right second array
     * @return true when arrays have identical bytes
     */
    private static boolean matches(final byte[] left, final byte[] right) {
        if (left.length != right.length) {
            return false;
        }
        for (int index = 0; index < left.length; index++) {
            if (left[index] != right[index]) {
                return false;
            }
        }
        return true;
    }

    /**
     * DNS resolver callback used by the DoH transport.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @FunctionalInterface
    public interface Resolver {

        /**
         * Resolves one DNS query.
         *
         * @param request       DNS query wire bytes
         * @param clientAddress remote client address
         * @return DNS response wire bytes
         */
        byte[] resolve(byte[] request, InetAddress clientAddress);

    }

    /**
     * Parsed HTTP/1.1 request line.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private static final class Http1RequestLine {

        /**
         * HTTP method.
         */
        private final Http.Method method;

        /**
         * Request target.
         */
        private final String target;

        /**
         * Creates a request-line value.
         *
         * @param method HTTP method
         * @param target request target
         */
        private Http1RequestLine(final Http.Method method, final String target) {
            this.method = method;
            this.target = target;
        }

        /**
         * Parses one HTTP/1.1 request line.
         *
         * @param line ASCII request line
         * @return parsed request line
         */
        private static Http1RequestLine parse(final String line) {
            final String[] parts = line.split(Symbol.SPACE, 3);
            if (parts.length != 3) {
                throw new StatefulException(Http.Status.BAD_REQUEST, "HTTP " + Http.Status.BAD_REQUEST);
            }
            return new Http1RequestLine(Http.Method.of(parts[0]), parts[1]);
        }

    }

    /**
     * HTTP/2 frame header.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private static final class Http2FrameHeader {

        /**
         * Payload length.
         */
        private final int length;

        /**
         * Frame type.
         */
        private final int type;

        /**
         * Frame flags.
         */
        private final int flags;

        /**
         * Stream identifier.
         */
        private final int streamId;

        /**
         * Creates a frame header.
         *
         * @param length   payload length
         * @param type     frame type
         * @param flags    frame flags
         * @param streamId stream identifier
         */
        private Http2FrameHeader(final int length, final int type, final int flags, final int streamId) {
            this.length = length;
            this.type = type;
            this.flags = flags;
            this.streamId = streamId;
        }

        /**
         * Reads one HTTP/2 frame header.
         *
         * @param input client input
         * @return decoded frame header
         * @throws IOException if the socket cannot be read
         */
        private static Http2FrameHeader read(final InputStream input) throws IOException {
            final byte[] header = input.readNBytes(9);
            if (header.length == 0) {
                throw new EOFException();
            }
            if (header.length != 9) {
                throw new EOFException();
            }
            final int length = ((header[0] & 0xff) << 16) | ((header[1] & 0xff) << 8) | (header[2] & 0xff);
            if (length > DnsCodec.MAX_MESSAGE_BYTES) {
                throw new ProtocolException("HTTP/2 frame exceeds DNS DoH limit");
            }
            final int streamId = ((header[5] & 0x7f) << 24) | ((header[6] & 0xff) << 16) | ((header[7] & 0xff) << 8)
                    | (header[8] & 0xff);
            return new Http2FrameHeader(length, header[3] & 0xff, header[4] & 0xff, streamId);
        }

    }

    /**
     * Mutable HTTP/2 request state.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private static final class Http2RequestState {

        /**
         * Request headers keyed by lowercase name.
         */
        private final Map<String, String> headers;

        /**
         * Request body bytes.
         */
        private final ByteArrayOutputStream body;

        /**
         * Creates request state.
         *
         * @param headers request headers
         */
        private Http2RequestState(final Map<String, String> headers) {
            this.headers = headers;
            this.body = new ByteArrayOutputStream();
        }

        /**
         * Creates request state from decoded headers.
         *
         * @param decoded decoded header list
         * @return request state
         */
        private static Http2RequestState from(final List<Http2Header> decoded) {
            final HashMap<String, String> headers = new HashMap<>();
            for (final Http2Header header : decoded) {
                headers.put(header.name().toLowerCase(Locale.ROOT), header.value());
            }
            return new Http2RequestState(headers);
        }

        /**
         * Appends DATA bytes.
         *
         * @param data body bytes
         */
        private void append(final byte[] data) {
            if (body.size() + data.length > DnsCodec.MAX_MESSAGE_BYTES) {
                throw new StatefulException(Http.Status.CONTENT_TOO_LARGE, "HTTP " + Http.Status.CONTENT_TOO_LARGE);
            }
            body.writeBytes(data);
        }

        /**
         * Returns the HTTP method.
         *
         * @return HTTP method
         */
        private Http.Method method() {
            final String value = headers.get(":method");
            if (value == null) {
                throw new StatefulException(Http.Status.BAD_REQUEST, "HTTP " + Http.Status.BAD_REQUEST);
            }
            return Http.Method.of(value);
        }

        /**
         * Returns the request path.
         *
         * @return request path
         */
        private String path() {
            final String value = headers.get(":path");
            if (value == null) {
                throw new StatefulException(Http.Status.BAD_REQUEST, "HTTP " + Http.Status.BAD_REQUEST);
            }
            return value;
        }

    }

}
