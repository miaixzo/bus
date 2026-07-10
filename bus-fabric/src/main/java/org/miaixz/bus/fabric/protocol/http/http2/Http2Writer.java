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
package org.miaixz.bus.fabric.protocol.http.http2;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.network.Connection;

/**
 * Serial HTTP/2 frame writer.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Http2Writer implements AutoCloseable {

    /**
     * Frame header size.
     */
    private static final int FRAME_HEADER = 9;

    /**
     * Default maximum frame payload size.
     */
    private static final int MAX_FRAME_SIZE = 16_384;

    /**
     * Maximum unsigned 31-bit value.
     */
    private static final long MAX_UNSIGNED_31 = 0x7fffffffL;

    /**
     * Initial write window defined by HTTP/2 before peer settings change it.
     */
    private static final long DEFAULT_WINDOW = 65_535L;

    /**
     * Fallback write timeout used until a request-specific timeout is supplied.
     */
    private static final Duration DEFAULT_WRITE_TIMEOUT = Duration.ofSeconds(5);

    /**
     * HTTP/2 client connection preface bytes.
     */
    private static final byte[] CONNECTION_PREFACE = { 'P', 'R', 'I', Symbol.C_SPACE, Symbol.C_STAR, Symbol.C_SPACE,
            'H', 'T', 'T', 'P', Symbol.C_SLASH, Symbol.C_TWO, Symbol.C_DOT, Symbol.C_ZERO, Symbol.C_CR, Symbol.C_LF,
            Symbol.C_CR, Symbol.C_LF, 'S', 'M', Symbol.C_CR, Symbol.C_LF, Symbol.C_CR, Symbol.C_LF };

    /**
     * Network connection.
     */
    private final Connection connection;

    /**
     * Header block codec.
     */
    private final HpackCodec hpack;

    /**
     * Stream windows.
     */
    private final ConcurrentHashMap<Integer, AtomicLong> streamWindows;

    /**
     * Connection window.
     */
    private final AtomicLong connectionWindow;

    /**
     * Writer state.
     */
    private final AtomicReference<Status> state;

    /**
     * Preface written flag.
     */
    private final AtomicBoolean prefaceWritten;

    /**
     * Write timeout.
     */
    private volatile Duration writeTimeout;

    /**
     * Creates a writer.
     *
     * @param connection connection
     */
    public Http2Writer(final Http2Connection connection) {
        this.connection = require(connection, "HTTP/2 connection").network();
        this.hpack = new HpackCodec();
        this.streamWindows = new ConcurrentHashMap<>();
        this.connectionWindow = new AtomicLong(DEFAULT_WINDOW);
        this.state = new AtomicReference<>(Status.OPENED);
        this.prefaceWritten = new AtomicBoolean();
        this.writeTimeout = DEFAULT_WRITE_TIMEOUT;
    }

    /**
     * Sets the write timeout used while waiting for network writes.
     *
     * @param timeout write timeout; zero means no explicit timeout
     */
    public synchronized void timeout(final Duration timeout) {
        if (timeout == null || timeout.isNegative()) {
            throw new ValidateException("HTTP/2 writer timeout must be non-null and non-negative");
        }
        this.writeTimeout = timeout;
    }

    /**
     * Writes the client connection preface.
     */
    public synchronized void connectionPreface() {
        ensureOpen();
        if (!prefaceWritten.compareAndSet(false, true)) {
            throw new StatefulException("HTTP/2 connection preface has already been written");
        }
        write(ByteBuffer.wrap(CONNECTION_PREFACE));
    }

    /**
     * Writes a HEADERS frame.
     *
     * @param streamId  stream id
     * @param headers   headers
     * @param endStream end stream flag
     */
    public synchronized void headers(final int streamId, final Headers headers, final boolean endStream) {
        ensureOpen();
        positiveStream(streamId);
        if (headers == null) {
            throw new ValidateException("HTTP/2 headers must not be null");
        }
        final ByteBuffer payload = hpack.encode(toHttp2(headers));
        writeFrame(
                Http2Frame.HEADERS,
                streamId,
                Http2Frame.END_HEADERS | (endStream ? Http2Frame.END_STREAM : 0),
                payload);
    }

    /**
     * Writes a PUSH_PROMISE frame.
     *
     * @param streamId         associated stream id
     * @param promisedStreamId promised stream id
     * @param headers          promised request headers
     */
    public synchronized void pushPromise(final int streamId, final int promisedStreamId, final Headers headers) {
        ensureOpen();
        positiveStream(streamId);
        positiveStream(promisedStreamId);
        if (headers == null) {
            throw new ValidateException("HTTP/2 push headers must not be null");
        }
        final ByteBuffer encoded = hpack.encode(toHttp2(headers));
        final ByteBuffer payload = ByteBuffer.allocate(Integer.BYTES + encoded.remaining());
        payload.putInt(promisedStreamId & (int) MAX_UNSIGNED_31);
        payload.put(encoded.asReadOnlyBuffer());
        payload.flip();
        writeFrame(Http2Frame.PUSH_PROMISE, streamId, Http2Frame.END_HEADERS, payload);
    }

    /**
     * Writes DATA frames.
     *
     * @param streamId  stream id
     * @param data      data
     * @param endStream end stream flag
     */
    public synchronized void data(final int streamId, final ByteBuffer data, final boolean endStream) {
        ensureOpen();
        positiveStream(streamId);
        if (data == null) {
            throw new ValidateException("HTTP/2 data must not be null");
        }
        final ByteBuffer source = data.asReadOnlyBuffer();
        consumeWindow(streamId, source.remaining());
        if (!source.hasRemaining()) {
            writeFrame(Http2Frame.DATA, streamId, endStream ? Http2Frame.END_STREAM : 0, ByteBuffer.allocate(0));
            return;
        }
        while (source.hasRemaining()) {
            final int count = Math.min(source.remaining(), MAX_FRAME_SIZE);
            final byte[] bytes = new byte[count];
            source.get(bytes);
            final boolean last = !source.hasRemaining();
            final int flags = last && endStream ? Http2Frame.END_STREAM : 0;
            writeFrame(Http2Frame.DATA, streamId, flags, ByteBuffer.wrap(bytes));
        }
    }

    /**
     * Writes a SETTINGS frame.
     *
     * @param settings settings
     */
    public synchronized void settings(final Http2Settings settings) {
        ensureOpen();
        if (settings == null) {
            throw new ValidateException("HTTP/2 settings must not be null");
        }
        final int[] ids = settings.ids();
        final ByteBuffer payload = ByteBuffer.allocate(ids.length * 6);
        for (final int id : ids) {
            payload.putShort((short) id);
            payload.putInt(settings.get(id));
        }
        payload.flip();
        writeFrame(Http2Frame.SETTINGS, 0, 0, payload);
    }

    /**
     * Writes a SETTINGS ACK frame.
     */
    public synchronized void settingsAck() {
        ensureOpen();
        writeFrame(Http2Frame.SETTINGS, 0, Http2Frame.ACK, ByteBuffer.allocate(0));
    }

    /**
     * Writes a PING or PING ACK frame.
     *
     * @param payload opaque payload
     * @param ack     true for ACK
     */
    public synchronized void ping(final long payload, final boolean ack) {
        ensureOpen();
        final ByteBuffer body = ByteBuffer.allocate(Long.BYTES).putLong(payload);
        body.flip();
        writeFrame(Http2Frame.PING, 0, ack ? Http2Frame.ACK : 0, body);
    }

    /**
     * Writes a GOAWAY frame.
     *
     * @param lastStreamId last processed stream id
     * @param errorCode    error code
     * @param debugData    optional debug data
     */
    public synchronized void goAway(final int lastStreamId, final int errorCode, final ByteBuffer debugData) {
        ensureOpen();
        if (lastStreamId < 0 || errorCode < 0) {
            throw new ValidateException("Invalid HTTP/2 GOAWAY metadata");
        }
        final ByteBuffer debug = debugData == null ? ByteBuffer.allocate(0) : debugData.asReadOnlyBuffer();
        final ByteBuffer payload = ByteBuffer.allocate(Integer.BYTES + Integer.BYTES + debug.remaining());
        payload.putInt(lastStreamId & (int) MAX_UNSIGNED_31);
        payload.putInt(errorCode);
        payload.put(debug);
        payload.flip();
        writeFrame(Http2Frame.GOAWAY, 0, 0, payload);
    }

    /**
     * Writes a WINDOW_UPDATE frame.
     *
     * @param streamId stream id
     * @param delta    delta
     */
    public synchronized void windowUpdate(final int streamId, final long delta) {
        ensureOpen();
        if (streamId < 0 || delta <= 0 || delta > MAX_UNSIGNED_31) {
            throw new ValidateException("Invalid HTTP/2 window update");
        }
        final ByteBuffer payload = ByteBuffer.allocate(Integer.BYTES).putInt((int) delta);
        payload.flip();
        writeFrame(Http2Frame.WINDOW_UPDATE, streamId, 0, payload);
    }

    /**
     * Writes an RST_STREAM frame.
     *
     * @param streamId  stream id
     * @param errorCode error code
     */
    public synchronized void rstStream(final int streamId, final int errorCode) {
        ensureOpen();
        positiveStream(streamId);
        if (errorCode < 0) {
            throw new ValidateException("HTTP/2 error code must be non-negative");
        }
        final ByteBuffer payload = ByteBuffer.allocate(Integer.BYTES).putInt(errorCode);
        payload.flip();
        writeFrame(Http2Frame.RST_STREAM, streamId, 0, payload);
    }

    /**
     * Closes this writer.
     */
    @Override
    public synchronized void close() {
        final Status current = state.get();
        if (current == Status.CLOSED) {
            return;
        }
        if (!current.canTransit(Status.CLOSING)) {
            throw new StatefulException("HTTP/2 writer cannot close from state " + current);
        }
        state.set(Status.CLOSING);
        RuntimeException failure = null;
        try {
            connection.close();
        } catch (final RuntimeException e) {
            failure = e;
        }
        state.set(Status.CLOSED);
        if (failure != null) {
            throw closeFailure(failure);
        }
    }

    /**
     * Writes a frame.
     *
     * @param type     type
     * @param streamId stream id
     * @param flags    flags
     * @param payload  payload
     */
    private void writeFrame(final int type, final int streamId, final int flags, final ByteBuffer payload) {
        if (payload.remaining() > MAX_FRAME_SIZE) {
            throw new ProtocolException("HTTP/2 frame payload exceeds max frame size");
        }
        final ByteBuffer header = ByteBuffer.allocate(FRAME_HEADER);
        final int length = payload.remaining();
        header.put((byte) ((length >>> 16) & 0xff));
        header.put((byte) ((length >>> 8) & 0xff));
        header.put((byte) (length & 0xff));
        header.put((byte) type);
        header.put((byte) flags);
        header.putInt(streamId & (int) MAX_UNSIGNED_31);
        header.flip();
        write(header);
        write(payload);
    }

    /**
     * Consumes write windows.
     *
     * @param streamId stream id
     * @param length   length
     */
    private void consumeWindow(final int streamId, final int length) {
        if (length == 0) {
            return;
        }
        final AtomicLong streamWindow = streamWindows.computeIfAbsent(streamId, id -> new AtomicLong(DEFAULT_WINDOW));
        if (!subtractWindow(connectionWindow, length)) {
            throw new TimeoutException("HTTP/2 write window is exhausted");
        }
        if (!subtractWindow(streamWindow, length)) {
            connectionWindow.addAndGet(length);
            throw new TimeoutException("HTTP/2 write window is exhausted");
        }
    }

    /**
     * Subtracts from a window.
     *
     * @param window window
     * @param length length
     * @return true when available
     */
    private static boolean subtractWindow(final AtomicLong window, final long length) {
        long current;
        do {
            current = window.get();
            if (current < length) {
                return false;
            }
        } while (!window.compareAndSet(current, current - length));
        return true;
    }

    /**
     * Converts root headers to HTTP/2 headers.
     *
     * @param headers headers
     * @return HTTP/2 headers
     */
    private static List<Http2Header> toHttp2(final Headers headers) {
        final ArrayList<Http2Header> values = new ArrayList<>();
        for (final Map.Entry<String, List<String>> entry : headers.asMap().entrySet()) {
            for (final String value : entry.getValue()) {
                values.add(Http2Header.of(entry.getKey().toLowerCase(Locale.ROOT), value));
            }
        }
        return List.copyOf(values);
    }

    /**
     * Writes bytes.
     *
     * @param source source
     */
    private void write(final ByteBuffer source) {
        while (source.hasRemaining()) {
            final int position = source.position();
            final int written = await(connection.write(source));
            if (written < 0) {
                throw new SocketException("HTTP/2 writer reached EOF");
            }
            if (written == 0) {
                Thread.yield();
            } else {
                source.position(position + written);
            }
        }
    }

    /**
     * Waits for IO.
     *
     * @param future future
     * @return result
     */
    private int await(final CompletableFuture<Integer> future) {
        try {
            final Duration timeout = writeTimeout;
            return timeout.isZero() ? future.get() : future.get(timeout.toNanos(), TimeUnit.NANOSECONDS);
        } catch (final java.util.concurrent.TimeoutException e) {
            throw new TimeoutException("HTTP/2 writer timed out", e);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InternalException("Interrupted while waiting for HTTP/2 writer", e);
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new SocketException("HTTP/2 writer failed", cause);
        } catch (final ArithmeticException e) {
            throw new ValidateException("HTTP/2 writer timeout is too large");
        }
    }

    /**
     * Ensures writer is open.
     */
    private void ensureOpen() {
        if (state.get().terminal()) {
            throw new StatefulException("HTTP/2 writer is closed");
        }
    }

    /**
     * Validates stream id.
     *
     * @param streamId stream id
     */
    private static void positiveStream(final int streamId) {
        if (streamId <= 0) {
            throw new ValidateException("HTTP/2 stream id must be positive");
        }
    }

    /**
     * Classifies close failure.
     *
     * @param failure failure
     * @return runtime failure
     */
    private static RuntimeException closeFailure(final RuntimeException failure) {
        if (failure instanceof SocketException || failure instanceof InternalException
                || failure instanceof StatefulException) {
            return failure;
        }
        return new InternalException("Unable to close HTTP/2 writer", failure);
    }

    /**
     * Validates required value.
     *
     * @param value value
     * @param name  name
     * @param <T>   type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value;
    }

}
