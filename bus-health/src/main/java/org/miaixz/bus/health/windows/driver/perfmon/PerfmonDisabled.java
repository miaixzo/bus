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
package org.miaixz.bus.health.windows.driver.perfmon;

import java.util.Locale;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.health.Config;
import org.miaixz.bus.logger.Logger;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

/**
 * Tests whether performance counters are disabled
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class PerfmonDisabled {

    public static final boolean PERF_OS_DISABLED = isDisabled(Config._WINDOWS_PERFOS_DIABLED, "PerfOS");
    public static final boolean PERF_PROC_DISABLED = isDisabled(Config._WINDOWS_PERFPROC_DIABLED, "PerfProc");
    public static final boolean PERF_DISK_DISABLED = isDisabled(Config._WINDOWS_PERFDISK_DIABLED, "PerfDisk");

    /**
     * Everything in this class is static, never instantiate it
     */
    private PerfmonDisabled() {
        throw new AssertionError();
    }

    private static boolean isDisabled(String config, String service) {
        String perfDisabled = Config.get(config);
        // If null or empty, check registry
        if (StringKit.isBlank(perfDisabled)) {
            String key = String.format(Locale.ROOT, "SYSTEM\\CurrentControlSet\\Services\\%s\\Performance", service);
            String value = "Disable Performance Counters";
            // If disabled in registry, log warning and return
            if (Advapi32Util.registryValueExists(WinReg.HKEY_LOCAL_MACHINE, key, value)) {
                Object disabled = Advapi32Util.registryGetValue(WinReg.HKEY_LOCAL_MACHINE, key, value);
                if (disabled instanceof Integer) {
                    if ((Integer) disabled > 0) {
                        Logger.warn(
                                "{} counters are disabled and won't return data: {}\\\\{}\\\\{} > 0.",
                                service,
                                "HKEY_LOCAL_MACHINE",
                                key,
                                value);
                        return true;
                    }
                } else {
                    Logger.warn(
                            "Invalid registry value type detected for {} counters. Should be REG_DWORD. Ignoring: {}\\\\{}\\\\{}.",
                            service,
                            "HKEY_LOCAL_MACHINE",
                            key,
                            value);
                }
            }
            return false;
        }
        // If not null or empty, parse as boolean
        return Boolean.parseBoolean(perfDisabled);
    }

}
