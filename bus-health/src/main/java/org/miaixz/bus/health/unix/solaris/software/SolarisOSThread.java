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
package org.miaixz.bus.health.unix.solaris.software;

import java.util.function.Supplier;

import com.sun.jna.Pointer;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.builtin.software.OSProcess;
import org.miaixz.bus.health.builtin.software.common.AbstractOSThread;
import org.miaixz.bus.health.unix.shared.jna.SolarisLibc;
import org.miaixz.bus.health.unix.solaris.driver.PsInfo;

/**
 * OSThread implementation
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public class SolarisOSThread extends AbstractOSThread {

    /**
     * The threadId value.
     */
    private final int threadId;

    /**
     * The lwpsinfo value.
     */
    private final Supplier<SolarisLibc.SolarisLwpsInfo> lwpsinfo = Memoizer
            .memoize(this::queryLwpsInfo, Memoizer.defaultExpiration());

    /**
     * The prusage value.
     */
    private final Supplier<SolarisLibc.SolarisPrUsage> prusage = Memoizer
            .memoize(this::queryPrUsage, Memoizer.defaultExpiration());

    /**
     * The name value.
     */
    private String name;

    /**
     * The state value.
     */
    private OSProcess.State state = OSProcess.State.INVALID;

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
     * Creates a new SolarisOSThread instance.
     *
     * @param pid   the pid
     * @param lwpid the lwpid
     */
    public SolarisOSThread(int pid, int lwpid) {
        super(pid);
        this.threadId = lwpid;
        updateAttributes();
    }

    /**
     * Queries the lwps info.
     *
     * @return the query lwps info result
     */
    private SolarisLibc.SolarisLwpsInfo queryLwpsInfo() {
        return PsInfo.queryLwpsInfo(this.getOwningProcessId(), this.getThreadId());
    }

    /**
     * Queries the pr usage.
     *
     * @return the query pr usage result
     */
    private SolarisLibc.SolarisPrUsage queryPrUsage() {
        return PsInfo.queryPrUsage(this.getOwningProcessId(), this.getThreadId());
    }

    /**
     * Returns the name.
     *
     * @return the get name result
     */
    @Override
    public String getName() {
        return this.name != null ? name : Normal.EMPTY;
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
     * Returns the state.
     *
     * @return the get state result
     */
    @Override
    public OSProcess.State getState() {
        return this.state;
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
     * Returns the start time.
     *
     * @return the get start time result
     */
    @Override
    public long getStartTime() {
        return this.startTime;
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
        SolarisLibc.SolarisLwpsInfo info = lwpsinfo.get();
        if (info == null) {
            this.state = OSProcess.State.INVALID;
            return false;
        }
        SolarisLibc.SolarisPrUsage usage = prusage.get();
        long now = System.currentTimeMillis();
        this.state = SolarisOSProcess.getStateFromOutput((char) info.pr_sname);
        this.startTime = info.pr_start.tv_sec.longValue() * 1000L + info.pr_start.tv_nsec.longValue() / 1_000_000L;
        // Avoid divide by zero for processes up less than a millisecond
        long elapsedTime = now - this.startTime;
        this.upTime = elapsedTime < 1L ? 1L : elapsedTime;
        this.kernelTime = 0L;
        this.userTime = info.pr_time.tv_sec.longValue() * 1000L + info.pr_time.tv_nsec.longValue() / 1_000_000L;
        this.startMemoryAddress = Pointer.nativeValue(info.pr_addr);
        this.priority = info.pr_pri;
        this.contextSwitches = 0L;
        if (usage != null) {
            this.userTime = usage.pr_utime.tv_sec.longValue() * 1000L + usage.pr_utime.tv_nsec.longValue() / 1_000_000L;
            this.kernelTime = usage.pr_stime.tv_sec.longValue() * 1000L
                    + usage.pr_stime.tv_nsec.longValue() / 1_000_000L;
            this.contextSwitches = usage.pr_ictx.longValue() + usage.pr_vctx.longValue();
        }
        this.name = com.sun.jna.Native.toString(info.pr_name);
        if (StringKit.isBlank(name)) {
            this.name = com.sun.jna.Native.toString(info.pr_oldname);
        }
        return true;
    }

}
