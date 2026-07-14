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

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.protocol.websocket.body.WebSocketBody;

/**
 * Immutable WebSocket message snapshot.
 *
 * @param text        text-message flag
 * @param textValue   text value for text messages
 * @param binaryValue immutable binary payload snapshot
 * @author Kimi Liu
 * @since Java 21+
 */
public record WebSocketMessage(boolean text, String textValue, ByteString binaryValue) {

    /**
     * Maximum materialized WebSocket message payload bytes.
     */
    private static final long MAX_MESSAGE_BYTES = Normal._16 * Normal.MEBI;

    /**
     * First printable non-control character.
     */
    private static final char MIN_TEXT_CODE_POINT = 0x20;

    /**
     * Creates a message snapshot.
     *
     * @param text        text-message flag
     * @param textValue   text value
     * @param binaryValue binary payload
     */
    public WebSocketMessage {
        binaryValue = snapshot(binaryValue == null ? ByteString.EMPTY : binaryValue);
    }

    /**
     * Creates a text message.
     *
     * @param value text value
     * @return text message
     */
    public static WebSocketMessage text(final String value) {
        validateText(value);
        return new WebSocketMessage(true, value, ByteString.EMPTY);
    }

    /**
     * Creates a text message.
     *
     * @param value text bytes
     * @return text message
     */
    public static WebSocketMessage text(final ByteString value) {
        return text(decodeUtf8(require(value, "WebSocket text value")));
    }

    /**
     * Creates a binary message.
     *
     * @param value binary value
     * @return binary message
     * @deprecated use {@link #binary(ByteString)}
     */
    @Deprecated(since = "8.8.3")
    public static WebSocketMessage binary(final ByteBuffer value) {
        final ByteBuffer checked = require(value, "WebSocket binary value");
        return new WebSocketMessage(false, null, ByteString.of(checked.duplicate()));
    }

    /**
     * Creates a binary message.
     *
     * @param value binary value
     * @return binary message
     */
    public static WebSocketMessage binary(final ByteString value) {
        return new WebSocketMessage(false, null, require(value, "WebSocket binary value"));
    }

    /**
     * Creates a message from a body.
     *
     * @param body body
     * @return message
     */
    public static WebSocketMessage of(final WebSocketBody body) {
        final WebSocketBody checked = require(body, "WebSocket body");
        return checked.textMessage() ? text(checked.textValue()) : binary(checked.binaryBytes());
    }

    /**
     * Returns a read-only binary payload buffer view.
     *
     * @return read-only binary payload buffer
     * @deprecated use {@link #binaryValue()}
     */
    @Deprecated(since = "8.8.3")
    public ByteBuffer binaryBuffer() {
        return binaryValue.asByteBuffer();
    }

    /**
     * Returns the WebSocket body.
     *
     * @return body
     */
    public WebSocketBody body() {
        return text ? WebSocketBody.text(textValue) : WebSocketBody.binary(binaryValue);
    }

    /**
     * Copies a binary payload.
     *
     * @param value binary value
     * @return read-only payload
     */
    private static ByteString snapshot(final ByteString value) {
        if (value.size() > MAX_MESSAGE_BYTES) {
            throw new ProtocolException("WebSocket message payload is too large");
        }
        return ByteString.of(value.toByteArray());
    }

    /**
     * Validates text.
     *
     * @param value text value
     */
    private static void validateText(final String value) {
        final String checked = require(value, "WebSocket text value");
        for (int i = Normal._0; i < checked.length(); i++) {
            final char current = checked.charAt(i);
            if (current < MIN_TEXT_CODE_POINT && current != Symbol.C_CR && current != Symbol.C_LF) {
                throw new ValidateException("WebSocket text contains an invalid control character");
            }
        }
        ByteString.encodeUtf8(checked);
    }

    /**
     * Decodes text bytes as strict UTF-8.
     *
     * @param value text bytes
     * @return decoded text
     */
    private static String decodeUtf8(final ByteString value) {
        try {
            return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT).decode(value.asByteBuffer()).toString();
        } catch (final CharacterCodingException e) {
            throw new ValidateException("WebSocket text must be valid UTF-8", e);
        }
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

}
