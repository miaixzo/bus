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

    /**
     * The PROC_TASK_STAT_ORDERS constant.
     */
    private static final int[] PROC_TASK_STAT_ORDERS = new int[LinuxOSThread.ThreadPidStat.values().length];

    static {
        for (LinuxOSThread.ThreadPidStat stat : LinuxOSThread.ThreadPidStat.values()) {
            // The PROC_PID_STAT enum indices are 1-indexed.
            // Subtract one to get a zero-based index
            PROC_TASK_STAT_ORDERS[stat.ordinal()] = stat.getOrder() - 1;
        }
    }

    /**
     * The threadId value.
     */
    private final int threadId;

    /**
     * The name value.
     */
    private String name;

    /**
     * The state value.
     */
    private OSProcess.State state = OSProcess.State.INVALID;

    /**
     * The minorFaults value.
     */
    private long minorFaults;

    /**
     * The majorFaults value.
     */
    private long majorFaults;

    /**
     * The startMemoryAddress value.
     */
    private long startMemoryAddress;

    /**
     * The contextSwitches value.
     */
    private long contextSwitches;

    /**
     * The kernelTime value.
     */
    private long kernelTime;

    /**
     * The userTime value.
     */
    private long userTime;

    /**
     * The startTime value.
     */
    private long startTime;

    /**
     * The upTime value.
     */
    private long upTime;

    /**
     * The priority value.
     */
    private int priority;

    /**
     * Creates a new LinuxOSThread instance.
     *
     * @param processId the process id
     * @param tid       the tid
     */
    public LinuxOSThread(int processId, int tid) {
        super(processId);
        this.threadId = tid;
        updateAttributes();
    }

    /**
     * Returns the thread id.
     *
     * @return the get thread id result
     */
    @Override
    public int getThreadId() {
        return this.threadId;
    }

    /**
     * Returns the name.
     *
     * @return the get name result
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Returns the state.
     *
     * @return the get state result
     */
    @Override
    public OSProcess.State getState() {
        return this.state;
    }

    /**
     * Returns the start time.
     *
     * @return the get start time result
     */
    @Override
    public long getStartTime() {
        return this.startTime;
    }

    /**
     * Returns the start memory address.
     *
     * @return the get start memory address result
     */
    @Override
    public long getStartMemoryAddress() {
        return this.startMemoryAddress;
    }

    /**
     * Returns the context switches.
     *
     * @return the get context switches result
     */
    @Override
    public long getContextSwitches() {
        return this.contextSwitches;
    }

    /**
     * Returns the minor faults.
     *
     * @return the get minor faults result
     */
    @Override
    public long getMinorFaults() {
        return this.minorFaults;
    }

    /**
     * Returns the major faults.
     *
     * @return the get major faults result
     */
    @Override
    public long getMajorFaults() {
        return this.majorFaults;
    }

    /**
     * Returns the kernel time.
     *
     * @return the get kernel time result
     */
    @Override
    public long getKernelTime() {
        return this.kernelTime;
    }

    /**
     * Returns the user time.
     *
     * @return the get user time result
     */
    @Override
    public long getUserTime() {
        return this.userTime;
    }

    /**
     * Returns the up time.
     *
     * @return the get up time result
     */
    @Override
    public long getUpTime() {
        return this.upTime;
    }

    /**
     * Returns the priority.
     *
     * @return the get priority result
     */
    @Override
    public int getPriority() {
        return this.priority;
    }

    /**
     * Updates the attributes.
     *
     * @return the update attributes result
     */
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
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private enum ThreadPidStat {

        // The parsing implementation in Parsing requires these to be declared
        // in increasing order
        PPID(4), MINOR_FAULTS(10), MAJOR_FAULT(12), USER_TIME(14), KERNEL_TIME(15), PRIORITY(18), THREAD_COUNT(20),
        START_TIME(22), VSZ(23), RSS(24), START_CODE(26);

        /**
         * The order value.
         */
        private final int order;

        /**
         * Creates a new ThreadPidStat instance.
         *
         * @param order the order
         */
        ThreadPidStat(int order) {
            this.order = order;
        }

        /**
         * Returns the order.
         *
         * @return the get order result
         */
        public int getOrder() {
            return this.order;
        }

    }

}
