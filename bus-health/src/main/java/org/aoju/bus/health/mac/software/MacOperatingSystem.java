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
package org.aoju.bus.health.mac.software;

import com.sun.jna.platform.mac.SystemB;
import com.sun.jna.platform.mac.SystemB.ProcTaskAllInfo;
import com.sun.jna.platform.mac.SystemB.ProcTaskInfo;
import com.sun.jna.platform.mac.SystemB.Timeval;
import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.tuple.Pair;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Executor;
import org.aoju.bus.health.builtin.software.*;
import org.aoju.bus.health.mac.SysctlKit;
import org.aoju.bus.health.mac.drivers.Who;
import org.aoju.bus.health.mac.drivers.WindowInfo;
import org.aoju.bus.logger.Logger;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * macOS, previously Mac Mac OS) is a series of proprietary
 * graphical operating systems developed and marketed by Apple Inc. since 2001.
 * It is the primary operating system for Apple's Mac computers.
 *
 * @author Kimi Liu
 * @version 6.2.6
 * @since JDK 1.8+
 */
@ThreadSafe
public class MacOperatingSystem extends AbstractOperatingSystem {

    private static final String SYSTEM_LIBRARY_LAUNCH_AGENTS = "/System/Library/LaunchAgents";
    private static final String SYSTEM_LIBRARY_LAUNCH_DAEMONS = "/System/Library/LaunchDaemons";
    private static final long BOOTTIME;

    static {
        Timeval tv = new Timeval();
        if (!SysctlKit.sysctl("kern.boottime", tv) || tv.tv_sec.longValue() == 0L) {
            // Usually this works. If it doesn't, fall back to text parsing.
            // Boot time will be the first consecutive string of digits.
            BOOTTIME = Builder.parseLongOrDefault(
                    Executor.getFirstAnswer("sysctl -n kern.boottime").split(Symbol.COMMA)[0].replaceAll("\\D", Normal.EMPTY),
                    System.currentTimeMillis() / 1000);
        } else {
            // tv now points to a 64-bit timeval structure for boot time.
            // First 4 bytes are seconds, second 4 bytes are microseconds
            // (we ignore)
            BOOTTIME = tv.tv_sec.longValue();
        }
    }

    private final String osXVersion;
    private final int major;
    private final int minor;
    private int maxProc = 1024;

    public MacOperatingSystem() {
        String version = System.getProperty("os.version");
        int verMajor = Builder.getFirstIntValue(version);
        int verMinor = Builder.getNthIntValue(version, 2);
        // Big Sur (11.x) may return 10.16
        if (verMajor == 10 && verMinor > 15) {
            String swVers = Executor.getFirstAnswer("sw_vers -productVersion");
            if (!swVers.isEmpty()) {
                version = swVers;
            }
            verMajor = Builder.getFirstIntValue(version);
            verMinor = Builder.getNthIntValue(version, 2);
        }
        this.osXVersion = version;
        this.major = verMajor;
        this.minor = verMinor;
        // Set max processes
        this.maxProc = SysctlKit.sysctl("kern.maxproc", 0x1000);
    }

    private static int getParentProcessPid(int pid) {
        ProcTaskAllInfo taskAllInfo = new ProcTaskAllInfo();
        if (0 > SystemB.INSTANCE.proc_pidinfo(pid, SystemB.PROC_PIDTASKALLINFO, 0, taskAllInfo, taskAllInfo.size())) {
            return 0;
        }
        return taskAllInfo.pbsd.pbi_ppid;
    }

    @Override
    public String queryManufacturer() {
        return "Apple";
    }

    @Override
    public Pair<String, OSVersionInfo> queryFamilyVersionInfo() {
        String family = this.major > 10 || (this.major == 10 && this.minor >= 12) ? "macOS"
                : System.getProperty("os.name");
        String codeName = parseCodeName();
        String buildNumber = SysctlKit.sysctl("kern.osversion", Normal.EMPTY);
        return Pair.of(family, new OperatingSystem.OSVersionInfo(this.osXVersion, codeName, buildNumber));
    }

