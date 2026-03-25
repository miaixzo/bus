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
package org.miaixz.bus.mapper.support.audit;

import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.provider.MapperProvider;

/**
 * SQL audit logging provider interface.
 *
 * <p>
 * This interface extends {@link MapperProvider} to provide SQL execution audit logging capabilities. Implementations
 * can customize audit logging behavior by overriding the logging methods and optionally providing configuration via
 * {@link #getConfig()}.
 *
 * <p>
 * The interface provides both logging methods and optional configuration support:
 * <ul>
 * <li>Logging methods: {@link #log(AuditRecord)}, {@link #logSlowSql(AuditRecord)},
 * {@link #logFailure(AuditRecord)}</li>
 * <li>Configuration: {@link #getConfig()} - Optional method to provide audit configuration</li>
 * </ul>
 *
 * <h2>Configuration Priority</h2>
 * <ol>
 * <li>Provider.getConfig() - Highest priority</li>
 * <li>Configuration file (application.yml)</li>
 * <li>Default values</li>
 * </ol>
 *
 * <h2>Usage Examples</h2>
 * <p>
 * <b>Example 1: Only logging, use configuration file</b>
 *
 * <pre>{@code
 * @Component
 * public class SimpleAuditProvider implements AuditProvider {
 *
 *     public void logSlowSql(AuditRecord record) {
 *         // Custom slow SQL logging
 *         alertService.sendAlert("Slow SQL detected: " + record.getSqlId());
 *     }
 *
 *     // No getConfig() method - configuration from application.yml
 * }
 * }</pre>
 *
 * <p>
 * <b>Example 2: Full configuration from Provider</b>
 *
 * <pre>{@code
 * @Component
 * public class CustomAuditProvider implements AuditProvider {
 *
 *     public AuditConfig getConfig() {
 *         return AuditConfig.builder().slowSqlThreshold(2000L) // 2 seconds
 *                 .logAllSql(false) // Only log slow SQL
 *                 .logParameters(true) // Include parameters
 *                 .printConsole(true) // Also print to console
 *                 .enabled(true).build();
 *     }
 *
 *     public void logSlowSql(AuditRecord record) {
 *         // Custom slow SQL handling
 *         metricsService.increment("sql.slow.count");
 *     }
 * }
 * }</pre>
 *
 * <p>
 * <b>Example 3: Dynamic configuration based on environment</b>
 *
 * <pre>{@code
 * @Component
 * public class CustomAuditProvider implements AuditProvider {
 *
 *     // @Resource
 *     private Environment env;
 *
 *     public AuditConfig getConfig() {
 *         return AuditConfig.builder().slowSqlThreshold(2000L) // 2 seconds
 *                 .logAllSql(false) // Only log slow SQL
 *                 .logParameters(true) // Include parameters
 *                 .printConsole(true) // Also print to console
 *                 .enabled(true).build();
 *     }
 *
 *     public void logSlowSql(AuditRecord record) {
 *         // Custom slow SQL handling
 *         metricsService.increment("sql.slow.count");
 *     }
 * }
 * }</pre>
 *
 * <pre>{@code
 * @Component
 * public class DynamicAuditProvider implements AuditProvider {
 *
 *     // @Autowired
 *     private Environment env;
 *
 *     public AuditConfig getConfig() {
 *         String profile = env.getProperty("spring.profiles.active", "prod");
 *
 *         if ("dev".equals(profile)) {
 *             return AuditConfig.builder().slowSqlThreshold(500L) // More strict in dev
 *                     .logAllSql(true) // Log all SQL in dev
 *                     .logParameters(true).printConsole(true).enabled(true).build();
 *         } else {
 *             return null; // Use configuration file for other environments
 *         }
 *     }
 * }
 * }</pre>
 *
 * @author Kimi Liu
 *
 * @since Java 21+
 */
public interface AuditProvider extends MapperProvider<AuditConfig> {

