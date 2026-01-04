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
package org.miaixz.bus.core.center.date.culture.eightchar;

import org.miaixz.bus.core.center.date.culture.solar.SolarTime;

/**
 * Represents detailed information about the "Child Limit" (童限) period in Chinese astrology.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ChildLimitInfo {

    /**
     * The start time (birth time) of the Child Limit.
     */
    protected SolarTime startTime;

    /**
     * The end time (start of Grand Fortune) of the Child Limit.
     */
    protected SolarTime endTime;

    /**
     * The number of years in the Child Limit.
     */
    protected int yearCount;

    /**
     * The number of months in the Child Limit.
     */
    protected int monthCount;

    /**
     * The number of days in the Child Limit.
     */
    protected int dayCount;

    /**
     * The number of hours in the Child Limit.
     */
    protected int hourCount;

    /**
     * The number of minutes in the Child Limit.
     */
    protected int minuteCount;

    /**
     * Constructs a {@code ChildLimitInfo} instance with the specified details.
     *
     * @param startTime   The start time (birth time).
     * @param endTime     The end time (start of Grand Fortune).
     * @param yearCount   The number of years.
     * @param monthCount  The number of months.
     * @param dayCount    The number of days.
     * @param hourCount   The number of hours.
     * @param minuteCount The number of minutes.
     */
    public ChildLimitInfo(SolarTime startTime, SolarTime endTime, int yearCount, int monthCount, int dayCount,
            int hourCount, int minuteCount) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.yearCount = yearCount;
        this.monthCount = monthCount;
        this.dayCount = dayCount;
        this.hourCount = hourCount;
        this.minuteCount = minuteCount;
    }

    /**
     * Gets the start time (birth time) of the Child Limit.
     *
     * @return The {@link SolarTime} instance representing the start time.
     */
    public SolarTime getStartTime() {
        return startTime;
    }

    /**
     * Gets the end time (start of Grand Fortune) of the Child Limit.
     *
     * @return The {@link SolarTime} instance representing the end time.
     */
    public SolarTime getEndTime() {
        return endTime;
    }

    /**
     * Gets the number of years in the Child Limit.
     *
     * @return The number of years.
     */
    public int getYearCount() {
        return yearCount;
    }

    /**
     * Gets the number of months in the Child Limit.
     *
     * @return The number of months.
     */
    public int getMonthCount() {
        return monthCount;
    }

    /**
     * Gets the number of days in the Child Limit.
     *
     * @return The number of days.
     */
    public int getDayCount() {
        return dayCount;
    }

    /**
     * Gets the number of hours in the Child Limit.
     *
     * @return The number of hours.
     */
    public int getHourCount() {
        return hourCount;
    }

    /**
     * Gets the number of minutes in the Child Limit.
     *
     * @return The number of minutes.
     */
    public int getMinuteCount() {
        return minuteCount;
    }

}
