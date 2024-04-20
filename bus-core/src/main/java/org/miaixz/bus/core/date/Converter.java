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
package org.miaixz.bus.core.date;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.toolkit.CharsKit;
import org.miaixz.bus.core.toolkit.ZoneKit;

import javax.xml.datatype.XMLGregorianCalendar;
import java.sql.Timestamp;
import java.time.*;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * 日期转换
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Converter extends Formatter {

    /**
     * LocalDateTime转Date
     *
     * @param localDateTime LocalDateTime
     * @return Date
     */
    public static Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * LocalDate转Date
     *
     * @param localDate LocalDate
     * @return Date
     */
    public static Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * LocalTime转Date
     * 以当天的日期+LocalTime组成新的LocalDateTime转换为Date
     *
     * @param localTime LocalTime
     * @return Date
     */
    public static Date toDate(LocalTime localTime) {
        return Date.from(LocalDate.now().atTime(localTime).atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Instant转Date
     *
     * @param instant Instant
     * @return Date
     */
    public static Date toDate(Instant instant) {
        return Date.from(instant);
    }

    /**
     * 时间戳epochMilli毫秒转Date
     *
     * @param epochMilli 时间戳
     * @return Date
     */
    public static Date toDate(long epochMilli) {
        return new Date(epochMilli);
    }

    /**
     * ZonedDateTime转Date
     * 注意时间对应的时区和默认时区差异
     *
     * @param zonedDateTime ZonedDateTime
     * @return Date
     */
    public static Date toDate(ZonedDateTime zonedDateTime) {
        return Date.from(zonedDateTime.toInstant());
    }

    /**
     * YearMonth转Date
     * 注意dayOfMonth范围：1到31之间，最大值根据月份确定特殊情况，如2月闰年29，非闰年28
     * 如果要转换为当月最后一天，可以使用下面方法：toDateEndOfMonth(YearMonth)
     *
     * @param yearMonth  YearMonth
     * @param dayOfMonth 天
     * @return Date
     */
    public static Date toDate(YearMonth yearMonth, int dayOfMonth) {
        return toDate(yearMonth.atDay(dayOfMonth));
    }

    /**
     * YearMonth转Date，转换为当月第一天
     *
     * @param yearMonth YearMonth
     * @return Date
     */
    public static Date toDateStartOfMonth(YearMonth yearMonth) {
        return toDate(yearMonth, 1);
    }

    /**
     * YearMonth转Date，转换为当月最后一天
     *
     * @param yearMonth YearMonth
     * @return Date
     */
    public static Date toDateEndOfMonth(YearMonth yearMonth) {
        return toDate(yearMonth.atEndOfMonth());
    }

    /**
     * Date转LocalDateTime
     *
     * @param date Date
     * @return LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Timestamp转LocalDateTime
     *
     * @param timestamp Timestamp
     * @return LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp.toLocalDateTime();
    }

    /**
     * LocalDate转LocalDateTime
     *
     * @param localDate LocalDate
     * @return LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(LocalDate localDate) {
        return localDate.atStartOfDay();
    }

    /**
     * LocalTime转LocalDateTime
     * 以当天的日期+LocalTime组成新的LocalDateTime
     *
     * @param localTime LocalTime
     * @return LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(LocalTime localTime) {
        return LocalDate.now().atTime(localTime);
    }

    /**
     * Instant转LocalDateTime
     *
     * @param instant Instant
     * @return LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    /**
     * 时间戳epochMilli毫秒转LocalDateTime
     *
     * @param epochMilli 时间戳
     * @return LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(long epochMilli) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault());
    }

    /**
     * temporal转LocalDateTime
     *
     * @param temporal TemporalAccessor
     * @return LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(TemporalAccessor temporal) {
        return LocalDateTime.from(temporal);
    }

    /**
     * ZonedDateTime转LocalDateTime
     * 注意时间对应的时区和默认时区差异
     *
     * @param zonedDateTime ZonedDateTime
     * @return LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(ZonedDateTime zonedDateTime) {
        return zonedDateTime.toLocalDateTime();
    }

    /**
     * Date转LocalDate
     *
     * @param date Date
     * @return LocalDate
     */
    public static LocalDate toLocalDate(Date date) {
        return toLocalDateTime(date).toLocalDate();
    }

    /**
     * LocalDateTime转LocalDate
     *
     * @param localDateTime LocalDateTime
     * @return LocalDate
     */
    public static LocalDate toLocalDate(LocalDateTime localDateTime) {
        return localDateTime.toLocalDate();
    }

    /**
     * Instant转LocalDate
     *
     * @param instant Instant
     * @return LocalDate
     */
    public static LocalDate toLocalDate(Instant instant) {
        return toLocalDateTime(instant).toLocalDate();
    }

    /**
     * 时间戳epochMilli毫秒转LocalDate
     *
     * @param epochMilli 时间戳
     * @return LocalDate
     */
    public static LocalDate toLocalDate(long epochMilli) {
        return toLocalDateTime(epochMilli).toLocalDate();
    }

    /**
     * temporal转LocalDate
     *
     * @param temporal TemporalAccessor
     * @return LocalDate
     */
    public static LocalDate toLocalDate(TemporalAccessor temporal) {
        return LocalDate.from(temporal);
    }

    /**
     * ZonedDateTime转LocalDate
     * 注意时间对应的时区和默认时区差异
     *
     * @param zonedDateTime ZonedDateTime
     * @return LocalDate
     */
    public static LocalDate toLocalDate(ZonedDateTime zonedDateTime) {
        return zonedDateTime.toLocalDate();
    }

    /**
     * YearMonth转LocalDate
     * 注意dayOfMonth范围：1到31之间，最大值根据月份确定特殊情况，如2月闰年29，非闰年28
     * 如果要转换为当月最后一天，可以使用下面方法：toLocalDateEndOfMonth(YearMonth)
     *
     * @param yearMonth  YearMonth
     * @param dayOfMonth 天
     * @return LocalDate
     */
    public static LocalDate toLocalDate(YearMonth yearMonth, int dayOfMonth) {
        return yearMonth.atDay(dayOfMonth);
    }

    /**
     * {@link Date} 转换时区
     *
     * @param date   {@link Date}
     * @param zoneId {@link ZoneId}
     * @return {@link DateTime}
     */
    public static DateTime toTimeZone(Date date, ZoneId zoneId) {
        return new DateTime(date, ZoneKit.toTimeZone(zoneId));
    }

    /**
     * {@link Date} 转换时区
     *
     * @param date     {@link Date}
     * @param timeZone {@link TimeZone}
     * @return {@link DateTime}
     */
    public static DateTime toTimeZone(Date date, TimeZone timeZone) {
        return new DateTime(date, timeZone);
    }

    /**
     * YearMonth转LocalDate，转换为当月第一天
     *
     * @param yearMonth YearMonth
     * @return LocalDate
     */
    public static LocalDate toLocalDateStartOfMonth(YearMonth yearMonth) {
        return toLocalDate(yearMonth, 1);
    }

    /**
     * YearMonth转LocalDate，转换为当月最后一天
     *
     * @param yearMonth YearMonth
     * @return LocalDate
     */
    public static LocalDate toLocalDateEndOfMonth(YearMonth yearMonth) {
        return yearMonth.atEndOfMonth();
    }

    /**
     * Date转LocalTime
     *
     * @param date Date
     * @return LocalTime
     */
    public static LocalTime toLocalTime(Date date) {
        return toLocalDateTime(date).toLocalTime();
    }

    /**
     * LocalDateTime转LocalTime
     *
     * @param localDateTime LocalDateTime
     * @return LocalTime
     */
    public static LocalTime toLocalTime(LocalDateTime localDateTime) {
        return localDateTime.toLocalTime();
    }

    /**
     * Instant转LocalTime
     *
     * @param instant Instant
     * @return LocalTime
     */
    public static LocalTime toLocalTime(Instant instant) {
        return toLocalDateTime(instant).toLocalTime();
    }

    /**
     * temporal转LocalTime
     *
     * @param temporal TemporalAccessor
     * @return LocalTime
     */
    public static LocalTime toLocalTime(TemporalAccessor temporal) {
        return LocalTime.from(temporal);
    }

    /**
     * ZonedDateTime转LocalTime
     * 注意时间对应的时区和默认时区差异
     *
     * @param zonedDateTime ZonedDateTime
     * @return LocalTime
     */
    public static LocalTime toLocalTime(ZonedDateTime zonedDateTime) {
        return zonedDateTime.toLocalTime();
    }

    /**
     * Date转Instant
     *
     * @param date Date
     * @return Instant
     */
    public static Instant toInstant(Date date) {
        return date.toInstant();
    }

    /**
     * Timestamp转Instant
     *
     * @param timestamp Timestamp
     * @return Instant
     */
    public static Instant toInstant(Timestamp timestamp) {
        return timestamp.toInstant();
    }

    /**
     * LocalDateTime转Instant
     *
     * @param localDateTime LocalDateTime
     * @return Instant
     */
    public static Instant toInstant(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
    }

    /**
     * LocalDate转Instant
     *
     * @param localDate LocalDate
     * @return Instant
     */
    public static Instant toInstant(LocalDate localDate) {
        return toLocalDateTime(localDate).atZone(ZoneId.systemDefault()).toInstant();
    }

    /**
     * Calendar{@link Instant}对象
     *
     * @param calendar Date对象
     * @return {@link Instant}对象
     */
    public static Instant toInstant(Calendar calendar) {
        return null == calendar ? null : calendar.toInstant();
    }

    /**
     * LocalTime转Instant
     * 以当天的日期+LocalTime组成新的LocalDateTime转换为Instant
     *
     * @param localTime LocalTime
     * @return Instant
     */
    public static Instant toInstant(LocalTime localTime) {
        return toLocalDateTime(localTime).atZone(ZoneId.systemDefault()).toInstant();
    }

    /**
     * 时间戳epochMilli毫秒转Instant
     *
     * @param epochMilli 时间戳
     * @return Instant
     */
    public static Instant toInstant(long epochMilli) {
        return Instant.ofEpochMilli(epochMilli);
    }

    /**
     * ZonedDateTime转Instant
     * 注意，zonedDateTime时区必须和当前系统时区一致，不然会出现问题
     *
     * @param zonedDateTime ZonedDateTime
     * @return Instant
     */
    public static Instant toInstant(ZonedDateTime zonedDateTime) {
        return zonedDateTime.toInstant();
    }

    /**
     * Date对象转换为{@link Instant}对象
     *
     * @param temporalAccessor Date对象
     * @return {@link Instant}对象
     */
    public static Instant toInstant(TemporalAccessor temporalAccessor) {
        if (null == temporalAccessor) {
            return null;
        }

        Instant result;
        if (temporalAccessor instanceof Instant) {
            result = (Instant) temporalAccessor;
        } else if (temporalAccessor instanceof LocalDateTime) {
            result = ((LocalDateTime) temporalAccessor).atZone(ZoneId.systemDefault()).toInstant();
        } else if (temporalAccessor instanceof ZonedDateTime) {
            result = ((ZonedDateTime) temporalAccessor).toInstant();
        } else if (temporalAccessor instanceof OffsetDateTime) {
            result = ((OffsetDateTime) temporalAccessor).toInstant();
        } else if (temporalAccessor instanceof LocalDate) {
            result = ((LocalDate) temporalAccessor).atStartOfDay(ZoneId.systemDefault()).toInstant();
        } else if (temporalAccessor instanceof LocalTime) {
            // 指定本地时间转换 为Instant，取当天日期
            result = ((LocalTime) temporalAccessor).atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant();
        } else if (temporalAccessor instanceof OffsetTime) {
            // 指定本地时间转换 为Instant，取当天日期
            result = ((OffsetTime) temporalAccessor).atDate(LocalDate.now()).toInstant();
        } else {
            result = toInstant(DateTime.of(temporalAccessor));
        }

        return result;
    }

    /**
     * 转换为Calendar对象
     *
     * @param millis 时间戳
     * @return Calendar对象
     */
    public static Calendar toCalendar(long millis) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        return cal;
    }

    /**
     * 将{@code Date}转换为{@code Calendar}
     *
     * @param date 日期转换为日历的日期
     * @return 创建的日历
     */
    public static Calendar toCalendar(final Date date) {
        if (date instanceof DateTime) {
            return ((DateTime) date).toCalendar();
        }
        if (date instanceof Date) {
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return calendar;
        }
        return toCalendar(date.getTime());
    }

    /**
     * 转换为Calendar对象
     *
     * @param calendar 日期对象
     * @return Calendar对象
     */
    public static Calendar toCalendar(final XMLGregorianCalendar calendar) {
        return calendar.toGregorianCalendar();
    }

    /**
     * 将{@code Date}转换为{@code Calendar}.
     *
     * @param date     日期转换为日历的日期
     * @param timeZone 时区
     * @return 创建的日历
     */
    public static Calendar toCalendar(final Date date, final TimeZone timeZone) {
        final Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTime(date);
        return calendar;
    }

    /**
     * Date转时间戳
     * 从1970-01-01T00:00:00Z开始的毫秒值
     *
     * @param date Date
     * @return 时间戳
     */
    public static long toEpochMilli(Date date) {
        return date.getTime();
    }

    /**
     * Timestamp转时间戳
     * 从1970-01-01T00:00:00Z开始的毫秒值
     *
     * @param timestamp Timestamp
     * @return 时间戳
     */
    public static long toEpochMilli(Timestamp timestamp) {
        return timestamp.getTime();
    }

    /**
     * LocalDateTime转时间戳
     * 从1970-01-01T00:00:00Z开始的毫秒值
     *
     * @param localDateTime LocalDateTime
     * @return 时间戳
     */
    public static long toEpochMilli(LocalDateTime localDateTime) {
        return toInstant(localDateTime).toEpochMilli();
    }

    /**
     * LocalDate转时间戳
     * 从1970-01-01T00:00:00Z开始的毫秒值
     *
     * @param localDate LocalDate
     * @return 时间戳
     */
    public static long toEpochMilli(LocalDate localDate) {
        return toInstant(localDate).toEpochMilli();
    }

    /**
     * Instant转时间戳
     * 从1970-01-01T00:00:00Z开始的毫秒值
     *
     * @param instant Instant
     * @return 时间戳
     */
    public static long toEpochMilli(Instant instant) {
        return instant.toEpochMilli();
    }

    /**
     * ZonedDateTime转时间戳，注意，zonedDateTime时区必须和当前系统时区一致，不然会出现问题
     * 从1970-01-01T00:00:00Z开始的毫秒值
     *
     * @param zonedDateTime ZonedDateTime
     * @return 时间戳
     */
    public static long toEpochMilli(ZonedDateTime zonedDateTime) {
        return zonedDateTime.toInstant().toEpochMilli();
    }

    /**
     * {@link TemporalAccessor}转换为 时间戳（从1970-01-01T00:00:00Z开始的毫秒数）
     * 如果为{@link Month}，调用{@link Month#getValue()}
     *
     * @param temporalAccessor Date对象
     * @return {@link Instant}对象
     */
    public static long toEpochMilli(TemporalAccessor temporalAccessor) {
        if (temporalAccessor instanceof Month) {
            return ((Month) temporalAccessor).getValue();
        }
        return toInstant(temporalAccessor).toEpochMilli();
    }

    /**
     * Date转ZonedDateTime，时区为系统默认时区
     *
     * @param date Date
     * @return ZonedDateTime
     */
    public static ZonedDateTime toZonedDateTime(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault());
    }

    /**
     * Date转ZonedDateTime
     *
     * @param date   Date
     * @param zoneId 目标时区
     * @return ZonedDateTime
     */
    public static ZonedDateTime toZonedDateTime(Date date, String zoneId) {
        return toZonedDateTime(date, ZoneId.of(zoneId));
    }

    /**
     * Date转ZonedDateTime
     *
     * @param date Date
     * @param zone 目标时区
     * @return ZonedDateTime
     */
    public static ZonedDateTime toZonedDateTime(Date date, ZoneId zone) {
        return Instant.ofEpochMilli(date.getTime()).atZone(zone);
    }

    /**
     * LocalDateTime转ZonedDateTime，时区为系统默认时区
     *
     * @param localDateTime LocalDateTime
     * @return ZonedDateTime
     */
    public static ZonedDateTime toZonedDateTime(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault());
    }

    /**
     * LocalDateTime转ZonedDateTime，时区为zoneId对应时区
     * 注意，需要保证localDateTime和zoneId是对应的，不然会出现错误
     *
     * @param localDateTime LocalDateTime
     * @param zoneId        LocalDateTime
     * @return ZonedDateTime
     */
    public static ZonedDateTime toZonedDateTime(LocalDateTime localDateTime, String zoneId) {
        return localDateTime.atZone(ZoneId.of(zoneId));
    }

    /**
     * LocalDate转ZonedDateTime，时区为系统默认时区
     *
     * @param localDate LocalDate
     * @return ZonedDateTime such as 2020-02-19T00:00+08:00[Asia/Shanghai]
     */
    public static ZonedDateTime toZonedDateTime(LocalDate localDate) {
        return localDate.atStartOfDay().atZone(ZoneId.systemDefault());
    }

    /**
     * LocalTime转ZonedDateTime
     * 以当天的日期+LocalTime组成新的ZonedDateTime，时区为系统默认时区
     *
     * @param localTime LocalTime
     * @return ZonedDateTime
     */
    public static ZonedDateTime toZonedDateTime(LocalTime localTime) {
        return LocalDate.now().atTime(localTime).atZone(ZoneId.systemDefault());
    }

    /**
     * Instant转ZonedDateTime，时区为系统默认时区
     *
     * @param instant Instant
     * @return ZonedDateTime
     */
    public static ZonedDateTime toZonedDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).atZone(ZoneId.systemDefault());
    }

    /**
     * 时间戳epochMilli毫秒转ZonedDateTime，时区为系统默认时区
     *
     * @param epochMilli 时间戳
     * @return ZonedDateTime
     */
    public static ZonedDateTime toZonedDateTime(long epochMilli) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault())
                .atZone(ZoneId.systemDefault());
    }

    /**
     * temporal转ZonedDateTime，时区为系统默认时区
     *
     * @param temporal TemporalAccessor
     * @return ZonedDateTime
     */
    public static ZonedDateTime toZonedDateTime(TemporalAccessor temporal) {
        return LocalDateTime.from(temporal).atZone(ZoneId.systemDefault());
    }

    /**
     * Date转YearMonth
     *
     * @param date Date
     * @return YearMonth
     */
    public static YearMonth toYearMonth(Date date) {
        LocalDate localDate = toLocalDate(date);
        return YearMonth.of(localDate.getYear(), localDate.getMonthValue());
    }

    /**
     * LocalDateTime转YearMonth
     *
     * @param localDateTime LocalDateTime
     * @return YearMonth
     */
    public static YearMonth toYearMonth(LocalDateTime localDateTime) {
        LocalDate localDate = toLocalDate(localDateTime);
        return YearMonth.of(localDate.getYear(), localDate.getMonthValue());
    }

    /**
     * LocalDate转YearMonth
     *
     * @param localDate LocalDate
     * @return YearMonth
     */
    public static YearMonth toYearMonth(LocalDate localDate) {
        return YearMonth.of(localDate.getYear(), localDate.getMonthValue());
    }

    /**
     * Instant转YearMonth
     *
     * @param instant Instant
     * @return YearMonth
     */
    public static YearMonth toYearMonth(Instant instant) {
        LocalDate localDate = toLocalDate(instant);
        return YearMonth.of(localDate.getYear(), localDate.getMonthValue());
    }

    /**
     * ZonedDateTime转YearMonth
     *
     * @param zonedDateTime ZonedDateTime
     * @return YearMonth
     */
    public static YearMonth toYearMonth(ZonedDateTime zonedDateTime) {
        LocalDate localDate = toLocalDate(zonedDateTime);
        return YearMonth.of(localDate.getYear(), localDate.getMonthValue());
    }

    /**
     * Date转Timestamp
     *
     * @param date Date
     * @return Timestamp
     */
    public static Timestamp toTimestamp(Date date) {
        return new Timestamp(date.getTime());
    }

    /**
     * LocalDateTime转Timestamp
     *
     * @param localDateTime LocalDateTime
     * @return Timestamp
     */
    public static Timestamp toTimestamp(LocalDateTime localDateTime) {
        return Timestamp.valueOf(localDateTime);
    }

    /**
     * Instant转Timestamp
     *
     * @param instant Instant
     * @return Timestamp
     */
    public static Timestamp toTimestamp(Instant instant) {
        return Timestamp.from(instant);
    }

    /**
     * 时间戳epochMilli转Timestamp
     *
     * @param epochMilli 时间戳
     * @return Timestamp
     */
    public static Timestamp toTimestamp(long epochMilli) {
        return new Timestamp(epochMilli);
    }

    /**
     * 秒数转为时间格式(HH:mm:ss)
     *
     * @param seconds 需要转换的秒数
     * @return 转换后的字符串
     */
    public static String toTime(int seconds) {
        if (seconds < 0) {
            throw new IllegalArgumentException("Seconds must be a positive number!");
        }

        int hour = seconds / 3600;
        int other = seconds % 3600;
        int minute = other / 60;
        int second = other % 60;
        final StringBuilder sb = new StringBuilder();
        if (hour < 10) {
            sb.append(Symbol.ZERO);
        }
        sb.append(hour);
        sb.append(Symbol.COLON);
        if (minute < 10) {
            sb.append(Symbol.ZERO);
        }
        sb.append(minute);
        sb.append(Symbol.COLON);
        if (second < 10) {
            sb.append(Symbol.ZERO);
        }
        sb.append(second);
        return sb.toString();
    }

    /**
     * HH:mm:ss 时间格式字符串转为秒数
     *
     * @param time 字符串时分秒(HH:mm:ss)格式
     * @return 时分秒转换后的秒数
     */
    public static int toSecond(String time) {
        if (CharsKit.isEmpty(time)) {
            return 0;
        }

        final List<String> hms = CharsKit.splitTrim(time, Symbol.C_COLON, 3);
        int lastIndex = hms.size() - 1;

        int result = 0;
        for (int i = lastIndex; i >= 0; i--) {
            result += Integer.parseInt(hms.get(i)) * Math.pow(60, (lastIndex - i));
        }
        return result;
    }

}
