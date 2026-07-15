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

import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongConsumer;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Status;

/**
 * HTTP/2 stream state with buffered body source and sink.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Http2Stream implements AutoCloseable {

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
        if (id <= Normal._0) {
            throw new ValidateException("HTTP/2 stream id must be positive");
        }
        this.id = id;
        this.headers = require(headers, "HTTP/2 stream headers");
        this.source = new StreamBody(require(inboundConsumed, "HTTP/2 inbound consumed callback"));
        this.sink = new StreamBody(ignored -> {
        });
        this.state = new AtomicReference<>(Status.OPENED);
        this.receiveWindow = new AtomicLong(Builder.HTTP2_STREAM_DEFAULT_WINDOW);
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
    public void receiveData(final ByteString data) {
        final ByteString payload = require(data, "HTTP/2 data");
        if (!opened()) {
            throw new ProtocolException("HTTP/2 closed stream received data");
        }
        consumeReceiveWindow(payload.size());
        final Buffer buffer = new Buffer().write(payload);
        source.write(buffer, buffer.size());
    }

    /**
     * Restores inbound flow-control window after a WINDOW_UPDATE is sent.
     *
     * @param delta restored byte count
     */
    void updateReceiveWindow(final long delta) {
        if (delta <= Normal._0 || delta > Integer.MAX_VALUE) {
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
    public Source source() {
        return source;
    }

    /**
     * Returns sink.
     *
     * @return sink
     */
    public Sink sink() {
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
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

    /**
     * Buffered stream body.
     */
    private static final class StreamBody implements Source, Sink, Payload {

        /**
         * Buffer.
         */
        private final Buffer buffer = new Buffer();

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
            this.consumed = require(consumed, "HTTP/2 consumed callback");
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
         * Opens a bounded source view over the buffered HTTP/2 body.
         *
         * @return body source view
         */
        @Override
        public synchronized Source source() {
            ensureOpen();
            return new BufferSourceView(bufferSize());
        }

        /**
         * Reads from the buffered body and reports consumed flow-control bytes.
         *
         * @param target    destination buffer
         * @param byteCount maximum bytes to read
         * @return bytes read, or -1 at end of buffer
         */
        @Override
        public synchronized long read(final Buffer target, final long byteCount) {
            final Buffer checkedTarget = require(target, "HTTP/2 body target");
            if (byteCount < Normal._0) {
                throw new IllegalArgumentException("byteCount < 0: " + byteCount);
            }
            ensureOpen();
            if (byteCount == Normal._0) {
                return Normal._0;
            }
            final long size = buffer.size();
            if (readIndex >= size) {
                return Normal.__1;
            }
            final int count = (int) Math.min(Math.min(byteCount, size - readIndex), Integer.MAX_VALUE);
            buffer.copyTo(checkedTarget, readIndex, count);
            readIndex += count;
            creditConsumed(readIndex);
            return count;
        }

        /**
         * Returns source and sink timeout.
         *
         * @return timeout
         */
        @Override
        public Timeout timeout() {
            return Timeout.NONE;
        }

        /**
         * Reads all bytes.
         *
         * @return bytes
         */
        @Override
        public synchronized byte[] bytes() {
            return bytes(Builder.DEFAULT_MATERIALIZE_MAX_BYTES);
        }

        /**
         * Materializes buffered bytes and marks the buffer as consumed.
         *
         * @param maxBytes maximum bytes to materialize
         * @return materialized bytes
         */
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
            return text(charset, Builder.DEFAULT_MATERIALIZE_MAX_BYTES);
        }

        /**
         * Materializes and decodes the buffered body with an explicit threshold.
         *
         * @param charset  charset
         * @param maxBytes maximum bytes to materialize
         * @return decoded text
         */
        @Override
        public String text(final Charset charset, final long maxBytes) {
            return new String(bytes(maxBytes), require(charset, "Charset"));
        }

        /**
         * Returns whether this buffered body is repeatable.
         *
         * @return true
         */
        @Override
        public boolean repeatable() {
            return true;
        }

        /**
         * Writes buffered bytes.
         *
         * @param source    source buffer
         * @param byteCount byte count
         */
        @Override
        public synchronized void write(final Buffer source, final long byteCount) {
            final Buffer checkedSource = require(source, "HTTP/2 body source");
            if (byteCount < Normal._0) {
                throw new IllegalArgumentException("byteCount < 0: " + byteCount);
            }
            ensureOpen();
            buffer.write(checkedSource, byteCount);
        }

        /**
         * Returns written count.
         *
         * @return written
         */
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
            final Buffer copy = new Buffer();
            buffer.copyTo(copy, Normal._0, buffer.size());
            return copy.readByteArray();
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
         * Source view over the buffered segments.
         */
        private final class BufferSourceView implements Source {

            /**
             * Read limit captured when the source is opened.
             */
            private final int limit;

            /**
             * Read cursor.
             */
            private int cursor;

            /**
             * Creates a source view.
             *
             * @param limit readable limit
             */
            private BufferSourceView(final int limit) {
                this.limit = limit;
            }

            /**
             * Reads from this bounded buffer view and reports consumed bytes.
             *
             * @param target    destination buffer
             * @param byteCount maximum bytes to read
             * @return bytes read, or -1 at end of view
             */
            @Override
            public long read(final Buffer target, final long byteCount) {
                final Buffer checkedTarget = require(target, "HTTP/2 body target");
                if (byteCount < Normal._0) {
                    throw new IllegalArgumentException("byteCount < 0: " + byteCount);
                }
                if (byteCount == Normal._0) {
                    return Normal._0;
                }
                synchronized (StreamBody.this) {
                    if (cursor >= limit) {
                        return Normal.__1;
                    }
                    final int count = (int) Math.min(Math.min(byteCount, limit - cursor), Integer.MAX_VALUE);
                    buffer.copyTo(checkedTarget, cursor, count);
                    cursor += count;
                    creditConsumed(cursor);
                    return count;
                }
            }

            /**
             * Returns the source view timeout policy.
             *
             * @return timeout
             */
            @Override
            public Timeout timeout() {
                return Timeout.NONE;
            }

            /**
             * Leaves buffer lifecycle to the owning stream body.
             */
            @Override
            public void close() {
                // The backing stream body owns the buffer lifecycle.
            }

        }

    }

}
