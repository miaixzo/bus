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
 * 提供对Linux上某些/proc文件系统信息的访问
 *
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
public class ProcUtils {

    /**
     * CPU信息的进程路径
     */
    public static final String CPUINFO = "/cpuinfo";

    /**
     * 用于CPU统计信息的proc路径
     */
    public static final String STAT = "/stat";

    private static final Pattern DIGITS = Pattern.compile("\\d+");

    /**
     * proc文件系统位置。每小时更新
     */
    private static Supplier<String> proc = Memoizer.memoize(ProcUtils::queryProcConfig, TimeUnit.HOURS.toNanos(1));

    private ProcUtils() {
    }

    /**
     * 可以对proc文件系统位置进行定制，以允许使用其他proc插件，对于容器尤其有用
     *
     * @return proc文件系统路径，前面有/但后面没有，例如，“/proc”
     */
    public static String getProcPath() {
        return proc.get();
    }

    private static String queryProcConfig() {
        String procPath = Config.get("oshi.util.proc.path", "/proc");
        // 确保前缀以路径分隔符开始，但不以1结尾
        procPath = Symbol.C_SLASH + procPath.replaceAll("/$|^/", "");
        if (!new File(procPath).exists()) {
            throw new Config.PropertyException("oshi.util.proc.path", "The path does not exist");
        }
        return procPath;
    }

    /**
     * 将/proc/正常运行时间中的第一个值解析为启动后的秒数
     *
     * @return 秒后启动
     */
    public static double getSystemUptimeSeconds() {
        String uptime = Builder.getStringFromFile(getProcPath() + "/uptime");
        int spaceIndex = uptime.indexOf(Symbol.C_SPACE);
        try {
            if (spaceIndex < 0) {
                // 没有空间,错误
                return 0d;
            } else {
                return Double.parseDouble(uptime.substring(0, spaceIndex));
            }
        } catch (NumberFormatException nfe) {
            return 0d;
        }
    }

    /**
     * 从/proc/stat获取CPU时钟阵列
     *
     * @return CPU时钟阵列
     */
    public static long[] readSystemCpuLoadTicks() {
        long[] ticks = new long[CentralProcessor.TickType.values().length];
        // /proc/stat 预期的格式
        // 第一行是总体用户、nice、系统、idle、iowait、irq等
        // cpu 3357 0 4313 1362393 ...
        String tickStr;
        List<String> procStat = Builder.readFile(getProcPath() + STAT);
        if (!procStat.isEmpty()) {
            tickStr = procStat.get(0);
        } else {
            return ticks;
        }
        //  第一个(0)元素是“cpu”，因此其余元素被enum索引中的1所偏移
        String[] tickArr = Builder.whitespaces.split(tickStr);
        if (tickArr.length <= CentralProcessor.TickType.IDLE.getIndex()) {
            return ticks;
        }
        // 注意，tickArr的偏移量为1，因为第一个元素是“cpu”
        for (int i = 0; i < CentralProcessor.TickType.values().length; i++) {
            ticks[i] = Builder.parseLongOrDefault(tickArr[i + 1], 0L);
        }
        return ticks;
    }

    /**
     * 获取/proc目录中的文件数组，其中只有与进程对应的数字文件名
     *
     * @return 进程文件的文件对象数组
     */
    public static File[] getPidFiles() {
        File procdir = new File(getProcPath());
        File[] pids = procdir.listFiles(f -> DIGITS.matcher(f.getName()).matches());
        return pids != null ? pids : new File[0];
    }

}
