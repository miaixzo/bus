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
     * Network payload whose source is opened once and copied into the cache.
     */
    private final Payload delegate;

    /**
     * Destination receiving bytes as callers consume the network payload.
     */
    private final CacheWriter writer;

    /**
     * Callback invoked after the cache destination commits a complete body.
     */
    private final Runnable commitCallback;

    /**
     * Callback invoked after an incomplete cache destination is aborted.
     */
    private final Runnable abortCallback;

    /**
     * Guards the one-shot transition from unopened to opened.
     */
    private final AtomicBoolean opened = new AtomicBoolean();

    /**
     * Ensures that commit or abort completes the cache operation only once.
     */
    private final AtomicBoolean finished = new AtomicBoolean();

    /**
     * Open delegate source, or null before {@link #source()} is called.
     */
    private Source current;

    /**
     * Creates a cache-writing payload.
     *
     * @param delegate       non-null one-shot network payload to consume
     * @param writer         non-null cache destination receiving consumed bytes
     * @param commitCallback non-null callback run after a complete body is committed
     * @param abortCallback  non-null callback run after an incomplete body is aborted
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
     * Returns the length reported by the network payload.
     *
     * @return delegated byte length, or the delegate's unknown-length sentinel
     */
    @Override
    public long length() {
        return delegate.length();
    }

    /**
     * Opens the payload once and returns a source that copies consumed bytes to the cache writer.
     *
     * @return source that commits the cache at end-of-stream and aborts it on failure or premature close
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
     * Materializes the body while populating the cache, using the default size limit.
     *
     * @return complete payload bytes
     */
    @Override
    public byte[] bytes() {
        return bytes(Builder.DEFAULT_MATERIALIZE_MAX_BYTES);
    }

    /**
     * Reads all body bytes while writing cache with an explicit threshold.
     *
     * @param maxBytes maximum number of payload bytes permitted in memory
     * @return complete payload bytes
     */
    @Override
    public byte[] bytes(final long maxBytes) {
        return Payload.materialize(this, maxBytes, "HttpCacheWriter.bytes(long)");
    }

    /**
     * Materializes and decodes the body using the default size limit.
     *
     * @param charset non-null charset used to decode the body
     * @return decoded payload text
     */
    @Override
    public String text(final Charset charset) {
        return text(charset, Builder.DEFAULT_MATERIALIZE_MAX_BYTES);
    }

    /**
     * Reads body text with an explicit threshold.
     *
     * @param charset  non-null charset used to decode the body
     * @param maxBytes maximum number of payload bytes permitted in memory
     * @return decoded payload text
     */
    @Override
    public String text(final Charset charset, final long maxBytes) {
        return new String(bytes(maxBytes),
                Assert.notNull(charset, () -> new ValidateException("Charset must not be null")));
    }

    /**
     * Returns whether this payload is repeatable.
     *
     * @return always false because the delegate source can be opened only once
     */
    @Override
    public boolean repeatable() {
        return false;
    }

    /**
     * Aborts an unfinished cache write and closes the opened network source on a best-effort basis.
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
         * Network source from which caller-requested bytes are read.
         */
        private final Source source;

        /**
         * Cache destination receiving copies of successfully read bytes.
         */
        private final CacheWriter writer;

        /**
         * Completion action invoked when the source reaches end-of-stream.
         */
        private final Runnable commit;

        /**
         * Failure action invoked on read failure or close before end-of-stream.
         */
        private final Runnable abort;

        /**
         * Scratch buffer used to copy newly read bytes from the caller's target into the cache writer.
         */
        private final Buffer buffer = new Buffer();

        /**
         * Tracks end-of-stream or explicit closure and prevents further reads.
         */
        private final AtomicBoolean closed = new AtomicBoolean();

        /**
         * Creates a tee source.
         *
         * @param source network source to read
         * @param writer cache destination receiving copied bytes
         * @param commit action to run when end-of-stream is reached
         * @param abort  action to run after a read failure or premature close
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
         * @param target    caller buffer receiving bytes from the network source
         * @param byteCount maximum bytes requested from the source
         * @return number of bytes read, zero when the delegate makes no progress, or -1 at or after end-of-stream
         * @throws IOException when the delegate read or cache write fails with an I/O error
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
         * @return timeout object owned by the delegate network source
         */
        @Override
        public Timeout timeout() {
            return source.timeout();
        }

        /**
         * Closes the delegate source and aborts the cache operation if end-of-stream was not reached.
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
