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
 * @param control derived control-frame flag; constructor input is ignored and recomputed from the opcode
 * @author Kimi Liu
 * @since Java 21+
 */
public record WebSocketFrame(int opcode, boolean fin, ByteString payload, boolean control) {

    /**
     * Creates a frame snapshot.
     *
     * @param opcode  supported continuation, data, close, ping, or pong opcode
     * @param fin     whether this is the final fragment
     * @param payload payload bytes copied into an immutable snapshot
     * @param control compatibility argument ignored in favor of deriving control status from the opcode
     * @throws ProtocolException if the opcode, control-frame finality, or payload size violates frame rules
     * @throws ValidateException if {@code payload} is {@code null}
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
     * @param value text whose UTF-8 encoding becomes the frame payload
     * @return final text frame with an immutable encoded payload
     * @throws ValidateException if the text is {@code null} or contains a prohibited control character
     */
    public static WebSocketFrame text(final String value) {
        return of(Normal._1, true, validateText(value));
    }

    /**
     * Creates a text frame.
     *
     * @param value UTF-8 bytes to validate and copy
     * @return final text frame with an immutable payload snapshot
     * @throws ValidateException if the bytes are {@code null}, malformed UTF-8, or contain prohibited text controls
     */
    public static WebSocketFrame text(final ByteString value) {
        validateText(value);
        return of(Normal._1, true, value);
    }

    /**
     * Creates a binary frame.
     *
     * @param payload payload bytes
     * @return final binary frame containing an immutable copy
     * @throws ProtocolException if the payload exceeds the frame snapshot limit
     * @throws ValidateException if {@code payload} is {@code null}
     */
    public static WebSocketFrame binary(final ByteString payload) {
        return of(Builder.WEBSOCKET_OPCODE_BINARY, true, require(payload, "WebSocket payload"));
    }

    /**
     * Creates a close frame.
     *
     * @param code   valid WebSocket close status code
     * @param reason UTF-8 close reason that fits within a control-frame payload
     * @return final close frame containing the two-byte status and encoded reason
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
     * @param opcode  supported frame opcode
     * @param fin     whether this is the final fragment
     * @param payload payload bytes to snapshot
     * @return validated immutable frame
     */
    private static WebSocketFrame of(final int opcode, final boolean fin, final ByteString payload) {
        return new WebSocketFrame(opcode, fin, payload, opcode >= Normal._8);
    }

    /**
     * Validates an opcode.
     *
     * @param opcode candidate four-bit frame opcode
     * @return unchanged supported opcode
     * @throws ProtocolException if the opcode is reserved or unsupported
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
     * @param payload bytes to validate and defensively copy
     * @return independent immutable payload snapshot
     * @throws ProtocolException if the payload exceeds 16 MiB
     * @throws ValidateException if {@code payload} is {@code null}
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
     * @param value text to validate and encode
     * @return validated UTF-8 payload bytes
     * @throws ValidateException if the text is {@code null} or contains a prohibited control character
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
     * @throws ValidateException if the bytes are {@code null}, malformed UTF-8, or contain prohibited controls
     */
    private static void validateText(final ByteString value) {
        validateText(decodeUtf8(require(value, "WebSocket text")));
    }

    /**
     * Decodes text bytes as strict UTF-8.
     *
     * @param value bytes to decode without replacement
     * @return strictly decoded UTF-8 text
     * @throws ValidateException if the byte sequence is malformed or unmappable
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
     * @param value reference to validate
     * @param name  logical reference name included in the validation error
     * @param <T>   reference type
     * @return validated non-null reference
     * @throws ValidateException if {@code value} is {@code null}
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

}
