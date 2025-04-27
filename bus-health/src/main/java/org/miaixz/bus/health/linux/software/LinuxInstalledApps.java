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
package org.miaixz.bus.health.linux.software;

import java.util.*;
import java.util.regex.Pattern;

import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.software.ApplicationInfo;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public final class LinuxInstalledApps {

    private static final Pattern PIPE_PATTERN = Pattern.compile("\\|");
    private static final Map<String, String> PACKAGE_MANAGER_COMMANDS = initializePackageManagerCommands();

    private LinuxInstalledApps() {
    }

    private static Map<String, String> initializePackageManagerCommands() {
        Map<String, String> commands = new HashMap<>();

        if (isPackageManagerAvailable("dpkg")) {
            commands.put("dpkg",
                    "dpkg-query -W -f=${Package}|${Version}|${Architecture}|${Installed-Size}|${db-fsys:Last-Modified}|${Maintainer}|${Source}|${Homepage}\\n");
        } else if (isPackageManagerAvailable("rpm")) {
            commands.put("rpm",
                    "rpm -qa --queryformat %{NAME}|%{VERSION}-%{RELEASE}|%{ARCH}|%{SIZE}|%{INSTALLTIME}|%{PACKAGER}|%{SOURCERPM}|%{URL}\\n");
        }

        return commands;
    }

    /**
     * Retrieves the list of installed applications on a Linux system. This method determines the appropriate package
     * manager and parses the installed application details.
     *
     * @return A list of {@link ApplicationInfo} objects representing installed applications.
     */
    public static List<ApplicationInfo> queryInstalledApps() {
        List<String> output = fetchInstalledApps();
        return parseLinuxAppInfo(output);
    }

    /**
     * Fetches the list of installed applications by executing the appropriate package manager command. The package
     * manager is determined during class initialization and stored in {@code PACKAGE_MANAGER_COMMANDS}. If no supported
     * package manager is found, an empty list is returned.
     *
     * @return A list of strings, where each entry represents an installed application with its details. Returns an
     *         empty list if no supported package manager is available.
     */
    private static List<String> fetchInstalledApps() {
        if (PACKAGE_MANAGER_COMMANDS.isEmpty()) {
            return Collections.emptyList();
        }

        // Get the first available package manager's command
        String command = PACKAGE_MANAGER_COMMANDS.values().iterator().next();
        return Executor.runNative(command);
    }

    private static boolean isPackageManagerAvailable(String packageManager) {
        List<String> result = Executor.runNative(packageManager + " --version");
        // If the command executes fine the result is non-empty else empty
        return !result.isEmpty();
    }

    private static List<ApplicationInfo> parseLinuxAppInfo(List<String> output) {
        List<ApplicationInfo> appInfoList = new ArrayList<>();

        for (String line : output) {
            // split by the pipe character
            String[] parts = PIPE_PATTERN.split(line, -1); // -1 to keep empty fields

            // Check if we have all 8 fields
            if (parts.length >= 8) {
                // Additional info map
                Map<String, String> additionalInfo = new HashMap<>();
                additionalInfo.put("architecture", Parsing.getStringValueOrUnknown(parts[2]));
                additionalInfo.put("installedSize", String.valueOf(Parsing.parseLongOrDefault(parts[3], 0L)));
                additionalInfo.put("source", Parsing.getStringValueOrUnknown(parts[6]));
                additionalInfo.put("homepage", Parsing.getStringValueOrUnknown(parts[7]));

                ApplicationInfo app = new ApplicationInfo(Parsing.getStringValueOrUnknown(parts[0]), // Package name
                        Parsing.getStringValueOrUnknown(parts[1]), // Version
                        Parsing.getStringValueOrUnknown(parts[5]), // Vendor
                        Parsing.parseLongOrDefault(parts[4], 0L), // Date Epoch
                        additionalInfo);

                appInfoList.add(app);
            }
        }

        return appInfoList;
    }
}
