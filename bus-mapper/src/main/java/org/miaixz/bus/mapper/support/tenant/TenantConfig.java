/*
 * ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2025 miaixz.org and other contributors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 */
package org.miaixz.bus.mapper.support.tenant;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.mapper.Args;
import org.miaixz.bus.mapper.Holder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Multi-tenancy configuration class.
 *
 * <p>
 * Supports reading tenant-related configuration from configuration files. Configuration format example:
 * </p>
 *
 * <pre>
 * com_deepparser:
 *   table:
 *     prefix: dp_
 *   tenant:
 *     column: tenant_id
 *     ignore: tenant,assets,license,token,user
 *
 * com_deepparser_dev:
 *   table:
 *     prefix: dev_
 *   tenant:
 *     column: tenant_id
 *     ignore: tenant,assets,license,token,user
 * </pre>
 *
 * <p>
 * Usage examples:
 * </p>
 *
 * <pre>{@code
 *
 * // Method 1: Quick setup with custom provider only
 * TenantConfig config = TenantConfig.of(() -> SecurityContextHolder.getTenantId());
 *
 * // Method 2: Use default provider (from TenantContext)
 * TenantConfig config = TenantConfig.ofDefault();
 *
 * // Method 3: Full configuration with custom provider
 * TenantConfig config = TenantConfig.builder().mode(TenantMode.COLUMN).column("tenant_id")
 *         .ignoreTables("sys_config", "sys_dict").provider(() -> SecurityContextHolder.getTenantId()).build();
 *
 * // Method 4: Configuration file-based (auto-load from application.yml)
 * TenantConfig config = TenantConfig.builder().build();
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
public class TenantConfig {

    /**
     * Multi-tenancy mode.
     */
    @Builder.Default
    private final TenantMode mode = TenantMode.COLUMN;

    /**
     * Tenant ID column name (used in COLUMN mode).
     */
    private final String column;

    /**
     * List of ignored table names.
     */
    @Builder.Default
    private final List<String> ignore = new ArrayList<>();

    /**
     * List of ignored Mapper class names.
     */
    @Builder.Default
    private final List<String> ignoreMappers = new ArrayList<>();

    /**
     * Whether SQL cache is enabled.
     */
    @Builder.Default
    private final boolean enableSqlCache = true;

    /**
     * Tenant ID resolver.
     */
    private final TenantProvider provider;

    /**
     * Create a TenantConfig with custom provider and default settings.
     *
     * <p>
     * This is a convenient factory method for quick setup. It creates a configuration with:
     * </p>
     * <ul>
     * <li>COLUMN mode</li>
     * <li>Default column name from configuration or "tenant_id"</li>
     * <li>Ignored tables from configuration</li>
     * <li>Enabled by default</li>
     * <li>SQL cache enabled</li>
     * </ul>
     *
     * @param provider the tenant ID provider
     * @return a TenantConfig instance
     */
    public static TenantConfig of(TenantProvider provider) {
        return builder().provider(provider).build();
    }

    /**
     * Check if the specified table should ignore tenant filtering.
     *
     * @param tableName the table name (may include prefix)
     * @return true if ignored, false otherwise
     */
    public boolean isIgnoreTable(String tableName) {
        if (tableName == null || tableName.isEmpty()) {
            return false;
        }

        // Remove table prefix before comparison
        String tableNameWithoutPrefix = removeTablePrefix(tableName);

        return ignore.stream().anyMatch(
                ignore -> tableNameWithoutPrefix.equalsIgnoreCase(ignore)
                        || tableNameWithoutPrefix.matches(ignore.replace("*", ".*")));
    }

    /**
     * Remove table prefix.
     *
     * @param tableName the table name
     * @return the table name without prefix
     */
    private String removeTablePrefix(String tableName) {
        String key = Holder.getKey() + Symbol.DOT + Args.TABLE_KEY + Symbol.DOT + Args.TABLE_PREFIX;
        String prefix = org.miaixz.bus.core.Context.INSTANCE.getProperty(key, Normal.EMPTY);

        if (StringKit.isNotEmpty(prefix) && tableName.startsWith(prefix)) {
            return tableName.substring(prefix.length());
        }

        return tableName;
    }

    /**
     * Check if the specified Mapper should ignore tenant filtering.
     *
     * @param mapperClass the Mapper class name
     * @return true if ignored, false otherwise
     */
    public boolean isIgnoreMapper(String mapperClass) {
        if (mapperClass == null || mapperClass.isEmpty()) {
            return false;
        }
        return ignoreMappers.stream()
                .anyMatch(ignore -> mapperClass.equals(ignore) || mapperClass.endsWith("." + ignore));
    }

}
