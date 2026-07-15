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
package org.miaixz.bus.fabric;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Payload abstraction that keeps byte-array payloads repeatable and stream payloads one-shot.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Payload {

    /**
     * Returns an empty repeatable payload.
     *
     * @return empty payload
     */
    static Payload empty() {
        return Instances.get(Payload.class.getName() + ".empty", () -> Payload.of(Normal.EMPTY_BYTE_ARRAY));
    }

    /**
     * Creates a repeatable payload from bytes.
     *
     * @param bytes bytes
     * @return byte-array payload
     */
    static Payload of(final byte[] bytes) {
        if (bytes == null) {
            throw new ValidateException("Payload bytes must not be null");
        }
        return repeatable(ByteString.of(bytes));
    }

    /**
     * Creates a repeatable payload from immutable bytes.
     *
     * @param bytes bytes
     * @return byte-string payload
     */
    static Payload of(final ByteString bytes) {
        if (bytes == null) {
            throw new ValidateException("Payload bytes must not be null");
        }
        return repeatable(ByteString.of(bytes.toByteArray()));
    }

    /**
     * Creates a repeatable payload from a trusted byte snapshot.
     *
     * @param snapshot byte snapshot
     * @return repeatable payload
     */
    private static Payload repeatable(final ByteString snapshot) {
        return new Payload() {

            /**
             * Returns the repeatable snapshot length.
             *
             * @return snapshot length
             */
            @Override
            public long length() {
                return snapshot.size();
            }

            /**
             * Opens a new source over the repeatable snapshot.
             *
             * @return repeatable snapshot source
             */
            @Override
            public Source source() {
                return new Buffer().write(snapshot);
            }

            /**
             * Returns the repeatable snapshot bytes using the default threshold.
             *
             * @return snapshot bytes
             */
            @Override
            public byte[] bytes() {
                return bytes(Options.DEFAULT_MATERIALIZE_MAX_BYTES);
            }

            /**
             * Returns the repeatable snapshot bytes using an explicit threshold.
             *
             * @param maxBytes maximum bytes to materialize
             * @return snapshot bytes
             */
            @Override
            public byte[] bytes(final long maxBytes) {
                validateMaterializeMaxBytes(maxBytes);
                if (snapshot.size() > maxBytes) {
                    throw materializeExceeded(snapshot.size(), maxBytes, "Payload.bytes(long)");
                }
                return snapshot.toByteArray();
            }

            /**
             * Decodes the repeatable snapshot using the default threshold.
             *
             * @param charset charset
             * @return decoded text
             */
            @Override
            public String text(final Charset charset) {
                return text(charset, Options.DEFAULT_MATERIALIZE_MAX_BYTES);
            }

            /**
             * Decodes the repeatable snapshot using an explicit threshold.
             *
             * @param charset  charset
             * @param maxBytes maximum bytes to materialize
             * @return decoded text
             */
            @Override
            public String text(final Charset charset, final long maxBytes) {
                validateCharset(charset);
                return new String(bytes(maxBytes), charset);
            }

            /**
             * Returns true because byte snapshots can be read repeatedly.
             *
             * @return true
             */
            @Override
            public boolean repeatable() {
                return true;
            }
        };
    }

    /**
     * Creates a repeatable payload from text.
     *
     * @param text    text
     * @param charset charset
     * @return text payload
     */
    static Payload of(final String text, final Charset charset) {
        if (text == null) {
            throw new ValidateException("Payload text must not be null");
        }
        validateCharset(charset);
        return of(ByteString.encodeString(text, charset));
    }

    /**
     * Creates a one-shot source payload.
     *
     * @param input  source
     * @param length declared length, or -1 when unknown
     * @return source payload
     */
    static Payload source(final Source input, final long length) {
        if (input == null) {
            throw new ValidateException("Payload source must not be null");
        }
        if (length < -1) {
            throw new ValidateException("Payload length must be -1 or greater");
        }
        final AtomicBoolean opened = new AtomicBoolean();
        return new Payload() {

            /**
             * Returns the declared one-shot stream length.
             *
             * @return declared length, or -1 when unknown
             */
            @Override
            public long length() {
                return length;
            }

            /**
             * Opens the one-shot source exactly once.
             *
             * @return one-shot source
             */
            @Override
            public Source source() {
                if (!opened.compareAndSet(false, true)) {
                    throw new StatefulException("Streaming payload can only be opened once");
                }
                return input;
            }

            /**
             * Materializes this one-shot stream using the default threshold.
             *
             * @return materialized bytes
             */
            @Override
            public byte[] bytes() {
                return bytes(Options.DEFAULT_MATERIALIZE_MAX_BYTES);
            }

            /**
             * Materializes this one-shot stream using an explicit threshold.
             *
             * @param maxBytes maximum bytes to materialize
             * @return materialized bytes
             */
            @Override
            public byte[] bytes(final long maxBytes) {
                return materialize(this, maxBytes, "Payload.bytes(long)");
            }

            /**
             * Decodes this one-shot stream using the default threshold.
             *
             * @param charset charset
             * @return decoded text
             */
            @Override
            public String text(final Charset charset) {
                return text(charset, Options.DEFAULT_MATERIALIZE_MAX_BYTES);
            }

            /**
             * Decodes this one-shot stream using an explicit threshold.
             *
             * @param charset  charset
             * @param maxBytes maximum bytes to materialize
             * @return decoded text
             */
            @Override
            public String text(final Charset charset, final long maxBytes) {
                validateCharset(charset);
                return new String(bytes(maxBytes), charset);
            }

            /**
             * Returns false because the source can be opened only once.
             *
             * @return false
             */
            @Override
            public boolean repeatable() {
                return false;
            }
        };
    }

    /**
     * Returns the payload length.
     *
     * @return payload length, or -1 when unknown
     */
    long length();

    /**
     * Opens the payload source.
     *
     * @return payload source
     */
    Source source();

    /**
     * Reads all payload bytes.
     *
     * @return payload bytes
     */
    default byte[] bytes() {
        return materialize(this, Options.DEFAULT_MATERIALIZE_MAX_BYTES, "Payload.bytes()");
    }

    /**
     * Reads all payload bytes with an explicit materialize threshold.
     *
     * @param maxBytes maximum bytes to materialize
     * @return payload bytes
     */
    default byte[] bytes(final long maxBytes) {
        return materialize(this, maxBytes, "Payload.bytes(long)");
    }

    /**
     * Reads the payload as text.
     *
     * @param charset charset
     * @return payload text
     */
    default String text(final Charset charset) {
        return text(charset, Options.DEFAULT_MATERIALIZE_MAX_BYTES);
    }

    /**
     * Reads the payload as text with an explicit materialize threshold.
     *
     * @param charset  charset
     * @param maxBytes maximum bytes to materialize
     * @return payload text
     */
    default String text(final Charset charset, final long maxBytes) {
        validateCharset(charset);
        return new String(bytes(maxBytes), charset);
    }

    /**
     * Returns whether this payload can be read more than once.
     *
     * @return true when repeatable
     */
    boolean repeatable();

    /**
     * Reads a payload stream into memory while enforcing a threshold.
     *
     * @param payload  payload to read
     * @param maxBytes maximum bytes to materialize
     * @param entry    entry method name
     * @return materialized bytes
     */
    static byte[] materialize(final Payload payload, final long maxBytes, final String entry) {
        if (payload == null) {
            throw new ValidateException("Payload must not be null");
        }
        validateMaterializeMaxBytes(maxBytes);
        final String source = validateEntry(entry);
        final long length = payload.length();
        if (length > maxBytes) {
            throw materializeExceeded(length, maxBytes, source);
        }
        if (length > Integer.MAX_VALUE) {
            throw new InternalException(
                    "Materialize size " + length + " bytes exceeds JVM byte array limit at " + source);
        }
        try (Source input = payload.source()) {
            final Buffer buffer = new Buffer();
            long total = 0;
            while (true) {
                final long read = input.read(buffer, Normal._8192);
                if (read == -1) {
                    break;
                }
                total += read;
                if (total > maxBytes) {
                    throw materializeExceeded(total, maxBytes, source);
                }
                if (total > Integer.MAX_VALUE) {
                    throw new InternalException(
                            "Materialize size " + total + " bytes exceeds JVM byte array limit at " + source);
                }
            }
            if (length >= 0 && total > length) {
                throw new InternalException("Streaming payload exceeded declared length at " + source);
            }
            return buffer.readByteArray();
        } catch (final IOException e) {
            throw new InternalException("Unable to read payload at " + source, e);
        }
    }

    /**
     * Copies a payload stream to a sink without materializing it.
     *
     * @param payload payload
     * @param sink    sink
     * @return copied byte count
     */
    static long copyTo(final Payload payload, final Sink sink) {
        if (payload == null) {
            throw new ValidateException("Payload must not be null");
        }
        if (sink == null) {
            throw new ValidateException("Sink must not be null");
        }
        final long length = payload.length();
        long total = 0L;
        try (Source input = payload.source()) {
            final Buffer buffer = new Buffer();
            long read;
            while ((read = input.read(buffer, Normal._8192)) != -1) {
                total += read;
                if (length >= 0 && total > length) {
                    throw new InternalException("Streaming payload exceeded declared length at Payload.copyTo");
                }
                sink.write(buffer, read);
            }
            sink.flush();
            return total;
        } catch (final IOException e) {
            throw new InternalException("Unable to copy payload stream", e);
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
     * Validates a materialize threshold.
     *
     * @param maxBytes maximum bytes
     */
    static void validateMaterializeMaxBytes(final long maxBytes) {
        if (maxBytes <= 0) {
            throw new ValidateException("Materialize max bytes must be positive");
        }
    }

    /**
     * Creates a threshold exception.
     *
     * @param size     current size
     * @param maxBytes threshold
     * @param entry    entry method
     * @return exception
     */
    static InternalException materializeExceeded(final long size, final long maxBytes, final String entry) {
        return new InternalException("Materialize size " + size + " bytes exceeds threshold " + maxBytes + " bytes at "
                + validateEntry(entry));
    }

    /**
     * Validates an entry method label.
     *
     * @param entry entry method
     * @return entry method
     */
    private static String validateEntry(final String entry) {
        if (entry == null || entry.isBlank()) {
            return "Payload.materialize";
        }
        return entry;
    }

}
