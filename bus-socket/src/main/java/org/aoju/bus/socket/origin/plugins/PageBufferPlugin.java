/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.socket.origin.plugins;

import org.aoju.bus.core.io.BufferPool;
import org.aoju.bus.core.io.PageBuffer;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.socket.origin.AioQuickServer;
import org.aoju.bus.socket.origin.QuickTimer;

import java.lang.reflect.Field;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * 内存页监测插件
 *
 * @author Kimi Liu
 * @version 6.1.0
 * @since JDK 1.8+
 */
public class PageBufferPlugin<T> extends AbstractPlugin {

    /**
     * 任务执行频率
     */
    private int seconds = 0;

    private AioQuickServer<T> server;

    public PageBufferPlugin(AioQuickServer<T> server, int seconds) {
        this.seconds = seconds;
        this.server = server;
        init();
    }

    private void init() {
        long mills = TimeUnit.SECONDS.toMillis(seconds);
        QuickTimer.getTimer().schedule(new TimerTask() {
            @Override
            public void run() {
                {
                    if (server == null) {
                        Logger.error("unKnow server or client need to monitor!");
                        return;
                    }
                    try {
                        Field bufferPoolField = AioQuickServer.class.getDeclaredField("bufferPool");
                        bufferPoolField.setAccessible(true);
                        BufferPool pagePool = (BufferPool) bufferPoolField.get(server);
                        if (pagePool == null) {
                            Logger.error("server maybe has not started!");
                            return;
                        }
                        Field field = BufferPool.class.getDeclaredField("bufferPageList");
                        field.setAccessible(true);
                        PageBuffer[] pages = (PageBuffer[]) field.get(pagePool);
                        String logger = Normal.EMPTY;
                        for (PageBuffer page : pages) {
                            logger += Symbol.CRLF + page.toString();
                        }
                        Logger.info(logger);
                    } catch (Exception e) {
                        Logger.error(Normal.EMPTY, e);
                    }
                }
            }
        }, mills, mills);
    }

}
