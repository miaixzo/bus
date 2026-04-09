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
package org.miaixz.bus.core.center.queue;

import java.io.Serial;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Predicate;

import org.miaixz.bus.core.lang.Console;
import org.miaixz.bus.core.lang.thread.SimpleScheduler;
import org.miaixz.bus.core.xyz.RuntimeKit;

/**
 * A memory-safe {@link LinkedBlockingQueue} that helps prevent `OutOfMemoryError`. It works by checking the available
 * free memory via `Runtime.getRuntime().freeMemory()`. When the free memory drops below a specified threshold, it stops
 * accepting new elements.
 * <p>
 * This class is inspired by: <a href=
 * "https://github.com/apache/incubator-shenyu/blob/master/shenyu-common/src/main/java/org/apache/shenyu/common/concurrent/MemorySafeLinkedBlockingQueue.java">
 * Apache ShenYu's MemorySafeLinkedBlockingQueue</a>
 *
 * @param <E> The type of elements held in this collection.
 * @author Kimi Liu
 * @since Java 21+
 */
public class MemorySafeLinkedBlockingQueue<E> extends CheckedLinkedBlockingQueue<E> {

    @Serial
    private static final long serialVersionUID = 2852279836896L;

    /**
     * Constructor.
     *
     * @param maxFreeMemory The minimum free memory threshold in bytes. The queue will stop accepting new elements if
     *                      the available free memory is below this value.
     */
    public MemorySafeLinkedBlockingQueue(final long maxFreeMemory) {
        super(new MemoryChecker<>(maxFreeMemory));
    }

    /**
     * Constructor.
     *
     * @param c             The initial collection of elements.
     * @param maxFreeMemory The minimum free memory threshold in bytes.
     */
    public MemorySafeLinkedBlockingQueue(final Collection<? extends E> c, final long maxFreeMemory) {
        super(c, new MemoryChecker<>(maxFreeMemory));
    }

    /**
     * Gets the minimum free memory threshold.
     *
     * @return The minimum free memory threshold in bytes.
     */
    public long getMaxFreeMemory() {
        return ((MemoryChecker<E>) this.checker).maxFreeMemory;
    }

    /**
     * Sets the minimum free memory threshold.
     *
     * @param maxFreeMemory The minimum free memory threshold in bytes.
     */
    public void setMaxFreeMemory(final int maxFreeMemory) {
        ((MemoryChecker<E>) this.checker).maxFreeMemory = maxFreeMemory;
    }

    /**
     * A `Predicate` that checks if there is sufficient free memory.
     *
     * @param <E> The element type.
     */
    private static class MemoryChecker<E> implements Predicate<E> {

        /**
         * The minimum amount of free memory required before accepting a new element.
         */
        private long maxFreeMemory;

        /**
         * Constructor for MemoryChecker.
         * 
         * @param maxFreeMemory The minimum free memory threshold.
         */
        private MemoryChecker(final long maxFreeMemory) {
            this.maxFreeMemory = maxFreeMemory;
        }

        /**
         * Tests if the available memory is greater than the configured threshold.
         * 
         * @param e The element being offered to the queue (not used in the check).
         * @return `true` if there is enough memory, `false` otherwise.
         */
        @Override
        public boolean test(final E e) {
            Console.log(FreeMemoryCalculator.INSTANCE.getResult());
            return FreeMemoryCalculator.INSTANCE.getResult() > maxFreeMemory;
        }
    }

    /**
     * A scheduled task that periodically calculates the available free memory. This avoids calling
     * `Runtime.getRuntime().freeMemory()` on every single queue operation.
     */
    private static class FreeMemoryCalculator extends SimpleScheduler<Long> {

        /**
         * Singleton instance of the calculator.
         */
        private static final FreeMemoryCalculator INSTANCE = new FreeMemoryCalculator();

        /**
         * Private constructor to initialize the scheduled job.
         */
        FreeMemoryCalculator() {
            super(new SimpleScheduler.Job<>() {

                private volatile long maxAvailable = RuntimeKit.getFreeMemory();

                /**
                 * Getresult method.
                 *
                 * @return the Long value
                 */
                @Override
                public Long getResult() {
                    return this.maxAvailable;
                }

                /**
                 * Run method.
                 *
                 * @return the void value
                 */
                @Override
                public void run() {
                    this.maxAvailable = RuntimeKit.getFreeMemory();
                }
            }, 50); // Updates every 50 milliseconds
        }
    }

}
