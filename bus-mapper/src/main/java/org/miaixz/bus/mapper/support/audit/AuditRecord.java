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

import java.time.LocalDateTime;
import java.util.Map;

/**
 * SQL Audit Record, used to record detailed information about SQL execution
 *
 * <p>
 * Record content includes:
 * </p>
 * <ul>
 * <li>SQL statement and parameters</li>
 * <li>Execution time (start time, end time, elapsed time)</li>
 * <li>Execution result (affected rows, result count)</li>
 * <li>Execution status (success/failure)</li>
 * <li>Exception information (if any)</li>
 * <li>Execution context (Mapper method, user information, etc.)</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AuditRecord {

    /**
     * SQL statement ID (Mapper method fully qualified name)
     */
    private String sqlId;

    /**
     * SQL statement
     */
    private String sql;

    /**
     * SQL type (SELECT, INSERT, UPDATE, DELETE)
     */
    private String sqlType;

    /**
     * SQL parameters
     */
    private Object parameter;

    /**
     * Start time
     */
    private LocalDateTime startTime;

    /**
     * End time
     */
    private LocalDateTime endTime;

    /**
     * Execution elapsed time (milliseconds)
     */
    private long elapsedTime;

    /**
     * Whether execution was successful
     */
    private boolean success;

    /**
     * Affected rows (INSERT, UPDATE, DELETE)
     */
    private int affectedRows;

    /**
     * Result count (SELECT)
     */
    private int resultCount;

    /**
     * Exception information
     */
    private Throwable exception;

    /**
     * Additional information (user information, IP address, etc.)
     */
    private Map<String, Object> context;

    /**
     * Create audit record
     *
     * @return Audit record instance
     */
    public static AuditRecord create() {
        return new AuditRecord();
    }

    /**
     * Mark start time
     *
     * @return Current instance
     */
    public AuditRecord start() {
        this.startTime = LocalDateTime.now();
        return this;
    }

    /**
     * Mark end time and calculate elapsed time
     *
     * @return Current instance
     */
    public AuditRecord end() {
        this.endTime = LocalDateTime.now();
        if (this.startTime != null) {
            this.elapsedTime = java.time.Duration.between(this.startTime, this.endTime).toMillis();
        }
        return this;
    }

    /**
     * Mark execution as successful
     *
     * @return Current instance
     */
    public AuditRecord success() {
        this.success = true;
        return this;
    }

    /**
     * Mark execution as failed and record exception
     *
     * @param exception Exception information
     * @return Current instance
     */
    public AuditRecord failure(Throwable exception) {
        this.success = false;
        this.exception = exception;
        return this;
    }

    // Getter and Setter methods

    public String getSqlId() {
        return sqlId;
    }

    public AuditRecord sqlId(String sqlId) {
        this.sqlId = sqlId;
        return this;
    }

    public String getSql() {
        return sql;
    }

    public AuditRecord sql(String sql) {
        this.sql = sql;
        return this;
    }

    public String getSqlType() {
        return sqlType;
    }

    public AuditRecord sqlType(String sqlType) {
        this.sqlType = sqlType;
        return this;
    }

    public Object getParameter() {
        return parameter;
    }

    public AuditRecord parameter(Object parameter) {
        this.parameter = parameter;
        return this;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getAffectedRows() {
        return affectedRows;
    }

    public AuditRecord affectedRows(int affectedRows) {
        this.affectedRows = affectedRows;
        return this;
    }

    public int getResultCount() {
        return resultCount;
    }

    public AuditRecord resultCount(int resultCount) {
        this.resultCount = resultCount;
        return this;
    }

    public Throwable getException() {
        return exception;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public AuditRecord context(Map<String, Object> context) {
        this.context = context;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AuditRecord{");
        sb.append("sqlId='").append(sqlId).append('\'');
        sb.append(", sqlType='").append(sqlType).append('\'');
        sb.append(", elapsedTime=").append(elapsedTime).append("ms");
        sb.append(", success=").append(success);
        if (affectedRows > 0) {
            sb.append(", affectedRows=").append(affectedRows);
        }
        if (resultCount > 0) {
            sb.append(", resultCount=").append(resultCount);
        }
        if (exception != null) {
            sb.append(", exception=").append(exception.getClass().getSimpleName());
        }
        sb.append('}');
        return sb.toString();
    }

}
