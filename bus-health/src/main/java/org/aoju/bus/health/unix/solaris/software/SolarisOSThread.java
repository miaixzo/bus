/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org OSHI and other contributors.                 *
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
package org.aoju.bus.health.unix.solaris.software;

import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Executor;
import org.aoju.bus.health.builtin.software.AbstractOSThread;
import org.aoju.bus.health.builtin.software.OSProcess;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Kimi Liu
 * @version 6.1.5
 * @since JDK 1.8+
 */
public class SolarisOSThread extends AbstractOSThread {

    private int threadId;
    private OSProcess.State state = OSProcess.State.INVALID;
    private long startMemoryAddress;
    private long contextSwitches;
    private long kernelTime;
    private long userTime;
    private long startTime;
    private long upTime;
    private int priority;

    public SolarisOSThread(int pid, String[] split) {
        super(pid);
        updateAttributes(split);
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
        List<String> threadListInfo1 = Executor
                .runNative("ps -o lwp,s,etime,stime,time,addr,pri -p " + getOwningProcessId());
        List<String> threadListInfo2 = Executor.runNative("prstat -L -v -p " + getOwningProcessId());
        Map<Integer, String[]> threadMap = SolarisOSProcess.parseAndMergeThreadInfo(threadListInfo1, threadListInfo2);
        if (threadMap.keySet().size() > 1) {
            Optional<String[]> split = threadMap.entrySet().stream()
                    .filter(entry -> entry.getKey() == this.getThreadId()).map(Map.Entry::getValue).findFirst();
            if (split.isPresent()) {
                return updateAttributes(split.get());
            }
        }
        this.state = OSProcess.State.INVALID;
        return false;
    }

    private boolean updateAttributes(String[] split) {
        this.threadId = Builder.parseIntOrDefault(split[0], 0);
        this.state = SolarisOSProcess.getStateFromOutput(split[1].charAt(0));
        // Avoid divide by zero for processes up less than a second
        long elapsedTime = Builder.parseDHMSOrDefault(split[2], 0L); // etimes
        this.upTime = elapsedTime < 1L ? 1L : elapsedTime;
        long now = System.currentTimeMillis();
        this.startTime = now - this.upTime;
        this.kernelTime = Builder.parseDHMSOrDefault(split[3], 0L); // systime
        this.userTime = Builder.parseDHMSOrDefault(split[4], 0L) - this.kernelTime; // time
        this.startMemoryAddress = Builder.hexStringToLong(split[5], 0L);
        this.priority = Builder.parseIntOrDefault(split[6], 0);
        long nonVoluntaryContextSwitches = Builder.parseLongOrDefault(split[7], 0L);
        long voluntaryContextSwitches = Builder.parseLongOrDefault(split[8], 0L);
        this.contextSwitches = voluntaryContextSwitches + nonVoluntaryContextSwitches;
        return true;
    }

}