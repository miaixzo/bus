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
package org.miaixz.bus.health.windows.software;

import java.io.File;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.health.Config;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.jna.ByRef;
import org.miaixz.bus.health.builtin.software.OSProcess;
import org.miaixz.bus.health.builtin.software.OSThread;
import org.miaixz.bus.health.builtin.software.common.AbstractOSProcess;
import org.miaixz.bus.health.windows.WmiKit;
import org.miaixz.bus.health.windows.driver.registry.ProcessPerformanceData;
import org.miaixz.bus.health.windows.driver.registry.ProcessWtsData;
import org.miaixz.bus.health.windows.driver.registry.ProcessWtsData.WtsInfo;
import org.miaixz.bus.health.windows.driver.registry.ThreadPerformanceData;
import org.miaixz.bus.health.windows.driver.wmi.Win32Process;
import org.miaixz.bus.health.windows.driver.wmi.Win32Process.CommandLineProperty;
import org.miaixz.bus.health.windows.driver.wmi.Win32ProcessCached;
import org.miaixz.bus.logger.Logger;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.Advapi32Util.Account;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;

/**
 * OSProcess implementation
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public class WindowsOSProcess extends AbstractOSProcess {

    /**
     * The USE_BATCH_COMMANDLINE constant.
     */
    private static final boolean USE_BATCH_COMMANDLINE = Config.get(Config._WINDOWS_COMMANDLINE_BATCH, false);

    /**
     * The USE_PROCSTATE_SUSPENDED constant.
     */
    private static final boolean USE_PROCSTATE_SUSPENDED = Config.get(Config._WINDOWS_PROCSTATE_SUSPENDED, false);

    /**
     * The IS_VISTA_OR_GREATER constant.
     */
    private static final boolean IS_VISTA_OR_GREATER = VersionHelpers.IsWindowsVistaOrGreater();
    /**
     * The IS_WINDOWS7_OR_GREATER constant.
     */
    private static final boolean IS_WINDOWS7_OR_GREATER = VersionHelpers.IsWindows7OrGreater();

    // track the OperatingSystem object that created this
    /**
     * The os value.
     */
    private final WindowsOperatingSystem os;
    /**
     * The groupInfo value.
     */
    private final Supplier<Pair<String, String>> groupInfo = Memoizer.memoize(this::queryGroupInfo);
    /**
     * The cwdCmdEnv value.
     */
    private final Supplier<Triplet<String, String, Map<String, String>>> cwdCmdEnv = Memoizer
            .memoize(this::queryCwdCommandlineEnvironment);
    /**
     * The currentWorkingDirectory value.
     */
    private final Supplier<String> currentWorkingDirectory = Memoizer.memoize(this::queryCwd);
    /**
     * The tcb value.
     */
    private Map<Integer, ThreadPerformanceData.PerfCounterBlock> tcb;
    /**
     * The name value.
     */
    private String name;
    /**
     * The userInfo value.
     */
    private final Supplier<Pair<String, String>> userInfo = Memoizer.memoize(this::queryUserInfo);
    /**
     * The path value.
     */
    private String path = Normal.EMPTY;
    /**
     * The state value.
     */
    private OSProcess.State state = OSProcess.State.INVALID;
    /**
     * The parentProcessID value.
     */
    private int parentProcessID;
    /**
     * The threadCount value.
     */
    private int threadCount;
    /**
     * The priority value.
     */
    private int priority;
    /**
     * The virtualSize value.
     */
    private long virtualSize;
    /**
     * The workingSetSize value.
     */
    private long workingSetSize;
    /**
     * The privateWorkingSetSize value.
     */
    private long privateWorkingSetSize;
    /**
     * The kernelTime value.
     */
    private long kernelTime;
    /**
     * The userTime value.
     */
    private long userTime;
    /**
     * The startTime value.
     */
    private long startTime;
    /**
     * The commandLine value.
     */
    private final Supplier<String> commandLine = Memoizer.memoize(this::queryCommandLine);
    /**
     * The args value.
     */
    private final Supplier<List<String>> args = Memoizer.memoize(this::queryArguments);
    /**
     * The upTime value.
     */
    private long upTime;
    /**
     * The bytesRead value.
     */
    private long bytesRead;
    /**
     * The bytesWritten value.
     */
    private long bytesWritten;
    /**
     * The openFiles value.
     */
    private long openFiles;
    /**
     * The bitness value.
     */
    private int bitness;
    /**
     * The pageFaults value.
     */
    private long pageFaults;

    /**
     * Creates a new WindowsOSProcess instance.
     *
     * @param pid           the pid
     * @param os            the os
     * @param processMap    the process map
     * @param processWtsMap the process wts map
     * @param threadMap     the thread map
     */
    public WindowsOSProcess(int pid, WindowsOperatingSystem os,
            Map<Integer, ProcessPerformanceData.PerfCounterBlock> processMap, Map<Integer, WtsInfo> processWtsMap,
            Map<Integer, ThreadPerformanceData.PerfCounterBlock> threadMap) {
        super(pid);
        // Save a copy of OS creating this object for later use
        this.os = os;
        // Initially set to match OS bitness. If 64 will check later for 32-bit process
        this.bitness = os.getBitness();
        // Initialize thread counters
        this.tcb = threadMap;
        updateAttributes(processMap.get(pid), processWtsMap.get(pid));
    }

    /**
     * Returns the default cwd commandline environment result.
     *
     * @return the default cwd commandline environment result
     */
    private static Triplet<String, String, Map<String, String>> defaultCwdCommandlineEnvironment() {
        return Triplet.of(Normal.EMPTY, Normal.EMPTY, Collections.emptyMap());
    }

    /**
     * Reads the unicode string.
     *
     * @param h the h
     * @param s the s
     * @return the read unicode string result
     */
    private static String readUnicodeString(HANDLE h, org.miaixz.bus.health.windows.jna.NtDll.UNICODE_STRING s) {
        if (s.Length > 0) {
            // Add space for null terminator
            try (Memory m = new Memory(s.Length + 2L);
                    ByRef.CloseableIntByReference nRead = new ByRef.CloseableIntByReference()) {
                m.clear(); // really only need null in last 2 bytes but this is easier
                Kernel32.INSTANCE.ReadProcessMemory(h, s.Buffer, m, s.Length, nRead);
                if (nRead.getValue() > 0) {
                    return m.getWideString(0);
                }
            }
        }
        return Normal.EMPTY;
    }

    /**
     * Returns the name.
     *
     * @return the get name result
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Returns the path.
     *
     * @return the get path result
     */
    @Override
    public String getPath() {
        return this.path;
    }

    /**
     * Returns the command line.
     *
     * @return the get command line result
     */
    @Override
    public String getCommandLine() {
        return this.commandLine.get();
    }

    /**
     * Returns the arguments.
     *
     * @return the get arguments result
     */
    @Override
    public List<String> getArguments() {
        return args.get();
    }

    /**
     * Returns the environment variables.
     *
     * @return the get environment variables result
     */
    @Override
    public Map<String, String> getEnvironmentVariables() {
        return cwdCmdEnv.get().getRight();
    }

    /**
     * Returns the current working directory.
     *
     * @return the get current working directory result
     */
    @Override
    public String getCurrentWorkingDirectory() {
        return currentWorkingDirectory.get();
    }

    /**
     * Returns the user.
     *
     * @return the get user result
     */
    @Override
    public String getUser() {
        return userInfo.get().getLeft();
    }

    /**
     * Returns the user id.
     *
     * @return the get user id result
     */
    @Override
    public String getUserID() {
        return userInfo.get().getRight();
    }

    /**
     * Returns the group.
     *
     * @return the get group result
     */
    @Override
    public String getGroup() {
        return groupInfo.get().getLeft();
    }

    /**
     * Returns the group id.
     *
     * @return the get group id result
     */
    @Override
    public String getGroupID() {
        return groupInfo.get().getRight();
    }

    /**
     * Returns the parent process id.
     *
     * @return the get parent process id result
     */
    @Override
    public int getParentProcessID() {
        return this.parentProcessID;
    }

    /**
     * Returns the thread count.
     *
     * @return the get thread count result
     */
    @Override
    public int getThreadCount() {
        return this.threadCount;
    }

    /**
     * Returns the priority.
     *
     * @return the get priority result
     */
    @Override
    public int getPriority() {
        return this.priority;
    }

    /**
     * Returns the virtual size.
     *
     * @return the get virtual size result
     */
    @Override
    public long getVirtualSize() {
        return this.virtualSize;
    }

    /**
     * Returns the resident memory.
     *
     * @return the get resident memory result
     */
    @Override
    public long getResidentMemory() {
        return this.workingSetSize;
    }

    /**
     * Returns the private resident memory.
     *
     * @return the get private resident memory result
     */
    @Override
    public long getPrivateResidentMemory() {
        return this.privateWorkingSetSize;
    }

    /**
     * Returns the kernel time.
     *
     * @return the get kernel time result
     */
    @Override
    public long getKernelTime() {
        return this.kernelTime;
    }

    /**
     * Returns the user time.
     *
     * @return the get user time result
     */
    @Override
    public long getUserTime() {
        return this.userTime;
    }

    /**
     * Returns the up time.
     *
     * @return the get up time result
     */
    @Override
    public long getUpTime() {
        return this.upTime;
    }

    /**
     * Returns the start time.
     *
     * @return the get start time result
     */
    @Override
    public long getStartTime() {
        return this.startTime;
    }

    /**
     * Returns the bytes read.
     *
     * @return the get bytes read result
     */
    @Override
    public long getBytesRead() {
        return this.bytesRead;
    }

    /**
     * Returns the bytes written.
     *
     * @return the get bytes written result
     */
    @Override
    public long getBytesWritten() {
        return this.bytesWritten;
    }

    /**
     * Returns the open files.
     *
     * @return the get open files result
     */
    @Override
    public long getOpenFiles() {
        return this.openFiles;
    }

    /**
     * Returns the soft open file limit.
     *
     * @return the get soft open file limit result
     */
    @Override
    public long getSoftOpenFileLimit() {
        return WindowsFileSystem.MAX_WINDOWS_HANDLES;
    }

    /**
     * Returns the hard open file limit.
     *
     * @return the get hard open file limit result
     */
    @Override
    public long getHardOpenFileLimit() {
        return WindowsFileSystem.MAX_WINDOWS_HANDLES;
    }

    /**
     * Returns the bitness.
     *
     * @return the get bitness result
     */
    @Override
    public int getBitness() {
        return this.bitness;
    }

    /**
     * Returns the affinity mask.
     *
     * @return the get affinity mask result
     */
    @Override
    public long getAffinityMask() {
        final HANDLE pHandle = Kernel32.INSTANCE.OpenProcess(WinNT.PROCESS_QUERY_INFORMATION, false, getProcessID());
        if (pHandle != null) {
            try (ByRef.CloseableULONGptrByReference processAffinity = new ByRef.CloseableULONGptrByReference();
                    ByRef.CloseableULONGptrByReference systemAffinity = new ByRef.CloseableULONGptrByReference()) {
                if (Kernel32.INSTANCE.GetProcessAffinityMask(pHandle, processAffinity, systemAffinity)) {
                    return Pointer.nativeValue(processAffinity.getValue().toPointer());
                }
            } finally {
                Kernel32.INSTANCE.CloseHandle(pHandle);
            }
        }
        return 0L;
    }

    /**
     * Returns the minor faults.
     *
     * @return the get minor faults result
     */
    @Override
    public long getMinorFaults() {
        return this.pageFaults;
    }

    /**
     * Returns the state.
     *
     * @return the get state result
     */
    @Override
    public OSProcess.State getState() {
        return this.state;
    }

    /**
     * Returns the thread details.
     *
     * @return the get thread details result
     */
    @Override
    public List<OSThread> getThreadDetails() {
        Map<Integer, ThreadPerformanceData.PerfCounterBlock> threads = tcb == null
                ? queryMatchingThreads(Collections.singleton(this.getProcessID()))
                : tcb;
        if (threads == null) {
            threads = Collections.emptyMap();
        }
        return threads.entrySet().stream().parallel()
                .filter(entry -> entry.getValue().getOwningProcessID() == this.getProcessID())
                .map(entry -> new WindowsOSThread(getProcessID(), entry.getKey(), this.name, entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Updates the attributes.
     *
     * @return the update attributes result
     */
    @Override
    public boolean updateAttributes() {
        Set<Integer> pids = Collections.singleton(this.getProcessID());
        // Get data from the registry if possible
        Map<Integer, ProcessPerformanceData.PerfCounterBlock> pcb = ProcessPerformanceData
                .buildProcessMapFromRegistry(pids);
        // otherwise performance counters with WMI backup
        if (pcb == null || pcb.isEmpty()) {
            pcb = ProcessPerformanceData.buildProcessMapFromPerfCounters(pids);
        }
        ProcessPerformanceData.PerfCounterBlock perfCounterBlock = pcb == null ? null : pcb.get(this.getProcessID());
        if (USE_PROCSTATE_SUSPENDED) {
            if (perfCounterBlock != null) {
                this.name = perfCounterBlock.getName();
            }
            this.tcb = queryMatchingThreads(pids);
        }
        Map<Integer, WtsInfo> wts = ProcessWtsData.queryProcessWtsMap(pids);
        return updateAttributes(perfCounterBlock, wts == null ? null : wts.get(this.getProcessID()));
    }

    /**
     * Queries the matching threads.
     *
     * @param pids the pids
     * @return the query matching threads result
     */
    private Map<Integer, ThreadPerformanceData.PerfCounterBlock> queryMatchingThreads(Set<Integer> pids) {
        // fetch from registry
        Map<Integer, ThreadPerformanceData.PerfCounterBlock> threads = ThreadPerformanceData
                .buildThreadMapFromRegistry(pids);
        // otherwise performance counters with WMI backup
        if (threads == null || threads.isEmpty()) {
            threads = ThreadPerformanceData.buildThreadMapFromPerfCounters(pids, this.getName(), -1);
        }
        return threads;
    }

    /**
     * Queries the arguments.
     *
     * @return the query arguments result
     */
    private List<String> queryArguments() {
        String cl = getCommandLine();
        if (!cl.isEmpty()) {
            return Arrays.asList(Shell32Util.CommandLineToArgv(cl));
        }
        return Collections.emptyList();
    }

    /**
     * Queries the cwd.
     *
     * @return the query cwd result
     */
    private String queryCwd() {
        // Try to fetch from process memory
        if (!cwdCmdEnv.get().getLeft().isEmpty()) {
            return cwdCmdEnv.get().getLeft();
        }
        // For executing process, set CWD
        if (getProcessID() == this.os.getProcessId()) {
            String cwd = new File(".").getAbsolutePath();
            // trim off trailing "."
            if (!cwd.isEmpty()) {
                return cwd.substring(0, cwd.length() - 1);
            }
        }
        return Normal.EMPTY;
    }

    /**
     * Queries the user info.
     *
     * @return the query user info result
     */
    private Pair<String, String> queryUserInfo() {
        Pair<String, String> pair = null;
        final HANDLE pHandle = Kernel32.INSTANCE.OpenProcess(WinNT.PROCESS_QUERY_INFORMATION, false, getProcessID());
        if (pHandle != null) {
            try (ByRef.CloseableHANDLEByReference phToken = new ByRef.CloseableHANDLEByReference()) {
                try {
                    if (Advapi32.INSTANCE.OpenProcessToken(pHandle, WinNT.TOKEN_QUERY, phToken)) {
                        Account account = Advapi32Util.getTokenAccount(phToken.getValue());
                        pair = Pair.of(account.name, account.sidString);
                    } else {
                        int error = Kernel32.INSTANCE.GetLastError();
                        // Access denied errors are common. Fail silently.
                        if (error != WinError.ERROR_ACCESS_DENIED) {
                            Logger.error("Failed to get process token for process {}: {}", getProcessID(), error);
                        }
                    }
                } catch (Win32Exception e) {
                    Logger.warn(
                            "Failed to query user info for process {} ({}): {}",
                            getProcessID(),
                            getName(),
                            e.getMessage());
                } finally {
                    final HANDLE token = phToken.getValue();
                    if (token != null) {
                        Kernel32.INSTANCE.CloseHandle(token);
                    }
                    Kernel32.INSTANCE.CloseHandle(pHandle);
                }
            }
        }
        if (pair == null) {
            return Pair.of(Normal.UNKNOWN, Normal.UNKNOWN);
        }
        return pair;
    }

    /**
     * Queries the group info.
     *
     * @return the query group info result
     */
    private Pair<String, String> queryGroupInfo() {
        Pair<String, String> pair = null;
        final HANDLE pHandle = Kernel32.INSTANCE.OpenProcess(WinNT.PROCESS_QUERY_INFORMATION, false, getProcessID());
        if (pHandle != null) {
            try (ByRef.CloseableHANDLEByReference phToken = new ByRef.CloseableHANDLEByReference()) {
                try {
                    if (Advapi32.INSTANCE.OpenProcessToken(pHandle, WinNT.TOKEN_QUERY, phToken)) {
                        Account account = Advapi32Util.getTokenPrimaryGroup(phToken.getValue());
                        pair = Pair.of(account.name, account.sidString);
                    } else {
                        int error = Kernel32.INSTANCE.GetLastError();
                        // Access denied errors are common. Fail silently.
                        if (error != WinError.ERROR_ACCESS_DENIED) {
                            Logger.error("Failed to get process token for process {}: {}", getProcessID(), error);
                        }
                    }
                } catch (Win32Exception e) {
                    Logger.warn(
                            "Failed to query group info for process {} ({}): {}",
                            getProcessID(),
                            getName(),
                            e.getMessage());
                } finally {
                    final HANDLE token = phToken.getValue();
                    if (token != null) {
                        Kernel32.INSTANCE.CloseHandle(token);
                    }
                    Kernel32.INSTANCE.CloseHandle(pHandle);
                }
            }
        }
        if (pair == null) {
            return Pair.of(Normal.UNKNOWN, Normal.UNKNOWN);
        }
        return pair;
    }

    /**
     * Updates the attributes.
     *
     * @param pcb the pcb
     * @param wts the wts
     * @return the update attributes result
     */
    private boolean updateAttributes(ProcessPerformanceData.PerfCounterBlock pcb, WtsInfo wts) {
        if (pcb == null) {
            this.state = OSProcess.State.INVALID;
            return false;
        }
        this.name = pcb.getName();
        this.parentProcessID = pcb.getParentProcessID();
        this.priority = pcb.getPriority();
        this.workingSetSize = pcb.getWorkingSetSize();
        this.privateWorkingSetSize = pcb.getPrivateWorkingSetSize();
        this.startTime = pcb.getStartTime();
        this.upTime = pcb.getUpTime();
        this.bytesRead = pcb.getBytesRead();
        this.bytesWritten = pcb.getBytesWritten();
        this.pageFaults = pcb.getPageFaults();
        if (wts != null) {
            this.path = wts.getPath(); // Empty string for Win7+
            this.threadCount = wts.getThreadCount();
            this.virtualSize = wts.getVirtualSize();
            this.kernelTime = wts.getKernelTime();
            this.userTime = wts.getUserTime();
            this.openFiles = wts.getOpenFiles();
        }

        // There are only 3 possible Process states on Windows: RUNNING, SUSPENDED, or
        // UNKNOWN. Processes are considered running unless all of their threads are
        // SUSPENDED.
        this.state = OSProcess.State.RUNNING;
        if (this.tcb != null) {
            // If user hasn't enabled this in properties, we ignore
            int pid = this.getProcessID();
            // If any thread is NOT suspended, set running
            for (ThreadPerformanceData.PerfCounterBlock tpd : this.tcb.values()) {
                if (tpd.getOwningProcessID() == pid) {
                    if (tpd.getThreadWaitReason() == 5) {
                        this.state = OSProcess.State.SUSPENDED;
                    } else {
                        this.state = OSProcess.State.RUNNING;
                        break;
                    }
                }
            }
        }

        // Get a handle to the process for various extended info. Only gets
        // current user unless running as administrator
        final HANDLE pHandle = Kernel32.INSTANCE.OpenProcess(WinNT.PROCESS_QUERY_INFORMATION, false, getProcessID());
        if (pHandle != null) {
            try {
                // Test for 32-bit process on 64-bit windows
                if (IS_VISTA_OR_GREATER && this.bitness == 64) {
                    try (ByRef.CloseableIntByReference wow64 = new ByRef.CloseableIntByReference()) {
                        if (Kernel32.INSTANCE.IsWow64Process(pHandle, wow64) && wow64.getValue() > 0) {
                            this.bitness = 32;
                        }
                    }
                }
                try { // EXECUTABLEPATH
                    if (IS_WINDOWS7_OR_GREATER) {
                        this.path = Kernel32Util.QueryFullProcessImageName(pHandle, 0);
                    }
                } catch (Win32Exception e) {
                    // Best-effort path lookup; leave process state and path untouched on failure.
                }
            } finally {
                Kernel32.INSTANCE.CloseHandle(pHandle);
            }
        }

        return !this.state.equals(OSProcess.State.INVALID);
    }

    /**
     * Queries the command line.
     *
     * @return the query command line result
     */
    private String queryCommandLine() {
        // Try to fetch from process memory
        if (!cwdCmdEnv.get().getMiddle().isEmpty()) {
            return cwdCmdEnv.get().getMiddle();
        }
        // If using batch mode fetch from WMI Cache
        if (USE_BATCH_COMMANDLINE) {
            return Win32ProcessCached.getInstance().getCommandLine(getProcessID(), getStartTime());
        }
        // If no cache enabled, query line by line
        WmiResult<CommandLineProperty> commandLineProcs = Win32Process
                .queryCommandLines(Collections.singleton(getProcessID()));
        if (commandLineProcs.getResultCount() > 0) {
            return WmiKit.getString(commandLineProcs, CommandLineProperty.COMMANDLINE, 0);
        }
        return Normal.EMPTY;
    }

    /**
     * Queries the cwd commandline environment.
     *
     * @return the query cwd commandline environment result
     */
    private Triplet<String, String, Map<String, String>> queryCwdCommandlineEnvironment() {
        // Get the process handle
        HANDLE h = Kernel32.INSTANCE
                .OpenProcess(WinNT.PROCESS_QUERY_INFORMATION | WinNT.PROCESS_VM_READ, false, getProcessID());
        if (h != null) {
            try {
                // Can't check 32-bit procs from a 64-bit one
                if (WindowsOperatingSystem.isX86() == WindowsOperatingSystem.isWow(h)) {
                    try (ByRef.CloseableIntByReference nRead = new ByRef.CloseableIntByReference()) {
                        // Start by getting the address of the PEB
                        org.miaixz.bus.health.windows.jna.NtDll.PROCESS_BASIC_INFORMATION pbi = new org.miaixz.bus.health.windows.jna.NtDll.PROCESS_BASIC_INFORMATION();
                        int ret = org.miaixz.bus.health.windows.jna.NtDll.INSTANCE.NtQueryInformationProcess(
                                h,
                                org.miaixz.bus.health.windows.jna.NtDll.PROCESS_BASIC_INFORMATION,
                                pbi.getPointer(),
                                pbi.size(),
                                nRead);
                        if (ret != 0) {
                            return defaultCwdCommandlineEnvironment();
                        }
                        pbi.read();

                        // Now fetch the PEB
                        org.miaixz.bus.health.windows.jna.NtDll.PEB peb = new org.miaixz.bus.health.windows.jna.NtDll.PEB();
                        Kernel32.INSTANCE.ReadProcessMemory(h, pbi.PebBaseAddress, peb.getPointer(), peb.size(), nRead);
                        if (nRead.getValue() == 0) {
                            return defaultCwdCommandlineEnvironment();
                        }
                        peb.read();

                        // Now fetch the Process Parameters structure containing our data
                        org.miaixz.bus.health.windows.jna.NtDll.RTL_USER_PROCESS_PARAMETERS upp = new org.miaixz.bus.health.windows.jna.NtDll.RTL_USER_PROCESS_PARAMETERS();
                        Kernel32.INSTANCE
                                .ReadProcessMemory(h, peb.ProcessParameters, upp.getPointer(), upp.size(), nRead);
                        if (nRead.getValue() == 0) {
                            return defaultCwdCommandlineEnvironment();
                        }
                        upp.read();

                        // Get CWD and Command Line strings here
                        String cwd = readUnicodeString(h, upp.CurrentDirectory.DosPath);
                        String cl = readUnicodeString(h, upp.CommandLine);

                        // Fetch the Environment Strings
                        int envSize = upp.EnvironmentSize.intValue();
                        if (envSize > 0) {
                            try (Memory buffer = new Memory(envSize)) {
                                Kernel32.INSTANCE.ReadProcessMemory(h, upp.Environment, buffer, envSize, nRead);
                                if (nRead.getValue() > 0) {
                                    char[] env = buffer.getCharArray(0, envSize / 2);
                                    Map<String, String> envMap = Parsing.parseCharArrayToStringMap(env);
                                    // First entry in Environment is "=::=::\"
                                    envMap.remove(Normal.EMPTY);
                                    return Triplet.of(cwd, cl, Collections.unmodifiableMap(envMap));
                                }
                            }
                        }
                        return Triplet.of(cwd, cl, Collections.emptyMap());
                    }
                }
            } finally {
                Kernel32.INSTANCE.CloseHandle(h);
            }
        }
        return defaultCwdCommandlineEnvironment();
    }

}
