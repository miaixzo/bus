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

import java.time.LocalDateTime;
import java.util.Map;

import lombok.Getter;

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
 * @since Java 21+
 */
@Getter
public class AuditRecord {

    /**
     * Constructs a new AuditRecord instance.
     */
    public AuditRecord() {
        // No initialization required.
    }

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

    /**
     * Sets the mapped statement identifier.
     *
     * @param sqlId the mapped statement identifier
     * @return the current audit record
     */
    public AuditRecord sqlId(String sqlId) {
        this.sqlId = sqlId;
        return this;
    }

    /**
     * Sets the SQL text.
     *
     * @param sql the SQL text
     * @return the current audit record
     */
    public AuditRecord sql(String sql) {
        this.sql = sql;
        return this;
    }

    /**
     * Sets the SQL operation type.
     *
     * @param sqlType the SQL operation type
     * @return the current audit record
     */
    public AuditRecord sqlType(String sqlType) {
        this.sqlType = sqlType;
        return this;
    }

    /**
     * Sets the SQL parameter object.
     *
     * @param parameter the SQL parameter object
     * @return the current audit record
     */
    public AuditRecord parameter(Object parameter) {
        this.parameter = parameter;
        return this;
    }

    /**
     * Sets the affected row count.
     *
     * @param affectedRows the affected row count
     * @return the current audit record
     */
    public AuditRecord affectedRows(int affectedRows) {
        this.affectedRows = affectedRows;
        return this;
    }

    /**
     * Sets the returned result count.
     *
     * @param resultCount the returned result count
     * @return the current audit record
     */
    public AuditRecord resultCount(int resultCount) {
        this.resultCount = resultCount;
        return this;
    }

    /**
     * Sets additional audit context values.
     *
     * @param context additional audit context values
     * @return the current audit record
     */
    public AuditRecord context(Map<String, Object> context) {
        this.context = context;
        return this;
    }

    /**
     * Returns a readable representation of this audit record.
     *
     * @return the string representation
     */
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
