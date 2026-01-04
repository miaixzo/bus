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
package org.miaixz.bus.core.center.date;

import java.time.*;
import java.time.chrono.Era;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.ZoneKit;

/**
 * Date conversion utility.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Converter extends Formatter {

    /**
     * Constructs a new Converter. Utility class constructor for static access.
     */
    public Converter() {
    }

    /**
     * Converts a {@link Calendar} type time to a {@link DateTime} object. Always creates a new {@link DateTime} object
     * based on the existing {@link Calendar}.
     *
     * @param calendar The {@link Calendar} object; if {@code null} is passed, {@code null} is returned.
     * @return The {@link DateTime} object.
     */
    public static DateTime date(final Calendar calendar) {
        if (calendar == null) {
            return null;
        }
        return new DateTime(calendar);
    }

    /**
     * Converts a {@link TemporalAccessor} type time to a {@link DateTime} object. Always creates a new {@link DateTime}
     * object based on the existing {@link TemporalAccessor}.
     *
     * @param temporalAccessor The {@link TemporalAccessor} object; common subclasses include {@link LocalDateTime},
     *                         {@link LocalDate}. If {@code null} is passed, {@code null} is returned.
     * @return The {@link DateTime} object.
     */
    public static DateTime date(final TemporalAccessor temporalAccessor) {
        if (temporalAccessor == null) {
            return null;
        }
        return new DateTime(temporalAccessor);
    }

    /**
     * Safely retrieves a specific property of a time object. If the property does not exist, the minimum value (usually
     * 0) is returned. Caution: Use this method carefully, as some methods where
     * {@link TemporalAccessor#isSupported(TemporalField)} is {@code false} will return the minimum value.
     *
     * @param temporalAccessor The time object from which to retrieve the property.
     * @param field            The property to retrieve.
     * @return The value of the time property; if it cannot be retrieved, the minimum value (usually 0) is returned.
     */
    public static int get(final TemporalAccessor temporalAccessor, final TemporalField field) {
        if (temporalAccessor.isSupported(field)) {
            return temporalAccessor.get(field);
        }

        return (int) field.range().getMinimum();
    }

    /**
     * Converts a {@link TemporalAccessor} to an epoch millisecond timestamp (milliseconds since 1970-01-01T00:00:00Z).
     * If it's a {@link Month}, {@link Month#getValue()} is called. If it's a {@link DayOfWeek},
     * {@link DayOfWeek#getValue()} is called. If it's an {@link Era}, {@link Era#getValue()} is called.
     *
     * @param temporalAccessor The date object.
     * @return The epoch millisecond timestamp.
     */
    public static long toEpochMilli(final TemporalAccessor temporalAccessor) {
        if (temporalAccessor instanceof java.time.Month) {
            return ((java.time.Month) temporalAccessor).getValue();
        } else if (temporalAccessor instanceof DayOfWeek) {
            return ((DayOfWeek) temporalAccessor).getValue();
        } else if (temporalAccessor instanceof Era) {
            return ((Era) temporalAccessor).getValue();
        }
        return toInstant(temporalAccessor).toEpochMilli();
    }

    /**
     * Converts a {@link TemporalAccessor} to an {@link Instant} object.
     *
     * @param temporalAccessor The date object.
     * @return The {@link Instant} object.
     */
    public static Instant toInstant(final TemporalAccessor temporalAccessor) {
        if (null == temporalAccessor) {
            return null;
        }

        final Instant result;
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
            // Convert local time to Instant, using today's date
            result = ((LocalTime) temporalAccessor).atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant();
        } else if (temporalAccessor instanceof OffsetTime) {
            // Convert local time to Instant, using today's date
            result = ((OffsetTime) temporalAccessor).atDate(LocalDate.now()).toInstant();
        } else {
            result = toInstant(of(temporalAccessor));
        }

        return result;
    }

    /**
     * Converts {@link TimeUnit} to {@link ChronoUnit}.
     *
     * @param unit The {@link TimeUnit} to convert; if {@code null}, {@code null} is returned.
     * @return The {@link ChronoUnit}.
     * @throws IllegalArgumentException if the {@link TimeUnit} constant is unknown.
     */
    public static ChronoUnit toChronoUnit(final TimeUnit unit) throws IllegalArgumentException {
        if (null == unit) {
            return null;
        }
        switch (unit) {
            case NANOSECONDS:
                return ChronoUnit.NANOS;

            case MICROSECONDS:
                return ChronoUnit.MICROS;

            case MILLISECONDS:
                return ChronoUnit.MILLIS;

            case SECONDS:
                return ChronoUnit.SECONDS;

            case MINUTES:
                return ChronoUnit.MINUTES;

            case HOURS:
                return ChronoUnit.HOURS;

            case DAYS:
                return ChronoUnit.DAYS;

            default:
                throw new IllegalArgumentException("Unknown TimeUnit constant");
        }
    }

    /**
     * Converts {@link ChronoUnit} to {@link TimeUnit}.
     *
     * @param unit The {@link ChronoUnit}; if {@code null}, {@code null} is returned.
     * @return The {@link TimeUnit}.
     * @throws IllegalArgumentException if there is no corresponding unit in {@link TimeUnit}.
     */
    public static TimeUnit toTimeUnit(final ChronoUnit unit) throws IllegalArgumentException {
        if (null == unit) {
            return null;
        }
        switch (unit) {
            case NANOS:
                return TimeUnit.NANOSECONDS;

            case MICROS:
                return TimeUnit.MICROSECONDS;

            case MILLIS:
                return TimeUnit.MILLISECONDS;

            case SECONDS:
                return TimeUnit.SECONDS;

            case MINUTES:
                return TimeUnit.MINUTES;

            case HOURS:
                return TimeUnit.HOURS;

            case DAYS:
                return TimeUnit.DAYS;

            default:
                throw new IllegalArgumentException("ChronoUnit cannot be converted to TimeUnit: " + unit);
        }
    }

    /**
     * Converts {@link Instant} to {@link LocalDateTime}, using UTC time zone. This method automatically converts a UTC
     * time to local time; for example, if 00:00 is passed, the result will be 08:00.
     *
     * @param instant The {@link Instant} object.
     * @return The {@link LocalDateTime} object.
     */
    public static LocalDateTime ofUTC(final Instant instant) {
        return of(instant, ZoneKit.ZONE_ID_UTC);
    }

    /**
     * Converts {@link Instant} to {@link LocalDateTime}. An instant is a time stamp without a time zone. When
     * converting to local time, the time zone of this timestamp needs to be specified. If the specified time zone is
     * different from the current time zone, the time will be converted.
     *
     * @param instant The {@link Instant} object.
     * @param zoneId  The time zone; if the given time zone is different from the current time zone, the time will be
     *                converted.
     * @return The {@link LocalDateTime} object.
     */
    public static LocalDateTime of(final Instant instant, final ZoneId zoneId) {
        if (null == instant) {
            return null;
        }

        return LocalDateTime.ofInstant(instant, ObjectKit.defaultIfNull(zoneId, ZoneId::systemDefault));
    }

    /**
     * Converts {@link Instant} to {@link LocalDateTime}. An instant is a time stamp without a time zone. When
     * converting to local time, the time zone of this timestamp needs to be specified. If the specified time zone is
     * different from the current time zone, the time will be converted.
     *
     * @param instant  The {@link Instant} object.
     * @param timeZone The time zone; if the given time zone is different from the current time zone, the time will be
     *                 converted.
     * @return The {@link LocalDateTime} object.
     */
    public static LocalDateTime of(final Instant instant, final TimeZone timeZone) {
        if (null == instant) {
            return null;
        }

        return of(instant, ObjectKit.defaultIfNull(timeZone, TimeZone::getDefault).toZoneId());
    }

    /**
     * Converts milliseconds to {@link LocalDateTime}, using the default time zone.
     *
     * <p>
     * Note: This method uses the default time zone, which may cause a time offset if it's not UTC.
     * 
     *
     * @param epochMilli Milliseconds counted from 1970-01-01T00:00:00Z.
     * @return The {@link LocalDateTime} object.
     */
    public static LocalDateTime of(final long epochMilli) {
        return of(Instant.ofEpochMilli(epochMilli));
    }

    /**
     * Converts milliseconds to {@link LocalDateTime}, using the UTC time zone.
     *
     * @param epochMilli Milliseconds counted from 1970-01-01T00:00:00Z.
     * @return The {@link LocalDateTime} object.
     */
    public static LocalDateTime ofUTC(final long epochMilli) {
        return ofUTC(Instant.ofEpochMilli(epochMilli));
    }

    /**
     * Converts {@link Date} to {@link LocalDateTime}, using the default time zone. If it's a {@link DateTime} and time
     * zone information is provided, it will be converted to the default time zone according to the given time zone.
     *
     * @param date The Date object.
     * @return The {@link LocalDateTime} object.
     */
    public static LocalDateTime of(final Date date) {
        if (null == date) {
            return null;
        }

        if (date instanceof DateTime) {
            return of(date.toInstant(), ((DateTime) date).getZoneId());
        }
        return of(date.toInstant());
    }

    /**
     * Converts milliseconds to {@link LocalDateTime}. The result may have a time offset depending on the time zone.
     *
     * @param epochMilli Milliseconds counted from 1970-01-01T00:00:00Z.
     * @param zoneId     The time zone.
     * @return The {@link LocalDateTime} object.
     */
    public static LocalDateTime of(final long epochMilli, final ZoneId zoneId) {
        return of(Instant.ofEpochMilli(epochMilli), zoneId);
    }

    /**
     * Converts milliseconds to {@link LocalDateTime}. The result may have a time offset.
     *
     * @param epochMilli Milliseconds counted from 1970-01-01T00:00:00Z.
     * @param timeZone   The time zone.
     * @return The {@link LocalDateTime} object.
     */
    public static LocalDateTime of(final long epochMilli, final TimeZone timeZone) {
        return of(Instant.ofEpochMilli(epochMilli), timeZone);
    }

    /**
     * Converts {@link TemporalAccessor} to {@link LocalDateTime}, using the default time zone.
     *
     * @param temporalAccessor The {@link TemporalAccessor} object.
     * @return The {@link LocalDateTime} object.
     */
    public static LocalDateTime of(final TemporalAccessor temporalAccessor) {
        if (null == temporalAccessor) {
            return null;
        }

        if (temporalAccessor instanceof LocalDate) {
            return ((LocalDate) temporalAccessor).atStartOfDay();
        } else if (temporalAccessor instanceof Instant) {
            return LocalDateTime.ofInstant((Instant) temporalAccessor, ZoneId.systemDefault());
        }

        try {
            return LocalDateTime.from(temporalAccessor);
        } catch (final Exception ignore) {
            // ignore
        }

        try {
            return ZonedDateTime.from(temporalAccessor).toLocalDateTime();
        } catch (final Exception ignore) {
            // ignore
        }

        try {
            return LocalDateTime.ofInstant(Instant.from(temporalAccessor), ZoneId.systemDefault());
        } catch (final Exception ignore) {
            // ignore
        }

        return LocalDateTime.of(
                get(temporalAccessor, ChronoField.YEAR),
                get(temporalAccessor, ChronoField.MONTH_OF_YEAR),
                get(temporalAccessor, ChronoField.DAY_OF_MONTH),
                get(temporalAccessor, ChronoField.HOUR_OF_DAY),
                get(temporalAccessor, ChronoField.MINUTE_OF_HOUR),
                get(temporalAccessor, ChronoField.SECOND_OF_MINUTE),
                get(temporalAccessor, ChronoField.NANO_OF_SECOND));
    }

    /**
     * Converts {@link TemporalAccessor} to {@link LocalDate}, using the default time zone.
     *
     * @param temporalAccessor The {@link TemporalAccessor} object.
     * @return The {@link LocalDate} object.
     */
    public static LocalDate ofDate(final TemporalAccessor temporalAccessor) {
        if (null == temporalAccessor) {
            return null;
        }

        try {
            return LocalDate.from(temporalAccessor);
        } catch (final Exception ignore) {
            // ignore
        }

        if (temporalAccessor instanceof LocalDateTime) {
            return ((LocalDateTime) temporalAccessor).toLocalDate();
        } else if (temporalAccessor instanceof Instant) {
            return of(temporalAccessor).toLocalDate();
        }

        return LocalDate.of(
                get(temporalAccessor, ChronoField.YEAR),
                get(temporalAccessor, ChronoField.MONTH_OF_YEAR),
                get(temporalAccessor, ChronoField.DAY_OF_MONTH));
    }

    /**
     * Converts {@link TemporalAccessor} to {@link ZonedDateTime}.
     *
     * @param temporalAccessor The {@link TemporalAccessor} object.
     * @param zoneId           The time zone ID.
     * @return The {@link ZonedDateTime} object.
     */
    public static ZonedDateTime ofZoned(final TemporalAccessor temporalAccessor, ZoneId zoneId) {
        if (null == temporalAccessor) {
            return null;
        }
        if (null == zoneId) {
            zoneId = ZoneId.systemDefault();
        }

        if (temporalAccessor instanceof Instant) {
            return ZonedDateTime.ofInstant((Instant) temporalAccessor, zoneId);
        } else if (temporalAccessor instanceof LocalDateTime) {
            return ZonedDateTime.of((LocalDateTime) temporalAccessor, zoneId);
        } else if (temporalAccessor instanceof LocalDate) {
            return ZonedDateTime.of((LocalDate) temporalAccessor, LocalTime.MIN, zoneId);
        } else if (temporalAccessor instanceof LocalTime) {
            return ZonedDateTime.of(LocalDate.now(), (LocalTime) temporalAccessor, zoneId);
        }

        return ZonedDateTime.of(
                get(temporalAccessor, ChronoField.YEAR),
                get(temporalAccessor, ChronoField.MONTH_OF_YEAR),
                get(temporalAccessor, ChronoField.DAY_OF_MONTH),
                get(temporalAccessor, ChronoField.HOUR_OF_DAY),
                get(temporalAccessor, ChronoField.MINUTE_OF_HOUR),
                get(temporalAccessor, ChronoField.SECOND_OF_MINUTE),
                get(temporalAccessor, ChronoField.NANO_OF_SECOND),
                zoneId);
    }

    /**
     * Constructs a {@link DateTimeFormatter} from a date-time pattern string.
     *
     * @param pattern The format pattern, e.g., "yyyy-MM-dd".
     * @return The {@link DateTimeFormatter} object.
     */
    public static DateTimeFormatter ofPattern(final String pattern) {
        return new DateTimeFormatterBuilder().appendPattern(pattern).parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0).parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                .parseDefaulting(ChronoField.MILLI_OF_SECOND, 0).toFormatter();
    }

}
