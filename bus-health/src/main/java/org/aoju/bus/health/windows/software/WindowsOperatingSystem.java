/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org OSHI and other contributors.                 *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.health.windows.software;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.Advapi32Util.EventLogIterator;
import com.sun.jna.platform.win32.Advapi32Util.EventLogRecord;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;
import com.sun.jna.platform.win32.Psapi.PERFORMANCE_INFORMATION;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinNT.HANDLEByReference;
import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Config;
import org.aoju.bus.health.Memoize;
import org.aoju.bus.health.builtin.software.*;
import org.aoju.bus.health.builtin.software.OSService.State;
import org.aoju.bus.health.windows.WmiKit;
import org.aoju.bus.health.windows.drivers.*;
import org.aoju.bus.health.windows.drivers.ProcessPerformanceData.PerfCounterBlock;
import org.aoju.bus.health.windows.drivers.ProcessWtsData.WtsInfo;
import org.aoju.bus.health.windows.drivers.Win32OperatingSystem.OSVersionProperty;
import org.aoju.bus.health.windows.drivers.Win32Processor.BitnessProperty;
import org.aoju.bus.logger.Logger;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Microsoft Windows, commonly referred to as Windows, is a group of several
 * proprietary graphical operating system families, all of which are developed
 * and marketed by Microsoft.
 *
 * @author Kimi Liu
 * @version 6.1.3
 * @since JDK 1.8+
 */
@ThreadSafe
public class WindowsOperatingSystem extends AbstractOperatingSystem {

    private static final boolean IS_VISTA_OR_GREATER = VersionHelpers.IsWindowsVistaOrGreater();

    /**
     * Windows event log name
     */
    private static Supplier<String> systemLog = Memoize.memoize(WindowsOperatingSystem::querySystemLog,
            TimeUnit.HOURS.toNanos(1));

    private static final long BOOTTIME = querySystemBootTime();

    static {
        enableDebugPrivilege();
    }

    /*
     * Cache full process stats queries. Second query will only populate if first
     * one returns null.
     */
    private Supplier<Map<Integer, PerfCounterBlock>> processMapFromRegistry = Memoize.memoize(
            WindowsOperatingSystem::queryProcessMapFromRegistry, Memoize.defaultExpiration());
    private Supplier<Map<Integer, PerfCounterBlock>> processMapFromPerfCounters = Memoize.memoize(
            WindowsOperatingSystem::queryProcessMapFromPerfCounters, Memoize.defaultExpiration());

    private static String parseVersion(WmiResult<OSVersionProperty> versionInfo, int suiteMask, String buildNumber) {
        // Initialize a default, sane value
        String version = System.getProperty("os.version");

        // Version is major.minor.build. Parse the version string for
        // major/minor and get the build number separately
        String[] verSplit = WmiKit.getString(versionInfo, OSVersionProperty.VERSION, 0).split("\\D");
        int major = verSplit.length > 0 ? Builder.parseIntOrDefault(verSplit[0], 0) : 0;
        int minor = verSplit.length > 1 ? Builder.parseIntOrDefault(verSplit[1], 0) : 0;

        // see
        // https://msdn.microsoft.com/en-us/library/windows/desktop/ms724833%28v=vs.85%29.aspx
        boolean ntWorkstation = WmiKit.getUint32(versionInfo, OSVersionProperty.PRODUCTTYPE,
                0) == WinNT.VER_NT_WORKSTATION;
        switch (major) {
            case 10:
                if (minor == 0) {
                    if (ntWorkstation) {
                        version = "10";
                    } else {
                        // Build numbers greater than 17762 is Server 2019 for OS
                        // Version 10.0
                        version = (Builder.parseLongOrDefault(buildNumber, 0L) > 17762) ? "Server 2019" : "Server 2016";
                    }
                }
                break;
            case 6:
                if (minor == 3) {
                    version = ntWorkstation ? "8.1" : "Server 2012 R2";
                } else if (minor == 2) {
                    version = ntWorkstation ? "8" : "Server 2012";
                } else if (minor == 1) {
                    version = ntWorkstation ? "7" : "Server 2008 R2";
                } else if (minor == 0) {
                    version = ntWorkstation ? "Vista" : "Server 2008";
                }
                break;
            case 5:
                if (minor == 2) {
                    if ((suiteMask & 0x00008000) != 0) { // VER_SUITE_WH_SERVER
                        version = "Home Server";
                    } else if (ntWorkstation) {
                        version = "XP"; // 64 bits
                    } else {
                        version = User32.INSTANCE.GetSystemMetrics(WinUser.SM_SERVERR2) != 0 ? "Server 2003"
                                : "Server 2003 R2";
                    }
                } else if (minor == 1) {
                    version = "XP"; // 32 bits
                } else if (minor == 0) {
                    version = "2000";
                }
                break;
            default:
                break;
        }

        String sp = WmiKit.getString(versionInfo, OSVersionProperty.CSDVERSION, 0);
        if (!sp.isEmpty() && !"unknown".equals(sp)) {
            version = version + Symbol.SPACE + sp.replace("Service Pack ", "SP");
        }

        return version;
    }

