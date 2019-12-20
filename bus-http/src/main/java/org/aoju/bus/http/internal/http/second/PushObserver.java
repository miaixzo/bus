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

import org.aoju.bus.core.io.segment.BufferSource;
import org.aoju.bus.http.Protocol;

import java.io.IOException;
import java.util.List;

/**
 * {@link Protocol#HTTP_2 HTTP/2} only. Processes server-initiated HTTP requests on the client.
 * Implementations must quickly dispatch callbacks to avoid creating a bottleneck.
 *
 * <p>While {@link #onReset} may occur at any time, the following callbacks are expected in order,
 * correlated by stream ID.
 *
 * <ul>
 * <li>{@link #onRequest}</li> <li>{@link #onHeaders} (unless canceled)
 * <li>{@link #onData} (optional sequence of data frames)
 * </ul>
 *
 * <p>As a stream ID is scoped to a single HTTP/2 connection, implementations which target multiple
 * connections should expect repetition of stream IDs.
 *
 * <p>Return true to request cancellation of a pushed stream.  Note that this does not guarantee
 * future frames won't arrive on the stream ID.
 *
 * @author Kimi Liu
 * @version 5.3.6
 * @since JDK 1.8+
 */
public interface PushObserver {

    PushObserver CANCEL = new PushObserver() {

        @Override
        public boolean onRequest(int streamId, List<Header> requestHeaders) {
            return true;
        }

        @Override
        public boolean onHeaders(int streamId, List<Header> responseHeaders, boolean last) {
            return true;
        }

        @Override
        public boolean onData(int streamId, BufferSource source, int byteCount,
                              boolean last) throws IOException {
            source.skip(byteCount);
            return true;
        }

        @Override
        public void onReset(int streamId, ErrorCode errorCode) {
        }
    };

    boolean onRequest(int streamId, List<Header> requestHeaders);

    boolean onHeaders(int streamId, List<Header> responseHeaders, boolean last);

    boolean onData(int streamId, BufferSource source, int byteCount, boolean last)
            throws IOException;

    void onReset(int streamId, ErrorCode errorCode);
}
