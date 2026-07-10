/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
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
package org.miaixz.bus.health.windows.software;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.Advapi32Util.EventLogIterator;
import com.sun.jna.platform.win32.Advapi32Util.EventLogRecord;
import com.sun.jna.platform.win32.COM.WbemcliUtil;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinNT.LUID;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.Config;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.builtin.jna.ByRef;
import org.miaixz.bus.health.builtin.jna.Struct;
import org.miaixz.bus.health.builtin.software.*;
import org.miaixz.bus.health.builtin.software.common.AbstractOperatingSystem;
import org.miaixz.bus.health.windows.WmiKit;
import org.miaixz.bus.health.windows.driver.EnumWindows;
import org.miaixz.bus.health.windows.driver.registry.*;
import org.miaixz.bus.health.windows.driver.wmi.Win32OperatingSystem;
import org.miaixz.bus.health.windows.driver.wmi.Win32Processor;
import org.miaixz.bus.logger.Logger;

/**
 * Microsoft Windows, commonly referred to as Windows, is a group of several proprietary graphical operating system
 * families, all of which are developed and marketed by Microsoft.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public class WindowsOperatingSystem extends AbstractOperatingSystem {

    /**
     * Creates a new WindowsOperatingSystem instance.
     */
    public WindowsOperatingSystem() {
        // No initialization required.
    }

    /**
     * The USE_PROCSTATE_SUSPENDED constant.
     */
    private static final boolean USE_PROCSTATE_SUSPENDED = Config.get(Config._WINDOWS_PROCSTATE_SUSPENDED, false);

    /**
     * The IS_VISTA_OR_GREATER constant.
     */
    private static final boolean IS_VISTA_OR_GREATER = VersionHelpers.IsWindowsVistaOrGreater();

    /**
     * OSProcess code will need to know bitness of current process
     */
    private static final boolean X86 = isCurrentX86();

    /**
     * Windows event log name
     */
    private static final Supplier<String> systemLog = Memoizer
            .memoize(WindowsOperatingSystem::querySystemLog, TimeUnit.HOURS.toNanos(1));

    /**
     * The BOOTTIME constant.
     */
    private static final long BOOTTIME = querySystemBootTime();

    /**
     * The WOW constant.
     */
    private static final boolean WOW = isCurrentWow();

    /**
     * The installedAppsSupplier value.
     */
    private final Supplier<List<ApplicationInfo>> installedAppsSupplier = Memoizer
            .memoize(WindowsInstalledApps::queryInstalledApps, Memoizer.installedAppsExpiration());

    static {
        enableDebugPrivilege();
    }

    /*
     * Cache full process stats queries. Second query will only populate if first one returns null.
     */
    /**
     * The processMapFromRegistry value.
     */
    private final Supplier<Map<Integer, ProcessPerformanceData.PerfCounterBlock>> processMapFromRegistry = Memoizer
            .memoize(WindowsOperatingSystem::queryProcessMapFromRegistry, Memoizer.defaultExpiration());

    /**
     * The processMapFromPerfCounters value.
     */
    private final Supplier<Map<Integer, ProcessPerformanceData.PerfCounterBlock>> processMapFromPerfCounters = Memoizer
            .memoize(WindowsOperatingSystem::queryProcessMapFromPerfCounters, Memoizer.defaultExpiration());
    /*
     * Cache full thread stats queries. Second query will only populate if first one returns null. Only used if
     * USE_PROCSTATE_SUSPENDED is set true.
     */
    /**
     * The threadMapFromRegistry value.
     */
    private final Supplier<Map<Integer, ThreadPerformanceData.PerfCounterBlock>> threadMapFromRegistry = Memoizer
            .memoize(WindowsOperatingSystem::queryThreadMapFromRegistry, Memoizer.defaultExpiration());

    /**
     * The threadMapFromPerfCounters value.
     */
    private final Supplier<Map<Integer, ThreadPerformanceData.PerfCounterBlock>> threadMapFromPerfCounters = Memoizer
            .memoize(WindowsOperatingSystem::queryThreadMapFromPerfCounters, Memoizer.defaultExpiration());

    /**
     * Returns the parent pids from snapshot.
     *
     * @return the get parent pids from snapshot result
     */
    private static Map<Integer, Integer> getParentPidsFromSnapshot() {
        Map<Integer, Integer> parentPidMap = new HashMap<>();
        // Get processes from ToolHelp API for parent PID
        try (ByRef.CloseablePROCESSENTRY32ByReference processEntry = new ByRef.CloseablePROCESSENTRY32ByReference()) {
            WinNT.HANDLE snapshot = Kernel32.INSTANCE
                    .CreateToolhelp32Snapshot(Tlhelp32.TH32CS_SNAPPROCESS, new DWORD(0));
            try {
                while (Kernel32.INSTANCE.Process32Next(snapshot, processEntry)) {
                    parentPidMap
                            .put(processEntry.th32ProcessID.intValue(), processEntry.th32ParentProcessID.intValue());
                }
            } finally {
                Kernel32.INSTANCE.CloseHandle(snapshot);
            }
        }
        return parentPidMap;
    }

    /**
     * Queries the process map from registry.
     *
     * @return the query process map from registry result
     */
    private static Map<Integer, ProcessPerformanceData.PerfCounterBlock> queryProcessMapFromRegistry() {
        return ProcessPerformanceData.buildProcessMapFromRegistry(null);
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
            suites.add("Communications Server");
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
        if ((suiteMask & 0x00008000) != 0) {
            suites.add("Home Server");
        }
        return String.join(Symbol.COMMA, suites);
    }

    /**
     * Queries the process map from perf counters.
     *
     * @return the query process map from perf counters result
     */
    private static Map<Integer, ProcessPerformanceData.PerfCounterBlock> queryProcessMapFromPerfCounters() {
        return ProcessPerformanceData.buildProcessMapFromPerfCounters(null);
    }

    /**
     * Attempts to enable debug privileges for this process, required for OpenProcess() to get processes other than the
     * current user. Requires elevated permissions.
     *
     * @return {@code true} if debug privileges were successfully enabled.
     */
    private static boolean enableDebugPrivilege() {
        try (ByRef.CloseableHANDLEByReference hToken = new ByRef.CloseableHANDLEByReference()) {
            boolean success = Advapi32.INSTANCE.OpenProcessToken(
                    Kernel32.INSTANCE.GetCurrentProcess(),
                    WinNT.TOKEN_QUERY | WinNT.TOKEN_ADJUST_PRIVILEGES,
                    hToken);
            if (!success) {
                Logger.error(false, "Health", "Open process credential failed. Error: {}", Native.getLastError());
                return false;
            }
            try {
                LUID luid = new LUID();
                success = Advapi32.INSTANCE.LookupPrivilegeValue(null, WinNT.SE_DEBUG_NAME, luid);
                if (!success) {
                    Logger.error(false, "Health", "LookupPrivilegeValue failed. Error: {}", Native.getLastError());
                    return false;
                }
                WinNT.TOKEN_PRIVILEGES tkp = new WinNT.TOKEN_PRIVILEGES(1);
                tkp.Privileges[0] = new WinNT.LUID_AND_ATTRIBUTES(luid, new DWORD(WinNT.SE_PRIVILEGE_ENABLED));
                success = Advapi32.INSTANCE.AdjustTokenPrivileges(hToken.getValue(), false, tkp, 0, null, null);
                int err = Native.getLastError();
                if (!success) {
                    Logger.error(false, "Health", "Adjust process privileges failed. Error: {}", err);
                    return false;
                } else if (err == WinError.ERROR_NOT_ALL_ASSIGNED) {
                    Logger.debug(false, "Health", "Debug privileges not enabled.");
                    return false;
                }
            } finally {
                Kernel32.INSTANCE.CloseHandle(hToken.getValue());
            }
        }
        return true;
    }

    /**
     * Queries the system log.
     *
     * @return the query system log result
     */
    private static String querySystemLog() {
        String systemLog = Config.get(Config._WINDOWS_EVENTLOG, "System");
        if (systemLog.isEmpty()) {
            // Use faster boot time approximation
            return null;
        }
        // Check whether it works
        HANDLE h = Advapi32.INSTANCE.OpenEventLog(null, systemLog);
        if (h == null) {
            Logger.warn(
                    false,
                    "Health",
                    "Unable to open configured system Event log \"{}\". Calculating boot time from uptime.",
                    systemLog);
            return null;
        }
        return systemLog;
    }

    /**
     * Queries the thread map from registry.
     *
     * @return the query thread map from registry result
     */
    private static Map<Integer, ThreadPerformanceData.PerfCounterBlock> queryThreadMapFromRegistry() {
        return ThreadPerformanceData.buildThreadMapFromRegistry(null);
    }

    /**
     * Queries the thread map from perf counters.
     *
     * @return the query thread map from perf counters result
     */
    private static Map<Integer, ThreadPerformanceData.PerfCounterBlock> queryThreadMapFromPerfCounters() {
        return ThreadPerformanceData.buildThreadMapFromPerfCounters(null);
    }

    /**
     * Queries the system uptime.
     *
     * @return the query system uptime result
     */
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

    /**
     * Queries the system boot time.
     *
     * @return the query system boot time result
     */
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
                    EventLogRecord logRecord = iter.next();
                    if (logRecord.getStatusCode() == 12) {
                        // Event 12 is system boot. We want this value unless we find two 6005 events
                        // first (may occur with Fast Boot)
                        return logRecord.getRecord().TimeGenerated.longValue();
                    } else if (logRecord.getStatusCode() == 6005) {
                        // If we already found one, this means we've found a second one without finding
                        // an event 12. Return the latest one.
                        if (event6005Time > 0) {
                            return event6005Time;
                        }
                        // First 6005; tentatively assign
                        event6005Time = logRecord.getRecord().TimeGenerated.longValue();
                    }
                }
                // Only one 6005 found, return
                if (event6005Time > 0) {
                    return event6005Time;
                }
            } catch (Win32Exception e) {
                Logger.warn(false, "Health", "Can't open event log \"{}\".", eventLog);
            }
        }
        // If we get this far, event log reading has failed, either from no log or no
        // startup times. Subtract up time from current time as a reasonable proxy.
        return System.currentTimeMillis() / 1000L - querySystemUptime();
    }

    /**
     * Returns the installed applications.
     *
     * @return the get installed applications result
     */
    @Override
    public List<ApplicationInfo> getInstalledApplications() {
        return installedAppsSupplier.get();
    }

    /**
     * Is the processor architecture x86?
     *
     * @return true if the processor architecture is Intel x86
     */
    static boolean isX86() {
        return X86;
    }

    /**
     * Returns whether the current x86 condition is true.
     *
     * @return the is current x86 result
     */
    private static boolean isCurrentX86() {
        try (Struct.CloseableSystemInfo sysinfo = new Struct.CloseableSystemInfo()) {
            Kernel32.INSTANCE.GetNativeSystemInfo(sysinfo);
            return (0 == sysinfo.processorArchitecture.pi.wProcessorArchitecture.intValue());
        }
    }

    /**
     * Is the current operating process x86 or x86-compatibility mode?
     *
     * @return true if the current process is 32-bit
     */
    public static boolean isWow() {
        return WOW;
    }

    /**
     * Is the specified process x86 or x86-compatibility mode?
     *
     * @param h The handle to the processs to check
     * @return true if the process is 32-bit
     */
    public static boolean isWow(HANDLE h) {
        if (X86) {
            return true;
        }
        try (ByRef.CloseableIntByReference isWow = new ByRef.CloseableIntByReference()) {
            Kernel32.INSTANCE.IsWow64Process(h, isWow);
            return isWow.getValue() != 0;
        }
    }

    /**
     * Returns whether the current wow condition is true.
     *
     * @return the is current wow result
     */
    private static boolean isCurrentWow() {
        if (X86) {
            return true;
        }
        HANDLE h = Kernel32.INSTANCE.GetCurrentProcess();
        return h != null && isWow(h);
    }

    /**
     * Returns whether the elevated condition is true.
     *
     * @return the is elevated result
     */
    @Override
    public boolean isElevated() {
        return Advapi32Util.isCurrentProcessElevated();
    }

    /**
     * Returns the file system.
     *
     * @return the get file system result
     */
    @Override
    public FileSystem getFileSystem() {
        return new WindowsFileSystem();
    }

    /**
     * Returns the internet protocol stats.
     *
     * @return the get internet protocol stats result
     */
    @Override
    public InternetProtocolStats getInternetProtocolStats() {
        return new WindowsInternetProtocolStats();
    }

    /**
     * Returns the sessions.
     *
     * @return the get sessions result
     */
    @Override
    public List<OSSession> getSessions() {
        List<OSSession> whoList = HkeyUserData.queryUserSessions();
        whoList.addAll(SessionWtsData.queryUserSessions());
        whoList.addAll(NetSessionData.queryUserSessions());
        return whoList;
    }

    /**
     * Returns the processes.
     *
     * @param pids the pids
     * @return the get processes result
     */
    @Override
    public List<OSProcess> getProcesses(Collection<Integer> pids) {
        return processMapToList(pids);
    }

    /**
     * Queries the all processes.
     *
     * @return the query all processes result
     */
    @Override
    public List<OSProcess> queryAllProcesses() {
        return processMapToList(null);
    }

    /**
     * Queries the child processes.
     *
     * @param parentPid the parent pid
     * @return the query child processes result
     */
    @Override
    public List<OSProcess> queryChildProcesses(int parentPid) {
        Set<Integer> descendantPids = getChildrenOrDescendants(getParentPidsFromSnapshot(), parentPid, false);
        return processMapToList(descendantPids);
    }

    /**
     * Queries the descendant processes.
     *
     * @param parentPid the parent pid
     * @return the query descendant processes result
     */
    @Override
    public List<OSProcess> queryDescendantProcesses(int parentPid) {
        Set<Integer> descendantPids = getChildrenOrDescendants(getParentPidsFromSnapshot(), parentPid, true);
        return processMapToList(descendantPids);
    }

    /**
     * Returns the process id.
     *
     * @return the get process id result
     */
    @Override
    public int getProcessId() {
        return Kernel32.INSTANCE.GetCurrentProcessId();
    }

    /**
     * Returns the process count.
     *
     * @return the get process count result
     */
    @Override
    public int getProcessCount() {
        try (Struct.CloseablePerformanceInformation perfInfo = new Struct.CloseablePerformanceInformation()) {
            if (!Psapi.INSTANCE.GetPerformanceInfo(perfInfo, perfInfo.size())) {
                Logger.error(
                        false,
                        "Health",
                        "Failed to get Performance Info. Error code: {}",
                        Kernel32.INSTANCE.GetLastError());
                return 0;
            }
            return perfInfo.ProcessCount.intValue();
        }
    }

    /**
     * Returns the thread id.
     *
     * @return the get thread id result
     */
    @Override
    public int getThreadId() {
        return Kernel32.INSTANCE.GetCurrentThreadId();
    }

    /**
     * Returns the current thread.
     *
     * @return the get current thread result
     */
    @Override
    public OSThread getCurrentThread() {
        OSProcess proc = getCurrentProcess();
        final int tid = getThreadId();
        return proc.getThreadDetails().stream().filter(t -> t.getThreadId() == tid).findFirst()
                .orElse(new WindowsOSThread(proc.getProcessID(), tid, null, null));
    }

    /**
     * Returns the thread count.
     *
     * @return the get thread count result
     */
    @Override
    public int getThreadCount() {
        try (Struct.CloseablePerformanceInformation perfInfo = new Struct.CloseablePerformanceInformation()) {
            if (!Psapi.INSTANCE.GetPerformanceInfo(perfInfo, perfInfo.size())) {
                Logger.error(
                        false,
                        "Health",
                        "Failed to get Performance Info. Error code: {}",
                        Kernel32.INSTANCE.GetLastError());
                return 0;
            }
            return perfInfo.ThreadCount.intValue();
        }
    }

    /**
     * Returns the system uptime.
     *
     * @return the get system uptime result
     */
    @Override
    public long getSystemUptime() {
        return querySystemUptime();
    }

    /**
     * Returns the system boot time.
     *
     * @return the get system boot time result
     */
    @Override
    public long getSystemBootTime() {
        return BOOTTIME;
    }

    /**
     * Returns the network params.
     *
     * @return the get network params result
     */
    @Override
    public NetworkParams getNetworkParams() {
        return new WindowsNetworkParams();
    }

    /**
     * Returns the process.
     *
     * @param pid the pid
     * @return the get process result
     */
    @Override
    public OSProcess getProcess(int pid) {
        List<OSProcess> procList = processMapToList(List.of(pid));
        return procList.isEmpty() ? null : procList.get(0);
    }

    /**
     * Returns the process map to list result.
     *
     * @param pids the pids
     * @return the process map to list result
     */
    private List<OSProcess> processMapToList(Collection<Integer> pids) {
        // Get data from the registry if possible
        Map<Integer, ProcessPerformanceData.PerfCounterBlock> processMap = processMapFromRegistry.get();
        // otherwise performance counters with WMI backup
        if (processMap == null || processMap.isEmpty()) {
            processMap = (pids == null) ? processMapFromPerfCounters.get()
                    : ProcessPerformanceData.buildProcessMapFromPerfCounters(pids);
        }
        Map<Integer, ThreadPerformanceData.PerfCounterBlock> threadMap = null;
        if (USE_PROCSTATE_SUSPENDED) {
            // Get data from the registry if possible
            threadMap = threadMapFromRegistry.get();
            // otherwise performance counters with WMI backup
            if (threadMap == null || threadMap.isEmpty()) {
                threadMap = (pids == null) ? threadMapFromPerfCounters.get()
                        : ThreadPerformanceData.buildThreadMapFromPerfCounters(pids);
            }
        }

        Map<Integer, ProcessWtsData.WtsInfo> processWtsMap = ProcessWtsData.queryProcessWtsMap(pids);

        Set<Integer> mapKeys = new HashSet<>(processWtsMap.keySet());
        mapKeys.retainAll(processMap.keySet());

        final Map<Integer, ProcessPerformanceData.PerfCounterBlock> finalProcessMap = processMap;
        final Map<Integer, ThreadPerformanceData.PerfCounterBlock> finalThreadMap = threadMap;
        return mapKeys.stream().parallel()
                .map(pid -> new WindowsOSProcess(pid, this, finalProcessMap, processWtsMap, finalThreadMap))
                .filter(OperatingSystem.ProcessFiltering.VALID_PROCESS).collect(Collectors.toList());
    }

    /**
     * Package-private methods for use by WindowsOSProcess to limit process memory queries to processes with same
     * bitness as the current one
     */
    @Override
    public List<OSDesktopWindow> getDesktopWindows(boolean visibleOnly) {
        return EnumWindows.queryDesktopWindows(visibleOnly);
    }

    /**
     * Queries the manufacturer.
     *
     * @return the query manufacturer result
     */
    @Override
    public String queryManufacturer() {
        return "Microsoft";
    }

    /**
     * Queries the family version info.
     *
     * @return the query family version info result
     */
    @Override
    public Pair<String, OperatingSystem.OSVersionInfo> queryFamilyVersionInfo() {
        String version = System.getProperty("os.name");
        if (version.startsWith("Windows ")) {
            version = version.substring(8);
        }

        String sp;
        int suiteMask = 0;
        String buildNumber = Normal.EMPTY;
        WbemcliUtil.WmiResult<Win32OperatingSystem.OSVersionProperty> versionInfo = Win32OperatingSystem
                .queryOsVersion();
        if (versionInfo.getResultCount() > 0) {
            sp = WmiKit.getString(versionInfo, Win32OperatingSystem.OSVersionProperty.CSDVERSION, 0);
            if (!sp.isEmpty() && !Normal.UNKNOWN.equals(sp)) {
                version = version + Symbol.SPACE + sp.replace("Service Pack ", "SP");
            }
            suiteMask = WmiKit.getUint32(versionInfo, Win32OperatingSystem.OSVersionProperty.SUITEMASK, 0);
            buildNumber = WmiKit.getString(versionInfo, Win32OperatingSystem.OSVersionProperty.BUILDNUMBER, 0);
        }
        String codeName = parseCodeName(suiteMask);
        // Older JDKs don't recognize Win11 and Server2022
        if ("10".equals(version) && buildNumber.compareTo("22000") >= 0) {
            version = "11";
        }
        if ("Server 2016".equals(version) && buildNumber.compareTo("17762") > 0) {
            version = "Server 2019";
        }
        if ("Server 2019".equals(version) && buildNumber.compareTo("20347") > 0) {
            version = "Server 2022";
        }
        if ("Server 2022".equals(version) && buildNumber.compareTo("26039") > 0) {
            version = "Server 2025";
        }
        return Pair.of("Windows", new OperatingSystem.OSVersionInfo(version, codeName, buildNumber));
    }

    /**
     * Queries the bitness.
     *
     * @param jvmBitness the jvm bitness
     * @return the query bitness result
     */
    @Override
    protected int queryBitness(int jvmBitness) {
        if (jvmBitness < 64 && System.getenv("ProgramFiles(x86)") != null && IS_VISTA_OR_GREATER) {
            WbemcliUtil.WmiResult<Win32Processor.BitnessProperty> bitnessMap = Win32Processor.queryBitness();
            if (bitnessMap.getResultCount() > 0) {
                return WmiKit.getUint16(bitnessMap, Win32Processor.BitnessProperty.ADDRESSWIDTH, 0);
            }
        }
        return jvmBitness;
    }

    /**
     * Returns the services.
     *
     * @return the get services result
     */
    @Override
    public List<OSService> getServices() {
        try (W32ServiceManager sm = new W32ServiceManager()) {
            sm.open(Winsvc.SC_MANAGER_ENUMERATE_SERVICE);
            Winsvc.ENUM_SERVICE_STATUS_PROCESS[] services = sm
                    .enumServicesStatusExProcess(WinNT.SERVICE_WIN32, Winsvc.SERVICE_STATE_ALL, null);
            List<OSService> svcArray = new ArrayList<>();
            for (Winsvc.ENUM_SERVICE_STATUS_PROCESS service : services) {
                OSService.State state;
                switch (service.ServiceStatusProcess.dwCurrentState) {
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
                svcArray.add(new OSService(service.lpDisplayName, service.ServiceStatusProcess.dwProcessId, state));
            }
            return svcArray;
        } catch (com.sun.jna.platform.win32.Win32Exception ex) {
            Logger.error(
                    false,
                    "Health",
                    ex,
                    "Windows service query failed: exception={}",
                    ex.getClass().getSimpleName());
            return Collections.emptyList();
        }
    }

}
