/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org sandao and other contributors.         ~
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
