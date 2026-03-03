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
package org.miaixz.bus.cache.support.metrics;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

/**
 * SQLite database implementation for cache hit rate statistics.
 * <p>
 * This class provides a metrics solution using a lightweight, embedded SQLite database. It is suitable for single-node
 * or testing environments. It uses a {@link SingleConnectionDataSource} and {@link JdbcTemplate} for database
 * operations and automatically creates the necessary table for storing statistics.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SqliteMetrics extends AbstractMetrics {

    /**
     * Constructs an {@code SqliteMetrics} instance using a context map for database configuration.
     *
     * @param context A map containing the database JDBC URL (e.g., "url").
     */
    public SqliteMetrics(Map<String, Object> context) {
        super(context);
    }

    /**
     * Constructs an {@code SqliteMetrics} instance with explicit database connection details.
     *
     * @param url      The JDBC URL for the SQLite database.
     * @param username The username (typically ignored by SQLite).
     * @param password The password (typically ignored by SQLite).
     */
    public SqliteMetrics(String url, String username, String password) {
        super(url, username, password);
    }

    /**
     * Provides a {@link Supplier} for {@link JdbcOperations} configured for an SQLite database.
     * <p>
     * This method sets up a {@link SingleConnectionDataSource} and a {@link JdbcTemplate}. It then ensures that the
     * {@code t_cache_rate} table exists, creating it if necessary. Note: The DDL uses `IDENTITY`, which may not be
     * standard for all SQLite versions; `INTEGER PRIMARY KEY AUTOINCREMENT` is more common.
     * </p>
     *
     * @param context A map containing the database JDBC URL under the "url" key.
     * @return A supplier that provides an initialized {@link JdbcOperations} object.
     */
    @Override
    protected Supplier<JdbcOperations> jdbcOperationsSupplier(Map<String, Object> context) {
        return () -> {
            SingleConnectionDataSource dataSource = new SingleConnectionDataSource();
            dataSource.setDriverClassName("org.sqlite.JDBC");
            dataSource.setUrl((String) context.get("url"));
            JdbcTemplate template = new JdbcTemplate(dataSource);
            template.execute(
                    "CREATE TABLE IF NOT EXISTS t_cache_rate(" + "id INTEGER     PRIMARY KEY AUTOINCREMENT,"
                            + "pattern       VARCHAR(64) NOT NULL UNIQUE,"
                            + "hit_count     BIGINT      NOT NULL     DEFAULT 0,"
                            + "require_count BIGINT      NOT NULL     DEFAULT 0,"
                            + "version       BIGINT      NOT NULL     DEFAULT 0)");
            return template;
        };
    }

    /**
     * Transforms a list of database query results (maps) into a stream of {@link DataDO} objects.
     * <p>
     * Note: This implementation casts the numeric types from the result map to {@link Integer}, which is a common
     * behavior for the SQLite JDBC driver when dealing with BIGINT columns.
     * </p>
     *
     * @param mapResults A list of maps, where each map represents a row from the database query.
     * @return A {@link Stream} of {@link DataDO} objects, populated from the query results.
     */
    @Override
    protected Stream<DataDO> transferResults(List<Map<String, Object>> mapResults) {
        return mapResults.stream().map(result -> {
            DataDO dataDO = new DataDO();
            dataDO.setHitCount(((Number) result.get("hit_count")).longValue());
            dataDO.setPattern((String) result.get("pattern"));
            dataDO.setRequireCount(((Number) result.get("require_count")).longValue());
            dataDO.setVersion(((Number) result.get("version")).longValue());
            return dataDO;
        });
    }

}
