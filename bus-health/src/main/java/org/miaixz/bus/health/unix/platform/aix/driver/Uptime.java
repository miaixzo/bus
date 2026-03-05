/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.           ~
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
package org.miaixz.bus.health.unix.platform.aix.driver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;

/**
 * Utility to query up time.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class Uptime {

    private static final long MINUTE_MS = 60L * 1000L;
    private static final long HOUR_MS = 60L * MINUTE_MS;
    private static final long DAY_MS = 24L * HOUR_MS;

    // sample format:
    // 18:36pm up 10 days 8:11, 2 users, load average: 3.14, 2.74, 2.41

    private static final Pattern UPTIME_FORMAT_AIX = Pattern
            .compile(".*\\sup\\s+((\\d+)\\s+days?,?\\s+)?\\b((\\d+):)?(\\d+)(\\s+min(utes?)?)?,\\s+\\d+\\s+user.+"); // NOSONAR:squid:S5843

    private Uptime() {
    }

    /**
     * Query {@code uptime} to get up time
     *
     * @return Up time in milliseconds
     */
    public static long queryUpTime() {
        long uptime = 0L;
        String s = Executor.getFirstAnswer("uptime");
        if (s.isEmpty()) {
            s = Executor.getFirstAnswer("w");
        }
        if (s.isEmpty()) {
            s = Executor.getFirstAnswer("/usr/bin/uptime");
        }
        Matcher m = UPTIME_FORMAT_AIX.matcher(s);
        if (m.matches()) {
            if (m.group(2) != null) {
                uptime += Parsing.parseLongOrDefault(m.group(2), 0L) * DAY_MS;
            }
            if (m.group(4) != null) {
                uptime += Parsing.parseLongOrDefault(m.group(4), 0L) * HOUR_MS;
            }
            uptime += Parsing.parseLongOrDefault(m.group(5), 0L) * MINUTE_MS;
        }
        return uptime;
    }

}
