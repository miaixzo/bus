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

    private final int threadId;
    private final OSProcess.State state;
    private final long kernelTime;
    private final long userTime;
    private final long startTime;
    private final long upTime;
    private final int priority;

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

    public MacOSThread(int processId) {
        this(processId, 0, OSProcess.State.INVALID, 0L, 0L, 0L, 0L, 0);
    }

    @Override
    public int getThreadId() {
        return threadId;
    }

    @Override
    public OSProcess.State getState() {
        return state;
    }

    @Override
    public long getKernelTime() {
        return kernelTime;
    }

    @Override
    public long getUserTime() {
        return userTime;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public long getUpTime() {
        return upTime;
    }

    @Override
    public int getPriority() {
        return priority;
    }

}
