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
    public static AuditRecord of() {
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
