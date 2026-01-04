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
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.miaixz.bus.cache.Metrics;
import org.miaixz.bus.cache.magic.CachePair;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.setting.Builder;
import org.springframework.jdbc.core.JdbcOperations;

/**
 * An abstract base class for database-backed cache metrics implementations.
 * <p>
 * This class provides a framework for storing cache hit rate statistics in a relational database. It uses a
 * non-blocking queue to asynchronously write metrics to the database, minimizing performance impact on the application
 * threads. It also employs an optimistic locking strategy to handle concurrent updates and ensure data consistency.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractMetrics implements Metrics {

    /**
     * A single-threaded executor for asynchronously writing metrics to the database.
     */
    private static final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setName("cache:db-writer");
        thread.setDaemon(true);
        return thread;
    });

    /**
     * A lock to ensure consistency during the initial insertion of a new pattern record.
     */
    private static final Lock lock = new ReentrantLock();

    /**
     * A flag to indicate whether the metrics service has been shut down.
     */
    private volatile boolean isShutdown = false;

    /**
     * A queue for pending hit count increments.
     */
    private final BlockingQueue<CachePair<String, Integer>> hitQueue = new LinkedTransferQueue<>();

    /**
     * A queue for pending request count increments.
     */
    private final BlockingQueue<CachePair<String, Integer>> requireQueue = new LinkedTransferQueue<>();

    /**
     * The Spring JDBC template for database operations.
     */
    private final JdbcOperations jdbcOperations;

    /**
     * A collection of SQL statements loaded from a configuration file.
     */
    private final Properties sqls;

    /**
     * Initializes the metrics service with database connection details.
     *
     * @param context A map containing database connection parameters.
     */
    protected AbstractMetrics(Map<String, Object> context) {
        InputStream in = this.getClass().getClassLoader()
                .getResourceAsStream(Normal.META_INF + "/cache/bus.cache.yaml");
        this.sqls = Builder.loadYaml(in, Properties.class);
        this.jdbcOperations = jdbcOperationsSupplier(context).get();
        executor.submit(() -> {
            while (!isShutdown) {
                dumpToDB(hitQueue, "hit_count");
                dumpToDB(requireQueue, "require_count");
            }
        });
    }

    /**
     * A convenience constructor with explicit database connection details.
     *
     * @param url      The database JDBC URL.
     * @param username The database username.
     * @param password The database password.
     */
    public AbstractMetrics(String url, String username, String password) {
        this(newHashMap("url", url, "username", username, "password", password));
    }

    /**
     * A utility method to create a HashMap from a series of key-value pairs.
     *
     * @param keyValues An array of alternating keys and values.
     * @return A new {@link HashMap} instance.
     */
    public static Map<String, Object> newHashMap(Object... keyValues) {
        Map<String, Object> map = new HashMap<>(keyValues.length / 2);
        for (int i = 0; i < keyValues.length; i += 2) {
            String key = (String) keyValues[i];
            Object value = keyValues[i + 1];
            map.put(key, value);
        }
        return map;
    }

    /**
     * Creates a {@link JdbcOperations} instance and initializes the database.
     * <p>
     * Subclasses must implement this method to provide a configured {@code JdbcOperations} instance and to perform any
     * necessary database initialization (e.g., creating tables).
     * </p>
     *
     * @param context A map of configuration parameters.
     * @return A supplier that provides the initialized {@link JdbcOperations} object.
     */
    protected abstract Supplier<JdbcOperations> jdbcOperationsSupplier(Map<String, Object> context);

    /**
     * Transforms a list of database query results into a stream of {@link DataDO} objects.
     *
     * @param map A list of maps, where each map represents a row from the database.
     * @return A stream of {@link DataDO} objects.
     */
    protected abstract Stream<DataDO> transferResults(List<Map<String, Object>> map);

    /**
     * Drains the metrics queue and writes the aggregated counts to the database.
     *
     * @param queue  The queue to drain (either hit or request queue).
     * @param column The name of the database column to update (e.g., "hit_count").
     */
    private void dumpToDB(BlockingQueue<CachePair<String, Integer>> queue, String column) {
        long times = 0;
        CachePair<String, Integer> head;
        // Collect up to 100 items from the queue into an aggregated map.
        Map<String, AtomicLong> holdMap = new HashMap<>();
        while (null != (head = queue.poll()) && times <= 100) {
            holdMap.computeIfAbsent(head.getLeft(), (key) -> new AtomicLong(0L)).addAndGet(head.getRight());
            ++times;
        }
        // Write the aggregated counts to the database.
        holdMap.forEach((pattern, count) -> countAddCas(column, pattern, count.get()));
    }

    /**
     * Adds a hit count to the queue for a specific pattern.
     *
     * @param pattern The cache pattern name.
     * @param count   The number of hits to add.
     */
    @Override
    public void hitIncr(String pattern, int count) {
        if (count != 0) {
            hitQueue.add(CachePair.of(pattern, count));
        }
    }

    /**
     * Adds a request count to the queue for a specific pattern.
     *
     * @param pattern The cache pattern name.
     * @param count   The number of requests to add.
     */
    @Override
    public void reqIncr(String pattern, int count) {
        if (count != 0) {
            requireQueue.add(CachePair.of(pattern, count));
        }
    }

    /**
     * Retrieves the current cache hit rate statistics from the database.
     *
     * @return A map where keys are pattern names and values are {@link Snapshot} objects.
     */
    @Override
    public Map<String, Snapshot> getHitting() {
        List<DataDO> dataDOS = queryAll();
        AtomicLong statisticsHit = new AtomicLong(0);
        AtomicLong statisticsRequired = new AtomicLong(0);

        // Collect hit rates for each pattern.
        Map<String, Snapshot> result = dataDOS.stream().collect(Collectors.toMap(DataDO::getPattern, (dataDO) -> {
            statisticsHit.addAndGet(dataDO.hitCount);
            statisticsRequired.addAndGet(dataDO.requireCount);
            return Snapshot.newInstance(dataDO.hitCount, dataDO.requireCount);
        }, Snapshot::mergeShootingDO, LinkedHashMap::new));

        // Add a summary for all patterns.
        result.put(summaryName(), Snapshot.newInstance(statisticsHit.get(), statisticsRequired.get()));
        return result;
    }

    /**
     * Resets the statistics for a specific cache pattern by deleting its record from the database.
     *
     * @param pattern The cache pattern name.
     */
    @Override
    public void reset(String pattern) {
        jdbcOperations.update(sqls.getProperty("delete"), pattern);
    }

    /**
     * Resets all statistics by truncating the database table.
     */
    @Override
    public void resetAll() {
        jdbcOperations.update(sqls.getProperty("truncate"));
    }

    /**
     * Atomically increments a counter in the database using a Compare-And-Swap (CAS) approach.
     *
     * @param column  The name of the column to update.
     * @param pattern The cache pattern name.
     * @param count   The value to add to the counter.
     */
    private void countAddCas(String column, String pattern, long count) {
        Optional<DataDO> dataOptional = queryObject(pattern);
        if (dataOptional.isPresent()) {
            // If the record exists, attempt to update it in a loop until the CAS operation succeeds.
            DataDO dataDO = dataOptional.get();
            while (update(column, pattern, getObjectCount(dataDO, column, count), dataDO.version) <= 0) {
                dataDO = queryObject(pattern).get(); // Re-fetch the latest version
            }
        } else {
            // If the record does not exist, insert it.
            lock.lock();
            try {
                // Double-check to prevent race conditions.
                dataOptional = queryObject(pattern);
                if (dataOptional.isPresent()) {
                    update(column, pattern, count, dataOptional.get().version);
                } else {
                    insert(column, pattern, count);
                }
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * Queries the database for a single statistics record.
     *
     * @param pattern The cache pattern name.
     * @return An {@link Optional} containing the {@link DataDO} if found.
     */
    private Optional<DataDO> queryObject(String pattern) {
        String selectSql = sqls.getProperty("select");
        List<Map<String, Object>> mapResults = jdbcOperations.queryForList(selectSql, pattern);
        return transferResults(mapResults).findFirst();
    }

    /**
     * Queries the database for all statistics records.
     *
     * @return A list of all {@link DataDO} records.
     */
    private List<DataDO> queryAll() {
        String selectAllQuery = sqls.getProperty("select_all");
        List<Map<String, Object>> mapResults = jdbcOperations.queryForList(selectAllQuery);
        return transferResults(mapResults).collect(Collectors.toList());
    }

    /**
     * Inserts a new statistics record into the database.
     *
     * @param column  The name of the column to set the initial count for.
     * @param pattern The cache pattern name.
     * @param count   The initial count.
     * @return The number of affected rows.
     */
    private int insert(String column, String pattern, long count) {
        String insertSql = String.format(sqls.getProperty("insert"), column);
        return jdbcOperations.update(insertSql, pattern, count);
    }

    /**
     * Updates an existing statistics record in the database.
     *
     * @param column  The name of the column to update.
     * @param pattern The cache pattern name.
     * @param count   The new total count.
     * @param version The expected version for optimistic locking.
     * @return The number of affected rows.
     */
    private int update(String column, String pattern, long count, long version) {
        String updateSql = String.format(sqls.getProperty("update"), column);
        return jdbcOperations.update(updateSql, count, pattern, version);
    }

    /**
     * Calculates the new count for a specific column.
     *
     * @param data        The current data object.
     * @param column      The name of the column.
     * @param countOffset The value to add.
     * @return The new total count.
     */
    private long getObjectCount(DataDO data, String column, long countOffset) {
        long lastCount = "hit_count".equals(column) ? data.hitCount : data.requireCount;
        return lastCount + countOffset;
    }

    /**
     * A lifecycle method to gracefully shut down the metrics service.
     * <p>
     * This method waits for the processing queues to be empty before stopping the background thread.
     * </p>
     */
    @PreDestroy
    public void tearDown() {
        while (hitQueue.size() > 0 || requireQueue.size() > 0) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
        isShutdown = true;
    }

    /**
     * An internal Data Transfer Object (DTO) for storing cache statistics.
     */
    protected static final class DataDO {

        /**
         * The cache pattern name (e.g., cache name).
         */
        private String pattern;

        /**
         * The number of cache hits.
         */
        private long hitCount;

        /**
         * The number of cache requests.
         */
        private long requireCount;

        /**
         * The version number for optimistic locking.
         */
        private long version;

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }

        public void setHitCount(long hitCount) {
            this.hitCount = hitCount;
        }

        public void setRequireCount(long requireCount) {
            this.requireCount = requireCount;
        }

        public void setVersion(long version) {
            this.version = version;
        }
    }

}
