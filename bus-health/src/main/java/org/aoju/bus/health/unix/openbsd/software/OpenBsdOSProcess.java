/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org OSHI and other contributors.                 *
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
package org.aoju.bus.health.unix.openbsd.software;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.unix.LibCAPI;
import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Executor;
import org.aoju.bus.health.Memoize;
import org.aoju.bus.health.builtin.software.AbstractOSProcess;
import org.aoju.bus.health.builtin.software.OSProcess;
import org.aoju.bus.health.builtin.software.OSThread;
import org.aoju.bus.health.unix.openbsd.FstatKit;
import org.aoju.bus.health.unix.openbsd.OpenBsdLibc;
import org.aoju.bus.logger.Logger;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * OSProcess implemenation
 *
 * @author Kimi Liu
 * @version 6.3.3
 * @since JDK 1.8+
 */
@ThreadSafe
public class OpenBsdOSProcess extends AbstractOSProcess {

    static final String PS_THREAD_COLUMNS = Arrays.stream(PsThreadColumns.values()).map(Enum::name)
            .map(String::toLowerCase).collect(Collectors.joining(Symbol.COMMA));
    private static final int ARGMAX;

    static {
        int[] mib = new int[2];
        mib[0] = 1; // CTL_KERN
        mib[1] = 8; // KERN_ARGMAX
        Memory m = new Memory(Integer.BYTES);
        LibCAPI.size_t.ByReference size = new LibCAPI.size_t.ByReference(new LibCAPI.size_t(Integer.BYTES));
        if (OpenBsdLibc.INSTANCE.sysctl(mib, mib.length, m, size, null, LibCAPI.size_t.ZERO) == 0) {
            ARGMAX = m.getInt(0);
        } else {
            Logger.warn("Failed sysctl call for process arguments max size (kern.argmax). Error code: {}",
                    Native.getLastError());
            ARGMAX = 0;
        }
    }

    private Supplier<List<String>> arguments = Memoize.memoize(this::queryArguments);
    private Supplier<Map<String, String>> environmentVariables = Memoize.memoize(this::queryEnvironmentVariables);
    private int bitness;
    private String commandLineBackup;

    private String name;
    private String path = Normal.EMPTY;
    private String user;
    private String userID;
    private String group;
    private String groupID;
    private State state = OSProcess.State.INVALID;
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
    private Supplier<String> commandLine = Memoize.memoize(this::queryCommandLine);

    public OpenBsdOSProcess(int pid, Map<OpenBsdOperatingSystem.PsKeywords, String> psMap) {
        super(pid);
        updateThreadCount();
        updateAttributes(psMap);
    }

