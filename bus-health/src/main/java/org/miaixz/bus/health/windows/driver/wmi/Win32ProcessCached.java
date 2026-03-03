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
package org.miaixz.bus.health.windows.driver.wmi;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.GuardedBy;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.windows.WmiKit;

import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;

/**
 * Utility to query WMI class {@code Win32_Process} using cache
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class Win32ProcessCached {

    private static final Supplier<Win32ProcessCached> INSTANCE = Memoizer.memoize(Win32ProcessCached::createInstance);

    // Use a map to cache command line queries
    @GuardedBy("commandLineCacheLock")
    private final Map<Integer, Pair<Long, String>> commandLineCache = new HashMap<>();
    private final ReentrantLock commandLineCacheLock = new ReentrantLock();

    /**
     * Get the singleton instance of this class, instantiating the map which caches command lines.
     *
     * @return the singleton instance
     */
    public static Win32ProcessCached getInstance() {
        return INSTANCE.get();
    }

    private static Win32ProcessCached createInstance() {
        return new Win32ProcessCached();
    }

    /**
     * Gets the process command line, while also querying and caching command lines for all running processes if the
     * specified process is not in the cache.
     * <p>
     * When iterating over a process list, the WMI overhead of querying each single command line can quickly exceed the
     * time it takes to query all command lines. This method permits access to cached queries from a previous call,
     * significantly improving aggregate performance.
     *
     * @param processId The process ID for which to return the command line.
     * @param startTime The start time of the process, in milliseconds since the 1970 epoch. If this start time is after
     *                  the time this process was previously queried, the prior entry will be deemed invalid and the
     *                  cache refreshed.
     * @return The command line of the specified process. If the command line is cached from a previous call and the
     *         start time is prior to the time it was cached, this method will quickly return the cached value.
     *         Otherwise, will refresh the cache with all running processes prior to returning, which may incur some
     *         latency.
     *         <p>
     *         May return a command line from the cache even after a process has terminated. Otherwise will return the
     *         empty string.
     */
    public String getCommandLine(int processId, long startTime) {
        // We could use synchronized method but this is more clear
        commandLineCacheLock.lock();
        try {
            // See if this process is in the cache already
            Pair<Long, String> pair = commandLineCache.get(processId);
            // Valid process must have been started before map insertion
            if (pair != null && startTime < pair.getLeft()) {
                // Entry is valid, return it!
                return pair.getRight();
            } else {
                // Invalid entry, rebuild cache
                // Processes started after this time will be invalid
                long now = System.currentTimeMillis();
                // Gets all processes. Takes ~200ms
                WmiResult<Win32Process.CommandLineProperty> commandLineAllProcs = Win32Process.queryCommandLines(null);
                // Stale processes use resources. Periodically clear before building
                // Use a threshold of map size > 2x # of processes
                if (commandLineCache.size() > commandLineAllProcs.getResultCount() * 2) {
                    commandLineCache.clear();
                }
                // Iterate results and put in map. Save value for current PID along the way
                String result = Normal.EMPTY;
                for (int i = 0; i < commandLineAllProcs.getResultCount(); i++) {
                    int pid = WmiKit.getUint32(commandLineAllProcs, Win32Process.CommandLineProperty.PROCESSID, i);
                    String cl = WmiKit.getString(commandLineAllProcs, Win32Process.CommandLineProperty.COMMANDLINE, i);
                    commandLineCache.put(pid, Pair.of(now, cl));
                    if (pid == processId) {
                        result = cl;
                    }
                }
                return result;
            }
        } finally {
            commandLineCacheLock.unlock();
        }
    }

}