    /**
     * Gets suites available on the system and return as a codename
     *
     * @param suiteMask The suite mask bitmask
     * @return Suites
     */
    private static String parseCodeName(int suiteMask) {
        List<String> suites = new ArrayList<>();
        if ((suiteMask & 0x00000002) != 0) {
            suites.add("Enterprise");
        }
        if ((suiteMask & 0x00000004) != 0) {
            suites.add("BackOffice");
        }
        if ((suiteMask & 0x00000008) != 0) {
            suites.add("Communication Server");
        }
        if ((suiteMask & 0x00000080) != 0) {
            suites.add("Datacenter");
        }
        if ((suiteMask & 0x00000200) != 0) {
            suites.add("Home");
        }
        if ((suiteMask & 0x00000400) != 0) {
            suites.add("Web Server");
        }
        if ((suiteMask & 0x00002000) != 0) {
            suites.add("Storage Server");
        }
        if ((suiteMask & 0x00004000) != 0) {
            suites.add("Compute Cluster");
        }
        // 0x8000, Home Server, is included in main version name
        return String.join(Symbol.COMMA, suites);
    }

    private static Map<Integer, PerfCounterBlock> queryProcessMapFromRegistry() {
        return ProcessPerformanceData.buildProcessMapFromRegistry(null);
    }

    private static Map<Integer, PerfCounterBlock> queryProcessMapFromPerfCounters() {
        return ProcessPerformanceData.buildProcessMapFromPerfCounters(null);
    }

    private static long querySystemUptime() {
        // Uptime is in seconds so divide milliseconds
        // GetTickCount64 requires Vista (6.0) or later
        if (IS_VISTA_OR_GREATER) {
            return Kernel32.INSTANCE.GetTickCount64() / 1000L;
        } else {
            // 32 bit rolls over at ~ 49 days
            return Kernel32.INSTANCE.GetTickCount() / 1000L;
        }
    }

    private static long querySystemBootTime() {
        String eventLog = systemLog.get();
        if (eventLog != null) {
            try {
                EventLogIterator iter = new EventLogIterator(null, eventLog, WinNT.EVENTLOG_BACKWARDS_READ);
                // Get the most recent boot event (ID 12) from the Event log. If Windows "Fast
                // Startup" is enabled we may not see event 12, so also check for most recent ID
                // 6005 (Event log startup) as a reasonably close backup.
                long event6005Time = 0L;
                while (iter.hasNext()) {
                    EventLogRecord record = iter.next();
                    if (record.getStatusCode() == 12) {
                        // Event 12 is system boot. We want this value unless we find two 6005 events
                        // first (may occur with Fast Boot)
                        return record.getRecord().TimeGenerated.longValue();
                    } else if (record.getStatusCode() == 6005) {
                        // If we already found one, this means we've found a second one without finding
                        // an event 12. Return the latest one.
                        if (event6005Time > 0) {
                            return event6005Time;
                        }
                        // First 6005; tentatively assign
                        event6005Time = record.getRecord().TimeGenerated.longValue();
                    }
                }
                // Only one 6005 found, return
                if (event6005Time > 0) {
                    return event6005Time;
                }
            } catch (Win32Exception e) {
                Logger.warn("Can't open event log \"{}\".", eventLog);
            }
        }
        // If we get this far, event log reading has failed, either from no log or no
        // startup times. Subtract up time from current time as a reasonable proxy.
        return System.currentTimeMillis() / 1000L - querySystemUptime();
    }

