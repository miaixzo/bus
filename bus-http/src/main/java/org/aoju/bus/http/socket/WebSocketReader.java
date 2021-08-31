/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.http.socket;

import org.aoju.bus.core.io.Buffer;
import org.aoju.bus.core.io.BufferSource;
import org.aoju.bus.core.io.ByteString;
import org.aoju.bus.core.lang.Normal;

import java.io.IOException;
import java.net.ProtocolException;
import java.util.concurrent.TimeUnit;

/**
 * 兼容的WebSocket框架阅读器.
 * 这个类不是线程安全的
 *
 * @author Kimi Liu
 * @version 6.2.8
 * @since JDK 1.8+
 */
final class WebSocketReader {

    final boolean isClient;
    final BufferSource source;
    final FrameCallback frameCallback;
    private final Buffer controlFrameBuffer = new Buffer();
    private final Buffer messageFrameBuffer = new Buffer();
    private final byte[] maskKey;
    private final Buffer.UnsafeCursor maskCursor;
    boolean closed;
    int opcode;
    long frameLength;
    boolean isFinalFrame;
    boolean isControlFrame;

    WebSocketReader(boolean isClient, BufferSource source, FrameCallback frameCallback) {
        if (null == source) throw new NullPointerException("source == null");
        if (null == frameCallback) throw new NullPointerException("frameCallback == null");
        this.isClient = isClient;
        this.source = source;
        this.frameCallback = frameCallback;

        maskKey = isClient ? null : new byte[4];
        maskCursor = isClient ? null : new Buffer.UnsafeCursor();
    }

    void processNextFrame() throws IOException {
        readHeader();
        if (isControlFrame) {
            readControlFrame();
        } else {
            readMessageFrame();
        }
    }

    private void readHeader() throws IOException {
        if (closed) throw new IOException("closed");

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

        if (isControlFrame && !isFinalFrame) {
            throw new ProtocolException("Control frames must be final.");
        }

        boolean reservedFlag1 = (b0 & WebSocketProtocol.B0_FLAG_RSV1) != 0;
        boolean reservedFlag2 = (b0 & WebSocketProtocol.B0_FLAG_RSV2) != 0;
        boolean reservedFlag3 = (b0 & WebSocketProtocol.B0_FLAG_RSV3) != 0;
        if (reservedFlag1 || reservedFlag2 || reservedFlag3) {
            throw new ProtocolException("Reserved flags are unsupported.");
        }

        int b1 = source.readByte() & 0xff;

        boolean isMasked = (b1 & WebSocketProtocol.B1_FLAG_MASK) != 0;
        if (isMasked == isClient) {
            throw new ProtocolException(isClient
                    ? "Server-sent frames must not be masked."
                    : "Client-sent frames must be masked.");
        }

        frameLength = b1 & WebSocketProtocol.B1_MASK_LENGTH;
        if (frameLength == WebSocketProtocol.PAYLOAD_SHORT) {
            frameLength = source.readShort() & 0xffffL;
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
            source.readFully(maskKey);
        }
    }

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

    private void readUntilNonControlFrame() throws IOException {
        while (!closed) {
            readHeader();
            if (!isControlFrame) {
                break;
            }
            readControlFrame();
        }
    }

    private void readMessage() throws IOException {
        while (true) {
            if (closed) throw new IOException("closed");

            if (frameLength > 0) {
                source.readFully(messageFrameBuffer, frameLength);

                if (!isClient) {
                    messageFrameBuffer.readAndWriteUnsafe(maskCursor);
                    maskCursor.seek(messageFrameBuffer.size() - frameLength);
                    WebSocketProtocol.toggleMask(maskCursor, maskKey);
                    maskCursor.close();
                }
            }

            if (isFinalFrame) break;

            readUntilNonControlFrame();
            if (opcode != WebSocketProtocol.OPCODE_CONTINUATION) {
                throw new ProtocolException("Expected continuation opcode. Got: " + Integer.toHexString(opcode));
            }
        }
    }

    public interface FrameCallback {
        void onReadMessage(String text) throws IOException;

        void onReadMessage(ByteString bytes) throws IOException;

        void onReadPing(ByteString buffer);

        void onReadPong(ByteString buffer);

        void onReadClose(int code, String reason);
    }

}
