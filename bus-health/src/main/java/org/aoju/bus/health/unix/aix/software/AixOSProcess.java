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
package org.aoju.bus.health.unix.aix.software;

import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.RegEx;
import org.aoju.bus.core.lang.tuple.Pair;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Executor;
import org.aoju.bus.health.Memoize;
import org.aoju.bus.health.builtin.software.AbstractOSProcess;
import org.aoju.bus.health.builtin.software.OSProcess;
import org.aoju.bus.health.builtin.software.OSThread;
import org.aoju.bus.health.unix.aix.Perfstat;
import org.aoju.bus.health.unix.aix.drivers.perfstat.PerfstatCpu;

import java.util.*;
import java.util.function.Supplier;

/**
 * @author Kimi Liu
 * @version 6.1.0
 * @since JDK 1.8+
 */
@ThreadSafe
public class AixOSProcess extends AbstractOSProcess {

    private final Supplier<Long> affinityMask = Memoize.memoize(PerfstatCpu::queryCpuAffinityMask, Memoize.defaultExpiration());
    private Supplier<Integer> bitness = Memoize.memoize(this::queryBitness);
    private String name;
    private String path = Normal.EMPTY;
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
    private long majorFaults;

    // Memoized copy from OperatingSystem
    private Supplier<Perfstat.perfstat_process_t[]> procCpu;

    public AixOSProcess(int pid, String[] split, Map<Integer, Pair<Long, Long>> cpuMap,
                        Supplier<Perfstat.perfstat_process_t[]> procCpu) {
        super(pid);
        this.procCpu = procCpu;
        updateAttributes(split, cpuMap);
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
        return this.commandLine;
    }

    @Override
    public String getCurrentWorkingDirectory() {
        return Builder.getCwd(getProcessID());
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
        return Builder.getOpenFiles(getProcessID());
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
        // Need to capture BND field from ps
        // ps -m -o THREAD -p 12345
        // BND field for PID is either a dash (all processors) or the processor it's
        // bound to, do 1L << # to get mask
        long mask = 0L;
        List<String> processAffinityInfoList = Executor.runNative("ps -m -o THREAD -p " + getProcessID());
        if (processAffinityInfoList.size() > 2) { // what happens when the process has not thread?
            processAffinityInfoList.remove(0); // remove header row
            processAffinityInfoList.remove(0); // remove process row
            for (String processAffinityInfo : processAffinityInfoList) { // affinity information is in thread row
                String[] threadInfoSplit = RegEx.SPACES.split(processAffinityInfo.trim());
                if (threadInfoSplit.length > 13 && threadInfoSplit[4].charAt(0) != 'Z') { // only non-zombie threads
                    if (threadInfoSplit[11].charAt(0) == '-') { // affinity to all processors
                        return this.affinityMask.get();
                    } else {
                        int affinity = Builder.parseIntOrDefault(threadInfoSplit[11], 0);
                        mask |= 1L << affinity;
                    }
                }
            }
        }
        return mask;
    }

    @Override
    public List<OSThread> getThreadDetails() {
        List<String> threadListInfoPs = Executor.runNative("ps -m -o THREAD -p " + getProcessID());
        // 1st row is header, 2nd row is process data.
        if (threadListInfoPs.size() > 2) {
            List<OSThread> threads = new ArrayList<>();
            threadListInfoPs.remove(0); // header removed
            threadListInfoPs.remove(0); // process data removed
            for (String threadInfo : threadListInfoPs) {
                // USER,PID,PPID,TID,ST,CP,PRI,SC,WCHAN,F,TT,BND,COMMAND
                String[] threadInfoSplit = RegEx.SPACES.split(threadInfo.trim());
                if (threadInfoSplit.length == 13) {
                    String[] split = new String[3];
                    split[0] = threadInfoSplit[3]; // tid
                    split[1] = threadInfoSplit[4]; // state
                    split[2] = threadInfoSplit[6]; // priority
                    threads.add(new AixOSThread(getProcessID(), split));
                }
            }
            return threads;
        }
        return Collections.emptyList();
    }

    @Override
    public long getMajorFaults() {
        return this.majorFaults;
    }

    @Override
    public boolean updateAttributes() {
        Perfstat.perfstat_process_t[] perfstat = procCpu.get();
        List<String> procList = Executor.runNative(
                "ps -o s,pid,ppid,user,uid,group,gid,nlwp,pri,vsz,rss,etime,time,comm,args -p " + getProcessID());
        // Parse array to map of user/system times
        Map<Integer, Pair<Long, Long>> cpuMap = new HashMap<>();
        for (Perfstat.perfstat_process_t stat : perfstat) {
            cpuMap.put((int) stat.pid, Pair.of((long) stat.ucpu_time, (long) stat.scpu_time));
        }
        if (procList.size() > 1) {
            String[] split = RegEx.SPACES.split(procList.get(1).trim(), 15);
            // Elements should match ps command order
            if (split.length == 15) {
                return updateAttributes(split, cpuMap);
            }
        }
        this.state = State.INVALID;
        return false;
    }

    private boolean updateAttributes(String[] split, Map<Integer, Pair<Long, Long>> cpuMap) {
        long now = System.currentTimeMillis();
        this.state = getStateFromOutput(split[0].charAt(0));
        this.parentProcessID = Builder.parseIntOrDefault(split[2], 0);
        this.user = split[3];
        this.userID = split[4];
        this.group = split[5];
        this.groupID = split[6];
        this.threadCount = Builder.parseIntOrDefault(split[7], 0);
        this.priority = Builder.parseIntOrDefault(split[8], 0);
        // These are in KB, multiply
        this.virtualSize = Builder.parseLongOrDefault(split[9], 0) << 10;
        this.residentSetSize = Builder.parseLongOrDefault(split[10], 0) << 10;
        long elapsedTime = Builder.parseDHMSOrDefault(split[11], 0L);
        if (cpuMap.containsKey(getProcessID())) {
            Pair<Long, Long> userSystem = cpuMap.get(getProcessID());
            this.userTime = userSystem.getLeft();
            this.kernelTime = userSystem.getRight();
        } else {
            this.userTime = Builder.parseDHMSOrDefault(split[12], 0L);
            this.kernelTime = 0L;
        }
        // Avoid divide by zero for processes up less than a second
        this.upTime = elapsedTime < 1L ? 1L : elapsedTime;
        while (this.upTime < this.userTime + this.kernelTime) {
            this.upTime += 500L;
        }
        this.startTime = now - this.upTime;
        this.name = split[13];
        this.majorFaults = Builder.parseLongOrDefault(split[14], 0L);
        this.path = RegEx.SPACES.split(split[15])[0];
        this.commandLine = split[15];
        return true;
    }

}
