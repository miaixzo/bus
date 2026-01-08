/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.source.BufferSource;
import org.miaixz.bus.core.lang.Normal;

import java.io.IOException;
import java.net.ProtocolException;
import java.util.concurrent.TimeUnit;

/**
 * 
 * A reader for WebSocket protocol frames. This class parses frames from a source, handling control frames (ping, pong,
 * close) and message frames (text, binary). This class is not thread-safe and must be operated from a single thread.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class WebSocketReader {

    /**
     * True if this is a client-side reader.
     */
    final boolean isClient;
    /**
     * The source from which to read frames.
     */
    final BufferSource source;
    /**
     * The callback for frame events.
     */
    final FrameCallback frameCallback;
    /**
     * A buffer for reading control frames.
     */
    private final Buffer controlFrameBuffer = new Buffer();
    /**
     * A buffer for reading message frames.
     */
    private final Buffer messageFrameBuffer = new Buffer();
    /**
     * The mask key for unmasking client frames. Only used on the server side.
     */
    private final byte[] maskKey;
    /**
     * A cursor for efficiently applying the mask. Only used on the server side.
     */
    private final Buffer.UnsafeCursor maskCursor;
    /**
     * True if the reader has been closed.
     */
    boolean closed;
    /**
     * The opcode of the current frame.
     */
    int opcode;
    /**
     * The length of the current frame.
     */
    long frameLength;
    /**
     * True if the current frame is the final frame of a message.
     */
    boolean isFinalFrame;
    /**
     * True if the current frame is a control frame.
     */
    boolean isControlFrame;

    /**
     * 
     * Constructs a new WebSocketReader.
     *
     * @param isClient      True if this is a client-side reader.
     * @param source        The data source.
     * @param frameCallback The callback for frame events.
     * @throws NullPointerException if source or frameCallback is null.
     */
    WebSocketReader(boolean isClient, BufferSource source, FrameCallback frameCallback) {
        if (source == null)
            throw new NullPointerException("source == null");
        if (frameCallback == null)
            throw new NullPointerException("frameCallback == null");
        this.isClient = isClient;
        this.source = source;
        this.frameCallback = frameCallback;

        maskKey = isClient ? null : new byte[4];
        maskCursor = isClient ? null : new Buffer.UnsafeCursor();
    }

    /**
     * 
     * Processes the next frame from the source. Control frames will invoke a single callback, while message frames may
     * be delivered across multiple frames and will invoke {@link FrameCallback#onReadMessage}.
     *
     * @throws IOException if a read or protocol error occurs.
     */
    void processNextFrame() throws IOException {
        readHeader();
        if (isControlFrame) {
            readControlFrame();
        } else {
            readMessageFrame();
        }
    }

    /**
     * 
     * Reads the header of the next frame.
     *
     * @throws IOException if a read or protocol error occurs.
     */
    private void readHeader() throws IOException {
        if (closed)
            throw new IOException("closed");

        int b0;
        long timeoutBefore = source.timeout().timeoutNanos();
        source.timeout().clearTimeout();
        try {
            b0 = source.readByte() & 0xff;
        } finally {
            source.timeout().timeout(timeoutBefore, TimeUnit.NANOSECONDS);
        }

        opcode = b0 & WebSocketProtocol.B0_MASK_OPCODE;
        isFinalFrame = (b0 & WebSocketProtocol.B0_FLAG_FIN) != 0;
        isControlFrame = (b0 & WebSocketProtocol.OPCODE_FLAG_CONTROL) != 0;

        // Control frames must be final frames (cannot contain continuations).
        if (isControlFrame && !isFinalFrame) {
            throw new ProtocolException("Control frames must be final.");
        }

        boolean reservedFlag1 = (b0 & WebSocketProtocol.B0_FLAG_RSV1) != 0;
        boolean reservedFlag2 = (b0 & WebSocketProtocol.B0_FLAG_RSV2) != 0;
        boolean reservedFlag3 = (b0 & WebSocketProtocol.B0_FLAG_RSV3) != 0;
        if (reservedFlag1 || reservedFlag2 || reservedFlag3) {
            // Reserved flags are for extensions which we currently do not support.
            throw new ProtocolException("Reserved flags are unsupported.");
        }

        int b1 = source.readByte() & 0xff;

        boolean isMasked = (b1 & WebSocketProtocol.B1_FLAG_MASK) != 0;
        if (isMasked == isClient) {
            // Masked payloads must be read on the server. Unmasked payloads must be read on the client.
            throw new ProtocolException(
                    isClient ? "Server-sent frames must not be masked." : "Client-sent frames must be masked.");
        }

        // Get frame length, optionally reading from follow-up bytes if indicated by special values.
        frameLength = b1 & WebSocketProtocol.B1_MASK_LENGTH;
        if (frameLength == WebSocketProtocol.PAYLOAD_SHORT) {
            frameLength = source.readShort() & 0xffffL; // Value is unsigned.
        } else if (frameLength == WebSocketProtocol.PAYLOAD_LONG) {
            frameLength = source.readLong();
            if (frameLength < 0) {
                throw new ProtocolException(
                        "Frame length 0x" + Long.toHexString(frameLength) + " > 0x7FFFFFFFFFFFFFFF");
            }
        }

        if (isControlFrame && frameLength > WebSocketProtocol.PAYLOAD_BYTE_MAX) {
            throw new ProtocolException("Control frame must be less than " + WebSocketProtocol.PAYLOAD_BYTE_MAX + "B.");
        }

        if (isMasked) {
            // Read the masking key as bytes so that they can be used directly for unmasking.
            source.readFully(maskKey);
        }
    }

    /**
     * 
     * Reads the payload of a control frame and invokes the appropriate callback.
     *
     * @throws IOException if a read or protocol error occurs.
     */
    private void readControlFrame() throws IOException {
        if (frameLength > 0) {
            source.readFully(controlFrameBuffer, frameLength);

            if (!isClient) {
                controlFrameBuffer.readAndWriteUnsafe(maskCursor);
                maskCursor.seek(0);
                WebSocketProtocol.toggleMask(maskCursor, maskKey);
                maskCursor.close();
            }
        }

        switch (opcode) {
            case WebSocketProtocol.OPCODE_CONTROL_PING:
                frameCallback.onReadPing(controlFrameBuffer.readByteString());
                break;

            case WebSocketProtocol.OPCODE_CONTROL_PONG:
                frameCallback.onReadPong(controlFrameBuffer.readByteString());
                break;

            case WebSocketProtocol.OPCODE_CONTROL_CLOSE:
                int code = WebSocketProtocol.CLOSE_NO_STATUS_CODE;
                String reason = Normal.EMPTY;
                long bufferSize = controlFrameBuffer.size();
                if (bufferSize == 1) {
                    throw new ProtocolException("Malformed close payload length of 1.");
                } else if (bufferSize != 0) {
                    code = controlFrameBuffer.readShort();
                    reason = controlFrameBuffer.readUtf8();
                    String codeExceptionMessage = WebSocketProtocol.closeCodeExceptionMessage(code);
                    if (null != codeExceptionMessage) {
                        throw new ProtocolException(codeExceptionMessage);
                    }
                }
                frameCallback.onReadClose(code, reason);
                closed = true;
                break;

            default:
                throw new ProtocolException("Unknown control opcode: " + Integer.toHexString(opcode));
        }
    }

    /**
     * 
     * Reads the payload of a message frame and invokes the message callback.
     *
     * @throws IOException if a read or protocol error occurs.
     */
    private void readMessageFrame() throws IOException {
        int opcode = this.opcode;
        if (opcode != WebSocketProtocol.OPCODE_TEXT && opcode != WebSocketProtocol.OPCODE_BINARY) {
            throw new ProtocolException("Unknown opcode: " + Integer.toHexString(opcode));
        }

        readMessage();

        if (opcode == WebSocketProtocol.OPCODE_TEXT) {
            frameCallback.onReadMessage(messageFrameBuffer.readUtf8());
        } else {
            frameCallback.onReadMessage(messageFrameBuffer.readByteString());
        }
    }

    /**
     * 
     * Skips control frames until a non-control frame is found.
     *
     * @throws IOException if a read error occurs.
     */
    private void readUntilNonControlFrame() throws IOException {
        while (!closed) {
            readHeader();
            if (!isControlFrame) {
                break;
            }
            readControlFrame();
        }
    }

    /**
     * 
     * Reads a complete message, which may be split across multiple frames. This method handles interleaved control
     * frames.
     *
     * @throws IOException if a read or protocol error occurs.
     */
    private void readMessage() throws IOException {
        while (true) {
            if (closed)
                throw new IOException("closed");

            if (frameLength > 0) {
                source.readFully(messageFrameBuffer, frameLength);

                if (!isClient) {
                    messageFrameBuffer.readAndWriteUnsafe(maskCursor);
                    maskCursor.seek(messageFrameBuffer.size() - frameLength);
                    WebSocketProtocol.toggleMask(maskCursor, maskKey);
                    maskCursor.close();
                }
            }

            if (isFinalFrame)
                break;

            readUntilNonControlFrame();
            if (opcode != WebSocketProtocol.OPCODE_CONTINUATION) {
                throw new ProtocolException("Expected continuation opcode. Got: " + Integer.toHexString(opcode));
            }
        }
    }

    /**
     * 
     * A callback interface for WebSocket frame events.
     */
    public interface FrameCallback {

        /**
         * 
         * Invoked when a text message is received.
         *
         * @param text The text content.
         * @throws IOException if an error occurs during processing.
         */
        void onReadMessage(String text) throws IOException;

        /**
         * 
         * Invoked when a binary message is received.
         *
         * @param bytes The binary content.
         * @throws IOException if an error occurs during processing.
         */
        void onReadMessage(ByteString bytes) throws IOException;

        /**
         * 
         * Invoked when a ping frame is received.
         *
         * @param buffer The ping payload.
         */
        void onReadPing(ByteString buffer);

        /**
         * 
         * Invoked when a pong frame is received.
         *
         * @param buffer The pong payload.
         */
        void onReadPong(ByteString buffer);

        /**
         * 
         * Invoked when a close frame is received.
         *
         * @param code   The close code.
         * @param reason The close reason.
         */
        void onReadClose(int code, String reason);
    }

}
