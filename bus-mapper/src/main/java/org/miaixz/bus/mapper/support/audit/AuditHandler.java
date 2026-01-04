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
package org.miaixz.bus.mapper.support.audit;

import java.util.Properties;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.Args;
import org.miaixz.bus.mapper.Context;
import org.miaixz.bus.mapper.handler.ConditionHandler;

/**
 * SQL Audit Interceptor
 *
 * <p>
 * Automatically intercepts all SQL executions, records execution time, parameters, results and other information.
 * Supports:
 * </p>
 * <ul>
 * <li>SQL execution time statistics</li>
 * <li>Slow SQL detection and alerting</li>
 * <li>SQL execution failure recording</li>
 * <li>SQL parameters and results recording</li>
 * <li>Custom audit log output</li>
 * </ul>
 *
 * <p>
 * Usage example:
 * </p>
 *
 * <pre>{@code
 * // Configure interceptor
 * AuditConfig config = AuditConfig.builder().enabled(true).slowSqlThreshold(1000) // 1 second
 *         .logParameters(true).logAllSql(false) // Only log slow SQL
 *         .build();
 *
 * AuditHandler interceptor = new AuditHandler(config);
 *
 * // Add to MybatisInterceptor
 * MybatisInterceptor mybatisInterceptor = new MybatisInterceptor();
 * mybatisInterceptor.addHandler(interceptor);
 * }</pre>
 *
 * @param <T> the generic type parameter
 * @author Kimi Liu
 * @since Java 17+
 */
public class AuditHandler<T> extends ConditionHandler<T, AuditConfig> {

    /**
     * Audit configuration from file (lowest priority).
     */
    private AuditConfig config;

    /**
     * Default constructor (uses default configuration).
     */
    public AuditHandler() {

    }

    /**
     * Constructor with file configuration.
     *
     * @param config the audit configuration from file
     */
    public AuditHandler(AuditConfig config) {
        this.config = config;
    }

    /**
     * Get the handler name for logging purposes.
     *
     * @return the handler name "Audit"
     */
    @Override
    public String getHandler() {
        return "Audit";
    }

    /**
     * Sets the audit-related configuration properties. This method is typically called during plugin initialization to
     * configure SQL audit behaviors.
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
        AuditProvider provider = getProvider(properties, AuditProvider.class);

        // Set provider if found
        if (provider == null) {
            Logger.warn(false, getHandler(), "Provider not found, feature will not be enabled");
            return false;
        }

        // Build initial static config
        this.config = buildAuditConfig(datasourceKey, properties, provider);
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @return the scope key for audit configuration
     */
    @Override
    protected String scope() {
        return Args.AUDIT_KEY;
    }

    /**
     * {@inheritDoc}
     *
     * @return the default audit configuration
     */
    @Override
    protected AuditConfig defaults() {
        return config;
    }

    /**
     * {@inheritDoc}
     *
     * @return the captured audit configuration from context
     */
    @Override
    protected AuditConfig capture() {
        Context.MapperConfig contextConfig = Context.getMapperConfig();
        return contextConfig != null ? contextConfig.getAudit() : null;
    }

    /**
     * {@inheritDoc}
     *
     * @param datasourceKey the datasource key
     * @param properties    the properties
     * @return the derived audit configuration
     */
    @Override
    protected AuditConfig derived(String datasourceKey, Properties properties) {
        // Try to get provider from properties
        AuditProvider provider = getProvider(properties, AuditProvider.class);

        // Set provider if found
        if (provider == null) {
            return null;
        }

        return buildAuditConfig(datasourceKey, properties, provider);
    }

