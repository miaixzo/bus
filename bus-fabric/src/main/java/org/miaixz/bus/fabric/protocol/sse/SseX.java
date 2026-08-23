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

import static org.miaixz.bus.fabric.Builder.OPTION_TIMEOUT;

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
import org.miaixz.bus.fabric.*;
import org.miaixz.bus.fabric.guard.GuardRule;
import org.miaixz.bus.fabric.network.proxy.ProxyPlan;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.protocol.Itinerary;
import org.miaixz.bus.fabric.protocol.Mediator;
import org.miaixz.bus.fabric.protocol.Mediator.Type;
import org.miaixz.bus.fabric.protocol.sse.calls.SseCall;
import org.miaixz.bus.fabric.protocol.sse.event.SseEvent;
import org.miaixz.bus.fabric.protocol.sse.retry.SseRetryPolicy;

/**
 * Immutable SSE exchange.
 *
 * @author Kimi Liu
 */
public class SseX {

    /**
     * Immutable execution specification.
     */
    private final SseSpec spec;

    /**
     * Execution runner.
     */
    private final SseRunner runner;

    /**
     * Callback managed by the shared call lifecycle.
     */
    private final Callback<SseSession> callback;

    /**
     * Creates an exchange.
     *
     * @param builder configuration source used to create the immutable exchange specification
     */
    public SseX(final Builder builder) {
        final Context current = require(builder.context, "Context");
        final EventObserver currentObserver = builder.observer == null ? EventObserver.noop() : builder.observer;
        this.spec = new SseSpec(current, builder.uri, Address.from(builder.uri), builder.headers.build(),
                builder.timeout, builder.retryPolicy, builder.proxy, builder.lastEventId, builder.autoReconnect,
                builder.responseHandler, builder.guard, builder.filter, currentObserver,
                builder.handler == null ? noopHandler() : builder.handler, builder.listener);
        this.runner = new SseRunner(spec);
        this.callback = builder.callback;
    }

    /**
     * Creates an SSE builder.
     *
     * @param context shared context
     * @return new SSE exchange builder bound to the context
     */
    public static Builder builder(final Context context) {
        return new Builder(require(context, "Context"));
    }

    /**
     * Returns the stream protocol.
     *
     * @return HTTP or HTTPS protocol derived from the stream address
     */
    public Protocol protocol() {
        return spec.address().protocol();
    }

    /**
     * Returns the stream address.
     *
     * @return immutable SSE endpoint address
     */
    public Address address() {
        return spec.address();
    }

    /**
     * Returns SSE execution path.
     *
     * @return execution itinerary containing the HTTP protocol and stream address
     */
    public Itinerary itinerary() {
        return Itinerary.of(protocol(), address());
    }

    /**
     * Returns request headers.
     *
     * @return immutable SSE request headers
     */
    public Headers headers() {
        return spec.headers();
    }

    /**
     * Returns timeout policy.
     *
     * @return timeout policy captured by this exchange
     */
    public Timeout timeout() {
        return spec.timeout();
    }

    /**
     * Creates a protocol-neutral message from this SSE exchange and payload.
     *
     * @param payload payload to attach to the protocol-neutral message
     * @return message representing this exchange and the supplied payload
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
        return call().execute();
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
        return SseCall.create(
                spec.context().reactor().dispatcher(),
                callback,
                spec.observer(),
                spec.timeout(),
                cancellation -> Mediator.execute(Type.SSE, cancellation, runner::open),
                dispatchKey());
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
     * @return enqueued call for this SSE exchange
     */
    public Call<SseSession> enqueue() {
        return call().enqueue();
    }

    /**
     * Validates required values.
     *
     * @param value reference to validate
     * @param name  field name included in the validation failure
     * @param <T>   reference type
     * @return validated non-null reference
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

    /**
     * Parses a target URI.
     *
     * @param value raw SSE endpoint URL
     * @return validated HTTP or HTTPS target URI
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
     * @param duration candidate timeout or retry duration
     * @param name     field name
     * @return validated non-negative duration
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
     * @param value candidate Last-Event-ID header value
     * @return validated single-line Last-Event-ID value
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
     */
    public static class Builder {

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
        private SseRetryPolicy retryPolicy;

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
         * Message filter.
         */
        private Filter filter;

        /**
         * Observer.
         */
        private EventObserver observer;

