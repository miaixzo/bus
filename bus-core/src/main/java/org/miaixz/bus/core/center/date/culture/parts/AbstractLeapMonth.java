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
 * Abstract base class for months that support leap months.
 *
 * @author Kimi Liu
 */
public abstract class AbstractLeapMonth extends AbstractMonth {

    /**
     * Whether this month is leap.
     */
    protected boolean leap;

    /**
     * Constructs a leap-month-aware month.
     *
     * @param year  the year value
     * @param month the month value, negative when this is a leap month
     */
    public AbstractLeapMonth(final int year, final int month) {
        super(year, Math.abs(month));
        this.leap = month < 0;
    }

    /**
     * Returns whether this month is leap.
     *
     * @return true if this is a leap month
     */
    public boolean isLeap() {
        return leap;
    }

    /**
     * Gets the month value.
     *
     * @return the month, negative when this is a leap month
     * @deprecated use {@link #getMonthValue()}
     */
    @Deprecated
    public int getMonthWithLeap() {
        return getMonthValue();
    }

    @Override
    public int getMonthValue() {
        return leap ? -month : month;
    }

    /**
     * Gets the index in the year.
     *
     * @return 0-based index in the year
     */
    public int getIndexInYear() {
        int index = month - 1;
        if (leap) {
            index += 1;
        } else {
            final int leapMonth = getAbstractYear().getLeapMonth();
            if (leapMonth > 0 && month > leapMonth) {
                index += 1;
            }
        }
        return index;
    }

    @Override
    public AbstractLeapMonth next(final int n) {
        int ty = getYear();
        int tm = getMonthValue();
        if (n != 0) {
            int m = getIndexInYear() + 1 + n;
            AbstractYear y = getAbstractYear();
            if (n > 0) {
                int monthCount = y.getMonthCount();
                while (m > monthCount) {
                    m -= monthCount;
                    y = y.next(1);
                    monthCount = y.getMonthCount();
                }
            } else {
                while (m <= 0) {
                    y = y.next(-1);
                    m += y.getMonthCount();
                }
            }
            boolean leap = false;
            final int leapMonth = y.getLeapMonth();
            if (leapMonth > 0) {
                if (m == leapMonth + 1) {
                    leap = true;
                }
                if (m > leapMonth) {
                    m--;
                }
            }
            ty = y.getYear();
            tm = leap ? -m : m;
        }
        return new AbstractLeapMonth(ty, tm) {

            @Override
            public int getDayCount() {
                return 0;
            }

            @Override
            public AbstractDay getFirstDay() {
                return null;
            }

            @Override
            public AbstractYear getAbstractYear() {
                return null;
            }
        };
    }

}
