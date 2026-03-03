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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * MySQL database implementation for cache hit rate statistics.
 * <p>
 * This class provides a metrics solution using a MySQL database. It utilizes the HikariCP connection pool and
 * {@link JdbcTemplate} for efficient database operations. It automatically creates the necessary table for storing
 * statistics and supports concurrent updates via an optimistic locking mechanism.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MySQLMetrics extends AbstractMetrics {

    /**
     * Constructs a {@code MySQLMetrics} instance using a context map for database configuration.
     *
     * @param context A map containing database connection parameters for HikariCP.
     */
    public MySQLMetrics(Map<String, Object> context) {
        super(context);
    }

    /**
     * Constructs a {@code MySQLMetrics} instance with explicit database connection details.
     *
     * @param url      The JDBC URL for the MySQL database.
     * @param username The username for database access.
     * @param password The password for database access.
     */
    public MySQLMetrics(String url, String username, String password) {
        super(url, username, password);
    }

    /**
     * Provides a {@link Supplier} for {@link JdbcOperations} configured for a MySQL database.
     * <p>
     * This method sets up a {@link HikariDataSource} and a {@link JdbcTemplate}. It then ensures that the
     * {@code t_cache_rate} table exists, creating it if necessary. The table is designed for storing hit/request counts
     * and includes a version column for optimistic locking.
     * </p>
     *
     * @param context A map containing configuration properties for HikariCP.
     * @return A supplier that provides an initialized {@link JdbcOperations} object.
     * @throws InternalException if the data source cannot be initialized.
     */
    @Override
    protected Supplier<JdbcOperations> jdbcOperationsSupplier(Map<String, Object> context) {
        return () -> {
            try {
                Properties properties = new Properties();
                for (String key : context.keySet()) {
                    properties.setProperty(key, context.get(key).toString());
                }
                HikariDataSource dataSource = new HikariDataSource(new HikariConfig(properties));
                JdbcTemplate template = new JdbcTemplate(dataSource);
                template.execute(
                        "CREATE TABLE IF NOT EXISTS t_cache_rate(" + "id BIGINT     PRIMARY KEY AUTO_INCREMENT,"
                                + "pattern       VARCHAR(64) NOT NULL UNIQUE,"
                                + "hit_count     BIGINT      NOT NULL     DEFAULT 0,"
                                + "require_count BIGINT      NOT NULL     DEFAULT 0,"
                                + "version       BIGINT      NOT NULL     DEFAULT 0)");
                return template;
            } catch (Exception e) {
                throw new InternalException(e);
            }
        };
    }

    /**
     * Transforms a list of database query results (maps) into a stream of {@link DataDO} objects.
     *
     * @param mapResults A list of maps, where each map represents a row from the database query.
     * @return A {@link Stream} of {@link DataDO} objects, populated from the query results.
     */
    @Override
    protected Stream<DataDO> transferResults(List<Map<String, Object>> mapResults) {
        return mapResults.stream().map(result -> {
            DataDO dataDO = new DataDO();
            dataDO.setRequireCount((Long) result.get("require_count"));
            dataDO.setHitCount((Long) result.get("hit_count"));
            dataDO.setPattern((String) result.get("pattern"));
            dataDO.setVersion((Long) result.get("version"));
            return dataDO;
        });
    }

}
