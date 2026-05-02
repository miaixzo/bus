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
package org.miaixz.bus.mapper.support.tenant;

import java.util.function.Supplier;

import org.miaixz.bus.core.lang.Assert;

/**
 * Tenant context holder.
 *
 * <p>
 * Uses ThreadLocal to store the tenant ID of the current thread, ensuring thread safety.
 * </p>
 *
 * <p>
 * Usage example:
 * </p>
 *
 * <pre>{@code
 * // Set tenant ID
 * TenantContext.setCurrentTenantId("tenant_001");
 * try {
 *     // Execute business logic
 *     userMapper.selectAll();
 * } finally {
 *     // Clear tenant ID
 *     TenantContext.clear();
 * }
 *
 * // Or use Lambda approach
 * TenantContext.runWithTenant("tenant_001", () -> {
 *     userMapper.selectAll();
 * });
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class TenantContext {

    /**
     * ThreadLocal storage for tenant ID.
     */
    private static final ThreadLocal<String> TENANT_ID = new ThreadLocal<>();

    /**
     * Flag to ignore tenant filtering.
     */
    private static final ThreadLocal<Boolean> IGNORE = ThreadLocal.withInitial(() -> Boolean.FALSE);

    /**
     * Private constructor to prevent instantiation.
     */
    private TenantContext() {
    }

    /**
     * Get the current tenant ID.
     *
     * @return the current tenant ID, or null if not set
     */
    public static String getTenantId() {
        return TENANT_ID.get();
    }

    /**
     * Set the current tenant ID.
     *
     * @param tenantId the tenant ID
     * @throws IllegalArgumentException if the tenant ID is null or empty
     */
    public static void setTenantId(String tenantId) {
        TENANT_ID.set(Assert.notBlank(tenantId, "Tenant ID cannot be null or empty").trim());
    }

    /**
     * Clear the current tenant ID.
     *
     * <p>
     * It is recommended to call this method in a finally block to ensure resource cleanup.
     * </p>
     */
    public static void clear() {
        TENANT_ID.remove();
        IGNORE.remove();
    }

    /**
     * Check if tenant ID is set.
     *
     * @return true if set, false otherwise
     */
    public static boolean hasTenantId() {
        String tenantId = TENANT_ID.get();
        return tenantId != null && !tenantId.isEmpty();
    }

    /**
     * Check if tenant filtering should be ignored.
     *
     * @return true if ignored, false otherwise
     */
    public static boolean isIgnore() {
        return Boolean.TRUE.equals(IGNORE.get());
    }

    /**
     * Set whether to ignore tenant filtering.
     *
     * @param ignore true to ignore, false otherwise
     */
    public static void setIgnore(boolean ignore) {
        IGNORE.set(ignore);
    }

    /**
     * Execute an operation in the specified tenant context (no return value).
     *
     * <p>
     * Automatically sets and clears the tenant ID.
     * </p>
     *
     * @param tenantId the tenant ID
     * @param runnable the operation to execute
     */
    public static void runWith(String tenantId, Runnable runnable) {
        String originalTenantId = getTenantId();
        try {
            setTenantId(tenantId);
            runnable.run();
        } finally {
            if (originalTenantId != null) {
                setTenantId(originalTenantId);
            } else {
                clear();
            }
        }
    }

    /**
     * Execute an operation in the specified tenant context (with return value).
     *
     * <p>
     * Automatically sets and clears the tenant ID.
     * </p>
     *
     * @param tenantId the tenant ID
     * @param supplier the operation to execute
     * @param <T>      the return value type
     * @return the operation result
     */
    public static <T> T callWith(String tenantId, Supplier<T> supplier) {
        String originalTenantId = getTenantId();
        try {
            setTenantId(tenantId);
            return supplier.get();
        } finally {
            if (originalTenantId != null) {
                setTenantId(originalTenantId);
            } else {
                clear();
            }
        }
    }

    /**
     * Execute an operation while ignoring tenant filtering (no return value).
     *
     * <p>
     * Temporarily ignores tenant filtering during execution.
     * </p>
     *
     * @param runnable the operation to execute
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
     * Execute an operation while ignoring tenant filtering (with return value).
     *
     * <p>
     * Temporarily ignores tenant filtering during execution.
     * </p>
     *
     * @param supplier the operation to execute
     * @param <T>      the return value type
     * @return the operation result
     */
    public static <T> T callIgnore(Supplier<T> supplier) {
        boolean originalIgnore = isIgnore();
        try {
            setIgnore(true);
            return supplier.get();
        } finally {
            setIgnore(originalIgnore);
        }
    }

}
