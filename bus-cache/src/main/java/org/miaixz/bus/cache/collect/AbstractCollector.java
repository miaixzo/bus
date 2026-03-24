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
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.miaixz.bus.cache.Collector;
import org.miaixz.bus.cache.magic.CachePair;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.setting.Builder;

/**
 * An abstract base class for database-backed cache statistics implementations.
 * <p>
 * This class provides a framework for storing cache hit rate statistics in a relational database. It uses a
 * non-blocking queue to asynchronously write statistics to the database, minimizing performance impact on the
 * application threads. It also employs an optimistic locking strategy to handle concurrent updates and ensure data
 * consistency.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractCollector implements Collector, AutoCloseable {

    /**
     * A single-threaded executor for asynchronously writing statistics to the database.
     * <p>
     * Instance-level so that each collector subclass owns its own writer thread, avoiding cross-instance interference
     * and ensuring correct lifecycle management via {@link #close()}.
     * </p>
     */
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setName("cache:db-writer-" + System.identityHashCode(this));
        thread.setDaemon(true);
        return thread;
    });

    /**
     * A lock to ensure consistency during the initial insertion of a new pattern record.
     * <p>
     * Instance-level to avoid cross-instance contention when multiple collector implementations are used
     * simultaneously.
     * </p>
     */
    private final Lock lock = new ReentrantLock();

    /**
     * A flag to indicate whether the collector service has been shut down.
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
     * The plain-JDBC runner for database operations.
     */
    private final JdbcRunner jdbcRunner;

    /**
     * A collection of SQL statements loaded from a configuration file.
     */
    private final Properties sqls;

    /**
     * Initializes the collector with database connection details.
     *
     * @param context A map containing database connection parameters.
     */
    protected AbstractCollector(Map<String, Object> context) {
        InputStream in = this.getClass().getClassLoader()
                .getResourceAsStream(Normal.META_INF + "/cache/bus.cache.yaml");
        this.sqls = Builder.loadYaml(in, Properties.class);
        this.jdbcRunner = jdbcRunnerSupplier(context).get();
        executor.submit(() -> {
            while (!isShutdown) {
                if (hitQueue.isEmpty() && requireQueue.isEmpty()) {
                    // Both queues are empty; pause briefly to avoid busy-spinning.
                    try {
                        TimeUnit.MILLISECONDS.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    dumpToDB(hitQueue, "hit_count");
                    dumpToDB(requireQueue, "require_count");
                }
            }
            // Final drain after shutdown signal: loop until both queues are fully empty.
            while (!hitQueue.isEmpty()) {
                dumpToDB(hitQueue, "hit_count");
            }
            while (!requireQueue.isEmpty()) {
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
    public AbstractCollector(String url, String username, String password) {
        this(newHashMap("url", url, "username", username, "password", password));
    }

    /**
     * A utility method to create a HashMap from a series of key-value pairs.
     *
     * @param keyValues An array of alternating keys and values.
     * @return A new {@link HashMap} instance.
     */
    private static Map<String, Object> newHashMap(Object... keyValues) {
        Map<String, Object> map = new HashMap<>(keyValues.length / 2);
        for (int i = 0; i < keyValues.length; i += 2) {
            String key = (String) keyValues[i];
            Object value = keyValues[i + 1];
            map.put(key, value);
        }
        return map;
    }

    /**
     * Creates a {@link JdbcRunner} instance and initializes the database.
     * <p>
     * Subclasses must implement this method to provide a configured {@code JdbcRunner} and to perform any necessary
     * database initialization (e.g., creating tables).
     * </p>
     *
     * @param context A map of configuration parameters.
     * @return A supplier that provides the initialized {@link JdbcRunner} object.
     */
    protected abstract Supplier<JdbcRunner> jdbcRunnerSupplier(Map<String, Object> context);

    /**
     * Transforms a list of database query results into a stream of {@link Tally} objects.
     *
     * @param map A list of maps, where each map represents a row from the database.
     * @return A stream of {@link Tally} objects.
     */
    protected abstract Stream<Tally> transferResults(List<Map<String, Object>> map);

    /**
     * Drains the statistics queue and writes the aggregated counts to the database.
     *
     * @param queue  The queue to drain (either hit or request queue).
     * @param column The name of the database column to update (e.g., "hit_count").
     */
    private void dumpToDB(BlockingQueue<CachePair<String, Integer>> queue, String column) {
        long times = 0;
        CachePair<String, Integer> head;
        // Collect up to 100 items from the queue into an aggregated map.
        Map<String, AtomicLong> holdMap = new HashMap<>();
        while (null != (head = queue.poll()) && times < 100) {
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
        List<Tally> tally = queryAll();
        AtomicLong statisticsHit = new AtomicLong(0);
        AtomicLong statisticsRequired = new AtomicLong(0);

        // Collect hit rates for each pattern.
        Map<String, Snapshot> result = tally.stream().collect(Collectors.toMap(Tally::getPattern, (dataDO) -> {
            statisticsHit.addAndGet(dataDO.getHitCount());
            statisticsRequired.addAndGet(dataDO.getRequireCount());
            return Snapshot.newInstance(dataDO.getHitCount(), dataDO.getRequireCount());
        }, Snapshot::merge, LinkedHashMap::new));

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
        jdbcRunner.update(sqls.getProperty("delete"), pattern);
    }

    /**
     * Resets all statistics by truncating the database table.
     */
    @Override
    public void resetAll() {
        jdbcRunner.update(sqls.getProperty("truncate"));
    }

    /**
     * Atomically increments a counter in the database using a Compare-And-Swap (CAS) approach.
     *
     * @param column  The name of the column to update.
     * @param pattern The cache pattern name.
     * @param count   The value to add to the counter.
     */
    private void countAddCas(String column, String pattern, long count) {
        Optional<Tally> dataOptional = queryObject(pattern);
        if (dataOptional.isPresent()) {
            // If the record exists, attempt to update it in a loop until the CAS operation succeeds.
            Tally tally = dataOptional.get();
            int retries = 0;
            while (update(column, pattern, getObjectCount(tally, column, count), tally.getVersion()) <= 0) {
                if (++retries > 10) {
                    Logger.warn("CAS update gave up after 10 retries, pattern: {}, column: {}", pattern, column);
                    break;
                }
                // Re-fetch the latest version; abort if the record disappears concurrently.
                tally = queryObject(pattern).orElse(null);
                if (tally == null) {
                    break;
                }
            }
        } else {
            // If the record does not exist, insert it.
            lock.lock();
            try {
                // Double-check to prevent race conditions.
                dataOptional = queryObject(pattern);
                if (dataOptional.isPresent()) {
                    update(column, pattern, count, dataOptional.get().getVersion());
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
     * @return An {@link Optional} containing the {@link Tally} if found.
     */
    private Optional<Tally> queryObject(String pattern) {
        String selectSql = sqls.getProperty("select");
        List<Map<String, Object>> mapResults = jdbcRunner.queryForList(selectSql, pattern);
        return transferResults(mapResults).findFirst();
    }

    /**
     * Queries the database for all statistics records.
     *
     * @return A list of all {@link Tally} records.
     */
    private List<Tally> queryAll() {
        String selectAllQuery = sqls.getProperty("select_all");
        List<Map<String, Object>> mapResults = jdbcRunner.queryForList(selectAllQuery);
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
        return jdbcRunner.update(insertSql, pattern, count);
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
        return jdbcRunner.update(updateSql, count, pattern, version);
    }

    /**
     * Calculates the new count for a specific column.
     *
     * @param data        The current data object.
     * @param column      The name of the column.
     * @param countOffset The value to add.
     * @return The new total count.
     */
    private long getObjectCount(Tally data, String column, long countOffset) {
        long lastCount = "hit_count".equals(column) ? data.getHitCount() : data.getRequireCount();
        return lastCount + countOffset;
    }

    /**
     * Gracefully shuts down the collector service, flushing all pending statistics to the database.
     * <p>
     * Implements {@link AutoCloseable} so this collector can be used in try-with-resources blocks. Also annotated with
     * {@link PreDestroy} so Spring invokes it automatically on bean destruction. Sets the shutdown flag first so the
     * background thread exits its loop, then waits up to 5 seconds for the final drain to complete before forcing a
     * halt.
     * </p>
     */
    @Override
    @PreDestroy
    public void close() {
        isShutdown = true;
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * An internal record for persisting and retrieving per-pattern cache statistics from the database.
     * <p>
     * Each {@code Tally} maps one cache pattern to its cumulative hit and request counts, plus an optimistic-locking
     * {@code version} field used by the CAS update loop.
     * </p>
     */
    protected static final class Tally {

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

        /**
         * Returns the cache pattern name.
         *
         * @return the pattern name
         */
        public String getPattern() {
            return pattern;
        }

        /**
         * Sets the cache pattern name.
         *
         * @param pattern the pattern name to set
         */
        public void setPattern(String pattern) {
            this.pattern = pattern;
        }

        /**
         * Returns the number of cache hits.
         *
         * @return the hit count
         */
        public long getHitCount() {
            return hitCount;
        }

        /**
         * Sets the number of cache hits.
         *
         * @param hitCount the hit count to set
         */
        public void setHitCount(long hitCount) {
            this.hitCount = hitCount;
        }

        /**
         * Returns the number of cache requests.
         *
         * @return the request count
         */
        public long getRequireCount() {
            return requireCount;
        }

        /**
         * Sets the number of cache requests.
         *
         * @param requireCount the request count to set
         */
        public void setRequireCount(long requireCount) {
            this.requireCount = requireCount;
        }

        /**
         * Returns the version number used for optimistic locking.
         *
         * @return the version value
         */
        public long getVersion() {
            return version;
        }

        /**
         * Sets the version number used for optimistic locking.
         *
         * @param version the version value to set
         */
        public void setVersion(long version) {
            this.version = version;
        }
    }

}
