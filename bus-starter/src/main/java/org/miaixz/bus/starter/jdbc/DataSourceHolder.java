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
package org.miaixz.bus.starter.jdbc;

/**
 * Holds only the explicit routing key for the current thread.
 * <p>
 * The default data source belongs to each routing data source instance and is intentionally not stored here.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class DataSourceHolder {

    /**
     * Thread-local stack containing nested data source keys.
     */
    private static final ThreadLocal<String> CURRENT_KEY = new ThreadLocal<>();

    /**
     * Prevents instantiation of this thread-local data source holder.
     */
    private DataSourceHolder() {
        // No initialization required.
    }

    /**
     * Returns the explicitly selected routing key.
     *
     * @return current key, or {@code null} when the instance default must be used
     */
    public static String getCurrentKey() {
        return CURRENT_KEY.get();
    }

    /**
     * Selects an explicit routing key for the current thread.
     *
     * @param key routing key
     */
    public static void setKey(String key) {
        if (key == null) {
            remove();
        } else {
            CURRENT_KEY.set(key);
        }
    }

    /**
     * Removes the explicit routing key from the current thread.
     */
    public static void remove() {
        CURRENT_KEY.remove();
    }

    /**
     * Opens a nested routing scope.
     *
     * @param key explicit key for this scope
     * @return scope that restores the exact parent state
     */
    public static Scope scope(String key) {
        return new Scope(key);
    }

    /**
     * Idempotent nested routing scope.
     */
    public static final class Scope implements AutoCloseable {

        /**
         * Data source key that was active before this scope was opened.
         */
        private final String previous;

        /**
         * Ensures restoration occurs at most once.
         */
        private boolean closed;

        /**
         * Creates a scope that restores the previous data source key when closed.
         *
         * @param key lookup key
         */
        private Scope(String key) {
            this.previous = CURRENT_KEY.get();
            setKey(key);
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            setKey(previous);
        }

    }

}
