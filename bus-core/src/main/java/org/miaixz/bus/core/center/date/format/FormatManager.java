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
package org.miaixz.bus.core.center.date.format;

import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.xyz.DateKit;

/**
 * 全局自定义日期格式管理器，用于定义用户指定的日期格式与格式化/解析规则的映射。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FormatManager {

    /**
     * 日期格式化规则映射
     */
    private final Map<CharSequence, Function<Date, String>> formatterMap;
    /**
     * 日期解析规则映射
     */
    private final Map<CharSequence, Function<CharSequence, Date>> parserMap;

    /**
     * 构造函数，初始化预设的格式化与解析规则。
     */
    public FormatManager() {
        formatterMap = new ConcurrentHashMap<>();
        parserMap = new ConcurrentHashMap<>();

        // 预设格式：秒时间戳
        registerFormatter(Fields.FORMAT_SECONDS, (date) -> String.valueOf(Math.floorDiv(date.getTime(), 1000L)));
        registerParser(Fields.FORMAT_SECONDS,
                (dateStr) -> DateKit.date(Math.multiplyExact(Long.parseLong(dateStr.toString()), 1000L)));

        // 预设格式：毫秒时间戳
        registerFormatter(Fields.FORMAT_MILLISECONDS, (date) -> String.valueOf(date.getTime()));
        registerParser(Fields.FORMAT_MILLISECONDS, (dateStr) -> DateKit.date(Long.parseLong(dateStr.toString())));
    }

    /**
     * 获取FormatManager的单例实例。
     *
     * @return FormatManager单例
     */
    public static FormatManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 注册日期格式化规则。
     *
     * @param format 格式标识
     * @param func   格式化函数
     * @return 当前FormatManager实例
     * @throws IllegalArgumentException 如果format或func为null
     */
    public FormatManager registerFormatter(final String format, final Function<Date, String> func) {
        Assert.notNull(format, "Format must be not null !");
        Assert.notNull(func, "Function must be not null !");
        formatterMap.put(format, func);
        return this;
    }

    /**
     * 注册日期解析规则。
     *
     * @param format 格式标识
     * @param func   解析函数
     * @return 当前FormatManager实例
     * @throws IllegalArgumentException 如果format或func为null
     */
    public FormatManager registerParser(final String format, final Function<CharSequence, Date> func) {
        Assert.notNull(format, "Format must be not null !");
        Assert.notNull(func, "Function must be not null !");
        parserMap.put(format, func);
        return this;
    }

    /**
     * 检查是否为自定义格式化规则。
     *
     * @param format 格式标识
     * @return 是否为自定义格式化规则
     */
    public boolean isCustomFormat(final String format) {
        return formatterMap != null && formatterMap.containsKey(format);
    }

    /**
     * 检查是否为自定义解析规则。
     *
     * @param format 格式标识
     * @return 是否为自定义解析规则
     */
    public boolean isCustomParse(final String format) {
        return parserMap != null && parserMap.containsKey(format);
    }

    /**
     * 使用自定义格式格式化日期。
     *
     * @param date   日期对象
     * @param format 自定义格式标识
     * @return 格式化后的字符串，若无对应规则返回null
     */
    public String format(final Date date, final CharSequence format) {
        if (formatterMap != null) {
            final Function<Date, String> func = formatterMap.get(format);
            if (func != null) {
                return func.apply(date);
            }
        }
        return null;
    }

    /**
     * 使用自定义格式格式化时间对象。
     *
     * @param temporalAccessor 时间对象
     * @param format           自定义格式标识
     * @return 格式化后的字符串，若无对应规则返回null
     */
    public String format(final TemporalAccessor temporalAccessor, final CharSequence format) {
        return format(DateKit.date(temporalAccessor), format);
    }

    /**
     * 使用自定义格式解析日期字符串。
     *
     * @param date   日期字符串
     * @param format 自定义格式标识
     * @return 解析后的日期对象，若无对应规则返回null
     */
    public Date parse(final CharSequence date, final String format) {
        if (parserMap != null) {
            final Function<CharSequence, Date> func = parserMap.get(format);
            if (func != null) {
                return func.apply(date);
            }
        }
        return null;
    }

    /**
     * 单例持有类，实现延迟加载。
     */
    private static class SingletonHolder {
        /** 静态单例实例，由JVM保证线程安全 */
        private static final FormatManager INSTANCE = new FormatManager();
    }

}