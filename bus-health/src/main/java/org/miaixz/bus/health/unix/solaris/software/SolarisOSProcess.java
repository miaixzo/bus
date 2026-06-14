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
package org.miaixz.bus.health.unix.solaris.software;

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

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.IdGroup;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.software.OSProcess;
import org.miaixz.bus.health.builtin.software.OSThread;
import org.miaixz.bus.health.builtin.software.common.AbstractOSProcess;
import org.miaixz.bus.health.unix.shared.jna.SolarisLibc;
import org.miaixz.bus.health.unix.solaris.driver.PsInfo;
import org.miaixz.bus.logger.Logger;

/**
 * OSProcess implementation
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public class SolarisOSProcess extends AbstractOSProcess {

    /**
     * The os value.
     */
    private final SolarisOperatingSystem os;

    /**
     * The bitness value.
     */
    private final Supplier<Integer> bitness = Memoizer.memoize(this::queryBitness);

    /**
     * The psinfo value.
     */
    private final Supplier<SolarisLibc.SolarisPsInfo> psinfo = Memoizer
            .memoize(this::queryPsInfo, Memoizer.defaultExpiration());

    /**
     * The cmdEnv value.
     */
    private final Supplier<Pair<List<String>, Map<String, String>>> cmdEnv = Memoizer
            .memoize(this::queryCommandlineEnvironment);

    /**
     * The prusage value.
     */
    private final Supplier<SolarisLibc.SolarisPrUsage> prusage = Memoizer
            .memoize(this::queryPrUsage, Memoizer.defaultExpiration());

    /**
     * The name value.
     */
    private String name;

    /**
     * The path value.
     */
    private String path = Normal.EMPTY;

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
     * The residentSetSize value.
     */
    private long residentSetSize;

    /**
     * The residentSetSizePrivate value.
     */
    private long residentSetSizePrivate;

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
     * The minorFaults value.
     */
    private long minorFaults;

    /**
     * The majorFaults value.
     */
    private long majorFaults;

    /**
     * The contextSwitches value.
     */
    private long contextSwitches = 0; // default
    /**
     * The voluntaryContextSwitches value.
     */
    private long voluntaryContextSwitches = 0; // default
    /**
     * The involuntaryContextSwitches value.
     */
    private long involuntaryContextSwitches = 0; // default

    /**
     * Creates a new SolarisOSProcess instance.
     *
     * @param pid the pid
     * @param os  the os
     */
    public SolarisOSProcess(int pid, SolarisOperatingSystem os) {
        super(pid);
        this.os = os;
        updateAttributes();
    }

    /***
     * Returns Enum STATE for the state value obtained from status string of thread/process.
     *
     * @param stateValue state value from the status string
     * @return The state
     */
    static OSProcess.State getStateFromOutput(char stateValue) {
        OSProcess.State state;
        switch (stateValue) {
            case 'O':
                state = OSProcess.State.RUNNING;
                break;

            case 'S':
                state = OSProcess.State.SLEEPING;
                break;

            case 'R':
            case 'W':
                state = OSProcess.State.WAITING;
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
     * Queries the ps info.
     *
     * @return the query ps info result
     */
    private SolarisLibc.SolarisPsInfo queryPsInfo() {
        return PsInfo.queryPsInfo(this.getProcessID());
    }

    /**
     * Queries the pr usage.
     *
     * @return the query pr usage result
     */
    private SolarisLibc.SolarisPrUsage queryPrUsage() {
        return PsInfo.queryPrUsage(this.getProcessID());
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return the process name
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return the full path of the process executable
     */
    @Override
    public String getPath() {
        return this.path;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return the command line used to start the process
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
     * Description inherited from parent class or interface.
     *
     * @return the list of arguments passed to the process
     */
    @Override
    public List<String> getArguments() {
        return cmdEnv.get().getLeft();
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return a map of environment variables for the process
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
     * Description inherited from parent class or interface.
     *
     * @return the user name who owns the process
     */
    @Override
    public String getUser() {
        return this.user;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return the user ID of the process owner
     */
    @Override
    public String getUserID() {
        return this.userID;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return the group name that owns the process
     */
    @Override
    public String getGroup() {
        return this.group;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return the group ID of the process owner
     */
    @Override
    public String getGroupID() {
        return this.groupID;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return the current working directory of the process
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
     * Description inherited from parent class or interface.
     *
     * @return the parent process ID
     */
    @Override
    public int getParentProcessID() {
        return this.parentProcessID;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return the number of threads in the process
     */
    @Override
    public int getThreadCount() {
        return this.threadCount;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return the process scheduling priority
     */
    @Override
    public int getPriority() {
        return this.priority;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return the virtual memory size in bytes
     */
    @Override
    public long getVirtualSize() {
        return this.virtualSize;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return the resident set size (RSS) in bytes
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
        return this.residentSetSizePrivate;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return the kernel time in milliseconds
     */
    @Override
    public long getKernelTime() {
        return this.kernelTime;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return the user time in milliseconds
     */
    @Override
    public long getUserTime() {
        return this.userTime;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return the process uptime in milliseconds
     */
    @Override
    public long getUpTime() {
        return this.upTime;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return the process start time in milliseconds
     */
    @Override
    public long getStartTime() {
        return this.startTime;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return the number of bytes read from disk
     */
    @Override
    public long getBytesRead() {
        return this.bytesRead;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return the number of bytes written to disk
     */
    @Override
    public long getBytesWritten() {
        return this.bytesWritten;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return the number of minor page faults
     */
    @Override
    public long getMinorFaults() {
        return this.minorFaults;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return the number of major page faults
     */
    @Override
    public long getMajorFaults() {
        return this.majorFaults;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return the number of context switches
     */
    @Override
    public long getContextSwitches() {
        return this.contextSwitches;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return the number of voluntary context switches
     */
    @Override
    public long getVoluntaryContextSwitches() {
        return this.voluntaryContextSwitches;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return the number of involuntary context switches
     */
    @Override
    public long getInvoluntaryContextSwitches() {
        return this.involuntaryContextSwitches;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return the number of open file descriptors
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
     * Description inherited from parent class or interface.
     *
     * @return the soft limit of open files for the current process
     */
    @Override
    public long getSoftOpenFileLimit() {
        if (getProcessID() == this.os.getProcessId()) {
            final Resource.Rlimit rlimit = new Resource.Rlimit();
            if (SolarisLibc.INSTANCE.getrlimit(SolarisLibc.RLIMIT_NOFILE, rlimit) == 0) {
                return rlimit.rlim_cur;
            }
            return -1L;
        } else {
            return getProcessOpenFileLimit(getProcessID(), 1);
        }
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return the hard limit of open files for the current process
     */
    @Override
    public long getHardOpenFileLimit() {
        if (getProcessID() == this.os.getProcessId()) {
            final Resource.Rlimit rlimit = new Resource.Rlimit();
            if (SolarisLibc.INSTANCE.getrlimit(SolarisLibc.RLIMIT_NOFILE, rlimit) == 0) {
                return rlimit.rlim_max;
            }
            return -1L;
        } else {
            return getProcessOpenFileLimit(getProcessID(), 2);
        }
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return the process bitness (32 or 64)
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
     * Description inherited from parent class or interface.
     *
     * @return the state of the process
     */
    @Override
    public OSProcess.State getState() {
        return this.state;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return the processor affinity mask
     */
    @Override
    public long getAffinityMask() {
        long bitMask = 0L;
        String cpuset = Executor.getFirstAnswer("pbind -q " + getProcessID());
        // Sample output:
        // <empty string if no binding>
        // pid 101048 strongly bound to processor(s) 0 1 2 3.
        if (cpuset.isEmpty()) {
            List<String> allProcs = Executor.runNative("psrinfo");
            for (String proc : allProcs) {
                String[] split = Pattern.SPACES_PATTERN.split(proc);
                int bitToSet = Parsing.parseIntOrDefault(split[0], -1);
                if (bitToSet >= 0) {
                    bitMask |= 1L << bitToSet;
                }
            }
            return bitMask;
        } else if (cpuset.endsWith(".") && cpuset.contains("strongly bound to processor(s)")) {
            String parse = cpuset.substring(0, cpuset.length() - 1);
            String[] split = Pattern.SPACES_PATTERN.split(parse);
            for (int i = split.length - 1; i >= 0; i--) {
                int bitToSet = Parsing.parseIntOrDefault(split[i], -1);
                if (bitToSet >= 0) {
                    bitMask |= 1L << bitToSet;
                } else {
                    // Once we run into the word processor(s) we're done
                    break;
                }
            }
        }
        return bitMask;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return the list of thread details for the process
     */
    @Override
    public List<OSThread> getThreadDetails() {
        // Get process files in proc
        File directory = new File(String.format(Locale.ROOT, "/proc/%d/lwp", getProcessID()));
        File[] numericFiles = directory.listFiles(file -> Pattern.NUMBERS_PATTERN.matcher(file.getName()).matches());
        if (numericFiles == null) {
            return Collections.emptyList();
        }

        return Arrays.stream(numericFiles).parallel().map(
                lwpidFile -> new SolarisOSThread(getProcessID(), Parsing.parseIntOrDefault(lwpidFile.getName(), 0)))
                .filter(OSThread.ThreadFiltering.VALID_THREAD).collect(Collectors.toList());
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return {@code true} if the process attributes were successfully updated, {@code false} otherwise
     */
    @Override
    public boolean updateAttributes() {
        SolarisLibc.SolarisPsInfo info = psinfo.get();
        if (info == null) {
            this.state = OSProcess.State.INVALID;
            return false;
        }
        SolarisLibc.SolarisPrUsage usage = prusage.get();
        long now = System.currentTimeMillis();
        this.state = getStateFromOutput((char) info.pr_lwp.pr_sname);
        this.parentProcessID = info.pr_ppid;
        this.userID = Integer.toString(info.pr_euid);
        this.user = IdGroup.getUser(this.userID);
        this.groupID = Integer.toString(info.pr_egid);
        this.group = IdGroup.getGroupName(this.groupID);
        this.threadCount = info.pr_nlwp;
        this.priority = info.pr_lwp.pr_pri;
        // These are in KB, multiply
        this.virtualSize = info.pr_size.longValue() * 1024;
        this.residentSetSize = info.pr_rssize.longValue() * 1024;
        this.residentSetSizePrivate = info.pr_rssizepriv.longValue() * 1024;
        this.startTime = info.pr_start.tv_sec.longValue() * 1000L + info.pr_start.tv_nsec.longValue() / 1_000_000L;
        // Avoid divide by zero for processes up less than a millisecond
        long elapsedTime = now - this.startTime;
        this.upTime = elapsedTime < 1L ? 1L : elapsedTime;
        this.kernelTime = 0L;
        this.userTime = info.pr_time.tv_sec.longValue() * 1000L + info.pr_time.tv_nsec.longValue() / 1_000_000L;
        // 80 character truncation but enough for path and name (usually)
        this.commandLineBackup = Native.toString(info.pr_psargs);
        this.path = Pattern.SPACES_PATTERN.split(commandLineBackup)[0];
        this.name = this.path.substring(this.path.lastIndexOf('/') + 1);
        if (usage != null) {
            this.userTime = usage.pr_utime.tv_sec.longValue() * 1000L + usage.pr_utime.tv_nsec.longValue() / 1_000_000L;
            this.kernelTime = usage.pr_stime.tv_sec.longValue() * 1000L
                    + usage.pr_stime.tv_nsec.longValue() / 1_000_000L;
            this.bytesRead = usage.pr_ioch.longValue();
            this.majorFaults = usage.pr_majf.longValue();
            this.minorFaults = usage.pr_minf.longValue();
            this.voluntaryContextSwitches = usage.pr_vctx.longValue();
            this.involuntaryContextSwitches = usage.pr_ictx.longValue();
            this.contextSwitches = usage.pr_ictx.longValue() + usage.pr_vctx.longValue();
        }
        return true;
    }

    /**
     * Returns the process open file limit.
     *
     * @param processId the process id
     * @param index     the index
     * @return the get process open file limit result
     */
    private long getProcessOpenFileLimit(final long processId, final int index) {
        final List<String> output = Executor.runNative("plimit " + processId);
        if (output.isEmpty()) {
            return -1; // not supported
        }

        final Optional<String> nofilesLine = output.stream().filter(line -> line.trim().startsWith("nofiles"))
                .findFirst();
        if (!nofilesLine.isPresent()) {
            return -1;
        }

        // Split all non-Digits away -> ["", "{soft-limit}, "{hard-limit}"]
        final String[] split = nofilesLine.get().split("\\D+");
        if (split.length <= index) {
            return -1;
        }
        return Parsing.parseLongOrDefault(split[index], -1);
    }

}
