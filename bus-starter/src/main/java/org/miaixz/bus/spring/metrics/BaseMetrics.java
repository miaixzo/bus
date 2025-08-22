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
package org.miaixz.bus.spring.metrics;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * 启动状态的基本模型，用于跟踪和记录启动过程中的各项指标。
 * <p>
 * 该类提供了记录启动时间、结束时间、耗时以及自定义属性的功能， 可用于监控和分析系统启动性能。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class BaseMetrics {

    /**
     * 用于存储自定义属性的键值对集合
     */
    private final Map<String, String> attributes = new HashMap<>();

    /**
     * 指标名称
     */
    private String name;

    /**
     * 开始时间（毫秒）
     */
    private long startTime;

    /**
     * 结束时间（毫秒）
     */
    private long endTime;

    /**
     * 耗时（毫秒），通过结束时间减去开始时间计算得出
     */
    private long cost;

    /**
     * 设置结束时间并自动计算耗时
     * <p>
     * 设置结束时间的同时，会自动计算并更新cost字段的值（endTime - startTime）
     * </p>
     *
     * @param endTime 结束时间（毫秒）
     */
    public void setEndTime(long endTime) {
        this.endTime = endTime;
        this.cost = this.endTime - this.startTime;
    }

    /**
     * 添加自定义属性
     * <p>
     * 将指定的键值对添加到attributes集合中
     * </p>
     *
     * @param key   属性键
     * @param value 属性值
     */
    public void putAttribute(String key, String value) {
        this.attributes.put(key, value);
    }

    /**
     * 获取指定键的属性值
     * <p>
     * 从attributes集合中获取指定键对应的值
     * </p>
     *
     * @param key 属性键
     * @return 对应的属性值，如果不存在则返回null
     */
    public String getAttribute(String key) {
        return this.attributes.get(key);
    }
}