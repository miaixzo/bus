/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org OSHI and other contributors.                 *
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
package org.aoju.bus.health.windows.hardware;

import com.sun.jna.Memory;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;
import com.sun.jna.platform.win32.PowrProf.POWER_INFORMATION_LEVEL;
import com.sun.jna.platform.win32.WinBase.SYSTEM_INFO;
import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.core.lang.RegEx;
import org.aoju.bus.core.lang.tuple.Pair;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.builtin.hardware.AbstractCentralProcessor;
import org.aoju.bus.health.builtin.hardware.CentralProcessor;
import org.aoju.bus.health.windows.WmiKit;
import org.aoju.bus.health.windows.drivers.LogicalProcessorInformation;
import org.aoju.bus.health.windows.drivers.perfmon.ProcessorInformation;
import org.aoju.bus.health.windows.drivers.perfmon.SystemInformation;
import org.aoju.bus.health.windows.drivers.wmi.Win32Processor;
import org.aoju.bus.logger.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A CPU, representing all of a system's processors. It may contain multiple
 * individual Physical and Logical processors.
 *
 * @author Kimi Liu
 * @version 6.3.5
 * @since JDK 1.8+
 */
@ThreadSafe
final class WindowsCentralProcessor extends AbstractCentralProcessor {

    // populated by initProcessorCounts called by the parent constructor
    private Map<String, Integer> numaNodeProcToLogicalProcMap;

    // Stores first set of ticks for cpu adjustment
    private long[] systicks = null;
    private long[] idleticks = null;
    private long[] elapsedticks = null;
    private long[] privticks = null;
    private long[] procticks = null;
    private long[] baseticks = null;

    WindowsCentralProcessor() {
        super();
        if (ProcessorInformation.USE_CPU_UTILITY) {
            Pair<List<String>, Map<ProcessorInformation.ProcessorCapacityTickCountProperty, List<Long>>> initialTicks = ProcessorInformation
                    .queryInitialProcessorCapacityCounters();
            List<String> instances = initialTicks.getLeft();
            Map<ProcessorInformation.ProcessorCapacityTickCountProperty, List<Long>> valueMap = initialTicks.getRight();
            List<Long> systemList = valueMap.get(ProcessorInformation.ProcessorCapacityTickCountProperty.PERCENTPRIVILEGEDTIME);
            List<Long> userList = valueMap.get(ProcessorInformation.ProcessorCapacityTickCountProperty.PERCENTUSERTIME);
            // % Processor Time is actually Idle time
            List<Long> idleList = valueMap.get(ProcessorInformation.ProcessorCapacityTickCountProperty.PERCENTPROCESSORTIME);
            // Utility ticks, if configured
            List<Long> systemUtility = valueMap.get(ProcessorInformation.ProcessorCapacityTickCountProperty.PERCENTPRIVILEGEDUTILITY);
            List<Long> processorUtility = valueMap.get(ProcessorInformation.ProcessorCapacityTickCountProperty.PERCENTPROCESSORUTILITY);
            List<Long> processorUtilityBase = valueMap
                    .get(ProcessorInformation.ProcessorCapacityTickCountProperty.PERCENTPROCESSORUTILITY_BASE);
            int ncpu = getLogicalProcessorCount();
            systicks = new long[ncpu];
            idleticks = new long[ncpu];
            elapsedticks = new long[ncpu];
            privticks = new long[ncpu];
            procticks = new long[ncpu];
            baseticks = new long[ncpu];

            for (int p = 0; p < instances.size(); p++) {
                int cpu = instances.get(p).contains(",")
                        ? numaNodeProcToLogicalProcMap.getOrDefault(instances.get(p), 0)
                        : Builder.parseIntOrDefault(instances.get(p), 0);
                if (cpu < ncpu) {
                    systicks[cpu] = systemList.get(p);
                    idleticks[cpu] = idleList.get(p);
                    elapsedticks[cpu] = userList.get(p) + idleticks[cpu] + systicks[cpu];
                    privticks[cpu] = systemUtility.get(p);
                    procticks[cpu] = processorUtility.get(p);
                    baseticks[cpu] = processorUtilityBase.get(p);
                }
            }
        }
    }