    /**
     * Enables debug privileges for this process, required for OpenProcess() to get
     * processes other than the current user
     *
     * @return {@code true} if debug privileges were successfully enabled.
     */
    private static boolean enableDebugPrivilege() {
        HANDLEByReference hToken = new HANDLEByReference();
        boolean success = Advapi32.INSTANCE.OpenProcessToken(Kernel32.INSTANCE.GetCurrentProcess(),
                WinNT.TOKEN_QUERY | WinNT.TOKEN_ADJUST_PRIVILEGES, hToken);
        if (!success) {
            Logger.error("OpenProcessToken failed. Error: {}", Native.getLastError());
            return false;
        }
        try {
            WinNT.LUID luid = new WinNT.LUID();
            success = Advapi32.INSTANCE.LookupPrivilegeValue(null, WinNT.SE_DEBUG_NAME, luid);
            if (!success) {
                Logger.error("LookupPrivilegeValue failed. Error: {}", Native.getLastError());
                return false;
            }
            WinNT.TOKEN_PRIVILEGES tkp = new WinNT.TOKEN_PRIVILEGES(1);
            tkp.Privileges[0] = new WinNT.LUID_AND_ATTRIBUTES(luid, new DWORD(WinNT.SE_PRIVILEGE_ENABLED));
            success = Advapi32.INSTANCE.AdjustTokenPrivileges(hToken.getValue(), false, tkp, 0, null, null);
            int err = Native.getLastError();
            if (!success) {
                Logger.error("AdjustTokenPrivileges failed. Error: {}", err);
                return false;
            } else if (err == WinError.ERROR_NOT_ALL_ASSIGNED) {
                Logger.debug("Debug privileges not enabled.");
                return false;
            }
        } finally {
            Kernel32.INSTANCE.CloseHandle(hToken.getValue());
        }
        return true;
    }

    private static String querySystemLog() {
        String systemLog = Config.get("health.os.windows.eventlog", "System");
        if (systemLog.isEmpty()) {
            // Use faster boot time approximation
            return null;
        }
        // Check whether it works
        HANDLE h = Advapi32.INSTANCE.OpenEventLog(null, systemLog);
        if (h == null) {
            Logger.warn("Unable to open configured system Event log \"{}\". Calculating boot time from uptime.",
                    systemLog);
            return null;
        }
        return systemLog;
    }

    @Override
    public String queryManufacturer() {
        return "Microsoft";
    }

    @Override
    public FamilyVersionInfo queryFamilyVersionInfo() {
        WmiResult<OSVersionProperty> versionInfo = Win32OperatingSystem.queryOsVersion();
        if (versionInfo.getResultCount() < 1) {
            return new FamilyVersionInfo("Windows", new OSVersionInfo(System.getProperty("os.version"), null, null));
        }
        // Guaranteed that versionInfo is not null and lists non-empty
        // before calling the parse*() methods
        int suiteMask = WmiKit.getUint32(versionInfo, OSVersionProperty.SUITEMASK, 0);
        String buildNumber = WmiKit.getString(versionInfo, OSVersionProperty.BUILDNUMBER, 0);
        String version = parseVersion(versionInfo, suiteMask, buildNumber);
        String codeName = parseCodeName(suiteMask);
        return new FamilyVersionInfo("Windows", new OSVersionInfo(version, codeName, buildNumber));
    }

    @Override
    protected int queryBitness(int jvmBitness) {
        if (jvmBitness < 64 && System.getenv("ProgramFiles(x86)") != null && IS_VISTA_OR_GREATER) {
            WmiResult<BitnessProperty> bitnessMap = Win32Processor.queryBitness();
            if (bitnessMap.getResultCount() > 0) {
                return WmiKit.getUint16(bitnessMap, BitnessProperty.ADDRESSWIDTH, 0);
            }
        }
        return jvmBitness;
    }

    @Override
    public boolean queryElevated() {
        try {
            File dir = new File(System.getenv("windir") + "\\system32\\config\\systemprofile");
            return dir.isDirectory();
        } catch (SecurityException e) {
            return false;
        }
    }

    @Override
    public FileSystem getFileSystem() {
        return new WindowsFileSystem();
    }

    @Override
    public InternetProtocolStats getInternetProtocolStats() {
        return new WindowsInternetProtocolStats();
    }

    @Override
    public List<OSSession> getSessions() {
        List<OSSession> whoList = HkeyUserData.queryUserSessions();
        whoList.addAll(SessionWtsData.queryUserSessions());
        whoList.addAll(NetSessionData.queryUserSessions());
        return Collections.unmodifiableList(whoList);
    }

    @Override
    public List<OSProcess> getProcesses(int limit, ProcessSort sort) {
        List<OSProcess> procList = processMapToList(null);
        List<OSProcess> sorted = processSort(procList, limit, sort);
        return Collections.unmodifiableList(sorted);
    }

    @Override
    public List<OSProcess> getProcesses(Collection<Integer> pids) {
        return Collections.unmodifiableList(processMapToList(pids));
    }

