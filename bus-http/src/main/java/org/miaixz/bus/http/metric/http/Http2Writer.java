/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.http.metric.http;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.BufferSink;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.http.Headers;
import org.miaixz.bus.http.Settings;
import org.miaixz.bus.logger.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

/**
 * 编写HTTP/2传输帧.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Http2Writer implements Closeable {

    final Hpack.Writer hpackWriter;
    private final BufferSink sink;
    private final boolean client;
    private final Buffer hpackBuffer;
    /**
     * 在一次对{@link #data}的调用中可能发送的最大字节数
     */
    private int maxFrameSize;
    private boolean closed;

    Http2Writer(BufferSink sink, boolean client) {
        this.sink = sink;
        this.client = client;
        this.hpackBuffer = new Buffer();
        this.hpackWriter = new Hpack.Writer(hpackBuffer);
        this.maxFrameSize = Http2.INITIAL_MAX_FRAME_SIZE;
    }

    private static void writeMedium(BufferSink sink, int i) throws IOException {
        sink.writeByte((i >>> Normal._16) & 0xff);
        sink.writeByte((i >>> 8) & 0xff);
        sink.writeByte(i & 0xff);
    }

    public synchronized void connectionPreface() throws IOException {
        if (closed) throw new IOException("closed");
        if (!client) return; // Nothing to write; servers don't send connection headers!
        if (Logger.isDebug()) {
            Logger.warn(String.format(">> CONNECTION %s", Http2.CONNECTION_PREFACE.hex()));
        }
        sink.write(Http2.CONNECTION_PREFACE.toByteArray());
        sink.flush();
    }

    /**
     * Applies {@code peerSettings} and then sends a settings ACK.
     */
    public synchronized void applyAndAckSettings(Settings peerSettings) throws IOException {
        if (closed) throw new IOException("closed");
        this.maxFrameSize = peerSettings.getMaxFrameSize(maxFrameSize);
        if (peerSettings.getHeaderTableSize() != -1) {
            hpackWriter.setHeaderTableSizeSetting(peerSettings.getHeaderTableSize());
        }
        int length = 0;
        byte type = Http2.TYPE_SETTINGS;
        byte flags = Http2.FLAG_ACK;
        int streamId = 0;
        frameHeader(streamId, length, type, flags);
        sink.flush();
    }

    /**
     * HTTP/2 only. 发送推送header
     * 推送promise包含所有与服务器发起的请求相关的头信息，以及一个{@code promise edstreamid}，
     * 它将传递响应帧。推送承诺帧作为响应的一部分发送到{@code streamId}。{@code promisedStreamId}的
     * 优先级比{@code streamId}大1
     *
     * @param streamId         客户端发起的流ID。必须是奇数.
     * @param promisedStreamId 服务器发起的流ID。必须是偶数.
     * @param requestHeaders   最低限度包括 {@code :method}, {@code :scheme}, {@code :authority},
     *                         and {@code :path}.
     * @throws IOException 异常
     */
    public synchronized void pushPromise(int streamId, int promisedStreamId,
                                         List<Headers.Header> requestHeaders) throws IOException {
        if (closed) throw new IOException("closed");
        hpackWriter.writeHeaders(requestHeaders);

        long byteCount = hpackBuffer.size();
        int length = (int) Math.min(maxFrameSize - 4, byteCount);
        byte type = Http2.TYPE_PUSH_PROMISE;
        byte flags = byteCount == length ? Http2.FLAG_END_HEADERS : 0;
        frameHeader(streamId, length + 4, type, flags);
        sink.writeInt(promisedStreamId & 0x7fffffff);
        sink.write(hpackBuffer, length);

        if (byteCount > length) writeContinuationFrames(streamId, byteCount - length);
    }

    public synchronized void flush() throws IOException {
        if (closed) throw new IOException("closed");
        sink.flush();
    }

    public synchronized void rstStream(int streamId, ErrorCode errorCode)
            throws IOException {
        if (closed) throw new IOException("closed");
        if (errorCode.httpCode == -1) throw new IllegalArgumentException();

        int length = 4;
        byte type = Http2.TYPE_RST_STREAM;
        byte flags = Http2.FLAG_NONE;
        frameHeader(streamId, length, type, flags);
        sink.writeInt(errorCode.httpCode);
        sink.flush();
    }

    /**
     * The maximum size of bytes that may be sent in a single call to {@link #data}.
     */
    public int maxDataLength() {
        return maxFrameSize;
    }

    /**
     * {@code source.length} may be longer than the max length of the variant's data frame.
     * Implementations must send multiple frames as necessary.
     *
     * @param source    the buffer to draw bytes from. May be null if byteCount is 0.
     * @param byteCount must be between 0 and the minimum of {@code source.length} and {@link
     *                  #maxDataLength}.
     */
    public synchronized void data(boolean outFinished, int streamId, Buffer source, int byteCount)
            throws IOException {
        if (closed) throw new IOException("closed");
        byte flags = Http2.FLAG_NONE;
        if (outFinished) flags |= Http2.FLAG_END_STREAM;
        dataFrame(streamId, flags, source, byteCount);
    }

    void dataFrame(int streamId, byte flags, Buffer buffer, int byteCount) throws IOException {
        byte type = Http2.TYPE_DATA;
        frameHeader(streamId, byteCount, type, flags);
        if (byteCount > 0) {
            sink.write(buffer, byteCount);
        }
    }

    /**
     * Write httpd's settings to the peer.
     */
    public synchronized void settings(Settings settings) throws IOException {
        if (closed) throw new IOException("closed");
        int length = settings.size() * 6;
        byte type = Http2.TYPE_SETTINGS;
        byte flags = Http2.FLAG_NONE;
        int streamId = 0;
        frameHeader(streamId, length, type, flags);
        for (int i = 0; i < Settings.COUNT; i++) {
            if (!settings.isSet(i)) continue;
            int id = i;
            if (id == 4) {
                id = 3;
            } else if (id == 7) {
                id = 4;
            }
            sink.writeShort(id);
            sink.writeInt(settings.get(i));
        }
        sink.flush();
    }

    /**
     * Send a connection-level ping to the peer. {@code ack} indicates this is a reply. The data in
     * {@code payload1} and {@code payload2} opaque binary, and there are no rules on the content.
     */
    public synchronized void ping(boolean ack, int payload1, int payload2) throws IOException {
        if (closed) throw new IOException("closed");
        int length = 8;
        byte type = Http2.TYPE_PING;
        byte flags = ack ? Http2.FLAG_ACK : Http2.FLAG_NONE;
        int streamId = 0;
        frameHeader(streamId, length, type, flags);
        sink.writeInt(payload1);
        sink.writeInt(payload2);
        sink.flush();
    }

    /**
     * 告诉对方停止创建流，我们最后处理{@code lastGoodStreamId}，如果没有处理流，则为零.
     *
     * @param lastGoodStreamId 处理的最后一个流ID，如果没有处理流，则为零
     * @param errorCode        关闭连接的原因.
     * @param debugData        只适用于HTTP/2;要发送的不透明调试数据.
     */
    public synchronized void goAway(int lastGoodStreamId, ErrorCode errorCode, byte[] debugData)
            throws IOException {
        if (closed) throw new IOException("closed");
        if (errorCode.httpCode == -1) throw Http2.illegalArgument("errorCode.httpCode == -1");
        int length = 8 + debugData.length;
        byte type = Http2.TYPE_GOAWAY;
        byte flags = Http2.FLAG_NONE;
        int streamId = 0;
        frameHeader(streamId, length, type, flags);
        sink.writeInt(lastGoodStreamId);
        sink.writeInt(errorCode.httpCode);
        if (debugData.length > 0) {
            sink.write(debugData);
        }
        sink.flush();
    }

    /**
     * Inform peer that an additional {@code windowSizeIncrement} bytes can be sent on {@code
     * streamId}, or the connection if {@code streamId} is zero.
     */
    public synchronized void windowUpdate(int streamId, long windowSizeIncrement) throws IOException {
        if (closed) throw new IOException("closed");
        if (windowSizeIncrement == 0 || windowSizeIncrement > 0x7fffffffL) {
            throw Http2.illegalArgument("windowSizeIncrement == 0 || windowSizeIncrement > 0x7fffffffL: %s",
                    windowSizeIncrement);
        }
        int length = 4;
        byte type = Http2.TYPE_WINDOW_UPDATE;
        byte flags = Http2.FLAG_NONE;
        frameHeader(streamId, length, type, flags);
        sink.writeInt((int) windowSizeIncrement);
        sink.flush();
    }

    public void frameHeader(int streamId, int length, byte type, byte flags) throws IOException {
        if (Logger.isDebug()) {
            Logger.warn(Http2.frameLog(false, streamId, length, type, flags));
        }
        if (length > maxFrameSize) {
            throw Http2.illegalArgument("FRAME_SIZE_ERROR length > %d: %d", maxFrameSize, length);
        }
        if ((streamId & 0x80000000) != 0) throw Http2.illegalArgument("reserved bit set: %s", streamId);
        writeMedium(sink, length);
        sink.writeByte(type & 0xff);
        sink.writeByte(flags & 0xff);
        sink.writeInt(streamId & 0x7fffffff);
    }

    @Override
    public synchronized void close() throws IOException {
        closed = true;
        sink.close();
    }

    private void writeContinuationFrames(int streamId, long byteCount) throws IOException {
        while (byteCount > 0) {
            int length = (int) Math.min(maxFrameSize, byteCount);
            byteCount -= length;
            frameHeader(streamId, length, Http2.TYPE_CONTINUATION, byteCount == 0 ? Http2.FLAG_END_HEADERS : 0);
            sink.write(hpackBuffer, length);
        }
    }

    public synchronized void headers(
            boolean outFinished, int streamId, List<Headers.Header> headerBlock) throws IOException {
        if (closed) throw new IOException("closed");
        hpackWriter.writeHeaders(headerBlock);

        long byteCount = hpackBuffer.size();
        int length = (int) Math.min(maxFrameSize, byteCount);
        byte type = Http2.TYPE_HEADERS;
        byte flags = byteCount == length ? Http2.FLAG_END_HEADERS : 0;
        if (outFinished) flags |= Http2.FLAG_END_STREAM;
        frameHeader(streamId, length, type, flags);
        sink.write(hpackBuffer, length);

        if (byteCount > length) writeContinuationFrames(streamId, byteCount - length);
    }

}
