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
package org.miaixz.bus.mapper.support.tenant;

import java.util.Arrays;
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
public class TenantHandler<T> extends ConditionHandler<T, TenantConfig> {

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

        // Store all properties for dynamic lookup (in parent class)
        this.properties = properties;

        // Get current datasource key for static config initialization
        String datasourceKey = getDatasourceKey();

        // Try to get provider from properties
        TenantProvider provider = getProvider(properties, TenantProvider.class);

        // Build initial static config (will create default provider if column is configured)
        this.config = buildTenantConfig(datasourceKey, properties, provider);
        return this.config != null;
    }

    @Override
    protected String scope() {
        return Args.TENANT_KEY;
    }

    @Override
    protected TenantConfig defaults() {
        return config;
    }

    @Override
    protected TenantConfig capture() {
        Context.MapperConfig contextConfig = Context.getMapperConfig();
        return contextConfig != null ? contextConfig.getTenant() : null;
    }

    @Override
    protected TenantConfig derived(String datasourceKey, Properties properties) {
        // Try to get provider from properties
        TenantProvider provider = getProvider(properties, TenantProvider.class);

        // If no provider, tenant feature is not enabled
        if (provider == null) {
            return null;
        }

        return buildTenantConfig(datasourceKey, properties, provider);
    }

    /**
     * Build tenant configuration from properties for a specific datasource.
     *
     * @param datasourceKey the datasource key
     * @param properties    the properties
     * @param provider      the tenant provider
     * @return the tenant configuration
     */
    private TenantConfig buildTenantConfig(String datasourceKey, Properties properties, TenantProvider provider) {
        String sharedPrefix = Args.SHARED_KEY + Symbol.DOT + Args.TENANT_KEY + Symbol.DOT;
        String dsPrefix = datasourceKey + Symbol.DOT + Args.TENANT_KEY + Symbol.DOT;

        // Merge configuration: datasource-specific > shared > default
        String column = properties.getProperty(
                dsPrefix + Args.TENANT_COLUMN,
                properties.getProperty(sharedPrefix + Args.TENANT_COLUMN, Args.TENANT_ID));
        String ignoreTablesStr = properties.getProperty(
                dsPrefix + Args.PROP_IGNORE,
                properties.getProperty(sharedPrefix + Args.PROP_IGNORE, Normal.EMPTY));

        Logger.debug(false, "Tenant", "Building config for datasource: {}", datasourceKey);
        Logger.debug(false, "Tenant", "  Ignore config key: {}", dsPrefix + Args.PROP_IGNORE);
        Logger.debug(false, "Tenant", "  Ignore raw value: {}", ignoreTablesStr);

        List<String> ignoreTables = Arrays.stream(ignoreTablesStr.split(Symbol.COMMA)).map(String::trim)
                .filter(ObjectKit::isNotEmpty).collect(Collectors.toList());

        Logger.debug(false, "Tenant", "  Ignore parsed list: {}", ignoreTables);

        // If column is configured, create a default provider that gets tenant ID from TenantContext
        TenantProvider finalProvider = provider;
        if (finalProvider == null && StringKit.isNotEmpty(column)) {
            // Create default provider that uses TenantContext
            finalProvider = TenantContext::getTenantId;
        }

        if (finalProvider == null) {
            return null;
        }

        return TenantConfig.builder().column(column).ignore(ignoreTables).provider(finalProvider).build();
    }

    @Override
    public int getOrder() {
        return MIN_VALUE + 2;
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
        TenantConfig currentConfig = current();
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
        handleSqlInMappedStatement(ms, parameter, boundSql);
    }

    @Override
    public void update(Executor executor, MappedStatement ms, Object parameter) {
        Logger.debug(false, "Tenant", "Processing update SQL: {}", ms.getId());
        // Get BoundSql and process SQL by modifying MappedStatement's SqlSource
        BoundSql boundSql = ms.getBoundSql(parameter);
        handleSqlInMappedStatement(ms, parameter, boundSql);
    }

    /**
     * Process SQL by modifying the MappedStatement's SqlSource.
     *
     * <p>
     * <strong>Performance Optimization:</strong> Checks if the SqlSource has already been replaced by our custom
     * SqlSource. If yes, processing is skipped since the SQL has already been modified. This provides O(1) cache-like
     * performance without the issues of request-level caching.
     * </p>
     *
     * @param ms        the MappedStatement
     * @param parameter the parameter object
     * @param boundSql  the current BoundSql
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

        // Optimization: Check if SqlSource has already been replaced (O(1))
        // This is safe because once replaced, all subsequent calls will use the actual SQL
        if (SqlSource.class.isInstance(ms.getSqlSource())) {
            Logger.debug(false, "Tenant", "SqlSource already replaced for: {}", mapperId);
            return;
        }

        // Get original SQL
        String originalSql = boundSql.getSql();

        // Check if this SQL actually needs tenant filtering by using TenantBuilder
        // This checks if the table is in the ignore list
        TenantBuilder builder = new TenantBuilder(currentConfig);
        String testSql = builder.handleSql(originalSql, "__TEST__");

        Logger.debug(false, "Tenant", "Ignore check - Table: {}", currentConfig.getIgnore());
        Logger.debug(false, "Tenant", "Ignore check - SQL modified: {}", !originalSql.equals(testSql));

        // If SQL wasn't modified (table is ignored), skip tenant ID validation
        if (originalSql.equals(testSql)) {
            Logger.debug(false, "Tenant", "Table is in ignore list, skipping tenant filtering");
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
            Logger.debug(false, "Tenant", "SQL unchanged for mapper: {}", mapperId);
            return;
        }

        Logger.debug(false, "Tenant", "Applied tenant filter (tenantId={}) for mapper: {}", tenantId, mapperId);

        // Step 1: Modify the BoundSql parameter using reflection
        // This ensures the current BoundSql instance is modified for current execution
        if (!setBoundSql(boundSql, actualSql)) {
            Logger.warn(false, "Tenant", "Failed to modify BoundSql.sql");
            // Fallback: continue with SqlSource replacement only
        }

        // Step 2: Replace the SqlSource in MappedStatement
        // This ensures subsequent getBoundSql() calls return the actual SQL
        replaceSqlSource(ms, boundSql, actualSql);
        Logger.debug(false, "Tenant", "Replaced MappedStatement.sqlSource");
    }

    /**
     * Get tenant configuration from file (lowest priority).
     *
     * @return the tenant configuration
     */
    public TenantConfig getConfig() {
        return config;
    }

}
