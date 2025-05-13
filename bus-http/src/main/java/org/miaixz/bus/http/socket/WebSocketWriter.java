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
 * WebSocket 协议帧写入器
 * <p>
 * 用于将 WebSocket 协议帧写入输出流，遵循 RFC 6455 标准，支持控制帧（如 ping、pong、close）和消息帧。 此类非线程安全，需在单一线程中操作。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WebSocketWriter {

    /**
     * 是否为客户端
     */
    final boolean isClient;
    /**
     * 随机数生成器
     */
    final Random random;
    /**
     * 输出流
     */
    final BufferSink sink;
    /**
     * 输出流的缓冲区
     */
    final Buffer sinkBuffer;
    /**
     * 内部缓冲区
     */
    final Buffer buffer = new Buffer();
    /**
     * 帧输出流
     */
    final FrameSink frameSink = new FrameSink();
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
    boolean writerClosed;
    /**
     * 是否有活动写入器
     */
    boolean activeWriter;

    /**
     * 构造函数，初始化 WebSocketWriter 实例
     *
     * @param isClient 是否为客户端
     * @param sink     输出流
     * @param random   随机数生成器
     * @throws NullPointerException 如果 sink 或 random 为 null
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

        // Masks are only a concern for client writers.
        maskKey = isClient ? new byte[4] : null;
        maskCursor = isClient ? null : new Buffer.UnsafeCursor();
    }

    /**
     * 写入 ping 帧
     *
     * @param payload ping 数据
     * @throws IOException 如果写入失败
     */
    void writePing(ByteString payload) throws IOException {
        writeControlFrame(WebSocketProtocol.OPCODE_CONTROL_PING, payload);
    }

    /**
     * 写入 pong 帧
     *
     * @param payload pong 数据
     * @throws IOException 如果写入失败
     */
    void writePong(ByteString payload) throws IOException {
        writeControlFrame(WebSocketProtocol.OPCODE_CONTROL_PONG, payload);
    }

    /**
     * 写入 close 帧
     * <p>
     * 包含可选的关闭代码和原因，关闭后标记写入器为关闭状态。
     * </p>
     *
     * @param code   关闭代码（符合 RFC 6455 Section 7.4 或 0）
     * @param reason 关闭原因（可能为 null）
     * @throws IOException 如果写入失败
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
     * 写入控制帧
     *
     * @param opcode  操作码
     * @param payload 数据
     * @throws IOException              如果写入失败
     * @throws IllegalArgumentException 如果数据长度超限
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
     * 创建新的消息输出流
     * <p>
     * 用于流式写入消息，支持控制帧交错。
     * </p>
     *
     * @param formatOpcode  消息操作码
     * @param contentLength 内容长度
     * @return 输出流
     * @throws IllegalStateException 如果已有活动写入器
     */
    Sink newMessageSink(int formatOpcode, long contentLength) {
        if (activeWriter) {
            throw new IllegalStateException("Another message writer is active. Did you call close()?");
        }
        activeWriter = true;

        // Reset FrameSink state for a new writer.
        frameSink.formatOpcode = formatOpcode;
        frameSink.contentLength = contentLength;
        frameSink.isFirstFrame = true;
        frameSink.closed = false;

        return frameSink;
    }

    /**
     * 写入消息帧
     *
     * @param formatOpcode 消息操作码
     * @param byteCount    字节数
     * @param isFirstFrame 是否为首帧
     * @param isFinal      是否为最终帧
     * @throws IOException 如果写入失败
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
     * WebSocket 帧输出流
     */
    class FrameSink implements Sink {

        /**
         * 消息操作码
         */
        int formatOpcode;
        /**
         * 内容长度
         */
        long contentLength;
        /**
         * 是否为首帧
         */
        boolean isFirstFrame;
        /**
         * 是否已关闭
         */
        boolean closed;

        /**
         * 写入数据
         *
         * @param source    数据源
         * @param byteCount 字节数
         * @throws IOException 如果流已关闭或写入失败
         */
        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            if (closed)
                throw new IOException("closed");

            buffer.write(source, byteCount);

            // Determine if this is a buffered write which we can defer until close() flushes.
            boolean deferWrite = isFirstFrame && contentLength != -1
                    && buffer.size() > contentLength - 8192 /* segment size */;

            long emitCount = buffer.completeSegmentByteCount();
            if (emitCount > 0 && !deferWrite) {
                writeMessageFrame(formatOpcode, emitCount, isFirstFrame, false /* final */);
                isFirstFrame = false;
            }
        }

        /**
         * 刷新数据
         *
         * @throws IOException 如果流已关闭或写入失败
         */
        @Override
        public void flush() throws IOException {
            if (closed)
                throw new IOException("closed");

            writeMessageFrame(formatOpcode, buffer.size(), isFirstFrame, false /* final */);
            isFirstFrame = false;
        }

        /**
         * 获取超时配置
         *
         * @return 超时配置
         */
        @Override
        public Timeout timeout() {
            return sink.timeout();
        }

        /**
         * 关闭输出流
         *
         * @throws IOException 如果流已关闭或写入失败
         */
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