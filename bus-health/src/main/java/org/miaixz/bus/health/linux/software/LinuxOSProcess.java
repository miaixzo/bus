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
package org.miaixz.bus.health.linux.software;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import com.sun.jna.platform.unix.Resource;

import org.miaixz.bus.core.center.function.SupplierX;
import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Regex;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.health.*;
import org.miaixz.bus.health.builtin.software.OSThread;
import org.miaixz.bus.health.builtin.software.common.AbstractOSProcess;
import org.miaixz.bus.health.linux.ProcPath;
import org.miaixz.bus.health.linux.driver.proc.ProcessStat;
import org.miaixz.bus.health.linux.jna.LinuxLibc;
import org.miaixz.bus.health.unix.shared.driver.ProcLimits;
import org.miaixz.bus.logger.Logger;

/**
 * OSProcess implementation
 *
 * @author Kimi Liu
 */
@ThreadSafe
public class LinuxOSProcess extends AbstractOSProcess {

    /**
     * The LOG_PROCFS_WARNING constant.
     */
    private static final boolean LOG_PROCFS_WARNING = Builder.get(Builder._LINUX_PROCFS_LOGWARNING, false);

    /**
     * Get a list of orders to pass to Parsing.
     */
    private static final int[] PROC_PID_STAT_ORDERS = new int[ProcPidStat.values().length];

    static {
        for (ProcPidStat stat : ProcPidStat.values()) {
            // The PROC_PID_STAT enum indices are 1-indexed.
            // Subtract one to get a zero-based index
            PROC_PID_STAT_ORDERS[stat.ordinal()] = stat.getOrder() - 1;
        }
    }

    /**
     * The os value.
     */
    private final LinuxOperatingSystem os;

    /**
     * The commandLine value.
     */
    private final SupplierX<String> commandLine = Memoizer.memoize(this::queryCommandLine);

    /**
     * The arguments value.
     */
    private final SupplierX<List<String>> arguments = Memoizer.memoize(this::queryArguments);

    /**
     * The environmentVariables value.
     */
    private final SupplierX<Map<String, String>> environmentVariables = Memoizer
            .memoize(this::queryEnvironmentVariables);

    /**
     * The bitness value.
     */
    private final SupplierX<Integer> bitness = Memoizer.memoize(this::queryBitness);

    /**
     * The user value.
     */
    private final SupplierX<String> user = Memoizer.memoize(this::queryUser);

    /**
     * The group value.
     */
    private final SupplierX<String> group = Memoizer.memoize(this::queryGroup);

    /**
     * The path value.
     */
    private volatile String path = Normal.EMPTY;

    /**
     * The userID value.
     */
    private volatile String userID;

    /**
     * The groupID value.
     */
    private volatile String groupID;

    /**
     * The name value.
     */
    private volatile String name;

    /**
     * The state value.
     */
    private volatile State state = State.INVALID;

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
     * The privateResidentMemory value.
     */
    private volatile long privateResidentMemory;

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
     * The contextSwitches value.
     */
    private volatile long contextSwitches;

    /**
     * The voluntaryContextSwitches value.
     */
    private volatile long voluntaryContextSwitches;

    /**
     * The involuntaryContextSwitches value.
     */
    private volatile long involuntaryContextSwitches;

    /**
     * Whether the native rusage counters were populated.
     */
    private volatile boolean rusagePopulated;

    /**
     * The cached voluntary context-switch count from native rusage.
     */
    private volatile long cachedVoluntaryContextSwitches;

    /**
     * The cached involuntary context-switch count from native rusage.
     */
    private volatile long cachedInvoluntaryContextSwitches;

    /**
     * Creates a new LinuxOSProcess instance.
     *
     * @param pid the pid
     * @param os  the os
     */
    public LinuxOSProcess(int pid, LinuxOperatingSystem os) {
        super(pid);
        this.os = os;
        updateAttributes();
    }

