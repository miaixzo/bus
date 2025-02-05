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
package org.miaixz.bus.health.windows.software;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

import com.sun.jna.Native;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.Advapi32Util.EventLogIterator;
import com.sun.jna.platform.win32.Advapi32Util.EventLogRecord;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinNT.LUID;
import com.sun.jna.platform.win32.COM.WbemcliUtil;

/**
 * Microsoft Windows, commonly referred to as Windows, is a group of several proprietary graphical operating system
 * families, all of which are developed and marketed by Microsoft.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public class WindowsOperatingSystem extends AbstractOperatingSystem {

    private static final boolean USE_PROCSTATE_SUSPENDED = Config.get(Config._WINDOWS_PROCSTATE_SUSPENDED, false);

    private static final boolean IS_VISTA_OR_GREATER = VersionHelpers.IsWindowsVistaOrGreater();
    /*
     * OSProcess code will need to know bitness of current process
     */
    private static final boolean X86 = isCurrentX86();
    /*
     * Windows event log name
     */
    private static final Supplier<String> systemLog = Memoizer.memoize(WindowsOperatingSystem::querySystemLog,
            TimeUnit.HOURS.toNanos(1));
    private static final long BOOTTIME = querySystemBootTime();
    private static final boolean WOW = isCurrentWow();

    static {
        enableDebugPrivilege();
    }

    /*
     * Cache full process stats queries. Second query will only populate if first one returns null.
     */
    private final Supplier<Map<Integer, ProcessPerformanceData.PerfCounterBlock>> processMapFromRegistry = Memoizer
            .memoize(WindowsOperatingSystem::queryProcessMapFromRegistry, Memoizer.defaultExpiration());
    private final Supplier<Map<Integer, ProcessPerformanceData.PerfCounterBlock>> processMapFromPerfCounters = Memoizer
            .memoize(WindowsOperatingSystem::queryProcessMapFromPerfCounters, Memoizer.defaultExpiration());
    /*
     * Cache full thread stats queries. Second query will only populate if first one returns null. Only used if
     * USE_PROCSTATE_SUSPENDED is set true.
     */
    private final Supplier<Map<Integer, ThreadPerformanceData.PerfCounterBlock>> threadMapFromRegistry = Memoizer
            .memoize(WindowsOperatingSystem::queryThreadMapFromRegistry, Memoizer.defaultExpiration());
    private final Supplier<Map<Integer, ThreadPerformanceData.PerfCounterBlock>> threadMapFromPerfCounters = Memoizer
            .memoize(WindowsOperatingSystem::queryThreadMapFromPerfCounters, Memoizer.defaultExpiration());

    private static Map<Integer, Integer> getParentPidsFromSnapshot() {
        Map<Integer, Integer> parentPidMap = new HashMap<>();
        // Get processes from ToolHelp API for parent PID
        try (ByRef.CloseablePROCESSENTRY32ByReference processEntry = new ByRef.CloseablePROCESSENTRY32ByReference()) {
            WinNT.HANDLE snapshot = Kernel32.INSTANCE.CreateToolhelp32Snapshot(Tlhelp32.TH32CS_SNAPPROCESS,
                    new DWORD(0));
            try {
                while (Kernel32.INSTANCE.Process32Next(snapshot, processEntry)) {
                    parentPidMap.put(processEntry.th32ProcessID.intValue(),
                            processEntry.th32ParentProcessID.intValue());
                }
            } finally {
                Kernel32.INSTANCE.CloseHandle(snapshot);
            }
        }
        return parentPidMap;
    }

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
            boolean success = Advapi32.INSTANCE.OpenProcessToken(Kernel32.INSTANCE.GetCurrentProcess(),
                    WinNT.TOKEN_QUERY | WinNT.TOKEN_ADJUST_PRIVILEGES, hToken);
            if (!success) {
                Logger.error("OpenProcessToken failed. Error: {}", Native.getLastError());
                return false;
            }
            try {
                LUID luid = new LUID();
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
        }
        return true;
    }

    private static String querySystemLog() {
        String systemLog = Config.get(Config._WINDOWS_EVENTLOG, "System");
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

    private static Map<Integer, ThreadPerformanceData.PerfCounterBlock> queryThreadMapFromRegistry() {
        return ThreadPerformanceData.buildThreadMapFromRegistry(null);
    }

    private static Map<Integer, ThreadPerformanceData.PerfCounterBlock> queryThreadMapFromPerfCounters() {
        return ThreadPerformanceData.buildThreadMapFromPerfCounters(null);
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
                Logger.warn("Can't open event log \"{}\".", eventLog);
            }
        }
        // If we get this far, event log reading has failed, either from no log or no
        // startup times. Subtract up time from current time as a reasonable proxy.
        return System.currentTimeMillis() / 1000L - querySystemUptime();
    }

    /**
     * Is the processor architecture x86?
     *
     * @return true if the processor architecture is Intel x86
     */
    static boolean isX86() {
        return X86;
    }

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

    private static boolean isCurrentWow() {
        if (X86) {
            return true;
        }
        HANDLE h = Kernel32.INSTANCE.GetCurrentProcess();
        return (h == null) ? false : isWow(h);
    }

    @Override
    public boolean isElevated() {
        return Advapi32Util.isCurrentProcessElevated();
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
        return whoList;
    }

    @Override
    public List<OSProcess> getProcesses(Collection<Integer> pids) {
        return processMapToList(pids);
    }

    @Override
    public List<OSProcess> queryAllProcesses() {
        return processMapToList(null);
    }

    @Override
    public List<OSProcess> queryChildProcesses(int parentPid) {
        Set<Integer> descendantPids = getChildrenOrDescendants(getParentPidsFromSnapshot(), parentPid, false);
        return processMapToList(descendantPids);
    }

    @Override
    public List<OSProcess> queryDescendantProcesses(int parentPid) {
        Set<Integer> descendantPids = getChildrenOrDescendants(getParentPidsFromSnapshot(), parentPid, true);
        return processMapToList(descendantPids);
    }

    @Override
    public int getProcessId() {
        return Kernel32.INSTANCE.GetCurrentProcessId();
    }

    @Override
    public int getProcessCount() {
        try (Struct.CloseablePerformanceInformation perfInfo = new Struct.CloseablePerformanceInformation()) {
            if (!Psapi.INSTANCE.GetPerformanceInfo(perfInfo, perfInfo.size())) {
                Logger.error("Failed to get Performance Info. Error code: {}", Kernel32.INSTANCE.GetLastError());
                return 0;
            }
            return perfInfo.ProcessCount.intValue();
        }
    }

    @Override
    public int getThreadId() {
        return Kernel32.INSTANCE.GetCurrentThreadId();
    }

    @Override
    public OSThread getCurrentThread() {
        OSProcess proc = getCurrentProcess();
        final int tid = getThreadId();
        return proc.getThreadDetails().stream().filter(t -> t.getThreadId() == tid).findFirst()
                .orElse(new WindowsOSThread(proc.getProcessID(), tid, null, null));
    }

    @Override
    public int getThreadCount() {
        try (Struct.CloseablePerformanceInformation perfInfo = new Struct.CloseablePerformanceInformation()) {
            if (!Psapi.INSTANCE.GetPerformanceInfo(perfInfo, perfInfo.size())) {
                Logger.error("Failed to get Performance Info. Error code: {}", Kernel32.INSTANCE.GetLastError());
                return 0;
            }
            return perfInfo.ThreadCount.intValue();
        }
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
    public OSProcess getProcess(int pid) {
        List<OSProcess> procList = processMapToList(List.of(pid));
        return procList.isEmpty() ? null : procList.get(0);
    }

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

    @Override
    public String queryManufacturer() {
        return "Microsoft";
    }

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
        } else if ("Server 2019".equals(version) && buildNumber.compareTo("20347") > 0) {
            version = "Server 2022";
        }
        return Pair.of("Windows", new OperatingSystem.OSVersionInfo(version, codeName, buildNumber));
    }

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

    @Override
    public List<OSService> getServices() {
        try (W32ServiceManager sm = new W32ServiceManager()) {
            sm.open(Winsvc.SC_MANAGER_ENUMERATE_SERVICE);
            Winsvc.ENUM_SERVICE_STATUS_PROCESS[] services = sm.enumServicesStatusExProcess(WinNT.SERVICE_WIN32,
                    Winsvc.SERVICE_STATE_ALL, null);
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
            Logger.error("Win32Exception: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

}
