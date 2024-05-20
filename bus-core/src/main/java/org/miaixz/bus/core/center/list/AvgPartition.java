/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.core.center.list;

import org.miaixz.bus.core.lang.Assert;

import java.util.List;

/**
 * 列表分区或分段
 * 通过传入分区个数，将指定列表分区为不同的块，每块区域的长度均匀分布（个数差不超过1）
 * <pre>
 *     [1,2,3,4] - [1,2], [3, 4]
 *     [1,2,3,4] - [1,2], [3], [4]
 *     [1,2,3,4] - [1], [2], [3], [4]
 *     [1,2,3,4] - [1], [2], [3], [4], []
 * </pre>
 * 分区是在原List的基础上进行的，返回的分区是不可变的抽象列表，原列表元素变更，分区中元素也会变更。
 *
 * @param <T> 元素类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class AvgPartition<T> extends Partition<T> {

    final int limit;
    /**
     * 平均分完后剩余的个数，平均放在前remainder个分区中
     */
    final int remainder;

    /**
     * 列表分区
     *
     * @param list  被分区的列表
     * @param limit 分区个数
     */
    public AvgPartition(final List<T> list, final int limit) {
        super(list, list.size() / (limit <= 0 ? 1 : limit));
        Assert.isTrue(limit > 0, "Partition limit must be > 0");
        this.limit = limit;
        this.remainder = list.size() % limit;
    }

    @Override
    public List<T> get(final int index) {
        final int size = this.size;
        final int remainder = this.remainder;
        // 当limit个数超过list的size时，size为0，此时每个分区分1个元素，直到remainder个分配完，剩余分区为[]
        final int start = index * size + Math.min(index, remainder);
        int end = start + size;
        if (index + 1 <= remainder) {
            // 将remainder个元素平均分布在前面，每个分区分1个
            end += 1;
        }
        return list.subList(start, end);
    }

    @Override
    public int size() {
        return limit;
    }

}
