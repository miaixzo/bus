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
package org.miaixz.bus.cron.pattern.matcher;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * 年匹配器，用于Cron表达式中年份部分的匹配。
 * <p>
 * 考虑到年份数字通常较大，不适合使用布尔数组进行存储和匹配，因此单独使用{@link LinkedHashSet}进行匹配。
 * </p>
 *
 * <p>
 * 该类实现了{@link PartMatcher}接口，提供了年份匹配的具体实现。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class YearValueMatcher implements PartMatcher {

    /**
     * 存储年份值的集合
     */
    private final LinkedHashSet<Integer> valueList;

    /**
     * 构造一个年匹配器。
     *
     * @param intValueList 年份数字列表，不能为null
     * @throws IllegalArgumentException 如果intValueList为null
     */
    public YearValueMatcher(final Collection<Integer> intValueList) {
        if (intValueList == null) {
            throw new IllegalArgumentException("Year value list cannot be null");
        }
        this.valueList = new LinkedHashSet<>(intValueList);
    }

    /**
     * 测试给定的年份是否匹配。
     *
     * @param t 要测试的年份值
     * @return 如果给定的年份在匹配器中则返回true，否则返回false
     * @throws NullPointerException 如果参数t为null
     */
    @Override
    public boolean test(final Integer t) {
        if (t == null) {
            throw new NullPointerException("Year value to test cannot be null");
        }
        return valueList.contains(t);
    }

    /**
     * 获取大于等于给定值的下一个匹配年份。
     *
     * @param value 当前年份值
     * @return 大于等于给定值的下一个匹配年份，如果没有找到则返回-1表示无效
     */
    @Override
    public int nextAfter(final int value) {
        for (final Integer year : valueList) {
            if (year >= value) {
                return year;
            }
        }
        // 没有找到大于等于当前值的年份，年无效，此表达式整体无效
        return -1;
    }

    /**
     * 返回此年匹配器的字符串表示形式。
     *
     * @return 年匹配器的字符串表示形式
     */
    @Override
    public String toString() {
        return "YearValueMatcher{" + "valueList=" + valueList + '}';
    }

}