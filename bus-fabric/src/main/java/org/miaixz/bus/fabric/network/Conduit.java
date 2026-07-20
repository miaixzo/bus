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
package org.miaixz.bus.fabric.network;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.ThreadKit;

/**
 * Asynchronous network conduit contract.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Conduit extends AutoCloseable {

    /**
     * Reads bytes into a target buffer.
     *
     * @param target    target buffer
     * @param byteCount maximum byte count to read
     * @return future containing the read byte count, or {@code Normal.__1} at EOF
     */
    CompletableFuture<Long> read(Buffer target, long byteCount);

    /**
     * Writes and consumes exactly {@code byteCount} bytes from a source buffer.
     * <p>
     * A successful future always contains {@code byteCount}; partial success is forbidden. Implementations must loop
     * over native partial writes and fail after sixteen consecutive zero-progress attempts. Negative counts, counts
     * larger than {@link Buffer#size()}, EOF, closure, cancellation, and native failures complete the future
     * exceptionally. A zero count returns an already completed future containing zero.
     *
     * @param source    source buffer
     * @param byteCount byte count to write
     * @return future containing exactly {@code byteCount} on success
     */
    CompletableFuture<Long> write(Buffer source, long byteCount);

    /**
     * Returns the core.io read view.
     *
     * @return source view
     */
    default Source source() {
        final Conduit conduit = this;
        return new Source() {

            /**
             * Reads through the asynchronous conduit and verifies buffer consumption.
             *
             * @param sink      target buffer
             * @param byteCount maximum byte count
             * @return read count or -1 at EOF
             * @throws IOException when reading fails
             */
            @Override
            public long read(final Buffer sink, final long byteCount) throws IOException {
                final Buffer target = requireBuffer(sink, "Read target");
                if (byteCount < Normal._0) {
                    throw new ValidateException("Read byte count must not be negative");
                }
                if (!conduit.opened()) {
                    throw new SocketException("Conduit is closed");
                }
                final long before = target.size();
                final long read = await(conduit.read(target, byteCount), "Conduit read failed");
                final long appended = target.size() - before;
                if (read == Normal.__1) {
                    if (appended != Normal._0) {
                        throw new SocketException("Conduit EOF must not append bytes");
                    }
                    return read;
                }
                if (read < Normal._0 || read > byteCount || appended != read
                        || (byteCount > Normal._0 && read == Normal._0)) {
                    throw new SocketException("Conduit read violated the Source contract");
                }
                return read;
            }

            /**
             * Returns the no-operation core timeout.
             *
             * @return timeout
             */
            @Override
            public org.miaixz.bus.core.io.timout.Timeout timeout() {
                return org.miaixz.bus.core.io.timout.Timeout.NONE;
            }

            /**
             * Closes the enclosing conduit.
             */
            @Override
            public void close() {
                conduit.close();
            }
        };
    }

    /**
     * Returns the core.io write view.
     *
     * @return sink view
     */
    default Sink sink() {
        final Conduit conduit = this;
        return new Sink() {

            /**
             * Fully consumes one requested byte range through partial asynchronous writes.
             *
             * @param source    source buffer
             * @param byteCount requested count
             * @throws IOException when writing fails
             */
            @Override
            public void write(final Buffer source, final long byteCount) throws IOException {
                final Buffer current = requireBuffer(source, "Write source");
                if (byteCount < Normal._0 || byteCount > current.size()) {
                    throw new ValidateException("Write byte count must be between zero and source size");
                }
                if (byteCount == Normal._0) {
                    return;
                }
                if (!conduit.opened()) {
                    throw new SocketException("Conduit is closed");
                }
                final long initialSize = current.size();
                long remaining = byteCount;
                int zeroProgress = Normal._0;
                while (remaining > Normal._0) {
                    final long before = current.size();
                    final long written = await(conduit.write(current, remaining), "Conduit write failed");
                    final long consumed = before - current.size();
                    if (written < Normal._0 || written > remaining || consumed != written) {
                        throw new SocketException("Conduit write violated the Sink consumption contract");
                    }
                    if (written == Normal._0) {
                        zeroProgress++;
                        if (zeroProgress >= Normal._16) {
                            throw new SocketException("Conduit write made no progress after 16 attempts");
                        }
                        if (!ThreadKit.sleep(Normal._1)) {
                            throw new SocketException("Conduit write was interrupted");
                        }
                        continue;
                    }
                    zeroProgress = Normal._0;
                    remaining -= written;
                }
                if (current.size() != initialSize - byteCount) {
                    throw new SocketException("Conduit sink did not consume the requested byte count");
                }
            }

            /**
             * Flushes a conduit whose native writes are immediate.
             */
            @Override
            public void flush() {
                // Native conduit writes are unbuffered.
            }

            /**
             * Returns the no-operation core timeout.
             *
             * @return timeout
             */
            @Override
            public Timeout timeout() {
                return Timeout.NONE;
            }

            /**
             * Closes the enclosing conduit.
             */
            @Override
            public void close() {
                conduit.close();
            }
        };
    }

    /**
     * Returns whether this conduit is open.
     *
     * @return true when opened
     */
    boolean opened();

    /**
     * Closes the conduit.
     */
    @Override
    void close();

    /**
     * Awaits one conduit operation while preserving its original failure.
     *
     * @param future  operation future
     * @param message checked failure message
     * @return byte count
     * @throws IOException when a checked operation fails
     */
    private static long await(final CompletableFuture<Long> future, final String message) throws IOException {
        try {
            final Long value = Assert.notNull(future, () -> new ValidateException("Conduit future must not be null"))
                    .get();
            if (value == null) {
                throw new SocketException("Conduit future returned a null byte count");
            }
            return value;
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(message, e);
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof IOException io) {
                throw io;
            }
            if (cause instanceof RuntimeException runtime) {
                throw runtime;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new IOException(message, cause);
        }
    }

    /**
     * Validates a required core buffer.
     *
     * @param buffer buffer
     * @param name   name
     * @return buffer
     */
    private static Buffer requireBuffer(final Buffer buffer, final String name) {
        return Assert.notNull(buffer, () -> new ValidateException(name + " must not be null"));
    }

}
