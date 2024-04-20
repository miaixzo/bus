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
package org.miaixz.bus.socket.plugins;

import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.socket.AioQuickServer;
import org.miaixz.bus.socket.QuickTimer;
import org.miaixz.bus.socket.buffers.BufferPage;
import org.miaixz.bus.socket.buffers.BufferPool;

import java.lang.reflect.Field;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
    private int seconds = 0;

    private AioQuickServer server;

    private ScheduledFuture<?> future;

    public BufferPageMonitorPlugin(AioQuickServer server, int seconds) {
        this.seconds = seconds;
        this.server = server;
        init();
    }

    private void init() {
        long mills = TimeUnit.SECONDS.toMillis(seconds);
        future = QuickTimer.scheduleAtFixedRate(() -> {
            {
                if (server == null) {
                    Logger.error("unKnow server or client need to monitor!");
                    shutdown();
                    return;
                }
                try {
                    Field bufferPoolField = AioQuickServer.class.getDeclaredField("bufferPool");
                    bufferPoolField.setAccessible(true);
                    BufferPool pagePool = (BufferPool) bufferPoolField.get(server);
                    if (pagePool == null) {
                        Logger.error("server maybe has not started!");
                        shutdown();
                        return;
                    }
                    Field field = BufferPool.class.getDeclaredField("bufferPages");
                    field.setAccessible(true);
                    BufferPage[] pages = (BufferPage[]) field.get(pagePool);
                    String logger = "";
                    for (BufferPage page : pages) {
                        logger += "\r\n" + page.toString();
                    }
                    Logger.info(logger);
                } catch (Exception e) {
                    Logger.error("", e);
                }
            }
        }, mills, mills);
    }

    private void shutdown() {
        if (future != null) {
            future.cancel(true);
            future = null;
        }
    }
}
