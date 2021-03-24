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

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.Advapi32Util.Account;
import com.sun.jna.platform.win32.BaseTSD.ULONG_PTRByReference;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;
import com.sun.jna.ptr.IntByReference;
import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.tuple.Pair;
import org.aoju.bus.health.Config;
import org.aoju.bus.health.Memoize;
import org.aoju.bus.health.builtin.software.AbstractOSProcess;
import org.aoju.bus.health.builtin.software.OSThread;
import org.aoju.bus.health.windows.WmiKit;
import org.aoju.bus.health.windows.drivers.*;
import org.aoju.bus.logger.Logger;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * OSProcess implemenation
 *
 * @author Kimi Liu
 * @version 6.2.1
 * @since JDK 1.8+
 */
@ThreadSafe
public class WindowsOSProcess extends AbstractOSProcess {

    // Config param to enable cache
    public static final String OSHI_OS_WINDOWS_COMMANDLINE_BATCH = "health.os.windows.commandline.batch";
    private static final boolean USE_BATCH_COMMANDLINE = Config.get(OSHI_OS_WINDOWS_COMMANDLINE_BATCH, false);

    private static final boolean IS_VISTA_OR_GREATER = VersionHelpers.IsWindowsVistaOrGreater();
    private static final boolean IS_WINDOWS7_OR_GREATER = VersionHelpers.IsWindows7OrGreater();

    private Supplier<Pair<String, String>> userInfo = Memoize.memoize(this::queryUserInfo);
    private Supplier<Pair<String, String>> groupInfo = Memoize.memoize(this::queryGroupInfo);
    private String name;
    private String path;
    private String currentWorkingDirectory;
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
    private long upTime;
    private long bytesRead;
    private long bytesWritten;
    private long openFiles;
    private int bitness;
    private long pageFaults;

    public WindowsOSProcess(int pid, WindowsOperatingSystem os, Map<Integer, ProcessPerformanceData.PerfCounterBlock> processMap,
                            Map<Integer, ProcessWtsData.WtsInfo> processWtsMap) {
        super(pid);
        // For executing process, set CWD
        if (pid == os.getProcessId()) {
            String cwd = new File(Symbol.DOT).getAbsolutePath();
            // trim off trailing "."
            this.currentWorkingDirectory = cwd.isEmpty() ? Normal.EMPTY : cwd.substring(0, cwd.length() - 1);
        }
        // There is no easy way to get ExecutuionState for a process.
        // The WMI value is null. It's possible to get thread Execution
        // State and possibly roll up.
        this.state = State.RUNNING;
        // Initially set to match OS bitness. If 64 will check later for 32-bit process
        this.bitness = os.getBitness();
        updateAttributes(processMap.get(pid), processWtsMap.get(pid));
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
    public String getCurrentWorkingDirectory() {
        return this.currentWorkingDirectory;
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
        if (threads == null) {
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
        Map<Integer, ProcessPerformanceData.PerfCounterBlock> pcb = ProcessPerformanceData.buildProcessMapFromRegistry(null);
        // otherwise performance counters with WMI backup
        if (pcb == null) {
            pcb = ProcessPerformanceData.buildProcessMapFromPerfCounters(pids);
        }
        Map<Integer, ProcessWtsData.WtsInfo> wts = ProcessWtsData.queryProcessWtsMap(pids);
        return updateAttributes(pcb.get(this.getProcessID()), wts.get(this.getProcessID()));
    }

    private boolean updateAttributes(ProcessPerformanceData.PerfCounterBlock pcb, ProcessWtsData.WtsInfo wts) {
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

        // Get a handle to the process for various extended info. Only gets
        // current user unless running as administrator
        final WinNT.HANDLE pHandle = Kernel32.INSTANCE.OpenProcess(WinNT.PROCESS_QUERY_INFORMATION, false, getProcessID());
        if (null != pHandle) {
            try {
                // Test for 32-bit process on 64-bit windows
                if (IS_VISTA_OR_GREATER && this.bitness == 64) {
                    IntByReference wow64 = new IntByReference(0);
                    if (Kernel32.INSTANCE.IsWow64Process(pHandle, wow64) && wow64.getValue() > 0) {
                        this.bitness = 32;
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

    private String queryCommandLine() {
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

    // temp for testing
    private static Account getTokenAccount(WinNT.HANDLE hToken) {
        // get token group information size
        IntByReference tokenInformationLength = new IntByReference();
        if (Advapi32.INSTANCE.GetTokenInformation(hToken, WinNT.TOKEN_INFORMATION_CLASS.TokenUser, null, 0,
                tokenInformationLength)) {
            throw new RuntimeException("Expected GetTokenInformation to fail with ERROR_INSUFFICIENT_BUFFER");
        }
        int rc = Kernel32.INSTANCE.GetLastError();
        if (rc != W32Errors.ERROR_INSUFFICIENT_BUFFER) {
            throw new Win32Exception(rc);
        }
        // get token user information
        WinNT.TOKEN_USER user = new WinNT.TOKEN_USER(tokenInformationLength.getValue());
        if (!Advapi32.INSTANCE.GetTokenInformation(hToken, WinNT.TOKEN_INFORMATION_CLASS.TokenUser, user,
                tokenInformationLength.getValue(), tokenInformationLength)) {
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }
        try {
            return Advapi32Util.getAccountBySid(user.User.Sid);
        } finally {
            // Ensure, that the memory object is retained until the account
            // extraction is done.
            // From Java 9 onwards Reference#reachabilityFence would be
            // preferred
            user.getPointer().getByte(0);
        }
    }

    private Pair<String, String> queryUserInfo() {
        Pair<String, String> pair = null;
        final WinNT.HANDLE pHandle = Kernel32.INSTANCE.OpenProcess(WinNT.PROCESS_QUERY_INFORMATION, false, getProcessID());
        if (null != pHandle) {
            final WinNT.HANDLEByReference phToken = new WinNT.HANDLEByReference();
            try {
                if (Advapi32.INSTANCE.OpenProcessToken(pHandle, WinNT.TOKEN_DUPLICATE | WinNT.TOKEN_QUERY, phToken)) {
                    Account account = getTokenAccount(phToken.getValue());
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
        if (pair == null) {
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
        if (pair == null) {
            return Pair.of(Normal.UNKNOWN, Normal.UNKNOWN);
        }
        return pair;
    }

}
