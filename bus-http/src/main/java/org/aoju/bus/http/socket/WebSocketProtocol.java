/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.http.socket;

import org.aoju.bus.core.io.segment.Buffer;
import org.aoju.bus.core.io.segment.ByteString;

/**
 * @author Kimi Liu
 * @version 3.6.8
 * @since JDK 1.8+
 */
public final class WebSocketProtocol {
    /**
     * Magic value which must be appended to the key in a response header.
     */
    static final String ACCEPT_MAGIC = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    /**
     * Byte 0 flag for whether this is the final fragment in a message.
     */
    static final int B0_FLAG_FIN = 0b10000000;
    /**
     * Byte 0 reserved flag 1. Must be 0 unless negotiated otherwise.
     */
    static final int B0_FLAG_RSV1 = 0b01000000;
    /**
     * Byte 0 reserved flag 2. Must be 0 unless negotiated otherwise.
     */
    static final int B0_FLAG_RSV2 = 0b00100000;
    /**
     * Byte 0 reserved flag 3. Must be 0 unless negotiated otherwise.
     */
    static final int B0_FLAG_RSV3 = 0b00010000;
    /**
     * Byte 0 mask for the frame opcode.
     */
    static final int B0_MASK_OPCODE = 0b00001111;
    /**
     * Flag in the opcode which indicates a control frame.
     */
    static final int OPCODE_FLAG_CONTROL = 0b00001000;

    /**
     * Byte 1 flag for whether the payload data is masked. <p> If this flag is set, the next four
     * bytes represent the mask key. These bytes appear after any additional bytes specified by {@link
     * #B1_MASK_LENGTH}.
     */
    static final int B1_FLAG_MASK = 0b10000000;
    /**
     * Byte 1 mask for the payload length. <p> If this value is {@link #PAYLOAD_SHORT}, the next two
     * bytes represent the length. If this value is {@link #PAYLOAD_LONG}, the next eight bytes
     * represent the length.
     */
    static final int B1_MASK_LENGTH = 0b01111111;

    static final int OPCODE_CONTINUATION = 0x0;
    static final int OPCODE_TEXT = 0x1;
    static final int OPCODE_BINARY = 0x2;

    static final int OPCODE_CONTROL_CLOSE = 0x8;
    static final int OPCODE_CONTROL_PING = 0x9;
    static final int OPCODE_CONTROL_PONG = 0xa;

    /**
     * Maximum length of frame payload. Larger payloads, if supported by the frame type, can use the
     * special values {@link #PAYLOAD_SHORT} or {@link #PAYLOAD_LONG}.
     */
    static final long PAYLOAD_BYTE_MAX = 125L;
    /**
     * Maximum length of close message in bytes.
     */
    static final long CLOSE_MESSAGE_MAX = PAYLOAD_BYTE_MAX - 2;
    /**
     * Value for {@link #B1_MASK_LENGTH} which indicates the next two bytes are the unsigned length.
     */
    static final int PAYLOAD_SHORT = 126;
    /**
     * Maximum length of a frame payload to be denoted as {@link #PAYLOAD_SHORT}.
     */
    static final long PAYLOAD_SHORT_MAX = 0xffffL;
    /**
     * Value for {@link #B1_MASK_LENGTH} which indicates the next eight bytes are the unsigned
     * length.
     */
    static final int PAYLOAD_LONG = 127;

    /**
     * Used when an unchecked exception was thrown in a listener.
     */
    static final int CLOSE_CLIENT_GOING_AWAY = 1001;
    /**
     * Used when an empty close frame was received (i.e., without a status code).
     */
    static final int CLOSE_NO_STATUS_CODE = 1005;

    private WebSocketProtocol() {
        throw new AssertionError("No instances.");
    }

    static void toggleMask(Buffer.UnsafeCursor cursor, byte[] key) {
        int keyIndex = 0;
        int keyLength = key.length;
        do {
            byte[] buffer = cursor.data;
            for (int i = cursor.start, end = cursor.end; i < end; i++, keyIndex++) {
                keyIndex %= keyLength; // Reassign to prevent overflow breaking counter.
                buffer[i] = (byte) (buffer[i] ^ key[keyIndex]);
            }
        } while (cursor.next() != -1);
    }

    static String closeCodeExceptionMessage(int code) {
        if (code < 1000 || code >= 5000) {
            return "Code must be in range [1000,5000): " + code;
        } else if ((code >= 1004 && code <= 1006) || (code >= 1012 && code <= 2999)) {
            return "Code " + code + " is reserved and may not be used.";
        } else {
            return null;
        }
    }

    static void validateCloseCode(int code) {
        String message = closeCodeExceptionMessage(code);
        if (message != null) throw new IllegalArgumentException(message);
    }

    public static String acceptHeader(String key) {
        return ByteString.encodeUtf8(key + WebSocketProtocol.ACCEPT_MAGIC).sha1().base64();
    }

}
