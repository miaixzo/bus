/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org OSHI and other contributors.               ~
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
package org.miaixz.bus.health.unix.platform.freebsd.software;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.software.OSProcess;
import org.miaixz.bus.health.builtin.software.common.AbstractOSThread;

import java.util.List;
import java.util.Map;

/**
 * OSThread implementation
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public class FreeBsdOSThread extends AbstractOSThread {

    private int threadId;
    private String name = Normal.EMPTY;
    private OSProcess.State state = OSProcess.State.INVALID;
    private long minorFaults;
    private long majorFaults;
    private long startMemoryAddress;
    private long contextSwitches;
    private long kernelTime;
    private long userTime;
    private long startTime;
    private long upTime;
    private int priority;

    public FreeBsdOSThread(int processId, Map<FreeBsdOSProcess.PsThreadColumns, String> threadMap) {
        super(processId);
        updateAttributes(threadMap);
    }

    public FreeBsdOSThread(int processId, int threadId) {
        super(processId);
        this.threadId = threadId;
        updateAttributes();
    }

    @Override
    public int getThreadId() {
        return this.threadId;
    }

    @Override
    public String getName() {
        return this.name;
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
    public long getMinorFaults() {
        return this.minorFaults;
    }

    @Override
    public long getMajorFaults() {
        return this.majorFaults;
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
        List<String> threadList = Executor
                .runNative("ps -awwxo " + FreeBsdOSProcess.PS_THREAD_COLUMNS + " -H -p " + getOwningProcessId());
        // there is no switch for thread in ps command, hence filtering.
        String lwpStr = Integer.toString(this.threadId);
        for (String psOutput : threadList) {
            Map<FreeBsdOSProcess.PsThreadColumns, String> threadMap = Parsing.stringToEnumMap(FreeBsdOSProcess.PsThreadColumns.class, psOutput.trim(),
                    Symbol.C_SPACE);
            if (threadMap.containsKey(FreeBsdOSProcess.PsThreadColumns.PRI) && lwpStr.equals(threadMap.get(FreeBsdOSProcess.PsThreadColumns.LWP))) {
                return updateAttributes(threadMap);
            }
        }
        this.state = OSProcess.State.INVALID;
        return false;
    }

    private boolean updateAttributes(Map<FreeBsdOSProcess.PsThreadColumns, String> threadMap) {
        this.name = threadMap.get(FreeBsdOSProcess.PsThreadColumns.TDNAME);
        this.threadId = Parsing.parseIntOrDefault(threadMap.get(FreeBsdOSProcess.PsThreadColumns.LWP), 0);
        switch (threadMap.get(FreeBsdOSProcess.PsThreadColumns.STATE).charAt(0)) {
            case 'R':
                this.state = OSProcess.State.RUNNING;
                break;
            case 'I':
            case 'S':
                this.state = OSProcess.State.SLEEPING;
                break;
            case 'D':
            case 'L':
            case 'U':
                this.state = OSProcess.State.WAITING;
                break;
            case 'Z':
                this.state = OSProcess.State.ZOMBIE;
                break;
            case 'T':
                this.state = OSProcess.State.STOPPED;
                break;
            default:
                this.state = OSProcess.State.OTHER;
                break;
        }
        long elapsedTime = Parsing.parseDHMSOrDefault(threadMap.get(FreeBsdOSProcess.PsThreadColumns.ETIMES), 0L);
        // Avoid divide by zero for processes up less than a second
        this.upTime = elapsedTime < 1L ? 1L : elapsedTime;
        long now = System.currentTimeMillis();
        this.startTime = now - this.upTime;
        this.kernelTime = Parsing.parseDHMSOrDefault(threadMap.get(FreeBsdOSProcess.PsThreadColumns.SYSTIME), 0L);
        this.userTime = Parsing.parseDHMSOrDefault(threadMap.get(FreeBsdOSProcess.PsThreadColumns.TIME), 0L) - this.kernelTime;
        this.startMemoryAddress = Parsing.hexStringToLong(threadMap.get(FreeBsdOSProcess.PsThreadColumns.TDADDR), 0L);
        long nonVoluntaryContextSwitches = Parsing.parseLongOrDefault(threadMap.get(FreeBsdOSProcess.PsThreadColumns.NIVCSW), 0L);
        long voluntaryContextSwitches = Parsing.parseLongOrDefault(threadMap.get(FreeBsdOSProcess.PsThreadColumns.NVCSW), 0L);
        this.contextSwitches = voluntaryContextSwitches + nonVoluntaryContextSwitches;
        this.majorFaults = Parsing.parseLongOrDefault(threadMap.get(FreeBsdOSProcess.PsThreadColumns.MAJFLT), 0L);
        this.minorFaults = Parsing.parseLongOrDefault(threadMap.get(FreeBsdOSProcess.PsThreadColumns.MINFLT), 0L);
        this.priority = Parsing.parseIntOrDefault(threadMap.get(FreeBsdOSProcess.PsThreadColumns.PRI), 0);
        return true;
    }
}