    /**
     * Build audit configuration from properties for a specific datasource.
     *
     * @param datasourceKey the datasource key
     * @param properties    the properties
     * @param provider      the audit provider
     * @return the audit configuration
     */
    private AuditConfig buildAuditConfig(String datasourceKey, Properties properties, AuditProvider provider) {
        String sharedPrefix = Args.SHARED_KEY + Symbol.DOT + Args.AUDIT_KEY + Symbol.DOT;
        String dsPrefix = datasourceKey + Symbol.DOT + Args.AUDIT_KEY + Symbol.DOT;

        long slowSqlThreshold = Long.parseLong(
                properties.getProperty(
                        dsPrefix + Args.AUDIT_SLOW_SQL_THRESHOLD,
                        properties.getProperty(sharedPrefix + Args.AUDIT_SLOW_SQL_THRESHOLD, "1000")));
        boolean logParameters = Boolean.parseBoolean(
                properties.getProperty(
                        dsPrefix + Args.AUDIT_LOG_PARAMETERS,
                        properties.getProperty(sharedPrefix + Args.AUDIT_LOG_PARAMETERS, "true")));
        boolean logResults = Boolean.parseBoolean(
                properties.getProperty(
                        dsPrefix + Args.AUDIT_LOG_RESULTS,
                        properties.getProperty(sharedPrefix + Args.AUDIT_LOG_RESULTS, "false")));
        boolean logAllSql = Boolean.parseBoolean(
                properties.getProperty(
                        dsPrefix + Args.AUDIT_LOG_ALL_SQL,
                        properties.getProperty(sharedPrefix + Args.AUDIT_LOG_ALL_SQL, "false")));
        boolean printConsole = Boolean.parseBoolean(
                properties.getProperty(
                        dsPrefix + Args.AUDIT_PRINT_CONSOLE,
                        properties.getProperty(sharedPrefix + Args.AUDIT_PRINT_CONSOLE, "false")));

        return AuditConfig.builder().slowSqlThreshold(slowSqlThreshold).logParameters(logParameters).provider(provider)
                .logResults(logResults).logAllSql(logAllSql).printConsole(printConsole).build();
    }

    /**
     * {@inheritDoc}
     *
     * @return the order value for this handler
     */
    @Override
    public int getOrder() {
        return MIN_VALUE + 7;
    }

    /**
     * {@inheritDoc}
     *
     * @param executor        the executor
     * @param mappedStatement the mapped statement
     * @param parameter       the parameter
     * @return true if update should proceed, false otherwise
     */
    @Override
    public boolean isUpdate(Executor executor, MappedStatement mappedStatement, Object parameter) {
        // Get current configuration
        AuditConfig currentConfig = current();
        if (currentConfig == null || AuditContext.isIgnore()) {
            Logger.debug(true, getHandler(), "Audit disabled or ignored for update: {}", mappedStatement.getId());
            return true;
        }

        // Create builder on-demand for current config and check if should ignore
        AuditBuilder builder = new AuditBuilder(currentConfig);
        if (builder.shouldIgnoreAudit(mappedStatement)) {
            Logger.debug(true, getHandler(), "Audit ignored for mapper: {}", mappedStatement.getId());
            return true;
        }

        Logger.debug(false, getHandler(), "Starting audit for update: {}", mappedStatement.getId());
        // Start audit record
        builder.before(mappedStatement, parameter);
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @param executor        the executor
     * @param mappedStatement the mapped statement
     * @param parameter       the parameter
     * @param rowBounds       the row bounds
     * @param resultHandler   the result handler
     * @param boundSql        the bound SQL
     * @return true if query should proceed, false otherwise
     */
    @Override
    public boolean isQuery(
            Executor executor,
            MappedStatement mappedStatement,
            Object parameter,
            RowBounds rowBounds,
            ResultHandler resultHandler,
            BoundSql boundSql) {
        // Get current configuration
        AuditConfig currentConfig = current();
        if (currentConfig == null || AuditContext.isIgnore()) {
            Logger.debug(true, getHandler(), "Audit disabled or ignored for query: {}", mappedStatement.getId());
            return true;
        }

        // Create builder on-demand for current config and check if should ignore
        AuditBuilder builder = new AuditBuilder(currentConfig);
        if (builder.shouldIgnoreAudit(mappedStatement)) {
            Logger.debug(true, getHandler(), "Audit ignored for mapper: {}", mappedStatement.getId());
            return true;
        }

        Logger.debug(false, getHandler(), "Starting audit for query: {}", mappedStatement.getId());
        // Start audit record
        builder.before(mappedStatement, parameter, boundSql);
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @param result          the query result
     * @param executor        the executor
     * @param mappedStatement the mapped statement
     * @param parameter       the parameter
     * @param rowBounds       the row bounds
     * @param resultHandler   the result handler
     * @param boundSql        the bound SQL
     */
    @Override
    public void query(
            Object result,
            Executor executor,
            MappedStatement mappedStatement,
            Object parameter,
            RowBounds rowBounds,
            ResultHandler resultHandler,
            BoundSql boundSql) {
        // End audit record
        AuditConfig currentConfig = current();
        if (currentConfig != null) {
            Logger.debug(false, getHandler(), "Completing audit for query: {}", mappedStatement.getId());
            AuditBuilder builder = new AuditBuilder(currentConfig);
            builder.after(result, null);
        }
    }

    /**
     * Clear audit cache (no-op since we don't cache builders anymore).
     */
    public void clear() {
        // No-op: builder is created on-demand per SQL execution
    }

    /**
     * Get audit configuration from file (lowest priority).
     *
     * @return the audit configuration
     */
    public AuditConfig getConfig() {
        return this.config;
    }

}
