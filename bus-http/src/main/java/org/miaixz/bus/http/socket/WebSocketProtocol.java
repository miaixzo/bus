/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.http.socket;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;

/**
 * Constants and utility methods for the WebSocket protocol (RFC 6455).
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class WebSocketProtocol {

    /**
     * The magic value used to compute the Sec-WebSocket-Accept header.
     */
    static final String ACCEPT_MAGIC = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    /**
     * A frame's FIN bit.
     */
    static final int B0_FLAG_FIN = 0b10000000;
    /**
     * A frame's RSV1 bit.
     */
    static final int B0_FLAG_RSV1 = 0b01000000;
    /**
     * A frame's RSV2 bit.
     */
    static final int B0_FLAG_RSV2 = 0b00100000;
    /**
     * A frame's RSV3 bit.
     */
    static final int B0_FLAG_RSV3 = 0b00010000;
    /**
     * The opcode mask for a frame's first byte.
     */
    static final int B0_MASK_OPCODE = 0b00001111;
    /**
     * A bit in the opcode that indicates a control frame.
     */
    static final int OPCODE_FLAG_CONTROL = 0b00001000;

    /**
     * A frame's MASK bit.
     */
    static final int B1_FLAG_MASK = 0b10000000;
    /**
     * The payload length mask for a frame's second byte.
     */
    static final int B1_MASK_LENGTH = 0b01111111;

    /**
     * Opcode for a continuation frame.
     */
    static final int OPCODE_CONTINUATION = 0x0;
    /**
     * Opcode for a text frame.
     */
    static final int OPCODE_TEXT = 0x1;
    /**
     * Opcode for a binary frame.
     */
    static final int OPCODE_BINARY = 0x2;
    /**
     * Opcode for a close control frame.
     */
    static final int OPCODE_CONTROL_CLOSE = 0x8;
    /**
     * Opcode for a ping control frame.
     */
    static final int OPCODE_CONTROL_PING = 0x9;
    /**
     * Opcode for a pong control frame.
     */
    static final int OPCODE_CONTROL_PONG = 0xa;

    /**
     * The maximum payload length of a frame that can be expressed in the 7-bit length field.
     */
    static final long PAYLOAD_BYTE_MAX = 125L;
    /**
     * The maximum length of a close message in bytes.
     */
    static final long CLOSE_MESSAGE_MAX = PAYLOAD_BYTE_MAX - 2;
    /**
     * A special value in the 7-bit length field that indicates the length is in the following 2 bytes.
     */
    static final int PAYLOAD_SHORT = 126;
    /**
     * The maximum payload length that can be expressed in the 16-bit length field.
     */
    static final long PAYLOAD_SHORT_MAX = 0xffffL;
    /**
     * A special value in the 7-bit length field that indicates the length is in the following 8 bytes.
     */
    static final int PAYLOAD_LONG = 127;
    /**
     * A close code indicating that an endpoint is "going away".
     */
    static final int CLOSE_CLIENT_GOING_AWAY = 1001;
    /**
     * A reserved close code that MUST NOT be set in a close frame.
     */
    static final int CLOSE_NO_STATUS_CODE = 1005;

    public WebSocketProtocol() {
        throw new AssertionError("No instances.");
    }

    /**
     * Applies a WebSocket mask to the given data.
     * 
     * @param cursor The cursor pointing to the data to be masked/unmasked.
     * @param key    The 4-byte mask key.
     */
    static void toggleMask(Buffer.UnsafeCursor cursor, byte[] key) {
        int keyIndex = 0;
        int keyLength = key.length;
        do {
            byte[] buffer = cursor.data;
            for (int i = cursor.start, end = cursor.end; i < end; i++, keyIndex++) {
                keyIndex %= keyLength;
                buffer[i] = (byte) (buffer[i] ^ key[keyIndex]);
            }
        } while (cursor.next() != -1);
    }

    /**
     * Returns a human-readable error message for an invalid close code, or null if the code is valid.
     * 
     * @param code The close code.
     * @return An error message string or null.
     */
    static String closeCodeExceptionMessage(int code) {
        if (code < 1000 || code >= 5000) {
            return "Code must be in range [1000,5000): " + code;
        } else if ((code >= 1004 && code <= 1006) || (code >= 1012 && code <= 2999)) {
            return "Code " + code + " is reserved and may not be used.";
        } else {
            return null;
        }
    }

    /**
     * Validates a WebSocket close code.
     * 
     * @param code The close code to validate.
     * @throws IllegalArgumentException if the code is invalid.
     */
    static void validateCloseCode(int code) {
        String message = closeCodeExceptionMessage(code);
        if (null != message)
            throw new IllegalArgumentException(message);
    }

    /**
     * Computes the `Sec-WebSocket-Accept` header value from a client's key.
     * 
     * @param key The `Sec-WebSocket-Key` from the client.
     * @return The corresponding `Sec-WebSocket-Accept` value for the server's response.
     */
    public static String acceptHeader(String key) {
        return ByteString.encodeUtf8(key + WebSocketProtocol.ACCEPT_MAGIC).sha1().base64();
    }

}
