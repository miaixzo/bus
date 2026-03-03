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
package org.miaixz.bus.mapper;

import java.util.function.Supplier;

import org.miaixz.bus.mapper.support.audit.AuditConfig;
import org.miaixz.bus.mapper.support.populate.PopulateConfig;
import org.miaixz.bus.mapper.support.prefix.TablePrefixConfig;
import org.miaixz.bus.mapper.support.tenant.TenantConfig;
import org.miaixz.bus.mapper.support.visible.VisibleConfig;

/**
 * Extends the core context to provide a specific context for the mapper module with unified configuration override
 * mechanism.
 *
 * <p>
 * Provides ThreadLocal-based configuration override mechanism, allowing runtime configuration changes with priority:
 * Context > Provider.getConfig() > Configuration File > Default Values
 * </p>
 *
 * <p>
 * Usage example:
 * </p>
 *
 * <pre>{@code
 * // Method 1: Simple tenant configuration
 * Context.setTenantId("tenant_001", "sys_config", "sys_dict");
 * userMapper.selectAll(); // Automatically applies tenant filtering
 * Context.clear();
 *
 * // Method 2: Complete configuration
 * MapperConfig config = MapperConfig.builder().tenant(t -> {
 *     t.setTenantId("tenant_001");
 *     t.setColumn("tenant_id");
 *     t.setIgnoreTables(Arrays.asList("sys_config"));
 * }).audit(a -> {
 *     a.setEnabled(true);
 *     a.setSlowSqlThreshold(500);
 * }).build();
 *
 * Context.runWith(config, () -> {
 *     return userMapper.selectAll();
 * });
 *
 * // Method 3: Dynamic tenant switching
 * List<User> allUsers = new ArrayList<>();
 * for (String tenantId : tenantIds) {
 *     Context.setTenantId(tenantId);
 *     allUsers.addAll(userMapper.selectAll());
 * }
 * Context.clear();
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Context extends org.miaixz.bus.core.Context {

    /**
     * ThreadLocal storage for mapper configuration.
     */
    private static final ThreadLocal<MapperConfig> MAPPER_CONFIG = new ThreadLocal<>();

    /**
     * Get mapper configuration for current thread.
     *
     * @return the mapper configuration, or null if not set
     */
    public static MapperConfig getMapperConfig() {
        return MAPPER_CONFIG.get();
    }

    /**
     * Set mapper configuration for current thread.
     *
     * @param config the mapper configuration
     */
    public static void setMapperConfig(MapperConfig config) {
        MAPPER_CONFIG.set(config);
    }

    /**
     * Clear mapper configuration for current thread.
     */
    public static void clearMapperConfig() {
        MAPPER_CONFIG.remove();
    }

    /**
     * Execute operation with specified mapper configuration (with return value).
     *
     * @param config   the mapper configuration
     * @param supplier the operation to execute
     * @param <T>      the return value type
     * @return the operation result
     */
    public static <T> T runWith(MapperConfig config, Supplier<T> supplier) {
        MapperConfig original = getMapperConfig();
        try {
            setMapperConfig(config);
            return supplier.get();
        } finally {
            if (original != null) {
                setMapperConfig(original);
            } else {
                clearMapperConfig();
            }
        }
    }

    /**
     * Execute operation with specified mapper configuration (no return value).
     *
     * @param config   the mapper configuration
     * @param runnable the operation to execute
     */
    public static void runWith(MapperConfig config, Runnable runnable) {
        MapperConfig original = getMapperConfig();
        try {
            setMapperConfig(config);
            runnable.run();
        } finally {
            if (original != null) {
                setMapperConfig(original);
            } else {
                clearMapperConfig();
            }
        }
    }

    /**
     * Set tenant ID for current thread (simplified API).
     *
     * @param tenantId     the tenant ID
     * @param ignoreTables the tables to ignore tenant filtering (optional)
     */
    public static void setTenantId(String tenantId, String... ignoreTables) {
        MapperConfig config = getMapperConfig();
        if (config == null) {
            config = new MapperConfig();
            setMapperConfig(config);
        }

        // Create or update tenant config
        TenantConfig tenantConfig = config.getTenant();
        if (tenantConfig == null) {
            tenantConfig = TenantConfig.builder().column("tenant_id").provider(() -> tenantId).build();
            config.setTenant(tenantConfig);
        } else {
            // Update existing config with new tenant ID
            tenantConfig = TenantConfig.builder().column(tenantConfig.getColumn()).ignore(tenantConfig.getIgnore())
                    .ignoreMappers(tenantConfig.getIgnoreMappers()).provider(() -> tenantId).build();
            config.setTenant(tenantConfig);
        }
    }

    /**
     * Set table prefix for current thread (simplified API).
     *
     * @param prefix       the table prefix
     * @param ignoreTables the tables to ignore prefix (optional)
     */
    public static void setPrefix(String prefix, String... ignoreTables) {
        MapperConfig config = getMapperConfig();
        if (config == null) {
            config = new MapperConfig();
            setMapperConfig(config);
        }

        TablePrefixConfig tablePrefixConfig = TablePrefixConfig.builder().provider(() -> prefix).build();
        config.setPrefix(tablePrefixConfig);
    }

    /**
     * Unified mapper configuration holder.
     */
    public static class MapperConfig {

        private TenantConfig tenant;
        private AuditConfig audit;
        private PopulateConfig populate;
        private VisibleConfig visible;
        private TablePrefixConfig prefix;

        /**
         * Create builder for fluent API.
         *
         * @return the builder
         */
        public static Builder builder() {
            return new Builder();
        }

        public TenantConfig getTenant() {
            return tenant;
        }

        public void setTenant(TenantConfig tenant) {
            this.tenant = tenant;
        }

        public AuditConfig getAudit() {
            return audit;
        }

        public void setAudit(AuditConfig audit) {
            this.audit = audit;
        }

        public PopulateConfig getPopulate() {
            return populate;
        }

        public void setPopulate(PopulateConfig populate) {
            this.populate = populate;
        }

        public VisibleConfig getVisible() {
            return visible;
        }

        public void setVisible(VisibleConfig visible) {
            this.visible = visible;
        }

        public TablePrefixConfig getPrefix() {
            return prefix;
        }

        public void setPrefix(TablePrefixConfig prefix) {
            this.prefix = prefix;
        }

        /**
         * Fluent builder for MapperConfig.
         */
        public static class Builder {

            private final MapperConfig config = new MapperConfig();

            /**
             * Configure tenant settings.
             *
             * @param tenant the tenant configuration
             * @return this builder
             */
            public Builder tenant(TenantConfig tenant) {
                config.setTenant(tenant);
                return this;
            }

            /**
             * Configure audit settings.
             *
             * @param audit the audit configuration
             * @return this builder
             */
            public Builder audit(AuditConfig audit) {
                config.setAudit(audit);
                return this;
            }

            /**
             * Configure populate settings.
             *
             * @param populate the populate configuration
             * @return this builder
             */
            public Builder populate(PopulateConfig populate) {
                config.setPopulate(populate);
                return this;
            }

            /**
             * Configure visible settings.
             *
             * @param visible the visible configuration
             * @return this builder
             */
            public Builder visible(VisibleConfig visible) {
                config.setVisible(visible);
                return this;
            }

            /**
             * Configure prefix settings.
             *
             * @param prefix the prefix configuration
             * @return this builder
             */
            public Builder prefix(TablePrefixConfig prefix) {
                config.setPrefix(prefix);
                return this;
            }

            /**
             * Build the configuration.
             *
             * @return the mapper configuration
             */
            public MapperConfig build() {
                return config;
            }
        }
    }

}
