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
package org.miaixz.bus.health.unix.aix.software;

import static org.miaixz.bus.health.builtin.software.OSProcess.State.INVALID;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sun.jna.Native;
import com.sun.jna.platform.unix.Resource;
import com.sun.jna.platform.unix.aix.Perfstat.perfstat_process_t;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.lang.tuple.Tuple;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.IdGroup;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.software.OSProcess;
import org.miaixz.bus.health.builtin.software.OSThread;
import org.miaixz.bus.health.builtin.software.common.AbstractOSProcess;
import org.miaixz.bus.health.unix.aix.driver.PsInfo;
import org.miaixz.bus.health.unix.aix.driver.perfstat.PerfstatCpu;
import org.miaixz.bus.health.unix.shared.jna.AixLibc;
import org.miaixz.bus.logger.Logger;

/**
 * OSProcess implementation
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public class AixOSProcess extends AbstractOSProcess {

    /**
     * The affinityMask value.
     */
    private final Supplier<Long> affinityMask = Memoizer
            .memoize(PerfstatCpu::queryCpuAffinityMask, Memoizer.defaultExpiration());

    /**
     * The os value.
     */
    private final AixOperatingSystem os;

    /**
     * The bitness value.
     */
    private final Supplier<Integer> bitness = Memoizer.memoize(this::queryBitness);

    /**
     * The psinfo value.
     */
    private final Supplier<AixLibc.AixPsInfo> psinfo = Memoizer
            .memoize(this::queryPsInfo, Memoizer.defaultExpiration());

    /**
     * The cmdEnv value.
     */
    private final Supplier<Pair<List<String>, Map<String, String>>> cmdEnv = Memoizer
            .memoize(this::queryCommandlineEnvironment);
    // Memoized copy from OperatingSystem
    /**
     * The procCpu value.
     */
    private final Supplier<perfstat_process_t[]> procCpu;

    /**
     * The name value.
     */
    private String name;

    /**
     * The commandLineBackup value.
     */
    private String commandLineBackup;

    /**
     * The commandLine value.
     */
    private final Supplier<String> commandLine = Memoizer.memoize(this::queryCommandLine);

    /**
     * The user value.
     */
    private String user;

    /**
     * The userID value.
     */
    private String userID;

    /**
     * The group value.
     */
    private String group;

    /**
     * The groupID value.
     */
    private String groupID;

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
     * The residentSetSize value.
     */
    private long residentSetSize;

    /**
     * The privateResidentMemory value.
     */
    private long privateResidentMemory;

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
     * The path value.
     */
    private String path = Normal.EMPTY;

    /**
     * The state value.
     */
    private OSProcess.State state = INVALID;

    /**
     * Creates a new AixOSProcess instance.
     *
     * @param pid     the pid
     * @param cpuMem  the cpu mem
     * @param procCpu the proc cpu
     * @param os      the os
     */
    public AixOSProcess(int pid, Tuple cpuMem, Supplier<perfstat_process_t[]> procCpu, AixOperatingSystem os) {
        super(pid);
        this.procCpu = procCpu;
        this.os = os;
        updateAttributes(cpuMem);
    }

    /**
     * Returns Enum STATE for the state value obtained from status string of thread/process.
     *
     * @param stateValue state value from the status string
     * @return The state
     */
    static OSProcess.State getStateFromOutput(char stateValue) {
        OSProcess.State state;
        switch (stateValue) {
            case 'O':
                state = OSProcess.State.INVALID;
                break;

            case 'R':
            case 'A':
                state = OSProcess.State.RUNNING;
                break;

            case 'I':
                state = OSProcess.State.WAITING;
                break;

            case 'S':
            case 'W':
                state = OSProcess.State.SLEEPING;
                break;

            case 'Z':
                state = OSProcess.State.ZOMBIE;
                break;

            case 'T':
                state = OSProcess.State.STOPPED;
                break;

            default:
                state = OSProcess.State.OTHER;
                break;
        }
        return state;
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
     * Queries the command line.
     *
     * @return the query command line result
     */
    private String queryCommandLine() {
        String cl = String.join(Symbol.SPACE, getArguments());
        return cl.isEmpty() ? this.commandLineBackup : cl;
    }

    /**
     * Returns the arguments.
     *
     * @return the get arguments result
     */
    @Override
    public List<String> getArguments() {
        return cmdEnv.get().getLeft();
    }

    /**
     * Returns the environment variables.
     *
     * @return the get environment variables result
     */
    @Override
    public Map<String, String> getEnvironmentVariables() {
        return cmdEnv.get().getRight();
    }

    /**
     * Queries the commandline environment.
     *
     * @return the query commandline environment result
     */
    private Pair<List<String>, Map<String, String>> queryCommandlineEnvironment() {
        return PsInfo.queryArgsEnv(getProcessID(), psinfo.get());
    }

    /**
     * Queries the ps info.
     *
     * @return the query ps info result
     */
    private AixLibc.AixPsInfo queryPsInfo() {
        return PsInfo.queryPsInfo(this.getProcessID());
    }

    /**
     * Returns the user.
     *
     * @return the get user result
     */
    @Override
    public String getUser() {
        return this.user;
    }

    /**
     * Returns the user id.
     *
     * @return the get user id result
     */
    @Override
    public String getUserID() {
        return this.userID;
    }

    /**
     * Returns the group.
     *
     * @return the get group result
     */
    @Override
    public String getGroup() {
        return this.group;
    }

    /**
     * Returns the group id.
     *
     * @return the get group id result
     */
    @Override
    public String getGroupID() {
        return this.groupID;
    }

    /**
     * Returns the current working directory.
     *
     * @return the get current working directory result
     */
    @Override
    public String getCurrentWorkingDirectory() {
        try {
            String cwdLink = "/proc/" + getProcessID() + "/cwd";
            String cwd = new File(cwdLink).getCanonicalPath();
            if (!cwd.equals(cwdLink)) {
                return cwd;
            }
        } catch (IOException e) {
            Logger.trace(
                    false,
                    "Health",
                    "Couldn't find cwd for pid {}: {}",
                    getProcessID(),
                    e.getClass().getSimpleName());
        }
        return Normal.EMPTY;
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
        return this.residentSetSize;
    }

    /**
     * Returns the private resident memory.
     *
     * @return the get private resident memory result
     */
    @Override
    public long getPrivateResidentMemory() {
        return this.privateResidentMemory;
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
        try (Stream<Path> fd = Files.list(Paths.get("/proc/" + getProcessID() + "/fd"))) {
            return fd.count();
        } catch (IOException e) {
            return 0L;
        }
    }

    /**
     * Returns the soft open file limit.
     *
     * @return the get soft open file limit result
     */
    @Override
    public long getSoftOpenFileLimit() {
        if (getProcessID() == this.os.getProcessId()) {
            final Resource.Rlimit rlimit = new Resource.Rlimit();
            AixLibc.INSTANCE.getrlimit(AixLibc.RLIMIT_NOFILE, rlimit);
            return rlimit.rlim_cur;
        } else {
            return -1L; // not supported
        }
    }

    /**
     * Returns the hard open file limit.
     *
     * @return the get hard open file limit result
     */
    @Override
    public long getHardOpenFileLimit() {
        if (getProcessID() == this.os.getProcessId()) {
            final Resource.Rlimit rlimit = new Resource.Rlimit();
            AixLibc.INSTANCE.getrlimit(AixLibc.RLIMIT_NOFILE, rlimit);
            return rlimit.rlim_max;
        } else {
            return -1L; // not supported
        }
    }

    /**
     * Returns the bitness.
     *
     * @return the get bitness result
     */
    @Override
    public int getBitness() {
        return this.bitness.get();
    }

    /**
     * Queries the bitness.
     *
     * @return the query bitness result
     */
    private int queryBitness() {
        List<String> pflags = Executor.runNative("pflags " + getProcessID());
        for (String line : pflags) {
            if (line.contains("data model")) {
                if (line.contains("LP32")) {
                    return 32;
                } else if (line.contains("LP64")) {
                    return 64;
                }
            }
        }
        return 0;
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
     * Returns the affinity mask.
     *
     * @return the get affinity mask result
     */
    @Override
    public long getAffinityMask() {
        long mask = 0L;
        // Need to capture pr_bndpro for all threads
        // Get process files in proc
        File directory = new File(String.format(Locale.ROOT, "/proc/%d/lwp", getProcessID()));
        File[] numericFiles = directory.listFiles(file -> Pattern.NUMBERS_PATTERN.matcher(file.getName()).matches());
        if (numericFiles == null) {
            return mask;
        }
        // Iterate files
        for (File lwpidFile : numericFiles) {
            int lwpidNum = Parsing.parseIntOrDefault(lwpidFile.getName(), 0);
            AixLibc.AixLwpsInfo info = PsInfo.queryLwpsInfo(getProcessID(), lwpidNum);
            if (info != null) {
                mask |= info.pr_bindpro;
            }
        }
        mask &= affinityMask.get();
        return mask;
    }

    /**
     * Returns the thread details.
     *
     * @return the get thread details result
     */
    @Override
    public List<OSThread> getThreadDetails() {
        // Get process files in proc
        File directory = new File(String.format(Locale.ROOT, "/proc/%d/lwp", getProcessID()));
        File[] numericFiles = directory.listFiles(file -> Pattern.NUMBERS_PATTERN.matcher(file.getName()).matches());
        if (numericFiles == null) {
            return Collections.emptyList();
        }

        return Arrays.stream(numericFiles).parallel()
                .map(lwpidFile -> new AixOSThread(getProcessID(), Parsing.parseIntOrDefault(lwpidFile.getName(), 0)))
                .filter(OSThread.ThreadFiltering.VALID_THREAD).collect(Collectors.toList());
    }

    /**
     * Updates the attributes.
     *
     * @return the update attributes result
     */
    @Override
    public boolean updateAttributes() {
        perfstat_process_t[] perfstat = procCpu.get();
        for (perfstat_process_t stat : perfstat) {
            int statpid = (int) stat.pid;
            if (statpid == getProcessID()) {
                return updateAttributes(
                        Tuple.of(
                                stat.ucpu_time,
                                stat.scpu_time,
                                stat.real_inuse * 1024L,
                                (stat.proc_real_mem_data + stat.proc_real_mem_text) * 1024L));
            }
        }
        this.state = OSProcess.State.INVALID;
        return false;
    }

    /**
     * Updates the attributes.
     *
     * @param cpuMem the cpu mem
     * @return the update attributes result
     */
    private boolean updateAttributes(Tuple cpuMem) {
        AixLibc.AixPsInfo info = psinfo.get();
        if (info == null) {
            this.state = OSProcess.State.INVALID;
            return false;
        }

        long now = System.currentTimeMillis();
        this.state = getStateFromOutput((char) info.pr_lwp.pr_sname);
        this.parentProcessID = (int) info.pr_ppid;
        this.userID = Long.toString(info.pr_euid);
        this.user = IdGroup.getUser(this.userID);
        this.groupID = Long.toString(info.pr_egid);
        this.group = IdGroup.getGroupName(this.groupID);
        this.threadCount = info.pr_nlwp;
        this.priority = info.pr_lwp.pr_pri;
        // These are in KB, multiply
        this.virtualSize = info.pr_size * 1024;
        this.residentSetSize = info.pr_rssize * 1024;
        this.startTime = info.pr_start.tv_sec * 1000L + info.pr_start.tv_nsec / 1_000_000L;
        // Avoid divide by zero for processes up less than a millisecond
        long elapsedTime = now - this.startTime;
        this.upTime = elapsedTime < 1L ? 1L : elapsedTime;
        this.userTime = (long) cpuMem.getMembers()[0];
        this.kernelTime = (long) cpuMem.getMembers()[1];
        if ((long) cpuMem.getMembers()[2] > 0) {
            this.residentSetSize = (long) cpuMem.getMembers()[2];
            this.privateResidentMemory = (long) cpuMem.getMembers()[3];
        } else {
            this.privateResidentMemory = this.residentSetSize;
        }
        this.commandLineBackup = Native.toString(info.pr_psargs);
        this.path = Pattern.SPACES_PATTERN.split(commandLineBackup)[0];
        this.name = this.path.substring(this.path.lastIndexOf('/') + 1);
        if (this.name.isEmpty()) {
            this.name = Native.toString(info.pr_fname);
        }
        return true;
    }

}
