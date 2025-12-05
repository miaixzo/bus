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
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.Args;
import org.miaixz.bus.mapper.Context;
import org.miaixz.bus.mapper.Holder;
import org.miaixz.bus.mapper.handler.AbstractSqlHandler;
import org.miaixz.bus.mapper.handler.MapperHandler;

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
public class TablePrefixHandler extends AbstractSqlHandler implements MapperHandler<Object> {

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

        // Try to get provider from properties
        TablePrefixProvider provider = null;
        Object object = properties.get(Args.PROVIDER_KEY);
        if (object instanceof TablePrefixProvider) {
            provider = (TablePrefixProvider) object;
        }

        // Get current datasource key
        String datasourceKey = Holder.getKey();
        // Use actual default datasource name or fallback to "default"
        if (StringKit.isEmpty(datasourceKey)) {
            datasourceKey = "default";
        }

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
        List<String> ignoreTables = Arrays.stream(ignore.split(Symbol.COMMA)).map(String::trim)
                .filter(ObjectKit::isNotEmpty).collect(Collectors.toList());

        // If no provider and no prefix value from config, return false
        if (provider == null && StringKit.isEmpty(prefixValue)) {
            return false;
        }

        // Create a file-based provider if no provider bean but prefix value exists
        TablePrefixProvider finalProvider = provider;
        if (finalProvider == null && StringKit.isNotEmpty(prefixValue)) {
            final String configuredPrefix = prefixValue;
            finalProvider = () -> configuredPrefix;
        }

        // Build configuration
        this.config = TablePrefixConfig.builder().provider(finalProvider).ignore(ignoreTables).build();

        return true;
    }

    /**
     * Get current effective configuration with priority: Context > File Config.
     *
     * @return the effective prefix configuration
     */
    private TablePrefixConfig getConfig() {
        // 1. Highest priority: Context configuration
        Context.MapperConfig contextConfig = Context.getMapperConfig();
        if (contextConfig != null && contextConfig.getPrefix() != null) {
            return contextConfig.getPrefix();
        }

        // 2. Lowest priority: File configuration
        return config;
    }

    @Override
    public int getOrder() {
        return MIN_VALUE + 2;
    }

    @Override
    public void prepare(StatementHandler statementHandler) {
        TablePrefixConfig currentConfig = getConfig();
        if (currentConfig == null || currentConfig.getProvider() == null) {
            Logger.debug(true, "Prefix", "Table prefix config not found, skipping prepare phase");
            return;
        }

        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        BoundSql boundSql = (BoundSql) metaObject.getValue(DELEGATE_BOUNDSQL);
        if (boundSql != null) {
            Logger.debug(false, "Prefix", "Processing SQL in prepare phase");
            processSql(boundSql, currentConfig);
        }
    }

    @Override
    public void update(Executor executor, MappedStatement ms, Object parameter) {
        TablePrefixConfig currentConfig = getConfig();
        if (currentConfig == null || currentConfig.getProvider() == null) {
            Logger.debug(true, "Prefix", "Table prefix config not found, skipping update: {}", ms.getId());
            return;
        }

        Logger.debug(false, "Prefix", "Processing update SQL: {}", ms.getId());
        processSql(ms.getBoundSql(parameter), currentConfig);
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
        TablePrefixConfig currentConfig = getConfig();
        if (currentConfig == null || currentConfig.getProvider() == null) {
            Logger.debug(true, "Prefix", "Table prefix config not found, skipping query: {}", ms.getId());
            return;
        }

        Logger.debug(false, "Prefix", "Processing query SQL: {}", ms.getId());
        processSql(boundSql, currentConfig);
    }

    /**
     * Process SQL to apply table prefix.
     *
     * @param boundSql the bound SQL to process
     * @param config   the prefix configuration for current datasource
     */
    private void processSql(BoundSql boundSql, TablePrefixConfig config) {
        String prefix = config.getProvider().getPrefix();
        if (StringKit.isEmpty(prefix)) {
            Logger.debug(true, "Prefix", "Prefix is empty, skipping SQL processing");
            return;
        }

        try {
            String originalSql = boundSql.getSql();

            // Apply prefix using regex-based builder
            TablePrefixBuilder builder = new TablePrefixBuilder(prefix, config.getIgnore());
            String modifiedSql = builder.applyPrefix(originalSql);

            // Only update if SQL was modified
            if (!originalSql.equals(modifiedSql)) {
                Logger.debug(false, "Prefix", "Applied table prefix: {}", prefix);
                // Use reflection to update SQL in BoundSql
                try {
                    java.lang.reflect.Field field = BoundSql.class.getDeclaredField("sql");
                    field.setAccessible(true);
                    field.set(boundSql, modifiedSql);
                } catch (Exception e) {
                    Logger.warn(false, "Prefix", "Failed to update SQL in BoundSql: {}", e.getMessage());
                }
            } else {
                Logger.debug(false, "Prefix", "SQL unchanged, no prefix applied");
            }

        } catch (Exception e) {
            // Log warning but don't break execution
            Logger.warn(false, "Prefix", "Failed to apply table prefix to SQL: {}", e.getMessage());
        }
    }

}
