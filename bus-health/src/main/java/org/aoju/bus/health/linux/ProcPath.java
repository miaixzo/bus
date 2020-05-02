/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org OSHI and other contributors.                 *
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
 ********************************************************************************/
package org.aoju.bus.health.linux;

import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.health.Config;

import java.io.File;

/**
 * 提供对Linux上某些/proc文件系统信息的访问
 *
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
@ThreadSafe
public final class ProcPath {

    public static final String PROC = queryProcConfig();

    public static final String ASOUND = PROC + "/asound/";
    public static final String CPUINFO = PROC + "/cpuinfo";
    public static final String DISKSTATS = PROC + "/diskstats";
    public static final String MEMINFO = PROC + "/meminfo";
    public static final String MOUNTS = PROC + "/mounts";
    public static final String PID_CMDLINE = PROC + "/%d/cmdline";
    public static final String PID_CWD = PROC + "/%d/cwd";
    public static final String PID_EXE = PROC + "/%d/exe";
    public static final String PID_FD = PROC + "/%d/fd";
    public static final String PID_IO = PROC + "/%d/io";
    public static final String PID_STAT = PROC + "/%d/stat";
    public static final String PID_STATUS = PROC + "/%d/status";
    public static final String SELF_STAT = PROC + "/self/stat";
    public static final String STAT = PROC + "/stat";
    public static final String SYS_FS_FILE_NR = PROC + "/sys/fs/file-nr";
    public static final String UPTIME = PROC + "/uptime";
    public static final String VERSION = PROC + "/version";
    public static final String VMSTAT = PROC + "/vmstat";

    private ProcPath() {
    }

    private static String queryProcConfig() {
        String procPath = Config.get("health.proc.path", "/proc");
        // Ensure prefix begins with path separator, but doesn't end with one
        procPath = Symbol.C_SLASH + procPath.replaceAll("/$|^/", Normal.EMPTY);
        if (!new File(procPath).exists()) {
            throw new Config.PropertyException("health.proc.path", "The path does not exist");
        }
        return procPath;
    }

}
