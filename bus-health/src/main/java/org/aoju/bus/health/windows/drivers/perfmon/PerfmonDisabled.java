package org.aoju.bus.health.windows.drivers.perfmon;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.health.Config;
import org.aoju.bus.logger.Logger;

/**
 * Tests whether performance counters are disabled
 */
@ThreadSafe
public final class PerfmonDisabled {

    static final boolean PERF_OS_DISABLED = isDisabled(Config.OS_WINDOWS_PERFOS_DIABLED, "PerfOS");
    static final boolean PERF_PROC_DISABLED = isDisabled(Config.OS_WINDOWS_PERFPROC_DIABLED, "PerfProc");
    static final boolean PERF_DISK_DISABLED = isDisabled(Config.OS_WINDOWS_PERFDISK_DIABLED, "PerfDisk");

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
            String key = String.format("SYSTEM\\CurrentControlSet\\Services\\%s\\Performance", service);
            String value = "Disable Performance Counters";
            // If disabled in registry, log warning and return
            if (Advapi32Util.registryValueExists(WinReg.HKEY_LOCAL_MACHINE, key, value)
                    && Advapi32Util.registryGetIntValue(WinReg.HKEY_LOCAL_MACHINE, key, value) > 0) {
                Logger.warn("{} counters are disabled and won't return data: {}\\\\{}\\\\{} > 0.", service,
                        "HKEY_LOCAL_MACHINE", key, value);
                return true;
            }
            return false;
        }
        // If not null or empty, parse as boolean
        return Boolean.parseBoolean(perfDisabled);
    }

}
