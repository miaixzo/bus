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

import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.socket.accord.AioServer;
import org.miaixz.bus.socket.buffer.BufferPage;
import org.miaixz.bus.socket.buffer.BufferPagePool;
import org.miaixz.bus.socket.metric.HashedWheelTimer;
import org.miaixz.bus.socket.metric.SocketTask;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

/**
 * A plugin for monitoring {@link BufferPage} usage within {@link BufferPagePool}s.
 * <p>
 * This plugin periodically dumps the status of the write and read buffer pools of an {@link AioServer} to the log,
 * providing insights into memory allocation and usage.
 * </p>
 *
 * @param <T> the type of message object entity handled by this plugin
 * @author Kimi Liu
 * @since Java 17+
 */
public class BufferPageMonitorPlugin<T> extends AbstractPlugin<T> {

    /**
     * The frequency (in seconds) at which the monitoring task is executed.
     */
    private int seconds;

    /**
     * The {@link AioServer} instance whose buffer pools are being monitored.
     */
    private AioServer server;

    /**
     * The scheduled task for monitoring buffer pages.
     */
    private SocketTask future;

    /**
     * Constructs a {@code BufferPageMonitorPlugin}.
     *
     * @param server  the {@link AioServer} to monitor
     * @param seconds the monitoring frequency in seconds
     */
    public BufferPageMonitorPlugin(AioServer server, int seconds) {
        this.seconds = seconds;
        this.server = server;
        init();
    }

    /**
     * Dumps the current state of a {@link BufferPagePool} to the log.
     *
     * @param writeBufferPool the {@link BufferPagePool} to dump
     * @throws NoSuchFieldException   if the 'bufferPages' field is not found via reflection
     * @throws IllegalAccessException if access to the 'bufferPages' field is denied
     */
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

    /**
     * Initializes the scheduled monitoring task.
     */
    private void init() {
        future = HashedWheelTimer.DEFAULT_TIMER.scheduleWithFixedDelay(() -> {
            {
                if (server == null) {
                    Logger.error("Unknown server or client needs to be monitored!");
                    shutdown();
                    return;
                }
                try {
                    Field bufferPoolField = AioServer.class.getDeclaredField("writeBufferPool");
                    bufferPoolField.setAccessible(true);
                    BufferPagePool writeBufferPool = (BufferPagePool) bufferPoolField.get(server);
                    if (writeBufferPool == null) {
                        Logger.error("Server may not have started yet!");
                        shutdown();
                        return;
                    }
                    Field readBufferPoolField = AioServer.class.getDeclaredField("readBufferPool");
                    readBufferPoolField.setAccessible(true);
                    BufferPagePool readBufferPool = (BufferPagePool) readBufferPoolField.get(server);

                    if (readBufferPool != null && readBufferPool != writeBufferPool) {
                        Logger.info("Dumping writeBufferPool:");
                        dumpBufferPool(writeBufferPool);
                        Logger.info("Dumping readBufferPool:");
                        dumpBufferPool(readBufferPool);
                    } else {
                        Logger.info("Dumping bufferPool:");
                        dumpBufferPool(writeBufferPool);
                    }
                } catch (Exception e) {
                    Logger.error("Error during buffer pool monitoring", e);
                }
            }
        }, seconds, TimeUnit.SECONDS);
    }

    /**
     * Shuts down the monitoring task.
     */
    private void shutdown() {
        if (future != null) {
            future.cancel();
            future = null;
        }
    }

}
