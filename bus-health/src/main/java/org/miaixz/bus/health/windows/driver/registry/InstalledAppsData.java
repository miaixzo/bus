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

import static com.sun.jna.platform.win32.WinError.ERROR_INSUFFICIENT_BUFFER;
import static com.sun.jna.platform.win32.WinError.ERROR_SUCCESS;
import static com.sun.jna.platform.win32.WinNT.*;
import static com.sun.jna.platform.win32.WinReg.HKEY_CURRENT_USER;
import static com.sun.jna.platform.win32.WinReg.HKEY_LOCAL_MACHINE;

import java.util.*;

import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.software.ApplicationInfo;
import org.miaixz.bus.logger.Logger;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APITypeMapper;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public final class InstalledAppsData {

    private static final Advapi32 ADV = Advapi32.INSTANCE;

    private static final long THIRTY_YEARS_IN_SECS = 30L * 365 * 24 * 60 * 60;

    private static final Map<WinReg.HKEY, List<String>> REGISTRY_PATHS = new HashMap<>();
    private static final int[] ACCESS_FLAGS = { KEY_WOW64_64KEY, KEY_WOW64_32KEY };

    static {
        REGISTRY_PATHS.put(
                HKEY_LOCAL_MACHINE,
                Arrays.asList(
                        "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall",
                        "SOFTWARE\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall"));

        REGISTRY_PATHS.put(HKEY_CURRENT_USER, Arrays.asList("SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall"));
    }

    public static List<ApplicationInfo> queryInstalledApps() {
        Set<ApplicationInfo> appInfoSet = new LinkedHashSet<>();

        // Iterate through both HKLM and HKCU paths
        for (Map.Entry<WinReg.HKEY, List<String>> entry : REGISTRY_PATHS.entrySet()) {
            WinReg.HKEY rootKey = entry.getKey();
            List<String> uninstallPaths = entry.getValue();

            for (String registryPath : uninstallPaths) {
                for (int accessFlag : ACCESS_FLAGS) {
                    try {
                        String[] keys = Advapi32Util.registryGetKeys(rootKey, registryPath, accessFlag);
                        for (String key : keys) {
                            String fullPath = registryPath + "\\" + key;
                            try {
                                String name = registryValueToString(
                                        getRegistryValueOrNull(rootKey, fullPath, "DisplayName", accessFlag));
                                if (name == null) {
                                    continue;
                                }
                                String version = registryValueToString(
                                        getRegistryValueOrNull(rootKey, fullPath, "DisplayVersion", accessFlag));
                                String publisher = registryValueToString(
                                        getRegistryValueOrNull(rootKey, fullPath, "Publisher", accessFlag));
                                long installDate = registryValueToLong(
                                        getRegistryValueOrNull(rootKey, fullPath, "InstallDate", accessFlag));
                                String installLocation = registryValueToString(
                                        getRegistryValueOrNull(rootKey, fullPath, "InstallLocation", accessFlag));
                                String installSource = registryValueToString(
                                        getRegistryValueOrNull(rootKey, fullPath, "InstallSource", accessFlag));

                                Map<String, String> additionalInfo = new LinkedHashMap<>();
                                additionalInfo.put("installLocation", installLocation);
                                additionalInfo.put("installSource", installSource);

                                ApplicationInfo app = new ApplicationInfo(name, version, publisher, installDate,
                                        additionalInfo);
                                appInfoSet.add(app);
                            } catch (Win32Exception e) {
                                // Skip keys that are inaccessible or have missing values
                            }
                        }
                    } catch (Win32Exception e) {
                        // Skip paths that are inaccessible
                    }
                }
            }
        }

        return new ArrayList<>(appInfoSet);
    }

    private static String registryValueToString(Object registryValueOrNull) {
        if (registryValueOrNull instanceof Integer) {
            return Integer.toString((int) registryValueOrNull);
        }
        return (String) registryValueOrNull;
    }

    private static long registryValueToLong(Object registryValueOrNull) {
        if (registryValueOrNull == null) {
            return 0L;
        }

        // Calculate reasonable timestamp bounds (current time to 30 years ago)
        long currentTimeSecs = System.currentTimeMillis() / 1000L;
        long minSaneTimestamp = currentTimeSecs - THIRTY_YEARS_IN_SECS;
        if (registryValueOrNull instanceof Integer) {
            int value = (Integer) registryValueOrNull;
            if (value > minSaneTimestamp && value < currentTimeSecs) {
                return value * 1000L;
            }
            return value;
        } else if (registryValueOrNull instanceof String) {
            String dateStr = ((String) registryValueOrNull).trim();
            // Try yyyyMMdd first
            long epoch = Parsing.parseDateToEpoch(dateStr, "yyyyMMdd");
            if (epoch == 0) {
                // If that fails, try MM/dd/yyyy
                epoch = Parsing.parseDateToEpoch(dateStr, "MM/dd/yyyy");
            }
            return epoch;
        }
        return 0L;
    }

    private static Object getRegistryValueOrNull(WinReg.HKEY rootKey, String path, String key, int accessFlag) {
        WinReg.HKEY hKey = null;
        try {
            hKey = getRegistryHKey(rootKey, path, accessFlag);
            Object value = registryGetValue(hKey, key);
            if ((value instanceof Integer) || (value instanceof String && !((String) value).trim().isEmpty())) {
                return value;
            }
        } catch (Win32Exception e) {
            Logger.trace("Unable to access " + path + " with flag " + accessFlag + ": " + e.getMessage());
        } finally {
            if (hKey != null) {
                int rc = ADV.RegCloseKey(hKey);
                if (rc != ERROR_SUCCESS) {
                    throw new Win32Exception(rc);
                }
            }
        }
        return null;
    }

    private static WinReg.HKEY getRegistryHKey(WinReg.HKEY rootKey, String path, int accessFlag) {
        WinReg.HKEYByReference phkKey = new WinReg.HKEYByReference();
        int rc = ADV.RegOpenKeyEx(rootKey, path, 0, KEY_READ | accessFlag, phkKey);
        if (rc != ERROR_SUCCESS) {
            throw new Win32Exception(rc);
        }
        return phkKey.getValue();
    }

    private static Object registryGetValue(WinReg.HKEY hKey, String value) {
        IntByReference lpcbData = new IntByReference();
        IntByReference lpType = new IntByReference();
        int rc = ADV.RegQueryValueEx(hKey, value, 0, lpType, (Pointer) null, lpcbData);
        if (rc != ERROR_SUCCESS && rc != ERROR_INSUFFICIENT_BUFFER) {
            throw new Win32Exception(rc);
        }
        int type = lpType.getValue();
        switch (type) {
            case REG_SZ:
            case REG_EXPAND_SZ:
                return registryGetString(hKey, value, lpType, lpcbData);

            case REG_DWORD:
                return registryGetDword(hKey, value, lpType, lpcbData);

            default:
                Logger.warn("Unsupported registry data type {} for {}", type, value);
                return null;
        }
    }

    private static String registryGetString(
            WinReg.HKEY hKey,
            String value,
            IntByReference lpType,
            IntByReference lpcbData) {
        if (lpcbData.getValue() == 0) {
            return "";
        }
        // Add space for wide string null terminator
        Memory mem = new Memory(lpcbData.getValue() + Native.WCHAR_SIZE);
        mem.clear();
        int rc = ADV.RegQueryValueEx(hKey, value, 0, lpType, mem, lpcbData);
        if (rc != ERROR_SUCCESS && rc != ERROR_INSUFFICIENT_BUFFER) {
            throw new Win32Exception(rc);
        }
        if (W32APITypeMapper.DEFAULT == W32APITypeMapper.UNICODE) {
            return mem.getWideString(0);
        } else {
            return mem.getString(0);
        }
    }

    private static int registryGetDword(
            WinReg.HKEY hKey,
            String value,
            IntByReference lpType,
            IntByReference lpcbData) {
        IntByReference pData = new IntByReference();
        int rc = ADV.RegQueryValueEx(hKey, value, 0, lpType, pData, lpcbData);
        if (rc != ERROR_SUCCESS && rc != ERROR_INSUFFICIENT_BUFFER) {
            throw new Win32Exception(rc);
        }
        return pData.getValue();
    }

}
