/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
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
package org.aoju.bus.cron.pattern.parser;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.CrontabException;
import org.aoju.bus.core.toolkit.CollKit;
import org.aoju.bus.core.toolkit.MathKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.cron.pattern.matcher.AlwaysTrueValueMatcher;
import org.aoju.bus.cron.pattern.matcher.BoolArrayValueMatcher;
import org.aoju.bus.cron.pattern.matcher.ValueMatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * 简易值转换器将给定String值转为int，并限定最大值和最小值
 * 此类同时识别{@code L} 为最大值
 *
 * @author Kimi Liu
 * @version 6.5.0
 * @since Java 17+
 */
public abstract class AbstractValueParser implements ValueParser {

    /**
     * 最小值（包括）
     */
    protected int min;
    /**
     * 最大值（包括）
     */
    protected int max;

    /**
     * 构造
     *
     * @param min 最小值（包括）
     * @param max 最大值（包括）
     */
    public AbstractValueParser(int min, int max) {
        if (min > max) {
            this.min = max;
            this.max = min;
        } else {
            this.min = min;
            this.max = max;
        }
    }

    /**
     * 是否为全匹配符
     * 全匹配符指 * 或者 ?
     *
     * @param value 被检查的值
     * @return 是否为全匹配符
     */
    private static boolean isMatchAllStr(String value) {
        return (1 == value.length()) && ("*".equals(value) || "?".equals(value));
    }

    @Override
    public int parse(String value) throws CrontabException {
        if ("L".equalsIgnoreCase(value)) {
            // L表示最大值
            return max;
        }

        int i;
        try {
            i = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new CrontabException("Invalid integer value: '{}'", value);
        }
        if (i < min || i > max) {
            throw new CrontabException("Value {} out of range: [{} , {}]", i, min, max);
        }
        return i;
    }

    @Override
    public int getMin() {
        return this.min;
    }

    @Override
    public int getMax() {
        return this.max;
    }

    /**
     * 处理定时任务表达式每个时间字段
     * 多个时间使用逗号分隔
     *
     * @param value 某个时间字段
     * @return the list
     */
    @Override
    public ValueMatcher parseAsValueMatcher(String value) {
        if (isMatchAllStr(value)) {
            // 兼容Quartz的"?"表达式，不会出现互斥情况，与"*"作用相同
            return new AlwaysTrueValueMatcher();
        }

        List<Integer> values = parseArray(value);
        if (values.size() == 0) {
            throw new CrontabException("Invalid field: [{}]", value);
        }

        return buildValueMatcher(values);
    }

    /**
     * 根据解析的数字值列表构建{@link ValueMatcher}
     * 默认为{@link BoolArrayValueMatcher}，如果有特殊实现，子类须重写此方法
     *
     * @param values 数字值列表
     * @return {@link ValueMatcher}
     */
    protected ValueMatcher buildValueMatcher(List<Integer> values) {
        return new BoolArrayValueMatcher(values);
    }

    /**
     * 处理数组形式表达式
     * 处理的形式包括：
     * <ul>
     * <li><strong>a</strong> 或 <strong>*</strong></li>
     * <li><strong>a,b,c,d</strong></li>
     * </ul>
     *
     * @param value 子表达式值
     * @return 值列表
     */
    private List<Integer> parseArray(String value) {
        final List<Integer> values = new ArrayList<>();

        final List<String> parts = StringKit.split(value, Symbol.C_COMMA);
        for (String part : parts) {
            CollKit.addAllIfNotContains(values, parseStep(part));
        }
        return values;
    }

    /**
     * 处理间隔形式的表达式
     * 处理的形式包括：
     * <ul>
     * <li><strong>a</strong> 或 <strong>*</strong></li>
     * <li><strong>a&#47;b</strong> 或 <strong>*&#47;b</strong></li>
     * <li><strong>a-b/2</strong></li>
     * </ul>
     *
     * @param value 表达式值
     * @return List
     */
    private List<Integer> parseStep(String value) {
        final List<String> parts = StringKit.split(value, Symbol.C_SLASH);
        int size = parts.size();

        List<Integer> results;
        if (size == 1) {// 普通形式
            results = parseRange(value, -1);
        } else if (size == 2) {// 间隔形式
            final int step = parse(parts.get(1));
            if (step < 1) {
                throw new CrontabException("Non positive divisor for field: [{}]", value);
            }
            results = parseRange(parts.get(0), step);
        } else {
            throw new CrontabException("Invalid syntax of field: [{}]", value);
        }
        return results;
    }

    /**
     * 处理表达式中范围表达式 处理的形式包括：
     * <ul>
     * <li>*</li>
     * <li>2</li>
     * <li>3-8</li>
     * <li>8-3</li>
     * <li>3-3</li>
     * </ul>
     *
     * @param value 范围表达式
     * @param step  步进
     * @return List
     */
    private List<Integer> parseRange(String value, int step) {
        final List<Integer> results = new ArrayList<>();

        // 全部匹配形式
        if (value.length() <= 2) {
            // 根据步进的第一个数字确定起始时间，类似于 12/3则从12（秒、分等）开始
            int minValue = getMin();
            if (false == isMatchAllStr(value)) {
                minValue = Math.max(minValue, parse(value));
            } else {
                // 在全匹配模式下，如果步进不存在，表示步进为1
                if (step < 1) {
                    step = 1;
                }
            }
            if (step > 0) {
                final int maxValue = getMax();
                if (minValue > maxValue) {
                    throw new CrontabException("Invalid value {} > {}", minValue, maxValue);
                }
                // 有步进
                for (int i = minValue; i <= maxValue; i += step) {
                    results.add(i);
                }
            } else {
                // 固定时间
                results.add(minValue);
            }
            return results;
        }

        // Range模式
        List<String> parts = StringKit.split(value, '-');
        int size = parts.size();
        if (size == 1) {// 普通值
            final int v1 = parse(value);
            if (step > 0) {// 类似 20/2的形式
                MathKit.appendRange(v1, getMax(), step, results);
            } else {
                results.add(v1);
            }
        } else if (size == 2) {// range值
            final int v1 = parse(parts.get(0));
            final int v2 = parse(parts.get(1));
            if (step < 1) {
                // 在range模式下，如果步进不存在，表示步进为1
                step = 1;
            }
            if (v1 < v2) {// 正常范围，例如：2-5
                MathKit.appendRange(v1, v2, step, results);
            } else if (v1 > v2) {// 逆向范围，反选模式，例如：5-2
                MathKit.appendRange(v1, getMax(), step, results);
                MathKit.appendRange(getMin(), v2, step, results);
            } else {// v1 == v2，此时与单值模式一致
                MathKit.appendRange(v1, getMax(), step, results);
            }
        } else {
            throw new CrontabException("Invalid syntax of field: [{}]", value);
        }
        return results;
    }

}
