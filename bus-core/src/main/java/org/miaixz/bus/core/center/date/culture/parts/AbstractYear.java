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
 * Abstract base class for years.
 *
 * @author Kimi Liu
 */
public abstract class AbstractYear extends YearParts {

    /**
     * Constructs a year.
     *
     * @param year the year value
     */
    public AbstractYear(final int year) {
        super(year);
    }

    /**
     * Gets the number of months in the year.
     *
     * @return the month count
     */
    public int getMonthCount() {
        return getLeapMonth() < 1 ? 12 : 13;
    }

    /**
     * Gets the leap month.
     *
     * @return leap month number, or 0 when there is no leap month
     */
    public int getLeapMonth() {
        return 0;
    }

    @Override
    public String getName() {
        return year + "年";
    }

    @Override
    public abstract AbstractYear next(int n);

}
