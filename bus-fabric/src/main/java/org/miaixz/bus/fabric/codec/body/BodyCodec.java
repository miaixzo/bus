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

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.io.file.PathResolve;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ConvertException;
import org.miaixz.bus.core.lang.exception.IllegalPathException;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Payload;

/**
 * Default body codec for adapting payloads, streams, bytes, text, and files.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class BodyCodec {

    /**
     * Creates a body codec.
     */
    private BodyCodec() {
        // No initialization required.
    }

    /**
     * Returns the shared body codec.
     *
     * @return process-wide stateless body codec
     */
    public static BodyCodec create() {
        return Instances.get(BodyCodec.class.getName(), BodyCodec::new);
    }

    /**
     * Adapts a payload into a stream source.
     *
     * @param payload payload whose source is opened
     * @return source opened from the validated payload
     * @throws ValidateException if {@code payload} is {@code null}
     */
    public Source source(final Payload payload) {
        return validatePayload(payload).source();
    }

    /**
     * Creates a repeatable text payload.
     *
     * @param value   non-null text value
     * @param charset charset used to encode the text
     * @return repeatable encoded payload
     * @throws ValidateException if {@code value} is {@code null}
     * @throws ConvertException  if charset validation or text encoding fails
     */
    public Payload text(final String value, final Charset charset) {
        final String checkedValue = Assert.notNull(value, () -> new ValidateException("Text value must not be null"));
        try {
            return Payload.of(checkedValue, validateCharset(charset));
        } catch (final RuntimeException e) {
            throw new ConvertException("Unable to encode text payload", e);
        }
    }

    /**
     * Creates a repeatable byte payload.
     *
     * @param value non-null byte array copied into an immutable payload
     * @return repeatable byte payload
     * @throws ValidateException if {@code value} is {@code null}
     */
    public Payload bytes(final byte[] value) {
        return Payload.of(Assert.notNull(value, () -> new ValidateException("Byte value must not be null")));
    }

    /**
     * Creates a repeatable file payload that captures the current file length and opens sources lazily.
     * <p>
     * The media type is validated for API consistency but is not retained by the returned protocol-neutral payload.
     * </p>
     *
     * @param path  path that must currently identify a regular file
     * @param media non-null media metadata validated but not stored by the payload
     * @return repeatable payload backed by the path and captured length
     * @throws ValidateException    if {@code path} or {@code media} is {@code null}
     * @throws IllegalPathException if the path does not identify a regular file
     * @throws InternalException    if the file length cannot be read
     */
    public Payload file(final Path path, final MediaType media) {
        final Path checkedPath = Assert.notNull(path, () -> new ValidateException("File path must not be null"));
        Assert.notNull(media, () -> new ValidateException("File media must not be null"));
        if (!PathResolve.isFile(checkedPath, true)) {
            throw new IllegalPathException("File path must point to a regular file: " + checkedPath);
        }
        try {
            return new FilePayload(checkedPath, PathResolve.size(checkedPath));
        } catch (final InternalException e) {
            throw new InternalException("Unable to read file payload length", e);
        }
    }

    /**
     * Validates a payload.
     *
     * @param payload payload reference to validate
     * @return validated payload
     */
    private static Payload validatePayload(final Payload payload) {
        return Assert.notNull(payload, () -> new ValidateException("Payload must not be null"));
    }

    /**
     * Validates a charset.
     *
     * @param charset charset reference to validate
     * @return validated charset
     */
    private static Charset validateCharset(final Charset charset) {
        return Assert.notNull(charset, () -> new ValidateException("Charset must not be null"));
    }

    /**
     * Repeatable payload backed by a file path and a length captured at construction time.
     */
    private static final class FilePayload implements Payload {

        /**
         * Path opened anew for each source request.
         */
        private final Path path;

        /**
         * File length captured when the payload was created.
         */
        private final long length;

        /**
         * Creates a file payload.
         *
         * @param path   path opened for each read
         * @param length captured file length reported by this payload
         */
        private FilePayload(final Path path, final long length) {
            this.path = path;
            this.length = length;
        }

        /**
         * Returns the saved file length.
         *
         * @return file length captured at payload creation
         */
        @Override
        public long length() {
            return length;
        }

        /**
         * Opens a new file source.
         *
         * @return newly opened unbuffered source for the current path contents
         * @throws InternalException if the file cannot be opened
         */
        @Override
        public Source source() {
            try {
                return IoKit.source(path);
            } catch (final IOException e) {
                throw new InternalException("Unable to open file payload source", e);
            }
        }

        /**
         * Reads all file bytes.
         *
         * @return file bytes materialized with the default threshold
         * @throws InternalException if materialization exceeds limits or reading fails
         */
        @Override
        public byte[] bytes() {
            return bytes(Builder.DEFAULT_MATERIALIZE_MAX_BYTES);
        }

        /**
         * Reads all file bytes with an explicit materialize threshold.
         *
         * @param maxBytes positive maximum number of bytes to retain in memory
         * @return newly materialized file bytes
         * @throws ValidateException if {@code maxBytes} is not positive
         * @throws InternalException if materialization exceeds limits, current bytes exceed the captured length, or
         *                           reading fails
         */
        @Override
        public byte[] bytes(final long maxBytes) {
            return Payload.materialize(this, maxBytes, "BodyCodec.FilePayload.bytes(long)");
        }

        /**
         * Reads file text.
         *
         * @param charset non-null charset used to decode file bytes
         * @return file text materialized with the default threshold
         * @throws ValidateException if {@code charset} is {@code null}
         * @throws InternalException if byte materialization fails
         */
        @Override
        public String text(final Charset charset) {
            return text(charset, Builder.DEFAULT_MATERIALIZE_MAX_BYTES);
        }

        /**
         * Reads file text with an explicit materialize threshold.
         *
         * @param charset  non-null charset used to decode file bytes
         * @param maxBytes positive maximum number of bytes to retain in memory
         * @return text decoded from threshold-limited file bytes
         * @throws ValidateException if {@code charset} is {@code null} or {@code maxBytes} is not positive
         * @throws InternalException if byte materialization fails
         */
        @Override
        public String text(final Charset charset, final long maxBytes) {
            return new String(bytes(maxBytes), validateCharset(charset));
        }

        /**
         * Returns whether this payload is repeatable.
         *
         * @return {@code true} because each source request reopens the path
         */
        @Override
        public boolean repeatable() {
            return true;
        }

    }

}
