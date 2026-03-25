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
package org.miaixz.bus.socket.plugin;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.socket.metric.HashedWheelTimer;
import org.miaixz.bus.socket.metric.SocketTask;
import org.miaixz.bus.socket.metric.channel.AsynchronousSocketChannelProxy;

/**
 * A plugin for monitoring idle I/O states.
 *
 * @author Kimi Liu
 * @since Java 21+
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
            throw new IllegalArgumentException("readIdle and writeIdle both disabled");
        }
        this.idleTimeout = idleTimeout;
        this.writeMonitor = writeMonitor;
        this.readMonitor = readMonitor;
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public AsynchronousSocketChannel shouldAccept(AsynchronousSocketChannel channel) {
        return new IdleMonitorChannel(channel);
    }

    /**
     * A proxy channel that monitors idle time.
     */
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
                            Logger.debug(
                                    "close session:{} by IdleStatePlugin",
                                    asynchronousSocketChannel.getRemoteAddress());
                        }
                        close();
                    } catch (IOException e) {
                        Logger.debug("close exception", e);
                    }
                }
            }, IdleStatePlugin.this.idleTimeout, TimeUnit.MILLISECONDS);
        }

        /**
         * Description inherited from parent class or interface.
         */
        @Override
        public <A> void read(
                ByteBuffer dst,
                long timeout,
                TimeUnit unit,
                A attachment,
                CompletionHandler<Integer, ? super A> handler) {
            if (IdleStatePlugin.this.readMonitor) {
                readTimestamp = System.currentTimeMillis();
            }
            super.read(dst, timeout, unit, attachment, handler);
        }

        /**
         * Description inherited from parent class or interface.
         */
        @Override
        public <A> void write(
                ByteBuffer src,
                long timeout,
                TimeUnit unit,
                A attachment,
                CompletionHandler<Integer, ? super A> handler) {
            if (IdleStatePlugin.this.writeMonitor) {
                writeTimestamp = System.currentTimeMillis();
            }
            super.write(src, timeout, unit, attachment, handler);
        }

        /**
         * Description inherited from parent class or interface.
         */
        @Override
        public void close() throws IOException {
            if (task != null) {
                task.cancel();
            }
            super.close();
        }
    }

}
