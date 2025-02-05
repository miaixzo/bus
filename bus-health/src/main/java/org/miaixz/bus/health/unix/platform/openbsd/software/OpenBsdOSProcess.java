/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org OSHI and other contributors.               ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.health.unix.platform.openbsd.software;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.jna.ByRef;
import org.miaixz.bus.health.builtin.software.OSProcess;
import org.miaixz.bus.health.builtin.software.OSThread;
import org.miaixz.bus.health.builtin.software.common.AbstractOSProcess;
import org.miaixz.bus.health.unix.jna.OpenBsdLibc;
import org.miaixz.bus.health.unix.platform.openbsd.FstatKit;
import org.miaixz.bus.health.unix.platform.openbsd.software.OpenBsdOperatingSystem.PsKeywords;
import org.miaixz.bus.logger.Logger;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.unix.LibCAPI.size_t;
import com.sun.jna.platform.unix.Resource;

/**
 * OSProcess implementation
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public class OpenBsdOSProcess extends AbstractOSProcess {

    static final String PS_THREAD_COLUMNS = Arrays.stream(PsThreadColumns.values()).map(Enum::name)
            .map(name -> name.toLowerCase(Locale.ROOT)).collect(Collectors.joining(Symbol.COMMA));
    private static final int ARGMAX;

    static {
        int[] mib = new int[2];
        mib[0] = 1; // CTL_KERN
        mib[1] = 8; // KERN_ARGMAX
        try (Memory m = new Memory(Integer.BYTES);
                ByRef.CloseableSizeTByReference size = new ByRef.CloseableSizeTByReference(Integer.BYTES)) {
            if (OpenBsdLibc.INSTANCE.sysctl(mib, mib.length, m, size, null, size_t.ZERO) == 0) {
                ARGMAX = m.getInt(0);
            } else {
                Logger.warn("Failed sysctl call for process arguments max size (kern.argmax). Error code: {}",
                        Native.getLastError());
                ARGMAX = 0;
            }
        }
    }

    private final Supplier<List<String>> arguments = Memoizer.memoize(this::queryArguments);
    private final OpenBsdOperatingSystem os;
    private final Supplier<Map<String, String>> environmentVariables = Memoizer
            .memoize(this::queryEnvironmentVariables);
    private final int bitness;
    private OSProcess.State state = OSProcess.State.INVALID;

    private String name;
    private String path = Normal.EMPTY;
    private String user;
    private String userID;
    private String group;
    private String groupID;
    private String commandLineBackup;
    private final Supplier<String> commandLine = Memoizer.memoize(this::queryCommandLine);
    private int parentProcessID;
    private int threadCount;
    private int priority;
    private long virtualSize;
    private long residentSetSize;
    private long kernelTime;
    private long userTime;
    private long startTime;
    private long upTime;
    private long bytesRead;
    private long bytesWritten;
    private long minorFaults;
    private long majorFaults;
    private long contextSwitches;

    public OpenBsdOSProcess(int pid, Map<PsKeywords, String> psMap, OpenBsdOperatingSystem os) {
        super(pid);
        this.os = os;
        // OpenBSD does not maintain a compatibility layer.
        // Process bitness is OS bitness
        this.bitness = Native.LONG_SIZE * 8;
        updateThreadCount();
        updateAttributes(psMap);
    }

    @Override
    public String getCurrentWorkingDirectory() {
        return FstatKit.getCwd(getProcessID());
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public String getCommandLine() {
        return this.commandLine.get();
    }

    private String queryCommandLine() {
        String cl = String.join(Symbol.SPACE, getArguments());
        return cl.isEmpty() ? this.commandLineBackup : cl;
    }

    @Override
    public List<String> getArguments() {
        return arguments.get();
    }

    private List<String> queryArguments() {
        if (ARGMAX > 0) {
            // Get arguments via sysctl(3)
            int[] mib = new int[4];
            mib[0] = 1; // CTL_KERN
            mib[1] = 55; // KERN_PROC_ARGS
            mib[2] = getProcessID();
            mib[3] = 1; // KERN_PROC_ARGV
            // Allocate memory for arguments
            try (Memory m = new Memory(ARGMAX);
                    ByRef.CloseableSizeTByReference size = new ByRef.CloseableSizeTByReference(ARGMAX)) {
                // Fetch arguments
                if (OpenBsdLibc.INSTANCE.sysctl(mib, mib.length, m, size, null, size_t.ZERO) == 0) {
                    // Returns a null-terminated list of pointers to the actual data
                    List<String> args = new ArrayList<>();
                    // To iterate the pointer-list
                    long offset = 0;
                    // Get the data base address to calculate offsets
                    long baseAddr = Pointer.nativeValue(m);
                    long maxAddr = baseAddr + size.getValue().longValue();
                    // Get the address of the data. If null (0) we're done iterating
                    long argAddr = Pointer.nativeValue(m.getPointer(offset));
                    while (argAddr > baseAddr && argAddr < maxAddr) {
                        args.add(m.getString(argAddr - baseAddr));
                        offset += Native.POINTER_SIZE;
                        argAddr = Pointer.nativeValue(m.getPointer(offset));
                    }
                    return Collections.unmodifiableList(args);
                }
            }
        }
        return Collections.emptyList();
    }

    @Override
    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables.get();
    }

    private Map<String, String> queryEnvironmentVariables() {
        // Get environment variables via sysctl(3)
        int[] mib = new int[4];
        mib[0] = 1; // CTL_KERN
        mib[1] = 55; // KERN_PROC_ARGS
        mib[2] = getProcessID();
        mib[3] = 3; // KERN_PROC_ENV
        // Allocate memory for environment variables
        try (Memory m = new Memory(ARGMAX);
                ByRef.CloseableSizeTByReference size = new ByRef.CloseableSizeTByReference(ARGMAX)) {
            // Fetch environment variables
            if (OpenBsdLibc.INSTANCE.sysctl(mib, mib.length, m, size, null, size_t.ZERO) == 0) {
                // Returns a null-terminated list of pointers to the actual data
                Map<String, String> env = new LinkedHashMap<>();
                // To iterate the pointer-list
                long offset = 0;
                // Get the data base address to calculate offsets
                long baseAddr = Pointer.nativeValue(m);
                long maxAddr = baseAddr + size.longValue();
                // Get the address of the data. If null (0) we're done iterating
                long argAddr = Pointer.nativeValue(m.getPointer(offset));
                while (argAddr > baseAddr && argAddr < maxAddr) {
                    String envStr = m.getString(argAddr - baseAddr);
                    int idx = envStr.indexOf(Symbol.C_EQUAL);
                    if (idx > 0) {
                        env.put(envStr.substring(0, idx), envStr.substring(idx + 1));
                    }
                    offset += Native.POINTER_SIZE;
                    argAddr = Pointer.nativeValue(m.getPointer(offset));
                }
                return Collections.unmodifiableMap(env);
            }
        }
        return Collections.emptyMap();
    }

    @Override
    public OSProcess.State getState() {
        return this.state;
    }

    @Override
    public String getUser() {
        return this.user;
    }

    @Override
    public String getUserID() {
        return this.userID;
    }

    @Override
    public String getGroup() {
        return this.group;
    }

    @Override
    public String getGroupID() {
        return this.groupID;
    }

    @Override
    public long getOpenFiles() {
        return FstatKit.getOpenFiles(getProcessID());
    }

    @Override
    public int getParentProcessID() {
        return this.parentProcessID;
    }

    @Override
    public int getThreadCount() {
        return this.threadCount;
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public long getVirtualSize() {
        return this.virtualSize;
    }

    @Override
    public long getResidentSetSize() {
        return this.residentSetSize;
    }

    @Override
    public long getKernelTime() {
        return this.kernelTime;
    }

    @Override
    public long getUserTime() {
        return this.userTime;
    }

    @Override
    public long getUpTime() {
        return this.upTime;
    }

    @Override
    public long getStartTime() {
        return this.startTime;
    }

    @Override
    public long getBytesRead() {
        return this.bytesRead;
    }

    @Override
    public long getBytesWritten() {
        return this.bytesWritten;
    }

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

    @Override
    public long getSoftOpenFileLimit() {
        if (getProcessID() == this.os.getProcessId()) {
            final Resource.Rlimit rlimit = new Resource.Rlimit();
            OpenBsdLibc.INSTANCE.getrlimit(OpenBsdLibc.RLIMIT_NOFILE, rlimit);
            return rlimit.rlim_cur;
        } else {
            return -1L; // not supported
        }
    }

    @Override
    public long getHardOpenFileLimit() {
        if (getProcessID() == this.os.getProcessId()) {
            final Resource.Rlimit rlimit = new Resource.Rlimit();
            OpenBsdLibc.INSTANCE.getrlimit(OpenBsdLibc.RLIMIT_NOFILE, rlimit);
            return rlimit.rlim_max;
        } else {
            return -1L; // not supported
        }
    }

    @Override
    public int getBitness() {
        return this.bitness;
    }

    @Override
    public List<OSThread> getThreadDetails() {
        String psCommand = "ps -aHwwxo " + PS_THREAD_COLUMNS;
        if (getProcessID() >= 0) {
            psCommand += " -p " + getProcessID();
        }
        Predicate<Map<PsThreadColumns, String>> hasColumnsArgs = threadMap -> threadMap
                .containsKey(PsThreadColumns.ARGS);
        return Executor.runNative(psCommand).stream().skip(1)
                .map(thread -> Parsing.stringToEnumMap(PsThreadColumns.class, thread.trim(), Symbol.C_SPACE))
                .filter(hasColumnsArgs).map(threadMap -> new OpenBsdOSThread(getProcessID(), threadMap))
                .filter(OSThread.ThreadFiltering.VALID_THREAD).collect(Collectors.toList());
    }

    @Override
    public boolean updateAttributes() {
        // 'ps' does not provide threadCount or kernelTime on OpenBSD
        String psCommand = "ps -awwxo " + OpenBsdOperatingSystem.PS_COMMAND_ARGS + " -p " + getProcessID();
        List<String> procList = Executor.runNative(psCommand);
        if (procList.size() > 1) {
            // skip header row
            Map<PsKeywords, String> psMap = Parsing.stringToEnumMap(PsKeywords.class, procList.get(1).trim(),
                    Symbol.C_SPACE);
            // Check if last (thus all) value populated
            if (psMap.containsKey(PsKeywords.ARGS)) {
                updateThreadCount();
                return updateAttributes(psMap);
            }
        }
        this.state = OSProcess.State.INVALID;
        return false;
    }

    @Override
    public long getMinorFaults() {
        return this.minorFaults;
    }

    @Override
    public long getMajorFaults() {
        return this.majorFaults;
    }

    @Override
    public long getContextSwitches() {
        return this.contextSwitches;
    }

    private boolean updateAttributes(Map<PsKeywords, String> psMap) {
        long now = System.currentTimeMillis();
        switch (psMap.get(PsKeywords.STATE).charAt(0)) {
        case 'R':
            this.state = OSProcess.State.RUNNING;
            break;
        case 'I':
        case 'S':
            this.state = OSProcess.State.SLEEPING;
            break;
        case 'D':
        case 'L':
        case 'U':
            this.state = OSProcess.State.WAITING;
            break;
        case 'Z':
            this.state = OSProcess.State.ZOMBIE;
            break;
        case 'T':
            this.state = OSProcess.State.STOPPED;
            break;
        default:
            this.state = OSProcess.State.OTHER;
            break;
        }
        this.parentProcessID = Parsing.parseIntOrDefault(psMap.get(PsKeywords.PPID), 0);
        this.user = psMap.get(PsKeywords.USER);
        this.userID = psMap.get(PsKeywords.UID);
        this.group = psMap.get(PsKeywords.GROUP);
        this.groupID = psMap.get(PsKeywords.GID);
        this.priority = Parsing.parseIntOrDefault(psMap.get(PsKeywords.PRI), 0);
        // These are in KB, multiply
        this.virtualSize = Parsing.parseLongOrDefault(psMap.get(PsKeywords.VSZ), 0) * 1024;
        this.residentSetSize = Parsing.parseLongOrDefault(psMap.get(PsKeywords.RSS), 0) * 1024;
        // Avoid divide by zero for processes up less than a second
        long elapsedTime = Parsing.parseDHMSOrDefault(psMap.get(PsKeywords.ETIME), 0L);
        this.upTime = elapsedTime < 1L ? 1L : elapsedTime;
        this.startTime = now - this.upTime;
        this.userTime = Parsing.parseDHMSOrDefault(psMap.get(PsKeywords.CPUTIME), 0L);
        // kernel time is included in user time
        this.kernelTime = 0L;
        this.path = psMap.get(PsKeywords.COMM);
        this.name = this.path.substring(this.path.lastIndexOf('/') + 1);
        this.minorFaults = Parsing.parseLongOrDefault(psMap.get(PsKeywords.MINFLT), 0L);
        this.majorFaults = Parsing.parseLongOrDefault(psMap.get(PsKeywords.MAJFLT), 0L);
        long nonVoluntaryContextSwitches = Parsing.parseLongOrDefault(psMap.get(PsKeywords.NIVCSW), 0L);
        long voluntaryContextSwitches = Parsing.parseLongOrDefault(psMap.get(PsKeywords.NVCSW), 0L);
        this.contextSwitches = voluntaryContextSwitches + nonVoluntaryContextSwitches;
        this.commandLineBackup = psMap.get(PsKeywords.ARGS);
        return true;
    }

    private void updateThreadCount() {
        List<String> threadList = Executor.runNative("ps -axHo tid -p " + getProcessID());
        if (!threadList.isEmpty()) {
            // Subtract 1 for header
            this.threadCount = threadList.size() - 1;
        }
        this.threadCount = 1;
    }

    /**
     * Package-private for use by OpenBsdOSThread
     */
    enum PsThreadColumns {
        TID, STATE, ETIME, CPUTIME, NIVCSW, NVCSW, MAJFLT, MINFLT, PRI, ARGS
    }
}
