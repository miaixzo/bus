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
package org.miaixz.bus.core.center.date.format.parser;

import java.io.Serial;
import java.io.Serializable;

import org.miaixz.bus.core.center.date.DateTime;
import org.miaixz.bus.core.center.date.Formatter;
import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.PatternKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Parses time strings, defaulting the date to the current day. Supports formats similar to:
 *
 * <pre>
 *   HH:mm:ss
 *   HH:mm
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TimeParser implements PredicateDateParser, Serializable {

    @Serial
    private static final long serialVersionUID = 2852257133063L;

    /**
     * Singleton instance of {@code TimeParser}.
     */
    public static final TimeParser INSTANCE = new TimeParser();

    /**
     * Tests if the given string matches a time format.
     *
     * @param date The time string to test.
     * @return {@code true} if the string matches a time format, {@code false} otherwise.
     */
    @Override
    public boolean test(final CharSequence date) {
        return PatternKit.isMatch(Pattern.TIME_PATTERN, date);
    }

    /**
     * Parses a time string, defaulting the date to the current day.
     *
     * @param source The time string to parse.
     * @return The parsed {@link DateTime} object.
     */
    @Override
    public DateTime parse(CharSequence source) {
        source = StringKit.replaceChars(source, "时分秒", Symbol.COLON);
        source = StringKit.format("{} {}", DateKit.formatToday(), source);
        if (1 == StringKit.count(source, Symbol.C_COLON)) {
            // Time format is HH:mm
            return new DateTime(source, Fields.NORM_DATETIME_MINUTE);
        } else {
            // Time format is HH:mm:ss
            return new DateTime(source, Formatter.NORM_DATETIME_FORMAT);
        }
    }

}
