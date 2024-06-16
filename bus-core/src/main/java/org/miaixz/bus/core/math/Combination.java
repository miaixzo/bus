/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.math;

import org.miaixz.bus.core.xyz.MathKit;
import org.miaixz.bus.core.xyz.StringKit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 组合，即C(n, m)
 * 排列组合相关类 参考：<a href="http://cgs1999.iteye.com/blog/2327664">http://cgs1999.iteye.com/blog/2327664</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Combination implements Serializable {

    private static final long serialVersionUID = -1L;

    private final String[] datas;

    /**
     * 组合，即C(n, m)
     * 排列组合相关类 参考：<a href="http://cgs1999.iteye.com/blog/2327664">http://cgs1999.iteye.com/blog/2327664</a>
     *
     * @param datas 用于组合的数据
     */
    public Combination(final String[] datas) {
        this.datas = datas;
    }

    /**
     * 计算组合数，即C(n, m) = n!/((n-m)!* m!)
     *
     * @param n 总数
     * @param m 选择的个数
     * @return 组合数
     */
    public static long count(final int n, final int m) {
        if (0 == m || n == m) {
            return 1;
        }
        return (n > m) ? MathKit.factorial(n, n - m) / MathKit.factorial(m) : 0;
    }

    /**
     * 计算组合总数，即C(n, 1) + C(n, 2) + C(n, 3)...
     *
     * @param n 总数
     * @return 组合数
     */
    public static long countAll(final int n) {
        if (n < 0 || n > 63) {
            throw new IllegalArgumentException(StringKit.format("countAll must have n >= 0 and n <= 63, but got n={}", n));
        }
        return n == 63 ? Long.MAX_VALUE : (1L << n) - 1;
    }

    /**
     * 组合选择（从列表中选择m个组合）
     *
     * @param m 选择个数
     * @return 组合结果
     */
    public List<String[]> select(final int m) {
        final List<String[]> result = new ArrayList<>((int) count(this.datas.length, m));
        select(0, new String[m], 0, result);
        return result;
    }

    /**
     * 全组合
     *
     * @return 全排列结果
     */
    public List<String[]> selectAll() {
        final List<String[]> result = new ArrayList<>((int) countAll(this.datas.length));
        for (int i = 1; i <= this.datas.length; i++) {
            result.addAll(select(i));
        }
        return result;
    }

    /**
     * 组合选择
     *
     * @param dataIndex   待选开始索引
     * @param resultList  前面（resultIndex-1）个的组合结果
     * @param resultIndex 选择索引，从0开始
     * @param result      结果集
     */
    private void select(final int dataIndex, final String[] resultList, final int resultIndex, final List<String[]> result) {
        final int resultLen = resultList.length;
        final int resultCount = resultIndex + 1;
        if (resultCount > resultLen) { // 全部选择完时，输出组合结果
            result.add(Arrays.copyOf(resultList, resultList.length));
            return;
        }

        // 递归选择下一个
        for (int i = dataIndex; i < datas.length + resultCount - resultLen; i++) {
            resultList[resultIndex] = datas[i];
            select(i + 1, resultList, resultIndex + 1, result);
        }
    }

}
