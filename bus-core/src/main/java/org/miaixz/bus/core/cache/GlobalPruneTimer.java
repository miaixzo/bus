/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.cache;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.ThreadKit;

/**
 * 全局缓存清理定时器池，用于在需要过期支持的缓存对象中超时任务池
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum GlobalPruneTimer {

    /**
     * 单例对象
     */
    INSTANCE;

    /**
     * 缓存任务计数
     */
    private final AtomicInteger cacheTaskNumber = new AtomicInteger(1);

    /**
     * 定时器
     */
    private ScheduledExecutorService pruneTimer;

    /**
     * 构造
     */
    GlobalPruneTimer() {
        init();
    }

    /**
     * 启动定时任务
     *
     * @param task  任务
     * @param delay 周期
     * @return {@link ScheduledFuture}对象，可手动取消此任务
     */
    public ScheduledFuture<?> schedule(final Runnable task, final long delay) {
        return this.pruneTimer.scheduleAtFixedRate(task, delay, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * 初始化定时器
     */
    public void init() {
        if (null != pruneTimer) {
            shutdownNow();
        }
        this.pruneTimer = new ScheduledThreadPoolExecutor(1,
                r -> ThreadKit.newThread(r, StringKit.format("Pure-Timer-{}", cacheTaskNumber.getAndIncrement())));
    }

    /**
     * 销毁全局定时器
     */
    public void shutdown() {
        if (null != pruneTimer) {
            pruneTimer.shutdown();
        }
    }

    /**
     * 销毁全局定时器
     *
     * @return 销毁时未被执行的任务列表
     */
    public List<Runnable> shutdownNow() {
        if (null != pruneTimer) {
            return pruneTimer.shutdownNow();
        }
        return null;
    }

}
