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

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Call;
import org.miaixz.bus.fabric.Callback;
import org.miaixz.bus.fabric.Context;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Timeout;
import org.miaixz.bus.fabric.Wiring;
import org.miaixz.bus.fabric.guard.GuardRule;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.protocol.Itinerary;
import org.miaixz.bus.fabric.protocol.sse.calls.SseCall;
import org.miaixz.bus.fabric.protocol.sse.event.SseRetry;

/**
 * Immutable SSE exchange.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class SseX {

    /**
     * Runtime option key for default timeout.
     */
    private static final String TIMEOUT_OPTION = "timeout";

    /**
     * Immutable execution snapshot.
     */
    private final SseSnapshot snapshot;

    /**
     * Execution runner.
     */
    private final SseRunner runner;

    /**
     * Creates an exchange.
     *
     * @param builder builder
     */
    private SseX(final Builder builder) {
        final Context current = require(builder.context, "Context");
        final EventObserver currentObserver = builder.observer == null ? EventObserver.noop() : builder.observer;
        final Listener<? super SseSession> currentListener = Wiring
                .safe(Wiring.compose(current.listener(), builder.listener), currentObserver);
        this.snapshot = new SseSnapshot(current, builder.uri, Address.from(builder.uri), builder.headers.build(),
                builder.timeout, builder.retry, builder.lastEventId, builder.autoReconnect, builder.responseHandler,
                builder.guard, currentObserver, builder.callback == null ? Wiring.callback() : builder.callback,
                builder.handler == null ? noopHandler() : builder.handler, currentListener);
        this.runner = new SseRunner(snapshot);
    }

    /**
     * Creates an SSE builder.
     *
     * @param context shared context
     * @return builder
     */
    public static Builder builder(final Context context) {
        return new Builder(require(context, "Context"));
    }

    /**
     * Returns the stream protocol.
     *
     * @return protocol
     */
    public Protocol protocol() {
        return snapshot.address().protocol();
    }

    /**
     * Returns the stream address.
     *
     * @return address
     */
    public Address address() {
        return snapshot.address();
    }

    /**
     * Returns SSE execution path.
     *
     * @return itinerary
     */
    public Itinerary itinerary() {
        return Itinerary.of(protocol(), address());
    }

    /**
     * Returns request headers.
     *
     * @return headers
     */
    public Headers headers() {
        return snapshot.headers();
    }

    /**
     * Returns timeout policy.
     *
     * @return timeout
     */
    public Timeout timeout() {
        return snapshot.timeout();
    }

    /**
     * Creates a protocol-neutral message from this SSE exchange and payload.
     *
     * @param payload payload
     * @return message
     */
    public Message message(final Payload payload) {
        return Message.of(protocol(), address(), headers(), payload, null);
    }

    /**
     * Opens the SSE stream synchronously and starts background event delivery.
     *
     * @return opened session
     */
    public SseSession open() {
        return runner.open();
    }

    /**
     * Executes this exchange synchronously.
     *
     * @return opened session
     */
    public SseSession execute() {
        return open();
    }

    /**
     * Connects this exchange synchronously.
     *
     * @return opened session
     */
    public SseSession connect() {
        return execute();
    }

    /**
     * Creates a single-use call for this exchange.
     *
     * @return SSE call
     */
    public Call<SseSession> call() {
        return SseCall.create(this, snapshot.context().reactor().dispatcher());
    }

    /**
     * Returns the shared no-op event handler.
     *
     * @return no-op event handler
     */
    private static Consumer<SseEvent> noopHandler() {
        return Instances.get(SseX.class.getName() + ".noopHandler", () -> event -> {
        });
    }

    /**
     * Builds a stable reader dispatch key.
     *
     * @return dispatch key
     */
    public String dispatchKey() {
        return runner.dispatchKey();
    }

    /**
     * Enqueues the SSE stream asynchronously.
     *
     * @return call
     */
    public Call<SseSession> enqueue() {
        return call().enqueue();
    }

    /**
     * Validates required values.
     *
     * @param value value
     * @param name  field name
     * @param <T>   value type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

    /**
     * Parses a target URI.
     *
     * @param value target value
     * @return URI
     */
    private static URI parseTarget(final String value) {
        if (StringKit.isBlank(value) || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("SSE URL must be non-blank and single-line");
        }
        try {
            final URI parsed = new URI(value.trim());
            final String scheme = parsed.getScheme();
            if (!Protocol.HTTP.name.equalsIgnoreCase(scheme) && !Protocol.HTTPS.name.equalsIgnoreCase(scheme)) {
                throw new ProtocolException("SSE URL must use http or https");
            }
            Address.from(parsed);
            return parsed;
        } catch (final URISyntaxException e) {
            throw new ProtocolException("Invalid SSE URL", e);
        }
    }

    /**
     * Validates a duration.
     *
     * @param duration duration
     * @param name     field name
     * @return duration
     */
    private static Duration validateDuration(final Duration duration, final String name) {
        final Duration checked = Assert
                .notNull(duration, () -> new ValidateException(name + " must be non-null and non-negative"));
        Assert.isFalse(checked.isNegative(), () -> new ValidateException(name + " must be non-null and non-negative"));
        return checked;
    }

    /**
     * Validates a Last-Event-ID value.
     *
     * @param value value
     * @return value
     */
    private static String validateLastEventId(final String value) {
        if (StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("Last-Event-ID must be single-line");
        }
        return value;
    }

    /**
     * SSE exchange builder.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static final class Builder {

        /**
         * Shared context.
         */
        private final Context context;

        /**
         * Target URI.
         */
        private URI uri;

        /**
         * Headers builder.
         */
        private Headers.Builder headers;

        /**
         * Timeout policy.
         */
        private Timeout timeout;

        /**
         * Retry policy.
         */
        private SseRetry retry;

        /**
         * Whether EOF should reconnect.
         */
        private boolean autoReconnect;

        /**
         * HTTP response metadata handler.
         */
        private BiConsumer<Integer, Headers> responseHandler;

        /**
         * Last event id.
         */
        private String lastEventId;

        /**
         * Optional guard.
         */
        private GuardRule guard;

        /**
         * Observer.
         */
        private EventObserver observer;

        /**
         * Callback.
         */
        private Callback<SseSession> callback;

        /**
         * Session lifecycle listener.
         */
        private Listener<? super SseSession> listener;

        /**
         * Open handler.
         */
        private Consumer<SseSession> openHandler;

        /**
         * Error handler.
         */
        private Consumer<Throwable> errorHandler;

        /**
         * Event handler.
         */
        private Consumer<SseEvent> handler;

        /**
         * Creates a builder.
         *
         * @param context shared context
         */
        private Builder(final Context context) {
            this.context = context;
            this.headers = Headers.builder();
            final Timeout configured = context.options().get(TIMEOUT_OPTION, Timeout.class);
            this.timeout = configured == null ? Timeout.defaults() : configured;
            this.retry = SseRetry.defaults();
            this.autoReconnect = true;
            this.responseHandler = (status, headers) -> {
            };
            this.observer = EventObserver.noop();
            this.callback = Wiring.callback();
            this.handler = noopHandler();
            this.listener = Wiring.noop();
            this.openHandler = session -> {
            };
            this.errorHandler = cause -> {
            };
        }

        /**
         * Sets target URL.
         *
         * @param url URL
         * @return this builder
         */
        public Builder to(final String url) {
            this.uri = parseTarget(url);
            return this;
        }

        /**
         * Sets target URL.
         *
         * @param url URL
         * @return this builder
         */
        public Builder url(final String url) {
            return to(url);
        }

        /**
         * Appends a header.
         *
         * @param name  header name
         * @param value header value
         * @return this builder
         */
        public Builder header(final String name, final String value) {
            headers.add(name, value);
            return this;
        }

        /**
         * Merges headers.
         *
         * @param headers headers
         * @return this builder
         */
        public Builder headers(final Headers headers) {
            require(headers, "Headers");
            for (final Map.Entry<String, List<String>> entry : headers.asMap().entrySet()) {
                for (final String value : entry.getValue()) {
                    this.headers.add(entry.getKey(), value);
                }
            }
            return this;
        }

        /**
         * Merges single-value headers.
         *
         * @param headers headers
         * @return this builder
         */
        public Builder headers(final Map<String, String> headers) {
            require(headers, "Headers").forEach(this::header);
            return this;
        }

        /**
         * Sets timeout.
         *
         * @param timeout timeout
         * @return this builder
         */
        public Builder timeout(final Duration timeout) {
            this.timeout = Timeout.of(validateDuration(timeout, "Timeout"));
            return this;
        }

        /**
         * Sets timeout policy.
         *
         * @param timeout timeout policy
         * @return this builder
         */
        public Builder timeout(final Timeout timeout) {
            this.timeout = require(timeout, "Timeout");
            return this;
        }

        /**
         * Sets retry delay.
         *
         * @param retry retry delay
         * @return this builder
         */
        public Builder retry(final Duration retry) {
            this.retry.update(validateDuration(retry, "SSE retry"));
            return this;
        }

        /**
         * Sets the initial Last-Event-ID value used by reconnect requests.
         *
         * @param eventId event id
         * @return this builder
         */
        public Builder lastEventId(final String eventId) {
            this.lastEventId = validateLastEventId(eventId);
            return this;
        }

        /**
         * Sets the initial Last-Event-ID value used by reconnect requests.
         *
         * @param eventId event id
         * @return this builder
         */
        public Builder lastId(final String eventId) {
            return lastEventId(eventId);
        }

        /**
         * Sets whether EOF and recoverable stream failures should reconnect.
         *
         * @param autoReconnect true to reconnect
         * @return this builder
         */
        public Builder autoReconnect(final boolean autoReconnect) {
            this.autoReconnect = autoReconnect;
            return this;
        }

        /**
         * Sets whether EOF and recoverable stream failures should reconnect.
         *
         * @param autoReconnect true to reconnect
         * @return this builder
         */
        public Builder reconnect(final boolean autoReconnect) {
            return autoReconnect(autoReconnect);
        }

        /**
         * Handles HTTP response metadata before the SSE body is consumed.
         *
         * @param handler response handler
         * @return this builder
         */
        public Builder onResponse(final BiConsumer<Integer, Headers> handler) {
            this.responseHandler = handler == null ? (status, headers) -> {
            } : handler;
            return this;
        }

        /**
         * Sets event handler.
         *
         * @param handler event handler
         * @return this builder
         */
        public Builder onEvent(final Consumer<SseEvent> handler) {
            this.handler = handler == null ? noopHandler() : handler;
            return this;
        }

        /**
         * Sets open handler.
         *
         * @param handler open handler
         * @return this builder
         */
        public Builder onOpen(final Consumer<SseSession> handler) {
            this.openHandler = handler == null ? session -> {
            } : handler;
            return composeCallback();
        }

        /**
         * Sets error handler.
         *
         * @param handler error handler
         * @return this builder
         */
        public Builder onError(final Consumer<Throwable> handler) {
            this.errorHandler = handler == null ? cause -> {
            } : handler;
            return composeCallback();
        }

        /**
         * Sets guard.
         *
         * @param guard guard
         * @return this builder
         */
        public Builder guard(final GuardRule guard) {
            this.guard = guard;
            return this;
        }

        /**
         * Sets observer.
         *
         * @param observer observer
         * @return this builder
         */
        public Builder observe(final EventObserver observer) {
            this.observer = observer == null ? EventObserver.noop() : observer;
            return this;
        }

        /**
         * Sets callback.
         *
         * @param callback callback
         * @return this builder
         */
        public Builder callback(final Callback<SseSession> callback) {
            this.callback = callback == null ? Wiring.callback() : callback;
            return this;
        }

        /**
         * Sets lifecycle listener.
         *
         * @param listener lifecycle listener
         * @return this builder
         */
        public Builder listener(final Listener<? super SseSession> listener) {
            this.listener = listener == null ? Wiring.noop() : listener;
            return this;
        }

        /**
         * Builds an exchange snapshot.
         *
         * @return exchange
         */
        public SseX build() {
            Assert.notNull(uri, () -> new ValidateException("SSE target must be set"));
            return new SseX(this);
        }

        /**
         * Opens a built exchange.
         *
         * @return session
         */
        public SseSession open() {
            return build().open();
        }

        /**
         * Connects a built exchange.
         *
         * @return session
         */
        public SseSession connect() {
            return open();
        }

        /**
         * Executes a built exchange.
         *
         * @return session
         */
        public SseSession execute() {
            return build().execute();
        }

        /**
         * Creates a call for a built exchange.
         *
         * @return SSE call
         */
        public Call<SseSession> call() {
            return build().call();
        }

        /**
         * Enqueues a built exchange asynchronously.
         *
         * @return call
         */
        public Call<SseSession> enqueue() {
            return build().enqueue();
        }

        /**
         * Composes open and error handlers into a callback.
         *
         * @return this builder
         */
        private Builder composeCallback() {
            this.callback = new Callback<>() {

                @Override
                public void success(final SseSession value) {
                    openHandler.accept(value);
                }

                @Override
                public void failure(final Throwable cause) {
                    errorHandler.accept(cause);
                }
            };
            return this;
        }

    }

}
