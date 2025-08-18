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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import org.miaixz.bus.cache.Metrics;

/**
 * 内存缓存命中率统计实现
 * <p>
 * 基于内存的缓存命中率统计实现，使用ConcurrentHashMap存储命中次数和请求次数， 支持并发更新，适用于单机环境或测试环境。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MemoryMetrics implements Metrics {

    /**
     * 命中次数映射，键为缓存模式/分组名称，值为命中次数
     */
    private ConcurrentMap<String, AtomicLong> hitMap = new ConcurrentHashMap<>();

    /**
     * 请求次数映射，键为缓存模式/分组名称，值为请求次数
     */
    private ConcurrentMap<String, AtomicLong> requireMap = new ConcurrentHashMap<>();

    /**
     * 增加命中次数
     *
     * @param pattern 缓存模式/分组名称
     * @param count   增加的命中数量
     */
    @Override
    public void hitIncr(String pattern, int count) {
        hitMap.computeIfAbsent(pattern, (k) -> new AtomicLong()).addAndGet(count);
    }

    /**
     * 增加请求次数
     *
     * @param pattern 缓存模式/分组名称
     * @param count   增加的请求数量
     */
    @Override
    public void reqIncr(String pattern, int count) {
        requireMap.computeIfAbsent(pattern, (k) -> new AtomicLong()).addAndGet(count);
    }

    /**
     * 获取缓存命中率统计信息
     *
     * @return 缓存命中率统计映射，键为缓存模式/分组名称，值为HittingDO对象
     */
    @Override
    public Map<String, Snapshot> getHitting() {
        Map<String, Snapshot> result = new LinkedHashMap<>();
        AtomicLong statisticsHit = new AtomicLong(0);
        AtomicLong statisticsRequired = new AtomicLong(0);

        // 遍历请求次数映射，计算每个模式的命中率
        requireMap.forEach((pattern, count) -> {
            long hit = hitMap.computeIfAbsent(pattern, (key) -> new AtomicLong(0)).get();
            long require = count.get();
            statisticsHit.addAndGet(hit);
            statisticsRequired.addAndGet(require);
            result.put(pattern, Snapshot.newInstance(hit, require));
        });

        // 添加全局命中率统计
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
        hitMap.remove(pattern);
        requireMap.remove(pattern);
    }

    /**
     * 重置所有缓存模式的命中率统计
     */
    @Override
    public void resetAll() {
        hitMap.clear();
        requireMap.clear();
    }

}