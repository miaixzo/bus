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
package org.miaixz.bus.http.metric.http;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.BufferSink;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.logger.Logger;

/**
 * Writes HTTP/2 transport frames.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Http2Writer implements Closeable {

    /**
     * The HPACK writer for encoding headers.
     */
    final Hpack.Writer hpackWriter;
    /**
     * The underlying sink to which frames are written.
     */
    private final BufferSink sink;
    /**
     * True if this writer is for a client endpoint.
     */
    private final boolean client;
    /**
     * A buffer used for HPACK encoding.
     */
    private final Buffer hpackBuffer;
    /**
     * The maximum number of bytes that may be sent in a single DATA frame.
     */
    private int maxFrameSize;
    /**
     * True if this writer has been closed.
     */
    private boolean closed;

    /**
     * Constructs a new Http2Writer.
     *
     * @param sink   The sink to write to.
     * @param client True if this is a client endpoint.
     */
    Http2Writer(BufferSink sink, boolean client) {
        this.sink = sink;
        this.client = client;
        this.hpackBuffer = new Buffer();
        this.hpackWriter = new Hpack.Writer(hpackBuffer);
        this.maxFrameSize = Http2.INITIAL_MAX_FRAME_SIZE;
    }

    /**
     * Writes an unsigned 24-bit integer.
     *
     * @param sink The sink to write to.
     * @param i    The integer to write.
     * @throws IOException if an I/O error occurs.
     */
    private static void writeMedium(BufferSink sink, int i) throws IOException {
        sink.writeByte((i >>> Normal._16) & 0xff);
        sink.writeByte((i >>> 8) & 0xff);
        sink.writeByte(i & 0xff);
    }

    /**
     * Writes the HTTP/2 connection preface. This must be sent by the client at the beginning of a connection.
     *
     * @throws IOException if an I/O error occurs.
     */
    public synchronized void connectionPreface() throws IOException {
        if (closed)
            throw new IOException("closed");
        if (!client)
            return; // Nothing to write; servers don't send connection headers!
        if (Logger.isDebugEnabled()) {
            Logger.warn(String.format(">> CONNECTION %s", Http2.CONNECTION_PREFACE.hex()));
        }
        sink.write(Http2.CONNECTION_PREFACE.toByteArray());
        sink.flush();
    }

    /**
     * Applies {@code peerSettings} and then sends a settings ACK.
     *
     * @param peerSettings The settings received from the peer.
     * @throws IOException if an I/O error occurs.
     */
    public synchronized void applyAndAckSettings(Http2Settings peerSettings) throws IOException {
        if (closed)
            throw new IOException("closed");
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
     * HTTP/2 only. Sends push promise headers. A push promise contains all the headers associated with a
     * server-initiated request, and a {@code promisedStreamId} that will be used for the response frames. The push
     * promise frame is sent as part of the response to {@code streamId}. The priority of {@code promisedStreamId} is
     * one greater than that of {@code streamId}.
     *
     * @param streamId         The client-initiated stream ID. Must be an odd number.
     * @param promisedStreamId The server-initiated stream ID. Must be an even number.
     * @param requestHeaders   Minimally includes {@code :method}, {@code :scheme}, {@code :authority}, and
     *                         {@code :path}.
     * @throws IOException if an I/O error occurs.
     */
    public synchronized void pushPromise(int streamId, int promisedStreamId, List<Http2Header> requestHeaders)
            throws IOException {
        if (closed)
            throw new IOException("closed");
        hpackWriter.writeHeaders(requestHeaders);

        long byteCount = hpackBuffer.size();
        int length = (int) Math.min(maxFrameSize - 4, byteCount);
        byte type = Http2.TYPE_PUSH_PROMISE;
        byte flags = byteCount == length ? Http2.FLAG_END_HEADERS : 0;
        frameHeader(streamId, length + 4, type, flags);
        sink.writeInt(promisedStreamId & 0x7fffffff);
        sink.write(hpackBuffer, length);

        if (byteCount > length)
            writeContinuationFrames(streamId, byteCount - length);
    }

    /**
     * Flushes all buffered data on the underlying sink.
     *
     * @throws IOException if an I/O error occurs.
     */
    public synchronized void flush() throws IOException {
        if (closed)
            throw new IOException("closed");
        sink.flush();
    }

    /**
     * Sends a RST_STREAM frame to terminate a stream.
     *
     * @param streamId  The stream ID.
     * @param errorCode The error code indicating the reason for termination.
     * @throws IOException if an I/O error occurs.
     */
    public synchronized void rstStream(int streamId, Http2ErrorCode errorCode) throws IOException {
        if (closed)
            throw new IOException("closed");
        if (errorCode.httpCode == -1)
            throw new IllegalArgumentException();

        int length = 4;
        byte type = Http2.TYPE_RST_STREAM;
        byte flags = Http2.FLAG_NONE;
        frameHeader(streamId, length, type, flags);
        sink.writeInt(errorCode.httpCode);
        sink.flush();
    }

    /**
     * Returns the maximum size of bytes that may be sent in a single call to {@link #data}.
     *
     * @return The maximum data length.
     */
    public int maxDataLength() {
        return maxFrameSize;
    }

    /**
     * Sends a DATA frame.
     *
     * @param outFinished True if this is the last frame to be sent on this stream.
     * @param streamId    The stream ID.
     * @param source      The buffer to draw bytes from. May be null if byteCount is 0.
     * @param byteCount   Must be between 0 and the minimum of {@code source.length} and {@link #maxDataLength}.
     * @throws IOException if an I/O error occurs.
     */
    public synchronized void data(boolean outFinished, int streamId, Buffer source, int byteCount) throws IOException {
        if (closed)
            throw new IOException("closed");
        byte flags = Http2.FLAG_NONE;
        if (outFinished)
            flags |= Http2.FLAG_END_STREAM;
        dataFrame(streamId, flags, source, byteCount);
    }

    /**
     * Writes a DATA frame.
     *
     * @param streamId  The stream ID.
     * @param flags     The frame flags.
     * @param buffer    The buffer containing the data.
     * @param byteCount The number of bytes to write.
     * @throws IOException if an I/O error occurs.
     */
    void dataFrame(int streamId, byte flags, Buffer buffer, int byteCount) throws IOException {
        byte type = Http2.TYPE_DATA;
        frameHeader(streamId, byteCount, type, flags);
        if (byteCount > 0) {
            sink.write(buffer, byteCount);
        }
    }

    /**
     * Writes HTTP/2 settings to the peer.
     *
     * @param settings The settings to send.
     * @throws IOException if an I/O error occurs.
     */
    public synchronized void settings(Http2Settings settings) throws IOException {
        if (closed)
            throw new IOException("closed");
        int length = settings.size() * 6;
        byte type = Http2.TYPE_SETTINGS;
        byte flags = Http2.FLAG_NONE;
        int streamId = 0;
        frameHeader(streamId, length, type, flags);
        for (int i = 0; i < Http2Settings.COUNT; i++) {
            if (!settings.isSet(i))
                continue;
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
     * Sends a connection-level ping to the peer. {@code ack} indicates this is a reply. The data in {@code payload1}
     * and {@code payload2} is opaque binary, and there are no rules on the content.
     *
     * @param ack      True if this is a reply to a ping from the peer.
     * @param payload1 The first payload integer.
     * @param payload2 The second payload integer.
     * @throws IOException if an I/O error occurs.
     */
    public synchronized void ping(boolean ack, int payload1, int payload2) throws IOException {
        if (closed)
            throw new IOException("closed");
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
     * Informs the peer to stop creating streams. We last processed {@code lastGoodStreamId}, or zero if no streams were
     * processed.
     *
     * @param lastGoodStreamId The last stream ID that was processed, or zero if no streams were processed.
     * @param errorCode        The reason for closing the connection.
     * @param debugData        Opaque debug data for HTTP/2 only.
     * @throws IOException if an I/O error occurs.
     */
    public synchronized void goAway(int lastGoodStreamId, Http2ErrorCode errorCode, byte[] debugData)
            throws IOException {
        if (closed)
            throw new IOException("closed");
        if (errorCode.httpCode == -1)
            throw Http2.illegalArgument("errorCode.httpCode == -1");
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
     * Inform peer that an additional {@code windowSizeIncrement} bytes can be sent on {@code streamId}, or on the
     * connection if {@code streamId} is zero.
     *
     * @param streamId            The stream ID.
     * @param windowSizeIncrement The number of bytes to increment the window by.
     * @throws IOException if an I/O error occurs.
     */
    public synchronized void windowUpdate(int streamId, long windowSizeIncrement) throws IOException {
        if (closed)
            throw new IOException("closed");
        if (windowSizeIncrement == 0 || windowSizeIncrement > 0x7fffffffL) {
            throw Http2.illegalArgument(
                    "windowSizeIncrement == 0 || windowSizeIncrement > 0x7fffffffL: %s",
                    windowSizeIncrement);
        }
        int length = 4;
        byte type = Http2.TYPE_WINDOW_UPDATE;
        byte flags = Http2.FLAG_NONE;
        frameHeader(streamId, length, type, flags);
        sink.writeInt((int) windowSizeIncrement);
        sink.flush();
    }

    /**
     * Writes an HTTP/2 frame header.
     *
     * @param streamId The stream ID.
     * @param length   The length of the frame payload.
     * @param type     The frame type.
     * @param flags    The frame flags.
     * @throws IOException if an I/O error occurs.
     */
    public void frameHeader(int streamId, int length, byte type, byte flags) throws IOException {
        if (Logger.isDebugEnabled()) {
            Logger.warn(Http2.frameLog(false, streamId, length, type, flags));
        }
        if (length > maxFrameSize) {
            throw Http2.illegalArgument("FRAME_SIZE_ERROR length > %d: %d", maxFrameSize, length);
        }
        if ((streamId & 0x80000000) != 0)
            throw Http2.illegalArgument("reserved bit set: %s", streamId);
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

    /**
     * Writes CONTINUATION frames if the header block is larger than the maximum frame size.
     *
     * @param streamId  The stream ID.
     * @param byteCount The number of bytes to write.
     * @throws IOException if an I/O error occurs.
     */
    private void writeContinuationFrames(int streamId, long byteCount) throws IOException {
        while (byteCount > 0) {
            int length = (int) Math.min(maxFrameSize, byteCount);
            byteCount -= length;
            frameHeader(streamId, length, Http2.TYPE_CONTINUATION, byteCount == 0 ? Http2.FLAG_END_HEADERS : 0);
            sink.write(hpackBuffer, length);
        }
    }

    /**
     * Writes a HEADERS frame, followed by any necessary CONTINUATION frames.
     *
     * @param outFinished True if this is the last frame to be sent on this stream.
     * @param streamId    The stream ID.
     * @param headerBlock The list of headers to write.
     * @throws IOException if an I/O error occurs.
     */
    public synchronized void headers(boolean outFinished, int streamId, List<Http2Header> headerBlock)
            throws IOException {
        if (closed)
            throw new IOException("closed");
        hpackWriter.writeHeaders(headerBlock);

        long byteCount = hpackBuffer.size();
        int length = (int) Math.min(maxFrameSize, byteCount);
        byte type = Http2.TYPE_HEADERS;
        byte flags = byteCount == length ? Http2.FLAG_END_HEADERS : 0;
        if (outFinished)
            flags |= Http2.FLAG_END_STREAM;
        frameHeader(streamId, length, type, flags);
        sink.write(hpackBuffer, length);

        if (byteCount > length)
            writeContinuationFrames(streamId, byteCount - length);
    }

}
