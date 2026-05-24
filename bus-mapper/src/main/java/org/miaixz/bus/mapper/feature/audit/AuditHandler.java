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
package org.miaixz.bus.mapper.feature.audit;

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
import org.miaixz.bus.mapper.handler.ScopedProviderHandler;

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
 * @since Java 21+
 */
public class AuditHandler<T> extends ScopedProviderHandler<T, AuditConfig, AuditProvider> {

    /**
     * Default constructor (uses default configuration).
     */
    public AuditHandler() {
        super();
    }

    /**
     * Constructor with file configuration.
     *
     * @param config the audit configuration from file
     */
    public AuditHandler(AuditConfig config) {
        super(config);
    }

    /**
     * Gets the scope key for audit configuration.
     *
     * @return the scope key for audit configuration
     */
    @Override
    protected String scope() {
        return Args.AUDIT_KEY;
    }

    /**
     * Gets the captured audit configuration from context.
     *
     * @return the captured audit configuration from context
     */
    @Override
    protected AuditConfig capture() {
        Context.MapperConfig contextConfig = Context.getMapperConfig();
        return contextConfig != null ? contextConfig.getAudit() : null;
    }

    /**
     * Returns the audit provider contract.
     *
     * @return the audit provider contract type
     */
    @Override
    protected Class<AuditProvider> type() {
        return AuditProvider.class;
    }

    /**
     * Returns whether audit configuration requires an audit provider.
     *
     * @return {@code true}
     */
    @Override
    protected boolean requiresProvider() {
        return true;
    }

    /**
     * Resolves audit configuration from properties for a specific datasource.
     *
     * @param datasourceKey the datasource key
     * @param properties    the properties
     * @param provider      the audit provider
     * @return the audit configuration
     */
    @Override
    protected AuditConfig resolve(String datasourceKey, Properties properties, AuditProvider provider) {
        if (provider == null) {
            return null;
        }
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
     * Gets the order value for this handler.
     *
     * @return the order value for this handler
     */
    @Override
    public int getOrder() {
        return MIN_VALUE + 7;
    }

    /**
     * Checks if update should proceed with audit logging.
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
            Logger.debug(
                    true,
                    "Mapper",
                    "Audit update skipped: method={}, reason={}",
                    mappedStatement.getId(),
                    currentConfig == null ? "configMissing" : "contextIgnored");
            return true;
        }

        // Create builder on-demand for current config and check if should ignore
        AuditBuilder builder = new AuditBuilder(currentConfig);
        if (builder.shouldIgnoreAudit(mappedStatement)) {
            Logger.debug(
                    true,
                    "Mapper",
                    "Audit update skipped by mapper ignore rule: method={}",
                    mappedStatement.getId());
            return true;
        }

        Logger.debug(
                true,
                "Mapper",
                "Audit update started: method={}, sqlType={}, parameterPresent={}",
                mappedStatement.getId(),
                mappedStatement.getSqlCommandType(),
                parameter != null);
        // Start audit record
        builder.before(mappedStatement, parameter);
        return true;
    }

    /**
     * Checks if query should proceed with audit logging.
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
            Logger.debug(
                    true,
                    "Mapper",
                    "Audit query skipped: method={}, reason={}",
                    mappedStatement.getId(),
                    currentConfig == null ? "configMissing" : "contextIgnored");
            return true;
        }

        // Create builder on-demand for current config and check if should ignore
        AuditBuilder builder = new AuditBuilder(currentConfig);
        if (builder.shouldIgnoreAudit(mappedStatement)) {
            Logger.debug(
                    true,
                    "Mapper",
                    "Audit query skipped by mapper ignore rule: method={}",
                    mappedStatement.getId());
            return true;
        }

        Logger.debug(
                true,
                "Mapper",
                "Audit query started: method={}, rowOffset={}, rowLimit={}, parameterMappings={}, parameterPresent={}",
                mappedStatement.getId(),
                rowBounds.getOffset(),
                rowBounds.getLimit(),
                boundSql.getParameterMappings().size(),
                parameter != null);
        // Start audit record
        builder.before(mappedStatement, parameter, boundSql);
        return true;
    }

    /**
     * Processes query result and completes audit logging.
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
            Logger.debug(
                    false,
                    "Mapper",
                    "Audit query completed: method={}, resultType={}",
                    mappedStatement.getId(),
                    result == null ? "null" : result.getClass().getName());
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

}
