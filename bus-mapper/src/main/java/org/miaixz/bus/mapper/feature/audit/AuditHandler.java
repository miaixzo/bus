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
 * SQL audit handler.
 *
 * <p>
 * Participates in the mapper interceptor chain and records auditable query executions when an {@link AuditProvider} is
 * available. Query audit completion depends on an earlier query handler supplying the actual result to the shared
 * handler result holder. Update statements are currently allowed to proceed without audit record creation because the
 * existing interceptor lifecycle does not expose a safe update completion callback to this handler.
 * </p>
 *
 * <p>
 * Supports:
 * </p>
 * <ul>
 * <li>SQL execution time statistics</li>
 * <li>Slow SQL detection and alerting</li>
 * <li>SQL parameters and results recording</li>
 * <li>Custom audit log output</li>
 * </ul>
 *
 * <p>
 * Usage example:
 * </p>
 *
 * <pre>{@code
 * // Configure handler
 * AuditConfig config = AuditConfig.builder().slowSqlThreshold(1000) // 1 second
 *         .logParameters(true).logAllSql(false) // Only log slow SQL
 *         .provider(auditProvider).build();
 *
 * AuditHandler handler = new AuditHandler(config);
 *
 * // Add to MybatisInterceptor
 * MybatisInterceptor mybatisInterceptor = new MybatisInterceptor();
 * mybatisInterceptor.addHandler(handler);
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
     * Allows update execution and skips update audit record creation.
     * <p>
     * The current mapper interceptor lifecycle exposes the pre-update decision point to handlers but does not provide a
     * matching post-update callback with affected row count. This method therefore only applies ignore/config checks
     * and leaves the statement execution unchanged.
     *
     * @param executor        the executor
     * @param mappedStatement the mapped statement
     * @param parameter       the parameter
     * @return always {@code true} to allow the update to proceed
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
                "Audit update skipped: method={}, reason={}, sqlType={}, parameterPresent={}",
                mappedStatement.getId(),
                "unsupportedUpdateLifecycle",
                mappedStatement.getSqlCommandType(),
                parameter != null);
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
        AuditConfig currentConfig = current();
        if (currentConfig == null) {
            AuditContext.removeRecord();
            return;
        }
        Object actualResult = suppliedResult(result);
        if (actualResult == null) {
            Logger.debug(
                    true,
                    "Mapper",
                    "Audit query deferred: method={}, reason={}",
                    mappedStatement.getId(),
                    "resultNotSuppliedByPreviousHandler");
            AuditContext.removeRecord();
            return;
        }
        Logger.debug(
                false,
                "Mapper",
                "Audit query completed: method={}, resultType={}",
                mappedStatement.getId(),
                actualResult.getClass().getName());
        AuditBuilder builder = new AuditBuilder(currentConfig);
        builder.after(actualResult, null);
    }

    /**
     * Returns the actual query result supplied by an earlier query handler.
     * <p>
     * The mapper interceptor passes a mutable single-slot holder into handler query callbacks. Audit must only complete
     * a record when a previous handler, such as pagination, has already supplied the real result in that holder.
     *
     * @param result the handler callback result argument
     * @return the real query result, or {@code null} when not available yet
     */
    private Object suppliedResult(Object result) {
        if (result instanceof Object[] holder) {
            return holder.length > 0 ? holder[0] : null;
        }
        return result;
    }

    /**
     * Clear audit cache (no-op since we don't cache builders anymore).
     */
    public void clear() {
        // No-op: builder is created on-demand per SQL execution
    }

}
