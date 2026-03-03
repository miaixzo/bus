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
package org.miaixz.bus.health.unix.platform.openbsd.software;

import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.software.OSProcess;
import org.miaixz.bus.health.builtin.software.common.AbstractOSThread;

/**
 * OSThread implementation
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public class OpenBsdOSThread extends AbstractOSThread {

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

    public OpenBsdOSThread(int processId, Map<OpenBsdOSProcess.PsThreadColumns, String> threadMap) {
        super(processId);
        updateAttributes(threadMap);
    }

    public OpenBsdOSThread(int processId, int threadId) {
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
        String psCommand = "ps -aHwwxo " + OpenBsdOSProcess.PS_THREAD_COLUMNS + " -p " + getOwningProcessId();
        // there is no switch for thread in ps command, hence filtering.
        List<String> threadList = Executor.runNative(psCommand);
        String tidStr = Integer.toString(this.threadId);
        for (String psOutput : threadList) {
            Map<OpenBsdOSProcess.PsThreadColumns, String> threadMap = Parsing
                    .stringToEnumMap(OpenBsdOSProcess.PsThreadColumns.class, psOutput.trim(), Symbol.C_SPACE);
            if (threadMap.containsKey(OpenBsdOSProcess.PsThreadColumns.ARGS)
                    && tidStr.equals(threadMap.get(OpenBsdOSProcess.PsThreadColumns.TID))) {
                return updateAttributes(threadMap);
            }
        }
        this.state = OSProcess.State.INVALID;
        return false;
    }

    private boolean updateAttributes(Map<OpenBsdOSProcess.PsThreadColumns, String> threadMap) {
        this.threadId = Parsing.parseIntOrDefault(threadMap.get(OpenBsdOSProcess.PsThreadColumns.TID), 0);
        switch (threadMap.get(OpenBsdOSProcess.PsThreadColumns.STATE).charAt(0)) {
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
        // Avoid divide by zero for processes up less than a second
        long elapsedTime = Parsing.parseDHMSOrDefault(threadMap.get(OpenBsdOSProcess.PsThreadColumns.ETIME), 0L);
        this.upTime = elapsedTime < 1L ? 1L : elapsedTime;
        long now = System.currentTimeMillis();
        this.startTime = now - this.upTime;
        // ps does not provide kerneltime on OpenBSD
        this.kernelTime = 0L;
        this.userTime = Parsing.parseDHMSOrDefault(threadMap.get(OpenBsdOSProcess.PsThreadColumns.CPUTIME), 0L);
        this.startMemoryAddress = 0L;
        long nonVoluntaryContextSwitches = Parsing
                .parseLongOrDefault(threadMap.get(OpenBsdOSProcess.PsThreadColumns.NIVCSW), 0L);
        long voluntaryContextSwitches = Parsing
                .parseLongOrDefault(threadMap.get(OpenBsdOSProcess.PsThreadColumns.NVCSW), 0L);
        this.contextSwitches = voluntaryContextSwitches + nonVoluntaryContextSwitches;
        this.majorFaults = Parsing.parseLongOrDefault(threadMap.get(OpenBsdOSProcess.PsThreadColumns.MAJFLT), 0L);
        this.minorFaults = Parsing.parseLongOrDefault(threadMap.get(OpenBsdOSProcess.PsThreadColumns.MINFLT), 0L);
        this.priority = Parsing.parseIntOrDefault(threadMap.get(OpenBsdOSProcess.PsThreadColumns.PRI), 0);
        this.name = threadMap.get(OpenBsdOSProcess.PsThreadColumns.ARGS);
        return true;
    }

}
