/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
