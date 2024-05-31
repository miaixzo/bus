/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org OSHI Team and other contributors.          *
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
package org.miaixz.bus.health.unix.platform.openbsd.software;

import org.miaixz.bus.core.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.software.*;
import org.miaixz.bus.health.builtin.software.common.AbstractOperatingSystem;
import org.miaixz.bus.health.unix.jna.OpenBsdLibc;
import org.miaixz.bus.health.unix.platform.openbsd.OpenBsdSysctlKit;
import org.miaixz.bus.logger.Logger;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * OpenBsd is a free and open-source Unix-like operating system descended from the Berkeley Software Distribution (BSD),
 * which was based on Research Unix.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public class OpenBsdOperatingSystem extends AbstractOperatingSystem {

    static final String PS_COMMAND_ARGS = Arrays.stream(PsKeywords.values()).map(Enum::name)
            .map(name -> name.toLowerCase(Locale.ROOT)).collect(Collectors.joining(Symbol.COMMA));
    private static final long BOOTTIME = querySystemBootTime();

    private static long querySystemBootTime() {
        // Boot time will be the first consecutive string of digits.
        return Parsing.parseLongOrDefault(
                Executor.getFirstAnswer("sysctl -n kern.boottime").split(Symbol.COMMA)[0].replaceAll("\\D", Normal.EMPTY),
                System.currentTimeMillis() / 1000);
    }

    @Override
    public String queryManufacturer() {
        return "Unix/BSD";
    }

    @Override
    public Pair<String, OperatingSystem.OSVersionInfo> queryFamilyVersionInfo() {
        int[] mib = new int[2];
        mib[0] = OpenBsdLibc.CTL_KERN;
        mib[1] = OpenBsdLibc.KERN_OSTYPE;
        String family = OpenBsdSysctlKit.sysctl(mib, "OpenBSD");
        mib[1] = OpenBsdLibc.KERN_OSRELEASE;
        String version = OpenBsdSysctlKit.sysctl(mib, Normal.EMPTY);
        mib[1] = OpenBsdLibc.KERN_VERSION;
        String versionInfo = OpenBsdSysctlKit.sysctl(mib, Normal.EMPTY);
        String buildNumber = versionInfo.split(Symbol.COLON)[0].replace(family, Normal.EMPTY).replace(version, Normal.EMPTY).trim();

        return Pair.of(family, new OperatingSystem.OSVersionInfo(version, null, buildNumber));
    }

    @Override
    protected int queryBitness(int jvmBitness) {
        if (jvmBitness < 64 && Executor.getFirstAnswer("uname -m").indexOf("64") == -1) {
            return jvmBitness;
        }
        return 64;
    }

    @Override
    public FileSystem getFileSystem() {
        return new OpenBsdFileSystem();
    }

    @Override
    public InternetProtocolStats getInternetProtocolStats() {
        return new OpenBsdInternetProtocolStats();
    }

    @Override
    public List<OSProcess> queryAllProcesses() {
        return getProcessListFromPS(-1);
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
        List<OSProcess> procs = getProcessListFromPS(pid);
        if (procs.isEmpty()) {
            return null;
        }
        return procs.get(0);
    }

    private List<OSProcess> getProcessListFromPS(int pid) {
        List<OSProcess> procs = new ArrayList<>();
        // https://man.openbsd.org/ps#KEYWORDS
        // missing are threadCount and kernelTime which is included in cputime
        String psCommand = "ps -awwxo " + PS_COMMAND_ARGS;
        if (pid >= 0) {
            psCommand += " -p " + pid;
        }
        List<String> procList = Executor.runNative(psCommand);
        if (procList.isEmpty() || procList.size() < 2) {
            return procs;
        }

        // remove header row
        procList.remove(0);
        // Fill list
        for (String proc : procList) {
            Map<PsKeywords, String> psMap = Parsing.stringToEnumMap(PsKeywords.class, proc.trim(), Symbol.C_SPACE);
            // Check if last (thus all) value populated
            if (psMap.containsKey(PsKeywords.ARGS)) {
                procs.add(new OpenBsdOSProcess(
                        pid < 0 ? Parsing.parseIntOrDefault(psMap.get(PsKeywords.PID), 0) : pid, psMap, this));
            }
        }
        return procs;
    }

    @Override
    public int getProcessId() {
        return OpenBsdLibc.INSTANCE.getpid();
    }

    @Override
    public int getProcessCount() {
        List<String> procList = Executor.runNative("ps -axo pid");
        if (!procList.isEmpty()) {
            // Subtract 1 for header
            return procList.size() - 1;
        }
        return 0;
    }

    @Override
    public int getThreadId() {
        return OpenBsdLibc.INSTANCE.getthrid();
    }

    @Override
    public OSThread getCurrentThread() {
        OSProcess proc = getCurrentProcess();
        final int tid = getThreadId();
        return proc.getThreadDetails().stream().filter(t -> t.getThreadId() == tid).findFirst()
                .orElse(new OpenBsdOSThread(proc.getProcessID(), tid));
    }

    @Override
    public int getThreadCount() {
        // -H "Also display information about kernel visible threads"
        // -k "Also display information about kernel threads"
        // column TID holds thread ID
        List<String> threadList = Executor.runNative("ps -axHo tid");
        if (!threadList.isEmpty()) {
            // Subtract 1 for header
            return threadList.size() - 1;
        }
        return 0;
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
        return new OpenBsdNetworkParams();
    }

    @Override
    public List<OSService> getServices() {
        // Get running services
        List<OSService> services = new ArrayList<>();
        Set<String> running = new HashSet<>();
        for (OSProcess p : getChildProcesses(1, OperatingSystem.ProcessFiltering.ALL_PROCESSES, OperatingSystem.ProcessSorting.PID_ASC, 0)) {
            OSService s = new OSService(p.getName(), p.getProcessID(), OSService.State.RUNNING);
            services.add(s);
            running.add(p.getName());
        }
        // Get Directories for stopped services
        File dir = new File("/etc/rc.d");
        File[] listFiles;
        if (dir.exists() && dir.isDirectory() && (listFiles = dir.listFiles()) != null) {
            for (File f : listFiles) {
                String name = f.getName();
                if (!running.contains(name)) {
                    OSService s = new OSService(name, 0, OSService.State.STOPPED);
                    services.add(s);
                }
            }
        } else {
            Logger.error("Directory: /etc/rc.d does not exist");
        }
        return services;
    }

    /**
     * Package-private for use by OpenBsdOSProcess
     */
    enum PsKeywords {
        STATE, PID, PPID, USER, UID, GROUP, GID, PRI, VSZ, RSS, ETIME, CPUTIME, COMM, MAJFLT, MINFLT, NVCSW, NIVCSW,
        ARGS // ARGS must always be last
    }
}
