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

/**
 * A ZooKeeper-based implementation of {@link Metrics} for distributed cache statistics.
 * <p>
 * This class uses the Apache Curator framework to interact with ZooKeeper, leveraging {@link DistributedAtomicLong} to
 * maintain distributed counters for hit and request rates. It is designed for use in distributed environments where
 * statistics need to be aggregated across multiple application instances.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ZookeeperMetrics implements Metrics {

    /**
     * A single-threaded executor for asynchronously writing metrics to ZooKeeper.
     */
    private static final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setName("cache:zk-uploader");
        thread.setDaemon(true);
        return thread;
    });

    /**
     * The root namespace in ZooKeeper for all cache metrics.
     */
    private static final String NAME_SPACE = "cache";

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
     * A map to hold the distributed counters for hit counts.
     */
    private final Map<String, DistributedAtomicLong> hitCounterMap = new HashMap<>();

    /**
     * A map to hold the distributed counters for request counts.
     */
    private final Map<String, DistributedAtomicLong> requireCounterMap = new HashMap<>();

    /**
     * The Curator framework client for ZooKeeper.
     */
    private final CuratorFramework client;

    /**
     * The base ZooKeeper path for storing hit counters.
     */
    private final String hitPathPrefix;

    /**
     * The base ZooKeeper path for storing request counters.
     */
    private final String requirePathPrefix;

    /**
     * Constructs a {@code ZookeeperMetrics} instance with a default product name.
     *
     * @param zkServer The connection string for the ZooKeeper ensemble (e.g., "host1:port1,host2:port2").
     */
    public ZookeeperMetrics(String zkServer) {
        this(zkServer, System.getProperty("product.name", "unnamed"));
    }

    /**
     * Constructs a {@code ZookeeperMetrics} instance.
     *
     * @param zkServer    The ZooKeeper connection string.
     * @param productName A name to isolate metrics for a specific product or application, used as a sub-path in
     *                    ZooKeeper.
     */
    public ZookeeperMetrics(String zkServer, String productName) {
        this.client = CuratorFrameworkFactory.builder().connectString(zkServer).retryPolicy(new RetryNTimes(3, 0))
                .namespace(NAME_SPACE).build();
        client.start();

        String uniqueProductName = processProductName(productName);
        this.hitPathPrefix = String.format("%s%s", uniqueProductName, "hit");
        this.requirePathPrefix = String.format("%s%s", uniqueProductName, "require");

        try {
            client.create().creatingParentsIfNeeded().forPath(hitPathPrefix);
            client.create().creatingParentsIfNeeded().forPath(requirePathPrefix);
        } catch (KeeperException.NodeExistsException ignored) {
            // Path already exists, which is fine.
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to create base paths in ZooKeeper: " + hitPathPrefix + ", " + requirePathPrefix, e);
        }

        executor.submit(() -> {
            while (!isShutdown) {
                dumpToZK(hitQueue, hitCounterMap, hitPathPrefix);
                dumpToZK(requireQueue, requireCounterMap, requirePathPrefix);
            }
        });
    }

    /**
     * Increments the hit counter for the specified pattern by adding the count to a queue.
     * <p>
     * The count is asynchronously written to ZooKeeper in batches.
     * </p>
     *
     * @param pattern The cache pattern name (e.g., cache name).
     * @param count   The number to increment the hit counter by.
     */
    @Override
    public void hitIncr(String pattern, int count) {
        if (count != 0) {
            hitQueue.add(CachePair.of(pattern, count));
        }
    }

    /**
     * Increments the request counter for the specified pattern by adding the count to a queue.
     * <p>
     * The count is asynchronously written to ZooKeeper in batches.
     * </p>
     *
     * @param pattern The cache pattern name (e.g., cache name).
     * @param count   The number to increment the request counter by.
     */
    @Override
    public void reqIncr(String pattern, int count) {
        if (count != 0) {
            requireQueue.add(CachePair.of(pattern, count));
        }
    }

    /**
     * Retrieves the current hit statistics for all cache patterns from ZooKeeper.
     * <p>
     * This method reads the distributed counters and calculates the hit rate.
     * </p>
     *
     * @return A map of cache patterns to their {@link Snapshot} statistics, including a summary entry.
     */
    @Override
    public Map<String, Snapshot> getHitting() {
        Map<String, Snapshot> result = new LinkedHashMap<>();
        AtomicLong totalHit = new AtomicLong(0L);
        AtomicLong totalRequire = new AtomicLong(0L);

        this.requireCounterMap.forEach((key, requireCounter) -> {
            try {
                long require = getValue(requireCounter.get());
                long hit = getValue(hitCounterMap.get(key));
                totalRequire.addAndGet(require);
                totalHit.addAndGet(hit);
                result.put(key, Snapshot.newInstance(hit, require));
            } catch (Exception e) {
                Logger.error(e, "Failed to acquire hit count for pattern [{}]: {}", key, e.getMessage());
            }
        });

        result.put(summaryName(), Snapshot.newInstance(totalHit.get(), totalRequire.get()));
        return result;
    }

    /**
     * Resets the distributed counters for the specified pattern to zero.
     *
     * @param pattern The cache pattern to reset.
     */
    @Override
    public void reset(String pattern) {
        hitCounterMap.computeIfPresent(pattern, this::doReset);
        requireCounterMap.computeIfPresent(pattern, this::doReset);
    }

    /**
     * Resets all distributed counters for all patterns to zero.
     */
    @Override
    public void resetAll() {
        hitCounterMap.forEach(this::doReset);
        requireCounterMap.forEach(this::doReset);
    }

    /**
     * Shuts down the ZooKeeper metrics service gracefully.
     * <p>
     * This method waits for all pending metrics to be written to ZooKeeper, then closes the ZooKeeper client
     * connection. This method is called automatically when the application context is destroyed.
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
        client.close();
    }

    private String processProductName(String productName) {
        if (!productName.startsWith(Symbol.SLASH)) {
            productName = Symbol.SLASH + productName;
        }
        if (!productName.endsWith(Symbol.SLASH)) {
            productName = productName + Symbol.SLASH;
        }
        return productName;
    }

    private DistributedAtomicLong doReset(String pattern, DistributedAtomicLong counter) {
        try {
            counter.forceSet(0L);
        } catch (Exception e) {
            Logger.error(e, "Failed to reset distributed counter for pattern [{}]: {}", pattern, e.getMessage());
        }
        return null; // To satisfy the BiFunction interface
    }

    private void dumpToZK(
            BlockingQueue<CachePair<String, Integer>> queue,
            Map<String, DistributedAtomicLong> counterMap,
            String zkPrefix) {
        long count = 0;
        CachePair<String, Integer> head;
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
                counter.add(atomicCount.get());
            } catch (Exception e) {
                Logger.error(e, "Failed to dump data to ZooKeeper for path [{}]: {}", zkPath, e.getMessage());
            }
        });
    }

    private long getValue(Object value) throws Exception {
        if (value instanceof DistributedAtomicLong) {
            return getValue(((DistributedAtomicLong) value).get());
        } else if (value instanceof AtomicValue) {
            return (long) ((AtomicValue) value).postValue();
        } else if (value instanceof AtomicLong) {
            return ((AtomicLong) value).get();
        } else if (value == null) {
            return 0L;
        }
        return 0L;
    }

}
