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

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.source.BufferSource;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.logger.Logger;

/**
 * Reads HTTP/2 transport frames. This implementation assumes we haven't sent a SETTINGS frame to the peer that
 * increases the frame size. Therefore, we expect all frames to have a maximum length of
 * {@link Http2#INITIAL_MAX_FRAME_SIZE}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Http2Reader implements Closeable {

    /**
     * The HPACK reader for decoding headers.
     */
    public final Hpack.Reader hpackReader;
    /**
     * The underlying source from which frames are read.
     */
    public final BufferSource source;
    /**
     * A source that reads continuation frames.
     */
    public final ContinuationSource continuation;
    /**
     * True if this is a client endpoint.
     */
    public final boolean client;

    /**
     * Creates a frame reader with a max header table size of 4096.
     *
     * @param source The source to read from.
     * @param client True if this is a client endpoint.
     */
    Http2Reader(BufferSource source, boolean client) {
        this.source = source;
        this.client = client;
        this.continuation = new ContinuationSource(this.source);
        this.hpackReader = new Hpack.Reader(4096, continuation);
    }

    /**
     * Reads an unsigned 24-bit integer.
     *
     * @param source The source to read from.
     * @return The integer value.
     * @throws IOException if an I/O error occurs.
     */
    static int readMedium(BufferSource source) throws IOException {
        return (source.readByte() & 0xff) << 16 | (source.readByte() & 0xff) << 8 | (source.readByte() & 0xff);
    }

    /**
     * Calculates the length of the frame payload, excluding padding.
     *
     * @param length  The total length of the frame.
     * @param flags   The frame flags.
     * @param padding The padding length.
     * @return The length of the payload without padding.
     * @throws IOException if the padding is greater than the remaining length.
     */
    static int lengthWithoutPadding(int length, byte flags, short padding) throws IOException {
        if ((flags & Http2.FLAG_PADDED) != 0)
            length--; // Account for reading the padding length.
        if (padding > length) {
            throw Http2.ioException("PROTOCOL_ERROR padding %s > remaining length %s", padding, length);
        }
        return (short) (length - padding);
    }

    /**
     * Reads the connection preface, which is different for clients and servers.
     *
     * @param handler The handler for frame events.
     * @throws IOException if an I/O error occurs or the preface is incorrect.
     */
    public void readConnectionPreface(Handler handler) throws IOException {
        if (client) {
            // The client reads the initial SETTINGS frame.
            if (!nextFrame(true, handler)) {
                throw Http2.ioException("Required SETTINGS preface not received");
            }
        } else {
            ByteString connectionPreface = source.readByteString(Http2.CONNECTION_PREFACE.size());
            if (Logger.isDebugEnabled()) {
                Logger.debug(String.format("<< CONNECTION %s" + connectionPreface.hex()));
            }
            if (!Http2.CONNECTION_PREFACE.equals(connectionPreface)) {
                throw Http2.ioException("Expected a connection header but was %s", connectionPreface.utf8());
            }
        }
    }

    /**
     * Reads the next frame from the source. This is a blocking call.
     *
     * @param requireSettings True if the next frame must be a SETTINGS frame.
     * @param handler         The handler for frame events.
     * @return True if a frame was read, false if the stream is exhausted.
     * @throws IOException if an I/O error occurs.
     */
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
        int streamId = (source.readInt() & 0x7fffffff);
        if (Logger.isDebugEnabled()) {
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

    /**
     * Reads a HEADERS frame.
     *
     * @param handler  The handler for frame events.
     * @param length   The length of the frame.
     * @param flags    The frame flags.
     * @param streamId The stream ID.
     * @throws IOException if an I/O error occurs.
     */
    private void readHeaders(Handler handler, int length, byte flags, int streamId) throws IOException {
        if (streamId == 0)
            throw Http2.ioException("PROTOCOL_ERROR: TYPE_HEADERS streamId == 0");

        boolean endStream = (flags & Http2.FLAG_END_STREAM) != 0;

        short padding = (flags & Http2.FLAG_PADDED) != 0 ? (short) (source.readByte() & 0xff) : 0;

        if ((flags & Http2.FLAG_PRIORITY) != 0) {
            readPriority(handler, streamId);
            length -= 5;
        }

        length = lengthWithoutPadding(length, flags, padding);

        List<Http2Header> headerBlock = readHeaderBlock(length, padding, flags, streamId);

        handler.headers(endStream, streamId, -1, headerBlock);
    }

    /**
     * Reads a header block, which may be spread across multiple CONTINUATION frames.
     *
     * @param length   The length of the header block.
     * @param padding  The padding length.
     * @param flags    The frame flags.
     * @param streamId The stream ID.
     * @return A list of decoded headers.
     * @throws IOException if an I/O error occurs.
     */
    private List<Http2Header> readHeaderBlock(int length, short padding, byte flags, int streamId) throws IOException {
        continuation.length = continuation.left = length;
        continuation.padding = padding;
        continuation.flags = flags;
        continuation.streamId = streamId;

        hpackReader.readHeaders();
        return hpackReader.getAndResetHeaderList();
    }

    /**
     * Reads a DATA frame.
     *
     * @param handler  The handler for frame events.
     * @param length   The length of the frame.
     * @param flags    The frame flags.
     * @param streamId The stream ID.
     * @throws IOException if an I/O error occurs.
     */
    private void readData(Handler handler, int length, byte flags, int streamId) throws IOException {
        if (streamId == 0)
            throw Http2.ioException("PROTOCOL_ERROR: TYPE_DATA streamId == 0");

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

    /**
     * Reads a PRIORITY frame.
     *
     * @param handler  The handler for frame events.
     * @param length   The length of the frame.
     * @param flags    The frame flags.
     * @param streamId The stream ID.
     * @throws IOException if an I/O error occurs.
     */
    private void readPriority(Handler handler, int length, byte flags, int streamId) throws IOException {
        if (length != 5)
            throw Http2.ioException("TYPE_PRIORITY length: %d != 5", length);
        if (streamId == 0)
            throw Http2.ioException("TYPE_PRIORITY streamId == 0");
        readPriority(handler, streamId);
    }

    /**
     * Reads the payload of a PRIORITY frame.
     *
     * @param handler  The handler for frame events.
     * @param streamId The stream ID.
     * @throws IOException if an I/O error occurs.
     */
    private void readPriority(Handler handler, int streamId) throws IOException {
        int w1 = source.readInt();
        boolean exclusive = (w1 & 0x80000000) != 0;
        int streamDependency = (w1 & 0x7fffffff);
        int weight = (source.readByte() & 0xff) + 1;
        handler.priority(streamId, streamDependency, weight, exclusive);
    }

    /**
     * Reads a RST_STREAM frame.
     *
     * @param handler  The handler for frame events.
     * @param length   The length of the frame.
     * @param flags    The frame flags.
     * @param streamId The stream ID.
     * @throws IOException if an I/O error occurs.
     */
    private void readRstStream(Handler handler, int length, byte flags, int streamId) throws IOException {
        if (length != 4)
            throw Http2.ioException("TYPE_RST_STREAM length: %d != 4", length);
        if (streamId == 0)
            throw Http2.ioException("TYPE_RST_STREAM streamId == 0");
        int errorCodeInt = source.readInt();
        Http2ErrorCode errorCode = Http2ErrorCode.fromHttp2(errorCodeInt);
        if (null == errorCode) {
            throw Http2.ioException("TYPE_RST_STREAM unexpected error code: %d", errorCodeInt);
        }
        handler.rstStream(streamId, errorCode);
    }

    /**
     * Reads a SETTINGS frame.
     *
     * @param handler  The handler for frame events.
     * @param length   The length of the frame.
     * @param flags    The frame flags.
     * @param streamId The stream ID.
     * @throws IOException if an I/O error occurs.
     */
    private void readSettings(Handler handler, int length, byte flags, int streamId) throws IOException {
        if (streamId != 0)
            throw Http2.ioException("TYPE_SETTINGS streamId != 0");
        if ((flags & Http2.FLAG_ACK) != 0) {
            if (length != 0)
                throw Http2.ioException("FRAME_SIZE_ERROR ack frame should be empty!");
            handler.ackSettings();
            return;
        }

        if (length % 6 != 0)
            throw Http2.ioException("TYPE_SETTINGS length %% 6 != 0: %s", length);
        Http2Settings settings = new Http2Settings();
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

    /**
     * Reads a PUSH_PROMISE frame.
     *
     * @param handler  The handler for frame events.
     * @param length   The length of the frame.
     * @param flags    The frame flags.
     * @param streamId The stream ID.
     * @throws IOException if an I/O error occurs.
     */
    private void readPushPromise(Handler handler, int length, byte flags, int streamId) throws IOException {
        if (streamId == 0) {
            throw Http2.ioException("PROTOCOL_ERROR: TYPE_PUSH_PROMISE streamId == 0");
        }
        short padding = (flags & Http2.FLAG_PADDED) != 0 ? (short) (source.readByte() & 0xff) : 0;
        int promisedStreamId = source.readInt() & 0x7fffffff;
        length -= 4;
        length = lengthWithoutPadding(length, flags, padding);
        List<Http2Header> headerBlock = readHeaderBlock(length, padding, flags, streamId);
        handler.pushPromise(streamId, promisedStreamId, headerBlock);
    }

    /**
     * Reads a PING frame.
     *
     * @param handler  The handler for frame events.
     * @param length   The length of the frame.
     * @param flags    The frame flags.
     * @param streamId The stream ID.
     * @throws IOException if an I/O error occurs.
     */
    private void readPing(Handler handler, int length, byte flags, int streamId) throws IOException {
        if (length != 8)
            throw Http2.ioException("TYPE_PING length != 8: %s", length);
        if (streamId != 0)
            throw Http2.ioException("TYPE_PING streamId != 0");
        int payload1 = source.readInt();
        int payload2 = source.readInt();
        boolean ack = (flags & Http2.FLAG_ACK) != 0;
        handler.ping(ack, payload1, payload2);
    }

    /**
     * Reads a GOAWAY frame.
     *
     * @param handler  The handler for frame events.
     * @param length   The length of the frame.
     * @param flags    The frame flags.
     * @param streamId The stream ID.
     * @throws IOException if an I/O error occurs.
     */
    private void readGoAway(Handler handler, int length, byte flags, int streamId) throws IOException {
        if (length < 8)
            throw Http2.ioException("TYPE_GOAWAY length < 8: %s", length);
        if (streamId != 0)
            throw Http2.ioException("TYPE_GOAWAY streamId != 0");
        int lastStreamId = source.readInt();
        int errorCodeInt = source.readInt();
        int opaqueDataLength = length - 8;
        Http2ErrorCode errorCode = Http2ErrorCode.fromHttp2(errorCodeInt);
        if (null == errorCode) {
            throw Http2.ioException("TYPE_GOAWAY unexpected error code: %d", errorCodeInt);
        }
        ByteString debugData = ByteString.EMPTY;
        if (opaqueDataLength > 0) {
            debugData = source.readByteString(opaqueDataLength);
        }
        handler.goAway(lastStreamId, errorCode, debugData);
    }

    /**
     * Reads a WINDOW_UPDATE frame.
     *
     * @param handler  The handler for frame events.
     * @param length   The length of the frame.
     * @param flags    The frame flags.
     * @param streamId The stream ID.
     * @throws IOException if an I/O error occurs.
     */
    private void readWindowUpdate(Handler handler, int length, byte flags, int streamId) throws IOException {
        if (length != 4)
            throw Http2.ioException("TYPE_WINDOW_UPDATE length !=4: %s", length);
        long increment = (source.readInt() & 0x7fffffffL);
        if (increment == 0)
            throw Http2.ioException("windowSizeIncrement was 0", increment);
        handler.windowUpdate(streamId, increment);
    }

    @Override
    public void close() throws IOException {
        source.close();
    }

    /**
     * Handler for HTTP/2 frames.
     */
    interface Handler {

        /**
         * Handles a DATA frame.
         * 
         * @param inFinished True if this is the last frame of the stream.
         * @param streamId   The stream ID.
         * @param source     The source of the data.
         * @param length     The length of the data.
         * @throws IOException if an I/O error occurs.
         */
        void data(boolean inFinished, int streamId, BufferSource source, int length) throws IOException;

        /**
         * Creates or updates incoming headers, creating the corresponding stream if necessary. The frames that trigger
         * this are HEADERS and PUSH_PROMISE.
         *
         * @param inFinished         true if the sender will not send more frames.
         * @param streamId           the stream that owns these headers.
         * @param associatedStreamId the stream that triggered the sender to create this stream.
         * @param headerBlock        the header block.
         */
        void headers(boolean inFinished, int streamId, int associatedStreamId, List<Http2Header> headerBlock);

        /**
         * Handles a RST_STREAM frame.
         * 
         * @param streamId  The stream ID.
         * @param errorCode The error code.
         */
        void rstStream(int streamId, Http2ErrorCode errorCode);

        /**
         * Handles a SETTINGS frame.
         * 
         * @param clearPrevious True if the settings should be cleared before applying.
         * @param settings      The settings to apply.
         */
        void settings(boolean clearPrevious, Http2Settings settings);

        /**
         * HTTP/2 only. Acknowledges the reception of a SETTINGS frame.
         */
        void ackSettings();

        /**
         * Reads a connection-level ping from the peer. {@code ack} indicates this is a reply. The data in
         * {@code payload1} and {@code payload2} is opaque binary and there are no rules on its content.
         *
         * @param ack      the ack flag.
         * @param payload1 the first payload integer.
         * @param payload2 the second payload integer.
         */
        void ping(boolean ack, int payload1, int payload2);

        /**
         * The peer tells us to stop creating streams. It is safe to replay streams with {@code ID >
         * lastGoodStreamId} on a new connection. A running stream with {@code ID <= lastGoodStreamId} can only be
         * replayed on a new connection if it is idempotent.
         *
         * @param lastGoodStreamId The last stream ID that was processed before this message was sent. If
         *                         {@code lastGoodStreamId} is zero, the peer did not process any frames.
         * @param errorCode        The reason for closing the connection.
         * @param debugData        Opaque debug data for HTTP/2 only.
         */
        void goAway(int lastGoodStreamId, Http2ErrorCode errorCode, ByteString debugData);

        /**
         * Notifies that an additional {@code windowSizeIncrement} bytes can be sent on {@code streamId}, or on the
         * connection if {@code streamId} is zero.
         *
         * @param streamId            The stream that owns these headers.
         * @param windowSizeIncrement The number of bytes.
         */
        void windowUpdate(int streamId, long windowSizeIncrement);

        /**
         * Called when a HEADERS or PRIORITY frame is read. This can be used to change the stream's weight from the
         * default (16) to a new value.
         *
         * @param streamId         The stream with the priority change.
         * @param streamDependency The stream ID that this stream depends on.
         * @param weight           The relative weight of the priority [1..256].
         * @param exclusive        Inserts this stream ID as the sole child of {@code streamDependency}.
         */
        void priority(int streamId, int streamDependency, int weight, boolean exclusive);

        /**
         * HTTP/2 only. Receives a push promise header block. A push promise contains all the headers associated with a
         * server-initiated request, and a {@code promisedStreamId} that will be sent in a subsequent response frame.
         * The push promise frame is sent as part of the response to {@code streamId}.
         *
         * @param streamId         The client-initiated stream ID. Must be odd.
         * @param promisedStreamId The server-initiated stream ID. Must be even.
         * @param requestHeaders   Minimally includes {@code :method}, {@code :scheme}, {@code :authority}, and
         *                         {@code :path}.
         * @throws IOException if an I/O error occurs.
         */
        void pushPromise(int streamId, int promisedStreamId, List<Http2Header> requestHeaders) throws IOException;

        /**
         * HTTP/2 only. Indicates that a resource is available from a different network location or with a different
         * protocol configuration.
         *
         * @param streamId When a client-initiated stream ID (odd number), the origin of this alternate service is the
         *                 origin of the stream. When 0, the origin is specified in the {@code origin} parameter.
         * @param origin   When present, the origin is typically represented as a combination of scheme, host, and port.
         *                 When empty, the origin is that of the {@code streamId}.
         * @param protocol ALPN protocol, such as {@code h2}.
         * @param host     IP address or hostname.
         * @param port     The IP port associated with the service.
         * @param maxAge   The time in seconds that this option is considered fresh.
         */
        void alternateService(int streamId, String origin, ByteString protocol, String host, int port, long maxAge);
    }

    /**
     * Decompression of the header block occurs above the frame layer. When {@link Hpack.Reader#readHeaders()} needs
     * continuation frames, this class lazily reads them.
     */
    static class ContinuationSource implements Source {

        /**
         * 
         * The underlying source.
         */
        private final BufferSource source;

        /**
         * The length of the current frame.
         */
        int length;
        /**
         * The flags of the current frame.
         */
        byte flags;
        /**
         * The stream ID of the current frame.
         */
        int streamId;

        /**
         * The number of bytes left to read in the current frame.
         */
        int left;
        /**
         * The padding length of the current frame.
         */
        short padding;

        ContinuationSource(BufferSource source) {
            this.source = source;
        }

        @Override
        public long read(Buffer sink, long byteCount) throws IOException {
            while (left == 0) {
                source.skip(padding);
                padding = 0;
                if ((flags & Http2.FLAG_END_HEADERS) != 0)
                    return -1;
                readContinuationHeader();
            }

            long read = source.read(sink, Math.min(byteCount, left));
            if (read == -1)
                return -1;
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
            if (Logger.isDebugEnabled()) {
                Logger.warn(Http2.frameLog(true, streamId, length, type, flags));
            }
            streamId = (source.readInt() & 0x7fffffff);
            if (type != Http2.TYPE_CONTINUATION)
                throw Http2.ioException("%s != TYPE_CONTINUATION", type);
            if (streamId != previousStreamId)
                throw Http2.ioException("TYPE_CONTINUATION streamId changed");
        }

    }

}
