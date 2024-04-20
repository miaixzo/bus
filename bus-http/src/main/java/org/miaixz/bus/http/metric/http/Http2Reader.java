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

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.source.BufferSource;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.http.Headers;
import org.miaixz.bus.http.Settings;
import org.miaixz.bus.logger.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

/**
 * Reads HTTP/2运输框架
 * 此实现假设我们没有向对等端发送增加的{@link Settings#getMaxFrameSize frame size设置}。
 * 因此，我们希望所有帧的最大长度为{@link Http2#INITIAL_MAX_FRAME_SIZE}。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Http2Reader implements Closeable {

    public final Hpack.Reader hpackReader;
    public final BufferSource source;
    public final ContinuationSource continuation;
    public final boolean client;

    /**
     * Creates a frame reader with max header table size of 4096.
     */
    Http2Reader(BufferSource source, boolean client) {
        this.source = source;
        this.client = client;
        this.continuation = new ContinuationSource(this.source);
        this.hpackReader = new Hpack.Reader(4096, continuation);
    }

    static int readMedium(BufferSource source) throws IOException {
        return (source.readByte() & 0xff) << 16
                | (source.readByte() & 0xff) << 8
                | (source.readByte() & 0xff);
    }

    static int lengthWithoutPadding(int length, byte flags, short padding)
            throws IOException {
        if ((flags & Http2.FLAG_PADDED) != 0) length--; // Account for reading the padding length.
        if (padding > length) {
            throw Http2.ioException("PROTOCOL_ERROR padding %s > remaining length %s", padding, length);
        }
        return (short) (length - padding);
    }

    public void readConnectionPreface(Handler handler) throws IOException {
        if (client) {
            // The client reads the initial SETTINGS frame.
            if (!nextFrame(true, handler)) {
                throw Http2.ioException("Required SETTINGS preface not received");
            }
        } else {
            ByteString connectionPreface = source.readByteString(Http2.CONNECTION_PREFACE.size());
            if (Logger.isDebug()) {
                Logger.debug(String.format("<< CONNECTION %s" + connectionPreface.hex()));
            }
            if (!Http2.CONNECTION_PREFACE.equals(connectionPreface)) {
                throw Http2.ioException("Expected a connection header but was %s", connectionPreface.utf8());
            }
        }
    }

    public boolean nextFrame(boolean requireSettings, Handler handler) throws IOException {
        try {
            source.require(9);
        } catch (IOException e) {
            return false;
        }

        //  0                   1                   2                   3
        //  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
        // +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
        // |                 Length (24)                   |
        // +---------------+---------------+---------------+
        // |   Type (8)    |   Flags (8)   |
        // +-+-+-----------+---------------+-------------------------------+
        // |R|                 Stream Identifier (31)                      |
        // +=+=============================================================+
        // |                   Frame Payload (0...)                      ...
        // +---------------------------------------------------------------+
        int length = readMedium(source);
        if (length < 0 || length > Http2.INITIAL_MAX_FRAME_SIZE) {
            throw Http2.ioException("FRAME_SIZE_ERROR: %s", length);
        }
        byte type = (byte) (source.readByte() & 0xff);
        if (requireSettings && type != Http2.TYPE_SETTINGS) {
            throw Http2.ioException("Expected a SETTINGS frame but was %s", type);
        }
        byte flags = (byte) (source.readByte() & 0xff);
        int streamId = (source.readInt() & 0x7fffffff);
        if (Logger.isDebug()) {
            Logger.warn(Http2.frameLog(true, streamId, length, type, flags));
        }

        switch (type) {
            case Http2.TYPE_DATA:
                readData(handler, length, flags, streamId);
                break;

            case Http2.TYPE_HEADERS:
                readHeaders(handler, length, flags, streamId);
                break;

            case Http2.TYPE_PRIORITY:
                readPriority(handler, length, flags, streamId);
                break;

            case Http2.TYPE_RST_STREAM:
                readRstStream(handler, length, flags, streamId);
                break;

            case Http2.TYPE_SETTINGS:
                readSettings(handler, length, flags, streamId);
                break;

            case Http2.TYPE_PUSH_PROMISE:
                readPushPromise(handler, length, flags, streamId);
                break;

            case Http2.TYPE_PING:
                readPing(handler, length, flags, streamId);
                break;

            case Http2.TYPE_GOAWAY:
                readGoAway(handler, length, flags, streamId);
                break;

            case Http2.TYPE_WINDOW_UPDATE:
                readWindowUpdate(handler, length, flags, streamId);
                break;

            default:
                source.skip(length);
        }
        return true;
    }

    private void readHeaders(Handler handler, int length, byte flags, int streamId)
            throws IOException {
        if (streamId == 0) throw Http2.ioException("PROTOCOL_ERROR: TYPE_HEADERS streamId == 0");

        boolean endStream = (flags & Http2.FLAG_END_STREAM) != 0;

        short padding = (flags & Http2.FLAG_PADDED) != 0 ? (short) (source.readByte() & 0xff) : 0;

        if ((flags & Http2.FLAG_PRIORITY) != 0) {
            readPriority(handler, streamId);
            length -= 5;
        }

        length = lengthWithoutPadding(length, flags, padding);

        List<Headers.Header> headerBlock = readHeaderBlock(length, padding, flags, streamId);

        handler.headers(endStream, streamId, -1, headerBlock);
    }

    private List<Headers.Header> readHeaderBlock(int length, short padding, byte flags, int streamId)
            throws IOException {
        continuation.length = continuation.left = length;
        continuation.padding = padding;
        continuation.flags = flags;
        continuation.streamId = streamId;

        hpackReader.readHeaders();
        return hpackReader.getAndResetHeaderList();
    }

    private void readData(Handler handler, int length, byte flags, int streamId)
            throws IOException {
        if (streamId == 0) throw Http2.ioException("PROTOCOL_ERROR: TYPE_DATA streamId == 0");

        boolean inFinished = (flags & Http2.FLAG_END_STREAM) != 0;
        boolean gzipped = (flags & Http2.FLAG_COMPRESSED) != 0;
        if (gzipped) {
            throw Http2.ioException("PROTOCOL_ERROR: FLAG_COMPRESSED without SETTINGS_COMPRESS_DATA");
        }

        short padding = (flags & Http2.FLAG_PADDED) != 0 ? (short) (source.readByte() & 0xff) : 0;
        length = lengthWithoutPadding(length, flags, padding);

        handler.data(inFinished, streamId, source, length);
        source.skip(padding);
    }

    private void readPriority(Handler handler, int length, byte flags, int streamId)
            throws IOException {
        if (length != 5) throw Http2.ioException("TYPE_PRIORITY length: %d != 5", length);
        if (streamId == 0) throw Http2.ioException("TYPE_PRIORITY streamId == 0");
        readPriority(handler, streamId);
    }

    private void readPriority(Handler handler, int streamId) throws IOException {
        int w1 = source.readInt();
        boolean exclusive = (w1 & 0x80000000) != 0;
        int streamDependency = (w1 & 0x7fffffff);
        int weight = (source.readByte() & 0xff) + 1;
        handler.priority(streamId, streamDependency, weight, exclusive);
    }

    private void readRstStream(Handler handler, int length, byte flags, int streamId)
            throws IOException {
        if (length != 4) throw Http2.ioException("TYPE_RST_STREAM length: %d != 4", length);
        if (streamId == 0) throw Http2.ioException("TYPE_RST_STREAM streamId == 0");
        int errorCodeInt = source.readInt();
        ErrorCode errorCode = ErrorCode.fromHttp2(errorCodeInt);
        if (null == errorCode) {
            throw Http2.ioException("TYPE_RST_STREAM unexpected error code: %d", errorCodeInt);
        }
        handler.rstStream(streamId, errorCode);
    }

    private void readSettings(Handler handler, int length, byte flags, int streamId)
            throws IOException {
        if (streamId != 0) throw Http2.ioException("TYPE_SETTINGS streamId != 0");
        if ((flags & Http2.FLAG_ACK) != 0) {
            if (length != 0) throw Http2.ioException("FRAME_SIZE_ERROR ack frame should be empty!");
            handler.ackSettings();
            return;
        }

        if (length % 6 != 0) throw Http2.ioException("TYPE_SETTINGS length %% 6 != 0: %s", length);
        Settings settings = new Settings();
        for (int i = 0; i < length; i += 6) {
            int id = source.readShort() & 0xFFFF;
            int value = source.readInt();

            switch (id) {
                case 1: // SETTINGS_HEADER_TABLE_SIZE
                    break;
                case 2: // SETTINGS_ENABLE_PUSH
                    if (value != 0 && value != 1) {
                        throw Http2.ioException("PROTOCOL_ERROR SETTINGS_ENABLE_PUSH != 0 or 1");
                    }
                    break;
                case 3: // SETTINGS_MAX_CONCURRENT_STREAMS
                    id = 4; // Renumbered in draft 10.
                    break;
                case 4: // SETTINGS_INITIAL_WINDOW_SIZE
                    id = 7; // Renumbered in draft 10.
                    if (value < 0) {
                        throw Http2.ioException("PROTOCOL_ERROR SETTINGS_INITIAL_WINDOW_SIZE > 2^31 - 1");
                    }
                    break;
                case 5: // SETTINGS_MAX_FRAME_SIZE
                    if (value < Http2.INITIAL_MAX_FRAME_SIZE || value > 16777215) {
                        throw Http2.ioException("PROTOCOL_ERROR SETTINGS_MAX_FRAME_SIZE: %s", value);
                    }
                    break;
                case 6: // SETTINGS_MAX_HEADER_LIST_SIZE
                    break; // Advisory only, so ignored.
                default:
                    break; // Must ignore setting with unknown id.
            }
            settings.set(id, value);
        }
        handler.settings(false, settings);
    }

    private void readPushPromise(Handler handler, int length, byte flags, int streamId)
            throws IOException {
        if (streamId == 0) {
            throw Http2.ioException("PROTOCOL_ERROR: TYPE_PUSH_PROMISE streamId == 0");
        }
        short padding = (flags & Http2.FLAG_PADDED) != 0 ? (short) (source.readByte() & 0xff) : 0;
        int promisedStreamId = source.readInt() & 0x7fffffff;
        length -= 4;
        length = lengthWithoutPadding(length, flags, padding);
        List<Headers.Header> headerBlock = readHeaderBlock(length, padding, flags, streamId);
        handler.pushPromise(streamId, promisedStreamId, headerBlock);
    }

    private void readPing(Handler handler, int length, byte flags, int streamId)
            throws IOException {
        if (length != 8) throw Http2.ioException("TYPE_PING length != 8: %s", length);
        if (streamId != 0) throw Http2.ioException("TYPE_PING streamId != 0");
        int payload1 = source.readInt();
        int payload2 = source.readInt();
        boolean ack = (flags & Http2.FLAG_ACK) != 0;
        handler.ping(ack, payload1, payload2);
    }

    private void readGoAway(Handler handler, int length, byte flags, int streamId)
            throws IOException {
        if (length < 8) throw Http2.ioException("TYPE_GOAWAY length < 8: %s", length);
        if (streamId != 0) throw Http2.ioException("TYPE_GOAWAY streamId != 0");
        int lastStreamId = source.readInt();
        int errorCodeInt = source.readInt();
        int opaqueDataLength = length - 8;
        ErrorCode errorCode = ErrorCode.fromHttp2(errorCodeInt);
        if (null == errorCode) {
            throw Http2.ioException("TYPE_GOAWAY unexpected error code: %d", errorCodeInt);
        }
        ByteString debugData = ByteString.EMPTY;
        if (opaqueDataLength > 0) {
            debugData = source.readByteString(opaqueDataLength);
        }
        handler.goAway(lastStreamId, errorCode, debugData);
    }

    private void readWindowUpdate(Handler handler, int length, byte flags, int streamId)
            throws IOException {
        if (length != 4) throw Http2.ioException("TYPE_WINDOW_UPDATE length !=4: %s", length);
        long increment = (source.readInt() & 0x7fffffffL);
        if (increment == 0) throw Http2.ioException("windowSizeIncrement was 0", increment);
        handler.windowUpdate(streamId, increment);
    }

    @Override
    public void close() throws IOException {
        source.close();
    }

    interface Handler {
        void data(boolean inFinished, int streamId, BufferSource source, int length)
                throws IOException;

        /**
         * 创建或更新传入的标头，必要时创建相应的流。触发这个的帧是header和PUSH_PROMISE.
         *
         * @param inFinished         如果发送方不发送更多的帧，则为真
         * @param streamId           拥有这请求头的流.
         * @param associatedStreamId 触发发送方创建此流的流.
         * @param headerBlock        header信息
         */
        void headers(boolean inFinished, int streamId, int associatedStreamId,
                     List<Headers.Header> headerBlock);

        void rstStream(int streamId, ErrorCode errorCode);

        void settings(boolean clearPrevious, Settings settings);

        /**
         * HTTP/2 only.
         */
        void ackSettings();

        /**
         * 从对等端读取连接级ping,{@code ack}表示这是一个回复,{@code payload1}
         * 和{@code payload2}中的数据是不透明的二进制，并且没有关于内容的规则
         *
         * @param ack      the ack
         * @param payload1 the payload1
         * @param payload2 the payload2
         */
        void ping(boolean ack, int payload1, int payload2);

        /**
         * 同伴告诉我们停止创建流。在新连接上使用{@code ID > lastGoodStreamId}重播流是安全的。
         * 带有{@code ID <= lastGoodStreamId}的正在运行的流只能在新连接上重播，如果它们是幂等的
         *
         * @param lastGoodStreamId 发送此消息之前处理的最后一个流ID。
         *                         如果{@code lastGoodStreamId}为零，则该对等点不处理任何帧.
         * @param errorCode        关闭连接的原因.
         * @param debugData        只适用于HTTP/2;要发送的不透明调试数据
         */
        void goAway(int lastGoodStreamId, ErrorCode errorCode, ByteString debugData);

        /**
         * 通知可以在{@code streamId}上发送额外的{@code windowSizeIncrement}字节
         * 或者在{@code streamId}为零的情况下发送连接.
         *
         * @param streamId            拥有这请求头的流
         * @param windowSizeIncrement 字节
         */
        void windowUpdate(int streamId, long windowSizeIncrement);

        /**
         * 读取标题或优先级帧时调用。这可以用来将流的权值从默认值(16)更改为一个新值.
         *
         * @param streamId         具有优先级更改的流.
         * @param streamDependency 此流所依赖的流ID.
         * @param weight           先级的相对比例[1..256].
         * @param exclusive        将这个流ID作为{@code streamDependency}的唯一子元素插入.
         */
        void priority(int streamId, int streamDependency, int weight, boolean exclusive);

        /**
         * HTTP / 2只。接收推送承诺头块
         * 一个推送承诺包含所有与服务器发起的请求相关的报头，以及一个{@code promise streamid}
         * 它将被发送到响应帧。推送承诺帧作为响应的一部分发送到{@code streamId}.
         *
         * @param streamId         客户端发起的流ID。必须是奇数.
         * @param promisedStreamId 服务器发起的流ID。必须是偶数.
         * @param requestHeaders   最低限度包括{@code:method}、{@code:scheme}、{@code:authority}和(@code:path}.
         * @throws IOException 异常信息
         */
        void pushPromise(int streamId, int promisedStreamId, List<Headers.Header> requestHeaders)
                throws IOException;

        /**
         * HTTP/2 only. 表示用于连接或客户端发起的流的资源可从不同的网络位置或协议配置获得.
         *
         * @param streamId 当客户端发起的流ID(奇数)时，此备用服务的起源就是流的起源。当为0时，
         *                 原点在{@code origin}参数中指定.
         * @param origin   当出现时，源通常表示为scheme、主机和端口的组合。
         *                 当为空时，原点是{@code streamId}.
         * @param protocol ALPN协议，如{@code h2}.
         * @param host     IP地址或主机名。
         * @param port     与服务相关联的IP端口
         * @param maxAge   这个选项被认为是新鲜的时间(以秒为单位).
         */
        void alternateService(int streamId, String origin, ByteString protocol, String host, int port,
                              long maxAge);
    }

    /**
     * 头信息块的解压发生在帧层之上。当{@link Hpack.Reader#readHeaders()}需要延续帧时，该类延迟读取它们
     */
    static class ContinuationSource implements Source {
        private final BufferSource source;

        int length;
        byte flags;
        int streamId;

        int left;
        short padding;

        ContinuationSource(BufferSource source) {
            this.source = source;
        }

        @Override
        public long read(Buffer sink, long byteCount) throws IOException {
            while (left == 0) {
                source.skip(padding);
                padding = 0;
                if ((flags & Http2.FLAG_END_HEADERS) != 0) return -1;
                readContinuationHeader();
            }

            long read = source.read(sink, Math.min(byteCount, left));
            if (read == -1) return -1;
            left -= read;
            return read;
        }

        @Override
        public Timeout timeout() {
            return source.timeout();
        }

        @Override
        public void close() {

        }

        private void readContinuationHeader() throws IOException {
            int previousStreamId = streamId;

            length = left = readMedium(source);
            byte type = (byte) (source.readByte() & 0xff);
            flags = (byte) (source.readByte() & 0xff);
            if (Logger.isDebug()) {
                Logger.warn(Http2.frameLog(true, streamId, length, type, flags));
            }
            streamId = (source.readInt() & 0x7fffffff);
            if (type != Http2.TYPE_CONTINUATION) throw Http2.ioException("%s != TYPE_CONTINUATION", type);
            if (streamId != previousStreamId) throw Http2.ioException("TYPE_CONTINUATION streamId changed");
        }

    }

}
