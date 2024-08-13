/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org sandao and other contributors.             ~
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
package org.miaixz.bus.socket.plugin;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.socket.metric.HashedWheelTimer;
import org.miaixz.bus.socket.metric.SocketTask;
import org.miaixz.bus.socket.metric.channels.AsynchronousSocketChannelProxy;

/**
 * 空闲IO状态监听插件
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class IdleStatePlugin<T> extends AbstractPlugin<T> {

    private static final HashedWheelTimer timer = new HashedWheelTimer(r -> {
        Thread thread = new Thread(r, "idleStateMonitor");
        thread.setDaemon(true);
        return thread;
    });

    private final int idleTimeout;

    private final boolean writeMonitor;
    private final boolean readMonitor;

    public IdleStatePlugin(int idleTimeout) {
        this(idleTimeout, true, true);
    }

    public IdleStatePlugin(int idleTimeout, boolean readMonitor, boolean writeMonitor) {
        if (idleTimeout <= 0) {
            throw new IllegalArgumentException("invalid idleTimeout");
        }
        if (!writeMonitor && !readMonitor) {
            throw new IllegalArgumentException("readIdle and writeIdle both disable");
        }
        this.idleTimeout = idleTimeout;
        this.writeMonitor = writeMonitor;
        this.readMonitor = readMonitor;
    }

    @Override
    public AsynchronousSocketChannel shouldAccept(AsynchronousSocketChannel channel) {
        return new IdleMonitorChannel(channel);
    }

    class IdleMonitorChannel extends AsynchronousSocketChannelProxy {
        SocketTask task;
        long readTimestamp;
        long writeTimestamp;

        public IdleMonitorChannel(AsynchronousSocketChannel asynchronousSocketChannel) {
            super(asynchronousSocketChannel);
            if (!IdleStatePlugin.this.readMonitor) {
                readTimestamp = Long.MAX_VALUE;
            }
            if (!IdleStatePlugin.this.writeMonitor) {
                writeTimestamp = Long.MAX_VALUE;
            }
            this.task = timer.scheduleWithFixedDelay(() -> {
                long currentTime = System.currentTimeMillis();
                if ((currentTime - readTimestamp) > IdleStatePlugin.this.idleTimeout
                        || (currentTime - writeTimestamp) > IdleStatePlugin.this.idleTimeout) {
                    try {
                        if (asynchronousSocketChannel.isOpen() && Logger.isDebugEnabled()) {
                            Logger.debug("close session:{} by IdleStatePlugin",
                                    asynchronousSocketChannel.getRemoteAddress());
                        }
                        close();
                    } catch (IOException e) {
                        Logger.debug("close exception", e);
                    }
                }
            }, IdleStatePlugin.this.idleTimeout, TimeUnit.MILLISECONDS);
        }

        @Override
        public <A> void read(ByteBuffer dst, long timeout, TimeUnit unit, A attachment,
                CompletionHandler<Integer, ? super A> handler) {
            if (IdleStatePlugin.this.readMonitor) {
                readTimestamp = System.currentTimeMillis();
            }
            super.read(dst, timeout, unit, attachment, handler);
        }

        @Override
        public <A> void write(ByteBuffer src, long timeout, TimeUnit unit, A attachment,
                CompletionHandler<Integer, ? super A> handler) {
            if (IdleStatePlugin.this.writeMonitor) {
                writeTimestamp = System.currentTimeMillis();
            }
            super.write(src, timeout, unit, attachment, handler);
        }

        @Override
        public void close() throws IOException {
            task.cancel();
            super.close();
        }
    }
}
