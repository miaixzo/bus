/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.mapper.support.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * SQL Audit Configuration
 *
 * <p>
 * Used to configure the behavior of audit functionality, including:
 * </p>
 * <ul>
 * <li>Whether to enable audit</li>
 * <li>Slow SQL threshold (SQL exceeding this threshold will be logged as slow SQL)</li>
 * <li>Whether to log SQL parameters</li>
 * <li>Whether to log SQL results</li>
 * <li>Whether to log all SQL (including fast SQL)</li>
 * </ul>
 *
 * <p>
 * Usage example:
 * </p>
 *
 * <pre>{@code
 *
 * // Create configuration
 * AuditConfig config = AuditConfig.builder().slowSqlThreshold(1000) // 1 second
 *         .logParameters(true).logAllSql(false) // Only log slow SQL
 *         .build();
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@SuperBuilder
@AllArgsConstructor
public class AuditConfig {

    /**
     * Default slow SQL threshold (milliseconds)
     */
    public static final long DEFAULT_SLOW_SQL_THRESHOLD = 1000L;

    /**
     * Slow SQL threshold (milliseconds)
     */
    @Builder.Default
    private final long slowSqlThreshold = DEFAULT_SLOW_SQL_THRESHOLD;

    /**
     * Whether to log SQL parameters
     */
    @Builder.Default
    private final boolean logParameters = true;

    /**
     * Whether to log SQL results
     */
    @Builder.Default
    private final boolean logResults = false;

    /**
     * Whether to log all SQL (including fast SQL)
     */
    @Builder.Default
    private final boolean logAllSql = false;

    /**
     * Whether to print audit information to console
     */
    @Builder.Default
    private final boolean printConsole = false;

    /**
     * Audit logger
     */
    private final AuditProvider provider;

    /**
     * Determine whether it is slow SQL
     *
     * @param elapsedTime Execution elapsed time (milliseconds)
     * @return true if it is slow SQL
     */
    public boolean isSlowSql(long elapsedTime) {
        return elapsedTime >= slowSqlThreshold;
    }

}
