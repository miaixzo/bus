/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.lang.thread;

import java.io.Closeable;
import java.io.IOException;

import org.miaixz.bus.core.center.date.StopWatch;

/**
 * 高并发测试工具类
 *
 * <pre>
 * 模拟1000个线程并发
 * ConcurrencyTester ct = new ConcurrencyTester(1000);
 * ct.test(() -&gt; {
 *      // 需要并发测试的业务代码
 * });
 * Console.logger(ct.getInterval());
 * ct.close();
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ConcurrencyTester implements Closeable {

    private final SyncFinisher sf;
    private final StopWatch timeInterval;
    private long interval;

    /**
     * 构造
     *
     * @param threadSize 线程数
     */
    public ConcurrencyTester(final int threadSize) {
        this.sf = new SyncFinisher(threadSize);
        this.timeInterval = new StopWatch();
    }

    /**
     * 执行测试 执行测试后不会关闭线程池，可以调用{@link #close()}释放线程池
     *
     * @param runnable 要测试的内容
     * @return this
     */
    public ConcurrencyTester test(final Runnable runnable) {
        this.sf.clearWorker();

        timeInterval.start();
        this.sf.addRepeatWorker(runnable).setBeginAtSameTime(true).start();

        timeInterval.stop();
        this.interval = timeInterval.getLastTaskTimeMillis();
        return this;
    }

    /**
     * 重置测试器，重置包括：
     *
     * <ul>
     * <li>清空worker</li>
     * <li>重置计时器</li>
     * </ul>
     *
     * @return this
     */
    public ConcurrencyTester reset() {
        this.sf.clearWorker();
        return this;
    }

    /**
     * 获取执行时间
     *
     * @return 执行时间，单位毫秒
     */
    public long getInterval() {
        return this.interval;
    }

    @Override
    public void close() throws IOException {
        this.sf.close();
    }

}
