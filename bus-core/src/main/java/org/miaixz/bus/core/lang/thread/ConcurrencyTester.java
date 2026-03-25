/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
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
 * @since Java 21+
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
