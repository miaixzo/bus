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
/**
 * Provides classes for Gregorian (Solar) calendar operations.
 * <p>
 * This package contains classes representing various components of the Gregorian calendar system, including years,
 * quarters, half-years, months, weeks, days, and times. It also provides support for solar terms (the 24 seasonal
 * periods), solar festivals, and conversions between the Gregorian calendar and other calendar systems (such as Lunar
 * and Tibetan calendars).
 * </p>
 * <p>
 * Key classes include:
 * <ul>
 * <li>{@link org.miaixz.bus.core.center.date.culture.solar.SolarYear} - Represents a year in the Gregorian
 * calendar</li>
 * <li>{@link org.miaixz.bus.core.center.date.culture.solar.SolarQuarter} - Represents a quarter (3-month period)</li>
 * <li>{@link org.miaixz.bus.core.center.date.culture.solar.SolarMonth} - Represents a month</li>
 * <li>{@link org.miaixz.bus.core.center.date.culture.solar.SolarWeek} - Represents a week</li>
 * <li>{@link org.miaixz.bus.core.center.date.culture.solar.SolarDay} - Represents a day</li>
 * <li>{@link org.miaixz.bus.core.center.date.culture.solar.SolarTime} - Represents a specific time (hour, minute,
 * second)</li>
 * <li>{@link org.miaixz.bus.core.center.date.culture.solar.SolarTerms} - Represents the 24 solar terms</li>
 * <li>{@link org.miaixz.bus.core.center.date.culture.solar.SolarFestival} - Represents modern Gregorian festivals</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.core.center.date.culture.solar;
