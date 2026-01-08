/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.center.date.builder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.core.center.date.DateTime;
import org.miaixz.bus.core.lang.exception.DateException;
import org.miaixz.bus.core.xyz.ZoneKit;

/**
 * The {@code DateBuilder} class is used to construct and manipulate dates. This class provides multiple methods to set
 * date fields such as year, month, and day, and to retrieve the constructed date object. It is immutable, so each
 * setter method returns a new {@code DateBuilder} instance.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class DateBuilder {

    private static final ZoneOffset DEFAULT_OFFSET = OffsetDateTime.now().getOffset();

    /**
     * Year.
     */
    private int year;
    /**
     * Month, starting from 1.
     */
    private int month;
    /**
     * Week number. ISO8601 standard, 1 for Monday, 2 for Tuesday, and so on.
     */
    private int week;
    /**
     * Day of month.
     */
    private int day;
    /**
     * Hour.
     */
    private int hour;
    /**
     * Minute.
     */
    private int minute;
    /**
     * Second.
     */
    private int second;
    /**
     * Nanosecond.
     */
    private int nanosecond;
    /**
     * Unix timestamp (seconds).
     */
    private long unixsecond;
    /**
     * Millisecond.
     */
    private long millisecond;
    /**
     * Flag indicating if time zone offset has been set.
     */
    private boolean flag;
    /**
     * Time zone offset in minutes.
     */
    private int zoneOffset;
    /**
     * Time zone.
     */
    private TimeZone zone;
    /**
     * AM flag.
     */
    private boolean am;
    /**
     * PM flag.
     */
    private boolean pm;

    /**
     * Constructs a {@code DateBuilder} instance, resetting all values to default.
     */
    public DateBuilder() {
        reset();
    }

    /**
     * Creates and returns a new {@code DateBuilder} instance.
     *
     * @return A new {@code DateBuilder} instance.
     */
    public static DateBuilder of() {
        return new DateBuilder();
    }

    /**
     * Gets the year.
     *
     * @return The set year.
     */
    public int getYear() {
        return year;
    }

    /**
     * Sets the year.
     *
     * @param year The year to set.
     * @return This {@code DateBuilder} instance, supporting chained calls.
     */
    public DateBuilder setYear(final int year) {
        this.year = year;
        return this;
    }

    /**
     * Gets the month, starting from 1.
     *
     * @return The set month, starting from 1.
     */
    public int getMonth() {
        return month;
    }

    /**
     * Sets the month, starting from 1.
     *
     * @param month The month to set, starting from 1.
     * @return This {@code DateBuilder} instance.
     */
    public DateBuilder setMonth(final int month) {
        this.month = month;
        return this;
    }

    /**
     * Gets the current week number.
     *
     * @return The current week number.
     */
    public int getWeek() {
        return week;
    }

    /**
     * Sets the week number for the date builder. According to ISO8601 standard, 1 represents Monday, 2 represents
     * Tuesday, and so on.
     *
     * @param week The specified week number, typically used to construct a specific date object. ISO8601 standard, 1
     *             for Monday, 2 for Tuesday, and so on.
     * @return This {@code DateBuilder} instance.
     */
    public DateBuilder setWeek(final int week) {
        this.week = week;
        return this;
    }

    /**
     * Gets the day of the month from the current date object.
     *
     * @return An integer representing the day of the month.
     */
    public int getDay() {
        return day;
    }

    /**
     * Sets the day of the month in the date object.
     *
     * @param day The day to set, must be an integer.
     * @return This {@code DateBuilder} instance.
     */
    public DateBuilder setDay(final int day) {
        this.day = day;
        return this;
    }

    /**
     * Gets the hour from the current date object.
     *
     * @return The hour, as an integer.
     */
    public int getHour() {
        return hour;
    }

    /**
     * Sets the hour in the date object.
     *
     * @param hour The hour to set, must be an integer.
     * @return This {@code DateBuilder} instance.
     */
    public DateBuilder setHour(final int hour) {
        this.hour = hour;
        return this;
    }

    /**
     * Gets the minute from the current date builder.
     *
     * @return The set minute, as an integer.
     */
    public int getMinute() {
        return minute;
    }

    /**
     * Sets the minute in the date builder.
     *
     * @param minute The minute to set, must be an integer.
     * @return This {@code DateBuilder} instance.
     */
    public DateBuilder setMinute(final int minute) {
        this.minute = minute;
        return this;
    }

    /**
     * Gets the second from the current date-time object.
     *
     * @return The second from the current date-time object.
     */
    public int getSecond() {
        return second;
    }

    /**
     * Sets the second in the date-time object.
     *
     * @param second The second to set.
     * @return This {@code DateBuilder} instance.
     */
    public DateBuilder setSecond(final int second) {
        this.second = second;
        return this;
    }

    /**
     * Gets the nanosecond.
     *
     * @return The nanosecond of the current object.
     */
    public int getNanosecond() {
        return nanosecond;
    }

    /**
     * Sets the nanosecond.
     *
     * @param nanosecond The nanosecond to set.
     * @return This {@code DateBuilder} instance.
     */
    public DateBuilder setNanosecond(final int nanosecond) {
        this.nanosecond = nanosecond;
        return this;
    }

    /**
     * Gets the Unix timestamp (seconds).
     *
     * @return The Unix timestamp of the current object (in seconds).
     */
    public long getUnixsecond() {
        return unixsecond;
    }

    /**
     * Sets the Unix timestamp (seconds).
     *
     * @param unixsecond The Unix timestamp to set (in seconds).
     * @return This {@code DateBuilder} instance.
     */
    public DateBuilder setUnixsecond(final long unixsecond) {
        this.unixsecond = unixsecond;
        return this;
    }

    /**
     * Gets the timestamp (milliseconds).
     *
     * @return The timestamp of the current object (in milliseconds).
     */
    public long getMillisecond() {
        return millisecond;
    }

    /**
     * Sets the timestamp (milliseconds).
     *
     * @param millisecond The timestamp to set (in milliseconds).
     * @return This {@code DateBuilder} instance.
     */
    public DateBuilder setMillisecond(final long millisecond) {
        this.millisecond = millisecond;
        return this;
    }

    /**
     * Checks if the time zone offset has been set.
     *
     * @return {@code true} if the time zone offset has been set, {@code false} otherwise.
     */
    public boolean isFlag() {
        return flag;
    }

    /**
     * Sets the status of whether the time zone offset has been set.
     *
     * @param flag The status indicating whether the time zone offset has been set.
     * @return This {@code DateBuilder} instance.
     */
    public DateBuilder setFlag(final boolean flag) {
        this.flag = flag;
        return this;
    }

    /**
     * Gets the time zone offset.
     *
     * @return The set time zone offset.
     */
    public int getZoneOffset() {
        return zoneOffset;
    }

    /**
     * Sets the time zone offset.
     *
     * @param zoneOffset The time zone offset to set.
     * @return This {@code DateBuilder} instance.
     */
    public DateBuilder setZoneOffset(final int zoneOffset) {
        this.zoneOffset = zoneOffset;
        return this;
    }

    /**
     * Gets the time zone.
     *
     * @return The set time zone.
     */
    public TimeZone getZone() {
        return zone;
    }

    /**
     * Sets the time zone.
     *
     * @param zone The time zone to set.
     * @return This {@code DateBuilder} instance.
     */
    public DateBuilder setZone(final TimeZone zone) {
        this.zone = zone;
        return this;
    }

    /**
     * Checks if the current time is AM.
     *
     * @return {@code true} if currently set to AM, {@code false} otherwise.
     */
    public boolean isAm() {
        return am;
    }

    /**
     * Sets the AM status.
     *
     * @param am The status indicating whether it is AM.
     * @return This {@code DateBuilder} instance.
     */
    public DateBuilder setAm(final boolean am) {
        this.am = am;
        return this;
    }

    /**
     * Checks if the current time is PM.
     *
     * @return {@code true} if currently set to PM, {@code false} otherwise.
     */
    public boolean isPm() {
        return pm;
    }

    /**
     * Sets the PM status.
     *
     * @param pm The status indicating whether it is PM.
     * @return This {@code DateBuilder} instance.
     */
    public DateBuilder setPm(final boolean pm) {
        this.pm = pm;
        return this;
    }
    // endregion

    /**
     * Resets all values to their default.
     *
     * @return This {@code DateBuilder} instance.
     */
    public DateBuilder reset() {
        this.week = 1;
        this.year = 0;
        this.month = 1;
        this.day = 1;
        this.hour = 0;
        this.minute = 0;
        this.second = 0;
        this.nanosecond = 0;
        this.unixsecond = 0;
        this.millisecond = 0;
        this.am = false;
        this.pm = false;
        this.flag = false;
        this.zoneOffset = 0;
        this.zone = null;
        return this;
    }

    /**
     * Converts the current time object to a {@link DateTime} type. This method uses different conversion strategies
     * based on whether the time zone offset is set.
     * <ul>
     * <li>If the time zone offset is not set, it creates a {@link DateTime} via {@link Calendar} without time zone
     * conversion.</li>
     * <li>If the time zone offset is set, it converts to {@link OffsetDateTime} and then to a {@link DateTime} in the
     * local time zone.</li>
     * </ul>
     *
     * @return A {@link DateTime} object representing the current time.
     */
    public DateTime toDate() {
        if (!flag) {
            // Time zone offset not set, convert using Calendar without reading time zone
            return new DateTime(toCalendar().getTimeInMillis());
        }
        // Time zone offset set, convert directly to Date object
        return new DateTime(toOffsetDateTime().toInstant().toEpochMilli());
    }

    /**
     * Converts to a {@link DateTime} with time zone information. This method preserves the original timestamp and time
     * zone information.
     *
     * @return A {@link DateTime} object.
     */
    public DateTime toZonedDateTime() {
        final Calendar calendar = toCalendar();
        return new DateTime(calendar.getTimeInMillis(), calendar.getTimeZone());
    }

    /**
     * Converts the current object's date and time information to a {@link Calendar} instance. If `unixsecond` is not 0,
     * the Calendar will be constructed based on the Unix timestamp (seconds) and nanosecond offset. Otherwise, the
     * Calendar's time zone will be set based on the provided `zone` or `zoneOffset` information. Finally, year, month,
     * day, hour, minute, second, and millisecond information will be set.
     *
     * @return A {@link Calendar} instance constructed from the current date and time information.
     * @throws DateException If the time zone offset cannot be converted to a valid time zone ID.
     */
    public Calendar toCalendar() {
        this.prepare();
        final Calendar calendar = Calendar.getInstance(); // Get a Calendar instance

        // Set time zone
        if (zone != null) {
            calendar.setTimeZone(zone); // Use the specified time zone
        } else if (flag) { // If time zone offset is set
            final TimeZone timeZone = ZoneKit.getTimeZoneByOffset(zoneOffset, TimeUnit.SECONDS);
            if (null == timeZone) { // If no valid time zone ID is found
                throw new DateException("Invalid zoneOffset: {}", this.zoneOffset);
            }
            calendar.setTimeZone(timeZone);
        }

        // If milliseconds are not 0, use milliseconds to set the time directly
        if (millisecond != 0) {
            calendar.setTimeInMillis(millisecond);
            return calendar;
        }

        // If there is a Unix timestamp, set the time accordingly
        if (unixsecond != 0) {
            calendar.setTimeInMillis(unixsecond * 1000 + nanosecond / 1_000_000); // Set milliseconds corresponding to
                                                                                  // the timestamp
            return calendar;
        }

        // Set date and time fields
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1); // Calendar months are 0-indexed
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, nanosecond / 1_000_000); // Convert nanoseconds to milliseconds
        return calendar;
    }

    /**
     * Converts the current object's date and time information to a {@link LocalDateTime}. This method creates and
     * returns a {@code LocalDateTime} instance based on the object's time information (year, month, day, hour, minute,
     * second, nanosecond) and time zone information (if present). Time zone information can be seconds from Unix
     * timestamp (`unixsecond`), an explicitly set time zone offset (`zoneOffsetSetted`), or a default time zone (`zone
     * != null`).
     *
     * @return A {@link LocalDateTime} instance representing the current object's date and time.
     */
    public LocalDateTime toLocalDateTime() {
        this.prepare();

        if (millisecond > 0) {
            final Instant instant = Instant.ofEpochMilli(millisecond);
            return LocalDateTime.ofEpochSecond(instant.getEpochSecond(), instant.getNano(), DEFAULT_OFFSET);
        }

        // If unixsecond is greater than 0, create LocalDateTime using Unix timestamp
        if (unixsecond > 0) {
            return LocalDateTime.ofEpochSecond(unixsecond, nanosecond, DEFAULT_OFFSET);
        }

        // Create LocalDateTime instance using year, month, day, hour, minute, second, nanosecond information
        final LocalDateTime dateTime = LocalDateTime.of(year, month, day, hour, minute, second, nanosecond);

        int zoneSecond = 0; // Used to store time zone offset in seconds

        // If time zone is set, calculate time zone offset
        if (zone != null) {
            zoneSecond = (TimeZone.getDefault().getRawOffset() - zone.getRawOffset()) / 1000;
        }

        // If explicit time zone offset is set, calculate time zone offset
        if (flag) {
            zoneSecond = TimeZone.getDefault().getRawOffset() / 1000 - zoneOffset * 60;
        }

        // If there is a time zone offset, adjust LocalDateTime and return; otherwise, return the original LocalDateTime
        // instance directly
        return zoneSecond == 0 ? dateTime : dateTime.plusSeconds(zoneSecond);
    }

    /**
     * Converts the current object to an {@link OffsetDateTime}. This method constructs an {@code OffsetDateTime} based
     * on `unixsecond`, time zone offset, or time zone. If `unixsecond` is greater than 0, it will be used with
     * nanoseconds to create a UTC time. If a time zone offset is set, that offset will be used to construct the
     * {@link OffsetDateTime}. If a time zone is set, that time zone will be used to construct the
     * {@link OffsetDateTime}. If none of the above information is set, it defaults to creating an
     * {@code OffsetDateTime} from UTC timestamp 0.
     *
     * @return An {@link OffsetDateTime} object representing the current time.
     */
    public OffsetDateTime toOffsetDateTime() {
        this.prepare(); // Preparation work, may involve some initialization or data processing

        if (millisecond > 0) {
            return OffsetDateTime.ofInstant(Instant.ofEpochMilli(millisecond), ZoneKit.ZONE_ID_UTC);
        }

        if (unixsecond > 0) {
            // If Unix timestamp is set, use it and nanoseconds to create UTC time
            return OffsetDateTime.ofInstant(Instant.ofEpochSecond(unixsecond, nanosecond), ZoneKit.ZONE_ID_UTC);
        }
        final LocalDateTime dateTime = LocalDateTime.of(year, month, day, hour, minute, second, nanosecond); // Create
                                                                                                             // LocalDateTime
                                                                                                             // object

        // Check if time zone offset is set
        if (flag) {
            final ZoneOffset offset = ZoneOffset.ofHoursMinutes(zoneOffset / 60, zoneOffset % 60); // Create ZoneOffset
                                                                                                   // based on offset
            return dateTime.atOffset(offset); // Construct OffsetDateTime using time zone offset
        }

        // Check if time zone is set
        if (zone != null) {
            return dateTime.atZone(zone.toZoneId()).toOffsetDateTime(); // Construct OffsetDateTime using time zone
        }

        // By default, create OffsetDateTime from UTC timestamp 0
        return dateTime.atZone(ZoneOffset.ofHoursMinutes(0, 0)).toOffsetDateTime();
    }

    /**
     * Converts 12-hour format to 24-hour format based on AM/PM settings.
     */
    private void prepare() {
        // If set to AM and hour is 12, adjust hour to 0
        if (am && hour == 12) {
            this.hour = 0;
        }
        // If set to PM and hour is not 12, add 12 to hour
        if (pm && hour != 12) {
            this.hour += 12;
        }
    }

}
