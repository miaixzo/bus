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
package org.miaixz.bus.socket.buffer;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A pool of {@link BufferPage} instances for managing {@link java.nio.ByteBuffer} memory.
 * <p>
 * This class provides a pool of buffer pages to reduce the overhead of memory allocation and garbage collection. It
 * uses a round-robin strategy to distribute allocations across the available pages and employs a daemon thread to
 * periodically clean up and reclaim unused memory.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class BufferPagePool {

    /**
     * A default, shared instance of the buffer page pool. This instance is configured with zero-sized pages,
     * effectively acting as a no-op pool where allocations fall back to creating new buffers.
     */
    public static final BufferPagePool DEFAULT_BUFFER_PAGE_POOL = new BufferPagePool(0, 1, false);
    /**
     * A daemon thread executor for periodically reclaiming memory resources from the buffer pages.
     */
    private static final ScheduledThreadPoolExecutor BUFFER_POOL_CLEAN = new ScheduledThreadPoolExecutor(1, r -> {
        Thread thread = new Thread(r, "BufferPoolClean");
        thread.setDaemon(true);
        return thread;
    });
    /**
     * A cursor for round-robin allocation of buffer pages.
     */
    private final AtomicInteger cursor = new AtomicInteger(0);
    /**
     * An array of {@link BufferPage} instances that constitute the pool.
     */
    private BufferPage[] bufferPages;
    /**
     * A flag indicating whether the pool is active and can be used for allocations.
     */
    private boolean enabled = true;

    /**
     * Constructs a new BufferPagePool.
     *
     * @param pageSize the size of each buffer page
     * @param pageNum  the number of buffer pages in the pool
     * @param isDirect whether to use direct (off-heap) buffers
     */
    public BufferPagePool(final int pageSize, final int pageNum, final boolean isDirect) {
        bufferPages = new BufferPage[pageNum];
        for (int i = 0; i < pageNum; i++) {
            bufferPages[i] = new BufferPage(pageSize, isDirect);
        }
        if (pageNum == 0 || pageSize == 0) {
            future.cancel(false);
        }
    }

    /**
     * Allocates a {@link BufferPage} from the pool.
     * <p>
     * This method uses a round-robin strategy to select a page from the pool, ensuring balanced usage.
     * </p>
     *
     * @return an allocated {@link BufferPage}
     * @throws IllegalStateException if the buffer pool has been disabled
     */
    public BufferPage allocateBufferPage() {
        if (enabled) {
            // Use a round-robin cursor to distribute allocations evenly across pages
            return bufferPages[(cursor.getAndIncrement() & Integer.MAX_VALUE) % bufferPages.length];
        }
        throw new IllegalStateException("Buffer pool is disabled");
    }

    /**
     * Disables the buffer pool. After this method is called, {@link #allocateBufferPage()} will throw an
     * {@link IllegalStateException}. The background cleanup task will handle the final release of resources.
     */
    public void release() {
        enabled = false;
    }

    /**
     * The scheduled task for performing periodic memory cleanup.
     * <p>
     * When the pool is enabled, this task periodically calls {@link BufferPage#tryClean()} on each page. When the pool
     * is disabled, it releases all resources held by the pages and cancels itself.
     * </p>
     */
    private final ScheduledFuture<?> future = BUFFER_POOL_CLEAN.scheduleWithFixedDelay(new Runnable() {

        @Override
        public void run() {
            if (enabled) {
                for (BufferPage bufferPage : bufferPages) {
                    bufferPage.tryClean();
                }
            } else {
                if (bufferPages != null) {
                    for (BufferPage page : bufferPages) {
                        page.release();
                    }
                    bufferPages = null;
                }
                future.cancel(false);
            }
        }
    }, 500, 1000, TimeUnit.MILLISECONDS);

}
