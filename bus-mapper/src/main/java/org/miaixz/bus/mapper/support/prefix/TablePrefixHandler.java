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
package org.miaixz.bus.mapper.support.prefix;

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
import org.miaixz.bus.mapper.parsing.SqlSource;

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
 * @since Java 17+
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

    @Override
    protected String scope() {
        return Args.TABLE_KEY;
    }

    @Override
    protected TablePrefixConfig defaults() {
        return config;
    }

    @Override
    protected TablePrefixConfig capture() {
        Context.MapperConfig contextConfig = Context.getMapperConfig();
        return contextConfig != null ? contextConfig.getPrefix() : null;
    }

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

    @Override
    public int getOrder() {
        return MIN_VALUE + 1;
    }

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
            Logger.debug(true, "Prefix", "Table prefix config not found, skipping query: {}", ms.getId());
            return;
        }

        Logger.debug(false, "Prefix", "Processing query SQL: {}", ms.getId());
        processSqlInMappedStatement(ms, boundSql, currentConfig);
    }

    @Override
    public void update(Executor executor, MappedStatement ms, Object parameter) {
        TablePrefixConfig currentConfig = current();
        if (currentConfig == null || currentConfig.getProvider() == null) {
            Logger.debug(true, "Prefix", "Table prefix config not found, skipping update: {}", ms.getId());
            return;
        }

        Logger.debug(false, "Prefix", "Processing update SQL: {}", ms.getId());
        BoundSql boundSql = ms.getBoundSql(parameter);
        processSqlInMappedStatement(ms, boundSql, currentConfig);
    }

    /**
     * Process SQL by modifying the BoundSql parameter and replacing MappedStatement's SqlSource. This ensures that: 1.
     * The current BoundSql instance is modified (visible to current execution) 2. Subsequent getBoundSql() calls return
     * the actual SQL (via SqlSource replacement)
     *
     * <p>
     * <strong>Performance Optimization:</strong> Checks if the SqlSource has already been replaced by our custom
     * SqlSource. If yes, processing is skipped since the SQL has already been modified. This provides O(1) cache-like
     * performance without the issues of request-level caching.
     * </p>
     *
     * @param ms       the MappedStatement
     * @param boundSql the BoundSql parameter (will be modified directly)
     * @param config   the prefix configuration
     */
    private void processSqlInMappedStatement(MappedStatement ms, BoundSql boundSql, TablePrefixConfig config) {
        String prefix = config.getProvider().getPrefix();
        if (StringKit.isEmpty(prefix)) {
            Logger.debug(true, "Prefix", "Prefix is empty, skipping SQL processing");
            return;
        }

        // Optimization: Check if SqlSource has already been replaced (O(1))
        // This is safe because once replaced, all subsequent calls will use the actual SQL
        if (SqlSource.class.isInstance(ms.getSqlSource())) {
            Logger.debug(false, "Prefix", "SqlSource already replaced for: {}", ms.getId());
            return;
        }

        try {
            // Get original SQL from the BoundSql parameter
            String originalSql = boundSql.getSql();

            // Apply prefix directly
            TablePrefixBuilder builder = new TablePrefixBuilder(prefix, config.getIgnore());
            String actualSql = builder.applyPrefix(originalSql);

            if (!originalSql.equals(actualSql)) {
                // Step 1: Modify the BoundSql parameter using reflection
                // This ensures the current BoundSql instance is modified for current execution
                if (!setBoundSql(boundSql, actualSql)) {
                    Logger.warn(false, "Prefix", "Failed to modify BoundSql.sql");
                    // Fallback: continue with SqlSource replacement only
                }

                // Step 2: Replace the SqlSource in MappedStatement
                // This ensures subsequent getBoundSql() calls return the actual SQL
                replaceSqlSource(ms, boundSql, actualSql);
                Logger.debug(false, "Prefix", "Applied table prefix '{}' to: {}", prefix, ms.getId());
            } else {
                // SQL unchanged (table in ignore list or no match)
                Logger.debug(false, "Prefix", "SQL unchanged for: {}", ms.getId());
            }
        } catch (Exception e) {
            Logger.warn(false, "Prefix", "Failed to apply table prefix to SQL: {}", e.getMessage());
        }
    }

}