    /**
     * Record normal SQL execution audit log.
     *
     * <p>
     * This method is called for all successful SQL executions when {@code logAllSql} is enabled in configuration. The
     * default implementation logs basic information at DEBUG level.
     * </p>
     *
     * <p>
     * Use cases:
     * </p>
     * <ul>
     * <li>Development debugging - Log all SQL to understand query patterns</li>
     * <li>Performance analysis - Collect timing data for all queries</li>
     * <li>Usage statistics - Track which SQL statements are executed most frequently</li>
     * </ul>
     *
     * @param record the audit record containing SQL execution details
     */
    default void log(AuditRecord record) {
        if (record.isSuccess()) {
            Logger.debug(
                    false,
                    "Audit",
                    "SQL: {} | Elapsed: {}ms | Rows: {}",
                    record.getSqlId(),
                    record.getElapsedTime(),
                    record.getAffectedRows() > 0 ? record.getAffectedRows() : record.getResultCount());
        } else {
            Logger.error(
                    false,
                    "Audit",
                    "SQL Failed: {} | Elapsed: {}ms | Error: {}",
                    record.getSqlId(),
                    record.getElapsedTime(),
                    record.getException() != null ? record.getException().getMessage() : "Unknown");
        }
    }

    /**
     * Record slow SQL execution audit log.
     *
     * <p>
     * This method is called when SQL execution time exceeds the configured {@code slowSqlThreshold}. The default
     * implementation logs detailed information including SQL statement and parameters at WARN level.
     * </p>
     *
     * <p>
     * Slow SQL detection is crucial for:
     * </p>
     * <ul>
     * <li>Performance optimization - Identify queries that need index tuning</li>
     * <li>Capacity planning - Understand which queries consume most resources</li>
     * <li>Proactive monitoring - Alert before performance becomes critical</li>
     * <li>Database optimization - Find candidates for query rewriting or caching</li>
     * </ul>
     *
     * <p>
     * Override this method to implement custom slow SQL handling such as:
     * </p>
     * <ul>
     * <li>Send alerts to monitoring systems (PagerDuty, Slack, etc.)</li>
     * <li>Persist to database for historical analysis</li>
     * <li>Trigger automatic performance tuning actions</li>
     * </ul>
     *
     * @param record the audit record containing slow SQL execution details
     */
    default void logSlowSql(AuditRecord record) {
        Logger.warn(
                false,
                "Audit",
                "Slow SQL: {} | Elapsed: {}ms | SQL: {}",
                record.getSqlId(),
                record.getElapsedTime(),
                record.getSql().replaceAll("\\s+", " ").trim());

        if (record.getParameter() != null) {
            Logger.warn(false, "Audit", "Parameters: {}", record.getParameter());
        }
    }

    /**
     * Record SQL execution failure audit log.
     *
     * <p>
     * This method is called when SQL execution fails with an exception. The default implementation logs comprehensive
     * error information including SQL statement, parameters, and exception stack trace at ERROR level.
     * </p>
     *
     * <p>
     * SQL execution failures should be monitored for:
     * </p>
     * <ul>
     * <li>Application errors - Database connection issues, syntax errors, constraint violations</li>
     * <li>Data integrity - Foreign key violations, unique constraint violations</li>
     * <li>Permission issues - Access denied errors, insufficient privileges</li>
     * <li>Resource limits - Deadlocks, lock timeouts, table space issues</li>
     * </ul>
     *
     * <p>
     * Override this method to implement custom failure handling such as:
     * </p>
     * <ul>
     * <li>Send immediate alerts for critical database errors</li>
     * <li>Log to centralized error tracking systems (Sentry, Rollbar)</li>
     * <li>Trigger incident management workflows</li>
     * <li>Collect statistics on error frequency and types</li>
     * </ul>
     *
     * @param record the audit record containing failure details and exception information
     */
    default void logFailure(AuditRecord record) {
        Logger.error(
                false,
                "Audit",
                "SQL Failed: {} | Elapsed: {}ms | SQL: {}",
                record.getSqlId(),
                record.getElapsedTime(),
                record.getSql().replaceAll("\\s+", " ").trim());

        if (record.getParameter() != null) {
            Logger.error(false, "Audit", "Parameters: {}", record.getParameter());
        }

        if (record.getException() != null) {
            Logger.error(false, "Audit", "Exception: {}", record.getException().getMessage(), record.getException());
        }
    }

}
