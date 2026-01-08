/*
 * ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 * ~                                                                               ~
 * ~ The MIT License (MIT)                                                         ~
 * ~                                                                               ~
 * ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 * ~                                                                               ~
 * ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 * ~ of this software and associated documentation files (the "Software"), to deal ~
 * ~ in the Software without restriction, including without limitation the rights  ~
 * ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 * ~ copies of the Software, and to permit persons to whom the Software is         ~
 * ~ furnished to do so, subject to the following conditions:                      ~
 * ~                                                                               ~
 * ~ The above copyright notice and this permission notice shall be included in    ~
 * ~ all copies or substantial portions of the Software.                           ~
 * ~                                                                               ~
 * ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 * ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 * ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 * ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 * ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 * ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 * ~ THE SOFTWARE.                                                                 ~
 * ~                                                                               ~
 * ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 */
package org.miaixz.bus.mapper.dialect;

import org.miaixz.bus.mapper.support.paging.Pageable;

/**
 * AbstractDialect base class for database dialects providing common functionality and default implementations for the
 * {@link Dialect} interface methods.
 *
 * <p>
 * This class handles basic dialect identification (database name and JDBC prefix) and provides a standard
 * {@code LIMIT OFFSET} pagination strategy applicable to many SQL databases.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractDialect implements Dialect {

    /**
     * Recommended extra capacity to allocate for {@link StringBuilder} when constructing simple pagination SQL (e.g.,
     * standard {@code LIMIT OFFSET} clauses).
     */
    protected static final int PAGINATION_SQL_EXTRA_CAPACITY = 50;

    /**
     * The canonical name of the database product (e.g., "MySQL", "PostgreSQL").
     */
    private final String databaseName;

    /**
     * The standard JDBC URL prefix for this database (e.g., "jdbc:mysql:").
     */
    private final String jdbcUrlPrefix;

    /**
     * Constructs an AbstractDialect with the specified database name and JDBC URL prefix.
     *
     * @param databaseName  The canonical database name (e.g., "mysql").
     * @param jdbcUrlPrefix The standard JDBC URL prefix (e.g., "jdbc:mysql:").
     */
    protected AbstractDialect(String databaseName, String jdbcUrlPrefix) {
        this.databaseName = databaseName;
        this.jdbcUrlPrefix = jdbcUrlPrefix;
    }

    /**
     * Retrieves the canonical name of the database product.
     *
     * @return The database name (e.g., "MySQL").
     */
    @Override
    public String getDatabase() {
        return databaseName;
    }

    /**
     * Retrieves the standard JDBC URL prefix for this dialect.
     *
     * @return The JDBC URL prefix (e.g., "jdbc:mysql:").
     */
    @Override
    public String getJdbcPrefix() {
        return jdbcUrlPrefix;
    }

    /**
     * Checks if this dialect supports a given database product name.
     *
     * <p>
     * The check is performed by converting both the product name and the dialect's database name to lower case and
     * checking if the product name contains the database name.
     * </p>
     *
     * @param productName The product name, typically from {@code DatabaseMetaData.getDatabaseProductName()}.
     * @return {@code true} if the dialect supports the product, {@code false} otherwise.
     */
    @Override
    public boolean supportsProduct(String productName) {
        if (productName == null || productName.isEmpty()) {
            return false;
        }
        String lowerName = productName.toLowerCase();
        String lowerDbName = databaseName.toLowerCase();
        return lowerName.contains(lowerDbName);
    }

    /**
     * Checks if this dialect supports a given JDBC URL.
     *
     * <p>
     * The check is performed by converting both the JDBC URL and the dialect's JDBC prefix to lower case and checking
     * if the URL starts with the prefix.
     * </p>
     *
     * @param jdbcUrl The JDBC connection URL.
     * @return {@code true} if the dialect supports the URL prefix, {@code false} otherwise.
     */
    @Override
    public boolean supportsUrl(String jdbcUrl) {
        if (jdbcUrl == null || jdbcUrl.isEmpty()) {
            return false;
        }
        return jdbcUrl.toLowerCase().startsWith(jdbcUrlPrefix.toLowerCase());
    }

    /**
     * Provides a default implementation for generating the count SQL statement from an original SQL query.
     *
     * <p>
     * The default implementation wraps the original SQL in a subquery:
     * {@code SELECT COUNT(*) FROM (originalSql) AS total_count}. This is generally safe but can be inefficient for
     * complex queries. Dialect implementations should override this method if a more optimized counting mechanism is
     * available (e.g., removing Order BY clauses).
     * </p>
     *
     * @param originalSql The original SQL query string.
     * @return The SQL query used to get the total row count.
     */
    @Override
    public String getCountSql(String originalSql) {
        // Default count SQL implementation
        return "SELECT COUNT(*) FROM (" + originalSql + ") AS total_count";
    }

    /**
     * Indicates whether the database product generally supports JDBC batch operations.
     *
     * @return {@code true} by default.
     */
    @Override
    public boolean supportsJdbcBatch() {
        return true;
    }

    /**
     * Indicates whether the database product supports limiting results without an offset (i.e., just LIMIT n).
     *
     * @return {@code true} by default.
     */
    @Override
    public boolean supportsLimitWithoutOffset() {
        return true;
    }

    /**
     * Gets the maximum number of value tuples supported in a single multi-value INSERT statement (e.g.,
     * {@code INSERT INTO T (c) VALUES (v1), (v2), ...}).
     *
     * @return The maximum size, or -1 if unlimited (the default).
     */
    @Override
    public int getMaxMultiValuesInsertSize() {
        return -1; // Unlimited
    }

    /**
     * Build standard {@code LIMIT OFFSET} pagination SQL.
     *
     * <p>
     * This template method is used by subclasses to construct pagination queries for databases that support the
     * standard {@code LIMIT <pageSize> OFFSET <offset>} syntax.
     * </p>
     *
     * <p>
     * **Applicable Databases:**
     * </p>
     * <ul>
     * <li>MySQL</li>
     * <li>PostgreSQL</li>
     * <li>H2</li>
     * <li>SQLite</li>
     * <li>CirroData</li>
     * <li>HerdDB</li>
     * <li>Xugudb</li>
     * <li>Oscar</li>
     * <li>Hsqldb</li>
     * </ul>
     *
     * @param originalSql The original SQL statement.
     * @param pageable    The pagination object containing page size and offset information.
     * @return The paginated SQL statement using {@code LIMIT} and {@code OFFSET}.
     * @see Dialect#getLimitKeyword()
     * @see Dialect#getOffsetKeyword()
     */
    protected String buildLimitOffsetPagination(String originalSql, Pageable pageable) {
        if (pageable.isUnpaged()) {
            return originalSql;
        }

        StringBuilder sql = new StringBuilder(originalSql.length() + PAGINATION_SQL_EXTRA_CAPACITY);
        sql.append(originalSql);
        sql.append(" ").append(getLimitKeyword()).append(" ").append(pageable.getPageSize());

        if (pageable.getOffset() > 0) {
            sql.append(" ").append(getOffsetKeyword()).append(" ").append(pageable.getOffset());
        }

        return sql.toString();
    }

    /**
     * Returns a string representation of this dialect, showing its database name.
     *
     * @return A string in the format "Dialect[databaseName]".
     */
    @Override
    public String toString() {
        return "Dialect[" + databaseName + "]";
    }

}
