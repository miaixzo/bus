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
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.io.file.PathResolve;
import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ConvertException;
import org.miaixz.bus.core.lang.exception.IllegalPathException;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.fabric.Options;
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
     * @return body codec
     */
    public static BodyCodec create() {
        return Instances.get(BodyCodec.class.getName(), BodyCodec::new);
    }

    /**
     * Adapts a payload into a stream source.
     *
     * @param payload payload
     * @return source
     */
    public Source source(final Payload payload) {
        return validatePayload(payload).source();
    }

    /**
     * Adapts an output stream into a stream sink.
     *
     * @param output output stream
     * @return sink
     * @deprecated use {@link org.miaixz.bus.core.xyz.IoKit#sink(OutputStream)}
     */
    @Deprecated(since = "8.8.3")
    public Sink sink(final OutputStream output) {
        return IoKit.sink(Assert.notNull(output, () -> new ValidateException("Output stream must not be null")));
    }

    /**
     * Creates a repeatable text payload.
     *
     * @param value   text value
     * @param charset charset
     * @return payload
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
     * @param value byte value
     * @return payload
     */
    public Payload bytes(final byte[] value) {
        return Payload.of(Assert.notNull(value, () -> new ValidateException("Byte value must not be null")));
    }

    /**
     * Creates a repeatable file payload that opens streams lazily.
     *
     * @param path  file path
     * @param media media type
     * @return payload
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
     * @param payload payload
     */
    private static Payload validatePayload(final Payload payload) {
        return Assert.notNull(payload, () -> new ValidateException("Payload must not be null"));
    }

    /**
     * Validates a charset.
     *
     * @param charset charset
     */
    private static Charset validateCharset(final Charset charset) {
        return Assert.notNull(charset, () -> new ValidateException("Charset must not be null"));
    }

    /**
     * Repeatable payload backed by a file path.
     */
    private static final class FilePayload implements Payload {

        /**
         * File path.
         */
        private final Path path;

        /**
         * Saved file length.
         */
        private final long length;

        /**
         * Creates a file payload.
         *
         * @param path   file path
         * @param length file length
         */
        private FilePayload(final Path path, final long length) {
            this.path = path;
            this.length = length;
        }

        /**
         * Returns the saved file length.
         *
         * @return file length
         */
        @Override
        public long length() {
            return length;
        }

        /**
         * Opens a new file source.
         *
         * @return file source
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
         * @return file bytes
         */
        @Override
        public byte[] bytes() {
            return bytes(Options.DEFAULT_MATERIALIZE_MAX_BYTES);
        }

        /**
         * Reads all file bytes with an explicit materialize threshold.
         *
         * @param maxBytes maximum bytes to materialize
         * @return file bytes
         */
        @Override
        public byte[] bytes(final long maxBytes) {
            return Payload.materialize(this, maxBytes, "BodyCodec.FilePayload.bytes(long)");
        }

        /**
         * Reads file text.
         *
         * @param charset charset
         * @return file text
         */
        @Override
        public String text(final Charset charset) {
            return text(charset, Options.DEFAULT_MATERIALIZE_MAX_BYTES);
        }

        @Override
        public String text(final Charset charset, final long maxBytes) {
            return new String(bytes(maxBytes), validateCharset(charset));
        }

        /**
         * Returns whether this payload is repeatable.
         *
         * @return true
         */
        @Override
        public boolean repeatable() {
            return true;
        }

    }

}
