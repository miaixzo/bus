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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.exception.ConvertException;
import org.miaixz.bus.core.lang.exception.IllegalPathException;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.codec.stream.StreamSink;
import org.miaixz.bus.fabric.codec.stream.StreamSource;

/**
 * Default body codec for adapting payloads, streams, bytes, text, and files.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class BodyCodec {

    /**
     * Default buffer size for stream copying.
     */
    private static final int BUFFER_SIZE = 8192;

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
     * @return stream source
     */
    public StreamSource source(final Payload payload) {
        validatePayload(payload);
        return new PayloadSource(payload);
    }

    /**
     * Adapts an output stream into a stream sink.
     *
     * @param output output stream
     * @return stream sink
     */
    public StreamSink sink(final OutputStream output) {
        if (output == null) {
            throw new ValidateException("Output stream must not be null");
        }
        return new OutputSink(output);
    }

    /**
     * Creates a repeatable text payload.
     *
     * @param value   text value
     * @param charset charset
     * @return payload
     */
    public Payload text(final String value, final Charset charset) {
        if (value == null) {
            throw new ValidateException("Text value must not be null");
        }
        validateCharset(charset);
        try {
            return Payload.of(value.getBytes(charset));
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
        if (value == null) {
            throw new ValidateException("Byte value must not be null");
        }
        return Payload.of(value);
    }

    /**
     * Creates a repeatable file payload that opens streams lazily.
     *
     * @param path  file path
     * @param media media type
     * @return payload
     */
    public Payload file(final Path path, final MediaType media) {
        if (path == null) {
            throw new ValidateException("File path must not be null");
        }
        if (media == null) {
            throw new ValidateException("File media must not be null");
        }
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new IllegalPathException("File path must point to a regular file: " + path);
        }
        try {
            return new FilePayload(path, Files.size(path));
        } catch (final IOException e) {
            throw new InternalException("Unable to read file payload length", e);
        }
    }

    /**
     * Validates a payload.
     *
     * @param payload payload
     */
    private static void validatePayload(final Payload payload) {
        if (payload == null) {
            throw new ValidateException("Payload must not be null");
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
         * Opens a new file stream.
         *
         * @return file stream
         */
        @Override
        public InputStream stream() {
            try {
                return Files.newInputStream(path);
            } catch (final IOException e) {
                throw new InternalException("Unable to open file payload stream", e);
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
            validateCharset(charset);
            return new String(bytes(maxBytes), charset);
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

    /**
     * Stream source backed by a payload.
     */
    private static final class PayloadSource implements StreamSource {

        /**
         * Payload to read.
         */
        private final Payload payload;

        /**
         * Saved payload length.
         */
        private final long length;

        /**
         * Whether a one-shot payload has been opened.
         */
        private final AtomicBoolean opened = new AtomicBoolean();

        /**
         * Source lifecycle state.
         */
        private final AtomicReference<Status> state = new AtomicReference<>(Status.OPENED);

        /**
         * Current stream used by incremental reads.
         */
        private InputStream current;

        /**
         * Creates a payload source.
         *
         * @param payload payload
         */
        private PayloadSource(final Payload payload) {
            this.payload = payload;
            this.length = payload.length();
            if (length < -1) {
                throw new ValidateException("Payload source length must be -1 or greater");
            }
        }

        /**
         * Returns the payload length.
         *
         * @return payload length
         */
        @Override
        public long length() {
            return length;
        }

        /**
         * Opens a payload stream.
         *
         * @return payload stream
         */
        @Override
        public InputStream stream() {
            ensureOpen();
            if (!payload.repeatable() && !opened.compareAndSet(false, true)) {
                throw new StatefulException("Payload source stream can only be opened once");
            }
            try {
                final InputStream stream = payload.stream();
                current = stream;
                return stream;
            } catch (final RuntimeException e) {
                if (e instanceof InternalException || e instanceof StatefulException
                        || e instanceof ValidateException) {
                    throw e;
                }
                throw new InternalException("Unable to open payload source stream", e);
            }
        }

        /**
         * Reads bytes into the target buffer.
         *
         * @param target target buffer
         * @return read byte count, or -1 at EOF
         */
        @Override
        public int read(final ByteBuffer target) {
            if (target == null) {
                throw new ValidateException("Target buffer must not be null");
            }
            ensureOpen();
            if (!target.hasRemaining()) {
                return 0;
            }
            try {
                final InputStream input = current == null ? stream() : current;
                final byte[] buffer = new byte[Math.min(target.remaining(), BUFFER_SIZE)];
                final int read = input.read(buffer);
                if (read > 0) {
                    target.put(buffer, 0, read);
                }
                return read;
            } catch (final IOException e) {
                throw new InternalException("Unable to read payload source", e);
            } catch (final RuntimeException e) {
                if (e instanceof InternalException || e instanceof StatefulException
                        || e instanceof ValidateException) {
                    throw e;
                }
                throw new InternalException("Unable to read payload source", e);
            }
        }

        /**
         * Reads all payload bytes.
         *
         * @return payload bytes
         */
        @Override
        public byte[] bytes() {
            return bytes(Options.DEFAULT_MATERIALIZE_MAX_BYTES);
        }

        /**
         * Reads all payload bytes with an explicit materialize threshold.
         *
         * @param maxBytes maximum bytes to materialize
         * @return payload bytes
         */
        @Override
        public byte[] bytes(final long maxBytes) {
            ensureOpen();
            return StreamSource.super.bytes(maxBytes);
        }

        /**
         * Reads payload text.
         *
         * @param charset charset
         * @return text value
         */
        @Override
        public String text(final Charset charset) {
            return text(charset, Options.DEFAULT_MATERIALIZE_MAX_BYTES);
        }

        @Override
        public String text(final Charset charset, final long maxBytes) {
            validateCharset(charset);
            try {
                return new String(bytes(maxBytes), charset);
            } catch (final RuntimeException e) {
                if (e instanceof InternalException || e instanceof StatefulException) {
                    throw e;
                }
                throw new ConvertException("Unable to decode payload source text", e);
            }
        }

        /**
         * Closes the current stream.
         */
        @Override
        public void close() {
            if (!state.compareAndSet(Status.OPENED, Status.CLOSING)) {
                return;
            }
            try {
                if (current != null) {
                    current.close();
                }
                state.set(Status.CLOSED);
            } catch (final IOException e) {
                state.set(Status.CLOSED);
                throw new InternalException("Unable to close payload source", e);
            } catch (final RuntimeException e) {
                state.set(Status.CLOSED);
                if (e instanceof InternalException || e instanceof StatefulException) {
                    throw e;
                }
                throw new InternalException("Unable to close payload source", e);
            }
        }

        /**
         * Ensures the source is open.
         */
        private void ensureOpen() {
            if (state.get().terminal()) {
                throw new StatefulException("Payload source is closed");
            }
        }

    }

    /**
     * Stream sink backed by an output stream.
     */
    private static final class OutputSink implements StreamSink {

        /**
         * Output stream.
         */
        private final OutputStream output;

        /**
         * Sink lifecycle state.
         */
        private final AtomicReference<Status> state = new AtomicReference<>(Status.OPENED);

        /**
         * Written byte count.
         */
        private long written;

        /**
         * Creates an output sink.
         *
         * @param output output stream
         */
        private OutputSink(final OutputStream output) {
            this.output = output;
        }

        /**
         * Writes the source buffer.
         *
         * @param source source buffer
         */
        @Override
        public void write(final ByteBuffer source) {
            if (source == null) {
                throw new ValidateException("Source buffer must not be null");
            }
            ensureOpen();
            final byte[] buffer = new byte[Math.min(Math.max(source.remaining(), 1), BUFFER_SIZE)];
            try {
                while (source.hasRemaining()) {
                    final int count = Math.min(source.remaining(), buffer.length);
                    source.get(buffer, 0, count);
                    output.write(buffer, 0, count);
                    written += count;
                }
            } catch (final IOException e) {
                throw new InternalException("Unable to write output buffer", e);
            } catch (final RuntimeException e) {
                if (e instanceof ValidateException || e instanceof StatefulException
                        || e instanceof InternalException) {
                    throw e;
                }
                throw new InternalException("Unable to write output buffer", e);
            }
        }

        /**
         * Writes the payload stream.
         *
         * @param payload payload
         */
        @Override
        public void write(final Payload payload) {
            validatePayload(payload);
            ensureOpen();
            try {
                written += Payload.copyTo(payload, output);
            } catch (final RuntimeException e) {
                if (e instanceof ValidateException || e instanceof StatefulException
                        || e instanceof InternalException) {
                    throw e;
                }
                throw new InternalException("Unable to write output payload", e);
            }
        }

        /**
         * Returns the written byte count.
         *
         * @return written byte count
         */
        @Override
        public long written() {
            return written;
        }

        /**
         * Flushes the output stream.
         */
        @Override
        public void flush() {
            ensureOpen();
            try {
                output.flush();
            } catch (final IOException e) {
                throw new InternalException("Unable to flush output sink", e);
            } catch (final RuntimeException e) {
                if (e instanceof InternalException || e instanceof StatefulException) {
                    throw e;
                }
                throw new InternalException("Unable to flush output sink", e);
            }
        }

        /**
         * Closes the output stream once.
         */
        @Override
        public void close() {
            if (!state.compareAndSet(Status.OPENED, Status.CLOSING)) {
                return;
            }
            try {
                output.close();
                state.set(Status.CLOSED);
            } catch (final IOException e) {
                state.set(Status.CLOSED);
                throw new InternalException("Unable to close output sink", e);
            } catch (final RuntimeException e) {
                state.set(Status.CLOSED);
                if (e instanceof InternalException || e instanceof StatefulException) {
                    throw e;
                }
                throw new InternalException("Unable to close output sink", e);
            }
        }

        /**
         * Ensures the sink is open.
         */
        private void ensureOpen() {
            if (state.get().terminal()) {
                throw new StatefulException("Output sink is closed");
            }
        }

    }

}
