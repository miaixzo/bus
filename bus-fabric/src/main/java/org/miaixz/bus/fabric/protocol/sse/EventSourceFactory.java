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

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.StatefulException;
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
public final class EventSourceFactory implements EventSource.Factory, AutoCloseable {

    /**
     * Runtime context.
     */
    private final Context context;

    /**
     * Whether this factory owns its Context.
     */
    private final boolean ownsContext;

    /**
     * Active sources using identity semantics.
     */
    private final Set<EventSource> sources;

    /**
     * Factory close guard.
     */
    private final AtomicBoolean closed;

    /**
     * Creates a factory.
     *
     * @param context     context
     * @param ownsContext whether close releases the context
     */
    private EventSourceFactory(final Context context, final boolean ownsContext) {
        this.context = require(context, "Context");
        this.ownsContext = ownsContext;
        this.sources = Collections.newSetFromMap(new IdentityHashMap<>());
        this.closed = new AtomicBoolean();
    }

    /**
     * Creates a factory with a default context.
     *
     * @return factory
     */
    public static EventSourceFactory create() {
        return new EventSourceFactory(Context.create(), true);
    }

    /**
     * Creates a factory.
     *
     * @param context context
     * @return factory
     */
    public static EventSourceFactory create(final Context context) {
        return new EventSourceFactory(context, false);
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
        ensureOpen();
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
        final DefaultEventSource source;
        synchronized (sources) {
            ensureOpen();
            source = new DefaultEventSource(context, require(request, "Request"),
                    listener == null ? new EventSourceListener() {
                    } : listener, this::remove);
            sources.add(source);
        }
        try {
            source.connect();
            return source;
        } catch (final RuntimeException e) {
            remove(source);
            source.cancel();
            throw e;
        }
    }

    /**
     * Closes active sources before releasing an owned Context.
     */
    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        final ArrayList<EventSource> active;
        synchronized (sources) {
            active = new ArrayList<>(sources);
            sources.clear();
        }
        RuntimeException failure = null;
        for (final EventSource source : active) {
            try {
                source.cancel();
            } catch (final RuntimeException e) {
                if (failure == null) {
                    failure = e;
                } else {
                    failure.addSuppressed(e);
                }
            }
        }
        if (ownsContext) {
            try {
                context.close();
            } catch (final RuntimeException e) {
                if (failure == null) {
                    failure = e;
                } else {
                    failure.addSuppressed(e);
                }
            }
        }
        if (failure != null) {
            throw failure;
        }
    }

    /**
     * Removes a terminal source from the active identity set.
     *
     * @param source source
     */
    private void remove(final EventSource source) {
        synchronized (sources) {
            sources.remove(source);
        }
    }

    /**
     * Rejects source creation after close.
     */
    private void ensureOpen() {
        if (closed.get()) {
            throw new StatefulException("EventSourceFactory is closed");
        }
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
            return HttpResponse.builder().request(request).code(status).headers(headers).body(PayloadBody.empty())
                    .build();
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
         * Factory removal hook.
         */
        private final java.util.function.Consumer<EventSource> onClose;

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
         * @param onClose  factory removal hook
         */
        private DefaultEventSource(final Context context, final HttpRequest request, final EventSourceListener listener,
                final java.util.function.Consumer<EventSource> onClose) {
            this.context = require(context, "Context");
            this.request = require(request, "Request");
            this.listener = require(listener, "Listener");
            this.onClose = require(onClose, "Close hook");
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
            if (cancelled.get()) {
                finish();
                return;
            }
            if (request.method() != HTTP.Method.GET) {
                throw new ProtocolException("EventSource request must use GET");
            }
            final SseX exchange = SseX.builder(context).to(request.url().encoded()).headers(request.headers())
                    .timeout(request.timeout()).autoReconnect(false)
                    .onResponse((status, headers) -> response.set(new ResponseMeta(status, headers)))
                    .onEvent(event -> listener.onEvent(this, event.id(), event.event(), event.data()))
                    .listener(new Listener<>() {

                        /**
                         * Notifies the event source listener when the SSE session closes.
                         *
                         * @param source SSE session
                         */
                        @Override
                        public void close(final SseSession source) {
                            if (terminal.compareAndSet(false, true)) {
                                try {
                                    listener.onClosed(DefaultEventSource.this);
                                } finally {
                                    finish();
                                }
                            }
                        }

                        /**
                         * Notifies the event source listener when the SSE session fails.
                         *
                         * @param source SSE session
                         * @param cause  failure cause
                         */
                        @Override
                        public void failure(final SseSession source, final Throwable cause) {
                            if (!cancelled.get() && terminal.compareAndSet(false, true)) {
                                try {
                                    listener.onFailure(DefaultEventSource.this, cause, response());
                                } finally {
                                    finish();
                                }
                            } else {
                                finish();
                            }
                        }
                    }).callback(new Callback<>() {

                        /**
                         * Stores the opened SSE session and emits the open callback.
                         *
                         * @param value opened session
                         */
                        @Override
                        public void success(final SseSession value) {
                            session.set(value);
                            value.onClose(DefaultEventSource.this::finish);
                            if (cancelled.get()) {
                                value.cancel();
                                finish();
                                return;
                            }
                            listener.onOpen(DefaultEventSource.this, response());
                        }

                        /**
                         * Notifies the event source listener when opening fails.
                         *
                         * @param cause failure cause
                         */
                        @Override
                        public void failure(final Throwable cause) {
                            if (!cancelled.get() && terminal.compareAndSet(false, true)) {
                                try {
                                    listener.onFailure(DefaultEventSource.this, cause, response());
                                } finally {
                                    finish();
                                }
                            } else {
                                finish();
                            }
                        }
                    }).build();
            final Call<SseSession> current = exchange.call().enqueue();
            call.set(current);
            if (cancelled.get()) {
                current.cancel();
                finish();
            }
        }

        /**
         * Returns the original event source request.
         *
         * @return request
         */
        @Override
        public HttpRequest request() {
            return request;
        }

        /**
         * Cancels the event source, active session, and pending open call.
         */
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
            finish();
        }

        /**
         * Removes this terminal source from its Factory once.
         */
        private void finish() {
            onClose.accept(this);
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
