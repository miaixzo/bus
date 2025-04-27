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
package org.miaixz.bus.health.windows.driver.registry;

import java.util.*;

import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.software.ApplicationInfo;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinReg;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public final class InstalledAppsData {

    private InstalledAppsData() {
    }

    private static final Map<WinReg.HKEY, List<String>> REGISTRY_PATHS = new HashMap<>();

    static {
        REGISTRY_PATHS.put(WinReg.HKEY_LOCAL_MACHINE,
                Arrays.asList("SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall",
                        "SOFTWARE\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall"));

        REGISTRY_PATHS.put(WinReg.HKEY_CURRENT_USER,
                Arrays.asList("SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall"));
    }

    public static List<ApplicationInfo> queryInstalledApps() {
        List<ApplicationInfo> appInfoList = new ArrayList<>();

        // Iterate through both HKLM and HKCU paths
        for (Map.Entry<WinReg.HKEY, List<String>> entry : REGISTRY_PATHS.entrySet()) {
            WinReg.HKEY rootKey = entry.getKey();
            List<String> uninstallPaths = entry.getValue();

            for (String registryPath : uninstallPaths) {
                String[] keys = Advapi32Util.registryGetKeys(rootKey, registryPath);

                for (String key : keys) {
                    String fullPath = registryPath + "\\" + key;
                    try {
                        String name = getRegistryValueOrUnknown(rootKey, fullPath, "DisplayName");
                        String version = getRegistryValueOrUnknown(rootKey, fullPath, "DisplayVersion");
                        String publisher = getRegistryValueOrUnknown(rootKey, fullPath, "Publisher");
                        String installDate = getRegistryValueOrUnknown(rootKey, fullPath, "InstallDate");
                        String installLocation = getRegistryValueOrUnknown(rootKey, fullPath, "InstallLocation");
                        String installSource = getRegistryValueOrUnknown(rootKey, fullPath, "InstallSource");

                        long installDateEpoch = Parsing.parseDateToEpoch(installDate, "yyyyMMdd");

                        Map<String, String> additionalInfo = new HashMap<>();
                        additionalInfo.put("installLocation", installLocation);
                        additionalInfo.put("installSource", installSource);

                        ApplicationInfo app = new ApplicationInfo(name, version, publisher, installDateEpoch,
                                additionalInfo);

                        appInfoList.add(app);
                    } catch (Win32Exception e) {
                        // Skip keys that are inaccessible or have missing values
                    }
                }
            }
        }

        return appInfoList;
    }

    private static String getRegistryValueOrUnknown(WinReg.HKEY rootKey, String path, String key) {
        String value = Advapi32Util.registryGetStringValue(rootKey, path, key);
        return Parsing.getStringValueOrUnknown(value);
    }

}
