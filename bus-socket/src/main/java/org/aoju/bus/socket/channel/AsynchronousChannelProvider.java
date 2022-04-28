/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org sandao and other contributors.               *
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
package org.aoju.bus.socket.channel;

import org.aoju.bus.core.exception.InstrumentException;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.*;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class AsynchronousChannelProvider extends java.nio.channels.spi.AsynchronousChannelProvider {

    @Override
    public java.nio.channels.AsynchronousChannelGroup openAsynchronousChannelGroup(int nThreads, ThreadFactory threadFactory) throws IOException {
        return new AsynchronousChannelGroup(this, new ThreadPoolExecutor(nThreads, nThreads,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(nThreads),
                threadFactory), nThreads);
    }

    @Override
    public java.nio.channels.AsynchronousChannelGroup openAsynchronousChannelGroup(ExecutorService executor, int initialSize) throws IOException {
        return new AsynchronousChannelGroup(this, executor, initialSize);
    }

    @Override
    public java.nio.channels.AsynchronousServerSocketChannel openAsynchronousServerSocketChannel(java.nio.channels.AsynchronousChannelGroup group) throws IOException {
        return new AsynchronousServerSocketChannel(checkAndGet(group));
    }

    @Override
    public java.nio.channels.AsynchronousSocketChannel openAsynchronousSocketChannel(java.nio.channels.AsynchronousChannelGroup group) throws IOException {
        return new AsynchronousSocketChannel(checkAndGet(group), SocketChannel.open());
    }

    private AsynchronousChannelGroup checkAndGet(java.nio.channels.AsynchronousChannelGroup group) {
        if (!(group instanceof AsynchronousChannelGroup)) {
            throw new InstrumentException("invalid class");
        }
        return (AsynchronousChannelGroup) group;
    }

}