    /**
     * Parses identifier string
     *
     * @param identifier the full identifier string
     * @param key        the key to retrieve
     * @return the string following id
     */
    private static String parseIdentifier(String identifier, String key) {
        String[] idSplit = RegEx.SPACES.split(identifier);
        boolean found = false;
        for (String s : idSplit) {
            // If key string found, return next value
            if (found) {
                return s;
            }
            found = s.equals(key);
        }
        // If key string not found, return empty string
        return "";
    }

    /**
     * Initializes Class variables
     */
    @Override
    protected CentralProcessor.ProcessorIdentifier queryProcessorId() {
        String cpuVendor = "";
        String cpuName = "";
        String cpuIdentifier = "";
        String cpuFamily = "";
        String cpuModel = "";
        String cpuStepping = "";
        long cpuVendorFreq = 0L;
        String processorID;
        boolean cpu64bit = false;

        final String cpuRegistryRoot = "HARDWARE\\DESCRIPTION\\System\\CentralProcessor\\";
        String[] processorIds = Advapi32Util.registryGetKeys(WinReg.HKEY_LOCAL_MACHINE, cpuRegistryRoot);
        if (processorIds.length > 0) {
            String cpuRegistryPath = cpuRegistryRoot + processorIds[0];
            cpuVendor = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, cpuRegistryPath,
                    "VendorIdentifier");
            cpuName = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, cpuRegistryPath,
                    "ProcessorNameString");
            cpuIdentifier = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, cpuRegistryPath,
                    "Identifier");
            try {
                cpuVendorFreq = Advapi32Util.registryGetIntValue(WinReg.HKEY_LOCAL_MACHINE, cpuRegistryPath, "~MHz")
                        * 1_000_000L;
            } catch (Win32Exception e) {
                // Leave as 0, parse the identifier as backup
            }
        }
        if (!cpuIdentifier.isEmpty()) {
            cpuFamily = parseIdentifier(cpuIdentifier, "Family");
            cpuModel = parseIdentifier(cpuIdentifier, "Model");
            cpuStepping = parseIdentifier(cpuIdentifier, "Stepping");
        }
        SYSTEM_INFO sysinfo = new SYSTEM_INFO();
        Kernel32.INSTANCE.GetNativeSystemInfo(sysinfo);
        int processorArchitecture = sysinfo.processorArchitecture.pi.wProcessorArchitecture.intValue();
        if (processorArchitecture == 9 // PROCESSOR_ARCHITECTURE_AMD64
                || processorArchitecture == 12 // PROCESSOR_ARCHITECTURE_ARM64
                || processorArchitecture == 6) { // PROCESSOR_ARCHITECTURE_IA64
            cpu64bit = true;
        }
        WmiResult<Win32Processor.ProcessorIdProperty> processorId = Win32Processor.queryProcessorId();
        if (processorId.getResultCount() > 0) {
            processorID = WmiKit.getString(processorId, Win32Processor.ProcessorIdProperty.PROCESSORID, 0);
        } else {
            processorID = createProcessorID(cpuStepping, cpuModel, cpuFamily,
                    cpu64bit ? new String[]{"ia64"} : new String[0]);
        }
        return new CentralProcessor.ProcessorIdentifier(cpuVendor, cpuName, cpuFamily, cpuModel, cpuStepping, processorID, cpu64bit,
                cpuVendorFreq);
    }

    @Override
    protected Pair<List<CentralProcessor.LogicalProcessor>, List<CentralProcessor.PhysicalProcessor>> initProcessorCounts() {
        if (VersionHelpers.IsWindows7OrGreater()) {
            Pair<List<CentralProcessor.LogicalProcessor>, List<CentralProcessor.PhysicalProcessor>> procs = LogicalProcessorInformation
                    .getLogicalProcessorInformationEx();
            // Save numaNode,Processor lookup for future PerfCounter instance lookup
            // The processor number is based on the Processor Group, so we keep a separate
            // index by NUMA node.
            int curNode = -1;
            int procNum = 0;
            // 0-indexed list of all lps for array lookup
            int lp = 0;
            this.numaNodeProcToLogicalProcMap = new HashMap<>();
            for (CentralProcessor.LogicalProcessor logProc : procs.getLeft()) {
                int node = logProc.getNumaNode();
                // This list is grouped by NUMA node so a change in node will reset this counter
                if (node != curNode) {
                    curNode = node;
                    procNum = 0;
                }
                numaNodeProcToLogicalProcMap.put(String.format("%d,%d", logProc.getNumaNode(), procNum++), lp++);
            }
            return procs;
        } else {
            return LogicalProcessorInformation.getLogicalProcessorInformation();
        }
    }

    @Override
    public long[] querySystemCpuLoadTicks() {
        // To get load in processor group scenario, we need perfmon counters, but the
        // _Total instance is an average rather than total (scaled) number of ticks
        // which matches GetSystemTimes() results. We can just query the per-processor
        // ticks and add them up. Calling the get() method gains the benefit of
        // synchronizing this output with the memoized result of per-processor ticks as
        // well.
        long[] ticks = new long[CentralProcessor.TickType.values().length];
        // Sum processor ticks
        long[][] procTicks = getProcessorCpuLoadTicks();
        for (int i = 0; i < ticks.length; i++) {
            for (long[] procTick : procTicks) {
                ticks[i] += procTick[i];
            }
        }
        return ticks;
    }

    @Override
    public long[] queryCurrentFreq() {
        if (VersionHelpers.IsWindows7OrGreater()) {
            Pair<List<String>, Map<ProcessorInformation.ProcessorFrequencyProperty, List<Long>>> instanceValuePair = ProcessorInformation
                    .queryFrequencyCounters();
            List<String> instances = instanceValuePair.getLeft();
            Map<ProcessorInformation.ProcessorFrequencyProperty, List<Long>> valueMap = instanceValuePair.getRight();
            List<Long> percentMaxList = valueMap.get(ProcessorInformation.ProcessorFrequencyProperty.PERCENTOFMAXIMUMFREQUENCY);
            if (!instances.isEmpty()) {
                long maxFreq = this.getMaxFreq();
                long[] freqs = new long[getLogicalProcessorCount()];
                for (int p = 0; p < instances.size(); p++) {
                    int cpu = instances.get(p).contains(",")
                            ? numaNodeProcToLogicalProcMap.getOrDefault(instances.get(p), 0)
                            : Builder.parseIntOrDefault(instances.get(p), 0);
                    if (cpu >= getLogicalProcessorCount()) {
                        continue;
                    }
                    freqs[cpu] = percentMaxList.get(cpu) * maxFreq / 100L;
                }
                return freqs;
            }
        }
        // If <Win7 or anything failed in PDH/WMI, use the native call
        return queryNTPower(2); // Current is field index 2
    }

    @Override
    public long queryMaxFreq() {
        long[] freqs = queryNTPower(1); // Max is field index 1
        return Arrays.stream(freqs).max().orElse(-1L);
    }

    /**
     * Call CallNTPowerInformation for Processor information and return an array of
     * the specified index
     *
     * @param fieldIndex The field, in order as defined in the
     *                   {@link  PowrProf} structure.
     * @return The array of values.
     */
    private long[] queryNTPower(int fieldIndex) {
        org.aoju.bus.health.windows.PowrProf.ProcessorPowerInformation ppi = new org.aoju.bus.health.windows.PowrProf.ProcessorPowerInformation();
        long[] freqs = new long[getLogicalProcessorCount()];
        int bufferSize = ppi.size() * freqs.length;
        Memory mem = new Memory(bufferSize);
        if (0 != org.aoju.bus.health.windows.PowrProf.INSTANCE.CallNtPowerInformation(POWER_INFORMATION_LEVEL.ProcessorInformation, null, 0, mem,
                bufferSize)) {
            Logger.error("Unable to get Processor Information");
            Arrays.fill(freqs, -1L);
            return freqs;
        }
        for (int i = 0; i < freqs.length; i++) {
            ppi = new org.aoju.bus.health.windows.PowrProf.ProcessorPowerInformation(mem.share(i * (long) ppi.size()));
            if (fieldIndex == 1) { // Max
                freqs[i] = ppi.maxMhz * 1_000_000L;
            } else if (fieldIndex == 2) { // Current
                freqs[i] = ppi.currentMhz * 1_000_000L;
            } else {
                freqs[i] = -1L;
            }
        }
        return freqs;
    }

    @Override
    public double[] getSystemLoadAverage(int nelem) {
        if (nelem < 1 || nelem > 3) {
            throw new IllegalArgumentException("Must include from one to three elements.");
        }
        double[] average = new double[nelem];
        // Windows doesn't have load average
        for (int i = 0; i < average.length; i++) {
            average[i] = -1;
        }
        return average;
    }

    @Override
    public long[][] queryProcessorCpuLoadTicks() {
        List<String> instances;
        List<Long> systemList;
        List<Long> userList;
        List<Long> irqList;
        List<Long> softIrqList;
        List<Long> idleList;
        List<Long> systemUtility = null;
        List<Long> processorUtility = null;
        List<Long> processorUtilityBase = null;
        if (ProcessorInformation.USE_CPU_UTILITY) {
            Pair<List<String>, Map<ProcessorInformation.ProcessorCapacityTickCountProperty, List<Long>>> instanceValuePair = ProcessorInformation
                    .queryProcessorCapacityCounters();
            instances = instanceValuePair.getLeft();
            Map<ProcessorInformation.ProcessorCapacityTickCountProperty, List<Long>> valueMap = instanceValuePair.getRight();
            systemList = valueMap.get(ProcessorInformation.ProcessorCapacityTickCountProperty.PERCENTPRIVILEGEDTIME);
            userList = valueMap.get(ProcessorInformation.ProcessorCapacityTickCountProperty.PERCENTUSERTIME);
            irqList = valueMap.get(ProcessorInformation.ProcessorCapacityTickCountProperty.PERCENTINTERRUPTTIME);
            softIrqList = valueMap.get(ProcessorInformation.ProcessorCapacityTickCountProperty.PERCENTDPCTIME);
            // % Processor Time is actually Idle time
            idleList = valueMap.get(ProcessorInformation.ProcessorCapacityTickCountProperty.PERCENTPROCESSORTIME);
            // Utility ticks, if configured
            systemUtility = valueMap.get(ProcessorInformation.ProcessorCapacityTickCountProperty.PERCENTPRIVILEGEDUTILITY);
            processorUtility = valueMap.get(ProcessorInformation.ProcessorCapacityTickCountProperty.PERCENTPROCESSORUTILITY);
            processorUtilityBase = valueMap.get(ProcessorInformation.ProcessorCapacityTickCountProperty.PERCENTPROCESSORUTILITY_BASE);
        } else {
            Pair<List<String>, Map<ProcessorInformation.ProcessorTickCountProperty, List<Long>>> instanceValuePair = ProcessorInformation
                    .queryProcessorCounters();
            instances = instanceValuePair.getLeft();
            Map<ProcessorInformation.ProcessorTickCountProperty, List<Long>> valueMap = instanceValuePair.getRight();
            systemList = valueMap.get(ProcessorInformation.ProcessorTickCountProperty.PERCENTPRIVILEGEDTIME);
            userList = valueMap.get(ProcessorInformation.ProcessorTickCountProperty.PERCENTUSERTIME);
            irqList = valueMap.get(ProcessorInformation.ProcessorTickCountProperty.PERCENTINTERRUPTTIME);
            softIrqList = valueMap.get(ProcessorInformation.ProcessorTickCountProperty.PERCENTDPCTIME);
            // % Processor Time is actually Idle time
            idleList = valueMap.get(ProcessorInformation.ProcessorTickCountProperty.PERCENTPROCESSORTIME);
        }

        int ncpu = getLogicalProcessorCount();
        long[][] ticks = new long[ncpu][CentralProcessor.TickType.values().length];
        if (instances.isEmpty() || systemList == null || userList == null || irqList == null || softIrqList == null
                || idleList == null || (ProcessorInformation.USE_CPU_UTILITY
                && (systemUtility == null || processorUtility == null || processorUtilityBase == null))) {
            return ticks;
        }
        for (int p = 0; p < instances.size(); p++) {
            int cpu = instances.get(p).contains(",") ? numaNodeProcToLogicalProcMap.getOrDefault(instances.get(p), 0)
                    : Builder.parseIntOrDefault(instances.get(p), 0);
            if (cpu >= ncpu) {
                continue;
            }
            ticks[cpu][CentralProcessor.TickType.SYSTEM.getIndex()] = systemList.get(cpu);
            ticks[cpu][CentralProcessor.TickType.USER.getIndex()] = userList.get(cpu);
            ticks[cpu][CentralProcessor.TickType.IRQ.getIndex()] = irqList.get(cpu);
            ticks[cpu][CentralProcessor.TickType.SOFTIRQ.getIndex()] = softIrqList.get(cpu);
            ticks[cpu][CentralProcessor.TickType.IDLE.getIndex()] = idleList.get(cpu);

            // If users want Task Manager output we have to do some math to get there
            if (ProcessorInformation.USE_CPU_UTILITY) {
                // We'll be matching up percentage of elapsed total (base counter)
                // System currently includes IRQ and SOFTIRQ.
                long elapsedTime = ticks[cpu][CentralProcessor.TickType.SYSTEM.getIndex()] + ticks[cpu][CentralProcessor.TickType.USER.getIndex()]
                        + ticks[cpu][CentralProcessor.TickType.IDLE.getIndex()];

                // We have two new capacity numbers, processor (all but idle) and system
                // (included in processor). To further complicate matters, these are in percent
                // units so must be divided by 100.

                // Calculate Utility CPU usage since init
                long deltaBase = processorUtilityBase.get(cpu) - baseticks[cpu];

                if (deltaBase > 0) {
                    long deltaProc = processorUtility.get(cpu) - procticks[cpu];
                    long deltaSys = systemUtility.get(cpu) - privticks[cpu]; // subset of proc
                    double idlePercent = 1d - deltaProc / (100d * deltaBase);
                    double sysPercent = deltaSys / (100d * deltaBase);

                    // Apply to elapsed ticks
                    long deltaT = elapsedTime - elapsedticks[cpu];
                    long newIdle = idleticks[cpu] + Math.round(deltaT * idlePercent);
                    long newSystem = systicks[cpu] + Math.round(deltaT * sysPercent);

                    // Adjust idle to new, saving the delta
                    long delta = newIdle - ticks[cpu][CentralProcessor.TickType.IDLE.getIndex()];
                    ticks[cpu][CentralProcessor.TickType.IDLE.getIndex()] = newIdle;
                    // Do the same for system
                    delta += newSystem - ticks[cpu][CentralProcessor.TickType.SYSTEM.getIndex()];
                    ticks[cpu][CentralProcessor.TickType.SYSTEM.getIndex()] = newSystem;
                    // Subtract delta from user
                    ticks[cpu][CentralProcessor.TickType.USER.getIndex()] -= delta;
                }
            }

            // Decrement IRQ to avoid double counting in the total array
            ticks[cpu][CentralProcessor.TickType.SYSTEM.getIndex()] -= ticks[cpu][CentralProcessor.TickType.IRQ.getIndex()]
                    + ticks[cpu][CentralProcessor.TickType.SOFTIRQ.getIndex()];

            // Raw value is cumulative 100NS-ticks
            // Divide by 10_000 to get milliseconds
            ticks[cpu][CentralProcessor.TickType.SYSTEM.getIndex()] /= 10_000L;
            ticks[cpu][CentralProcessor.TickType.USER.getIndex()] /= 10_000L;
            ticks[cpu][CentralProcessor.TickType.IRQ.getIndex()] /= 10_000L;
            ticks[cpu][CentralProcessor.TickType.SOFTIRQ.getIndex()] /= 10_000L;
            ticks[cpu][CentralProcessor.TickType.IDLE.getIndex()] /= 10_000L;
        }
        // Skipping nice and IOWait, they'll stay 0
        return ticks;
    }

    @Override
    public long queryContextSwitches() {
        return SystemInformation.queryContextSwitchCounters().getOrDefault(SystemInformation.ContextSwitchProperty.CONTEXTSWITCHESPERSEC,
                0L);
    }

    @Override
    public long queryInterrupts() {
        return ProcessorInformation.queryInterruptCounters().getOrDefault(ProcessorInformation.InterruptsProperty.INTERRUPTSPERSEC, 0L);
    }

}
