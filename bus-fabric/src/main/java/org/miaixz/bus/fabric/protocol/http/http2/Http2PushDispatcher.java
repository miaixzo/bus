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

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;

/**
 * Non-blocking HTTP/2 push-observer dispatcher and pushed-stream identity owner.
 *
 * @author Kimi Liu
 */
final class Http2PushDispatcher {

    /**
     * Push observer.
     */
    private final PushObserver observer;

    /**
     * Runtime dispatcher.
     */
    private final Dispatcher dispatcher;

    /**
     * Pushed stream identifiers.
     */
    private final Set<Integer> streams = ConcurrentHashMap.newKeySet();

    /**
     * Creates a push dispatcher.
     *
     * @param observer   observer
     * @param dispatcher dispatcher
     */
    Http2PushDispatcher(final PushObserver observer, final Dispatcher dispatcher) {
        this.observer = observer;
        this.dispatcher = dispatcher;
    }

    /**
     * Registers one pushed stream.
     */
    boolean add(final int streamId) {
        return streams.add(streamId);
    }

    /**
     * Removes one pushed stream.
     */
    boolean remove(final int streamId) {
        return streams.remove(streamId);
    }

    /**
     * Returns whether an identifier belongs to push.
     */
    boolean contains(final int streamId) {
        return streams.contains(streamId);
    }

    /**
     * Clears all pushed stream tracking.
     */
    void clear() {
        streams.clear();
    }

    /**
     * Dispatches PUSH_PROMISE request metadata.
     */
    CompletableFuture<Boolean> request(final int streamId, final List<Http2Header> headers) {
        return dispatcher.supply("http2:push-request:" + streamId, () -> observer.onRequest(streamId, headers));
    }

    /**
     * Dispatches pushed response headers.
     */
    CompletableFuture<Boolean> headers(final int streamId, final List<Http2Header> headers, final boolean endStream) {
        return dispatcher
                .supply("http2:push-headers:" + streamId, () -> observer.onHeaders(streamId, headers, endStream));
    }

    /**
     * Dispatches pushed DATA.
     */
    CompletableFuture<Boolean> data(final int streamId, final ByteString data, final boolean endStream) {
        return dispatcher.supply("http2:push-data:" + streamId, () -> observer.onData(streamId, data, endStream));
    }

    /**
     * Dispatches pushed RST_STREAM notification.
     */
    CompletableFuture<Void> reset(final int streamId, final int errorCode) {
        return dispatcher.run("http2:push-reset:" + streamId, () -> observer.onReset(streamId, errorCode));
    }

    /**
     * Dispatches ALTSVC notification without blocking the frame reader.
     */
    CompletableFuture<Void> alternateService(final int streamId, final Http2AlternateService service) {
        return dispatcher
                .run("http2:push-alternate-service:" + streamId, () -> observer.onAlternateService(streamId, service));
    }

}
