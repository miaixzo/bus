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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * PostgreSQL database implementation for cache hit rate statistics.
 * <p>
 * Uses the HikariCP connection pool together with {@link JdbcRunner} for efficient database operations. The
 * {@code t_cache_rate} table is created automatically on startup if it does not already exist (primary key uses
 * {@code BIGSERIAL} for auto-increment), and concurrent updates are handled via an optimistic locking mechanism.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PostgreSQLCollector extends AbstractCollector {

    /**
     * Constructs a {@code PostgreSQLCollector} instance using a context map for database configuration.
     *
     * @param context A map containing database connection parameters for HikariCP.
     */
    public PostgreSQLCollector(Map<String, Object> context) {
        super(context);
    }

    /**
     * Constructs a {@code PostgreSQLCollector} instance with explicit database connection details.
     *
     * @param url      The JDBC URL for the PostgreSQL database.
     * @param username The username for database access.
     * @param password The password for database access.
     */
    public PostgreSQLCollector(String url, String username, String password) {
        super(url, username, password);
    }

    /**
     * Provides a {@link Supplier} for a {@link JdbcRunner} configured for a PostgreSQL database.
     * <p>
     * Sets up a {@link HikariDataSource} and ensures the {@code t_cache_rate} table exists. The table uses a
     * {@code BIGSERIAL} primary key for auto-incrementing IDs.
     * </p>
     *
     * @param context A map containing configuration properties for HikariCP.
     * @return A supplier that provides an initialized {@link JdbcRunner} object.
     * @throws InternalException if the data source cannot be initialized.
     */
    @Override
    protected Supplier<JdbcRunner> jdbcRunnerSupplier(Map<String, Object> context) {
        return () -> {
            try {
                Properties properties = new Properties();
                for (String key : context.keySet()) {
                    properties.setProperty(key, context.get(key).toString());
                }
                HikariDataSource dataSource = new HikariDataSource(new HikariConfig(properties));
                JdbcRunner runner = JdbcRunner.forDataSource(dataSource);
                runner.execute(
                        "CREATE TABLE IF NOT EXISTS t_cache_rate(" + "id            BIGSERIAL   PRIMARY KEY,"
                                + "pattern       VARCHAR(64) NOT NULL UNIQUE,"
                                + "hit_count     BIGINT      NOT NULL     DEFAULT 0,"
                                + "require_count BIGINT      NOT NULL     DEFAULT 0,"
                                + "version       BIGINT      NOT NULL     DEFAULT 0)");
                return runner;
            } catch (Exception e) {
                throw new InternalException(e);
            }
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
        return mapResults.stream().map(result -> {
            Tally tally = new Tally();
            tally.setRequireCount((Long) result.get("require_count"));
            tally.setHitCount((Long) result.get("hit_count"));
            tally.setPattern((String) result.get("pattern"));
            tally.setVersion((Long) result.get("version"));
            return tally;
        });
    }

}
