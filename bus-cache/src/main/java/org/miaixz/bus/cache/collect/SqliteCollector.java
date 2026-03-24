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
package org.miaixz.bus.cache.collect;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * SQLite database implementation for cache hit rate statistics.
 * <p>
 * Suitable for single-node or testing environments. A single persistent connection is held via
 * {@link JdbcRunner#forSingleConnection} to prevent the connection from being closed between statement executions. The
 * {@code t_cache_rate} table is created automatically on startup if it does not already exist.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SqliteCollector extends AbstractCollector {

    /**
     * Constructs an {@code SqliteCollector} instance using a context map for database configuration.
     *
     * @param context A map containing the database JDBC URL (e.g., "url").
     */
    public SqliteCollector(Map<String, Object> context) {
        super(context);
    }

    /**
     * Constructs an {@code SqliteCollector} instance with explicit database connection details.
     *
     * @param url      The JDBC URL for the SQLite database.
     * @param username The username (typically ignored by SQLite).
     * @param password The password (typically ignored by SQLite).
     */
    public SqliteCollector(String url, String username, String password) {
        super(url, username, password);
    }

    /**
     * Provides a {@link Supplier} for a {@link JdbcRunner} configured for an SQLite database.
     * <p>
     * Uses {@link JdbcRunner#forSingleConnection} so the file-based or in-memory SQLite connection is not closed
     * between statement executions.
     * </p>
     *
     * @param context A map containing the database JDBC URL under the "url" key.
     * @return A supplier that provides an initialized {@link JdbcRunner} object.
     */
    @Override
    protected Supplier<JdbcRunner> jdbcRunnerSupplier(Map<String, Object> context) {
        return () -> {
            JdbcRunner runner = JdbcRunner
                    .forSingleConnection("org.sqlite.JDBC", (String) context.get("url"), null, null);
            runner.execute(
                    "CREATE TABLE IF NOT EXISTS t_cache_rate(" + "id INTEGER     PRIMARY KEY AUTOINCREMENT,"
                            + "pattern       VARCHAR(64) NOT NULL UNIQUE,"
                            + "hit_count     BIGINT      NOT NULL     DEFAULT 0,"
                            + "require_count BIGINT      NOT NULL     DEFAULT 0,"
                            + "version       BIGINT      NOT NULL     DEFAULT 0)");
            return runner;
        };
    }

    /**
     * Transforms a list of database query results (maps) into a stream of {@link Tally} objects.
     * <p>
     * Note: This implementation casts the numeric types from the result map to {@link Integer}, which is a common
     * behavior for the SQLite JDBC driver when dealing with BIGINT columns.
     * </p>
     *
     * @param mapResults A list of maps, where each map represents a row from the database query.
     * @return A {@link Stream} of {@link Tally} objects, populated from the query results.
     */
    @Override
    protected Stream<Tally> transferResults(List<Map<String, Object>> mapResults) {
        return mapResults.stream().map(result -> {
            Tally tally = new Tally();
            tally.setHitCount(((Number) result.get("hit_count")).longValue());
            tally.setPattern((String) result.get("pattern"));
            tally.setRequireCount(((Number) result.get("require_count")).longValue());
            tally.setVersion(((Number) result.get("version")).longValue());
            return tally;
        });
    }

}
