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
package org.miaixz.bus.mapper.feature.tenant;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.Args;
import org.miaixz.bus.mapper.Context;
import org.miaixz.bus.mapper.handler.ScopedProviderHandler;

/**
 * Multi-tenancy handler.
 *
 * <p>
 * Implements the MapperHandler interface to automatically add tenant filtering conditions to SQL statements.
 * </p>
 *
 * <p>
 * Usage example:
 * </p>
 *
 * <pre>{@code
 * // 1. Create tenant configuration
 * TenantConfig config = TenantConfig.builder().mode(Isolation.COLUMN).column("tenant_id")
 *         .ignoreTables("sys_config", "sys_dict").enabled(true).build();
 *
 * // 2. Create tenant handler
 * TenantHandler tenantHandler = new TenantHandler(config);
 *
 * // 3. Register to MybatisInterceptor
 * MybatisInterceptor interceptor = new MybatisInterceptor();
 * interceptor.addHandler(tenantHandler);
 *
 * // Or with custom tenant ID resolver
 * TenantConfig config2 = TenantConfig.builder().mode(Isolation.COLUMN).tenantIdResolver(() -> {
 *     // Custom logic to get tenant ID
 *     return SecurityContextHolder.getTenantId();
 * }).enabled(true).build();
 * }</pre>
 *
 * @param <T> the generic type parameter
 * @author Kimi Liu
 * @since Java 21+
 */
public class TenantHandler<T> extends ScopedProviderHandler<T, TenantConfig, TenantProvider> {

    /**
     * Default constructor (uses default configuration).
     */
    public TenantHandler() {
        super();
    }

    /**
     * Constructor with file configuration.
     *
     * @param config the tenant configuration from file
     */
    public TenantHandler(TenantConfig config) {
        super(Assert.notNull(config, "TenantConfig cannot be null"));
    }

    /**
     * Gets the scope key for tenant configuration.
     *
     * @return the scope key for tenant configuration
     */
    @Override
    protected String scope() {
        return Args.TENANT_KEY;
    }

    /**
     * Gets the captured tenant configuration from context.
     *
     * @return the captured tenant configuration from context
     */
    @Override
    protected TenantConfig capture() {
        Context.MapperConfig contextConfig = Context.getMapperConfig();
        return contextConfig != null ? contextConfig.getTenant() : null;
    }

    /**
     * Returns the tenant provider contract.
     *
     * @return the tenant provider contract type
     */
    @Override
    protected Class<TenantProvider> type() {
        return TenantProvider.class;
    }

    /**
     * Resolves tenant configuration from properties for a specific datasource.
     *
     * @param datasourceKey the datasource key
     * @param properties    the properties
     * @param provider      the tenant provider
     * @return the tenant configuration, or null if tenant feature is not configured
     */
    @Override
    protected TenantConfig resolve(String datasourceKey, Properties properties, TenantProvider provider) {
        String sharedTenantPrefix = Args.SHARED_KEY + Symbol.DOT + Args.TENANT_KEY + Symbol.DOT;
        String dsTenantPrefix = datasourceKey + Symbol.DOT + Args.TENANT_KEY + Symbol.DOT;
        String sharedTablePrefix = Args.SHARED_KEY + Symbol.DOT + Args.TABLE_KEY + Symbol.DOT;
        String dsTablePrefix = datasourceKey + Symbol.DOT + Args.TABLE_KEY + Symbol.DOT;

        // Check if tenant.column is explicitly configured (datasource-specific or shared)
        // If not configured, tenant feature should be disabled
        String column = properties.getProperty(dsTenantPrefix + Args.TENANT_COLUMN);
        if (column == null) {
            column = properties.getProperty(sharedTenantPrefix + Args.TENANT_COLUMN);
        }

        // If column is not configured, tenant feature is disabled
        if (column == null || column.trim().isEmpty()) {
            Logger.debug(
                    false,
                    "Mapper",
                    "Tenant feature disabled: datasourceKey={}, reason={}",
                    datasourceKey,
                    "columnMissing");
            return null;
        }

        String ignoreTablesStr = properties.getProperty(
                dsTenantPrefix + Args.PROP_IGNORE,
                properties.getProperty(sharedTenantPrefix + Args.PROP_IGNORE, Normal.EMPTY));
        String tablePrefix = properties.getProperty(
                dsTablePrefix + Args.TABLE_PREFIX,
                properties.getProperty(sharedTablePrefix + Args.TABLE_PREFIX, Normal.EMPTY));

        Logger.debug(
                false,
                "Mapper",
                "Tenant config resolve started: datasourceKey={}, columnPresent={}, ignoreConfigKey={}",
                datasourceKey,
                true,
                dsTenantPrefix + Args.PROP_IGNORE);

        List<String> ignoreTables = Arrays.stream(ignoreTablesStr.split(Symbol.COMMA)).map(String::trim)
                .filter(ObjectKit::isNotEmpty).collect(Collectors.toList());

        Logger.debug(
                false,
                "Mapper",
                "Tenant config resolve completed: datasourceKey={}, ignoreTableCount={}, "
                        + "tablePrefixPresent={}, providerPresent={}",
                datasourceKey,
                ignoreTables.size(),
                ObjectKit.isNotEmpty(tablePrefix),
                provider != null);

        // If column is configured, create a default provider that gets tenant ID from TenantContext
        if (provider == null) {
            // Create default provider that uses TenantContext
            provider = TenantContext::getTenantId;
        }

        return TenantConfig.builder().column(column).ignore(ignoreTables).tablePrefix(tablePrefix).provider(provider)
                .build();
    }

