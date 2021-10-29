/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org OSHI and other contributors.                 *
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

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.Advapi32Util.Account;
import com.sun.jna.platform.win32.BaseTSD.ULONG_PTRByReference;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;
import com.sun.jna.ptr.IntByReference;
import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.tuple.Pair;
import org.aoju.bus.core.lang.tuple.Triple;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Config;
import org.aoju.bus.health.Memoize;
import org.aoju.bus.health.builtin.software.AbstractOSProcess;
import org.aoju.bus.health.builtin.software.OSProcess;
import org.aoju.bus.health.builtin.software.OSThread;
import org.aoju.bus.health.windows.NtDll;
import org.aoju.bus.health.windows.NtDll.UNICODE_STRING;
import org.aoju.bus.health.windows.WmiKit;
import org.aoju.bus.health.windows.drivers.*;
import org.aoju.bus.logger.Logger;

import java.io.File;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * OSProcess implemenation
 *
 * @author Kimi Liu
 * @version 6.3.0
 * @since JDK 1.8+
 */
@ThreadSafe
public class WindowsOSProcess extends AbstractOSProcess {

    // Config param to enable cache
    public static final String OSHI_OS_WINDOWS_COMMANDLINE_BATCH = "oshi.os.windows.commandline.batch";
    private static final boolean USE_BATCH_COMMANDLINE = Config.get(OSHI_OS_WINDOWS_COMMANDLINE_BATCH, false);

    private static final boolean USE_PROCSTATE_SUSPENDED = Config
            .get(WindowsOperatingSystem.OSHI_OS_WINDOWS_PROCSTATE_SUSPENDED, false);

    private static final boolean IS_VISTA_OR_GREATER = VersionHelpers.IsWindowsVistaOrGreater();
    private static final boolean IS_WINDOWS7_OR_GREATER = VersionHelpers.IsWindows7OrGreater();

    // track the OperatingSystem object that created this
    private final WindowsOperatingSystem os;
    private Supplier<Pair<String, String>> groupInfo = Memoize.memoize(this::queryGroupInfo);
    private Supplier<Triple<String, String, Map<String, String>>> cwdCmdEnv = Memoize.memoize(
            this::queryCwdCommandlineEnvironment);
    private Supplier<String> currentWorkingDirectory = Memoize.memoize(this::queryCwd);
    private String name;
    private Supplier<Pair<String, String>> userInfo = Memoize.memoize(this::queryUserInfo);
    private String path;
    private State state = State.INVALID;
    private int parentProcessID;
    private int threadCount;
    private int priority;
    private long virtualSize;
    private long residentSetSize;
    private long kernelTime;
    private long userTime;
    private long startTime;
    private Supplier<String> commandLine = Memoize.memoize(this::queryCommandLine);
    private Supplier<List<String>> args = Memoize.memoize(this::queryArguments);
    private long upTime;
    private long bytesRead;
    private long bytesWritten;
    private long openFiles;
    private int bitness;
    private long pageFaults;

    public WindowsOSProcess(int pid, WindowsOperatingSystem os,
                            Map<Integer, ProcessPerformanceData.PerfCounterBlock> processMap, Map<Integer, ProcessWtsData.WtsInfo> processWtsMap,
                            Map<Integer, ThreadPerformanceData.PerfCounterBlock> threadMap) {
        super(pid);
        // Save a copy of OS creating this object for later use
        this.os = os;
        // Initially set to match OS bitness. If 64 will check later for 32-bit process
        this.bitness = os.getBitness();
        updateAttributes(processMap.get(pid), processWtsMap.get(pid), threadMap);
    }

    private static String readUnicodeString(WinNT.HANDLE h, UNICODE_STRING s) {
        IntByReference nRead = new IntByReference();
        if (s.Length > 0) {
            // Add space for null terminator
            Memory m = new Memory(s.Length + 2L);
            m.clear(); // really only need null in last 2 bytes but this is easier
            Kernel32.INSTANCE.ReadProcessMemory(h, s.Buffer, m, s.Length, nRead);
            if (nRead.getValue() > 0) {
                return m.getWideString(0);
            }
        }
        return "";
    }

