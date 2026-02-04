/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
