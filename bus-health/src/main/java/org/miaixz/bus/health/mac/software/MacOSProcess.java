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
package org.miaixz.bus.health.mac.software;

import java.util.*;
import java.util.stream.Collectors;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.platform.mac.IOKit.IOIterator;
import com.sun.jna.platform.mac.IOKit.IORegistryEntry;
import com.sun.jna.platform.mac.IOKitUtil;
import com.sun.jna.platform.mac.SystemB.Group;
import com.sun.jna.platform.mac.SystemB.Passwd;
import com.sun.jna.platform.unix.LibCAPI.size_t;
import com.sun.jna.platform.unix.Resource;

import org.miaixz.bus.core.center.function.SupplierX;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.jna.Struct;
import org.miaixz.bus.health.builtin.software.OSThread;
import org.miaixz.bus.health.builtin.software.common.AbstractOSProcess;
import org.miaixz.bus.health.mac.SysctlKit;
import org.miaixz.bus.health.mac.driver.ThreadInfo;
import org.miaixz.bus.health.mac.jna.SystemB;
import org.miaixz.bus.logger.Logger;

/**
 * OSProcess implementation
 *
 * @author Kimi Liu
 */
@ThreadSafe
public class MacOSProcess extends AbstractOSProcess {

    /**
     * The ARGMAX constant.
     */
    private static final int ARGMAX = SysctlKit.sysctl("kern.argmax", 0);

    /**
     * The TICKS_PER_MS constant.
     */
    private static final long TICKS_PER_MS;

    /**
     * The LOG_MAC_SYSCTL_WARNING constant.
     */
    private static final boolean LOG_MAC_SYSCTL_WARNING = Builder.get(Builder._MAC_SYSCTL_LOGWARNING, false);

    /**
     * The MAC_RLIMIT_NOFILE constant.
     */
    private static final int MAC_RLIMIT_NOFILE = 8;

    /**
     * 64-bit flag.
     */
    private static final int P_LP64 = 0x4;

    /**
     * Sleeping on high priority.
     */
    private static final int SSLEEP = 1;
    /**
     * Sleeping on low priority.
     */
    private static final int SWAIT = 2;
    /**
     * Running.
     */
    private static final int SRUN = 3;
    /**
     * Intermediate state in process creation.
     */
    private static final int SIDL = 4;
    /**
     * Intermediate state in process termination.
     */
    private static final int SZOMB = 5;
    /**
     * Process being traced.
     */
    private static final int SSTOP = 6;

    static {
        // default to 1 tick per nanosecond
        long ticksPerSec = 1_000_000_000L;
        IOIterator iter = IOKitUtil.getMatchingServices("IOPlatformDevice");
        if (iter != null) {
            IORegistryEntry cpu = iter.next();
            while (cpu != null) {
                try {
                    String s = StringKit.toLowerCase(cpu.getName());
                    if (s.startsWith("cpu") && s.length() > 3) {
                        byte[] data = cpu.getByteArrayProperty("timebase-frequency");
                        if (data != null) {
                            ticksPerSec = Parsing.byteArrayToLong(data, 4, false);
                            break;
                        }
                    }
                } finally {
                    cpu.release();
                }
                cpu = iter.next();
            }
            iter.release();
        }
        // Convert to ticks per millisecond
        TICKS_PER_MS = ticksPerSec / 1000L;
    }

    /**
     * The os value.
     */
    private final MacOperatingSystem os;

    /**
     * The argsEnviron value.
     */
    private final SupplierX<Pair<List<String>, Map<String, String>>> argsEnviron = Memoizer
            .memoize(this::queryArgsAndEnvironment);

    /**
     * The commandLine value.
     */
    private final SupplierX<String> commandLine = Memoizer.memoize(this::queryCommandLine);

    /**
     * The majorVersion value.
     */
    private int majorVersion;

    /**
     * The minorVersion value.
     */
    private int minorVersion;

    /**
     * The name value.
     */
    private volatile String name = Normal.EMPTY;

    /**
     * The path value.
     */
    private volatile String path = Normal.EMPTY;

    /**
     * The currentWorkingDirectory value.
     */
    private volatile String currentWorkingDirectory;

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
     * The memoryFootprint value.
     */
    private volatile long memoryFootprint;

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
     * The openFiles value.
     */
    private volatile long openFiles;

