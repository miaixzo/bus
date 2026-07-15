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
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Builder;
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
     * Current source.
     */
    private Source current;

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
        this.delegate = Assert.notNull(delegate, () -> new ValidateException("Payload must not be null"));
        this.writer = Assert.notNull(writer, () -> new ValidateException("Cache writer must not be null"));
        this.commitCallback = Assert
                .notNull(commitCallback, () -> new ValidateException("Commit callback must not be null"));
        this.abortCallback = Assert
                .notNull(abortCallback, () -> new ValidateException("Abort callback must not be null"));
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
     * Opens a tee source.
     *
     * @return tee source
     */
    @Override
    public Source source() {
        if (!opened.compareAndSet(false, true)) {
            throw new StatefulException("Cache writing payload can only be opened once");
        }
        current = delegate.source();
        return new CacheWritingSource(current, writer, this::commit, this::abort);
    }

    /**
     * Reads all body bytes while writing cache.
     *
     * @return bytes
     */
    @Override
    public byte[] bytes() {
        return bytes(Builder.DEFAULT_MATERIALIZE_MAX_BYTES);
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
        return text(charset, Builder.DEFAULT_MATERIALIZE_MAX_BYTES);
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
        return new String(bytes(maxBytes),
                Assert.notNull(charset, () -> new ValidateException("Charset must not be null")));
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
     * Source that tees network bytes into a cache writer.
     */
    private static final class CacheWritingSource implements Source {

        /**
         * Source stream.
         */
        private final Source source;

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
         * Bus-core transfer buffer.
         */
        private final Buffer buffer = new Buffer();

        /**
         * Closed state.
         */
        private final AtomicBoolean closed = new AtomicBoolean();

        /**
         * Creates a tee source.
         *
         * @param source source stream
         * @param writer cache writer
         * @param commit commit callback
         * @param abort  abort callback
         */
        private CacheWritingSource(final Source source, final CacheWriter writer, final Runnable commit,
                final Runnable abort) {
            this.source = source;
            this.writer = writer;
            this.commit = commit;
            this.abort = abort;
        }

        /**
         * Reads bytes and writes them to cache.
         *
         * @param target target buffer
         * @return byte count or -1
         * @throws IOException when reading fails
         */
        @Override
        public long read(final Buffer target, final long byteCount) throws IOException {
            if (closed.get()) {
                return -1;
            }
            try {
                final long offset = target.size();
                final long read = source.read(target, byteCount);
                if (read < 0) {
                    commit.run();
                    closed.set(true);
                    return -1;
                }
                if (read > 0) {
                    target.copyTo(buffer, offset, read);
                    writer.write(buffer, read);
                }
                return read;
            } catch (final IOException | RuntimeException e) {
                abort.run();
                throw e;
            }
        }

        /**
         * Returns the delegate source timeout.
         *
         * @return source timeout
         */
        @Override
        public Timeout timeout() {
            return source.timeout();
        }

        /**
         * Closes this source and aborts if EOF was not reached.
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