    @Override
    protected int queryBitness(int jvmBitness) {
        if (jvmBitness == 64 || (this.major == 10 && this.minor > 6)) {
            return 64;
        }
        return Builder.parseIntOrDefault(Executor.getFirstAnswer("getconf LONG_BIT"), 32);
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
    public List<OSSession> getSessions() {
        return USE_WHO_COMMAND ? super.getSessions() : Who.queryUtxent();
    }

    @Override
    public List<OSProcess> queryAllProcesses() {
        List<OSProcess> procs = new ArrayList<>();
        int[] pids = new int[this.maxProc];
        int numberOfProcesses = SystemB.INSTANCE.proc_listpids(SystemB.PROC_ALL_PIDS, 0, pids,
                pids.length * SystemB.INT_SIZE) / SystemB.INT_SIZE;
        for (int i = 0; i < numberOfProcesses; i++) {
            // Handle off-by-one bug in proc_listpids where the size returned
            // is: SystemB.INT_SIZE * (pids + 1)
            if (pids[i] != 0) {
                OSProcess proc = getProcess(pids[i]);
                if (null != proc) {
                    procs.add(proc);
                }
            }
        }
        return procs;
    }

    @Override
    public List<OSProcess> queryChildProcesses(int parentPid) {
        List<OSProcess> allProcs = queryAllProcesses();
        Set<Integer> descendantPids = getChildrenOrDescendants(allProcs, parentPid, false);
        return allProcs.stream().filter(p -> descendantPids.contains(p.getProcessID())).collect(Collectors.toList());
    }

    @Override
    public List<OSProcess> queryDescendantProcesses(int parentPid) {
        List<OSProcess> allProcs = queryAllProcesses();
        Set<Integer> descendantPids = getChildrenOrDescendants(allProcs, parentPid, true);
        return allProcs.stream().filter(p -> descendantPids.contains(p.getProcessID())).collect(Collectors.toList());
    }

    @Override
    public OSProcess getProcess(int pid) {
        OSProcess proc = new MacOSProcess(pid, this.minor);
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
        ProcTaskInfo taskInfo = new ProcTaskInfo();
        for (int i = 0; i < numberOfProcesses; i++) {
            int exit = SystemB.INSTANCE.proc_pidinfo(pids[i], SystemB.PROC_PIDTASKINFO, 0, taskInfo, taskInfo.size());
            if (exit != -1) {
                numberOfThreads += taskInfo.pti_threadnum;
            }
        }
        return numberOfThreads;
    }

    @Override
    public long getSystemUptime() {
        return System.currentTimeMillis() / 1000 - BOOTTIME;
    }

    @Override
    public long getSystemBootTime() {
        return BOOTTIME;
    }

    @Override
    public NetworkParams getNetworkParams() {
        return new MacNetworkParams();
    }

    @Override
    public OSService[] getServices() {
        // Get running services
        List<OSService> services = new ArrayList<>();
        Set<String> running = new HashSet<>();
        for (OSProcess p : getChildProcesses(1, ProcessFiltering.ALL_PROCESSES, ProcessSorting.PID_ASC, 0)) {
            OSService s = new OSService(p.getName(), p.getProcessID(), OSService.State.RUNNING);
            services.add(s);
            running.add(p.getName());
        }
        // Get Directories for stopped services
        ArrayList<File> files = new ArrayList<>();
        File dir = new File(SYSTEM_LIBRARY_LAUNCH_AGENTS);
        if (dir.exists() && dir.isDirectory()) {
            files.addAll(Arrays.asList(dir.listFiles((f, name) -> name.toLowerCase().endsWith(".plist"))));
        } else {
            Logger.error("Directory: /System/Library/LaunchAgents does not exist");
        }
        dir = new File(SYSTEM_LIBRARY_LAUNCH_DAEMONS);
        if (dir.exists() && dir.isDirectory()) {
            files.addAll(Arrays.asList(dir.listFiles((f, name) -> name.toLowerCase().endsWith(".plist"))));
        } else {
            Logger.error("Directory: /System/Library/LaunchDaemons does not exist");
        }
        for (File f : files) {
            // remove .plist extension
            String name = f.getName().substring(0, f.getName().length() - 6);
            int index = name.lastIndexOf(Symbol.C_DOT);
            String shortName = (index < 0 || index > name.length() - 2) ? name : name.substring(index + 1);
            if (!running.contains(name) && !running.contains(shortName)) {
                OSService s = new OSService(name, 0, OSService.State.STOPPED);
                services.add(s);
            }
        }
        return services.toArray(new OSService[0]);
    }

    @Override
    public List<OSDesktopWindow> getDesktopWindows(boolean visibleOnly) {
        return WindowInfo.queryDesktopWindows(visibleOnly);
    }

    private String parseCodeName() {
        if (this.major > 10) {
            switch (this.major) {
                case 12:
                    return "Monterey";
                case 11:
                    return "Big Sur";
            }
        } else if (this.major == 10) {
            switch (this.minor) {
                case 15:
                    return "Catalina";
                case 14:
                    return "Mojave";
                case 13:
                    return "High Sierra";
                case 12:
                    return "Sierra";
                case 11:
                    return "El Capitan";
                case 10:
                    return "Yosemite";
                case 9:
                    return "Mavericks";
                case 8:
                    return "Mountain Lion";
                case 7:
                    return "Lion";
                case 6:
                    return "Snow Leopard";
                case 5:
                    return "Leopard";
                case 4:
                    return "Tiger";
                case 3:
                    return "Panther";
                case 2:
                    return "Jaguar";
                case 1:
                    return "Puma";
                case 0:
                    return "Cheetah";
                default:
            }
        }
        Logger.warn("Unable to parse version {}.{} to a codename.", this.major, this.minor);
        return "Big Sur";
    }

}