    @Override
    public String getCommandLine() {
        return this.commandLine.get();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    private String queryCommandLine() {
        String cl = String.join(" ", getArguments());
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
            Memory m = new Memory(ARGMAX);
            LibCAPI.size_t.ByReference size = new LibCAPI.size_t.ByReference(new LibCAPI.size_t(ARGMAX));
            // Fetch arguments
            if (OpenBsdLibc.INSTANCE.sysctl(mib, mib.length, m, size, null, LibCAPI.size_t.ZERO) == 0) {
                // Returns a null-terminated list of pointers to the actual data
                List<String> args = new ArrayList<>();
                // To iterate the pointer-list
                long offset = 0;
                // Get the data base address to calculate offsets
                long baseAddr = Pointer.nativeValue(m);
                long maxAddr = baseAddr + size.longValue();
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
        Memory m = new Memory(ARGMAX);
        LibCAPI.size_t.ByReference size = new LibCAPI.size_t.ByReference(new LibCAPI.size_t(ARGMAX));
        // Fetch environment variables
        if (OpenBsdLibc.INSTANCE.sysctl(mib, mib.length, m, size, null, LibCAPI.size_t.ZERO) == 0) {
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
                int idx = envStr.indexOf('=');
                if (idx > 0) {
                    env.put(envStr.substring(0, idx), envStr.substring(idx + 1));
                }
                offset += Native.POINTER_SIZE;
                argAddr = Pointer.nativeValue(m.getPointer(offset));
            }
            return Collections.unmodifiableMap(env);
        }
        return Collections.emptyMap();
    }

    @Override
    public int getBitness() {
        return this.bitness;
    }

    @Override
    public String getCurrentWorkingDirectory() {
        return FstatKit.getCwd(getProcessID());
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
    public State getState() {
        return this.state;
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
    public long getOpenFiles() {
        return FstatKit.getOpenFiles(getProcessID());
    }

    @Override
    public List<OSThread> getThreadDetails() {
        List<OSThread> threads = new ArrayList<>();
        // tdname, systime and tdaddr are unknown to OpenBSD ps
        // command may give the thread name, but should be put last with fixed length
        // split to avoid parsing any spaces
        String psCommand = "ps -aHwwxo " + PS_THREAD_COLUMNS;
        if (getProcessID() >= 0) {
            psCommand += " -p " + getProcessID();
        }
        List<String> threadList = Executor.runNative(psCommand);
        if (threadList.isEmpty() || threadList.size() < 2) {
            return threads;
        }
        // remove header row
        threadList.remove(0);
        // Fill list
        for (String thread : threadList) {
            Map<PsThreadColumns, String> threadMap = Builder.stringToEnumMap(PsThreadColumns.class, thread.trim(),
                    Symbol.C_SPACE);
            if (threadMap.containsKey(PsThreadColumns.ARGS)) {
                threads.add(new OpenBsdOSThread(getProcessID(), threadMap));
            }
        }
        return threads;
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
                int bitToSet = Builder.parseIntOrDefault(bit.trim(), -1);
                if (bitToSet >= 0) {
                    bitMask |= 1L << bitToSet;
                }
            }
        }
        return bitMask;
    }

    @Override
    public boolean updateAttributes() {
        // 'ps' does not provide threadCount or kernelTime on OpenBSD
        String psCommand = "ps -awwxo " + OpenBsdOperatingSystem.PS_COMMAND_ARGS + " -p " + getProcessID();
        List<String> procList = Executor.runNative(psCommand);
        if (procList.size() > 1) {
            // skip header row
            Map<OpenBsdOperatingSystem.PsKeywords, String> psMap = Builder.stringToEnumMap(OpenBsdOperatingSystem.PsKeywords.class, procList.get(1).trim(), Symbol.C_SPACE);
            // Check if last (thus all) value populated
            if (psMap.containsKey(OpenBsdOperatingSystem.PsKeywords.ARGS)) {
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

    private boolean updateAttributes(Map<OpenBsdOperatingSystem.PsKeywords, String> psMap) {
        long now = System.currentTimeMillis();
        switch (psMap.get(OpenBsdOperatingSystem.PsKeywords.STATE).charAt(0)) {
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
        this.parentProcessID = Builder.parseIntOrDefault(psMap.get(OpenBsdOperatingSystem.PsKeywords.PPID), 0);
        this.user = psMap.get(OpenBsdOperatingSystem.PsKeywords.USER);
        this.userID = psMap.get(OpenBsdOperatingSystem.PsKeywords.UID);
        this.group = psMap.get(OpenBsdOperatingSystem.PsKeywords.GROUP);
        this.groupID = psMap.get(OpenBsdOperatingSystem.PsKeywords.GID);
        this.priority = Builder.parseIntOrDefault(psMap.get(OpenBsdOperatingSystem.PsKeywords.PRI), 0);
        // These are in KB, multiply
        this.virtualSize = Builder.parseLongOrDefault(psMap.get(OpenBsdOperatingSystem.PsKeywords.VSZ), 0) * Normal._1024;
        this.residentSetSize = Builder.parseLongOrDefault(psMap.get(OpenBsdOperatingSystem.PsKeywords.RSS), 0) * Normal._1024;
        // Avoid divide by zero for processes up less than a second
        long elapsedTime = Builder.parseDHMSOrDefault(psMap.get(OpenBsdOperatingSystem.PsKeywords.ETIME), 0L);
        this.upTime = elapsedTime < 1L ? 1L : elapsedTime;
        this.startTime = now - this.upTime;
        this.userTime = Builder.parseDHMSOrDefault(psMap.get(OpenBsdOperatingSystem.PsKeywords.CPUTIME), 0L);
        // kernel time is included in user time
        this.kernelTime = 0L;
        this.path = psMap.get(OpenBsdOperatingSystem.PsKeywords.COMM);
        this.name = this.path.substring(this.path.lastIndexOf('/') + 1);
        this.minorFaults = Builder.parseLongOrDefault(psMap.get(OpenBsdOperatingSystem.PsKeywords.MINFLT), 0L);
        this.majorFaults = Builder.parseLongOrDefault(psMap.get(OpenBsdOperatingSystem.PsKeywords.MAJFLT), 0L);
        long nonVoluntaryContextSwitches = Builder.parseLongOrDefault(psMap.get(OpenBsdOperatingSystem.PsKeywords.NIVCSW), 0L);
        long voluntaryContextSwitches = Builder.parseLongOrDefault(psMap.get(OpenBsdOperatingSystem.PsKeywords.NVCSW), 0L);
        this.contextSwitches = voluntaryContextSwitches + nonVoluntaryContextSwitches;
        this.commandLineBackup = psMap.get(OpenBsdOperatingSystem.PsKeywords.ARGS);
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

    enum PsThreadColumns {
        TID, STATE, ETIME, CPUTIME, NIVCSW, NVCSW, MAJFLT, MINFLT, PRI, ARGS;
    }

}
