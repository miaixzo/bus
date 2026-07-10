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
import java.nio.charset.StandardCharsets;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.protocol.websocket.body.WebSocketBody;

/**
 * Immutable WebSocket message snapshot.
 *
 * @param text        text-message flag
 * @param textValue   text value for text messages
 * @param binaryValue binary payload snapshot
 * @author Kimi Liu
 * @since Java 21+
 */
public record WebSocketMessage(boolean text, String textValue, ByteBuffer binaryValue) {

    /**
     * Maximum in-memory payload.
     */
    private static final int MAX_PAYLOAD = 16 * 1024 * 1024;

    /**
     * Creates a message snapshot.
     *
     * @param text        text-message flag
     * @param textValue   text value
     * @param binaryValue binary payload
     */
    public WebSocketMessage {
        binaryValue = snapshot(binaryValue == null ? ByteBuffer.allocate(0) : binaryValue);
    }

    /**
     * Creates a text message.
     *
     * @param value text value
     * @return text message
     */
    public static WebSocketMessage text(final String value) {
        validateText(value);
        return new WebSocketMessage(true, value, ByteBuffer.allocate(0));
    }

    /**
     * Creates a binary message.
     *
     * @param value binary value
     * @return binary message
     */
    public static WebSocketMessage binary(final ByteBuffer value) {
        if (value == null) {
            throw new ValidateException("WebSocket binary value must not be null");
        }
        return new WebSocketMessage(false, null, value);
    }

    /**
     * Creates a message from a body.
     *
     * @param body body
     * @return message
     */
    public static WebSocketMessage of(final WebSocketBody body) {
        if (body == null) {
            throw new ValidateException("WebSocket body must not be null");
        }
        return body.textMessage() ? text(body.textValue()) : binary(body.binaryValue());
    }

    /**
     * Returns a read-only binary payload view.
     *
     * @return read-only binary payload
     */
    @Override
    public ByteBuffer binaryValue() {
        return binaryValue.asReadOnlyBuffer();
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
    private static ByteBuffer snapshot(final ByteBuffer value) {
        if (value.remaining() > MAX_PAYLOAD) {
            throw new ProtocolException("WebSocket message payload is too large");
        }
        final ByteBuffer duplicate = value.duplicate();
        final ByteBuffer copy = ByteBuffer.allocate(duplicate.remaining());
        copy.put(duplicate).flip();
        return copy.asReadOnlyBuffer();
    }

    /**
     * Validates text.
     *
     * @param value text value
     */
    private static void validateText(final String value) {
        if (value == null) {
            throw new ValidateException("WebSocket text value must not be null");
        }
        for (int i = 0; i < value.length(); i++) {
            final char current = value.charAt(i);
            if (current < 0x20 && current != Symbol.C_CR && current != Symbol.C_LF) {
                throw new ValidateException("WebSocket text contains an invalid control character");
            }
        }
        value.getBytes(StandardCharsets.UTF_8);
    }

}
