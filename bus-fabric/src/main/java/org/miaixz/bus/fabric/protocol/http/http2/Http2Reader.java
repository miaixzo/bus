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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.network.Connection;

/**
 * Streaming HTTP/2 frame reader.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Http2Reader implements AutoCloseable {

    /**
     * Frame header size.
     */
    private static final int FRAME_HEADER = Normal._9;

    /**
     * Default maximum frame payload size.
     */
    private static final int MAX_FRAME_SIZE = Normal._16384;

    /**
     * Maximum accumulated HPACK header block size.
     */
    private static final int MAX_HEADER_BLOCK_SIZE = Normal._64 * Normal._1024;

    /**
     * Maximum stream identifier.
     */
    private static final int MAX_STREAM_ID = Integer.MAX_VALUE;

    /**
     * SETTINGS ACK flag.
     */
    private static final int SETTINGS_ACK = Http2Frame.ACK;

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
     * Reader lifecycle state.
     */
    private final AtomicReference<Status> state;

    /**
     * Preface read flag.
     */
    private final AtomicBoolean prefaceRead;

    /**
     * Creates a reader.
     *
     * @param connection connection
     */
    public Http2Reader(final Http2Connection connection) {
        this(require(connection, "HTTP/2 connection").network());
    }

    /**
     * Creates a reader.
     *
     * @param connection connection
     */
    Http2Reader(final Connection connection) {
        this.connection = require(connection, "Network connection");
        this.hpack = new HpackCodec();
        this.state = new AtomicReference<>(Status.OPENED);
        this.prefaceRead = new AtomicBoolean();
    }

    /**
     * Reads the next complete HTTP/2 frame.
     *
     * @return frame
     */
    public Http2Frame nextFrame() {
        ensureOpen();
        final Buffer header = readFully(FRAME_HEADER);
        final int length = readMedium(header);
        final int type = header.readByte() & 0xff;
        final int flags = header.readByte() & 0xff;
        final int streamId = header.readInt() & MAX_STREAM_ID;
        validateFrame(type, streamId, flags, length);
        ByteString payload = readFully(length).readByteString();
        Http2Priority priority = null;
        Http2AlternateService alternateService = null;
        final List<Http2Header> headers = switch (type) {
            case Http2Frame.HEADERS -> {
                priority = decodeHeaderPriority(streamId, flags, payload);
                payload = headerBlock(streamId, flags, headerFragment(flags, payload));
                yield hpack.decode(new Buffer().write(payload));
            }
            case Http2Frame.PUSH_PROMISE -> {
                payload = headerBlock(streamId, flags, payload);
                yield hpack.decode(new Buffer().write(pushHeaderBlock(payload)));
            }
            case Http2Frame.PRIORITY -> {
                priority = Http2Priority.decode(payload, streamId);
                yield List.of();
            }
            case Http2Frame.ALTSVC -> {
                alternateService = Http2AlternateService.decode(payload, streamId);
                yield List.of();
            }
            default -> List.of();
        };
        final int decodedFlags = type == Http2Frame.HEADERS || type == Http2Frame.PUSH_PROMISE
                ? flags | Http2Frame.END_HEADERS
                : flags;
        final Http2Frame frame = Http2Frame
                .decoded(type, streamId, decodedFlags, payload, headers, priority, alternateService);
        if (type == Http2Frame.SETTINGS && !frame.ack()) {
            applySettings(frame.settings());
        }
        return frame;
    }

    /**
     * Applies peer header-compression limits to this standalone reader.
     *
     * @param settings settings
     */
    void applySettings(final Http2Settings settings) {
        if (settings == null) {
            return;
        }
        hpack.maxTableSize(settings.headerTableSize());
        if (settings.isSet(Http2Settings.MAX_HEADER_LIST_SIZE)) {
            hpack.maxHeaderListSize(settings.maxHeaderListSize());
        }
    }

    /**
     * Reads the HTTP/2 client connection preface.
     */
    public void readConnectionPreface() {
        ensureOpen();
        if (prefaceRead.get()) {
            throw new StatefulException("HTTP/2 connection preface has already been read");
        }
        final Buffer actual = readFully(CONNECTION_PREFACE.length);
        for (final byte expected : CONNECTION_PREFACE) {
            if (actual.size() == Normal._0 || actual.readByte() != expected) {
                throw new ProtocolException("Invalid HTTP/2 connection preface");
            }
        }
        prefaceRead.set(true);
    }

    /**
     * Closes this reader.
     */
    @Override
    public synchronized void close() {
        final Status current = state.get();
        if (current == Status.CLOSED) {
            return;
        }
        if (!current.canTransit(Status.CLOSING)) {
            throw new StatefulException("HTTP/2 reader cannot close from state " + current);
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
     * Reads an exact byte count into a core buffer.
     *
     * @param length length
     * @return buffer
     */
    private Buffer readFully(final int length) {
        final Buffer buffer = new Buffer();
        while (buffer.size() < length) {
            final int remaining = (int) Math.min(length - buffer.size(), MAX_FRAME_SIZE);
            final ByteBuffer chunk = ByteBuffer.allocate(remaining);
            final int position = chunk.position();
            final int read = await(connection.read(chunk));
            if (read < Normal._0) {
                throw new SocketException("HTTP/2 reader reached EOF");
            }
            if (read == Normal._0) {
                Thread.yield();
            } else {
                chunk.position(position + read);
                chunk.flip();
                try {
                    buffer.write(chunk);
                } catch (final java.io.IOException e) {
                    throw new InternalException("Unable to buffer HTTP/2 reader bytes", e);
                }
            }
        }
        return buffer;
    }

    /**
     * Validates supported frame metadata.
     *
     * @param type     type
     * @param streamId stream id
     * @param flags    flags
     * @param length   length
     */
    private static void validateFrame(final int type, final int streamId, final int flags, final int length) {
        if (streamId < Normal._0 || streamId > MAX_STREAM_ID || flags < Normal._0 || flags > 0xff || length < Normal._0
                || length > MAX_FRAME_SIZE) {
            throw new ProtocolException("Invalid HTTP/2 frame metadata");
        }
        if (type == Http2Frame.SETTINGS) {
            if (streamId != Normal._0 || (flags & ~SETTINGS_ACK) != Normal._0
                    || ((flags & SETTINGS_ACK) != Normal._0 && length != Normal._0)
                    || length % Normal._6 != Normal._0) {
                throw new ProtocolException("Invalid HTTP/2 SETTINGS frame");
            }
            return;
        }
        if (type == Http2Frame.PING) {
            if (streamId != Normal._0 || (flags & ~Http2Frame.ACK) != Normal._0 || length != Normal._8) {
                throw new ProtocolException("Invalid HTTP/2 PING frame");
            }
            return;
        }
        if (type == Http2Frame.GOAWAY) {
            if (streamId != Normal._0 || flags != Normal._0 || length < Normal._4 * Normal._2) {
                throw new ProtocolException("Invalid HTTP/2 GOAWAY frame");
            }
            return;
        }
        if (type == Http2Frame.WINDOW_UPDATE) {
            if (flags != Normal._0 || length != Normal._4) {
                throw new ProtocolException("Invalid HTTP/2 WINDOW_UPDATE frame");
            }
            return;
        }
        if (type == Http2Frame.ALTSVC) {
            if (flags != Normal._0 || length < Normal._2) {
                throw new ProtocolException("Invalid HTTP/2 ALTSVC frame");
            }
            return;
        }
        if (streamId <= Normal._0) {
            throw new ProtocolException("Invalid HTTP/2 stream frame id");
        }
        switch (type) {
            case Http2Frame.DATA -> validateFlags(flags, Http2Frame.END_STREAM);
            case Http2Frame.HEADERS -> {
                validateFlags(flags, Http2Frame.END_STREAM | Http2Frame.END_HEADERS | Http2Frame.PRIORITY_FLAG);
                if ((flags & Http2Frame.PRIORITY_FLAG) != Normal._0 && length < Http2Priority.LENGTH) {
                    throw new ProtocolException("Invalid HTTP/2 HEADERS priority payload");
                }
            }
            case Http2Frame.PRIORITY -> {
                validateFlags(flags, Normal._0);
                if (length != Http2Priority.LENGTH) {
                    throw new ProtocolException("Invalid HTTP/2 PRIORITY length");
                }
            }
            case Http2Frame.PUSH_PROMISE -> {
                validateFlags(flags, Http2Frame.END_HEADERS);
                if (length < Normal._4) {
                    throw new ProtocolException("Invalid HTTP/2 PUSH_PROMISE frame");
                }
            }
            case Http2Frame.RST_STREAM -> {
                validateFlags(flags, Normal._0);
                if (length != Normal._4) {
                    throw new ProtocolException("Invalid HTTP/2 RST_STREAM length");
                }
            }
            default -> throw new ProtocolException("Unsupported HTTP/2 frame type");
        }
    }

    /**
     * Decodes optional HEADERS priority metadata.
     *
     * @param streamId stream id
     * @param flags    flags
     * @param payload  original HEADERS payload
     * @return priority or null
     */
    private static Http2Priority decodeHeaderPriority(final int streamId, final int flags, final ByteString payload) {
        if ((flags & Http2Frame.PRIORITY_FLAG) == Normal._0) {
            return null;
        }
        return Http2Priority.decode(payload, streamId);
    }

    /**
     * Returns the HPACK fragment from a HEADERS payload.
     *
     * @param flags   flags
     * @param payload original HEADERS payload
     * @return header fragment
     */
    private static ByteString headerFragment(final int flags, final ByteString payload) {
        if ((flags & Http2Frame.PRIORITY_FLAG) != Normal._0) {
            return payload.substring(Http2Priority.LENGTH);
        }
        return payload;
    }

    /**
     * Reads CONTINUATION frames until the header block is complete.
     *
     * @param streamId stream id
     * @param flags    first frame flags
     * @param first    first header block fragment
     * @return complete header block payload
     */
    private ByteString headerBlock(final int streamId, final int flags, final ByteString first) {
        if ((flags & Http2Frame.END_HEADERS) != Normal._0) {
            if (first.size() > MAX_HEADER_BLOCK_SIZE) {
                throw new ProtocolException("HTTP/2 header block exceeds max size");
            }
            return first;
        }
        final Buffer fragments = new Buffer();
        int total = appendHeaderFragment(fragments, first, Normal._0);
        int currentFlags = flags;
        while ((currentFlags & Http2Frame.END_HEADERS) == Normal._0) {
            final Buffer header = readFully(FRAME_HEADER);
            final int length = readMedium(header);
            final int type = header.readByte() & 0xff;
            currentFlags = header.readByte() & 0xff;
            final int continuationStreamId = header.readInt() & MAX_STREAM_ID;
            validateContinuation(streamId, type, continuationStreamId, currentFlags, length);
            total = appendHeaderFragment(fragments, readFully(length).readByteString(), total);
        }
        return fragments.readByteString();
    }

    /**
     * Adds a header block fragment and checks the accumulated size.
     *
     * @param fragments fragments
     * @param fragment  new fragment
     * @param total     current total
     * @return updated total
     */
    private static int appendHeaderFragment(final Buffer fragments, final ByteString fragment, final int total) {
        final int next = total + fragment.size();
        if (next < total || next > MAX_HEADER_BLOCK_SIZE) {
            throw new ProtocolException("HTTP/2 header block exceeds max size");
        }
        fragments.write(fragment);
        return next;
    }

    /**
     * Validates a CONTINUATION frame while reading a header block.
     *
     * @param expectedStreamId expected stream id
     * @param type             frame type
     * @param streamId         frame stream id
     * @param flags            frame flags
     * @param length           payload length
     */
    private static void validateContinuation(
            final int expectedStreamId,
            final int type,
            final int streamId,
            final int flags,
            final int length) {
        if (type != Http2Frame.CONTINUATION || streamId != expectedStreamId || streamId <= Normal._0
                || length < Normal._0 || length > MAX_FRAME_SIZE) {
            throw new ProtocolException("Invalid HTTP/2 CONTINUATION frame");
        }
        validateFlags(flags, Http2Frame.END_HEADERS);
    }

    /**
     * Returns the HPACK header block from a PUSH_PROMISE payload.
     *
     * @param payload payload
     * @return header block
     */
    private static ByteString pushHeaderBlock(final ByteString payload) {
        return payload.substring(Normal._4);
    }

    /**
     * Reads a 24-bit unsigned integer.
     *
     * @param buffer buffer
     * @return value
     */
    private static int readMedium(final Buffer buffer) {
        return ((buffer.readByte() & 0xff) << Normal._16) | ((buffer.readByte() & 0xff) << Normal._8)
                | (buffer.readByte() & 0xff);
    }

    /**
     * Validates frame flags.
     *
     * @param flags   flags
     * @param allowed allowed mask
     */
    private static void validateFlags(final int flags, final int allowed) {
        if ((flags & ~allowed) != Normal._0) {
            throw new ProtocolException("Unsupported HTTP/2 frame flags");
        }
    }

    /**
     * Waits for IO completion.
     *
     * @param future future
     * @return result
     */
    private static int await(final CompletableFuture<Integer> future) {
        try {
            return future.get(Normal._5, TimeUnit.SECONDS);
        } catch (final java.util.concurrent.TimeoutException e) {
            throw new TimeoutException("HTTP/2 reader timed out", e);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InternalException("Interrupted while waiting for HTTP/2 reader", e);
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new SocketException("HTTP/2 reader failed", cause);
        }
    }

    /**
     * Ensures this reader is open.
     */
    private void ensureOpen() {
        if (state.get().terminal()) {
            throw new StatefulException("HTTP/2 reader is closed");
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
        return new InternalException("Unable to close HTTP/2 reader", failure);
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
