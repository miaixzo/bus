/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org OSHI and other contributors.                 *
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
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.health.windows;

import org.aoju.bus.core.annotation.NotThreadSafe;
import org.aoju.bus.health.Formats;
import org.aoju.bus.health.windows.PerfDataKit.PerfCounter;
import org.aoju.bus.logger.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * 处理性能计数器查询
 *
 * @author Kimi Liu
 * @version 6.2.1
 * @since JDK 1.8+
 */
@NotThreadSafe
public final class PerfCounterQueryHandler implements AutoCloseable {

    // Map of counter handles
    private final Map<PerfCounter, WinNT.HANDLEByReference> counterHandleMap = new HashMap<>();
    // The query handle
    private WinNT.HANDLEByReference queryHandle = null;

    /**
     * 开始监视一个性能数据计数器，该计数器附加到一个键为指定字符串的查询
     *
     * @param counter 一个PerfCounter对象
     * @return 如果成功添加了计数器，则为真
     */
    public boolean addCounterToQuery(PerfCounter counter) {
        // 打开一个新查询或获取一个现有查询的句柄
        if (null == this.queryHandle) {
            this.queryHandle = new WinNT.HANDLEByReference();
            if (!PerfDataKit.openQuery(this.queryHandle)) {
                Logger.warn("Failed to open a query for PDH object: {}", counter.getObject());
                this.queryHandle = null;
                return false;
            }
        }
        // 获取新的计数器拦截
        WinNT.HANDLEByReference p = new WinNT.HANDLEByReference();
        if (!PerfDataKit.addCounter(this.queryHandle, counter.getCounterPath(), p)) {
            Logger.warn("Failed to add counter for PDH object: {}", counter.getObject());
            return false;
        }
        counterHandleMap.put(counter, p);
        return true;
    }

    /**
     * 停止监视附加到键为指定字符串的查询的性能数据计数器
     *
     * @param counter PerfCounter对象
     * @return 如果成功移除计数器，则为真
     */
    public boolean removeCounterFromQuery(PerfCounter counter) {
        boolean success = false;
        WinNT.HANDLEByReference href = counterHandleMap.remove(counter);
        // 如果句柄不存在，则为空
        if (null != href) {
            success = PerfDataKit.removeCounter(href);
        }
        if (counterHandleMap.isEmpty()) {
            PerfDataKit.closeQuery(queryHandle);
            queryHandle = null;
        }
        return success;
    }

    /**
     * 停止监视所有性能数据计数器并释放它们的资源
     */
    public void removeAllCounters() {
        // 删除所有计数器手柄
        for (WinNT.HANDLEByReference href : counterHandleMap.values()) {
            PerfDataKit.removeCounter(href);
        }
        counterHandleMap.clear();
        // 删除所有的查询
        if (null != this.queryHandle) {
            PerfDataKit.closeQuery(this.queryHandle);
        }
        this.queryHandle = null;
    }

    /**
     * 更新查询中的所有计数器
     *
     * @return 更新所有计数器的时间戳，以从epoch开始的毫秒为单位，如果更新失败则为0
     */
    public long updateQuery() {
        if (null == queryHandle) {
            Logger.warn("Query does not exist to update.");
            return 0L;
        }
        return PerfDataKit.updateQueryTimestamp(queryHandle);
    }

    /**
     * 查询性能数据计数器的原始计数器值。进一步的数学操作/转换留给调用者
     *
     * @param counter 要查询的计数器
     * @return 计数器的原始值
     */
    public long queryCounter(PerfCounter counter) {
        if (!counterHandleMap.containsKey(counter)) {
            if (Logger.get().isWarn()) {
                Logger.warn("Counter {} does not exist to query.", counter.getCounterPath());
            }
            return 0;
        }
        long value = PerfDataKit.queryCounter(counterHandleMap.get(counter));
        if (value < 0) {
            if (Logger.get().isWarn()) {
                Logger.warn("Error querying counter {}: {}", counter.getCounterPath(),
                        String.format(Formats.formatError((int) value)));
            }
            return 0L;
        }
        return value;
    }

    @Override
    public void close() {
        removeAllCounters();
    }

}
