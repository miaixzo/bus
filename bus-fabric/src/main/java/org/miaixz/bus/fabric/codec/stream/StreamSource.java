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
package org.miaixz.bus.fabric.codec.stream;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Payload;

/**
 * Stream source contract for incremental reads and materialization.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface StreamSource extends AutoCloseable {

    /**
     * Returns the source length.
     *
     * @return source length, or -1 when unknown
     */
    long length();

    /**
     * Opens the source stream.
     *
     * @return source stream
     */
    InputStream stream();

    /**
     * Reads bytes into the target buffer.
     *
     * @param target target buffer
     * @return read byte count or -1 at EOF
     */
    int read(ByteBuffer target);

    /**
     * Reads all source bytes.
     *
     * @return source bytes
     */
    byte[] bytes();

    /**
     * Reads all source bytes with an explicit materialize threshold.
     *
     * @param maxBytes maximum bytes to materialize
     * @return source bytes
     */
    default byte[] bytes(final long maxBytes) {
        return Payload.materialize(Payload.stream(stream(), length()), maxBytes, "StreamSource.bytes(long)");
    }

    /**
     * Reads the source as text.
     *
     * @param charset charset
     * @return source text
     */
    String text(Charset charset);

    /**
     * Reads the source as text with an explicit materialize threshold.
     *
     * @param charset  charset
     * @param maxBytes maximum bytes to materialize
     * @return source text
     */
    default String text(final Charset charset, final long maxBytes) {
        if (charset == null) {
            throw new ValidateException("Charset must not be null");
        }
        return new String(bytes(maxBytes), charset);
    }

    /**
     * Closes the source.
     */
    @Override
    void close();

}
