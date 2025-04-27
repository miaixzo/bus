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
package org.miaixz.bus.socket.plugin;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.socket.accord.AioServer;
import org.miaixz.bus.socket.buffer.BufferPage;
import org.miaixz.bus.socket.buffer.BufferPagePool;
import org.miaixz.bus.socket.metric.HashedWheelTimer;
import org.miaixz.bus.socket.metric.SocketTask;

/**
 * 内存页监测插件
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BufferPageMonitorPlugin<T> extends AbstractPlugin<T> {

    /**
     * 任务执行频率
     */
    private int seconds;

    private AioServer server;

    private SocketTask future;

    public BufferPageMonitorPlugin(AioServer server, int seconds) {
        this.seconds = seconds;
        this.server = server;
        init();
    }

    private static void dumpBufferPool(BufferPagePool writeBufferPool)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = BufferPagePool.class.getDeclaredField("bufferPages");
        field.setAccessible(true);
        BufferPage[] pages = (BufferPage[]) field.get(writeBufferPool);
        String logger = "";
        for (BufferPage page : pages) {
            logger += "\r\n" + page.toString();
        }
        Logger.info(logger);
    }

    private void init() {
        future = HashedWheelTimer.DEFAULT_TIMER.scheduleWithFixedDelay(() -> {
            {
                if (server == null) {
                    Logger.error("unKnow server or client need to monitor!");
                    shutdown();
                    return;
                }
                try {
                    Field bufferPoolField = AioServer.class.getDeclaredField("writeBufferPool");
                    bufferPoolField.setAccessible(true);
                    BufferPagePool writeBufferPool = (BufferPagePool) bufferPoolField.get(server);
                    if (writeBufferPool == null) {
                        Logger.error("server maybe has not started!");
                        shutdown();
                        return;
                    }
                    Field readBufferPoolField = AioServer.class.getDeclaredField("readBufferPool");
                    readBufferPoolField.setAccessible(true);
                    BufferPagePool readBufferPool = (BufferPagePool) readBufferPoolField.get(server);

                    if (readBufferPool != null && readBufferPool != writeBufferPool) {
                        Logger.info("dump writeBuffer");
                        dumpBufferPool(writeBufferPool);
                        Logger.info("dump readBuffer");
                        dumpBufferPool(readBufferPool);
                    } else {
                        dumpBufferPool(writeBufferPool);
                    }
                } catch (Exception e) {
                    Logger.error("", e);
                }
            }
        }, seconds, TimeUnit.SECONDS);
    }

    private void shutdown() {
        if (future != null) {
            future.cancel();
            future = null;
        }
    }

}