        /**
         * Outbound proxy policy inherited by every reconnect.
         */
        private ProxyPlan proxy;

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
        public Builder(final Context context) {
            this.context = context;
            this.headers = Headers.builder();
            final Timeout configured = context.options().get(OPTION_TIMEOUT);
            this.timeout = configured == null ? Timeout.defaults() : configured;
            this.retryPolicy = SseRetryPolicy.resolve(context.options());
            this.autoReconnect = true;
            this.responseHandler = (status, headers) -> {
            };
            this.observer = EventObserver.noop();
            this.proxy = ProxyPlan.inherit();
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
         * @param url raw SSE endpoint URL
         * @return this builder
         */
        public Builder to(final String url) {
            this.uri = parseTarget(url);
            return this;
        }

        /**
         * Sets target URL.
         *
         * @param url raw SSE endpoint URL forwarded to {@link #to(String)}
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
         * @param headers SSE request headers whose values are appended
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
         * @param headers single-value SSE request headers to append
         * @return this builder
         */
        public Builder headers(final Map<String, String> headers) {
            require(headers, "Headers").forEach(this::header);
            return this;
        }

        /**
         * Sets timeout.
         *
         * @param timeout non-negative duration assigned to every timeout phase
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
            final Duration current = validateDuration(retry, "SSE retry");
            this.retryPolicy = new SseRetryPolicy(current, retryPolicy.maxDelay());
            return this;
        }

        /**
         * Sets the complete SSE reconnect policy.
         *
         * @param retryPolicy complete immutable reconnect policy
         * @return this builder
         */
        public Builder retry(final SseRetryPolicy retryPolicy) {
            this.retryPolicy = require(retryPolicy, "SSE retry policy");
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
         * @param handler status-and-header consumer, or {@code null} for no action
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
         * @param handler SSE event consumer, or {@code null} for no action
         * @return this builder
         */
        public Builder onEvent(final Consumer<SseEvent> handler) {
            this.handler = handler == null ? noopHandler() : handler;
            return this;
        }

        /**
         * Sets open handler.
         *
         * @param handler consumer invoked after a session opens, or {@code null} for no action
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
         * @param handler consumer invoked when opening fails, or {@code null} for no action
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
         * @param guard rule applied to SSE messages
         * @return this builder
         */
        public Builder guard(final GuardRule guard) {
            this.guard = guard;
            return this;
        }

        /**
         * Sets message filter.
         *
         * @param filter applied to SSE messages
         * @return this builder
         */
        public Builder filter(final Filter filter) {
            this.filter = filter;
            return this;
        }

        /**
         * Sets the outbound proxy policy used by the stream and every reconnect.
         *
         * @param proxy non-null policy propagated to every HTTP connection attempt
         * @return this builder
         */
        public Builder proxy(final ProxyPlan proxy) {
            this.proxy = require(proxy, "Proxy plan");
            return this;
        }

        /**
         * Inherits the context-level network proxy policy for every connection attempt.
         *
         * @return this builder
         */
        public Builder inheritProxy() {
            return proxy(ProxyPlan.inherit());
        }

        /**
         * Resolves every connection attempt through the current system proxy selector.
         *
         * @return this builder
         */
        public Builder systemProxy() {
            return proxy(ProxyPlan.system());
        }

        /**
         * Bypasses configured and system proxies for every connection attempt.
         *
         * @return this builder
         */
        public Builder directProxy() {
            return proxy(ProxyPlan.direct());
        }

        /**
         * Uses a fixed HTTP proxy for the event stream and every reconnect.
         *
         * @param proxy non-null plain HTTP proxy address
         * @return this builder
         */
        public Builder httpProxy(final Address proxy) {
            return proxy(ProxyPlan.http(require(proxy, "HTTP proxy")));
        }

        /**
         * Uses a fixed SOCKS5 proxy for the event stream and every reconnect.
         *
         * @param proxy non-null plain stream address of the SOCKS5 server
         * @return this builder
         */
        public Builder socksProxy(final Address proxy) {
            return proxy(ProxyPlan.socks(require(proxy, "SOCKS proxy")));
        }

        /**
         * Sets the event observer used by this stream.
         *
         * @param observer observer implementation, or {@code null} to disable observation
         * @return this builder
         */
        public Builder observe(final EventObserver observer) {
            this.observer = observer == null ? EventObserver.noop() : observer;
            return this;
        }

        /**
         * Sets callback.
         *
         * @param callback call-lifecycle callback for asynchronous opening
         * @return this builder
         */
        public Builder callback(final Callback<SseSession> callback) {
            this.callback = callback;
            return this;
        }

        /**
         * Sets lifecycle listener.
         *
         * @param listener lifecycle listener
         * @return this builder
         */
        public Builder listener(final Listener<? super SseSession> listener) {
            this.listener = listener;
            return this;
        }

        /**
         * Builds an exchange specification.
         *
         * @return immutable SSE exchange built from the current configuration
         */
        public SseX build() {
            Assert.notNull(uri, () -> new ValidateException("SSE target must be set"));
            return new SseX(this);
        }

        /**
         * Opens a built exchange.
         *
         * @return opened SSE session with background event delivery
         */
        public SseSession open() {
            return build().open();
        }

        /**
         * Connects a built exchange.
         *
         * @return connected SSE session
         */
        public SseSession connect() {
            return open();
        }

        /**
         * Executes a built exchange.
         *
         * @return SSE session produced by synchronous execution
         */
        public SseSession execute() {
            return build().execute();
        }

        /**
         * Creates a call for a built exchange.
         *
         * @return new single-use call for the built exchange
         */
        public Call<SseSession> call() {
            return build().call();
        }

        /**
         * Enqueues a built exchange asynchronously.
         *
         * @return enqueued call for the built exchange
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

                /**
                 * Forwards a successful open session to the configured open handler.
                 *
                 * @param value opened SSE session
                 */
                @Override
                public void success(final SseSession value) {
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

    }

}
