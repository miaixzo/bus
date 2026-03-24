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
package org.miaixz.bus.mapper.support.visible;

import java.util.function.Supplier;

/**
 * Permission context for managing perimeter control state.
 *
 * <p>
 * This class provides a thread-local context for temporarily disabling perimeter filtering. It is useful in scenarios
 * where you need to bypass permission checks, such as:
 * </p>
 * <ul>
 * <li>Administrative operations</li>
 * <li>System-level queries</li>
 * <li>Batch operations</li>
 * <li>Background jobs</li>
 * </ul>
 *
 * <p>
 * Usage examples:
 * </p>
 *
 * <pre>{@code
 * // Method 1: Using try-with-resources (recommended)
 * try (VisibleContext.Ignore ignored = VisibleContext.ignore()) {
 *     List<Order> allOrders = orderMapper.selectAll(); // No perimeter filtering
 * }
 *
 * // Method 2: Using runIgnore
 * List<Order> allOrders = VisibleContext.runIgnore(() -> orderMapper.selectAll());
 *
 * // Method 3: Manual control
 * VisibleContext.setIgnore(true);
 * try {
 *     List<Order> allOrders = orderMapper.selectAll();
 * } finally {
 *     VisibleContext.clear();
 * }
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class VisibleContext {

    /**
     * Thread-local storage for ignore flag.
     */
    private static final ThreadLocal<Boolean> IGNORE_PERMISSION = ThreadLocal.withInitial(() -> false);

    /**
     * Private constructor to prevent instantiation.
     */
    private VisibleContext() {
    }

    /**
     * Check if perimeter filtering should be ignored.
     *
     * @return true if perimeter filtering should be ignored, false otherwise
     */
    public static boolean isIgnore() {
        return IGNORE_PERMISSION.get();
    }

    /**
     * Set whether to ignore perimeter filtering.
     *
     * @param ignore true to ignore perimeter filtering, false otherwise
     */
    public static void setIgnore(boolean ignore) {
        IGNORE_PERMISSION.set(ignore);
    }

    /**
     * Clear the ignore flag.
     */
    public static void clear() {
        IGNORE_PERMISSION.remove();
    }

    /**
     * Create an ignore scope that automatically restores the previous state.
     *
     * <p>
     * This method is designed to be used with try-with-resources for automatic cleanup:
     * </p>
     *
     * <pre>{@code
     * try (VisibleContext.Ignore ignored = VisibleContext.ignore()) {
     *     // Permission filtering is disabled here
     *     List<Order> allOrders = orderMapper.selectAll();
     * }
     * // Permission filtering is automatically restored here
     * }</pre>
     *
     * @return an AutoCloseable that restores the previous ignore state
     */
    public static Ignore ignore() {
        boolean previousState = IGNORE_PERMISSION.get();
        IGNORE_PERMISSION.set(true);
        return () -> IGNORE_PERMISSION.set(previousState);
    }

    /**
     * Execute a task with perimeter filtering disabled.
     *
     * <p>
     * This method temporarily disables perimeter filtering for the duration of the task execution, then automatically
     * restores the previous state.
     * </p>
     *
     * <pre>{@code
     * 
     * List<Order> allOrders = VisibleContext.runIgnore(() -> orderMapper.selectAll());
     * }</pre>
     *
     * @param task the task to execute
     * @param <T>  the return type
     * @return the result of the task execution
     */
    public static <T> T runIgnore(Supplier<T> task) {
        try (Ignore ignored = ignore()) {
            return task.get();
        }
    }

    /**
     * Execute a task with perimeter filtering disabled (no return value).
     *
     * <p>
     * This method temporarily disables perimeter filtering for the duration of the task execution, then automatically
     * restores the previous state.
     * </p>
     *
     * <pre>{@code
     * VisibleContext.runIgnore(() -> {
     *     orderMapper.deleteAll();
     * });
     * }</pre>
     *
     * @param task the task to execute
     */
    public static void runIgnore(Runnable task) {
        try (Ignore ignored = ignore()) {
            task.run();
        }
    }

    /**
     * AutoCloseable interface for automatic state restoration.
     */
    @FunctionalInterface
    public interface Ignore extends AutoCloseable {

        @Override
        void close();

    }

}