    /**
     * Gets the order value for this handler.
     *
     * @return the order value for this handler
     */
    @Override
    public int getOrder() {
        return MIN_VALUE + 2;
    }

    /**
     * Checks if query should proceed with tenant filtering.
     *
     * @param executor      the executor
     * @param ms            the mapped statement
     * @param parameter     the parameter
     * @param rowBounds     the row bounds
     * @param resultHandler the result handler
     * @param boundSql      the bound SQL
     * @return true if query should proceed, false otherwise
     */
    @Override
    public boolean isQuery(
            Executor executor,
            MappedStatement ms,
            Object parameter,
            RowBounds rowBounds,
            ResultHandler resultHandler,
            BoundSql boundSql) {
        // If multi-tenancy is not enabled, return true directly
        TenantConfig currentConfig = current();
        if (currentConfig == null) {
            Logger.debug(true, "Mapper", "Tenant query skipped: method={}, reason={}", ms.getId(), "configMissing");
            return true;
        }

        // If tenant filtering is ignored, return true directly
        if (TenantContext.isIgnore()) {
            Logger.debug(true, "Mapper", "Tenant query skipped: method={}, reason={}", ms.getId(), "contextIgnored");
            return true;
        }

        // Check if the Mapper should be ignored
        String mapperId = ms.getId();
        if (currentConfig.isIgnoreMapper(mapperId)) {
            Logger.debug(true, "Mapper", "Tenant query skipped: method={}, reason={}", ms.getId(), "mapperIgnored");
            return true;
        }

        return true;
    }

    /**
     * Processes query result and applies tenant filtering.
     *
     * @param result        the query result
     * @param executor      the executor
     * @param ms            the mapped statement
     * @param parameter     the parameter
     * @param rowBounds     the row bounds
     * @param resultHandler the result handler
     * @param boundSql      the bound SQL
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
        Logger.debug(false, "Mapper", "Tenant query processing started: method={}", ms.getId());
        handleSqlInMappedStatement(ms, parameter, boundSql);
    }

    /**
     * Processes update operation and applies tenant filtering.
     *
     * @param executor  the executor
     * @param ms        the mapped statement
     * @param parameter the parameter
     */
    @Override
    public void update(Executor executor, MappedStatement ms, Object parameter) {
        Logger.debug(
                false,
                "Mapper",
                "Tenant update processing started: method={}, sqlCommandType={}",
                ms.getId(),
                ms.getSqlCommandType());
        handleSqlInMappedStatement(ms, parameter, null);
    }

