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
package org.miaixz.bus.fabric.protocol.websocket.frame;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.protocol.websocket.WebSocketClose;

/**
 * Immutable WebSocket frame payload snapshot.
 *
 * @param opcode  frame opcode
 * @param fin     final-fragment flag
 * @param payload immutable payload snapshot
 * @param control control-frame flag
 * @author Kimi Liu
 * @since Java 21+
 */
public record WebSocketFrame(int opcode, boolean fin, ByteString payload, boolean control) {

    /**
     * Continuation opcode.
     */

    /**
     * Text opcode.
     */

    /**
     * Binary opcode.
     */

    /**
     * Close opcode.
     */

    /**
     * Ping opcode.
     */

    /**
     * Pong opcode.
     */

    /**
     * Maximum message payload bytes accepted by this implementation.
     */

    /**
     * Maximum control frame payload bytes.
     */

    /**
     * Creates a frame snapshot.
     *
     * @param opcode  frame opcode
     * @param fin     final-fragment flag
     * @param payload payload
     * @param control control-frame flag
     */
    public WebSocketFrame {
        opcode = validateOpcode(opcode);
        payload = snapshot(payload);
        control = opcode >= Normal._8;
        if (control && !fin) {
            throw new ProtocolException("WebSocket control frame must be final");
        }
        if (control && payload.size() > Builder._125) {
            throw new ProtocolException("WebSocket control payload is too large");
        }
    }

    /**
     * Creates a text frame.
     *
     * @param value text value
     * @return text frame
     */
    public static WebSocketFrame text(final String value) {
        return of(Normal._1, true, validateText(value));
    }

    /**
     * Creates a text frame.
     *
     * @param value text bytes
     * @return text frame
     */
    public static WebSocketFrame text(final ByteString value) {
        validateText(value);
        return of(Normal._1, true, value);
    }

    /**
     * Creates a binary frame.
     *
     * @param payload payload bytes
     * @return binary frame
     */
    public static WebSocketFrame binary(final ByteString payload) {
        return of(Builder.WEBSOCKET_OPCODE_BINARY, true, require(payload, "WebSocket payload"));
    }

    /**
     * Creates a close frame.
     *
     * @param code   close code
     * @param reason close reason
     * @return close frame
     */
    public static WebSocketFrame close(final int code, final String reason) {
        final WebSocketClose close = WebSocketClose.of(code, reason);
        final ByteString reasonBytes = ByteString.encodeUtf8(close.reason());
        final byte[] reasonData = reasonBytes.toByteArray();
        final byte[] payload = new byte[Short.BYTES + reasonData.length];
        payload[0] = (byte) (code >>> Byte.SIZE);
        payload[1] = (byte) code;
        System.arraycopy(reasonData, 0, payload, Short.BYTES, reasonData.length);
        return of(Normal._8, true, ByteString.of(payload));
    }

    /**
     * Creates an internal frame.
     *
     * @param opcode  opcode
     * @param fin     fin flag
     * @param payload payload
     * @return frame
     */
    private static WebSocketFrame of(final int opcode, final boolean fin, final ByteString payload) {
        return new WebSocketFrame(opcode, fin, payload, opcode >= Normal._8);
    }

    /**
     * Validates an opcode.
     *
     * @param opcode opcode
     * @return opcode
     */
    private static int validateOpcode(final int opcode) {
        return switch (opcode) {
            case Normal._0, Normal._1, Builder.WEBSOCKET_OPCODE_BINARY, Normal._8, Builder.WEBSOCKET_OPCODE_PING, Builder.WEBSOCKET_OPCODE_PONG -> opcode;
            default -> throw new ProtocolException("Invalid WebSocket opcode");
        };
    }

    /**
     * Copies a payload.
     *
     * @param payload payload
     * @return read-only payload
     */
    private static ByteString snapshot(final ByteString payload) {
        final ByteString checked = require(payload, "WebSocket payload");
        if (checked.size() > Builder.BYTES_16_MIB) {
            throw new ProtocolException("WebSocket payload is too large");
        }
        return ByteString.of(checked.toByteArray());
    }

    /**
     * Validates text.
     *
     * @param value text value
     */
    private static ByteString validateText(final String value) {
        final String checked = require(value, "WebSocket text");
        for (int i = Normal._0; i < checked.length(); i++) {
            final char current = checked.charAt(i);
            if (current < Builder.WEB_SOCKET_FRAME_MIN_TEXT_CODE_POINT && current != Symbol.C_CR
                    && current != Symbol.C_LF) {
                throw new ValidateException("WebSocket text contains an invalid control character");
            }
        }
        return ByteString.encodeUtf8(checked);
    }

    /**
     * Validates text bytes.
     *
     * @param value text bytes
     */
    private static void validateText(final ByteString value) {
        validateText(decodeUtf8(require(value, "WebSocket text")));
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
     * @param name  name
     * @param <T>   value type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

}
