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
package org.miaixz.bus.cache;

import java.util.Map;

/**
 * 缓存命中统计接口
 * <p>
 * 定义缓存命中率统计的核心操作，包括记录请求次数、命中次数、获取统计信息和重置统计。 提供对缓存模式或分组的命中率统计功能，适用于监控缓存性能。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Metrics {

    /**
     * 增加缓存请求次数
     * <p>
     * 为指定缓存模式或分组增加请求次数，用于统计缓存访问频率。 示例代码：
     * </p>
     * 
     * <pre>{@code
     * Metrics metrics = new SomeMetricsImpl();
     * metrics.reqIncr("userCache", 1);
     * }</pre>
     *
     * @param pattern 缓存模式或分组名称
     * @param count   增加的请求数量
     */
    void reqIncr(String pattern, int count);

    /**
     * 增加缓存命中次数
     * <p>
     * 为指定缓存模式或分组增加命中次数，用于统计缓存命中情况。 示例代码：
     * </p>
     * 
     * <pre>{@code
     * Metrics metrics = new SomeMetricsImpl();
     * metrics.hitIncr("userCache", 1);
     * }</pre>
     *
     * @param pattern 缓存模式或分组名称
     * @param count   增加的命中数量
     */
    void hitIncr(String pattern, int count);

    /**
     * 获取缓存命中率统计信息
     * <p>
     * 返回缓存命中率统计数据，键为缓存模式或分组名称，值为对应的命中率数据对象。 示例代码：
     * </p>
     * 
     * <pre>{@code
     * Metrics metrics = new SomeMetricsImpl();
     * Map<String, Snapshot> stats = metrics.getHitting();
     * stats.forEach((pattern, snapshot) -> System.out.println(pattern + ": 命中率 " + snapshot.getRate()));
     * }</pre>
     *
     * @return 包含缓存模式及其命中率数据的映射
     */
    Map<String, Snapshot> getHitting();

    /**
     * 重置指定缓存模式的命中率统计
     * <p>
     * 清空指定缓存模式或分组的命中率统计数据，重新开始计数。 示例代码：
     * </p>
     * 
     * <pre>{@code
     * Metrics metrics = new SomeMetricsImpl();
     * metrics.reset("userCache");
     * }</pre>
     *
     * @param pattern 缓存模式或分组名称
     */
    void reset(String pattern);

    /**
     * 重置所有缓存模式的命中率统计
     * <p>
     * 清空所有缓存模式或分组的命中率统计数据，重新开始计数。 示例代码：
     * </p>
     * 
     * <pre>{@code
     * Metrics metrics = new SomeMetricsImpl();
     * metrics.resetAll();
     * }</pre>
     */
    void resetAll();

    /**
     * 获取汇总名称
     * <p>
     * 根据系统语言环境返回汇总名称，中文环境返回“全局”，其他环境返回“summary”。 示例代码：
     * </p>
     * 
     * <pre>{@code
     * Metrics metrics = new SomeMetricsImpl();
     * String summary = metrics.summaryName();
     * System.out.println("汇总名称: " + summary);
     * }</pre>
     *
     * @return 汇总名称
     */
    default String summaryName() {
        return "zh".equalsIgnoreCase(System.getProperty("user.language")) ? "全局" : "summary";
    }

    /**
     * 缓存命中率数据对象
     * <p>
     * 用于存储和计算缓存命中率信息，包括命中次数、请求次数和命中率百分比。
     * </p>
     */
    class Snapshot {
        /**
         * 命中次数
         */
        private final long hit;

        /**
         * 请求次数
         */
        private final long required;

        /**
         * 命中率字符串，格式为"xx.x%"
         */
        private final String rate;

        /**
         * 构造缓存命中率数据对象
         * <p>
         * 根据指定的命中次数、请求次数和命中率字符串创建数据对象。
         * </p>
         *
         * @param hit      命中次数
         * @param required 请求次数
         * @param rate     命中率字符串
         */
        private Snapshot(long hit, long required, String rate) {
            this.hit = hit;
            this.required = required;
            this.rate = rate;
        }

        /**
         * 创建缓存命中率数据对象
         * <p>
         * 根据命中次数和请求次数计算命中率，创建新的数据对象。 示例代码：
         * </p>
         * 
         * <pre>{@code
         * Snapshot snapshot = Snapshot.newInstance(50, 100);
         * System.out.println("命中率: " + snapshot.getRate());
         * }</pre>
         *
         * @param hit      命中次数
         * @param required 请求次数
         * @return 缓存命中率数据对象
         */
        public static Snapshot newInstance(long hit, long required) {
            double rate = (required == 0 ? 0.0 : hit * 100.0 / required);
            String rateStr = String.format("%.1f%s", rate, "%");
            return new Snapshot(hit, required, rateStr);
        }

        /**
         * 合并两个缓存命中率数据对象
         * <p>
         * 将两个数据对象的命中次数和请求次数相加，创建新的数据对象。 示例代码：
         * </p>
         * 
         * <pre>{@code
         * Snapshot snapshot1 = Snapshot.newInstance(50, 100);
         * Snapshot snapshot2 = Snapshot.newInstance(30, 50);
         * Snapshot merged = Snapshot.mergeShootingDO(snapshot1, snapshot2);
         * System.out.println("合并后命中率: " + merged.getRate());
         * }</pre>
         *
         * @param do1 第一个缓存命中率数据对象
         * @param do2 第二个缓存命中率数据对象
         * @return 合并后的缓存命中率数据对象
         */
        public static Snapshot mergeShootingDO(Snapshot do1, Snapshot do2) {
            long hit = do1.getHit() + do2.getHit();
            long required = do1.getRequired() + do2.getRequired();
            return newInstance(hit, required);
        }

        /**
         * 获取命中次数
         * <p>
         * 返回缓存命中次数。
         * </p>
         *
         * @return 命中次数
         */
        public long getHit() {
            return hit;
        }

        /**
         * 获取请求次数
         * <p>
         * 返回缓存请求次数。
         * </p>
         *
         * @return 请求次数
         */
        public long getRequired() {
            return required;
        }

        /**
         * 获取命中率字符串
         * <p>
         * 返回格式为“xx.x%”的命中率字符串。
         * </p>
         *
         * @return 命中率字符串
         */
        public String getRate() {
            return rate;
        }
    }

}