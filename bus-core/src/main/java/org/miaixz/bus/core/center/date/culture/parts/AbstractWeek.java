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
 * Abstract base class for weeks.
 *
 * @author Kimi Liu
 */
public abstract class AbstractWeek extends WeekParts {

    /**
     * Constructs a week.
     *
     * @param year  the year value
     * @param month the month value
     * @param index the week index
     * @param start the starting weekday
     */
    public AbstractWeek(final int year, final int month, final int index, final int start) {
        super(year, month, index, start);
    }

    /**
     * Gets the abstract month.
     *
     * @return the abstract month
     */
    public abstract AbstractMonth getAbstractMonth();

    @Override
    public AbstractWeek next(final int n) {
        int d = index + n;
        AbstractMonth m = getAbstractMonth();
        if (n > 0) {
            int weekCount = m.getWeekCount(start);
            while (d >= weekCount) {
                d -= weekCount;
                m = m.next(1);
                if (m.getFirstDay().getWeek().getIndex() != start) {
                    d += 1;
                }
                weekCount = m.getWeekCount(start);
            }
        } else if (n < 0) {
            while (d < 0) {
                if (m.getFirstDay().getWeek().getIndex() != start) {
                    d -= 1;
                }
                m = m.next(-1);
                d += m.getWeekCount(start);
            }
        }
        final AbstractMonth month = m;
        final int index = d;
        return new AbstractWeek(month.getYear(), month.getMonthValue(), index, start) {

            @Override
            public AbstractMonth getAbstractMonth() {
                return null;
            }
        };
    }

    @Override
    public String toString() {
        return getAbstractMonth() + getName();
    }

}