    /**
     * The bitness value.
     */
    private volatile int bitness;

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
     * Creates a new MacOSProcess instance.
     *
     * @param pid   the pid
     * @param major the major
     * @param minor the minor
     * @param os    the os
     */
    public MacOSProcess(int pid, int major, int minor, MacOperatingSystem os) {
        super(pid);
        this.majorVersion = major;
        this.minorVersion = minor;
        this.os = os;
        updateAttributes();
    }

    /**
     * Parses the {@code KERN_PROCARGS2} buffer into arguments and environment variables.
     *
     * @param procargs the raw sysctl buffer
     * @param size     the number of valid bytes in the buffer
     * @return a pair containing the argument list and environment map
     */
    static Pair<List<String>, Map<String, String>> parseProcArgs(byte[] procargs, int size) {
        List<String> args = new ArrayList<>();
        Map<String, String> env = new LinkedHashMap<>();
        int limit = Math.min(size, procargs.length);
        if (limit > Integer.BYTES) {
            int nargs = (int) Parsing.byteArrayToLong(procargs, Integer.BYTES, false);
            if (nargs > 0 && nargs <= limit - Integer.BYTES) {
                int offset = nextNull(procargs, Integer.BYTES, limit);
                while (offset < limit && procargs[offset] == 0) {
                    offset++;
                }
                for (int i = 0; i < nargs && offset < limit; i++) {
                    int end = nextNull(procargs, offset, limit);
                    if (end == limit) {
                        break;
                    }
                    args.add(decode(procargs, offset, end));
                    offset = end + 1;
                }
                while (offset < limit) {
                    while (offset < limit && procargs[offset] == 0) {
                        offset++;
                    }
                    if (offset >= limit) {
                        break;
                    }
                    int end = nextNull(procargs, offset, limit);
                    if (end == limit) {
                        break;
                    }
                    String entry = decode(procargs, offset, end);
                    int idx = entry.indexOf(Symbol.C_EQUAL);
                    if (idx > 0) {
                        env.put(entry.substring(0, idx), entry.substring(idx + 1));
                    }
                    offset = end + 1;
                }
            }
        }
        return Pair.of(Collections.unmodifiableList(args), Collections.unmodifiableMap(env));
    }

    /**
     * Decodes the byte range as UTF-8.
     *
     * @param buffer the source buffer
     * @param from   the inclusive start offset
     * @param to     the exclusive end offset
     * @return the decoded string
     */
    private static String decode(byte[] buffer, int from, int to) {
        return new String(buffer, from, to - from, Charset.UTF_8);
    }

    /**
     * Finds the next NUL byte.
     *
     * @param buffer the source buffer
     * @param from   the starting offset
     * @param limit  the exclusive upper bound
     * @return the NUL byte index, or {@code limit} if none is found
     */
    private static int nextNull(byte[] buffer, int from, int limit) {
        int index = from;
        while (index < limit && buffer[index] != 0) {
            index++;
        }
        return index;
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
        return String.join(Symbol.SPACE, getArguments());
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
        return argsEnviron.get().getLeft();
    }

    /**
     * Makes a best effort attempt to obtain the environment variables of the process. May require elevated permissions
     * or same-user ownership.
     *
     * @return a map of environment variables for the process
     */
    @Override
    public Map<String, String> getEnvironmentVariables() {
        return argsEnviron.get().getRight();
    }

    /**
     * Queries the args and environment.
     *
     * @return the query args and environment result
     */
    private Pair<List<String>, Map<String, String>> queryArgsAndEnvironment() {
        int pid = getProcessID();
        // Get command line via sysctl
        int[] mib = new int[3];
        mib[0] = 1; // CTL_KERN
        mib[1] = 49; // KERN_PROCARGS2
        mib[2] = pid;
        // Allocate memory for arguments
        try (Memory procargs = new Memory(ARGMAX)) {
            procargs.clear();
            size_t.ByReference size = new size_t.ByReference(ARGMAX);
            // Fetch arguments
            if (0 == SystemB.INSTANCE.sysctl(mib, mib.length, procargs, size, null, size_t.ZERO)) {
                int length = (int) size.longValue();
                return parseProcArgs(procargs.getByteArray(0, length), length);
            } else {
                // Don't warn for pid 0
                if (pid > 0 && LOG_MAC_SYSCTL_WARNING) {
                    Logger.warn(
                            false,
                            "Health",
                            "Failed sysctl call for process arguments (kern.procargs2), process {} may not exist. Error code: {}",
                            pid,
                            Native.getLastError());
                }
            }
        }
        return Pair.of(Collections.emptyList(), Collections.emptyMap());
    }

