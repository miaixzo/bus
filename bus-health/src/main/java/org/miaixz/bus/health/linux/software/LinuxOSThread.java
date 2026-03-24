/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.           ~
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
package org.miaixz.bus.health.linux.software;

import java.util.Locale;
import java.util.Map;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.software.OSProcess;
import org.miaixz.bus.health.builtin.software.common.AbstractOSThread;
import org.miaixz.bus.health.linux.ProcPath;
import org.miaixz.bus.health.linux.driver.proc.ProcessStat;

/**
 * OSThread implementation
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public class LinuxOSThread extends AbstractOSThread {

    private static final int[] PROC_TASK_STAT_ORDERS = new int[LinuxOSThread.ThreadPidStat.values().length];

    static {
        for (LinuxOSThread.ThreadPidStat stat : LinuxOSThread.ThreadPidStat.values()) {
            // The PROC_PID_STAT enum indices are 1-indexed.
            // Subtract one to get a zero-based index
            PROC_TASK_STAT_ORDERS[stat.ordinal()] = stat.getOrder() - 1;
        }
    }

    private final int threadId;
    private String name;
    private OSProcess.State state = OSProcess.State.INVALID;
    private long minorFaults;
    private long majorFaults;
    private long startMemoryAddress;
    private long contextSwitches;
    private long kernelTime;
    private long userTime;
    private long startTime;
    private long upTime;
    private int priority;

    public LinuxOSThread(int processId, int tid) {
        super(processId);
        this.threadId = tid;
        updateAttributes();
    }

    @Override
    public int getThreadId() {
        return this.threadId;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public OSProcess.State getState() {
        return this.state;
    }

    @Override
    public long getStartTime() {
        return this.startTime;
    }

    @Override
    public long getStartMemoryAddress() {
        return this.startMemoryAddress;
    }

    @Override
    public long getContextSwitches() {
        return this.contextSwitches;
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
    public int getPriority() {
        return this.priority;
    }

    @Override
    public boolean updateAttributes() {
        this.name = Builder.getStringFromFile(
                String.format(Locale.ROOT, ProcPath.TASK_COMM, this.getOwningProcessId(), this.threadId));
        Map<String, String> status = Builder.getKeyValueMapFromFile(
                String.format(Locale.ROOT, ProcPath.TASK_STATUS, this.getOwningProcessId(), this.threadId),
                Symbol.COLON);
        String stat = Builder.getStringFromFile(
                String.format(Locale.ROOT, ProcPath.TASK_STAT, this.getOwningProcessId(), this.threadId));
        if (stat.isEmpty()) {
            this.state = OSProcess.State.INVALID;
            return false;
        }
        long now = System.currentTimeMillis();
        long[] statArray = Parsing
                .parseStringToLongArray(stat, PROC_TASK_STAT_ORDERS, ProcessStat.PROC_PID_STAT_LENGTH, Symbol.C_SPACE);

        // BOOTTIME is in seconds and start time from proc/pid/stat is in jiffies.
        // Combine units to jiffies and convert to millijiffies before hz division to
        // avoid precision loss without having to cast
        this.startTime = (LinuxOperatingSystem.BOOTTIME * LinuxOperatingSystem.getHz()
                + statArray[LinuxOSThread.ThreadPidStat.START_TIME.ordinal()]) * 1000L / LinuxOperatingSystem.getHz();
        // BOOT_TIME could be up to 500ms off and start time up to 5ms off. A process
        // that has started within last 505ms could produce a future start time/negative
        // up time, so insert a sanity check.
        if (this.startTime >= now) {
            this.startTime = now - 1;
        }
        this.minorFaults = statArray[ThreadPidStat.MINOR_FAULTS.ordinal()];
        this.majorFaults = statArray[ThreadPidStat.MAJOR_FAULT.ordinal()];
        this.startMemoryAddress = statArray[ThreadPidStat.START_CODE.ordinal()];
        long voluntaryContextSwitches = Parsing.parseLongOrDefault(status.get("voluntary_ctxt_switches"), 0L);
        long nonVoluntaryContextSwitches = Parsing.parseLongOrDefault(status.get("nonvoluntary_ctxt_switches"), 0L);
        this.contextSwitches = voluntaryContextSwitches + nonVoluntaryContextSwitches;
        this.state = ProcessStat.getState(status.getOrDefault("State", "U").charAt(0));
        this.kernelTime = statArray[ThreadPidStat.KERNEL_TIME.ordinal()] * 1000L / LinuxOperatingSystem.getHz();
        this.userTime = statArray[ThreadPidStat.USER_TIME.ordinal()] * 1000L / LinuxOperatingSystem.getHz();
        this.upTime = now - startTime;
        this.priority = (int) statArray[ThreadPidStat.PRIORITY.ordinal()];
        return true;
    }

    /**
     * Enum used to update attributes. The order field represents the 1-indexed numeric order of the stat in
     * /proc/pid/task/tid/stat per the man file.
     */
    private enum ThreadPidStat {

        // The parsing implementation in Parsing requires these to be declared
        // in increasing order
        PPID(4), MINOR_FAULTS(10), MAJOR_FAULT(12), USER_TIME(14), KERNEL_TIME(15), PRIORITY(18), THREAD_COUNT(20),
        START_TIME(22), VSZ(23), RSS(24), START_CODE(26);

        private final int order;

        ThreadPidStat(int order) {
            this.order = order;
        }

        public int getOrder() {
            return this.order;
        }
    }

}