    @Override
    public List<OSProcess> getChildProcesses(int parentPid, int limit, ProcessSort sort) {
        Set<Integer> childPids = new HashSet<>();
        // Get processes from ToolHelp API for parent PID
        Tlhelp32.PROCESSENTRY32.ByReference processEntry = new Tlhelp32.PROCESSENTRY32.ByReference();
        WinNT.HANDLE snapshot = Kernel32.INSTANCE.CreateToolhelp32Snapshot(Tlhelp32.TH32CS_SNAPPROCESS, new DWORD(0));
        try {
            while (Kernel32.INSTANCE.Process32Next(snapshot, processEntry)) {
                if (processEntry.th32ParentProcessID.intValue() == parentPid) {
                    childPids.add(processEntry.th32ProcessID.intValue());
                }
            }
        } finally {
            Kernel32.INSTANCE.CloseHandle(snapshot);
        }
        List<OSProcess> procList = processMapToList(childPids);
        List<OSProcess> sorted = processSort(procList, limit, sort);
        return Collections.unmodifiableList(sorted);
    }

    @Override
    public OSProcess getProcess(int pid) {
        List<OSProcess> procList = processMapToList(Arrays.asList(pid));
        return procList.isEmpty() ? null : procList.get(0);
    }

    private List<OSProcess> processMapToList(Collection<Integer> pids) {
        // Get data from the registry if possible
        Map<Integer, PerfCounterBlock> processMap = processMapFromRegistry.get();
        // otherwise performance counters with WMI backup
        if (processMap == null) {
            processMap = (pids == null) ? processMapFromPerfCounters.get()
                    : ProcessPerformanceData.buildProcessMapFromPerfCounters(pids);
        }

        Map<Integer, WtsInfo> processWtsMap = ProcessWtsData.queryProcessWtsMap(pids);

        Set<Integer> mapKeys = new HashSet<>(processWtsMap.keySet());
        mapKeys.retainAll(processMap.keySet());

        List<OSProcess> processList = new ArrayList<>();
        for (Integer pid : mapKeys) {
            processList.add(new WindowsOSProcess(pid, this, processMap, processWtsMap));
        }
        return processList;
    }

    @Override
    public int getProcessId() {
        return Kernel32.INSTANCE.GetCurrentProcessId();
    }

    @Override
    public int getProcessCount() {
        PERFORMANCE_INFORMATION perfInfo = new PERFORMANCE_INFORMATION();
        if (!Psapi.INSTANCE.GetPerformanceInfo(perfInfo, perfInfo.size())) {
            Logger.error("Failed to get Performance Info. Error code: {}", Kernel32.INSTANCE.GetLastError());
            return 0;
        }
        return perfInfo.ProcessCount.intValue();
    }

    @Override
    public int getThreadCount() {
        PERFORMANCE_INFORMATION perfInfo = new PERFORMANCE_INFORMATION();
        if (!Psapi.INSTANCE.GetPerformanceInfo(perfInfo, perfInfo.size())) {
            Logger.error("Failed to get Performance Info. Error code: {}", Kernel32.INSTANCE.GetLastError());
            return 0;
        }
        return perfInfo.ThreadCount.intValue();
    }

    @Override
    public long getSystemUptime() {
        return querySystemUptime();
    }

    @Override
    public long getSystemBootTime() {
        return BOOTTIME;
    }

    @Override
    public NetworkParams getNetworkParams() {
        return new WindowsNetworkParams();
    }

    @Override
    public OSService[] getServices() {
        try (W32ServiceManager sm = new W32ServiceManager()) {
            sm.open(Winsvc.SC_MANAGER_ENUMERATE_SERVICE);
            Winsvc.ENUM_SERVICE_STATUS_PROCESS[] services = sm.enumServicesStatusExProcess(WinNT.SERVICE_WIN32,
                    Winsvc.SERVICE_STATE_ALL, null);
            OSService[] svcArray = new OSService[services.length];
            for (int i = 0; i < services.length; i++) {
                State state;
                switch (services[i].ServiceStatusProcess.dwCurrentState) {
                    case 1:
                        state = OSService.State.STOPPED;
                        break;
                    case 4:
                        state = OSService.State.RUNNING;
                        break;
                    default:
                        state = OSService.State.OTHER;
                        break;
                }
                svcArray[i] = new OSService(services[i].lpDisplayName, services[i].ServiceStatusProcess.dwProcessId,
                        state);
            }
            return svcArray;
        } catch (com.sun.jna.platform.win32.Win32Exception ex) {
            Logger.error("Win32Exception: {}", ex.getMessage());
            return new OSService[0];
        }
    }

}
