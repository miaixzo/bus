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
package org.miaixz.bus.fabric.protocol;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.function.Function;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Handler;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Session;

/**
 * Demultiplexes protocol messages to channel-specific handlers.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Demuxer implements Handler {

    /**
     * Immutable handlers indexed by channel identifier.
     */
    private final Map<String, Handler> handlers;

    /**
     * Handler used when no channel-specific registration matches, or {@code null}.
     */
    private final Handler fallback;

    /**
     * Message header name used as the secondary channel source.
     */
    private final String channelHeader;

    /**
     * Primary channel resolver, or {@code null} when header and tag lookup are used directly.
     */
    private final Function<Message, String> resolver;

    /**
     * Creates a demuxer.
     *
     * @param handlers      handlers indexed by their validated channel identifiers
     * @param fallback      handler used when no channel-specific handler matches, or {@code null}
     * @param channelHeader header name inspected when the resolver does not provide a channel
     * @param resolver      optional function that resolves a channel directly from a message
     */
    private Demuxer(final Map<String, Handler> handlers, final Handler fallback, final String channelHeader,
            final Function<Message, String> resolver) {
        this.handlers = Collections.unmodifiableMap(new LinkedHashMap<>(require(handlers, "Handlers")));
        this.fallback = fallback;
        this.channelHeader = validateToken(channelHeader, "Channel header");
        this.resolver = resolver;
    }

    /**
     * Creates a demuxer builder.
     *
     * @return a new demuxer builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Routes a received message to the matching channel handler.
     *
     * @param session session on which the message was received
     * @param message protocol message to route
     * @throws ProtocolException if the message has no channel or no handler accepts its channel
     */
    @Override
    public void message(final Session session, final Message message) {
        final String channel = channel(message);
        final Handler target = handlers.get(channel);
        if (target != null) {
            target.message(session, message);
            return;
        }
        if (fallback != null) {
            fallback.message(session, message);
            return;
        }
        throw new ProtocolException("No message handler for channel: " + channel);
    }

    /**
     * Forwards a session failure to all configured targets.
     *
     * @param session failed session
     * @param cause   failure reported by the session
     */
    @Override
    public void failure(final Session session, final Throwable cause) {
        for (final Handler handler : targets()) {
            handler.failure(session, cause);
        }
    }

    /**
     * Forwards a session close event to all configured targets.
     *
     * @param session session that was closed
     */
    @Override
    public void closed(final Session session) {
        for (final Handler handler : targets()) {
            handler.closed(session);
        }
    }

    /**
     * Returns immutable channel handlers.
     *
     * @return immutable map of channel identifiers to handlers
     */
    public Map<String, Handler> handlers() {
        return handlers;
    }

    /**
     * Returns the configured channel header.
     *
     * @return header name used for channel lookup
     */
    public String channelHeader() {
        return channelHeader;
    }

    /**
     * Resolves the channel for a message.
     *
     * @param message message whose channel is required
     * @return validated channel resolved from the custom resolver, configured header, or message tag
     * @throws ProtocolException if none of the supported message sources contains a channel
     * @throws ValidateException if the resolved channel is blank or contains a line break
     */
    public String channel(final Message message) {
        final Message current = require(message, "Message");
        final String resolved = resolver == null ? null : resolver.apply(current);
        if (StringKit.isNotBlank(resolved)) {
            return validateToken(resolved, "Message channel");
        }
        final String header = current.headers().get(channelHeader);
        if (StringKit.isNotBlank(header)) {
            return validateToken(header, "Message channel");
        }
        final Object tag = current.tag();
        if (tag != null && StringKit.isNotBlank(tag.toString())) {
            return validateToken(tag.toString(), "Message channel");
        }
        throw new ProtocolException("Message channel is missing");
    }

    /**
     * Returns all unique notification targets.
     *
     * @return unique handlers in notification order, including the fallback when configured
     */
    private Iterable<Handler> targets() {
        final LinkedHashSet<Handler> result = new LinkedHashSet<>(handlers.values());
        if (fallback != null) {
            result.add(fallback);
        }
        return result;
    }

    /**
     * Validates required references.
     *
     * @param value reference to validate
     * @param name  logical field name included in the validation error
     * @param <T>   reference type
     * @return the validated non-null reference
     * @throws ValidateException if {@code value} is {@code null}
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

    /**
     * Validates a channel token.
     *
     * @param value token text to trim and validate
     * @param name  logical field name included in the validation error
     * @return trimmed, non-blank, single-line token
     * @throws ValidateException if the token is blank or contains a line break
     */
    private static String validateToken(final String value, final String name) {
        final String current = value == null ? null : StringKit.trim(value);
        if (StringKit.isBlank(current) || StringKit.containsAny(current, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException(name + " must be non-blank and single-line");
        }
        return current;
    }

    /**
     * Returns the shared no-op message handler.
     *
     * @return shared handler that ignores received messages
     */
    public static Handler noop() {
        return Instances.get(Demuxer.class.getName() + ".noop", NoopHandler::new);
    }

    /**
     * Builder for demuxers.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static final class Builder {

        /**
         * Mutable channel registrations in insertion order.
         */
        private final LinkedHashMap<String, Handler> handlers = new LinkedHashMap<>();

        /**
         * Handler used for unregistered channels, or {@code null}.
         */
        private Handler fallback;

        /**
         * Header name inspected when the custom resolver yields no channel.
         */
        private String channelHeader = org.miaixz.bus.fabric.Builder.DEMUXER_DEFAULT_CHANNEL_HEADER;

        /**
         * Optional function that derives a channel from a message.
         */
        private Function<Message, String> resolver;

        /**
         * Creates a builder.
         */
        private Builder() {
            // No initialization required.
        }

        /**
         * Registers a channel handler.
         *
         * @param channel channel identifier to register
         * @param handler handler that receives messages for the channel
         * @return this builder
         */
        public Builder channel(final String channel, final Handler handler) {
            handlers.put(validateToken(channel, "Channel id"), require(handler, "Handler"));
            return this;
        }

        /**
         * Sets fallback handler.
         *
         * @param handler handler used when no channel-specific registration matches
         * @return this builder
         */
        public Builder fallback(final Handler handler) {
            this.fallback = handler;
            return this;
        }

        /**
         * Sets channel header name.
         *
         * @param name message header name used for channel lookup
         * @return this builder
         */
        public Builder header(final String name) {
            this.channelHeader = validateToken(name, "Channel header");
            return this;
        }

        /**
         * Sets a custom channel resolver.
         *
         * @param resolver function that optionally derives a channel from each message
         * @return this builder
         */
        public Builder resolver(final Function<Message, String> resolver) {
            this.resolver = require(resolver, "Message channel resolver");
            return this;
        }

        /**
         * Builds a demuxer.
         *
         * @return immutable demuxer configured from this builder
         * @throws ValidateException if neither a channel handler nor a fallback is configured
         */
        public Demuxer build() {
            Assert.isTrue(
                    !handlers.isEmpty() || fallback != null,
                    () -> new ValidateException("Demuxer must declare a channel handler or fallback"));
            return new Demuxer(handlers, fallback, channelHeader, resolver);
        }

    }

    /**
     * No-op handler.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private static final class NoopHandler implements Handler {

        /**
         * Ignores fallback protocol messages.
         *
         * @param session session on which the ignored message was received
         * @param message protocol message to ignore
         */
        @Override
        public void message(final Session session, final Message message) {
            // No-op handler intentionally ignores protocol messages.
        }

    }

}
