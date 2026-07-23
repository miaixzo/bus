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

import java.nio.charset.Charset;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ConvertException;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Immutable binary frame payload.
 *
 * @param payload immutable snapshot of the frame bytes
 * @param length  byte count, always equal to {@code payload.size()}
 * @author Kimi Liu
 * @since Java 21+
 */
public record Frame(ByteString payload, int length) {

    /**
     * Creates a frame from a payload snapshot.
     *
     * @param payload source bytes copied into an immutable snapshot
     * @param length  declared byte count, which must match the source payload
     * @throws ValidateException if {@code payload} is {@code null}
     * @throws ProtocolException if {@code length} is negative or differs from the payload size
     */
    public Frame {
        final ByteString checkedPayload = Assert
                .notNull(payload, () -> new ValidateException("Frame payload must not be null"));
        Assert.isTrue(length >= 0, () -> new ProtocolException("Frame length must be non-negative"));
        Assert.isTrue(
                length == checkedPayload.size(),
                () -> new ProtocolException("Frame length does not match payload bytes"));
        payload = ByteString.of(checkedPayload.asByteBuffer());
    }

    /**
     * Creates a frame from immutable bytes.
     *
     * @param payload source bytes copied into the frame snapshot
     * @return frame whose declared length is derived from the payload size
     * @throws ValidateException if {@code payload} is {@code null}
     */
    public static Frame of(final ByteString payload) {
        final ByteString checkedPayload = Assert
                .notNull(payload, () -> new ValidateException("Frame payload must not be null"));
        return new Frame(checkedPayload, checkedPayload.size());
    }

    /**
     * Creates a text frame.
     *
     * @param value   non-null text to encode
     * @param charset charset used to encode the text
     * @return frame containing the encoded text bytes
     * @throws ValidateException if {@code value} or {@code charset} is {@code null}
     * @throws ConvertException  if the text cannot be encoded
     */
    public static Frame text(final String value, final Charset charset) {
        final String checkedValue = Assert.notNull(value, () -> new ValidateException("Frame text must not be null"));
        try {
            return of(ByteString.encodeString(checkedValue, validateCharset(charset)));
        } catch (final RuntimeException e) {
            if (e instanceof ValidateException || e instanceof ProtocolException) {
                throw e;
            }
            throw new ConvertException("Unable to encode frame text", e);
        }
    }

    /**
     * Decodes the frame payload as text using the supplied charset.
     *
     * @param charset charset used to decode the payload
     * @return decoded text
     * @throws ValidateException if {@code charset} is {@code null}
     * @throws ConvertException  if the payload cannot be decoded
     */
    public String text(final Charset charset) {
        try {
            return payload.string(validateCharset(charset));
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
     * @param charset charset reference to validate
     * @return validated non-null charset
     * @throws ValidateException if {@code charset} is {@code null}
     */
    private static Charset validateCharset(final Charset charset) {
        return Assert.notNull(charset, () -> new ValidateException("Charset must not be null"));
    }

}
