/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.               ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.health.mac.driver;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.software.OSProcess;

/**
 * Utility to query threads for a process on macOS.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class ThreadInfo {

    /**
     * Pattern for parsing `ps -awwxM` output for thread information. Groups: 1: PID 2: CPU usage 3: State 4: Priority
     * 5: System time 6: User time
     */
    private static final Pattern PS_M = Pattern.compile(
            "\\D+(\\d+).+(\\d+\\.\\d)\\s+(\\w)\\s+(\\d+)\\D+(\\d+:\\d{2}\\.\\d{2})\\s+(\\d+:\\d{2}\\.\\d{2}).+");

    /**
     * Queries thread statistics for a given process ID.
     *
     * @param pid The process ID to query.
     * @return A list of {@link ThreadStats} objects for the threads of the specified process.
     */
    public static List<ThreadStats> queryTaskThreads(int pid) {
        String pidStr = Symbol.SPACE + pid + Symbol.SPACE;
        List<ThreadStats> taskThreads = new ArrayList<>();
        // Only way to get thread info without root permissions
        // Using the M switch gives all threads with no possibility to filter
        List<String> psThread = Executor.runNative("ps -awwxM").stream().filter(s -> s.contains(pidStr))
                .collect(Collectors.toList());
        int tid = 0;
        for (String thread : psThread) {
            Matcher m = PS_M.matcher(thread);
            if (m.matches() && pid == Parsing.parseIntOrDefault(m.group(1), -1)) {
                double cpu = Parsing.parseDoubleOrDefault(m.group(2), 0d);
                char state = m.group(3).charAt(0);
                int pri = Parsing.parseIntOrDefault(m.group(4), 0);
                long sTime = Parsing.parseDHMSOrDefault(m.group(5), 0L);
                long uTime = Parsing.parseDHMSOrDefault(m.group(6), 0L);
                taskThreads.add(new ThreadStats(tid++, cpu, state, sTime, uTime, pri));
            }
        }
        return taskThreads;
    }

    /**
     * Class to encapsulate mach thread information.
     */
    @Immutable
    public static class ThreadStats {

        private final int threadId;
        private final long userTime;
        private final long systemTime;
        private final long upTime;
        private final OSProcess.State state;
        private final int priority;

        /**
         * Constructs a {@code ThreadStats} object.
         *
         * @param tid   The thread ID.
         * @param cpu   The CPU usage of the thread.
         * @param state The state of the thread (e.g., 'R' for running, 'S' for sleeping).
         * @param sTime The system time used by the thread.
         * @param uTime The user time used by the thread.
         * @param pri   The priority of the thread.
         */
        public ThreadStats(int tid, double cpu, char state, long sTime, long uTime, int pri) {
            this.threadId = tid;
            this.userTime = uTime;
            this.systemTime = sTime;
            // user + system / uptime = cpu/100
            // so: uptime = user+system / cpu/100
            this.upTime = (long) ((uTime + sTime) / (cpu / 100d + 0.0005));
            switch (state) {
                case 'I':
                case 'S':
                    this.state = OSProcess.State.SLEEPING;
                    break;

                case 'U':
                    this.state = OSProcess.State.WAITING;
                    break;

                case 'R':
                    this.state = OSProcess.State.RUNNING;
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
            this.priority = pri;
        }

        /**
         * Gets the thread ID.
         *
         * @return The thread ID.
         */
        public int getThreadId() {
            return threadId;
        }

        /**
         * Gets the user time used by the thread.
         *
         * @return The user time.
         */
        public long getUserTime() {
            return userTime;
        }

        /**
         * Gets the system time used by the thread.
         *
         * @return The system time.
         */
        public long getSystemTime() {
            return systemTime;
        }

        /**
         * Gets the uptime of the thread.
         *
         * @return The uptime.
         */
        public long getUpTime() {
            return upTime;
        }

        /**
         * Gets the state of the thread.
         *
         * @return The state.
         */
        public OSProcess.State getState() {
            return state;
        }

        /**
         * Gets the priority of the thread.
         *
         * @return The priority.
         */
        public int getPriority() {
            return priority;
        }
    }

}
