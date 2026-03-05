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
package org.miaixz.bus.health.linux;

import java.io.File;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.exception.NotFoundException;
import org.miaixz.bus.health.Config;

/**
 * Provides constants for paths in the {@code /proc} filesystem on Linux. If the user desires to configure a custom
 * {@code /proc} path, it must be declared in the configuration file or updated in the {@link Config} class prior to
 * initializing this class.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class ProcPath {

    /**
     * The /proc filesystem location.
     */
    public static final String PROC = queryProcConfig();

    public static final String ASOUND = PROC + "/asound/";
    public static final String AUXV = PROC + "/self/auxv";
    public static final String CPUINFO = PROC + "/cpuinfo";
    public static final String DISKSTATS = PROC + "/diskstats";
    public static final String MEMINFO = PROC + "/meminfo";
    public static final String MODEL = PROC + "/device-tree/model";
    public static final String MOUNTS = PROC + "/mounts";
    public static final String NET = PROC + "/net";
    public static final String PID_CMDLINE = PROC + "/%d/cmdline";
    public static final String PID_CWD = PROC + "/%d/cwd";
    public static final String PID_EXE = PROC + "/%d/exe";
    public static final String PID_ENVIRON = PROC + "/%d/environ";
    public static final String PID_FD = PROC + "/%d/fd";
    public static final String PID_IO = PROC + "/%d/io";
    public static final String PID_STAT = PROC + "/%d/stat";
    public static final String PID_STATM = PROC + "/%d/statm";
    public static final String PID_STATUS = PROC + "/%d/status";
    public static final String SELF_STAT = PROC + "/self/stat";
    public static final String SNMP = NET + "/snmp";
    public static final String SNMP6 = NET + "/snmp6";
    public static final String STAT = PROC + "/stat";
    public static final String SYS_FS_FILE_NR = PROC + "/sys/fs/file-nr";
    public static final String SYS_FS_FILE_MAX = PROC + "/sys/fs/file-max";
    public static final String TASK_PATH = PROC + "/%d/task";
    public static final String TASK_COMM = TASK_PATH + "/%d/comm";
    public static final String TASK_STATUS = TASK_PATH + "/%d/status";
    public static final String TASK_STAT = TASK_PATH + "/%d/stat";
    public static final String THREAD_SELF = PROC + "/thread-self";
    public static final String UPTIME = PROC + "/uptime";
    public static final String VERSION = PROC + "/version";
    public static final String VMSTAT = PROC + "/vmstat";

    private static String queryProcConfig() {
        String procPath = Config.get(Config._UTIL_PROC_PATH, "/proc");
        // Ensure prefix begins with path separator, but doesn't end with one
        procPath = '/' + procPath.replaceAll("/$|^/", Normal.EMPTY);
        if (!new File(procPath).exists()) {
            throw new NotFoundException("The path does not exist " + Config._UTIL_PROC_PATH);
        }
        return procPath;
    }

}
