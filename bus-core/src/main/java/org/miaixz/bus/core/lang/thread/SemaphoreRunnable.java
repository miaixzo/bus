/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
