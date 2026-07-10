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
package org.miaixz.bus.health.windows.driver.registry;

import java.util.*;

import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.windows.driver.perfmon.PerfmonDisabled;
import org.miaixz.bus.health.windows.driver.perfmon.ThreadInformation;
import org.miaixz.bus.health.windows.driver.perfmon.ThreadInformation.ThreadPerformanceProperty;

/**
 * Utility to read thread data from HKEY_PERFORMANCE_DATA information with backup from Performance Counters or WMI
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class ThreadPerformanceData {

    /**
     * Prevents instantiation of utility class.
     */
    private ThreadPerformanceData() {
        // No initialization required.
    }

    /**
     * The THREAD constant.
     */
    public static final String THREAD = "Thread";

    /**
     * Query the registry for thread performance counters
     *
     * @param pids An optional collection of thread IDs to filter the list to. May be null for no filtering.
     * @return A map with Thread ID as the key and a {@link PerfCounterBlock} object populated with performance counter
     *         information if successful, or null otherwise.
     */
    public static Map<Integer, PerfCounterBlock> buildThreadMapFromRegistry(Collection<Integer> pids) {
        // Grab the data from the registry.
        Triplet<List<Map<ThreadPerformanceProperty, Object>>, Long, Long> threadData = HkeyPerformanceDataKit
                .readPerfDataFromRegistry(THREAD, ThreadPerformanceProperty.class);
        if (threadData == null) {
            return null;
        }
        List<Map<ThreadPerformanceProperty, Object>> threadInstanceMaps = threadData.getLeft();
        long perfTime100nSec = threadData.getMiddle(); // 1601
        long now = threadData.getRight(); // 1970 epoch

        // Create a map and fill it
        Map<Integer, PerfCounterBlock> threadMap = new HashMap<>();
        // Iterate instances.
        for (Map<ThreadPerformanceProperty, Object> threadInstanceMap : threadInstanceMaps) {
            Integer pid = ((Integer) threadInstanceMap.get(ThreadPerformanceProperty.IDPROCESS)).intValue();
            if ((pids == null || pids.contains(pid)) && pid > 0) {
                int tid = ((Integer) threadInstanceMap.get(ThreadPerformanceProperty.IDTHREAD)).intValue();
                String name = (String) threadInstanceMap.get(ThreadPerformanceProperty.NAME);
                long upTime = (perfTime100nSec - (Long) threadInstanceMap.get(ThreadPerformanceProperty.ELAPSEDTIME))
                        / 10_000L;
                if (upTime < 1) {
                    upTime = 1;
                }
                long user = ((Long) threadInstanceMap.get(ThreadPerformanceProperty.PERCENTUSERTIME)).longValue()
                        / 10_000L;
                long kernel = ((Long) threadInstanceMap.get(ThreadPerformanceProperty.PERCENTPRIVILEGEDTIME))
                        .longValue() / 10_000L;
                int priority = ((Integer) threadInstanceMap.get(ThreadPerformanceProperty.PRIORITYCURRENT)).intValue();
                int threadState = ((Integer) threadInstanceMap.get(ThreadPerformanceProperty.THREADSTATE)).intValue();
                int threadWaitReason = ((Integer) threadInstanceMap.get(ThreadPerformanceProperty.THREADWAITREASON))
                        .intValue();
                // Start address is pointer sized when fetched from registry, so this could be
                // either Integer (uint32) or Long depending on OS bitness
                Object addr = threadInstanceMap.get(ThreadPerformanceProperty.STARTADDRESS);
                long startAddr = addr.getClass().equals(Long.class) ? (Long) addr
                        : Integer.toUnsignedLong((Integer) addr);
                long contextSwitches = Integer.toUnsignedLong(
                        (Integer) threadInstanceMap.get(ThreadPerformanceProperty.CONTEXTSWITCHESPERSEC));
                threadMap.put(
                        tid,
                        new PerfCounterBlock(name, tid, pid, now - upTime, user, kernel, priority, threadState,
                                threadWaitReason, startAddr, contextSwitches));
            }
        }
        return threadMap;
    }

    /**
     * Query PerfMon for thread performance counters
     *
     * @param pids An optional collection of process IDs to filter the list to. May be null for no filtering.
     * @return A map with Thread ID as the key and a {@link PerfCounterBlock} object populated with performance counter
     *         information.
     */
    public static Map<Integer, PerfCounterBlock> buildThreadMapFromPerfCounters(Collection<Integer> pids) {
        return buildThreadMapFromPerfCounters(pids, null, -1);
    }

    /**
     * Query PerfMon for thread performance counters
     *
     * @param pids      An optional collection of process IDs to filter the list to. May be null for no filtering.
     * @param procName  Limit the matches to processes matching the given name.
     * @param threadNum Limit the matches to threads matching the given thread. Use -1 to match all threads.
     * @return A map with Thread ID as the key and a {@link PerfCounterBlock} object populated with performance counter
     *         information.
     */
    public static Map<Integer, PerfCounterBlock> buildThreadMapFromPerfCounters(
            Collection<Integer> pids,
            String procName,
            int threadNum) {
        if (PerfmonDisabled.PERF_PROC_DISABLED) {
            return Collections.emptyMap();
        }
        Map<Integer, PerfCounterBlock> threadMap = new HashMap<>();
        Pair<List<String>, Map<ThreadPerformanceProperty, List<Long>>> instanceValues = StringKit.isBlank(procName)
                ? ThreadInformation.queryThreadCounters()
                : ThreadInformation.queryThreadCounters(procName, threadNum);
        if (instanceValues == null) {
            return null;
        }
        long now = System.currentTimeMillis(); // 1970 epoch
        List<String> instances = instanceValues.getLeft();
        Map<ThreadPerformanceProperty, List<Long>> valueMap = instanceValues.getRight();
        List<Long> tidList = valueMap.get(ThreadPerformanceProperty.IDTHREAD);
        List<Long> pidList = valueMap.get(ThreadPerformanceProperty.IDPROCESS);
        List<Long> userList = valueMap.get(ThreadPerformanceProperty.PERCENTUSERTIME); // 100-nsec
        List<Long> kernelList = valueMap.get(ThreadPerformanceProperty.PERCENTPRIVILEGEDTIME); // 100-nsec
        List<Long> startTimeList = valueMap.get(ThreadPerformanceProperty.ELAPSEDTIME); // filetime
        List<Long> priorityList = valueMap.get(ThreadPerformanceProperty.PRIORITYCURRENT);
        List<Long> stateList = valueMap.get(ThreadPerformanceProperty.THREADSTATE);
        List<Long> waitReasonList = valueMap.get(ThreadPerformanceProperty.THREADWAITREASON);
        List<Long> startAddrList = valueMap.get(ThreadPerformanceProperty.STARTADDRESS);
        List<Long> contextSwitchesList = valueMap.get(ThreadPerformanceProperty.CONTEXTSWITCHESPERSEC);

        int nameIndex = 0;
        for (int inst = 0; inst < instances.size(); inst++) {
            int pid = pidList.get(inst).intValue();
            if (pids == null || pids.contains(pid)) {
                int tid = tidList.get(inst).intValue();
                String name = Integer.toString(nameIndex++);
                long startTime = startTimeList.get(inst);
                startTime = Parsing.filetimeToUtcMs(startTime, false);
                if (startTime > now) {
                    startTime = now - 1;
                }
                long user = userList.get(inst) / 10_000L;
                long kernel = kernelList.get(inst) / 10_000L;
                int priority = priorityList.get(inst).intValue();
                int threadState = stateList.get(inst).intValue();
                int threadWaitReason = waitReasonList.get(inst).intValue();
                long startAddr = startAddrList.get(inst).longValue();
                long contextSwitches = contextSwitchesList.get(inst).longValue();

                // if creation time value is less than current millis, it's in 1970 epoch,
                // otherwise it's 1601 epoch and we must convert
                threadMap.put(
                        tid,
                        new PerfCounterBlock(name, tid, pid, startTime, user, kernel, priority, threadState,
                                threadWaitReason, startAddr, contextSwitches));
            }
        }
        return threadMap;
    }

    /**
     * Class to encapsulate data from the registry performance counter block
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @Immutable
    public static class PerfCounterBlock {

        /**
         * The name value.
         */
        private final String name;

        /**
         * The threadID value.
         */
        private final int threadID;

        /**
         * The owningProcessID value.
         */
        private final int owningProcessID;

        /**
         * The startTime value.
         */
        private final long startTime;

        /**
         * The userTime value.
         */
        private final long userTime;

        /**
         * The kernelTime value.
         */
        private final long kernelTime;

        /**
         * The priority value.
         */
        private final int priority;

        /**
         * The threadState value.
         */
        private final int threadState;

        /**
         * The threadWaitReason value.
         */
        private final int threadWaitReason;

        /**
         * The startAddress value.
         */
        private final long startAddress;

        /**
         * The contextSwitches value.
         */
        private final long contextSwitches;

        /**
         * Creates a new PerfCounterBlock instance.
         *
         * @param name             the name
         * @param threadID         the thread id
         * @param owningProcessID  the owning process id
         * @param startTime        the start time
         * @param userTime         the user time
         * @param kernelTime       the kernel time
         * @param priority         the priority
         * @param threadState      the thread state
         * @param threadWaitReason the thread wait reason
         * @param startAddress     the start address
         * @param contextSwitches  the context switches
         */
        public PerfCounterBlock(String name, int threadID, int owningProcessID, long startTime, long userTime,
                long kernelTime, int priority, int threadState, int threadWaitReason, long startAddress,
                long contextSwitches) {
            this.name = name;
            this.threadID = threadID;
            this.owningProcessID = owningProcessID;
            this.startTime = startTime;
            this.userTime = userTime;
            this.kernelTime = kernelTime;
            this.priority = priority;
            this.threadState = threadState;
            this.threadWaitReason = threadWaitReason;
            this.startAddress = startAddress;
            this.contextSwitches = contextSwitches;
        }

        /**
         * Returns the thread name.
         *
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the thread identifier.
         *
         * @return the threadID
         */
        public int getThreadID() {
            return threadID;
        }

        /**
         * Returns the owning process identifier.
         *
         * @return the owningProcessID
         */
        public int getOwningProcessID() {
            return owningProcessID;
        }

        /**
         * Returns the thread start time.
         *
         * @return the startTime
         */
        public long getStartTime() {
            return startTime;
        }

        /**
         * Returns the user time.
         *
         * @return the userTime
         */
        public long getUserTime() {
            return userTime;
        }

        /**
         * Returns the kernel time.
         *
         * @return the kernelTime
         */
        public long getKernelTime() {
            return kernelTime;
        }

        /**
         * Returns the priority.
         *
         * @return the priority
         */
        public int getPriority() {
            return priority;
        }

        /**
         * Returns the thread state.
         *
         * @return the threadState
         */
        public int getThreadState() {
            return threadState;
        }

        /**
         * Returns the thread wait reason.
         *
         * @return the threadWaitReason
         */
        public int getThreadWaitReason() {
            return threadWaitReason;
        }

        /**
         * Returns the start memory address.
         *
         * @return the startMemoryAddress
         */
        public long getStartAddress() {
            return startAddress;
        }

        /**
         * Returns the context switches.
         *
         * @return the contextSwitches
         */
        public long getContextSwitches() {
            return contextSwitches;
        }

    }

}
