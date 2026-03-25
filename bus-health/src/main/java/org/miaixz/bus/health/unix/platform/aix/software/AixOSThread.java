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
package org.miaixz.bus.health.unix.platform.aix.software;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.builtin.software.OSProcess;
import org.miaixz.bus.health.builtin.software.common.AbstractOSThread;
import org.miaixz.bus.health.unix.jna.AixLibc;
import org.miaixz.bus.health.unix.platform.aix.driver.PsInfo;

/**
 * OSThread implementation
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public class AixOSThread extends AbstractOSThread {

    private int threadId;
    private OSProcess.State state = OSProcess.State.INVALID;
    private long startMemoryAddress;
    private long contextSwitches;
    private long kernelTime;
    private long userTime;
    private long startTime;
    private long upTime;
    private int priority;

    public AixOSThread(int pid, int tid) {
        super(pid);
        this.threadId = tid;
        updateAttributes();
    }

    @Override
    public int getThreadId() {
        return this.threadId;
    }

    @Override
    public OSProcess.State getState() {
        return this.state;
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
    public int getPriority() {
        return this.priority;
    }

    @Override
    public boolean updateAttributes() {
        AixLibc.AixLwpsInfo lwpsinfo = PsInfo.queryLwpsInfo(getOwningProcessId(), getThreadId());
        if (lwpsinfo == null) {
            this.state = OSProcess.State.INVALID;
            return false;
        }
        this.threadId = (int) lwpsinfo.pr_lwpid; // 64 bit storage but always 32 bit
        this.startMemoryAddress = lwpsinfo.pr_addr;
        this.state = AixOSProcess.getStateFromOutput((char) lwpsinfo.pr_sname);
        this.priority = lwpsinfo.pr_pri;
        return true;
    }

}
