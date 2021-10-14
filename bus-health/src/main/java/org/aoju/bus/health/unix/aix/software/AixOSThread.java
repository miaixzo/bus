/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org OSHI and other contributors.                 *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.health.unix.aix.software;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Executor;
import org.aoju.bus.health.builtin.software.AbstractOSThread;
import org.aoju.bus.health.builtin.software.OSProcess;

import java.util.List;
import java.util.Map;

/**
 * OSThread implementation
 *
 * @author Kimi Liu
 * @version 6.3.0
 * @since JDK 1.8+
 */
public class AixOSThread extends AbstractOSThread {

    private int threadId;
    private OSProcess.State state = OSProcess.State.INVALID;
    private long contextSwitches;
    private long kernelTime;
    private long userTime;
    private long startTime;
    private long upTime;
    private int priority;

    public AixOSThread(int pid, Map<AixOSProcess.PsThreadColumns, String> threadMap) {
        super(pid);
        updateAttributes(threadMap);
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
        List<String> threadListInfoPs = Executor.runNative("ps -m -o THREAD -p " + getOwningProcessId());
        // 1st row is header, 2nd row is process data.
        if (threadListInfoPs.size() > 2) {
            threadListInfoPs.remove(0); // header removed
            threadListInfoPs.remove(0); // process data removed
            String tidStr = Integer.toString(this.getThreadId());
            for (String threadInfo : threadListInfoPs) {
                Map<AixOSProcess.PsThreadColumns, String> threadMap = Builder.stringToEnumMap(AixOSProcess.PsThreadColumns.class,
                        threadInfo.trim(), Symbol.C_SPACE);
                if (threadMap.containsKey(AixOSProcess.PsThreadColumns.COMMAND)
                        && tidStr.equals(threadMap.get(AixOSProcess.PsThreadColumns.TID))) {
                    return updateAttributes(threadMap);
                }
            }
        }
        this.state = OSProcess.State.INVALID;
        return false;
    }

    private boolean updateAttributes(Map<AixOSProcess.PsThreadColumns, String> threadMap) {
        this.threadId = Builder.parseIntOrDefault(threadMap.get(AixOSProcess.PsThreadColumns.TID), 0);
        this.state = AixOSProcess.getStateFromOutput(threadMap.get(AixOSProcess.PsThreadColumns.ST).charAt(0));
        this.priority = Builder.parseIntOrDefault(threadMap.get(AixOSProcess.PsThreadColumns.PRI), 0);
        return true;
    }

}
