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
import java.util.Set;
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
 * Does not depend on third-party SQL parsing libraries; it combines regular expressions with lightweight top-level SQL
 * keyword scanners.
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
            .compile("\\s*SELECT\\s+.*?\\s+FROM\\s+([\\w.]+)\\s*(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /**
     * INSERT statement regex.
     */
    private static final Pattern INSERT_PATTERN = Pattern.compile(
            "\\s*INSERT\\s+INTO\\s+([\\w.]+)\\s*\\(([^)]+)\\)\\s*VALUES\\s*\\(([^)]+)\\)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /**
     * UPDATE statement regex.
     */
    private static final Pattern UPDATE_PATTERN = Pattern.compile(
            "\\s*UPDATE\\s+([\\w.]+)\\s+SET\\s+(.*?)(?:\\s+WHERE\\s+(.*))?$",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /**
     * DELETE statement regex.
     */
    private static final Pattern DELETE_PATTERN = Pattern.compile(
            "\\s*DELETE\\s+FROM\\s+([\\w.]+)\\s*(?:WHERE\\s+(.*))?$",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /**
     * SQL words that may follow a table name but must not be treated as table aliases.
     */
    private static final Set<String> ALIAS_STOP_WORDS = Set.of(
            "WHERE",
            "GROUP",
            "ORDER",
            "HAVING",
            "LIMIT",
            "OFFSET",
            "FETCH",
            "FOR",
            "JOIN",
            "INNER",
            "LEFT",
            "RIGHT",
            "FULL",
            "CROSS",
            "UNION",
            "EXCEPT",
            "INTERSECT");

    /**
     * SQL clauses that can trail the main SELECT condition or table suffix.
     */
    private static final String[] SELECT_TRAILING_CLAUSES = { " ORDER BY ", " GROUP BY ", " HAVING ", " LIMIT ",
            " OFFSET ", " FETCH ", " FOR UPDATE", " FOR SHARE", " UNION " };

    /**
     * Tenant configuration.
     */
    private final TenantConfig config;

    /**
     * SQL cache (original SQL and tenant ID -> actual SQL).
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
        if (ignoredTable(tableName)) {
            return sql;
        }

        // Check if WHERE clause exists in rest (which is everything after the table name)
        int whereIndex = indexOfTopLevelWhere(rest);
        if (whereIndex >= 0) {
            // WHERE exists, add tenant filtering after WHERE keyword
            String beforeWhere = rest.substring(0, whereIndex);
            String tableAlias = tableAlias(beforeWhere);
            String tenantCondition = buildTenantCondition(tenantId, tableAlias);
            SelectCondition condition = splitSelectCondition(rest.substring(whereEnd(rest, whereIndex)));
            String whereClause = condition.condition().trim();
            String tailClause = condition.tail();
            if (whereClause.isEmpty()) {
                return String.format(
                        "SELECT %s FROM %s WHERE %s%s",
                        extractSelectColumns(sql),
                        tableReference(tableName, beforeWhere),
                        tenantCondition,
                        tailClause);
            }
            return String.format(
                    "SELECT %s FROM %s WHERE %s AND (%s)%s",
                    extractSelectColumns(sql),
                    tableReference(tableName, beforeWhere),
                    tenantCondition,
                    whereClause,
                    tailClause);
        } else {
            SelectCondition condition = splitSelectCondition(rest);
            String tableSuffix = condition.condition();
            String tableAlias = tableAlias(tableSuffix);
            String tenantCondition = buildTenantCondition(tenantId, tableAlias);
            // No WHERE, add WHERE clause
            return String.format(
                    "SELECT %s FROM %s WHERE %s%s",
                    extractSelectColumns(sql),
                    tableReference(tableName, tableSuffix),
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
        if (ignoredTable(tableName)) {
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
        if (ignoredTable(tableName)) {
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
        if (ignoredTable(tableName)) {
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
        return buildTenantCondition(tenantId, Normal.EMPTY);
    }

    /**
     * Build tenant filtering condition.
     *
     * @param tenantId   the tenant ID
     * @param tableAlias the real table alias, or empty when absent
     * @return the tenant filtering condition
     */
    private String buildTenantCondition(String tenantId, String tableAlias) {
        String column = config.getColumn();
        if (tableAlias != null && !tableAlias.isBlank()) {
            column = tableAlias + Symbol.DOT + column;
        }
        return String.format("%s = '%s'", column, escapeSql(tenantId));
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
     * Escapes a tenant ID string literal for the generated SQL.
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
     * Builds the table reference while preserving aliases and joins.
     *
     * @param tableName   the main table name
     * @param tableSuffix the SQL fragment after the table name and before WHERE or a trailing clause
     * @return the complete table reference
     */
    private String tableReference(String tableName, String tableSuffix) {
        if (tableSuffix == null || tableSuffix.isBlank()) {
            return tableName;
        }
        return tableName + Symbol.SPACE + tableSuffix.strip();
    }

    /**
     * Resolves the main table alias from the table suffix.
     *
     * @param tableSuffix the SQL fragment after the table name and before WHERE or a trailing clause
     * @return the real alias, or empty when absent
     */
    private String tableAlias(String tableSuffix) {
        if (tableSuffix == null || tableSuffix.isBlank()) {
            return Normal.EMPTY;
        }
        String first = nextToken(tableSuffix, 0);
        if (first == null) {
            return Normal.EMPTY;
        }
        if ("AS".equalsIgnoreCase(first)) {
            int index = tableSuffix.indexOf(first) + first.length();
            String alias = nextToken(tableSuffix, index);
            return alias == null || aliasStopWord(alias) ? Normal.EMPTY : alias;
        }
        return aliasStopWord(first) ? Normal.EMPTY : first;
    }

    /**
     * Returns the next identifier token from a SQL fragment.
     *
     * @param sql   the SQL fragment
     * @param start the scan start index
     * @return the next token, or {@code null}
     */
    private String nextToken(String sql, int start) {
        int index = start;
        while (index < sql.length() && Character.isWhitespace(sql.charAt(index))) {
            index++;
        }
        int begin = index;
        while (index < sql.length()) {
            char current = sql.charAt(index);
            if (!Character.isLetterOrDigit(current) && current != '_') {
                break;
            }
            index++;
        }
        return index > begin ? sql.substring(begin, index) : null;
    }

    /**
     * Tests whether a token cannot be a table alias.
     *
     * @param token the token to test
     * @return {@code true} when the token is not a real alias
     */
    private boolean aliasStopWord(String token) {
        return token == null || ALIAS_STOP_WORDS.contains(token.toUpperCase(java.util.Locale.ROOT));
    }

    /**
     * Tests whether tenant filtering should ignore a table.
     * <p>
     * The configured ignore list historically receives simple table names, while SQL may contain a schema-qualified
     * name such as {@code public.user}. Both forms are checked to keep the existing configuration compatible.
     *
     * @param tableName the table name from SQL
     * @return {@code true} when tenant filtering should be skipped
     */
    private boolean ignoredTable(String tableName) {
        return config.isIgnoreTable(tableName) || config.isIgnoreTable(simpleTableName(tableName));
    }

    /**
     * Returns the table name without a qualifier.
     *
     * @param tableName the table name
     * @return the simple table name
     */
    private String simpleTableName(String tableName) {
        int qualifier = tableName == null ? -1 : tableName.lastIndexOf(Symbol.C_DOT);
        return qualifier < 0 ? tableName : tableName.substring(qualifier + 1);
    }

    /**
     * Finds a top-level WHERE keyword outside strings and nested expressions.
     *
     * @param sql the SQL fragment to scan
     * @return the WHERE index, or {@code -1}
     */
    private int indexOfTopLevelWhere(String sql) {
        int depth = 0;
        boolean singleQuoted = false;
        boolean doubleQuoted = false;
        boolean backtickQuoted = false;
        boolean bracketQuoted = false;
        for (int i = 0; i < sql.length(); i++) {
            char current = sql.charAt(i);
            if (singleQuoted) {
                if (current == Symbol.C_SINGLE_QUOTE && i + 1 < sql.length()
                        && sql.charAt(i + 1) == Symbol.C_SINGLE_QUOTE) {
                    i++;
                    continue;
                }
                if (current == Symbol.C_SINGLE_QUOTE) {
                    singleQuoted = false;
                }
                continue;
            }
            if (doubleQuoted) {
                if (current == Symbol.C_DOUBLE_QUOTES && i + 1 < sql.length()
                        && sql.charAt(i + 1) == Symbol.C_DOUBLE_QUOTES) {
                    i++;
                    continue;
                }
                if (current == Symbol.C_DOUBLE_QUOTES) {
                    doubleQuoted = false;
                }
                continue;
            }
            if (backtickQuoted) {
                if (current == '`') {
                    backtickQuoted = false;
                }
                continue;
            }
            if (bracketQuoted) {
                if (current == ']') {
                    bracketQuoted = false;
                }
                continue;
            }
            if (current == Symbol.C_SINGLE_QUOTE) {
                singleQuoted = true;
                continue;
            }
            if (current == Symbol.C_DOUBLE_QUOTES) {
                doubleQuoted = true;
                continue;
            }
            if (current == '`') {
                backtickQuoted = true;
                continue;
            }
            if (current == '[') {
                bracketQuoted = true;
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
            if (depth == 0 && isKeywordAt(sql, i, "WHERE")) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the end index after a WHERE keyword and following whitespace.
     *
     * @param sql        the SQL fragment
     * @param whereIndex the WHERE start index
     * @return the body start index
     */
    private int whereEnd(String sql, int whereIndex) {
        int index = whereIndex + "WHERE".length();
        while (index < sql.length() && Character.isWhitespace(sql.charAt(index))) {
            index++;
        }
        return index;
    }

    /**
     * Tests whether a keyword starts at the index with word boundaries.
     *
     * @param sql     the SQL fragment
     * @param index   the index to test
     * @param keyword the keyword
     * @return {@code true} when matched
     */
    private boolean isKeywordAt(String sql, int index, String keyword) {
        if (!sql.regionMatches(true, index, keyword, 0, keyword.length())) {
            return false;
        }
        int before = index - 1;
        int after = index + keyword.length();
        return (before < 0 || !identifierPart(sql.charAt(before)))
                && (after >= sql.length() || !identifierPart(sql.charAt(after)));
    }

    /**
     * Tests whether a character is part of an SQL identifier.
     *
     * @param value the character to test
     * @return {@code true} when it belongs to an identifier
     */
    private boolean identifierPart(char value) {
        return Character.isLetterOrDigit(value) || value == '_';
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
        boolean backtickQuoted = false;
        boolean bracketQuoted = false;
        for (int i = 0; i < sql.length(); i++) {
            char current = sql.charAt(i);
            if (singleQuoted) {
                if (current == Symbol.C_SINGLE_QUOTE && i + 1 < sql.length()
                        && sql.charAt(i + 1) == Symbol.C_SINGLE_QUOTE) {
                    i++;
                    continue;
                }
                if (current == Symbol.C_SINGLE_QUOTE) {
                    singleQuoted = false;
                }
                continue;
            }
            if (doubleQuoted) {
                if (current == Symbol.C_DOUBLE_QUOTES && i + 1 < sql.length()
                        && sql.charAt(i + 1) == Symbol.C_DOUBLE_QUOTES) {
                    i++;
                    continue;
                }
                if (current == Symbol.C_DOUBLE_QUOTES) {
                    doubleQuoted = false;
                }
                continue;
            }
            if (backtickQuoted) {
                if (current == '`') {
                    backtickQuoted = false;
                }
                continue;
            }
            if (bracketQuoted) {
                if (current == ']') {
                    bracketQuoted = false;
                }
                continue;
            }
            if (current == Symbol.C_SINGLE_QUOTE) {
                singleQuoted = true;
                continue;
            }
            if (current == Symbol.C_DOUBLE_QUOTES) {
                doubleQuoted = true;
                continue;
            }
            if (current == '`') {
                backtickQuoted = true;
                continue;
            }
            if (current == '[') {
                bracketQuoted = true;
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
