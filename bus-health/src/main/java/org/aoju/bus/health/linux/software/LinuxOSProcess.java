/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org OSHI and other contributors.                 *
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
 ********************************************************************************/
package org.aoju.bus.health.linux.software;

import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.core.lang.RegEx;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Executor;
import org.aoju.bus.health.Memoize;
import org.aoju.bus.health.builtin.software.AbstractOSProcess;
import org.aoju.bus.health.builtin.software.OSThread;
import org.aoju.bus.health.linux.ProcPath;
import org.aoju.bus.health.linux.drivers.ProcessStat;
import org.aoju.bus.health.linux.drivers.UserGroup;
import org.aoju.bus.logger.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Kimi Liu
 * @version 6.0.9
 * @since JDK 1.8+
 */
@ThreadSafe
public class LinuxOSProcess extends AbstractOSProcess {

    private static final String LS_F_PROC_PID_FD = "ls -f " + ProcPath.PID_FD;
    // Resident Set Size is the number of pages the process has in real memory. To
    // get the actual size in bytes we need to multiply that with page size.
    private static final int PAGE_SIZE = Builder
            .parseIntOrDefault(Executor.getFirstAnswer("getconf PAGESIZE"), 4096);

    // Get a list of orders to pass to Builder
    private static final int[] PROC_PID_STAT_ORDERS = new int[ProcPidStat.values().length];

    static {
        for (ProcPidStat stat : ProcPidStat.values()) {
            // The PROC_PID_STAT enum indices are 1-indexed.
            // Subtract one to get a zero-based index
            PROC_PID_STAT_ORDERS[stat.ordinal()] = stat.getOrder() - 1;
        }
    }

    private String name;
    private String path = "";
    private Supplier<Integer> bitness = Memoize.memoize(this::queryBitness);
    private String commandLine;
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

    public LinuxOSProcess(int pid) {
        super(pid);
        this.commandLine = Builder.getStringFromFile(String.format(ProcPath.PID_CMDLINE, pid));
        updateAttributes();
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
        return this.commandLine;
    }

    @Override
    public String getCurrentWorkingDirectory() {
        try {
            String cwdLink = String.format(ProcPath.PID_CWD, getProcessID());
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
    public long getOpenFiles() {
        // subtract 1 from size for header
        return Executor.runNative(String.format(LS_F_PROC_PID_FD, getProcessID())).size() - 1L;
    }

    @Override
    public int getBitness() {
        return this.bitness.get();
    }

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
                Logger.warn("Failed to read process file: {}", path);
            }
        }
        return 0;
    }

    @Override
    public long getAffinityMask() {
        // Would prefer to use native sched_getaffinity call but variable sizing is
        // kernel-dependent and requires C macros, so we use command line instead.
        String mask = Executor.getFirstAnswer("taskset -p " + getProcessID());
        // Output:
        // pid 3283's current affinity mask: 3
        // pid 9726's current affinity mask: f
        String[] split = RegEx.SPACES.split(mask);
        try {
            return new BigInteger(split[split.length - 1], 16).longValue();
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public List<OSThread> getThreadDetails() {
        List<OSThread> threadDetails = ProcessStat.getThreadIds(getProcessID()).stream()
                .map(id -> new LinuxOSThread(getProcessID(), id)).collect(Collectors.toList());
        return Collections.unmodifiableList(threadDetails);
    }

    @Override
    public boolean updateAttributes() {
        String procPidExe = String.format(ProcPath.PID_EXE, getProcessID());
        try {
            Path link = Paths.get(procPidExe);
            this.path = Files.readSymbolicLink(link).toString();
            // For some services the symbolic link process has terminated
            int index = path.indexOf(" (deleted)");
            if (index != -1) {
                path = path.substring(0, index);
            }
        } catch (InvalidPathException | IOException | UnsupportedOperationException | SecurityException e) {
            Logger.debug("Unable to open symbolic link {}", procPidExe);
        }
        // Fetch all the values here
        // check for terminated process race condition after last one.
        Map<String, String> io = Builder.getKeyValueMapFromFile(String.format(ProcPath.PID_IO, getProcessID()), ":");
        Map<String, String> status = Builder.getKeyValueMapFromFile(String.format(ProcPath.PID_STATUS, getProcessID()),
                ":");
        String stat = Builder.getStringFromFile(String.format(ProcPath.PID_STAT, getProcessID()));
        if (stat.isEmpty()) {
            this.state = State.INVALID;
            return false;
        }
        long now = System.currentTimeMillis();

        // We can get name and status more easily from /proc/pid/status which we
        // call later, so just get the numeric bits here
        // See man proc for how to parse /proc/[pid]/stat
        long[] statArray = Builder.parseStringToLongArray(stat, PROC_PID_STAT_ORDERS,
                ProcessStat.PROC_PID_STAT_LENGTH, ' ');

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
        this.virtualSize = statArray[ProcPidStat.VSZ.ordinal()];
        this.residentSetSize = statArray[ProcPidStat.RSS.ordinal()] * PAGE_SIZE;
        this.kernelTime = statArray[ProcPidStat.KERNEL_TIME.ordinal()] * 1000L / LinuxOperatingSystem.getHz();
        this.userTime = statArray[ProcPidStat.USER_TIME.ordinal()] * 1000L / LinuxOperatingSystem.getHz();
        this.minorFaults = statArray[ProcPidStat.MINOR_FAULTS.ordinal()];
        this.majorFaults = statArray[ProcPidStat.MAJOR_FAULTS.ordinal()];
        this.upTime = now - startTime;

        // See man proc for how to parse /proc/[pid]/io
        this.bytesRead = Builder.parseLongOrDefault(io.getOrDefault("read_bytes", ""), 0L);
        this.bytesWritten = Builder.parseLongOrDefault(io.getOrDefault("write_bytes", ""), 0L);

        // Don't set open files or bitness or currentWorkingDirectory; fetch on demand.

        this.userID = RegEx.SPACES.split(status.getOrDefault("Uid", ""))[0];
        this.user = UserGroup.getUser(userID);
        this.groupID = RegEx.SPACES.split(status.getOrDefault("Gid", ""))[0];
        this.group = UserGroup.getGroupName(groupID);
        this.name = status.getOrDefault("Name", "");
        this.state = ProcessStat.getState(status.getOrDefault("State", "U").charAt(0));
        return true;
    }

    /**
     * Enum used to update attributes. The order field represents the 1-indexed
     * numeric order of the stat in /proc/pid/stat per the man file.
     */
    private enum ProcPidStat {
        // The parsing implementation in Builder requires these to be declared
        // in increasing order
        PPID(4), MINOR_FAULTS(10), MAJOR_FAULTS(12), USER_TIME(14), KERNEL_TIME(15), PRIORITY(18), THREAD_COUNT(20),
        START_TIME(22), VSZ(23), RSS(24);

        private int order;

        ProcPidStat(int order) {
            this.order = order;
        }

        public int getOrder() {
            return this.order;
        }
    }

}
