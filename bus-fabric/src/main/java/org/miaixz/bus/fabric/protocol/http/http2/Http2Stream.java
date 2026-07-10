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
package org.miaixz.bus.fabric.protocol.http.http2;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongConsumer;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.codec.stream.SegmentedBuffer;
import org.miaixz.bus.fabric.codec.stream.StreamSink;
import org.miaixz.bus.fabric.codec.stream.StreamSource;

/**
 * HTTP/2 stream state with buffered body source and sink.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Http2Stream implements AutoCloseable {

    /**
     * Default stream flow-control window.
     */
    private static final long DEFAULT_WINDOW = 65_535L;

    /**
     * Stream id.
     */
    private final int id;

    /**
     * Body source.
     */
    private final StreamBody source;

    /**
     * Body sink.
     */
    private final StreamBody sink;

    /**
     * State.
     */
    private final AtomicReference<Status> state;

    /**
     * Inbound flow-control window.
     */
    private final AtomicLong receiveWindow;

    /**
     * Headers.
     */
    private volatile Headers headers;

    /**
     * Priority metadata.
     */
    private volatile Http2Priority priority;

    /**
     * Creates a stream.
     *
     * @param id      id
     * @param headers headers
     */
    Http2Stream(final int id, final Headers headers) {
        this(id, headers, ignored -> {
        });
    }

    /**
     * Creates a stream.
     *
     * @param id              id
     * @param headers         headers
     * @param inboundConsumed inbound body bytes consumed by the application
     */
    Http2Stream(final int id, final Headers headers, final LongConsumer inboundConsumed) {
        if (id <= 0) {
            throw new ValidateException("HTTP/2 stream id must be positive");
        }
        this.id = id;
        this.headers = require(headers, "HTTP/2 stream headers");
        this.source = new StreamBody(require(inboundConsumed, "HTTP/2 inbound consumed callback"));
        this.sink = new StreamBody(ignored -> {
        });
        this.state = new AtomicReference<>(Status.OPENED);
        this.receiveWindow = new AtomicLong(DEFAULT_WINDOW);
    }

    /**
     * Returns stream id.
     *
     * @return id
     */
    public int id() {
        return id;
    }

    /**
     * Returns header snapshot.
     *
     * @return headers
     */
    public Headers headers() {
        return headers;
    }

    /**
     * Returns priority metadata.
     *
     * @return priority or null
     */
    public Http2Priority priority() {
        return priority;
    }

    /**
     * Receives headers.
     *
     * @param headers headers
     */
    public void receiveHeaders(final Headers headers) {
        ensureOpen();
        this.headers = require(headers, "HTTP/2 headers");
    }

    /**
     * Receives priority metadata.
     *
     * @param priority priority metadata
     */
    void priority(final Http2Priority priority) {
        ensureOpen();
        this.priority = require(priority, "HTTP/2 priority");
    }

    /**
     * Receives body data.
     *
     * @param data data
     */
    public void receiveData(final ByteBuffer data) {
        if (data == null) {
            throw new ValidateException("HTTP/2 data must not be null");
        }
        if (!opened()) {
            throw new ProtocolException("HTTP/2 closed stream received data");
        }
        final ByteBuffer view = data.asReadOnlyBuffer();
        consumeReceiveWindow(view.remaining());
        source.write(view);
    }

    /**
     * Restores inbound flow-control window after a WINDOW_UPDATE is sent.
     *
     * @param delta restored byte count
     */
    void updateReceiveWindow(final long delta) {
        if (delta <= 0 || delta > Integer.MAX_VALUE) {
            throw new ProtocolException("Invalid HTTP/2 stream window update");
        }
        long current;
        long next;
        do {
            current = receiveWindow.get();
            next = current + delta;
            if (next > Integer.MAX_VALUE || next < current) {
                throw new ProtocolException("HTTP/2 stream flow-control window overflow");
            }
        } while (!receiveWindow.compareAndSet(current, next));
    }

    /**
     * Returns source.
     *
     * @return source
     */
    public StreamSource source() {
        return source;
    }

    /**
     * Returns sink.
     *
     * @return sink
     */
    public StreamSink sink() {
        return sink;
    }

    /**
     * Closes stream.
     */
    @Override
    public synchronized void close() {
        final Status current = state.get();
        if (current == Status.CLOSED) {
            return;
        }
        if (!current.canTransit(Status.CLOSING)) {
            throw new StatefulException("HTTP/2 stream cannot close from state " + current);
        }
        state.set(Status.CLOSING);
        RuntimeException failure = null;
        try {
            source.close();
        } catch (final RuntimeException e) {
            failure = e;
        }
        try {
            sink.close();
        } catch (final RuntimeException e) {
            if (failure == null) {
                failure = e;
            }
        }
        state.set(Status.CLOSED);
        if (failure != null) {
            throw closeFailure(failure);
        }
    }

    /**
     * Returns whether stream is opened.
     *
     * @return true when opened
     */
    public boolean opened() {
        return state.get() == Status.OPENED;
    }

    /**
     * Ensures stream is open.
     */
    private void ensureOpen() {
        if (!opened()) {
            throw new StatefulException("HTTP/2 stream is closed");
        }
    }

    /**
     * Consumes inbound flow-control window.
     *
     * @param length length
     */
    private void consumeReceiveWindow(final int length) {
        long current;
        do {
            current = receiveWindow.get();
            if (length > current) {
                throw new ProtocolException("HTTP/2 stream flow-control window is exhausted");
            }
        } while (!receiveWindow.compareAndSet(current, current - length));
    }

    /**
     * Classifies close failure.
     *
     * @param failure failure
     * @return runtime failure
     */
    private static RuntimeException closeFailure(final RuntimeException failure) {
        if (failure instanceof InternalException || failure instanceof StatefulException) {
            return failure;
        }
        return new InternalException("Unable to close stream", failure);
    }

    /**
     * Validates required value.
     *
     * @param value value
     * @param name  name
     * @param <T>   type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value;
    }

    /**
     * Buffered stream body.
     */
    private static final class StreamBody implements StreamSource, StreamSink {

        /**
         * Buffer.
         */
        private final SegmentedBuffer buffer = SegmentedBuffer.create();

        /**
         * Callback for newly consumed bytes.
         */
        private final LongConsumer consumed;

        /**
         * Read cursor.
         */
        private int readIndex;

        /**
         * Highest absolute buffer index already credited to flow-control.
         */
        private int creditedIndex;

        /**
         * Closed flag.
         */
        private boolean closed;

        /**
         * Creates a body.
         *
         * @param consumed consumed byte callback
         */
        private StreamBody(final LongConsumer consumed) {
            this.consumed = consumed;
        }

        /**
         * Returns length.
         *
         * @return length
         */
        @Override
        public synchronized long length() {
            return buffer.size();
        }

        /**
         * Opens a stream snapshot.
         *
         * @return stream
         */
        @Override
        public synchronized InputStream stream() {
            ensureOpen();
            return new SegmentedInputStream(buffer.size());
        }

        /**
         * Reads bytes.
         *
         * @param target target buffer
         * @return read count
         */
        @Override
        public synchronized int read(final ByteBuffer target) {
            if (target == null) {
                throw new ValidateException("HTTP/2 body target must not be null");
            }
            ensureOpen();
            if (!target.hasRemaining()) {
                return 0;
            }
            final long size = buffer.size();
            if (readIndex >= size) {
                return -1;
            }
            final int count = (int) Math.min(target.remaining(), size - readIndex);
            buffer.copyTo(readIndex, target, count);
            readIndex += count;
            creditConsumed(readIndex);
            return count;
        }

        /**
         * Reads all bytes.
         *
         * @return bytes
         */
        @Override
        public synchronized byte[] bytes() {
            return bytes(Options.DEFAULT_MATERIALIZE_MAX_BYTES);
        }

        @Override
        public synchronized byte[] bytes(final long maxBytes) {
            ensureOpen();
            Payload.validateMaterializeMaxBytes(maxBytes);
            if (buffer.size() > maxBytes) {
                throw Payload.materializeExceeded(buffer.size(), maxBytes, "Http2Stream.StreamBody.bytes(long)");
            }
            creditConsumed(bufferSize());
            return snapshot();
        }

        /**
         * Reads text.
         *
         * @param charset charset
         * @return text
         */
        @Override
        public String text(final Charset charset) {
            return text(charset, Options.DEFAULT_MATERIALIZE_MAX_BYTES);
        }

        @Override
        public String text(final Charset charset, final long maxBytes) {
            if (charset == null) {
                throw new ValidateException("Charset must not be null");
            }
            return new String(bytes(maxBytes), charset);
        }

        /**
         * Writes bytes.
         *
         * @param source source buffer
         */
        @Override
        public synchronized void write(final ByteBuffer source) {
            if (source == null) {
                throw new ValidateException("HTTP/2 body source must not be null");
            }
            ensureOpen();
            buffer.append(source);
            source.position(source.limit());
        }

        /**
         * Writes a payload.
         *
         * @param payload payload
         */
        @Override
        public void write(final Payload payload) {
            if (payload == null) {
                throw new ValidateException("Payload must not be null");
            }
            final byte[] bytes = new byte[8192];
            try (InputStream input = payload.stream()) {
                int read = input.read(bytes);
                while (read >= 0) {
                    if (read > 0) {
                        write(ByteBuffer.wrap(bytes, 0, read));
                    }
                    read = input.read(bytes);
                }
            } catch (final IOException e) {
                throw new InternalException("Unable to stream HTTP/2 payload", e);
            }
        }

        /**
         * Returns written count.
         *
         * @return written
         */
        @Override
        public synchronized long written() {
            return buffer.size();
        }

        /**
         * Flushes body.
         */
        @Override
        public synchronized void flush() {
            ensureOpen();
        }

        /**
         * Closes body.
         */
        @Override
        public synchronized void close() {
            closed = true;
        }

        /**
         * Ensures this body is open.
         */
        private void ensureOpen() {
            if (closed) {
                throw new StatefulException("HTTP/2 body is closed");
            }
        }

        /**
         * Returns a snapshot without consuming the backing buffer.
         *
         * @return snapshot bytes
         */
        private byte[] snapshot() {
            return buffer.copy(0, buffer.size());
        }

        /**
         * Credits newly consumed bytes once, even across multiple source views.
         *
         * @param absoluteIndex consumed absolute buffer index
         */
        private void creditConsumed(final int absoluteIndex) {
            if (absoluteIndex <= creditedIndex) {
                return;
            }
            final int delta = absoluteIndex - creditedIndex;
            creditedIndex = absoluteIndex;
            consumed.accept(delta);
        }

        /**
         * Returns the backing buffer size as an int.
         *
         * @return buffer size
         */
        private int bufferSize() {
            final long size = buffer.size();
            if (size > Integer.MAX_VALUE) {
                throw new ProtocolException("HTTP/2 body buffer is too large");
            }
            return (int) size;
        }

        /**
         * Input stream view over the buffered segments.
         */
        private final class SegmentedInputStream extends InputStream {

            /**
             * Read limit captured when the stream is opened.
             */
            private final int limit;

            /**
             * Read cursor.
             */
            private int cursor;

            /**
             * Creates a stream view.
             *
             * @param limit readable limit
             */
            private SegmentedInputStream(final int limit) {
                this.limit = limit;
            }

            /**
             * Reads one byte.
             *
             * @return byte or -1
             */
            @Override
            public int read() {
                synchronized (StreamBody.this) {
                    if (cursor >= limit) {
                        return -1;
                    }
                    final int value = buffer.get(cursor++) & 0xff;
                    creditConsumed(cursor);
                    return value;
                }
            }

            /**
             * Reads bytes into a target array.
             *
             * @param target target
             * @param offset offset
             * @param length length
             * @return read count or -1
             */
            @Override
            public int read(final byte[] target, final int offset, final int length) {
                if (target == null) {
                    throw new ValidateException("HTTP/2 body target must not be null");
                }
                if (offset < 0 || length < 0 || length > target.length - offset) {
                    throw new IndexOutOfBoundsException("HTTP/2 body read range is outside target");
                }
                if (length == 0) {
                    return 0;
                }
                synchronized (StreamBody.this) {
                    if (cursor >= limit) {
                        return -1;
                    }
                    final int count = Math.min(length, limit - cursor);
                    buffer.copyTo(cursor, ByteBuffer.wrap(target, offset, count), count);
                    cursor += count;
                    creditConsumed(cursor);
                    return count;
                }
            }

        }

    }

}
