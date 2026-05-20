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
package org.miaixz.bus.mapper.handler;

import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.FieldKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.Args;
import org.miaixz.bus.mapper.Holder;
import org.miaixz.bus.mapper.parsing.SqlSource;

/**
 * Base class for handling multi-table conditions. Provides methods for processing SELECT, UPDATE, and DELETE statements
 * and appending conditions based on table metadata.
 * <p>
 * This class also provides common functionality for dynamic configuration management in multi-threaded/virtual thread
 * environments, including:
 * <ul>
 * <li>Properties storage and access</li>
 * <li>Dynamic configuration building based on current datasource</li>
 * <li>Three-tier configuration priority (Capture > Derived > Defaults)</li>
 * </ul>
 *
 * @param <T> the type parameter for the mapper handler
 * @param <C> the configuration type (e.g., TenantConfig, PopulateConfig, etc.)
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class ConditionHandler<T, C> extends AbstractSqlHandler implements MapperHandler<T> {

    /**
     * Constructs a new ConditionHandler instance.
     */
    public ConditionHandler() {
        // No initialization required.
    }

    /**
     * All properties for dynamic configuration lookup.
     * <p>
     * This field is shared across all handler subclasses and used to dynamically build configuration based on the
     * current datasource key at runtime.
     * </p>
     */
    protected Properties properties;

    /**
     * Cache for datasource-derived configuration values.
     */
    private final ConcurrentMap<DerivedConfigKey, Optional<C>> derivedConfigCache = new ConcurrentHashMap<>();

    /**
     * Properties instance currently associated with the derived configuration cache.
     */
    private volatile Properties cachedProperties;

    /**
     * Get the configuration key for this handler (e.g., {@link Args#TENANT_KEY}, {@link Args#POPULATE_KEY}).
     * <p>
     * This key is used to build configuration paths like "shared.{key}.xxx" or "{datasource}.{key}.xxx".
     * </p>
     *
     * @return the configuration key for this handler
     */
    protected abstract String scope();

    /**
     * Get the runtime configuration from Context (ThreadLocal/InheritableThreadLocal).
     * <p>
     * This is the highest priority configuration (Level 1). Used for request/thread-level dynamic overrides.
     * </p>
     *
     * @return the runtime configuration, or null if not set
     */
    protected abstract C capture();

    /**
     * Get the default configuration initialized from properties file.
     * <p>
     * This is the lowest priority configuration (Level 3). Used as global fallback configuration.
     * </p>
     *
     * @return the default configuration, or null if not initialized
     */
    protected abstract C defaults();

    /**
     * Get the datasource configuration from properties file for a specific datasource.
     * <p>
     * This is called on every SQL execution to ensure correct configuration in multi-threaded/reactive environments.
     * This is the medium priority configuration (Level 2).
     * </p>
     *
     * @param datasourceKey the current datasource key
     * @param properties    the properties to read configuration from
     * @return the datasource configuration, or null if provider is not available
     */
    protected abstract C derived(String datasourceKey, Properties properties);

    /**
     * Get the current configuration with three-tier priority:
     * <ol>
     * <li>Runtime configuration (highest priority) - from ThreadLocal/InheritableThreadLocal</li>
     * <li>Datasource configuration (medium priority) - from properties file for current datasource</li>
     * <li>Default configuration (lowest priority) - from properties file global settings</li>
     * </ol>
     *
     * @return the current configuration
     */
    protected C current() {
        // 1. Highest priority: Runtime configuration (ThreadLocal/InheritableThreadLocal)
        C captured = capture();
        if (captured != null) {
            Logger.debug(false, "Mapper", "Using Runtime configuration");
            return captured;
        }

        // 2. Medium priority: Datasource configuration (from properties file for current datasource)
        if (properties != null) {
            Properties currentProperties = properties;
            refreshDerivedConfigCache(currentProperties);
            String key = Holder.getKey();
            if (StringKit.isEmpty(key)) {
                key = "default";
            }

            String datasourceKey = key;
            DerivedConfigKey cacheKey = new DerivedConfigKey(scope(), datasourceKey, currentProperties);
            C derived = derivedConfigCache.computeIfAbsent(
                    cacheKey,
                    ignored -> Optional.ofNullable(derived(datasourceKey, currentProperties))).orElse(null);
            if (derived != null) {
                Logger.debug(false, "Mapper", "Using Datasource configuration");
                return derived;
            }
        }

        // 3. Lowest priority: Default configuration (from properties file global settings)
        C defaults = defaults();
        if (defaults != null) {
            Logger.debug(false, "Mapper", "Using Default configuration");
        } else {
            Logger.debug(true, "Mapper", "No configuration available");
        }
        return defaults;
    }

    /**
     * Clears the derived configuration cache when the properties instance changes.
     *
     * @param currentProperties the current properties instance
     */
    private void refreshDerivedConfigCache(Properties currentProperties) {
        if (cachedProperties == currentProperties) {
            return;
        }
        synchronized (this) {
            if (cachedProperties != currentProperties) {
                derivedConfigCache.clear();
                cachedProperties = currentProperties;
            }
        }
    }

    /**
     * Build the configuration path for a given setting.
     * <p>
     * Example: "shared.tenant.column" or "ds1.tenant.column"
     * </p>
     *
     * @param datasourceKey the datasource key (can be "shared" or a specific datasource name)
     * @param settingKey    the specific setting key (e.g., "column", "ignore", etc.)
     * @return the full configuration path
     */
    protected String path(String datasourceKey, String settingKey) {
        return datasourceKey + Symbol.DOT + scope() + Symbol.DOT + settingKey;
    }

    /**
     * Build the shared configuration path.
     * <p>
     * Example: "shared.tenant.xxx"
     * </p>
     *
     * @param settingKey the specific setting key
     * @return the full shared configuration path
     */
    protected String path(String settingKey) {
        return path(Args.SHARED_KEY, settingKey);
    }

    /**
     * Get property value with datasource-specific fallback to shared.
     * <p>
     * Searches in order: {datasource}.{configKey}.{setting} -> shared.{configKey}.{setting} -> defaultValue
     * </p>
     *
     * @param datasourceKey the datasource key
     * @param settingKey    the specific setting key
     * @param defaultValue  the default value if not found
     * @return the property value
     */
    protected String find(String datasourceKey, String settingKey, String defaultValue) {
        String specific = path(datasourceKey, settingKey);
        String shared = path(settingKey);
        return properties.getProperty(specific, properties.getProperty(shared, defaultValue));
    }

    /**
     * Get the current datasource key with fallback to "default".
     *
     * @return the datasource key, or "default" if not set
     */
    protected String getDatasourceKey() {
        return Holder.getKey();
    }

    /**
     * Get provider from properties for the specified type.
     *
     * @param <P>           the provider type
     * @param properties    the properties
     * @param providerClass the provider class
     * @return the provider instance, or null if not found
     */
    protected <P> P getProvider(Properties properties, Class<P> providerClass) {
        if (properties == null) {
            return null;
        }
        Object object = properties.get(Args.PROVIDER_KEY);
        if (providerClass.isInstance(object)) {
            return providerClass.cast(object);
        }
        return null;
    }

    /**
     * Gets the original SqlSource from a MappedStatement, unwrapping our custom SqlSource if present.
     *
     * @param ms the MappedStatement
     * @return the original SqlSource before any interceptor modifications
     */
    protected org.apache.ibatis.mapping.SqlSource getOriginalSqlSource(MappedStatement ms) {
        org.apache.ibatis.mapping.SqlSource currentSqlSource = ms.getSqlSource();

        // If current SqlSource is already our custom SqlSource, get the original from it
        if (currentSqlSource instanceof SqlSource customSqlSource) {
            try {
                return (org.apache.ibatis.mapping.SqlSource) FieldKit.getFieldValue(customSqlSource, "sqlSource");
            } catch (Exception e) {
                Logger.warn(false, "Mapper", "Failed to get original SqlSource: {}", e.getMessage());
                return currentSqlSource;
            }
        }

        return currentSqlSource;
    }

    /**
     * Gets a fresh BoundSql from the original SqlSource (before any interceptor modifications). This ensures we get the
     * correct SQL and parameter mappings for the current execution, not stale SQL from previous calls.
     *
     * @param ms        the MappedStatement
     * @param parameter the parameter object
     * @return a fresh BoundSql with correct SQL and parameter mappings
     */
    protected BoundSql getFreshBoundSql(MappedStatement ms, Object parameter) {
        org.apache.ibatis.mapping.SqlSource originalSqlSource = getOriginalSqlSource(ms);
        return originalSqlSource.getBoundSql(parameter);
    }

    /**
     * Reads the request-scoped SQL rewrite for a mapped statement.
     *
     * @param ms the mapped statement
     * @return the rewritten SQL, or {@code null} when no rewrite exists
     */
    protected String getSqlRewrite(MappedStatement ms) {
        return SqlRewriteContext.get(ms.getId());
    }

    /**
     * Stores the request-scoped SQL rewrite for a mapped statement.
     *
     * @param ms  the mapped statement
     * @param sql the rewritten SQL
     */
    protected void putSqlRewrite(MappedStatement ms, String sql) {
        SqlRewriteContext.put(ms.getId(), sql);
    }

    /**
     * Replaces the SqlSource in a MappedStatement with a custom SqlSource that preserves SQL modifications.
     * <p>
     * This method creates a new custom SqlSource that:
     * </p>
     * <ul>
     * <li>Saves the actual SQL (after interceptor processing)</li>
     * <li>Delegates to the original SqlSource for parameter mappings</li>
     * <li>Parameter mappings are dynamically generated based on current parameters</li>
     * <li>Subsequent interceptors can process the modified SQL correctly</li>
     * </ul>
     * <p>
     * Built-in handlers no longer use this shared-state propagation path for request-scoped SQL rewrites. They now pass
     * rewritten SQL through {@link SqlRewriteContext} and the current {@link BoundSql}. This method remains available
     * for source and binary compatibility with custom handler subclasses.
     * </p>
     *
     * @param ms        the MappedStatement
     * @param boundSql  the current BoundSql
     * @param actualSql the actual SQL (after interceptor processing)
     */
    protected void replaceSqlSource(MappedStatement ms, BoundSql boundSql, String actualSql) {
        try {
            // Get the original SqlSource before any interceptor modifications
            org.apache.ibatis.mapping.SqlSource sqlSource = getOriginalSqlSource(ms);

            // Create new custom SqlSource with actual SQL and original SqlSource
            SqlSource newSqlSource = new SqlSource(ms, actualSql, sqlSource);

            // Replace the SqlSource in MappedStatement
            MetaObject msMetaObject = SystemMetaObject.forObject(ms);
            msMetaObject.setValue("sqlSource", newSqlSource);

            Logger.debug(false, "Mapper", "Replaced SqlSource: method={}", ms.getId());
        } catch (Exception e) {
            Logger.warn(false, "Mapper", "Failed to replace SqlSource: {}", e.getMessage());
        }
    }

    /**
     * Cache key for datasource-derived configuration.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private static final class DerivedConfigKey {

        /**
         * Handler configuration scope.
         */
        private final String scope;

        /**
         * Datasource key.
         */
        private final String datasourceKey;

        /**
         * Properties instance compared by identity.
         */
        private final Properties properties;

        /**
         * Creates a derived configuration key.
         *
         * @param scope         the handler configuration scope
         * @param datasourceKey the datasource key
         * @param properties    the properties instance
         */
        private DerivedConfigKey(String scope, String datasourceKey, Properties properties) {
            this.scope = scope;
            this.datasourceKey = datasourceKey;
            this.properties = properties;
        }

        /**
         * Tests equality using properties identity and value fields.
         *
         * @param object the object to compare
         * @return {@code true} when both keys identify the same derived configuration
         */
        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof DerivedConfigKey that)) {
                return false;
            }
            return properties == that.properties && Objects.equals(scope, that.scope)
                    && Objects.equals(datasourceKey, that.datasourceKey);
        }

        /**
         * Returns a hash code based on properties identity and value fields.
         *
         * @return the hash code
         */
        @Override
        public int hashCode() {
            return Objects.hash(scope, datasourceKey, System.identityHashCode(properties));
        }

    }

}
