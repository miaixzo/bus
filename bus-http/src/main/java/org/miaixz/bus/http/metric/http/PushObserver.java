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
package org.miaixz.bus.http.metric.http;

import org.miaixz.bus.core.io.source.BufferSource;
import org.miaixz.bus.core.net.Protocol;

import java.io.IOException;
import java.util.List;

/**
 * An interface for handling server-initiated HTTP requests, specific to {@link Protocol#HTTP_2 HTTP/2}.
 * <p>
 * Implementations of this interface can choose to cancel pushed streams by returning {@code true}. Note that this does
 * not guarantee that future frames will not arrive for the canceled stream ID.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface PushObserver {

    /**
     * A push observer that cancels all incoming pushed streams.
     */
    PushObserver CANCEL = new PushObserver() {

        @Override
        public boolean onRequest(int streamId, List<Http2Header> requestHeaders) {
            return true; // Cancel the stream.
        }

        @Override
        public boolean onHeaders(int streamId, List<Http2Header> responseHeaders, boolean last) {
            return true; // Cancel the stream.
        }

        @Override
        public boolean onData(int streamId, BufferSource source, int byteCount, boolean last) throws IOException {
            source.skip(byteCount);
            return true; // Cancel the stream.
        }

        @Override
        public void onReset(int streamId, Http2ErrorCode errorCode) {
            // Do nothing.
        }
    };

    /**
     * Describes the request that the server intends to push a response for.
     *
     * @param streamId       The server-initiated stream ID, which will be an even number.
     * @param requestHeaders The request headers, minimally including method, scheme, authority, and path.
     * @return {@code true} to cancel the pushed stream, {@code false} to accept it.
     */
    boolean onRequest(int streamId, List<Http2Header> requestHeaders);

    /**
     * The response headers corresponding to the pushed request. When {@code last} is true, there are no subsequent data
     * frames.
     *
     * @param streamId        The server-initiated stream ID, which will be an even number.
     * @param responseHeaders The response headers, minimally including the status.
     * @param last            {@code true} if there is no response data.
     * @return {@code true} to cancel the pushed stream, {@code false} to accept it.
     */
    boolean onHeaders(int streamId, List<Http2Header> responseHeaders, boolean last);

    /**
     * A block of response data corresponding to the pushed request. This data must be read or skipped.
     *
     * @param streamId  The server-initiated stream ID, which will be an even number.
     * @param source    The source of the data corresponding to this stream ID.
     * @param byteCount The number of bytes to read or skip from the source.
     * @param last      {@code true} if no more data frames will follow.
     * @return {@code true} to cancel the pushed stream, {@code false} to accept it.
     * @throws IOException if an I/O error occurs.
     */
    boolean onData(int streamId, BufferSource source, int byteCount, boolean last) throws IOException;

    /**
     * Indicates the reason why this stream was canceled.
     *
     * @param streamId  The server-initiated stream ID, which will be an even number.
     * @param errorCode The error code indicating the reason for cancellation.
     */
    void onReset(int streamId, Http2ErrorCode errorCode);

}
