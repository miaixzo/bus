/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.cron.pattern.matcher;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.CollUtils;
import org.aoju.bus.core.utils.NumberUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.cron.pattern.parser.DayOfMonthValueParser;
import org.aoju.bus.cron.pattern.parser.ValueParser;
import org.aoju.bus.cron.pattern.parser.YearValueParser;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link ValueMatcher} 构建器,用于构建表达式中每一项的匹配器
 *
 * @author Kimi Liu
 * @version 5.3.2
 * @since JDK 1.8+
 */
public class ValueMatcherBuilder {

    /**
     * 处理定时任务表达式每个时间字段
     * 多个时间使用逗号分隔
     *
     * @param value  某个时间字段
     * @param parser 针对这个时间字段的解析器
     * @return List
     */
    public static ValueMatcher build(String value, ValueParser parser) {
        if (isMatchAllStr(value)) {
            //兼容Quartz的"?"表达式,不会出现互斥情况,与"*"作用相同
            return new AlwaysTrueValueMatcher();
        }

        List<Integer> values = parseArray(value, parser);
        if (values.size() == 0) {
            throw new InstrumentException("Invalid field: [{}]", value);
        }

        if (parser instanceof DayOfMonthValueParser) {
            //考虑每月的天数不同,且存在闰年情况,日匹配单独使用
            return new DayOfMonthValueMatcher(values);
        } else if (parser instanceof YearValueParser) {
            //考虑年数字太大,不适合boolean数组,单独使用列表遍历匹配
            return new YearValueMatcher(values);
        } else {
            return new BoolArrayValueMatcher(values);
        }
    }

    /**
     * 处理数组形式表达式
     * 处理的形式包括：
     * <ul>
     * <li><strong>a</strong> 或 <strong>*</strong></li>
     * <li><strong>a,b,c,d</strong></li>
     * </ul>
     *
     * @param value  子表达式值
     * @param parser 针对这个字段的解析器
     * @return 值列表
     */
    private static List<Integer> parseArray(String value, ValueParser parser) {
        final List<Integer> values = new ArrayList<>();

        final List<String> parts = StringUtils.split(value, Symbol.C_COMMA);
        for (String part : parts) {
            CollUtils.addAllIfNotContains(values, parseStep(part, parser));
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
     * @param value  表达式值
     * @param parser 针对这个时间字段的解析器
     * @return List
     */
    private static List<Integer> parseStep(String value, ValueParser parser) {
        final List<String> parts = StringUtils.split(value, Symbol.C_SLASH);
        int size = parts.size();

        List<Integer> results;
        if (size == 1) {// 普通形式
            results = parseRange(value, -1, parser);
        } else if (size == 2) {// 间隔形式
            final int step = parser.parse(parts.get(1));
            if (step < 1) {
                throw new InstrumentException("Non positive divisor for field: [{}]", value);
            }
            results = parseRange(parts.get(0), step, parser);
        } else {
            throw new InstrumentException("Invalid syntax of field: [{}]", value);
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
     * @param value  范围表达式
     * @param step   步进
     * @param parser 针对这个时间字段的解析器
     * @return List
     */
    private static List<Integer> parseRange(String value, int step, ValueParser parser) {
        final List<Integer> results = new ArrayList<>();

        // 全部匹配形式
        if (value.length() <= 2) {
            //根据步进的第一个数字确定起始时间,类似于 12/3则从12（秒、分等）开始
            int minValue = parser.getMin();
            if (false == isMatchAllStr(value)) {
                try {
                    minValue = Math.max(minValue, Integer.parseInt(value));
                } catch (NumberFormatException e) {
                    throw new InstrumentException("Invalid field value: [{}]", value);
                }
            } else {
                //在全匹配模式下,如果步进不存在,表示步进为1
                if (step < 1) {
                    step = 1;
                }
            }
            if (step > 0) {
                final int maxValue = parser.getMax();
                if (minValue > maxValue) {
                    throw new InstrumentException("Invalid value {} > {}", minValue, maxValue);
                }
                //有步进
                for (int i = minValue; i <= maxValue; i += step) {
                    results.add(i);
                }
            } else {
                //固定时间
                results.add(minValue);
            }
            return results;
        }

        //Range模式
        List<String> parts = StringUtils.split(value, '-');
        int size = parts.size();
        if (size == 1) {// 普通值
            final int v1 = parser.parse(value);
            if (step > 0) {//类似 20/2的形式
                NumberUtils.appendRange(v1, parser.getMax(), step, results);
            } else {
                results.add(v1);
            }
        } else if (size == 2) {// range值
            final int v1 = parser.parse(parts.get(0));
            final int v2 = parser.parse(parts.get(1));
            if (step < 1) {
                //在range模式下,如果步进不存在,表示步进为1
                step = 1;
            }
            if (v1 < v2) {// 正常范围,例如：2-5
                NumberUtils.appendRange(v1, v2, step, results);
            } else if (v1 > v2) {// 逆向范围,反选模式,例如：5-2
                NumberUtils.appendRange(v1, parser.getMax(), step, results);
                NumberUtils.appendRange(parser.getMin(), v2, step, results);
            } else {// v1 == v2,此时与单值模式一致
                if (step > 0) {//类似 20/2的形式
                    NumberUtils.appendRange(v1, parser.getMax(), step, results);
                } else {
                    results.add(v1);
                }
            }
        } else {
            throw new InstrumentException("Invalid syntax of field: [{}]", value);
        }
        return results;
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
}
