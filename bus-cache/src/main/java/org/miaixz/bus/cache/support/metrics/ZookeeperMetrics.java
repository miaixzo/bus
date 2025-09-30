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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicLong;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.KeeperException;
import org.miaixz.bus.cache.Metrics;
import org.miaixz.bus.cache.magic.CachePair;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.logger.Logger;
import jakarta.annotation.PreDestroy;

/**
 * ZooKeeper缓存命中率统计实现
 * <p>
 * 基于ZooKeeper实现的分布式缓存命中率统计，使用Curator框架操作ZooKeeper， 支持分布式原子计数器，适用于分布式环境下的缓存命中率统计。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ZookeeperMetrics implements Metrics {

    /**
     * 单线程执行器，用于异步写入ZooKeeper
     */
    private static final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setName("cache:zk-uploader");
        thread.setDaemon(true);
        return thread;
    });

    /**
     * ZooKeeper命名空间
     */
    private static final String NAME_SPACE = "cache";

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
     * 命中计数器映射
     */
    private Map<String, DistributedAtomicLong> hitCounterMap = new HashMap<>();

    /**
     * 请求计数器映射
     */
    private Map<String, DistributedAtomicLong> requireCounterMap = new HashMap<>();

    /**
     * ZooKeeper客户端
     */
    private CuratorFramework client;

    /**
     * 命中路径前缀
     */
    private String hitPathPrefix;

    /**
     * 请求路径前缀
     */
    private String requirePathPrefix;

    /**
     * 构造方法
     *
     * @param zkServer ZooKeeper服务器地址
     */
    public ZookeeperMetrics(String zkServer) {
        this(zkServer, System.getProperty("product.name", "unnamed"));
    }

    /**
     * 构造方法
     *
     * @param zkServer    ZooKeeper服务器地址
     * @param productName 产品名称，用于命名空间隔离
     */
    public ZookeeperMetrics(String zkServer, String productName) {
        this.client = CuratorFrameworkFactory.builder().connectString(zkServer).retryPolicy(new RetryNTimes(3, 0))
                .namespace(NAME_SPACE).build();
        client.start();
        // 添加前缀和后缀
        String uniqueProductName = processProductName(productName);
        this.hitPathPrefix = String.format("%s%s", uniqueProductName, "hit");
        this.requirePathPrefix = String.format("%s%s", uniqueProductName, "require");
        try {
            client.create().creatingParentsIfNeeded().forPath(hitPathPrefix);
            client.create().creatingParentsIfNeeded().forPath(requirePathPrefix);
        } catch (KeeperException.NodeExistsException ignored) {
            // 节点已存在，忽略异常
        } catch (Exception e) {
            throw new RuntimeException("create path: " + hitPathPrefix + ", " + requirePathPrefix + " on namespace: "
                    + NAME_SPACE + " error", e);
        }
        executor.submit(() -> {
            while (!isShutdown) {
                dumpToZK(hitQueue, hitCounterMap, hitPathPrefix);
                dumpToZK(requireQueue, requireCounterMap, requirePathPrefix);
            }
        });
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
        Map<String, Snapshot> result = new LinkedHashMap<>();
        AtomicLong totalHit = new AtomicLong(0L);
        AtomicLong totalRequire = new AtomicLong(0L);

        // 遍历请求计数器映射，计算每个模式的命中率
        this.requireCounterMap.forEach((key, requireCounter) -> {
            try {
                long require = getValue(requireCounter.get());
                long hit = getValue(hitCounterMap.get(key));
                totalRequire.addAndGet(require);
                totalHit.addAndGet(hit);
                result.put(key, Snapshot.newInstance(hit, require));
            } catch (Exception e) {
                Logger.error(e, "acquire hit count error: ", e.getMessage());
            }
        });

        // 添加全局命中率统计
        result.put(summaryName(), Snapshot.newInstance(totalHit.get(), totalRequire.get()));
        return result;
    }

    /**
     * 重置指定缓存模式的命中率统计
     *
     * @param pattern 缓存模式/分组名称
     */
    @Override
    public void reset(String pattern) {
        hitCounterMap.computeIfPresent(pattern, this::doReset);
        requireCounterMap.computeIfPresent(pattern, this::doReset);
    }

    /**
     * 重置所有缓存模式的命中率统计
     */
    @Override
    public void resetAll() {
        hitCounterMap.forEach(this::doReset);
        requireCounterMap.forEach(this::doReset);
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
     * 处理产品名称，确保以斜杠开头和结尾
     *
     * @param productName 产品名称
     * @return 处理后的产品名称
     */
    private String processProductName(String productName) {
        if (!productName.startsWith(Symbol.SLASH)) {
            productName = Symbol.SLASH + productName;
        }
        if (!productName.endsWith(Symbol.SLASH)) {
            productName = productName + Symbol.SLASH;
        }
        return productName;
    }

    /**
     * 重置分布式计数器
     *
     * @param pattern 缓存模式/分组名称
     * @param counter 分布式计数器
     * @return null
     */
    private DistributedAtomicLong doReset(String pattern, DistributedAtomicLong counter) {
        try {
            counter.forceSet(0L);
        } catch (Exception e) {
            Logger.error(e, "reset distribute counter error: ", e.getMessage());
        }
        return null;
    }

    /**
     * 将队列数据转储到ZooKeeper
     *
     * @param queue      队列
     * @param counterMap 计数器映射
     * @param zkPrefix   ZooKeeper路径前缀
     */
    private void dumpToZK(
            BlockingQueue<CachePair<String, Integer>> queue,
            Map<String, DistributedAtomicLong> counterMap,
            String zkPrefix) {
        long count = 0;
        CachePair<String, Integer> head;
        // 将队列中所有的或前100条数据聚合到一个暂存Map中
        Map<String, AtomicLong> holdMap = new HashMap<>();
        while (null != (head = queue.poll()) && count <= 100) {
            holdMap.computeIfAbsent(head.getLeft(), (key) -> new AtomicLong(0L)).addAndGet(head.getRight());
            ++count;
        }
        holdMap.forEach((pattern, atomicCount) -> {
            String zkPath = String.format("%s/%s", zkPrefix, pattern);
            DistributedAtomicLong counter = counterMap.computeIfAbsent(
                    pattern,
                    (key) -> new DistributedAtomicLong(client, zkPath, new RetryNTimes(10, 10)));
            try {
                counter.add(atomicCount.get()).postValue();
            } catch (Exception e) {
                Logger.error(e, "dump data from queue to zookeeper error: ", e.getMessage());
            }
        });
    }

    /**
     * 获取值
     *
     * @param value 值对象
     * @return 长整型值
     * @throws Exception 可能抛出的异常
     */
    private long getValue(Object value) throws Exception {
        long result = 0L;
        if (null != value) {
            if (value instanceof DistributedAtomicLong) {
                result = getValue(((DistributedAtomicLong) value).get());
            } else if (value instanceof AtomicValue) {
                result = (long) ((AtomicValue) value).postValue();
            } else {
                result = ((AtomicLong) value).get();
            }
        }
        return result;
    }

}
