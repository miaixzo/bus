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
package org.miaixz.bus.fabric.protocol.stomp;

import static org.miaixz.bus.fabric.Builder.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
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
import org.miaixz.bus.fabric.Filter;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Timeout;
import org.miaixz.bus.fabric.guard.GuardRule;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.protocol.Itinerary;
import org.miaixz.bus.fabric.protocol.stomp.calls.StompCall;

/**
 * Immutable STOMP exchange.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class StompX {

    /**
     * Immutable execution snapshot.
     */
    private final StompSnapshot snapshot;

    /**
     * Execution runner.
     */
    private final StompRunner runner;

    /**
     * Creates an exchange.
     *
     * @param builder builder
     */
    private StompX(final Builder builder) {
        final Context current = require(builder.context, "Context");
        final EventObserver currentObserver = builder.observer == null ? EventObserver.noop() : builder.observer;
        this.snapshot = new StompSnapshot(current, builder.uri, Address.from(builder.uri), builder.headers.build(),
                builder.timeout, builder.destination, builder.login, builder.passcode, builder.guard, builder.filter,
                currentObserver, builder.callback, builder.handler == null ? noopHandler() : builder.handler,
                builder.listener);
        this.runner = new StompRunner(snapshot);
    }

    /**
     * Creates a STOMP builder.
     *
     * @param context shared context
     * @return builder
     */
    public static Builder builder(final Context context) {
        return new Builder(require(context, "Context"));
    }

    /**
     * Returns the STOMP transport protocol.
     *
     * @return protocol
     */
    public Protocol protocol() {
        return snapshot.address().protocol();
    }

    /**
     * Returns the target address.
     *
     * @return address
     */
    public Address address() {
        return snapshot.address();
    }

    /**
     * Returns STOMP execution path.
     *
     * @return itinerary
     */
    public Itinerary itinerary() {
        return Itinerary.of(protocol(), address());
    }

    /**
     * Returns STOMP headers.
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
     * Returns the default destination as the descriptor tag.
     *
     * @return destination
     */
    public Object tag() {
        return snapshot.destination();
    }

    /**
     * Creates a protocol-neutral message from this STOMP exchange and payload.
     *
     * @param payload payload
     * @return message
     */
    public Message message(final Payload payload) {
        return Message.of(protocol(), address(), headers(), payload, tag());
    }

    /**
     * Opens a STOMP session over WebSocket.
     *
     * @return session
     */
    public StompSession open() {
        return runner.open();
    }

    /**
     * Executes this exchange synchronously.
     *
     * @return session
     */
    public StompSession execute() {
        return open();
    }

    /**
     * Connects this exchange synchronously.
     *
     * @return session
     */
    public StompSession connect() {
        return execute();
    }

    /**
     * Creates a single-use call for this exchange.
     *
     * @return STOMP call
     */
    public Call<StompSession> call() {
        return StompCall.create(this, snapshot.context().reactor().dispatcher());
    }

    /**
     * Enqueues this exchange asynchronously.
     *
     * @return call
     */
    public Call<StompSession> enqueue() {
        return call().enqueue();
    }

    /**
     * Builds a stable dispatch key for asynchronous opens.
     *
     * @return dispatch key
     */
    public String dispatchKey() {
        return "stomp" + Symbol.COLON + Symbol.SLASH + Symbol.SLASH + snapshot.address().host() + Symbol.C_COLON
                + snapshot.address().port();
    }

    /**
     * Returns the shared no-op STOMP message handler.
     *
     * @return no-op message handler
     */
    private static Consumer<StompMessage> noopHandler() {
        return Instances.get(StompX.class.getName() + ".noopHandler", () -> message -> {
        });
    }

    /**
     * Validates required references.
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
     * @param value target
     * @return URI
     */
    private static URI parseTarget(final String value) {
        if (StringKit.isBlank(value) || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("STOMP URL must be non-blank and single-line");
        }
        try {
            final URI parsed = new URI(value.trim());
            final String scheme = parsed.getScheme();
            if (!Protocol.WS.name.equalsIgnoreCase(scheme) && !Protocol.WSS.name.equalsIgnoreCase(scheme)
                    && !Protocol.TCP.name.equalsIgnoreCase(scheme)) {
                throw new ProtocolException("STOMP URL must use ws, wss, or tcp");
            }
            Address.from(parsed);
            return parsed;
        } catch (final URISyntaxException e) {
            throw new ProtocolException("Invalid STOMP URL", e);
        }
    }

    /**
     * Validates a duration.
     *
     * @param duration duration
     * @return duration
     */
    private static Duration validateDuration(final Duration duration) {
        return validateDuration(duration, "Timeout");
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
     * STOMP exchange builder.
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
         * Default destination.
         */
        private String destination;

        /**
         * Login.
         */
        private String login;

        /**
         * Passcode.
         */
        private String passcode;

        /**
         * Optional guard.
         */
        private GuardRule guard;

        /**
         * Message filter.
         */
        private Filter filter;

        /**
         * Observer.
         */
        private EventObserver observer;

        /**
         * Callback.
         */
        private Callback<StompSession> callback;

        /**
         * Message handler.
         */
        private Consumer<StompMessage> handler;

        /**
         * Session lifecycle listener.
         */
        private Listener<? super StompSession> listener;

        /**
         * Open handler.
         */
        private Consumer<StompSession> openHandler;

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
            final Timeout configured = context.options().get(OPTION_TIMEOUT, Timeout.class);
            this.timeout = configured == null ? Timeout.defaults() : configured;
            this.observer = EventObserver.noop();
            this.callback = null;
            this.handler = noopHandler();
            this.listener = null;
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
         * Sets default destination.
         *
         * @param destination destination
         * @return this builder
         */
        public Builder destination(final String destination) {
            this.destination = StompMessage.validateToken(destination, "STOMP destination");
            return this;
        }

        /**
         * Sets login.
         *
         * @param login login
         * @return this builder
         */
        public Builder login(final String login) {
            this.login = StompMessage.validateToken(login, "STOMP login");
            return this;
        }

        /**
         * Sets passcode.
         *
         * @param passcode passcode
         * @return this builder
         */
        public Builder passcode(final String passcode) {
            this.passcode = StompMessage.validateToken(passcode, "STOMP passcode");
            return this;
        }

        /**
         * Sets the STOMP heart-beat CONNECT header.
         *
         * @param outgoing outgoing heartbeat interval
         * @param incoming incoming heartbeat interval
         * @return this builder
         */
        public Builder heartBeat(final Duration outgoing, final Duration incoming) {
            headers.set(STOMP_HEADER_HEART_BEAT, heartbeat(outgoing) + "," + heartbeat(incoming));
            return this;
        }

        /**
         * Sets login and passcode.
         *
         * @param login    login
         * @param passcode passcode
         * @return this builder
         */
        public Builder login(final String login, final String passcode) {
            return login(login).passcode(passcode);
        }

        /**
         * Appends a header.
         *
         * @param name  name
         * @param value value
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
         * Sets timeout.
         *
         * @param timeout timeout
         * @return this builder
         */
        public Builder timeout(final Duration timeout) {
            this.timeout = Timeout.of(validateDuration(timeout));
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
         * Sets message handler.
         *
         * @param handler handler
         * @return this builder
         */
        public Builder onMessage(final Consumer<StompMessage> handler) {
            this.handler = handler == null ? noopHandler() : handler;
            return this;
        }

        /**
         * Sets open handler.
         *
         * @param handler open handler
         * @return this builder
         */
        public Builder onOpen(final Consumer<StompSession> handler) {
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
         * Sets message filter.
         *
         * @param filter filter
         * @return this builder
         */
        public Builder filter(final Filter filter) {
            this.filter = filter;
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
        public Builder callback(final Callback<StompSession> callback) {
            this.callback = callback;
            return this;
        }

        /**
         * Sets lifecycle listener.
         *
         * @param listener lifecycle listener
         * @return this builder
         */
        public Builder listener(final Listener<? super StompSession> listener) {
            this.listener = listener;
            return this;
        }

        /**
         * Builds an exchange snapshot.
         *
         * @return exchange
         */
        public StompX build() {
            Assert.notNull(uri, () -> new ValidateException("STOMP target must be set"));
            return new StompX(this);
        }

        /**
         * Opens a built exchange.
         *
         * @return session
         */
        public StompSession open() {
            return build().open();
        }

        /**
         * Connects a built exchange.
         *
         * @return session
         */
        public StompSession connect() {
            return open();
        }

        /**
         * Executes a built exchange.
         *
         * @return session
         */
        public StompSession execute() {
            return build().execute();
        }

        /**
         * Creates a call for a built exchange.
         *
         * @return STOMP call
         */
        public Call<StompSession> call() {
            return build().call();
        }

        /**
         * Enqueues a built exchange asynchronously.
         *
         * @return call
         */
        public Call<StompSession> enqueue() {
            return build().enqueue();
        }

        /**
         * Composes open and error handlers into a callback.
         *
         * @return this builder
         */
        private Builder composeCallback() {
            this.callback = new Callback<>() {

                /**
                 * Forwards a successful open session to the configured open handler.
                 *
                 * @param value opened STOMP session
                 */
                @Override
                public void success(final StompSession value) {
                    openHandler.accept(value);
                }

                /**
                 * Forwards an open failure to the configured error handler.
                 *
                 * @param cause failure cause
                 */
                @Override
                public void failure(final Throwable cause) {
                    errorHandler.accept(cause);
                }
            };
            return this;
        }

        /**
         * Converts a heartbeat duration to milliseconds.
         *
         * @param duration duration
         * @return milliseconds
         */
        private static long heartbeat(final Duration duration) {
            final Duration checked = validateDuration(duration, "STOMP heart-beat");
            try {
                return checked.toMillis();
            } catch (final ArithmeticException e) {
                throw new ValidateException("STOMP heart-beat is too large", e);
            }
        }

    }

}
