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

import java.io.Serial;
import java.time.*;
import java.time.chrono.Era;
import java.time.chrono.IsoEra;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import org.miaixz.bus.core.center.date.DateTime;
import org.miaixz.bus.core.center.date.Resolver;
import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Converter for java.time package objects introduced in JDK8, supporting:
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
 * @since Java 17+
 */
public class TemporalAccessorConverter extends AbstractConverter {

    @Serial
    private static final long serialVersionUID = 2852272056385L;

    /**
     * Singleton instance
     */
    public static final TemporalAccessorConverter INSTANCE = new TemporalAccessorConverter();

    /**
     * Date format pattern
     */
    private String format;

    /**
     * Constructs a new TemporalAccessorConverter
     */
    public TemporalAccessorConverter() {
        this(null);
    }

    /**
     * Constructs a new TemporalAccessorConverter with specified format
     *
     * @param format the date format pattern
     */
    public TemporalAccessorConverter(final String format) {
        this.format = format;
    }

    /**
     * Gets the date format pattern
     *
     * @return the date format pattern
     */
    public String getFormat() {
        return format;
    }

    /**
     * Sets the date format pattern
     *
     * @param format the date format pattern
     */
    public void setFormat(final String format) {
        this.format = format;
    }

    @Override
    protected TemporalAccessor convertInternal(final Class<?> targetClass, final Object value) {
        if (value instanceof Long) {
            return parseFromLong(targetClass, (Long) value);
        } else if (value instanceof Integer) {
            return parseFromLong(targetClass, ((Integer) value).longValue());
        } else if (value instanceof TemporalAccessor) {
            return parseFromTemporalAccessor(targetClass, (TemporalAccessor) value);
        } else if (value instanceof Date) {
            final DateTime dateTime = DateKit.date((Date) value);
            return parseFromInstant(targetClass, dateTime.toInstant(), dateTime.getZoneId());
        } else if (value instanceof Calendar calendar) {
            return parseFromInstant(targetClass, calendar.toInstant(), calendar.getTimeZone().toZoneId());
        } else {
            return parseFromCharSequence(targetClass, convertToString(value));
        }
    }

    /**
     * Parses a string to java.time object using reflection
     *
     * @param value the string value
     * @return the date object
     */
    private TemporalAccessor parseFromCharSequence(final Class<?> targetClass, final CharSequence value) {
        if (StringKit.isBlank(value)) {
            return null;
        }

        if (DayOfWeek.class == targetClass) {
            return DayOfWeek.valueOf(StringKit.toString(value));
        } else if (Month.class == targetClass) {
            return Month.valueOf(StringKit.toString(value));
        } else if (Era.class == targetClass) {
            return IsoEra.valueOf(StringKit.toString(value));
        } else if (MonthDay.class == targetClass) {
            return MonthDay.parse(value);
        }

        final Instant instant;
        final ZoneId zoneId;
        if (null != this.format) {
            final DateTimeFormatter formatter = Resolver.ofPattern(this.format);
            final TemporalAccessor temporalAccessor = parseWithFormat(targetClass, value, formatter);
            if (null != temporalAccessor) {
                return temporalAccessor;
            }

            instant = formatter.parse(value, Instant::from);
            zoneId = formatter.getZone();
        } else {
            final DateTime date = Resolver.parse(value);
            instant = Objects.requireNonNull(date).toInstant();
            zoneId = date.getZoneId();
        }
        return parseFromInstant(targetClass, instant, zoneId);
    }

    /**
     * For custom formatted strings, parse separately to {@link TemporalAccessor}
     *
     * @param targetClass the target type
     * @param value       the date string
     * @param formatter   the formatter
     * @return the {@link TemporalAccessor}
     */
    private TemporalAccessor parseWithFormat(
            final Class<?> targetClass,
            final CharSequence value,
            final DateTimeFormatter formatter) {
        if (LocalDate.class == targetClass) {
            return LocalDate.parse(value, formatter);
        } else if (LocalDateTime.class == targetClass) {
            return LocalDateTime.parse(value, formatter);
        } else if (LocalTime.class == targetClass) {
            return LocalTime.parse(value, formatter);
        }
        return null;
    }

    /**
     * Converts Long timestamp to java.time object
     *
     * @param targetClass the target type
     * @param time        the timestamp
     * @return the java.time object
     */
    private TemporalAccessor parseFromLong(final Class<?> targetClass, final Long time) {
        if (targetClass == Month.class) {
            return Month.of(Math.toIntExact(time));
        } else if (targetClass == DayOfWeek.class) {
            return DayOfWeek.of(Math.toIntExact(time));
        } else if (Era.class == targetClass) {
            return IsoEra.of(Math.toIntExact(time));
        }

        final Instant instant;
        if (Fields.FORMAT_SECONDS.equals(this.format)) {
            // Unix timestamp in seconds
            instant = Instant.ofEpochSecond(time);
        } else {
            instant = Instant.ofEpochMilli(time);
        }
        return parseFromInstant(targetClass, instant, null);
    }

