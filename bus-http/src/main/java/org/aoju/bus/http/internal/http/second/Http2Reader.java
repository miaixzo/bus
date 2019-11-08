/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.http.internal.http.second;

import org.aoju.bus.core.io.segment.*;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

/**
 * Reads HTTP/2 transport frames.
 *
 * <p>This implementation assumes we do not send an increased {@link Settings#getMaxFrameSize frame
 * size setting} to the peer. Hence, we expect all frames to have a max length of {@link
 * Http2#INITIAL_MAX_FRAME_SIZE}.
 *
 * @author Kimi Liu
 * @version 5.1.0
 * @since JDK 1.8+
 */
final class Http2Reader implements Closeable {

    final Hpack.Reader hpackReader;
    private final BufferSource source;
    private final ContinuationSource continuation;
    private final boolean client;

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
            if (!nextFrame(true, handler)) {
                throw Http2.ioException("Required SETTINGS preface not received");
            }
        } else {
            ByteString connectionPreface = source.readByteString(Http2.CONNECTION_PREFACE.size());
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
        int length = readMedium(source);
        if (length < 0 || length > Http2.INITIAL_MAX_FRAME_SIZE) {
            throw Http2.ioException("FRAME_SIZE_ERROR: %s", length);
        }
        byte type = (byte) (source.readByte() & 0xff);
        if (requireSettings && type != Http2.TYPE_SETTINGS) {
            throw Http2.ioException("Expected a SETTINGS frame but was %s", type);
        }
        byte flags = (byte) (source.readByte() & 0xff);
        int streamId = (source.readInt() & 0x7fffffff); // Ignore reserved bit.

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

        List<Header> headerBlock = readHeaderBlock(length, padding, flags, streamId);

        handler.headers(endStream, streamId, -1, headerBlock);
    }

    private List<Header> readHeaderBlock(int length, short padding, byte flags, int streamId)
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

        // TODO: checkState open or half-closed (local) or raise STREAM_CLOSED
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
        if (errorCode == null) {
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
        length -= 4; // account for above read.
        length = lengthWithoutPadding(length, flags, padding);
        List<Header> headerBlock = readHeaderBlock(length, padding, flags, streamId);
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
        if (errorCode == null) {
            throw Http2.ioException("TYPE_GOAWAY unexpected error code: %d", errorCodeInt);
        }
        ByteString debugData = ByteString.EMPTY;
        if (opaqueDataLength > 0) { // Must read debug data in order to not corrupt the connection.
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

        void headers(boolean inFinished, int streamId, int associatedStreamId,
                     List<Header> headerBlock);

        void rstStream(int streamId, ErrorCode errorCode);

        void settings(boolean clearPrevious, Settings settings);

        void ackSettings();

        void ping(boolean ack, int payload1, int payload2);

        void goAway(int lastGoodStreamId, ErrorCode errorCode, ByteString debugData);

        void windowUpdate(int streamId, long windowSizeIncrement);

        void priority(int streamId, int streamDependency, int weight, boolean exclusive);

        void pushPromise(int streamId, int promisedStreamId, List<Header> requestHeaders)
                throws IOException;

        void alternateService(int streamId, String origin, ByteString protocol, String host, int port,
                              long maxAge);
    }

    static final class ContinuationSource implements Source {
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
                // TODO: test case for empty continuation header?
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
        public void close() throws IOException {
        }

        private void readContinuationHeader() throws IOException {
            int previousStreamId = streamId;

            length = left = readMedium(source);
            byte type = (byte) (source.readByte() & 0xff);
            flags = (byte) (source.readByte() & 0xff);
            streamId = (source.readInt() & 0x7fffffff);
            if (type != Http2.TYPE_CONTINUATION) throw Http2.ioException("%s != TYPE_CONTINUATION", type);
            if (streamId != previousStreamId) throw Http2.ioException("TYPE_CONTINUATION streamId changed");
        }
    }

}
