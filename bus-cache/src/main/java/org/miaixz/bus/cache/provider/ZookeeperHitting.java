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
package org.miaixz.bus.cache.provider;

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
import org.miaixz.bus.cache.Hitting;
import org.miaixz.bus.cache.magic.CachePair;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.logger.Logger;

import jakarta.annotation.PreDestroy;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ZookeeperHitting implements Hitting {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setName("cache:zk-uploader");
        thread.setDaemon(true);
        return thread;
    });

    private static final String NAME_SPACE = "cache";

    private volatile boolean isShutdown = false;

    private BlockingQueue<CachePair<String, Integer>> hitQueue = new LinkedTransferQueue<>();

    private BlockingQueue<CachePair<String, Integer>> requireQueue = new LinkedTransferQueue<>();

    private Map<String, DistributedAtomicLong> hitCounterMap = new HashMap<>();

    private Map<String, DistributedAtomicLong> requireCounterMap = new HashMap<>();

    private CuratorFramework client;

    private String hitPathPrefix;

    private String requirePathPrefix;

    public ZookeeperHitting(String zkServer) {
        this(zkServer, System.getProperty("product.name", "unnamed"));
    }

    public ZookeeperHitting(String zkServer, String productName) {
        this.client = CuratorFrameworkFactory.builder().connectString(zkServer).retryPolicy(new RetryNTimes(3, 0))
                .namespace(NAME_SPACE).build();
        client.start();

        // append prefix and suffix
        String uniqueProductName = processProductName(productName);
        this.hitPathPrefix = String.format("%s%s", uniqueProductName, "hit");
        this.requirePathPrefix = String.format("%s%s", uniqueProductName, "require");
        try {
            client.create().creatingParentsIfNeeded().forPath(hitPathPrefix);
            client.create().creatingParentsIfNeeded().forPath(requirePathPrefix);
        } catch (KeeperException.NodeExistsException ignored) {
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
        Map<String, Hitting.HittingDO> result = new LinkedHashMap<>();

        AtomicLong totalHit = new AtomicLong(0L);
        AtomicLong totalRequire = new AtomicLong(0L);
        this.requireCounterMap.forEach((key, requireCounter) -> {
            try {
                long require = getValue(requireCounter.get());
                long hit = getValue(hitCounterMap.get(key));

                totalRequire.addAndGet(require);
                totalHit.addAndGet(hit);

                result.put(key, Hitting.HittingDO.newInstance(hit, require));
            } catch (Exception e) {
                Logger.error(e, "acquire hit count error: ", e.getMessage());
            }
        });

        result.put(summaryName(), Hitting.HittingDO.newInstance(totalHit.get(), totalRequire.get()));

        return result;
    }

    @Override
    public void reset(String pattern) {
        hitCounterMap.computeIfPresent(pattern, this::doReset);
        requireCounterMap.computeIfPresent(pattern, this::doReset);
    }

    @Override
    public void resetAll() {
        hitCounterMap.forEach(this::doReset);
        requireCounterMap.forEach(this::doReset);
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
            Logger.error(e, "reset distribute counter error: ", e.getMessage());
        }

        return null;
    }

    private void dumpToZK(BlockingQueue<CachePair<String, Integer>> queue,
            Map<String, DistributedAtomicLong> counterMap, String zkPrefix) {
        long count = 0;
        CachePair<String, Integer> head;

        // 将queue中所有的 || 前100条数据聚合到一个暂存Map中
        Map<String, AtomicLong> holdMap = new HashMap<>();
        while (null != (head = queue.poll()) && count <= 100) {
            holdMap.computeIfAbsent(head.getLeft(), (key) -> new AtomicLong(0L)).addAndGet(head.getRight());
            ++count;
        }

        holdMap.forEach((pattern, atomicCount) -> {
            String zkPath = String.format("%s/%s", zkPrefix, pattern);
            DistributedAtomicLong counter = counterMap.computeIfAbsent(pattern,
                    (key) -> new DistributedAtomicLong(client, zkPath, new RetryNTimes(10, 10)));
            try {
                counter.add(atomicCount.get()).postValue();
            } catch (Exception e) {
                Logger.error(e, "dump data from queue to zookeeper error: ", e.getMessage());
            }
        });
    }

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
