/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org OSHI and other contributors.               ~
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
package org.miaixz.bus.health.unix.platform.freebsd.software;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.platform.unix.LibCAPI.size_t;
import com.sun.jna.platform.unix.Resource;
import org.miaixz.bus.core.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.jna.ByRef;
import org.miaixz.bus.health.builtin.software.OSThread;
import org.miaixz.bus.health.builtin.software.common.AbstractOSProcess;
import org.miaixz.bus.health.unix.jna.FreeBsdLibc;
import org.miaixz.bus.health.unix.platform.freebsd.BsdSysctlKit;
import org.miaixz.bus.health.unix.platform.freebsd.ProcstatKit;
import org.miaixz.bus.logger.Logger;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * OSProcess implementation
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public class FreeBsdOSProcess extends AbstractOSProcess {

    static final String PS_THREAD_COLUMNS = Arrays.stream(PsThreadColumns.values()).map(Enum::name)
            .map(name -> name.toLowerCase(Locale.ROOT)).collect(Collectors.joining(Symbol.COMMA));
    private static final int ARGMAX = BsdSysctlKit.sysctl("kern.argmax", 0);
    private final FreeBsdOperatingSystem os;
    private final Supplier<Integer> bitness = Memoizer.memoize(this::queryBitness);
    private final Supplier<List<String>> arguments = Memoizer.memoize(this::queryArguments);
    private final Supplier<Map<String, String>> environmentVariables = Memoizer.memoize(this::queryEnvironmentVariables);
    private String path = Normal.EMPTY;
    private String name;
    private State state = State.INVALID;
    private String user;
    private String userID;
    private String group;
    private String groupID;
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
    private String commandLineBackup;
    private final Supplier<String> commandLine = Memoizer.memoize(this::queryCommandLine);

    public FreeBsdOSProcess(int pid, Map<FreeBsdOperatingSystem.PsKeywords, String> psMap, FreeBsdOperatingSystem os) {
        super(pid);
        this.os = os;
        updateAttributes(psMap);
    }

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
                            "Failed sysctl call for process arguments (kern.proc.args), process {} may not exist. Error code: {}",
                            getProcessID(), Native.getLastError());
                }
            }
        }
        return Collections.emptyList();
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
                            "Failed sysctl call for process environment variables (kern.proc.env), process {} may not exist. Error code: {}",
                            getProcessID(), Native.getLastError());
                }
            }
        }
        return Collections.emptyMap();
    }

    @Override
    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables.get();
    }

    @Override
    public String getCurrentWorkingDirectory() {
        return ProcstatKit.getCwd(getProcessID());
    }

    @Override
    public State getState() {
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
        return ProcstatKit.getOpenFiles(getProcessID());
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
            FreeBsdLibc.INSTANCE.getrlimit(FreeBsdLibc.RLIMIT_NOFILE, rlimit);
            return rlimit.rlim_cur;
        } else {
            return getProcessOpenFileLimit(getProcessID(), 1);
        }
    }

    @Override
    public long getHardOpenFileLimit() {
        if (getProcessID() == this.os.getProcessId()) {
            final Resource.Rlimit rlimit = new Resource.Rlimit();
            FreeBsdLibc.INSTANCE.getrlimit(FreeBsdLibc.RLIMIT_NOFILE, rlimit);
            return rlimit.rlim_max;
        } else {
            return getProcessOpenFileLimit(getProcessID(), 2);
        }
    }

    @Override
    public int getBitness() {
        return this.bitness.get();
    }

    @Override
    public List<OSThread> getThreadDetails() {
        String psCommand = "ps -awwxo " + PS_THREAD_COLUMNS + " -H";
        if (getProcessID() >= 0) {
            psCommand += " -p " + getProcessID();
        }
        Predicate<Map<PsThreadColumns, String>> hasColumnsPri = threadMap -> threadMap.containsKey(PsThreadColumns.PRI);
        return Executor.runNative(psCommand).stream().skip(1).parallel()
                .map(thread -> Parsing.stringToEnumMap(PsThreadColumns.class, thread.trim(), Symbol.C_SPACE))
                .filter(hasColumnsPri).map(threadMap -> new FreeBsdOSThread(getProcessID(), threadMap))
                .filter(OSThread.ThreadFiltering.VALID_THREAD).collect(Collectors.toList());
    }

    private int queryBitness() {
        // Get process abi vector
        int[] mib = new int[4];
        mib[0] = 1; // CTL_KERN
        mib[1] = 14; // KERN_PROC
        mib[2] = 9; // KERN_PROC_SV_NAME
        mib[3] = getProcessID();
        // Allocate memory for arguments
        try (Memory abi = new Memory(32); ByRef.CloseableSizeTByReference size = new ByRef.CloseableSizeTByReference(32)) {
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

    @Override
    public boolean updateAttributes() {
        String psCommand = "ps -awwxo " + FreeBsdOperatingSystem.PS_COMMAND_ARGS + " -p " + getProcessID();
        List<String> procList = Executor.runNative(psCommand);
        if (procList.size() > 1) {
            // skip header row
            Map<FreeBsdOperatingSystem.PsKeywords, String> psMap = Parsing.stringToEnumMap(FreeBsdOperatingSystem.PsKeywords.class, procList.get(1).trim(), Symbol.C_SPACE);
            // Check if last (thus all) value populated
            if (psMap.containsKey(FreeBsdOperatingSystem.PsKeywords.ARGS)) {
                return updateAttributes(psMap);
            }
        }
        this.state = State.INVALID;
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

    private boolean updateAttributes(Map<FreeBsdOperatingSystem.PsKeywords, String> psMap) {
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
            case 'L':
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
        this.kernelTime = Parsing.parseDHMSOrDefault(psMap.get(FreeBsdOperatingSystem.PsKeywords.SYSTIME), 0L);
        this.userTime = Parsing.parseDHMSOrDefault(psMap.get(FreeBsdOperatingSystem.PsKeywords.TIME), 0L) - this.kernelTime;
        this.path = psMap.get(FreeBsdOperatingSystem.PsKeywords.COMM);
        this.name = this.path.substring(this.path.lastIndexOf('/') + 1);
        this.minorFaults = Parsing.parseLongOrDefault(psMap.get(FreeBsdOperatingSystem.PsKeywords.MAJFLT), 0L);
        this.majorFaults = Parsing.parseLongOrDefault(psMap.get(FreeBsdOperatingSystem.PsKeywords.MINFLT), 0L);
        long nonVoluntaryContextSwitches = Parsing.parseLongOrDefault(psMap.get(FreeBsdOperatingSystem.PsKeywords.NVCSW), 0L);
        long voluntaryContextSwitches = Parsing.parseLongOrDefault(psMap.get(FreeBsdOperatingSystem.PsKeywords.NIVCSW), 0L);
        this.contextSwitches = voluntaryContextSwitches + nonVoluntaryContextSwitches;
        this.commandLineBackup = psMap.get(FreeBsdOperatingSystem.PsKeywords.ARGS);
        return true;
    }

    private long getProcessOpenFileLimit(long processId, int index) {
        final String limitsPath = String.format(Locale.ROOT, "/proc/%d/limits", processId);
        if (!Files.exists(Paths.get(limitsPath))) {
            return -1; // not supported
        }
        final List<String> lines = Builder.readFile(limitsPath);
        final Optional<String> maxOpenFilesLine = lines.stream().filter(line -> line.startsWith("Max open files"))
                .findFirst();
        if (!maxOpenFilesLine.isPresent()) {
            return -1;
        }

        // Split all non-Digits away -> ["", "{soft-limit}, "{hard-limit}"]
        final String[] split = maxOpenFilesLine.get().split("\\D+");
        return Parsing.parseLongOrDefault(split[index], -1);
    }

    /*
     * Package-private for use by FreeBsdOSThread
     */
    enum PsThreadColumns {
        TDNAME, LWP, STATE, ETIMES, SYSTIME, TIME, TDADDR, NIVCSW, NVCSW, MAJFLT, MINFLT, PRI
    }
}
