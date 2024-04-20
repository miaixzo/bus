/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org sandao and other contributors.             *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.socket.channel;

import java.io.IOException;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.concurrent.*;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public final class EnhanceAsynchronousChannelProvider extends AsynchronousChannelProvider {
    /**
     * 读监听信号
     */
    public static final int READ_MONITOR_SIGNAL = -2;
    /**
     * 可读信号
     */
    public static final int READABLE_SIGNAL = -3;
    /**
     * 低内存模式
     */
    private final boolean lowMemory;

    public EnhanceAsynchronousChannelProvider(boolean lowMemory) {
        this.lowMemory = lowMemory;
    }

    public EnhanceAsynchronousChannelProvider() {
        this(false);
    }

    @Override
    public AsynchronousChannelGroup openAsynchronousChannelGroup(int nThreads, ThreadFactory threadFactory) throws IOException {
        return new EnhanceAsynchronousChannelGroup(this, new ThreadPoolExecutor(nThreads, nThreads,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(nThreads),
                threadFactory), nThreads);
    }

    @Override
    public AsynchronousChannelGroup openAsynchronousChannelGroup(ExecutorService executor, int initialSize) throws IOException {
        return new EnhanceAsynchronousChannelGroup(this, executor, initialSize);
    }

    @Override
    public AsynchronousServerSocketChannel openAsynchronousServerSocketChannel(AsynchronousChannelGroup group) throws IOException {
        return new EnhanceAsynchronousServerSocketChannel(checkAndGet(group), lowMemory);
    }

    @Override
    public AsynchronousSocketChannel openAsynchronousSocketChannel(AsynchronousChannelGroup group) throws IOException {
        return new EnhanceAsynchronousSocketChannel(checkAndGet(group), SocketChannel.open(), lowMemory);
    }

    private EnhanceAsynchronousChannelGroup checkAndGet(AsynchronousChannelGroup group) {
        if (!(group instanceof EnhanceAsynchronousChannelGroup)) {
            throw new RuntimeException("invalid class");
        }
        return (EnhanceAsynchronousChannelGroup) group;
    }

}
