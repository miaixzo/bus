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
import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.xyz.DateKit;

/**
 * 全局自定义格式 用于定义用户指定的日期格式和输出日期的关系
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FormatManager {

    private final Map<CharSequence, Function<Date, String>> formatterMap;
    private final Map<CharSequence, Function<CharSequence, Date>> parserMap;

    /**
     * 构造
     */
    public FormatManager() {
        formatterMap = new SafeConcurrentHashMap<>();
        parserMap = new SafeConcurrentHashMap<>();

        // 预设的几种自定义格式
        registerFormatter(Fields.FORMAT_SECONDS, (date) -> String.valueOf(Math.floorDiv(date.getTime(), 1000L)));
        registerParser(Fields.FORMAT_SECONDS,
                (dateStr) -> DateKit.date(Math.multiplyExact(Long.parseLong(dateStr.toString()), 1000L)));

        registerFormatter(Fields.FORMAT_MILLISECONDS, (date) -> String.valueOf(date.getTime()));
        registerParser(Fields.FORMAT_MILLISECONDS, (dateStr) -> DateKit.date(Long.parseLong(dateStr.toString())));
    }

    /**
     * 获得单例的 DateFormatManager
     *
     * @return DateFormatManager
     */
    public static FormatManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 加入日期格式化规则
     *
     * @param format 格式
     * @param func   格式化函数
     * @return this
     */
    public FormatManager registerFormatter(final String format, final Function<Date, String> func) {
        Assert.notNull(format, "Format must be not null !");
        Assert.notNull(func, "Function must be not null !");
        formatterMap.put(format, func);
        return this;
    }

    /**
     * 加入日期解析规则
     *
     * @param format 格式
     * @param func   解析函数
     * @return this
     */
    public FormatManager registerParser(final String format, final Function<CharSequence, Date> func) {
        Assert.notNull(format, "Format must be not null !");
        Assert.notNull(func, "Function must be not null !");
        parserMap.put(format, func);
        return this;
    }

    /**
     * 检查指定格式是否为自定义格式
     *
     * @param format 格式
     * @return 是否为自定义格式
     */
    public boolean isCustomFormat(final String format) {
        return null != formatterMap && formatterMap.containsKey(format);
    }

    /**
     * 检查指定格式是否为自定义格式
     *
     * @param format 格式
     * @return 是否为自定义格式
     */
    public boolean isCustomParse(final String format) {
        return null != parserMap && parserMap.containsKey(format);
    }

    /**
     * 使用自定义格式格式化日期
     *
     * @param date   日期
     * @param format 自定义格式
     * @return 格式化后的日期
     */
    public String format(final Date date, final CharSequence format) {
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
    public String format(final TemporalAccessor temporalAccessor, final CharSequence format) {
        return format(DateKit.date(temporalAccessor), format);
    }

    /**
     * 使用自定义格式解析日期
     *
     * @param date   日期字符串
     * @param format 自定义格式
     * @return 格式化后的日期
     */
    public Date parse(final CharSequence date, final String format) {
        if (null != parserMap) {
            final Function<CharSequence, Date> func = parserMap.get(format);
            if (null != func) {
                return func.apply(date);
            }
        }
        return null;
    }

    /**
     * 类级的内部类，也就是静态的成员式内部类，该内部类的实例与外部类的实例 没有绑定关系，而且只有被调用到才会装载，从而实现了延迟加载
     */
    private static class SingletonHolder {
        /**
         * 静态初始化器，由JVM来保证线程安全
         */
        private static final FormatManager INSTANCE = new FormatManager();
    }

}
