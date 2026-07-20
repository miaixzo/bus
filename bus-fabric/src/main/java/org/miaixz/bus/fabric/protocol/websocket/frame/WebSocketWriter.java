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
import java.security.SecureRandom;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Builder;

/**
 * Encodes and completely writes one WebSocket frame.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class WebSocketWriter {

    /**
     * Output sink borrowed from the owning WebSocket session.
     */
    private final Sink output;

    /**
     * True when client frames require a mask key.
     */
    private final boolean mask;

    /**
     * Cryptographically secure mask-key source.
     */
    private final SecureRandom random;

    /**
     * Creates a frame writer.
     *
     * @param output output sink
     * @param mask   true for a client writer, false for a server writer
     */
    public WebSocketWriter(final Sink output, final boolean mask) {
        this(output, mask, new SecureRandom());
    }

    /**
     * Creates a frame writer with an explicit secure random source.
     *
     * @param output output sink
     * @param mask   mask direction
     * @param random secure mask-key source
     */
    WebSocketWriter(final Sink output, final boolean mask, final SecureRandom random) {
        this.output = require(output, "WebSocket output");
        this.mask = mask;
        this.random = require(random, "WebSocket secure random");
    }

    /**
     * Encodes one frame and writes its complete wire representation without flushing the sink.
     *
     * @param frame frame
     * @return complete wire byte count including header, mask key and payload
     */
    public synchronized long write(final WebSocketFrame frame) {
        final WebSocketFrame checked = require(frame, "WebSocket frame");
        final ByteString payload = checked.payload();
        final long length = payload.size();
        if (length > Builder.BYTES_16_MIB) {
            throw new ProtocolException("WebSocket frame exceeds the 16 MiB limit");
        }
        final Buffer encoded = new Buffer();
        encoded.writeByte((checked.fin() ? Normal._128 : Normal._0) | checked.opcode());
        writeLength(encoded, length);
        if (mask) {
            final byte[] key = new byte[Normal._4];
            random.nextBytes(key);
            encoded.write(key);
            encoded.write(mask(payload, key));
        } else {
            encoded.write(payload);
        }
        final long wireBytes = encoded.size();
        try {
            output.write(encoded, wireBytes);
            return wireBytes;
        } catch (final IOException e) {
            throw new SocketException("Unable to write complete WebSocket frame", e);
        }
    }

    /**
     * Writes the canonical payload-length field.
     *
     * @param target target buffer
     * @param length payload length
     */
    private void writeLength(final Buffer target, final long length) {
        final int maskBit = mask ? Normal._128 : Normal._0;
        if (length <= Builder._125) {
            target.writeByte(maskBit | (int) length);
            return;
        }
        if (length <= Normal._65535) {
            target.writeByte(maskBit | Builder.WEBSOCKET_LENGTH_16_MARKER);
            target.writeByte((int) (length >>> Byte.SIZE) & Builder.UNSIGNED_BYTE_MASK);
            target.writeByte((int) length & Builder.UNSIGNED_BYTE_MASK);
            return;
        }
        target.writeByte(maskBit | Builder._127);
        for (int shift = Normal._56; shift >= Normal._0; shift -= Byte.SIZE) {
            target.writeByte((int) (length >>> shift) & Builder.UNSIGNED_BYTE_MASK);
        }
    }

    /**
     * Returns a masked payload snapshot.
     *
     * @param payload payload
     * @param key     four-byte mask key
     * @return masked bytes
     */
    private static byte[] mask(final ByteString payload, final byte[] key) {
        final byte[] bytes = payload.toByteArray();
        for (int index = Normal._0; index < bytes.length; index++) {
            bytes[index] = (byte) (bytes[index] ^ key[index & Normal._3]);
        }
        return bytes;
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
