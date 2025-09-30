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
import jakarta.annotation.PreDestroy;

/**
 * 抽象缓存命中率统计实现
 * <p>
 * 基于数据库存储的缓存命中率统计实现，使用队列异步写入数据库， 支持并发更新和乐观锁机制，确保数据一致性。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractMetrics implements Metrics {

    /**
     * 单线程执行器，用于异步写入数据库
     */
    private static final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setName("cache:db-writer");
        thread.setDaemon(true);
        return thread;
    });

    /**
     * 可重入锁，用于保证数据一致性
     */
    private static final Lock lock = new ReentrantLock();

    /**
     * 是否已关闭标志
     */
    private volatile boolean isShutdown = false;

    /**
     * 命中次数队列
     */
    private BlockingQueue<CachePair<String, Integer>> hitQueue = new LinkedTransferQueue<>();

    /**
     * 请求次数队列
     */
    private BlockingQueue<CachePair<String, Integer>> requireQueue = new LinkedTransferQueue<>();

    /**
     * JDBC操作模板
     */
    private JdbcOperations jdbcOperations;

    /**
     * SQL语句集合
     */
    private Properties sqls;

    /**
     * 构造方法
     *
     * @param context 上下文参数
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
     * 构造方法
     *
     * @param url      数据库URL
     * @param username 用户名
     * @param password 密码
     */
    public AbstractMetrics(String url, String username, String password) {
        this(newHashMap("url", url, "username", username, "password", password));
    }

    /**
     * 创建新的HashMap
     *
     * @param keyValues 键值对数组
     * @return HashMap实例
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
     * 创建JdbcOperations并初始化数据库
     * <p>
     * 1. 创建JdbcOperations实例 2. 初始化数据库（如加载SQL脚本、创建表、初始化表等）
     * </p>
     *
     * @param context 构造函数中的其他参数
     * @return 初始化完成的JdbcOperations对象
     */
    protected abstract Supplier<JdbcOperations> jdbcOperationsSupplier(Map<String, Object> context);

    /**
     * 将数据库查询结果转换为DataDO流
     *
     * @param map 数据库查询结果
     * @return DataDO流
     */
    protected abstract Stream<DataDO> transferResults(List<Map<String, Object>> map);

    /**
     * 将队列数据转储到数据库
     *
     * @param queue  队列
     * @param column 列名
     */
    private void dumpToDB(BlockingQueue<CachePair<String, Integer>> queue, String column) {
        long times = 0;
        CachePair<String, Integer> head;
        // 收集队列中的全部或前100条数据到一个Map中
        Map<String, AtomicLong> holdMap = new HashMap<>();
        while (null != (head = queue.poll()) && times <= 100) {
            holdMap.computeIfAbsent(head.getLeft(), (key) -> new AtomicLong(0L)).addAndGet(head.getRight());
            ++times;
        }
        // 批量写入数据库
        holdMap.forEach((pattern, count) -> countAddCas(column, pattern, count.get()));
    }

    /**
     * 增加命中次数
     *
     * @param pattern 缓存模式/分组名称
     * @param count   增加的命中数量
     */
    @Override
    public void hitIncr(String pattern, int count) {
        if (count != 0)
            hitQueue.add(CachePair.of(pattern, count));
    }

    /**
     * 增加请求次数
     *
     * @param pattern 缓存模式/分组名称
     * @param count   增加的请求数量
     */
    @Override
    public void reqIncr(String pattern, int count) {
        if (count != 0)
            requireQueue.add(CachePair.of(pattern, count));
    }

    /**
     * 获取缓存命中率统计信息
     *
     * @return 缓存命中率统计映射，键为缓存模式/分组名称，值为HittingDO对象
     */
    @Override
    public Map<String, Snapshot> getHitting() {
        List<DataDO> dataDOS = queryAll();
        AtomicLong statisticsHit = new AtomicLong(0);
        AtomicLong statisticsRequired = new AtomicLong(0);
        // 收集各模式的命中率
        Map<String, Snapshot> result = dataDOS.stream().collect(Collectors.toMap(DataDO::getPattern, (dataDO) -> {
            statisticsHit.addAndGet(dataDO.hitCount);
            statisticsRequired.addAndGet(dataDO.requireCount);
            return Snapshot.newInstance(dataDO.hitCount, dataDO.requireCount);
        }, Snapshot::mergeShootingDO, LinkedHashMap::new));
        // 收集应用所有模式的命中率
        result.put(summaryName(), Snapshot.newInstance(statisticsHit.get(), statisticsRequired.get()));
        return result;
    }

    /**
     * 重置指定缓存模式的命中率统计
     *
     * @param pattern 缓存模式/分组名称
     */
    @Override
    public void reset(String pattern) {
        jdbcOperations.update(sqls.getProperty("delete"), pattern);
    }

    /**
     * 重置所有缓存模式的命中率统计
     */
    @Override
    public void resetAll() {
        jdbcOperations.update(sqls.getProperty("truncate"));
    }

    /**
     * 使用CAS（Compare And Swap）方式增加计数
     *
     * @param column  列名
     * @param pattern 缓存模式/分组名称
     * @param count   计数增量
     */
    private void countAddCas(String column, String pattern, long count) {
        Optional<DataDO> dataOptional = queryObject(pattern);
        // 如果存在模式记录，则更新它
        if (dataOptional.isPresent()) {
            DataDO dataDO = dataOptional.get();
            while (update(column, pattern, getObjectCount(dataDO, column, count), dataDO.version) <= 0) {
                dataDO = queryObject(pattern).get();
            }
        } else {
            lock.lock();
            try {
                // 双重检查
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
     * 查询单个对象
     *
     * @param pattern 缓存模式/分组名称
     * @return DataDO对象
     */
    private Optional<DataDO> queryObject(String pattern) {
        String selectSql = sqls.getProperty("select");
        List<Map<String, Object>> mapResults = jdbcOperations.queryForList(selectSql, pattern);
        return transferResults(mapResults).findFirst();
    }

    /**
     * 查询所有对象
     *
     * @return DataDO列表
     */
    private List<DataDO> queryAll() {
        String selectAllQuery = sqls.getProperty("select_all");
        List<Map<String, Object>> mapResults = jdbcOperations.queryForList(selectAllQuery);
        return transferResults(mapResults).collect(Collectors.toList());
    }

    /**
     * 插入记录
     *
     * @param column  列名
     * @param pattern 缓存模式/分组名称
     * @param count   计数值
     * @return 影响行数
     */
    private int insert(String column, String pattern, long count) {
        String insertSql = String.format(sqls.getProperty("insert"), column);
        return jdbcOperations.update(insertSql, pattern, count);
    }

    /**
     * 更新记录
     *
     * @param column  列名
     * @param pattern 缓存模式/分组名称
     * @param count   计数值
     * @param version 版本号
     * @return 影响行数
     */
    private int update(String column, String pattern, long count, long version) {
        String updateSql = String.format(sqls.getProperty("update"), column);
        return jdbcOperations.update(updateSql, count, pattern, version);
    }

    /**
     * 获取对象计数值
     *
     * @param data        数据对象
     * @param column      列名
     * @param countOffset 计数偏移量
     * @return 计数值
     */
    private long getObjectCount(DataDO data, String column, long countOffset) {
        long lastCount = column.equals("hit_count") ? data.hitCount : data.requireCount;
        return lastCount + countOffset;
    }

    /**
     * 销毁方法
     * <p>
     * 使用@PreDestroy注解，在Bean销毁时等待队列处理完毕并关闭执行器
     * </p>
     */
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

    /**
     * 数据对象
     * <p>
     * 用于存储缓存命中率统计数据的内部类
     * </p>
     */
    protected static final class DataDO {

        /**
         * 缓存模式/分组名称
         */
        private String pattern;

        /**
         * 命中次数
         */
        private long hitCount;

        /**
         * 请求次数
         */
        private long requireCount;

        /**
         * 版本号，用于乐观锁
         */
        private long version;

        /**
         * 获取缓存模式/分组名称
         *
         * @return 缓存模式/分组名称
         */
        public String getPattern() {
            return pattern;
        }

        /**
         * 设置缓存模式/分组名称
         *
         * @param pattern 缓存模式/分组名称
         */
        public void setPattern(String pattern) {
            this.pattern = pattern;
        }

        /**
         * 设置命中次数
         *
         * @param hitCount 命中次数
         */
        public void setHitCount(long hitCount) {
            this.hitCount = hitCount;
        }

        /**
         * 设置请求次数
         *
         * @param requireCount 请求次数
         */
        public void setRequireCount(long requireCount) {
            this.requireCount = requireCount;
        }

        /**
         * 设置版本号
         *
         * @param version 版本号
         */
        public void setVersion(long version) {
            this.version = version;
        }
    }

}
