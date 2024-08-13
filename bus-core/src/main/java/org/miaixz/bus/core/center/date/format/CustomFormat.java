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
package org.miaixz.bus.core.center.date.format;

import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import org.miaixz.bus.core.center.map.concurrent.SafeConcurrentHashMap;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.DateKit;

/**
 * 全局自定义格式 用于定义用户指定的日期格式和输出日期的关系
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CustomFormat {

    /**
     * 格式：秒时间戳（Unix时间戳）
     */
    public static final String FORMAT_SECONDS = "#sss";
    /**
     * 格式：毫秒时间戳
     */
    public static final String FORMAT_MILLISECONDS = "#SSS";

    private static final Map<CharSequence, Function<Date, String>> formatterMap;
    private static final Map<CharSequence, Function<CharSequence, Date>> parserMap;

    static {
        formatterMap = new SafeConcurrentHashMap<>();
        parserMap = new SafeConcurrentHashMap<>();

        // 预设的几种自定义格式
        putFormatter(FORMAT_SECONDS, (date) -> String.valueOf(Math.floorDiv(date.getTime(), 1000L)));
        putParser(FORMAT_SECONDS, (date) -> DateKit.date(Math.multiplyExact(Long.parseLong(date.toString()), 1000L)));

        putFormatter(FORMAT_MILLISECONDS, (date) -> String.valueOf(date.getTime()));
        putParser(FORMAT_MILLISECONDS, (date) -> DateKit.date(Long.parseLong(date.toString())));
    }

    /**
     * 加入日期格式化规则
     *
     * @param format 格式
     * @param func   格式化函数
     */
    public static void putFormatter(final String format, final Function<Date, String> func) {
        Assert.notNull(format, "Format must be not null !");
        Assert.notNull(func, "Function must be not null !");
        formatterMap.put(format, func);
    }

    /**
     * 加入日期解析规则
     *
     * @param format 格式
     * @param func   解析函数
     */
    public static void putParser(final String format, final Function<CharSequence, Date> func) {
        Assert.notNull(format, "Format must be not null !");
        Assert.notNull(func, "Function must be not null !");
        parserMap.put(format, func);
    }

    /**
     * 检查指定格式是否为自定义格式
     *
     * @param format 格式
     * @return 是否为自定义格式
     */
    public static boolean isCustomFormat(final String format) {
        return formatterMap.containsKey(format);
    }

    /**
     * 使用自定义格式格式化日期
     *
     * @param date   日期
     * @param format 自定义格式
     * @return 格式化后的日期
     */
    public static String format(final Date date, final CharSequence format) {
        if (null != formatterMap) {
            final Function<Date, String> func = formatterMap.get(format);
            if (null != func) {
                return func.apply(date);
            }
        }

        return null;
    }

    /**
     * 使用自定义格式格式化日期
     *
     * @param temporalAccessor 日期
     * @param format           自定义格式
     * @return 格式化后的日期
     */
    public static String format(final TemporalAccessor temporalAccessor, final CharSequence format) {
        return format(DateKit.date(temporalAccessor), format);
    }

    /**
     * 使用自定义格式解析日期
     *
     * @param date   日期字符串
     * @param format 自定义格式
     * @return 格式化后的日期
     */
    public static Date parse(final CharSequence date, final String format) {
        if (null != parserMap) {
            final Function<CharSequence, Date> func = parserMap.get(format);
            if (null != func) {
                return func.apply(date);
            }
        }

        return null;
    }

}
