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
package org.miaixz.bus.fabric.codec.body;

import java.nio.charset.Charset;

import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Payload;

/**
 * Protocol-neutral body contract backed by a payload and media metadata.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Body extends AutoCloseable {

    /**
     * Returns the body payload.
     *
     * @return non-null payload backing this body
     */
    Payload payload();

    /**
     * Returns the body media type.
     *
     * @return media metadata associated with the payload
     */
    MediaType media();

    /**
     * Returns the body length.
     *
     * @return payload length in bytes, or {@code -1} when unknown
     */
    default long length() {
        return payload().length();
    }

    /**
     * Returns whether this body can be read more than once.
     *
     * @return {@code true} when the backing payload can open more than one source
     */
    default boolean repeatable() {
        return payload().repeatable();
    }

    /**
     * Opens the body source.
     *
     * @return source opened by the backing payload
     */
    default Source source() {
        return payload().source();
    }

    /**
     * Materializes all body bytes using {@link Builder#DEFAULT_MATERIALIZE_MAX_BYTES} as the safety threshold.
     *
     * @return newly materialized body bytes
     * @throws InternalException if the payload exceeds the threshold or JVM array limit, violates its declared length,
     *                           or cannot be read
     */
    default byte[] bytes() {
        return Payload.materialize(payload(), Builder.DEFAULT_MATERIALIZE_MAX_BYTES, "Body.bytes()");
    }

    /**
     * Reads all body bytes with an explicit materialize threshold.
     *
     * @param maxBytes positive maximum number of bytes to retain in memory
     * @return newly materialized body bytes
     * @throws ValidateException if {@code maxBytes} is not positive
     * @throws InternalException if the payload exceeds the threshold or JVM array limit, violates its declared length,
     *                           or cannot be read
     */
    default byte[] bytes(final long maxBytes) {
        return Payload.materialize(payload(), maxBytes, "Body.bytes(long)");
    }

    /**
     * Reads the body as text.
     *
     * @param charset non-null charset used to decode materialized bytes
     * @return text decoded after materializing with the default threshold
     * @throws ValidateException if {@code charset} is {@code null}
     * @throws InternalException if byte materialization fails
     */
    default String text(final Charset charset) {
        return new String(bytes(), Assert.notNull(charset, () -> new ValidateException("Charset must not be null")));
    }

    /**
     * Reads the body as text with an explicit materialize threshold.
     *
     * @param charset  non-null charset used to decode materialized bytes
     * @param maxBytes positive maximum number of bytes to retain in memory
     * @return text decoded after threshold-limited materialization
     * @throws ValidateException if {@code charset} is {@code null} or {@code maxBytes} is not positive
     * @throws InternalException if byte materialization fails
     */
    default String text(final Charset charset, final long maxBytes) {
        return new String(bytes(maxBytes),
                Assert.notNull(charset, () -> new ValidateException("Charset must not be null")));
    }

    /**
     * Closes the backing payload when it implements {@link AutoCloseable}; otherwise performs no work.
     *
     * @throws InternalException if the closeable payload reports an exception
     */
    @Override
    default void close() {
        if (payload() instanceof AutoCloseable closeable) {
            try {
                closeable.close();
            } catch (final Exception e) {
                throw new InternalException("Unable to close body payload", e);
            }
        }
    }

}
