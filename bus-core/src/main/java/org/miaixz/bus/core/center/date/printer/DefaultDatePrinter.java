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
package org.miaixz.bus.core.center.date.printer;

import java.io.Serial;
import java.io.Serializable;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Default date basic information class, providing default date format, time zone, and locale settings.
 * <ul>
 * <li>{@link #getPattern()} returns {@code null}</li>
 * <li>{@link #getTimeZone()} returns {@link TimeZone#getDefault()}</li>
 * <li>{@link #getLocale()} returns {@link Locale#getDefault()}</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DefaultDatePrinter implements DatePrinter, Serializable {

    /**
     * Constructs a new DefaultDatePrinter. Utility class constructor for static access.
     */
    private DefaultDatePrinter() {
    }

    @Serial
    private static final long serialVersionUID = 2852257378058L;

    /**
     * Gets the date format pattern.
     *
     * @return Always returns null.
     */
    @Override
    public String getPattern() {
        return null;
    }

    /**
     * Gets the time zone.
     *
     * @return The default time zone.
     */
    @Override
    public TimeZone getTimeZone() {
        return TimeZone.getDefault();
    }

    /**
     * Gets the locale settings.
     *
     * @return The default locale.
     */
    @Override
    public Locale getLocale() {
        return Locale.getDefault();
    }

}
