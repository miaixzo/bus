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
import java.nio.channels.SocketChannel;
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
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.runtime.lifecycle.LifecycleScope;

/**
 * Ingress adapter for server-side accepted socket channels.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Ingress implements Connection, Conduit {

    /**
     * Ingress destination.
     */
    private final Destination destination;

    /**
     * Ingress socket channel.
     */
    private final SocketChannel channel;

    /**
     * Prefetched bytes served before channel reads.
     */
    private final Buffer prefix;

    /**
     * Ingress lifecycle.
     */
    private final LifecycleScope lifecycle;

    /**
     * Source view for protocol readers.
     */
    private final Source source;

    /**
     * Sink view for protocol writers.
     */
    private final Sink sink;

    /**
     * Creates an ingress.
     *
     * @param destination ingress destination
     * @param channel     socket channel
     * @param prefix      prefetched bytes
     */
    private Ingress(final Destination destination, final SocketChannel channel, final Buffer prefix) {
        this.destination = destination;
        this.channel = channel;
        this.prefix = prefix == null ? new Buffer() : prefix;
        this.lifecycle = LifecycleScope.resource(this, "ingress", null, EventObserver.noop());
        this.source = new IngressSource();
        this.sink = new IngressSink();
    }

    /**
     * Creates an opened ingress.
     *
     * @param address peer address
     * @param channel socket channel
     * @param prefix  prefetched bytes
     * @return ingress
     */
    public static Ingress of(final Address address, final SocketChannel channel, final Buffer prefix) {
        final Address currentAddress = Assert
                .notNull(address, () -> new ValidateException("Ingress address must not be null"));
        final SocketChannel currentChannel = Assert
                .notNull(channel, () -> new ValidateException("Ingress channel must not be null"));
        final Ingress ingress = new Ingress(Destination.of(currentAddress.protocol(), currentAddress, Options.empty()),
                currentChannel, prefix);
        ingress.lifecycle.open(ingress);
        return ingress;
    }

    /**
     * Returns the ingress destination.
     *
     * @return destination
     */
    @Override
    public Destination destination() {
        return destination;
    }

    /**
     * Returns this ingress as its conduit.
     *
     * @return conduit
     */
    @Override
    public Conduit conduit() {
        return this;
    }

    /**
     * Returns lifecycle state.
     *
     * @return lifecycle state
     */
    @Override
    public Status state() {
        return lifecycle.state();
    }

    /**
     * Returns the protocol-layer source.
     *
     * @return source view
     */
    @Override
    public Source source() {
        return source;
    }

    /**
     * Returns the protocol-layer sink.
     *
     * @return sink view
     */
    @Override
    public Sink sink() {
        return sink;
    }

    /**
     * Reads bytes from prefix first and then from the channel.
     *
     * @param target    target buffer
     * @param byteCount maximum byte count
     * @return read future
     */
    @Override
    public CompletableFuture<Long> read(final Buffer target, final long byteCount) {
        final Buffer checkedTarget = Assert
                .notNull(target, () -> new ValidateException("Read target must not be null"));
        Assert.isTrue(byteCount >= Normal._0, () -> new ValidateException("Read byte count must not be negative"));
        if (byteCount == Normal._0) {
            return CompletableFuture.completedFuture(0L);
        }
        try {
            if (prefix.size() > Normal._0) {
                return CompletableFuture
                        .completedFuture(prefix.read(checkedTarget, Math.min(byteCount, prefix.size())));
            }
            final ByteBuffer buffer = ByteBuffer.allocate(readCapacity(byteCount));
            final int read = channel.read(buffer);
            if (read == Normal.__1) {
                return CompletableFuture.completedFuture((long) Normal.__1);
            }
            buffer.flip();
            checkedTarget.write(buffer);
            return CompletableFuture.completedFuture((long) read);
        } catch (final IOException e) {
            lifecycle.fail(e);
            return CompletableFuture.failedFuture(new SocketException("Unable to read ingress", e));
        }
    }

    /**
     * Writes source bytes to the channel and consumes written bytes.
     *
     * @param source    source buffer
     * @param byteCount byte count to write
     * @return write future
     */
    @Override
    public CompletableFuture<Long> write(final Buffer source, final long byteCount) {
        final Buffer checkedSource = Assert
                .notNull(source, () -> new ValidateException("Write source must not be null"));
        Assert.isTrue(byteCount >= Normal._0, () -> new ValidateException("Write byte count must not be negative"));
        Assert.isTrue(
                byteCount <= checkedSource.size(),
                () -> new ValidateException("Write byte count must not exceed source size"));
        long written = Normal._0;
        long remaining = byteCount;
        try {
            while (remaining > Normal._0) {
                final ByteBuffer view = checkedSource.nioBuffer(toIntSize(remaining));
                final int count = channel.write(view);
                if (count == Normal._0) {
                    break;
                }
                checkedSource.skip(count);
                written += count;
                remaining -= count;
            }
            return CompletableFuture.completedFuture(written);
        } catch (final IOException e) {
            lifecycle.fail(e);
            return CompletableFuture.failedFuture(new SocketException("Unable to write ingress", e));
        }
    }

    /**
     * Returns whether this ingress is healthy.
     *
     * @return true when healthy
     */
    @Override
    public boolean healthy() {
        return channel.isOpen() && lifecycle.state() == Status.OPENED;
    }

    /**
     * Returns whether this connection is idle.
     *
     * @return true when idle
     */
    @Override
    public boolean idle() {
        return healthy();
    }

    /**
     * Returns whether this conduit is opened.
     *
     * @return true when opened
     */
    @Override
    public boolean opened() {
        return healthy();
    }

    /**
     * Closes this ingress.
     */
    @Override
    public void close() {
        IOException failure = null;
        try {
            channel.close();
        } catch (final IOException e) {
            failure = e;
        } finally {
            lifecycle.close(this);
        }
        if (failure != null) {
            throw new SocketException("Unable to close ingress", failure);
        }
    }

    /**
     * Returns a bounded channel read capacity.
     *
     * @param byteCount requested byte count
     * @return read capacity
     */
    private static int readCapacity(final long byteCount) {
        return toIntSize(Math.min(byteCount, Normal._8192));
    }

    /**
     * Converts a long byte count to an int size accepted by JDK buffers.
     *
     * @param byteCount byte count
     * @return int size
     */
    private static int toIntSize(final long byteCount) {
        return (int) Math.min(byteCount, Integer.MAX_VALUE);
    }

    /**
     * Awaits a core IO operation and converts checked failures.
     *
     * @param future  operation future
     * @param message failure message
     * @return byte count
     * @throws IOException when the operation fails
     */
    private static long await(final CompletableFuture<Long> future, final String message) throws IOException {
        try {
            return Assert.notNull(future, () -> new ValidateException("IO future must not be null")).get();
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
            throw new IOException(message, cause);
        }
    }

    /**
     * Source view backed by the ingress.
     */
    private final class IngressSource implements Source {

        /**
         * Reads bytes through the enclosing ingress.
         *
         * @param sink      target buffer
         * @param byteCount maximum byte count
         * @return read byte count
         * @throws IOException when reading fails
         */
        @Override
        public long read(final Buffer sink, final long byteCount) throws IOException {
            return await(Ingress.this.read(sink, byteCount), "Unable to read ingress source");
        }

        /**
         * Returns the no-op timeout.
         *
         * @return timeout
         */
        @Override
        public Timeout timeout() {
            return Timeout.NONE;
        }

        /**
         * Closes the enclosing ingress.
         */
        @Override
        public void close() {
            Ingress.this.close();
        }

    }

    /**
     * Sink view backed by the ingress.
     */
    private final class IngressSink implements Sink {

        /**
         * Writes bytes through the enclosing ingress.
         *
         * @param source    source buffer
         * @param byteCount byte count
         * @throws IOException when writing fails
         */
        @Override
        public void write(final Buffer source, final long byteCount) throws IOException {
            await(Ingress.this.write(source, byteCount), "Unable to write ingress sink");
        }

        /**
         * Flushes the ingress sink.
         */
        @Override
        public void flush() {
            // SocketChannel writes are flushed by the operating system.
        }

        /**
         * Returns the no-op timeout.
         *
         * @return timeout
         */
        @Override
        public Timeout timeout() {
            return Timeout.NONE;
        }

        /**
         * Closes the enclosing ingress.
         */
        @Override
        public void close() {
            Ingress.this.close();
        }

    }

}
