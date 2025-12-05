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
import org.miaixz.bus.mapper.handler.ConditionHandler;

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
 * TenantConfig config = TenantConfig.builder().mode(TenantMode.COLUMN).column("tenant_id")
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
 * TenantConfig config2 = TenantConfig.builder().mode(TenantMode.COLUMN).tenantIdResolver(() -> {
 *     // Custom logic to get tenant ID
 *     return SecurityContextHolder.getTenantId();
 * }).enabled(true).build();
 * }</pre>
 *
 * @param <T> the generic type parameter
 * @author Kimi Liu
 * @since Java 17+
 */
public class TenantHandler<T> extends ConditionHandler<T> {

    /**
     * Tenant configuration from file (lowest priority).
     */
    private TenantConfig config;

    /**
     * Default constructor (uses default configuration).
     */
    public TenantHandler() {

    }

    /**
     * Constructor with file configuration.
     *
     * @param config the tenant configuration from file
     */
    public TenantHandler(TenantConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("TenantConfig cannot be null");
        }
        this.config = config;
    }

    /**
     * Sets the tenant-related configuration properties. This method is typically called during plugin initialization to
     * configure multi-tenancy behaviors.
     *
     * @param properties the configuration properties (contains all datasources)
     * @return true if properties were successfully set, false if properties is null
     */
    @Override
    public boolean setProperties(Properties properties) {
        if (properties == null) {
            return false;
        }

        // Try to get provider from properties
        TenantProvider provider = null;
        Object object = properties.get(Args.PROVIDER_KEY);
        if (object instanceof TenantProvider) {
            provider = (TenantProvider) object;
        }

        // If no provider, tenant feature is not enabled
        if (provider == null) {
            Logger.warn(false, "Mapper", "TenantProvider not found, tenant feature will not be enabled");
            return false;
        }

        // Get current datasource key
        String datasourceKey = Holder.getKey();
        // Use actual default datasource name or fallback to "default"
        if (StringKit.isEmpty(datasourceKey)) {
            datasourceKey = "default";
        }

        // Build configuration paths
        String sharedPrefix = Args.SHARED_KEY + Symbol.DOT + Args.TENANT_KEY + Symbol.DOT;
        String dsPrefix = datasourceKey + Symbol.DOT + Args.TENANT_KEY + Symbol.DOT;

        // Merge configuration: datasource-specific > shared > default
        String column = properties.getProperty(
                dsPrefix + Args.TENANT_COLUMN,
                properties.getProperty(sharedPrefix + Args.TENANT_COLUMN, Args.TENANT_ID));
        String ignoreTablesStr = properties.getProperty(
                dsPrefix + Args.PROP_IGNORE,
                properties.getProperty(sharedPrefix + Args.PROP_IGNORE, Normal.EMPTY));

        List<String> ignoreTables = Arrays.stream(ignoreTablesStr.split(Symbol.COMMA)).map(String::trim)
                .filter(ObjectKit::isNotEmpty).collect(Collectors.toList());

        // Build and store config
        this.config = TenantConfig.builder().column(column).ignore(ignoreTables).provider(provider).build();

        return true;
    }

    /**
     * Get current effective configuration with priority: Context > Provider.getConfig() > File Config.
     *
     * @return the effective tenant configuration
     */
    private TenantConfig getCurrentConfig() {
        // 1. Highest priority: Context configuration
        Context.MapperConfig contextConfig = Context.getMapperConfig();
        if (contextConfig != null && contextConfig.getTenant() != null) {
            return contextConfig.getTenant();
        }

        // 2. Medium priority: Provider's dynamic configuration
        if (config != null && config.getProvider() != null) {
            // Check if provider implements getConfig() method (custom providers might have this)
            TenantProvider provider = config.getProvider();
            if (provider instanceof TenantProvider) {
                // For now, provider doesn't have getConfig(), so use fileConfig
                // In future, if TenantProvider extends Configurable<TenantConfig>, check here
            }
        }

        // 3. Lowest priority: File configuration
        return config;
    }

    @Override
    public int getOrder() {
        return MIN_VALUE + 3;
    }

    @Override
    public boolean isQuery(
            Executor executor,
            MappedStatement ms,
            Object parameter,
            RowBounds rowBounds,
            ResultHandler resultHandler,
            BoundSql boundSql) {
        // If multi-tenancy is not enabled, return true directly
        TenantConfig currentConfig = getCurrentConfig();
        if (currentConfig == null) {
            Logger.debug(true, "Tenant", "Multi-tenancy disabled for query: {}", ms.getId());
            return true;
        }

        // If tenant filtering is ignored, return true directly
        if (TenantContext.isIgnore()) {
            Logger.debug(true, "Tenant", "Tenant filtering ignored for query: {}", ms.getId());
            return true;
        }

        // Check if the Mapper should be ignored
        String mapperId = ms.getId();
        if (currentConfig.isIgnoreMapper(mapperId)) {
            Logger.debug(true, "Tenant", "Mapper ignored for tenant filtering: {}", mapperId);
            return true;
        }

        return true;
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
        Logger.debug(false, "Tenant", "Processing query SQL: {}", ms.getId());
        // Process query SQL
        handleSql(boundSql, ms);
    }

    @Override
    public boolean isUpdate(Executor executor, MappedStatement ms, Object parameter) {
        // If multi-tenancy is not enabled, return true directly
        TenantConfig currentConfig = getCurrentConfig();
        if (currentConfig == null) {
            Logger.debug(true, "Tenant", "Multi-tenancy disabled for update: {}", ms.getId());
            return true;
        }

        // If tenant filtering is ignored, return true directly
        if (TenantContext.isIgnore()) {
            Logger.debug(true, "Tenant", "Tenant filtering ignored for update: {}", ms.getId());
            return true;
        }

        // Check if the Mapper should be ignored
        String mapperId = ms.getId();
        if (currentConfig.isIgnoreMapper(mapperId)) {
            Logger.debug(true, "Tenant", "Mapper ignored for tenant filtering: {}", mapperId);
            return true;
        }

        return true;
    }

    @Override
    public void update(Executor executor, MappedStatement ms, Object parameter) {
        Logger.debug(false, "Tenant", "Processing update SQL: {}", ms.getId());
        // Process update SQL
        BoundSql boundSql = ms.getBoundSql(parameter);
        handleSql(boundSql, ms);
    }

    @Override
    public void prepare(StatementHandler statementHandler) {
        // Process SQL in prepare phase
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);

        // Get BoundSql
        BoundSql boundSql = (BoundSql) metaObject.getValue(DELEGATE_BOUNDSQL);
        if (boundSql == null) {
            Logger.debug(true, "Tenant", "BoundSql is null in prepare phase");
            return;
        }

        // Get MappedStatement
        MappedStatement ms = (MappedStatement) metaObject.getValue(DELEGATE_MAPPEDSTATEMENT);
        if (ms == null) {
            Logger.debug(true, "Tenant", "MappedStatement is null in prepare phase");
            return;
        }

        Logger.debug(false, "Tenant", "Processing SQL in prepare phase: {}", ms.getId());
        // Process SQL
        handleSql(boundSql, ms);
    }

    /**
     * Process SQL.
     *
     * @param boundSql        the BoundSql object
     * @param mappedStatement the MappedStatement object
     */
    private void handleSql(BoundSql boundSql, MappedStatement mappedStatement) {
        // Get current configuration
        TenantConfig currentConfig = getCurrentConfig();
        if (currentConfig == null) {
            return;
        }

        // If tenant filtering is ignored, return directly
        if (TenantContext.isIgnore()) {
            return;
        }

        // Get current tenant ID using configured resolver
        String tenantId = currentConfig.getProvider().getTenantId();
        if (tenantId == null || tenantId.isEmpty()) {
            Logger.warn(false, "Tenant", "Tenant ID not found for mapper: {}", mappedStatement.getId());
            return;
        }

        // Check if the Mapper should be ignored
        String mapperId = mappedStatement.getId();
        if (currentConfig.isIgnoreMapper(mapperId)) {
            return;
        }

        // Get original SQL
        String originalSql = boundSql.getSql();

        // Create builder for current config and rewrite SQL
        TenantBuilder builder = new TenantBuilder(currentConfig);
        String newSql = builder.handleSql(originalSql, tenantId);

        // If SQL hasn't changed, return directly
        if (originalSql.equals(newSql)) {
            Logger.debug(false, "Tenant", "SQL unchanged for mapper: {}", mapperId);
            return;
        }

        Logger.debug(false, "Tenant", "Applied tenant filter (tenantId={}) for mapper: {}", tenantId, mapperId);

        // Use reflection to modify SQL in BoundSql
        MetaObject metaObject = SystemMetaObject.forObject(boundSql);
        metaObject.setValue("sql", newSql);
    }

    /**
     * Get tenant configuration from file (lowest priority).
     *
     * @return the tenant configuration
     */
    public TenantConfig getConfig() {
        return config;
    }

    /**
     * Clear SQL cache (no-op since we don't cache builders anymore).
     */
    public void clear() {
        // No-op: builder is created on-demand per SQL execution
    }

}
