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
package org.miaixz.bus.fabric.protocol.websocket;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Call;
import org.miaixz.bus.fabric.Callback;
import org.miaixz.bus.fabric.Context;
import org.miaixz.bus.fabric.Handler;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Session;
import org.miaixz.bus.fabric.Timeout;
import org.miaixz.bus.fabric.Wiring;
import org.miaixz.bus.fabric.guard.GuardRule;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.protocol.Itinerary;
import org.miaixz.bus.fabric.protocol.websocket.calls.WebSocketCall;

/**
 * Immutable WebSocket exchange.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class WebSocketX {

    /**
     * Immutable execution snapshot.
     */
    private final WebSocketSnapshot snapshot;

    /**
     * Execution runner.
     */
    private final WebSocketRunner runner;

    /**
     * Creates an exchange.
     *
     * @param builder builder
     */
    private WebSocketX(final Builder builder) {
        final Context current = require(builder.context, "Context");
        final EventObserver currentObserver = builder.observer == null ? EventObserver.noop() : builder.observer;
        final Listener<? super WebSocketSession> currentListener = Wiring
                .safe(Wiring.compose(current.listener(), builder.listener), currentObserver);
        this.snapshot = new WebSocketSnapshot(current, builder.uri, Address.from(builder.uri), builder.headers.build(),
                builder.timeout, builder.guard, currentObserver,
                builder.callback == null ? Wiring.callback() : builder.callback,
                builder.handler == null ? noopHandler() : builder.handler, currentListener);
        this.runner = new WebSocketRunner(snapshot);
    }

    /**
     * Creates a WebSocket builder.
     *
     * @param context shared context
     * @return builder
     */
    public static Builder builder(final Context context) {
        return new Builder(require(context, "Context"));
    }

    /**
     * Opens the WebSocket synchronously.
     *
     * @return opened session
     */
    public WebSocketSession open() {
        return runner.open();
    }

    /**
     * Executes this exchange synchronously.
     *
     * @return opened session
     */
    public WebSocketSession execute() {
        return open();
    }

    /**
     * Connects this exchange synchronously.
     *
     * @return opened session
     */
    public WebSocketSession connect() {
        return execute();
    }

    /**
     * Creates a single-use call for this exchange.
     *
     * @return WebSocket call
     */
    public Call<WebSocketSession> call() {
        return WebSocketCall.create(this, snapshot.context().reactor().dispatcher());
    }

    /**
     * Enqueues the WebSocket asynchronously.
     *
     * @return call
     */
    public Call<WebSocketSession> enqueue() {
        return call().enqueue();
    }

    /**
     * Builds a stable dispatch key for asynchronous opens.
     *
     * @return dispatch key
     */
    public String dispatchKey() {
        return runner.dispatchKey();
    }

    /**
     * Returns the WebSocket protocol.
     *
     * @return protocol
     */
    public Protocol protocol() {
        return snapshot.address().protocol();
    }

    /**
     * Returns address.
     *
     * @return address
     */
    public Address address() {
        return snapshot.address();
    }

    /**
     * Returns WebSocket execution path.
     *
     * @return itinerary
     */
    public Itinerary itinerary() {
        return Itinerary.of(protocol(), address());
    }

    /**
     * Returns headers.
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
     * Creates a protocol-neutral message from this WebSocket exchange and payload.
     *
     * @param payload payload
     * @return message
     */
    public Message message(final Payload payload) {
        return Message.of(protocol(), address(), headers(), payload, null);
    }

    /**
     * Returns the shared no-op WebSocket handler.
     *
     * @return no-op handler
     */
    private static Handler noopHandler() {
        return Instances.get(WebSocketX.class.getName() + ".noopHandler", () -> new Handler() {

            @Override
            public void message(final Session session, final Message message) {
                // No-op handler intentionally ignores incoming messages.
            }
        });
    }

    /**
     * Validates a required value.
     *
     * @param value value
     * @param name  name
     * @param <T>   type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value;
    }

    /**
     * Parses a target URI.
     *
     * @param value target value
     * @return URI
     */
    private static URI parseTarget(final String value) {
        if (StringKit.isBlank(value) || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("WebSocket URL must be non-blank and single-line");
        }
        try {
            final URI uri = new URI(value.trim());
            final String scheme = uri.getScheme();
            if (!"ws".equalsIgnoreCase(scheme) && !"wss".equalsIgnoreCase(scheme)) {
                throw new ProtocolException("WebSocket URL must use ws or wss");
            }
            Address.from(uri);
            return uri;
        } catch (final URISyntaxException e) {
            throw new ProtocolException("Invalid WebSocket URL", e);
        }
    }

    /**
     * Validates a duration.
     *
     * @param duration duration
     * @param name     name
     * @return duration
     */
    private static Duration validateDuration(final Duration duration, final String name) {
        if (duration == null || duration.isNegative()) {
            throw new ValidateException(name + " must be non-null and non-negative");
        }
        return duration;
    }

    /**
     * WebSocket exchange builder.
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
         * Time policy.
         */
        private Timeout timeout;

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
        private Callback<WebSocketSession> callback;

        /**
         * Handler.
         */
        private Handler handler;

        /**
         * Session lifecycle listener.
         */
        private Listener<? super WebSocketSession> listener;

        /**
         * Open handler.
         */
        private Consumer<WebSocketSession> openHandler;

        /**
         * Error handler.
         */
        private Consumer<Throwable> errorHandler;

        /**
         * Creates a builder.
         *
         * @param context shared context
         */
        private Builder(final Context context) {
            this.context = context;
            this.headers = Headers.builder();
            final Timeout configured = context.options().get("timeout", Timeout.class);
            this.timeout = configured == null ? Timeout.defaults() : configured;
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
         * Sets the Sec-WebSocket-Protocol header.
         *
         * @param protocol protocol value
         * @return this builder
         */
        public Builder protocol(final String protocol) {
            headers.set("Sec-WebSocket-Protocol", protocol);
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
         * Sets timeout.
         *
         * @param timeout timeout
         * @return this builder
         */
        public Builder timeout(final Timeout timeout) {
            this.timeout = require(timeout, "Timeout");
            return this;
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
        public Builder callback(final Callback<WebSocketSession> callback) {
            this.callback = callback == null ? Wiring.callback() : callback;
            return this;
        }

        /**
         * Sets message handler.
         *
         * @param handler handler
         * @return this builder
         */
        public Builder onMessage(final Handler handler) {
            this.handler = handler == null ? noopHandler() : handler;
            return this;
        }

        /**
         * Sets a UTF-8 text message handler.
         *
         * @param handler text handler
         * @return this builder
         */
        public Builder onText(final Consumer<String> handler) {
            if (handler == null) {
                this.handler = noopHandler();
            } else {
                this.handler = (session, message) -> handler.accept(message.payload().text(StandardCharsets.UTF_8));
            }
            return this;
        }

        /**
         * Sets open handler.
         *
         * @param handler open handler
         * @return this builder
         */
        public Builder onOpen(final Consumer<WebSocketSession> handler) {
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
         * Sets lifecycle listener.
         *
         * @param listener lifecycle listener
         * @return this builder
         */
        public Builder listener(final Listener<? super WebSocketSession> listener) {
            this.listener = listener == null ? Wiring.noop() : listener;
            return this;
        }

        /**
         * Builds an exchange snapshot.
         *
         * @return exchange
         */
        public WebSocketX build() {
            if (uri == null) {
                throw new ValidateException("WebSocket target must be set");
            }
            return new WebSocketX(this);
        }

        /**
         * Opens a built exchange.
         *
         * @return session
         */
        public WebSocketSession open() {
            return build().open();
        }

        /**
         * Connects a built exchange.
         *
         * @return session
         */
        public WebSocketSession connect() {
            return open();
        }

        /**
         * Executes a built exchange.
         *
         * @return session
         */
        public WebSocketSession execute() {
            return build().execute();
        }

        /**
         * Creates a call for a built exchange.
         *
         * @return WebSocket call
         */
        public Call<WebSocketSession> call() {
            return build().call();
        }

        /**
         * Enqueues a built exchange asynchronously.
         *
         * @return call
         */
        public Call<WebSocketSession> enqueue() {
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
                public void success(final WebSocketSession value) {
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
