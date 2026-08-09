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

import lombok.Getter;
import lombok.Setter;

import org.miaixz.bus.core.center.function.SupplierX;
import org.miaixz.bus.mapper.feature.affix.AffixRuleConfig;
import org.miaixz.bus.mapper.feature.affix.AffixValueProvider;
import org.miaixz.bus.mapper.feature.audit.AuditConfig;
import org.miaixz.bus.mapper.feature.populate.PopulateConfig;
import org.miaixz.bus.mapper.feature.tenant.TenantConfig;
import org.miaixz.bus.mapper.feature.visible.VisibleConfig;

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
 */
public class Context extends org.miaixz.bus.core.Context {

    /**
     * Constructs a new Context instance.
     */
    public Context() {
        // No initialization required.
    }

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
    public static <T> T runWith(MapperConfig config, SupplierX<T> supplier) {
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
            tenantConfig = TenantConfig.builder().mode(tenantConfig.getMode()).column(tenantConfig.getColumn())
                    .ignore(tenantConfig.getIgnore()).ignoreMappers(tenantConfig.getIgnoreMappers())
                    .affixPrefix(tenantConfig.getAffixPrefix()).affixSuffix(tenantConfig.getAffixSuffix())
                    .enableSqlCache(tenantConfig.isEnableSqlCache()).required(tenantConfig.isRequired())
                    .provider(() -> tenantId).build();
            config.setTenant(tenantConfig);
        }
    }

    /**
     * Sets table prefix and suffix values for the current thread.
     *
     * @param prefix       table prefix
     * @param suffix       table suffix
     * @param ignoreTables logical tables excluded from affix handling
     */
    public static void setAffix(String prefix, String suffix, String... ignoreTables) {
        java.util.List<String> ignore = ignoreTables == null ? java.util.Collections.emptyList()
                : java.util.Arrays.asList(ignoreTables);
        setAffix(prefix, ignore, suffix, ignore);
    }

    /**
     * Sets independent prefix and suffix rules for the current thread.
     *
     * @param prefix       table prefix
     * @param prefixIgnore logical tables excluded only from prefix handling
     * @param suffix       table suffix
     * @param suffixIgnore logical tables excluded only from suffix handling
     */
    public static void setAffix(
            String prefix,
            java.util.List<String> prefixIgnore,
            String suffix,
            java.util.List<String> suffixIgnore) {
        MapperConfig config = getMapperConfig();
        if (config == null) {
            config = new MapperConfig();
            setMapperConfig(config);
        }
        AffixValueProvider provider = new AffixValueProvider() {

            @Override
            public String getPrefix() {
                return prefix;
            }

            @Override
            public String getSuffix() {
                return suffix;
            }
        };
        config.setAffix(
                AffixRuleConfig.builder().provider(provider).prefixIgnore(prefixIgnore).suffixIgnore(suffixIgnore)
                        .build());

        TenantConfig tenantConfig = config.getTenant();
        if (tenantConfig != null) {
            config.setTenant(
                    TenantConfig.builder().mode(tenantConfig.getMode()).column(tenantConfig.getColumn())
                            .ignore(tenantConfig.getIgnore()).ignoreMappers(tenantConfig.getIgnoreMappers())
                            .affixPrefix(prefix).affixSuffix(suffix).enableSqlCache(tenantConfig.isEnableSqlCache())
                            .required(tenantConfig.isRequired()).provider(tenantConfig.getProvider()).build());
        }
    }

    /**
     * Unified mapper configuration holder.
     *
     * @author Kimi Liu
     */
    @Getter
    @Setter
    public static class MapperConfig {

        /**
         * Constructs a new MapperConfig instance.
         */
        public MapperConfig() {
            // No initialization required.
        }

        /**
         * Tenant isolation configuration.
         */
        private TenantConfig tenant;

        /**
         * SQL audit configuration.
         */
        private AuditConfig audit;

        /**
         * Automatic field population configuration.
         */
        private PopulateConfig populate;

        /**
         * Data visibility filtering configuration.
         */
        private VisibleConfig visible;

        /**
         * Table prefix and suffix rewriting configuration.
         */
        private AffixRuleConfig affix;

        /**
         * Create builder for fluent API.
         *
         * @return the builder
         */
        public static Builder builder() {
            return new Builder();
        }

        /**
         * Fluent builder for MapperConfig.
         *
         * @author Kimi Liu
         */
        public static class Builder {

            /**
             * Constructs a new Builder instance.
             */
            public Builder() {
                // No initialization required.
            }

            /**
             * Mutable configuration instance assembled by this builder.
             */
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
             * Configures prefix and suffix rewrite rules.
             *
             * @param affix affix rule configuration
             * @return this builder
             */
            public Builder affix(AffixRuleConfig affix) {
                config.setAffix(affix);
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
