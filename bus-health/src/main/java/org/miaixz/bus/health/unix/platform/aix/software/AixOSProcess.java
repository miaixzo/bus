/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org OSHI Team and other contributors.          *
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
package org.miaixz.bus.health.unix.platform.aix.software;

import com.sun.jna.Native;
import com.sun.jna.platform.unix.Resource;
import com.sun.jna.platform.unix.aix.Perfstat.perfstat_process_t;
import org.miaixz.bus.core.annotation.ThreadSafe;
import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.IdGroup;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.software.OSProcess;
import org.miaixz.bus.health.builtin.software.OSThread;
import org.miaixz.bus.health.builtin.software.common.AbstractOSProcess;
import org.miaixz.bus.health.unix.jna.AixLibc;
import org.miaixz.bus.health.unix.platform.aix.driver.PsInfo;
import org.miaixz.bus.health.unix.platform.aix.driver.perfstat.PerfstatCpu;
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
public class AixOSProcess extends AbstractOSProcess {

    private final Supplier<Long> affinityMask = Memoizer.memoize(PerfstatCpu::queryCpuAffinityMask, Memoizer.defaultExpiration());
    private final AixOperatingSystem os;
    private final Supplier<Integer> bitness = Memoizer.memoize(this::queryBitness);
    private final Supplier<AixLibc.AixPsInfo> psinfo = Memoizer.memoize(this::queryPsInfo, Memoizer.defaultExpiration());
    private final Supplier<Pair<List<String>, Map<String, String>>> cmdEnv = Memoizer.memoize(this::queryCommandlineEnvironment);
    // Memoized copy from OperatingSystem
    private final Supplier<perfstat_process_t[]> procCpu;
    private String name;
    private String commandLineBackup;
    private final Supplier<String> commandLine = Memoizer.memoize(this::queryCommandLine);
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
    private String path = Normal.EMPTY;
    private OSProcess.State state = OSProcess.State.INVALID;

    public AixOSProcess(int pid, Pair<Long, Long> userSysCpuTime, Supplier<perfstat_process_t[]> procCpu,
                        AixOperatingSystem os) {
        super(pid);
        this.procCpu = procCpu;
        this.os = os;
        updateAttributes(userSysCpuTime);
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

    private AixLibc.AixPsInfo queryPsInfo() {
        return PsInfo.queryPsInfo(this.getProcessID());
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
        return Normal.EMPTY;
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
            AixLibc.INSTANCE.getrlimit(AixLibc.RLIMIT_NOFILE, rlimit);
            return rlimit.rlim_cur;
        } else {
            return -1L; // not supported
        }
    }

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
    public OSProcess.State getState() {
        return this.state;
    }

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

    @Override
    public boolean updateAttributes() {
        perfstat_process_t[] perfstat = procCpu.get();
        for (perfstat_process_t stat : perfstat) {
            int statpid = (int) stat.pid;
            if (statpid == getProcessID()) {
                return updateAttributes(Pair.of((long) stat.ucpu_time, (long) stat.scpu_time));
            }
        }
        this.state = OSProcess.State.INVALID;
        return false;
    }

    private boolean updateAttributes(Pair<Long, Long> userSysCpuTime) {
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
        this.userTime = userSysCpuTime.getLeft();
        this.kernelTime = userSysCpuTime.getRight();
        this.commandLineBackup = Native.toString(info.pr_psargs);
        this.path = Pattern.SPACES_PATTERN.split(commandLineBackup)[0];
        this.name = this.path.substring(this.path.lastIndexOf('/') + 1);
        if (this.name.isEmpty()) {
            this.name = Native.toString(info.pr_fname);
        }
        return true;
    }

}
