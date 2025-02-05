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
package org.miaixz.bus.core.convert;

import java.lang.reflect.Type;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;

import org.miaixz.bus.core.center.date.DateTime;
import org.miaixz.bus.core.center.date.Resolver;
import org.miaixz.bus.core.lang.exception.ConvertException;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 日期转换器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DateConverter extends AbstractConverter implements MatcherConverter {

    /**
     * 单例
     */
    public static final DateConverter INSTANCE = new DateConverter();
    private static final long serialVersionUID = -1L;
    /**
     * 日期格式化
     */
    private String format;

    /**
     * 构造
     */
    public DateConverter() {
        this(null);
    }

    /**
     * 构造
     *
     * @param format 日期格式，{@code null}表示无格式定义
     */
    public DateConverter(final String format) {
        this.format = format;
    }

    /**
     * 获取日期格式
     *
     * @return 设置日期格式
     */
    public String getFormat() {
        return format;
    }

    /**
     * 设置日期格式
     *
     * @param format 日期格式
     */
    public void setFormat(final String format) {
        this.format = format;
    }

    @Override
    public boolean match(final Type targetType, final Class<?> rawType, final Object value) {
        return Date.class.isAssignableFrom(rawType);
    }

    @Override
    protected java.util.Date convertInternal(final Class<?> targetClass, final Object value) {
        if (value == null || (value instanceof CharSequence && StringKit.isBlank(value.toString()))) {
            return null;
        }
        if (value instanceof TemporalAccessor) {
            return wrap(targetClass, DateKit.date((TemporalAccessor) value));
        } else if (value instanceof Calendar) {
            return wrap(targetClass, DateKit.date((Calendar) value));
        } else if (null == this.format && value instanceof Number) {
            return wrap(targetClass, ((Number) value).longValue());
        } else {
            // 统一按照字符串处理
            final String values = convertToString(value);
            final Date date = StringKit.isBlank(this.format) //
                    ? Resolver.parse(values) //
                    : Resolver.parse(values, this.format);
            if (null != date) {
                return wrap(targetClass, date);
            }
        }

        throw new ConvertException("Can not support {}:[{}] to {}", value.getClass().getName(), value,
                targetClass.getName());
    }

    /**
     * java.util.Date转为子类型
     *
     * @param date Date
     * @return 目标类型对象
     */
    private java.util.Date wrap(final Class<?> targetClass, final Date date) {
        if (targetClass == date.getClass()) {
            return date;
        }

        return wrap(targetClass, date.getTime());
    }

    /**
     * 时间戳转为子类型，支持：
     * <ul>
     * <li>{@link java.util.Date}</li>
     * <li>{@link DateTime}</li>
     * <li>{@link java.sql.Date}</li>
     * <li>{@link java.sql.Time}</li>
     * <li>{@link java.sql.Timestamp}</li>
     * </ul>
     *
     * @param mills Date
     * @return 目标类型对象
     */
    private java.util.Date wrap(final Class<?> targetClass, final long mills) {
        // 返回指定类型
        if (java.util.Date.class == targetClass) {
            return new java.util.Date(mills);
        }
        if (DateTime.class == targetClass) {
            return DateKit.date(mills);
        }

        final String dateClassName = targetClass.getName();
        if (dateClassName.startsWith("java.sql.")) {
            // 为了解决在JDK9+模块化项目中用户没有引入java.sql模块导致的问题，此处增加判断
            // 如果targetClass是java.sql的类，说明引入了此模块
            return DateKit.SQL.wrap(targetClass, mills);
        }

        throw new ConvertException("Unsupported target Date type: {}", targetClass.getName());
    }

}
