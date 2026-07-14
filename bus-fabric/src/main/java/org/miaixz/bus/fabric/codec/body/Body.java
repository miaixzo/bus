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

import java.io.InputStream;
import java.nio.charset.Charset;

import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.fabric.Options;
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
     * @return payload
     */
    Payload payload();

    /**
     * Returns the body media type.
     *
     * @return media type
     */
    MediaType media();

    /**
     * Returns the body length.
     *
     * @return body length, or -1 when unknown
     */
    default long length() {
        return payload().length();
    }

    /**
     * Returns whether this body can be read more than once.
     *
     * @return true when repeatable
     */
    default boolean repeatable() {
        return payload().repeatable();
    }

    /**
     * Opens the body source.
     *
     * @return source
     */
    default Source source() {
        return payload().source();
    }

    /**
     * Opens the compatibility body stream.
     *
     * @return input stream
     * @deprecated use {@link #source()}
     */
    @Deprecated(since = "8.8.3")
    default InputStream stream() {
        return IoKit.buffer(source()).inputStream();
    }

    /**
     * Reads all body bytes.
     *
     * @return body bytes
     */
    default byte[] bytes() {
        return Payload.materialize(payload(), Options.DEFAULT_MATERIALIZE_MAX_BYTES, "Body.bytes()");
    }

    /**
     * Reads all body bytes with an explicit materialize threshold.
     *
     * @param maxBytes maximum bytes to materialize
     * @return body bytes
     */
    default byte[] bytes(final long maxBytes) {
        return Payload.materialize(payload(), maxBytes, "Body.bytes(long)");
    }

    /**
     * Reads the body as text.
     *
     * @param charset charset
     * @return body text
     */
    default String text(final Charset charset) {
        return new String(bytes(), Assert.notNull(charset, () -> new ValidateException("Charset must not be null")));
    }

    /**
     * Reads the body as text with an explicit materialize threshold.
     *
     * @param charset  charset
     * @param maxBytes maximum bytes to materialize
     * @return body text
     */
    default String text(final Charset charset, final long maxBytes) {
        return new String(bytes(maxBytes),
                Assert.notNull(charset, () -> new ValidateException("Charset must not be null")));
    }

    /**
     * Closes the body when the underlying payload is closeable.
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
