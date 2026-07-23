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

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.io.timout.Timeout;
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
        return EmptyHolder.EMPTY;
    }

    /** Lazily initialized holder for the shared empty payload. */
    final class EmptyHolder {

        /**
         * Shared immutable empty payload.
         */
        private static final Payload EMPTY = Payload.owned(ByteString.EMPTY);

        private EmptyHolder() {
        }
    }

    /**
     * Creates a repeatable payload from bytes.
     *
     * @param bytes mutable bytes copied into an immutable snapshot
     * @return repeatable payload backed by the copied byte snapshot
     */
    static Payload of(final byte[] bytes) {
        if (bytes == null) {
            throw new ValidateException("Payload bytes must not be null");
        }
        return bytes.length == 0 ? empty() : repeatable(ByteString.of(bytes));
    }

    /**
     * Creates a repeatable payload from immutable bytes.
     *
     * @param bytes immutable byte string used as the payload snapshot
     * @return repeatable payload backed by the immutable byte string
     */
    static Payload of(final ByteString bytes) {
        if (bytes == null) {
            throw new ValidateException("Payload bytes must not be null");
        }
        return bytes.size() == 0 ? empty() : repeatable(bytes);
    }

    /**
     * Creates a repeatable payload from an already immutable owned snapshot without copying it.
     *
     * @param snapshot immutable owned bytes
     * @return repeatable payload
     */
    static Payload owned(final ByteString snapshot) {
        if (snapshot == null) {
            throw new ValidateException("Payload snapshot must not be null");
        }
        return repeatable(snapshot);
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
                return new Source() {

                    /**
                     * Next unread byte offset.
                     */
                    private int offset;

                    /**
                     * Closed state.
                     */
                    private boolean closed;

                    /**
                     * Copies snapshot bytes into the supplied sink.
                     *
                     * @param sink      destination buffer
                     * @param byteCount maximum number of bytes to copy
                     * @return copied byte count, {@code 0} for an empty request, or {@code -1} at EOF
                     */
                    @Override
                    public long read(final Buffer sink, final long byteCount) {
                        if (sink == null) {
                            throw new ValidateException("Payload source sink must not be null");
                        }
                        if (byteCount < 0) {
                            throw new ValidateException("Payload source byte count must not be negative");
                        }
                        if (closed) {
                            throw new StatefulException("Payload source is closed");
                        }
                        if (byteCount == 0) {
                            return 0;
                        }
                        if (offset == snapshot.size()) {
                            return -1;
                        }
                        final int count = (int) Math.min(byteCount, snapshot.size() - offset);
                        sink.write(snapshot.data, offset, count);
                        offset += count;
                        return count;
                    }

                    /**
                     * Returns the source timeout policy.
                     *
                     * @return the non-expiring timeout policy
                     */
                    @Override
                    public Timeout timeout() {
                        return Timeout.NONE;
                    }

                    /**
                     * Closes this snapshot cursor.
                     */
                    @Override
                    public void close() {
                        closed = true;
                    }
                };
            }

            /**
             * Returns the immutable owner without copying.
             *
             * @return immutable bytes owned by this repeatable payload
             */
            @Override
            public ByteString ownedBytes() {
                return snapshot;
            }

            /**
             * Returns the repeatable snapshot bytes using the default threshold.
             *
             * @return snapshot bytes
             */
            @Override
            public byte[] bytes() {
                return bytes(Builder.DEFAULT_MATERIALIZE_MAX_BYTES);
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
                return snapshot.size() == 0 ? Normal.EMPTY_BYTE_ARRAY : snapshot.toByteArray();
            }

            /**
             * Decodes the repeatable snapshot using the default threshold.
             *
             * @param charset character encoding used to decode the snapshot
             * @return decoded text
             */
            @Override
            public String text(final Charset charset) {
                return text(charset, Builder.DEFAULT_MATERIALIZE_MAX_BYTES);
            }

            /**
             * Decodes the repeatable snapshot using an explicit threshold.
             *
             * @param charset  character encoding used to decode the snapshot
             * @param maxBytes maximum bytes to materialize
             * @return decoded text
             */
            @Override
            public String text(final Charset charset, final long maxBytes) {
                validateCharset(charset);
                validateMaterializeMaxBytes(maxBytes);
                if (snapshot.size() > maxBytes) {
                    throw materializeExceeded(snapshot.size(), maxBytes, "Payload.text(Charset,long)");
                }
                return snapshot.string(charset);
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
     * @param text    text encoded into the payload snapshot
     * @param charset character encoding used to encode the text
     * @return repeatable payload containing the encoded text
     */
    static Payload of(final String text, final Charset charset) {
        if (text == null) {
            throw new ValidateException("Payload text must not be null");
        }
        validateCharset(charset);
        return owned(ByteString.encodeString(text, charset));
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
        return new Payload() {

            /**
             * Guarded one-shot state without a separate atomic allocation.
             */
            private boolean opened;

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
            public synchronized Source source() {
                if (opened) {
                    throw new StatefulException("Streaming payload can only be opened once");
                }
                opened = true;
                return input;
            }

            /**
             * Materializes this one-shot stream using the default threshold.
             *
             * @return materialized bytes
             */
            @Override
            public byte[] bytes() {
                return bytes(Builder.DEFAULT_MATERIALIZE_MAX_BYTES);
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
             * @param charset character encoding used to decode the one-shot stream
             * @return decoded text
             */
            @Override
            public String text(final Charset charset) {
                return text(charset, Builder.DEFAULT_MATERIALIZE_MAX_BYTES);
            }

            /**
             * Decodes this one-shot stream using an explicit threshold.
             *
             * @param charset  character encoding used to decode the one-shot stream
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
     * @return newly opened source, subject to the implementation's repeatability
     */
    Source source();

    /**
     * Reads all payload bytes.
     *
     * @return payload bytes
     */
    default byte[] bytes() {
        return materialize(this, Builder.DEFAULT_MATERIALIZE_MAX_BYTES, "Payload.bytes()");
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
     * @param charset character encoding used to decode payload bytes
     * @return payload text
     */
    default String text(final Charset charset) {
        return text(charset, Builder.DEFAULT_MATERIALIZE_MAX_BYTES);
    }

    /**
     * Reads the payload as text with an explicit materialize threshold.
     *
     * @param charset  character encoding used to decode payload bytes
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
     * Returns immutable owned bytes for copy-free protocol consumers.
     *
     * <p>
     * Streaming implementations retain their one-shot semantics and therefore reject this operation.
     * </p>
     *
     * @return immutable byte owner
     */
    default ByteString ownedBytes() {
        if (!repeatable()) {
            throw new StatefulException("Streaming payload has no repeatable byte owner");
        }
        return ByteString.of(bytes());
    }

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
            if (length >= Normal.LONG_ZERO) {
                final byte[] result = new byte[(int) length];
                final Buffer scratch = new Buffer();
                int offset = Normal._0;
                while (offset < result.length) {
                    final long read = input.read(scratch, result.length - offset);
                    if (read == Normal.__1) {
                        throw new InternalException("Streaming payload ended before declared length at " + source);
                    }
                    if (read == Normal.LONG_ZERO) {
                        continue;
                    }
                    final int count = (int) read;
                    scratch.read(result, offset, count);
                    offset += count;
                }
                if (input.read(scratch, Normal._1) != Normal.__1) {
                    throw new InternalException("Streaming payload exceeded declared length at " + source);
                }
                return result;
            }
            final Buffer buffer = new Buffer();
            long total = 0;
            while (true) {
                final long requestBytes = length > total ? Math.min(length - total, Integer.MAX_VALUE) : Normal._1;
                final long read = input.read(buffer, requestBytes);
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
     * @param payload payload whose source is streamed
     * @param sink    destination receiving payload bytes and a final flush
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
     * @param charset character encoding reference to validate
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