    /**
     * Converts TemporalAccessor to java.time object
     *
     * @param temporalAccessor the TemporalAccessor object
     * @return the java.time object
     */
    private TemporalAccessor parseFromTemporalAccessor(
            final Class<?> targetClass,
            final TemporalAccessor temporalAccessor) {
        if (DayOfWeek.class == targetClass) {
            return DayOfWeek.from(temporalAccessor);
        } else if (Month.class == targetClass) {
            return Month.from(temporalAccessor);
        } else if (MonthDay.class == targetClass) {
            return MonthDay.from(temporalAccessor);
        }

        TemporalAccessor result = null;
        if (temporalAccessor instanceof LocalDateTime) {
            result = parseFromLocalDateTime(targetClass, (LocalDateTime) temporalAccessor);
        } else if (temporalAccessor instanceof ZonedDateTime) {
            result = parseFromZonedDateTime(targetClass, (ZonedDateTime) temporalAccessor);
        }

        if (null == result) {
            result = parseFromInstant(targetClass, DateKit.toInstant(temporalAccessor), null);
        }

        return result;
    }

    /**
     * Converts LocalDateTime to java.time object
     *
     * @param targetClass   the target class
     * @param localDateTime the {@link LocalDateTime} object
     * @return the java.time object
     */
    private TemporalAccessor parseFromLocalDateTime(final Class<?> targetClass, final LocalDateTime localDateTime) {
        if (Instant.class.equals(targetClass)) {
            return DateKit.toInstant(localDateTime);
        }
        if (LocalDate.class.equals(targetClass)) {
            return localDateTime.toLocalDate();
        }
        if (LocalTime.class.equals(targetClass)) {
            return localDateTime.toLocalTime();
        }
        if (ZonedDateTime.class.equals(targetClass)) {
            return localDateTime.atZone(ZoneId.systemDefault());
        }
        if (OffsetDateTime.class.equals(targetClass)) {
            return localDateTime.atZone(ZoneId.systemDefault()).toOffsetDateTime();
        }
        if (OffsetTime.class.equals(targetClass)) {
            return localDateTime.atZone(ZoneId.systemDefault()).toOffsetDateTime().toOffsetTime();
        }

        return null;
    }

    /**
     * Converts ZonedDateTime to java.time object
     *
     * @param zonedDateTime the {@link ZonedDateTime} object
     * @return the java.time object
     */
    private TemporalAccessor parseFromZonedDateTime(final Class<?> targetClass, final ZonedDateTime zonedDateTime) {
        if (Instant.class.equals(targetClass)) {
            return DateKit.toInstant(zonedDateTime);
        }
        if (LocalDateTime.class.equals(targetClass)) {
            return zonedDateTime.toLocalDateTime();
        }
        if (LocalDate.class.equals(targetClass)) {
            return zonedDateTime.toLocalDate();
        }
        if (LocalTime.class.equals(targetClass)) {
            return zonedDateTime.toLocalTime();
        }
        if (OffsetDateTime.class.equals(targetClass)) {
            return zonedDateTime.toOffsetDateTime();
        }
        if (OffsetTime.class.equals(targetClass)) {
            return zonedDateTime.toOffsetDateTime().toOffsetTime();
        }

        return null;
    }

    /**
     * Converts Instant to java.time object
     *
     * @param instant the {@link Instant} object
     * @param zoneId  the zone ID, null means system default timezone
     * @return the java.time object
     */
    private TemporalAccessor parseFromInstant(final Class<?> targetClass, final Instant instant, ZoneId zoneId) {
        if (Instant.class.equals(targetClass)) {
            return instant;
        }

        zoneId = ObjectKit.defaultIfNull(zoneId, ZoneId::systemDefault);

        TemporalAccessor result = null;
        if (LocalDateTime.class.equals(targetClass)) {
            result = LocalDateTime.ofInstant(instant, zoneId);
        } else if (LocalDate.class.equals(targetClass)) {
            result = instant.atZone(zoneId).toLocalDate();
        } else if (LocalTime.class.equals(targetClass)) {
            result = instant.atZone(zoneId).toLocalTime();
        } else if (ZonedDateTime.class.equals(targetClass)) {
            result = instant.atZone(zoneId);
        } else if (OffsetDateTime.class.equals(targetClass)) {
            result = OffsetDateTime.ofInstant(instant, zoneId);
        } else if (OffsetTime.class.equals(targetClass)) {
            result = OffsetTime.ofInstant(instant, zoneId);
        }
        return result;
    }

}
