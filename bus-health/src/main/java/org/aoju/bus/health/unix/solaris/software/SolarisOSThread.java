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
package org.aoju.bus.health.unix.solaris.software;

import org.aoju.bus.core.annotation.ThreadSafe;
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
 * @version 6.2.6
 * @since JDK 1.8+
 */
@ThreadSafe
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

    public SolarisOSThread(int pid, Map<SolarisOSProcess.PsThreadColumns, String> psMap, Map<SolarisOperatingSystem.PrstatKeywords, String> prstatMap) {
        super(pid);
        updateAttributes(psMap, prstatMap);
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
        int pid = getOwningProcessId();
        List<String> threadList = Executor
                .runNative("ps -o " + SolarisOSProcess.PS_THREAD_COLUMNS + " -p " + pid);
        if (threadList.size() > 1) {
            // there is no switch for thread in ps command, hence filtering.
            String lwpStr = Integer.toString(this.threadId);
            for (String psOutput : threadList) {
                Map<SolarisOSProcess.PsThreadColumns, String> threadMap = Builder.stringToEnumMap(SolarisOSProcess.PsThreadColumns.class,
                        psOutput.trim(), ' ');
                if (threadMap.containsKey(SolarisOSProcess.PsThreadColumns.PRI) && lwpStr.equals(threadMap.get(SolarisOSProcess.PsThreadColumns.LWP))) {
                    List<String> prstatList = Executor.runNative("prstat -L -v -p " + pid + " 1 1");
                    String prstatRow = "";
                    for (String s : prstatList) {
                        String row = s.trim();
                        if (row.endsWith("/" + lwpStr)) {
                            prstatRow = row;
                            break;
                        }
                    }
                    Map<SolarisOperatingSystem.PrstatKeywords, String> prstatMap = Builder.stringToEnumMap(SolarisOperatingSystem.PrstatKeywords.class, prstatRow,
                            ' ');
                    return updateAttributes(threadMap, prstatMap);
                }
            }
        }
        this.state = OSProcess.State.INVALID;
        return false;
    }

    private boolean updateAttributes(Map<SolarisOSProcess.PsThreadColumns, String> psMap, Map<SolarisOperatingSystem.PrstatKeywords, String> prstatMap) {
        this.threadId = Builder.parseIntOrDefault(psMap.get(SolarisOSProcess.PsThreadColumns.LWP), 0);
        this.state = SolarisOSProcess.getStateFromOutput(psMap.get(SolarisOSProcess.PsThreadColumns.S).charAt(0));
        // Avoid divide by zero for processes up less than a second
        long elapsedTime = Builder.parseDHMSOrDefault(psMap.get(SolarisOSProcess.PsThreadColumns.ETIME), 0L);
        this.upTime = elapsedTime < 1L ? 1L : elapsedTime;
        long now = System.currentTimeMillis();
        this.startTime = now - this.upTime;
        this.kernelTime = 0L;
        this.userTime = Builder.parseDHMSOrDefault(psMap.get(SolarisOSProcess.PsThreadColumns.TIME), 0L);
        this.startMemoryAddress = Builder.hexStringToLong(psMap.get(SolarisOSProcess.PsThreadColumns.ADDR), 0L);
        this.priority = Builder.parseIntOrDefault(psMap.get(SolarisOSProcess.PsThreadColumns.PRI), 0);
        long nonVoluntaryContextSwitches = Builder.parseLongOrDefault(prstatMap.get(SolarisOperatingSystem.PrstatKeywords.ICX), 0L);
        long voluntaryContextSwitches = Builder.parseLongOrDefault(prstatMap.get(SolarisOperatingSystem.PrstatKeywords.VCX), 0L);
        this.contextSwitches = voluntaryContextSwitches + nonVoluntaryContextSwitches;
        return true;
    }

}