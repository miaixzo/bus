/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org OSHI and other contributors.                 *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.health.windows;

import com.sun.jna.platform.win32.WinNT.HANDLEByReference;
import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.health.Builder;
import org.aoju.bus.logger.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 处理性能计数器查询
 *
 * @author Kimi Liu
 * @version 5.9.5
 * @since JDK 1.8+
 */
@ThreadSafe
public final class PerfCounterQueryHandler {

    private Map<PerfDataCounter.PerfCounter, HANDLEByReference> counterHandleMap = new ConcurrentHashMap<>();
    private Map<String, HANDLEByReference> queryHandleMap = new ConcurrentHashMap<>();
    private Map<String, List<PerfDataCounter.PerfCounter>> queryCounterMap = new ConcurrentHashMap<>();

    public PerfCounterQueryHandler() {
        // Set up hook to close all queries on shutdown
        // User is expected to release all queries so this should only be a
        // backup
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                removeAllCounters();
            }
        });
    }

    /**
     * 开始监视一个性能数据计数器，该计数器附加到一个查询，该查询的键是计数器的对象
     *
     * @param counter 一个PerfCounter对象
     * @return 如果成功添加了计数器，则为真
     */
    public boolean addCounterToQuery(PerfDataCounter.PerfCounter counter) {
        return addCounterToQuery(counter, counter.getObject());
    }

    /**
     * 开始监视一个性能数据计数器，该计数器附加到一个键为指定字符串的查询
     *
     * @param counter 一个PerfCounter对象
     * @param key     用作查询键的字符串。当任何一个计数器被更新时，所有具有此键的计数器都将被更新
     * @return 如果成功添加了计数器，则为真
     */
    public boolean addCounterToQuery(PerfDataCounter.PerfCounter counter, String key) {
        // 打开一个新查询或获取一个现有查询的句柄
        HANDLEByReference q = getOrOpenQuery(key);
        if (q == null) {
            Logger.error("Failed to open a query for PDH object: {}", counter.getObject());
            return false;
        }
        // 获取新的计数器拦截
        HANDLEByReference p = new HANDLEByReference();
        if (PerfDataCounter.addCounter(q, counter.getCounterPath(), p)) {
            counterHandleMap.put(counter, p);
            List<PerfDataCounter.PerfCounter> counterList = queryCounterMap.get(key);
            if (counterList != null) {
                counterList.add(counter);
            }
            return true;
        }
        return false;
    }

    /**
     * 停止监视附加到查询(其键是计数器的对象)上的性能数据计数器
     *
     * @param counter PerfCounter对象
     * @return 如果成功移除计数器，则为真
     */
    public boolean removeCounterFromQuery(PerfDataCounter.PerfCounter counter) {
        return removeCounterFromQuery(counter, counter.getObject());
    }

    /**
     * 停止监视附加到键为指定字符串的查询的性能数据计数器
     *
     * @param counter PerfCounter对象
     * @param key     用作查询键的字符串。当任何一个计数器被更新时，所有具有此键的计数器都将被更新
     * @return 如果成功移除计数器，则为真
     */
    public boolean removeCounterFromQuery(PerfDataCounter.PerfCounter counter, String key) {
        HANDLEByReference href = counterHandleMap.remove(counter);
        // 如果句柄不存在，则为空
        boolean success = false;
        if (href != null) {
            success = PerfDataCounter.removeCounter(href);
        }
        List<PerfDataCounter.PerfCounter> counterList = queryCounterMap.get(key);
        // 如果列表不存在，则为空
        if (counterList != null && counterList.remove(counter) && counterList.isEmpty()) {
            queryCounterMap.remove(key);
            PerfDataCounter.closeQuery(queryHandleMap.remove(key));
        }
        return success;
    }

    /**
     * 停止监视特定queryKey的性能数据计数器，并释放它们的资源
     *
     * @param queryKey 要从中移除计数器的计数器对象
     */
    public void removeAllCountersFromQuery(String queryKey) {
        // 从queryCounter映射中删除计数器列表
        List<PerfDataCounter.PerfCounter> counterList = queryCounterMap.remove(queryKey);
        if (counterList == null) {
            return;
        }
        // 从处理映射中移除所有计数器
        for (PerfDataCounter.PerfCounter counter : counterList) {
            HANDLEByReference href = counterHandleMap.remove(counter);
            // 如果句柄不存在，则为空
            if (href != null) {
                PerfDataCounter.removeCounter(href);
            }
        }
        // 从查询映射中删除查询
        HANDLEByReference href = queryHandleMap.remove(queryKey);
        if (href != null) {
            PerfDataCounter.closeQuery(href);
        }
    }

    /**
     * 停止监视所有性能数据计数器并释放它们的资源
     */
    public void removeAllCounters() {
        // 删除所有计数器手柄
        for (HANDLEByReference href : counterHandleMap.values()) {
            PerfDataCounter.removeCounter(href);
        }
        counterHandleMap.clear();
        // 删除所有的查询
        for (HANDLEByReference query : queryHandleMap.values()) {
            PerfDataCounter.closeQuery(query);
        }
        queryHandleMap.clear();
        queryCounterMap.clear();
    }

    /**
     * 更新查询中的所有计数器
     *
     * @param key 要更新的查询的键
     * @return 更新所有计数器的时间戳，以从epoch开始的毫秒为单位，如果更新失败则为0
     */
    public long updateQuery(String key) {
        if (!queryHandleMap.containsKey(key)) {
            Logger.error("Query key {} does not exist to update.", key);
            return 0L;
        }
        return PerfDataCounter.updateQueryTimestamp(queryHandleMap.get(key));
    }

    /**
     * 查询性能数据计数器的原始计数器值。进一步的数学操作/转换留给调用者
     *
     * @param counter 要查询的计数器
     * @return 计数器的原始值
     */
    public long queryCounter(PerfDataCounter.PerfCounter counter) {
        if (!counterHandleMap.containsKey(counter)) {
            if (Logger.get().isError()) {
                Logger.error("Counter {} does not exist to query.", counter.getCounterPath());
            }
            return 0;
        }
        long value = PerfDataCounter.queryCounter(counterHandleMap.get(counter));
        if (value < 0) {
            if (Logger.get().isTrace()) {
                Logger.warn("Error querying counter {}: {}", counter.getCounterPath(),
                        String.format(Builder.formatError((int) value)));
            }
            return 0L;
        }
        return value;
    }

    /**
     * 为给定的字符串打开一个查询，或者确认已经为该字符串打开了一个查询
     * 可以将多个计数器添加到此字符串，但将同时查询所有计数器
     *
     * @param key 与计数器关联的字符串。大多数代码默认使用英文PDH对象
     *            名称，因此自定义键应该避免这些字符串
     * @return 一个查询句柄，如果发生错误则为null
     */
    private HANDLEByReference getOrOpenQuery(String key) {
        if (queryHandleMap.containsKey(key)) {
            return queryHandleMap.get(key);
        }
        HANDLEByReference q = new HANDLEByReference();
        if (PerfDataCounter.openQuery(q)) {
            queryHandleMap.put(key, q);
            List<PerfDataCounter.PerfCounter> counterList = Collections.synchronizedList(new ArrayList<>());
            queryCounterMap.put(key, counterList);
            return q;
        }
        return null;
    }

}
