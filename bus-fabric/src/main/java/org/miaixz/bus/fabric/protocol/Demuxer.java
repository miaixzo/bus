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
     * Default message channel header.
     */
    private static final String DEFAULT_CHANNEL_HEADER = "X-Fabric-Channel";

    /**
     * Channel handlers.
     */
    private final Map<String, Handler> handlers;

    /**
     * Optional fallback handler.
     */
    private final Handler fallback;

    /**
     * Header used for channel lookup.
     */
    private final String channelHeader;

    /**
     * Optional custom resolver.
     */
    private final Function<Message, String> resolver;

    /**
     * Creates a demuxer.
     *
     * @param handlers      channel handlers
     * @param fallback      fallback handler
     * @param channelHeader channel header
     * @param resolver      custom resolver
     */
    private Demuxer(
            final Map<String, Handler> handlers,
            final Handler fallback,
            final String channelHeader,
            final Function<Message, String> resolver) {
        this.handlers = Collections.unmodifiableMap(new LinkedHashMap<>(require(handlers, "Handlers")));
        this.fallback = fallback;
        this.channelHeader = validateToken(channelHeader, "Channel header");
        this.resolver = resolver;
    }

    /**
     * Creates a demuxer builder.
     *
     * @return builder
     */
    public static Builder builder() {
        return new Builder();
    }

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

    @Override
    public void failure(final Session session, final Throwable cause) {
        for (final Handler handler : targets()) {
            handler.failure(session, cause);
        }
    }

    @Override
    public void closed(final Session session) {
        for (final Handler handler : targets()) {
            handler.closed(session);
        }
    }

    /**
     * Returns immutable channel handlers.
     *
     * @return channel handlers
     */
    public Map<String, Handler> handlers() {
        return handlers;
    }

    /**
     * Returns the configured channel header.
     *
     * @return channel header
     */
    public String channelHeader() {
        return channelHeader;
    }

    /**
     * Resolves the channel for a message.
     *
     * @param message message
     * @return channel
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
     * @return targets
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
     * @param value value
     * @param name  field name
     * @param <T>   value type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

    /**
     * Validates a channel token.
     *
     * @param value value
     * @param name  field name
     * @return token
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
     * @return no-op handler
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
         * Channel handlers.
         */
        private final LinkedHashMap<String, Handler> handlers = new LinkedHashMap<>();

        /**
         * Fallback handler.
         */
        private Handler fallback;

        /**
         * Channel header name.
         */
        private String channelHeader = DEFAULT_CHANNEL_HEADER;

        /**
         * Custom channel resolver.
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
         * @param channel channel id
         * @param handler handler
         * @return this builder
         */
        public Builder channel(final String channel, final Handler handler) {
            handlers.put(validateToken(channel, "Channel id"), require(handler, "Handler"));
            return this;
        }

        /**
         * Sets fallback handler.
         *
         * @param handler fallback handler
         * @return this builder
         */
        public Builder fallback(final Handler handler) {
            this.fallback = handler;
            return this;
        }

        /**
         * Sets channel header name.
         *
         * @param name header name
         * @return this builder
         */
        public Builder header(final String name) {
            this.channelHeader = validateToken(name, "Channel header");
            return this;
        }

        /**
         * Sets a custom channel resolver.
         *
         * @param resolver resolver
         * @return this builder
         */
        public Builder resolver(final Function<Message, String> resolver) {
            this.resolver = require(resolver, "Message channel resolver");
            return this;
        }

        /**
         * Builds a demuxer.
         *
         * @return demuxer
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

        @Override
        public void message(final Session session, final Message message) {
            // No-op handler intentionally ignores protocol messages.
        }

    }

}
