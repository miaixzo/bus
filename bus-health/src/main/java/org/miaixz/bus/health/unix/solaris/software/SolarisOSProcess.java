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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sun.jna.Native;
import com.sun.jna.platform.unix.Resource;

import org.miaixz.bus.core.center.function.SupplierX;
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
    private final SupplierX<Integer> bitness = Memoizer.memoize(this::queryBitness);

    /**
     * The psinfo value.
     */
    private final SupplierX<SolarisLibc.SolarisPsInfo> psinfo = Memoizer
            .memoize(this::queryPsInfo, Memoizer.defaultExpiration());

    /**
     * The cmdEnv value.
     */
    private final SupplierX<Pair<List<String>, Map<String, String>>> cmdEnv = Memoizer
            .memoize(this::queryCommandlineEnvironment);

    /**
     * The prusage value.
     */
    private final SupplierX<SolarisLibc.SolarisPrUsage> prusage = Memoizer
            .memoize(this::queryPrUsage, Memoizer.defaultExpiration());

    /**
     * The commandLine value.
     */
    private final SupplierX<String> commandLine = Memoizer.memoize(this::queryCommandLine);

    /**
     * The name value.
     */
    private volatile String name;

    /**
     * The path value.
     */
    private volatile String path = Normal.EMPTY;

    /**
     * The commandLineBackup value.
     */
    private volatile String commandLineBackup;

    /**
     * The user value.
     */
    private volatile String user;

    /**
     * The userID value.
     */
    private volatile String userID;

    /**
     * The group value.
     */
    private volatile String group;

    /**
     * The groupID value.
     */
    private volatile String groupID;

    /**
     * The state value.
     */
    private volatile OSProcess.State state = OSProcess.State.INVALID;

    /**
     * The parentProcessID value.
     */
    private volatile int parentProcessID;

    /**
     * The threadCount value.
     */
    private volatile int threadCount;

    /**
     * The priority value.
     */
    private volatile int priority;

    /**
     * The virtualSize value.
     */
    private volatile long virtualSize;

    /**
     * The residentSetSize value.
     */
    private volatile long residentSetSize;

    /**
     * The residentSetSizePrivate value.
     */
    private volatile long residentSetSizePrivate;

    /**
     * The kernelTime value.
     */
    private volatile long kernelTime;

    /**
     * The userTime value.
     */
    private volatile long userTime;

    /**
     * The startTime value.
     */
    private volatile long startTime;

    /**
     * The upTime value.
     */
    private volatile long upTime;

    /**
     * The bytesRead value.
     */
    private volatile long bytesRead;

    /**
     * The bytesWritten value.
     */
    private volatile long bytesWritten;

    /**
     * The minorFaults value.
     */
    private volatile long minorFaults;

    /**
     * The majorFaults value.
     */
    private volatile long majorFaults;

    /**
     * Default.
     */
    private volatile long contextSwitches = 0;
    /**
     * Default.
     */
    private volatile long voluntaryContextSwitches = 0;
    /**
     * Default.
     */
    private volatile long involuntaryContextSwitches = 0;

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
     * Gets the name of the process, often the executable program.
     *
     * @return the process name
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Gets the full filesystem path of the executing process.
     *
     * @return the full path of the process executable
     */
    @Override
    public String getPath() {
        return this.path;
    }

    /**
     * Gets the process command line used to start the process, including arguments if available to be determined. This
     * method generally returns the same information as {@link #getArguments()} in a more user-readable format, and is
     * more robust to non-elevated access.
     * <p>
     * The format of this string is platform-dependent, may be truncated, and may require the end user to parse the
     * result. Users should generally prefer {@link #getArguments()} which already parses the results, and use this
     * method as a backup.
     * </p>
     * <p>
     * On AIX and Solaris, the string may be truncated to 80 characters if there was insufficient permission to read the
     * process memory.
     * </p>
     * <p>
     * On Windows, attempts to retrieve the value from process memory, which requires that the process be owned by the
     * same user as the executing process, or elevated permissions, and additionally requires the target process to have
     * the same bitness (e.g., this will fail on a 32-bit process if queried by 64-bit and vice versa). If reading
     * process memory fails, by default, performs a single WMI query for this process, with some latency. If this method
     * will be frequently called for multiple processes, see the configuration file to enable a batch query mode
     * leveraging {@link org.miaixz.bus.health.windows.driver.wmi.Win32ProcessCached#getCommandLine} to improve
     * performance, or setting that parameter via {@link org.miaixz.bus.health.Builder#set(String, Object)} before
     * instantiating any {@link org.miaixz.bus.health.builtin.software.OSProcess} object.
     * </p>
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
     * Makes a best effort attempt to get a list of the the command-line arguments of the process. Returns the same
     * information as {@link #getCommandLine()} but parsed to a list. May require elevated permissions or same-user
     * ownership.
     *
     * @return the list of arguments passed to the process
     */
    @Override
    public List<String> getArguments() {
        return cmdEnv.get().getLeft();
    }

    /**
     * Makes a best effort attempt to obtain the environment variables of the process. May require elevated permissions
     * or same-user ownership.
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
     * Gets the user name of the process owner.
     *
     * @return the user name who owns the process
     */
    @Override
    public String getUser() {
        return this.user;
    }

    /**
     * Gets the user id of the process owner.
     *
     * @return the user ID of the process owner
     */
    @Override
    public String getUserID() {
        return this.userID;
    }

    /**
     * Gets the group under which the process is executing.
     * <p>
     * On Windows systems, populating this value for processes other than the current user requires administrative
     * privileges (and still may fail for some system processes) and can incur significant latency. When successful,
     * returns a the default primary group with access to this process, corresponding to the SID in
     * {@link #getGroupID()}.
     *
     * @return the group name that owns the process
     */
    @Override
    public String getGroup() {
        return this.group;
    }

    /**
     * Gets the group id under which the process is executing.
     * <p>
     * On Windows systems, populating this value for processes other than the current user requires administrative
     * privileges (and still may fail for some system processes) and can incur significant latency. When successful,
     * returns the default primary group SID with access to this process, corresponding to the name in
     * {@link #getGroup()}.
     *
     * @return the group ID of the process owner
     */
    @Override
    public String getGroupID() {
        return this.groupID;
    }

    /**
     * Makes a best effort attempt to obtain the current working directory for the process.
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
     * Gets the process ID of this process's parent.
     *
     * @return the parent process ID
     */
    @Override
    public int getParentProcessID() {
        return this.parentProcessID;
    }

    /**
     * Gets the number of threads being executed by this process. More information is available using
     * {@link #getThreadDetails()}.
     *
     * @return the number of threads in the process
     */
    @Override
    public int getThreadCount() {
        return this.threadCount;
    }

    /**
     * Gets the priority of this process.
     * <p>
     * For Linux and Unix, priority is a value in the range -20 to 19 (20 on some systems). The default priority is 0;
     * lower priorities cause more favorable scheduling.
     * <p>
     * For Windows, priority values can range from 0 (lowest priority) to 31 (highest priority).
     * <p>
     * macOS has 128 priority levels, ranging from 0 (lowest priority) to 127 (highest priority). They are divided into
     * several major bands: 0 through 51 are the normal levels; the default priority is 31. 52 through 79 are the
     * highest priority regular threads; 80 through 95 are for kernel mode threads; and 96 through 127 correspond to
     * real-time threads, which are treated differently than other threads by the scheduler.
     *
     * @return the process scheduling priority
     */
    @Override
    public int getPriority() {
        return this.priority;
    }

    /**
     * Gets the Virtual Memory Size (VSZ). Includes all memory that the process can access, including memory that is
     * swapped out and memory that is from shared libraries.
     *
     * @return the virtual memory size in bytes
     */
    @Override
    public long getVirtualSize() {
        return this.virtualSize;
    }

    /**
     * Returns the total amount of physical memory (RAM) currently mapped to the process's address space. This value
     * represents the Resident Set Size (RSS) and includes both private memory and memory shared with other processes
     * (such as shared libraries). This aligns with the reporting behavior of standard command-line utilities like
     * {@code ps} and {@code top}.
     * <p>
     * On Linux, returns the RSS value from {@code /proc/[pid]/stat}, which may be inaccurate because of a
     * kernel-internal scalability optimization. If accurate values are required, read {@code /proc/[pid]/smaps} using
     * {@link org.miaixz.bus.health.Builder#getKeyValueMapFromFile(String, String)}.
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
     * Gets kernel/system (privileged) time used by the process.
     *
     * @return the kernel time in milliseconds
     */
    @Override
    public long getKernelTime() {
        return this.kernelTime;
    }

    /**
     * Gets user time used by the process.
     *
     * @return the user time in milliseconds
     */
    @Override
    public long getUserTime() {
        return this.userTime;
    }

    /**
     * Gets up time / elapsed time since the process started.
     *
     * @return the process uptime in milliseconds
     */
    @Override
    public long getUpTime() {
        return this.upTime;
    }

    /**
     * Gets the process start time.
     *
     * @return the process start time in milliseconds
     */
    @Override
    public long getStartTime() {
        return this.startTime;
    }

    /**
     * Gets the bytes read by the process. This includes all I/O activity generated by the process to include file,
     * network, and device I/Os.
     * <p>
     * On Solaris, includes both bytes read and written.
     *
     * @return the number of bytes read from disk
     */
    @Override
    public long getBytesRead() {
        return this.bytesRead;
    }

    /**
     * Gets the bytes written by the process. This includes all I/O activity generated by the process to include file,
     * network, and device I/Os.
     * <p>
     * On Solaris, all IO bytes are included read bytes so this value is 0.
     *
     * @return the number of bytes written to disk
     */
    @Override
    public long getBytesWritten() {
        return this.bytesWritten;
    }

    /**
     * Gets the number of minor (soft) faults the process has made which have not required loading a memory page from
     * disk. Sometimes called reclaims.
     * <p>
     * On Windows, this includes the total of major and minor faults.
     * <p>
     * Not available on AIX.
     *
     * @return the number of minor page faults
     */
    @Override
    public long getMinorFaults() {
        return this.minorFaults;
    }

    /**
     * Gets the number of major (hard) faults the process has made which have required loading a memory page from disk.
     * <p>
     * Windows does not distinguish major and minor faults at the process level, so this value returns 0 and major
     * faults are included in {@link #getMinorFaults()}.
     * <p>
     * Not available on AIX.
     *
     * @return the number of major page faults
     */
    @Override
    public long getMajorFaults() {
        return this.majorFaults;
    }

    /**
     * A snapshot of the context switches the process has done, equal to the sum of
     * {@link #getVoluntaryContextSwitches()} and {@link #getInvoluntaryContextSwitches()} on platforms that provide the
     * split.
     * <p>
     * On Windows, this may return a total from available counters while the split remains unavailable. On macOS for
     * non-current processes, this may return the combined total from the kernel while the split remains unavailable.
     * <p>
     * Not available on AIX.
     *
     * @return the number of context switches
     */
    @Override
    public long getContextSwitches() {
        return this.contextSwitches;
    }

    /**
     * The number of voluntary context switches the process has made. A voluntary context switch occurs when a process
     * gives up the CPU before its time slice expires.
     *
     * @return the number of voluntary context switches
     */
    @Override
    public long getVoluntaryContextSwitches() {
        return this.voluntaryContextSwitches;
    }

    /**
     * The number of involuntary context switches the process has made. An involuntary context switch occurs when the
     * scheduler preempts the process.
     *
     * @return the number of involuntary context switches
     */
    @Override
    public long getInvoluntaryContextSwitches() {
        return this.involuntaryContextSwitches;
    }

    /**
     * Gets the number of open file handles (or network connections) that belongs to the process.
     * <p>
     * On FreeBSD and Solaris, this value is only populated if information for a single process id is requested.
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
     * Gets the soft limit for open file handles (or network connections) of the given process.
     * <p>
     * Retrieving the soft limit for processes other than the calling process is only supported on Linux, FreeBsd and
     * Solaris.
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
     * Gets the hard limit for open file handles (or network connections) that belong to the given process.
     * <p>
     * Retrieving the hard limit for processes other than the calling process is only supported on Linux, FreeBsd and
     * Solaris.
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
     * Attempts to get the bitness (32 or 64) of the process.
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
     * Gets the process state.
     *
     * @return the state of the process
     */
    @Override
    public OSProcess.State getState() {
        return this.state;
    }

    /**
     * Gets the process affinity mask for this process.
     * <p>
     * On Windows systems with more than 64 processors, if the threads of the calling process are in a single processor
     * group, returns the process affinity mask for that group (which may be zero if the specified process is running in
     * a different group). If the calling process contains threads in multiple groups, returns zero.
     * <p>
     * Because macOS does not export interfaces that identify processors or control thread placement, explicit thread to
     * processor binding is not supported and this method will return a bitmask of all logical processors.
     * <p>
     * If the Operating System fails to retrieve an affinity mask (e.g., the process has terminated), returns zero.
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
        } else if (cpuset.endsWith(Symbol.DOT) && cpuset.contains("strongly bound to processor(s)")) {
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
     * Retrieves the threads of the process and their details.
     * <p>
     * The amount of returned information is operating-system dependent and may incur some latency.
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
     * Attempts to update process attributes. Returns false if the update fails, which will occur if the process no
     * longer exists.
     *
     * @return {@code true} if the process attributes were successfully updated, {@code false} otherwise
     */
    @Override
    public synchronized boolean updateAttributes() {
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
        this.name = this.path.substring(this.path.lastIndexOf(Symbol.C_SLASH) + 1);
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
