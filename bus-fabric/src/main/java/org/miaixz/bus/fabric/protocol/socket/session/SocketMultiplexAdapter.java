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
package org.miaixz.bus.fabric.protocol.socket.session;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Handler;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Session;

/**
 * Logical-channel router backed by the current socket handler contract.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class SocketMultiplexAdapter implements Handler {

    /**
     * Default channel header name.
     */
    public static final String CHANNEL_HEADER = "X-Fabric-Channel";

    /**
     * Channel handlers.
     */
    private final Map<String, Handler> handlers;

    /**
     * Optional fallback handler.
     */
    private final Handler fallback;

    /**
     * Channel header name.
     */
    private final String channelHeader;

    /**
     * Creates an adapter.
     *
     * @param handlers      handlers
     * @param fallback      fallback
     * @param channelHeader channel header
     */
    private SocketMultiplexAdapter(final Map<String, Handler> handlers, final Handler fallback,
            final String channelHeader) {
        this.handlers = Collections.unmodifiableMap(new LinkedHashMap<>(require(handlers, "Handlers")));
        this.fallback = fallback;
        this.channelHeader = validateToken(channelHeader, "Channel header");
    }

    /**
     * Creates a builder.
     *
     * @return builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Routes one message to a channel handler.
     *
     * @param session session
     * @param message message
     */
    @Override
    public void message(final Session session, final Message message) {
        route(session, message);
    }

    /**
     * Routes one message to a channel handler.
     *
     * @param session session
     * @param message message
     */
    public void route(final Session session, final Message message) {
        require(session, "Session");
        require(message, "Message");
        final String channel = channel(message);
        final Handler handler = handlers.get(channel);
        if (handler != null) {
            handler.message(session, message);
            return;
        }
        if (fallback != null) {
            fallback.message(session, message);
            return;
        }
        throw new ProtocolException("No socket multiplex handler for channel: " + channel);
    }

    /**
     * Forwards failures to all registered handlers and fallback.
     *
     * @param session session
     * @param cause   failure cause
     */
    @Override
    public void failure(final Session session, final Throwable cause) {
        handlers.values().forEach(handler -> handler.failure(session, cause));
        if (fallback != null) {
            fallback.failure(session, cause);
        }
    }

    /**
     * Forwards close notifications to all registered handlers and fallback.
     *
     * @param session session
     */
    @Override
    public void closed(final Session session) {
        handlers.values().forEach(handler -> handler.closed(session));
        if (fallback != null) {
            fallback.closed(session);
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
     * Resolves a channel id from a message.
     *
     * @param message message
     * @return channel id
     */
    public String channel(final Message message) {
        final Message current = require(message, "Message");
        final String header = current.headers().get(channelHeader);
        if (header != null && !header.isBlank()) {
            return validateToken(header, "Channel id");
        }
        final Object tag = current.tag();
        if (tag != null) {
            return validateToken(tag.toString(), "Channel id");
        }
        throw new ProtocolException("Socket multiplex channel id is missing");
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
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value;
    }

    /**
     * Validates a single-line token.
     *
     * @param value value
     * @param name  field name
     * @return value
     */
    private static String validateToken(final String value, final String name) {
        if (StringKit.isBlank(value) || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException(name + " must be non-blank and single-line");
        }
        return value;
    }

    /**
     * Builder for socket multiplex adapters.
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
        private String channelHeader = CHANNEL_HEADER;

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
         * Builds an adapter.
         *
         * @return adapter
         */
        public SocketMultiplexAdapter build() {
            return new SocketMultiplexAdapter(handlers, fallback, channelHeader);
        }

    }

}
