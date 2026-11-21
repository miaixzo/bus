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
package org.miaixz.bus.health.unix.freebsd.software;

import java.util.*;
import java.util.stream.Collectors;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.platform.unix.LibCAPI.size_t;
import com.sun.jna.platform.unix.Resource;

import org.miaixz.bus.core.center.function.PredicateX;
import org.miaixz.bus.core.center.function.SupplierX;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.jna.ByRef;
import org.miaixz.bus.health.builtin.software.OSThread;
import org.miaixz.bus.health.builtin.software.common.AbstractOSProcess;
import org.miaixz.bus.health.unix.freebsd.BsdSysctlKit;
import org.miaixz.bus.health.unix.freebsd.ProcstatKit;
import org.miaixz.bus.health.unix.shared.jna.FreeBsdLibc;
import org.miaixz.bus.logger.Logger;

/**
 * OSProcess implementation
 *
 * @author Kimi Liu
 */
@ThreadSafe
public class FreeBsdOSProcess extends AbstractOSProcess {

    /**
     * The PS_THREAD_COLUMNS constant.
     */
    static final String PS_THREAD_COLUMNS = Arrays.stream(PsThreadColumns.values()).map(Enum::name)
            .map(name -> StringKit.toLowerCase(name)).collect(Collectors.joining(Symbol.COMMA));

    /**
     * The ARGMAX constant.
     */
    private static final int ARGMAX = BsdSysctlKit.sysctl("kern.argmax", 0);

    /**
     * The os value.
     */
    private final FreeBsdOperatingSystem os;

    /**
     * The bitness value.
     */
    private final SupplierX<Integer> bitness = Memoizer.memoize(this::queryBitness);

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
     * The commandLine value.
     */
    private final SupplierX<String> commandLine = Memoizer.memoize(this::queryCommandLine);

    /**
     * The path value.
     */
    private volatile String path = Normal.EMPTY;

    /**
     * The name value.
     */
    private volatile String name;

    /**
     * The state value.
     */
    private volatile State state = State.INVALID;

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
     * The commandLineBackup value.
     */
    private volatile String commandLineBackup;

    /**
     * Creates a new FreeBsdOSProcess instance.
     *
     * @param pid   the pid
     * @param psMap the ps map
     * @param os    the os
     */
    public FreeBsdOSProcess(int pid, Map<FreeBsdOperatingSystem.PsKeywords, String> psMap, FreeBsdOperatingSystem os) {
        super(pid);
        this.os = os;
        updateAttributes(psMap);
    }

    /**
     * Clamps a freshly parsed CPU-time counter so it never falls below the value already reported.
     *
     * @param previous The value last reported.
     * @param parsed   The value just parsed from {@code ps}.
     * @return {@code parsed}, or {@code previous} if that would be a decrease.
     */
    static long monotonic(long previous, long parsed) {
        return Math.max(previous, parsed);
    }

    /**
     * Queries the arguments.
     *
     * @return the query arguments result
     */
    private List<String> queryArguments() {
        if (ARGMAX > 0) {
            // Get arguments via sysctl(3)
            int[] mib = new int[4];
            mib[0] = 1; // CTL_KERN
            mib[1] = 14; // KERN_PROC
            mib[2] = 7; // KERN_PROC_ARGS
            mib[3] = getProcessID();
            // Allocate memory for arguments
            try (Memory m = new Memory(ARGMAX);
                    ByRef.CloseableSizeTByReference size = new ByRef.CloseableSizeTByReference(ARGMAX)) {
                // Fetch arguments
                if (FreeBsdLibc.INSTANCE.sysctl(mib, mib.length, m, size, null, size_t.ZERO) == 0) {
                    return Collections.unmodifiableList(
                            Parsing.parseByteArrayToStrings(m.getByteArray(0, size.getValue().intValue())));
                } else {
                    Logger.warn(
                            false,
                            "Health",
                            "Failed sysctl call for process arguments (kern.proc.args), process {} may not exist. Error code: {}",
                            getProcessID(),
                            Native.getLastError());
                }
            }
        }
        return Collections.emptyList();
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
        return arguments.get();
    }

