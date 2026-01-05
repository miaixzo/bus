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

import org.miaixz.bus.socket.metric.channel.AsynchronousSocketChannelProxy;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A plugin for controlling network traffic rate (rate limiting) on socket channels.
 * <p>
 * This plugin applies rate limits to both read and write operations, ensuring that data transfer does not exceed
 * specified thresholds within a given time window.
 * </p>
 *
 * @param <T> the type of message object entity handled by this plugin
 * @author Kimi Liu
 * @since Java 17+
 */
public class RateLimiterPlugin<T> extends AbstractPlugin<T> {

    /**
     * The read rate limit threshold in bytes per second.
     */
    private final int readRateLimiter;
    /**
     * The write rate limit threshold in bytes per second.
     */
    private final int writeRateLimiter;
    /**
     * Flag indicating whether rate limiting is enabled.
     */
    private final boolean enabled;
    /**
     * A small buffer time to account for scheduling delays, in milliseconds.
     */
    private final int bufferTime = 10;
    /**
     * A scheduled executor service for delaying read/write operations when rate limits are hit.
     */
    private ScheduledExecutorService executorService;

    /**
     * Constructs a {@code RateLimiterPlugin} with specified read and write rate limits.
     *
     * @param readRateLimiter  the read rate limit in bytes per second (0 or less to disable read limiting)
     * @param writeRateLimiter the write rate limit in bytes per second (0 or less to disable write limiting)
     */
    public RateLimiterPlugin(int readRateLimiter, int writeRateLimiter) {
        this.readRateLimiter = readRateLimiter;
        this.writeRateLimiter = writeRateLimiter;
        this.enabled = readRateLimiter > 0 || writeRateLimiter > 0;
        if (enabled) {
            executorService = Executors.newSingleThreadScheduledExecutor();
        }
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public AsynchronousSocketChannel shouldAccept(AsynchronousSocketChannel channel) {
        return enabled ? new RateLimiterChannel(channel, readRateLimiter, writeRateLimiter) : channel;
    }

    /**
     * An internal channel proxy that applies rate limiting to read and write operations.
     */
    class RateLimiterChannel extends AsynchronousSocketChannelProxy {

        private final int readRateLimiter;
        private final int writeRateLimiter;
        /**
         * The timestamp of the last read rate limiting window boundary.
         */
        private long latestReadTime;

        /**
         * The number of bytes read within the current rate limiting window.
         */
        private int readSize;
        /**
         * The timestamp of the last write rate limiting window boundary.
         */
        private long latestWriteTime;
        /**
         * The number of bytes written within the current rate limiting window.
         */
        private int writeCount;

        /**
         * Constructs a {@code RateLimiterChannel}.
         *
         * @param asynchronousSocketChannel the underlying {@link AsynchronousSocketChannel}
         * @param readRateLimiter           the read rate limit in bytes per second
         * @param writeRateLimiter          the write rate limit in bytes per second
         */
        public RateLimiterChannel(AsynchronousSocketChannel asynchronousSocketChannel, int readRateLimiter,
                int writeRateLimiter) {
            super(asynchronousSocketChannel);
            this.readRateLimiter = readRateLimiter;
            this.writeRateLimiter = writeRateLimiter;
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
            if (dst.remaining() == 0 || readRateLimiter <= 0) {
                super.read(dst, timeout, unit, attachment, handler);
                return;
            }
            int availReadSize;
            long remainTime = 1000 + latestReadTime - System.currentTimeMillis();
            // Start a new rate limiting window if the previous one has expired
            if (remainTime <= bufferTime) {
                readSize = 0;
                latestReadTime = System.currentTimeMillis();
            }
            availReadSize = Math.min(readRateLimiter - readSize, dst.remaining());
            // If rate limit is triggered, schedule the read operation for later
            if (availReadSize <= 0) {
                executorService.schedule(
                        () -> RateLimiterChannel.this.read(dst, timeout, unit, attachment, handler),
                        remainTime,
                        TimeUnit.MILLISECONDS);
                return;
            }

            int limit = dst.limit();
            // Limit the buffer to prevent exceeding the rate limit
            dst.limit(dst.position() + availReadSize);
            super.read(dst, timeout, unit, attachment, new CompletionHandler<>() {

                /**
                 * Description inherited from parent class or interface.
                 */
                @Override
                public void completed(Integer result, A attachment) {
                    if (result > 0) {
                        // Check if a new rate limiting window should be started
                        if (System.currentTimeMillis() - latestReadTime > 1000) {
                            readSize = 0;
                            latestReadTime = System.currentTimeMillis();
                        } else {
                            readSize += result;
                        }
                    }
                    // Reset the buffer limit
                    dst.limit(limit);
                    handler.completed(result, attachment);
                }

                /**
                 * Description inherited from parent class or interface.
                 */
                @Override
                public void failed(Throwable exc, A attachment) {
                    handler.failed(exc, attachment);
                }
            });
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
            if (src.remaining() == 0 || writeRateLimiter <= 0) {
                super.write(src, timeout, unit, attachment, handler);
                return;
            }
            int availWriteSize;
            long remainTime = 1000 + latestWriteTime - System.currentTimeMillis();
            // Start a new rate limiting window if the previous one has expired
            if (remainTime <= bufferTime) {
                writeCount = 0;
                latestWriteTime = System.currentTimeMillis();
            }
            availWriteSize = Math.min(writeRateLimiter - writeCount, src.remaining());
            // If rate limit is triggered, schedule the write operation for later
            if (availWriteSize <= 0) {
                executorService.schedule(
                        () -> RateLimiterChannel.this.write(src, timeout, unit, attachment, handler),
                        remainTime,
                        TimeUnit.MILLISECONDS);
                return;
            }

            int limit = src.limit();
            // Limit the buffer to prevent exceeding the rate limit
            src.limit(src.position() + availWriteSize);
            super.write(src, timeout, unit, attachment, new CompletionHandler<>() {

                /**
                 * Description inherited from parent class or interface.
                 */
                @Override
                public void completed(Integer result, A attachment) {
                    if (result > 0) {
                        // Check if a new rate limiting window should be started
                        if (System.currentTimeMillis() - latestWriteTime > 1000) {
                            writeCount = 0;
                            latestWriteTime = System.currentTimeMillis();
                        } else {
                            writeCount += result;
                        }
                    }
                    // Reset the buffer limit
                    src.limit(limit);
                    handler.completed(result, attachment);
                }

                /**
                 * Description inherited from parent class or interface.
                 */
                @Override
                public void failed(Throwable exc, A attachment) {
                    handler.failed(exc, attachment);
                }
            });
        }
    }

}
