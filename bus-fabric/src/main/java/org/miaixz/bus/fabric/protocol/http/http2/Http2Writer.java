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

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.HTTP;
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
     * Default maximum frame payload size.
     */
    private static final int MAX_FRAME_SIZE = Normal._16384;

    /**
     * Maximum unsigned 31-bit value.
     */
    private static final long MAX_UNSIGNED_31 = Integer.MAX_VALUE;

    /**
     * Initial write window defined by HTTP/2 before peer settings change it.
     */
    private static final long DEFAULT_WINDOW = HTTP.DEFAULT_INITIAL_WINDOW_SIZE;

    /**
     * Fallback write timeout used until a request-specific timeout is supplied.
     */
    private static final Duration DEFAULT_WRITE_TIMEOUT = Duration.ofSeconds(Normal._5);

    /**
     * HTTP/2 client connection preface bytes.
     */
    private static final byte[] CONNECTION_PREFACE = {'P', 'R', 'I', Symbol.C_SPACE, Symbol.C_STAR, Symbol.C_SPACE,
            'H', 'T', 'T', 'P', Symbol.C_SLASH, Symbol.C_TWO, Symbol.C_DOT, Symbol.C_ZERO, Symbol.C_CR, Symbol.C_LF,
            Symbol.C_CR, Symbol.C_LF, 'S', 'M', Symbol.C_CR, Symbol.C_LF, Symbol.C_CR, Symbol.C_LF};

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
        final Duration checkedTimeout = require(timeout, "HTTP/2 writer timeout");
        if (checkedTimeout.isNegative()) {
            throw new ValidateException("HTTP/2 writer timeout must be non-negative");
        }
        this.writeTimeout = checkedTimeout;
    }

    /**
     * Writes the client connection preface.
     */
    public synchronized void connectionPreface() {
        ensureOpen();
        if (!prefaceWritten.compareAndSet(false, true)) {
            throw new StatefulException("HTTP/2 connection preface has already been written");
        }
        write(new Buffer().write(CONNECTION_PREFACE));
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
        final Headers checkedHeaders = require(headers, "HTTP/2 headers");
        final Buffer payload = hpack.encodeBuffer(toHttp2(checkedHeaders));
        writeFrame(
                Http2Frame.HEADERS,
                streamId,
                Http2Frame.END_HEADERS | (endStream ? Http2Frame.END_STREAM : Normal._0),
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
        final Headers checkedHeaders = require(headers, "HTTP/2 push headers");
        final Buffer encoded = hpack.encodeBuffer(toHttp2(checkedHeaders));
        final Buffer payload = new Buffer();
        payload.writeInt(promisedStreamId & (int) MAX_UNSIGNED_31);
        payload.write(encoded, encoded.size());
        writeFrame(Http2Frame.PUSH_PROMISE, streamId, Http2Frame.END_HEADERS, payload);
    }

    /**
     * Writes DATA frames.
     *
     * @param streamId  stream id
     * @param data      data
     * @param endStream end stream flag
     */
    public synchronized void data(final int streamId, final Buffer data, final boolean endStream) {
        ensureOpen();
        positiveStream(streamId);
        final Buffer checkedData = require(data, "HTTP/2 data");
        consumeWindow(streamId, toIntSize(checkedData.size()));
        if (checkedData.size() == Normal._0) {
            writeFrame(Http2Frame.DATA, streamId, endStream ? Http2Frame.END_STREAM : Normal._0, new Buffer());
            return;
        }
        while (checkedData.size() > Normal._0) {
            final long count = Math.min(checkedData.size(), MAX_FRAME_SIZE);
            final Buffer payload = new Buffer();
            payload.write(checkedData, count);
            final boolean last = checkedData.size() == Normal._0;
            final int flags = last && endStream ? Http2Frame.END_STREAM : Normal._0;
            writeFrame(Http2Frame.DATA, streamId, flags, payload);
        }
    }

    /**
     * Writes a SETTINGS frame.
     *
     * @param settings settings
     */
    public synchronized void settings(final Http2Settings settings) {
        ensureOpen();
        final Http2Settings checkedSettings = require(settings, "HTTP/2 settings");
        final int[] ids = checkedSettings.ids();
        final Buffer payload = new Buffer();
        for (final int id : ids) {
            payload.writeShort(id);
            payload.writeInt(checkedSettings.get(id));
        }
        writeFrame(Http2Frame.SETTINGS, Normal._0, Normal._0, payload);
    }

    /**
     * Writes a SETTINGS ACK frame.
     */
    public synchronized void settingsAck() {
        ensureOpen();
        writeFrame(Http2Frame.SETTINGS, Normal._0, Http2Frame.ACK, new Buffer());
    }

    /**
     * Writes a PING or PING ACK frame.
     *
     * @param payload opaque payload
     * @param ack     true for ACK
     */
    public synchronized void ping(final long payload, final boolean ack) {
        ensureOpen();
        final Buffer body = new Buffer();
        body.writeLong(payload);
        writeFrame(Http2Frame.PING, Normal._0, ack ? Http2Frame.ACK : Normal._0, body);
    }

    /**
     * Writes a GOAWAY frame.
     *
     * @param lastStreamId last processed stream id
     * @param errorCode    error code
     * @param debugData    optional debug data
     */
    public synchronized void goAway(final int lastStreamId, final int errorCode, final ByteString debugData) {
        ensureOpen();
        if (lastStreamId < Normal._0 || errorCode < Normal._0) {
            throw new ValidateException("Invalid HTTP/2 GOAWAY metadata");
        }
        final Buffer payload = new Buffer();
        payload.writeInt(lastStreamId & (int) MAX_UNSIGNED_31);
        payload.writeInt(errorCode);
        if (debugData != null) {
            payload.write(debugData);
        }
        writeFrame(Http2Frame.GOAWAY, Normal._0, Normal._0, payload);
    }

    /**
     * Writes a WINDOW_UPDATE frame.
     *
     * @param streamId stream id
     * @param delta    delta
     */
    public synchronized void windowUpdate(final int streamId, final long delta) {
        ensureOpen();
        if (streamId < Normal._0 || delta <= Normal._0 || delta > MAX_UNSIGNED_31) {
            throw new ValidateException("Invalid HTTP/2 window update");
        }
        final Buffer payload = new Buffer();
        payload.writeInt((int) delta);
        writeFrame(Http2Frame.WINDOW_UPDATE, streamId, Normal._0, payload);
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
        if (errorCode < Normal._0) {
            throw new ValidateException("HTTP/2 error code must be non-negative");
        }
        final Buffer payload = new Buffer();
        payload.writeInt(errorCode);
        writeFrame(Http2Frame.RST_STREAM, streamId, Normal._0, payload);
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
    private void writeFrame(final int type, final int streamId, final int flags, final Buffer payload) {
        final Buffer body = require(payload, "HTTP/2 frame payload");
        if (body.size() > MAX_FRAME_SIZE) {
            throw new ProtocolException("HTTP/2 frame payload exceeds max frame size");
        }
        final int length = toIntSize(body.size());
        final Buffer header = new Buffer();
        header.writeByte((length >>> Normal._16) & 0xff);
        header.writeByte((length >>> Normal._8) & 0xff);
        header.writeByte(length & 0xff);
        header.writeByte(type);
        header.writeByte(flags);
        header.writeInt(streamId & (int) MAX_UNSIGNED_31);
        write(header);
        write(body);
    }

    /**
     * Consumes write windows.
     *
     * @param streamId stream id
     * @param length   length
     */
    private void consumeWindow(final int streamId, final int length) {
        if (length == Normal._0) {
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
    private void write(final Buffer source) {
        while (source.size() > Normal._0) {
            final ByteBuffer view = source.nioBuffer(toIntSize(Math.min(source.size(), Integer.MAX_VALUE)));
            final int written = await(connection.write(view));
            if (written < Normal._0) {
                throw new SocketException("HTTP/2 writer reached EOF");
            }
            if (written == Normal._0) {
                Thread.yield();
            } else {
                try {
                    source.skip(written);
                } catch (final java.io.IOException e) {
                    throw new InternalException("Unable to consume written HTTP/2 bytes", e);
                }
            }
        }
    }

    /**
     * Converts a buffer size to int.
     *
     * @param size size
     * @return int size
     */
    private static int toIntSize(final long size) {
        if (size > Integer.MAX_VALUE) {
            throw new ProtocolException("HTTP/2 buffer exceeds integer range");
        }
        return (int) size;
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
        if (streamId <= Normal._0) {
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
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

}
