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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.ArrayKit;

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
        return Instances.get(Payload.class.getName() + ".empty", () -> Payload.of(new byte[0]));
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
        final byte[] snapshot = ArrayKit.clone(bytes);
        return new Payload() {

            @Override
            public long length() {
                return snapshot.length;
            }

            @Override
            public InputStream stream() {
                return new ByteArrayInputStream(snapshot);
            }

            @Override
            public byte[] bytes() {
                return bytes(Options.DEFAULT_MATERIALIZE_MAX_BYTES);
            }

            @Override
            public byte[] bytes(final long maxBytes) {
                validateMaterializeMaxBytes(maxBytes);
                if (snapshot.length > maxBytes) {
                    throw materializeExceeded(snapshot.length, maxBytes, "Payload.bytes(long)");
                }
                return ArrayKit.clone(snapshot);
            }

            @Override
            public String text(final Charset charset) {
                return text(charset, Options.DEFAULT_MATERIALIZE_MAX_BYTES);
            }

            @Override
            public String text(final Charset charset, final long maxBytes) {
                validateCharset(charset);
                return new String(bytes(maxBytes), charset);
            }

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
        return of(text.getBytes(charset));
    }

    /**
     * Creates a one-shot streaming payload.
     *
     * @param input  input stream
     * @param length declared length, or -1 when unknown
     * @return stream payload
     */
    static Payload stream(final InputStream input, final long length) {
        if (input == null) {
            throw new ValidateException("Payload stream must not be null");
        }
        if (length < -1) {
            throw new ValidateException("Payload length must be -1 or greater");
        }
        final AtomicBoolean opened = new AtomicBoolean();
        return new Payload() {

            @Override
            public long length() {
                return length;
            }

            @Override
            public InputStream stream() {
                if (!opened.compareAndSet(false, true)) {
                    throw new StatefulException("Streaming payload can only be opened once");
                }
                return input;
            }

            @Override
            public byte[] bytes() {
                return bytes(Options.DEFAULT_MATERIALIZE_MAX_BYTES);
            }

            @Override
            public byte[] bytes(final long maxBytes) {
                return materialize(this, maxBytes, "Payload.bytes(long)");
            }

            @Override
            public String text(final Charset charset) {
                return text(charset, Options.DEFAULT_MATERIALIZE_MAX_BYTES);
            }

            @Override
            public String text(final Charset charset, final long maxBytes) {
                validateCharset(charset);
                return new String(bytes(maxBytes), charset);
            }

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
     * Opens the payload stream.
     *
     * @return payload stream
     */
    InputStream stream();

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
        final int initial = length >= 0 ? (int) length : 8192;
        try (InputStream input = payload.stream(); ByteArrayOutputStream output = new ByteArrayOutputStream(initial)) {
            final byte[] buffer = new byte[8192];
            long total = 0;
            while (true) {
                final int read = input.read(buffer);
                if (read < 0) {
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
                output.write(buffer, 0, read);
            }
            if (length >= 0 && total > length) {
                throw new InternalException("Streaming payload exceeded declared length at " + source);
            }
            return output.toByteArray();
        } catch (final IOException e) {
            throw new InternalException("Unable to read payload at " + source, e);
        }
    }

    /**
     * Copies a payload stream to an output stream without materializing it.
     *
     * @param payload payload
     * @param output  output stream
     * @return copied byte count
     */
    static long copyTo(final Payload payload, final OutputStream output) {
        if (payload == null) {
            throw new ValidateException("Payload must not be null");
        }
        if (output == null) {
            throw new ValidateException("Output stream must not be null");
        }
        final long length = payload.length();
        final byte[] buffer = new byte[8192];
        long total = 0L;
        try (InputStream input = payload.stream()) {
            int read;
            while ((read = input.read(buffer)) != -1) {
                total += read;
                if (length >= 0 && total > length) {
                    throw new InternalException("Streaming payload exceeded declared length at Payload.copyTo");
                }
                output.write(buffer, 0, read);
            }
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
