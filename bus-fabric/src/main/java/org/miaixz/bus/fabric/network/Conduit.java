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
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
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
     * Performs a synchronous read for blocking transports. Native synchronous conduits should override this method;
     * asynchronous implementations retain the compatibility fallback.
     *
     * @param target    destination buffer
     * @param byteCount maximum bytes to read
     * @return bytes read, or {@code -1} for EOF
     * @throws IOException on transport failure
     */
    default long readSynchronously(final Buffer target, final long byteCount) throws IOException {
        return await(read(target, byteCount), "Conduit read failed");
    }

    /**
     * Performs one synchronous write for blocking transports. Native synchronous conduits should override this method;
     * asynchronous implementations retain the compatibility fallback.
     *
     * @param source    source buffer
     * @param byteCount maximum bytes to consume
     * @return consumed bytes
     * @throws IOException on transport failure
     */
    default long writeSynchronously(final Buffer source, final long byteCount) throws IOException {
        return await(write(source, byteCount), "Conduit write failed");
    }

    /**
     * Performs one synchronous read directly into a caller-owned NIO buffer. Native blocking conduits should override
     * this method; asynchronous implementations retain the compatibility fallback.
     *
     * @param target writable destination buffer
     * @return bytes read, or {@code -1} for EOF
     * @throws IOException on transport failure
     */
    default int readSynchronously(final ByteBuffer target) throws IOException {
        return awaitInteger(read(target), "Conduit NIO read failed");
    }

    /**
     * Performs a synchronous complete write directly from a caller-owned NIO buffer.
     *
     * @param source source buffer consumed by the write
     * @return bytes written
     * @throws IOException on transport failure
     */
    default int writeSynchronously(final ByteBuffer source) throws IOException {
        return awaitInteger(write(source), "Conduit NIO write failed");
    }

    /**
     * Reads once into a caller-owned NIO buffer. The position advances only after successful completion and by exactly
     * the reported byte count. Implementations may override this copying fallback with a native zero-copy operation.
     *
     * @param target caller-owned target
     * @return future containing bytes read, or {@code -1} at EOF
     */
    default CompletableFuture<Integer> read(final ByteBuffer target) {
        if (target == null) {
            return CompletableFuture.failedFuture(new ValidateException("Read target must not be null"));
        }
        if (target.isReadOnly()) {
            return CompletableFuture.failedFuture(new ReadOnlyBufferException());
        }
        final int position = target.position();
        final int limit = target.limit();
        if (position == limit) {
            return CompletableFuture.completedFuture(0);
        }
        final Buffer staging = new Buffer();
        final CompletableFuture<Long> operation;
        try {
            operation = requireFuture(read(staging, limit - position));
        } catch (final RuntimeException failure) {
            return CompletableFuture.failedFuture(failure);
        }
        return operation.thenApply(count -> transferRead(staging, target, position, limit, count));
    }

    /**
     * Writes once from a caller-owned NIO buffer. The position advances only after successful completion and by exactly
     * the reported byte count.
     *
     * @param source caller-owned source
     * @return future containing bytes written
     */
    default CompletableFuture<Integer> write(final ByteBuffer source) {
        if (source == null) {
            return CompletableFuture.failedFuture(new ValidateException("Write source must not be null"));
        }
        final int position = source.position();
        final int limit = source.limit();
        if (position == limit) {
            return CompletableFuture.completedFuture(0);
        }
        final Buffer staging = new Buffer();
        final CompletableFuture<Long> operation;
        try {
            staging.write(source.duplicate());
            operation = requireFuture(write(staging, limit - position));
        } catch (final IOException | RuntimeException failure) {
            return CompletableFuture.failedFuture(failure);
        }
        return operation.thenApply(count -> advanceWrite(source, position, limit, staging, count));
    }

    /**
     * Performs one scatter read over the complete buffer array.
     *
     * @param targets caller-owned targets
     * @return future containing bytes read, or {@code -1} at EOF
     */
    default CompletableFuture<Long> read(final ByteBuffer[] targets) {
        return read(targets, 0, targets == null ? 0 : targets.length);
    }

    /**
     * Performs one scatter read over an array range using a correct copying fallback.
     *
     * @param targets caller-owned targets
     * @param offset  first target
     * @param length  target count
     * @return future containing bytes read, or {@code -1} at EOF
     */
    default CompletableFuture<Long> read(final ByteBuffer[] targets, final int offset, final int length) {
        final BufferRange range;
        try {
            range = bufferRange(targets, offset, length, true);
        } catch (final RuntimeException failure) {
            return CompletableFuture.failedFuture(failure);
        }
        if (range.total == 0L) {
            return CompletableFuture.completedFuture(0L);
        }
        final Buffer staging = new Buffer();
        final CompletableFuture<Long> operation;
        try {
            operation = requireFuture(read(staging, range.total));
        } catch (final RuntimeException failure) {
            return CompletableFuture.failedFuture(failure);
        }
        return operation.thenApply(count -> scatterRead(staging, targets, range, count));
    }

    /**
     * Performs one gathering write over the complete buffer array.
     *
     * @param sources caller-owned sources
     * @return future containing bytes written
     */
    default CompletableFuture<Long> write(final ByteBuffer[] sources) {
        return write(sources, 0, sources == null ? 0 : sources.length);
    }

    /**
     * Performs one gathering write over an array range using a correct copying fallback.
     *
     * @param sources caller-owned sources
     * @param offset  first source
     * @param length  source count
     * @return future containing bytes written
     */
    default CompletableFuture<Long> write(final ByteBuffer[] sources, final int offset, final int length) {
        final BufferRange range;
        try {
            range = bufferRange(sources, offset, length, false);
        } catch (final RuntimeException failure) {
            return CompletableFuture.failedFuture(failure);
        }
        if (range.total == 0L) {
            return CompletableFuture.completedFuture(0L);
        }
        final Buffer staging = new Buffer();
        final CompletableFuture<Long> operation;
        try {
            for (int index = 0; index < length; index++) {
                staging.write(sources[offset + index].duplicate());
            }
            operation = requireFuture(write(staging, range.total));
        } catch (final IOException | RuntimeException failure) {
            return CompletableFuture.failedFuture(failure);
        }
        return operation.thenApply(count -> advanceGather(sources, range, staging, count));
    }

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
                if (byteCount == Normal._0) {
                    return Normal._0;
                }
                if (!conduit.opened()) {
                    throw new SocketException("Conduit is closed");
                }
                final long before = target.size();
                final long read = conduit.readSynchronously(target, byteCount);
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
                    final long written = conduit.writeSynchronously(current, remaining);
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
     * Awaits one integer-count conduit operation while preserving its original failure.
     *
     * @param future  operation future
     * @param message checked failure message
     * @return byte count
     * @throws IOException when a checked operation fails
     */
    private static int awaitInteger(final CompletableFuture<Integer> future, final String message) throws IOException {
        try {
            final Integer value = future == null ? null : future.get();
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
     * @param buffer core buffer reference to validate
     * @param name   diagnostic parameter name
     * @return the validated buffer
     */
    private static Buffer requireBuffer(final Buffer buffer, final String name) {
        return Assert.notNull(buffer, () -> new ValidateException(name + " must not be null"));
    }

    /**
     * Validates a future returned by a legacy implementation.
     *
     * @param future asynchronous byte-count result to validate
     * @return the validated future
     */
    private static CompletableFuture<Long> requireFuture(final CompletableFuture<Long> future) {
        return Assert.notNull(future, () -> new ValidateException("Conduit future must not be null"));
    }

    /**
     * Copies a completed read into one unchanged caller buffer.
     *
     * @param staging  internal buffer containing bytes appended by the read
     * @param target   caller buffer receiving completed bytes
     * @param position target position captured at submission time
     * @param limit    target limit captured at submission time
     * @param count    asynchronous read result, including {@code -1} for EOF
     * @return transferred byte count, or {@code -1} for EOF
     */
    private static int transferRead(
            final Buffer staging,
            final ByteBuffer target,
            final int position,
            final int limit,
            final Long count) {
        final long actual = requireCount(count, -1L, limit - position, "read");
        if (target.position() != position || target.limit() != limit) {
            throw new IllegalStateException("Read target changed before asynchronous completion");
        }
        if (actual == -1L) {
            if (staging.size() != 0L) {
                throw new SocketException("Conduit EOF must not append bytes");
            }
            return -1;
        }
        if (staging.size() != actual) {
            throw new SocketException("Conduit read count did not match appended bytes");
        }
        staging.readTo(target, (int) actual);
        return (int) actual;
    }

    /**
     * Advances one unchanged caller source after a completed write.
     *
     * @param source   caller buffer whose consumed region is advanced
     * @param position source position captured at submission time
     * @param limit    source limit captured at submission time
     * @param staging  internal buffer retaining unwritten bytes
     * @param count    asynchronous write result
     * @return number of source bytes consumed
     */
    private static int advanceWrite(
            final ByteBuffer source,
            final int position,
            final int limit,
            final Buffer staging,
            final Long count) {
        final long actual = requireCount(count, 0L, limit - position, "write");
        if (source.position() != position || source.limit() != limit) {
            throw new IllegalStateException("Write source changed before asynchronous completion");
        }
        if (staging.size() != (long) (limit - position) - actual) {
            throw new SocketException("Conduit write count did not match consumed bytes");
        }
        source.position(position + (int) actual);
        return (int) actual;
    }

    /**
     * Validates an array range and captures positions before an asynchronous operation.
     *
     * @param buffers  caller buffer array
     * @param offset   first included array index
     * @param length   number of included buffers
     * @param writable whether every included buffer must accept writes
     * @return immutable snapshot of the validated range
     */
    private static BufferRange bufferRange(
            final ByteBuffer[] buffers,
            final int offset,
            final int length,
            final boolean writable) {
        if (buffers == null) {
            throw new ValidateException("Buffer array must not be null");
        }
        if (offset < 0 || length < 0 || offset > buffers.length - length) {
            throw new IndexOutOfBoundsException("Invalid buffer array range");
        }
        final int[] positions = new int[length];
        final int[] limits = new int[length];
        long total = 0L;
        for (int index = 0; index < length; index++) {
            final ByteBuffer buffer = buffers[offset + index];
            if (buffer == null) {
                throw new ValidateException("Buffer array element must not be null");
            }
            if (writable && buffer.isReadOnly()) {
                throw new ReadOnlyBufferException();
            }
            positions[index] = buffer.position();
            limits[index] = buffer.limit();
            total = Math.addExact(total, buffer.remaining());
        }
        return new BufferRange(offset, length, total, positions, limits);
    }

    /**
     * Scatters a completed read only after every caller buffer passes its ownership check.
     *
     * @param staging internal buffer containing bytes appended by the read
     * @param targets caller buffers receiving completed bytes
     * @param range   positions and limits captured at submission time
     * @param count   asynchronous read result, including {@code -1} for EOF
     * @return transferred byte count, or {@code -1} for EOF
     */
    private static long scatterRead(
            final Buffer staging,
            final ByteBuffer[] targets,
            final BufferRange range,
            final Long count) {
        final long actual = requireCount(count, -1L, range.total, "scatter read");
        verifyUnchanged(targets, range, "Read target");
        if (actual == -1L) {
            if (staging.size() != 0L) {
                throw new SocketException("Conduit EOF must not append bytes");
            }
            return -1L;
        }
        if (staging.size() != actual) {
            throw new SocketException("Conduit scatter read count did not match appended bytes");
        }
        long remaining = actual;
        for (int index = 0; index < range.length && remaining > 0L; index++) {
            final ByteBuffer target = targets[range.offset + index];
            final int transfer = (int) Math.min(remaining, target.remaining());
            staging.readTo(target, transfer);
            remaining -= transfer;
        }
        return actual;
    }

    /**
     * Advances gathering sources only after every ownership and consumption check succeeds.
     *
     * @param sources caller buffers whose consumed regions are advanced
     * @param range   positions and limits captured at submission time
     * @param staging internal buffer retaining unwritten bytes
     * @param count   asynchronous gathering-write result
     * @return number of source bytes consumed
     */
    private static long advanceGather(
            final ByteBuffer[] sources,
            final BufferRange range,
            final Buffer staging,
            final Long count) {
        final long actual = requireCount(count, 0L, range.total, "gather write");
        verifyUnchanged(sources, range, "Write source");
        if (staging.size() != range.total - actual) {
            throw new SocketException("Conduit gather write count did not match consumed bytes");
        }
        long remaining = actual;
        for (int index = 0; index < range.length && remaining > 0L; index++) {
            final int available = range.limits[index] - range.positions[index];
            final int consumed = (int) Math.min(remaining, available);
            sources[range.offset + index].position(range.positions[index] + consumed);
            remaining -= consumed;
        }
        return actual;
    }

    /**
     * Ensures callers honored ownership until asynchronous completion.
     *
     * @param buffers caller buffers to compare with the captured range
     * @param range   positions and limits captured at submission time
     * @param name    diagnostic buffer role used in failure messages
     */
    private static void verifyUnchanged(final ByteBuffer[] buffers, final BufferRange range, final String name) {
        for (int index = 0; index < range.length; index++) {
            final ByteBuffer buffer = buffers[range.offset + index];
            if (buffer.position() != range.positions[index] || buffer.limit() != range.limits[index]) {
                throw new IllegalStateException(name + " changed before asynchronous completion");
            }
        }
    }

    /**
     * Validates one nullable boxed byte count.
     *
     * @param count     asynchronous byte count to validate
     * @param minimum   smallest permitted result
     * @param maximum   largest permitted result
     * @param operation operation name used in protocol diagnostics
     * @return validated primitive byte count
     */
    private static long requireCount(final Long count, final long minimum, final long maximum, final String operation) {
        if (count == null || count < minimum || count > maximum) {
            throw new SocketException("Conduit " + operation + " returned an invalid byte count");
        }
        return count;
    }

    /**
     * Immutable snapshot of a caller buffer range at asynchronous submission time.
     * <p>
     * Positions and limits are retained separately so completion can detect caller mutation before advancing or
     * scattering bytes.
     * </p>
     */
    final class BufferRange {

        /**
         * First buffer index in the caller array.
         */
        private final int offset;

        /**
         * Number of buffers included in the operation.
         */
        private final int length;

        /**
         * Total bytes available in the captured range at submission time.
         */
        private final long total;

        /**
         * Captured position for each included buffer.
         */
        private final int[] positions;

        /**
         * Captured limit for each included buffer.
         */
        private final int[] limits;

        /**
         * Creates an immutable caller-range snapshot.
         *
         * @param offset    first array index
         * @param length    number of included buffers
         * @param total     total available bytes
         * @param positions captured positions
         * @param limits    captured limits
         */
        private BufferRange(final int offset, final int length, final long total, final int[] positions,
                final int[] limits) {
            this.offset = offset;
            this.length = length;
            this.total = total;
            this.positions = positions;
            this.limits = limits;
        }
    }

}
