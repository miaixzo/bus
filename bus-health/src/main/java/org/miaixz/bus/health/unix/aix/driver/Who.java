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
package org.miaixz.bus.health.unix.aix.driver;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;

/**
 * Queries logged in users.
 *
 * @author Kimi Liu
 */
@ThreadSafe
public class Who {

    // sample format:
    // system boot 2020-06-16 09:12
    /**
     * The BOOT_FORMAT_AIX constant.
     */
    private static final Pattern BOOT_FORMAT_AIX = Pattern.compile("\\D+(\\d{4}-\\d{2}-\\d{2})\\s+(\\d{2}:\\d{2}).*");

    /**
     * The BOOT_DATE_FORMAT_AIX constant.
     */
    private static final DateTimeFormatter BOOT_DATE_FORMAT_AIX = DateTimeFormatter
            .ofPattern(Fields.NORM_DATETIME_MINUTE, Locale.ROOT);

    /**
     * The BOOT_FORMAT_AIX_NO_YEAR constant.
     */
    private static final Pattern BOOT_FORMAT_AIX_NO_YEAR = Pattern
            .compile("\\D+?([A-Z][a-z]{2})\\s+(\\d{1,2})\\s+(\\d{2}:\\d{2}).*");

    /**
     * The BOOT_NO_YEAR_PATTERN_AIX constant.
     */
    private static final String BOOT_NO_YEAR_PATTERN_AIX = "MMM d HH:mm";

    /**
     * Creates a new Who instance.
     */
    public Who() {
        // No initialization required.
    }

    /**
     * Query {@code who -b} to get boot time
     *
     * @return Boot time in milliseconds since the epoch
     */
    public static long queryBootTime() {
        String s = Executor.getFirstAnswer("who -b");
        if (s.isEmpty()) {
            s = Executor.getFirstAnswer("/usr/bin/who -b");
        }
        Matcher m = BOOT_FORMAT_AIX.matcher(s);
        if (m.matches()) {
            try {
                return LocalDateTime.parse(m.group(1) + Symbol.SPACE + m.group(2), BOOT_DATE_FORMAT_AIX)
                        .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            } catch (DateTimeParseException | NullPointerException e) {
                // Shouldn't happen with regex matching
            }
        }
        m = BOOT_FORMAT_AIX_NO_YEAR.matcher(s);
        if (m.matches()) {
            return Parsing.parseYearlessDateToEpoch(
                    m.group(1) + Symbol.SPACE + m.group(2) + Symbol.SPACE + m.group(3),
                    BOOT_NO_YEAR_PATTERN_AIX,
                    LocalDateTime.now(ZoneId.systemDefault()));
        }
        return 0L;
    }

}
