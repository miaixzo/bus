/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
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
package org.miaixz.bus.health.builtin.software.common;

import java.util.Locale;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.builtin.software.OSProcess;
import org.miaixz.bus.health.builtin.software.OSThread;

/**
 * Common methods for OSThread implementation
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public abstract class AbstractOSThread implements OSThread {

    /**
     * The cumulativeCpuLoad value.
     */
    private final Supplier<Double> cumulativeCpuLoad = Memoizer
            .memoize(this::queryCumulativeCpuLoad, Memoizer.defaultExpiration());

    /**
     * The owningProcessId value.
     */
    private final int owningProcessId;

    /**
     * The thread id.
     */
    protected int threadId;

    /**
     * The thread name.
     */
    protected String name = "";

    /**
     * The thread state.
     */
    protected OSProcess.State state = OSProcess.State.INVALID;

    /**
     * The minor page fault count.
     */
    protected long minorFaults;

    /**
     * The major page fault count.
     */
    protected long majorFaults;

    /**
     * The start memory address.
     */
    protected long startMemoryAddress;

    /**
     * The context switch count.
     */
    protected long contextSwitches;

    /**
     * The kernel time in milliseconds.
     */
    protected long kernelTime;

    /**
     * The user time in milliseconds.
     */
    protected long userTime;

    /**
     * The start time in milliseconds since the epoch.
     */
    protected long startTime;

    /**
     * The uptime in milliseconds.
     */
    protected long upTime;

    /**
     * The OS-dependent priority.
     */
    protected int priority;

    /**
     * Creates a new AbstractOSThread instance.
     *
     * @param processId the process id
     */
    protected AbstractOSThread(int processId) {
        this.owningProcessId = processId;
    }

    /**
     * Returns the owning process id.
     *
     * @return the get owning process id result
     */
    @Override
    public int getOwningProcessId() {
        return this.owningProcessId;
    }

    /**
     * Returns the thread id.
     *
     * @return the thread id
     */
    @Override
    public int getThreadId() {
        return this.threadId;
    }

    /**
     * Returns the thread name.
     *
     * @return the thread name
     */
    @Override
    public String getName() {
        return this.name == null ? "" : this.name;
    }

    /**
     * Returns the thread state.
     *
     * @return the thread state
     */
    @Override
    public OSProcess.State getState() {
        return this.state;
    }

    /**
     * Returns the start memory address.
     *
     * @return the start memory address
     */
    @Override
    public long getStartMemoryAddress() {
        return this.startMemoryAddress;
    }

    /**
     * Returns the context switch count.
     *
     * @return the context switch count
     */
    @Override
    public long getContextSwitches() {
        return this.contextSwitches;
    }

    /**
     * Returns the minor fault count.
     *
     * @return the minor fault count
     */
    @Override
    public long getMinorFaults() {
        return this.minorFaults;
    }

    /**
     * Returns the major fault count.
     *
     * @return the major fault count
     */
    @Override
    public long getMajorFaults() {
        return this.majorFaults;
    }

    /**
     * Returns the kernel time.
     *
     * @return the kernel time
     */
    @Override
    public long getKernelTime() {
        return this.kernelTime;
    }

    /**
     * Returns the user time.
     *
     * @return the user time
     */
    @Override
    public long getUserTime() {
        return this.userTime;
    }

    /**
     * Returns the uptime.
     *
     * @return the uptime
     */
    @Override
    public long getUpTime() {
        return this.upTime;
    }

    /**
     * Returns the start time.
     *
     * @return the start time
     */
    @Override
    public long getStartTime() {
        return this.startTime;
    }

    /**
     * Returns the priority.
     *
     * @return the priority
     */
    @Override
    public int getPriority() {
        return this.priority;
    }

    /**
     * Returns the thread cpu load cumulative.
     *
     * @return the get thread cpu load cumulative result
     */
    @Override
    public double getThreadCpuLoadCumulative() {
        return cumulativeCpuLoad.get();
    }

    /**
     * Queries the cumulative cpu load.
     *
     * @return the query cumulative cpu load result
     */
    private double queryCumulativeCpuLoad() {
        return getUpTime() > 0d ? (getKernelTime() + getUserTime()) / (double) getUpTime() : 0d;
    }

    /**
     * Returns the thread cpu load between ticks.
     *
     * @param priorSnapshot the prior snapshot
     * @return the get thread cpu load between ticks result
     */
    @Override
    public double getThreadCpuLoadBetweenTicks(OSThread priorSnapshot) {
        if (priorSnapshot != null && owningProcessId == priorSnapshot.getOwningProcessId()
                && getThreadId() == priorSnapshot.getThreadId() && getUpTime() > priorSnapshot.getUpTime()) {
            return (getUserTime() - priorSnapshot.getUserTime() + getKernelTime() - priorSnapshot.getKernelTime())
                    / (double) (getUpTime() - priorSnapshot.getUpTime());
        }
        return getThreadCpuLoadCumulative();
    }

    /**
     * Returns the to string result.
     *
     * @return the to string result
     */
    @Override
    public String toString() {
        return "OSThread [threadId=" + getThreadId() + ", owningProcessId=" + getOwningProcessId() + ", name="
                + getName() + ", state=" + getState() + ", kernelTime=" + getKernelTime() + ", userTime="
                + getUserTime() + ", upTime=" + getUpTime() + ", startTime=" + getStartTime()
                + ", startMemoryAddress=0x" + String.format(Locale.ROOT, "%x", getStartMemoryAddress())
                + ", contextSwitches=" + getContextSwitches() + ", minorFaults=" + getMinorFaults() + ", majorFaults="
                + getMajorFaults() + "]";
    }

}
