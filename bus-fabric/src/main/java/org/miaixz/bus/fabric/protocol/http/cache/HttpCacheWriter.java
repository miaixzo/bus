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
package org.miaixz.bus.fabric.protocol.http.cache;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.cache.CacheWriter;

/**
 * One-shot payload that writes consumed network bytes into a cache writer.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class HttpCacheWriter implements Payload, AutoCloseable {

    /**
     * Original one-shot payload.
     */
    private final Payload delegate;

    /**
     * Cache writer.
     */
    private final CacheWriter writer;

    /**
     * Commit callback.
     */
    private final Runnable commitCallback;

    /**
     * Abort callback.
     */
    private final Runnable abortCallback;

    /**
     * Opened state.
     */
    private final AtomicBoolean opened = new AtomicBoolean();

    /**
     * Finished state.
     */
    private final AtomicBoolean finished = new AtomicBoolean();

    /**
     * Current stream.
     */
    private InputStream current;

    /**
     * Creates a cache-writing payload.
     *
     * @param delegate       original payload
     * @param writer         cache writer
     * @param commitCallback commit callback
     * @param abortCallback  abort callback
     */
    HttpCacheWriter(final Payload delegate, final CacheWriter writer, final Runnable commitCallback,
            final Runnable abortCallback) {
        this.delegate = require(delegate, "Payload");
        this.writer = require(writer, "Cache writer");
        this.commitCallback = require(commitCallback, "Commit callback");
        this.abortCallback = require(abortCallback, "Abort callback");
    }

    /**
     * Returns body length.
     *
     * @return body length
     */
    @Override
    public long length() {
        return delegate.length();
    }

    /**
     * Opens a tee stream.
     *
     * @return tee stream
     */
    @Override
    public InputStream stream() {
        if (!opened.compareAndSet(false, true)) {
            throw new StatefulException("Cache writing payload can only be opened once");
        }
        current = delegate.stream();
        return new CacheWritingInputStream(current, writer, this::commit, this::abort);
    }

    /**
     * Reads all body bytes while writing cache.
     *
     * @return bytes
     */
    @Override
    public byte[] bytes() {
        return bytes(Options.DEFAULT_MATERIALIZE_MAX_BYTES);
    }

    /**
     * Reads all body bytes while writing cache with an explicit threshold.
     *
     * @param maxBytes maximum bytes to materialize
     * @return bytes
     */
    @Override
    public byte[] bytes(final long maxBytes) {
        return Payload.materialize(this, maxBytes, "HttpCacheWriter.bytes(long)");
    }

    /**
     * Reads body text.
     *
     * @param charset charset
     * @return text
     */
    @Override
    public String text(final Charset charset) {
        return text(charset, Options.DEFAULT_MATERIALIZE_MAX_BYTES);
    }

    /**
     * Reads body text with an explicit threshold.
     *
     * @param charset  charset
     * @param maxBytes maximum bytes to materialize
     * @return text
     */
    @Override
    public String text(final Charset charset, final long maxBytes) {
        if (charset == null) {
            throw new ValidateException("Charset must not be null");
        }
        return new String(bytes(maxBytes), charset);
    }

    /**
     * Returns whether this payload is repeatable.
     *
     * @return false
     */
    @Override
    public boolean repeatable() {
        return false;
    }

    /**
     * Aborts unfinished cache writes.
     */
    @Override
    public void close() {
        abort();
    }

    /**
     * Publishes the cache writer after the entire network body has been consumed.
     */
    private void commit() {
        if (!finished.compareAndSet(false, true)) {
            return;
        }
        writer.commit();
        commitCallback.run();
    }

    /**
     * Aborts the cache writer and closes the current response stream when the cached copy is incomplete.
     */
    private void abort() {
        if (!finished.compareAndSet(false, true)) {
            return;
        }
        writer.abort();
        abortCallback.run();
        if (current != null) {
            try {
                current.close();
            } catch (final IOException ignored) {
                // Best-effort close after abort.
            }
        }
    }

    /**
     * Validates collaborators used by the cache-writing response body.
     *
     * @param value value
     * @param name  field name used in validation messages
     * @param <T>   value type
     * @return validated value
     */
    private static <T> T require(final T value, final String name) {
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value;
    }

    /**
     * Input stream that tees network bytes into a cache writer.
     */
    private static final class CacheWritingInputStream extends InputStream {

        /**
         * Source stream.
         */
        private final InputStream source;

        /**
         * Cache writer.
         */
        private final CacheWriter writer;

        /**
         * Commit callback.
         */
        private final Runnable commit;

        /**
         * Abort callback.
         */
        private final Runnable abort;

        /**
         * Single-byte buffer.
         */
        private final byte[] single = new byte[1];

        /**
         * Bus-core transfer buffer.
         */
        private final Buffer buffer = new Buffer();

        /**
         * Closed state.
         */
        private final AtomicBoolean closed = new AtomicBoolean();

        /**
         * Creates a tee stream.
         *
         * @param source source stream
         * @param writer cache writer
         * @param commit commit callback
         * @param abort  abort callback
         */
        private CacheWritingInputStream(final InputStream source, final CacheWriter writer, final Runnable commit,
                final Runnable abort) {
            this.source = source;
            this.writer = writer;
            this.commit = commit;
            this.abort = abort;
        }

        /**
         * Reads one byte.
         *
         * @return byte or -1
         * @throws IOException when reading fails
         */
        @Override
        public int read() throws IOException {
            final int read = read(single, 0, 1);
            return read < 0 ? -1 : single[0] & 0xff;
        }

        /**
         * Reads bytes and writes them to cache.
         *
         * @param target target buffer
         * @param offset offset
         * @param length length
         * @return byte count or -1
         * @throws IOException when reading fails
         */
        @Override
        public int read(final byte[] target, final int offset, final int length) throws IOException {
            if (closed.get()) {
                return -1;
            }
            try {
                final int read = source.read(target, offset, length);
                if (read < 0) {
                    commit.run();
                    closed.set(true);
                    return -1;
                }
                if (read > 0) {
                    buffer.write(target, offset, read);
                    writer.write(buffer, read);
                }
                return read;
            } catch (final IOException | RuntimeException e) {
                abort.run();
                throw e;
            }
        }

        /**
         * Closes this stream and aborts if EOF was not reached.
         *
         * @throws IOException when source close fails
         */
        @Override
        public void close() throws IOException {
            if (closed.compareAndSet(false, true)) {
                abort.run();
            }
            source.close();
        }

    }

}