    private static Triple<String, String, Map<String, String>> defaultCwdCommandlineEnvironment() {
        return Triple.of("", "", Collections.emptyMap());
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public String getCommandLine() {
        return this.commandLine.get();
    }

    @Override
    public List<String> getArguments() {
        return args.get();
    }

    @Override
    public Map<String, String> getEnvironmentVariables() {
        return cwdCmdEnv.get().getRight();
    }

    @Override
    public String getUser() {
        return userInfo.get().getLeft();
    }

    @Override
    public String getUserID() {
        return userInfo.get().getRight();
    }

    @Override
    public String getGroup() {
        return groupInfo.get().getLeft();
    }

    @Override
    public String getGroupID() {
        return groupInfo.get().getRight();
    }

    @Override
    public State getState() {
        return this.state;
    }

    @Override
    public int getParentProcessID() {
        return this.parentProcessID;
    }

    @Override
    public int getThreadCount() {
        return this.threadCount;
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public long getVirtualSize() {
        return this.virtualSize;
    }

    @Override
    public long getResidentSetSize() {
        return this.residentSetSize;
    }

    @Override
    public long getKernelTime() {
        return this.kernelTime;
    }

    @Override
    public long getUserTime() {
        return this.userTime;
    }

    @Override
    public long getUpTime() {
        return this.upTime;
    }

    @Override
    public long getStartTime() {
        return this.startTime;
    }

    @Override
    public long getBytesRead() {
        return this.bytesRead;
    }

    @Override
    public long getBytesWritten() {
        return this.bytesWritten;
    }

    @Override
    public long getOpenFiles() {
        return this.openFiles;
    }

    @Override
    public int getBitness() {
        return this.bitness;
    }

    @Override
    public long getAffinityMask() {
        final WinNT.HANDLE pHandle = Kernel32.INSTANCE.OpenProcess(WinNT.PROCESS_QUERY_INFORMATION, false, getProcessID());
        if (null != pHandle) {
            try {
                ULONG_PTRByReference processAffinity = new ULONG_PTRByReference();
                ULONG_PTRByReference systemAffinity = new ULONG_PTRByReference();
                if (Kernel32.INSTANCE.GetProcessAffinityMask(pHandle, processAffinity, systemAffinity)) {
                    return Pointer.nativeValue(processAffinity.getValue().toPointer());
                }
            } finally {
                Kernel32.INSTANCE.CloseHandle(pHandle);
            }
            Kernel32.INSTANCE.CloseHandle(pHandle);
        }
        return 0L;
    }

    @Override
    public List<OSThread> getThreadDetails() {
        // Get data from the registry if possible
        Map<Integer, ThreadPerformanceData.PerfCounterBlock> threads = ThreadPerformanceData
                .buildThreadMapFromRegistry(Collections.singleton(getProcessID()));
        // otherwise performance counters with WMI backup
        if (null != threads) {
            threads = ThreadPerformanceData.buildThreadMapFromPerfCounters(Collections.singleton(this.getProcessID()));
        }
        if (null == threads) {
            return Collections.emptyList();
        }
        return threads.entrySet().stream()
                .map(entry -> new WindowsOSThread(getProcessID(), entry.getKey(), this.name, entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public long getMinorFaults() {
        return this.pageFaults;
    }

    @Override
    public boolean updateAttributes() {
        Set<Integer> pids = Collections.singleton(this.getProcessID());
        // Get data from the registry if possible
        Map<Integer, ProcessPerformanceData.PerfCounterBlock> pcb = ProcessPerformanceData
                .buildProcessMapFromRegistry(null);
        // otherwise performance counters with WMI backup
        if (pcb == null) {
            pcb = ProcessPerformanceData.buildProcessMapFromPerfCounters(pids);
        }
        Map<Integer, ThreadPerformanceData.PerfCounterBlock> tcb = null;
        if (USE_PROCSTATE_SUSPENDED) {
            tcb = ThreadPerformanceData.buildThreadMapFromRegistry(null);
            // otherwise performance counters with WMI backup
            if (tcb == null) {
                tcb = ThreadPerformanceData.buildThreadMapFromPerfCounters(null);
            }
        }
        Map<Integer, ProcessWtsData.WtsInfo> wts = ProcessWtsData.queryProcessWtsMap(pids);
        return updateAttributes(pcb.get(this.getProcessID()), wts.get(this.getProcessID()), tcb);
    }

    private boolean updateAttributes(ProcessPerformanceData.PerfCounterBlock pcb, ProcessWtsData.WtsInfo wts,
                                     Map<Integer, ThreadPerformanceData.PerfCounterBlock> threadMap) {
        this.name = pcb.getName();
        this.path = wts.getPath(); // Empty string for Win7+
        this.parentProcessID = pcb.getParentProcessID();
        this.threadCount = wts.getThreadCount();
        this.priority = pcb.getPriority();
        this.virtualSize = wts.getVirtualSize();
        this.residentSetSize = pcb.getResidentSetSize();
        this.kernelTime = wts.getKernelTime();
        this.userTime = wts.getUserTime();
        this.startTime = pcb.getStartTime();
        this.upTime = pcb.getUpTime();
        this.bytesRead = pcb.getBytesRead();
        this.bytesWritten = pcb.getBytesWritten();
        this.openFiles = wts.getOpenFiles();
        this.pageFaults = pcb.getPageFaults();

        // There are only 3 possible Process states on Windows: RUNNING, SUSPENDED, or
        // UNKNOWN. Processes are considered running unless all of their threads are
        // SUSPENDED.
        this.state = OSProcess.State.RUNNING;
        if (threadMap != null) {
            // If user hasn't enabled this in properties, we ignore
            int pid = this.getProcessID();
            // If any thread is NOT suspended, set running
            for (ThreadPerformanceData.PerfCounterBlock tcb : threadMap.values()) {
                if (tcb.getOwningProcessID() == pid) {
                    if (tcb.getThreadWaitReason() == 5) {
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
        final WinNT.HANDLE pHandle = Kernel32.INSTANCE.OpenProcess(WinNT.PROCESS_QUERY_INFORMATION, false, getProcessID());
        if (null != pHandle) {
            try {
                // Test for 32-bit process on 64-bit windows
                if (IS_VISTA_OR_GREATER && this.bitness == Normal._64) {
                    IntByReference wow64 = new IntByReference(0);
                    if (Kernel32.INSTANCE.IsWow64Process(pHandle, wow64) && wow64.getValue() > 0) {
                        this.bitness = Normal._32;
                    }
                }
                // Full path
                final WinNT.HANDLEByReference phToken = new WinNT.HANDLEByReference();
                try { // EXECUTABLEPATH
                    if (IS_WINDOWS7_OR_GREATER) {
                        this.path = Kernel32Util.QueryFullProcessImageName(pHandle, 0);
                    }
                } catch (Win32Exception e) {
                    this.state = State.INVALID;
                } finally {
                    final WinNT.HANDLE token = phToken.getValue();
                    if (null != token) {
                        Kernel32.INSTANCE.CloseHandle(token);
                    }
                }
            } finally {
                Kernel32.INSTANCE.CloseHandle(pHandle);
            }
        }

        return !this.state.equals(State.INVALID);
    }

    @Override
    public String getCurrentWorkingDirectory() {
        return currentWorkingDirectory.get();
    }

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
        WmiResult<Win32Process.CommandLineProperty> commandLineProcs = Win32Process
                .queryCommandLines(Collections.singleton(getProcessID()));
        if (commandLineProcs.getResultCount() > 0) {
            return WmiKit.getString(commandLineProcs, Win32Process.CommandLineProperty.COMMANDLINE, 0);
        }
        return Normal.UNKNOWN;
    }

    private List<String> queryArguments() {
        String cl = getCommandLine();
        if (!cl.isEmpty()) {
            return Arrays.asList(Shell32Util.CommandLineToArgv(cl));
        }
        return Collections.emptyList();
    }

    private Pair<String, String> queryUserInfo() {
        Pair<String, String> pair = null;
        final WinNT.HANDLE pHandle = Kernel32.INSTANCE.OpenProcess(WinNT.PROCESS_QUERY_INFORMATION, false, getProcessID());
        if (null != pHandle) {
            final WinNT.HANDLEByReference phToken = new WinNT.HANDLEByReference();
            try {
                if (Advapi32.INSTANCE.OpenProcessToken(pHandle, WinNT.TOKEN_DUPLICATE | WinNT.TOKEN_QUERY, phToken)) {
                    Account account = Advapi32Util.getTokenAccount(phToken.getValue());
                    pair = Pair.of(account.name, account.sidString);
                } else {
                    int error = Kernel32.INSTANCE.GetLastError();
                    // Access denied errors are common. Fail silently.
                    if (error != WinError.ERROR_ACCESS_DENIED) {
                        Logger.error("Failed to get process token for process {}: {}", getProcessID(),
                                Kernel32.INSTANCE.GetLastError());
                    }
                }
            } catch (Win32Exception e) {
                Logger.warn("Failed to query user info for process {} ({}): {}", getProcessID(), getName(),
                        e.getMessage());
            } finally {
                final WinNT.HANDLE token = phToken.getValue();
                if (null != token) {
                    Kernel32.INSTANCE.CloseHandle(token);
                }
                Kernel32.INSTANCE.CloseHandle(pHandle);
            }
        }
        if (null == pair) {
            return Pair.of(Normal.UNKNOWN, Normal.UNKNOWN);
        }
        return pair;
    }

    private Pair<String, String> queryGroupInfo() {
        Pair<String, String> pair = null;
        final WinNT.HANDLE pHandle = Kernel32.INSTANCE.OpenProcess(WinNT.PROCESS_QUERY_INFORMATION, false, getProcessID());
        if (null != pHandle) {
            final WinNT.HANDLEByReference phToken = new WinNT.HANDLEByReference();
            if (Advapi32.INSTANCE.OpenProcessToken(pHandle, WinNT.TOKEN_DUPLICATE | WinNT.TOKEN_QUERY, phToken)) {
                Account account = Advapi32Util.getTokenPrimaryGroup(phToken.getValue());
                pair = Pair.of(account.name, account.sidString);
            } else {
                int error = Kernel32.INSTANCE.GetLastError();
                // Access denied errors are common. Fail silently.
                if (error != WinError.ERROR_ACCESS_DENIED) {
                    Logger.error("Failed to get process token for process {}: {}", getProcessID(),
                            Kernel32.INSTANCE.GetLastError());
                }
            }
            final WinNT.HANDLE token = phToken.getValue();
            if (null != token) {
                Kernel32.INSTANCE.CloseHandle(token);
            }
            Kernel32.INSTANCE.CloseHandle(pHandle);
        }
        if (null == pair) {
            return Pair.of(Normal.UNKNOWN, Normal.UNKNOWN);
        }
        return pair;
    }

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
        return "";
    }

    private Triple<String, String, Map<String, String>> queryCwdCommandlineEnvironment() {
        // Get the process handle
        WinNT.HANDLE h = Kernel32.INSTANCE.OpenProcess(WinNT.PROCESS_QUERY_INFORMATION | WinNT.PROCESS_VM_READ, false,
                getProcessID());
        if (h != null) {
            try {
                // Can't check 32-bit procs from a 64-bit one
                if (WindowsOperatingSystem.isX86() == WindowsOperatingSystem.isWow(h)) {

                    IntByReference nRead = new IntByReference();

                    // Start by getting the address of the PEB
                    NtDll.PBI pbi = new NtDll.PBI();
                    int ret = NtDll.INSTANCE.NtQueryInformationProcess(h, NtDll.PROCESS_BASIC_INFORMATION,
                            pbi.getPointer(), pbi.size(), nRead);
                    if (ret != 0) {
                        return defaultCwdCommandlineEnvironment();
                    }
                    pbi.read();

                    // Now fetch the PEB
                    NtDll.PEB peb = new NtDll.PEB();
                    Kernel32.INSTANCE.ReadProcessMemory(h, pbi.PebBaseAddress, peb.getPointer(), peb.size(), nRead);
                    if (nRead.getValue() == 0) {
                        return defaultCwdCommandlineEnvironment();
                    }
                    peb.read();

                    // Now fetch the Process Parameters structure containing our data
                    NtDll.RTL_USER_PROCESS_PARAMETERS upp = new NtDll.RTL_USER_PROCESS_PARAMETERS();
                    Kernel32.INSTANCE.ReadProcessMemory(h, peb.ProcessParameters, upp.getPointer(), upp.size(), nRead);
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
                        Memory buffer = new Memory(envSize);
                        Kernel32.INSTANCE.ReadProcessMemory(h, upp.Environment, buffer, envSize, nRead);
                        if (nRead.getValue() > 0) {
                            char[] env = buffer.getCharArray(0, envSize / 2);
                            Map<String, String> envMap = Builder.parseCharArrayToStringMap(env);
                            // First entry in Environment is "=::=::\"
                            envMap.remove("");
                            return Triple.of(cwd, cl, Collections.unmodifiableMap(envMap));
                        }
                    }
                    return Triple.of(cwd, cl, Collections.emptyMap());
                }
            } finally {
                Kernel32.INSTANCE.CloseHandle(h);
            }
        }
        return defaultCwdCommandlineEnvironment();
    }

}
