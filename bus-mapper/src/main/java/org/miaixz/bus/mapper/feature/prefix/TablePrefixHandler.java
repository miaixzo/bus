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
package org.miaixz.bus.mapper.feature.prefix;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.Args;
import org.miaixz.bus.mapper.Context;
import org.miaixz.bus.mapper.handler.ConditionHandler;

/**
 * Table prefix handler.
 *
 * <p>
 * This handler intercepts MyBatis SQL execution and dynamically applies table prefixes based on the configured
 * {@link TablePrefixProvider}. It modifies SQL statements at runtime to add prefixes to table names, enabling:
 *
 * <ul>
 * <li>Multi-environment support (dev/test/prod with different prefixes)</li>
 * <li>Dynamic data source switching with environment-specific prefixes</li>
 * <li>Flexible prefix strategies through custom providers</li>
 * </ul>
 *
 * <p>
 * <b>Implementation Note:</b> This handler uses regex-based SQL parsing to identify and replace table names. It does
 * not rely on any third-party SQL parsing libraries, ensuring minimal dependencies.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class TablePrefixHandler extends ConditionHandler<Object, TablePrefixConfig> {

    /**
     * Prefix configuration from file (lowest priority).
     */
    private TablePrefixConfig config;

    /**
     * Default constructor (uses default configuration).
     */
    public TablePrefixHandler() {

    }

    /**
     * Constructs a TablePrefixHandler with the specified file configuration.
     *
     * @param config the prefix configuration from file
     */
    public TablePrefixHandler(TablePrefixConfig config) {
        this.config = config;
    }

    /**
     * Get the handler name for logging purposes.
     *
     * @return the handler name "Prefix"
     */
    @Override
    public String getHandler() {
        return "Prefix";
    }

    /**
     * Sets the prefix-related configuration properties. This method is typically called during plugin initialization to
     * configure table prefix behaviors.
     *
     * @param properties the configuration properties (contains all datasources)
     * @return true if properties were successfully set, false if properties is null or no valid prefix configuration
     */
    @Override
    public boolean setProperties(Properties properties) {
        if (properties == null) {
            return false;
        }

        // Store all properties for dynamic lookup (in parent class)
        this.properties = properties;

        // Get current datasource key for static config initialization
        String datasourceKey = getDatasourceKey();

        // Try to get provider from properties
        TablePrefixProvider provider = getProvider(properties, TablePrefixProvider.class);

        // Build initial static config
        TablePrefixConfig staticConfig = buildTablePrefixConfig(datasourceKey, properties, provider);
        if (staticConfig == null) {
            return false;
        }

        this.config = staticConfig;
        return true;
    }

    /**
     * Returns the property scope key used to resolve table prefix configuration.
     *
     * @return the property scope key
     */
    @Override
    protected String scope() {
        return Args.TABLE_KEY;
    }

    /**
     * Returns the default table prefix configuration loaded for this handler.
     *
     * @return the default configuration, or {@code null} when unavailable
     */
    @Override
    protected TablePrefixConfig defaults() {
        return config;
    }

    /**
     * Captures the current thread-local table prefix configuration override.
     *
     * @return the captured configuration, or {@code null} when no override is active
     */
    @Override
    protected TablePrefixConfig capture() {
        Context.MapperConfig contextConfig = Context.getMapperConfig();
        return contextConfig != null ? contextConfig.getPrefix() : null;
    }

    /**
     * Builds datasource-specific table prefix configuration from the supplied properties.
     *
     * @param datasourceKey the datasource key used to resolve scoped configuration
     * @param properties    the configuration properties used to build the scoped configuration
     * @return the derived configuration, or {@code null} when the datasource is not configured
     */
    @Override
    protected TablePrefixConfig derived(String datasourceKey, Properties properties) {
        // Try to get provider from properties
        TablePrefixProvider provider = getProvider(properties, TablePrefixProvider.class);

        return buildTablePrefixConfig(datasourceKey, properties, provider);
    }

    /**
     * Build table prefix configuration from properties for a specific datasource.
     *
     * @param datasourceKey the datasource key
     * @param properties    the properties
     * @param provider      the table prefix provider
     * @return the table prefix configuration, or null if no valid configuration
     */
    private TablePrefixConfig buildTablePrefixConfig(
            String datasourceKey,
            Properties properties,
            TablePrefixProvider provider) {
        // Build configuration paths
        String sharedPrefix = Args.SHARED_KEY + Symbol.DOT + Args.TABLE_KEY + Symbol.DOT;
        String dsPrefix = datasourceKey + Symbol.DOT + Args.TABLE_KEY + Symbol.DOT;

        // Merge configuration: datasource-specific > shared > null
        String prefixValue = properties.getProperty(
                dsPrefix + Args.TABLE_PREFIX,
                properties.getProperty(sharedPrefix + Args.TABLE_PREFIX, Normal.EMPTY));

        String ignore = properties.getProperty(
                dsPrefix + Args.PROP_IGNORE,
                properties.getProperty(sharedPrefix + Args.PROP_IGNORE, Normal.EMPTY));

        // Get ignore tables list
        List<String> ignoreTables = StringKit.isNotEmpty(ignore) ? Arrays.stream(ignore.split(Symbol.COMMA))
                .map(String::trim).filter(ObjectKit::isNotEmpty).collect(Collectors.toList()) : Collections.emptyList();

        // If no provider and no prefix value from config, return null
        if (provider == null && StringKit.isEmpty(prefixValue)) {
            return null;
        }

        // Create a file-based provider if no provider bean but prefix value exists
        TablePrefixProvider finalProvider = provider;
        if (finalProvider == null && StringKit.isNotEmpty(prefixValue)) {
            final String configuredPrefix = prefixValue;
            finalProvider = () -> configuredPrefix;
        }

        // Build and return configuration
        return TablePrefixConfig.builder().provider(finalProvider).ignore(ignoreTables).build();
    }

    /**
     * Returns the execution order for the table prefix handler in the mapper interceptor chain.
     *
     * @return the handler order value
     */
    @Override
    public int getOrder() {
        return MIN_VALUE + 1;
    }

    /**
     * Applies configured table-prefix rewriting to a query statement before execution.
     *
     * @param result        the mutable result holder used by the interceptor chain
     * @param executor      the MyBatis executor
     * @param ms            the mapped statement being processed
     * @param parameter     the statement parameter object
     * @param rowBounds     the MyBatis row bounds
     * @param resultHandler the MyBatis result handler
     * @param boundSql      the bound SQL being processed
     */
    @Override
    public void query(
            Object result,
            Executor executor,
            MappedStatement ms,
            Object parameter,
            RowBounds rowBounds,
            ResultHandler resultHandler,
            BoundSql boundSql) {
        TablePrefixConfig currentConfig = current();
        if (currentConfig == null || currentConfig.getProvider() == null) {
            Logger.debug(true, "Mapper", "Config not found, skipping: method={}", ms.getId());
            return;
        }

        Logger.debug(false, "Mapper", "Processing query: method={}", ms.getId());
        processSqlInMappedStatement(ms, boundSql, parameter, currentConfig);
    }

    /**
     * Applies configured table-prefix rewriting to an update statement before execution.
     *
     * @param executor  the MyBatis executor
     * @param ms        the mapped statement being processed
     * @param parameter the statement parameter object
     */
    @Override
    public void update(Executor executor, MappedStatement ms, Object parameter) {
        TablePrefixConfig currentConfig = current();
        if (currentConfig == null || currentConfig.getProvider() == null) {
            Logger.debug(true, "Mapper", "Config not found, skipping: method={}", ms.getId());
            return;
        }

        Logger.debug(false, "Mapper", "Processing insert/update: method={}", ms.getId());
        processSqlInMappedStatement(ms, null, parameter, currentConfig);
    }

    /**
     * Processes SQL by updating the current BoundSql or the request scoped rewrite context.
     *
     * @param ms        the MappedStatement
     * @param boundSql  the BoundSql from interceptor, or {@code null} for update interception
     * @param parameter the parameter object
     * @param config    the prefix configuration
     */
    private void processSqlInMappedStatement(
            MappedStatement ms,
            BoundSql boundSql,
            Object parameter,
            TablePrefixConfig config) {
        String prefix = config.getProvider().getPrefix();
        if (StringKit.isEmpty(prefix)) {
            Logger.debug(true, "Mapper", "Prefix is empty, skipping");
            return;
        }

        try {
            String originalSql = currentSql(ms, parameter, boundSql);

            // Apply prefix to fresh SQL
            TablePrefixBuilder builder = new TablePrefixBuilder(prefix, config.getIgnore());
            String actualSql = builder.applyPrefix(originalSql);

            if (!originalSql.equals(actualSql)) {
                // Step 1: Modify the BoundSql parameter using reflection
                // This ensures the current BoundSql instance is modified for current execution
                if (boundSql != null && !setBoundSql(boundSql, actualSql)) {
                    Logger.warn(false, "Mapper", "Failed to modify BoundSql");
                }
                putSqlRewrite(ms, actualSql);
                Logger.debug(false, "Mapper", "Applied: prefix={}, method={}", prefix, ms.getId());
            } else {
                // SQL unchanged (table in ignore list or no match)
                Logger.debug(false, "Mapper", "SQL unchanged: method={}", ms.getId());
            }
        } catch (Exception e) {
            Logger.warn(false, "Mapper", "Failed to apply prefix: {}", e.getMessage());
        }
    }

    /**
     * Resolves the SQL text that should be used as the next rewrite input.
     *
     * @param ms        the mapped statement
     * @param parameter the statement parameter
     * @param boundSql  the current bound SQL, or {@code null}
     * @return the SQL text to rewrite
     */
    private String currentSql(MappedStatement ms, Object parameter, BoundSql boundSql) {
        if (boundSql != null) {
            return boundSql.getSql();
        }
        String rewrittenSql = getSqlRewrite(ms);
        if (rewrittenSql != null) {
            return rewrittenSql;
        }
        return getFreshBoundSql(ms, parameter).getSql();
    }

}
