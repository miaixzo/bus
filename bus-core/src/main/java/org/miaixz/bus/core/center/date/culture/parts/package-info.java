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
 * Provides date component part classes for cultural calendar implementations.
 *
 * <p>
 * This package contains abstract base classes that define hierarchical date components (year, month, day, hour, minute,
 * second, week) for various cultural and traditional calendar systems. These classes form a component hierarchy:
 * </p>
 *
 * <ul>
 * <li>{@link org.miaixz.bus.core.center.date.culture.parts.YearParts} - Base class containing year field</li>
 * <li>{@link org.miaixz.bus.core.center.date.culture.parts.MonthParts} - Extends YearParts, adds month field</li>
 * <li>{@link org.miaixz.bus.core.center.date.culture.parts.DayParts} - Extends MonthParts, adds day field</li>
 * <li>{@link org.miaixz.bus.core.center.date.culture.parts.SecondParts} - Extends DayParts, adds hour, minute, second
 * fields</li>
 * <li>{@link org.miaixz.bus.core.center.date.culture.parts.WeekParts} - Extends MonthParts, adds week-related
 * fields</li>
 * </ul>
 *
 * <p>
 * These part classes are used as building blocks for implementing various cultural calendars including Chinese lunar
 * calendar, Tibetan Rabjung calendar, and other traditional calendar systems.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.core.center.date.culture.parts;
