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
package org.miaixz.bus.health.unix.platform.aix.software;

import java.util.*;
import java.util.regex.Pattern;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.software.ApplicationInfo;

/**
 * Utility class for querying installed applications on AIX operating systems.
 * <p>
 * This class provides methods to retrieve information about installed software by executing the AIX-specific
 * {@code lslpp} command and parsing its output.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class AixInstalledApps {

    private static final Pattern COLON_PATTERN = Pattern.compile(Symbol.COLON);

    public static List<ApplicationInfo> queryInstalledApps() {
        // https://www.ibm.com/docs/en/aix/7.1.0?topic=l-lslpp-command
        List<String> output = Executor.runNative("lslpp -Lc");
        return parseAixAppInfo(output);
    }

    private static List<ApplicationInfo> parseAixAppInfo(List<String> lines) {
        Set<ApplicationInfo> appInfoSet = new LinkedHashSet<>();
        String architecture = System.getProperty("os.arch");
        boolean isFirstLine = true;
        for (String line : lines) {
            if (isFirstLine) {
                isFirstLine = false;
                continue; // Skip the first line as it consists column names
            }
            /*
             * Sample output: (1) devices.chrp.IBM.lhca:devices.chrp.IBM.lhca.rte:7.1.5.30: : :C:F:Infiniband Logical
             * HCA Runtime Environment: : : : : : :0:0:/:1837 (2) bash:bash-5.0.18-1:5.0.18-1: : :C:R:The GNU Bourne
             * Again shell (bash) version 5.0.18: :/bin/rpm -e bash: : : : :0: :(none):Fri Sep 18 15:53:11 2020
             */
            // split by the colon character
            String[] parts = COLON_PATTERN.split(line, -1); // -1 to keep empty fields
            String name = Parsing.getStringValueOrUnknown(parts[0]);
            if (name.equals(Normal.UNKNOWN)) {
                continue;
            }
            String version = Parsing.getStringValueOrUnknown(parts[2]);
            String vendor = Normal.UNKNOWN; // lslpp command does not provide vendor info, hence, assigning as
            // unknown
            // Build Date is of two formats YYWW and EEE MMM dd HH:mm:ss yyyy
            String buildDate = Parsing.getStringValueOrUnknown(parts[17]);
            long timestamp = 0;
            if (!buildDate.equals(Normal.UNKNOWN)) {
                if (buildDate.matches("\\d{4}")) {
                    // Convert to ISO week date string (e.g., 1125 -> 2011-W25-2 for Monday)
                    String isoWeekString = "20" + buildDate.substring(0, 2) + "-W" + buildDate.substring(2) + "-2";
                    timestamp = Parsing.parseDateToEpoch(isoWeekString, "YYYY-'W'ww-e");
                } else {
                    timestamp = Parsing.parseDateToEpoch(buildDate, "EEE MMM dd HH:mm:ss yyyy");
                }
            }
            String description = Parsing.getStringValueOrUnknown(parts[7].trim());
            String installPath = Parsing.getStringValueOrUnknown(parts[16].trim());
            Map<String, String> additionalInfo = new LinkedHashMap<>();
            additionalInfo.put("architecture", architecture);
            additionalInfo.put("description", description);
            additionalInfo.put("installPath", installPath);
            ApplicationInfo app = new ApplicationInfo(name, version, vendor, timestamp, additionalInfo);
            appInfoSet.add(app);
        }

        return new ArrayList<>(appInfoSet);
    }

}
