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
package org.miaixz.bus.mapper.support.paging;

import java.util.regex.Pattern;

import org.miaixz.bus.core.text.PooledStringBuilder;
import org.miaixz.bus.core.text.StringBuilderPool;
import org.miaixz.bus.mapper.Order;

/**
 * Pagination SQL builder that handles sorting and pagination SQL generation.
 *
 * <p>
 * This builder provides functionality to:
 * </p>
 * <ul>
 * <li>Apply sorting to SQL queries using the Sort interface</li>
 * <li>Generate pagination SQL for different database dialects</li>
 * <li>Handle complex Order BY clauses</li>
 * <li>Validate and sanitize sort properties</li>
 * </ul>
 *
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>{@code
 *
 * PageBuilder builder = new PageBuilder();
 *
 * // Apply sorting
 * Sort sort = Sort.by("name").ascending().and("age").descending();
 * String sortedSql = builder.applySort("SELECT * FROM users", sort);
 *
 * // Generate pagination SQL
 * Pageable pageable = Pageable.of(1, 10, sort);
 * String pageSql = builder.buildPaginationSql(sortedSql, pageable, dialect);
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PageBuilder {

    /**
     * Pattern to detect Order BY clause in SQL.
     */
    private static final Pattern ORDER_BY_PATTERN = Pattern.compile(
            "\\bORDER\\s+BY\\s+(?:(?:[^\\s,]+|\"[^\"]*\"|'[^']*')(?:\\s+(?:ASC|DESC))?\\s*(?:,\\s*(?:[^\\s,]+|\"[^\"]*\"|'[^']*')(?:\\s+(?:ASC|DESC))?)*)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    /**
     * Creates a new PageBuilder with default settings.
     */
    public PageBuilder() {
        // No instance state needed
    }

    /**
     * Applies sorting to the given SQL query.
     *
     * @param originalSql the original SQL query
     * @param sort        the sort specification
     * @return the SQL query with sorting applied
     */
    public String applySort(String originalSql, Sort sort) {
        if (originalSql == null || originalSql.trim().isEmpty() || sort == null || !sort.isSorted()) {
            return originalSql;
        }

        // Remove any existing Order BY clause first
        String sqlWithoutOrderBy = removeExistingOrderBy(originalSql);

        // Build Order BY clause
        String orderByClause = buildOrderByClause(sort);

        // Apply Order BY to SQL
        PooledStringBuilder builder = StringBuilderPool
                .acquire(sqlWithoutOrderBy.length() + orderByClause.length() + 10);
        builder.append(sqlWithoutOrderBy);
        if (!sqlWithoutOrderBy.trim().endsWith(";")) {
            builder.append(" ");
        }
        builder.append(orderByClause);
        return builder.toString();
    }

    /**
     * Removes existing Order BY clause from SQL.
     *
     * @param sql the SQL query
     * @return SQL without Order BY clause
     */
    private String removeExistingOrderBy(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return sql;
        }

        // Check if there's an Order BY clause
        if (!ORDER_BY_PATTERN.matcher(sql).find()) {
            return sql;
        }

        // Find the position of Order BY clause
        int orderByIndex = -1;
        String lowerSql = sql.toLowerCase();

        // Look for Order BY that's not in a subquery
        int searchPos = 0;
        while (orderByIndex == -1 && searchPos < lowerSql.length()) {
            int tempIndex = lowerSql.indexOf("order by", searchPos);
            if (tempIndex == -1) {
                break;
            }

            // Check if this Order BY is outside parentheses
            if (!isInSubquery(lowerSql, tempIndex)) {
                orderByIndex = tempIndex;
                break;
            }
            searchPos = tempIndex + 8; // Move past this occurrence
        }

        if (orderByIndex != -1) {
            // Remove Order BY and everything after it
            return sql.substring(0, orderByIndex).trim();
        }

        return sql;
    }

    /**
     * Checks if the given position is within a subquery.
     *
     * @param sql      the SQL string (lowercase)
     * @param position the position to check
     * @return true if the position is in a subquery
     */
    private boolean isInSubquery(String sql, int position) {
        // Count parentheses before the position
        int parenthesesCount = 0;
        for (int i = 0; i < position; i++) {
            char c = sql.charAt(i);
            if (c == '(') {
                parenthesesCount++;
            } else if (c == ')') {
                parenthesesCount--;
            }
        }

        return parenthesesCount > 0;
    }

    /**
     * Builds Order BY clause from Sort object.
     *
     * @param sort the sort specification
     * @return Order BY clause
     */
    private String buildOrderByClause(Sort sort) {
        PooledStringBuilder builder = StringBuilderPool.acquire(256);
        builder.append("Order BY ");

        boolean first = true;
        for (Order order : sort.getOrders()) {
            if (!first) {
                builder.append(", ");
            }

            // Quote property to handle reserved words and special characters
            String property = quoteProperty(order.getProperty());
            builder.append(property);

            if (order.isDescending()) {
                builder.append(" DESC");
            } else {
                builder.append(" ASC");
            }

            first = false;
        }
        return builder.toString();
    }

    /**
     * Quotes a property name to handle SQL identifiers.
     *
     * @param property the property name
     * @return quoted property name
     */
    private String quoteProperty(String property) {
        if (property == null || property.trim().isEmpty()) {
            return property;
        }

        // If already quoted, return as-is
        if ((property.startsWith("\"") && property.endsWith("\""))
                || (property.startsWith("'") && property.endsWith("'"))
                || (property.startsWith("`") && property.endsWith("`"))) {
            return property;
        }

        // Check if property contains special characters or is a reserved word
        if (needsQuoting(property)) {
            return "\"" + property.replace("\"", "\"\"") + "\"";
        }

        return property;
    }

    /**
     * Checks if a property name needs to be quoted.
     *
     * @param property the property name
     * @return true if quoting is needed
     */
    private boolean needsQuoting(String property) {
        if (property == null || property.trim().isEmpty()) {
            return false;
        }

        // Check for spaces or special characters
        if (!property.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            return true;
        }

        // Check for SQL keywords
        String upperProperty = property.toUpperCase();
        return isSqlKeyword(upperProperty);
    }

    /**
     * Checks if a word is a SQL keyword.
     *
     * @param word the word to check (uppercase)
     * @return true if it's a SQL keyword
     */
    private boolean isSqlKeyword(String word) {
        switch (word) {
            case "SELECT":
            case "FROM":
            case "WHERE":
            case "Order":
            case "BY":
            case "GROUP":
            case "HAVING":
            case "JOIN":
            case "INNER":
            case "LEFT":
            case "RIGHT":
            case "OUTER":
            case "ON":
            case "AND":
            case "OR":
            case "NOT":
            case "NULL":
            case "IS":
            case "IN":
            case "EXISTS":
            case "BETWEEN":
            case "LIKE":
            case "AS":
            case "DISTINCT":
            case "ALL":
            case "ANY":
            case "SOME":
            case "UNION":
            case "INTERSECT":
            case "EXCEPT":
                return true;

            default:
                return false;
        }
    }

    /**
     * Result object containing pagination SQL and related information.
     */
    public static class PaginationResult {

        private final String sql;
        private final String countSql;
        private final boolean countRequired;

        /**
         * Creates a new PaginationResult.
         *
         * @param sql           the paginated SQL
         * @param countSql      the count SQL
         * @param countRequired whether a count query is required
         */
        public PaginationResult(String sql, String countSql, boolean countRequired) {
            this.sql = sql;
            this.countSql = countSql;
            this.countRequired = countRequired;
        }

        /**
         * Gets the paginated SQL.
         *
         * @return the paginated SQL
         */
        public String getSql() {
            return sql;
        }

        /**
         * Gets the count SQL.
         *
         * @return the count SQL
         */
        public String getCountSql() {
            return countSql;
        }

        /**
         * Checks if a count query is required.
         *
         * @return true if count query is required
         */
        public boolean isCountRequired() {
            return countRequired;
        }

    }

}
