/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
 * A utility class for high concurrency testing. It allows simulating a specified number of threads executing a given
 * task concurrently and measures the total execution time.
 *
 * <p>
 * Example usage:
 * 
 * <pre>
 * // Simulate 1000 concurrent threads
 * ConcurrencyTester ct = new ConcurrencyTester(1000);
 * ct.test(() -> {
 *     // Business logic to be concurrently tested
 * });
 * System.out.println(ct.getInterval());
 * ct.close();
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ConcurrencyTester implements Closeable {

    /**
     * The {@link SyncFinisher} used to manage and synchronize the concurrent execution of tasks.
     */
    private final SyncFinisher sf;
    /**
     * The {@link StopWatch} used to measure the total time taken for all concurrent tasks to complete.
     */
    private final StopWatch timeInterval;
    /**
     * The measured execution time in milliseconds for the last test run.
     */
    private long interval;

    /**
     * Constructs a new {@code ConcurrencyTester} with the specified number of threads.
     *
     * @param threadSize The number of threads to simulate for concurrent execution.
     */
    public ConcurrencyTester(final int threadSize) {
        this.sf = new SyncFinisher(threadSize);
        this.timeInterval = new StopWatch();
    }

    /**
     * Executes the given {@link Runnable} concurrently using the configured number of threads. After the test, the
     * thread pool will not be automatically closed. Call {@link #close()} to release resources.
     *
     * @param runnable The {@link Runnable} task to be executed concurrently.
     * @return This {@code ConcurrencyTester} instance for method chaining.
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
     * Resets the tester, which includes:
     *
     * <ul>
     * <li>Clearing all registered worker tasks.</li>
     * <li>Resetting the internal timer.</li>
     * </ul>
     *
     * @return This {@code ConcurrencyTester} instance for method chaining.
     */
    public ConcurrencyTester reset() {
        this.sf.clearWorker();
        return this;
    }

    /**
     * Retrieves the execution time of the last concurrent test run.
     *
     * @return The execution time in milliseconds.
     */
    public long getInterval() {
        return this.interval;
    }

    /**
     * Closes the underlying {@link SyncFinisher} and releases its resources.
     *
     * @throws IOException If an I/O error occurs during closing.
     */
    @Override
    public void close() throws IOException {
        this.sf.close();
    }

}
