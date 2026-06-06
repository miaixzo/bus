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
package org.miaixz.bus.health.unix.openbsd;

import java.util.List;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Executor;

/**
 * Reads from fstat.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class FstatKit {

    /**
     * Constructs a new FstatKit instance.
     */
    public FstatKit() {
        // No initialization required.
    }

    /**
     * Gets current working directory info (using {@code ps} actually).
     *
     * @param pid a process ID
     * @return the current working directory for that process.
     */
    public static String getCwd(int pid) {
        List<String> ps = Executor.runNative("ps -axwwo cwd -p " + pid);
        if (ps.size() > 1) {
            return ps.get(1);
        }
        return Normal.EMPTY;
    }

    /**
     * Gets open number of files.
     *
     * @param pid The process ID
     * @return the number of open files.
     */
    public static long getOpenFiles(int pid) {
        return parseOpenFiles(Executor.runNative("fstat -sp " + pid));
    }

    /**
     * Counts open file descriptor rows from {@code fstat -sp} output.
     *
     * @param fstatLines the fstat output lines
     * @return the number of open files
     */
    public static long parseOpenFiles(List<String> fstatLines) {
        long fd = 0L;
        for (String line : fstatLines) {
            String[] split = Pattern.SPACES_PATTERN.split(line.trim(), 11);
            if (split.length == 11 && !split[4].equals("pipe") && !split[4].equals("unix")) {
                fd++;
            }
        }
        // Subtract 1 for the header row, but never return a negative count.
        return fd > 0 ? fd - 1 : 0;
    }

}
