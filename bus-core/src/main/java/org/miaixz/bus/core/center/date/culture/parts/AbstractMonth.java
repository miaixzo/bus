/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.center.date.culture.parts;

/**
 * Abstract base class for months.
 *
 * @author Kimi Liu
 */
public abstract class AbstractMonth extends MonthParts {

    /**
     * Constructs a month.
     *
     * @param year  the year value
     * @param month the month value
     */
    public AbstractMonth(final int year, final int month) {
        super(year, month);
    }

    /**
     * Gets the week count in this month.
     *
     * @param start the starting weekday, 1-6 for Monday-Saturday and 0 for Sunday
     * @return the week count
     */
    public int getWeekCount(final int start) {
        return (int) Math.ceil((indexOf(getFirstDay().getWeek().getIndex() - start, 7) + getDayCount()) / 7D);
    }

    @Override
    public abstract AbstractMonth next(int n);

    /**
     * Gets the day count.
     *
     * @return the day count
     */
    public abstract int getDayCount();

    /**
     * Gets the first day in the month.
     *
     * @return the first day
     */
    public abstract AbstractDay getFirstDay();

    /**
     * Gets the month value.
     *
     * @return the month, negative when this is a leap month
     */
    public int getMonthValue() {
        return month;
    }

    /**
     * Gets the abstract year.
     *
     * @return the abstract year
     */
    public abstract AbstractYear getAbstractYear();

    @Override
    public String toString() {
        return getAbstractYear() + getName();
    }

}
