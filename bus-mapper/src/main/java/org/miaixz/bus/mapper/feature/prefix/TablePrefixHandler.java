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
import org.miaixz.bus.mapper.Holder;
import org.miaixz.bus.mapper.handler.ScopedProviderHandler;

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
public class TablePrefixHandler extends ScopedProviderHandler<Object, TablePrefixConfig, TablePrefixProvider> {

    /**
     * Default constructor (uses default configuration).
     */
    public TablePrefixHandler() {
        super();
    }

    /**
     * Constructs a TablePrefixHandler with the specified file configuration.
     *
     * @param config the prefix configuration from file
     */
    public TablePrefixHandler(TablePrefixConfig config) {
        super(config);
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
     * Returns the table prefix provider contract.
     *
     * @return the table prefix provider contract type
     */
    @Override
    protected Class<TablePrefixProvider> type() {
        return TablePrefixProvider.class;
    }

    /**
     * Resolves datasource-specific table prefix configuration from mapper properties.
     *
     * @param datasourceKey the datasource key
     * @param properties    the mapper configuration properties
     * @param provider      the optional table prefix provider
     * @return the table prefix configuration, or {@code null} when no prefix can be resolved
     */
    @Override
    protected TablePrefixConfig resolve(String datasourceKey, Properties properties, TablePrefixProvider provider) {
        return resolveConfig(datasourceKey, properties, provider);
    }

    /**
     * Resolves table prefix configuration with the same datasource-specific and global property rules used by this
     * handler.
     * <p>
     * Resolution order is datasource-bound configuration, shared global configuration, then default global
     * configuration.
     *
     * @param datasourceKey the datasource key
     * @param properties    the mapper configuration properties
     * @param provider      the optional table prefix provider
     * @return the table prefix configuration, or {@code null} when no prefix can be resolved
     */
    public static TablePrefixConfig resolveConfig(
            String datasourceKey,
            Properties properties,
            TablePrefixProvider provider) {
        if (properties == null && provider == null) {
            return null;
        }
        if (properties == null) {
            return TablePrefixConfig.builder().provider(provider).ignore(Collections.emptyList()).build();
        }
        String key = StringKit.isNotEmpty(datasourceKey) ? datasourceKey : Holder.getDefault();
        String sharedPrefix = Args.SHARED_KEY + Symbol.DOT + Args.TABLE_KEY + Symbol.DOT;
        String defaultPrefix = Holder.getDefault() + Symbol.DOT + Args.TABLE_KEY + Symbol.DOT;
        String legacyDefaultPrefix = "default" + Symbol.DOT + Args.TABLE_KEY + Symbol.DOT;
        String dsPrefix = key + Symbol.DOT + Args.TABLE_KEY + Symbol.DOT;

        String prefixValue = properties.getProperty(
                dsPrefix + Args.TABLE_PREFIX,
                properties.getProperty(
                        sharedPrefix + Args.TABLE_PREFIX,
                        properties.getProperty(
                                defaultPrefix + Args.TABLE_PREFIX,
                                properties.getProperty(legacyDefaultPrefix + Args.TABLE_PREFIX, Normal.EMPTY))));

        String ignore = properties.getProperty(
                dsPrefix + Args.PROP_IGNORE,
                properties.getProperty(
                        sharedPrefix + Args.PROP_IGNORE,
                        properties.getProperty(
                                defaultPrefix + Args.PROP_IGNORE,
                                properties.getProperty(legacyDefaultPrefix + Args.PROP_IGNORE, Normal.EMPTY))));

        List<String> ignoreTables = StringKit.isNotEmpty(ignore) ? Arrays.stream(ignore.split(Symbol.COMMA))
                .map(String::trim).filter(ObjectKit::isNotEmpty).collect(Collectors.toList()) : Collections.emptyList();

        if (provider == null && StringKit.isEmpty(prefixValue)) {
            return null;
        }

        TablePrefixProvider finalProvider = provider;
        if (finalProvider == null && StringKit.isNotEmpty(prefixValue)) {
            final String configuredPrefix = prefixValue;
            finalProvider = () -> configuredPrefix;
        }

        return TablePrefixConfig.builder().provider(finalProvider).ignore(ignoreTables).build();
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
                    Logger.warn(
                            false,
                            "Mapper",
                            "Table prefix SQL update failed: method={}, reason=boundSqlImmutable",
                            ms.getId());
                }
                putSqlRewrite(ms, actualSql);
                Logger.debug(false, "Mapper", "Applied: prefix={}, method={}", prefix, ms.getId());
            } else {
                // SQL unchanged (table in ignore list or no match)
                Logger.debug(false, "Mapper", "SQL unchanged: method={}", ms.getId());
            }
        } catch (Exception e) {
            Logger.warn(
                    false,
                    "Mapper",
                    e,
                    "Table prefix SQL rewrite failed: method={}, exception={}",
                    ms.getId(),
                    e.getClass().getSimpleName());
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
