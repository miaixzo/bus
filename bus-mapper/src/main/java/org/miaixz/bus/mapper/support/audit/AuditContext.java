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
package org.miaixz.bus.mapper.support.audit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Audit Context, uses ThreadLocal to store audit information for the current thread
 *
 * <p>
 * Provides thread-level audit record storage, supports passing audit information before and after SQL execution. Mainly
 * used for:
 * </p>
 * <ul>
 * <li>Storing audit records for current SQL</li>
 * <li>Passing context data such as user information, request information</li>
 * <li>Supporting audit for nested SQL calls</li>
 * </ul>
 *
 * <p>
 * Usage example:
 * </p>
 *
 * <pre>{@code
 * // Set user information
 * AuditContext.setContextInfo("userId", "12345");
 * AuditContext.setContextInfo("userName", "John Doe");
 *
 * // Execute SQL operation
 * userMapper.selectById(1);
 *
 * // Clear context
 * AuditContext.clear();
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class AuditContext {

    /**
     * Store audit record for current thread
     */
    private static final ThreadLocal<AuditRecord> RECORD = new ThreadLocal<>();

    /**
     * Store context information for current thread
     */
    private static final ThreadLocal<Map<String, Object>> CONTEXT = ThreadLocal.withInitial(ConcurrentHashMap::new);

    /**
     * Store whether to ignore audit for current thread
     */
    private static final ThreadLocal<Boolean> IGNORE = ThreadLocal.withInitial(() -> false);

    /**
     * Private constructor to prevent instantiation
     */
    private AuditContext() {

    }

    /**
     * Get audit record for current thread
     *
     * @return Audit record, returns null if not exists
     */
    public static AuditRecord getRecord() {
        return RECORD.get();
    }

    /**
     * Set audit record for current thread
     *
     * @param record Audit record
     */
    public static void setRecord(AuditRecord record) {
        RECORD.set(record);
    }

    /**
     * Remove audit record for current thread
     */
    public static void removeRecord() {
        RECORD.remove();
    }

    /**
     * Get context information for current thread
     *
     * @return Context information Map
     */
    public static Map<String, Object> get() {
        return CONTEXT.get();
    }

    /**
     * Set context information
     *
     * @param key   Key
     * @param value Value
     */
    public static void set(String key, Object value) {
        CONTEXT.get().put(key, value);
    }

    /**
     * Get context information
     *
     * @param key Key
     * @return the value associated with the key, or null if not found
     */
    public static Object get(String key) {
        return CONTEXT.get().get(key);
    }

    /**
     * Remove context information
     *
     * @param key Key
     */
    public static void remove(String key) {
        CONTEXT.get().remove(key);
    }

    /**
     * Clear context information
     */
    public static void clear() {
        CONTEXT.get().clear();
    }

    /**
     * Determine whether to ignore audit
     *
     * @return true to ignore, false not to ignore
     */
    public static boolean isIgnore() {
        return Boolean.TRUE.equals(IGNORE.get());
    }

    /**
     * Set whether to ignore audit
     *
     * @param ignore true to ignore, false not to ignore
     */
    public static void setIgnore(boolean ignore) {
        IGNORE.set(ignore);
    }

    /**
     * Execute operation while ignoring audit
     *
     * @param runnable Operation to execute
     */
    public static void runIgnore(Runnable runnable) {
        boolean originalIgnore = isIgnore();
        try {
            setIgnore(true);
            runnable.run();
        } finally {
            setIgnore(originalIgnore);
        }
    }

    /**
     * Execute operation while ignoring audit and return result
     *
     * @param supplier Operation to execute
     * @param <T>      Return value type
     * @return Return value of the operation
     */
    public static <T> T callIgnore(java.util.function.Supplier<T> supplier) {
        boolean originalIgnore = isIgnore();
        try {
            setIgnore(true);
            return supplier.get();
        } finally {
            setIgnore(originalIgnore);
        }
    }

    /**
     * Clear all audit context (including audit record, context information and ignore flag)
     */
    public static void clearAll() {
        RECORD.remove();
        CONTEXT.remove();
        IGNORE.remove();
    }

}
