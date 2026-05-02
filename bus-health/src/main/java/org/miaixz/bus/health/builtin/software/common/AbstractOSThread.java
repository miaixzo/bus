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
package org.miaixz.bus.health.builtin.software.common;

import java.util.Locale;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Memoizer;
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
