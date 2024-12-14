/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org OSHI and other contributors.               ~
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
 * @since Java 17+
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
                        Logger.warn("{} counters are disabled and won't return data: {}\\\\{}\\\\{} > 0.", service,
                                "HKEY_LOCAL_MACHINE", key, value);
                        return true;
                    }
                } else {
                    Logger.warn(
                            "Invalid registry value type detected for {} counters. Should be REG_DWORD. Ignoring: {}\\\\{}\\\\{}.",
                            service, "HKEY_LOCAL_MACHINE", key, value);
                }
            }
            return false;
        }
        // If not null or empty, parse as boolean
        return Boolean.parseBoolean(perfDisabled);
    }

}
