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
package org.miaixz.bus.health.mac.software;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.jna.Struct;
import org.miaixz.bus.health.builtin.software.*;
import org.miaixz.bus.health.mac.SysctlKit;
import org.miaixz.bus.health.mac.driver.Who;
import org.miaixz.bus.health.mac.driver.WindowInfo;

import com.sun.jna.platform.mac.SystemB;

/**
 * macOS, previously Mac OS X and later OS X) is a series of proprietary graphical operating systems developed and
 * marketed by Apple Inc. since 2001. It is the primary operating system for Apple's Mac computers.
 * 
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public class MacOperatingSystemJNA extends MacOperatingSystem {

    private static final long BOOTTIME;

    static {
        try (Struct.CloseableTimeval tv = new Struct.CloseableTimeval()) {
            if (!SysctlKit.sysctl("kern.boottime", tv) || tv.tv_sec.longValue() == 0L) {
                // Usually this works. If it doesn't, fall back to text parsing.
                // Boot time will be the first consecutive string of digits.
                BOOTTIME = Parsing.parseLongOrDefault(
                        Executor.getFirstAnswer("sysctl -n kern.boottime").split(",")[0].replaceAll("\\D", ""),
                        System.currentTimeMillis() / 1000);
            } else {
                // tv now points to a 64-bit timeval structure for boot time.
                // First 4 bytes are seconds, second 4 bytes are microseconds
                // (we ignore)
                BOOTTIME = tv.tv_sec.longValue();
            }
        }
    }

    public MacOperatingSystemJNA() {
        super(SysctlKit.sysctl("kern.maxproc", 0x1000));
    }

    protected MacOperatingSystemJNA(int maxproc) {
        super(maxproc);
    }

    @Override
    public long getSystemBootTime() {
        return BOOTTIME;
    }

    @Override
    public FileSystem getFileSystem() {
        return new MacFileSystem();
    }

    @Override
    public InternetProtocolStats getInternetProtocolStats() {
        return new MacInternetProtocolStats(isElevated());
    }

    @Override
    public Pair<String, OSVersionInfo> queryFamilyVersionInfo() {
        String family = this.major > 10 || (this.major == 10 && this.minor >= 12) ? "macOS"
                : System.getProperty("os.name");
        String codeName = parseCodeName();
        String buildNumber = SysctlKit.sysctl("kern.osversion", "");
        return new Pair<>(family, new OSVersionInfo(this.osXVersion, codeName, buildNumber));
    }

    @Override
    public List<OSSession> getSessions() {
        return USE_WHO_COMMAND ? super.getSessions() : Who.queryUtxent();
    }

    @Override
    public List<OSProcess> queryAllProcesses() {
        List<OSProcess> procs = new ArrayList<>();
        int[] pids = new int[this.maxProc];
        Arrays.fill(pids, -1);
        int numberOfProcesses = SystemB.INSTANCE
                .proc_listpids(SystemB.PROC_ALL_PIDS, 0, pids, pids.length * SystemB.INT_SIZE) / SystemB.INT_SIZE;
        for (int i = 0; i < numberOfProcesses; i++) {
            if (pids[i] >= 0) {
                OSProcess proc = getProcess(pids[i]);
                if (proc != null) {
                    procs.add(proc);
                }
            }
        }
        return procs;
    }

    @Override
    public OSProcess getProcess(int pid) {
        OSProcess proc = new MacOSProcess(pid, this.major, this.minor, this);
        return proc.getState().equals(OSProcess.State.INVALID) ? null : proc;
    }

    @Override
    public int getProcessId() {
        return SystemB.INSTANCE.getpid();
    }

    @Override
    public int getProcessCount() {
        return SystemB.INSTANCE.proc_listpids(SystemB.PROC_ALL_PIDS, 0, null, 0) / SystemB.INT_SIZE;
    }

    @Override
    public int getThreadCount() {
        // Get current pids, then slightly pad in case new process starts while
        // allocating array space
        int[] pids = new int[getProcessCount() + 10];
        int numberOfProcesses = SystemB.INSTANCE.proc_listpids(SystemB.PROC_ALL_PIDS, 0, pids, pids.length)
                / SystemB.INT_SIZE;
        int numberOfThreads = 0;
        try (Struct.CloseableProcTaskInfo taskInfo = new Struct.CloseableProcTaskInfo()) {
            for (int i = 0; i < numberOfProcesses; i++) {
                int exit = SystemB.INSTANCE
                        .proc_pidinfo(pids[i], SystemB.PROC_PIDTASKINFO, 0, taskInfo, taskInfo.size());
                if (exit != -1) {
                    numberOfThreads += taskInfo.pti_threadnum;
                }
            }
        }
        return numberOfThreads;
    }

    @Override
    public NetworkParams getNetworkParams() {
        return new MacNetworkParams();
    }

    @Override
    public List<OSDesktopWindow> getDesktopWindows(boolean visibleOnly) {
        return WindowInfo.queryDesktopWindows(visibleOnly);
    }

}
