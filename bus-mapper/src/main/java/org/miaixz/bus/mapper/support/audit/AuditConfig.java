/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
