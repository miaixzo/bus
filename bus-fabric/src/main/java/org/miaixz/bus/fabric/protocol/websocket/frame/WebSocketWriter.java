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
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Synchronous single-path WebSocket frame writer.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class WebSocketWriter implements AutoCloseable {

    /**
     * Ping opcode.
     */
    private static final int PING = 0x9;

    /**
     * Pong opcode.
     */
    private static final int PONG = 0xA;

    /**
     * Queue limit.
     */
    private static final long QUEUE_LIMIT = 16L * 1024L * 1024L;

    /**
     * Output stream.
     */
    private final OutputStream output;

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
     * @param output output stream
     * @param mask   mask flag
     */
    public WebSocketWriter(final OutputStream output, final boolean mask) {
        this(output, mask, new SecureRandom());
    }

    /**
     * Creates a writer with an explicit random source.
     *
     * @param output output stream
     * @param mask   mask flag
     * @param random random source
     */
    WebSocketWriter(final OutputStream output, final boolean mask, final Random random) {
        if (output == null) {
            throw new ValidateException("WebSocket output must not be null");
        }
        if (random == null) {
            throw new ValidateException("WebSocket random must not be null");
        }
        this.output = output;
        this.mask = mask;
        this.random = random;
        this.maskKey = new byte[4];
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
        if (frame == null) {
            throw new ValidateException("WebSocket frame must not be null");
        }
        ensureOpen();
        final ByteBuffer payload = frame.payload();
        final long length = payload.remaining();
        reserve(length);
        try {
            writeHeader(frame, length);
            writePayload(payload, length);
            output.flush();
        } catch (final IOException e) {
            throw new SocketException("Unable to write WebSocket frame", e);
        } finally {
            queuedBytes.addAndGet(-length);
        }
    }

    /**
     * Writes ping.
     *
     * @param payload payload
     */
    public void ping(final ByteBuffer payload) {
        if (payload == null) {
            throw new ValidateException("WebSocket ping payload must not be null");
        }
        if (payload.remaining() > 125) {
            throw new ValidateException("WebSocket ping payload is too large");
        }
        write(new WebSocketFrame(PING, true, payload, true));
    }

    /**
     * Writes pong.
     *
     * @param payload payload
     */
    public void pong(final ByteBuffer payload) {
        if (payload == null) {
            throw new ValidateException("WebSocket pong payload must not be null");
        }
        if (payload.remaining() > 125) {
            throw new ProtocolException("WebSocket pong payload is too large");
        }
        write(new WebSocketFrame(PONG, true, payload, true));
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
        output.write((frame.fin() ? 0x80 : 0) | frame.opcode());
        final int maskBit = mask ? 0x80 : 0;
        if (length <= 125) {
            output.write(maskBit | (int) length);
        } else if (length <= 0xFFFF) {
            output.write(maskBit | 126);
            output.write((int) (length >>> 8) & 0xFF);
            output.write((int) length & 0xFF);
        } else {
            output.write(maskBit | 127);
            for (int shift = 56; shift >= 0; shift -= 8) {
                output.write((int) (length >>> shift) & 0xFF);
            }
        }
    }

    /**
     * Writes payload bytes.
     *
     * @param payload payload
     * @param length  payload length
     * @throws IOException when writing fails
     */
    private void writePayload(final ByteBuffer payload, final long length) throws IOException {
        staging.clear();
        try {
            final ByteBuffer source = payload.duplicate();
            while (source.hasRemaining()) {
                staging.write(source);
            }
            if (mask) {
                random.nextBytes(maskKey);
                output.write(maskKey);
                mask(length);
            }
            staging.writeTo(output, length);
        } finally {
            staging.clear();
        }
    }

    /**
     * Masks staged bytes in place.
     *
     * @param byteCount payload byte count
     */
    private void mask(final long byteCount) {
        if (byteCount == 0L) {
            return;
        }
        staging.readAndWriteUnsafe(maskCursor);
        try {
            long processed = 0L;
            int available = maskCursor.seek(0L);
            while (available != -1 && processed < byteCount) {
                for (int i = maskCursor.start; i < maskCursor.end && processed < byteCount; i++) {
                    maskCursor.data[i] = (byte) (maskCursor.data[i] ^ maskKey[(int) (processed & 3L)]);
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
        if (current > QUEUE_LIMIT) {
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

}
