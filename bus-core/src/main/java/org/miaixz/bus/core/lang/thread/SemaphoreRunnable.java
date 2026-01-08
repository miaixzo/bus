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

import java.util.concurrent.Semaphore;

/**
 * An abstract implementation of the {@link Runnable} interface that incorporates {@link Semaphore} control.
 * <p>
 * By using a semaphore, the number of threads that can access certain resources (physical or logical) can be limited.
 * For example, if the semaphore is set to 2, a maximum of two threads can execute the logic concurrently, while other
 * threads will wait until the current threads complete their execution.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SemaphoreRunnable implements Runnable {

    /**
     * The actual {@link Runnable} logic to be executed.
     */
    private final Runnable runnable;
    /**
     * The {@link Semaphore} used to control concurrent access.
     */
    private final Semaphore semaphore;

    /**
     * Constructs a new {@code SemaphoreRunnable} with the specified runnable task and semaphore.
     *
     * @param runnable  The actual {@link Runnable} task to be executed.
     * @param semaphore The {@link Semaphore} instance. Multiple threads must share the same semaphore instance.
     */
    public SemaphoreRunnable(final Runnable runnable, final Semaphore semaphore) {
        this.runnable = runnable;
        this.semaphore = semaphore;
    }

    /**
     * Retrieves the {@link Semaphore} instance associated with this runnable.
     *
     * @return The {@link Semaphore} instance.
     */
    public Semaphore getSemaphore() {
        return this.semaphore;
    }

    /**
     * Executes the wrapped runnable task, acquiring a permit from the semaphore before execution and releasing it
     * afterwards. If the thread is interrupted while acquiring a permit, its interrupted status is set.
     */
    @Override
    public void run() {
        if (null != this.semaphore) {
            try {
                semaphore.acquire();
                try {
                    this.runnable.run();
                } finally {
                    semaphore.release();
                }
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
