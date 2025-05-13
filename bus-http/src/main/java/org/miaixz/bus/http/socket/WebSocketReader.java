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
import java.net.ProtocolException;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.source.BufferSource;
import org.miaixz.bus.core.lang.Normal;

/**
 * WebSocket 协议帧读取器
 * <p>
 * 用于从 WebSocket 数据源读取和解析协议帧，支持控制帧（如 ping、pong、close）和消息帧（文本或二进制）。 此类非线程安全，需在单一线程中操作。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WebSocketReader {

    /**
     * 是否为客户端
     */
    final boolean isClient;
    /**
     * 数据源
     */
    final BufferSource source;
    /**
     * 帧回调接口
     */
    final FrameCallback frameCallback;
    /**
     * 控制帧缓冲区
     */
    private final Buffer controlFrameBuffer = new Buffer();
    /**
     * 消息帧缓冲区
     */
    private final Buffer messageFrameBuffer = new Buffer();
    /**
     * 掩码密钥
     */
    private final byte[] maskKey;
    /**
     * 掩码游标
     */
    private final Buffer.UnsafeCursor maskCursor;
    /**
     * 是否已关闭
     */
    boolean closed;
    /**
     * 操作码
     */
    int opcode;
    /**
     * 帧长度
     */
    long frameLength;
    /**
     * 是否为最终帧
     */
    boolean isFinalFrame;
    /**
     * 是否为控制帧
     */
    boolean isControlFrame;

    /**
     * 构造函数，初始化 WebSocketReader 实例
     *
     * @param isClient      是否为客户端
     * @param source        数据源
     * @param frameCallback 帧回调接口
     * @throws NullPointerException 如果 source 或 frameCallback 为 null
     */
    WebSocketReader(boolean isClient, BufferSource source, FrameCallback frameCallback) {
        if (source == null)
            throw new NullPointerException("source == null");
        if (frameCallback == null)
            throw new NullPointerException("frameCallback == null");
        this.isClient = isClient;
        this.source = source;
        this.frameCallback = frameCallback;

        // Masks are only a concern for server writers.
        maskKey = isClient ? null : new byte[4];
        maskCursor = isClient ? null : new Buffer.UnsafeCursor();
    }

    /**
     * 处理下一个协议帧
     * <p>
     * 对于控制帧，调用一次 {@link FrameCallback} 方法。 对于消息帧，调用 {@link FrameCallback#onReadMessage}，可能包含多个帧的连续消息。 控制帧可能在消息帧之间交错处理。
     * </p>
     *
     * @throws IOException 如果读取或解析失败
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
     * 读取帧头
     *
     * @throws IOException 如果读取失败或帧头格式无效
     */
    private void readHeader() throws IOException {
        if (closed)
            throw new IOException("closed");

        // Disable the timeout to read the first byte of a new frame.
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
     * 读取控制帧
     *
     * @throws IOException 如果读取失败或控制帧格式无效
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
     * 读取消息帧
     *
     * @throws IOException 如果读取失败或操作码无效
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
     * 读取直到非控制帧
     *
     * @throws IOException 如果读取失败
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
     * 读取消息体
     * <p>
     * 跨多个帧读取消息体，处理交错的控制帧并解码掩码数据。
     * </p>
     *
     * @throws IOException 如果读取失败或帧格式无效
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
                break; // We are exhausted and have no continuations.

            readUntilNonControlFrame();
            if (opcode != WebSocketProtocol.OPCODE_CONTINUATION) {
                throw new ProtocolException("Expected continuation opcode. Got: " + Integer.toHexString(opcode));
            }
        }
    }

    /**
     * WebSocket 帧回调接口
     */
    public interface FrameCallback {

        /**
         * 接收文本消息
         *
         * @param text 文本内容
         * @throws IOException 如果处理失败
         */
        void onReadMessage(String text) throws IOException;

        /**
         * 接收二进制消息
         *
         * @param bytes 二进制内容
         * @throws IOException 如果处理失败
         */
        void onReadMessage(ByteString bytes) throws IOException;

        /**
         * 接收 ping 帧
         *
         * @param buffer ping 数据
         */
        void onReadPing(ByteString buffer);

        /**
         * 接收 pong 帧
         *
         * @param buffer pong 数据
         */
        void onReadPong(ByteString buffer);

        /**
         * 接收 close 帧
         *
         * @param code   关闭代码
         * @param reason 关闭原因
         */
        void onReadClose(int code, String reason);
    }

}