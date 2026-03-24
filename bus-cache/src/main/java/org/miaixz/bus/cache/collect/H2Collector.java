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

import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * H2 database implementation for cache hit rate statistics.
 * <p>
 * Uses an embedded H2 database to persist cache statistics. A single persistent connection is held via
 * {@link JdbcRunner#forSingleConnection} to prevent the in-memory database from being destroyed between statement
 * executions. The {@code t_cache_rate} table is created automatically on startup if it does not already exist, and
 * concurrent updates are handled via an optimistic locking mechanism.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class H2Collector extends AbstractCollector {

    /**
     * Constructs an {@code H2Collector} instance using a context map for database configuration.
     *
     * @param context A map containing database connection parameters (e.g., "url", "username", "password").
     */
    public H2Collector(Map<String, Object> context) {
        super(context);
    }

    /**
     * Constructs an {@code H2Collector} instance with explicit database connection details.
     *
     * @param url      The JDBC URL for the H2 database.
     * @param username The username for database access.
     * @param password The password for database access.
     */
    public H2Collector(String url, String username, String password) {
        super(url, username, password);
    }

    /**
     * Provides a {@link Supplier} for a {@link JdbcRunner} configured for an H2 database.
     * <p>
     * Uses {@link JdbcRunner#forSingleConnection} to open a persistent connection so that the in-memory H2 database is
     * not destroyed when individual statements finish. The {@code t_cache_rate} table is created on startup if it does
     * not already exist.
     * </p>
     *
     * @param context A map containing database connection parameters.
     * @return A supplier that provides an initialized {@link JdbcRunner} object.
     */
    @Override
    protected Supplier<JdbcRunner> jdbcRunnerSupplier(Map<String, Object> context) {
        return () -> {
            JdbcRunner runner = JdbcRunner.forSingleConnection(
                    "org.h2.Driver",
                    StringKit.toString(context.get("url")),
                    StringKit.toString(context.get("username")),
                    StringKit.toString(context.get("password")));
            runner.execute(
                    "CREATE TABLE IF NOT EXISTS t_cache_rate(" + "id BIGINT     IDENTITY PRIMARY KEY,"
                            + "pattern       VARCHAR(64) NOT NULL UNIQUE,"
                            + "hit_count     BIGINT      NOT NULL     DEFAULT 0,"
                            + "require_count BIGINT      NOT NULL     DEFAULT 0,"
                            + "version       BIGINT      NOT NULL     DEFAULT 0)");
            return runner;
        };
    }

    /**
     * Transforms a list of database query results (maps) into a stream of {@link Tally} objects.
     *
     * @param mapResults A list of maps, where each map represents a row from the database query.
     * @return A {@link Stream} of {@link Tally} objects, populated from the query results.
     */
    @Override
    protected Stream<Tally> transferResults(List<Map<String, Object>> mapResults) {
        return mapResults.stream().map((map) -> {
            Tally tally = new Tally();
            tally.setPattern((String) map.get("PATTERN"));
            tally.setHitCount((long) map.get("HIT_COUNT"));
            tally.setRequireCount((long) map.get("REQUIRE_COUNT"));
            tally.setVersion((long) map.get("VERSION"));
            return tally;
        });
    }

    /**
     * Lifecycle callback invoked before this bean is destroyed.
     * <p>
     * Delegates to {@link AbstractCollector#close()} to allow the background write thread to drain its queue before the
     * JVM shuts down.
     * </p>
     */
    @Override
    @PreDestroy
    public void close() {
        super.close();
    }

}
