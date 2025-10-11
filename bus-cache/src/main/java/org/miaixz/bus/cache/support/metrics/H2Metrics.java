/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.cache.support.metrics;

import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.miaixz.bus.core.xyz.StringKit;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

/**
 * H2 database implementation for cache hit rate statistics.
 * <p>
 * This class provides a metrics solution using an in-memory H2 database. It leverages
 * {@link SingleConnectionDataSource} and {@link JdbcTemplate} for database operations. It automatically creates a table
 * to store cache statistics and supports concurrent updates with an optimistic locking mechanism (though not explicitly
 * shown in this snippet, it's implied by the table schema).
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class H2Metrics extends AbstractMetrics {

    /**
     * Constructs an {@code H2Metrics} instance using a context map for database configuration.
     *
     * @param context A map containing database connection parameters (e.g., "url", "username", "password").
     */
    public H2Metrics(Map<String, Object> context) {
        super(context);
    }

    /**
     * Constructs an {@code H2Metrics} instance with explicit database connection details.
     *
     * @param url      The JDBC URL for the H2 database.
     * @param username The username for database access.
     * @param password The password for database access.
     */
    public H2Metrics(String url, String username, String password) {
        super(url, username, password);
    }

    /**
     * Provides a {@link Supplier} for {@link JdbcOperations} configured for an H2 database.
     * <p>
     * This method sets up a {@link SingleConnectionDataSource} and a {@link JdbcTemplate}, then creates the necessary
     * {@code t_cache_rate} table if it doesn't already exist. The table schema includes columns for pattern, hit count,
     * require count, and a version for optimistic locking.
     * </p>
     *
     * @param context A map containing database connection parameters.
     * @return A supplier that provides an initialized {@link JdbcOperations} object.
     */
    @Override
    protected Supplier<JdbcOperations> jdbcOperationsSupplier(Map<String, Object> context) {
        return () -> {
            SingleConnectionDataSource dataSource = new SingleConnectionDataSource();
            dataSource.setDriverClassName("org.h2.Driver");
            dataSource.setUrl(StringKit.toString(context.get("url")));
            dataSource.setUsername(StringKit.toString(context.get("username")));
            dataSource.setPassword(StringKit.toString(context.get("password")));
            JdbcTemplate template = new JdbcTemplate(dataSource);
            template.execute(
                    "CREATE TABLE IF NOT EXISTS t_cache_rate(" + "id BIGINT     IDENTITY PRIMARY KEY,"
                            + "pattern       VARCHAR(64) NOT NULL UNIQUE,"
                            + "hit_count     BIGINT      NOT NULL     DEFAULT 0,"
                            + "require_count BIGINT      NOT NULL     DEFAULT 0,"
                            + "version       BIGINT      NOT NULL     DEFAULT 0)");
            return template;
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
        return mapResults.stream().map((map) -> {
            AbstractMetrics.DataDO dataDO = new AbstractMetrics.DataDO();
            dataDO.setPattern((String) map.get("PATTERN"));
            dataDO.setHitCount((long) map.get("HIT_COUNT"));
            dataDO.setRequireCount((long) map.get("REQUIRE_COUNT"));
            dataDO.setVersion((long) map.get("VERSION"));
            return dataDO;
        });
    }

    /**
     * A lifecycle method to clean up resources.
     * <p>
     * Annotated with {@link PreDestroy}, this method is typically invoked by a dependency injection container when the
     * bean is being destroyed. It delegates to the superclass's {@code tearDown()} method to close the database
     * connection.
     * </p>
     */
    @PreDestroy
    public void tearDown() {
        super.tearDown();
    }

}
