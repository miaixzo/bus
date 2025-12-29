package org.miaixz.bus.mapper.handler;

import java.util.Properties;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.miaixz.bus.core.lang.Symbol;
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
 * @since Java 17+
 */
public abstract class ConditionHandler<T, C> extends AbstractSqlHandler implements MapperHandler<T> {

    /**
     * All properties for dynamic configuration lookup.
     * <p>
     * This field is shared across all handler subclasses and used to dynamically build configuration based on the
     * current datasource key at runtime.
     * </p>
     */
    protected Properties properties;

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
            Logger.debug(false, handler(), "Using Runtime configuration");
            return captured;
        }

        // 2. Medium priority: Datasource configuration (from properties file for current datasource)
        if (properties != null) {
            String key = Holder.getKey();
            if (StringKit.isEmpty(key)) {
                key = "default";
            }

            C derived = derived(key, properties);
            if (derived != null) {
                Logger.debug(false, handler(), "Using Datasource configuration");
                return derived;
            }
        }

        // 3. Lowest priority: Default configuration (from properties file global settings)
        C defaults = defaults();
        if (defaults != null) {
            Logger.debug(false, handler(), "Using Default configuration");
        } else {
            Logger.debug(true, handler(), "No configuration available");
        }
        return defaults;
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
     * Get the handler name for logging purposes.
     * <p>
     * Default implementation uses the simple class name. Subclasses can override for custom names.
     * </p>
     *
     * @return the handler name
     */
    protected String handler() {
        return this.getClass().getSimpleName();
    }

    /**
     * Get the current datasource key with fallback to "default".
     *
     * @return the datasource key, or "default" if not set
     */
    protected String getDatasourceKey() {
        String datasourceKey = Holder.getKey();
        if (StringKit.isEmpty(datasourceKey)) {
            datasourceKey = "default";
        }
        return datasourceKey;
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
     * Replace the SqlSource in MappedStatement with actual SQL.
     * <p>
     * This ensures subsequent getBoundSql() calls return the actual SQL.
     * </p>
     *
     * @param ms        the MappedStatement
     * @param boundSql  the BoundSql object
     * @param actualSql the actual SQL string
     */
    protected void replaceSqlSource(MappedStatement ms, BoundSql boundSql, String actualSql) {
        try {
            // Step 1: Modify BoundSql SQL field directly using reflection
            MetaObject boundSqlMetaObject = SystemMetaObject.forObject(boundSql);
            boundSqlMetaObject.setValue("sql", actualSql);

            // Step 2: Replace SqlSource to ensure consistency in all phases
            MetaObject msMetaObject = SystemMetaObject.forObject(ms);
            org.apache.ibatis.mapping.SqlSource sqlSource = new SqlSource(ms, actualSql);
            msMetaObject.setValue("sqlSource", sqlSource);

            Logger.debug(false, handler(), "Replaced SqlSource for MappedStatement: {}", ms.getId());
        } catch (Exception e) {
            Logger.warn(false, handler(), "Failed to replace SqlSource: {}", e.getMessage());
        }
    }

}
