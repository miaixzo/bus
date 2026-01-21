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
package org.miaixz.bus.mapper.dialect;

import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.support.paging.Pageable;

import java.util.List;

/**
 * Database dialect interface providing database-specific SQL generation and capabilities.
 *
 * <p>
 * This interface defines methods for:
 * </p>
 * <ul>
 * <li>Database identification and detection</li>
 * <li>Pagination SQL generation</li>
 * <li>Batch operation capabilities (Multi-Values INSERT, UPSERT, etc.)</li>
 * <li>Database-specific SQL templates</li>
 * </ul>
 *
 * <p>
 * Each database (MySQL, PostgreSQL, Oracle, etc.) should have its own implementation that provides database-specific
 * SQL syntax and capabilities.
 * </p>
 *
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * // Get dialect from registry
 * Dialect dialect = DialectRegistry.getDialect(connection);
 *
 * // Generate pagination SQL
 * Pageable pageable = Pageable.of(0, 10);
 * String pageSql = dialect.getPaginationSql("SELECT * FROM users", pageable);
 *
 * // Check capabilities
 * if (dialect.supportsMultiValuesInsert()) {
 *     // Use multi-values insert
 * }
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Dialect {

    /**
     * Gets the database product name.
     *
     * @return the database product name (e.g., "MySQL", "PostgreSQL")
     */
    String getDatabase();

    /**
     * Gets the JDBC URL prefix for this database.
     *
     * @return the JDBC URL prefix (e.g., "jdbc:mysql:", "jdbc:postgresql:")
     */
    String getJdbcPrefix();

    /**
     * Checks if this dialect supports the specified database product name.
     *
     * @param productName the database product name from DatabaseMetaData
     * @return true if this dialect supports the database
     */
    boolean supportsProduct(String productName);

    /**
     * Checks if this dialect supports the specified JDBC URL.
     *
     * @param jdbcUrl the JDBC URL
     * @return true if this dialect supports the URL
     */
    boolean supportsUrl(String jdbcUrl);

    /**
     * Generates pagination SQL for the specified query.
     *
     * @param originalSql the original SQL query
     * @param pageable    the pagination information
     * @return the paginated SQL query
     */
    String getPaginationSql(String originalSql, Pageable pageable);

    /**
     * Generates count SQL for the specified query.
     *
     * @param originalSql the original SQL query
     * @return the count SQL query
     */
    String getCountSql(String originalSql);

    /**
     * Checks if the database supports multi-values INSERT.
     *
     * <p>
     * Example: INSERT INTO table (col1, col2) VALUES (1, 2), (3, 4), (5, 6)
     * </p>
     *
     * @return true if multi-values INSERT is supported
     */
    boolean supportsMultiValuesInsert();

    /**
     * Gets the maximum number of values per multi-values INSERT.
     *
     * @return the maximum batch size, or -1 if unlimited
     */
    default int getMaxMultiValuesInsertSize() {
        return -1; // Unlimited by default
    }

    /**
     * Checks if the database supports UPSERT (INSERT ... ON DUPLICATE KEY UPDATE, etc.).
     *
     * @return true if UPSERT is supported
     */
    boolean supportsUpsert();

    /**
     * Gets the INSERT SQL template.
     *
     * <p>
     * The template may contain placeholders that will be replaced with actual values using {@code String.format()}.
     * Standard placeholders: {@code %s} for table name, columns, and values.
     * </p>
     *
     * <p>
     * Example templates:
     * </p>
     * <ul>
     * <li>Most databases: {@code "INSERT INTO %s (%s) VALUES %s"}</li>
     * <li>SQLite (optional): {@code "INSERT OR REPLACE INTO %s (%s) VALUES %s"}</li>
     * </ul>
     *
     * @return the INSERT SQL template
     */
    default String getInsertTemplate() {
        return "INSERT INTO %s (%s) VALUES %s";
    }

    /**
     * Gets the UPDATE SQL template.
     *
     * @return the UPDATE SQL template
     */
    default String getUpdateTemplate() {
        return "UPDATE %s SET %s%s";
    }

    /**
     * Gets the SELECT SQL template.
     *
     * @return the SELECT SQL template
     */
    default String getSelectTemplate() {
        return "SELECT %s FROM %s%s";
    }

    /**
     * Gets the DELETE SQL template.
     *
     * @return the DELETE SQL template
     */
    default String getDeleteTemplate() {
        return "DELETE FROM %s%s";
    }

    /**
     * Gets the COUNT SQL template.
     *
     * @return the COUNT SQL template
     */
    default String getCountTemplate() {
        return "SELECT COUNT(*) FROM %s%s";
    }

    /**
     * Gets the UPSERT SQL template.
     *
     * <p>
     * The template may contain placeholders that will be replaced with actual values.
     * </p>
     *
     * @return the UPSERT SQL template, or null if not supported
     */
    String getUpsertTemplate();

    /**
     * Builds complete dynamic UPSERT SQL with MyBatis dynamic tags for selective field insertion.
     *
     * <p>
     * This method generates the complete UPSERT SQL statement with dynamic fields (only non-null fields). The SQL
     * includes database-specific UPSERT syntax with MyBatis {@code <if>} tags for conditional field inclusion.
     * </p>
     *
     * <p>
     * Examples of what each database returns:
     * </p>
     * <ul>
     * <li>MySQL: {@code "INSERT INTO table (col1, col2) VALUES\n<foreach...>\nON DUPLICATE KEY UPDATE\n  <if test=
     * '...'>col1 = VALUES(col1),</if>\n"}</li>
     * <li>PostgreSQL/H2/CirroData:
     * {@code "INSERT INTO table (col1, col2) VALUES\n<foreach...>\nON CONFLICT (id) DO UPDATE SET\n  <if test=
     * '...'>col1 = EXCLUDED.col1,</if>\n"}</li>
     * <li>SQLite: {@code "INSERT OR REPLACE INTO table (col1, col2) VALUES\n<foreach...>"}</li>
     * </ul>
     *
     * @param tableName     Table name
     * @param columnList    Dynamic column list (with MyBatis {@code <trim>} tags)
     * @param valuesList    Dynamic values list (with MyBatis {@code <foreach>} tags)
     * @param keyColumns    Comma-separated primary key column names
     * @param updateColumns List of column metadata for updatable columns
     * @param itemPrefix    The prefix for item variable in MyBatis (e.g., "list[0]", "")
     * @return The complete dynamic UPSERT SQL statement, or null if not supported
     */
    default String buildUpsertSql(
            String tableName,
            String columnList,
            String valuesList,
            String keyColumns,
            List<ColumnMeta> updateColumns,
            String itemPrefix) {
        return null;
    }

    /**
     * Checks if the database supports limiting results without offset.
     *
     * @return true if LIMIT without OFFSET is supported
     */
    default boolean supportsLimitWithoutOffset() {
        return true;
    }

    /**
     * Gets the SQL keyword for limiting results (e.g., "LIMIT", "TOP", "FETCH FIRST").
     *
     * @return the LIMIT keyword
     */
    default String getLimitKeyword() {
        return "LIMIT";
    }

    /**
     * Gets the SQL keyword for offset (e.g., "OFFSET", "SKIP").
     *
     * @return the OFFSET keyword
     */
    default String getOffsetKeyword() {
        return "OFFSET";
    }

}
