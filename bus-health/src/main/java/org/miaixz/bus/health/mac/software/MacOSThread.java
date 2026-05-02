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
package org.miaixz.bus.health.mac.software;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.builtin.software.OSProcess;
import org.miaixz.bus.health.builtin.software.common.AbstractOSThread;

/**
 * OSThread implementation
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public class MacOSThread extends AbstractOSThread {

    /**
     * The threadId value.
     */
    private final int threadId;
    /**
     * The state value.
     */
    private final OSProcess.State state;
    /**
     * The kernelTime value.
     */
    private final long kernelTime;
    /**
     * The userTime value.
     */
    private final long userTime;
    /**
     * The startTime value.
     */
    private final long startTime;
    /**
     * The upTime value.
     */
    private final long upTime;
    /**
     * The priority value.
     */
    private final int priority;

    /**
     * Creates a new MacOSThread instance.
     *
     * @param pid        the pid
     * @param threadId   the thread id
     * @param state      the state
     * @param kernelTime the kernel time
     * @param userTime   the user time
     * @param startTime  the start time
     * @param upTime     the up time
     * @param priority   the priority
     */
    public MacOSThread(int pid, int threadId, OSProcess.State state, long kernelTime, long userTime, long startTime,
            long upTime, int priority) {
        super(pid);
        this.threadId = threadId;
        this.state = state;
        this.kernelTime = kernelTime;
        this.userTime = userTime;
        this.startTime = startTime;
        this.upTime = upTime;
        this.priority = priority;
    }

    /**
     * Creates a new MacOSThread instance.
     *
     * @param processId the process id
     */
    public MacOSThread(int processId) {
        this(processId, 0, OSProcess.State.INVALID, 0L, 0L, 0L, 0L, 0);
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
     * Returns the state.
     *
     * @return the get state result
     */
    @Override
    public OSProcess.State getState() {
        return state;
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
        return priority;
    }

}
