/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
package org.aoju.bus.health.common.linux;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Config;
import org.aoju.bus.health.Memoizer;
import org.aoju.bus.health.hardware.CentralProcessor;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Provides access to some /proc filesystem info on Linux
 *
 * @author Kimi Liu
 * @version 5.8.1
 * @since JDK 1.8+
 */
public class ProcUtils {

    /**
     * The proc path for CPU information
     */
    public static final String CPUINFO = "/cpuinfo";

    /**
     * The proc path for CPU statistics
     */
    public static final String STAT = "/stat";

    private static final Pattern DIGITS = Pattern.compile("\\d+");

    /**
     * The /proc filesystem location. Update hourly.
     */
    private static Supplier<String> proc = Memoizer.memoize(ProcUtils::queryProcConfig, TimeUnit.HOURS.toNanos(1));

    private ProcUtils() {
    }

    /**
     * The proc filesystem location may be customized to allow alternative proc
     * plugins, particularly useful for containers.
     *
     * @return The proc filesystem path, with a leading / but not a trailing one,
     * e.g., "/proc"
     */
    public static String getProcPath() {
        return proc.get();
    }

    private static String queryProcConfig() {
        String procPath = Config.get("oshi.util.proc.path", "/proc");
        // Ensure prefix begins with path separator, but doesn't end with one
        procPath = Symbol.C_SLASH + procPath.replaceAll("/$|^/", "");
        if (!new File(procPath).exists()) {
            throw new Config.PropertyException("oshi.util.proc.path", "The path does not exist");
        }
        return procPath;
    }

    /**
     * Parses the first value in /proc/uptime for seconds since boot
     *
     * @return Seconds since boot
     */
    public static double getSystemUptimeSeconds() {
        String uptime = Builder.getStringFromFile(getProcPath() + "/uptime");
        int spaceIndex = uptime.indexOf(Symbol.C_SPACE);
        try {
            if (spaceIndex < 0) {
                // No space, error
                return 0d;
            } else {
                return Double.parseDouble(uptime.substring(0, spaceIndex));
            }
        } catch (NumberFormatException nfe) {
            return 0d;
        }
    }

    /**
     * Gets the CPU ticks array from /proc/stat
     *
     * @return Array of CPU ticks
     */
    public static long[] readSystemCpuLoadTicks() {
        long[] ticks = new long[CentralProcessor.TickType.values().length];
        // /proc/stat expected format
        // first line is overall user,nice,system,idle,iowait,irq, etc.
        // cpu 3357 0 4313 1362393 ...
        String tickStr;
        List<String> procStat = Builder.readFile(getProcPath() + STAT);
        if (!procStat.isEmpty()) {
            tickStr = procStat.get(0);
        } else {
            return ticks;
        }
        // Split the line. Note the first (0) element is "cpu" so remaining
        // elements are offset by 1 from the enum index
        String[] tickArr = Builder.whitespaces.split(tickStr);
        if (tickArr.length <= CentralProcessor.TickType.IDLE.getIndex()) {
            // If ticks don't at least go user/nice/system/idle, abort
            return ticks;
        }
        // Note tickArr is offset by 1 because first element is "cpu"
        for (int i = 0; i < CentralProcessor.TickType.values().length; i++) {
            ticks[i] = Builder.parseLongOrDefault(tickArr[i + 1], 0L);
        }
        // Ignore guest or guest_nice, they are included in user/nice
        return ticks;
    }

    /**
     * Gets an array of files in the /proc directory with only numeric digit
     * filenames, corresponding to processes
     *
     * @return An array of File objects for the process files
     */
    public static File[] getPidFiles() {
        File procdir = new File(getProcPath());
        File[] pids = procdir.listFiles(f -> DIGITS.matcher(f.getName()).matches());
        return pids != null ? pids : new File[0];
    }
}
