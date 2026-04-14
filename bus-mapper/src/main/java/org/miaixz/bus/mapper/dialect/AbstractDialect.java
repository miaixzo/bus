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
package org.miaixz.bus.mapper.dialect;

import org.miaixz.bus.mapper.support.paging.Pageable;

/**
 * Base implementation for database dialects.
 *
 * <p>
 * This class centralizes common behavior shared by concrete dialects, including JDBC URL prefix-based resolution, count
 * SQL generation, and standard {@code LIMIT/OFFSET}-style pagination assembly.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class AbstractDialect implements Dialect {

    /**
     * Extra buffer size reserved when building paginated SQL statements.
     */
    protected static final int PAGINATION_SQL_EXTRA_CAPACITY = 50;

    /**
     * Human-readable database name.
     */
    private final String databaseName;

    /**
     * Lower-level JDBC URL prefix used for dialect detection.
     */
    private final String jdbcUrlPrefix;

    /**
     * Creates a new dialect base instance.
     *
     * @param databaseName  the database product name exposed by this dialect
     * @param jdbcUrlPrefix the JDBC URL prefix used to identify the dialect
     */
    protected AbstractDialect(String databaseName, String jdbcUrlPrefix) {
        this.databaseName = databaseName;
        this.jdbcUrlPrefix = jdbcUrlPrefix;
    }

    /**
     * Returns the database name exposed by this dialect.
     *
     * @return the database name
     */
    @Override
    public String getDatabase() {
        return this.databaseName;
    }

    /**
     * Resolves the supplied JDBC URL by comparing it with the configured prefix.
     *
     * @param jdbcUrl the JDBC URL to evaluate
     * @return this dialect when the URL starts with the configured prefix, or {@code null} otherwise
     */
    @Override
    public Dialect resolve(String jdbcUrl) {
        if (jdbcUrl == null || jdbcUrl.isEmpty()) {
            return null;
        }
        return jdbcUrl.toLowerCase().startsWith(this.jdbcUrlPrefix.toLowerCase()) ? this : null;
    }

    /**
     * Wraps the supplied SQL as a subquery and produces a generic count statement.
     *
     * @param originalSql the original SQL statement
     * @return the generated count SQL
     */
    @Override
    public String buildCountSql(String originalSql) {
        return "SELECT COUNT(*) FROM (" + originalSql + ") AS total_count";
    }

    /**
     * Returns a concise textual representation of the dialect.
     *
     * @return the formatted dialect description
     */
    @Override
    public String toString() {
        return "Dialect[" + this.databaseName + "]";
    }

    /**
     * Builds a paginated SQL statement for dialects that use a plain {@code LIMIT/OFFSET} style syntax.
     *
     * @param originalSql the original SQL statement
     * @param pageable    the requested pagination information
     * @return the paginated SQL statement, or the original SQL when paging is disabled
     */
    protected String buildPaginatedSql(String originalSql, Pageable pageable) {
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

}
