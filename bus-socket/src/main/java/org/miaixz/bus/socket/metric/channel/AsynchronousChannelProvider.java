/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org sandao and other contributors.             ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.socket.metric.channel;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.*;

/**
 * A custom {@link java.nio.channels.spi.AsynchronousChannelProvider} implementation.
 * <p>
 * This provider allows for the creation of custom {@link java.nio.channels.AsynchronousChannelGroup} and
 * {@link java.nio.channels.AsynchronousSocketChannel} instances, with options for low memory mode.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class AsynchronousChannelProvider extends java.nio.channels.spi.AsynchronousChannelProvider {

    /**
     * Signal indicating a read monitor event.
     */
    public static final int READ_MONITOR_SIGNAL = -2;
    /**
     * Signal indicating that data is available for reading.
     */
    public static final int READABLE_SIGNAL = -3;
    /**
     * Flag indicating whether low memory mode is enabled.
     */
    private final boolean lowMemory;

    /**
     * Constructs a new AsynchronousChannelProvider.
     *
     * @param lowMemory {@code true} to enable low memory mode, {@code false} otherwise.
     */
    public AsynchronousChannelProvider(boolean lowMemory) {
        this.lowMemory = lowMemory;
    }

    /**
     * {@inheritDoc}
     *
     * @param nThreads      the number of threads for the group
     * @param threadFactory the thread factory for creating new threads
     * @return a new asynchronous channel group
     * @throws IOException if an I/O error occurs
     */
    @Override
    public java.nio.channels.AsynchronousChannelGroup openAsynchronousChannelGroup(
            int nThreads,
            ThreadFactory threadFactory) throws IOException {
        return new AsynchronousChannelGroup(this, new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(nThreads), threadFactory), nThreads);
    }

    /**
     * {@inheritDoc}
     *
     * @param executor    the executor service for the group
     * @param initialSize the initial size of the group
     * @return a new asynchronous channel group
     * @throws IOException if an I/O error occurs
     */
    @Override
    public java.nio.channels.AsynchronousChannelGroup openAsynchronousChannelGroup(
            ExecutorService executor,
            int initialSize) throws IOException {
        return new AsynchronousChannelGroup(this, executor, initialSize);
    }

    /**
     * {@inheritDoc}
     *
     * @param group the asynchronous channel group, or {@code null} for the default group
     * @return a new asynchronous server socket channel
     * @throws IOException if an I/O error occurs
     */
    @Override
    public java.nio.channels.AsynchronousServerSocketChannel openAsynchronousServerSocketChannel(
            java.nio.channels.AsynchronousChannelGroup group) throws IOException {
        return new AsynchronousServerSocketChannel(checkAndGet(group), lowMemory);
    }

    /**
     * {@inheritDoc}
     *
     * @param group the asynchronous channel group, or {@code null} for the default group
     * @return a new asynchronous socket channel
     * @throws IOException if an I/O error occurs
     */
    @Override
    public AsynchronousSocketChannel openAsynchronousSocketChannel(java.nio.channels.AsynchronousChannelGroup group)
            throws IOException {
        return new AsynchronousClientChannel(checkAndGet(group), SocketChannel.open(), lowMemory);
    }

    /**
     * Checks if the provided {@link java.nio.channels.AsynchronousChannelGroup} is an instance of
     * {@link AsynchronousChannelGroup} and casts it.
     *
     * @param group the group to check
     * @return the cast {@link AsynchronousChannelGroup}
     * @throws RuntimeException if the group is not an instance of {@link AsynchronousChannelGroup}
     */
    private AsynchronousChannelGroup checkAndGet(java.nio.channels.AsynchronousChannelGroup group) {
        if (!(group instanceof AsynchronousChannelGroup)) {
            throw new RuntimeException("Invalid class: expected AsynchronousChannelGroup");
        }
        return (AsynchronousChannelGroup) group;
    }

}
