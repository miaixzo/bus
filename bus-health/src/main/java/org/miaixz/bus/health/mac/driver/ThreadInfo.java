/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.               ~
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
