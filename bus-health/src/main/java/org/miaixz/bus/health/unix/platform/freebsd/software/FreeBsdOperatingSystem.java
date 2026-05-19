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
package org.miaixz.bus.health.unix.platform.freebsd.software;

import java.io.File;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.sun.jna.ptr.NativeLongByReference;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.software.*;
import org.miaixz.bus.health.builtin.software.common.AbstractOperatingSystem;
import org.miaixz.bus.health.unix.jna.FreeBsdLibc;
import org.miaixz.bus.health.unix.platform.freebsd.BsdSysctlKit;
import org.miaixz.bus.health.unix.platform.freebsd.driver.Who;
import org.miaixz.bus.logger.Logger;

/**
 * FreeBSD is a free and open-source Unix-like operating system descended from the Berkeley Software Distribution (BSD),
 * which was based on Research Unix. The first version of FreeBSD was released in 1993. In 2005, FreeBSD was the most
 * popular open-source BSD operating system, accounting for more than three-quarters of all installed simply,
 * permissively licensed BSD systems.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public class FreeBsdOperatingSystem extends AbstractOperatingSystem {

    /**
     * Constructs a new FreeBsdOperatingSystem instance.
     */
    public FreeBsdOperatingSystem() {
        // No initialization required.
    }

    /**
     * The PS_COMMAND_ARGS constant.
     */
    static final String PS_COMMAND_ARGS = Arrays.stream(PsKeywords.values()).map(Enum::name)
            .map(name -> name.toLowerCase(Locale.ROOT)).collect(Collectors.joining(Symbol.COMMA));

    /**
     * The BOOTTIME constant.
     */
    private static final long BOOTTIME = querySystemBootTime();

    /**
     * Queries the system boot time.
     *
     * @return the query system boot time result
     */
    private static long querySystemBootTime() {
        FreeBsdLibc.Timeval tv = new FreeBsdLibc.Timeval();
        if (!BsdSysctlKit.sysctl("kern.boottime", tv) || tv.tv_sec == 0) {
            // Usually this works. If it doesn't, fall back to text parsing.
            // Boot time will be the first consecutive string of digits.
            return Parsing.parseLongOrDefault(
                    Executor.getFirstAnswer("sysctl -n kern.boottime").split(Symbol.COMMA)[0]
                            .replaceAll("\\D", Normal.EMPTY),
                    System.currentTimeMillis() / 1000);
        }
        // tv now points to a 128-bit timeval structure for boot time.
        // First 8 bytes are seconds, second 8 bytes are microseconds (we ignore)
        return tv.tv_sec;
    }

    /**
     * Queries the manufacturer.
     *
     * @return the query manufacturer result
     */
    @Override
    public String queryManufacturer() {
        return "Unix/BSD";
    }

    /**
     * Queries the family version info.
     *
     * @return the query family version info result
     */
    @Override
    public Pair<String, OperatingSystem.OSVersionInfo> queryFamilyVersionInfo() {
        String family = BsdSysctlKit.sysctl("kern.ostype", "FreeBSD");

        String version = BsdSysctlKit.sysctl("kern.osrelease", Normal.EMPTY);
        String versionInfo = BsdSysctlKit.sysctl("kern.version", Normal.EMPTY);
        String buildNumber = versionInfo.split(Symbol.COLON)[0].replace(family, Normal.EMPTY)
                .replace(version, Normal.EMPTY).trim();

        return Pair.of(family, new OperatingSystem.OSVersionInfo(version, null, buildNumber));
    }

    /**
     * Queries the bitness.
     *
     * @param jvmBitness the jvm bitness
     * @return the query bitness result
     */
    @Override
    protected int queryBitness(int jvmBitness) {
        if (jvmBitness < 64 && Executor.getFirstAnswer("uname -m").indexOf("64") == -1) {
            return jvmBitness;
        }
        return 64;
    }

    /**
     * Returns the file system.
     *
     * @return the get file system result
     */
    @Override
    public FileSystem getFileSystem() {
        return new FreeBsdFileSystem();
    }

    /**
     * Returns the internet protocol stats.
     *
     * @return the get internet protocol stats result
     */
    @Override
    public InternetProtocolStats getInternetProtocolStats() {
        return new FreeBsdInternetProtocolStats();
    }

    /**
     * Returns the sessions.
     *
     * @return the get sessions result
     */
    @Override
    public List<OSSession> getSessions() {
        return USE_WHO_COMMAND ? super.getSessions() : Who.queryUtxent();
    }

    /**
     * Queries the all processes.
     *
     * @return the query all processes result
     */
    @Override
    public List<OSProcess> queryAllProcesses() {
        return getProcessListFromPS(-1);
    }

    /**
     * Queries the child processes.
     *
     * @param parentPid the parent pid
     * @return the query child processes result
     */
    @Override
    public List<OSProcess> queryChildProcesses(int parentPid) {
        List<OSProcess> allProcs = queryAllProcesses();
        Set<Integer> descendantPids = getChildrenOrDescendants(allProcs, parentPid, false);
        return allProcs.stream().filter(p -> descendantPids.contains(p.getProcessID())).collect(Collectors.toList());
    }

    /**
     * Queries the descendant processes.
     *
     * @param parentPid the parent pid
     * @return the query descendant processes result
     */
    @Override
    public List<OSProcess> queryDescendantProcesses(int parentPid) {
        List<OSProcess> allProcs = queryAllProcesses();
        Set<Integer> descendantPids = getChildrenOrDescendants(allProcs, parentPid, true);
        return allProcs.stream().filter(p -> descendantPids.contains(p.getProcessID())).collect(Collectors.toList());
    }

    /**
     * Returns the process.
     *
     * @param pid the pid
     * @return the get process result
     */
    @Override
    public OSProcess getProcess(int pid) {
        List<OSProcess> procs = getProcessListFromPS(pid);
        if (procs.isEmpty()) {
            return null;
        }
        return procs.get(0);
    }

    /**
     * Returns the process list from ps.
     *
     * @param pid the pid
     * @return the get process list from ps result
     */
    private List<OSProcess> getProcessListFromPS(int pid) {
        String psCommand = "ps -awwxo " + PS_COMMAND_ARGS;
        if (pid >= 0) {
            psCommand += " -p " + pid;
        }

        Predicate<Map<PsKeywords, String>> hasKeywordArgs = psMap -> psMap.containsKey(PsKeywords.ARGS);
        return Executor.runNative(psCommand).stream().skip(1).parallel()
                .map(proc -> Parsing.stringToEnumMap(PsKeywords.class, proc.trim(), Symbol.C_SPACE))
                .filter(hasKeywordArgs)
                .map(
                        psMap -> new FreeBsdOSProcess(
                                pid < 0 ? Parsing.parseIntOrDefault(psMap.get(PsKeywords.PID), 0) : pid, psMap, this))
                .filter(OperatingSystem.ProcessFiltering.VALID_PROCESS).collect(Collectors.toList());
    }

    /**
     * Returns the process id.
     *
     * @return the get process id result
     */
    @Override
    public int getProcessId() {
        return FreeBsdLibc.INSTANCE.getpid();
    }

    /**
     * Returns the process count.
     *
     * @return the get process count result
     */
    @Override
    public int getProcessCount() {
        List<String> procList = Executor.runNative("ps -axo pid");
        if (!procList.isEmpty()) {
            // Subtract 1 for header
            return procList.size() - 1;
        }
        return 0;
    }

    /**
     * Returns the thread id.
     *
     * @return the get thread id result
     */
    @Override
    public int getThreadId() {
        NativeLongByReference pTid = new NativeLongByReference();
        if (FreeBsdLibc.INSTANCE.thr_self(pTid) < 0) {
            return 0;
        }
        return pTid.getValue().intValue();
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
                .orElse(new FreeBsdOSThread(proc.getProcessID(), tid));
    }

    /**
     * Returns the thread count.
     *
     * @return the get thread count result
     */
    @Override
    public int getThreadCount() {
        int threads = 0;
        for (String proc : Executor.runNative("ps -axo nlwp")) {
            threads += Parsing.parseIntOrDefault(proc.trim(), 0);
        }
        return threads;
    }

    /**
     * Returns the system uptime.
     *
     * @return the get system uptime result
     */
    @Override
    public long getSystemUptime() {
        return System.currentTimeMillis() / 1000 - BOOTTIME;
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
     * Returns the services.
     *
     * @return the get services result
     */
    @Override
    public List<OSService> getServices() {
        // Get running services
        List<OSService> services = new ArrayList<>();
        Set<String> running = new HashSet<>();
        for (OSProcess p : getChildProcesses(
                1,
                OperatingSystem.ProcessFiltering.ALL_PROCESSES,
                OperatingSystem.ProcessSorting.PID_ASC,
                0)) {
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
            Logger.error(false, "Health", "Directory: /etc/init does not exist");
        }
        return services;
    }

    /**
     * Returns the network params.
     *
     * @return the get network params result
     */
    @Override
    public NetworkParams getNetworkParams() {
        return new FreeBsdNetworkParams();
    }

    /*
     * Package-private for use by FreeBsdOSProcess
     */
    /**
     * The PsKeywords enum.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    enum PsKeywords {
        STATE, PID, PPID, USER, UID, GROUP, GID, NLWP, PRI, VSZ, RSS, ETIMES, SYSTIME, TIME, COMM, MAJFLT, MINFLT,
        NVCSW, NIVCSW, ARGS // ARGS must always be last

    }

}
