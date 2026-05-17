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
package org.miaixz.bus.health.windows.software;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.builtin.software.OSProcess;
import org.miaixz.bus.health.builtin.software.common.AbstractOSThread;
import org.miaixz.bus.health.windows.driver.registry.ThreadPerformanceData;
import org.miaixz.bus.health.windows.driver.registry.ThreadPerformanceData.PerfCounterBlock;

/**
 * OSThread implementation
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public class WindowsOSThread extends AbstractOSThread {

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
    private OSProcess.State state;

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
     * Creates a new WindowsOSThread instance.
     *
     * @param pid      the pid
     * @param tid      the tid
     * @param procName the proc name
     * @param pcb      the pcb
     */
    public WindowsOSThread(int pid, int tid, String procName, PerfCounterBlock pcb) {
        super(pid);
        this.threadId = tid;
        updateAttributes(procName, pcb);
    }

    /**
     * Returns the thread id.
     *
     * @return the get thread id result
     */
    @Override
    public int getThreadId() {
        return threadId;
    }

    /**
     * Returns the name.
     *
     * @return the get name result
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the state.
     *
     * @return the get state result
     */
    @Override
    public OSProcess.State getState() {
        return state;
    }

    /**
     * Returns the start memory address.
     *
     * @return the get start memory address result
     */
    @Override
    public long getStartMemoryAddress() {
        return startMemoryAddress;
    }

    /**
     * Returns the context switches.
     *
     * @return the get context switches result
     */
    @Override
    public long getContextSwitches() {
        return contextSwitches;
    }

    /**
     * Returns the kernel time.
     *
     * @return the get kernel time result
     */
    @Override
    public long getKernelTime() {
        return kernelTime;
    }

    /**
     * Returns the user time.
     *
     * @return the get user time result
     */
    @Override
    public long getUserTime() {
        return userTime;
    }

    /**
     * Returns the start time.
     *
     * @return the get start time result
     */
    @Override
    public long getStartTime() {
        return startTime;
    }

    /**
     * Returns the up time.
     *
     * @return the get up time result
     */
    @Override
    public long getUpTime() {
        return upTime;
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
        Set<Integer> pids = Collections.singleton(getOwningProcessId());
        String procName = this.name == null ? "" : this.name.split("/")[0];
        Map<Integer, ThreadPerformanceData.PerfCounterBlock> threads = ThreadPerformanceData
                .buildThreadMapFromPerfCounters(pids, procName, getThreadId());
        return updateAttributes(procName, threads == null ? null : threads.get(getThreadId()));
    }

    /**
     * Updates the attributes.
     *
     * @param procName the proc name
     * @param pcb      the pcb
     * @return the update attributes result
     */
    private boolean updateAttributes(String procName, PerfCounterBlock pcb) {
        if (pcb == null) {
            this.state = OSProcess.State.INVALID;
            return false;
        } else if (pcb.getName().contains("/") || procName == null || procName.isEmpty()) {
            this.name = pcb.getName();
        } else {
            this.name = procName + "/" + pcb.getName();
        }
        if (pcb.getThreadWaitReason() == 5) {
            state = OSProcess.State.SUSPENDED;
        } else {
            switch (pcb.getThreadState()) {
                case 0:
                    state = OSProcess.State.NEW;
                    break;

                case 2:
                case 3:
                    state = OSProcess.State.RUNNING;
                    break;

                case 4:
                    state = OSProcess.State.STOPPED;
                    break;

                case 5:
                    state = OSProcess.State.SLEEPING;
                    break;

                case 1:
                case 6:
                    state = OSProcess.State.WAITING;
                    break;

                case 7:
                default:
                    state = OSProcess.State.OTHER;
            }
        }
        startMemoryAddress = pcb.getStartAddress();
        contextSwitches = pcb.getContextSwitches();
        kernelTime = pcb.getKernelTime();
        userTime = pcb.getUserTime();
        startTime = pcb.getStartTime();
        upTime = System.currentTimeMillis() - pcb.getStartTime();
        priority = pcb.getPriority();
        return true;
    }

}
