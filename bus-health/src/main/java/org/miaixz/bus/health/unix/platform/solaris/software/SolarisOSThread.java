/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org OSHI and other contributors.               ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.health.unix.platform.solaris.software;

import java.util.function.Supplier;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.builtin.software.OSProcess;
import org.miaixz.bus.health.builtin.software.common.AbstractOSThread;
import org.miaixz.bus.health.unix.jna.SolarisLibc;
import org.miaixz.bus.health.unix.platform.solaris.driver.PsInfo;

import com.sun.jna.Pointer;

/**
 * OSThread implementation
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public class SolarisOSThread extends AbstractOSThread {

    private final int threadId;
    private final Supplier<SolarisLibc.SolarisLwpsInfo> lwpsinfo = Memoizer.memoize(this::queryLwpsInfo,
            Memoizer.defaultExpiration());
    private final Supplier<SolarisLibc.SolarisPrUsage> prusage = Memoizer.memoize(this::queryPrUsage,
            Memoizer.defaultExpiration());
    private String name;
    private OSProcess.State state = OSProcess.State.INVALID;
    private long startMemoryAddress;
    private long contextSwitches;
    private long kernelTime;
    private long userTime;
    private long startTime;
    private long upTime;
    private int priority;

    public SolarisOSThread(int pid, int lwpid) {
        super(pid);
        this.threadId = lwpid;
        updateAttributes();
    }

    private SolarisLibc.SolarisLwpsInfo queryLwpsInfo() {
        return PsInfo.queryLwpsInfo(this.getOwningProcessId(), this.getThreadId());
    }

    private SolarisLibc.SolarisPrUsage queryPrUsage() {
        return PsInfo.queryPrUsage(this.getOwningProcessId(), this.getThreadId());
    }

    @Override
    public String getName() {
        return this.name != null ? name : Normal.EMPTY;
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
