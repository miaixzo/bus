/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.http.socket;

import java.io.IOException;
import java.util.Random;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.BufferSink;
import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.timout.Timeout;

/**
 *
 * A writer for WebSocket protocol frames. This class writes frames to a sink according to RFC 6455, supporting control
 * frames (ping, pong, close) and message frames. This class is not thread-safe and must be operated from a single
 * thread.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WebSocketWriter {

    /**
     * True if this is a client-side writer, which must mask outgoing frames.
     */
    final boolean isClient;
    /**
     * A source of randomness for generating mask keys.
     */
    final Random random;
    /**
     * The destination sink for writing frames.
     */
    final BufferSink sink;
    /**
     * A buffer for the destination sink.
     */
    final Buffer sinkBuffer;
    /**
     * A per-message buffer for partial writes.
     */
    final Buffer buffer = new Buffer();
    /**
     * A sink for writing a single message frame.
     */
    final FrameSink frameSink = new FrameSink();
    /**
     * The mask key for masking client frames.
     */
    private final byte[] maskKey;
    /**
     * A cursor for efficiently applying the mask.
     */
    private final Buffer.UnsafeCursor maskCursor;
    /**
     * True if the writer has been closed.
     */
    boolean writerClosed;
    /**
     * True if a message sink is currently active.
     */
    boolean activeWriter;

    /**
     * Constructs a new WebSocketWriter.
     *
     * @param isClient True if this is a client-side writer.
     * @param sink     The destination sink.
     * @param random   A source of randomness.
     * @throws NullPointerException if sink or random is null.
     */
    WebSocketWriter(boolean isClient, BufferSink sink, Random random) {
        if (sink == null)
            throw new NullPointerException("sink == null");
        if (random == null)
            throw new NullPointerException("random == null");
        this.isClient = isClient;
        this.sink = sink;
        this.sinkBuffer = sink.buffer();
        this.random = random;

        maskKey = isClient ? new byte[4] : null;
        maskCursor = isClient ? new Buffer.UnsafeCursor() : null;
    }

    /**
     * Writes a ping frame with the given payload.
     *
     * @param payload The ping payload.
     * @throws IOException if a write error occurs.
     */
    void writePing(ByteString payload) throws IOException {
        writeControlFrame(WebSocketProtocol.OPCODE_CONTROL_PING, payload);
    }

    /**
     * Writes a pong frame with the given payload.
     *
     * @param payload The pong payload.
     * @throws IOException if a write error occurs.
     */
    void writePong(ByteString payload) throws IOException {
        writeControlFrame(WebSocketProtocol.OPCODE_CONTROL_PONG, payload);
    }

    /**
     * Writes a close frame with an optional code and reason, then marks the writer as closed.
     *
     * @param code   The close code (per RFC 6455 Section 7.4) or 0.
     * @param reason The close reason, or null.
     * @throws IOException if a write error occurs.
     */
    void writeClose(int code, ByteString reason) throws IOException {
        ByteString payload = ByteString.EMPTY;
        if (code != 0 || reason != null) {
            if (code != 0) {
                WebSocketProtocol.validateCloseCode(code);
            }
            Buffer buffer = new Buffer();
            buffer.writeShort(code);
            if (reason != null) {
                buffer.write(reason);
            }
            payload = buffer.readByteString();
        }

        try {
            writeControlFrame(WebSocketProtocol.OPCODE_CONTROL_CLOSE, payload);
        } finally {
            writerClosed = true;
        }
    }

    /**
     * Writes a control frame with the given opcode and payload.
     *
     * @param opcode  The control frame opcode.
     * @param payload The payload data.
     * @throws IOException              if a write error occurs.
     * @throws IllegalArgumentException if the payload size is too large for a control frame.
     */
    private void writeControlFrame(int opcode, ByteString payload) throws IOException {
        if (writerClosed)
            throw new IOException("closed");

        int length = payload.size();
        if (length > WebSocketProtocol.PAYLOAD_BYTE_MAX) {
            throw new IllegalArgumentException(
                    "Payload size must be less than or equal to " + WebSocketProtocol.PAYLOAD_BYTE_MAX);
        }

        int b0 = WebSocketProtocol.B0_FLAG_FIN | opcode;
        sinkBuffer.writeByte(b0);

        int b1 = length;
        if (isClient) {
            b1 |= WebSocketProtocol.B1_FLAG_MASK;
            sinkBuffer.writeByte(b1);

            random.nextBytes(maskKey);
            sinkBuffer.write(maskKey);

            if (length > 0) {
                long payloadStart = sinkBuffer.size();
                sinkBuffer.write(payload);

                sinkBuffer.readAndWriteUnsafe(maskCursor);
                maskCursor.seek(payloadStart);
                WebSocketProtocol.toggleMask(maskCursor, maskKey);
                maskCursor.close();
            }
        } else {
            sinkBuffer.writeByte(b1);
            sinkBuffer.write(payload);
        }

        sink.flush();
    }

    /**
     * Returns a sink for writing a message. This allows for streaming message writes.
     *
     * @param formatOpcode  The opcode for the message (text or binary).
     * @param contentLength The total length of the message.
     * @return A {@link Sink} for writing the message payload.
     * @throws IllegalStateException if another message writer is already active.
     */
    Sink newMessageSink(int formatOpcode, long contentLength) {
        if (activeWriter) {
            throw new IllegalStateException("Another message writer is active. Did you call close()?");
        }
        activeWriter = true;

        frameSink.formatOpcode = formatOpcode;
        frameSink.contentLength = contentLength;
        frameSink.isFirstFrame = true;
        frameSink.closed = false;

        return frameSink;
    }

    /**
     * Writes a single message frame.
     *
     * @param formatOpcode The opcode of the message.
     * @param byteCount    The number of bytes in the payload.
     * @param isFirstFrame True if this is the first frame of the message.
     * @param isFinal      True if this is the final frame of the message.
     * @throws IOException if a write error occurs.
     */
    void writeMessageFrame(int formatOpcode, long byteCount, boolean isFirstFrame, boolean isFinal) throws IOException {
        if (writerClosed)
            throw new IOException("closed");

        int b0 = isFirstFrame ? formatOpcode : WebSocketProtocol.OPCODE_CONTINUATION;
        if (isFinal) {
            b0 |= WebSocketProtocol.B0_FLAG_FIN;
        }
        sinkBuffer.writeByte(b0);

        int b1 = 0;
        if (isClient) {
            b1 |= WebSocketProtocol.B1_FLAG_MASK;
        }
        if (byteCount <= WebSocketProtocol.PAYLOAD_BYTE_MAX) {
            b1 |= (int) byteCount;
            sinkBuffer.writeByte(b1);
        } else if (byteCount <= WebSocketProtocol.PAYLOAD_SHORT_MAX) {
            b1 |= WebSocketProtocol.PAYLOAD_SHORT;
            sinkBuffer.writeByte(b1);
            sinkBuffer.writeShort((int) byteCount);
        } else {
            b1 |= WebSocketProtocol.PAYLOAD_LONG;
            sinkBuffer.writeByte(b1);
            sinkBuffer.writeLong(byteCount);
        }

        if (isClient) {
            random.nextBytes(maskKey);
            sinkBuffer.write(maskKey);

            if (byteCount > 0) {
                long bufferStart = sinkBuffer.size();
                sinkBuffer.write(buffer, byteCount);

                sinkBuffer.readAndWriteUnsafe(maskCursor);
                maskCursor.seek(bufferStart);
                WebSocketProtocol.toggleMask(maskCursor, maskKey);
                maskCursor.close();
            }
        } else {
            sinkBuffer.write(buffer, byteCount);
        }

        sink.emit();
    }

    /**
     * A sink for writing the payload of a single message frame.
     */
    final class FrameSink implements Sink {

        /**
         * The opcode for this message.
         */
        int formatOpcode;
        /**
         * The total length of the message.
         */
        long contentLength;
        /**
         * True if this is the first frame of the message.
         */
        boolean isFirstFrame;
        /**
         * True if this sink is closed.
         */
        boolean closed;

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            if (closed)
                throw new IOException("closed");

            buffer.write(source, byteCount);

            boolean deferWrite = isFirstFrame && contentLength != -1 && buffer.size() > contentLength - 8192; // 8192 is
                                                                                                              // Buffer.SIZE

            long emitCount = buffer.completeSegmentByteCount();
            if (emitCount > 0 && !deferWrite) {
                writeMessageFrame(formatOpcode, emitCount, isFirstFrame, false /* final */);
                isFirstFrame = false;
            }
        }

        @Override
        public void flush() throws IOException {
            if (closed)
                throw new IOException("closed");

            writeMessageFrame(formatOpcode, buffer.size(), isFirstFrame, false /* final */);
            isFirstFrame = false;
        }

        @Override
        public Timeout timeout() {
            return sink.timeout();
        }

        @Override
        public void close() throws IOException {
            if (closed)
                throw new IOException("closed");

            writeMessageFrame(formatOpcode, buffer.size(), isFirstFrame, true /* final */);
            closed = true;
            activeWriter = false;
        }
    }

}