    /**
     * Processes SQL by updating the current BoundSql or the request scoped rewrite context.
     *
     * @param ms        the MappedStatement
     * @param parameter the parameter object
     * @param boundSql  the BoundSql from interceptor, or {@code null} for update interception
     */
    private void handleSqlInMappedStatement(MappedStatement ms, Object parameter, BoundSql boundSql) {
        // Get current configuration
        TenantConfig currentConfig = current();
        if (currentConfig == null) {
            return;
        }

        // If tenant filtering is ignored, return directly
        if (TenantContext.isIgnore()) {
            return;
        }

        // Check if the Mapper should be ignored
        String mapperId = ms.getId();
        if (currentConfig.isIgnoreMapper(mapperId)) {
            return;
        }

        String originalSql = currentSql(ms, parameter, boundSql);

        // Check if SQL already contains tenant_id condition (e.g., from previous processing)
        // This prevents duplicate tenant conditions and avoids unnecessary re-processing
        String tenantColumn = currentConfig.getColumn();
        if (hasTenantWhereCondition(originalSql, tenantColumn)) {
            Logger.debug(
                    false,
                    "Mapper",
                    "Tenant SQL unchanged: method={}, reason={}, tenantColumnPresent={}",
                    ms.getId(),
                    "conditionAlreadyPresent",
                    true);
            return;
        }

        // Check if this SQL actually needs tenant filtering by using TenantBuilder
        // This checks if the table is in the ignore list
        TenantBuilder builder = new TenantBuilder(currentConfig);
        String tenantSql = builder.handleSql(originalSql, Args.TENANT_ID);

        Logger.debug(
                false,
                "Mapper",
                "Tenant ignore check completed: method={}, ignoreTableCount={}, tenantRequired={}",
                ms.getId(),
                currentConfig.getIgnore() == null ? 0 : currentConfig.getIgnore().size(),
                !originalSql.equals(tenantSql));

        // If SQL wasn't modified (table is ignored), skip tenant ID validation
        if (originalSql.equals(tenantSql)) {
            Logger.debug(false, "Mapper", "Tenant SQL unchanged: method={}, reason={}", ms.getId(), "tableIgnored");
            return;
        }

        // Get current tenant ID using configured resolver
        // Only validate tenant ID when we actually need to add it to SQL
        String tenantId = currentConfig.getProvider().getTenantId();
        if (tenantId == null || tenantId.isEmpty()) {
            // Throw exception without SQL details to avoid duplicate logging
            throw new IllegalStateException("Tenant ID is required but not found. "
                    + "Please set tenant ID using TenantContext.setTenantId() before executing database operations. "
                    + "Mapper: " + mapperId);
        }

        // Rewrite SQL with actual tenant ID
        String actualSql = builder.handleSql(originalSql, tenantId);

        // If SQL hasn't changed, return directly
        if (originalSql.equals(actualSql)) {
            Logger.debug(false, "Mapper", "Tenant SQL unchanged: method={}, reason={}", ms.getId(), "builderNoChange");
            return;
        }

        Logger.debug(
                false,
                "Mapper",
                "Tenant condition applied: method={}, tenantPresent={}, sqlCommandType={}",
                ms.getId(),
                true,
                ms.getSqlCommandType());

        if (boundSql != null && !setBoundSql(boundSql, actualSql)) {
            Logger.warn(
                    false,
                    "Mapper",
                    "Tenant BoundSql update failed: method={}, sqlCommandType={}",
                    ms.getId(),
                    ms.getSqlCommandType());
        }
        putSqlRewrite(ms, actualSql);
    }

    /**
     * Tests whether the SQL WHERE clause already contains the tenant condition.
     *
     * @param sql          the SQL to inspect
     * @param tenantColumn the tenant column
     * @return {@code true} when the WHERE clause contains the tenant condition
     */
    private boolean hasTenantWhereCondition(String sql, String tenantColumn) {
        if (sql == null || tenantColumn == null || tenantColumn.isBlank()) {
            return false;
        }
        String lowerSql = sql.toLowerCase(java.util.Locale.ROOT);
        int whereIndex = lowerSql.indexOf(" where ");
        if (whereIndex < 0) {
            return false;
        }
        String whereClause = lowerSql.substring(whereIndex);
        String column = tenantColumn.toLowerCase(java.util.Locale.ROOT);
        return whereClause.contains(column + " =") || whereClause.contains(column + "=");
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
