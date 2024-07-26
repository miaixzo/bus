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
package org.miaixz.bus.health.unix.platform.freebsd;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads from procstat into a map
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class ProcstatKit {

    /**
     * Gets a map containing current working directory info
     *
     * @param pid a process ID, optional
     * @return a map of process IDs to their current working directory. If {@code pid} is a negative number, all
     *         processes are returned; otherwise the map may contain only a single element for {@code pid}
     */
    public static Map<Integer, String> getCwdMap(int pid) {
        List<String> procstat = Executor.runNative("procstat -f " + (pid < 0 ? "-a" : pid));
        Map<Integer, String> cwdMap = new HashMap<>();
        for (String line : procstat) {
            String[] split = Pattern.SPACES_PATTERN.split(line.trim(), 10);
            if (split.length == 10 && split[2].equals("cwd")) {
                cwdMap.put(Parsing.parseIntOrDefault(split[0], -1), split[9]);
            }
        }
        return cwdMap;
    }

    /**
     * Gets current working directory info
     *
     * @param pid a process ID
     * @return the current working directory for that process.
     */
    public static String getCwd(int pid) {
        List<String> procstat = Executor.runNative("procstat -f " + pid);
        for (String line : procstat) {
            String[] split = Pattern.SPACES_PATTERN.split(line.trim(), 10);
            if (split.length == 10 && split[2].equals("cwd")) {
                return split[9];
            }
        }
        return Normal.EMPTY;
    }

    /**
     * Gets open files
     *
     * @param pid The process ID
     * @return the number of open files.
     */
    public static long getOpenFiles(int pid) {
        long fd = 0L;
        List<String> procstat = Executor.runNative("procstat -f " + pid);
        for (String line : procstat) {
            String[] split = Pattern.SPACES_PATTERN.split(line.trim(), 10);
            if (split.length == 10 && !"Vd-".contains(split[4])) {
                fd++;
            }
        }
        return fd;
    }

}
