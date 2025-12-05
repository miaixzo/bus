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
 * @since Java 17+
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
