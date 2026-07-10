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

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Immutable WebSocket frame payload snapshot.
 *
 * @param opcode  frame opcode
 * @param fin     final-fragment flag
 * @param payload payload snapshot
 * @param control control-frame flag
 * @author Kimi Liu
 * @since Java 21+
 */
public record WebSocketFrame(int opcode, boolean fin, ByteBuffer payload, boolean control) {

    /**
     * Continuation opcode.
     */
    private static final int CONTINUATION = 0x0;

    /**
     * Text opcode.
     */
    private static final int TEXT = 0x1;

    /**
     * Binary opcode.
     */
    private static final int BINARY = 0x2;

    /**
     * Close opcode.
     */
    private static final int CLOSE = 0x8;

    /**
     * Ping opcode.
     */
    private static final int PING = 0x9;

    /**
     * Pong opcode.
     */
    private static final int PONG = 0xA;

    /**
     * Maximum in-memory payload.
     */
    private static final int MAX_PAYLOAD = 16 * 1024 * 1024;

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
        control = opcode >= CLOSE;
        if (control && !fin) {
            throw new ProtocolException("WebSocket control frame must be final");
        }
        if (control && payload.remaining() > 125) {
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
        validateText(value);
        return of(TEXT, true, ByteBuffer.wrap(value.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Creates a binary frame.
     *
     * @param payload payload
     * @return binary frame
     */
    public static WebSocketFrame binary(final ByteBuffer payload) {
        return of(BINARY, true, payload);
    }

    /**
     * Creates a close frame.
     *
     * @param code   close code
     * @param reason close reason
     * @return close frame
     */
    public static WebSocketFrame close(final int code, final String reason) {
        final String text = validateClose(code, reason);
        final byte[] reasonBytes = text.getBytes(StandardCharsets.UTF_8);
        if (reasonBytes.length > 123) {
            throw new ProtocolException("WebSocket close reason is too large");
        }
        final ByteBuffer buffer = ByteBuffer.allocate(2 + reasonBytes.length);
        buffer.putShort((short) code).put(reasonBytes).flip();
        return of(CLOSE, true, buffer);
    }

    /**
     * Creates an internal frame.
     *
     * @param opcode  opcode
     * @param fin     fin flag
     * @param payload payload
     * @return frame
     */
    private static WebSocketFrame of(final int opcode, final boolean fin, final ByteBuffer payload) {
        return new WebSocketFrame(opcode, fin, payload, opcode >= CLOSE);
    }

    /**
     * Returns a read-only payload view.
     *
     * @return read-only payload
     */
    @Override
    public ByteBuffer payload() {
        return payload.asReadOnlyBuffer();
    }

    /**
     * Validates an opcode.
     *
     * @param opcode opcode
     * @return opcode
     */
    private static int validateOpcode(final int opcode) {
        return switch (opcode) {
            case CONTINUATION, TEXT, BINARY, CLOSE, PING, PONG -> opcode;
            default -> throw new ProtocolException("Invalid WebSocket opcode");
        };
    }

    /**
     * Copies a payload.
     *
     * @param payload payload
     * @return read-only payload
     */
    private static ByteBuffer snapshot(final ByteBuffer payload) {
        if (payload == null) {
            throw new ValidateException("WebSocket payload must not be null");
        }
        if (payload.remaining() > MAX_PAYLOAD) {
            throw new ProtocolException("WebSocket payload is too large");
        }
        final ByteBuffer duplicate = payload.duplicate();
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
            throw new ValidateException("WebSocket text must not be null");
        }
        for (int i = 0; i < value.length(); i++) {
            final char current = value.charAt(i);
            if (current < 0x20 && current != Symbol.C_CR && current != Symbol.C_LF) {
                throw new ValidateException("WebSocket text contains an invalid control character");
            }
        }
    }

    /**
     * Validates close code and reason.
     *
     * @param code   close code
     * @param reason close reason
     * @return normalized reason
     */
    private static String validateClose(final int code, final String reason) {
        if (!validCloseCode(code)) {
            throw new ValidateException("Invalid WebSocket close code");
        }
        final String text = reason == null ? Normal.EMPTY : reason;
        if (StringKit.containsAny(text, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("WebSocket close reason must be single-line");
        }
        if (text.getBytes(StandardCharsets.UTF_8).length > 123) {
            throw new ValidateException("WebSocket close reason is too large");
        }
        return text;
    }

    /**
     * Returns whether a close code can be sent on the wire.
     *
     * @param code close code
     * @return true when valid
     */
    private static boolean validCloseCode(final int code) {
        return code == 1000 || code >= 1001 && code <= 1014 && code != 1005 && code != 1006
                || code >= 3000 && code <= 4999;
    }

}
