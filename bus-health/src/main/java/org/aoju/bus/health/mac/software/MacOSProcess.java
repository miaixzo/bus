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
package org.aoju.bus.health.mac.software;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.unix.LibCAPI;
import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.health.Memoize;
import org.aoju.bus.health.builtin.software.AbstractOSProcess;
import org.aoju.bus.health.builtin.software.OSThread;
import org.aoju.bus.health.mac.SysctlKit;
import org.aoju.bus.health.mac.SystemB;
import org.aoju.bus.health.mac.ThreadInfo;
import org.aoju.bus.health.unix.NativeSizeTByReference;
import org.aoju.bus.logger.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * OSProcess implemenation
 *
 * @author Kimi Liu
 * @version 6.2.5
 * @since JDK 1.8+
 */
@ThreadSafe
public class MacOSProcess extends AbstractOSProcess {

    // 64-bit flag
    private static final int P_LP64 = 0x4;
    /*
     * Mac OS States:
     */
    private static final int SSLEEP = 1; // sleeping on high priority
    private static final int SWAIT = 2; // sleeping on low priority
    private static final int SRUN = 3; // running
    private static final int SIDL = 4; // intermediate state in process creation
    private static final int SZOMB = 5; // intermediate state in process termination
    private static final int SSTOP = 6; // process being traced

    private int minorVersion;

    private Supplier<String> commandLine = Memoize.memoize(this::queryCommandLine);

    private String name = Normal.EMPTY;
    private String path = Normal.EMPTY;
    private String currentWorkingDirectory;
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
    private long openFiles;
    private int bitness;
    private long minorFaults;
    private long majorFaults;
    private long contextSwitches;

    public MacOSProcess(int pid, int minor) {
        super(pid);
        this.minorVersion = minor;
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
        return this.commandLine.get();
    }

    private String queryCommandLine() {
        // Get command line via sysctl
        int[] mib = new int[3];
        mib[0] = 1; // CTL_KERN
        mib[1] = 49; // KERN_PROCARGS2
        mib[2] = getProcessID();
        // Allocate memory for arguments
        int argmax = SysctlKit.sysctl("kern.argmax", 0);
        Pointer procargs = new Memory(argmax);
        NativeSizeTByReference size = new NativeSizeTByReference(new LibCAPI.size_t(argmax));
        // Fetch arguments
        if (0 != SystemB.INSTANCE.sysctl(mib, mib.length, procargs, size, null, LibCAPI.size_t.ZERO)) {
            Logger.warn(
                    "Failed syctl call for process arguments (kern.procargs2), process {} may not exist. Error code: {}",
                    getProcessID(), Native.getLastError());
            return Normal.EMPTY;
        }
        // Procargs contains an int representing total # of args, followed by a
        // null-terminated execpath string and then the arguments, each
        // null-terminated (possible multiple consecutive nulls),
        // The execpath string is also the first arg.
        int nargs = procargs.getInt(0);
        // Sanity check
        if (nargs < 0 || nargs > 1024) {
            Logger.error("Nonsensical number of process arguments for pid {}: {}", getProcessID(), nargs);
            return Normal.EMPTY;
        }
        List<String> args = new ArrayList<>(nargs);
        // Skip first int (containing value of nargs)
        long offset = SystemB.INT_SIZE;
        // Skip exec_command
        offset += procargs.getString(offset).length();
        // Iterate character by character using offset
        // Build each arg and add to list
        while (nargs-- > 0 && offset < size.getValue().longValue()) {
            // Advance through additional nulls
            while (procargs.getByte(offset) == 0) {
                if (++offset >= size.getValue().longValue()) {
                    break;
                }
            }
            // Grab a string. This should go until the null terminator
            String arg = procargs.getString(offset);
            args.add(arg);
            // Advance offset to next null
            offset += arg.length();
        }
        // Return args null-delimited
        return String.join("\0", args);
    }

    @Override
    public String getCurrentWorkingDirectory() {
        return this.currentWorkingDirectory;
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
        return this.openFiles;
    }

    @Override
    public int getBitness() {
        return this.bitness;
    }

    @Override
    public long getAffinityMask() {
        // macOS doesn't do affinity. Return a bitmask of the current processors.
        int logicalProcessorCount = SysctlKit.sysctl("hw.logicalcpu", 1);
        return logicalProcessorCount < 64 ? (1L << logicalProcessorCount) - 1 : -1L;
    }

