/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org OSHI and other contributors.               ~
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
package org.miaixz.bus.health.mac.software;

import java.util.*;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.software.ApplicationInfo;
import org.miaixz.bus.health.mac.jna.CoreFoundation;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public final class MacInstalledApps {

    private static final String COLON = Symbol.COLON;
    private static final CoreFoundation CF = CoreFoundation.INSTANCE;

    private MacInstalledApps() {
    }

    public static List<ApplicationInfo> queryInstalledApps() {
        List<String> output = Executor.runNative("system_profiler SPApplicationsDataType");
        return parseMacAppInfo(output);
    }

    private static List<ApplicationInfo> parseMacAppInfo(List<String> lines) {
        Set<ApplicationInfo> appInfoSet = new LinkedHashSet<>();
        String appName = null;
        Map<String, String> appDetails = null;
        boolean collectingAppDetails = false;
        String dateFormat = getLocaleDateTimeFormat(CoreFoundation.CFDateFormatterStyle.kCFDateFormatterShortStyle);

        for (String line : lines) {
            line = line.trim();

            // Check for app name, ends with ":"
            if (line.endsWith(COLON)) {
                // When app and appDetails are not empty then we reached the next app, add it to the list
                if (appName != null && !appDetails.isEmpty()) {
                    appInfoSet.add(createAppInfo(appName, appDetails, dateFormat));
                }

                // store app name and proceed with collecting app details
                appName = line.substring(0, line.length() - 1);
                appDetails = new HashMap<>();
                collectingAppDetails = true;
                continue;
            }

            // Process app details
            if (collectingAppDetails && line.contains(COLON)) {
                int colonIndex = line.indexOf(COLON);
                String key = line.substring(0, colonIndex).trim();
                String value = line.substring(colonIndex + 1).trim();
                appDetails.put(key, value);
            }
        }

        return new ArrayList<>(appInfoSet);
    }

    private static ApplicationInfo createAppInfo(String name, Map<String, String> details, String dateFormat) {
        String obtainedFrom = Parsing.getValueOrUnknown(details, "Obtained from");
        String signedBy = Parsing.getValueOrUnknown(details, "Signed by");
        String vendor = (obtainedFrom.equals("Identified Developer")) ? signedBy : obtainedFrom;

        String lastModified = details.getOrDefault("Last Modified", Normal.UNKNOWN);
        long lastModifiedEpoch = Parsing.parseDateToEpoch(lastModified, dateFormat);

        // Additional info map
        Map<String, String> additionalInfo = new LinkedHashMap<>();
        additionalInfo.put("Kind", Parsing.getValueOrUnknown(details, "Kind"));
        additionalInfo.put("Location", Parsing.getValueOrUnknown(details, "Location"));
        additionalInfo.put("Get Info String", Parsing.getValueOrUnknown(details, "Get Info String"));

        return new ApplicationInfo(name, Parsing.getValueOrUnknown(details, "Version"), vendor, lastModifiedEpoch,
                additionalInfo);
    }

    private static String getLocaleDateTimeFormat(CoreFoundation.CFDateFormatterStyle style) {
        CoreFoundation.CFIndex styleIndex = style.index();
        CoreFoundation.CFLocale locale = CF.CFLocaleCopyCurrent();
        try {
            CoreFoundation.CFDateFormatter formatter = CF.CFDateFormatterCreate(null, locale, styleIndex, styleIndex);
            if (formatter == null) {
                return "";
            }
            try {
                CoreFoundation.CFStringRef format = CF.CFDateFormatterGetFormat(formatter);
                return (format == null) ? "" : format.stringValue();
            } finally {
                CF.CFRelease(formatter);
            }
        } finally {
            CF.CFRelease(locale);
        }
    }

}
