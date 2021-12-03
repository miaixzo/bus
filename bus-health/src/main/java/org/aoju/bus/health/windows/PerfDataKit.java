/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org OSHI and other contributors.                 *
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
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.health.windows;

import com.sun.jna.platform.win32.BaseTSD.DWORD_PTR;
import com.sun.jna.platform.win32.Pdh;
import com.sun.jna.platform.win32.Pdh.PDH_RAW_COUNTER;
import com.sun.jna.platform.win32.PdhMsg;
import com.sun.jna.platform.win32.VersionHelpers;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.DWORDByReference;
import com.sun.jna.platform.win32.WinDef.LONGLONGByReference;
import com.sun.jna.platform.win32.WinError;
import org.aoju.bus.core.annotation.Immutable;
import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Formats;
import org.aoju.bus.logger.Logger;

/**
 * 帮助类来集中PDH计数器设置的样板部分，并允许应用程序轻松地添加、查询和删除计数器
 *
 * @author Kimi Liu
 * @version 6.3.1
 * @since JDK 1.8+
 */
@ThreadSafe
public final class PerfDataKit {

    private static final DWORD_PTR PZERO = new DWORD_PTR(0);
    private static final DWORDByReference PDH_FMT_RAW = new DWORDByReference(new DWORD(Pdh.PDH_FMT_RAW));
    private static final Pdh PDH = Pdh.INSTANCE;

    private static final boolean IS_VISTA_OR_GREATER = VersionHelpers.IsWindowsVistaOrGreater();

    private PerfDataKit() {
    }

    /**
     * 创建一个性能计数器
     *
     * @param object   计数器的对象/路径
     * @param instance 计数器的实例，如果没有实例则为空
     * @param counter  计数器的名称
     * @return 封装对象、实例和计数器的PerfCounter对象
     */
    public static PerfCounter createCounter(String object, String instance, String counter) {
        return new PerfCounter(object, instance, counter);
    }

    /**
     * 更新查询并获取时间戳
     *
     * @param query 要更新所有计数器的查询
     * @return 查询中第一个计数器的更新时间戳
     */
    public static long updateQueryTimestamp(WinNT.HANDLEByReference query) {
        LONGLONGByReference pllTimeStamp = new LONGLONGByReference();
        int ret = IS_VISTA_OR_GREATER ? PDH.PdhCollectQueryDataWithTime(query.getValue(), pllTimeStamp)
                : PDH.PdhCollectQueryData(query.getValue());
        // 由于竞争条件，PDH_NO_DATA初始更新可能失败
        int retries = 0;
        while (ret == PdhMsg.PDH_NO_DATA && retries++ < 3) {
            // 指数后退
            Builder.sleep(1 << retries);
            ret = IS_VISTA_OR_GREATER ? PDH.PdhCollectQueryDataWithTime(query.getValue(), pllTimeStamp)
                    : PDH.PdhCollectQueryData(query.getValue());
        }
        if (ret != WinError.ERROR_SUCCESS) {
            if (Logger.get().isWarn()) {
                Logger.warn("Failed to update counter. Error code: {}", String.format(Formats.formatError(ret)));
            }
            return 0L;
        }
        // Perf计数器时间戳是本地时间
        return IS_VISTA_OR_GREATER ? Builder.filetimeToUtcMs(pllTimeStamp.getValue().longValue(), true)
                : System.currentTimeMillis();
    }

    /**
     * 打开一个pdh查询
     *
     * @param q 指向查询的指针
     * @return 如果成功, 则为true
     */
    public static boolean openQuery(WinNT.HANDLEByReference q) {
        int ret = PDH.PdhOpenQuery(null, PZERO, q);
        if (ret != WinError.ERROR_SUCCESS) {
            if (Logger.get().isError()) {
                Logger.error("Failed to open PDH Query. Error code: {}", String.format(Formats.formatError(ret)));
            }
            return false;
        }
        return true;
    }

    /**
     * 关闭一个pdh查询
     *
     * @param q 指向查询的指针
     * @return 如果成功, 则为true
     */
    public static boolean closeQuery(WinNT.HANDLEByReference q) {
        return WinError.ERROR_SUCCESS == PDH.PdhCloseQuery(q.getValue());
    }

    /**
     * 获取pdh计数器的值
     *
     * @param counter 计数器得到的值
     * @return 数器的长整型值，或表示错误代码的负值
     */
    public static long queryCounter(WinNT.HANDLEByReference counter) {
        PDH_RAW_COUNTER counterValue = new PDH_RAW_COUNTER();
        int ret = PDH.PdhGetRawCounterValue(counter.getValue(), PDH_FMT_RAW, counterValue);
        if (ret != WinError.ERROR_SUCCESS) {
            if (Logger.get().isWarn()) {
                Logger.warn("Failed to get counter. Error code: {}", String.format(Formats.formatError(ret)));
            }
            return ret;
        }
        return counterValue.FirstValue;
    }

    /**
     * 向查询添加pdh计数器
     *
     * @param query 指向要添加计数器的查询的指针
     * @param path  PerfMon计数器的字符串名称
     * @param p     指向计数器的指针
     * @return 如果成功, 则为true
     */
    public static boolean addCounter(WinNT.HANDLEByReference query, String path, WinNT.HANDLEByReference p) {
        int ret = IS_VISTA_OR_GREATER ? PDH.PdhAddEnglishCounter(query.getValue(), path, PZERO, p)
                : PDH.PdhAddCounter(query.getValue(), path, PZERO, p);
        if (ret != WinError.ERROR_SUCCESS) {
            if (Logger.get().isWarn()) {
                Logger.warn("Failed to add PDH Counter: {}, Error code: {}", path,
                        String.format(Formats.formatError(ret)));
            }
            return false;
        }
        return true;
    }

    /**
     * 删除pdh计数器
     *
     * @param p 指向计数器的指针
     * @return 如果成功, 则为true
     */
    public static boolean removeCounter(WinNT.HANDLEByReference p) {
        return WinError.ERROR_SUCCESS == PDH.PdhRemoveCounter(p.getValue());
    }

    /**
     * Encapsulates the three string components of a performance counter
     */
    @Immutable
    public static class PerfCounter {
        private String object;
        private String instance;
        private String counter;

        public PerfCounter(String objectName, String instanceName, String counterName) {
            this.object = objectName;
            this.instance = instanceName;
            this.counter = counterName;
        }

        /**
         * @return 返回对象
         */
        public String getObject() {
            return object;
        }

        /**
         * @return 返回实例
         */
        public String getInstance() {
            return instance;
        }

        /**
         * @return 返回计数器
         */
        public String getCounter() {
            return counter;
        }

        /**
         * 返回此计数器的路径
         *
         * @return 表示计数器路径的字符串
         */
        public String getCounterPath() {
            StringBuilder sb = new StringBuilder();
            sb.append(Symbol.C_BACKSLASH).append(object);
            if (null != instance) {
                sb.append(Symbol.C_PARENTHESE_LEFT).append(instance).append(Symbol.C_PARENTHESE_RIGHT);
            }
            sb.append(Symbol.C_BACKSLASH).append(counter);
            return sb.toString();
        }
    }

}
