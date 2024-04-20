/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org OSHI and other contributors.               *
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
package org.miaixz.bus.health.unix.solaris.software;

import com.sun.jna.Native;
import com.sun.jna.platform.unix.Resource;
import org.miaixz.bus.core.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.RegEx;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.IdGroup;
import org.miaixz.bus.health.Memoize;
import org.miaixz.bus.health.builtin.software.AbstractOSProcess;
import org.miaixz.bus.health.builtin.software.OSThread;
import org.miaixz.bus.health.unix.SolarisLibc;
import org.miaixz.bus.health.unix.solaris.drivers.PsInfo;
import org.miaixz.bus.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * OSProcess implementation
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public class SolarisOSProcess extends AbstractOSProcess {

    private final Supplier<Integer> bitness = Memoize.memoize(this::queryBitness);
    private final Supplier<SolarisLibc.SolarisPsInfo> psinfo = Memoize.memoize(this::queryPsInfo, Memoize.defaultExpiration());
    private final Supplier<Pair<List<String>, Map<String, String>>> cmdEnv = Memoize.memoize(this::queryCommandlineEnvironment);
    private final Supplier<SolarisLibc.SolarisPrUsage> prusage = Memoize.memoize(this::queryPrUsage, Memoize.defaultExpiration());
    private final SolarisOperatingSystem os;
    private String name;
    private String path = "";
    private String commandLineBackup;
    private final Supplier<String> commandLine = Memoize.memoize(this::queryCommandLine);
    private String user;
    private String userID;
    private String group;
    private String groupID;
    private State state = State.INVALID;
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
    private long contextSwitches = 0; // default

    public SolarisOSProcess(int pid, SolarisOperatingSystem os) {
        super(pid);
        this.os = os;
        updateAttributes();
    }

    /***
     * Returns Enum STATE for the state value obtained from status string of
     * thread/process.
     *
     * @param stateValue
     *            state value from the status string
     * @return The state
     */
    static State getStateFromOutput(char stateValue) {
        State state;
        switch (stateValue) {
            case 'O':
                state = State.RUNNING;
                break;
            case 'S':
                state = State.SLEEPING;
                break;
            case 'R':
            case 'W':
                state = State.WAITING;
                break;
            case 'Z':
                state = State.ZOMBIE;
                break;
            case 'T':
                state = State.STOPPED;
                break;
            default:
                state = State.OTHER;
                break;
        }
        return state;
    }

    private SolarisLibc.SolarisPsInfo queryPsInfo() {
        return PsInfo.queryPsInfo(this.getProcessID());
    }

    private SolarisLibc.SolarisPrUsage queryPrUsage() {
        return PsInfo.queryPrUsage(this.getProcessID());
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
        String cl = String.join(" ", getArguments());
        return cl.isEmpty() ? this.commandLineBackup : cl;
    }

    @Override
    public List<String> getArguments() {
        return cmdEnv.get().getLeft();
    }

    @Override
    public Map<String, String> getEnvironmentVariables() {
        return cmdEnv.get().getRight();
    }

    private Pair<List<String>, Map<String, String>> queryCommandlineEnvironment() {
        return PsInfo.queryArgsEnv(getProcessID(), psinfo.get());
    }

    @Override
    public String getCurrentWorkingDirectory() {
        try {
            String cwdLink = "/proc" + getProcessID() + "/cwd";
            String cwd = new File(cwdLink).getCanonicalPath();
            if (!cwd.equals(cwdLink)) {
                return cwd;
            }
        } catch (IOException e) {
            Logger.trace("Couldn't find cwd for pid {}: {}", getProcessID(), e.getMessage());
        }
        return "";
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

    @Override
    public long getOpenFiles() {
        try (Stream<Path> fd = Files.list(Paths.get("/proc/" + getProcessID() + "/fd"))) {
            return fd.count();
        } catch (IOException e) {
            return 0L;
        }
    }

    @Override
    public long getSoftOpenFileLimit() {
        if (getProcessID() == this.os.getProcessId()) {
            final Resource.Rlimit rlimit = new Resource.Rlimit();
            SolarisLibc.INSTANCE.getrlimit(SolarisLibc.RLIMIT_NOFILE, rlimit);
            return rlimit.rlim_cur;
        } else {
            return getProcessOpenFileLimit(getProcessID(), 1);
        }
    }

    @Override
    public long getHardOpenFileLimit() {
        if (getProcessID() == this.os.getProcessId()) {
            final Resource.Rlimit rlimit = new Resource.Rlimit();
            SolarisLibc.INSTANCE.getrlimit(SolarisLibc.RLIMIT_NOFILE, rlimit);
            return rlimit.rlim_max;
        } else {
            return getProcessOpenFileLimit(getProcessID(), 2);
        }
    }

    @Override
    public int getBitness() {
        return this.bitness.get();
    }

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
                String[] split = RegEx.SPACES.split(proc);
                int bitToSet = Builder.parseIntOrDefault(split[0], -1);
                if (bitToSet >= 0) {
                    bitMask |= 1L << bitToSet;
                }
            }
            return bitMask;
        } else if (cpuset.endsWith(".") && cpuset.contains("strongly bound to processor(s)")) {
            String parse = cpuset.substring(0, cpuset.length() - 1);
            String[] split = RegEx.SPACES.split(parse);
            for (int i = split.length - 1; i >= 0; i--) {
                int bitToSet = Builder.parseIntOrDefault(split[i], -1);
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

    @Override
    public List<OSThread> getThreadDetails() {
        // Get process files in proc
        File directory = new File(String.format(Locale.ROOT, "/proc/%d/lwp", getProcessID()));
        File[] numericFiles = directory.listFiles(file -> RegEx.NUMBERS.matcher(file.getName()).matches());
        if (numericFiles == null) {
            return Collections.emptyList();
        }

        return Arrays.stream(numericFiles).parallel().map(
                        lwpidFile -> new SolarisOSThread(getProcessID(), Builder.parseIntOrDefault(lwpidFile.getName(), 0)))
                .filter(OSThread.ThreadFiltering.VALID_THREAD).collect(Collectors.toList());
    }

    @Override
    public boolean updateAttributes() {
        SolarisLibc.SolarisPsInfo info = psinfo.get();
        if (info == null) {
            this.state = State.INVALID;
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
        this.startTime = info.pr_start.tv_sec.longValue() * 1000L + info.pr_start.tv_nsec.longValue() / 1_000_000L;
        // Avoid divide by zero for processes up less than a millisecond
        long elapsedTime = now - this.startTime;
        this.upTime = elapsedTime < 1L ? 1L : elapsedTime;
        this.kernelTime = 0L;
        this.userTime = info.pr_time.tv_sec.longValue() * 1000L + info.pr_time.tv_nsec.longValue() / 1_000_000L;
        // 80 character truncation but enough for path and name (usually)
        this.commandLineBackup = Native.toString(info.pr_psargs);
        this.path = RegEx.SPACES.split(commandLineBackup)[0];
        this.name = this.path.substring(this.path.lastIndexOf('/') + 1);
        if (usage != null) {
            this.userTime = usage.pr_utime.tv_sec.longValue() * 1000L + usage.pr_utime.tv_nsec.longValue() / 1_000_000L;
            this.kernelTime = usage.pr_stime.tv_sec.longValue() * 1000L
                    + usage.pr_stime.tv_nsec.longValue() / 1_000_000L;
            this.bytesRead = usage.pr_ioch.longValue();
            this.majorFaults = usage.pr_majf.longValue();
            this.minorFaults = usage.pr_minf.longValue();
            this.contextSwitches = usage.pr_ictx.longValue() + usage.pr_vctx.longValue();
        }
        return true;
    }

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
        return Long.parseLong(split[index]);
    }

}
