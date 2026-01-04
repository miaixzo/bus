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

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tenant SQL handler.
 *
 * <p>
 * Responsible for rewriting SQL to add tenant filtering conditions.
 * </p>
 *
 * <p>
 * Does not depend on third-party SQL parsing libraries, uses regular expressions for implementation.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TenantBuilder {

    /**
     * SELECT statement regex (case insensitive).
     */
    private static final Pattern SELECT_PATTERN = Pattern
            .compile("\\s*SELECT\\s+.*?\\s+FROM\\s+(\\w+)\\s*(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    /**
     * INSERT statement regex.
     */
    private static final Pattern INSERT_PATTERN = Pattern.compile(
            "\\s*INSERT\\s+INTO\\s+(\\w+)\\s*\\(([^)]+)\\)\\s*VALUES\\s*\\(([^)]+)\\)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    /**
     * UPDATE statement regex.
     */
    private static final Pattern UPDATE_PATTERN = Pattern.compile(
            "\\s*UPDATE\\s+(\\w+)\\s+SET\\s+(.*?)(?:\\s+WHERE\\s+(.*))?$",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    /**
     * DELETE statement regex.
     */
    private static final Pattern DELETE_PATTERN = Pattern
            .compile("\\s*DELETE\\s+FROM\\s+(\\w+)\\s*(?:WHERE\\s+(.*))?$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    /**
     * WHERE clause regex.
     */
    private static final Pattern WHERE_PATTERN = Pattern.compile("\\s+WHERE\\s+", Pattern.CASE_INSENSITIVE);
    /**
     * Tenant configuration.
     */
    private final TenantConfig config;
    /**
     * SQL cache (original SQL -> actual SQL).
     */
    private final Map<String, String> sqlCache;

    /**
     * Constructor.
     *
     * @param config the tenant configuration
     */
    public TenantBuilder(TenantConfig config) {
        this.config = config;
        this.sqlCache = config.isEnableSqlCache() ? new ConcurrentHashMap<>() : null;
    }

    /**
     * Process SQL.
     *
     * @param originalSql the original SQL
     * @param tenantId    the tenant ID
     * @return the actual SQL
     */
    public String handleSql(String originalSql, String tenantId) {
        // If tenant filtering is ignored, return original SQL directly
        if (TenantContext.isIgnore()) {
            return originalSql;
        }

        // If tenant ID is empty, return original SQL directly
        if (tenantId == null || tenantId.isEmpty()) {
            return originalSql;
        }

        // If cache is enabled, retrieve from cache first
        if (sqlCache != null) {
            String cacheKey = originalSql + ":" + tenantId;
            return sqlCache.computeIfAbsent(cacheKey, k -> doHandleSql(originalSql, tenantId));
        }

        return doHandleSql(originalSql, tenantId);
    }

    /**
     * Actually process SQL.
     *
     * @param originalSql the original SQL
     * @param tenantId    the tenant ID
     * @return the actual SQL
     */
    private String doHandleSql(String originalSql, String tenantId) {
        String sql = originalSql.trim();

        // Determine SQL type and process
        if (sql.toUpperCase().startsWith("SELECT")) {
            return handleSelect(sql, tenantId);
        } else if (sql.toUpperCase().startsWith("INSERT")) {
            return handleInsert(sql, tenantId);
        } else if (sql.toUpperCase().startsWith("UPDATE")) {
            return handleUpdate(sql, tenantId);
        } else if (sql.toUpperCase().startsWith("DELETE")) {
            return handleDelete(sql, tenantId);
        }

        // Don't process other types of SQL
        return originalSql;
    }

    /**
     * Process SELECT statement.
     *
     * @param sql      the original SQL
     * @param tenantId the tenant ID
     * @return the actual SQL
     */
    private String handleSelect(String sql, String tenantId) {
        Matcher matcher = SELECT_PATTERN.matcher(sql);
        if (!matcher.matches()) {
            return sql;
        }

        String tableName = matcher.group(1);
        String rest = matcher.group(2);

        // Check if the table should be ignored
        if (config.isIgnoreTable(tableName)) {
            return sql;
        }

        // Build tenant condition
        String tenantCondition = buildTenantCondition(tenantId);

        // Check if WHERE clause exists in rest (which is everything after the table name)
        Matcher whereMatcher = WHERE_PATTERN.matcher(rest);
        if (whereMatcher.find()) {
            // WHERE exists, add tenant filtering after WHERE keyword
            int whereStart = whereMatcher.start();
            int whereEnd = whereMatcher.end();
            String beforeWhere = rest.substring(0, whereEnd); // Includes "WHERE "
            String afterWhere = rest.substring(whereEnd); // Original WHERE conditions
            return String.format(
                    "SELECT %s FROM %s%s%s AND (%s)",
                    extractSelectColumns(sql),
                    tableName,
                    beforeWhere,
                    tenantCondition,
                    afterWhere);
        } else {
            // No WHERE, add WHERE clause
            return String.format(
                    "SELECT %s FROM %s WHERE %s %s",
                    extractSelectColumns(sql),
                    tableName,
                    tenantCondition,
                    rest);
        }
    }

    /**
     * Process INSERT statement.
     *
     * @param sql      the original SQL
     * @param tenantId the tenant ID
     * @return the actual SQL
     */
    private String handleInsert(String sql, String tenantId) {
        Matcher matcher = INSERT_PATTERN.matcher(sql);
        if (!matcher.matches()) {
            return sql;
        }

        String tableName = matcher.group(1);
        String columns = matcher.group(2);
        String values = matcher.group(3);

        // Check if the table should be ignored
        if (config.isIgnoreTable(tableName)) {
            return sql;
        }

        // Check if tenant ID column is already included
        String tenantColumn = config.getColumn();
        if (columns.toLowerCase().contains(tenantColumn.toLowerCase())) {
            return sql;
        }

        // Add tenant ID column and value
        String newColumns = columns + ", " + tenantColumn;
        String newValues = values + ", '" + tenantId + "'";

        return String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, newColumns, newValues);
    }

    /**
     * Process UPDATE statement.
     *
     * @param sql      the original SQL
     * @param tenantId the tenant ID
     * @return the actual SQL
     */
    private String handleUpdate(String sql, String tenantId) {
        Matcher matcher = UPDATE_PATTERN.matcher(sql);
        if (!matcher.matches()) {
            return sql;
        }

        String tableName = matcher.group(1);
        String setClause = matcher.group(2);
        String whereClause = matcher.group(3);

        // Check if the table should be ignored
        if (config.isIgnoreTable(tableName)) {
            return sql;
        }

        // Build tenant condition
        String tenantCondition = buildTenantCondition(tenantId);

        // Add tenant filtering condition
        if (whereClause != null && !whereClause.trim().isEmpty()) {
            return String
                    .format("UPDATE %s SET %s WHERE %s AND (%s)", tableName, setClause, tenantCondition, whereClause);
        } else {
            return String.format("UPDATE %s SET %s WHERE %s", tableName, setClause, tenantCondition);
        }
    }

    /**
     * Process DELETE statement.
     *
     * @param sql      the original SQL
     * @param tenantId the tenant ID
     * @return the actual SQL
     */
    private String handleDelete(String sql, String tenantId) {
        Matcher matcher = DELETE_PATTERN.matcher(sql);
        if (!matcher.matches()) {
            return sql;
        }

        String tableName = matcher.group(1);
        String whereClause = matcher.group(2);

        // Check if the table should be ignored
        if (config.isIgnoreTable(tableName)) {
            return sql;
        }

        // Build tenant condition
        String tenantCondition = buildTenantCondition(tenantId);

        // Add tenant filtering condition
        if (whereClause != null && !whereClause.trim().isEmpty()) {
            return String.format("DELETE FROM %s WHERE %s AND (%s)", tableName, tenantCondition, whereClause);
        } else {
            return String.format("DELETE FROM %s WHERE %s", tableName, tenantCondition);
        }
    }

    /**
     * Build tenant filtering condition.
     *
     * @param tenantId the tenant ID
     * @return the tenant filtering condition
     */
    private String buildTenantCondition(String tenantId) {
        return String.format("%s = '%s'", config.getColumn(), escapeSql(tenantId));
    }

    /**
     * Extract SELECT columns.
     *
     * @param sql the SQL statement
     * @return the SELECT columns part
     */
    private String extractSelectColumns(String sql) {
        int selectIndex = sql.toUpperCase().indexOf("SELECT");
        int fromIndex = sql.toUpperCase().indexOf("FROM");
        if (selectIndex >= 0 && fromIndex > selectIndex) {
            return sql.substring(selectIndex + 6, fromIndex).trim();
        }
        return Symbol.STAR;
    }

    /**
     * SQL escaping (to prevent SQL injection).
     *
     * @param value the original value
     * @return the escaped value
     */
    private String escapeSql(String value) {
        if (value == null) {
            return Normal.EMPTY;
        }
        // Escape single quotes
        return value.replace(Symbol.SINGLE_QUOTE, "''");
    }

    /**
     * Clear SQL cache.
     */
    public void clear() {
        if (sqlCache != null) {
            sqlCache.clear();
        }
    }

}