    @Override
    public List<OSThread> getThreadDetails() {
        long now = System.currentTimeMillis();
        List<OSThread> details = new ArrayList<>();
        List<ThreadInfo.ThreadStats> stats = ThreadInfo.queryTaskThreads(getProcessID());
        for (ThreadInfo.ThreadStats stat : stats) {
            // For long running threads the start time calculation can overestimate
            long start = now - stat.getUpTime();
            if (start < this.getStartTime()) {
                start = this.getStartTime();
            }
            details.add(new MacOSThread(getProcessID(), stat.getThreadId(), stat.getState(), stat.getSystemTime(),
                    stat.getUserTime(), start, now - start, stat.getPriority()));
        }
        return details;
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
    public boolean updateAttributes() {
        long now = System.currentTimeMillis();
        SystemB.ProcTaskAllInfo taskAllInfo = new SystemB.ProcTaskAllInfo();
        if (0 > SystemB.INSTANCE.proc_pidinfo(getProcessID(), SystemB.PROC_PIDTASKALLINFO, 0, taskAllInfo,
                taskAllInfo.size()) || taskAllInfo.ptinfo.pti_threadnum < 1) {
            this.state = State.INVALID;
            return false;
        }
        Pointer buf = new Memory(SystemB.PROC_PIDPATHINFO_MAXSIZE);
        if (0 < SystemB.INSTANCE.proc_pidpath(getProcessID(), buf, SystemB.PROC_PIDPATHINFO_MAXSIZE)) {
            this.path = buf.getString(0).trim();
            // Overwrite name with last part of path
            String[] pathSplit = this.path.split(Symbol.SLASH);
            if (pathSplit.length > 0) {
                this.name = pathSplit[pathSplit.length - 1];
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
        SystemB.Passwd pwuid = SystemB.INSTANCE.getpwuid(taskAllInfo.pbsd.pbi_uid);
        if (null != pwuid) {
            this.user = pwuid.pw_name;
        }
        this.groupID = Integer.toString(taskAllInfo.pbsd.pbi_gid);
        SystemB.Group grgid = SystemB.INSTANCE.getgrgid(taskAllInfo.pbsd.pbi_gid);
        if (null != grgid) {
            this.group = grgid.gr_name;
        }
        this.threadCount = taskAllInfo.ptinfo.pti_threadnum;
        this.priority = taskAllInfo.ptinfo.pti_priority;
        this.virtualSize = taskAllInfo.ptinfo.pti_virtual_size;
        this.residentSetSize = taskAllInfo.ptinfo.pti_resident_size;
        this.kernelTime = taskAllInfo.ptinfo.pti_total_system / 1_000_000L;
        this.userTime = taskAllInfo.ptinfo.pti_total_user / 1_000_000L;
        this.startTime = taskAllInfo.pbsd.pbi_start_tvsec * 1000L + taskAllInfo.pbsd.pbi_start_tvusec / 1000L;
        this.upTime = now - this.startTime;
        this.openFiles = taskAllInfo.pbsd.pbi_nfiles;
        this.bitness = (taskAllInfo.pbsd.pbi_flags & P_LP64) == 0 ? 32 : 64;
        this.majorFaults = taskAllInfo.ptinfo.pti_pageins;
        // testing using getrusage confirms pti_faults includes both major and minor
        this.minorFaults = taskAllInfo.ptinfo.pti_faults - taskAllInfo.ptinfo.pti_pageins;
        this.contextSwitches = taskAllInfo.ptinfo.pti_csw;
        if (this.minorVersion >= 9) {
            SystemB.RUsageInfoV2 rUsageInfoV2 = new SystemB.RUsageInfoV2();
            if (0 == SystemB.INSTANCE.proc_pid_rusage(getProcessID(), SystemB.RUSAGE_INFO_V2, rUsageInfoV2)) {
                this.bytesRead = rUsageInfoV2.ri_diskio_bytesread;
                this.bytesWritten = rUsageInfoV2.ri_diskio_byteswritten;
            }
        }
        SystemB.VnodePathInfo vpi = new SystemB.VnodePathInfo();
        if (0 < SystemB.INSTANCE.proc_pidinfo(getProcessID(), SystemB.PROC_PIDVNODEPATHINFO, 0, vpi, vpi.size())) {
            int len = 0;
            for (byte b : vpi.pvi_cdir.vip_path) {
                if (b == 0) {
                    break;
                }
                len++;
            }
            this.currentWorkingDirectory = new String(vpi.pvi_cdir.vip_path, 0, len, Charset.US_ASCII);
        }
        return true;
    }

}
