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
package org.miaixz.bus.fabric.codec.frame;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.miaixz.bus.core.lang.exception.ConvertException;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Immutable binary frame payload.
 *
 * @param payload read-only payload
 * @param length  payload length
 * @author Kimi Liu
 * @since Java 21+
 */
public record Frame(ByteBuffer payload, int length) {

    /**
     * Creates a frame from a payload snapshot.
     *
     * @param payload payload
     * @param length  expected payload length
     */
    public Frame {
        if (payload == null) {
            throw new ValidateException("Frame payload must not be null");
        }
        final ByteBuffer duplicate = payload.duplicate();
        if (length != duplicate.remaining()) {
            throw new ProtocolException("Frame length does not match payload remaining bytes");
        }
        if (length < 0) {
            throw new ProtocolException("Frame length must be non-negative");
        }
        final byte[] data = new byte[length];
        duplicate.get(data);
        payload = ByteBuffer.wrap(data).asReadOnlyBuffer();
    }

    /**
     * Creates a frame from the remaining bytes of a buffer.
     *
     * @param payload payload buffer
     * @return frame
     */
    public static Frame of(final ByteBuffer payload) {
        if (payload == null) {
            throw new ValidateException("Frame payload must not be null");
        }
        return new Frame(payload, payload.remaining());
    }

    /**
     * Creates a text frame.
     *
     * @param value   text value
     * @param charset charset
     * @return frame
     */
    public static Frame text(final String value, final Charset charset) {
        if (value == null) {
            throw new ValidateException("Frame text must not be null");
        }
        validateCharset(charset);
        try {
            return of(ByteBuffer.wrap(value.getBytes(charset)));
        } catch (final RuntimeException e) {
            if (e instanceof ValidateException || e instanceof ProtocolException) {
                throw e;
            }
            throw new ConvertException("Unable to encode frame text", e);
        }
    }

    /**
     * Returns a read-only payload duplicate.
     *
     * @return payload duplicate
     */
    @Override
    public ByteBuffer payload() {
        return payload.asReadOnlyBuffer();
    }

    /**
     * Reads frame text.
     *
     * @param charset charset
     * @return text
     */
    public String text(final Charset charset) {
        validateCharset(charset);
        try {
            final ByteBuffer duplicate = payload();
            final byte[] data = new byte[duplicate.remaining()];
            duplicate.get(data);
            return new String(data, charset);
        } catch (final RuntimeException e) {
            if (e instanceof ValidateException) {
                throw e;
            }
            throw new ConvertException("Unable to decode frame text", e);
        }
    }

    /**
     * Validates a charset.
     *
     * @param charset charset
     */
    private static void validateCharset(final Charset charset) {
        if (charset == null) {
            throw new ValidateException("Charset must not be null");
        }
    }

}