    /**
     * Queries the environment variables.
     *
     * @return the query environment variables result
     */
    private Map<String, String> queryEnvironmentVariables() {
        if (ARGMAX > 0) {
            // Get environment variables via sysctl(3)
            int[] mib = new int[4];
            mib[0] = 1; // CTL_KERN
            mib[1] = 14; // KERN_PROC
            mib[2] = 35; // KERN_PROC_ENV
            mib[3] = getProcessID();
            // Allocate memory for environment variables
            try (Memory m = new Memory(ARGMAX);
                    ByRef.CloseableSizeTByReference size = new ByRef.CloseableSizeTByReference(ARGMAX)) {
                // Fetch environment variables
                if (FreeBsdLibc.INSTANCE.sysctl(mib, mib.length, m, size, null, size_t.ZERO) == 0) {
                    return Collections.unmodifiableMap(
                            Parsing.parseByteArrayToStringMap(m.getByteArray(0, size.getValue().intValue())));
                } else {
                    Logger.warn(
                            false,
                            "Health",
                            "Failed sysctl call for process environment variables (kern.proc.env), process {} may not exist. Error code: {}",
                            getProcessID(),
                            Native.getLastError());
                }
            }
        }
        return Collections.emptyMap();
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
     * Makes a best effort attempt to obtain the current working directory for the process.
     *
     * @return the current working directory of the process
     */
    @Override
    public String getCurrentWorkingDirectory() {
        return ProcstatKit.getCwd(getProcessID());
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
     * Gets the number of open file handles (or network connections) that belongs to the process.
     * <p>
     * On FreeBSD and Solaris, this value is only populated if information for a single process id is requested.
     *
     * @return the number of open file descriptors
     */
    @Override
    public long getOpenFiles() {
        return ProcstatKit.getOpenFiles(getProcessID());
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
        // Would prefer to use native cpuset_getaffinity call but variable sizing is
        // kernel-dependent and requires C macros, so we use commandline instead.
        String cpuset = Executor.getFirstAnswer("cpuset -gp " + getProcessID());
        // Sample output:
        // pid 8 mask: 0, 1
        // cpuset: getaffinity: No such process
        String[] split = cpuset.split(Symbol.COLON);
        if (split.length > 1) {
            String[] bits = split[1].split(Symbol.COMMA);
            for (String bit : bits) {
                int bitToSet = Parsing.parseIntOrDefault(bit.trim(), -1);
                if (bitToSet >= 0) {
                    bitMask |= 1L << bitToSet;
                }
            }
        }
        return bitMask;
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
            FreeBsdLibc.INSTANCE.getrlimit(Normal._8, rlimit);
            return rlimit.rlim_cur;
        } else {
            return queryOtherRlimitNofile(true);
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
            FreeBsdLibc.INSTANCE.getrlimit(Normal._8, rlimit);
            return rlimit.rlim_max;
        } else {
            return queryOtherRlimitNofile(false);
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
     * Retrieves the threads of the process and their details.
     * <p>
     * The amount of returned information is operating-system dependent and may incur some latency.
     *
     * @return the list of thread details for the process
     */
    @Override
    public List<OSThread> getThreadDetails() {
        String psCommand = "ps -awwxo " + PS_THREAD_COLUMNS + " -H";
        if (getProcessID() >= 0) {
            psCommand += " -p " + getProcessID();
        }
        PredicateX<Map<PsThreadColumns, String>> hasColumnsPri = threadMap -> threadMap
                .containsKey(PsThreadColumns.PRI);
        return Executor.runNative(psCommand).stream().skip(1).parallel()
                .map(thread -> Parsing.stringToEnumMap(PsThreadColumns.class, thread.trim(), Symbol.C_SPACE))
                .filter(hasColumnsPri).map(threadMap -> new FreeBsdOSThread(getProcessID(), threadMap))
                .filter(OSThread.ThreadFiltering.VALID_THREAD).collect(Collectors.toList());
    }

    /**
     * Queries the bitness.
     *
     * @return the query bitness result
     */
    private int queryBitness() {
        // Get process abi vector
        int[] mib = new int[4];
        mib[0] = 1; // CTL_KERN
        mib[1] = 14; // KERN_PROC
        mib[2] = 9; // KERN_PROC_SV_NAME
        mib[3] = getProcessID();
        // Allocate memory for arguments
        try (Memory abi = new Memory(32);
                ByRef.CloseableSizeTByReference size = new ByRef.CloseableSizeTByReference(32)) {
            // Fetch abi vector
            if (0 == FreeBsdLibc.INSTANCE.sysctl(mib, mib.length, abi, size, null, size_t.ZERO)) {
                String elf = abi.getString(0);
                if (elf.contains("ELF32")) {
                    return 32;
                } else if (elf.contains("ELF64")) {
                    return 64;
                }
            }
        }
        return 0;
    }

    /**
     * Attempts to update process attributes. Returns false if the update fails, which will occur if the process no
     * longer exists.
     *
     * @return {@code true} if the process attributes were successfully updated, {@code false} otherwise
     */
    @Override
    public boolean updateAttributes() {
        String psCommand = "ps -awwxo " + FreeBsdOperatingSystem.PS_COMMAND_ARGS + " -p " + getProcessID();
        List<String> procList = Executor.runNative(psCommand);
        if (procList.size() > 1) {
            // skip header row
            Map<FreeBsdOperatingSystem.PsKeywords, String> psMap = Parsing
                    .stringToEnumMap(FreeBsdOperatingSystem.PsKeywords.class, procList.get(1).trim(), Symbol.C_SPACE);
            // Check if last (thus all) value populated
            if (psMap.containsKey(FreeBsdOperatingSystem.PsKeywords.ARGS)) {
                return updateAttributes(psMap);
            }
        }
        this.state = State.INVALID;
        return false;
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
     * Updates the attributes.
     *
     * @param psMap the ps map
     * @return the update attributes result
     */
    private synchronized boolean updateAttributes(Map<FreeBsdOperatingSystem.PsKeywords, String> psMap) {
        long now = System.currentTimeMillis();
        switch (psMap.get(FreeBsdOperatingSystem.PsKeywords.STATE).charAt(0)) {
            case 'R':
                this.state = State.RUNNING;
                break;

            case 'I':
            case 'S':
                this.state = State.SLEEPING;
                break;

            case 'D':
            case Symbol.C_L:
            case 'U':
                this.state = State.WAITING;
                break;

            case 'Z':
                this.state = State.ZOMBIE;
                break;

            case 'T':
                this.state = State.STOPPED;
                break;

            default:
                this.state = State.OTHER;
                break;
        }
        this.parentProcessID = Parsing.parseIntOrDefault(psMap.get(FreeBsdOperatingSystem.PsKeywords.PPID), 0);
        this.user = psMap.get(FreeBsdOperatingSystem.PsKeywords.USER);
        this.userID = psMap.get(FreeBsdOperatingSystem.PsKeywords.UID);
        this.group = psMap.get(FreeBsdOperatingSystem.PsKeywords.GROUP);
        this.groupID = psMap.get(FreeBsdOperatingSystem.PsKeywords.GID);
        this.threadCount = Parsing.parseIntOrDefault(psMap.get(FreeBsdOperatingSystem.PsKeywords.NLWP), 0);
        this.priority = Parsing.parseIntOrDefault(psMap.get(FreeBsdOperatingSystem.PsKeywords.PRI), 0);
        // These are in KB, multiply
        this.virtualSize = Parsing.parseLongOrDefault(psMap.get(FreeBsdOperatingSystem.PsKeywords.VSZ), 0) * 1024;
        this.residentSetSize = Parsing.parseLongOrDefault(psMap.get(FreeBsdOperatingSystem.PsKeywords.RSS), 0) * 1024;
        // Avoid divide by zero for processes up less than a second
        long elapsedTime = Parsing.parseDHMSOrDefault(psMap.get(FreeBsdOperatingSystem.PsKeywords.ETIMES), 0L);
        this.upTime = elapsedTime < 1L ? 1L : elapsedTime;
        this.startTime = now - this.upTime;
        long sysTime = Parsing.parseDHMSOrDefault(psMap.get(FreeBsdOperatingSystem.PsKeywords.SYSTIME), 0L);
        this.kernelTime = monotonic(this.kernelTime, sysTime);
        this.userTime = monotonic(
                this.userTime,
                Parsing.parseDHMSOrDefault(psMap.get(FreeBsdOperatingSystem.PsKeywords.TIME), 0L) - sysTime);
        this.path = queryExecutablePath();
        if (this.path.isEmpty()) {
            this.path = psMap.get(FreeBsdOperatingSystem.PsKeywords.COMM);
        }
        this.name = this.path.substring(this.path.lastIndexOf(Symbol.C_SLASH) + 1);
        this.minorFaults = Parsing.parseLongOrDefault(psMap.get(FreeBsdOperatingSystem.PsKeywords.MINFLT), 0L);
        this.majorFaults = Parsing.parseLongOrDefault(psMap.get(FreeBsdOperatingSystem.PsKeywords.MAJFLT), 0L);
        this.voluntaryContextSwitches = Parsing
                .parseLongOrDefault(psMap.get(FreeBsdOperatingSystem.PsKeywords.NVCSW), 0L);
        this.involuntaryContextSwitches = Parsing
                .parseLongOrDefault(psMap.get(FreeBsdOperatingSystem.PsKeywords.NIVCSW), 0L);
        this.contextSwitches = this.voluntaryContextSwitches + this.involuntaryContextSwitches;
        this.commandLineBackup = psMap.get(FreeBsdOperatingSystem.PsKeywords.ARGS);
        return true;
    }

    /**
     * Reads another process's open-file limit through {@code kern.proc.rlimit}.
     *
     * @param soft {@code true} for the soft limit or {@code false} for the hard limit
     * @return the limit, or {@code -1} if unavailable
     */
    private long queryOtherRlimitNofile(boolean soft) {
        int[] mib = { Normal._1, Normal._14, Normal._37, getProcessID(), Normal._8 };
        try (Memory limit = new Memory(Normal._16);
                ByRef.CloseableSizeTByReference size = new ByRef.CloseableSizeTByReference(Normal._16)) {
            if (FreeBsdLibc.INSTANCE.sysctl(mib, mib.length, limit, size, null, size_t.ZERO) == Normal._0) {
                return limit.getLong(soft ? Normal._0 : Normal._8);
            }
        }
        return -1L;
    }

    /**
     * Reads the executable path from the kernel through {@code kern.proc.pathname}.
     *
     * @return the executable path, or an empty string
     */
    private String queryExecutablePath() {
        int[] mib = { Normal._1, Normal._14, Normal._12, getProcessID() };
        try (Memory buffer = new Memory(Normal._1024);
                ByRef.CloseableSizeTByReference size = new ByRef.CloseableSizeTByReference(Normal._1024)) {
            if (FreeBsdLibc.INSTANCE.sysctl(mib, mib.length, buffer, size, null, size_t.ZERO) == Normal._0) {
                return buffer.getString(Normal._0);
            }
        }
        return Normal.EMPTY;
    }

    /**
     * The PsThreadColumns enum.
     *
     * @author Kimi Liu
     */
    enum PsThreadColumns {
        /**
         * Thread name.
         */
        TDNAME,
        /**
         * Lightweight process identifier.
         */
        LWP,
        /**
         * Thread state.
         */
        STATE,
        /**
         * Elapsed time in seconds.
         */
        ETIMES,
        /**
         * Accumulated system CPU time.
         */
        SYSTIME,
        /**
         * Accumulated total CPU time.
         */
        TIME,
        /**
         * Kernel thread address.
         */
        TDADDR,
        /**
         * Involuntary context-switch count.
         */
        NIVCSW,
        /**
         * Voluntary context-switch count.
         */
        NVCSW,
        /**
         * Major page-fault count.
         */
        MAJFLT,
        /**
         * Minor page-fault count.
         */
        MINFLT,
        /**
         * Scheduling priority.
         */
        PRI

    }

}
