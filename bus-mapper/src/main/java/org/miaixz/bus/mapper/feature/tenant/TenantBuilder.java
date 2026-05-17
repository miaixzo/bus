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
package org.miaixz.bus.mapper.feature.tenant;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;

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
 * @since Java 21+
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
    private static final Pattern WHERE_PATTERN = Pattern.compile("\\bWHERE\\b\\s*", Pattern.CASE_INSENSITIVE);

    /**
     * SQL clauses that appear after the SELECT WHERE condition.
     */
    private static final String[] SELECT_TRAILING_CLAUSES = { " ORDER BY ", " GROUP BY ", " HAVING ", " LIMIT ",
            " OFFSET ", " FETCH ", " FOR UPDATE", " FOR SHARE", " UNION " };

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
            String beforeWhere = rest.substring(0, whereStart);
            SelectCondition condition = splitSelectCondition(rest.substring(whereEnd));
            String whereClause = condition.condition().trim();
            String tailClause = condition.tail();
            if (whereClause.isEmpty()) {
                return String.format(
                        "SELECT %s FROM %s%s WHERE %s%s",
                        extractSelectColumns(sql),
                        tableName,
                        beforeWhere,
                        tenantCondition,
                        tailClause);
            }
            return String.format(
                    "SELECT %s FROM %s%s WHERE %s AND (%s)%s",
                    extractSelectColumns(sql),
                    tableName,
                    beforeWhere,
                    tenantCondition,
                    whereClause,
                    tailClause);
        } else {
            SelectCondition condition = splitSelectCondition(rest);
            // No WHERE, add WHERE clause
            return String.format(
                    "SELECT %s FROM %s%s WHERE %s%s",
                    extractSelectColumns(sql),
                    tableName,
                    condition.condition(),
                    tenantCondition,
                    condition.tail());
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
     * Splits a SELECT condition body into the filter condition and the trailing SQL clause.
     *
     * @param sql the SQL body after the table name or WHERE keyword
     * @return the split condition
     */
    private SelectCondition splitSelectCondition(String sql) {
        if (sql == null || sql.isEmpty()) {
            return new SelectCondition(Normal.EMPTY, Normal.EMPTY);
        }
        int index = indexOfTrailingClause(sql);
        if (index < 0) {
            return new SelectCondition(sql, Normal.EMPTY);
        }
        String tail = sql.substring(index);
        if (!tail.isEmpty() && !Character.isWhitespace(tail.charAt(0))) {
            tail = Symbol.SPACE + tail;
        }
        return new SelectCondition(sql.substring(0, index), tail);
    }

    /**
     * Finds the first trailing SELECT clause outside strings and nested expressions.
     *
     * @param sql the SQL body to scan
     * @return the trailing clause index, or {@code -1} when no trailing clause exists
     */
    private int indexOfTrailingClause(String sql) {
        int depth = 0;
        boolean singleQuoted = false;
        boolean doubleQuoted = false;
        for (int i = 0; i < sql.length(); i++) {
            char current = sql.charAt(i);
            if (current == '\'' && !doubleQuoted) {
                singleQuoted = !singleQuoted;
                continue;
            }
            if (current == '"' && !singleQuoted) {
                doubleQuoted = !doubleQuoted;
                continue;
            }
            if (singleQuoted || doubleQuoted) {
                continue;
            }
            if (current == '(') {
                depth++;
                continue;
            }
            if (current == ')' && depth > 0) {
                depth--;
                continue;
            }
            if (depth == 0 && isTrailingClauseAt(sql, i)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Tests whether a trailing SELECT clause starts at the specified position.
     *
     * @param sql   the SQL body
     * @param index the position to test
     * @return {@code true} when a trailing clause starts at the position
     */
    private boolean isTrailingClauseAt(String sql, int index) {
        for (String clause : SELECT_TRAILING_CLAUSES) {
            if (sql.regionMatches(true, index, clause, 0, clause.length())) {
                return true;
            }
            if (index == 0 && Character.isWhitespace(clause.charAt(0))) {
                String clauseWithoutLeadingSpace = clause.substring(1);
                if (sql.regionMatches(true, index, clauseWithoutLeadingSpace, 0, clauseWithoutLeadingSpace.length())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * SELECT condition split result.
     *
     * @param condition the condition or table suffix before a trailing clause
     * @param tail      the trailing clause, including the leading space
     */
    private record SelectCondition(String condition, String tail) {
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
