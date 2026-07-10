/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.fabric.protocol.http.http2;

import java.nio.ByteBuffer;
import java.util.List;

import org.miaixz.bus.core.instance.Instances;

/**
 * Observer for HTTP/2 server push events.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface PushObserver {

    /**
     * Returns the default observer that rejects pushed streams.
     *
     * @return canceling observer
     */
    static PushObserver canceling() {
        return Instances.get(PushObserver.class.getName() + ".canceling", () -> new PushObserver() {

            @Override
            public boolean onRequest(final int streamId, final List<Http2Header> headers) {
                return true;
            }

            @Override
            public boolean onHeaders(final int streamId, final List<Http2Header> headers, final boolean endStream) {
                return true;
            }

            @Override
            public boolean onData(final int streamId, final ByteBuffer data, final boolean endStream) {
                return true;
            }

            @Override
            public void onReset(final int streamId, final int errorCode) {
                // Reset is already terminal for a canceled observer.
            }
        });
    }

    /**
     * Handles a pushed request.
     *
     * @param streamId pushed stream id
     * @param headers  request headers
     * @return true to cancel the pushed stream
     */
    boolean onRequest(int streamId, List<Http2Header> headers);

    /**
     * Handles pushed response headers.
     *
     * @param streamId  pushed stream id
     * @param headers   response headers
     * @param endStream true when the stream ends with this event
     * @return true to cancel the pushed stream
     */
    boolean onHeaders(int streamId, List<Http2Header> headers, boolean endStream);

    /**
     * Handles pushed data.
     *
     * @param streamId  pushed stream id
     * @param data      data snapshot
     * @param endStream true when the stream ends with this event
     * @return true to cancel the pushed stream
     */
    boolean onData(int streamId, ByteBuffer data, boolean endStream);

    /**
     * Handles a pushed stream reset.
     *
     * @param streamId  pushed stream id
     * @param errorCode HTTP/2 error code
     */
    void onReset(int streamId, int errorCode);

    /**
     * Handles an advisory ALTSVC frame. The default behavior is to ignore it safely.
     *
     * @param streamId         stream id, 0 for connection scoped ALTSVC
     * @param alternateService alternate service metadata
     */
    default void onAlternateService(final int streamId, final Http2AlternateService alternateService) {
        // ALTSVC is advisory and does not alter routing unless an observer opts in.
    }

}