    /**
     * Makes a best effort attempt to obtain the current working directory for the process.
     *
     * @return the current working directory of the process
     */
    @Override
    public String getCurrentWorkingDirectory() {
        return this.currentWorkingDirectory;
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
     * Retrieves the threads of the process and their details.
     * <p>
     * The amount of returned information is operating-system dependent and may incur some latency.
     *
     * @return the list of thread details for the process
     */
    @Override
    public List<OSThread> getThreadDetails() {
        long now = System.currentTimeMillis();
        return ThreadInfo.queryTaskThreads(getProcessID()).stream().parallel().map(stat -> {
            // For long running threads the start time calculation can overestimate
            long start = Math.max(now - stat.getUpTime(), getStartTime());
            return new MacOSThread(getProcessID(), stat.getThreadId(), stat.getState(), stat.getSystemTime(),
                    stat.getUserTime(), start, now - start, stat.getPriority());
        }).collect(Collectors.toList());
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
        return this.memoryFootprint;
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
     * Gets the number of open file handles (or network connections) that belongs to the process.
     * <p>
     * On FreeBSD and Solaris, this value is only populated if information for a single process id is requested.
     *
     * @return the number of open file descriptors
     */
    @Override
    public long getOpenFiles() {
        return this.openFiles;
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
            SystemB.INSTANCE.getrlimit(MAC_RLIMIT_NOFILE, rlimit);
            return rlimit.rlim_cur;
        } else {
            return -1L; // not supported
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
            SystemB.INSTANCE.getrlimit(MAC_RLIMIT_NOFILE, rlimit);
            return rlimit.rlim_max;
        } else {
            return -1L; // not supported
        }
    }

    /**
     * Attempts to get the bitness (32 or 64) of the process.
     *
     * @return the process bitness (32 or 64)
     */
    @Override
    public int getBitness() {
        return this.bitness;
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
        // macOS doesn't do affinity. Return a bitmask of the current processors.
        int logicalProcessorCount = SysctlKit.sysctl("hw.logicalcpu", 1);
        return logicalProcessorCount < 64 ? (1L << logicalProcessorCount) - 1 : -1L;
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
     * Attempts to update process attributes. Returns false if the update fails, which will occur if the process no
     * longer exists.
     *
     * @return {@code true} if the process attributes were successfully updated, {@code false} otherwise
     */
    @Override
    public synchronized boolean updateAttributes() {
        long now = System.currentTimeMillis();
        try (Struct.CloseableProcTaskAllInfo taskAllInfo = new Struct.CloseableProcTaskAllInfo()) {
            if (0 > SystemB.INSTANCE
                    .proc_pidinfo(getProcessID(), SystemB.PROC_PIDTASKALLINFO, 0, taskAllInfo, taskAllInfo.size())
                    || taskAllInfo.ptinfo.pti_threadnum < 1) {
                this.state = State.INVALID;
                return false;
            }
            try (Memory buf = new Memory(SystemB.PROC_PIDPATHINFO_MAXSIZE)) {
                if (0 < SystemB.INSTANCE.proc_pidpath(getProcessID(), buf, SystemB.PROC_PIDPATHINFO_MAXSIZE)) {
                    this.path = buf.getString(0).trim();
                    // Overwrite name with last part of path
                    String[] pathSplit = this.path.split(Symbol.SLASH);
                    if (pathSplit.length > 0) {
                        this.name = pathSplit[pathSplit.length - 1];
                    }
                }
            }
            if (this.name.isEmpty()) {
                // pbi_comm contains first 16 characters of name
                this.name = Native.toString(taskAllInfo.pbsd.pbi_comm, Charset.UTF_8);
            }

            switch (taskAllInfo.pbsd.pbi_status) {
                case SSLEEP:
                    this.state = State.SLEEPING;
                    break;

                case SWAIT:
                    this.state = State.WAITING;
                    break;

                case SRUN:
                    this.state = State.RUNNING;
                    break;

                case SIDL:
                    this.state = State.NEW;
                    break;

                case SZOMB:
                    this.state = State.ZOMBIE;
                    break;

                case SSTOP:
                    this.state = State.STOPPED;
                    break;

                default:
                    this.state = State.OTHER;
                    break;
            }
            this.parentProcessID = taskAllInfo.pbsd.pbi_ppid;
            this.userID = Integer.toString(taskAllInfo.pbsd.pbi_uid);
            Passwd pwuid = SystemB.INSTANCE.getpwuid(taskAllInfo.pbsd.pbi_uid);
            this.user = pwuid == null ? Integer.toString(taskAllInfo.pbsd.pbi_uid) : pwuid.pw_name;
            this.groupID = Integer.toString(taskAllInfo.pbsd.pbi_gid);
            Group grgid = SystemB.INSTANCE.getgrgid(taskAllInfo.pbsd.pbi_gid);
            this.group = grgid == null ? Integer.toString(taskAllInfo.pbsd.pbi_gid) : grgid.gr_name;
            this.threadCount = taskAllInfo.ptinfo.pti_threadnum;
            this.priority = taskAllInfo.ptinfo.pti_priority;
            this.virtualSize = taskAllInfo.ptinfo.pti_virtual_size;
            this.residentSetSize = taskAllInfo.ptinfo.pti_resident_size;
            // Default/fallback: RSS. Will be overwritten by phys_footprint when available.
            this.memoryFootprint = this.residentSetSize;
            this.kernelTime = taskAllInfo.ptinfo.pti_total_system / TICKS_PER_MS;
            this.userTime = taskAllInfo.ptinfo.pti_total_user / TICKS_PER_MS;
            this.startTime = taskAllInfo.pbsd.pbi_start_tvsec * 1000L + taskAllInfo.pbsd.pbi_start_tvusec / 1000L;
            this.upTime = now - this.startTime;
            this.openFiles = taskAllInfo.pbsd.pbi_nfiles;
            this.bitness = (taskAllInfo.pbsd.pbi_flags & P_LP64) == 0 ? 32 : 64;
            this.majorFaults = taskAllInfo.ptinfo.pti_pageins;
            // testing using getrusage confirms pti_faults includes both major and minor
            this.minorFaults = taskAllInfo.ptinfo.pti_faults - taskAllInfo.ptinfo.pti_pageins; // NOSONAR squid:S2184
            if (getProcessID() == this.os.getProcessId()) {
                SystemB.Rusage rusage = new SystemB.Rusage();
                if (0 == SystemB.INSTANCE.getrusage(SystemB.RUSAGE_SELF, rusage)) {
                    this.voluntaryContextSwitches = rusage.ru_nvcsw.longValue();
                    this.involuntaryContextSwitches = rusage.ru_nivcsw.longValue();
                    this.contextSwitches = this.voluntaryContextSwitches + this.involuntaryContextSwitches;
                } else {
                    this.contextSwitches = taskAllInfo.ptinfo.pti_csw;
                    this.voluntaryContextSwitches = 0L;
                    this.involuntaryContextSwitches = 0L;
                }
            } else {
                this.contextSwitches = taskAllInfo.ptinfo.pti_csw;
            }
        }
        if (this.majorVersion > 10 || (this.majorVersion == 10 && this.minorVersion >= 9)) {
            try (Struct.CloseableRUsageInfoV2 rUsageInfoV2 = new Struct.CloseableRUsageInfoV2()) {
                if (0 == SystemB.INSTANCE.proc_pid_rusage(getProcessID(), SystemB.RUSAGE_INFO_V2, rUsageInfoV2)) {
                    this.bytesRead = rUsageInfoV2.ri_diskio_bytesread;
                    this.bytesWritten = rUsageInfoV2.ri_diskio_byteswritten;
                    this.memoryFootprint = rUsageInfoV2.ri_phys_footprint;
                }
            }
        }
        try (Struct.CloseableVnodePathInfo vpi = new Struct.CloseableVnodePathInfo()) {
            if (0 < SystemB.INSTANCE.proc_pidinfo(getProcessID(), SystemB.PROC_PIDVNODEPATHINFO, 0, vpi, vpi.size())) {
                this.currentWorkingDirectory = Native.toString(vpi.pvi_cdir.vip_path, Charset.UTF_8);
            }
        }
        return true;
    }

}