    /**
     * If some details couldn't be read from ProcPath.PID_STATUS try reading it from ProcPath.PID_STAT
     *
     * @param status status map to fill.
     * @param stat   string to read from.
     */
    private static void getMissingDetails(Map<String, String> status, String stat) {
        if (status == null || stat == null) {
            return;
        }

        int nameStart = stat.indexOf(Symbol.C_PARENTHESE_LEFT);
        int nameEnd = stat.lastIndexOf(Symbol.C_PARENTHESE_RIGHT);
        if (StringKit.isBlank(status.get("Name")) && nameStart > 0 && nameStart < nameEnd) {
            // remove leading and trailing parentheses
            String statName = stat.substring(nameStart + 1, nameEnd);
            status.put("Name", statName);
        }

        // As per man, the next item after the name is the state
        if (StringKit.isBlank(status.get("State")) && nameEnd > 0 && stat.length() > nameEnd + 2) {
            String statState = String.valueOf(stat.charAt(nameEnd + 2));
            status.put("State", statState);
        }
    }

    /**
     * Parse the affinity mask from taskset output.
     *
     * @param tasksetOutput output of {@code taskset -p <pid>}
     * @return the affinity mask as a long, or 0 if unparseable
     */
    static long parseAffinityMask(String tasksetOutput) {
        // Output:
        // pid 3283's current affinity mask: 3
        // pid 9726's current affinity mask: f
        String[] split = Pattern.SPACES_PATTERN.split(tasksetOutput);
        if (split.length == 0) {
            return 0;
        }
        try {
            return new BigInteger(split[split.length - 1], 16).longValue();
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Parses the hexadecimal {@code Cpus_allowed} field from a process status file.
     *
     * @param cpusAllowed the comma-separated hexadecimal mask
     * @return the low 64 bits of the mask, or zero when unparseable
     */
    static long parseCpusAllowed(String cpusAllowed) {
        String hex = cpusAllowed.replace(Symbol.COMMA, Normal.EMPTY).trim();
        if (hex.isEmpty()) {
            return 0;
        }
        try {
            return new BigInteger(hex, Normal._16).longValue();
        } catch (NumberFormatException e) {
            return 0;
        }
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
        return commandLine.get();
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
        return arguments.get();
    }

    /**
     * Queries the command line.
     *
     * @return the query command line result
     */
    private String queryCommandLine() {
        return Arrays.stream(
                Builder.getStringFromFile(String.format(Locale.ROOT, ProcPath.PID_CMDLINE, getProcessID())).split("\0"))
                .collect(Collectors.joining(Symbol.SPACE));
    }

    /**
     * Makes a best effort attempt to obtain the environment variables of the process. May require elevated permissions
     * or same-user ownership.
     *
     * @return a map of environment variables for the process
     */
    @Override
    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables.get();
    }

    /**
     * Queries the arguments.
     *
     * @return the query arguments result
     */
    private List<String> queryArguments() {
        return Collections.unmodifiableList(
                Parsing.parseByteArrayToStrings(
                        Builder.readAllBytes(
                                String.format(Locale.ROOT, ProcPath.PID_CMDLINE, getProcessID()),
                                LOG_PROCFS_WARNING)));
    }

    /**
     * Makes a best effort attempt to obtain the current working directory for the process.
     *
     * @return the current working directory of the process
     */
    @Override
    public String getCurrentWorkingDirectory() {
        try {
            String cwdLink = String.format(Locale.ROOT, ProcPath.PID_CWD, getProcessID());
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
     * Gets the user name of the process owner.
     *
     * @return the user name who owns the process
     */
    @Override
    public String getUser() {
        return user.get();
    }

    /**
     * Queries the environment variables.
     *
     * @return the query environment variables result
     */
    private Map<String, String> queryEnvironmentVariables() {
        return Collections.unmodifiableMap(
                Parsing.parseByteArrayToStringMap(
                        Builder.readAllBytes(
                                String.format(Locale.ROOT, ProcPath.PID_ENVIRON, getProcessID()),
                                LOG_PROCFS_WARNING)));
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
        return group.get();
    }

    /**
     * Queries the user.
     *
     * @return the query user result
     */
    private String queryUser() {
        return IdGroup.getUser(userID);
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
     * Gets the process state.
     *
     * @return the state of the process
     */
    @Override
    public State getState() {
        return this.state;
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
     * Retrieves the threads of the process and their details.
     * <p>
     * The amount of returned information is operating-system dependent and may incur some latency.
     *
     * @return the list of thread details for the process
     */
    @Override
    public List<OSThread> getThreadDetails() {
        return ProcessStat.getThreadIds(getProcessID()).stream().parallel()
                .map(id -> new LinuxOSThread(getProcessID(), id)).filter(OSThread.ThreadFiltering.VALID_THREAD)
                .collect(Collectors.toList());
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
        return this.rusagePopulated ? this.cachedVoluntaryContextSwitches : this.voluntaryContextSwitches;
    }

    /**
     * The number of involuntary context switches the process has made. An involuntary context switch occurs when the
     * scheduler preempts the process.
     *
     * @return the number of involuntary context switches
     */
    @Override
    public long getInvoluntaryContextSwitches() {
        return this.rusagePopulated ? this.cachedInvoluntaryContextSwitches : this.involuntaryContextSwitches;
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
        return ProcessStat.getFileDescriptorFiles(getProcessID()).length;
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
            if (0 == LinuxLibc.INSTANCE.getrlimit(LinuxLibc.RLIMIT_NOFILE, rlimit)) {
                return rlimit.rlim_cur;
            }
            return getProcessOpenFileLimit(getProcessID(), 1);
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
            if (0 == LinuxLibc.INSTANCE.getrlimit(LinuxLibc.RLIMIT_NOFILE, rlimit)) {
                return rlimit.rlim_max;
            }
            return getProcessOpenFileLimit(getProcessID(), 2);
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
        // get 5th byte of file for 64-bit check
        // https://en.wikipedia.org/wiki/Executable_and_Linkable_Format#File_header
        byte[] buffer = new byte[5];
        if (!path.isEmpty()) {
            try (InputStream is = new FileInputStream(path)) {
                if (is.read(buffer) == buffer.length) {
                    return buffer[4] == 1 ? 32 : 64;
                }
            } catch (IOException e) {
                Logger.warn(false, "Health", "Failed to read process file: {}", path);
            }
        }
        return 0;
    }

    /**
     * Queries the group.
     *
     * @return the query group result
     */
    private String queryGroup() {
        return IdGroup.getGroupName(groupID);
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
        String cpusAllowed = Builder
                .getKeyValueMapFromFile(String.format(Locale.ROOT, ProcPath.PID_STATUS, getProcessID()), Symbol.COLON)
                .get("Cpus_allowed");
        if (cpusAllowed != null) {
            return parseCpusAllowed(cpusAllowed);
        }
        return parseAffinityMask(Executor.getFirstAnswer("taskset -p " + getProcessID()));
    }

    /**
     * Attempts to update process attributes. Returns false if the update fails, which will occur if the process no
     * longer exists.
     *
     * @return {@code true} if the process attributes were successfully updated, {@code false} otherwise
     */
    @Override
    public synchronized boolean updateAttributes() {
        boolean result = updateAttributesFromProc();
        if (result && getProcessID() == this.os.getProcessId()) {
            long[] contextSwitches = queryContextSwitches();
            if (contextSwitches != null) {
                this.cachedVoluntaryContextSwitches = contextSwitches[0];
                this.cachedInvoluntaryContextSwitches = contextSwitches[1];
                this.rusagePopulated = true;
            } else {
                this.rusagePopulated = false;
            }
        }
        return result;
    }

    /**
     * Queries the current process native context-switch counts.
     *
     * @return the voluntary and involuntary context-switch counts, or {@code null} if unavailable
     */
    protected long[] queryContextSwitches() {
        LinuxLibc.Rusage rusage = new LinuxLibc.Rusage();
        if (0 == LinuxLibc.INSTANCE.getrusage(LinuxLibc.RUSAGE_SELF, rusage)) {
            return new long[] { rusage.ru_nvcsw.longValue(), rusage.ru_nivcsw.longValue() };
        }
        return null;
    }

    /**
     * Updates process attributes from the proc filesystem.
     *
     * @return {@code true} if the proc attributes were updated
     */
    private boolean updateAttributesFromProc() {
        String procPidExe = String.format(Locale.ROOT, ProcPath.PID_EXE, getProcessID());
        try {
            Path link = Paths.get(procPidExe);
            String exePath = Files.readSymbolicLink(link).toString();
            // For some services the symbolic link process has terminated
            int index = exePath.indexOf(" (deleted)");
            if (index != -1) {
                exePath = exePath.substring(0, index);
            }
            this.path = exePath;
        } catch (InvalidPathException | IOException | UnsupportedOperationException | SecurityException e) {
            Logger.debug(false, "Health", "Unable to open symbolic link {}", procPidExe);
        }
        // Fetch all the values here
        // check for terminated process race condition after last one.
        Map<String, String> io = Builder
                .getKeyValueMapFromFile(String.format(Locale.ROOT, ProcPath.PID_IO, getProcessID()), Symbol.COLON);
        Map<String, String> status = Builder
                .getKeyValueMapFromFile(String.format(Locale.ROOT, ProcPath.PID_STATUS, getProcessID()), Symbol.COLON);
        String stat = Builder.getStringFromFile(String.format(Locale.ROOT, ProcPath.PID_STAT, getProcessID()));
        String statm = Builder.getStringFromFile(String.format(Locale.ROOT, ProcPath.PID_STATM, getProcessID()));
        if (stat.isEmpty()) {
            this.state = State.INVALID;
            return false;
        }
        // If some details couldn't be read from ProcPath.PID_STATUS try reading it from
        // ProcPath.PID_STAT
        getMissingDetails(status, stat);

        long now = System.currentTimeMillis();

        // We can get name and status more easily from /proc/pid/status which we
        // call later, so just get the numeric bits here
        // See man proc for how to parse /proc/[pid]/stat
        long[] statArray = Parsing
                .parseStringToLongArray(stat, PROC_PID_STAT_ORDERS, ProcessStat.PROC_PID_STAT_LENGTH, Symbol.C_SPACE);

        // BOOTTIME is in seconds and start time from proc/pid/stat is in jiffies.
        // Combine units to jiffies and convert to millijiffies before hz division to
        // avoid precision loss without having to cast
        this.startTime = (LinuxOperatingSystem.BOOTTIME * LinuxOperatingSystem.getHz()
                + statArray[ProcPidStat.START_TIME.ordinal()]) * 1000L / LinuxOperatingSystem.getHz();
        // BOOT_TIME could be up to 500ms off and start time up to 5ms off. A process
        // that has started within last 505ms could produce a future start time/negative
        // up time, so insert a sanity check.
        if (startTime >= now) {
            startTime = now - 1;
        }
        this.parentProcessID = (int) statArray[ProcPidStat.PPID.ordinal()];
        this.threadCount = (int) statArray[ProcPidStat.THREAD_COUNT.ordinal()];
        this.priority = (int) statArray[ProcPidStat.PRIORITY.ordinal()];
        // Parse /proc/[pid]/statm for resident and shared pages (all in pages)
        // Fields: size resident shared text lib data dt
        String[] statmFields = Regex.SPACES.split(statm);
        if (statmFields.length > 2) {
            long resident = Parsing.parseLongOrDefault(statmFields[1], 0L);
            long shared = Parsing.parseLongOrDefault(statmFields[2], 0L);
            long pageSize = LinuxOperatingSystem.getPageSize();
            this.residentSetSize = resident * pageSize;
            this.privateResidentMemory = (resident - shared) * pageSize;
        } else {
            this.residentSetSize = statArray[ProcPidStat.RSS.ordinal()] * LinuxOperatingSystem.getPageSize();
            this.privateResidentMemory = this.residentSetSize;
        }
        this.kernelTime = statArray[ProcPidStat.KERNEL_TIME.ordinal()] * 1000L / LinuxOperatingSystem.getHz();
        this.userTime = statArray[ProcPidStat.USER_TIME.ordinal()] * 1000L / LinuxOperatingSystem.getHz();
        this.minorFaults = statArray[ProcPidStat.MINOR_FAULTS.ordinal()];
        this.majorFaults = statArray[ProcPidStat.MAJOR_FAULTS.ordinal()];
        this.voluntaryContextSwitches = Parsing.parseLongOrDefault(status.get("voluntary_ctxt_switches"), 0L);
        this.involuntaryContextSwitches = Parsing.parseLongOrDefault(status.get("nonvoluntary_ctxt_switches"), 0L);
        this.contextSwitches = this.voluntaryContextSwitches + this.involuntaryContextSwitches;

        this.upTime = now - startTime;

        // See man proc for how to parse /proc/[pid]/io
        this.bytesRead = Parsing.parseLongOrDefault(io.getOrDefault("read_bytes", Normal.EMPTY), 0L);
        this.bytesWritten = Parsing.parseLongOrDefault(io.getOrDefault("write_bytes", Normal.EMPTY), 0L);

        // Don't set open files or bitness or currentWorkingDirectory; fetch on demand.

        this.userID = Pattern.SPACES_PATTERN.split(status.getOrDefault("Uid", Normal.EMPTY))[0];
        // defer user lookup until asked
        this.groupID = Pattern.SPACES_PATTERN.split(status.getOrDefault("Gid", Normal.EMPTY))[0];
        // defer group lookup until asked
        this.name = status.getOrDefault("Name", Normal.EMPTY);
        this.state = ProcessStat.getState(status.getOrDefault("State", "U").charAt(0));
        return true;
    }

    /**
     * Returns the process open file limit.
     *
     * @param processId the process id
     * @param index     the index
     * @return the get process open file limit result
     */
    private long getProcessOpenFileLimit(long processId, int index) {
        return ProcLimits.queryOpenFileLimit(processId, index);
    }

    /**
     * Enum used to update attributes. The order field represents the 1-indexed numeric order of the stat in
     * /proc/pid/stat per the man file.
     *
     * @author Kimi Liu
     */
    private enum ProcPidStat {

        /**
         * Parent process identifier field.
         * <p>
         * The parsing implementation in Parsing requires these to be declared in increasing order.
         */
        PPID(4),
        /**
         * Minor page-fault count field.
         */
        MINOR_FAULTS(10),
        /**
         * Major page-fault count field.
         */
        MAJOR_FAULTS(12),
        /**
         * User-mode CPU time field.
         */
        USER_TIME(14),
        /**
         * Kernel-mode CPU time field.
         */
        KERNEL_TIME(15),
        /**
         * Scheduling priority field.
         */
        PRIORITY(18),
        /**
         * Thread count field.
         */
        THREAD_COUNT(20),
        /**
         * Process start time field.
         */
        START_TIME(22),
        /**
         * Virtual memory size field.
         */
        VSZ(23),
        /**
         * Resident set size field.
         */
        RSS(24);

        /**
         * The order value.
         */
        private final int order;

        /**
         * Creates a new ProcPidStat instance.
         *
         * @param order the order
         */
        ProcPidStat(int order) {
            this.order = order;
        }

        /**
         * Returns the order.
         *
         * @return the get order result
         */
        public int getOrder() {
            return this.order;
        }

    }

}
