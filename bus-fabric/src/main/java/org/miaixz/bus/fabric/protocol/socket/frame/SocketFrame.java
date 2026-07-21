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
package org.miaixz.bus.fabric.protocol.socket.frame;

import java.nio.charset.Charset;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ConvertException;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Builder;

/**
 * Immutable socket frame payload.
 *
 * @param payload immutable payload
 * @param length  payload length
 * @author Kimi Liu
 * @since Java 21+
 */
public record SocketFrame(ByteString payload, int length) {

    /**
     * Creates a frame.
     *
     * @param payload payload
     * @param length  length
     */
    public SocketFrame {
        final ByteString checkedPayload = Assert
                .notNull(payload, () -> new ValidateException("Socket frame payload must not be null"));
        Assert.isTrue(
                length >= Normal._0 && length <= Builder.BYTES_16_MIB,
                () -> new ProtocolException("Socket frame length exceeds maximum"));
        Assert.isTrue(
                length == checkedPayload.size(),
                () -> new ProtocolException("Socket frame length does not match payload"));
        payload = ByteString.of(checkedPayload.asByteBuffer());
    }

    /**
     * Creates a frame from immutable bytes.
     *
     * @param payload payload bytes
     * @return frame
     */
    public static SocketFrame of(final ByteString payload) {
        final ByteString checkedPayload = Assert
                .notNull(payload, () -> new ValidateException("Socket frame payload must not be null"));
        return new SocketFrame(checkedPayload, checkedPayload.size());
    }

    /**
     * Creates a text frame.
     *
     * @param value   text
     * @param charset charset
     * @return frame
     */
    public static SocketFrame text(final String value, final Charset charset) {
        final String checkedValue = Assert
                .notNull(value, () -> new ValidateException("Socket frame text must not be null"));
        final Charset checkedCharset = Assert.notNull(charset, () -> new ValidateException("Charset must not be null"));
        try {
            return of(ByteString.encodeString(checkedValue, checkedCharset));
        } catch (final RuntimeException e) {
            if (e instanceof ValidateException || e instanceof ProtocolException) {
                throw e;
            }
            throw new ConvertException("Unable to encode socket frame text", e);
        }
    }

    /**
     * Returns payload length.
     *
     * @return length
     */
    @Override
    public int length() {
        return length;
    }

}
