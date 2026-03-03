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
