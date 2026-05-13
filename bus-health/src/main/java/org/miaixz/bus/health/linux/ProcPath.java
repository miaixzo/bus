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
 * @since Java 21+
 */
@ThreadSafe
public final class ProcPath {

    /**
     * The /proc filesystem location.
     */
    public static final String PROC = queryProcConfig();

    /**
     * The ASOUND constant.
     */
    public static final String ASOUND = PROC + "/asound/";

    /**
     * The AUXV constant.
     */
    public static final String AUXV = PROC + "/self/auxv";

    /**
     * The CPUINFO constant.
     */
    public static final String CPUINFO = PROC + "/cpuinfo";

    /**
     * The DISKSTATS constant.
     */
    public static final String DISKSTATS = PROC + "/diskstats";

    /**
     * The LOADAVG constant.
     */
    public static final String LOADAVG = PROC + "/loadavg";

    /**
     * The MEMINFO constant.
     */
    public static final String MEMINFO = PROC + "/meminfo";

    /**
     * The MODEL constant.
     */
    public static final String MODEL = PROC + "/device-tree/model";

    /**
     * The MOUNTS constant.
     */
    public static final String MOUNTS = PROC + "/mounts";

    /**
     * The NET constant.
     */
    public static final String NET = PROC + "/net";

    /**
     * The PID_CMDLINE constant.
     */
    public static final String PID_CMDLINE = PROC + "/%d/cmdline";

    /**
     * The PID_CWD constant.
     */
    public static final String PID_CWD = PROC + "/%d/cwd";

    /**
     * The PID_EXE constant.
     */
    public static final String PID_EXE = PROC + "/%d/exe";

    /**
     * The PID_ENVIRON constant.
     */
    public static final String PID_ENVIRON = PROC + "/%d/environ";

    /**
     * The PID_FD constant.
     */
    public static final String PID_FD = PROC + "/%d/fd";

    /**
     * The PID_IO constant.
     */
    public static final String PID_IO = PROC + "/%d/io";

    /**
     * The PID_STAT constant.
     */
    public static final String PID_STAT = PROC + "/%d/stat";

    /**
     * The PID_STATM constant.
     */
    public static final String PID_STATM = PROC + "/%d/statm";

    /**
     * The PID_STATUS constant.
     */
    public static final String PID_STATUS = PROC + "/%d/status";

    /**
     * The SELF_STAT constant.
     */
    public static final String SELF_STAT = PROC + "/self/stat";

    /**
     * The SNMP constant.
     */
    public static final String SNMP = NET + "/snmp";

    /**
     * The SNMP6 constant.
     */
    public static final String SNMP6 = NET + "/snmp6";

    /**
     * The STAT constant.
     */
    public static final String STAT = PROC + "/stat";

    /**
     * The SYS_FS_FILE_NR constant.
     */
    public static final String SYS_FS_FILE_NR = PROC + "/sys/fs/file-nr";

    /**
     * The SYS_FS_FILE_MAX constant.
     */
    public static final String SYS_FS_FILE_MAX = PROC + "/sys/fs/file-max";

    /**
     * The TASK_PATH constant.
     */
    public static final String TASK_PATH = PROC + "/%d/task";

    /**
     * The TASK_COMM constant.
     */
    public static final String TASK_COMM = TASK_PATH + "/%d/comm";

    /**
     * The TASK_STATUS constant.
     */
    public static final String TASK_STATUS = TASK_PATH + "/%d/status";

    /**
     * The TASK_STAT constant.
     */
    public static final String TASK_STAT = TASK_PATH + "/%d/stat";

    /**
     * The THREAD_SELF constant.
     */
    public static final String THREAD_SELF = PROC + "/thread-self";

    /**
     * The UPTIME constant.
     */
    public static final String UPTIME = PROC + "/uptime";

    /**
     * The VERSION constant.
     */
    public static final String VERSION = PROC + "/version";

    /**
     * The VMSTAT constant.
     */
    public static final String VMSTAT = PROC + "/vmstat";

    /**
     * The SELF_CGROUP constant.
     */
    public static final String SELF_CGROUP = PROC + "/self/cgroup";

    /**
     * The FILESYSTEMS constant.
     */
    public static final String FILESYSTEMS = PROC + "/filesystems";

    /**
     * Queries the proc config.
     *
     * @return the query proc config result
     */
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
