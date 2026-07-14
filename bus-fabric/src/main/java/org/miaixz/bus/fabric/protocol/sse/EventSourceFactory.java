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
package org.miaixz.bus.fabric.protocol.sse;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.fabric.Call;
import org.miaixz.bus.fabric.Callback;
import org.miaixz.bus.fabric.Context;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.UnoUrl;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.protocol.http.body.PayloadBody;

/**
 * EventSource factory backed by {@link SseX}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class EventSourceFactory implements EventSource.Factory {

    /**
     * Runtime context.
     */
    private final Context context;

    /**
     * Creates a factory.
     *
     * @param context context
     */
    private EventSourceFactory(final Context context) {
        this.context = require(context, "Context");
    }

    /**
     * Creates a factory with a default context.
     *
     * @return factory
     */
    public static EventSourceFactory create() {
        return create(Context.create());
    }

    /**
     * Creates a factory.
     *
     * @param context context
     * @return factory
     */
    public static EventSourceFactory create(final Context context) {
        return new EventSourceFactory(context);
    }

    /**
     * Creates a factory.
     *
     * @param context context
     * @return factory
     */
    public static EventSourceFactory of(final Context context) {
        return create(context);
    }

    /**
     * Opens an event source from a URL.
     *
     * @param url      URL
     * @param listener listener
     * @return event source
     */
    public EventSource newEventSource(final String url, final EventSourceListener listener) {
        return newEventSource(HttpRequest.builder().method(HTTP.Method.GET).url(UnoUrl.parse(url)).build(), listener);
    }

    /**
     * Opens an event source from an HTTP request.
     *
     * @param request  request
     * @param listener listener
     * @return event source
     */
    @Override
    public EventSource newEventSource(final HttpRequest request, final EventSourceListener listener) {
        final DefaultEventSource source = new DefaultEventSource(context, require(request, "Request"),
                listener == null ? new EventSourceListener() {
                } : listener);
        source.connect();
        return source;
    }

    /**
     * Validates required references.
     *
     * @param value value
     * @param name  name
     * @param <T>   type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, name + " must not be null");
    }

    /**
     * Captured response metadata.
     *
     * @param status  status
     * @param headers headers
     */
    private record ResponseMeta(int status, Headers headers) {

        /**
         * Converts metadata to an empty HTTP response.
         *
         * @param request request
         * @return response
         */
        HttpResponse toResponse(final HttpRequest request) {
            return HttpResponse.builder().request(request).code(status).headers(headers).body(PayloadBody.empty()).build();
        }

    }

    /**
     * Event source implementation backed by one SseX call.
     */
    private static final class DefaultEventSource implements EventSource {

        /**
         * Runtime context.
         */
        private final Context context;

        /**
         * Original request.
         */
        private final HttpRequest request;

        /**
         * Listener.
         */
        private final EventSourceListener listener;

        /**
         * Response metadata.
         */
        private final AtomicReference<ResponseMeta> response;

        /**
         * Open call.
         */
        private final AtomicReference<Call<SseSession>> call;

        /**
         * Session.
         */
        private final AtomicReference<SseSession> session;

        /**
         * Whether terminal callback was delivered.
         */
        private final AtomicBoolean terminal;

        /**
         * Whether cancellation was requested by user.
         */
        private final AtomicBoolean cancelled;

        /**
         * Creates a default event source.
         *
         * @param context  context
         * @param request  request
         * @param listener listener
         */
        private DefaultEventSource(final Context context, final HttpRequest request,
                final EventSourceListener listener) {
            this.context = require(context, "Context");
            this.request = require(request, "Request");
            this.listener = require(listener, "Listener");
            this.response = new AtomicReference<>();
            this.call = new AtomicReference<>();
            this.session = new AtomicReference<>();
            this.terminal = new AtomicBoolean();
            this.cancelled = new AtomicBoolean();
        }

        /**
         * Opens this source asynchronously.
         */
        void connect() {
            if (request.method() != HTTP.Method.GET) {
                throw new ProtocolException("EventSource request must use GET");
            }
            final SseX exchange = SseX.builder(context).to(request.url().encoded()).headers(request.headers())
                    .timeout(request.timeout()).autoReconnect(false)
                    .onResponse((status, headers) -> response.set(new ResponseMeta(status, headers)))
                    .onEvent(event -> listener.onEvent(this, event.id(), event.event(), event.data()))
                    .listener(new Listener<>() {

                        @Override
                        public void close(final SseSession source) {
                            if (terminal.compareAndSet(false, true)) {
                                listener.onClosed(DefaultEventSource.this);
                            }
                        }

                        @Override
                        public void failure(final SseSession source, final Throwable cause) {
                            if (!cancelled.get() && terminal.compareAndSet(false, true)) {
                                listener.onFailure(DefaultEventSource.this, cause, response());
                            }
                        }
                    }).callback(new Callback<>() {

                        @Override
                        public void success(final SseSession value) {
                            session.set(value);
                            listener.onOpen(DefaultEventSource.this, response());
                        }

                        @Override
                        public void failure(final Throwable cause) {
                            if (!cancelled.get() && terminal.compareAndSet(false, true)) {
                                listener.onFailure(DefaultEventSource.this, cause, response());
                            }
                        }
                    }).build();
            final Call<SseSession> current = exchange.call().enqueue();
            call.set(current);
        }

        @Override
        public HttpRequest request() {
            return request;
        }

        @Override
        public void cancel() {
            cancelled.set(true);
            final SseSession currentSession = session.get();
            if (currentSession != null) {
                currentSession.cancel();
            }
            final Call<SseSession> currentCall = call.get();
            if (currentCall != null) {
                currentCall.cancel();
            }
        }

        /**
         * Returns response metadata as an HTTP response.
         *
         * @return response or null
         */
        private HttpResponse response() {
            final ResponseMeta meta = response.get();
            return meta == null ? null : meta.toResponse(request);
        }

    }

}
