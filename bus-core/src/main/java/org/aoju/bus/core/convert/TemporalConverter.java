/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.core.convert;

import org.aoju.bus.core.date.DateTime;
import org.aoju.bus.core.toolkit.DateKit;
import org.aoju.bus.core.toolkit.ObjectKit;
import org.aoju.bus.core.toolkit.StringKit;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/**
 * JDK8中新加入的java.time包对象解析转换器
 * 支持的对象包括：
 *
 * <pre>
 * java.time.Instant
 * java.time.LocalDateTime
 * java.time.LocalDate
 * java.time.LocalTime
 * java.time.ZonedDateTime
 * java.time.OffsetDateTime
 * java.time.OffsetTime
 * </pre>
 *
 * @author Kimi Liu
 * @version 6.0.6
 * @since JDK 1.8+
 */
public class TemporalConverter extends AbstractConverter<TemporalAccessor> {

    private final Class<?> targetType;
    /**
     * 日期格式化
     */
    private String format;

    /**
     * 构造
     *
     * @param targetType 目标类型
     */
    public TemporalConverter(Class<?> targetType) {
        this(targetType, null);
    }

    /**
     * 构造
     *
     * @param targetType 目标类型
     * @param format     日期格式
     */
    public TemporalConverter(Class<?> targetType, String format) {
        this.targetType = targetType;
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
    public void setFormat(String format) {
        this.format = format;
    }

    @Override
    protected TemporalAccessor convertInternal(Object value) {
        if (value instanceof Long) {
            return parseFromLong((Long) value);
        } else if (value instanceof TemporalAccessor) {
            return parseFromTemporalAccessor((TemporalAccessor) value);
        } else if (value instanceof Date) {
            final DateTime dateTime = DateKit.date((Date) value);
            return parseFromInstant(dateTime.toInstant(), dateTime.getZoneId());
        } else if (value instanceof Calendar) {
            final Calendar calendar = (Calendar) value;
            return parseFromInstant(calendar.toInstant(), calendar.getTimeZone().toZoneId());
        } else {
            return parseFromCharSequence(convertToStr(value));
        }
    }

    /**
     * 通过反射从字符串转java.time中的对象
     *
     * @param value 字符串值
     * @return 日期对象
     */
    private TemporalAccessor parseFromCharSequence(CharSequence value) {
        if (StringKit.isBlank(value)) {
            return null;
        }
        final Instant instant;
        ZoneId zoneId;
        if (null != this.format) {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(this.format);
            instant = formatter.parse(value, Instant::from);
            zoneId = formatter.getZone();
        } else {
            final DateTime dateTime = DateKit.parse(value);
            instant = Objects.requireNonNull(dateTime).toInstant();
            zoneId = dateTime.getZoneId();
        }
        return parseFromInstant(instant, zoneId);
    }

    /**
     * 将Long型时间戳转换为java.time中的对象
     *
     * @param time 时间戳
     * @return java.time中的对象
     */
    private TemporalAccessor parseFromLong(Long time) {
        return parseFromInstant(Instant.ofEpochMilli(time), null);
    }

    /**
     * 将TemporalAccessor型时间戳转换为java.time中的对象
     *
     * @param temporalAccessor TemporalAccessor对象
     * @return java.time中的对象
     */
    private TemporalAccessor parseFromTemporalAccessor(TemporalAccessor temporalAccessor) {
        TemporalAccessor result = null;
        if (temporalAccessor instanceof LocalDateTime) {
            result = parseFromLocalDateTime((LocalDateTime) temporalAccessor);
        } else if (temporalAccessor instanceof ZonedDateTime) {
            result = parseFromZonedDateTime((ZonedDateTime) temporalAccessor);
        }

        if (null == result) {
            result = parseFromInstant(DateKit.toInstant(temporalAccessor), null);
        }

        return result;
    }

    /**
     * 将TemporalAccessor型时间戳转换为java.time中的对象
     *
     * @param localDateTime {@link LocalDateTime}对象
     * @return java.time中的对象
     */
    private TemporalAccessor parseFromLocalDateTime(LocalDateTime localDateTime) {
        if (Instant.class.equals(this.targetType)) {
            return DateKit.toInstant(localDateTime);
        }
        if (LocalDate.class.equals(this.targetType)) {
            return localDateTime.toLocalDate();
        }
        if (LocalTime.class.equals(this.targetType)) {
            return localDateTime.toLocalTime();
        }
        if (ZonedDateTime.class.equals(this.targetType)) {
            return localDateTime.atZone(ZoneId.systemDefault());
        }
        if (OffsetDateTime.class.equals(this.targetType)) {
            return localDateTime.atZone(ZoneId.systemDefault()).toOffsetDateTime();
        }
        if (OffsetTime.class.equals(this.targetType)) {
            return localDateTime.atZone(ZoneId.systemDefault()).toOffsetDateTime().toOffsetTime();
        }

        return null;
    }

    /**
     * 将TemporalAccessor型时间戳转换为java.time中的对象
     *
     * @param zonedDateTime {@link ZonedDateTime}对象
     * @return java.time中的对象
     */
    private TemporalAccessor parseFromZonedDateTime(ZonedDateTime zonedDateTime) {
        if (Instant.class.equals(this.targetType)) {
            return DateKit.toInstant(zonedDateTime);
        }
        if (LocalDateTime.class.equals(this.targetType)) {
            return zonedDateTime.toLocalDateTime();
        }
        if (LocalDate.class.equals(this.targetType)) {
            return zonedDateTime.toLocalDate();
        }
        if (LocalTime.class.equals(this.targetType)) {
            return zonedDateTime.toLocalTime();
        }
        if (OffsetDateTime.class.equals(this.targetType)) {
            return zonedDateTime.toOffsetDateTime();
        }
        if (OffsetTime.class.equals(this.targetType)) {
            return zonedDateTime.toOffsetDateTime().toOffsetTime();
        }

        return null;
    }

    /**
     * 将TemporalAccessor型时间戳转换为java.time中的对象
     *
     * @param instant {@link Instant}对象
     * @param zoneId  时区ID，null表示当前系统默认的时区
     * @return java.time中的对象
     */
    private TemporalAccessor parseFromInstant(Instant instant, ZoneId zoneId) {
        if (Instant.class.equals(this.targetType)) {
            return instant;
        }

        zoneId = ObjectKit.defaultIfNull(zoneId, ZoneId.systemDefault());

        TemporalAccessor result = null;
        if (LocalDateTime.class.equals(this.targetType)) {
            result = LocalDateTime.ofInstant(instant, zoneId);
        } else if (LocalDate.class.equals(this.targetType)) {
            result = instant.atZone(zoneId).toLocalDate();
        } else if (LocalTime.class.equals(this.targetType)) {
            result = instant.atZone(zoneId).toLocalTime();
        } else if (ZonedDateTime.class.equals(this.targetType)) {
            result = instant.atZone(zoneId);
        } else if (OffsetDateTime.class.equals(this.targetType)) {
            result = OffsetDateTime.ofInstant(instant, zoneId);
        } else if (OffsetTime.class.equals(this.targetType)) {
            result = OffsetTime.ofInstant(instant, zoneId);
        }
        return result;
    }

}
