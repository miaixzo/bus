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
 * Database dialect interface providing database-specific pagination and UPSERT capabilities.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Dialect {

    /**
     * Gets the database product name.
     *
     * @return the database product name
     */
    String getDatabase();

    /**
     * Resolves the supplied JDBC URL to a final dialect instance.
     *
     * <p>
     * Implementations should return {@code null} when the URL does not belong to the dialect. Stateless dialects
     * usually return {@code this} when the URL matches. Product-family dialects such as Polardb may inspect the URL
     * further and return a new resolved dialect instance that carries the concrete engine state needed for downstream
     * SQL generation.
     * </p>
     *
     * @param jdbcUrl the JDBC URL to resolve
     * @return the resolved dialect instance, or {@code null} if the URL does not belong to this dialect
     */
    Dialect resolve(String jdbcUrl);

    /**
     * Returns the UPSERT type used by this dialect.
     *
     * <p>
     * The returned value identifies which SQL shape the provider should build for UPSERT operations on the current
     * database family.
     * </p>
     *
     * @return the UPSERT type for the dialect
     */
    Type getUpsertType();

    /**
     * Gets the SQL keyword for limiting results.
     *
     * @return the LIMIT keyword
     */
    default String getLimitKeyword() {
        return "LIMIT";
    }

    /**
     * Gets the SQL keyword for offset.
     *
     * @return the OFFSET keyword
     */
    default String getOffsetKeyword() {
        return "OFFSET";
    }

    /**
     * Builds count SQL for the specified query.
     *
     * @param originalSql the original SQL query
     * @return the count SQL query
     */
    String buildCountSql(String originalSql);

    /**
     * Builds pagination SQL for the specified query.
     *
     * @param originalSql the original SQL query
     * @param pageable    the pagination information
     * @return the paginated SQL query
     */
    String buildPaginationSql(String originalSql, Pageable pageable);

    /**
     * Enumerates the UPSERT types recognized by the dialect layer.
     *
     * <p>
     * Each constant represents a stable SQL shape that providers can switch on when building UPSERT statements for a
     * specific database family.
     * </p>
     */
    enum Type {

        /**
         * UPSERT is not supported.
         */
        NONE,

        /**
         * MySQL style: INSERT ... ON DUPLICATE KEY UPDATE.
         */
        INSERT_ON_DUPLICATE,

        /**
         * PostgreSQL style: INSERT ... ON CONFLICT (...) DO UPDATE.
         */
        INSERT_ON_CONFLICT,

        /**
         * SQLite style: INSERT OR REPLACE.
         */
        INSERT_OR_REPLACE,

        /**
         * Firebird style: UPDATE OR INSERT ... MATCHING (...).
         */
        UPDATE_OR_INSERT,

        /**
         * MERGE using VALUES source rows.
         */
        MERGE_USING_VALUES,

        /**
         * MERGE using DUAL / source select.
         */
        MERGE_USING_DUAL

    }

}
