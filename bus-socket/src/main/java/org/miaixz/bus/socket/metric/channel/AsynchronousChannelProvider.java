/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org sandao and other contributors.             ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
     * Description inherited from parent class or interface.
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
     * Description inherited from parent class or interface.
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
     * Description inherited from parent class or interface.
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
     * Description inherited from parent class or interface.
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
