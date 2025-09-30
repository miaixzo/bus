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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 统计模型持有的子模块，用于构建具有层级结构的指标体系。
 * <p>
 * 该类继承自BaseMetrics，并添加了对子指标的管理功能。 通过使用线程安全的CopyOnWriteArrayList存储子指标，确保在多线程环境下的安全性。 这种设计允许构建树状结构的指标体系，便于组织和展示复杂的监控数据。
 * </p>
 *
 * @param <T> 子指标的类型，必须继承自BaseMetrics
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class ChildrenMetrics<T extends BaseMetrics> extends BaseMetrics {

    /**
     * 子指标列表，使用线程安全的CopyOnWriteArrayList实现
     * <p>
     * CopyOnWriteArrayList适用于读多写少的并发场景，提供线程安全的同时保证读取性能
     * </p>
     */
    private List<T> children = new CopyOnWriteArrayList<>();

    /**
     * 添加子指标
     * <p>
     * 将指定的子指标添加到children列表中
     * </p>
     *
     * @param child 要添加的子指标，不能为null
     */
    public void addChild(T child) {
        this.children.add(child);
    }

}
