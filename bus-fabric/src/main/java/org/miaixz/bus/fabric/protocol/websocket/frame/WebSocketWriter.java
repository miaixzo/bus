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
package org.miaixz.bus.fabric.protocol.websocket.frame;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Builder;

/**
 * Synchronous single-path WebSocket frame writer.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class WebSocketWriter implements AutoCloseable {

    /**
     * Output sink.
     */
    private final Sink output;

    /**
     * Mask client frames.
     */
    private final boolean mask;

    /**
     * Random mask source.
     */
    private final Random random;

    /**
     * Reusable mask key.
     */
    private final byte[] maskKey;

    /**
     * Reusable frame header buffer.
     */
    private final Buffer header;

    /**
     * Reusable staging buffer.
     */
    private final Buffer staging;

    /**
     * Cursor for in-place masking.
     */
    private final Buffer.UnsafeCursor maskCursor;

    /**
     * Queued bytes.
     */
    private final AtomicLong queuedBytes;

    /**
     * Closed flag.
     */
    private boolean closed;

    /**
     * Creates a writer.
     *
     * @param output output sink
     * @param mask   mask flag
     */
    public WebSocketWriter(final Sink output, final boolean mask) {
        this(output, mask, new SecureRandom());
    }

    /**
     * Creates a writer with an explicit random source.
     *
     * @param output output sink
     * @param mask   mask flag
     * @param random random source
     */
    WebSocketWriter(final Sink output, final boolean mask, final Random random) {
        this.output = require(output, "WebSocket output");
        this.mask = mask;
        this.random = require(random, "WebSocket random");
        this.maskKey = new byte[Normal._4];
        this.header = new Buffer();
        this.staging = new Buffer();
        this.maskCursor = new Buffer.UnsafeCursor();
        this.queuedBytes = new AtomicLong();
    }

    /**
     * Writes a frame.
     *
     * @param frame frame
     */
    public synchronized void write(final WebSocketFrame frame) {
        final WebSocketFrame checkedFrame = require(frame, "WebSocket frame");
        ensureOpen();
        final ByteString payload = checkedFrame.payload();
        final long length = payload.size();
        reserve(length);
        try {
            writeHeader(checkedFrame, length);
            writePayload(payload, length);
            output.flush();
        } catch (final IOException e) {
            throw new SocketException("Unable to write WebSocket frame", e);
        } finally {
            queuedBytes.addAndGet(-length);
        }
    }

    /**
     * Writes a binary message from a source.
     *
     * @param source source
     * @param length payload length
     */
    public synchronized void binary(final Source source, final long length) {
        final Source checkedSource = require(source, "WebSocket binary source");
        if (length < Normal.LONG_ZERO || length > Builder.BYTES_16_MIB) {
            throw new ProtocolException("WebSocket binary payload length is invalid");
        }
        ensureOpen();
        reserve(length);
        try {
            writeHeader(Builder.WEBSOCKET_OPCODE_BINARY, true, length);
            if (mask) {
                random.nextBytes(maskKey);
                writeMaskKey();
            }
            writePayload(checkedSource, length);
            output.flush();
        } catch (final IOException e) {
            throw new SocketException("Unable to write WebSocket binary message", e);
        } finally {
            queuedBytes.addAndGet(-length);
        }
    }

    /**
     * Writes ping.
     *
     * @param payload payload
     */
    public void ping(final ByteString payload) {
        final ByteString checkedPayload = require(payload, "WebSocket ping payload");
        if (checkedPayload.size() > Builder._125) {
            throw new ValidateException("WebSocket ping payload is too large");
        }
        write(new WebSocketFrame(Builder.WEBSOCKET_OPCODE_PING, true, checkedPayload, true));
    }

    /**
     * Writes pong.
     *
     * @param payload payload
     */
    public void pong(final ByteString payload) {
        final ByteString checkedPayload = require(payload, "WebSocket pong payload");
        if (checkedPayload.size() > Builder._125) {
            throw new ProtocolException("WebSocket pong payload is too large");
        }
        write(new WebSocketFrame(Builder.WEBSOCKET_OPCODE_PONG, true, checkedPayload, true));
    }

    /**
     * Writes and closes with a close frame.
     *
     * @param code   close code
     * @param reason close reason
     */
    public synchronized void close(final int code, final String reason) {
        if (closed) {
            return;
        }
        writeClose(code, reason);
        close();
    }

    /**
     * Writes a close frame without closing the underlying stream.
     *
     * @param code   close code
     * @param reason close reason
     */
    public synchronized void writeClose(final int code, final String reason) {
        if (closed) {
            return;
        }
        write(WebSocketFrame.close(code, reason));
    }

    /**
     * Returns queued payload bytes.
     *
     * @return queued bytes
     */
    public long queuedBytes() {
        return queuedBytes.get();
    }

    /**
     * Closes the writer.
     */
    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;
        try {
            output.close();
        } catch (final IOException e) {
            throw new SocketException("Unable to close WebSocket writer", e);
        }
    }

    /**
     * Writes frame header.
     *
     * @param frame  frame
     * @param length payload length
     * @throws IOException when writing fails
     */
    private void writeHeader(final WebSocketFrame frame, final long length) throws IOException {
        writeHeader(frame.opcode(), frame.fin(), length);
    }

    /**
     * Writes frame header.
     *
     * @param opcode frame opcode
     * @param fin    final-fragment flag
     * @param length payload length
     * @throws IOException when writing fails
     */
    private void writeHeader(final int opcode, final boolean fin, final long length) throws IOException {
        header.clear();
        header.writeByte((fin ? Normal._128 : Normal._0) | opcode);
        final int maskBit = mask ? Normal._128 : Normal._0;
        if (length <= Builder._125) {
            header.writeByte(maskBit | (int) length);
        } else if (length <= Normal._65535) {
            header.writeByte(maskBit | Builder.WEBSOCKET_LENGTH_16_MARKER);
            header.writeByte((int) (length >>> Byte.SIZE) & Builder.UNSIGNED_BYTE_MASK);
            header.writeByte((int) length & Builder.UNSIGNED_BYTE_MASK);
        } else {
            header.writeByte(maskBit | Builder._127);
            for (int shift = Normal._56; shift >= Normal._0; shift -= Byte.SIZE) {
                header.writeByte((int) (length >>> shift) & Builder.UNSIGNED_BYTE_MASK);
            }
        }
        output.write(header, header.size());
    }

    /**
     * Writes payload bytes.
     *
     * @param payload payload
     * @param length  payload length
     * @throws IOException when writing fails
     */
    private void writePayload(final ByteString payload, final long length) throws IOException {
        staging.clear();
        try {
            staging.write(payload);
            if (mask) {
                random.nextBytes(maskKey);
                writeMaskKey();
                mask(staging, length, Normal.LONG_ZERO);
            }
            output.write(staging, length);
        } finally {
            staging.clear();
        }
    }

    /**
     * Writes payload bytes from a source.
     *
     * @param source source
     * @param length payload length
     * @throws IOException when writing fails
     */
    private void writePayload(final Source source, final long length) throws IOException {
        final Buffer buffer = new Buffer();
        long written = Normal.LONG_ZERO;
        while (written < length) {
            final long read = source.read(buffer, Math.min(Normal._8192, length - written));
            if (read < Normal.LONG_ZERO) {
                throw new SocketException("WebSocket binary source ended early");
            }
            if (read == Normal.LONG_ZERO) {
                continue;
            }
            if (mask) {
                mask(buffer, read, written);
            }
            output.write(buffer, read);
            written += read;
        }
    }

    /**
     * Writes the current mask key.
     *
     * @throws IOException when writing fails
     */
    private void writeMaskKey() throws IOException {
        header.clear();
        header.write(maskKey);
        output.write(header, header.size());
    }

    /**
     * Masks staged bytes in place.
     *
     * @param byteCount payload byte count
     */
    private void mask(final Buffer buffer, final long byteCount, final long offset) {
        if (byteCount == Normal.LONG_ZERO) {
            return;
        }
        buffer.readAndWriteUnsafe(maskCursor);
        try {
            long processed = Normal.LONG_ZERO;
            int available = maskCursor.seek(Normal.LONG_ZERO);
            while (available != Normal.__1 && processed < byteCount) {
                for (int i = maskCursor.start; i < maskCursor.end && processed < byteCount; i++) {
                    maskCursor.data[i] = (byte) (maskCursor.data[i]
                            ^ maskKey[(int) ((offset + processed) & Normal._3)]);
                    processed++;
                }
                if (processed < byteCount) {
                    available = maskCursor.next();
                }
            }
        } finally {
            maskCursor.close();
        }
    }

    /**
     * Reserves queue capacity.
     *
     * @param length payload length
     */
    private void reserve(final long length) {
        final long current = queuedBytes.addAndGet(length);
        if (current > Builder.BYTES_16_MIB) {
            queuedBytes.addAndGet(-length);
            throw new StatefulException("WebSocket write queue is full");
        }
    }

    /**
     * Ensures the writer is open.
     */
    private void ensureOpen() {
        if (closed) {
            throw new StatefulException("WebSocket writer is closed");
        }
    }

    /**
     * Validates required references.
     *
     * @param value value
     * @param name  name
     * @param <T>   value type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

}
