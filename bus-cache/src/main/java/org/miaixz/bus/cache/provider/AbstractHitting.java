/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.cache.provider;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.miaixz.bus.cache.Hitting;
import org.miaixz.bus.cache.magic.CachePair;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.setting.Builder;
import org.springframework.jdbc.core.JdbcOperations;

import jakarta.annotation.PreDestroy;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractHitting implements Hitting {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setName("cache:db-writer");
        thread.setDaemon(true);
        return thread;
    });

    private static final Lock lock = new ReentrantLock();

    private volatile boolean isShutdown = false;

    private BlockingQueue<CachePair<String, Integer>> hitQueue = new LinkedTransferQueue<>();

    private BlockingQueue<CachePair<String, Integer>> requireQueue = new LinkedTransferQueue<>();

    private JdbcOperations jdbcOperations;

    private Properties sqls;

    protected AbstractHitting(Map<String, Object> context) {
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

    public AbstractHitting(String url, String username, String password) {
        this(newHashMap("url", url, "username", username, "password", password));
    }

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
     * 1. create JdbcOperations 2. init db(like: load sql script, create table, init table...)
     *
     * @param context :other parameters from constructor
     * @return initiated JdbOperations object
     */
    protected abstract Supplier<JdbcOperations> jdbcOperationsSupplier(Map<String, Object> context);

    /**
     * convert DB Map Result to DataDO(Stream)
     *
     * @param map result from query DB.
     * @return the object
     */
    protected abstract Stream<DataDO> transferResults(List<Map<String, Object>> map);

    private void dumpToDB(BlockingQueue<CachePair<String, Integer>> queue, String column) {
        long times = 0;
        CachePair<String, Integer> head;

        // gather queue's all or before 100 data to a Map
        Map<String, AtomicLong> holdMap = new HashMap<>();
        while (null != (head = queue.poll()) && times <= 100) {
            holdMap.computeIfAbsent(head.getLeft(), (key) -> new AtomicLong(0L)).addAndGet(head.getRight());
            ++times;
        }

        // batch write to DB
        holdMap.forEach((pattern, count) -> countAddCas(column, pattern, count.get()));
    }

    @Override
    public void hitIncr(String pattern, int count) {
        if (count != 0)
            hitQueue.add(CachePair.of(pattern, count));
    }

    @Override
    public void reqIncr(String pattern, int count) {
        if (count != 0)
            requireQueue.add(CachePair.of(pattern, count));
    }

    @Override
    public Map<String, Hitting.HittingDO> getHitting() {
        List<DataDO> dataDOS = queryAll();
        AtomicLong statisticsHit = new AtomicLong(0);
        AtomicLong statisticsRequired = new AtomicLong(0);

        // gather pattern's hit rate
        Map<String, Hitting.HittingDO> result = dataDOS.stream()
                .collect(Collectors.toMap(DataDO::getPattern, (dataDO) -> {
                    statisticsHit.addAndGet(dataDO.hitCount);
                    statisticsRequired.addAndGet(dataDO.requireCount);
                    return Hitting.HittingDO.newInstance(dataDO.hitCount, dataDO.requireCount);
                }, Hitting.HittingDO::mergeShootingDO, LinkedHashMap::new));

        // gather application all pattern's hit rate
        result.put(summaryName(), Hitting.HittingDO.newInstance(statisticsHit.get(), statisticsRequired.get()));

        return result;
    }

    @Override
    public void reset(String pattern) {
        jdbcOperations.update(sqls.getProperty("delete"), pattern);
    }

    @Override
    public void resetAll() {
        jdbcOperations.update(sqls.getProperty("truncate"));
    }

    private void countAddCas(String column, String pattern, long count) {
        Optional<DataDO> dataOptional = queryObject(pattern);

        // if has pattern record, update it.
        if (dataOptional.isPresent()) {
            DataDO dataDO = dataOptional.get();
            while (update(column, pattern, getObjectCount(dataDO, column, count), dataDO.version) <= 0) {
                dataDO = queryObject(pattern).get();
            }
        } else {
            lock.lock();
            try {
                // double check
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

    private Optional<DataDO> queryObject(String pattern) {
        String selectSql = sqls.getProperty("select");
        List<Map<String, Object>> mapResults = jdbcOperations.queryForList(selectSql, pattern);

        return transferResults(mapResults).findFirst();
    }

    private List<DataDO> queryAll() {
        String selectAllQuery = sqls.getProperty("select_all");
        List<Map<String, Object>> mapResults = jdbcOperations.queryForList(selectAllQuery);

        return transferResults(mapResults).collect(Collectors.toList());
    }

    private int insert(String column, String pattern, long count) {
        String insertSql = String.format(sqls.getProperty("insert"), column);

        return jdbcOperations.update(insertSql, pattern, count);
    }

    private int update(String column, String pattern, long count, long version) {
        String updateSql = String.format(sqls.getProperty("update"), column);

        return jdbcOperations.update(updateSql, count, pattern, version);
    }

    private long getObjectCount(DataDO data, String column, long countOffset) {
        long lastCount = column.equals("hit_count") ? data.hitCount : data.requireCount;

        return lastCount + countOffset;
    }

    @PreDestroy
    public void tearDown() {
        while (hitQueue.size() > 0 || requireQueue.size() > 0) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException ignored) {
            }
        }

        isShutdown = true;
    }

    protected static final class DataDO {

        private String pattern;

        private long hitCount;

        private long requireCount;

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
