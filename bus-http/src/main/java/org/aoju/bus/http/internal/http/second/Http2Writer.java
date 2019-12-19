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

import org.aoju.bus.core.io.segment.Buffer;
import org.aoju.bus.core.io.segment.BufferSink;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

/**
 * Writes HTTP/2 transport frames.
 *
 * @author Kimi Liu
 * @version 5.3.5
 * @since JDK 1.8+
 */
final class Http2Writer implements Closeable {

    final Hpack.Writer hpackWriter;
    private final BufferSink sink;
    private final boolean client;
    private final Buffer hpackBuffer;
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
        sink.writeByte((i >>> 16) & 0xff);
        sink.writeByte((i >>> 8) & 0xff);
        sink.writeByte(i & 0xff);
    }

    public synchronized void connectionPreface() throws IOException {
        if (closed) throw new IOException("closed");
        if (!client) return; // Nothing to write; servers don't send connection headers!
        sink.write(Http2.CONNECTION_PREFACE.toByteArray());
        sink.flush();
    }

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

    public synchronized void pushPromise(int streamId, int promisedStreamId,
                                         List<Header> requestHeaders) throws IOException {
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

    public synchronized void synStream(boolean outFinished, int streamId,
                                       int associatedStreamId, List<Header> headerBlock) throws IOException {
        if (closed) throw new IOException("closed");
        headers(outFinished, streamId, headerBlock);
    }

    public synchronized void synReply(boolean outFinished, int streamId,
                                      List<Header> headerBlock) throws IOException {
        if (closed) throw new IOException("closed");
        headers(outFinished, streamId, headerBlock);
    }

    public synchronized void headers(int streamId, List<Header> headerBlock)
            throws IOException {
        if (closed) throw new IOException("closed");
        headers(false, streamId, headerBlock);
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

    public int maxDataLength() {
        return maxFrameSize;
    }

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
                id = 3; // SETTINGS_MAX_CONCURRENT_STREAMS renumbered.
            } else if (id == 7) {
                id = 4; // SETTINGS_INITIAL_WINDOW_SIZE renumbered.
            }
            sink.writeShort(id);
            sink.writeInt(settings.get(i));
        }
        sink.flush();
    }

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

    void headers(boolean outFinished, int streamId, List<Header> headerBlock) throws IOException {
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
