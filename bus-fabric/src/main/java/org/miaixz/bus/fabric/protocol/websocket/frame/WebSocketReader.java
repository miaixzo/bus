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

import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.protocol.websocket.WebSocketClose;

/**
 * Parser for one WebSocket frame from a byte source.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class WebSocketReader {

    /**
     * Source borrowed from the owning WebSocket session.
     */
    private final Source source;

    /**
     * Reusable input buffer preserving bytes beyond the current frame.
     */
    private final Buffer input;

    /**
     * Required mask direction for the peer role.
     */
    private final boolean expectMasked;

    /**
     * Creates a single-frame reader.
     *
     * @param source       byte source
     * @param expectMasked true for server-side readers, false for client-side readers
     */
    public WebSocketReader(final Source source, final boolean expectMasked) {
        this.source = require(source, "WebSocket source");
        this.input = new Buffer();
        this.expectMasked = expectMasked;
    }

    /**
     * Reads and validates one complete WebSocket frame.
     *
     * @return immutable frame
     */
    public WebSocketFrame next() {
        final int first = readByte();
        final int second = readByte();
        if ((first & Builder.WEBSOCKET_RSV_MASK) != Normal._0) {
            throw new ProtocolException("WebSocket RSV bits must be zero");
        }
        final boolean fin = (first & Normal._128) != Normal._0;
        final int opcode = first & Builder.WEBSOCKET_OPCODE_MASK;
        final boolean control = opcode >= Normal._8;
        final boolean masked = (second & Normal._128) != Normal._0;
        if (masked != expectMasked) {
            throw new ProtocolException("Unexpected WebSocket mask direction");
        }
        final int marker = second & Builder._127;
        final long length = payloadLength(marker);
        validateControl(fin, control, length);
        if (length > Builder.BYTES_16_MIB) {
            throw new ProtocolException("WebSocket frame exceeds the 16 MiB limit");
        }
        final byte[] mask = masked ? readBytes(Normal._4) : null;
        final Buffer payload = readBuffer((int) length);
        if (mask != null) {
            unmask(payload, mask, length);
        }
        final ByteString bytes = payload.readByteString();
        validateText(opcode, fin, bytes);
        validateClose(opcode, bytes);
        return new WebSocketFrame(opcode, fin, bytes, control);
    }

    /**
     * Decodes a canonical payload length.
     *
     * @param marker seven-bit length marker
     * @return payload length
     */
    private long payloadLength(final int marker) {
        if (marker < Builder.WEBSOCKET_LENGTH_16_MARKER) {
            return marker;
        }
        if (marker == Builder.WEBSOCKET_LENGTH_16_MARKER) {
            final int length = readUnsignedShort();
            if (length < Builder.WEBSOCKET_LENGTH_16_MARKER) {
                throw new ProtocolException("WebSocket payload length uses non-canonical 16-bit encoding");
            }
            return length;
        }
        final long length = readLong();
        if (length < Normal.LONG_ZERO) {
            throw new ProtocolException("WebSocket 64-bit payload length has its reserved bit set");
        }
        if (length <= Normal._65535) {
            throw new ProtocolException("WebSocket payload length uses non-canonical 64-bit encoding");
        }
        return length;
    }

    /**
     * Validates control-frame fragmentation and length rules.
     *
     * @param fin     final flag
     * @param control control-frame flag
     * @param length  payload length
     */
    private static void validateControl(final boolean fin, final boolean control, final long length) {
        if (control && !fin) {
            throw new ProtocolException("WebSocket control frames must set FIN");
        }
        if (control && length > Builder._125) {
            throw new ProtocolException("WebSocket control payload exceeds 125 bytes");
        }
    }

    /**
     * Strictly validates a complete text frame.
     *
     * @param opcode  frame opcode
     * @param fin     final flag
     * @param payload frame payload
     */
    private static void validateText(final int opcode, final boolean fin, final ByteString payload) {
        if (opcode == Normal._1 && fin) {
            decodeUtf8(payload, "WebSocket text payload");
        }
    }

    /**
     * Validates close code and strict UTF-8 close reason.
     *
     * @param opcode  frame opcode
     * @param payload frame payload
     */
    private static void validateClose(final int opcode, final ByteString payload) {
        if (opcode != Normal._8 || payload.size() == Normal._0) {
            return;
        }
        if (payload.size() == Normal._1) {
            throw new ProtocolException("WebSocket close payload cannot contain one byte");
        }
        final Buffer value = new Buffer().write(payload);
        final int code = Short.toUnsignedInt(value.readShort());
        final String reason = decodeUtf8(value.readByteString(), "WebSocket close reason");
        try {
            WebSocketClose.of(code, reason);
        } catch (final ValidateException e) {
            throw new ProtocolException("Invalid WebSocket close code or reason", e);
        }
    }

    /**
     * Decodes UTF-8 with malformed and unmappable input reporting enabled.
     *
     * @param value encoded bytes
     * @param field diagnostic field name
     * @return decoded value
     */
    private static String decodeUtf8(final ByteString value, final String field) {
        try {
            return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT).decode(value.asByteBuffer()).toString();
        } catch (final CharacterCodingException e) {
            throw new ProtocolException(field + " is invalid UTF-8; close code 1007 is required", e);
        }
    }

    /**
     * @return one unsigned byte
     */
    private int readByte() {
        requireBytes(Byte.BYTES);
        return input.readByte() & Builder.UNSIGNED_BYTE_MASK;
    }

    /**
     * @return one unsigned network-order short
     */
    private int readUnsignedShort() {
        requireBytes(Short.BYTES);
        return Short.toUnsignedInt(input.readShort());
    }

    /**
     * @return one network-order long
     */
    private long readLong() {
        requireBytes(Long.BYTES);
        return input.readLong();
    }

    /**
     * Reads an exact byte array.
     *
     * @param length byte count
     * @return bytes
     */
    private byte[] readBytes(final int length) {
        requireBytes(length);
        try {
            return input.readByteArray(length);
        } catch (final IOException e) {
            throw new SocketException("Unable to read WebSocket frame bytes", e);
        }
    }

    /**
     * Reads an exact payload buffer.
     *
     * @param length byte count
     * @return payload buffer
     */
    private Buffer readBuffer(final int length) {
        requireBytes(length);
        final Buffer payload = new Buffer();
        payload.write(input, length);
        return payload;
    }

    /**
     * Ensures the reusable input contains an exact byte count.
     *
     * @param byteCount required byte count
     */
    private void requireBytes(final long byteCount) {
        try {
            while (input.size() < byteCount) {
                final long read = source.read(input, byteCount - input.size());
                if (read < Normal.LONG_ZERO) {
                    throw new SocketException("Unexpected EOF inside WebSocket frame");
                }
            }
        } catch (final IOException e) {
            throw new SocketException("Unable to read WebSocket frame", e);
        }
    }

    /**
     * Applies the four-byte WebSocket mask in place.
     *
     * @param payload payload buffer
     * @param mask    mask key
     * @param length  payload length
     */
    private static void unmask(final Buffer payload, final byte[] mask, final long length) {
        if (length == Normal.LONG_ZERO) {
            return;
        }
        final Buffer.UnsafeCursor cursor = new Buffer.UnsafeCursor();
        payload.readAndWriteUnsafe(cursor);
        try {
            long processed = Normal.LONG_ZERO;
            int available = cursor.seek(Normal.LONG_ZERO);
            while (available != Normal.__1 && processed < length) {
                for (int index = cursor.start; index < cursor.end && processed < length; index++) {
                    cursor.data[index] = (byte) (cursor.data[index] ^ mask[(int) (processed & Normal._3)]);
                    processed++;
                }
                if (processed < length) {
                    available = cursor.next();
                }
            }
        } finally {
            cursor.close();
        }
    }

    /**
     * Validates a required reference.
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
